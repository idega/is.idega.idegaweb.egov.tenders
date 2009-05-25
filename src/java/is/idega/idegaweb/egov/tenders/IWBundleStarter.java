package is.idega.idegaweb.egov.tenders;

import java.util.Collection;

import com.idega.block.process.data.CaseCode;
import com.idega.block.process.data.CaseCodeHome;
import com.idega.data.IDOLookup;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWBundleStartable;
import com.idega.util.ListUtil;

/**
 * Bundle starter for Tenders project. Creates case code for Tenders cases.
 * 
 * @author <a href="mailto:valdas@idega.com">Valdas Žemaitis</a>
 * @version $Revision: 1.1 $
 *
 * Last modified: $Date: 2009/05/25 13:51:37 $ by: $Author: valdas $
 */
public class IWBundleStarter implements IWBundleStartable {

	public void start(IWBundle starterBundle) {
		createTendersCaseCode();
	}

	public void stop(IWBundle starterBundle) {
	}
	
	private void createTendersCaseCode() {
		CaseCodeHome caseCodeHome = null;
		try {
			caseCodeHome = (CaseCodeHome) IDOLookup.getHome(CaseCode.class);
		} catch(Exception e) {
			e.printStackTrace();
		}
		if (caseCodeHome == null) {
			return;
		}
		
		Collection<CaseCode> allCodes = null;
		try {
			allCodes = caseCodeHome.findAllCaseCodes();
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		boolean codeExists = false;
		if (!ListUtil.isEmpty(allCodes)) {
			for (CaseCode code: allCodes) {
				if (TendersConstants.TENDER_CASES_CODE.equals(code.getCode())) {
					codeExists = true;
					break;
				}
			}
		}
		
		if (codeExists) {
			return;
		}
		
		try {
			CaseCode tenderCasesCodeEntity = caseCodeHome.create();
			tenderCasesCodeEntity.setCode(TendersConstants.TENDER_CASES_CODE);
			tenderCasesCodeEntity.setDescription("Code for all Tender applications");
			tenderCasesCodeEntity.store();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

}
