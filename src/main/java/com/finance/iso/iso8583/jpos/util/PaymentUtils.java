package com.finance.iso.iso8583.jpos.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;

import com.finance.iso.iso8583.mediator.XLinkAccountInfoWrapper;
import com.finance.iso.iso8583.mediator.XLinkISO8583Constant;
import com.finance.iso.iso8583.mediator.XLinkSessionWrapper;

public class PaymentUtils {
	
	private static final String OPERATION_TYPE = "OperationType";
	private static final String REV_OPERATION_TYPE = "RevOperationType";
	
	public static void createPaymentMessage(ISOMsg request,OMElement requestOM, XLinkAccountInfoWrapper accountInfoWrapper,
			XLinkSessionWrapper sessionWrapper)
			throws ISOException {
		
		final String field11 = ISOUtil.padleft(sessionWrapper.getNextRequestId(), XLinkISO8583Constant.FIELD_11_LENGTH, '0');
		
		// MBSB Spec is here
		request.setMTI("0200");
		request.set(3, "501000");
		
		OMElement amountNode = requestOM.getFirstChildWithName(new QName("Amount"));
		String amount= "0";
		if(amountNode!=null){
			amount=amountNode.getText();
		}
		
		request.set(4, ISOUtil.padleft(amount, 18, '0'));
		SimpleDateFormat dateFormat = new SimpleDateFormat("MMddHHmmss");
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		request.set(7, dateFormat.format(new Date()));
		request.set(11, field11);
		request.set(12, new SimpleDateFormat("HHmmss").format(new Date()));
		request.set(13, new SimpleDateFormat("MMdd").format(new Date()));
		request.set(18, XLinkISO8583Constant.REQUEST_FIELD_18); 
		request.set(24, "000");
		request.set(32, XLinkISO8583Constant.REQUEST_FIELD_32);
		request.set(37, new SimpleDateFormat("MMdd").format(new Date())+"00"+field11.substring(6)); 
		String source=accountInfoWrapper.getAccountno();
		request.set(41, ISOMsgUtils.generateField41(source)); 
		request.set(42,ISOMsgUtils.generateField42(source)); 
		request.set(43, ISOMsgUtils.generateField43(source));
		request.set(49, XLinkISO8583Constant.REQUEST_FIELD_49);
		StringBuilder sb=new StringBuilder();
		
		String destination=null;
		OMElement toNode = requestOM.getFirstChildWithName(new QName("To"));
		if(toNode !=null){
			destination = toNode.getText();
		}
		
		String curRate="1.0";
		OMElement curRateNode = requestOM.getFirstChildWithName(new QName("CurRate"));
		if(curRateNode !=null){
			curRate = curRateNode.getText();
		}
		
		sb.append(ISOUtil.padright(destination, 30, ' '));
		sb.append(ISOUtil.padright(source, 30, ' '));
		sb.append(ISOUtil.padright(field11, 16, ' '));
		sb.append(360);
		sb.append(360);
		sb.append(ISOUtil.padright(curRate, 12, '0'));
		request.set(61,sb.toString()); //FROM ACCOUNT
		
		request.set(98,ISOUtil.padright(XLinkISO8583Constant.REQUEST_FIELD_98_ON_US, 25, ' ')); //ON US Why 25 length what to do for the rest?
		request.set(102, source); //SAME as PAN?
		
		//This is only for Payments
//		request.set(103, destination); //REMOTE PAN
//		
//		String toAIIC=null;
//		OMElement toAIICNode = requestOM.getFirstChildWithName(new QName("ToAIIC"));
//		if(toAIICNode != null){
//			toAIIC=toAIICNode.getText();
//		}
//		//For testing Transfer on us
//		if(toAIIC == null){
//			toAIIC = XLinkISO8583Constant.REQUEST_FIELD_32;
//		}
//		
//		request.set(127, toAIIC); //From previous Trx
		
	}
	
	public static void createTopUpMessage(ISOMsg request,OMElement requestOM, XLinkAccountInfoWrapper accountInfoWrapper,
			XLinkSessionWrapper sessionWrapper)
			throws ISOException {
		
		final String field11 = ISOUtil.padleft(sessionWrapper.getNextRequestId(), XLinkISO8583Constant.FIELD_11_LENGTH, '0');
		
		// MBSB Spec is here
		request.setMTI("0200");
		request.set(3, "561000"); //300100 AccType 01-Savings Account
		
		OMElement amountNode = requestOM.getFirstChildWithName(new QName("Amount"));
		String amount= null;
		if(amountNode!=null){
			amount=amountNode.getText();
		}
		//Testing
		if(amount == null){
			amount="15000000";
		}
		
		request.set(4, ISOUtil.padleft(amount, 18, '0'));
		SimpleDateFormat dateFormat = new SimpleDateFormat("MMddHHmmss");
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		request.set(7, dateFormat.format(new Date()));
		request.set(11, field11);
		request.set(12, new SimpleDateFormat("HHmmss").format(new Date()));
		request.set(13, new SimpleDateFormat("MMdd").format(new Date()));
		request.set(18, XLinkISO8583Constant.REQUEST_FIELD_18); // >> For MBSB it needs to be 6017
		request.set(24, "000");
		request.set(32, XLinkISO8583Constant.REQUEST_FIELD_32);
		request.set(37, new SimpleDateFormat("MMdd").format(new Date())+"00"+field11.substring(6)); //TrxId 12345678
		String source=accountInfoWrapper.getAccountno();
		request.set(41, ISOMsgUtils.generateField41(source)); //Needd to know
		request.set(42,ISOMsgUtils.generateField42(source)); //Need to know
		request.set(43, ISOMsgUtils.generateField43(source));
		request.set(49, XLinkISO8583Constant.REQUEST_FIELD_49);
		StringBuilder sb=new StringBuilder();
		
		String destination=null;
		OMElement toNode = requestOM.getFirstChildWithName(new QName("To"));
		if(toNode !=null){
			destination = toNode.getText();
		}
		//For testing
		if(destination == null){
			destination = "1000452239";
		}
		
		
		//For testing
		if(source == null){
			source = "1000296231";
		}
		
		String curRate="1.0";
		OMElement curRateNode = requestOM.getFirstChildWithName(new QName("CurRate"));
		if(curRateNode !=null){
			curRate = curRateNode.getText();
		}
		
		sb.append(ISOUtil.padright(destination, 30, ' '));
		sb.append(ISOUtil.padright(source, 30, ' '));
		sb.append(ISOUtil.padright(field11, 16, ' '));
		sb.append(360);
		sb.append(360);
		sb.append(ISOUtil.padright(curRate, 12, '0'));
		request.set(61,sb.toString()); //FROM ACCOUNT
		
		
		request.set(98,ISOUtil.padright(XLinkISO8583Constant.REQUEST_FIELD_98_ON_US, 25, ' ')); //ON US Why 25 length what to do for the rest?
		request.set(102, source); //SAME as PAN?
		
		//This is only for Payments
//		request.set(103, destination); //REMOTE PAN
//		
//		String toAIIC=null;
//		OMElement toAIICNode = requestOM.getFirstChildWithName(new QName("ToAIIC"));
//		if(toAIICNode != null){
//			toAIIC=toAIICNode.getText();
//		}
//		//For testing Transfer on us
//		if(toAIIC == null){
//			toAIIC = XLinkISO8583Constant.REQUEST_FIELD_32;
//		}
//		
//		request.set(127, toAIIC); //From previous Trx
		
	}
	
	public static void createTransferMessage(ISOMsg request,OMElement requestOM, XLinkAccountInfoWrapper accountInfoWrapper,
			XLinkSessionWrapper sessionWrapper)
			throws ISOException {
		
		final String field11 = ISOUtil.padleft(sessionWrapper.getNextRequestId(), XLinkISO8583Constant.FIELD_11_LENGTH, '0');
		
		// MBSB Spec is here
		request.setMTI("0200");
		
		String sourceAcctType="10"; //Assign the Default Account
		OMElement sourceAcctTypeNode = requestOM.getFirstChildWithName(new QName("SourceAcctType"));
		if(sourceAcctTypeNode !=null && !"".equals(sourceAcctTypeNode.getText())){
			sourceAcctType=sourceAcctTypeNode.getText();
		}
		
		String destAcctType="00"; //Assign the Default Account
		OMElement destAcctTypeNode = requestOM.getFirstChildWithName(new QName("DestAcctType"));
		if(destAcctTypeNode !=null && !"".equals(destAcctTypeNode.getText())){
			destAcctType=destAcctTypeNode.getText();
		}
		request.set(3, "49"+sourceAcctType+destAcctType);
		
		OMElement amountNode = requestOM.getFirstChildWithName(new QName("Amount"));
		String amount= "0";
		if(amountNode!=null){
			amount=amountNode.getText();
		}
		
//		if(amount.contains(".")){
//			amount=amount.replace(".", "");
//		}
		
		request.set(4, ISOUtil.padleft(amount, 18, '0'));
		SimpleDateFormat dateFormat = new SimpleDateFormat("MMddHHmmss");
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		request.set(7, dateFormat.format(new Date()));
		request.set(11, field11);
		request.set(12, new SimpleDateFormat("HHmmss").format(new Date()));
		request.set(13, new SimpleDateFormat("MMdd").format(new Date()));
		request.set(18, XLinkISO8583Constant.REQUEST_FIELD_18); 
		request.set(24, "000");
		request.set(32, XLinkISO8583Constant.REQUEST_FIELD_32);
		
		OMElement messageIdNode = requestOM.getFirstChildWithName(new QName("MessageId"));
		String messageId= field11.substring(6);
		if(messageIdNode!=null){
			messageId=messageIdNode.getText();
		}
		//This is going to be our reference no as well
		String field37 = new SimpleDateFormat("MMdd").format(new Date())+"00"+messageId;
		request.set(37, field37); 
		
		String source=accountInfoWrapper.getAccountno();
		
		request.set(41, ISOMsgUtils.generateField41(source)); 
		request.set(42,ISOMsgUtils.generateField42(source)); 
		request.set(43, ISOMsgUtils.generateField43(source));
		request.set(49, XLinkISO8583Constant.REQUEST_FIELD_49);
		StringBuilder sb=new StringBuilder();
		
		String destinationName="";
		OMElement toNameNode = requestOM.getFirstChildWithName(new QName("ToAccountName"));
		if(toNameNode !=null){
			destinationName = toNameNode.getText();
		}
		
		String sourceName="";
		OMElement fromNameNode = requestOM.getFirstChildWithName(new QName("FromAccountName"));
		if(toNameNode !=null){
			sourceName = fromNameNode.getText();
		}
		
		String curRate="1.0";
		OMElement curRateNode = requestOM.getFirstChildWithName(new QName("CurRate"));
		if(curRateNode !=null){
			curRate = curRateNode.getText();
		}
		
		String toCurCode="360";
		OMElement curCodeNode = requestOM.getFirstChildWithName(new QName("CurCode"));
		if(curCodeNode !=null){
			toCurCode = curCodeNode.getText();
		}
		
		sb.append(ISOUtil.padright(destinationName, 30, ' '));
		sb.append(ISOUtil.padright(sourceName, 30, ' '));
		sb.append(ISOUtil.padright(messageId, 16, ' '));
		sb.append(toCurCode);
		sb.append(360);
		sb.append(ISOUtil.padleft(curRate, 12, ' '));
		request.set(61,sb.toString()); 
				
		String destination=null;
		OMElement toNode = requestOM.getFirstChildWithName(new QName("To"));
		if(toNode !=null){
			destination = toNode.getText();
		}

		request.set(98,ISOUtil.padright(XLinkISO8583Constant.REQUEST_FIELD_98_ON_US, 25, ' ')); 
		request.set(102, source); 
		request.set(103, destination); 
		
		String dIIC=null;
		OMElement dIICNode = requestOM.getFirstChildWithName(new QName("DIIC"));
		if(dIICNode != null){
			dIIC=dIICNode.getText();
		}
		//Assuming an Internal Transfer if DIIC is not there
		if(dIIC == null){
			dIIC = XLinkISO8583Constant.REQUEST_FIELD_32;
		}
		
		request.set(127, dIIC); 
		
	}
	
	public static void createReversalMessage(ISOMsg request,OMElement requestOM, XLinkAccountInfoWrapper accountInfoWrapper,
			XLinkSessionWrapper sessionWrapper, String reversalKey)
			throws ISOException {
		final String field11 = ISOUtil.padleft(sessionWrapper.getNextRequestId(), XLinkISO8583Constant.FIELD_11_LENGTH, '0');

		// MBSB Spec is here
		request.setMTI("0420");
		OMElement revOperationRequest = requestOM
				.getFirstChildWithName((new QName(REV_OPERATION_TYPE)));
		String revOperationType=null;
		if(revOperationRequest !=null){
			revOperationType=revOperationRequest.getText();
		}
		request.set(3, revOperationType+"1000"); //300100 AccType 01-Savings Account
		
		OMElement amountNode = requestOM.getFirstChildWithName(new QName("Amount"));
		String amount= null;
		if(amountNode!=null){
			amount=amountNode.getText();
		}
		
//		if(amount.contains(".")){
//			amount=amount.replace(".", "");
//		}
		//Testing
		if(amount == null){
			amount="0";
		}
		
		String field7=reversalKey.substring(17,27);
		String field37=reversalKey.substring(31);
		
		request.set(4, ISOUtil.padleft(amount, 12, '0'));
		request.set(7, field7);
		request.set(11, field11);
		request.set(18, XLinkISO8583Constant.REQUEST_FIELD_18); // >> For MBSB it needs to be 6017
		request.set(32, XLinkISO8583Constant.REQUEST_FIELD_32);
		request.set(37, field37); //TrxId 12345678
		String source=accountInfoWrapper.getAccountno();
		request.set(41, ISOMsgUtils.generateField41(source)); //Needd to know
		
		if(reversalKey==null){
			OMElement revKeyNode = requestOM.getFirstChildWithName(new QName("RevKey"));
			if(revKeyNode!=null){
				reversalKey=revKeyNode.getText();
			}
		}
		request.set(90, reversalKey);
		
		String destination=null;
		OMElement toNode = requestOM.getFirstChildWithName(new QName("To"));
		if(toNode !=null){
			destination = toNode.getText();
		}
		//For testing
		if(destination == null){
			destination = "1000452239";
		}
		
		
		//For testing
		if(source == null){
			source = "1000296231";
		}
		
		request.set(102, source); //SAME as PAN?
		
		if(destination != null){
		request.set(103, destination); //REMOTE PAN
		}
		
		String toAIIC=null;
		OMElement toAIICNode = requestOM.getFirstChildWithName(new QName("DIIC"));
		if(toAIICNode != null){
			toAIIC=toAIICNode.getText();
		}
		//For testing Transfer on us
		if(toAIIC == null){
			toAIIC = XLinkISO8583Constant.REQUEST_FIELD_32;
		}
		
		if(toAIIC !=null){
		request.set(127, toAIIC); //From previous Trx
		}
	}

}
