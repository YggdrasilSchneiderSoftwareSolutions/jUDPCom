package de.network.udp.sender;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

import de.network.udp.UDPInterface;

public class UDPSender extends UDPInterface {
	
	private Map<InetAddress, Integer> receivers = new HashMap<>();
	private Microphone microphone;

	public UDPSender(int port) {
		super(port);
		microphone = new Microphone();
	}

	@Override
	public void run() {
		
		if (inCall)
			microphone.startStream();
		
		while (running) {
			if (inCall) {
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
		}
	}

	public void sendDataPacketToReceiver(DatagramPacket packet) throws IOException {
		socket.send(packet);
	}
	
	public void tearDown() {
		microphone.closeStream();
		receivers.clear();
	}
	
	private byte[] getDataBytesToSend() {
		// Daten vom Mic holen
		if (microphone == null) 
			return new byte[PACKET_BYTE_SIZE];
		return microphone.getInputBytes();
	}
	
	public Map<InetAddress, Integer> getReceivers() {
		return receivers;
	}
}
