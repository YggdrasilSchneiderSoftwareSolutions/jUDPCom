package de.network.udp.observer;

public interface IncomingCommunicationListener {
	void onIncomingCommunication(String host, int port, int portMyReveicer, boolean isConfirmed);
}
