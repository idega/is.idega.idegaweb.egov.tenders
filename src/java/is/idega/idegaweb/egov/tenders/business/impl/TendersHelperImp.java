package is.idega.idegaweb.egov.tenders.business.impl;

import is.idega.idegaweb.egov.bpm.cases.messages.CaseUserFactory;
import is.idega.idegaweb.egov.bpm.cases.messages.CaseUserImpl;
import is.idega.idegaweb.egov.tenders.TendersConstants;
import is.idega.idegaweb.egov.tenders.bean.CasePresentationInfo;
import is.idega.idegaweb.egov.tenders.business.TendersCommentsPersistenceManager;
import is.idega.idegaweb.egov.tenders.business.TendersHelper;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jbpm.graph.exe.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.idega.block.article.component.CommentsViewer;
import com.idega.block.process.business.CaseBusiness;
import com.idega.block.process.business.CaseManagersProvider;
import com.idega.block.process.business.CasesRetrievalManager;
import com.idega.block.process.data.Case;
import com.idega.block.process.data.CaseCode;
import com.idega.block.process.data.CaseCodeHome;
import com.idega.block.process.data.CaseHome;
import com.idega.block.process.presentation.beans.CasePresentation;
import com.idega.block.process.presentation.beans.CasePresentationComparator;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.core.accesscontrol.business.AccessController;
import com.idega.core.accesscontrol.business.LoginSession;
import com.idega.core.business.DefaultSpringBean;
import com.idega.core.persistence.Param;
import com.idega.data.IDOLookup;
import com.idega.data.IDOLookupException;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.idegaweb.IWMainApplication;
import com.idega.idegaweb.egov.bpm.data.CaseProcInstBind;
import com.idega.idegaweb.egov.bpm.data.dao.CasesBPMDAO;
import com.idega.jbpm.bean.VariableInstanceInfo;
import com.idega.jbpm.bean.VariableStringInstance;
import com.idega.jbpm.data.Actor;
import com.idega.jbpm.data.ActorPermissions;
import com.idega.jbpm.data.NativeIdentityBind;
import com.idega.jbpm.data.NativeIdentityBind.IdentityType;
import com.idega.jbpm.data.VariableInstanceQuerier;
import com.idega.jbpm.data.dao.BPMDAO;
import com.idega.jbpm.exe.BPMFactory;
import com.idega.jbpm.exe.ProcessInstanceW;
import com.idega.jbpm.exe.TaskInstanceW;
import com.idega.jbpm.identity.Identity;
import com.idega.jbpm.identity.Role;
import com.idega.jbpm.identity.RolesManager;
import com.idega.jbpm.identity.permission.Access;
import com.idega.jbpm.variables.BinaryVariable;
import com.idega.presentation.IWContext;
import com.idega.presentation.paging.PagedDataCollection;
import com.idega.user.data.User;
import com.idega.user.data.UserHome;
import com.idega.util.CoreConstants;
import com.idega.util.ListUtil;
import com.idega.util.StringUtil;
import com.idega.util.URIUtil;
import com.idega.util.datastructures.map.MapUtil;
import com.idega.util.expression.ELUtil;

/**
 * Helper methods for tenders project logic
 * @author <a href="mailto:valdas@idega.com">Valdas Žemaitis</a>
 * @version $Revision: 1.6 $
 *
 * Last modified: $Date: 2009/07/03 09:01:16 $ by: $Author: valdas $
 */
@Service
@Scope(BeanDefinition.SCOPE_SINGLETON)
@Transactional(readOnly = true)
public class TendersHelperImp extends DefaultSpringBean implements TendersHelper {

	private static final Logger LOGGER = Logger.getLogger(TendersHelperImp.class.getName());

	@Autowired
	private CaseManagersProvider caseManagersProvider;

	@Autowired
	private CasesBPMDAO casesDAO;

	@Autowired
	private CaseUserFactory caseUserFactory;

	@Autowired
	private BPMFactory bpmFactory;

	@Autowired
	private BPMDAO bpmDAO;

	@Autowired
	private VariableInstanceQuerier variablesQuerier;

	@Override
	public PagedDataCollection<CasePresentation> getAllCases(Locale locale, String statusesToHide, String statusesToShow) {
		getLogger().info("Starting method to find all tender cases. Locale: " + locale + ", statuses to hide: " + statusesToHide +
				", statuses to show: " + statusesToShow);
		PagedDataCollection<CasePresentation> result = null;
		try {
			CaseHome caseHome = null;
			try {
				caseHome = (CaseHome) IDOLookup.getHome(Case.class);
			} catch (IDOLookupException e) {
				LOGGER.log(Level.WARNING, "Error getting " + CaseHome.class, e);
			}
			if (caseHome == null) {
				return null;
			}
			CaseCodeHome caseCodeHome = null;
			try {
				caseCodeHome = (CaseCodeHome) IDOLookup.getHome(CaseCode.class);
			} catch(Exception e) {
				LOGGER.log(Level.WARNING, "Error getting " + CaseCodeHome.class, e);
			}
			if (caseCodeHome == null) {
				return null;
			}

			Collection<Case> cases = null;
			try {
				CaseCode caseCode = caseCodeHome.findByPrimaryKey(TendersConstants.TENDER_CASES_CODE);
				getLogger().info("Found tender case code: " + caseCode);
				cases = caseHome.findAllByCaseCode(caseCode);
				getLogger().info("Found tender cases by code: " + cases);
			} catch(Exception e) {
				LOGGER.log(Level.SEVERE, "Error getting tenders cases", e);
			}
			if (ListUtil.isEmpty(cases)) {
				return null;
			}

			Collection<Case> filteredCases = null;
			List<String> allStatusesToShow = StringUtil.getValuesFromString(statusesToShow, CoreConstants.COMMA);
			List<String> allStatusesToHide = StringUtil.getValuesFromString(statusesToHide, CoreConstants.COMMA);
			if (allStatusesToShow != null || allStatusesToHide != null) {
				filteredCases = new ArrayList<Case>();
				for (Case theCase: cases) {
					String status = theCase.getStatus();

					if (allStatusesToShow != null && allStatusesToShow.contains(status)) {
						filteredCases.add(theCase);						//	Current status is defined to SHOW case
					} else if (ListUtil.isEmpty(allStatusesToHide)) {
						filteredCases.add(theCase);						//	ALL cases with statuses must be SHOWN
					} else if (!allStatusesToHide.contains(status)) {
						filteredCases.add(theCase);						//	Current status is NOT defined to HIDE
					}
				}
				if (ListUtil.isEmpty(filteredCases)) {
					return null;
				}
			}

			result = getCaseManagersProvider().getCaseManager().getCasesByEntities(filteredCases == null ? cases : filteredCases, locale);
			return result;
		} finally {
			getLogger().info("Returning all tender cases: " + result);
		}
	}

	@Override
	public List<CasePresentation> getSortedCases(List<CasePresentation> casesToSort, Locale locale) {
		if (ListUtil.isEmpty(casesToSort)) {
			return null;
		}

		Collections.sort(casesToSort, new CasePresentationComparator(locale));

		return casesToSort;
	}

	@Override
	@Transactional(readOnly=true)
	public Collection<CasePresentation> getValidTendersCases(Collection<CasePresentation> cases, User currentUser, Locale locale) {
		if (ListUtil.isEmpty(cases)) {
			return null;
		}

		Collection<String> identifiers = new ArrayList<String>();
		for (CasePresentation theCase: cases) {
			if (theCase.isBpm() && !identifiers.contains(theCase.getCaseIdentifier())) {
				identifiers.add(theCase.getCaseIdentifier());
			}
		}
		if (ListUtil.isEmpty(identifiers)) {
			return null;
		}

		List<Object[]> binds = null;
		try {
			binds = getCasesDAO().getCaseProcInstBindProcessInstanceByCaseIdentifier(identifiers);
		} catch(Exception e) {
			LOGGER.log(Level.SEVERE, "Error getting binds for cases identifiers: " + identifiers, e);
		}
		if (ListUtil.isEmpty(binds)) {
			return null;
		}

		Map<Long, CasePresentationInfo> info = new HashMap<Long, CasePresentationInfo>();

		List<Long> processInstanceIds = new ArrayList<Long>();
		for (Object[] bind: binds) {
			Object o = bind[1];
			if (o instanceof ProcessInstance) {
				ProcessInstance pi = (ProcessInstance) o;
				if (pi.hasEnded()) {
					continue;
				}

				Long id = ((ProcessInstance) o).getId();
				if (!processInstanceIds.contains(id)) {
					o = bind[0];
					if (o instanceof CaseProcInstBind) {
						CasePresentationInfo caseInfo = getCaseInfo(cases, ((CaseProcInstBind) o).getCaseId(), id);
						if (caseInfo != null) {
							processInstanceIds.add(id);
							info.put(id, caseInfo);
						}
					}
				}
			}
		}
		if (ListUtil.isEmpty(processInstanceIds)) {
			LOGGER.warning("No process instances found for identifiers: " + identifiers);
			return null;
		}

		Map<Long, Map<String, VariableInstanceInfo>> variables = null;
		try {
			getLogger().info("Will load tender cases info for proc. instances: " + processInstanceIds);
			variables = getVariablesQuerier().getVariablesByNamesAndValuesAndExpressionsByProcesses(
					null,
					Arrays.asList(
							TendersConstants.TENDER_CASE_START_DATE_VARIABLE,
							TendersConstants.TENDER_CASE_END_DATE_VARIABLE,
							TendersConstants.TENDER_CASE_IS_PRIVATE_VARIABLE,
							TendersConstants.TENDER_CASE_IS_PAYMENT_VARIABLE
					),
					null,
					processInstanceIds,
					null
			);
		} catch(Exception e) {
			LOGGER.log(Level.SEVERE, "Error getting date variables for processes: " + processInstanceIds, e);
		}
		if (MapUtil.isEmpty(variables)) {
			return null;
		}

		for (Long piId: variables.keySet()) {
			CasePresentationInfo caseInfo = info.get(piId);
			if (caseInfo == null) {
				continue;
			}

			Map<String, VariableInstanceInfo> procVars = variables.get(piId);
			Timestamp start = getValue(procVars, TendersConstants.TENDER_CASE_START_DATE_VARIABLE);
			if (start != null) {
				caseInfo.setStartDate(start);
			}
			Timestamp end = getValue(procVars, TendersConstants.TENDER_CASE_END_DATE_VARIABLE);
			if (end != null) {
				caseInfo.setEndDate(end);
			}

			String privateTender = getValue(procVars, TendersConstants.TENDER_CASE_IS_PRIVATE_VARIABLE);
			if (!StringUtil.isEmpty(privateTender) && Boolean.valueOf(privateTender)) {
				caseInfo.setCaseIsPrivate(Boolean.TRUE);
			}

			String paymentTender = getValue(procVars, TendersConstants.TENDER_CASE_IS_PAYMENT_VARIABLE);
			if (!StringUtil.isEmpty(paymentTender) && Boolean.valueOf(paymentTender)) {
				caseInfo.setPaymentCase(Boolean.TRUE);
			}
		}

		Timestamp currentTime = new Timestamp(System.currentTimeMillis());
		List<CasePresentation> validCases = new ArrayList<CasePresentation>();
		for (CasePresentationInfo caseInfo: info.values()) {
			if (isCaseVisible(caseInfo, currentUser)) {
				if (caseInfo.getStartDate() != null && caseInfo.getEndDate() != null) {
					if (currentTime.after(caseInfo.getStartDate()) && currentTime.before(caseInfo.getEndDate())) {
						validCases.add(caseInfo.getCasePresentation());
					}
				}
			}
		}

		return getSortedCases(validCases, locale);
	}

	private <T extends Serializable> T getValue(Map<String, VariableInstanceInfo> vars, String name) {
		if (MapUtil.isEmpty(vars) || StringUtil.isEmpty(name)) {
			return null;
		}

		VariableInstanceInfo var = vars.get(name);
		if (var == null) {
			return null;
		}

		Serializable value = var.getValue();
		@SuppressWarnings("unchecked")
		T result = (T) value;
		return result;
	}

	private boolean isCaseVisible(CasePresentationInfo caseInfo, User currentUser) {
		if (!caseInfo.isCaseIsPrivate()) {
			return true;
		}

		caseInfo.getCasePresentation().setPrivate(true);

		if (currentUser == null) {
			return false;
		}

		AccessController accessController = IWMainApplication.getDefaultIWMainApplication().getAccessController();
		if (canManageCaseSubscribers(currentUser)) {
			return true;									//	Handler can see all cases
		} else if (accessController.hasRole(currentUser, TendersConstants.TENDER_CASES_OWNER_ROLE)) {
			return isUserOwner(caseInfo, currentUser);		//	Owner can see case
		} else if (isUserInvited(caseInfo, currentUser)) {
			return isUserInvited(caseInfo, currentUser);	//	User can see case if invited
		}

		return false;
	}

	@Override
	public boolean canManageCaseSubscribers(User user) {
		if (user == null) {
			return false;
		}

		try {
			LoginSession loginSession = ELUtil.getInstance().getBean(LoginSession.class);
			if (loginSession != null && loginSession.isSuperAdmin()) {
				return true;
			}
		} catch(Exception e) {}

		return IWMainApplication.getDefaultIWMainApplication().getAccessController().hasRole(user, TendersConstants.TENDER_CASES_HANDLER_ROLE);
	}

	private boolean isUserInvited(CasePresentationInfo caseInfo, User currentUser) {
		return isSubscribed(IWMainApplication.getDefaultIWApplicationContext(), currentUser, caseInfo.getCasePresentation().getId());
	}

	private boolean isUserOwner(CasePresentationInfo caseInfo, User currentUser) {
		User owner = caseInfo.getCasePresentation().getOwner();
		if (owner == null) {
			return false;
		}

		return owner.getId().equals(currentUser.getId());
	}

	private CasePresentationInfo getCaseInfo(Collection<CasePresentation> cases, Integer caseId, Long processInstanceId) {
		String id = String.valueOf(caseId);
		for (CasePresentation theCase: cases) {
			if (id.equals(theCase.getId())) {
				return new CasePresentationInfo(processInstanceId, theCase);
			}
		}

		return null;
	}

	public CasesBPMDAO getCasesDAO() {
		return casesDAO;
	}

	public void setCasesDAO(CasesBPMDAO casesDAO) {
		this.casesDAO = casesDAO;
	}

	public CaseManagersProvider getCaseManagersProvider() {
		return caseManagersProvider;
	}

	public void setCaseManagersProvider(CaseManagersProvider caseManagersProvider) {
		this.caseManagersProvider = caseManagersProvider;
	}

	@Override
	@Transactional(readOnly=true)
	public CasePresentationInfo getTenderCaseInfo(Object caseId) {
		if (caseId == null) {
			return null;
		}

		CaseProcInstBind bind = null;
		try {
			bind = getCasesDAO().getCaseProcInstBindByCaseId(Integer.valueOf(caseId.toString()));
		} catch(Exception e) {
			LOGGER.log(Level.WARNING, "Error getting BPM case by ID: " + caseId, e);
		}
		if (bind == null) {
			return null;
		}

		CasePresentationInfo info = new CasePresentationInfo(bind.getProcInstId());

		Collection<VariableInstanceInfo> variables = null;
		try {
			variables = getVariablesQuerier().getVariablesByProcessInstanceIdAndVariablesNames(Arrays.asList(info.getProcessInstanceId()), Arrays.asList(
					TendersConstants.TENDER_CASE_TENDER_NAME_VARIABLE,
					TendersConstants.TENDER_CASE_TENDER_ISSUER_VARIABLE,
					TendersConstants.TENDER_CASE_JOB_DESCRIPTION_VARIABLE,
					TendersConstants.TENDER_CASE_IS_PRIVATE_VARIABLE,
					TendersConstants.TENDER_CASE_IS_PAYMENT_VARIABLE
			));
		} catch(Exception e) {
			LOGGER.log(Level.WARNING, "Error getting info about tender case: " + caseId, e);
		}
		if (ListUtil.isEmpty(variables)) {
			return info;
		}

		for (VariableInstanceInfo variable: variables) {
			if (variable instanceof VariableStringInstance) {
				info.addInfo(variable.getName(), variable.getValue().toString());

				if (TendersConstants.TENDER_CASE_IS_PRIVATE_VARIABLE.equals(variable.getName())) {
					info.setCaseIsPrivate(Boolean.valueOf(variable.getValue().toString()));
				} else if (TendersConstants.TENDER_CASE_IS_PAYMENT_VARIABLE.equals(variable.getName())) {
					info.setPaymentCase(Boolean.valueOf(variable.getValue().toString()));
				}
			}
		}

		return info;
	}

	@Override
	public boolean isSubscribed(IWApplicationContext iwac, User user, String caseId) {
		if (user == null || caseId == null) {
			return false;
		}

		CaseBusiness caseBusiness = getCaseBusiness(iwac);
		if (caseBusiness == null) {
			return false;
		}

		return caseBusiness.isSubscribed(caseId, user);
	}

	@Override
	public String getLinkToSubscribedCase(IWContext iwc, User user, String caseId) {
		CaseProcInstBind bind = null;
		try {
			bind = getCasesDAO().getCaseProcInstBindByCaseId(Integer.valueOf(caseId));
		} catch(Exception e) {
			LOGGER.log(Level.WARNING, "Error getting bind for case: " + caseId, e);
		}
		if (bind == null) {
			return null;
		}

		return getLinkToSubscribedCase(iwc, user, bind.getProcInstId());
	}

	@Override
	public String getLinkToSubscribedCase(IWContext iwc, User user, Long processInstanceId) {
		ProcessInstanceW piw = getProcessInstanceW(processInstanceId);
		CaseUserImpl caseUser = caseUserFactory.getCaseUser(user, piw);
		String uri = caseUser.getUrlToTheCase();

		URIUtil uriUtil = new URIUtil(uri);
		uriUtil.setParameter(CasesRetrievalManager.COMMENTS_PERSISTENCE_MANAGER_IDENTIFIER, TendersCommentsPersistenceManager.BEAN_IDENTIFIER);
		uriUtil.setParameter(CommentsViewer.AUTO_SHOW_COMMENTS, Boolean.TRUE.toString());

		String url = uriUtil.getUri();
		if (url.startsWith("http") && iwc.getIWMainApplication().getSettings().getBoolean("tenders.relative_link", Boolean.TRUE)) {
			url = url.substring(url.indexOf(CoreConstants.PAGES_URI_PREFIX));
		}
		return url;
	}

	@Override
	public boolean doSubscribeToCase(IWContext iwc, Collection<User> users, Case theCase) {
		try {
			ProcessInstance pi = getProcessInstance(theCase.getId());

			AccessController accessController = iwc.getAccessController();
			for (User user: users) {
				if (!setAccessForUserPerProcess(user, pi, Boolean.FALSE)) {
					return false;
				}

				theCase.addSubscriber(user);
				accessController.addRoleToGroup(TendersConstants.TENDERS_ROLE, user, iwc);
			}
			return true;
		} catch(Exception e) {
			LOGGER.log(Level.WARNING, "Error subscribing to case: " + theCase + ", for users: " + users, e);
		}

		return false;
	}

	@Override
	public boolean doSubscribeToCase(IWContext iwc, User user, Case theCase) {
		if (user == null || theCase == null) {
			return false;
		}

		return doSubscribeToCase(iwc, Arrays.asList(user), theCase);
	}

	@Override
	public boolean doSubscribeToCase(IWContext iwc, User user, String caseId) {
		if (StringUtil.isEmpty(caseId)) {
			return false;
		}

		CaseBusiness caseBusiness = getCaseBusiness(iwc);
		try {
			return doSubscribeToCase(iwc, user, caseBusiness.getCase(caseId));
		} catch(Exception e) {
			LOGGER.log(Level.WARNING, "Error subscribing to case: " + caseId + ", for user: " + user, e);
		}

		return false;
	}

	@Override
	public boolean doUnSubscribeFromCase(IWContext iwc, Collection<User> users, Case theCase) {
		if (ListUtil.isEmpty(users)) {
			return false;
		}

		try {
			String caseId = theCase.getId();
			ProcessInstanceW piw = getProcessInstanceW(caseId);
			ProcessInstance pi = piw.getProcessInstance();
			AccessController accessController = iwc.getAccessController();
			for (User user: users) {
				if (!setAccessForUserPerProcess(user, pi, Boolean.TRUE)) {
					return false;
				}

				theCase.removeSubscriber(user);
				accessController.removeRoleFromGroup(TendersConstants.TENDER_CASES_INVITED_ROLE, user, iwc);
				accessController.removeRoleFromGroup(TendersConstants.TENDERS_ROLE, user, iwc);
			}

			removePayers(users, piw, caseId);

			return true;
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Error unsubscribing from case: " + theCase + ", for users: " + users, e);
		}

		return false;
	}

	private boolean setAccessForUserPerProcess(User user, ProcessInstance pi, boolean remove) {
		if (user == null || pi == null) {
			LOGGER.warning("User or process instance is null!");
			return false;
		}

		Long piId = pi.getId();
		try {
			String userId = user.getId();

			Identity identity = new Identity(userId, IdentityType.USER);

			List<Actor> actors = getUserActors(userId, piId);

			RolesManager rolesManager = getBpmFactory().getRolesManager();
			if (remove) {
				return removeIdentities(actors, userId);
			} else {
				Role invitedRole = new Role(TendersConstants.TENDER_CASES_INVITED_ROLE, Access.read);
				invitedRole.setIdentities(Arrays.asList(new Identity(userId, IdentityType.USER)));
				Collection<Role> roles = Arrays.asList(invitedRole);

				if (ListUtil.isEmpty(actors)) {
					rolesManager.createProcessActors(roles, pi);
				}
				rolesManager.createIdentitiesForRoles(roles, identity, piId);
			}

			return true;
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Error setting accesses '" + Access.read + "' for user: " + user + " for process: " + pi, e);
		}

		return false;
	}

	@Override
	public CaseBusiness getCaseBusiness(IWApplicationContext iwac) {
		try {
			return IBOLookup.getServiceInstance(iwac == null ? IWMainApplication.getDefaultIWApplicationContext() : iwac, CaseBusiness.class);
		} catch (IBOLookupException e) {
			LOGGER.log(Level.WARNING, "Error getting: " + CaseBusiness.class, e);
		}
		return null;
	}

	public BPMFactory getBpmFactory() {
		return bpmFactory;
	}

	public void setBpmFactory(BPMFactory bpmFactory) {
		this.bpmFactory = bpmFactory;
	}

	@Override
	public boolean disableToSeeAllAttachmentsForNonPayers(TaskInstanceW currentTask) {
		if (currentTask == null) {
			return false;
		}

		ProcessInstanceW processInstance = currentTask.getProcessInstanceW();
		if (processInstance == null) {
			return false;
		}

		//	Disabling for all users
		List<TaskInstanceW> tasks = processInstance.getSubmittedTaskInstances();
		if (ListUtil.isEmpty(tasks)) {
			tasks = Arrays.asList(currentTask);
		} else if (!tasks.contains(currentTask)) {
			tasks.add(currentTask);
		}

		if (!disableToSeeAllAttachments(tasks)) {
			return false;
		}

		Case theCase = getCase(processInstance.getProcessInstanceId());
		if (theCase == null) {
			return false;
		}

		Collection<User> payers = getPayers(theCase.getPrimaryKey().toString());
		if (ListUtil.isEmpty(payers)) {
			return true;
		}

		//	Enabling just for payers
		for (User payer: payers) {
			if (!enableToSeeAllAttachmentsForUser(processInstance.getProcessInstanceId(), tasks, payer)) {
				return false;
			}
		}

		return true;
	}

	@Override
	public boolean disableToSeeAllAttachments(ProcessInstanceW processInstance) {
		if (processInstance == null) {
			return false;
		}

		return disableToSeeAllAttachments(processInstance.getAllTaskInstances());
	}

	private boolean disableToSeeAllAttachments(List<TaskInstanceW> tasks) {
		if (ListUtil.isEmpty(tasks)) {
			return false;
		}

		for (TaskInstanceW taskInstance: tasks) {
			if (!setAccessRight(taskInstance, TendersConstants.TENDER_CASES_3RD_PARTIES_ROLES, null)) {
				return false;
			}
		}
		return true;
	}

	private boolean setAccessRight(TaskInstanceW taskInstance, List<String> rolesNames, Access access) {
		if (taskInstance == null) {
			return false;
		}

		List<BinaryVariable> attachments = taskInstance.getAttachments();
		if (ListUtil.isEmpty(attachments)) {
			return true;
		}

		try {
			for (BinaryVariable attachment: attachments) {
				for (String roleName: rolesNames) {
					taskInstance.setTaskRolePermissions(new Role(roleName, access), false, String.valueOf(attachment.getHash()));
				}
			}
		} catch(Exception e) {
			LOGGER.log(Level.WARNING, "Error setting access rights for task: " + taskInstance.getTaskInstanceId(), e);
			return false;
		}

		return true;
	}

	@Override
	public boolean enableToSeeAllAttachmentsForUser(ProcessInstanceW processInstance, User user) {
		return setAccessRight(processInstance, Access.seeAttachments, user);
	}

	private boolean enableToSeeAllAttachmentsForUser(Long processInstanceId, List<TaskInstanceW> tasks, User user) {
		return setAccessRight(processInstanceId, tasks, Access.seeAttachments, user);
	}

	@Override
	public boolean disableToSeeAllAttachmentsForUser(ProcessInstanceW processInstance, User user) {
		return setAccessRight(processInstance, null, user);
	}

	private boolean setAccessRight(ProcessInstanceW processInstance, Access access, User user) {
		if (processInstance == null || user == null) {
			return false;
		}

		return setAccessRight(processInstance.getProcessInstanceId(), processInstance.getSubmittedTaskInstances(), access, user);
	}

	private boolean setAccessRight(Long processInstanceId, List<TaskInstanceW> tasks, Access access, User user) {
		if (ListUtil.isEmpty(tasks)) {
			return false;
		}

		try {
			for (TaskInstanceW taskInstance: tasks) {
				List<BinaryVariable> attachments = taskInstance.getAttachments();
				if (ListUtil.isEmpty(attachments)) {
					continue;
				}

				Map<String, Boolean> variablesAccesses = getAccessesForVariables(taskInstance, attachments);

				Role role = new Role(TendersConstants.TENDER_CASES_INVITED_ROLE, access);
				role.setUserId(user.getId());
				role.setForTaskInstance(Boolean.TRUE);
				for (BinaryVariable attachment: attachments) {
					String variableIdentifier = String.valueOf(attachment.getHash());

					boolean canSetPermission = canSetPermission(variablesAccesses, variableIdentifier);
					if (canSetPermission) {
						getBpmFactory().getRolesManager().setAttachmentPermission(role, processInstanceId, taskInstance.getTaskInstanceId(),
								variableIdentifier, Integer.valueOf(user.getId()));
					}
				}
			}
		} catch(Exception e) {
			LOGGER.log(Level.WARNING, "Error setting access rights", e);
			return false;
		}

		return true;
	}

	private boolean canSetPermission(Map<String, Boolean> variablesAccesses, String variableIdentifier) {
		if (variablesAccesses == null || ListUtil.isEmpty(variablesAccesses.values())) {
			return true;
		}

		Boolean access = variablesAccesses.get(variableIdentifier);
		return access == null ? true : access.booleanValue();
	}

	@Transactional(readOnly = true)
	private Map<String, Boolean> getAccessesForVariables(TaskInstanceW taskInstance, List<BinaryVariable> attachments) {
		List<ActorPermissions> perms = null;
		try {
			perms = getBpmDAO().getResultList(ActorPermissions.getSetByTaskIdOrTaskInstanceId, ActorPermissions.class,
					new Param(ActorPermissions.taskInstanceIdProperty, taskInstance.getTaskInstanceId()),
					new Param(ActorPermissions.taskIdProperty, Long.valueOf(-1))
			);
		} catch(Exception e) {
			LOGGER.log(Level.WARNING, "Error getting permissions", e);
		}
		if (ListUtil.isEmpty(perms)) {
			return null;
		}

		List<String> identifiers = new ArrayList<String>(attachments.size());
		for (BinaryVariable attachment: attachments) {
			identifiers.add(String.valueOf(attachment.getHash()));
		}

		Map<String, Boolean> accesses = new HashMap<String, Boolean>(identifiers.size());

		for (ActorPermissions permission: perms) {
			String identifier = permission.getVariableIdentifier();
			if (!StringUtil.isEmpty(identifier)) {
				Boolean currentPermission = accesses.get(identifier);

				if (currentPermission == null || currentPermission.booleanValue()) {
					Boolean canSeeAttachments = permission.getCanSeeAttachments();

					if (canSeeAttachments != null && "all".equals(permission.getCanSeeAttachmentsOfRoleName())) {
						accesses.put(identifier, canSeeAttachments);
					}
				}
			}
		}

		return accesses;
	}

	@Override
	public ProcessInstanceW getProcessInstanceW(String caseId) {
		if (StringUtil.isEmpty(caseId)) {
			return null;
		}

		CaseProcInstBind bind = null;
		try {
			bind = getCasesDAO().getCaseProcInstBindByCaseId(Integer.valueOf(caseId));
		} catch(Exception e) {
			LOGGER.log(Level.WARNING, "Error getting process instace for case: " + caseId);
		}
		if (bind == null) {
			return null;
		}

		return getProcessInstanceW(bind.getProcInstId());
	}

	private ProcessInstance getProcessInstance(String caseId) {
		return getProcessInstance(getProcessInstanceW(caseId).getProcessInstanceId());
	}

	private ProcessInstance getProcessInstance(final Long processInstanceId) {
		try {
			return getBpmFactory().getProcessInstanceW(processInstanceId).getProcessInstance();
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Error getting process instance by id: " + processInstanceId);
		}
		return null;
	}

	public BPMDAO getBpmDAO() {
		return bpmDAO;
	}

	public void setBpmDAO(BPMDAO bpmDAO) {
		this.bpmDAO = bpmDAO;
	}

	@Override
	public Case getCase(Long processInstanceId) {
		CaseProcInstBind bind = null;
		try {
			bind = getCasesDAO().getCaseProcInstBindByProcessInstanceId(processInstanceId);
		} catch(Exception e) {
			LOGGER.log(Level.WARNING, "Error getting case for process instance: " + processInstanceId);
		}
		if (bind == null) {
			return null;
		}

		try {
			return getCaseBusiness(IWMainApplication.getDefaultIWApplicationContext()).getCase(bind.getCaseId());
		} catch(Exception e) {
			LOGGER.log(Level.WARNING, "Error getting case by id: " + bind.getCaseId(), e);
		}

		return null;
	}

	@Override
	public Collection<User> getPayers(String caseId) {
		if (StringUtil.isEmpty(caseId)) {
			return null;
		}

		UserHome userHome = null;
		try {
			userHome = (UserHome) IDOLookup.getHome(User.class);
		} catch (IDOLookupException e) {
			e.printStackTrace();
		}
		if (userHome == null) {
			return null;
		}

		try {
			return userHome.findUsersByMetaData(getMetaDataKey(caseId), Boolean.TRUE.toString());
		} catch(Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public String getMetaDataKey(String caseId) {
		return new StringBuilder(TendersConstants.USER_HAS_PAYED_FOR_TENDER_CASE_ATTACHMENTS_META_DATA_KEY).append(caseId).toString();
	}

	@Override
	public ProcessInstanceW getProcessInstanceW(Long processInstanceId) {
		return getBpmFactory().getProcessManagerByProcessInstanceId(processInstanceId).getProcessInstance(processInstanceId);
	}

	@Transactional(readOnly = true)
	private List<Actor> getUserActors(String userId, Long processInstanceId) {
		List<Actor> actors = null;
		try {
			actors = getBpmDAO().getResultList(Actor.getActorsByUserIdentityAndProcessInstanceId, Actor.class,
				    new Param(Actor.processInstanceIdProperty, processInstanceId),
				    new Param(NativeIdentityBind.identityTypeProperty, IdentityType.USER),
				    new Param(NativeIdentityBind.identityIdProperty, userId)
			);
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Error getting actors for user: " + userId + " and process instance: " + processInstanceId);
		}
		return actors;
	}

	@Transactional(readOnly = false)
	private boolean removeIdentities(List<Actor> actors, String userId) {
		if (ListUtil.isEmpty(actors)) {
			LOGGER.warning("There are no 'permissions' for user: " + userId);
			return true;
		}

		List<Long> ids = new ArrayList<Long>();
		for (Actor actor: actors) {
			Long id = actor.getActorId();
			if (!ids.contains(id)) {
				ids.add(id);
			}
		}

		List<NativeIdentityBind> identities = null;
		try {
			identities = getBpmDAO().getNativeIdentities(ids, IdentityType.USER);
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Error getting identities for user: " + userId, e);
			return false;
		}
		if (ListUtil.isEmpty(identities)) {
			LOGGER.warning("There are no 'permissions' for user: " + userId);
			return true;
		}

		List<Long> idsToRemove = new ArrayList<Long>();
		for (NativeIdentityBind identity: identities) {
			if (userId.equals(identity.getIdentityId()) && !idsToRemove.contains(identity.getId())) {
				idsToRemove.add(identity.getId());
			}
		}

		if (ListUtil.isEmpty(idsToRemove)) {
			LOGGER.warning("There are no 'permissions' for user: " + userId);
			return true;
		}

		try {
			getBpmDAO().createNamedQuery(NativeIdentityBind.deleteByIds).setParameter(NativeIdentityBind.idsParam, idsToRemove).executeUpdate();
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Error removing identities: " + idsToRemove + " for user: " + userId, e);
			return false;
		}

		return true;
	}

	@Override
	public boolean removePayers(Collection<User> users, String caseId) {
		return removePayers(users, getProcessInstanceW(caseId), caseId);
	}

	private boolean removePayers(Collection<User> users, ProcessInstanceW processInstance, String caseId) {
		if (ListUtil.isEmpty(users) || processInstance == null || StringUtil.isEmpty(caseId)) {
			return false;
		}

		try {
			String metaDataKey = getMetaDataKey(caseId);
			for (User payer: users) {
				String metaData = payer.getMetaData(metaDataKey);
				if (!StringUtil.isEmpty(metaData) && Boolean.TRUE.toString().equals(metaData)) {
					if (!disableToSeeAllAttachmentsForUser(processInstance, payer)) {
						return false;
					}

					payer.removeMetaData(metaDataKey);
					payer.store();
				}
			}
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Error removing payers: " + users + " for case: " + caseId, e);
			return false;
		}

		return true;
	}

	@Override
	public boolean setPayers(Collection<User> users, String caseId) {
		if (ListUtil.isEmpty(users) || StringUtil.isEmpty(caseId)) {
			return false;
		}

		try {
			String metaDataKey = getMetaDataKey(caseId);
			ProcessInstanceW processInstance = getProcessInstanceW(caseId);
			for (User newPayer: users) {
				newPayer.setMetaData(metaDataKey, Boolean.TRUE.toString());
				newPayer.store();

				if (!enableToSeeAllAttachmentsForUser(processInstance, newPayer)) {
					return false;
				}
			}
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Error setting payers: " + users + " for case: " + caseId, e);
			return false;
		}

		return true;
	}

	public VariableInstanceQuerier getVariablesQuerier() {
		return variablesQuerier;
	}

	public void setVariablesQuerier(VariableInstanceQuerier variablesQuerier) {
		this.variablesQuerier = variablesQuerier;
	}
}