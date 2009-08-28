package is.idega.idegaweb.egov.tenders.bean;

import java.io.Serializable;
import java.util.Date;

import com.idega.util.StringUtil;

public class TenderApplicationData implements Serializable {

	private static final long serialVersionUID = -1387333272903656257L;
	
	private String identifier;
	
	private String privateCaseValue;
	private String paymentCaseValue;
	
	private Date whenTheTenderShouldBeDisplayed;
	private Date lastDayToSendBids;
	
	private String deadlineToSendBids;
	
	public String getPrivateCaseValue() {
		return privateCaseValue;
	}
	public void setPrivateCaseValue(String privateCaseValue) {
		this.privateCaseValue = privateCaseValue;
	}
	public String getPaymentCaseValue() {
		return paymentCaseValue;
	}
	public void setPaymentCaseValue(String paymentCaseValue) {
		this.paymentCaseValue = paymentCaseValue;
	}
	public Date getWhenTheTenderShouldBeDisplayed() {
		return whenTheTenderShouldBeDisplayed;
	}
	public void setWhenTheTenderShouldBeDisplayed(Date whenTheTenderShouldBeDisplayed) {
		this.whenTheTenderShouldBeDisplayed = whenTheTenderShouldBeDisplayed;
	}
	public Date getLastDayToSendBids() {
		return lastDayToSendBids;
	}
	public void setLastDayToSendBids(Date lastDayToSendBids) {
		this.lastDayToSendBids = lastDayToSendBids;
	}
	
	private boolean getBooleanValue(String value) {
		return StringUtil.isEmpty(value) ? false : Boolean.valueOf(value);
	}
	
	public boolean isPrivateCase() {
		return getBooleanValue(getPrivateCaseValue());
	}
	
	public boolean isPaymentCase() {
		return getBooleanValue(getPaymentCaseValue());
	}
	public String getIdentifier() {
		return identifier;
	}
	public void setIdentifier(String identifier) {
		this.identifier = identifier;
	}
	public String getDeadlineToSendBids() {
		return deadlineToSendBids;
	}
	public void setDeadlineToSendBids(String deadlineToSendBids) {
		this.deadlineToSendBids = deadlineToSendBids;
	}
	
}
