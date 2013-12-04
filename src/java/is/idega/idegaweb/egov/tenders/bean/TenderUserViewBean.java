package is.idega.idegaweb.egov.tenders.bean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;

import javax.ejb.FinderException;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import com.idega.business.IBOLookup;
import com.idega.business.IBOLookupException;
import com.idega.company.business.CompanyBusiness;
import com.idega.company.data.Company;
import com.idega.core.accesscontrol.data.LoginRecord;
import com.idega.core.accesscontrol.data.LoginRecordHome;
import com.idega.core.business.DefaultSpringBean;
import com.idega.core.contact.data.Email;
import com.idega.core.contact.data.Phone;
import com.idega.data.IDOLookup;
import com.idega.data.IDOLookupException;
import com.idega.idegaweb.IWMainApplication;
import com.idega.user.business.UserBusiness;
import com.idega.user.data.User;
import com.idega.util.IWTimestamp;
import com.idega.util.ListUtil;
import com.idega.util.StringUtil;


@Service(TenderUserViewBean.BEAN_NAME)
@Scope("request")
public class TenderUserViewBean  extends DefaultSpringBean {

	public static final String BEAN_NAME = "tenderUserViewBean";
	
	public List<TenderUserData> getUsersData(){
		Collection<User> users;
		try {
			users = getUserBusiness().getUserHome().findAllUsers();
		} catch (Exception e) {
			getLogger().log(Level.WARNING, "Failed getting users", e);
			users = Collections.emptyList();
		}
		CompanyBusiness companyBusiness = null;
		try {
			companyBusiness = IBOLookup.getServiceInstance(IWMainApplication.getDefaultIWApplicationContext(), CompanyBusiness.class);
		} catch (IBOLookupException e) {
			getLogger().log(Level.WARNING, "Failed getting users", e);
			users = Collections.emptyList();
		}
		List<TenderUserData> tendersUsers = new ArrayList<TenderUserData>(users.size());
		LoginRecordHome loginrecorshoHome;
		try {
			loginrecorshoHome = (LoginRecordHome) IDOLookup.getHome(LoginRecord.class);
		} catch (IDOLookupException e) {
			loginrecorshoHome = null;
			getLogger().log(Level.WARNING, "Failed getting loginrecordhome", e);
		}
		Locale locale = getCurrentLocale();
		for(User user : users){
			TenderUserData tenderUserData = new TenderUserData();
			tendersUsers.add(tenderUserData);
			
			tenderUserData.setName(user.getName());
			Collection<Phone> phones =  user.getPhones();
			if(!ListUtil.isEmpty(phones)){
				StringBuilder builder = new StringBuilder();
				boolean added = false;
				for(Phone phone : phones){
					String number = phone.getNumber();
					if(StringUtil.isEmpty(number)){
						continue;
					}
					if(added){
						builder.append(", ");
					}else{
						added = true;
					}
					builder.append(number);
				}
				tenderUserData.setPhones(builder.toString());
			}
			Collection<Email> emails =  user.getEmails();
			if(!ListUtil.isEmpty(emails)){
				StringBuilder builder = new StringBuilder();
				boolean added = false;
				for(Email email : emails){
					String emailAddress = email.getEmailAddress();
					if(StringUtil.isEmpty(emailAddress)){
						continue;
					}
					if(added){
						builder.append(", ");
					}else{
						added = true;
					}
					builder.append(emailAddress);
				}
				tenderUserData.setEmails(builder.toString());
			}
			tenderUserData.setPersonalId(user.getPersonalID());
			Collection<Company> companies =  companyBusiness.getCompaniesForUser(user);
			if(!ListUtil.isEmpty(companies)){
				StringBuilder builder = new StringBuilder();
				boolean added = false;
				for(Company company : companies){
					String name = company.getName();
					if(StringUtil.isEmpty(name)){
						continue;
					}
					if(added){
						builder.append(", ");
					}else{
						added = true;
					}
					builder.append(name);
				}
				tenderUserData.setCompany(builder.toString());
			}
			try {
				LoginRecord loginRecord = loginrecorshoHome.findLastLoginRecord(user);
				String date = new IWTimestamp(loginRecord.getLogInStamp()).getLocaleDateAndTime(locale);
				tenderUserData.setLastLoginDate(date);
			} catch (FinderException e) {
//				getLogger().log(Level.WARNING, "Failed getting login record of user "+user.getId(), e);
			}
		}
		
		return tendersUsers;
	}
	
	public UserBusiness getUserBusiness(){
		return getServiceInstance(UserBusiness.class);
	}
}
