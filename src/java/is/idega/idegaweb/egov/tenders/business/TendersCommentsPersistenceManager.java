package is.idega.idegaweb.egov.tenders.business;

import is.idega.idegaweb.egov.bpm.business.BPMCommentsPersistenceManager;
import is.idega.idegaweb.egov.tenders.TendersConstants;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.idega.block.article.bean.CommentsViewerProperties;
import com.idega.block.process.data.Case;
import com.idega.jbpm.exe.ProcessInstanceW;
import com.idega.jbpm.exe.TaskInstanceW;
import com.idega.user.data.User;
import com.idega.util.ListUtil;

@Scope(BeanDefinition.SCOPE_SINGLETON)
@Service(TendersCommentsPersistenceManager.BEAN_IDENTIFIER)
public class TendersCommentsPersistenceManager extends BPMCommentsPersistenceManager {

	public static final String BEAN_IDENTIFIER = "tendersCommentsPersistenceManager";
	private static final Logger LOGGER = Logger.getLogger(TendersCommentsPersistenceManager.class.getName());
	
	@Autowired
	private TendersHelper tendersHelper;
	
	@Override
	@Transactional(readOnly = true)
	public boolean useFilesUploader(CommentsViewerProperties properties) {
		if (properties == null) {
			return false;
		}
		
		ProcessInstanceW piw = null;
		try {
			piw = getProcessInstance(properties.getIdentifier());
		} catch(Exception e) {
			LOGGER.log(Level.WARNING, "Error getting process instance: " + properties.getIdentifier());
		}
		if (piw == null) {
			return false;
		}
		
		TaskInstanceW taskInstanceW = null;
		try {
			taskInstanceW = getSubmittedTaskInstance(piw, getTaskNameForAttachments());
		} catch(Exception e) {
			LOGGER.log(Level.WARNING, "Error getting task: " + getTaskNameForAttachments() + " from process: " +  piw.getProcessInstanceId(), e);
		}
		if (taskInstanceW == null) {
			LOGGER.warning("Task was not found: " + getTaskNameForAttachments());
			return false;
		}
		
		Object o = null;
		try {
			o = taskInstanceW.getVariable(TendersConstants.TENDER_CASE_LAST_DATE_FOR_QUESTIONS_VARIABLE);
		} catch(Exception e) {
			LOGGER.log(Level.WARNING, "Error getting variable: " + TendersConstants.TENDER_CASE_LAST_DATE_FOR_QUESTIONS_VARIABLE + " from task: " +
					taskInstanceW.getTaskInstanceId(), e);
		}
		if (!(o instanceof Timestamp)) {
			return false;
		}
		
		Timestamp lastDayForQuestions = (Timestamp) o;		
		Timestamp currentTime = new Timestamp(System.currentTimeMillis());
		return !currentTime.after(lastDayForQuestions);
	}

	@Override
	public String getTaskNameForAttachments() {
		return "Register New Tender";
	}
	
	@Override
	public Object addComment(CommentsViewerProperties properties) {
		Object commentId = super.addComment(properties);
		if (commentId == null) {
			return null;
		}
		
		if (!hasFullRightsForComments(properties.getIdentifier())) {
			return commentId;	//	Comment's attachment wasn't attached to process
		}
		
		ProcessInstanceW processInstance = getTendersHelper().getProcessInstance(Long.valueOf(properties.getIdentifier()));
	
		TaskInstanceW taskInstance = processInstance.getStartTaskInstance();
		Object paymentVariable = taskInstance.getVariable(TendersConstants.TENDER_CASE_IS_PAYMENT_VARIABLE);
		if (paymentVariable == null || !Boolean.TRUE.toString().equals(paymentVariable.toString())) {
			return commentId;	//	Not "payment" case, no need to enable/disable attachments
		}
		
		//	Disabling ALL attachments for ALL users
		getTendersHelper().disableToSeeAllAttachments(processInstance.getStartTaskInstance());
		
		//	Enabling new attachments for payers
		Case theCase = getTendersHelper().getCase(processInstance.getProcessInstanceId());
		Collection<User> payers = getTendersHelper().getPayers(theCase.getPrimaryKey().toString());
		if (ListUtil.isEmpty(payers)) {
			return commentId;
		}
		
		for (User payer: payers) {
			if (!getTendersHelper().enableToSeeAllAttachmentsForUser(processInstance, payer)) {
				return null;
			}
		}
		
		return commentId;
	}

	public TendersHelper getTendersHelper() {
		return tendersHelper;
	}

	public void setTendersHelper(TendersHelper tendersHelper) {
		this.tendersHelper = tendersHelper;
	}

	@Override
	public boolean isNotificationsAutoEnabled(CommentsViewerProperties properties) {
		return properties == null ? false : true;
	}
	
}
