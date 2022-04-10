package de.network.udp.receiver;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

import de.network.udp.Device;

//https://stackoverflow.com/questions/28122097/live-audio-stream-java
public class Speaker extends Device {
	
	private SourceDataLine sourceDataLine;
	
	@Override
	public void startStream() {
		try {
			sourceDataLine = AudioSystem.getSourceDataLine(format);
			sourceDataLine.open(format);
			sourceDataLine.start();
			System.out.println("Speaker started");
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void closeStream() {
		sourceDataLine.flush();
        sourceDataLine.close();
        System.out.println("Speaker shut down");
	}
	
	public void outputToSpeaker(byte[] soundData) {
		sourceDataLine.write(soundData, 0, soundData.length);
	}

}
