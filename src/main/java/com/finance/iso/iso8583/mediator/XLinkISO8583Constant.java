package com.finance.iso.iso8583.mediator;

public class XLinkISO8583Constant {
	
	public final static String TAG_FIELD="field";
	
	public final static String TAG_CONFIG="config";
	
	public final static String TAG_DATA="data";
	
	public final static String TAG_MTI="mti";
	
	public final static String MOBILE_CONNECTION_KEY = "MSISDN";
	
	public static final String JPOS_STREM_DEF = "basic.xml";
	
	public static final String NETWORK_REQ_MSG_MTI="0800";
	
	public static final String NETWORK_RES_MSG_MTI="0810";
	
	public static final String NET_MGT_ID_CODE_ECHO_TEST="301";
	
	public static final String NET_MGT_ID_CODE_SIGN_OFF="002";
	
	public static final String NET_MGT_ID_CODE_SIGN_ON="001";
	
	public static final String RESPONSE_CODE_SUCCESS="00";
	
	public static final int ECHO_TEST_RETRY_COUNT=3;
	
	public static final int FIELD_NET_MGT_ID_CODE=70;
	
	public static final int FIELD_RESPONSE_CODE=39;
	
	public static final String REQUEST_FIELD_18= "6014"; //TODO: This is changed as per Puji's mail.
	
	public static final String REQUEST_FIELD_32= "950"; //TODO: Verify this.
	
	public static final String REQUEST_FIELD_41= "CF"; //TODO: Verify this.
	
	public static final String REQUEST_FIELD_42= "CF"; //TODO: Verify this.
	
	public static final String REQUEST_FIELD_43= "JAKARTA      ID"; //TODO: Verify this.
	
	public static final String REQUEST_FIELD_49 = "360"; //IDR CURR CODE
	
	public static final String REQUEST_FIELD_98_ON_US="000000";
	
	public static final int FIELD_11_LENGTH=12; //TODO: This will be 12 in the prod system. Make sure to change it
	

}
