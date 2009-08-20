package is.idega.idegaweb.egov.tenders;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Constants for tenders bundle
 * 
 * @author <a href="mailto:valdas@idega.com">Valdas Žemaitis</a>
 * @version $Revision: 1.5 $
 *
 * Last modified: $Date: 2009/06/10 07:05:00 $ by: $Author: valdas $
 */
public class TendersConstants {

	public static final String IW_BUNDLE_IDENTIFIER = "is.idega.idegaweb.egov.tenders";
	
	public static final String TENDER_CASE_START_DATE_VARIABLE = "date_whenTheTenderShouldBeDisplayed";
	public static final String TENDER_CASE_END_DATE_VARIABLE = "date_lastDayToSendBids";
	public static final String TENDER_CASE_IS_PRIVATE_VARIABLE = "string_caseIsPrivate";
	public static final String TENDER_CASE_IS_PAYMENT_VARIABLE = "string_paymentCase";
	
	public static final String TENDER_CASE_LAST_DATE_FOR_QUESTIONS = "lastDayForQuestions";
	public static final String TENDER_CASE_LAST_DATE_FOR_QUESTIONS_VARIABLE = "date_" + TENDER_CASE_LAST_DATE_FOR_QUESTIONS;
	public static final String TENDER_CASE_LAST_DAY_TO_ANSWER_QUESTIONS = "lastDayToAnswerQuestions";
	public static final String TENDER_CASE_LAST_DAY_TO_ANSWER_QUESTIONS_VARIABLE = "date_" + TENDER_CASE_LAST_DAY_TO_ANSWER_QUESTIONS;
	
	public static final String TENDER_CASE_TENDER_NAME_VARIABLE = "string_caseDescription";
	public static final String TENDER_CASE_TENDER_ISSUER_VARIABLE = "string_issuerOfTender";
	public static final String TENDER_CASE_JOB_DESCRIPTION_VARIABLE = "string_descriptionOfJob";
	
	public static final String TENDER_CASES_CODE = "TENDER";
	
	public static final String TENDER_CASES_HANDLER_ROLE = "bpm_engineering_tenders_handler";
	public static final String TENDER_CASES_OWNER_ROLE = "bpm_engineering_tenders_owner";
	public static final String TENDER_CASES_INVITED_ROLE = "bpm_engineering_tenders_invited";
	public static final String TENDERS_ROLE = "tenderusers";
	
	public static final List<String> TENDER_CASES_3RD_PARTIES_ROLES = Collections.unmodifiableList(Arrays.asList(
			TENDER_CASES_OWNER_ROLE,
			TENDER_CASES_INVITED_ROLE
	));
	
	public static final String SPECIAL_TENDER_CASE_PAGE_REDIRECTOR_REQUESTED = "specialTenderCasePageRedirectorRequested";
	
	public static final String PAGE_TYPE_CASE_VIEWER = "tender_case_viewer";
	public static final String PAGE_TYPE_CASE_MANAGER = "tender_case_manager";
	
	public static final String USER_HAS_PAYED_FOR_TENDER_CASE_ATTACHMENTS_META_DATA_KEY = "userHasPayedForTenderCaseAttachments";
}