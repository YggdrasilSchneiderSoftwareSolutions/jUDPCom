package de.network.udp;

import java.net.DatagramSocket;
import java.net.SocketException;

public abstract class UDPInterface implements Runnable {
	
	public static final int PACKET_BYTE_SIZE = 1024;
	public static final int HEADER_SIZE = 12;
	public static final String ACKNOWLEDGE_HEAD = "REQ_ACK";
	public static final String TEAR_DOWN_HEAD = "REQ_END";
	public static final String CONFIRM_ACKNOWLEDGEMENT_HEAD = "RES_ACK";
	
	protected DatagramSocket socket;
	protected int port;
	protected volatile boolean running;
	protected volatile boolean inCall;
	
	public UDPInterface(int port) {
		this.port = port;
		try {
			socket = new DatagramSocket(port);
		} catch (SocketException e) {
			e.printStackTrace();
		}
		
		System.out.println(this.getClass() + " listening on Port " + port);
	}
	
	public int getPort() {
		return port;
	}
		
}
