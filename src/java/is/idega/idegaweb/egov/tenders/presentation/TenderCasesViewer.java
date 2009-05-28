package is.idega.idegaweb.egov.tenders.presentation;

import is.idega.idegaweb.egov.cases.presentation.OpenCases;
import is.idega.idegaweb.egov.tenders.TendersConstants;
import is.idega.idegaweb.egov.tenders.bean.CasePresentationInfo;
import is.idega.idegaweb.egov.tenders.business.TendersHelper;
import is.idega.idegaweb.egov.tenders.business.TendersSubscriber;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import com.idega.block.process.presentation.UserCases;
import com.idega.block.process.presentation.beans.CasePresentation;
import com.idega.block.web2.business.JQuery;
import com.idega.block.web2.business.Web2Business;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.CSSSpacer;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.paging.PagedDataCollection;
import com.idega.presentation.text.Heading1;
import com.idega.presentation.text.Link;
import com.idega.presentation.text.ListItem;
import com.idega.presentation.text.Lists;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.GenericButton;
import com.idega.presentation.ui.InterfaceObject;
import com.idega.presentation.ui.Label;
import com.idega.presentation.ui.SubmitButton;
import com.idega.presentation.ui.TextArea;
import com.idega.presentation.ui.TextInput;
import com.idega.user.data.User;
import com.idega.util.CoreConstants;
import com.idega.util.ListUtil;
import com.idega.util.PresentationUtil;
import com.idega.util.expression.ELUtil;

/**
 * Viewer filters tenders cases
 * 
 * @author <a href="mailto:valdas@idega.com">Valdas Žemaitis</a>
 * @version $Revision: 1.4 $
 *
 * Last modified: $Date: 2009/05/28 12:59:35 $ by: $Author: valdas $
 */
public class TenderCasesViewer extends OpenCases {
	
	@Autowired
	private TendersHelper tendersHelper;
	
	@Autowired
	private JQuery jQuery;
	
	@Autowired
	private Web2Business web2;
	
	@Override
	protected void present(IWContext iwc) throws Exception {
		ELUtil.getInstance().autowire(this);
	}
	
	@Override
	protected void display(IWContext iwc) throws Exception {
		PresentationUtil.addStyleSheetToHeader(iwc, getBundle().getVirtualPathWithFileNameString("style/tenders.css"));
		
		if (parseAction(iwc) == ACTION_VIEW) {
			Layer container = new Layer();
			add(container);
			container.setStyleClass("currentTendersCasesViewer");
			
			PagedDataCollection<CasePresentation> cases = tendersHelper.getAllCases(iwc.getCurrentLocale(), getCaseStatusesToHide(), getCaseStatusesToShow());
			if (cases == null || ListUtil.isEmpty(cases.getCollection())) {
				container.add(new Heading1(getResourceBundle(iwc).getLocalizedString("tender_cases.no_cases_available", "There are no cases currently")));
				return;
			}
			
			Collection<CasePresentation> validCases = tendersHelper.getValidTendersCases(cases.getCollection());
			if (ListUtil.isEmpty(validCases)) {
				container.add(new Heading1(getResourceBundle(iwc).getLocalizedString("tender_cases.no_cases_available", "There are no cases currently")));
				return;
			}
			
			User currentUser = iwc.isLoggedOn() ? iwc.getCurrentUser() : null;
			Lists list = new Lists();
			container.add(list);
			for (CasePresentation theCase: validCases) {
				ListItem item = new ListItem();
				list.add(item);
				
				item.add(getLinkToCase(iwc, theCase, currentUser));
			}
			
			return;
		}
		
		super.display(iwc);
	}
	
	@Override
	protected void showProcessor(IWContext iwc, Object casePK) throws RemoteException {
		Form form = new Form();
		add(form);
		
		Layer container = new Layer();
		form.add(container);
		container.setStyleClass("tenderCaseViewerWithNoLogin formSection");
		
		IWResourceBundle iwrb = getResourceBundle(iwc);
		
		CasePresentationInfo caseInfo = tendersHelper.getTenderCaseInfo(casePK);
		if (caseInfo == null || caseInfo.isEmpty()) {
			container.add(new Heading1(iwrb.getLocalizedString("tender_cases.sorry_case_not_found", "Selected case was not found.")));
			return;
		}
		
		//	Heading
		container.add(getLabelAndInput(iwrb.getLocalizedString("tender_cases.tender_case_heading", "Tender name"),
				caseInfo.getInfo(TendersConstants.TENDER_CASE_TENDER_NAME_VARIABLE)));
		
		//	Issuer
		container.add(getLabelAndInput(iwrb.getLocalizedString("tender_cases.tender_case_issuer", "Who is issuing the tender"),
				caseInfo.getInfo(TendersConstants.TENDER_CASE_TENDER_ISSUER_VARIABLE)));
		
		//	Description
		container.add(getLabelAntTextArea(iwrb.getLocalizedString("tender_cases.tender_case_job_description", "Description of job"),
				caseInfo.getInfo(TendersConstants.TENDER_CASE_JOB_DESCRIPTION_VARIABLE)));
		
		Layer buttons = new Layer();
		container.add(buttons);
		buttons.setStyleClass("buttonLayer");
		
		SubmitButton back = new SubmitButton(iwrb.getLocalizedString("tender_cases.back", "Back"));
		back.addParameter(UserCases.PARAMETER_ACTION, String.valueOf(ACTION_VIEW));
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
	
	private Link getLinkToCase(IWContext iwc, CasePresentation theCase, User currentUser) {
		Link link = new Link(theCase.getSubject());
		
		if (tendersHelper.isSubscribed(iwc, currentUser, theCase.getId())) {
			link.setURL(tendersHelper.getLinkToSubscribedCase(iwc, currentUser, theCase.getId()));
		} else {
			link.addParameter(PARAMETER_CASE_PK, theCase.getId());
			if (currentUser == null) {
				link.addParameter(UserCases.PARAMETER_ACTION, String.valueOf(ACTION_PROCESS));
			}
			else {
				link.addParameter(TendersConstants.SPECIAL_TENDER_CASE_PAGE_REDIRECTOR_REQUESTED, Boolean.TRUE.toString());
			}
		}
		
		return link;
	}

	@Override
	public String getBundleIdentifier() {
		return TendersConstants.IW_BUNDLE_IDENTIFIER;
	}
}
