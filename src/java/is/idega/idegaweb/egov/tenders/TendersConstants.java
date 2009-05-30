package is.idega.idegaweb.egov.tenders;

/**
 * Constants for tenders bundle
 * 
 * @author <a href="mailto:valdas@idega.com">Valdas Žemaitis</a>
 * @version $Revision: 1.4 $
 *
 * Last modified: $Date: 2009/05/30 09:33:59 $ by: $Author: valdas $
 */
public class TendersConstants {

	public static final String IW_BUNDLE_IDENTIFIER = "is.idega.idegaweb.egov.tenders";
	
	public static final String TENDER_CASE_START_DATE_VARIABLE = "date_whenTheTenderShouldBeDisplayed";
	public static final String TENDER_CASE_END_DATE_VARIABLE = "date_lastDayToSendBids";
	public static final String TENDER_CASE_IS_PRIVATE = "string_caseIsPrivate";
	
	public static final String TENDER_CASE_TENDER_NAME_VARIABLE = "string_caseDescription";
	public static final String TENDER_CASE_TENDER_ISSUER_VARIABLE = "string_issuerOfTender";
	public static final String TENDER_CASE_JOB_DESCRIPTION_VARIABLE = "string_descriptionOfJob";
	
	public static final String TENDER_CASES_CODE = "TENDER";
	
	public static final String TENDER_CASES_HANDLER_ROLE = "bpm_engineering_tenders_handler";
	public static final String TENDER_CASES_OWNER_ROLE = "bpm_engineering_tenders_owner";
	
	public static final String SPECIAL_TENDER_CASE_PAGE_REDIRECTOR_REQUESTED = "specialTenderCasePageRedirectorRequested";
	
	public static final String PAGE_TYPE_CASE_VIEWER = "tender_case_viewer";
	public static final String PAGE_TYPE_CASE_MANAGER = "tender_case_manager";
}
