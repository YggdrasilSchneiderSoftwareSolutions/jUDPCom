package de.network.udp.gui;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import de.network.udp.UDPCommunicationUnit;

public class UDPComUI extends JFrame {

	private static final long serialVersionUID = 2465587519132303250L;
	
	private JLabel labelMySenderIP;
	
	private JLabel labelMySenderPort;
	
	private JLabel labelMyReceiverPort;
	
	private JPanel myDataPanel;

	private JLabel labelInputIP;
	
	private JTextField textFieldInputIP;
	
	private JLabel labelInputPort;
	
	private JTextField textFieldInputPort;
	
	private JButton buttonCall;
	
	private JButton buttonHangUp;
	
	private JPanel inputPanel;
	
	private JPanel mainPanel;
	
	private JPanel callPanel;
	
	private JLabel labelCurrentCall;
	
	private UDPCommunicationUnit comUnit;
	
	private String lastCallIP;
	
	private String lastCallPort;
	
	public UDPComUI(UDPCommunicationUnit comUnit) {
		this.comUnit = comUnit;
		
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
		labelMySenderIP = new JLabel(localIP);
		labelMySenderPort = new JLabel("SEND:" + String.valueOf(this.comUnit.getSender().getPort()));
		labelMyReceiverPort = new JLabel("REC:" + String.valueOf(this.comUnit.getReceiver().getPort()));
		myDataPanel.add(labelMySenderIP);
		myDataPanel.add(labelMySenderPort);
		myDataPanel.add(labelMyReceiverPort);
		
		// input panel
		inputPanel = new JPanel();
		labelInputIP = new JLabel("Call IP");
		textFieldInputIP = new JTextField(9);
		labelInputPort = new JLabel("on port");
		textFieldInputPort = new JTextField(4);
		buttonCall = new JButton("Call");
		buttonCall.addActionListener(event -> {
			lastCallIP = textFieldInputIP.getText();
			lastCallPort = textFieldInputPort.getText();
			boolean ok = this.comUnit.getSender().requestAcknowledge(lastCallIP, 
					Integer.valueOf(lastCallPort), this.comUnit.getReceiver().getPort());
			if (ok) {
				labelCurrentCall.setText("Current call: " + lastCallIP + ":" + lastCallPort);
				buttonCall.setEnabled(false);
				buttonHangUp.setEnabled(true);
			}
		});
		buttonHangUp = new JButton("Hang up");
		buttonHangUp.setEnabled(false);
		buttonHangUp.addActionListener(event -> {
			this.comUnit.getSender().finishCall(lastCallIP, Integer.parseInt(lastCallPort), 
					this.comUnit.getReceiver().getPort());
			this.comUnit.getReceiverThread().interrupt();
			this.comUnit.getSenderThread().interrupt();
			buttonHangUp.setEnabled(false);
			buttonCall.setEnabled(true);
		});
		inputPanel.add(labelInputIP);
		inputPanel.add(textFieldInputIP);
		inputPanel.add(labelInputPort);
		inputPanel.add(textFieldInputPort);
		inputPanel.add(buttonCall);
		inputPanel.add(buttonHangUp);
		
		// call panel
		callPanel = new JPanel();
		labelCurrentCall = new JLabel("");
		callPanel.add(labelCurrentCall);
		
		mainPanel.add(myDataPanel);
		mainPanel.add(inputPanel);
		mainPanel.add(callPanel);
		
		add(mainPanel);
		
		pack();
		setVisible(true);
	}

}
