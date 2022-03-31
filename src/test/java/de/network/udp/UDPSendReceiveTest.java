package de.network.udp;

import java.io.IOException;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;

import org.junit.Ignore;
import org.junit.Test;

import de.network.udp.receiver.Speaker;
import de.network.udp.sender.Microphone;

public class UDPSendReceiveTest {
	
	@Test
	@Ignore
	public void testCommunicationUnit() throws InterruptedException, IOException {
		// Zwei Telefone erstellen
		UDPCommunicationUnit comUnit1 = new UDPCommunicationUnit(8887, 8888);
		UDPCommunicationUnit comUnit2 = new UDPCommunicationUnit(9887, 9888);
		
		Thread.sleep(2000);
		// Telefon 1 ruft bei Telefon 2 an und wird berechtigt
		boolean ok = comUnit1.getSender().requestAcknowledge("127.0.0.1", 9888, comUnit1.getReceiver().port);
		if (ok) {
			// Telefon 1 nimmt Telefon 2 in seine Receiver auf -> es wird kommuniziert
			comUnit1.getSender().acceptReceiver("127.0.0.1", 9888, 8888, true);
		}
		
		Thread.sleep(50);
		
		comUnit1.getReceiverThread().interrupt();
		comUnit1.getSenderThread().interrupt();
		comUnit2.getReceiverThread().interrupt();
		comUnit2.getSenderThread().interrupt();
	}
	
	@Test
	@Ignore
	public void testCommunicationOnlySenderAndReceiver() throws InterruptedException, IOException {
		// Nur Sender
		UDPCommunicationUnit comUnit1 = new UDPCommunicationUnit(8887, -1);
		// Nur Receiver
		UDPCommunicationUnit comUnit2 = new UDPCommunicationUnit(-1, 9888);
		
		Thread.sleep(2000);
		// Telefon 1 ruft bei Telefon 2 an und wird berechtigt
		boolean ok = comUnit1.getSender().requestAcknowledge("127.0.0.1", 9888, 8000);
		if (ok) {
			// Telefon 1 nimmt Telefon 2 in seine Receiver auf -> es wird kommuniziert
			comUnit1.getSender().acceptReceiver("127.0.0.1", 9888, 8888, true);
		}
		
		Thread.sleep(10000);
		
		comUnit1.getSenderThread().interrupt();
		comUnit2.getReceiverThread().interrupt();
	}
	
	@Test
	@Ignore
	public void testCommunication() throws InterruptedException {
		// Zwei Telefone erstellen
		UDPCommunicationUnit comUnit1 = new UDPCommunicationUnit(8887, 8888);
		UDPCommunicationUnit comUnit2 = new UDPCommunicationUnit(9887, 9888);

		Thread.sleep(2000);
		// Telefon 1 ruft bei Telefon 2 an und wird berechtigt
		boolean ok = comUnit1.getSender().requestAcknowledge("127.0.0.1", 9888, comUnit1.getReceiver().port);
		
		Thread.sleep(10000);
		
		comUnit1.getReceiverThread().interrupt();
		comUnit1.getSenderThread().interrupt();
		comUnit2.getReceiverThread().interrupt();
		comUnit2.getSenderThread().interrupt();
	}
	
	@Test
	@Ignore
	public void testAllInputDevices() throws LineUnavailableException {
		Mixer.Info[] mixerInfos = AudioSystem.getMixerInfo();
		for (Mixer.Info info : mixerInfos) {
			Mixer m = AudioSystem.getMixer(info);
			Line.Info[] lineInfos = m.getSourceLineInfo();
			for (Line.Info lineInfo : lineInfos) {
				System.out.println(info.getName() + "---" + lineInfo);
				Line line = m.getLine(lineInfo);
				System.out.println("\t-----" + line);
			}
			lineInfos = m.getTargetLineInfo();
			for (Line.Info lineInfo : lineInfos) {
				System.out.println(m + "---" + lineInfo);
				Line line = m.getLine(lineInfo);
				System.out.println("\t-----" + line);
			}
		}
	}
	
	@Test
	public void testMicAndSpeaker() {
		
		boolean finished = false;
		
		long start = System.currentTimeMillis();
		
		Microphone mic = new Microphone();
		Speaker speaker = new Speaker();
		
		while (!finished) {
			byte[] record = mic.getInputBytes();
			speaker.outputToSpeaker(record);
			
			long now = System.currentTimeMillis();
			if ((now - start) > 5000) finished = true;
		}
		
		mic.closeStream();
		speaker.closeStream();
	}

}
