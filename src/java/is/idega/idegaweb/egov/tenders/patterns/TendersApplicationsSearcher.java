package is.idega.idegaweb.egov.tenders.patterns;

import java.util.Map;
import java.util.regex.Pattern;

import javax.mail.MessagingException;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.block.email.bean.FoundMessagesInfo;
import com.idega.block.email.bean.MessageParserType;
import com.idega.block.email.client.business.EmailParams;
import com.idega.block.email.patterns.DefaultSubjectPatternFinder;

@Service
@Scope(BeanDefinition.SCOPE_SINGLETON)
public class TendersApplicationsSearcher extends DefaultSubjectPatternFinder {

	private static final long serialVersionUID = 7328581020368368856L;

	private static final String IDENTIFIER_REGULAR_EXPRESSION = "\\d+-[UF]\\d\\d";
	private static final Pattern IDENTIFIER_PATTERN = Pattern.compile(IDENTIFIER_REGULAR_EXPRESSION);

	public TendersApplicationsSearcher() {
		super();

		addPattern(IDENTIFIER_PATTERN);
	}

	@Override
	public Map<String, FoundMessagesInfo> getSearchResultsFormatted(EmailParams params) throws MessagingException {
		return super.getCaseIdentifierSearchResultsFormatted(params);
	}

	@Override
	public MessageParserType getParserType() {
		return MessageParserType.BPM;
	}

}