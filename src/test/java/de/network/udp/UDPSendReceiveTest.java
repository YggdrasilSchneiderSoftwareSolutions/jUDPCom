package de.network.udp;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;

import org.junit.Ignore;
import org.junit.Test;

import de.network.udp.gui.UDPComUI;
import de.network.udp.receiver.Speaker;
import de.network.udp.receiver.UDPReceiver;
import de.network.udp.sender.Microphone;
import de.network.udp.sender.UDPSender;

public class UDPSendReceiveTest {
	
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
	@Ignore
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
	
	@Test
	@Ignore
	public void testNewUnits() throws InterruptedException, UnknownHostException {
		InetAddress address = InetAddress.getByName("127.0.0.1");
		
		UDPReceiver receiver = new UDPReceiver(8888);
		UDPSender sender = new UDPSender(8887);
		
		sender.getReceivers().put(address, Integer.valueOf(8888));
		
		Thread t1 = new Thread(receiver);
		Thread t2 = new Thread(sender);
		t1.start();
		t2.start();
		
		Thread.sleep(5000);
		
		t1.interrupt();
		t2.interrupt();
	}
	
	@Test
	public void testWithUiLocal() {
		UDPCommunicationUnit comUnit1 = new UDPCommunicationUnit(8887, 8888, 
				8889, 8886);
		new UDPComUI(comUnit1);
		
		UDPCommunicationUnit comUnit2 = new UDPCommunicationUnit(9887, 9888, 
				9889, 9886);
		new UDPComUI(comUnit2);
		
		while(true) {
			
		}
	}

}
