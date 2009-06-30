package is.idega.idegaweb.egov.tenders.exe;

import is.idega.idegaweb.egov.bpm.cases.exe.CaseIdentifier;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.util.CoreConstants;
import com.idega.util.IWTimestamp;

@Scope(BeanDefinition.SCOPE_SINGLETON)
@Service
@Qualifier("tenderCaseIdentifier")
public class TenderCaseIdentifier extends CaseIdentifier {
	
	@Override
	public synchronized Object[] generateNewCaseIdentifier() {
		Object[] data = super.generateNewCaseIdentifier();
		
		CaseIdentifierBean identifierBean = getCaseIdentifierBean();
		Integer number = identifierBean.getNumber();
		IWTimestamp time = identifierBean.getTime();
		
		//	Year
		String year = String.valueOf(time.getYear());
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
		
		//	TODO: find out if we need spaces
		data[1] = new StringBuilder(year).append(numberValue).append(CoreConstants.SPACE).append(CoreConstants.MINUS).append(CoreConstants.SPACE).append(letter)
			.append(CoreConstants.SPACE).append(lastNumner).toString();
	
		return data;
	}
	
	private String getLetter() {
		return "U";		//	TODO: find out when is U and when is V
	}
	
	private String getLastNumber() {
		return "01";	//	TODO: find out if it's correct
	}

}
