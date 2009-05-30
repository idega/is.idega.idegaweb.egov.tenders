package is.idega.idegaweb.egov.tenders.bean;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.idega.block.process.presentation.beans.CasePresentation;

/**
 * Simple POJO to store info about BPM case
 * 
 * @author <a href="mailto:valdas@idega.com">Valdas Žemaitis</a>
 * @version $Revision: 1.3 $
 *
 * Last modified: $Date: 2009/05/30 09:33:59 $ by: $Author: valdas $
 */
public class CasePresentationInfo {
	private Long processInstanceId;
	private CasePresentation casePresentation;
	private Timestamp startDate;
	private Timestamp endDate;
	private boolean caseIsPrivate;
	
	private Map<String, String> info;
	
	public CasePresentationInfo(Long processInstanceId) {
		this.processInstanceId = processInstanceId;
	}
	
	public CasePresentationInfo(Long processInstanceId, CasePresentation casePresentation) {
		this(processInstanceId);
		this.casePresentation = casePresentation;
	}

	public Long getProcessInstanceId() {
		return processInstanceId;
	}

	public void setProcessInstanceId(Long processInstanceId) {
		this.processInstanceId = processInstanceId;
	}

	public CasePresentation getCasePresentation() {
		return casePresentation;
	}

	public void setCasePresentation(CasePresentation casePresentation) {
		this.casePresentation = casePresentation;
	}

	public Timestamp getStartDate() {
		return startDate;
	}

	public void setStartDate(Timestamp startDate) {
		this.startDate = startDate;
	}

	public Timestamp getEndDate() {
		return endDate;
	}

	@SuppressWarnings("deprecation")
	public void setEndDate(Timestamp endDate) {
		if (endDate != null) {
			endDate.setHours(23);
			endDate.setMinutes(59);
			endDate.setSeconds(59);
			endDate.setNanos(999999999);
		}
		this.endDate = endDate;
	}
	
	public void addInfo(String key, String value) {
		if (key == null || value == null) {
			Logger.getLogger(getClass().getName()).warning("Can not add info by key: " + key + " and value: " + value);
			return;
		}
		
		if (info == null) {
			info = new HashMap<String, String>();
		}
		
		info.put(key, value);
	}
	
	public String getInfo(String key) {
		if (key == null || info == null) {
			return null;
		}
		
		return info.get(key);
	}
	
	public boolean isEmpty() {
		return info == null ? true : info.isEmpty();
	}

	public boolean isCaseIsPrivate() {
		return caseIsPrivate;
	}

	public void setCaseIsPrivate(boolean caseIsPrivate) {
		this.caseIsPrivate = caseIsPrivate;
	}
	
}
