package is.idega.idegaweb.egov.tenders.presentation;

import is.idega.idegaweb.egov.cases.presentation.CasesProcessor;
import is.idega.idegaweb.egov.tenders.TendersConstants;

import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.text.Heading1;
import com.idega.presentation.ui.BackButton;
import com.idega.presentation.ui.Form;
import com.idega.user.presentation.user.UsersFilter;
import com.idega.util.StringUtil;

public class CaseSubscribersManager extends Block {

	private String caseId;
	
	@Override
	public void main(IWContext iwc) throws Exception {
		if (StringUtil.isEmpty(caseId)) {
			caseId = iwc.getParameter(CasesProcessor.PARAMETER_CASE_PK);
		}
		IWResourceBundle iwrb = getResourceBundle(iwc);
		if (StringUtil.isEmpty(caseId)) {
			add(new Heading1(iwrb.getLocalizedString("tender_case_manager.case_not_found", "Sorry, case was not found")));
			return;
		}
		
		Form form = new Form();
		form.addParameter(CasesProcessor.PARAMETER_CASE_PK, caseId);
		add(form);
		
		TenderCaseViewer caseViewer = new TenderCaseViewer();
		caseViewer.setActAsStandalone(false);
		caseViewer.setCaseId(caseId);
		form.add(caseViewer);
		
		//	TODO: finish up!
		UsersFilter usersFilter = new UsersFilter();
		form.add(usersFilter);
		
		Layer buttons = new Layer();
		form.add(buttons);
		buttons.setStyleClass("buttonLayer");
		
		BackButton back = new BackButton(iwrb.getLocalizedString("tender_cases.back", "Back"));
		buttons.add(back);
	}

	public String getCaseId() {
		return caseId;
	}

	public void setCaseId(String caseId) {
		this.caseId = caseId;
	}
	
	@Override
	public String getBundleIdentifier() {
		return TendersConstants.IW_BUNDLE_IDENTIFIER;
	}
}
