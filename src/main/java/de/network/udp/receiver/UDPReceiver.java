package de.network.udp.receiver;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import de.network.udp.UDPInterface;
import de.network.udp.observer.IncomingCommunicationListener;

public class UDPReceiver extends UDPInterface {
	
	private List<IncomingCommunicationListener> listeners = new ArrayList<>();
	private Speaker speaker;

	public UDPReceiver(int port) {
		super(port);
	}
	
	public void addListener(IncomingCommunicationListener listener) {
		listeners.add(listener);
	}
	
	public void emitIncomingEvent(String host, int port, boolean confirmed) {
		listeners.forEach(listener -> listener.onIncomingCommunication(host, port, this.port, confirmed));
	}

	@Override
	public void run() {
		running = true;
		while(running) {
			// Auf Anfrage warten
			DatagramPacket packet = new DatagramPacket(new byte[PACKET_BYTE_SIZE], PACKET_BYTE_SIZE);
			try {
				socket.receive(packet);
				processDataPacket(packet);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// TODO erst wenn aufgelegt wird
		speaker.closeStream();
	}
	
	private void processDataPacket(DatagramPacket packet) {
		// Empfänger auslesen
		InetAddress address = packet.getAddress();
		String host = address.getHostAddress();
		int port = packet.getPort();
		int len = packet.getLength();
		byte[] data = packet.getData();
		String dataHeader = new String(data, 0, HEADER_SIZE);
		
		if (speaker == null)
			speaker = new Speaker();
		// Prüfung ob neuer Client
		if (dataHeader.startsWith(ACKNOWLEDGE_HEAD)) {
			System.out.println(this.port + " : " + dataHeader);
			// Port des Receiver des neuen Clients extrahieren und an den eigenen Sender emitten zur Aufnahmen der Empfänger
			int portReceiverOfRequestClient = Integer.valueOf(dataHeader.substring(dataHeader.indexOf('/') + 1, HEADER_SIZE));
			emitIncomingEvent(host, portReceiverOfRequestClient, false);
		} else if (dataHeader.startsWith(CONFIRM_ACKNOWLEDGEMENT_HEAD)) { // Confirm vom zuvor angeforderten Client
			System.out.println(this.port + " : " + dataHeader);
			// Port des Receiver des neuen Clients extrahieren und an den eigenen Sender emitten zur Aufnahmen der Empfänger
			int portReceiverOfRequestClient = Integer.valueOf(dataHeader.substring(dataHeader.indexOf('/') + 1, HEADER_SIZE));
			emitIncomingEvent(host, portReceiverOfRequestClient, true);
		} else {
			// Auf Lautsprecher ausgeben
			speaker.outputToSpeaker(data);
		}

		//System.out.printf("Hier %d Msg von %s vom Port %d mit der Länge %d:%n%s%n", this.port, address, port, len,
		//		new String(data, 0, len));
	}
}
