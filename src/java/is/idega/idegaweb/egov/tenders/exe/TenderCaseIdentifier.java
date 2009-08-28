package is.idega.idegaweb.egov.tenders.exe;

import is.idega.idegaweb.egov.bpm.cases.CasesBPMProcessConstants;
import is.idega.idegaweb.egov.bpm.cases.exe.CaseIdentifier;

import java.util.Arrays;
import java.util.Collection;

import org.jbpm.context.exe.VariableInstance;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.idegaweb.egov.bpm.data.CaseProcInstBind;
import com.idega.util.CoreConstants;
import com.idega.util.IWTimestamp;
import com.idega.util.ListUtil;
import com.idega.util.StringUtil;

@Scope(BeanDefinition.SCOPE_SINGLETON)
@Service
@Qualifier("tenderCaseIdentifier")
public class TenderCaseIdentifier extends CaseIdentifier {
	
	@Override
	public synchronized Object[] generateNewCaseIdentifier() {
		return generateNewCaseIdentifier(null);
	}
	
	private Object[] generateNewCaseIdentifier(String usedIdentifier) {
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
		String numberValue = getNumbersPart(usedIdentifier, number, year);
		
		//	U or V
		String letter = getLetter();
		
		//	01
		String lastNumner = getLastNumber();
		
		Object[] data = new Object[2];
		data[0] = number;
		String newIdentifier = new StringBuilder(year).append(numberValue).append(CoreConstants.MINUS).append(letter).append(lastNumner).toString();
		data[1] = newIdentifier;
	
		return canUseIdentifier(newIdentifier) ? data : generateNewCaseIdentifier(newIdentifier);
	}
	
	private String getNumbersPart(String usedIdentifier, Integer number, String year) {
		String numbersPart = null;
		if (StringUtil.isEmpty(usedIdentifier)) {
			numbersPart = String.valueOf(number);
		} else {
			number = Integer.valueOf(usedIdentifier.split(CoreConstants.MINUS)[0].replaceFirst(year, CoreConstants.EMPTY));
			number++;
			numbersPart = String.valueOf(number);
		}
		
		while (numbersPart.length() < 3) {
			numbersPart = new StringBuilder("0").append(numbersPart).toString();
		}
		return numbersPart;
	}
	
	private boolean canUseIdentifier(String identifier) {
		Collection<VariableInstance> variables = getCasesBPMDAO().getVariablesByNames(Arrays.asList(CasesBPMProcessConstants.caseIdentifier));
		if (ListUtil.isEmpty(variables)) {
			return true;
		}
		
		for (VariableInstance variable: variables) {
			if (identifier.equals(variable.getValue())) {
				return false;
			}
		}
		
		return true;
	}
	
	private String getLetter() {
		return "U";		//	By default using U
	}
	
	private String getLastNumber() {
		return "01";	//	By default using 01
	}

}
