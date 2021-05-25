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
		microphone = new Microphone();
		
		if (!confirmed) {
			byte[] resAck = (CONFIRM_ACKNOWLEDGEMENT_HEAD + "/" + portMyReceiver).getBytes();
			try {
				sendDataPacketToReceiver(new DatagramPacket(resAck, resAck.length, inetAddress, port));
			} catch (IOException e) {
				e.printStackTrace();
			}
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

}
