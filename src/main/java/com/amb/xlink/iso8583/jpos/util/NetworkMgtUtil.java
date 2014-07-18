package com.amb.xlink.iso8583.jpos.util;

import com.amb.xlink.iso8583.mediator.XLinkISO8583Constant;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.TimeZone;

public class NetworkMgtUtil {
	
	public static String getRandomNumber() {
		Random random = new Random();
		long n = (long) (100000L + random.nextFloat() * 900000L);
		return new Long(n).toString();
	}

	public static void createKeyExchangeMessage(ISOMsg request, String requestId)
			throws ISOException {
		request.setMTI("0800");
		SimpleDateFormat dateFormat = new SimpleDateFormat("MMddHHmmss");
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		request.set(7, dateFormat.format(new Date()));
		request.set(11, ISOUtil.padleft(requestId, XLinkISO8583Constant.FIELD_11_LENGTH, '0'));
		request.set(32, XLinkISO8583Constant.REQUEST_FIELD_32);
		request.set(70, "101");
		
	}

	public static void createSignOnMessage(ISOMsg request, String requestId) throws ISOException {
		request.setMTI("0800");
		SimpleDateFormat dateFormat = new SimpleDateFormat("MMddHHmmss");
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		request.set(7, dateFormat.format(new Date()));
        request.set(11, ISOUtil.padleft(requestId, XLinkISO8583Constant.FIELD_11_LENGTH, '0'));
        request.set(32, XLinkISO8583Constant.REQUEST_FIELD_32); 
        request.set(70, "001");
		
	}

	public static void createSignOffMessage(ISOMsg request, String requestId)
			throws ISOException {
		request.setMTI("0800");
		SimpleDateFormat dateFormat = new SimpleDateFormat("MMddHHmmss");
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		request.set(7, dateFormat.format(new Date()));
        request.set(11, ISOUtil.padleft(requestId, XLinkISO8583Constant.FIELD_11_LENGTH, '0'));
        request.set(32, XLinkISO8583Constant.REQUEST_FIELD_32);
        request.set(70, "002");
		
	}

	public static void createEchoTestMessage(ISOMsg request, String requestId)
			throws ISOException {
		request.setMTI("0800");
		SimpleDateFormat dateFormat = new SimpleDateFormat("MMddHHmmss");
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		request.set(7, dateFormat.format(new Date()));
        request.set(11, ISOUtil.padleft(requestId, XLinkISO8583Constant.FIELD_11_LENGTH, '0'));
        request.set(32, XLinkISO8583Constant.REQUEST_FIELD_32);
        request.set(70, "301");
		
	}

	public static void createCutoffMessage(ISOMsg request, String requestId) throws ISOException {
		request.setMTI("0800");
		SimpleDateFormat dateFormat = new SimpleDateFormat("MMddHHmmss");
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
		request.set(7, dateFormat.format(new Date()));
		request.set(11, ISOUtil.padleft(requestId, XLinkISO8583Constant.FIELD_11_LENGTH, '0'));
		request.set(32, XLinkISO8583Constant.REQUEST_FIELD_32);
		request.set(70, "201");
		
	}

}
