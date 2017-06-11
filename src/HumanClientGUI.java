import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JOptionPane;

public class HumanClientGUI {
	
	public static void main(String[] args){
		String hostName;
		int portNumber;
		
		// Set the default values for the port hostname/port number
		if (args.length != 2) {
            hostName = "";
            portNumber = 0;
        } else {
        	hostName = args[0];
        	try {
        		portNumber = Integer.parseInt(args[1]);
        	} catch (NumberFormatException e){
        		portNumber = 0;
        	}
        }
        
		// Create the GUI
        HumanClientGUI GUI = new HumanClientGUI(hostName, portNumber);
	}
	
	// The main game window
	private JFrame window;
	
	// The four main panels in the window
	private JPanel mapPanel;
	private JPanel userControlsPanel;
	private JPanel serverChatPanel;
	private JPanel textInputPanel;
	
	// The text area that displays the chat (Goes inside the serverChatPanel)
	private JTextArea textArea;
	
	// The text field the player uses to type
	JTextField textField;
	
	// The field the client uses to set who they wish to whisper to
	JTextField whisperField;
	
	// The components of the map view panel (Goes inside the mapPanel)
	private JLabel mapComponents[][] = new JLabel[5][5];
	
	// The socket to be used
	private Socket socket;
	
	// For the transmission with the server
	private ObjectInputStream FromServer;
	private ObjectOutputStream ToServer;
	
	// Information about gold required
	private JLabel goldRequired;
	private JLabel goldPossessed;
	
	// Player's ID
	private JLabel playerID;
	
	public HumanClientGUI(String host, int port){
		String hostName = null;
		String portNumber = null;
		
		try {
			// Show a dialogue box that prompts the user for the hostname and port number
			hostName = JOptionPane.showInputDialog("Host", host);
			portNumber = JOptionPane.showInputDialog("Port Number", port);
			
			// Establish a connection to the server
			socket = new Socket(hostName,Integer.parseInt(portNumber));
        	ToServer = new ObjectOutputStream(socket.getOutputStream());
        	FromServer = new ObjectInputStream(socket.getInputStream());
        	
        	// The server always expects the first line to be an identification of what type of player you are
        	ToServer.writeObject("P");
        	
        	// Build and display the GUI
        	createGUI();
        	
        	// Start a thread that constantly listens to the server, updating the GUI with everything it receives
        	new HumanClientGUIThread(FromServer, textArea, mapComponents, goldRequired, goldPossessed, playerID, window).start();
        	
		} catch (UnknownHostException e){
        	System.err.println("The host " + host + " does not exist.");
        	// GUI Element to display error
        	JOptionPane.showMessageDialog(window, "The host " + hostName + " does not exist.\nPlease try again");
        } catch (IOException e){
        	System.err.println("Failed to connect to " + hostName);
        	// GUI Element to display error
        	JOptionPane.showMessageDialog(window, "Failed to connect to " + hostName + "\nPlease try again");
        } catch (NumberFormatException e){
        	System.err.println("Please enter a valid port number.");
        	// GUI Element to display error
        	JOptionPane.showMessageDialog(window, "Invalid port number.\nPlease try again");
        }
	}
	
	/**
	 * The following method initializes the GUI
	 */
	private void createGUI(){
		// Creating the window
		window = new JFrame("Dungeon of Doom");
		window.setPreferredSize(new Dimension(514,517));
		window.setResizable(false);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// Quit's the game cleanly if the user closes the window
		window.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				try {
					ToServer.writeObject("quit");
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
	
		// Setting the window's layout
		FlowLayout flow = new FlowLayout(FlowLayout.LEFT);
		window.setLayout(flow);
					
		// Create four panels, and add them to the window
		mapPanel = new JPanel();
		mapPanel.setBackground(Color.gray);
		mapPanel.setPreferredSize(new Dimension(250,250));
		mapPanel.setLayout(new GridLayout(5,5));
		window.getContentPane().add(mapPanel);
					
		serverChatPanel = new JPanel();
		serverChatPanel.setBackground(Color.gray);
		serverChatPanel.setPreferredSize(new Dimension(250,250));
		window.getContentPane().add(serverChatPanel);
					
		userControlsPanel = new JPanel();
		userControlsPanel.setBackground(Color.gray);
		userControlsPanel.setPreferredSize(new Dimension(250,250));
		window.getContentPane().add(userControlsPanel);
					
		textInputPanel = new JPanel();
		textInputPanel.setBackground(Color.gray);
		textInputPanel.setPreferredSize(new Dimension(250,250));
		window.getContentPane().add(textInputPanel);
					
		// Creating the North/East/South/West/Pickup buttons and playerID label
		userControlsPanel.setLayout(new GridBagLayout());
		GridBagConstraints constraints = new GridBagConstraints();
					
		// Displaying the player's ID
		playerID = new JLabel ("Player ID: ");
		constraints.gridx = 1;
		constraints.gridy = 0;
		userControlsPanel.add(playerID, constraints);
		
		// North
		JButton north = new JButton("North");
		north.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				try {
					ToServer.writeObject("move n");
				} catch (SocketException e2){
					showGameAlreadyOver();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		constraints.gridx = 1;
		constraints.gridy = 1;
		userControlsPanel.add(north, constraints);
					
		// East
		JButton east = new JButton("East");
		east.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				try {
					ToServer.writeObject("move e");
				} catch (SocketException e2){
					showGameAlreadyOver();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		constraints.gridx = 2;
		constraints.gridy = 2;
		userControlsPanel.add(east, constraints);
					
		// South
		JButton south = new JButton("South");
		south.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				try {
					ToServer.writeObject("move s");
				} catch (SocketException e2){
					showGameAlreadyOver();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		constraints.gridx = 1;
		constraints.gridy = 3;
		userControlsPanel.add(south, constraints);
					
		// West
		JButton west = new JButton("West");
		west.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				try {
					ToServer.writeObject("move w");
				} catch (SocketException e2){
					showGameAlreadyOver();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		constraints.gridx = 0;
		constraints.gridy = 2;
		userControlsPanel.add(west, constraints);
					
		// Pickup
		JButton pickup = new JButton("Pickup");
		pickup.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				try {
					ToServer.writeObject("pickup");
				} catch (SocketException e2){
					showGameAlreadyOver();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		constraints.gridx = 1;
		constraints.gridy = 2;
		userControlsPanel.add(pickup, constraints);
		
		// Gold required
		goldRequired = new JLabel("Gold Required: ");
		constraints.gridx = 1;
		constraints.gridy = 4;
		userControlsPanel.add(goldRequired, constraints);
		
		// Gold possessed
		goldPossessed = new JLabel("Gold Possessed: ");
		constraints.gridx = 1;
		constraints.gridy = 5;
		userControlsPanel.add(goldPossessed, constraints);
		
		// Quit button
		JButton quit = new JButton ("Quit");
		quit.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				try {
					ToServer.writeObject("quit");
					System.exit(0);
				} catch (SocketException e2){
					showGameAlreadyOver();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		constraints.gridx = 1;
		constraints.gridy = 6;
		userControlsPanel.add(quit, constraints);
					
		// Public Message Chat
		textField = new JTextField();
		textField.setBackground(Color.white);
		textField.setPreferredSize(new Dimension(240,210));
		textInputPanel.add(textField);
		JButton sendMessage = new JButton("Shout");
		sendMessage.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				try {
					ToServer.writeObject("shout "+ textField.getText());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				textField.setText("");
			}
		});
		textInputPanel.add(sendMessage);
		
		// Private Message Chat
		whisperField = new JTextField();    // This field is used to input the destination player's ID
		whisperField.setBackground(Color.white);
		whisperField.setPreferredSize(new Dimension(40,20));
		JButton whisperMessage = new JButton("Whisper");
		whisperMessage.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				try {
					ToServer.writeObject("whisper "+ whisperField.getText() + " " + textField.getText());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				textField.setText("");
				whisperField.setText("");
			}
		});
		textInputPanel.add(whisperMessage);
		textInputPanel.add(whisperField);
					
		// Scrollable chat view
		textArea = new JTextArea();
		textArea.setBackground(Color.white);
		textArea.setLineWrap(true);
		textArea.append("--------SERVER CHAT--------\n");
		JScrollPane scrollPane = new JScrollPane(textArea, 
				JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		scrollPane.setPreferredSize(new Dimension(240,240));
		serverChatPanel.add(scrollPane);
					
		// Filling up the map view with JLabels
		for (int i=0;i<mapComponents.length; i++){
			for (int j=0;j<mapComponents[0].length; j++){
				mapComponents[i][j] = new JLabel();
				mapPanel.add(mapComponents[i][j]);
			}
		}
				
		// Displaying the window
		window.pack();
		window.setVisible(true);
	}
	
	/**
	 * Is used when the player attempts to send a command when the game has already ended
	 */
	private void showGameAlreadyOver(){
		JOptionPane.showMessageDialog(window, "The game has already ended!\nThanks for playing!");
		System.exit(0);
	}
	
}
