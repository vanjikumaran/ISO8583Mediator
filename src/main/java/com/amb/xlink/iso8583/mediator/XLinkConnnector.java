package com.amb.xlink.iso8583.mediator;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.packager.GenericPackager;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

// TODO:here we need accomodate the logic such as

// Setup a session with XLink and maintain the session state
// throughout the request from that mobile device (Note: Each
// time a new mobile device connects to do a payment, the ESB
// will treat it like an ATM connecting to the XLink switch)

// Initiate a Key exchange handshake with XLink using ISO – 8583
// over TCPIP to obtain a Session Key

// process of obtaining key key..
/*
 * If there was a previous connection between the ESB and XLink,
 * the ESB shall: 1.Send a Sign Off using the ISO - 8583 message
 * over the TCP connection.
 * 
 * 2. The TCP connection between ESB – XLink shall be closed
 * 
 * 3. ESB will be assigned a unique Master Key by XLink to
 * initiate the Key Exchange process
 * 
 * 4. ESB initiate the Key Exchange process with XLink using the
 * assigned master key to the ESB On success, ESB will decode
 * the response from the XLink and extract the Session Key sent
 * by XLink
 * 
 * 5. ESB will initiate a TCP Connection to XLink using the
 * shared Session Key:Send a Sign On request
 * 
 * 6.Perform an Echo test
 */
public class XLinkConnnector {

	private static final Log log= LogFactory.getLog(XLinkConnnector.class);

	private static XLinkConnnector connnector = new XLinkConnnector();
	private Map<String, XLinkSessionWrapper> sessionMap = new ConcurrentHashMap<String, XLinkSessionWrapper>();
    private List<String> expiredSessionList = Collections.synchronizedList(new ArrayList());
    private Thread monitor = new MonitorThread();
	
	private Object mutableObj = new Object();
	private static int ERROR_RETRY_COUNT= 10;
	private static long SUSPEND_DURATION =1000L;
	private static long DEFAULT_MONITOR_DURATION=1000L;
	private static int DEFAULT_TIMEOUT=60000;

	public static XLinkConnnector getInstance() {
		return connnector;
	}
	
	public XLinkConnnector(){
		monitor.start();
	}

	/**
	 * This will handles the key exchange part between XLink.
	 * 
	 * @param mobileConnectionKey
	 * @param host
	 * @param port
	 * @param transactionHandler
	 * @return
	 * @throws ISOException
	 */
	public XLinkSessionWrapper getSession(String mobileConnectionKey,
			String host, String port,
			XLinkISO8583TransactionHandler transactionHandler)
			throws ISOException {
		XLinkSessionWrapper sessionWrapper = sessionMap
				.get(mobileConnectionKey);

		if(sessionWrapper != null){
			log.info("Session Wrapper not null");
			if(sessionWrapper.getChannel().stopped){
				//clean it..(a pre-measurement to clean up if connection closed,
				//but there is a new request triggering..)
				sessionMap.remove(mobileConnectionKey);
				expiredSessionList.remove(mobileConnectionKey);
				//To Trigger the new connection for the stale connection, make the sessionWrapper to null
				sessionWrapper=null;
			}
		}
		
		if (sessionWrapper == null) {
			log.info("Session wrapper null");
			synchronized (mutableObj) {

				sessionWrapper = sessionMap
						.get(mobileConnectionKey);//double checking...

				if (sessionWrapper == null) { 
					sessionWrapper = new XLinkSessionWrapper();
					ISOPackager packager = new GenericPackager(this.getClass()
							.getResourceAsStream(
									XLinkISO8583Constant.JPOS_STREM_DEF));
					boolean sessionCreated = false;
					int retrycount = 0;
					while (!sessionCreated && retrycount != ERROR_RETRY_COUNT) {
						try {
							log.info("Starting to initiating XLink connection");
							this.exchangeKeys(host, port, transactionHandler,
									sessionWrapper, packager);
						} catch (XLinkISO8583Exception e) {
							retrycount++;
							try {
								Thread.sleep(SUSPEND_DURATION);
							} catch (InterruptedException e1) {

							}
							continue;

						}
						sessionCreated = true;
					}
					if (retrycount == ERROR_RETRY_COUNT) {
						return null; // TODO provide proper definition
					}

					sessionWrapper.getChannel().setMsisdnKey(
							mobileConnectionKey);
					sessionWrapper.getChannel().setExpiredListRef(
							expiredSessionList);
					// assign after
					// performing the [5]
					sessionMap.put(mobileConnectionKey, sessionWrapper);
					log.info("Session map updated");
				}
			
			}

		}else{
			System.out.println("##### found session information " + sessionWrapper.getSessionId());
		}
		return sessionWrapper;
	}

	/**
	 * Method to handle key exchange and assign session key.
	 * 
	 * @param host
	 * @param port
	 * @param transactionHandler
	 * @param sessionWrapper
	 * @param packager
	 * @param chl
	 * @throws ISOException
	 * @throws XLinkISO8583Exception
	 */
	private void exchangeKeys(String host, String port,
			XLinkISO8583TransactionHandler transactionHandler,
			XLinkSessionWrapper sessionWrapper, ISOPackager packager)
			throws ISOException, XLinkISO8583Exception {
		XLinkChannel chl = null;
		try {
			chl = new XLinkChannel(host, Integer.valueOf(port), packager);
			
			try {
				//chl.setTimeout(DEFAULT_TIMEOUT);
				chl.connect();
			} catch (IOException e) {
				log.error(e);
				mutableObj.notify();
				throw new XLinkISO8583Exception(
						"Error while connecting XLink {system may be shutdown} I/O error",
						e);
			}catch (Exception e) {
				log.error(e);
				mutableObj.notify();
				throw new XLinkISO8583Exception(
						"Error while connecting XLink {system may be unexpected} I/O error",
						e);
			}

			sessionWrapper.setChannel(chl);

			transactionHandler.doKeyExchange(sessionWrapper);
			
			log.info("Sign On done and proceeding ...");

		} catch (XLinkISO8583Exception exception) {
			try {
				chl.disconnect();
			} catch (IOException e) {
				log.error("Error while cleaning the channels", e);
			}
			throw exception;
		} catch (ISOException ex) {
			try {
				chl.disconnect();
			} catch (IOException e) {
				log.error("Error while cleaning the channels", e);
			}
			throw ex;
		}
	}

	public Map<String, XLinkSessionWrapper> getMap() {
		return sessionMap;
	}
	
	
	public void setXLinkOnErrorRetryCount(int count){
		ERROR_RETRY_COUNT = count;
	}
	
	public void setXLinkOnRetrySuspend(long retrySuspend){
		SUSPEND_DURATION = retrySuspend;
	}
	
	
	public void setMontorTriggerDuration(long monitorTriggerDuration){
		DEFAULT_MONITOR_DURATION =monitorTriggerDuration;
	}
	
	@SuppressWarnings("unused")
	private class MonitorThread extends Thread {

		@Override
		public void run() {
			System.out.println("Monitor ######" + expiredSessionList.size());
			while (true) {
				// System.out.println("###### monitor"
				// +expiredSessionList.size());
				try {
					Thread.sleep(DEFAULT_MONITOR_DURATION);
				} catch (InterruptedException e) {
					this.interrupt();
				}
				this.checkExpiredSessions();
			}
		}

	
		private void checkExpiredSessions() {
			if (expiredSessionList.size() > 0) {
				System.out.println("###### MONITOR TRIGGERS #######");
				Iterator<String> listItr = expiredSessionList.iterator();
				while (listItr.hasNext()) {
					String expired = listItr.next();
					sessionMap.remove(expired);
					listItr.remove();
					System.out.println("removed #" + expired + ": sesMap "
							+ sessionMap.size() + ": expired list ->"
							+ expiredSessionList.size());
				}
			}
		}

	}

}
