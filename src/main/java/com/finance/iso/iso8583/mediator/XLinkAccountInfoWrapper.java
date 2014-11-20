package com.finance.iso.iso8583.mediator;

public class XLinkAccountInfoWrapper {

	private String accountno=null;
	private String dob=null;

	public XLinkAccountInfoWrapper(String accountno, String dob) {
		// TODO Auto-generated constructor stub
		this.accountno = accountno;
		this.dob = dob;
	}

	public String getAccountno() {
		return accountno;
	}

	public void setAccountno(String accountno) {
		this.accountno = accountno;
	}

	public String getDOB() {
		return dob;
	}

	public void setDOB(String dob) {
		this.dob = dob;
	}


}
