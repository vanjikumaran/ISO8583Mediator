package com.finance.iso.iso8583.mediator;

import java.io.IOException;

import org.jpos.iso.BaseChannel;
import org.jpos.iso.ISOException;
import org.jpos.iso.ISOMsg;
import org.jpos.iso.ISOPackager;
import org.jpos.iso.ISORequestListener;
import org.jpos.iso.ISOServer;
import org.jpos.iso.ISOSource;
import org.jpos.iso.ServerChannel;
import org.jpos.iso.channel.ASCIIChannel;
import org.jpos.iso.packager.GenericPackager;

public class MockISO8583Server implements ISORequestListener {

	/**
	 * @param args
	 *            the command line arguments
	 */
	public static void main(String[] args) throws ISOException {
		String hostname = "localhost";
		int portNumber = 5000;

		ISOPackager packager = new GenericPackager(
				XLinkISO8583Constant.JPOS_STREM_DEF);
		ServerChannel channel = new ASCIIChannel(hostname, portNumber, packager);
		ISOServer server = new ISOServer(portNumber, channel, null);

		server.addISORequestListener(new MockISO8583Server());

		System.out.println("ISO8583 server started...");
		new Thread(server).start();

	}

	public boolean process(ISOSource isoSrc, ISOMsg isoMsg) {
		try {
			System.out.println("ISO8583 incoming message on host ["
					+ ((BaseChannel) isoSrc).getSocket().getInetAddress()
							.getHostAddress() + "]");

			if (isoMsg.getMTI().equalsIgnoreCase("1800")) {

				receiveMessage(isoSrc, isoMsg);
				logISOMsg(isoMsg);

			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return true;
	}

	private void receiveMessage(ISOSource isoSrc, ISOMsg isoMsg)
			throws ISOException, IOException {
		System.out.println("ISO8583 Message received...");
		ISOMsg reply = (ISOMsg) isoMsg.clone();
		reply.setMTI("1810");
		reply.set(39, "00");

		isoSrc.send(reply);
	}

	private static void logISOMsg(ISOMsg msg) {
		System.out.println("----ISO MESSAGE-----");
		try {
			System.out.println("  MTI : " + msg.getMTI());
			for (int i = 1; i <= msg.getMaxField(); i++) {
				if (msg.hasField(i)) {
					System.out.println("    Field-" + i + " : "
							+ msg.getString(i));
				}
			}
		} catch (ISOException e) {
			e.printStackTrace();
		} finally {
			System.out.println("--------------------");
		}

	}

}
