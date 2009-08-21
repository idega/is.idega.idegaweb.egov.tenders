package is.idega.idegaweb.egov.tenders.presentation;

import is.idega.idegaweb.egov.cases.presentation.CasesProcessor;
import is.idega.idegaweb.egov.tenders.TendersConstants;
import is.idega.idegaweb.egov.tenders.bean.CasePresentationInfo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import javax.faces.component.UIComponent;

import com.idega.block.process.data.Case;
import com.idega.block.process.presentation.UserCases;
import com.idega.block.process.presentation.beans.CasePresentation;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWResourceBundle;
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
import com.idega.util.CoreConstants;
import com.idega.util.ListUtil;
import com.idega.util.PresentationUtil;
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
		
		PresentationUtil.addJavaScriptSourcesLinesToHeader(iwc, Arrays.asList(
				CoreConstants.DWR_UTIL_SCRIPT,
				getBundle(iwc).getVirtualPathWithFileNameString("javascript/TendersHelper.js")
		));
		
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
				savePayers(iwc);
				saveSubscribers(iwc);
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
			setPayers(iwc, new ArrayList<String>(Arrays.asList(usersIDs)));
		}
	}

	private boolean setSubscribers(IWContext iwc, String[] usersIDs) {
		IWResourceBundle iwrb = getResourceBundle(iwc);
		
		List<String> selectedSubscribersIds = Arrays.asList(usersIDs);
		List<String> newSubscribersIds = new ArrayList<String>();
		List<String> subscribersToRemove = new ArrayList<String>();
		List<String> currentSubscribers = getSubscribersIds(iwc);
		resolveNewAndOldIds(iwc, currentSubscribers, selectedSubscribersIds, subscribersToRemove, newSubscribersIds);
		if (!ListUtil.isEmpty(subscribersToRemove)) {
			if (!removeSubscribers(iwc, getUsers(iwc, ArrayUtil.convertListToArray(subscribersToRemove)))) {
				saveActionMessage = iwrb.getLocalizedString("tender_case_manager.unable_to_set_subscribers", "Unable to save: unable to set subscribers");
				return false;
			}
		}
		if (ListUtil.isEmpty(newSubscribersIds)) {
			saveActionMessage = iwrb.getLocalizedString("tender_case_manager.subscribers_set_successfully", "Subscribers were set successfully");
			return true;
		}
		
		Collection<User> newSubscribers = getUsers(iwc, ArrayUtil.convertListToArray(newSubscribersIds));
		if (ListUtil.isEmpty(newSubscribers)) {
			saveActionMessage = iwrb.getLocalizedString("tender_case_manager.unable_to_set_subscribers", "Unable to save: unable to set subscribers");
			return false;
		}
		
		Case theCase = getTheCase();
		if (!getTendersHelper().doSubscribeToCase(iwc, newSubscribers, theCase)) {
			saveActionMessage = iwrb.getLocalizedString("tender_case_manager.unable_to_set_subscribers", "Unable to save: unable to set subscribers");
			return false;
		}
		
		saveActionMessage = iwrb.getLocalizedString("tender_case_manager.subscribers_set_successfully", "Subscribers were set successfully");
		return true;
	}
	
	private void resolveNewAndOldIds(IWContext iwc, List<String> currentIds, List<String> selectedIds, List<String> removeIds, List<String> addIds) {
		if (ListUtil.isEmpty(currentIds)) {
			//	No users set for current case yet
			addIds.addAll(selectedIds);
			return;
		}
				
		//	Will resolve which users to remove and which users are "new"
		for (String selectedId: selectedIds) {
			if (!currentIds.contains(selectedId)) {
				//	New user was selected
				addIds.add(selectedId);
			}
		}
		
		for (String currentId: currentIds) {
			if (!selectedIds.contains(currentId)) {
				//	User was de-selected
				removeIds.add(currentId);
			}
		}
	}
	
	private boolean setPayers(IWContext iwc, List<String> selectedPayersIds) {
		IWResourceBundle iwrb = getResourceBundle(iwc);
		
		List<String> newPayersIds = new ArrayList<String>();
		List<String> payersToRemove = new ArrayList<String>();
		List<String> currentPayers = getPayersIds(iwc);
		resolveNewAndOldIds(iwc, currentPayers, selectedPayersIds, payersToRemove, newPayersIds);
		if (!ListUtil.isEmpty(payersToRemove)) {
			if (!removePayers(iwc, getUsers(iwc, ArrayUtil.convertListToArray(payersToRemove)))) {
				savePayersActionMessage = iwrb.getLocalizedString("tender_case_manager.unable_to_set_payers", "Unable to save: unable to set payers");
				return false;
			}
		}
		if (ListUtil.isEmpty(newPayersIds)) {
			savePayersActionMessage = iwrb.getLocalizedString("tender_case_manager.payers_set_successfully", "Payers were set successfully");
			return true;
		}
		
		Collection<User> newPayers = getUsers(iwc, ArrayUtil.convertListToArray(newPayersIds));
		if (ListUtil.isEmpty(newPayers)) {
			savePayersActionMessage = iwrb.getLocalizedString("tender_case_manager.unable_to_set_payers", "Unable to save: unable to set payers");
			return false;
		}
		
		String caseId = iwc.getParameter(CasesProcessor.PARAMETER_CASE_PK);
		if (!getTendersHelper().setPayers(newPayers, caseId)) {
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
		return removeSubscribers(iwc, doPrepareForRemove(iwc));
	}
	
	private boolean removeSubscribers(IWContext iwc, Collection<User> subscribers) {
		if (ListUtil.isEmpty(subscribers)) {
			return true;
		}
		
		IWResourceBundle iwrb = getResourceBundle(iwc);
		
		Case theCase = getTheCase();
		if (!getTendersHelper().doUnSubscribeFromCase(iwc, subscribers, theCase)) {
			saveActionMessage = iwrb.getLocalizedString("tender_case_manager.unable_to_remove_subscribers", "Unable to save: unable to remove subscribers");
			return false;
		}
			
		saveActionMessage = iwrb.getLocalizedString("tender_case_manager.subscribers_removed_successfully", "All subscribers were removed successfully");
		return true;
	}
	
	private boolean removeAllPayers(IWContext iwc) {
		Collection<User> payers = getTendersHelper().getPayers(getCaseId());
		if (ListUtil.isEmpty(payers)) {
			return true;
		}
		
		return removePayers(iwc, payers);
	}
	
	private boolean removePayers(IWContext iwc, Collection<User> payers) {
		IWResourceBundle iwrb = getResourceBundle(iwc);
		String caseId = iwc.getParameter(CasesProcessor.PARAMETER_CASE_PK);
		
		if (!getTendersHelper().removePayers(payers, caseId)) {
			savePayersActionMessage = iwrb.getLocalizedString("tender_case_manager.unable_to_remove_payers", "Unable to save: unable to remove payers");
			return false;
		}
		
		savePayersActionMessage = iwrb.getLocalizedString("tender_case_manager.payers_removed_successfully", "All payers were removed successfully");
		return true;
	}
	
	private DropdownMenu getCasesToManage(IWContext iwc, String formId) {
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
		
		IWResourceBundle iwrb = getResourceBundle(iwc);
		menu.addFirstOption(new SelectOption(iwrb.getLocalizedString("tender_case_manager.select_tender", "Select tender"), -1));
		
		String caseId = null;
		if (iwc.isParameterSet(CasesProcessor.PARAMETER_CASE_PK)) {
			caseId = iwc.getParameter(CasesProcessor.PARAMETER_CASE_PK);
			menu.setSelectedElement(caseId);
		} else {
			caseId = String.valueOf(-1);
		}
		
		setCaseId(caseId);
		
		menu.setOnChange("TendersHelper.setSelectedTender('"+formId+"', '"+menu.getId()+"', '"+CasesProcessor.PARAMETER_CASE_PK+"');");
//		menu.setOnChange(new StringBuilder("this.form['").append(CasesProcessor.PARAMETER_CASE_PK).append("'].value=dwr.util.getValue('")
//				.append(menu.getId()).append("');this.form.submit();").toString());
		
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
		
		DropdownMenu casesMenu = getCasesToManage(iwc, form.getId());
		
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
	
		CasePresentationInfo caseInfo = getCaseInfo();
		if (caseInfo != null) {
			//	Subscribed users
			addUsersFilter(form, getSubscribersIds(iwc), iwrb.getLocalizedString("tender_case_manager.subscribe_users_to_case", "Set subscribed users"),
					SUBSCRIBED_USERS_PARAMETER);
			
			if (caseInfo.isPaymentCase()) {
				form.addParameter(PAYMENT_CASE, Boolean.TRUE.toString());
				
				//	Payed for attachments users
				addUsersFilter(form, getPayersIds(iwc),
						iwrb.getLocalizedString("tender_case_manager.mark_payed_users_for_attachments", "Mark payed users for the documents"),
						PAYED_FOR_THE_ATTACHMENTS_USERS_PARAMETER);
			}
		}
		
		//	Buttons
		Layer buttons = new Layer();
		form.add(buttons);
		buttons.setStyleClass("buttonLayer");
		
		BackButton back = new BackButton(iwrb.getLocalizedString("tender_cases.back", "Back"));
		buttons.add(back);
		
		if (caseInfo != null) {
			SubmitButton save = new SubmitButton(iwrb.getLocalizedString("tender_cases.save_subscriders", "Save"), UserCases.PARAMETER_ACTION,
					String.valueOf(CasesProcessor.ACTION_SAVE));
			buttons.add(save);
		}
	}
	
	private void addUsersFilter(UIComponent container, List<String> selectedUsers, String label, String selectedUsersInputName) {
		Layer usersFilterContainer = new Layer();
		container.getChildren().add(usersFilterContainer);
		usersFilterContainer.setStyleClass("formItem");
		
		UsersFilter usersFilter = new UsersFilter();
		usersFilter.setAddLabel(false);
		usersFilter.setSelectedUsers(selectedUsers);
		usersFilter.setSelectedUserInputName(selectedUsersInputName);
		usersFilter.setRoles(Arrays.asList(TendersConstants.TENDERS_ROLE));
		usersFilter.setShowGroupChooser(false);
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
