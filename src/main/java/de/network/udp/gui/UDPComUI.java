package de.network.udp.gui;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import de.network.udp.UDPCommunicationUnit;
import de.network.udp.observer.FinishCallListener;
import de.network.udp.observer.IncomingCallListener;

public class UDPComUI extends JFrame {

	private static final long serialVersionUID = 2465587519132303250L;
	
	private JLabel labelMySenderIP;
	
	private JLabel labelMySenderPort;
	
	private JLabel labelMyReceiverPort;
	
	private JLabel labelMyReceiverPortConnectionHandler;
	
	private JPanel myDataPanel;

	private JLabel labelInputIP;
	
	private JTextField textFieldInputIP;
	
	private JLabel labelInputPort;
	
	private JTextField textFieldInputPort;
	
	private JButton buttonCall;
	
	private JButton buttonHangUp;
	
	private JButton buttonAnswerCall;
	
	private JPanel inputPanel;
	
	private JPanel mainPanel;
	
	private JPanel callPanel;
	
	private JLabel labelCurrentCall;
	
	private UDPCommunicationUnit comUnit;
	
	private String lastCallIP;
	
	private int lastCallPort;
	
	private int lastCallConnectionHandler;
	
	private int incomingSecCounter = 0;
	
	IncomingCallListener incomingListener = (host, port, conhandler) -> {
		System.out.println("Incoming call from " + host + ":" + port);
		buttonCall.setEnabled(false);
		buttonAnswerCall.setEnabled(true);
		lastCallIP = host;
		lastCallPort = port;
		lastCallConnectionHandler = conhandler;
		// Anzeige
		labelCurrentCall.setText("Incoming call: " + lastCallIP);
		pack();
		// Nach 20 mal anklingeln gilt Anruf als abgelehnt TODO umstellen auf Zeit
		++incomingSecCounter;
		if (incomingSecCounter == 20) {
			buttonAnswerCall.setEnabled(false);
			buttonCall.setEnabled(true);
			labelCurrentCall.setText("");
			pack();
			incomingSecCounter = 0;
		}
	};
	
	FinishCallListener finishListener = (address) -> {
		System.out.println("Finish call from " + address.getHostAddress());
		buttonCall.setEnabled(true);
		buttonHangUp.setEnabled(false);
		buttonAnswerCall.setEnabled(false);
		lastCallIP = "";
		lastCallPort = -1;
		lastCallConnectionHandler = -1;
		// Anzeige
		labelCurrentCall.setText("");
		pack();
	};
	
	public UDPComUI(UDPCommunicationUnit comUnit) {
		this.comUnit = comUnit;
		this.comUnit.addListener(incomingListener);
		this.comUnit.addListener(finishListener);
		
		InetAddress address = null;
		String localIP = "127.0.0.1";
		try {
			address = InetAddress.getLocalHost();
			localIP = address.getHostAddress();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		setTitle("jUDP-Com");
		setSize(500, 500);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		mainPanel = new JPanel();
		
		// own data
		myDataPanel = new JPanel();
		labelMySenderIP = new JLabel(localIP 
			+ " | ");
		labelMySenderPort = new JLabel("SEND:" + String.valueOf(this.comUnit.getSender().getPort()) 
			+ " | ");
		labelMyReceiverPort = new JLabel("REC:" + String.valueOf(this.comUnit.getReceiver().getPort()) 
			+ " | ");
		labelMyReceiverPortConnectionHandler = new JLabel("CON-HNDL:" 
				+ String.valueOf(this.comUnit.getConHndl().getPortReveicer()));
		myDataPanel.add(labelMySenderIP);
		myDataPanel.add(labelMySenderPort);
		myDataPanel.add(labelMyReceiverPort);
		myDataPanel.add(labelMyReceiverPortConnectionHandler);
		
		// input panel
		inputPanel = new JPanel();
		labelInputIP = new JLabel("Call IP");
		textFieldInputIP = new JTextField(9);
		labelInputPort = new JLabel("on port");
		textFieldInputPort = new JTextField(4);
		
		buttonCall = new JButton("Call");
		buttonCall.addActionListener(event -> {
			lastCallIP = textFieldInputIP.getText();
			lastCallConnectionHandler = Integer.parseInt(textFieldInputPort.getText());
			// Anzeige
			// FIXME wird nicht angezeigt, weil requestAck blockiert
			labelCurrentCall.setText("Calling " + lastCallIP + "...");
			pack();
			
			boolean ack = this.comUnit.requestAcknowledge(lastCallIP, lastCallConnectionHandler);
				
			// Es wurde abgehoben
			if (ack) {
				System.out.println("Am Telefon...");
				// Telefonieren
				labelCurrentCall.setText("Current call: " + lastCallIP);
				pack();
				buttonCall.setEnabled(false);
				buttonHangUp.setEnabled(true);
			} else {
				System.out.println("Anrufer hat nicht geantwortet...");
				labelCurrentCall.setText("");
				pack();
			}
		});
		
		buttonHangUp = new JButton("Hang up");
		buttonHangUp.setEnabled(false);
		buttonHangUp.addActionListener(event -> {
			this.comUnit.getConHndl().destroyConnection(lastCallIP, lastCallConnectionHandler);
			labelCurrentCall.setText("");
			pack();
			buttonHangUp.setEnabled(false);
			buttonCall.setEnabled(true);
			buttonAnswerCall.setEnabled(false);
		});
		
		buttonAnswerCall = new JButton("Answer");
		buttonAnswerCall.setEnabled(false);
		buttonAnswerCall.addActionListener(event -> {
			this.comUnit.getConHndl().confirmConnection(lastCallIP, lastCallPort, lastCallConnectionHandler);
			// Telefonieren
			labelCurrentCall.setText("Current call: " + lastCallIP);
			pack();
			buttonHangUp.setEnabled(true);
			buttonCall.setEnabled(false);
			buttonAnswerCall.setEnabled(false);
		});
		
		inputPanel.add(labelInputIP);
		inputPanel.add(textFieldInputIP);
		inputPanel.add(labelInputPort);
		inputPanel.add(textFieldInputPort);
		inputPanel.add(buttonCall);
		inputPanel.add(buttonHangUp);
		inputPanel.add(buttonAnswerCall);
		
		// call panel
		callPanel = new JPanel();
		labelCurrentCall = new JLabel("Welcome!");
		callPanel.add(labelCurrentCall);
		
		mainPanel.add(myDataPanel);
		mainPanel.add(inputPanel);
		mainPanel.add(callPanel);
		
		add(mainPanel);
		
		pack();
		setVisible(true);
	}
}
