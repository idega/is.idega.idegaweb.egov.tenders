package is.idega.idegaweb.egov.tenders;

import java.util.Arrays;
import java.util.Collection;

import org.springframework.beans.factory.annotation.Autowired;

import com.idega.block.process.data.CaseCode;
import com.idega.block.process.data.CaseCodeHome;
import com.idega.data.IDOLookup;
import com.idega.idegaweb.IWBundle;
import com.idega.idegaweb.IWBundleStartable;
import com.idega.jbpm.data.VariableInstanceQuerier;
import com.idega.util.ListUtil;
import com.idega.util.expression.ELUtil;

/**
 * Bundle starter for Tenders project. Creates case code for Tenders cases.
 *
 * @author <a href="mailto:valdas@idega.com">Valdas Žemaitis</a>
 * @version $Revision: 1.1 $
 *
 * Last modified: $Date: 2009/05/25 13:51:37 $ by: $Author: valdas $
 */
public class IWBundleStarter implements IWBundleStartable {

	@Autowired
	private VariableInstanceQuerier querier;

	private VariableInstanceQuerier getVariableInstanceQuerier() {
		if (querier == null) {
			ELUtil.getInstance().autowire(this);
		}
		return querier;
	}

	@Override
	public void start(IWBundle starterBundle) {
		createTendersCaseCode();

		getVariableInstanceQuerier().loadVariables(Arrays.asList(
				TendersConstants.TENDER_CASE_START_DATE_VARIABLE,
				TendersConstants.TENDER_CASE_END_DATE_VARIABLE,
				TendersConstants.TENDER_CASE_IS_PRIVATE_VARIABLE,
				TendersConstants.TENDER_CASE_IS_PAYMENT_VARIABLE)
		);
	}

	@Override
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
