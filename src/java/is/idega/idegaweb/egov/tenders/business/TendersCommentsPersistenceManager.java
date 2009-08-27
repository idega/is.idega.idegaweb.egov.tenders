package is.idega.idegaweb.egov.tenders.business;

import is.idega.idegaweb.egov.bpm.business.BPMCommentsPersistenceManager;
import is.idega.idegaweb.egov.tenders.TendersConstants;

import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.idega.block.article.bean.CommentsViewerProperties;
import com.idega.block.article.data.Comment;
import com.idega.block.process.data.Case;
import com.idega.jbpm.exe.ProcessInstanceW;
import com.idega.jbpm.exe.TaskInstanceW;
import com.idega.user.data.User;
import com.idega.util.IWTimestamp;
import com.idega.util.ListUtil;
import com.idega.util.StringUtil;

@Scope(BeanDefinition.SCOPE_SINGLETON)
@Service(TendersCommentsPersistenceManager.BEAN_IDENTIFIER)
public class TendersCommentsPersistenceManager extends BPMCommentsPersistenceManager {

	public static final String BEAN_IDENTIFIER = "tendersCommentsPersistenceManager";
	private static final Logger LOGGER = Logger.getLogger(TendersCommentsPersistenceManager.class.getName());
	
	@Autowired
	private TendersHelper tendersHelper;
	
	@Transactional(readOnly = true)
	private boolean checkDeadlineForVariable(TaskInstanceW taskInstanceW, String variable) {
		Object o = null;
		try {
			o = taskInstanceW.getVariable(variable);
		} catch(Exception e) {
			LOGGER.log(Level.WARNING, "Error getting variable: " + variable + " from task: " + taskInstanceW.getTaskInstanceId(), e);
		}
		if (!(o instanceof Timestamp)) {
			LOGGER.warning("Expected Timestamp object, got: " + o + " for variable: " + variable);
			return false;
		}
		
		Timestamp deadline = (Timestamp) o;		
		Timestamp currentTime = new Timestamp(System.currentTimeMillis());
		boolean stillOntime = !currentTime.after(deadline);
		if (!stillOntime) {
			LOGGER.info("Deadline reached! Last date was: " + new IWTimestamp(deadline));
			return Boolean.FALSE;
		}
		return stillOntime;
	}
	
	@Override
	@Transactional(readOnly = true)
	public boolean isCommentsCreationEnabled(CommentsViewerProperties properties) {
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
		
		if (hasFullRightsForComments(piw.getProcessInstanceId())) {
			return checkDeadlineForVariable(taskInstanceW, TendersConstants.TENDER_CASE_LAST_DAY_TO_ANSWER_QUESTIONS_VARIABLE);
		}
		
		return checkDeadlineForVariable(taskInstanceW, TendersConstants.TENDER_CASE_LAST_DATE_FOR_QUESTIONS_VARIABLE);
	}

	@Override
	public boolean canWriteComments(CommentsViewerProperties properties) {
		return isCommentsCreationEnabled(properties);
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
		
		ProcessInstanceW processInstance = getTendersHelper().getProcessInstanceW(Long.valueOf(properties.getIdentifier()));
	
		TaskInstanceW taskInstance = getSubmittedTaskInstance(processInstance, getTaskNameForAttachments());
		Object paymentVariable = taskInstance.getVariable(TendersConstants.TENDER_CASE_IS_PAYMENT_VARIABLE);
		if (paymentVariable == null || !Boolean.TRUE.toString().equals(paymentVariable.toString())) {
			return commentId;	//	Not "payment" case, no need to enable/disable attachments
		}
		
		//	Disabling ALL attachments for ALL users
		getTendersHelper().disableToSeeAllAttachments(processInstance);
		
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

	@Override
	public List<String> getPersonsToNotifyAboutComment(CommentsViewerProperties properties, Object commentId, boolean justPublished) {
		if (properties == null || commentId == null) {
			return null;
		}
	
		Comment comment = getComment(commentId);
		if (comment == null) {
			return null;
		}
		
		List<String> emails = null;
		if (properties.getReplyForComment() == null ? false : properties.getReplyForComment() < 0 ? false : true) {
			//	Reply to comment: notifying only author
			Comment originalComment = getComment(properties.getReplyForComment());
			if (originalComment == null) {
				return null;
			}
			
			String authorEmail = getUserMail(originalComment.getAuthorId());
			if (StringUtil.isEmpty(authorEmail)) {
				return null;
			}
			
			List<String> allSubscribers = getAllFeedSubscribers(properties.getIdentifier(), null);
			if (ListUtil.isEmpty(allSubscribers)) {
				return null;
			}
			
			emails = allSubscribers.contains(authorEmail) ? Arrays.asList(authorEmail) : null;
		} else if (comment.isPrivateComment() && !justPublished) {
			//	Private comment was created: notifying handlers
			emails = getEmails(getCaseHandlers(properties.getIdentifier()));
		} else if (justPublished) {
			//	Private comment was published: notifying everybody except author and handlers
			List<String> subscribersEmails = getAllFeedSubscribers(properties.getIdentifier(), comment.getAuthorId());
			if (ListUtil.isEmpty(subscribersEmails)) {
				return null;
			}
			
			List<String> handlersEmails = getEmails(getCaseHandlers(properties.getIdentifier()));
			if (ListUtil.isEmpty(handlersEmails)) {
				return subscribersEmails;
			}
			
			subscribersEmails.removeAll(handlersEmails);
			emails = subscribersEmails;
		} else {
			//	Public comment, notifying all "subscribers" except author
			emails = getAllFeedSubscribers(properties.getIdentifier(), comment.getAuthorId());
		}
		
		return emails;
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

	@Override
	public String getHandlerRoleKey() {
		return TendersConstants.TENDER_CASES_HANDLER_ROLE;
	}
	
}
