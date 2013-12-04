package is.idega.idegaweb.egov.tenders.bean;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.company.business.CompanyBusiness;
import com.idega.company.data.Company;
import com.idega.core.accesscontrol.data.LoginRecord;
import com.idega.core.accesscontrol.data.LoginRecordHome;
import com.idega.core.business.DefaultSpringBean;
import com.idega.core.contact.data.Email;
import com.idega.core.contact.data.Phone;
import com.idega.data.IDOLookup;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.User;
import com.idega.util.CoreConstants;
import com.idega.util.IWTimestamp;
import com.idega.util.ListUtil;
import com.idega.util.StringUtil;
import com.idega.util.datastructures.map.MapUtil;

@Service(TenderUserViewBean.BEAN_NAME)
@Scope("request")
public class TenderUserViewBean extends DefaultSpringBean {

	public static final String BEAN_NAME = "tenderUserViewBean";

	private List<TenderUserData> data;

	public List<TenderUserData> getUsersData() {
		if (data != null) {
			return data;
		}

		data = new ArrayList<TenderUserData>();

		LoginRecordHome loginRecordsHome = null;
		try {
			loginRecordsHome = (LoginRecordHome) IDOLookup.getHome(LoginRecord.class);
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (loginRecordsHome == null) {
			return data;
		}

		Map<User, Date> lastLogins = null;
		try {
			lastLogins = loginRecordsHome.getLastLoginRecordsForAllUsers();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (MapUtil.isEmpty(lastLogins)) {
			return data;
		}

		Locale locale = getCurrentLocale();
		CompanyBusiness companyBusiness = getServiceInstance(CompanyBusiness.class);

		for (Map.Entry<User, Date> entry: lastLogins.entrySet()) {
			User user = entry.getKey();
			Collection<Company> companies = null;
			try {
				companies = companyBusiness.getCompaniesForUser(user);
			} catch (Exception e) {}
			if (ListUtil.isEmpty(companies)) {
				continue;
			}

			Date date = entry.getValue();
			TenderUserData userData = new TenderUserData(user.getName(), date);
			userData.setPersonalId(user.getPersonalID());
			String lastLoginDate = new IWTimestamp(date).getLocaleDateAndTime(locale, IWTimestamp.SHORT, IWTimestamp.SHORT);
			userData.setLastLoginDate(lastLoginDate);

			//	Companies
			List<Phone> phones = new ArrayList<Phone>();
			List<Email> emails = new ArrayList<Email>();
			StringBuilder companiesValue = new StringBuilder();
			for (Iterator<Company> companiesIter = companies.iterator(); companiesIter.hasNext();) {
				Company company = companiesIter.next();
				companiesValue.append(company.getName());
				if (companiesIter.hasNext()) {
					companiesValue.append(CoreConstants.COMMA).append(CoreConstants.SPACE);
				}

				Phone phone = company.getPhone();
				if (phone != null) {
					phones.add(phone);
				}
				Email email = company.getEmail();
				if (email != null) {
					emails.add(email);
				}
			}
			userData.setCompany(companiesValue.toString());

			//	Phones
			StringBuilder phonesValue = new StringBuilder();
			Collection<Phone> userPhones = user.getPhones();
			if (!ListUtil.isEmpty(userPhones)) {
				phones.addAll(userPhones);
			}
			Map<String, Boolean> addedPhones = new HashMap<String, Boolean>();
			boolean phoneAdded = false;
			for (Iterator<Phone> phonesIter = phones.iterator(); phonesIter.hasNext();) {
				Phone phone = phonesIter.next();
				String number = phone.getNumber();
				if (!StringUtil.isEmpty(number) && !addedPhones.containsKey(number)) {
					addedPhones.put(number, Boolean.TRUE);

					if (phoneAdded) {
						phonesValue.append(CoreConstants.COMMA).append(CoreConstants.SPACE);
					} else {
						phoneAdded = true;
					}
					phonesValue.append(number);
				}
			}
			userData.setPhones(phonesValue.toString());

			// Emails
			StringBuilder emailsValue = new StringBuilder();
			Collection<Email> userEmails = user.getEmails();
			if (!ListUtil.isEmpty(userEmails)) {
				emails.addAll(userEmails);
			}
			Map<String, Boolean> addedEmails = new HashMap<String, Boolean>();
			for (Iterator<Email> emailsIter = emails.iterator(); emailsIter.hasNext();) {
				Email email = emailsIter.next();
				String address = email.getEmailAddress();
				if (!StringUtil.isEmpty(address) && !addedEmails.containsKey(address)) {
					addedEmails.put(address, Boolean.TRUE);
					emailsValue.append(address);
					if (emailsIter.hasNext()) {
						emailsValue.append(CoreConstants.COMMA).append(CoreConstants.SPACE);
					}
				}
			}
			userData.setEmails(emailsValue.toString());

			data.add(userData);
		}

		final Collator collator = Collator.getInstance(getCurrentLocale());
		Collections.sort(data, new Comparator<TenderUserData>() {

			@Override
			public int compare(TenderUserData user1, TenderUserData user2) {
				return collator.compare(user1.getName(), user2.getName());
			}
		});
		return data;
	}

	public UserBusiness getUserBusiness(){
		return getServiceInstance(UserBusiness.class);
	}
}
