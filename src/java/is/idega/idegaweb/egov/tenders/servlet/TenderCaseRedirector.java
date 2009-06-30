package is.idega.idegaweb.egov.tenders.servlet;

import is.idega.idegaweb.egov.cases.presentation.CasesProcessor;
import is.idega.idegaweb.egov.tenders.TendersConstants;
import is.idega.idegaweb.egov.tenders.business.TendersHelper;

import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;

import com.idega.presentation.IWContext;
import com.idega.servlet.filter.BaseFilter;
import com.idega.util.StringUtil;
import com.idega.util.expression.ELUtil;

public class TenderCaseRedirector extends BaseFilter implements Filter {

	private static final Logger LOGGER = Logger.getLogger(TenderCaseRedirector.class.getName());
	
	@Autowired
	private TendersHelper tendersHelper;
	
	public void destroy() {
	}

	public void doFilter(ServletRequest srequest, ServletResponse sresponse, FilterChain chain) throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) srequest;
		HttpServletResponse response = (HttpServletResponse) sresponse;
		IWContext iwc = getIWContext(request, response);
		
		if (!canRedirect(iwc)) {
			chain.doFilter(srequest, sresponse);
			return;
		}
		
		if (!doSubscribeToCase(iwc)) {
			chain.doFilter(srequest, sresponse);
			return;
		}
		
		String newUrl = getNewRedirectURL(iwc);
		if (StringUtil.isEmpty(newUrl)) {
			LOGGER.warning("Couldn't create uri to redirect to task viewer");
			chain.doFilter(srequest, sresponse);
			return;
		}
		response.sendRedirect(newUrl);
	}
	
	private boolean doSubscribeToCase(IWContext iwc) {
		return getTendersHelper().doSubscribeToCase(iwc, iwc.getCurrentUser(), iwc.getParameter(CasesProcessor.PARAMETER_CASE_PK));
	}
	
	private String getNewRedirectURL(IWContext iwc) {
		return getTendersHelper().getLinkToSubscribedCase(iwc, iwc.getCurrentUser(), iwc.getParameter(CasesProcessor.PARAMETER_CASE_PK));
	}
	
	private boolean canRedirect(IWContext iwc) {
		return iwc.isLoggedOn() && iwc.isParameterSet(TendersConstants.SPECIAL_TENDER_CASE_PAGE_REDIRECTOR_REQUESTED) &&
			iwc.isParameterSet(CasesProcessor.PARAMETER_CASE_PK);
	}

	public TendersHelper getTendersHelper() {
		if (tendersHelper == null) {
			ELUtil.getInstance().autowire(this);
		}
		return tendersHelper;
	}

	public void setTendersHelper(TendersHelper tendersHelper) {
		this.tendersHelper = tendersHelper;
	}

	public void init(FilterConfig arg0) throws ServletException {
	}

}
