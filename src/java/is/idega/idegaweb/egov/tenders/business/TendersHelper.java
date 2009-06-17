package is.idega.idegaweb.egov.tenders.business;

import is.idega.idegaweb.egov.tenders.bean.CasePresentationInfo;

import java.util.Collection;
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
 * @version $Revision: 1.8 $
 *
 * Last modified: $Date: 2009/06/17 15:24:24 $ by: $Author: valdas $
 */
public interface TendersHelper {
	
	public PagedDataCollection<CasePresentation> getAllCases(Locale locale, String statusesToHide, String statusesToShow);

	public Collection<CasePresentation> getValidTendersCases(Collection<CasePresentation> cases, User currentUser, Locale locale);
	
	public CasePresentationInfo getTenderCaseInfo(Object caseId);

	public boolean isSubscribed(IWApplicationContext iwac, User user, String caseId);
	
	public String getLinkToSubscribedCase(IWContext iwc, User user, String caseId);
	
	public String getLinkToSubscribedCase(IWContext iwc, User user, Long processInstanceId);
	
	public boolean doSubscribeToCase(IWContext iwc, User user, String caseId);
	
	public boolean canManageCaseSubscribers(User user);
	
	public CaseBusiness getCaseBusiness(IWApplicationContext iwac);
	
	public boolean disableToSeeAllAttachments(TaskInstanceW taskInstance);
	
	public boolean enableToSeeAllAttachmentsForUser(ProcessInstanceW processInstance, User user);
	
	public boolean disableToSeeAllAttachmentsForUser(ProcessInstanceW processInstance, User user);
	
	public ProcessInstanceW getProcessInstance(String caseId);
	public ProcessInstanceW getProcessInstance(Long processInstanceId);
	
	public Case getCase(Long processInstanceId);
	
	public Collection<User> getPayers(String caseId);
	
	public String getMetaDataKey(String caseId);
}