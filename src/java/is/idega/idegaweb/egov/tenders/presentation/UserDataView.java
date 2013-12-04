package is.idega.idegaweb.egov.tenders.presentation;

import is.idega.idegaweb.egov.tenders.TendersConstants;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import com.idega.facelets.ui.FaceletComponent;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWMainApplication;
import com.idega.presentation.IWBaseComponent;
import com.idega.presentation.IWContext;
import com.idega.util.PresentationUtil;

public class UserDataView  extends IWBaseComponent{

	@Override
	protected void initializeComponent(FacesContext context) {
		super.initializeComponent(context);
		
		IWContext iwc = IWContext.getIWContext(context);
		IWBundle iwb = IWMainApplication.getDefaultIWMainApplication().getBundle(
				TendersConstants.IW_BUNDLE_IDENTIFIER);
		
		PresentationUtil.addStyleSheetToHeader(iwc, iwb.getVirtualPathWithFileNameString("style/tenders.css"));
		
		
		
		
		UIComponent facelet = getIWMainApplication(iwc)
				.createComponent(FaceletComponent.COMPONENT_TYPE);		
		if (facelet instanceof FaceletComponent) {
			((FaceletComponent) facelet).setFaceletURI(iwb.getFaceletURI("user_data_view.xhtml"));
		}

		add(facelet);
		
	}
	
}
