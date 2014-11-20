package com.finance.iso.iso8583.jpos.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.synapse.MessageContext;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;

import com.finance.iso.iso8583.mediator.XLinkAccountInfoWrapper;
import com.finance.iso.iso8583.mediator.XLinkISO8583Constant;
import com.finance.iso.iso8583.mediator.XLinkSessionWrapper;


public class InquiryUtils {
	
	public static void createBalanceInquiryMessage(ISOMsg request,OMElement requestOM,MessageContext synCtx, XLinkAccountInfoWrapper accountInfoWrapper,
			XLinkSessionWrapper sessionWrapper)
			throws ISOException {
		final String field11 = ISOUtil.padleft(sessionWrapper.getNextRequestId(), XLinkISO8583Constant.FIELD_11_LENGTH, '0');
		
		// MBSB Spec is here
		request.setMTI("0200");
		request.set(3, "301000"); 
		request.set(4, ISOUtil.padleft("0", 18, '0'));
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
		request.set(98,ISOUtil.padright(XLinkISO8583Constant.REQUEST_FIELD_98_ON_US, 25, ' '));
		request.set(102, source);
	}
	
	public static void createInquiryTransferMessage(ISOMsg request,OMElement requestOM,MessageContext synCtx, XLinkAccountInfoWrapper accountInfoWrapper,
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
		
		request.set(3, "37"+sourceAcctType+destAcctType); 
		
		String amount="0";
		OMElement amountNode = requestOM.getFirstChildWithName(new QName("Amount"));
		if(amountNode !=null){
			amount = amountNode.getText();
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
		request.set(37, new SimpleDateFormat("MMdd").format(new Date())+"00"+field11.substring(6)); 
		
		String source=accountInfoWrapper.getAccountno();
		
		request.set(41, ISOMsgUtils.generateField41(source));
		request.set(42,ISOMsgUtils.generateField42(source)); 
		request.set(43, ISOMsgUtils.generateField43(source));
		request.set(49, XLinkISO8583Constant.REQUEST_FIELD_49);
		request.set(98,ISOUtil.padright(XLinkISO8583Constant.REQUEST_FIELD_98_ON_US, 25, ' '));
		
		String destination=null;
		OMElement toNode = requestOM.getFirstChildWithName(new QName("To"));
		if(toNode !=null){
			destination = toNode.getText();
		}
		
		request.set(102, source);
		request.set(103, destination); 
		
		String dIIC=null;
		OMElement dIICNode = requestOM.getFirstChildWithName(new QName("DIIC"));
		if(dIICNode != null){
			dIIC=dIICNode.getText();
		}
	
		if(dIIC == null){
			dIIC = XLinkISO8583Constant.REQUEST_FIELD_32;
		}
		
		request.set(127, dIIC); 
	}
	
	public static void createInquiryPaymentMessage(ISOMsg request,OMElement requestOM,MessageContext synCtx, XLinkAccountInfoWrapper accountInfoWrapper,
			XLinkSessionWrapper sessionWrapper)
			throws ISOException {
		final String field11 = ISOUtil.padleft(sessionWrapper.getNextRequestId(), XLinkISO8583Constant.FIELD_11_LENGTH, '0');
		
		// MBSB Spec is here
		request.setMTI("0200");
		request.set(3, "381000"); 
		request.set(4, ISOUtil.padleft("0", 18, '0'));
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
		sb.append(ISOUtil.padright(destination, 30, ' '));
		sb.append(ISOUtil.padright(source, 30, ' '));
		sb.append(ISOUtil.padright(field11, 16, ' '));
		sb.append(360);
		sb.append(360);
		sb.append(ISOUtil.padleft("1.0", 12, ' '));
		request.set(61,sb.toString()); 
		
		request.set(98,ISOUtil.padright(XLinkISO8583Constant.REQUEST_FIELD_98_ON_US, 25, ' ')); //ON US Why 25 length what to do for the rest?
		
		request.set(102, source); 
	}
	
	public static void createTransactionHistoryMessage(ISOMsg request,OMElement requestOM, MessageContext synCtx, XLinkAccountInfoWrapper accountInfoWrapper,
			XLinkSessionWrapper sessionWrapper)
			throws ISOException {
		
		
		final String field11 = ISOUtil.padleft(sessionWrapper.getNextRequestId(), XLinkISO8583Constant.FIELD_11_LENGTH, '0');
		
		// MBSB Spec is here
		request.setMTI("0200");
		request.set(3, "361000"); 
		request.set(4, ISOUtil.padleft("0", 18, '0'));
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
		request.set(98,ISOUtil.padright(XLinkISO8583Constant.REQUEST_FIELD_98_ON_US, 25, ' '));
		request.set(102, source);
		
	}

}
