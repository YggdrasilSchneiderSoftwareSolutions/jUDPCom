package de.network.udp;

import javax.sound.sampled.AudioFormat;

public abstract class Device {
	protected AudioFormat format = new AudioFormat(8000.0f, 16, 1, true, true);
	public abstract void startStream();
	public abstract void closeStream();
}
