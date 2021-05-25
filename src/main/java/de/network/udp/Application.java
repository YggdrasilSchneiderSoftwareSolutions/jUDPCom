package de.network.udp;

import de.network.udp.config.Config;
import de.network.udp.gui.UDPComUI;

public class Application {

	public static void main(String[] args) {
		Config config = new Config();
		int portSender = Integer.valueOf(config.getConfigFor("sender.port"));
		int portReceiver = Integer.valueOf(config.getConfigFor("receiver.port"));
		
		//UDPCommunicationUnit comUnit = new UDPCommunicationUnit(portSender, portReceiver);
		new UDPComUI(new UDPCommunicationUnit(portSender, portReceiver));
	}

}
