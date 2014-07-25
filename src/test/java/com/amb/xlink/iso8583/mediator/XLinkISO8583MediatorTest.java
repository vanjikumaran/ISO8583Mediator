package com.amb.xlink.iso8583.mediator;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.xml.stream.XMLStreamException;

import junit.framework.TestCase;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.engine.AxisConfiguration;
import org.apache.synapse.MessageContext;
import org.apache.synapse.config.Entry;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.core.axis2.Axis2SynapseEnvironment;

public class XLinkISO8583MediatorTest extends TestCase {

	private final Map<String, Entry> entries = new HashMap<String, Entry>();



	public void testXLinkSignOn() {

		XLinkISO8583Mediator linkISO8583Mediator = new XLinkISO8583Mediator();
		linkISO8583Mediator.init(null);
		linkISO8583Mediator.setHost("localhost");
		linkISO8583Mediator.setPort("8000");
		MessageContext msgCtx = null;

		try {
			msgCtx = this.generateMessage(msgCtx,null,null); // message # 1 ISO
			setupAccountInfo(msgCtx);
			linkISO8583Mediator.mediate(msgCtx);

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (Exception e) {
			fail("failed to perform sign-on for XLink");
		}

	}
	
/*	{
		  "APIRequest": {
		    "InquiryType": "30",
		    "UserData": "Enc(PAN|CARDNO|PIN)",
		    "TrxId": "xx",
		    "Currency": "IDR|VND"
		  }
	}	 
	
	XML...
	<APIRequest>
	  <OperationType>30</OperationType>
	  <UserData>Enc(PAN|CARDNO|PIN)</UserData>
	  <TrxId>xx</TrxId>
	  <Currency>IDR|VND</Currency>
	</APIRequest>

	*/
	
	public void testDoTransaction() {
		String payload = "<APIRequest> " + "<OperationType>30</OperationType>"
				+ "<TrxId>123456789</TrxId>" + "<Currency>IDR</Currency>"
				+ "</APIRequest>";
		XLinkISO8583Mediator linkISO8583Mediator = new XLinkISO8583Mediator();
		linkISO8583Mediator.init(null);
		linkISO8583Mediator.setHost("localhost");
		linkISO8583Mediator.setPort("8000");
		MessageContext msgCtx = null;

		try {
			OMElement payloadOM = AXIOMUtil.stringToOM(payload);
			msgCtx = this.generateMessage(msgCtx, payloadOM,null); // message # 1 ISO
			setupAccountInfo(msgCtx);
			linkISO8583Mediator.mediate(msgCtx);
			
			org.apache.axis2.context.MessageContext axis2msgCtx = ((Axis2MessageContext) msgCtx)
					.getAxis2MessageContext();
			System.out.println(axis2msgCtx.getEnvelope());

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (Exception e) {
			fail("failed to perform sign-on for XLink");
		}

	}
	
	
	/*if JSON, once converting it 
	
	<jsonObject>
	<APIRequest>
	<OperationType>30</OperationType>
	<UserData>Enc(PAN|CARDNO|PIN)</UserData>
	<TrxId>xx</TrxId><Currency>IDR|VND</Currency>
	</APIRequest>
	</jsonObject>*/
	public void testTransactionMultiple() {
		String payload = "<jsonObject><APIRequest> " + "<OperationType>30</OperationType>"
				+ "<TrxId>xx</TrxId>" + "<Currency>IDR</Currency>"
				+ "</APIRequest></jsonObject>";
		XLinkISO8583Mediator linkISO8583Mediator = new XLinkISO8583Mediator();
		linkISO8583Mediator.init(null);
		linkISO8583Mediator.setHost("localhost");
		linkISO8583Mediator.setPort("8000");
		MessageContext msgCtx = null;
		String msisdn = UUID.randomUUID().toString();
		try {
			OMElement payloadOM = AXIOMUtil.stringToOM(payload);
			msgCtx = this.generateMessage(msgCtx, payloadOM,msisdn); // message # 1 ISO
			setupAccountInfo(msgCtx);
			linkISO8583Mediator.mediate(msgCtx);
			
			org.apache.axis2.context.MessageContext axis2msgCtx = ((Axis2MessageContext) msgCtx)
					.getAxis2MessageContext();
			System.out.println(axis2msgCtx.getEnvelope());

			try {
				Thread.sleep(20000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			for (int i = 0; i < 3; i++) {
				System.out.println("iterating ####"+i);
				echoMesg(payload, linkISO8583Mediator, msgCtx, msisdn);
			}
			
		} catch (Exception e) {
			fail("failed to perform sign-on for XLink");
		}

	}

	private void echoMesg(String payload,
			XLinkISO8583Mediator linkISO8583Mediator, MessageContext msgCtx,
			String msisdn) throws XMLStreamException {
		OMElement payloadOM;
		org.apache.axis2.context.MessageContext axis2msgCtx;
		payloadOM = AXIOMUtil.stringToOM(payload);
		msgCtx = this.generateMessage(msgCtx, payloadOM,msisdn); // message # 1 ISO
		setupAccountInfo(msgCtx);
		linkISO8583Mediator.mediate(msgCtx);
		
		axis2msgCtx = ((Axis2MessageContext) msgCtx)
				.getAxis2MessageContext();
		System.out.println(axis2msgCtx.getEnvelope());
		
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	/*if JSON, once converting it 
	
	<jsonObject>
	<APIRequest>
	<OperationType>50</OperationType>
	<UserData>MTIzOjQ1Njo3ODk=</UserData>
	<TrxId>12345678</TrxId>
	<Currency>IDR</Currency>
	<Amount>999999</Amount>
	</APIRequest>
	</jsonObject>*/
	public void testPaymentJSON() {
		String payload = "<jsonObject><APIRequest> " + "<OperationType>50</OperationType>"
				+ "<UserData>MTIzNDU2OjEyMzQ1NjoxMjM0NTYxMjM0NTYxMjM0</UserData>"  //base 64encode
				+ "<TrxId>123213999</TrxId>" 
				+ "<Currency>LKR</Currency>"
				+ "<Amount>20000</Amount>"
				+ "</APIRequest></jsonObject>";
		XLinkISO8583Mediator linkISO8583Mediator = new XLinkISO8583Mediator();
		linkISO8583Mediator.init(null);
		linkISO8583Mediator.setHost("localhost");
		linkISO8583Mediator.setPort("8000");
		MessageContext msgCtx = null;

		try {
			OMElement payloadOM = AXIOMUtil.stringToOM(payload);
			msgCtx = this.generateMessage(msgCtx, payloadOM,null); // message # 1 ISO
			setupAccountInfo(msgCtx);
			linkISO8583Mediator.mediate(msgCtx);
			
			org.apache.axis2.context.MessageContext axis2msgCtx = ((Axis2MessageContext) msgCtx)
					.getAxis2MessageContext();
			System.out.println(axis2msgCtx.getEnvelope());

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (Exception e) {
			fail("failed to perform sign-on for XLink");
		}

	}

	private void setupAccountInfo(MessageContext msgCtx) {
		msgCtx.setProperty("cardno", "card123");
		msgCtx.setProperty("accountno", "accountno123");
		msgCtx.setProperty("pinno", "1234567890123456");
	}
	
	
	
/*if JSON, once converting it 
	
	<jsonObject>
	<APIRequest>
	<OperationType>50</OperationType>
	<UserData>MTIzOjQ1Njo3ODk=</UserData>
	<TrxId>12345678</TrxId>
	<Currency>IDR</Currency>
	<Amount>999999</Amount>
	</APIRequest>
	</jsonObject>*/
	public void testPaymentJSONThenSignOff() {
		String payload = "<jsonObject><APIRequest> " + "<OperationType>50</OperationType>"
				+ "<UserData>MTIzNDU2OjEyMzQ1NjoxMjM0NTYxMjM0NTYxMjM0</UserData>"  //base 64encode
				+ "<TrxId>123213999</TrxId>" 
				+ "<Currency>LKR</Currency>"
				+ "<Amount>20000</Amount>"
				+ "</APIRequest></jsonObject>";
		XLinkISO8583Mediator linkISO8583Mediator = new XLinkISO8583Mediator();
		linkISO8583Mediator.init(null);
		linkISO8583Mediator.setHost("localhost");
		linkISO8583Mediator.setPort("8000");
		MessageContext msgCtx = null;

		String msisdn = UUID.randomUUID().toString();
		try {
			OMElement payloadOM = AXIOMUtil.stringToOM(payload);
			msgCtx = this.generateMessage(msgCtx, payloadOM,msisdn); // message # 1 ISO
			setupAccountInfo(msgCtx);
			linkISO8583Mediator.mediate(msgCtx);
			
			org.apache.axis2.context.MessageContext axis2msgCtx = ((Axis2MessageContext) msgCtx)
					.getAxis2MessageContext();
			System.out.println(axis2msgCtx.getEnvelope());

			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//signOff Request.
			payload = "<jsonObject><APIRequest> " + "<OperationType>02</OperationType>"
					+ "</APIRequest></jsonObject>";
			payloadOM = AXIOMUtil.stringToOM(payload);
			msgCtx = this.generateMessage(msgCtx, payloadOM,msisdn);
			linkISO8583Mediator.mediate(msgCtx);
			Thread.sleep(10000);
		} catch (Exception e) {
			fail("failed to perform sign-on for XLink");
		}

	}
	
	
	// msgCtx = this.generateMessage(msgCtx); // message # 2 ISO
	// linkISO8583Mediator.mediate(msgCtx);
	//
	// Map<String, XLinkSessionWrapper> map = XLinkConnnector.getInstance()
	// .getMap();
	// assertEquals(2, map.size());
	//
	// try {
	// Thread.sleep(60000);
	// } catch (InterruptedException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	//
	// for (Map.Entry<String, XLinkSessionWrapper> entry : map.entrySet()) {
	// msgCtx.setProperty(XLinkISO8583Constant.MOBILE_CONNECTION_KEY,
	// entry.getKey());
	// linkISO8583Mediator.mediate(msgCtx);
	// }
	// assertEquals(2, map.size());

	private MessageContext generateMessage(MessageContext msgCtx,OMElement payload,String msisdn) {
		try {
			msgCtx = build();
			org.apache.axis2.context.MessageContext axis2MsgCtx = ((Axis2MessageContext) msgCtx)
					.getAxis2MessageContext();
			OMElement payloadOM = AXIOMUtil.stringToOM(XML_PAYLOAD_A);
			axis2MsgCtx.getEnvelope().getBody().addChild(payload !=null?payload:payloadOM);
			msgCtx.setProperty(XLinkISO8583Constant.MOBILE_CONNECTION_KEY, msisdn ==null ?(UUID
					.randomUUID().toString()):msisdn);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return msgCtx;
	}

	/**
	 * Build the test message context. This method returns a new (and
	 * independent) instance on every invocation.
	 * 
	 * @return
	 * @throws Exception
	 */
	public MessageContext build() throws Exception {

		SynapseConfiguration testConfig = new SynapseConfiguration();
		// TODO: check whether we need a SynapseEnvironment in all cases
		SynapseEnvironment synEnv = new Axis2SynapseEnvironment(
				new ConfigurationContext(new AxisConfiguration()),

				testConfig);

		MessageContext synCtx = new Axis2MessageContext(
				new org.apache.axis2.context.MessageContext(), testConfig,
				synEnv);

		for (Map.Entry<String, Entry> mapEntry : entries.entrySet()) {
			testConfig.addEntry(mapEntry.getKey(), mapEntry.getValue());
		}
		;
		SOAPEnvelope envelope = OMAbstractFactory.getSOAP11Factory()
				.getDefaultEnvelope();

		org.apache.axis2.context.MessageContext axis2MsgCtx = ((Axis2MessageContext) synCtx)
				.getAxis2MessageContext();

		axis2MsgCtx.setEnvelope(envelope);
		return synCtx;
	}
	
	private static String XML_PAYLOAD_A = "<iso8583message>" + "<config>"
			+ "<mti>1800</mti>" + "</config>" + "<data>"
			+ "<field id=\"3\">110</field>" + "<field id=\"5\">4200.00</field>"
			+ "<field id=\"48\">Simple Credit Transaction</field>"
			+ "<field id=\"6\">645.23</field>"
			+ "<field id=\"88\">66377125</field>" + "</data>"
			+ "</iso8583message>";
}
