package is.idega.idegaweb.egov.tenders.business;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.directwebremoting.annotations.Param;
import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;
import org.directwebremoting.spring.SpringCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.block.process.business.CaseBusiness;
import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.dwr.business.DWRAnnotationPersistance;
import com.idega.idegaweb.IWApplicationContext;
import com.idega.presentation.IWContext;
import com.idega.user.data.User;
import com.idega.util.CoreUtil;
import com.idega.util.StringUtil;

/**
 * DWR and Spring bean to handle Tenders logic
 * 
 * @author <a href="mailto:valdas@idega.com">Valdas Žemaitis</a>
 * @version $Revision: 1.1 $
 *
 * Last modified: $Date: 2009/05/25 13:51:37 $ by: $Author: valdas $
 */
@Scope(BeanDefinition.SCOPE_SINGLETON)
@Service(TendersSubscriber.SPRING_BEAN_NAME)
@RemoteProxy(creator=SpringCreator.class, creatorParams={
		@Param(name="beanName", value=TendersSubscriber.SPRING_BEAN_NAME),
		@Param(name="javascript", value=TendersSubscriber.DWR_OBJECT)
	}, name=TendersSubscriber.DWR_OBJECT)
public class TendersSubscriber implements DWRAnnotationPersistance {

	static final String SPRING_BEAN_NAME = "tendersSubscriberToCasesBean";
	public static final String DWR_OBJECT = "TendersSubscriber";

	private static final Logger LOGGER = Logger.getLogger(TendersSubscriber.class.getName());
	
	@Autowired
	private TendersHelper tendersHelper;
	
	@RemoteMethod
	public String subscribe(String caseId, Long processInstanceId) {
		if (StringUtil.isEmpty(caseId) || processInstanceId == null) {
			return null;
		}
		
		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			return null;
		}
		
		User currentUser = iwc.isLoggedOn() ? iwc.getCurrentUser() : null;
		if (currentUser == null) {
			LOGGER.warning("Can not add case ('"+caseId+"') to user's cases because user is not logged on!");
			return null;
		}
		
		String uri = tendersHelper.getLinkToSubscribedCase(iwc, currentUser, processInstanceId);
		if (StringUtil.isEmpty(uri)) {
			LOGGER.warning("Unable to resolve URI to case: " + caseId);
			return null;
		}

		CaseBusiness caseBusiness = getCaseBusiness(iwc);
		if (caseBusiness == null) {
			return null;
		}
		if (caseBusiness.addSubscriber(caseId, currentUser)) {
			return uri;
		}
		
		return null;
	}
	
	private CaseBusiness getCaseBusiness(IWApplicationContext iwac) {
		try {
			return IBOLookup.getServiceInstance(iwac, CaseBusiness.class);
		} catch (IBOLookupException e) {
			LOGGER.log(Level.WARNING, "Error getting " + CaseBusiness.class, e);
		}
		return null;
	}
}
