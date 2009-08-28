package is.idega.idegaweb.egov.tenders.handler;

import is.idega.idegaweb.egov.bpm.cases.CasesBPMProcessConstants;
import is.idega.idegaweb.egov.tenders.TendersConstants;
import is.idega.idegaweb.egov.tenders.bean.TenderApplicationData;
import is.idega.idegaweb.egov.tenders.business.TendersHelper;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jbpm.JbpmContext;
import org.jbpm.JbpmException;
import org.jbpm.graph.def.ActionHandler;
import org.jbpm.graph.exe.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.idega.block.process.data.Case;
import com.idega.block.process.variables.Variable;
import com.idega.block.process.variables.VariableDataType;
import com.idega.idegaweb.IWMainApplication;
import com.idega.idegaweb.IWMainApplicationSettings;
import com.idega.idegaweb.egov.bpm.data.CaseProcInstBind;
import com.idega.idegaweb.egov.bpm.data.dao.CasesBPMDAO;
import com.idega.jbpm.BPMContext;
import com.idega.jbpm.JbpmCallback;
import com.idega.jbpm.exe.BPMFactory;
import com.idega.jbpm.exe.ProcessInstanceW;
import com.idega.jbpm.exe.TaskInstanceW;
import com.idega.jbpm.variables.VariablesHandler;
import com.idega.util.IWTimestamp;
import com.idega.util.StringUtil;

@Service("tenderApplicationHandler")
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class TenderApplicationHandler implements ActionHandler {

	private static final long serialVersionUID = -7289311991826240963L;
	private static final Logger LOGGER = Logger.getLogger(TenderApplicationHandler.class.getName());
	
	private TenderApplicationData tenderData;
	
	private long taskInstanceId;
	
	@Autowired
	private BPMFactory bpmFactory;
	
	@Autowired
	private BPMContext bpmContext;
	
	@Autowired
	private TendersHelper tendersHelper;
	
	@Autowired
	private CasesBPMDAO casesBPMDAO;
	
	@Autowired
	private VariablesHandler variablesHandler;
	
	public void execute(ExecutionContext context) throws Exception {
		TenderApplicationData data = getTenderData();
		if (data == null) {
			LOGGER.warning("No data available!");
			return;
		}
		
		TaskInstanceW currentTask = getBpmFactory().getTaskInstanceW(taskInstanceId);
		if (currentTask == null) {
			LOGGER.warning("Task instance was not found by provided id: " + taskInstanceId);
			return;
		}

		setRealDeadline(currentTask, data);
		Date lastDayToSendBids = (Date) currentTask.getVariable(TendersConstants.TENDER_CASE_END_DATE_VARIABLE);
		IWMainApplicationSettings settings = IWMainApplication.getDefaultIWMainApplication().getSettings();
		addDateVariable(currentTask, TendersConstants.TENDER_CASE_LAST_DATE_FOR_QUESTIONS_VARIABLE, lastDayToSendBids,
				settings.getProperty("tndr_time_frame_make_questions", String.valueOf(7)));
		addDateVariable(currentTask, TendersConstants.TENDER_CASE_LAST_DAY_TO_ANSWER_QUESTIONS_VARIABLE, lastDayToSendBids,
				settings.getProperty("tndr_time_frame_answer_questions", String.valueOf(4)));
		
		if (data.isPaymentCase()) {
			if (!getTendersHelper().disableToSeeAllAttachmentsForNonPayers(currentTask)) {
				LOGGER.warning("Unable to disable seeing attachments of task " + currentTask.getTaskInstance().getName() + " for roles: " +
						TendersConstants.TENDER_CASES_3RD_PARTIES_ROLES);
			}
		}
		
		setIdentifier(currentTask, data);
	}
	
	@Transactional(readOnly=false)
	private void setIdentifier(TaskInstanceW currentTask, TenderApplicationData data) {
		final ProcessInstanceW piw = currentTask.getProcessInstanceW();
		final Long processInstanceId = piw.getProcessInstanceId();
		
		Case theCase = tendersHelper.getCase(processInstanceId);
		if (theCase == null) {
			LOGGER.warning("Unable to set new identifer, case was not found for process: " + processInstanceId);
			return;
		}
		
		String currentIdentifier = theCase.getCaseIdentifier();
		final String identifierFromForm = data.getIdentifier();
		if (StringUtil.isEmpty(identifierFromForm) || identifierFromForm.equals(currentIdentifier)) {
			return;
		}
		
		theCase.setCaseIdentifier(identifierFromForm);
		theCase.store();
		
		CaseProcInstBind bind = getCasesBPMDAO().getCaseProcInstBindByProcessInstanceId(processInstanceId);
		bind.setCaseIdentifier(identifierFromForm);
		getCasesBPMDAO().persist(bind);
		
		String identifierFromProcess = piw.getProcessIdentifier();
		if (StringUtil.isEmpty(identifierFromProcess) || !identifierFromForm.equals(identifierFromProcess)) {
			bpmContext.execute(new JbpmCallback() {
				public Object doInJbpm(JbpmContext context) throws JbpmException {
					try {
						piw.getProcessInstance().getContextInstance().setVariable(CasesBPMProcessConstants.caseIdentifier, identifierFromForm);
						return Boolean.TRUE;
					} catch (Exception e) {
						LOGGER.log(Level.WARNING, "Error setting identifier '"+identifierFromForm+"' for process: " + processInstanceId, e);
						return Boolean.FALSE;
					}
				}
			});
		}
	}
	
	private void setRealDeadline(TaskInstanceW taskInstance, TenderApplicationData data) {
		Object o = taskInstance.getVariable(TendersConstants.TENDER_CASE_END_DATE_VARIABLE);
		if (!(o instanceof Date)) {
			return;
		}
		
		IWTimestamp lastDatToAnswerQuestions = new IWTimestamp((Date) o);
		
		Integer hour = 23;
		Integer minutes = 59;
		Integer seconds = 59;
		Integer milliseconds = 999;
		if (!StringUtil.isEmpty(data.getDeadlineToSendBids())) {
			String[] hourAndMinutes = data.getDeadlineToSendBids().split(":");
			try {
				hour = Integer.valueOf(hourAndMinutes[0]);
			} catch (Exception e) {}
			if (hour > 23) {
				hour = 23;
			} else if (hour < 0) {
				hour = 0;
			}
			
			if (hourAndMinutes.length == 2) {
				try {
					minutes = Integer.valueOf(hourAndMinutes[1]);
				} catch(Exception e) {}
				if (minutes > 59) {
					minutes = 59;
				} else if (minutes < 0) {
					minutes = 0;
				}
			}
			
			seconds = 0;
			milliseconds = 0;
		}
		Variable timeVariable = new Variable(TendersConstants.TENDER_CASE_DEADLINE_TO_SEND_BIDS_VARIABLE, VariableDataType.STRING);
		taskInstance.addVariable(timeVariable, hour + ":" + minutes);
		
		lastDatToAnswerQuestions.setHour(hour);
		lastDatToAnswerQuestions.setMinute(minutes);
		lastDatToAnswerQuestions.setSecond(seconds);
		lastDatToAnswerQuestions.setMilliSecond(milliseconds);
		Variable lastDatToAnswerQuestionsVariable = new Variable(TendersConstants.TENDER_CASE_END_DATE_VARIABLE, VariableDataType.DATE);
		Date realDeadline = new Date(lastDatToAnswerQuestions.getTimestamp().getTime());
		taskInstance.addVariable(lastDatToAnswerQuestionsVariable, realDeadline);
	}
	
	private void addDateVariable(TaskInstanceW taskInstance, String name, Date value, String change) {
		int realChange = 0;
		try {
			realChange = Integer.valueOf(change);
		} catch(Exception e) {
			LOGGER.log(Level.WARNING, "Invalid number: " + change, e);
		}
		
		Variable dateVariable = new Variable(name, VariableDataType.DATE);
		
		IWTimestamp changedTime = new IWTimestamp(value.getTime());
		changedTime.setDay(changedTime.getDay() - realChange);
		Date changedDate = new Date(changedTime.getTimestamp().getTime());
		
		taskInstance.addVariable(dateVariable, changedDate);
	}

	public TenderApplicationData getTenderData() {
		return tenderData;
	}

	public void setTenderData(TenderApplicationData tenderData) {
		this.tenderData = tenderData;
	}

	public BPMFactory getBpmFactory() {
		return bpmFactory;
	}

	public void setBpmFactory(BPMFactory bpmFactory) {
		this.bpmFactory = bpmFactory;
	}

	public long getTaskInstanceId() {
		return taskInstanceId;
	}

	public void setTaskInstanceId(long taskInstanceId) {
		this.taskInstanceId = taskInstanceId;
	}

	public TendersHelper getTendersHelper() {
		return tendersHelper;
	}

	public void setTendersHelper(TendersHelper tendersHelper) {
		this.tendersHelper = tendersHelper;
	}

	public CasesBPMDAO getCasesBPMDAO() {
		return casesBPMDAO;
	}

	public void setCasesBPMDAO(CasesBPMDAO casesBPMDAO) {
		this.casesBPMDAO = casesBPMDAO;
	}

	public VariablesHandler getVariablesHandler() {
		return variablesHandler;
	}

	public void setVariablesHandler(VariablesHandler variablesHandler) {
		this.variablesHandler = variablesHandler;
	}

}
