package de.network.udp.receiver;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class Speaker {
	//https://stackoverflow.com/questions/28122097/live-audio-stream-java
	private AudioFormat format = new AudioFormat(8000.0f, 16, 1, true, true);
	private SourceDataLine sourceDataLine;
	
	public Speaker() {
		try {
			sourceDataLine = AudioSystem.getSourceDataLine(format);
			sourceDataLine.open(format);
			sourceDataLine.start();
			System.out.println("Speaker started");
		} catch (LineUnavailableException e) {
			e.printStackTrace();
		}
	}
	
	public void outputToSpeaker(byte[] soundData) {
		sourceDataLine.write(soundData, 0, soundData.length);
	}
	
	public void closeStream() {
		sourceDataLine.drain();
        sourceDataLine.close();
        System.out.println("Speaker shut down");
	}

}
