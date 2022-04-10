package de.network.udp.observer;

import java.net.InetAddress;

@FunctionalInterface
public interface FinishCallListener extends CommunicationListener {
	void onFinishCall(InetAddress address);
}
