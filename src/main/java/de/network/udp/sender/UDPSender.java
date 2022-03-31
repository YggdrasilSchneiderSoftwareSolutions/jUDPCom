package de.network.udp.sender;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import de.network.udp.UDPInterface;

public class UDPSender extends UDPInterface {

	private Map<InetAddress, Integer> receivers = new HashMap<>();
	private Microphone microphone;
	
	public UDPSender(int port) {
		super(port);
	}

	@Override
	public void run() {
		running = true;
		while (running) {
			byte[] data = getDataBytesToSend();
			receivers.forEach((address, port) -> {
				DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
				try {
					sendDataPacketToReceiver(packet);
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		}
		// TODO erst beim Auflegen
		microphone.closeStream();
	}
	
	private byte[] getDataBytesToSend() {
		// Daten vom Mic holen
		if (microphone == null) 
			return new byte[PACKET_BYTE_SIZE];
		return microphone.getInputBytes();
	}
	
	public void acceptReceiver(String address, int port, int portMyReceiver, boolean confirmed) throws UnknownHostException {
		InetAddress inetAddress = InetAddress.getByName(address);
		receivers.put(inetAddress, port);
		
		if (!confirmed) {
			byte[] resAck = (CONFIRM_ACKNOWLEDGEMENT_HEAD + "/" + portMyReceiver).getBytes();
			try {
				sendDataPacketToReceiver(new DatagramPacket(resAck, resAck.length, inetAddress, port));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		if (microphone == null) {
			microphone = new Microphone();
		}
	}
	
	public void sendDataPacketToReceiver(DatagramPacket packet) throws IOException {
		socket.send(packet);
	}
	
	public boolean requestAcknowledge(String host, int portReceiver, int portMyReceiver) {
		boolean acknowledged = true;
		byte[] reqAck = (ACKNOWLEDGE_HEAD + "/" + portMyReceiver).getBytes();
		InetAddress address;
		try {
			address = InetAddress.getByName(host);
			sendDataPacketToReceiver(new DatagramPacket(reqAck, reqAck.length, address, portReceiver));
		} catch (IOException e) {
			e.printStackTrace();
			acknowledged = false;
		}
		
		return acknowledged;
	}
	
	public void finishCall(String host, int portReceiver, int portMyReceiver) {
		sendHangUp(host, portReceiver, portMyReceiver);
		removeReceiver(host);
		microphone.closeStream();
		microphone = null;
	}

	public Map<InetAddress, Integer> getReceivers() {
		return receivers;
	}
	
	public boolean removeReceiver(String address) {
		InetAddress inetAddress;
		try {
			inetAddress = InetAddress.getByName(address);
			receivers.remove(inetAddress);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	private boolean sendHangUp(String host, int portReceiver, int portMyReceiver) {
		byte[] reqHangUp = (TEAR_DOWN_HEAD + "/" + portMyReceiver).getBytes();
		InetAddress address;
		try {
			address = InetAddress.getByName(host);
			sendDataPacketToReceiver(new DatagramPacket(reqHangUp, reqHangUp.length, address, portReceiver));
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}

}
