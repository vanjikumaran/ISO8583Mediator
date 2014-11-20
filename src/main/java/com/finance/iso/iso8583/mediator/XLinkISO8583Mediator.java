package com.finance.iso.iso8583.mediator;

import javax.xml.namespace.QName;

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.mediators.AbstractMediator;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;

import com.finance.iso.iso8583.bean.ReversalBean;
import com.finance.iso.iso8583.jpos.util.XLinkMessageHelper;

public class XLinkISO8583Mediator extends AbstractMediator implements ManagedLifecycle{
	
	private static final Log log = LogFactory.getLog(XLinkISO8583Mediator.class);
	private static final String OPERATION_TYPE = "OperationType";
	
	private String port;
	private String host;
	private int  xlinkRetryCount=10;
	private long xlinkSuspendOnRetryFail=1000L;
	private long monitorTrigger=1000L;

	@Override
	public boolean mediate(MessageContext synCtx) {
		org.apache.axis2.context.MessageContext msgCtx = ((Axis2MessageContext) synCtx)
				.getAxis2MessageContext(); 

		try {
			String mobileConnectionKey = (String) synCtx
					.getProperty(XLinkISO8583Constant.MOBILE_CONNECTION_KEY);
            log.info("Mediator : MSISDN "+mobileConnectionKey);
			XLinkConnnector connnector = XLinkConnnector.getInstance();
			connnector.setMontorTriggerDuration(monitorTrigger);
			connnector.setXLinkOnErrorRetryCount(xlinkRetryCount);
			connnector.setXLinkOnRetrySuspend(xlinkSuspendOnRetryFail);
			XLinkISO8583TransactionHandler transactionHandler = new XLinkISO8583TransactionHandler();
			log.info("Initializing the X-Link mediator");
			XLinkSessionWrapper xLinkSessionWrapper = connnector.getSession(
					mobileConnectionKey, host, port,transactionHandler);
            log.info("Connector gotSession");
			if (xLinkSessionWrapper != null && xLinkSessionWrapper.isSignOn()) {
				// if sign on means upto now
				// all session validation
				// done successfully
                log.info("Connector gotSession Success");
				String accountno = (String) synCtx.getProperty("accountno");
				String dob =(String) synCtx.getProperty("userDOB");
				XLinkAccountInfoWrapper accountInfoWrapper = new XLinkAccountInfoWrapper(accountno, dob);
		    	ISOMsg request =XLinkMessageHelper.createFinacialMessage(msgCtx,synCtx,accountInfoWrapper, xLinkSessionWrapper);
		    	ReversalBean reversalBean = generateReversalKey(request, msgCtx);
		    	log.info("Reversal Key generated for the Request: "+ reversalBean.getReversalKey());
				OMElement responseElement = transactionHandler.handleFinancialMessage(request, xLinkSessionWrapper, msgCtx, synCtx);
//		TODO:	CAPTURE the ERROR for FIN MSGS HERE FROM DOWN STREAM and  DO THE REVERSAL HERE
					if (responseElement != null) {
//						count++;
						log.info("API Response Decoding." );
							OMElement statusCode = responseElement.getFirstChildWithName(new QName("StatusCode"));
							log.info("Decoding Status Code");
							if (statusCode != null) {
								log.info("Decoded Status Code: "+statusCode.getText());
//								String statusCodeValue = statusCode.getText();
//								//TODO: FIX PROPERLY
//								if (!"000000".equals(statusCodeValue) && !"000068".equals(statusCodeValue)) {
//									//Set conditions for reversal
//									log.info("Starting Reversal builing");
//									OMElement requestElement = msgCtx.getEnvelope().getBody().getFirstElement(); //.detach();
//									log.info("First Child: "+requestElement.getLocalName());
//									
//									//REmove API Response and add APIRequest Element
//									OMFactory fac = OMAbstractFactory.getOMFactory();
//									OMElement apiRequest = fac.createOMElement(new QName("APIRequest"));
//									
//									OMElement operationType = fac.createOMElement(new  QName("OperationType"));
//									operationType.setText("51");
//									apiRequest.addChild(operationType);
//									
//									OMElement revOperationType = fac.createOMElement(new  QName("RevOperationType"));
//									revOperationType.setText(reversalBean.getOperationType());
//									apiRequest.addChild(revOperationType);
//									
//									OMElement revStatusCode = fac.createOMElement(new  QName("RevStatusCode"));
//									revStatusCode.setText(statusCodeValue);
//									msgCtx.setProperty("RevStatusCode", statusCodeValue);
//									apiRequest.addChild(revStatusCode);
//									
//									OMElement revField32 = fac.createOMElement(new  QName("RevField32"));
//									revField32.setText(reversalBean.getField32());
//									msgCtx.setProperty("RevField32", revField32);
//									apiRequest.addChild(revField32);
//									
//									if(reversalBean.getAmount() != null){
//									OMElement amountNode = fac.createOMElement(new  QName("Amount"));
//									amountNode.setText(reversalBean.getAmount());
//									apiRequest.addChild(amountNode);
//									}
//									
//									if(reversalBean.getDestination() != null){
//										OMElement toNode = fac.createOMElement(new  QName("To"));
//										toNode.setText(reversalBean.getDestination());
//										apiRequest.addChild(toNode);
//										}
//									
//									if(reversalBean.getDestinationCode() != null){
//										OMElement destCodeNode = fac.createOMElement(new  QName("DIIC"));
//										destCodeNode.setText(reversalBean.getDestinationCode());
//										apiRequest.addChild(destCodeNode);
//										}
//									
//									if(reversalBean.getDestinationCode() != null){
//										OMElement revKeyNode = fac.createOMElement(new  QName("RevKey"));
//										revKeyNode.setText(reversalBean.getReversalKey());
//										apiRequest.addChild(revKeyNode);
//										}
//									
//									
//									requestElement.detach();
//									msgCtx.getEnvelope().getBody().addChild(apiRequest);
//									
//									log.info("New Request is built");
//									
//									log.info("Setting the ReversalKey msgctx property:"+reversalBean.getReversalKey());
//									msgCtx.setProperty("reversalKey", reversalBean.getReversalKey());
//									//Stage set
//									log.info("Setting up reversal case");
//									request = XLinkMessageHelper.createFinacialMessage(msgCtx,synCtx, accountInfoWrapper,xLinkSessionWrapper);
////									reversalBean = generateReversalKey(request, msgCtx);//DO we need REVERSAL for REVERSAL?
//									responseElement = transactionHandler.handleFinancialMessage(request,xLinkSessionWrapper,msgCtx, synCtx);
//								}else{
//									//Successful message
////									break;
//								}
							}
					} else {
//						TODO: STILL NOT SENDING THE REVERSAL
						
//						//TODO: DO a reversal from Bean and then send the error response to client
//						OMFactory fac = OMAbstractFactory.getOMFactory();
//						OMElement apiRequest = fac.createOMElement(new QName("APIRequest"));
//						
//						OMElement operationType = fac.createOMElement(new  QName("OperationType"));
//						operationType.setText("51");
//						apiRequest.addChild(operationType);
//						
//						OMElement revOperationType = fac.createOMElement(new  QName("RevOperationType"));
//						revOperationType.setText(reversalBean.getOperationType());
//						apiRequest.addChild(revOperationType);
//						
//						String statusCodeValue="96";
//						OMElement revStatusCode = fac.createOMElement(new  QName("RevStatusCode"));
//						revStatusCode.setText(statusCodeValue);
//						msgCtx.setProperty("RevStatusCode", statusCodeValue);
//						apiRequest.addChild(revStatusCode);
//						
//						OMElement revField32 = fac.createOMElement(new  QName("RevField32"));
//						revField32.setText(reversalBean.getField32());
//						msgCtx.setProperty("RevField32", revField32);
//						apiRequest.addChild(revField32);
//						
//						if(reversalBean.getAmount() != null){
//						OMElement amountNode = fac.createOMElement(new  QName("Amount"));
//						amountNode.setText(reversalBean.getAmount());
//						apiRequest.addChild(amountNode);
//						}
//						
//						if(reversalBean.getDestination() != null){
//							OMElement toNode = fac.createOMElement(new  QName("To"));
//							toNode.setText(reversalBean.getDestination());
//							apiRequest.addChild(toNode);
//							}
//						
//						if(reversalBean.getDestinationCode() != null){
//							OMElement destCodeNode = fac.createOMElement(new  QName("DIIC"));
//							destCodeNode.setText(reversalBean.getDestinationCode());
//							apiRequest.addChild(destCodeNode);
//							}
//						
//						if(reversalBean.getDestinationCode() != null){
//							OMElement revKeyNode = fac.createOMElement(new  QName("RevKey"));
//							revKeyNode.setText(reversalBean.getReversalKey());
//							apiRequest.addChild(revKeyNode);
//							}
//						
//						
//						msgCtx.getEnvelope().getBody().addChild(apiRequest);
//						
//						log.info("New Request is built");
//						
//						log.info("Setting the ReversalKey msgctx property:"+reversalBean.getReversalKey());
//						msgCtx.setProperty("reversalKey", reversalBean.getReversalKey());
//						//Stage set
//						log.info("Setting up reversal case");
//						request = XLinkMessageHelper.createFinacialMessage(msgCtx,synCtx, accountInfoWrapper,xLinkSessionWrapper);
////						reversalBean = generateReversalKey(request, msgCtx);//DO we need REVERSAL for REVERSAL?
//						responseElement = transactionHandler.handleFinancialMessage(request,xLinkSessionWrapper,msgCtx, synCtx);
////					}
				}
				
			}else{
				handleException("XLink Commuincation Failed", synCtx);
			}

		} catch (Exception e) {
			e.printStackTrace();
			handleException("Error while sending message via JPos. Error: "+e.getLocalizedMessage(), synCtx);
		}

		return true;
	}
	
	private ReversalBean generateReversalKey(ISOMsg m, org.apache.axis2.context.MessageContext msgCtx){
		try {
			OMElement jSonObject = msgCtx.getEnvelope().getBody()
					.getFirstElement();
			 OMElement requestElement=jSonObject.getFirstElement();
			log.info("First Child: "+requestElement.getLocalName());
			log.info("Generating Reversal Bean");
			OMElement operationTypeNode = requestElement.getFirstChildWithName((new QName(OPERATION_TYPE)));
			String operationType = null;
			if(operationTypeNode != null){
				operationType=operationTypeNode.getText();
				log.info("Reversal: Op Type"+operationType);
			}
			OMElement amountNode = requestElement.getFirstChildWithName(new QName("Amount"));
			String amount = null;
			if(amountNode !=null){
				amount=amountNode.getText();
				log.info("Reversal: Amount"+amount);
			}
			OMElement userDataNode = requestElement.getFirstChildWithName(new QName("UserData"));
			String userData = null;
			if(userDataNode!=null){
				userData=userDataNode.getText();
				log.info("Reversal: userData"+userData);
			}
			OMElement destinationNode = requestElement.getFirstChildWithName(new QName("To"));
			String destination = null;
			if(destinationNode!=null){
				destination=destinationNode.getText();
				log.info("Reversal: To"+destination);
			}
			OMElement destinationCodeNode = requestElement.getFirstChildWithName(new QName("DIIC"));
			String destinationCode = null;
			if(destinationCodeNode!=null){
				destinationCode=destinationCodeNode.getText();
				log.info("Reversal: DIIC"+destinationCode);
			}
			return new ReversalBean((String)m.getValue(7),(String) m.getValue(11),(String)m.getValue(32), (String)m.getValue(37), userData, amount, destination, destinationCode, operationType);
		} catch (ISOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}



	public int getXlinkRetryCount() {
		return xlinkRetryCount;
	}



	public void setXlinkRetryCount(int xlinkRetryCount) {
		this.xlinkRetryCount = xlinkRetryCount;
	}



	public long getXlinkSuspendOnRetryFail() {
		return xlinkSuspendOnRetryFail;
	}



	public void setXlinkSuspendOnRetryFail(long xlinkSuspendOnRetryFail) {
		this.xlinkSuspendOnRetryFail = xlinkSuspendOnRetryFail;
	}
	
	
	

	public long getMonitorTrigger() {
		return monitorTrigger;
	}



	public void setMonitorTrigger(long monitorTrigger) {
		this.monitorTrigger = monitorTrigger;
	}



	@Override
	public void destroy() {
		// TODO Auto-generated method stub
		
	}



	@Override
	public void init(SynapseEnvironment arg0) {
		// TODO Auto-generated method stub
		//connnector = new XLinkConnnector();
	}
	
	

}
