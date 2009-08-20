package is.idega.idegaweb.egov.tenders.presentation;

import is.idega.idegaweb.egov.cases.presentation.CasesProcessor;
import is.idega.idegaweb.egov.tenders.TendersConstants;
import is.idega.idegaweb.egov.tenders.bean.CasePresentationInfo;
import is.idega.idegaweb.egov.tenders.business.TendersSubscriber;

import java.rmi.RemoteException;
import java.util.Arrays;

import javax.faces.component.UIComponent;

import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.CSSSpacer;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.Span;
import com.idega.presentation.text.Heading1;
import com.idega.presentation.ui.BackButton;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.GenericButton;
import com.idega.presentation.ui.InterfaceObject;
import com.idega.presentation.ui.Label;
import com.idega.presentation.ui.TextInput;
import com.idega.util.CoreConstants;
import com.idega.util.PresentationUtil;
import com.idega.util.StringUtil;
import com.idega.util.text.TextSoap;

public class TenderCaseViewer extends BasicTenderViewer {
	
	private boolean actAsStandalone = true;
	
	@Override
	public void main(IWContext iwc) throws Exception {
		super.main(iwc);
		
		showProcessor(iwc, iwc.getParameter(CasesProcessor.PARAMETER_CASE_PK));
	}
	
	private void showProcessor(IWContext iwc, Object casePK) throws RemoteException {
		UIComponent container = isActAsStandalone() ? new Form() : new Layer();
		add(container);
		
		Layer formContainer = new Layer();
		container.getChildren().add(formContainer);
		formContainer.setStyleClass("formSection");
		
		IWResourceBundle iwrb = getResourceBundle(iwc);
		
		if (casePK == null) {
			casePK = getCaseId();
		} else {
			setCaseId(casePK.toString());
		}
		
		CasePresentationInfo caseInfo = getCaseInfo();
		if (caseInfo == null || caseInfo.isEmpty()) {
			formContainer.add(new Heading1(iwrb.getLocalizedString("tender_cases.sorry_case_not_found", "Selected case was not found.")));
			return;
		}
		
		//	Heading
		formContainer.add(getLabelAndInput(iwrb.getLocalizedString("tender_cases.tender_case_heading", "Tender name"),
				caseInfo.getInfo(TendersConstants.TENDER_CASE_TENDER_NAME_VARIABLE)));
		
		//	Issuer
		formContainer.add(getLabelAndInput(iwrb.getLocalizedString("tender_cases.tender_case_issuer", "Who is issuing the tender"),
				caseInfo.getInfo(TendersConstants.TENDER_CASE_TENDER_ISSUER_VARIABLE)));
		
		//	Description
		formContainer.add(getLabelAntTextArea(iwrb.getLocalizedString("tender_cases.tender_case_job_description", "Description of job"),
				caseInfo.getInfo(TendersConstants.TENDER_CASE_JOB_DESCRIPTION_VARIABLE)));
		
		if (isActAsStandalone()) {
			Layer buttons = new Layer();
			formContainer.add(buttons);
			buttons.setStyleClass("buttonLayer");
			
			BackButton back = new BackButton(iwrb.getLocalizedString("tender_cases.back", "Back"));
			buttons.add(back);
			
			if (iwc.isLoggedOn()) {
				GenericButton actionButton = null;
				
				if (iwc.hasRole(TendersConstants.TENDER_CASES_HANDLER_ROLE)) {
					actionButton = new GenericButton("viewCase", iwrb.getLocalizedString("tender_cases.view_case", "View case"));
					actionButton.setOnClick(new StringBuilder("window.location.href='")
						.append(getTendersHelper().getLinkToSubscribedCase(iwc, iwc.getCurrentUser(), casePK.toString())).append("';").toString());
				} else {
					PresentationUtil.addJavaScriptSourcesLinesToHeader(iwc, Arrays.asList(
							CoreConstants.DWR_ENGINE_SCRIPT,
							"/dwr/interface/"+ TendersSubscriber.DWR_OBJECT +".js",
							getBundle(iwc).getVirtualPathWithFileNameString("javascript/TendersHelper.js"),
							getJQuery().getBundleURIToJQueryLib(),
							getWeb2().getBundleUriToHumanizedMessagesScript()
					));
					PresentationUtil.addStyleSheetToHeader(iwc, getWeb2().getBundleUriToHumanizedMessagesStyleSheet());
					
					actionButton = new GenericButton("subscribe", iwrb.getLocalizedString("tender_cases.subscribe_to_case", "Subscribe to case"));
					actionButton.setOnClick(new StringBuilder("TendersHelper.subscribe('").append(iwrb.getLocalizedString("subscribing", "Subscribing..."))
							.append("', '").append(casePK.toString()).append("', '").append(iwrb.getLocalizedString("loading", "Loading...")).append("', ")
							.append(caseInfo.getProcessInstanceId()).append(");")
					.toString());
				}

				buttons.add(actionButton);
			}
		}
	}
	
	private Layer getLabelAntTextArea(String labelText, String textAreaValue) {
		Span description = new Span();
		
		if (!StringUtil.isEmpty(textAreaValue)) {
			textAreaValue = TextSoap.formatText(textAreaValue);
		}
		description.addText(textAreaValue);
		
		Label label = new Label();
		label.add(labelText);
		label.setFor(description.getId());
		
		Layer container = new Layer();
		container.setStyleClass("formItem");
		container.add(label);
		container.add(description);
		container.add(new CSSSpacer());
		
		return container;
	}
	
	private Layer getLabelAndInput(String labelText, String inputValue) {
		TextInput text = new TextInput();
		text.setContent(inputValue);
		text.setDisabled(true);
		
		return getLabelAndInput(labelText, text);
	}
	
	private Layer getLabelAndInput(String labelText, InterfaceObject interfaceObject) {
		Label label = new Label(labelText, interfaceObject);
		
		Layer container = new Layer();
		container.setStyleClass("formItem");
		container.add(label);
		container.add(interfaceObject);
		container.add(new CSSSpacer());
		
		return container;
	}

	public boolean isActAsStandalone() {
		return actAsStandalone;
	}

	public void setActAsStandalone(boolean actAsStandalone) {
		this.actAsStandalone = actAsStandalone;
	}
	
}
