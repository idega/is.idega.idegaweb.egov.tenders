package is.idega.idegaweb.egov.tenders.business;

import is.idega.idegaweb.egov.tenders.bean.CasePresentationInfo;

import java.util.Collection;
import java.util.List;
import java.util.Locale;

import com.idega.block.process.business.CaseBusiness;
import com.idega.block.process.data.Case;
import com.idega.block.process.presentation.beans.CasePresentation;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.jbpm.exe.ProcessInstanceW;
import com.idega.jbpm.exe.TaskInstanceW;
import com.idega.presentation.IWContext;
import com.idega.presentation.paging.PagedDataCollection;
import com.idega.user.data.User;

/**
 * Methods for Tenders
 * 
 * @author <a href="mailto:valdas@idega.com">Valdas Žemaitis</a>
 * @version $Revision: 1.9 $
 *
 * Last modified: $Date: 2009/06/22 09:57:18 $ by: $Author: valdas $
 */
public interface TendersHelper {
	
	public PagedDataCollection<CasePresentation> getAllCases(Locale locale, String statusesToHide, String statusesToShow);

	public Collection<CasePresentation> getValidTendersCases(Collection<CasePresentation> cases, User currentUser, Locale locale);
	
	public List<CasePresentation> getSortedCases(List<CasePresentation> casesToSort, Locale locale);
	
	public CasePresentationInfo getTenderCaseInfo(Object caseId);

	public boolean isSubscribed(IWApplicationContext iwac, User user, String caseId);
	
	public String getLinkToSubscribedCase(IWContext iwc, User user, String caseId);
	
	public String getLinkToSubscribedCase(IWContext iwc, User user, Long processInstanceId);
	
	public boolean doSubscribeToCase(IWContext iwc, User user,String caseId);
	public boolean doSubscribeToCase(IWContext iwc, User user, Case theCase);
	public boolean doSubscribeToCase(IWContext iwc, Collection<User> users, Case theCase);
	
	public boolean doUnSubscribeFromCase(IWContext iwc, Collection<User> users, Case theCase);
	
	public boolean canManageCaseSubscribers(User user);
	
	public CaseBusiness getCaseBusiness(IWApplicationContext iwac);
	
	public boolean disableToSeeAllAttachments(ProcessInstanceW processInstance);
	
	public boolean disableToSeeAllAttachmentsForNonPayers(TaskInstanceW currentTask);
	
	public boolean enableToSeeAllAttachmentsForUser(ProcessInstanceW processInstance, User user);
	
	public boolean disableToSeeAllAttachmentsForUser(ProcessInstanceW processInstance, User user);
	
	public ProcessInstanceW getProcessInstanceW(String caseId);
	public ProcessInstanceW getProcessInstanceW(Long processInstanceId);
	
	public Case getCase(Long processInstanceId);
	
	public Collection<User> getPayers(String caseId);
	
	public String getMetaDataKey(String caseId);
	
	public boolean removePayers(Collection<User> users, String caseId);
	
	public boolean setPayers(Collection<User> users, String caseId);
}