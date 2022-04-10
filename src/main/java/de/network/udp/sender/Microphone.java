package de.network.udp.sender;

import java.io.ByteArrayOutputStream;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import de.network.udp.Device;
import de.network.udp.UDPInterface;

//https://stackoverflow.com/questions/3705581/java-sound-api-capturing-microphone
// https://docs.oracle.com/javase/tutorial/sound/capturing.html
public class Microphone extends Device {
	
	private TargetDataLine line;
	private ByteArrayOutputStream out;
	
	@Override
	public void startStream() {
		out = new ByteArrayOutputStream();
		DataLine.Info info = new DataLine.Info(TargetDataLine.class, format); // format is an AudioFormat object
		if (!AudioSystem.isLineSupported(info)) {
			// Handle the error ...
			System.err.println("Line not supported by system: " + info.toString());
		}
		// Obtain and open the line.
		try {
			line = AudioSystem.getTargetDataLine(format);
			line.open(format);
			// Begin audio capture.
			line.start();
			System.out.println("Microphone listening");
		} catch (LineUnavailableException ex) {
			ex.printStackTrace();
		}
	}
	
	@Override
	public void closeStream() {
		line.flush();
		line.close();
		System.out.println("Microphone shut down");
	}
	
	public byte[] getInputBytes() {
		out.reset();
		byte[] data = new byte[UDPInterface.PACKET_BYTE_SIZE];
		// Read the next chunk of data from the TargetDataLine.
		int numBytesRead = line.read(data, 0, data.length);
		// Save this chunk of data.
		out.write(data, 0, numBytesRead);
		
		return out.toByteArray();
	}
	
}
