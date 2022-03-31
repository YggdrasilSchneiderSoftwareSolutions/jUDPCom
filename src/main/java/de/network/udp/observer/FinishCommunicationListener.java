package de.network.udp.observer;

public interface FinishCommunicationListener extends CommunicationListener {
	void onEndCommunication(String address);
}
