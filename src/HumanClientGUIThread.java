/**
 * This class is meant to constantly listen to the server, printing whatever the server sends
 */

import java.awt.image.BufferedImage;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

public class HumanClientGUIThread extends Thread{
	
	private ObjectInputStream FromServer;
	private JFrame gameWindow;
	private JTextArea textArea;
	private JLabel[][] mapComponents;
	private JLabel goldRequired;
	private JLabel goldPossessed;
	private JLabel playerID;
	
	// Images to be used as icons
	private BufferedImage wall;
	private BufferedImage player;
	private BufferedImage robot;
	private BufferedImage empty;
	private BufferedImage gold;
	private BufferedImage exit;
	
	/**
	 * Constructor
	 * @param reader: Contains the ObjectInputStream reader
	 * @param text: Used to update the chat view panel
	 * @param map: Used to update the map view
	 * @param goldReq: Used to update the gold required JLabel
	 * @param goldPos: Used to update the gold possessed JLabel
	 * @param id: Used to update the playerID
	 * @param window: All popup messages are displayed onto this window
	 */
	public HumanClientGUIThread(ObjectInputStream reader, JTextArea text, JLabel map[][], 
			JLabel goldReq, JLabel goldPos, JLabel id, JFrame window) {
		super("HumanClientGUIListeningThread");
		this.FromServer = reader;
		this.textArea = text;
		this.mapComponents = map;
		this.goldRequired = goldReq;
		this.goldPossessed = goldPos;
		this.gameWindow = window;
		this.playerID = id;
	}


	public void run(){
		try {
			// The first transmission is always a current version of the map
			updateMap((char[][])FromServer.readObject());
			
			while(true){
				Object response = FromServer.readObject();
				
				// If the client receives null, it means the server has gone offline
				if (response == null){
					showLostConnection();
					System.err.println("Lost connection to server.");
					break;
				} 
				
				// If the object received is a 2d char array, it knows its an updated map, so it updates the map
				else if (response instanceof char[][]){
					updateMap((char[][]) response);
				} 
				
				// If the object received is an int array, it knows this contains updated info, so it updates the info
				else if (response instanceof int[]){
					updateInfo((int[]) response);
				} 
				
				// If the object received contains a '*' in the second character place, it recognizes this is a win message
				else if ((((String) response).charAt(1)) == '*'){
					showGameOver((String)response);
				} 
				
				// If none of the above criteria are matched, then this is a chat message, so it gets appended to the text area
				else {
					String message = (String) response;
					String [] splitUpMessage = message.trim().split(" ");
					System.out.println("" + message);
					// Only append the text area if this is a chat message
					if (!message.equals("SUCCESS") && !message.equals("FAIL") && !splitUpMessage[0].equals("You:") && !splitUpMessage[0].equals("SUCCESS,")){
						textArea.append("" + message + "\n");
					}
				}
			}
		} catch (EOFException e){
			showLostConnection();
			System.err.println("Lost connection to server.");
		} catch (IOException e) {
			//e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Updates the player's view of the map
	 * @param map: An updated look window
	 */
	private synchronized void updateMap(char[][] map){
		try {
			// Retrieving the needed images, and turning them into icons
			wall = ImageIO.read(new File("icons/#.png"));
			ImageIcon wallIcon = new ImageIcon(wall);
			player = ImageIO.read(new File("icons/P.png"));
			ImageIcon playerIcon = new ImageIcon(player);
			robot = ImageIO.read(new File("icons/B.png"));
			ImageIcon robotIcon = new ImageIcon(robot);
			empty = ImageIO.read(new File("icons/dot.png"));
			ImageIcon emptyIcon = new ImageIcon(empty);
			gold = ImageIO.read(new File("icons/G.png"));
			ImageIcon goldIcon = new ImageIcon(gold);
			exit = ImageIO.read(new File("icons/E.png"));
			ImageIcon exitIcon = new ImageIcon(exit);
	    	
			// Adding icons to the map according to what is found in the player's look window
	    	for (int i = 0; i<5; i++){
	    		for (int j = 0; j<5; j++){
	    			if (map[i][j] == '#'){
	    				mapComponents[j][i].setIcon(wallIcon);
	    			} else if (map[i][j] == 'X'){
	    				mapComponents[j][i].setIcon(wallIcon);
	    			} else if (map[i][j] == 'P'){
	    				mapComponents[j][i].setIcon(playerIcon);
	    			} else if (map[i][j] == 'B'){
	    				mapComponents[j][i].setIcon(robotIcon);
	    			} else if (map[i][j] == 'G'){
	    				mapComponents[j][i].setIcon(goldIcon);
	    			} else if (map[i][j] == 'E'){
	    				mapComponents[j][i].setIcon(exitIcon);
	    			} else if (map[i][j] == '.'){
	    				mapComponents[j][i].setIcon(emptyIcon);
	    			}
	    		}
	    	}
		} catch (IOException e) {
			//e.printStackTrace();
		}
	}
	
	/**
	 * Updates the player's gold required/possessed/id
	 * @param info: an int array containing the new info
	 */
	private void updateInfo(int[] info){
		this.goldRequired.setText("Gold Required: " + info[0]);
		this.goldPossessed.setText("Gold Possessed: " + info[1]);
		this.playerID.setText("Player ID: " + info[2]);
	}
	
	/**
	 * Shows the game over message
	 * @param message: The game over message, containing info about which player won the game
	 */
	private void showGameOver(String message){
		JOptionPane.showMessageDialog(gameWindow, message);
		System.exit(0);
	}
	
	/**
	 * Displayed whenever the connection to the server is lost
	 */
	private void showLostConnection(){
		JOptionPane.showMessageDialog(gameWindow, "Lost connection to server!");
		System.exit(0);
	}
}
