package is.idega.idegaweb.egov.tenders.business;

import is.idega.idegaweb.egov.tenders.TendersConstants;

import java.util.logging.Logger;

import org.directwebremoting.annotations.Param;
import org.directwebremoting.annotations.RemoteMethod;
import org.directwebremoting.annotations.RemoteProxy;
import org.directwebremoting.spring.SpringCreator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.builder.bean.AdvancedProperty;
import com.idega.dwr.business.DWRAnnotationPersistance;
import com.idega.idegaweb.IWResourceBundle;
import com.idega.presentation.IWContext;
import com.idega.user.data.User;
import com.idega.util.CoreUtil;
import com.idega.util.StringUtil;

/**
 * DWR and Spring bean to handle Tenders logic
 * 
 * @author <a href="mailto:valdas@idega.com">Valdas Žemaitis</a>
 * @version $Revision: 1.3 $
 *
 * Last modified: $Date: 2009/05/28 12:59:35 $ by: $Author: valdas $
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
	public AdvancedProperty subscribe(String caseId, Long processInstanceId) {
		String errorMessage = "Sorry, unable to subscribe to this case";
		AdvancedProperty result = new AdvancedProperty(errorMessage);
		
		if (StringUtil.isEmpty(caseId) || processInstanceId == null) {
			return result;
		}
		
		IWContext iwc = CoreUtil.getIWContext();
		if (iwc == null) {
			return result;
		}
		
		IWResourceBundle iwrb = iwc.getIWMainApplication().getBundle(TendersConstants.IW_BUNDLE_IDENTIFIER).getResourceBundle(iwc);
		
		User currentUser = iwc.isLoggedOn() ? iwc.getCurrentUser() : null;
		if (currentUser == null) {
			LOGGER.warning("Can not add case ('"+caseId+"') to user's cases because user is not logged on!");
			result.setId(iwrb.getLocalizedString("tenders_subscriber.error_user_not_logged", "Unable to subscribe to this case because you are not logged in"));
			return result;
		}
		
		String uri = tendersHelper.getLinkToSubscribedCase(iwc, currentUser, processInstanceId);
		if (StringUtil.isEmpty(uri)) {
			LOGGER.warning("Unable to resolve URI to case: " + caseId);
			result.setId(iwrb.getLocalizedString("tenders_subscriber.error_generating_case_viewer_link", errorMessage));
			return result;
		}

		if (tendersHelper.doSubscribeToCase(iwc, currentUser, caseId)) {
			result.setValue(uri);
			result.setId(iwrb.getLocalizedString("tenders_subscriber.successfully_subscribed", "You have successfully subscribed to this case"));
			return result;
		}
		
		result.setId(iwrb.getLocalizedString("tenders_subscriber.error_subscribing", errorMessage));
		return result;
	}
}
