package is.idega.idegaweb.egov.tenders.bean;

import java.io.Serializable;
import java.util.Date;

public class TenderUserData implements Serializable {

	private static final long serialVersionUID = 535491709181054599L;

	private String name;

	private String company;

	private String lastLoginDate;
	private Date loginDate;

	private String personalId;

	private String phones;

	private String emails;

	public TenderUserData(String name, Date loginDate) {
		super();

		this.name = name;
		this.loginDate = loginDate;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getCompany() {
		return company;
	}

	public void setCompany(String company) {
		this.company = company;
	}

	public String getLastLoginDate() {
		return lastLoginDate;
	}

	public void setLastLoginDate(String lastLoginDate) {
		this.lastLoginDate = lastLoginDate;
	}

	public String getPersonalId() {
		return personalId;
	}

	public void setPersonalId(String personalId) {
		this.personalId = personalId;
	}

	public String getPhones() {
		return phones;
	}

	public void setPhones(String phones) {
		this.phones = phones;
	}

	public String getEmails() {
		return emails;
	}

	public void setEmails(String emails) {
		this.emails = emails;
	}

	public Date getLoginDate() {
		return loginDate;
	}

	public void setLoginDate(Date loginDate) {
		this.loginDate = loginDate;
	}

}
