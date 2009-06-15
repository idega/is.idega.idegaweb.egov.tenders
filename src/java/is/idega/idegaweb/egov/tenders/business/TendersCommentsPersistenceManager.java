package is.idega.idegaweb.egov.tenders.business;

import is.idega.idegaweb.egov.bpm.business.BPMCommentsPersistenceManager;
import is.idega.idegaweb.egov.tenders.TendersConstants;

import java.sql.Timestamp;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.idega.block.article.bean.CommentsViewerProperties;
import com.idega.jbpm.exe.ProcessInstanceW;
import com.idega.jbpm.exe.TaskInstanceW;

@Scope(BeanDefinition.SCOPE_SINGLETON)
@Service(TendersCommentsPersistenceManager.BEAN_IDENTIFIER)
public class TendersCommentsPersistenceManager extends BPMCommentsPersistenceManager {

	public static final String BEAN_IDENTIFIER = "tendersCommentsPersistenceManager";
	private static final Logger LOGGER = Logger.getLogger(TendersCommentsPersistenceManager.class.getName());
	
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
	
}
