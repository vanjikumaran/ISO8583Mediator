package com.amb.xlink.iso8583.mediator;

import com.amb.xlink.iso8583.jpos.util.NetworkMgtUtil;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.channel.PostChannel;

import java.io.IOException;
import java.util.*;

public class XLinkChannel extends PostChannel implements Runnable {

	private static final Log log = LogFactory.getLog(XLinkChannel.class);

	volatile boolean responseReceived = false;
	volatile boolean stopped = false;
	volatile ISOMsg response = null;
	Thread receiver = null;
	private String msisdnKey;
	private List<String> expiredListRef;
	private Date lastTxTime = null;
	private long lastRequestTime = 0;
	private boolean isFinMsg=false;
	private int DEFAULT_TIMER_RATE= 180;
	private static final int TRANSACTION_ECHO_VALIDATE = 180000; //should be 3 min
	private static int requestId=0;
    private Timer statusTimer;

	public XLinkChannel(String host, int port, ISOPackager p) {
		super(host, port, p);
        //Each X-Link Channel has timer object to check the status of the connectivity
        statusTimer = new Timer();
		statusTimer.scheduleAtFixedRate(new ChannelStatusCheckTimerHander(this), 0,
				DEFAULT_TIMER_RATE * 1000);

	}

	@Override
	public void connect() throws IOException {
		super.connect();
		receiver = new Thread(this);
		receiver.start();
	}

	@Override
	public void disconnect() throws IOException {
		super.disconnect();
        //Timer should end when the X-link channel disconnect and disposed
        statusTimer.cancel();
		stopped = true;
		if(!expiredListRef.contains(msisdnKey)){
			expiredListRef.add(msisdnKey);
		}
	}

	public void run() {
		while (!stopped) {
			try {
				ISOMsg message = super.receive();
				if (isEchoTest(message)) {
					handleEchoTest(message);
				} else if (isSignOff(message)) {
					synchronized (this) {
						responseReceived = true;
						notify();
						this.disconnect();

					}

				} else {
					// This is a response
					synchronized (this) {
						response = message;
						responseReceived = true;
						notify();
					}
				}
			} catch (IOException e) {
				try {
					this.disconnect();
					//expiredListRef.add(msisdnKey);
					e.printStackTrace();
					System.out.println("Disconnecting IO Error!!!! @@@");
				} catch (IOException e1) {
					log.error("Error while terminating XLink channel", e);
					expiredListRef.add(msisdnKey);
				}
			} catch (ISOException e) {
				log.error(
						"XLink Terminated connector or client has terminated the connection",
						e);
				try {
					this.disconnect();
					//expiredListRef.add(msisdnKey);
					System.out.println("Disconnecting!!!!");
				} catch (IOException e1) {
					log.error("Error while terminating XLink channel", e);

				}
			}

		}

	}

	private void handleEchoTest(ISOMsg message) throws ISOException,
			IOException {
		System.out.println("Handling XLink initated EchoTest");
		XLinkISO8583Util.logISOMsg(message);
		message.set(XLinkISO8583Constant.FIELD_RESPONSE_CODE, XLinkISO8583Constant.RESPONSE_CODE_SUCCESS);
		message.setResponseMTI();
		XLinkISO8583Util.logISOMsg(message);
		super.send(message);
	}

	private boolean isEchoTest(ISOMsg message) throws ISOException {
		String mti = message.getMTI();
		if ((mti != null) && mti.equals(XLinkISO8583Constant.NETWORK_REQ_MSG_MTI)) {
			String netMgtIdCode = (String) message.getValue(XLinkISO8583Constant.FIELD_NET_MGT_ID_CODE);
			if ((netMgtIdCode != null) && netMgtIdCode.equals(XLinkISO8583Constant.NET_MGT_ID_CODE_ECHO_TEST)) {
				return true;
			}
		}
		return false;
	}

	private boolean isSignOff(ISOMsg message) throws ISOException {
		String mti = message.getMTI();
		XLinkISO8583Util.logISOMsg(message);
		if ((mti != null) && mti.equals(XLinkISO8583Constant.NETWORK_RES_MSG_MTI)) {
			String netMgtIdCode = (String) message.getValue(XLinkISO8583Constant.FIELD_NET_MGT_ID_CODE);
			if ((netMgtIdCode != null) && netMgtIdCode.equals(XLinkISO8583Constant.NET_MGT_ID_CODE_SIGN_OFF)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void send(ISOMsg m) throws IOException, ISOException {
		responseReceived = false;
		response = null;
		if(m.getMTI().equals("0200")){
			lastRequestTime=System.currentTimeMillis();
			isFinMsg=true;
		}else{
			isFinMsg=false;
			lastRequestTime=0;
		}
		super.send(m);

	}

	@Override
	public ISOMsg receive() throws IOException, ISOException {
		synchronized (this) {
			while (!responseReceived) {
				try {
					if(isFinMsg){
						wait(60000);
					}else{
						//SIGN ON/OFF, ECHO TEST ALSO Should terminate in 60 seconds to avoid  ESB thread block
						wait(60000);
					}
				} catch (InterruptedException ignore) {
				}
			}
		}
		return response;
	}

	public String getMsisdnKey() {
		return msisdnKey;
	}

	public void setMsisdnKey(String msisdnKey) {
		this.msisdnKey = msisdnKey;
	}

	public void setExpiredListRef(List<String> expiredListRef) {
		this.expiredListRef = expiredListRef;
	}

	public List<String> getExpiredListRef() {
		return expiredListRef;
	}

	public Date getLastTxTime() {
		return lastTxTime;
	}

	public void setLastTxTime(Date lastTxTime) {
		this.lastTxTime = lastTxTime;
	}
	
	public String getNextRequestId(){
		return ++requestId+"";
	}

	private class ChannelStatusCheckTimerHander extends TimerTask {
		private XLinkChannel channel;

		public ChannelStatusCheckTimerHander(XLinkChannel channel) {
			super();
			this.channel = channel;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			if(stopped){
				this.cancel();
				return;
			}
			if (lastTxTime != null) {
				Date now = Calendar.getInstance().getTime();
				// if last transX time - current greater than 20 mins
				if ((now.getTime() - lastTxTime.getTime()) >= TRANSACTION_ECHO_VALIDATE) {
					ISOMsg request = new ISOMsg();
					try {
						NetworkMgtUtil.createEchoTestMessage(request, getNextRequestId());
						int retryCount = 0;
						ISOMsg response = null;
						while (retryCount < XLinkISO8583Constant.ECHO_TEST_RETRY_COUNT) {
							try {
								System.out.println("echo test performing for msisdn " +msisdnKey);
								channel.send(request);
								response = channel.receive();
								
								//TODO: Handle and Validate the ECHO TEST RESPONSE and BREAK the LOOP
								
								break;
							} catch (Exception e) {
								retryCount++;
							}

						}
						if (retryCount == XLinkISO8583Constant.ECHO_TEST_RETRY_COUNT) {
							channel.disconnect();
							System.out
									.println("### Last Tx exceeds 180seconds and echo failed closing session");
							this.cancel();
						}

					} catch (ISOException e) {
						log.error("{Status expiary checking} unexpected error",
								e);
					} catch (IOException e) {
						log.error("{Status expiary checking} unexpected error",
								e);
					}
				}
			}

		}

	}

}
