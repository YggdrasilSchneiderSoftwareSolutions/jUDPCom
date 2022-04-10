package de.network.udp.observer;

@FunctionalInterface
public interface IncomingCallListener extends CommunicationListener {
	void onIncomingCall(String host, int port, int conhandler);
}
