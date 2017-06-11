import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.*;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

public class DODServerGUI {
	
	private static GameLogic gameLogic;
	private static boolean listening;
	private ArrayList<DODServerThread> allThreads;
	
	// GUI Components
	private JFrame window;
	private JLabel[][] mapComponents;
	private JPanel mapDisplayPanel;
	private JPanel infoPanel;
	
	// A thread responsible for updating the map as soon as the server starts
	private DODServerMapUpdaterThread initialThread;
	
	// A wrapper used to check whether blank mode is enabled
	private BlankModeWrapper blankMode;
	
	public static void main(String[] args){
		int port;
		
		if (args.length != 1) {
             port = 0;
        } else {
        	try {
        	port = Integer.parseInt(args[0]);
        	} catch (NumberFormatException e){
        		port = 0;
        	}
        }
		
		listening = true;
		
		DODServerGUI server = new DODServerGUI(port);
	}
	
	public DODServerGUI(int portNum){
		// Show dialog to input port number, with command line argument as default values
		String portNumberString = JOptionPane.showInputDialog("Port Number", portNum);
		
		int portNumberInt = 0;
		// Parse the entered port number to an integer
		try {
			portNumberInt = Integer.parseInt(portNumberString);
		} catch (NumberFormatException e){
			JOptionPane.showMessageDialog(window, "Please only use numbers in the port field.");
			System.exit(0);
		}
		
		// Instantiate the gameLogic
		gameLogic = new GameLogic();
		
		// Instantiate the blank mode wrapper
		blankMode = new BlankModeWrapper();
		
		// Instantiate the mapComponents to the size of the map
		mapComponents = new JLabel[gameLogic.getMapHeight()][gameLogic.getMapWidth()];
		
		// ArrayList containing all threads
		allThreads = new ArrayList<DODServerThread>(50);
		
		try (
			ServerSocket serverSocket = new ServerSocket(portNumberInt);
			) 
		{
			System.out.println("Dungeon of Doom Server running on port " + serverSocket.getLocalPort());
			
			// Player's ID, which always starts at zero
			int playerID = 0;
			
			// Build the GUI
			buildGUI(serverSocket);
			
			// This thread is used in order to instantly show the map on startup, and not have to wait for players to join to show it
			initialThread = new DODServerMapUpdaterThread(gameLogic,mapComponents);
			initialThread.start();
			
			/*
			 * Every time a new client connects;
			 * add the thread to the arrayList, 
			 * start the thread,
			 * and increment playerID
			 */
			while (listening){
				Socket clientSocket = serverSocket.accept();
				DODServerThread t = new DODServerThread(gameLogic,clientSocket,playerID,allThreads,mapComponents,window,blankMode);
				allThreads.add(t);
				t.start();
				playerID++;
			}
		} catch (NullPointerException e){
			e.printStackTrace();
		} catch (IOException e){
			System.err.println("Failed to listen on port " + portNumberInt);
		}
	}
	
	/**
	 * Builds the GUI
	 * @param socket: Used to display info about the server's IP address/port
	 */
	private void buildGUI(ServerSocket socket){
		// Controls the height of the information panel
		int infoPanelHeight = 15;
		
		// Creating the window
		window = new JFrame("Dungeon of Doom Server");
		window.setPreferredSize(new Dimension(50*gameLogic.getMapWidth(),50*gameLogic.getMapHeight()+infoPanelHeight+25));
		window.setResizable(false);
		window.setLayout(new GridBagLayout());
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		
		// Creating & adding the info panel
		infoPanel = new JPanel();
		infoPanel.setBackground(Color.gray);
		infoPanel.setPreferredSize(new Dimension(50*gameLogic.getMapWidth(),infoPanelHeight));
		c.ipady = infoPanelHeight;
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 3;
		window.getContentPane().add(infoPanel,c);
		
		// Creating & adding the map panel
		mapDisplayPanel = new JPanel();
		mapDisplayPanel.setBackground(Color.gray);
		mapDisplayPanel.setPreferredSize(new Dimension(50*gameLogic.getMapWidth(),50*gameLogic.getMapHeight()));
		mapDisplayPanel.setLayout(new GridLayout(gameLogic.getMapHeight(),gameLogic.getMapWidth()));
		c.ipady = gameLogic.getMapHeight()-1;
		c.gridwidth = 3;
		c.gridx = 0;
		c.gridy = 1;
		window.getContentPane().add(mapDisplayPanel,c);
		
		// Filling up the map view with JLabels
		for (int i=0;i<mapComponents.length; i++){
			for (int j=0;j<mapComponents[0].length; j++){
				mapComponents[i][j] = new JLabel();
				mapDisplayPanel.add(mapComponents[i][j]);
			}
		}
		
		// Adding blank button
		JButton blank = new JButton ("Blank Map");
		blank.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				blankMap();
			}
		});
		infoPanel.add(blank);
		
		// Adding info to the info panel
		JLabel ipAddress = new JLabel("IP Address: ");
		try {
			ipAddress.setText("IP Address: " + InetAddress.getLocalHost());
		} catch (UnknownHostException e1) {
			e1.printStackTrace();
		}
		infoPanel.add(ipAddress);
		
		JLabel port = new JLabel ("Port: " + socket.getLocalPort());
		infoPanel.add(port);
		
		// Shut down server button
		JButton shutDown = new JButton("Shut Down Server");
		shutDown.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				System.exit(0);
			}
		});
		infoPanel.add(shutDown);
		
		// Displaying the window
		window.pack();
		window.setVisible(true);
	}
	
	/**
	 * Used to "blank" the map
	 */
	private void blankMap(){
		// If blank mode is not enabled, enable it, and blank the map
		if (!blankMode.getMode()){
			blankMode.flipMode();
			initialThread.blankMap();
		} 
		
		// If blank mode is already enabled, disable it, and update the map GUI on the server
		else {
			blankMode.flipMode();
			initialThread.updateServerViewMap();
		}
	}
}