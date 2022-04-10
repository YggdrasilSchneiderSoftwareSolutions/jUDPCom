package de.network.udp.receiver;

import java.io.IOException;
import java.net.DatagramPacket;

import de.network.udp.UDPInterface;

public class UDPReceiver extends UDPInterface {
	
	private Speaker speaker;

	public UDPReceiver(int port) {
		super(port);
		speaker = new Speaker();
	}

	@Override
	public void run() {
		
		if (inCall)
			speaker.startStream();
		
		while (running) {
			if (inCall) {
				// Auf Anfrage warten
				DatagramPacket packet = new DatagramPacket(new byte[PACKET_BYTE_SIZE], PACKET_BYTE_SIZE);
				try {
					socket.receive(packet);
					processDataPacket(packet);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public void tearDown() {
		speaker.closeStream();
	}
	
	private void processDataPacket(DatagramPacket packet) {
		// Daten auslesen
		byte[] data = packet.getData();
		// Auf dem Lautsprecher ausgeben
		speaker.outputToSpeaker(data);
	}

}
