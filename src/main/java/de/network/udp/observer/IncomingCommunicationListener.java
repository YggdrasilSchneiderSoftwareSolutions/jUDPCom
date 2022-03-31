package de.network.udp.observer;

public interface IncomingCommunicationListener extends CommunicationListener {
	void onIncomingCommunication(String host, int port, int portMyReveicer, boolean isConfirmed);
}
