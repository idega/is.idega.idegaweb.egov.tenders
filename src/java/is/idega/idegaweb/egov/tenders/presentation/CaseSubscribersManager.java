package is.idega.idegaweb.egov.tenders.presentation;

import is.idega.idegaweb.egov.cases.presentation.CasesProcessor;
import is.idega.idegaweb.egov.tenders.TendersConstants;
import is.idega.idegaweb.egov.tenders.business.TendersHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.idega.block.process.data.Case;
import com.idega.block.process.presentation.UserCases;
import com.idega.block.process.presentation.beans.CasePresentation;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.Block;
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
import com.idega.user.business.GroupHelper;
import com.idega.user.data.User;
import com.idega.user.presentation.user.UsersFilter;
import com.idega.user.presentation.user.UsersFilterList;
import com.idega.util.ArrayUtil;
import com.idega.util.ListUtil;
import com.idega.util.StringUtil;
import com.idega.util.expression.ELUtil;

public class CaseSubscribersManager extends Block {

	@Autowired
	private TendersHelper tendersHelper;
	@Autowired
	private GroupHelper groupHelper;
	
	private Case theCase;
	
	private String caseId;
	
	private String saveActionMessage;
	
	@Override
	public void main(IWContext iwc) throws Exception {
		ELUtil.getInstance().autowire(this);
		
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
				showCaseForm(iwc);
				break;
				
			default:
				showCaseForm(iwc);
				break;
		}
	}
	
	private boolean resolveCaseId(IWContext iwc) {
		if (!StringUtil.isEmpty(caseId)) {
			return true;
		}
		
		caseId = iwc.getParameter(CasesProcessor.PARAMETER_CASE_PK);
		return StringUtil.isEmpty(caseId) ? false : true;
	}
	
	private void saveSubscribers(IWContext iwc) {
		String[] usersIDs = iwc.getParameterValues(UsersFilterList.USERS_FILTER_SELECTED_USERS);
		if (ArrayUtil.isEmpty(usersIDs)) {
			removeAllSubscribers(iwc);
		} else {
			setSubscribers(iwc, usersIDs);
		}
	}
	
	@SuppressWarnings("unchecked")
	private boolean setSubscribers(IWContext iwc, String[] usersIDs) {
		IWResourceBundle iwrb = getResourceBundle(iwc);

		if (!removeAllSubscribers(iwc)) {
			saveActionMessage = iwrb.getLocalizedString("tender_case_manager.unable_to_set_subscribers", "Unable to save: unable to set subscribers");
			return false;
		}
		
		Collection<User> newSubscribers = null;
		try {
			newSubscribers = groupHelper.getUserBusiness(iwc).getUsers(usersIDs);
		} catch(Exception e) {
			e.printStackTrace();
		}
		if (ListUtil.isEmpty(newSubscribers)) {
			saveActionMessage = iwrb.getLocalizedString("tender_case_manager.unable_to_set_subscribers", "Unable to save: unable to set subscribers");
			return false;
		}
		
		try {
			Case theCase = getCase(iwc);
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
	
	private boolean removeAllSubscribers(IWContext iwc) {
		IWResourceBundle iwrb = getResourceBundle(iwc);
		
		Case theCase = getCase(iwc);
		if (theCase == null) {
			saveActionMessage = iwrb.getLocalizedString("tender_case_manager.case_not_found", "Unable to save, case was not found");
			return false;
		}
		
		Collection<User> subscribers = theCase.getSubscribers();
		if (!ListUtil.isEmpty(subscribers)) {
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
		}
		
		saveActionMessage = iwrb.getLocalizedString("tender_case_manager.subscribers_removed_successfully", "All subscribers were removed successfully");
		return true;
	}
	
	private Case getCase(IWContext iwc) {
		if (theCase == null) {
			try {
				theCase = tendersHelper.getCaseBusiness(iwc).getCase(caseId);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		return theCase;
	}
	
	private DropdownMenu getCasesToManage(IWContext iwc) {
		DropdownMenu menu = new DropdownMenu();
		
		PagedDataCollection<CasePresentation> cases = tendersHelper.getAllCases(iwc.getCurrentLocale(), null, null);
		if (cases == null || ListUtil.isEmpty(cases.getCollection())) {
			return menu;
		}
		
		Collection<CasePresentation> validCases = tendersHelper.getValidTendersCases(cases.getCollection(), iwc.isLoggedOn() ?iwc.getCurrentUser() : null);
		if (ListUtil.isEmpty(validCases)) {
			return menu;
		}
		
		for (CasePresentation theCase: validCases) {
			menu.addOption(new SelectOption(theCase.getSubject(), theCase.getId()));
		}
		if (iwc.isParameterSet(CasesProcessor.PARAMETER_CASE_PK)) {
			caseId = iwc.getParameter(CasesProcessor.PARAMETER_CASE_PK);
		} else {
			caseId = cases.getCollection().iterator().next().getId();
		}
		
		menu.setSelectedElement(caseId);
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
		
		DropdownMenu casesMenu = getCasesToManage(iwc);
		
		if (StringUtil.isEmpty(caseId)) {
			form.add(new Heading1(iwrb.getLocalizedString("tender_case_manager.there_are_no_valid_cases_available", "There are no valid cases to manage")));
			return;
		}
		
		form.addParameter(CasesProcessor.PARAMETER_CASE_PK, caseId);
		
		Layer casesDropdownContainer = new Layer();
		form.add(casesDropdownContainer);
		casesDropdownContainer.setStyleClass("formItem");
		Label casesChooserLabel = new Label(iwrb.getLocalizedString("tender_case_manager.select_case", "Select case"), casesMenu);
		casesDropdownContainer.add(casesChooserLabel);
		casesDropdownContainer.add(casesMenu);
		
		TenderCaseViewer caseViewer = new TenderCaseViewer();
		caseViewer.setActAsStandalone(false);
		caseViewer.setCaseId(caseId);
		form.add(caseViewer);
		
		Layer usersFilterContainer = new Layer();
		form.add(usersFilterContainer);
		usersFilterContainer.setStyleClass("formItem");
		UsersFilter usersFilter = new UsersFilter();
		usersFilter.setAddLabel(false);
		usersFilter.setSelectedUsers(getSubscribersIds(iwc));
		Label usersFilterLabel = new Label(iwrb.getLocalizedString("tender_case_manager.subscribe_users_to_case", "Set subscribed users"), usersFilter);
		usersFilterContainer.add(usersFilterLabel);
		usersFilterContainer.add(usersFilter);
		
		Layer buttons = new Layer();
		form.add(buttons);
		buttons.setStyleClass("buttonLayer");
		
		BackButton back = new BackButton(iwrb.getLocalizedString("tender_cases.back", "Back"));
		buttons.add(back);
		
		SubmitButton save = new SubmitButton(iwrb.getLocalizedString("tender_cases.save_subscriders", "Save"), UserCases.PARAMETER_ACTION,
				String.valueOf(CasesProcessor.ACTION_SAVE));
		buttons.add(save);
	}
	
	private List<String> getSubscribersIds(IWContext iwc) {
		Case theCase = getCase(iwc);
		if (theCase == null) {
			return null;
		}
		
		Collection<User> subscribers = theCase.getSubscribers();
		if (ListUtil.isEmpty(subscribers)) {
			return null;
		}
		
		List<String> ids = new ArrayList<String>(subscribers.size());
		for (User subscriber: subscribers) {
			ids.add(subscriber.getId());
		}
		return ids;
	}
	
	private int parseAction(IWContext iwc) {
		if (iwc.isParameterSet(UserCases.PARAMETER_ACTION)) {
			return Integer.parseInt(iwc.getParameter(UserCases.PARAMETER_ACTION));
		}
		return CasesProcessor.ACTION_VIEW;
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
