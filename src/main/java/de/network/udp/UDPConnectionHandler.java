package de.network.udp;

public interface UDPConnectionHandler {
	/**
	 * Der Sender im Handler ruft beim Receiver an und teilt ihm seinen Receiverport mit.
	 * Der Request findet im Sekundentakt für 20 Sekunden statt.
	 * 
	 * @param host IP, die angerufen wird
	 * @param portReceiver Port des Teilnehmers
	 */
	void establishConnection(String host, int portReceiver);
	/**
	 * Der Receiver hat eine Anfrage bekommen und antwortet über den Sender.
	 * Er teilt dem Anrufer seinen Receiverport mit.
	 * 
	 * @param host IP des Anrufers
	 * @param portReceiver Port des Anrufers für Mapping
	 * @param portConnectionHandler port des ConnectionHandlers für Bestätigung
	 */
	void confirmConnection(String host, int portReceiver, int portConnectionHandler);
	/**
	 * Der Sender des auflegenden Teilnehmers sendet die Beendigung an den 
	 * Connection Handler des Gesprächspartners
	 * 
	 * @param host IP des Gesprächspartners
	 * @param portConnectionHandler Port des ConnectionHandlers
	 */
	void destroyConnection(String host, int portConnectionHandler);
}
