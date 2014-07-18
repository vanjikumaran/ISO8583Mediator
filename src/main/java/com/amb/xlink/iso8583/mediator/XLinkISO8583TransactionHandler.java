package com.amb.xlink.iso8583.mediator;

import com.amb.xlink.iso8583.jpos.util.NetworkMgtUtil;
import com.amb.xlink.iso8583.jpos.util.XLinkMessageHelper;
import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOFilter.VetoException;
import org.jpos.iso.ISOMsg;

import java.io.IOException;
import java.util.Calendar;

public class XLinkISO8583TransactionHandler {

	private static final Log log = LogFactory
			.getLog(XLinkISO8583TransactionHandler.class);

	public void doKeyExchange(XLinkSessionWrapper sessionWrapper)
			throws ISOException, XLinkISO8583Exception {
		try {
			// do SingOn operation as step 1
			this.doSignOn(sessionWrapper);
			//If Sign On is successful carry on the Key Exchange
//			NO KEY EXCHANGE FOR THE MOMENT
//			if(sessionWrapper.isSignOn()){
//				ISOMsg request = new ISOMsg();
//				NetworkMgtUtil.createKeyExchangeMessage(request);
//				sessionWrapper.send(request);
//				ISOMsg response = sessionWrapper.getChannel().receive();
//				if (response != null) {
//					XLinkISO8583Util.logISOMsg(response);
//					String sessionKey = response.getString("48");
//					if (sessionKey != null && !sessionKey.isEmpty()) {
//						log.info("setting session ID " + sessionKey);
//						sessionWrapper.setSessionId(sessionKey);
//						
//					} else {
//						throw new XLinkISO8583Exception(
//								"error while exchanging the session key");
//					}
//
//				} else {
//					// TODO:need to throw exception which should be handles through
//					// the
//					// exception layer
//				}
//			}
			
		} catch (IOException e) {
			throw new XLinkISO8583Exception(
					"error while connnecting XLink system", e);
		}
		
		
	}

	/**
	 * Do sign-on process (Any sent transactions before do Sign On will be
	 * rejected.)
	 * 
	 * @param sessionWrapper
	 * @throws XLinkISO8583Exception
	 * @throws IOException
	 */
	private void doSignOn(XLinkSessionWrapper sessionWrapper)
			throws XLinkISO8583Exception, IOException {
		ISOMsg request = new ISOMsg();
		try {
			NetworkMgtUtil.createSignOnMessage(request, sessionWrapper.getNextRequestId());
			log.info("Sending Sign On");
			XLinkISO8583Util.logISOMsg(request);
			sessionWrapper.send(request);
			ISOMsg response = sessionWrapper.getChannel().receive();
			XLinkISO8583Util.logISOMsg(response);
			log.info("Sign On response received");
			if (response != null) {
				String signOnCheck = (String) response.getValue(XLinkISO8583Constant.FIELD_RESPONSE_CODE);
				//TODO: More validations required to check whether the proper response for the request.
				log.info("Validating SignOn Response.");
				if (signOnCheck != null && !signOnCheck.equalsIgnoreCase(XLinkISO8583Constant.RESPONSE_CODE_SUCCESS)) {
					log.error("Sign On failed. Non Success reponse from X-Link");
					throw new XLinkISO8583Exception(
							"error while performing sign-on disconnecting");
				} else {
					sessionWrapper.setSignOn(true);
					log.info("Signon done");
				}

			} else {
				throw new XLinkISO8583Exception(
						"error while performing sign-on disconnecting");
			}
		} catch (ISOException e) {
			throw new XLinkISO8583Exception("error while performing do-signOn",
					e);
		}

	}

	/**
	 * Handle all mobile transaction post sign operation.
	 * 
	 * @param m
	 * @param sessionWrapper
	 * @throws ISOException
	 * @throws IOException
	 * @throws VetoException
	 */
	public OMElement handleFinancialMessage(ISOMsg m,
			XLinkSessionWrapper sessionWrapper,
			org.apache.axis2.context.MessageContext msgCtx,
			org.apache.synapse.MessageContext synCtx) throws ISOException, IOException,
			VetoException {

		sessionWrapper.getChannel().send(m);
		sessionWrapper.getChannel().setLastTxTime(
				Calendar.getInstance().getTime());
		log.info("ISO Msg sent. Ready to listen");
		ISOMsg response = sessionWrapper.getChannel().receive();

		if (response != null) {
			log.info("ISO Response Received");
			XLinkISO8583Util.logISOMsg(response);
//			TODO: If status code is not 00, immediately send a reversal
		 return	XLinkMessageHelper.handleResponse(sessionWrapper, msgCtx, synCtx,
					response);
		}
		return null;
	}

	public void doReversals(ISOMsg m, XLinkSessionWrapper sessionWrapper)
			throws IOException, ISOException {

		sessionWrapper.getChannel().send(m);
	}

}
