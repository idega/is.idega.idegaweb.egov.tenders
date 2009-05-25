package is.idega.idegaweb.egov.tenders.business;

import is.idega.idegaweb.egov.tenders.CasePresentationInfo;

import java.util.Collection;
import java.util.Locale;

import com.idega.block.process.presentation.beans.CasePresentation;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.presentation.IWContext;
import com.idega.presentation.paging.PagedDataCollection;
import com.idega.user.data.User;

/**
 * Methods for Tenders
 * 
 * @author <a href="mailto:valdas@idega.com">Valdas Žemaitis</a>
 * @version $Revision: 1.1 $
 *
 * Last modified: $Date: 2009/05/25 13:51:37 $ by: $Author: valdas $
 */
public interface TendersHelper {
	
	public PagedDataCollection<CasePresentation> getAllCases(Locale locale, String statusesToHide, String statusesToShow);

	public Collection<CasePresentation> getValidTendersCases(Collection<CasePresentation> cases);
	
	public CasePresentationInfo getTenderCaseInfo(Object caseId);

	public boolean isSubscribed(IWApplicationContext iwac, User user, String caseId);
	
	public String getLinkToSubscribedCase(IWContext iwc, User user, String caseId);
	
	public String getLinkToSubscribedCase(IWContext iwc, User user, Long processInstanceId);
}
