package is.idega.idegaweb.egov.tenders.handler;

import is.idega.idegaweb.egov.tenders.TendersConstants;
import is.idega.idegaweb.egov.tenders.bean.TenderApplicationData;
import is.idega.idegaweb.egov.tenders.business.TendersHelper;
import java.util.Date;
import java.util.logging.Logger;

import org.jbpm.graph.def.ActionHandler;
import org.jbpm.graph.exe.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import com.idega.block.process.variables.Variable;
import com.idega.block.process.variables.VariableDataType;
import com.idega.idegaweb.egov.bpm.data.dao.CasesBPMDAO;
import com.idega.jbpm.exe.BPMFactory;
import com.idega.jbpm.exe.TaskInstanceW;
import com.idega.jbpm.variables.VariablesHandler;
import com.idega.util.IWTimestamp;

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

		addDateVariable(currentTask, TendersConstants.TENDER_CASE_LAST_DATE_FOR_QUESTIONS_VARIABLE, data.getLastDayToSendBids(), -7);
		addDateVariable(currentTask, TendersConstants.TENDER_CASE_LAST_DAY_TO_ANSWER_QUESTIONS_VARIABLE, data.getLastDayToSendBids(), -4);
		
		if (data.isPaymentCase()) {
			if (!getTendersHelper().disableToSeeAllAttachmentsForNonPayers(currentTask)) {
				LOGGER.warning("Unable to disable seeing attachments of task " + currentTask.getTaskInstance().getName() + " for roles: " +
						TendersConstants.TENDER_CASES_3RD_PARTIES_ROLES);
			}
		}
	}
	
	private void addDateVariable(TaskInstanceW taskInstance, String name, Date value, int change) {
		Variable dateVariable = new Variable(name, VariableDataType.DATE);
		
		IWTimestamp changedTime = new IWTimestamp(value.getTime());
		changedTime.setDay(changedTime.getDay() + change);
		changedTime.setHour(23);
		changedTime.setMinute(59);
		changedTime.setSecond(59);
		changedTime.setMilliSecond(999);
		
		value = new Date(changedTime.getTimestamp().getTime());
		
		taskInstance.addVariable(dateVariable, value);
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
