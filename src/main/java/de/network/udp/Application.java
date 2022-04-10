package de.network.udp;

import de.network.udp.config.Config;
import de.network.udp.gui.UDPComUI;

public class Application {

	public static void main(String[] args) {
		Config config = new Config();
		int portSender = Integer.parseInt(config.getConfigFor("sender.port"));
		int portReceiver = Integer.parseInt(config.getConfigFor("receiver.port"));
		int portConnectionHandlerIn = Integer.parseInt(config.getConfigFor("receiver.portconnectionhandler"));
		int portConnectionHandlerOut = Integer.parseInt(config.getConfigFor("sender.portconnectionhandler"));

		UDPCommunicationUnit comUnit = new UDPCommunicationUnit(portSender, portReceiver, 
				portConnectionHandlerIn, portConnectionHandlerOut);
		new UDPComUI(comUnit);
	}

}
