package com.finance.iso.iso8583.mediator;

import java.util.Iterator;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.axiom.soap.SOAPEnvelope;
import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;

public class XLinkISO8583Util {
	private static final Log log = LogFactory.getLog(XLinkISO8583Util.class);
	
	public static void logISOMsg(ISOMsg msg) {
		log.info("----ISO MESSAGE-----");
		try {
			log.info("  MTI : " + msg.getMTI());
			for (int i = 1; i <= msg.getMaxField(); i++) {
				if (msg.hasField(i)) {
					log.info("    Field-" + i + " : "
							+ msg.getString(i));
				}
			}
		} catch (ISOException e) {
			log.error("Error occured ", e);
		} finally {
			log.info("--------End Of ISO Message------------");
		}

	}

	public static ISOMsg toISO8583(
			org.apache.axis2.context.MessageContext messageContext)
			throws AxisFault {
		SOAPEnvelope soapEnvelope = messageContext.getEnvelope();
		OMElement isoElements = soapEnvelope.getBody().getFirstElement();

		ISOMsg isoMsg = new ISOMsg();

		@SuppressWarnings("unchecked")
		Iterator<OMElement> fieldItr = isoElements.getFirstChildWithName(
				new QName(XLinkISO8583Constant.TAG_DATA))
				.getChildrenWithLocalName(XLinkISO8583Constant.TAG_FIELD);

		String mtiVal = isoElements
				.getFirstChildWithName(
						new QName(XLinkISO8583Constant.TAG_CONFIG))
				.getFirstChildWithName(new QName(XLinkISO8583Constant.TAG_MTI))
				.getText();

		try {
			isoMsg.setMTI(mtiVal);

			while (fieldItr.hasNext()) {

				OMElement isoElement = (OMElement) fieldItr.next();

				String isoValue = isoElement.getText();

				int isoTypeID = Integer.parseInt(isoElement.getAttribute(
						new QName("id")).getAttributeValue());

				isoMsg.set(isoTypeID, isoValue);

			}

			return isoMsg;

		} catch (ISOException ex) {
			throw new AxisFault("Error parsing the ISO8583 payload",ex);
		} catch (Exception e) {

			throw new AxisFault("Error processing stream", e);
		}

	}
}
