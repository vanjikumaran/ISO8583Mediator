package com.finance.iso.iso8583.mediator;

import java.io.IOException;
import java.util.Observable;
import java.util.Observer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;

public class XLinkSessionWrapper implements Observer {

	private static final Log log = LogFactory.getLog(XLinkSessionWrapper.class);

	private XLinkChannel channel;
	private String sessionId;
	private boolean sessionClosed = false;
	private boolean signOn = false;

	public XLinkChannel getChannel() {
		return channel;
	}

	public void setChannel(XLinkChannel channel) {
		this.channel = channel;
	}

	public String getSessionId() {
		return sessionId;
	}

	public void setSessionId(String sessionId) {
		this.sessionId = sessionId;
	}

	public void closeConnection() throws IOException {
		channel.disconnect();
	}

	public boolean isSessionClosed() {
		return sessionClosed;
	}

	public void setSessionClosed(boolean sessionClosed) {
		this.sessionClosed = sessionClosed;
	}

	public boolean isSignOn() {
		return signOn;
	}

	public void setSignOn(boolean signOn) {
		this.signOn = signOn;
	}
	
	public String getNextRequestId(){
		return channel.getNextRequestId();
	}
	
	
	public void send(ISOMsg isoMsg) throws IOException, ISOException, XLinkISO8583Exception {
		if (channel != null) {
			if (channel.isConnected()) {
				channel.send(isoMsg);
			} else {
				log.error("Error while sending message to destination. Channel not connected.");
				throw new XLinkISO8583Exception("Error while sending message to destination. Channel not connected.");
				// TODO
				/*
				 * Exception Flows: TCP connection from the previous session is
				 * lost A valid Master Key not being assigned by XLink for the
				 * ESB connection Key Exchange process between the ESB and XLink
				 * failing Inability to establish a valid TCP connection between
				 * the ESB and XLink.
				 */
			}

		}

	}

	@Override
	public void update(Observable o, Object arg) {
		// TODO Auto-generated method stub
		
	}

}
