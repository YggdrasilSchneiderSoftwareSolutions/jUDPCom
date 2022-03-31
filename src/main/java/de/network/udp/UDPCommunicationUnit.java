package de.network.udp;

import java.net.UnknownHostException;

import de.network.udp.observer.FinishCommunicationListener;
import de.network.udp.observer.IncomingCommunicationListener;
import de.network.udp.receiver.UDPReceiver;
import de.network.udp.sender.UDPSender;

public class UDPCommunicationUnit {
	
	private static class IncomingListener implements IncomingCommunicationListener {
		
		private UDPSender sender;
		
		public IncomingListener(UDPSender sender) {
			this.sender = sender;
		}

		@Override
		public void onIncomingCommunication(String host, int port, int portMyReceiver, boolean isConfirmed) {
			try {
				sender.acceptReceiver(host, port, portMyReceiver, isConfirmed);
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}
		}
	}
	
	private static class FinishListener implements FinishCommunicationListener {
		
		private UDPSender sender;
		
		public FinishListener(UDPSender sender) {
			this.sender = sender;
		}

		@Override
		public void onEndCommunication(String address) {
			sender.removeReceiver(address);
		}
		
	}
	
	private Thread senderThread;
	private Thread receiverThread;
	private UDPSender sender;
	private UDPReceiver receiver;
	
	public UDPCommunicationUnit(int portSender, int portReceiver) {
		if (portSender >= 0) {
			sender = new UDPSender(portSender);
			senderThread = new Thread(sender);
		}
		
		if (portReceiver >= 0) {
			receiver = new UDPReceiver(portReceiver);
			receiver.addListener(new IncomingListener(sender));
			receiver.addListener(new FinishListener(sender));
			receiverThread = new Thread(receiver);
		}
		
		if (receiverThread != null)
			receiverThread.start();
		if (senderThread != null)
			senderThread.start();
	}

	public Thread getSenderThread() {
		return senderThread;
	}

	public Thread getReceiverThread() {
		return receiverThread;
	}

	public UDPSender getSender() {
		return sender;
	}

	public UDPReceiver getReceiver() {
		return receiver;
	}
}
