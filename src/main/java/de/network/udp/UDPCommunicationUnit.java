package de.network.udp;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;

import de.network.udp.observer.CommunicationListener;
import de.network.udp.observer.FinishCallListener;
import de.network.udp.observer.IncomingCallListener;
import de.network.udp.receiver.UDPReceiver;
import de.network.udp.sender.UDPSender;

public class UDPCommunicationUnit {
	
	private Thread senderThread;
	private Thread receiverThread;
	private UDPSender sender;
	private UDPReceiver receiver;
	public volatile static boolean ringing;
	public volatile static boolean ackCall;
	
	private List<IncomingCallListener> incomingListeners = new ArrayList<>();
	private List<FinishCallListener> finishListeners = new ArrayList<>();
	
	/**
	 * Diese Klasse soll den Verbindungsauf- und abbau handeln
	 */
	public final class UDPConnectionHandlerImpl implements UDPConnectionHandler {
		
		private int portSender;
		private int portReveicer;
		
		private Runnable receiveHandler = () -> {
			try (DatagramSocket connectionHandlerSocket = new DatagramSocket(portReveicer)) {
				System.out.println("ConnectionHandler INCOMING listening on port " + portReveicer);
				
				while (true) {
					DatagramPacket packet = new DatagramPacket(new byte[UDPInterface.PACKET_BYTE_SIZE], 
							UDPInterface.PACKET_BYTE_SIZE);
					connectionHandlerSocket.receive(packet);
					// Empfänger auslesen
					InetAddress address = packet.getAddress();
					String host = address.getHostAddress();
					//int port = packet.getPort();
					//int len = packet.getLength();
					byte[] data = packet.getData();
					String dataHeader = new String(data);
					String[] dataParts = dataHeader.split("/");
					
					String action = dataParts[0].trim();
					int portReceiverOfCaller = Integer.parseInt(dataParts[1].trim());
					int portConnectionHandler = Integer.parseInt(dataParts[2].trim());
					
					// Prüfung ob neuer Client (Anklingeln)
					if (action.equals(UDPInterface.ACKNOWLEDGE_HEAD)) {
						System.out.println("Ring ring : " + dataHeader);
						emitEventToListeners(host, portReceiverOfCaller, portConnectionHandler);
					} else if (action.equals(UDPInterface.CONFIRM_ACKNOWLEDGEMENT_HEAD)) { // Confirm vom zuvor angeforderten Client
						System.out.println("Anrufer hat abgenommen...");
						addReceiverAndStartCall(address, portReceiverOfCaller);
						ackCall = true;
						ringing = false;
					} else if (action.equals(UDPInterface.TEAR_DOWN_HEAD)) { // Anderer Client hat aufgelegt
						System.out.println("Klack : " + dataHeader);
						removeReceiverAndStopCall(address);
						emitFinishToListeners(address);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		};
		
		public UDPConnectionHandlerImpl(int portSender, int portReceiver) {
			this.portSender = portSender;
			this.portReveicer = portReceiver;
			new Thread(receiveHandler).start();
		}

		@Override
		public void establishConnection(String host, int portReceiver) {
			try (DatagramSocket connectionHandlerSocket = new DatagramSocket(portSender)) {
				System.out.println("ConnectionHandler OUTGOING sending from port " + portSender 
						+ " trying to establish connection...");
				
				ackCall = false;
				long lastRing = System.currentTimeMillis();
				int cancel = 0;
				
				// Im Sekundentakt anklingeln, bis hingegangen wird oder Abbruch nach 20 Sekunden
				while (ringing && !ackCall) {
					long now = System.currentTimeMillis();
					if ((now - lastRing) >= 1000) {
						// TODO extra Objekt?
						byte[] reqAck = (UDPInterface.ACKNOWLEDGE_HEAD 
								+ "/" 
								+ receiver.getPort()
								+ "/"
								+ this.portReveicer).getBytes();
						InetAddress address;
						try {
							address = InetAddress.getByName(host);
							connectionHandlerSocket.send(new DatagramPacket(reqAck, reqAck.length, address, portReceiver));
						} catch (IOException e) {
							e.printStackTrace();
						}
						
						lastRing = now;
						cancel += 1000; // TODO evtl normaler Zähler? Analog UI...
						// Anrufer hat 20 sec nicht geantwortet
						if (cancel >= 20000) {
							ringing = false;
						}
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		@Override
		public void confirmConnection(String host, int portReceiver, int portConnectionHandler) {
			try (DatagramSocket connectionHandlerSocket = new DatagramSocket(portSender)) {
				System.out.println("ConnectionHandler OUTGOING sending from port " + portSender 
						+ " trying to confirm connection..." + host + ":" + portReceiver);
				// TODO eigenes Objekt?
				byte[] reqAck = (UDPInterface.CONFIRM_ACKNOWLEDGEMENT_HEAD 
						+ "/" 
						+ receiver.getPort()
						+ "/"
						+ this.portReveicer).getBytes();
				
				InetAddress address = InetAddress.getByName(host);
				connectionHandlerSocket.send(new DatagramPacket(reqAck, reqAck.length, address, portConnectionHandler));
				
				addReceiverAndStartCall(address, portReceiver);
				System.out.println("Habe angenommen...");
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		@Override
		public void destroyConnection(String host, int portConnectionHandler) {
			try (DatagramSocket connectionHandlerSocket = new DatagramSocket(portSender)) {
				System.out.println("ConnectionHandler OUTGOING sending from port " + portSender 
						+ " trying to destroy connection..." + host + ":" + portConnectionHandler);
				// TODO eigenes Objekt?
				byte[] reqAck = (UDPInterface.TEAR_DOWN_HEAD 
						+ "/" 
						+ receiver.getPort()
						+ "/"
						+ this.portReveicer).getBytes();
				
				InetAddress address = InetAddress.getByName(host);
				connectionHandlerSocket.send(new DatagramPacket(reqAck, reqAck.length, address, portConnectionHandler));
				
				removeReceiverAndStopCall(address);
				System.out.println("Gespräch beendet");
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public int getPortSender() {
			return portSender;
		}

		public int getPortReveicer() {
			return portReveicer;
		}		
	}
	
	private UDPConnectionHandlerImpl conHndl;
	
	public UDPCommunicationUnit(int portSender, int portReceiver, 
			int portConnectionHandlerIn, int portConnectionHandlerOut) {
		
		conHndl = new UDPConnectionHandlerImpl(portConnectionHandlerOut, portConnectionHandlerIn);
		
		if (portSender >= 0) {
			sender = new UDPSender(portSender);
			senderThread = new Thread(sender);
		}
		
		if (portReceiver >= 0) {
			receiver = new UDPReceiver(portReceiver);
			receiverThread = new Thread(receiver);
		}
	}
	
	public void start() {
		if (receiverThread != null) {
			receiver.inCall = true;
			receiver.running = true;
			// interrupt führt dazu, dass Thread nicht neu gestartet werden kann
			// und muss neu erzeugt werden (new oder bei wait kann er aufgeweckt werden)
			if (receiverThread.isInterrupted())
				receiverThread = new Thread(receiver);
			receiverThread.start();
		}
		if (senderThread != null) {
			sender.inCall = true;
			sender.running = true;
			if (senderThread.isInterrupted())
				senderThread = new Thread(sender);
			senderThread.start();
		}
	}
	
	public void stop() {
		if (receiverThread != null) {
			receiver.inCall = false;
			receiver.running = false;
			receiver.tearDown();
			receiverThread.interrupt();
		}
		if (senderThread != null) {
			sender.inCall = false;
			sender.running = false;
			sender.tearDown();
			senderThread.interrupt();
		}
	}
	
	public boolean requestAcknowledge(String host, int portReceiver) {
		ringing = true;
		conHndl.establishConnection(host, portReceiver);
		
		return ackCall;
	}
	
	public void addReceiverAndStartCall(InetAddress address, int port) {
		System.out.println("Telefoniere mit " + address.getHostName() + " " + port);
		sender.getReceivers().put(address, port);
		start();
	}
	
	public void removeReceiverAndStopCall(InetAddress address) {
		System.out.println("Beende Gespräch mit " + address.getHostName());
		sender.getReceivers().remove(address);
		stop();
	}
	
	public void addListener(CommunicationListener listener) {
		if (listener instanceof IncomingCallListener)
			incomingListeners.add((IncomingCallListener) listener);
		else if (listener instanceof FinishCallListener)
			finishListeners.add((FinishCallListener) listener);
	}
	
	public void emitEventToListeners(String host, int port, int conhandler) {
		incomingListeners.forEach(listener -> listener.onIncomingCall(host, port, conhandler));
	}
	
	public void emitFinishToListeners(InetAddress address) {
		finishListeners.forEach(listener -> listener.onFinishCall(address));
	}

	public Thread getSenderThread() {
		return senderThread;
	}

	public Thread getReceiverThread() {
		return receiverThread;
	}

	public UDPSender getSender() {
		return sender;
	}

	public UDPReceiver getReceiver() {
		return receiver;
	}

	public UDPConnectionHandlerImpl getConHndl() {
		return conHndl;
	}

}
