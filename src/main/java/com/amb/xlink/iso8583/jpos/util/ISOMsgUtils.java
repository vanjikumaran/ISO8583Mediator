package com.amb.xlink.iso8583.jpos.util;

import java.io.UnsupportedEncodingException;

import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOUtil;

import com.amb.xlink.iso8583.mediator.XLinkISO8583Constant;

public class ISOMsgUtils {
	
	public static ISOMsg addTransportHeader(ISOMsg request){
		try {
			int length = request.pack().length;
//			char[] d=new char[2];
//			d[1]= (char)length;
//			
//			char[] chars = d;
			char[] chars=new char[2];
			chars[1]=(char)length;
//			byte[] bytes = new byte[chars.length*2];
//			for(int i=0;i<chars.length;i++) {
//			   bytes[i*2] = (byte) (chars[i] >> 8);
//			   bytes[i*2+1] = (byte) chars[i];
//			}
//			char[] chars2 = new char[bytes.length/2];
//			for(int i=0;i<chars2.length;i++) {
//			   chars2[i] = (char) ((bytes[i*2] << 8) + (bytes[i*2+1] & 0xFF));
//			}
			String password = new String(chars);
			 request.setHeader(password.getBytes("US-ASCII"));
			
			
			return request;
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ISOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			return request;
		}
	}

	
	public static String generateField41(String sourceAccountNo){
		int length = sourceAccountNo.length();
		length=length-6;
		
		return XLinkISO8583Constant.REQUEST_FIELD_41+sourceAccountNo.substring(length);
	}
	
	public static String generateField42(String sourceAccountNo){
			
		try {
			return XLinkISO8583Constant.REQUEST_FIELD_42+ISOUtil.padleft(sourceAccountNo, 13, '0');
		} catch (ISOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static String generateField43(String sourceAccountNo){
		
		String field41 = generateField41(sourceAccountNo);
		int length=40-field41.length();
		
		try {
			return field41+ISOUtil.padleft(XLinkISO8583Constant.REQUEST_FIELD_43, length, ' ');
		} catch (ISOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public static void main(String[] args) {
		String generateField41 = generateField41("1234567890");
		System.out.println(generateField41);
		System.out.println(generateField42("1234567890"));
		System.out.println(generateField43("1234567890"));
	}
}
