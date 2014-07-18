package com.amb.xlink.iso8583.bean;

public class Transaction {
	private String date;
	private String amount;
	private String currency;
	private String txnType;
	
	public Transaction(String date, String amount, String currency, String txntype) {
		this.date=date;
		this.amount=amount;
		this.currency=currency;
		this.txnType=txntype;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getAmount() {
		return amount;
	}

	public void setAmount(String amount) {
		this.amount = amount;
	}

	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getTxnType() {
		return txnType;
	}

	public void setTxnType(String txnType) {
		this.txnType = txnType;
	}
	
	

}
