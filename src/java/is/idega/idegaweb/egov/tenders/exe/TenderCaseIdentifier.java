package is.idega.idegaweb.egov.tenders.exe;

import is.idega.idegaweb.egov.bpm.cases.exe.CaseIdentifier;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.idegaweb.egov.bpm.data.CaseProcInstBind;
import com.idega.util.CoreConstants;
import com.idega.util.IWTimestamp;

@Scope(BeanDefinition.SCOPE_SINGLETON)
@Service
@Qualifier("tenderCaseIdentifier")
public class TenderCaseIdentifier extends CaseIdentifier {
	
	@Override
	public synchronized Object[] generateNewCaseIdentifier() {
		IWTimestamp currentDate = new IWTimestamp(System.currentTimeMillis());
		
		Integer number = 1;
		String year = null;
		CaseProcInstBind lastCase = getCasesBPMDAO().getLastCreatedCaseProcInstBind();
		if (lastCase != null && lastCase.getDateCreated() != null) {
			IWTimestamp lastCaseDate = new IWTimestamp(lastCase.getDateCreated());
			if (lastCaseDate.getYear() == currentDate.getYear()) {
				number = lastCase.getCaseIdentierID() + 1;
			}
		}
		
		//	Year
		if (year == null) {
			year = String.valueOf(currentDate.getYear());
		}
		if (year.length() > 2) {
			year = year.substring(year.length() - 2);
		}
		
		//	Number
		String numberValue = String.valueOf(number);
		while (numberValue.length() < 3) {
			numberValue = new StringBuilder("0").append(numberValue).toString();
		}
		
		//	U or V
		String letter = getLetter();
		
		//	01
		String lastNumner = getLastNumber();
		
		Object[] data = new Object[2];
		data[0] = number;
		data[1] = new StringBuilder(year).append(numberValue).append(CoreConstants.MINUS).append(letter).append(lastNumner).toString();
	
		return data;
	}
	
	private String getLetter() {
		return "U";		//	By default using U
	}
	
	private String getLastNumber() {
		return "01";	//	By default using 01
	}

}
