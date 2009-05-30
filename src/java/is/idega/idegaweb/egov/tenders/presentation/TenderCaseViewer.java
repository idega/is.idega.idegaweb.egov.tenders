package is.idega.idegaweb.egov.tenders.presentation;

import is.idega.idegaweb.egov.application.IWBundleStarter;
import is.idega.idegaweb.egov.cases.presentation.CasesProcessor;
import is.idega.idegaweb.egov.tenders.TendersConstants;
import is.idega.idegaweb.egov.tenders.bean.CasePresentationInfo;
import is.idega.idegaweb.egov.tenders.business.TendersHelper;
import is.idega.idegaweb.egov.tenders.business.TendersSubscriber;

import java.rmi.RemoteException;
import java.util.Arrays;

import javax.faces.component.UIComponent;

import org.springframework.beans.factory.annotation.Autowired;

import com.idega.block.web2.business.JQuery;
import com.idega.block.web2.business.Web2Business;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.Block;
import com.idega.presentation.CSSSpacer;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.text.Heading1;
import com.idega.presentation.ui.BackButton;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.GenericButton;
import com.idega.presentation.ui.InterfaceObject;
import com.idega.presentation.ui.Label;
import com.idega.presentation.ui.TextArea;
import com.idega.presentation.ui.TextInput;
import com.idega.util.CoreConstants;
import com.idega.util.PresentationUtil;
import com.idega.util.expression.ELUtil;

public class TenderCaseViewer extends Block {

	@Autowired
	private TendersHelper tendersHelper;
	
	@Autowired
	private JQuery jQuery;
	
	@Autowired
	private Web2Business web2;

	private String caseId;
	
	private boolean actAsStandalone = true;
	
	@Override
	public void main(IWContext iwc) throws Exception {
		PresentationUtil.addStyleSheetsToHeader(iwc, Arrays.asList(
				iwc.getIWMainApplication().getBundle(IWBundleStarter.IW_BUNDLE_IDENTIFIER).getVirtualPathWithFileNameString("style/application.css"),
				getBundle(iwc).getVirtualPathWithFileNameString("style/tenders.css")
		));
		
		ELUtil.getInstance().autowire(this);
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
			casePK = caseId;
		}
		CasePresentationInfo caseInfo = tendersHelper.getTenderCaseInfo(casePK);
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
				PresentationUtil.addJavaScriptSourcesLinesToHeader(iwc, Arrays.asList(
						CoreConstants.DWR_ENGINE_SCRIPT,
						"/dwr/interface/"+ TendersSubscriber.DWR_OBJECT +".js",
						getBundle(iwc).getVirtualPathWithFileNameString("javascript/TendersHelper.js"),
						jQuery.getBundleURIToJQueryLib(),
						web2.getBundleUriToHumanizedMessagesScript()
				));
				PresentationUtil.addStyleSheetToHeader(iwc, web2.getBundleUriToHumanizedMessagesStyleSheet());
				
				GenericButton subscribe = new GenericButton("subscribe", iwrb.getLocalizedString("tender_cases.subscribe_to_case", "Subscribe to case"));
				buttons.add(subscribe);
				subscribe.setOnClick(new StringBuilder("TendersHelper.subscribe('").append(iwrb.getLocalizedString("subscribing", "Subscribing...")).append("', '")
						.append(casePK.toString()).append("', '").append(iwrb.getLocalizedString("loading", "Loading...")).append("', ")
						.append(caseInfo.getProcessInstanceId()).append(");")
				.toString());
			}
		}
	}
	
	private Layer getLabelAntTextArea(String labelText, String textAreaValue) {
		TextArea textArea = new TextArea("jobDescription", textAreaValue);
		textArea.setDisabled(true);
		
		return getLabelAndInput(labelText, textArea);
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

	public boolean isActAsStandalone() {
		return actAsStandalone;
	}

	public void setActAsStandalone(boolean actAsStandalone) {
		this.actAsStandalone = actAsStandalone;
	}
	
}
