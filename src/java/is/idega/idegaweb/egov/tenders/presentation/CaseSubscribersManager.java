package is.idega.idegaweb.egov.tenders.presentation;

import is.idega.idegaweb.egov.cases.presentation.CasesProcessor;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.faces.component.UIComponent;

import com.idega.block.process.data.Case;
import com.idega.block.process.presentation.UserCases;
import com.idega.block.process.presentation.beans.CasePresentation;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.jbpm.exe.ProcessInstanceW;
import com.idega.presentation.IWContext;
import com.idega.presentation.Layer;
import com.idega.presentation.paging.PagedDataCollection;
import com.idega.presentation.text.Heading1;
import com.idega.presentation.ui.BackButton;
import com.idega.presentation.ui.DropdownMenu;
import com.idega.presentation.ui.Form;
import com.idega.presentation.ui.Label;
import com.idega.presentation.ui.SelectOption;
import com.idega.presentation.ui.SubmitButton;
import com.idega.user.data.User;
import com.idega.user.presentation.user.UsersFilter;
import com.idega.util.ArrayUtil;
import com.idega.util.ListUtil;
import com.idega.util.StringUtil;

public class CaseSubscribersManager extends BasicTenderViewer {

	private static final String SUBSCRIBED_USERS_PARAMETER = "subscribedUserForTheCaseParameter";
	private static final String PAYED_FOR_THE_ATTACHMENTS_USERS_PARAMETER = "payedForTheAttachmentsParameter";
	private static final String PAYMENT_CASE = "paymentTenderCase";
	
	private String saveActionMessage;
	private String savePayersActionMessage;
	
	@Override
	public void main(IWContext iwc) throws Exception {
		super.main(iwc);
		
		switch (parseAction(iwc)) {
			case CasesProcessor.ACTION_VIEW:
				showCaseForm(iwc);
				break;
	
			case CasesProcessor.ACTION_PROCESS:
				if (!resolveCaseId(iwc)) {
					add(new Heading1(getResourceBundle(iwc).getLocalizedString("tender_case_manager.case_not_found", "Sorry, case was not found")));
					return;
				}
				showCaseForm(iwc);
				break;
				
			case CasesProcessor.ACTION_SAVE:
				if (!resolveCaseId(iwc)) {
					add(new Heading1(getResourceBundle(iwc).getLocalizedString("tender_case_manager.case_not_found", "Sorry, case was not found")));
					return;
				}
				saveSubscribers(iwc);
				savePayers(iwc);
				showCaseForm(iwc);
				break;
				
			default:
				showCaseForm(iwc);
				break;
		}
	}
	
	private boolean resolveCaseId(IWContext iwc) {
		if (!StringUtil.isEmpty(getCaseId())) {
			return true;
		}
		
		String caseId = iwc.getParameter(CasesProcessor.PARAMETER_CASE_PK);
		if (StringUtil.isEmpty(caseId)) {
			return false;
		}
		
		setCaseId(caseId);
		return true;
	}
	
	private void saveSubscribers(IWContext iwc) {
		String[] usersIDs = iwc.getParameterValues(SUBSCRIBED_USERS_PARAMETER);
		if (ArrayUtil.isEmpty(usersIDs)) {
			removeAllSubscribers(iwc);
		} else {
			setSubscribers(iwc, usersIDs);
		}
	}
	
	private void savePayers(IWContext iwc) {
		if (!iwc.isParameterSet(PAYMENT_CASE)) {
			return;
		}
		
		String[] usersIDs = iwc.getParameterValues(PAYED_FOR_THE_ATTACHMENTS_USERS_PARAMETER);
		if (ArrayUtil.isEmpty(usersIDs)) {
			removeAllPayers(iwc);
		} else {
			setPayers(iwc, usersIDs);
		}
	}

	private boolean setSubscribers(IWContext iwc, String[] usersIDs) {
		IWResourceBundle iwrb = getResourceBundle(iwc);

		if (!removeAllSubscribers(iwc)) {
			saveActionMessage = iwrb.getLocalizedString("tender_case_manager.unable_to_set_subscribers", "Unable to save: unable to set subscribers");
			return false;
		}
		
		Collection<User> newSubscribers = getUsers(iwc, usersIDs);
		if (ListUtil.isEmpty(newSubscribers)) {
			saveActionMessage = iwrb.getLocalizedString("tender_case_manager.unable_to_set_subscribers", "Unable to save: unable to set subscribers");
			return false;
		}
		
		try {
			Case theCase = getTheCase();
			for (User newSubscriber: newSubscribers) {
				theCase.addSubscriber(newSubscriber);
			}
			theCase.store();
		} catch(Exception e) {
			e.printStackTrace();
			saveActionMessage = iwrb.getLocalizedString("tender_case_manager.unable_to_set_subscribers", "Unable to save: unable to set subscribers");
			return false;
		}
		
		saveActionMessage = iwrb.getLocalizedString("tender_case_manager.subscribers_set_successfully", "Subscribers were set successfully");
		return true;
	}
	
	private boolean setPayers(IWContext iwc, String[] usersIDs) {
		IWResourceBundle iwrb = getResourceBundle(iwc);

		if (!removeAllPayers(iwc)) {
			savePayersActionMessage = iwrb.getLocalizedString("tender_case_manager.unable_to_set_payers", "Unable to save: unable to set payers");
			return false;
		}
		
		Collection<User> newPayers = getUsers(iwc, usersIDs);
		if (ListUtil.isEmpty(newPayers)) {
			savePayersActionMessage = iwrb.getLocalizedString("tender_case_manager.unable_to_set_payers", "Unable to save: unable to set payers");
			return false;
		}
		
		String caseId = iwc.getParameter(CasesProcessor.PARAMETER_CASE_PK);
		String metaDataKey = getTendersHelper().getMetaDataKey(caseId);
		ProcessInstanceW processInstance = getTendersHelper().getProcessInstance(caseId);
		try {
			for (User newPayer: newPayers) {
				newPayer.setMetaData(metaDataKey, Boolean.TRUE.toString());
				newPayer.store();
				
				if (!getTendersHelper().enableToSeeAllAttachmentsForUser(processInstance, newPayer)) {
					savePayersActionMessage = iwrb.getLocalizedString("tender_case_manager.unable_to_set_payers", "Unable to save: unable to set payers");
					return false;
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			savePayersActionMessage = iwrb.getLocalizedString("tender_case_manager.unable_to_set_payers", "Unable to save: unable to set payers");
			return false;
		}
		
		savePayersActionMessage = iwrb.getLocalizedString("tender_case_manager.payers_set_successfully", "Payers were set successfully");
		return true;
	}
	
	@SuppressWarnings("unchecked")
	private Collection<User> getUsers(IWApplicationContext iwac, String[] ids) {
		try {
			return getGroupHelper().getUserBusiness(iwac).getUsers(ids);
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private Collection<User> doPrepareForRemove(IWContext iwc) {
		IWResourceBundle iwrb = getResourceBundle(iwc);
		
		Case theCase = getTheCase();
		if (theCase == null) {
			saveActionMessage = iwrb.getLocalizedString("tender_case_manager.case_not_found", "Unable to save, case was not found");
			return null;
		}
		
		return theCase.getSubscribers();
	}
	
	private boolean removeAllSubscribers(IWContext iwc) {
		IWResourceBundle iwrb = getResourceBundle(iwc);
		
		Collection<User> subscribers = doPrepareForRemove(iwc);
		if (ListUtil.isEmpty(subscribers)) {
			return true;
		}
		
		Case theCase = getTheCase();
		try {
			for (User subscriber: subscribers) {
				theCase.removeSubscriber(subscriber);
			}
			theCase.store();
		} catch(Exception e) {
			e.printStackTrace();
			saveActionMessage = iwrb.getLocalizedString("tender_case_manager.unable_to_remove_subscribers", "Unable to save: unable to remove subscribers");
			return false;
		}
		
		saveActionMessage = iwrb.getLocalizedString("tender_case_manager.subscribers_removed_successfully", "All subscribers were removed successfully");
		return true;
	}
	
	private boolean removeAllPayers(IWContext iwc) {
		IWResourceBundle iwrb = getResourceBundle(iwc);
		
		Collection<User> payers = getTendersHelper().getPayers(getCaseId());
		if (ListUtil.isEmpty(payers)) {
			return true;
		}
		
		String caseId = iwc.getParameter(CasesProcessor.PARAMETER_CASE_PK);
		String metaDataKey = getTendersHelper().getMetaDataKey(caseId);
		ProcessInstanceW processInstance = getTendersHelper().getProcessInstance(caseId);
		try {
			for (User payer: payers) {
				String metaData = payer.getMetaData(metaDataKey);
				if (!StringUtil.isEmpty(metaData) && Boolean.TRUE.toString().equals(metaData)) {
					payer.removeMetaData(metaDataKey);
					payer.store();
					
					if (!getTendersHelper().disableToSeeAllAttachmentsForUser(processInstance, payer)) {
						savePayersActionMessage = iwrb.getLocalizedString("tender_case_manager.unable_to_remove_payers",
								"Unable to save: unable to remove payers");
						return false;
					}
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
			savePayersActionMessage = iwrb.getLocalizedString("tender_case_manager.unable_to_remove_payers", "Unable to save: unable to remove payers");
			return false;
		}
		
		savePayersActionMessage = iwrb.getLocalizedString("tender_case_manager.payers_removed_successfully", "All payers were removed successfully");
		return true;
	}
	
	private DropdownMenu getCasesToManage(IWContext iwc) {
		DropdownMenu menu = new DropdownMenu();
		
		PagedDataCollection<CasePresentation> cases = getTendersHelper().getAllCases(iwc.getCurrentLocale(), null, null);
		if (cases == null || ListUtil.isEmpty(cases.getCollection())) {
			return menu;
		}
		
		Collection<CasePresentation> validCases = getTendersHelper().getValidTendersCases(cases.getCollection(), iwc.isLoggedOn() ? iwc.getCurrentUser() : null,
				iwc.getCurrentLocale());
		if (ListUtil.isEmpty(validCases)) {
			return menu;
		}
		
		for (CasePresentation theCase: validCases) {
			menu.addOption(new SelectOption(theCase.getSubject(), theCase.getId()));
		}
		
		String caseId = null;
		if (iwc.isParameterSet(CasesProcessor.PARAMETER_CASE_PK)) {
			caseId = iwc.getParameter(CasesProcessor.PARAMETER_CASE_PK);
		} else {
			caseId = cases.getCollection().iterator().next().getId();
		}
		
		menu.setSelectedElement(caseId);
		setCaseId(caseId);
		
		menu.setOnChange(new StringBuilder("changeValue(this.form['").append(CasesProcessor.PARAMETER_CASE_PK).append("'], dwr.util.getValue('")
				.append(menu.getId()).append("'));this.form.submit();").toString());
		
		return menu;
	}
	
	private void showCaseForm(IWContext iwc) {
		IWResourceBundle iwrb = getResourceBundle(iwc);
		
		Form form = new Form();
		add(form);
		
		if (saveActionMessage != null) {
			form.add(new Heading1(saveActionMessage));
		}
		if (savePayersActionMessage != null) {
			form.add(new Heading1(savePayersActionMessage));
		}
		
		DropdownMenu casesMenu = getCasesToManage(iwc);
		
		if (StringUtil.isEmpty(getCaseId())) {
			form.add(new Heading1(iwrb.getLocalizedString("tender_case_manager.there_are_no_valid_cases_available", "There are no valid cases to manage")));
			return;
		}
		
		form.addParameter(CasesProcessor.PARAMETER_CASE_PK, getCaseId());
		
		Layer casesDropdownContainer = new Layer();
		form.add(casesDropdownContainer);
		casesDropdownContainer.setStyleClass("formItem");
		Label casesChooserLabel = new Label(iwrb.getLocalizedString("tender_case_manager.select_case", "Select case"), casesMenu);
		casesDropdownContainer.add(casesChooserLabel);
		casesDropdownContainer.add(casesMenu);
		
		//	Case view
		TenderCaseViewer caseViewer = new TenderCaseViewer();
		caseViewer.setActAsStandalone(false);
		caseViewer.setCaseId(getCaseId());
		form.add(caseViewer);
		
		//	Subscribed users
		addUsersFilter(form, getSubscribersIds(iwc), iwrb.getLocalizedString("tender_case_manager.subscribe_users_to_case", "Set subscribed users"),
				SUBSCRIBED_USERS_PARAMETER);
		
		if (getCaseInfo().isPaymentCase()) {
			form.addParameter(PAYMENT_CASE, Boolean.TRUE.toString());
			
			//	Payed for attachments users
			addUsersFilter(form, getPayersIds(iwc),
					iwrb.getLocalizedString("tender_case_manager.mark_payed_users_for_attachments", "Mark payed users for the documents"),
					PAYED_FOR_THE_ATTACHMENTS_USERS_PARAMETER);
		}
		
		//	Buttons
		Layer buttons = new Layer();
		form.add(buttons);
		buttons.setStyleClass("buttonLayer");
		
		BackButton back = new BackButton(iwrb.getLocalizedString("tender_cases.back", "Back"));
		buttons.add(back);
		
		SubmitButton save = new SubmitButton(iwrb.getLocalizedString("tender_cases.save_subscriders", "Save"), UserCases.PARAMETER_ACTION,
				String.valueOf(CasesProcessor.ACTION_SAVE));
		buttons.add(save);
	}
	
	private void addUsersFilter(UIComponent container, List<String> selectedUsers, String label, String selectedUsersInputName) {
		Layer usersFilterContainer = new Layer();
		container.getChildren().add(usersFilterContainer);
		usersFilterContainer.setStyleClass("formItem");
		
		UsersFilter usersFilter = new UsersFilter();
		usersFilter.setAddLabel(false);
		usersFilter.setSelectedUsers(selectedUsers);
		usersFilter.setSelectedUserInputName(selectedUsersInputName);
		Label usersFilterLabel = new Label(label, usersFilter);
		usersFilterContainer.add(usersFilterLabel);
		usersFilterContainer.add(usersFilter);
	}
	
	private Collection<User> getSubscribers(IWContext iwc) {
		Case theCase = getTheCase();
		if (theCase == null) {
			return null;
		}
		
		return theCase.getSubscribers();
	}
	
	private List<String> getSubscribersIds(IWContext iwc) {
		Collection<User> subscribers = getSubscribers(iwc);
		if (ListUtil.isEmpty(subscribers)) {
			return null;
		}
		
		List<String> ids = new ArrayList<String>(subscribers.size());
		for (User subscriber: subscribers) {
			ids.add(subscriber.getId());
		}
		return ids;
	}
	
	private List<String> getPayersIds(IWContext iwc) {
		Collection<User> payers = getTendersHelper().getPayers(getCaseId());
		if (ListUtil.isEmpty(payers)) {
			return null;
		}
		
		List<String> ids = new ArrayList<String>(payers.size());
		for (User payer: payers) {
			ids.add(payer.getId());
		}
		return ids;
	}
	
	private int parseAction(IWContext iwc) {
		if (iwc.isParameterSet(UserCases.PARAMETER_ACTION)) {
			return Integer.parseInt(iwc.getParameter(UserCases.PARAMETER_ACTION));
		}
		return CasesProcessor.ACTION_VIEW;
	}

}
