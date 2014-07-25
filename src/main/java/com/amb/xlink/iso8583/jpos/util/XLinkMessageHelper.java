package com.amb.xlink.iso8583.jpos.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axis2.context.MessageContext;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;

import com.amb.xlink.iso8583.bean.Transaction;
import com.amb.xlink.iso8583.mediator.XLinkAccountInfoWrapper;
import com.amb.xlink.iso8583.mediator.XLinkISO8583Mediator;
import com.amb.xlink.iso8583.mediator.XLinkISO8583Util;
import com.amb.xlink.iso8583.mediator.XLinkSessionWrapper;
import com.sun.org.apache.xalan.internal.xsltc.runtime.Hashtable;

public class XLinkMessageHelper {

	private static final String OPERATION_TYPE = "OperationType";

	private static final String REVERSAL_PROPERTY = "RevOperationType";
	
	private static final Log log = LogFactory.getLog(XLinkMessageHelper.class);

	/**
	 * @param axis2MsgCtx
	 * @return
	 * @throws ISOException
	 */
	public static ISOMsg createFinacialMessage(MessageContext axis2MsgCtx,org.apache.synapse.MessageContext synCtx,
			XLinkAccountInfoWrapper accountInfoWrapper, XLinkSessionWrapper sessionWrapper) throws ISOException {
		ISOMsg request = new ISOMsg();
		OMElement requestOM = axis2MsgCtx.getEnvelope().getBody()
				.getFirstElement();
		OMElement operationRequest = requestOM
				.getFirstChildWithName((new QName(OPERATION_TYPE)));

		// check if the reversal key exists set by the ResponseErrorSequence
		String revKey = (String) axis2MsgCtx.getProperty("reversalKey");
		if(revKey != null){
			log.info("Reversal Key found in MsgCtx: "+revKey);
		}

		// if above check null, we need to perform with jsonObject tag
		if (operationRequest == null) {
			log.info("OperationRewquest is null");
			OMElement jsonElm = requestOM.getFirstChildWithName((new QName(
					"APIRequest")));
			if (jsonElm != null) {
				log.info("API REquest is not null");
				operationRequest = jsonElm.getFirstChildWithName((new QName(
						OPERATION_TYPE)));
				requestOM = jsonElm;

			}
			
			if (axis2MsgCtx.getProperty("FORCE_ERROR_ON_SOURCE_FAULT")!=null){
				log.info("Setting Axis2 property");
				operationRequest = requestOM.cloneOMElement();
				operationRequest.setText("51");
			}

		}
		log.info("Operational REquest: "+operationRequest.getText());

//		TODO: Prepare the Reveesal Key here
		if (operationRequest != null) {
			switch (operationRequest.getText()) {
			case "30":
				InquiryUtils.createBalanceInquiryMessage(request,requestOM,synCtx, accountInfoWrapper, sessionWrapper);
//				createBalanceInquiryMessage(request, requestOM,
//						accountInfoWrapper);
				break;
			case "36":
				InquiryUtils.createTransactionHistoryMessage(request,requestOM, synCtx, accountInfoWrapper, sessionWrapper);
//				createBalanceInquiryMessage(request, requestOM,
//						accountInfoWrapper);
				break;
				
			case "37":
				InquiryUtils.createInquiryTransferMessage(request,requestOM, synCtx, accountInfoWrapper, sessionWrapper);
//				createBalanceInquiryMessage(request, requestOM,
//						accountInfoWrapper);
				break;
			case "38":
				InquiryUtils.createInquiryPaymentMessage(request,requestOM, synCtx, accountInfoWrapper, sessionWrapper);
//				createBalanceInquiryMessage(request, requestOM,
//						accountInfoWrapper);
				break;
			case "49":
				PaymentUtils.createTransferMessage(request, requestOM, accountInfoWrapper, sessionWrapper);
//				createPaymentMessage(request, requestOM, accountInfoWrapper);
				break;
			case "50":
				PaymentUtils.createPaymentMessage(request, requestOM, accountInfoWrapper, sessionWrapper);
//				createPaymentMessage(request, requestOM, accountInfoWrapper);
				break;
			case "51":
				PaymentUtils.createReversalMessage(request, requestOM, accountInfoWrapper, sessionWrapper, revKey);
//				createReversalMessage(request, requestOM, accountInfoWrapper, revKey);
				break;
			case "56":
				PaymentUtils.createTopUpMessage(request, requestOM, accountInfoWrapper, sessionWrapper);
//				createPaymentMessage(request, requestOM, accountInfoWrapper);
				break;
			case "02":
				createSignOff(request, requestOM, sessionWrapper);
				break;

			default:
				break;
			}
		}

		log.info("Logging XLink Request");
		XLinkISO8583Util.logISOMsg(request);
		return request;
	}

	public static OMElement handleResponse(XLinkSessionWrapper sessionWrapper,
			org.apache.axis2.context.MessageContext msgCtx, org.apache.synapse.MessageContext synCtx, ISOMsg m)
			throws ISOException, IOException {
		OMFactory fac = OMAbstractFactory.getOMFactory();
		OMElement apiResponse = fac.createOMElement(new QName("APIResponse"));
		
		String statusCode = (String) m.getValue(39);
		String processingCode = (String) m.getValue(3);
		msgCtx.setProperty("processingCode", processingCode);
		
		//Failure case
		if(statusCode != null && !"00".equals(statusCode) && !"68".equals(statusCode)){
			
			//This code is setting the response to the customer. Before doing this, it needs to generate the proper reversal.
			switch (processingCode.substring(0, 2)) {
			case "30":
				msgCtx.getEnvelope().getBody().getFirstElement().detach();
				
				String revStatusCode = (String) msgCtx.getProperty("RevStatusCode");
				OMElement status = fac.createOMElement(new QName("StatusCode"));
				if(revStatusCode != null){
					status.setText(revStatusCode);
				}else{
					status.setText("1000"+statusCode);
				}
				apiResponse.addChild(status);
				
				OMElement responseData = fac.createOMElement(new QName("Reason"));
				responseData.setText("Error occured while trying to query Account Balanace for "+m.getString(102));
				apiResponse.addChild(responseData);
				
				msgCtx.getEnvelope().getBody().addChild(apiResponse);
				
				break;
				
			case "36":
				msgCtx.getEnvelope().getBody().getFirstElement().detach();
				
				revStatusCode = (String) msgCtx.getProperty("RevStatusCode");
				status = fac.createOMElement(new QName("StatusCode"));
				if(revStatusCode != null){
					status.setText(revStatusCode);
				}else{
					status.setText("1000"+statusCode);
				}
				apiResponse.addChild(status);
				
				responseData = fac.createOMElement(new QName("Reason"));
				responseData.setText("Error occured while trying to query Account History for "+m.getString(102));
				apiResponse.addChild(responseData);
				
				msgCtx.getEnvelope().getBody().addChild(apiResponse);
				
				break;
				
			case "37":
				msgCtx.getEnvelope().getBody().getFirstElement().detach();
				
				revStatusCode = (String) msgCtx.getProperty("RevStatusCode");
				status = fac.createOMElement(new QName("StatusCode"));
				if(revStatusCode != null){
					status.setText(revStatusCode);
				}else{
					status.setText("1000"+statusCode);
				}
				apiResponse.addChild(status);
				
				responseData = fac.createOMElement(new QName("Reason"));
				responseData.setText("Error occured while trying to query Transfer from "+m.getString(102)+" to "+m.getString(103));
				apiResponse.addChild(responseData);
				
				msgCtx.getEnvelope().getBody().addChild(apiResponse);
				
				break;
				
			case "38":
				msgCtx.getEnvelope().getBody().getFirstElement().detach();
				
				revStatusCode = (String) msgCtx.getProperty("RevStatusCode");
				status = fac.createOMElement(new QName("StatusCode"));
				if(revStatusCode != null){
					status.setText(revStatusCode);
				}else{
					status.setText("1000"+statusCode);
				}
				apiResponse.addChild(status);
				
				responseData = fac.createOMElement(new QName("Reason"));
				responseData.setText("Payment Inquiry Not Supported for this channel.");
				apiResponse.addChild(responseData);
				
				msgCtx.getEnvelope().getBody().addChild(apiResponse);
				
				break;
				
			case "49":
				msgCtx.getEnvelope().getBody().getFirstElement().detach();
				
				revStatusCode = (String) msgCtx.getProperty("RevStatusCode");
				status = fac.createOMElement(new QName("StatusCode"));
				if(revStatusCode != null){
					status.setText(revStatusCode);
				}else{
					status.setText("1000"+statusCode);
				}
				apiResponse.addChild(status);
				
				responseData = fac.createOMElement(new QName("Reason"));
				responseData.setText("Error occured while trying to Transfer "+ m.getString(ISOUtil.unPadLeft(m.getString(4), '0')) +" from "+m.getString(102)+" to "+m.getString(103));
				apiResponse.addChild(responseData);
				
				msgCtx.getEnvelope().getBody().addChild(apiResponse);
				break;
				
			case "50":
				msgCtx.getEnvelope().getBody().getFirstElement().detach();
				
				revStatusCode = (String) msgCtx.getProperty("RevStatusCode");
				status = fac.createOMElement(new QName("StatusCode"));
				if(revStatusCode != null){
					status.setText(revStatusCode);
				}else{
					status.setText("1000"+statusCode);
				}
				apiResponse.addChild(status);
				
				responseData = fac.createOMElement(new QName("Reason"));
				responseData.setText("Payments Not Supported for this channel.");
				apiResponse.addChild(responseData);
				
				msgCtx.getEnvelope().getBody().addChild(apiResponse);
				break;
				
			case "56":
				msgCtx.getEnvelope().getBody().getFirstElement().detach();
				
				revStatusCode = (String) msgCtx.getProperty("RevStatusCode");
				status = fac.createOMElement(new QName("StatusCode"));
				if(revStatusCode != null){
					status.setText(revStatusCode);
				}else{
					status.setText("1000"+statusCode);
				}
				apiResponse.addChild(status);
				
				responseData = fac.createOMElement(new QName("Reason"));
				responseData.setText("TopUp Not Supported for this channel.");
				apiResponse.addChild(responseData);
				
				msgCtx.getEnvelope().getBody().addChild(apiResponse);
				break;

			default:
				break;
			}
			
			//TODO: Immediately send a REVERSAL. GRAB BIT90 data from RESPONSE
//	TODO:		FOR SUCCESS CASES, BIT90 sent to client side as reversalKey element and client supposed to send it back to server side.
			 return apiResponse;
		}
//		END OF FAILURE
		
//		SUCCESS CASE
		if(processingCode == null){
			return null;
//			Cant return null. Need to return proper response
		}
		
		switch (processingCode.substring(0, 2)) {
		case "30":
			msgCtx.getEnvelope().getBody().getFirstElement().detach();
			
			OMElement status = fac.createOMElement(new QName("StatusCode"));
			status.setText("0000"+statusCode);
			apiResponse.addChild(status);
			
			OMElement responseData = fac.createOMElement(new QName("ResponseData"));
			apiResponse.addChild(responseData);
			
			Map<String, String> decodedBalance = decodeBalance((String)m.getValue(54));
			for (Iterator iterator = decodedBalance.entrySet().iterator(); iterator
					.hasNext();) {
				Entry<String, String> type = (Entry<String, String>) iterator.next();
				OMElement balance = fac.createOMElement(new QName(type.getKey()));
				balance.setText(type.getValue());
				responseData.addChild(balance);
			}
			
			if (m.getValue(39).equals("00")) {
				String reversalKey = "0200" + m.getValue(11) + m.getValue(7)
						+ m.getValue(37);
				OMElement revkey = fac
						.createOMElement(new QName("ReversalKey"));
				revkey.setText(reversalKey);
				responseData.addChild(revkey);
				// Also add to the messageContext to initiate reversal if the response 
				// link is down
				synCtx.setProperty("reversalKey", reversalKey);
			}
			
			msgCtx.getEnvelope().getBody().addChild(apiResponse);
			break;
			
		case "36":
			msgCtx.getEnvelope().getBody().getFirstElement().detach();
			
			status = fac.createOMElement(new QName("StatusCode"));
			status.setText("0000"+statusCode);
			apiResponse.addChild(status);
			
			responseData = fac.createOMElement(new QName("ResponseData"));
			apiResponse.addChild(responseData);
			
			
			//Deocing the response.
			List<Transaction> decodeTxnHistory = decodeTxnHistory(m.getString(62));
			OMElement transactions = fac.createOMElement(new QName("Transactions"));
			responseData.addChild(transactions);
			
			for (Transaction transaction : decodeTxnHistory) {
				OMElement txn = fac.createOMElement(new QName("Transaction"));
				OMElement date = fac.createOMElement(new QName("Date"));
				date.setText(transaction.getDate());
				OMElement amountT = fac.createOMElement(new QName("Amount"));
				amountT.setText(transaction.getAmount());
				OMElement currency = fac.createOMElement(new QName("Currency"));
				currency.setText(transaction.getCurrency());
				OMElement txnType = fac.createOMElement(new QName("TxnType"));
				txnType.setText(transaction.getTxnType());
				txn.addChild(date);
				txn.addChild(amountT);
				txn.addChild(currency);
				txn.addChild(txnType);
				
				transactions.addChild(txn);
			}
			
			// Building the reversalKey
			if (m.getValue(39).equals("00")) {
				String reversalKey = "0200" + m.getValue(11) + m.getValue(7)
						+ m.getValue(37);
				OMElement revkey = fac
						.createOMElement(new QName("ReversalKey"));
				revkey.setText(reversalKey);
				responseData.addChild(revkey);
				// Also add to the messageContext to initiate reversal if the response 
				// link is down
				synCtx.setProperty("reversalKey", reversalKey);
			}
			
			msgCtx.getEnvelope().getBody().addChild(apiResponse);
			break;
			
		case "37":
			msgCtx.getEnvelope().getBody().getFirstElement().detach();
			
			status = fac.createOMElement(new QName("StatusCode"));
			status.setText("0000"+statusCode);
			apiResponse.addChild(status);
			
			 responseData = fac.createOMElement(new QName("ResponseData"));
				apiResponse.addChild(responseData);
				
				OMElement destinationAcct = fac.createOMElement(new QName("DestinationAccountNo"));
				destinationAcct.setText(m.getString(103));
				responseData.addChild(destinationAcct);
				
				OMElement destinationBankCode = fac.createOMElement(new QName("DestinationBankCode"));
				destinationBankCode.setText(m.getString(127));
				responseData.addChild(destinationBankCode);
				
				OMElement DestinationAccountType = fac.createOMElement(new QName("DestinationAccountType"));
				DestinationAccountType.setText(m.getString(3).substring(4));
				responseData.addChild(DestinationAccountType);
				
			Map<String, String> decodeBit61 = decodeBit61(m.getString(61));
			if(decodeBit61!=null){
				for (Iterator iterator = decodeBit61.entrySet().iterator(); iterator
						.hasNext();) {
					Entry<String, String> type = (Entry<String, String>) iterator.next();
					OMElement balance = fac.createOMElement(new QName(type.getKey()));
					balance.setText(type.getValue());
					responseData.addChild(balance);
				}
			}
				
				OMElement inquiryAmount = fac.createOMElement(new QName("InquiryAmount"));
				inquiryAmount.setText(ISOUtil.unPadLeft(m.getString(4), '0'));
				responseData.addChild(inquiryAmount);
				
				// Building the reversalKey
				if (m.getValue(39).equals("00")) {
					String reversalKey = "0200" + m.getValue(11) + m.getValue(7)
							+ m.getValue(37);
					OMElement revkey = fac
							.createOMElement(new QName("ReversalKey"));
					revkey.setText(reversalKey);
					responseData.addChild(revkey);
					// Also add to the messageContext to initiate reversal if the response 
					// link is down
					synCtx.setProperty("reversalKey", reversalKey);
				}
			
			msgCtx.getEnvelope().getBody().addChild(apiResponse);
			break;
			
		case "38":
			msgCtx.getEnvelope().getBody().getFirstElement().detach();
			
			status = fac.createOMElement(new QName("StatusCode"));
			status.setText("0000"+statusCode);
			apiResponse.addChild(status);
			
			responseData = fac.createOMElement(new QName("Reason"));
			responseData.setText("Inquiry Payment Supported for this channel.");
			apiResponse.addChild(responseData);
			
			msgCtx.getEnvelope().getBody().addChild(apiResponse);
			break;
			
		case "49":
			msgCtx.getEnvelope().getBody().getFirstElement().detach();
			
			status = fac.createOMElement(new QName("StatusCode"));
			status.setText("0000"+statusCode);
			apiResponse.addChild(status);
			
			responseData = fac.createOMElement(new QName("ResponseData"));
			apiResponse.addChild(responseData);
			
			destinationAcct = fac.createOMElement(new QName("DestinationAccountNo"));
			destinationAcct.setText(m.getString(103));
			responseData.addChild(destinationAcct);
			
			destinationBankCode = fac.createOMElement(new QName("DestinationBankCode"));
			destinationBankCode.setText(m.getString(127));
			responseData.addChild(destinationBankCode);
			
			OMElement messageIdNode = fac.createOMElement(new QName("MessageId"));
			messageIdNode.setText(m.getString(37).substring(6));
			responseData.addChild(messageIdNode);
			
		decodeBit61 = decodeBit61(m.getString(61));
		if(decodeBit61!=null){
			for (Iterator iterator = decodeBit61.entrySet().iterator(); iterator
					.hasNext();) {
				Entry<String, String> type = (Entry<String, String>) iterator.next();
				OMElement balance = fac.createOMElement(new QName(type.getKey()));
				balance.setText(type.getValue());
				responseData.addChild(balance);
			}
		}
			
			OMElement transferedAmount = fac.createOMElement(new QName("TransferedAmount"));
			transferedAmount.setText(ISOUtil.unPadLeft(m.getString(4), '0'));
			responseData.addChild(transferedAmount);
			// Building the reversalKey
			if (m.getValue(39).equals("00")) {
				String reversalKey = "0200" + m.getValue(11) + m.getValue(7)
						+ m.getValue(37);
				OMElement revkey = fac
						.createOMElement(new QName("ReversalKey"));
				revkey.setText(reversalKey);
				responseData.addChild(revkey);
				// Also add to the messageContext to initiate reversal if the response 
				// link is down
				synCtx.setProperty("reversalKey", reversalKey);
			}
			
			msgCtx.getEnvelope().getBody().addChild(apiResponse);
			break;
			
			
		case "50":
			msgCtx.getEnvelope().getBody().getFirstElement().detach();
			
			status = fac.createOMElement(new QName("StatusCode"));
			status.setText("0000"+statusCode);
			apiResponse.addChild(status);
			
			responseData = fac.createOMElement(new QName("Reason"));
			responseData.setText("Payment Not Supported for this channel.");
			apiResponse.addChild(responseData);
			
			msgCtx.getEnvelope().getBody().addChild(apiResponse);
			break;
			
		case "56":
			msgCtx.getEnvelope().getBody().getFirstElement().detach();
			
			status = fac.createOMElement(new QName("StatusCode"));
			status.setText("0000"+statusCode);
			apiResponse.addChild(status);
			
			responseData = fac.createOMElement(new QName("Reason"));
			responseData.setText("TopUp Not Supported for this channel.");
			apiResponse.addChild(responseData);
			
			msgCtx.getEnvelope().getBody().addChild(apiResponse);
			break;
			
			default:
				break;
		}
		
		return apiResponse;
	}

//	private static void createBalanceInquiryMessage(ISOMsg request,
//			OMElement requestOM, XLinkAccountInfoWrapper accountInfoWrapper)
//			throws ISOException {
//		
//		
//		request.setMTI("0200");
//		request.set(2, accountInfoWrapper.getAccountno()); // PAN:123456789
//		request.set(3, "300000"); // 300000
//		request.set(7, "0421080000");
//		request.set(11, NetworkMgtUtil.getRandomNumber());
//		request.set(12, "080000");
//		request.set(13, "0421");
//		request.set(14, "0422");
//		request.set(18, "6017");
//		request.set(24, "000");
//		request.set(32, "950");
//		String track2Data = "Hello World!";// TODO: Get proper Track2 data
//		request.set(35, track2Data);
//		request.set(37, "042112345678"); // TrxId 12345678
//		request.set(41, "12345678"); // Needd to know
//		request.set(42, "123456789123456"); // Need to know
//		request.set(43, "50 AMOY STREET SINGAPORE 069876       SG");
//		request.set(49, "IDR");
//		char[] array=new char[]{'C','7','4','B','1','D','B','3','2','8','8','A','C','1','5','C'};
//		request.set(52,new String(array).getBytes());
//		request.set(102, "123456789"); // SAME as PAN
//	}

	private static void createReversalMessage(ISOMsg request,
			OMElement requestOM, XLinkAccountInfoWrapper accountInfoWrapper, String revKey)
			throws ISOException {

		request.setMTI("0400");
		request.set(2, "123456789"); // PAN:123456789
		request.set(3, "500100"); // 300100 AccType 01-Savings Account
		request.set(4, "1500");
		request.set(7, "0421080000");
		request.set(11, "000000000083");
		request.set(14, "0422");
		request.set(18, "6017");
		request.set(24, "000");
		request.set(32, "950");
		request.set(37, "042112345678"); // TrxId 12345678
		request.set(41, "12345678"); // Need to know
		request.set(42, "123456789123456"); // Need to know
		request.set(49, "IDR");
		request.set(61, "This is MBSB");
		request.set(90, revKey);// From
																	// prevously
																	// done Trx
		request.set(98, "000000"); // ON US
		request.set(100, "950");// Need to know
		request.set(102, "123456789"); // SAME as PAN
		request.set(103, "123456788"); // REMOTE PAN
		request.set(127, "123456799"); // From previous Trx
	}

//	private static void createPaymentMessage(ISOMsg request,
//			OMElement requestOM, XLinkAccountInfoWrapper accountInfoWrapper)
//			throws ISOException {
//
//		request.setMTI("0200");
//		request.set(2, accountInfoWrapper.getAccountno()); // PAN:123456789
//		request.set(3, "500100"); // 300100 AccType 01-Savings Account
//		request.set(4, requestOM.getFirstChildWithName(new QName("Amount"))
//				.getText());
//		request.set(7, "0421080000"); // for 90
//		request.set(11, NetworkMgtUtil.getRandomNumber());  //for 90
//		request.set(12, "080000");
//		request.set(13, "0421");
//		request.set(14, "0422");
//		request.set(18, "6017");
//		request.set(24, "000");
//		request.set(32, "950");
//		String track2Data = ";" + accountInfoWrapper.getAccountno()
//				+ "=1408900543?";// PAN-123456789 ExpiryDate-14/08
//									// SeerviceCode-900 CVC-543
//		byte LRC = SecurityUtils.calculateLRC(track2Data.getBytes());
//		String LRCChar = new String(new byte[] { LRC });
//		request.set(35, track2Data + LRCChar);
//		request.set(37, requestOM.getFirstChildWithName(new QName("TrxId"))
//				.getText()); // TrxId 12345678 // for 90
//		request.set(41, "12345678"); // Needd to know
//		request.set(42, "123456789123456"); // Need to know
//		request.set(43, "50 AMOY STREET SINGAPORE 069876       SG");
//		request.set(49, requestOM.getFirstChildWithName(new QName("Currency"))
//				.getText());
//		char[] array=new char[]{'C','7','4','B','1','D','B','3','2','8','8','A','C','1','5','C'};
//		request.set(52,new String(array).getBytes());
//		request.set(61, "This is MBSB");
//		request.set(98, "000000"); // ON US
//		request.set(102, "123456789"); // SAME as PAN
//
//		System.out.println("PAYMENT SENDING");
//		XLinkISO8583Util.logISOMsg(request);
//	}

	private static void createSignOff(ISOMsg request, OMElement requestOM, XLinkSessionWrapper sessionWrapper)
			throws ISOException {
		// TODO Auto-generated method stub
		NetworkMgtUtil.createSignOffMessage(request, sessionWrapper.getNextRequestId());

	}
	
	private static Map<String,String> decodeBalance(String originalString){
		Map<String,String> hashTable=new java.util.Hashtable<String, String>();
		hashTable.put("AccountType", originalString.substring(0,2));
		hashTable.put("AmountType", originalString.substring(2,4));
		hashTable.put("CurrencyCode", originalString.substring(4,7));
		hashTable.put("BalanceType", originalString.substring(7,8));
		hashTable.put("BalanceAmount", ISOUtil.unPadLeft(originalString.substring(8,24), '0'));
		return hashTable;
	}
	
	private static List<Transaction> decodeTxnHistory(String originalString){
		String[] transactionArray = originalString.split("(?<=\\G.{28})");
		List<Transaction> txnList=null;
		if(transactionArray != null){
			txnList=new ArrayList<Transaction>();
			for (String string : transactionArray) {
				String txnDate = string.substring(0, 6).trim();
				String[] amountCurArray = string.substring(6, 27).trim().split(" ");
				String amount = amountCurArray[0];
				String currency = amountCurArray[1];
				String txnType = string.substring(27).trim();
				Transaction txn=new Transaction(txnDate, amount, currency, txnType);
				txnList.add(txn);
			}
		}
		return txnList;
	}
	
	private static Map<String,String> decodeBit61(String originalString){
		Map<String,String> hashTable=new java.util.Hashtable<String, String>();
		try {
			hashTable.put("ToAccountName", ISOUtil.unPadRight(originalString.substring(0,30), ' '));
			hashTable.put("FromAccountName", ISOUtil.unPadRight(originalString.substring(30,60), ' '));
			hashTable.put("RefNumber", ISOUtil.unPadRight(originalString.substring(60,76), ' '));
			hashTable.put("ToCurCode", originalString.substring(76,79));
			hashTable.put("FromCurCode", originalString.substring(79,82));
			hashTable.put("CurRate", ISOUtil.unPadRight(originalString.substring(82), ' '));
		} catch (Exception e) {
			log.error("Error occured while decoding the BIT 61",e);
		}
		return hashTable;
	}
}
