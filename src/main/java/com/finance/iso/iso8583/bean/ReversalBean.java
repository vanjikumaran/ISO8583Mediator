package com.finance.iso.iso8583.bean;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOUtil;

public class ReversalBean {
	private String field7;
	private String field11;
	private String field32;
	private String field37;
	private String userData;
	private String amount;
	private String destination;
	private String destinationCode;
	private String operationType;
	
	public ReversalBean(String field7, String field11,String field32,String field37, String userData, String amount, String destination, String destinationCode, String operationType) {
		this.field7=field7;
		this.field11=field11;
		this.field32=field32;
		this.field37=field37;
		this.userData=userData;
		this.amount=amount;
		this.destination=destination;
		this.destinationCode=destinationCode;
		this.setOperationType(operationType);
	}
	
	public String getField7() {
		return field7;
	}
	public void setField7(String field7) {
		this.field7 = field7;
	}
	public String getField11() {
		return field11;
	}
	public void setField11(String field11) {
		this.field11 = field11;
	}
	public String getField37() {
		return field37;
	}
	public void setField37(String field37) {
		this.field37 = field37;
	}
	public String getUserData() {
		return userData;
	}
	public void setUserData(String userData) {
		this.userData = userData;
	}
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	public String getDestination() {
		return destination;
	}
	public void setDestination(String destination) {
		this.destination = destination;
	}
	public String getDestinationCode() {
		return destinationCode;
	}
	public void setDestinationCode(String destinationCode) {
		this.destinationCode = destinationCode;
	}
	
	public String getReversalKey(){
		try {
			return "0200" + field11 + field7
					+ ISOUtil.padleft(field37, 16, '0');
		} catch (ISOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public String getOperationType() {
		return operationType;
	}

	public void setOperationType(String operationType) {
		this.operationType = operationType;
	}

	public String getField32() {
		return field32;
	}

	public void setField32(String field32) {
		this.field32 = field32;
	}

}
