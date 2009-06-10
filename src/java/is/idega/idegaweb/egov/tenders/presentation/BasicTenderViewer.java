package is.idega.idegaweb.egov.tenders.presentation;

import java.util.Arrays;

import is.idega.idegaweb.egov.application.IWBundleStarter;
import is.idega.idegaweb.egov.tenders.TendersConstants;
import is.idega.idegaweb.egov.tenders.bean.CasePresentationInfo;
import is.idega.idegaweb.egov.tenders.business.TendersHelper;

import org.springframework.beans.factory.annotation.Autowired;

import com.idega.block.process.data.Case;
import com.idega.block.web2.business.JQuery;
import com.idega.block.web2.business.Web2Business;
import com.idega.presentation.Block;
import com.idega.presentation.IWContext;
import com.idega.user.business.GroupHelper;
import com.idega.util.PresentationUtil;
import com.idega.util.expression.ELUtil;

public class BasicTenderViewer extends Block {

	@Autowired
	private JQuery jQuery;
	@Autowired
	private Web2Business web2;
	@Autowired
	private TendersHelper tendersHelper;
	@Autowired
	private GroupHelper groupHelper;
	
	private Case theCase;
	private String caseId;
	
	private CasePresentationInfo caseInfo;

	@Override
	public void main(IWContext iwc) throws Exception {
		ELUtil.getInstance().autowire(this);
		
		PresentationUtil.addStyleSheetsToHeader(iwc, Arrays.asList(
				iwc.getIWMainApplication().getBundle(IWBundleStarter.IW_BUNDLE_IDENTIFIER).getVirtualPathWithFileNameString("style/application.css"),
				getBundle(iwc).getVirtualPathWithFileNameString("style/tenders.css")
		));
	}
	
	@Override
	public String getBundleIdentifier() {
		return TendersConstants.IW_BUNDLE_IDENTIFIER;
	}

	public JQuery getJQuery() {
		return jQuery;
	}

	public void setJQuery(JQuery query) {
		jQuery = query;
	}

	public Web2Business getWeb2() {
		return web2;
	}

	public void setWeb2(Web2Business web2) {
		this.web2 = web2;
	}

	public TendersHelper getTendersHelper() {
		return tendersHelper;
	}

	public void setTendersHelper(TendersHelper tendersHelper) {
		this.tendersHelper = tendersHelper;
	}

	public GroupHelper getGroupHelper() {
		return groupHelper;
	}

	public void setGroupHelper(GroupHelper groupHelper) {
		this.groupHelper = groupHelper;
	}

	public Case getTheCase() {
		if (theCase == null) {
			try {
				theCase = tendersHelper.getCaseBusiness(getIWApplicationContext()).getCase(caseId);
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
		return theCase;
	}

	public void setTheCase(Case theCase) {
		this.theCase = theCase;
	}

	public String getCaseId() {
		return caseId;
	}

	public void setCaseId(String caseId) {
		this.caseId = caseId;
	}

	public CasePresentationInfo getCaseInfo() {
		if (caseInfo == null) {
			caseInfo = getTendersHelper().getTenderCaseInfo(caseId);
		}
		return caseInfo;
	}

	public void setCaseInfo(CasePresentationInfo caseInfo) {
		this.caseInfo = caseInfo;
	}
	
}
