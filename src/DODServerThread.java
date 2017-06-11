/*
 * The structure of this class was created with the aid of the Java Tutorial code on sockets, available from:
 * https://docs.oracle.com/javase/tutorial/networking/sockets/clientServer.html
 */

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

public class DODServerThread extends Thread{
		
	private Socket socket = null;
	
	// Static GameLogic, since all clients will play the same game
	private static GameLogic game;
	
	// Creating details for the client
	private int playerID;
	private PlayerInfo player;
	
	// Bot delay between movemenets
	private static final int DELAY = 2000;
	
	// ArrayList of all threads
	ArrayList<DODServerThread> allThreads;
	
	// Boolean used to ensure that the winner announcement is only sent once
	private static boolean winAnnounced = false;
	
	// Used for the transmission of objects; specifically, the 2d map char array
	private ObjectOutputStream ToClient;
	private ObjectInputStream FromClient;
	
	// Used to update the server's map
	private JLabel[][] mapComponents;
	
	private JFrame gameWindow;
	
	private BlankModeWrapper blankMode;
	
	/**
	 * Constructor
	 * 
	 * @param gameLogic: The game logic instance used by all players
	 * @param socket: The socket to be used
	 * @param playerID: The player's assigned ID
	 * @param threads: An arraylist containing all the server threads (for players) so far
	 * @param mapComps: A 5x5 look view of the map, that corresponds to this player's view
	 * @param window: The game window
	 * @param mode: A wrapper class used to determine whether the server is blanked or not
	 */
	public DODServerThread(GameLogic gameLogic, Socket socket, int playerID, 
			ArrayList<DODServerThread> threads, JLabel[][] mapComps, JFrame window, BlankModeWrapper mode){
		super("DODServerThread");
		this.socket = socket;
		this.playerID = playerID;
		this.allThreads = threads;
		game = gameLogic;
		this.mapComponents = mapComps;
		this.gameWindow = window;
		this.blankMode = mode;
		
		// Updates the ArrayList of all server threads within GameLogic
		game.assignThreads(threads);

		try {
			ToClient = new ObjectOutputStream(socket.getOutputStream());
			FromClient = new ObjectInputStream(socket.getInputStream());
			
		} catch (IOException e){
			e.printStackTrace();
		}
		
		// Create a new player
		player = new PlayerInfo(playerID,generateCoordinates());
		System.out.println("Server: Player with ID " + playerID + " connected " + this.socket.getRemoteSocketAddress());
	}
	
	public void run(){
		try {
			// Add the player to the game
			game.addPlayer(player);
			
			// The first transmission from the client will always be the client type
			player.setPlayerType((String)FromClient.readObject());
			
			// (GUI) The client expects a current version of the map as the first transmission
			sendUpdatedMap();
			
			// (GUI) The client also needs updated info (their player ID, and how much gold they require/possess)
			sendUpdatedInfo();
			
			// (GUI) Update the map on the Server GUI
			updateServerGUIMap();

			// This while loop runs for the duration of the player's game
			while (player.isGameActive() == true){
				// Read and process client's command, and send back a response
				ToClient.writeObject(game.processClientInput(playerID, (String) FromClient.readObject()));
				
				// After each client interaction, check if anyone won the game
				if (game.checkWin()){
					if (winAnnounced == false){
						announceWinMessage();
						winAnnounced = true;
					}
					game.flushChat();
					game.quitGame();
					ToClient.close();
					showWinMessage();
					break;
				}
				
				// If this is a bot's thread, rest after each move
				if (player.getPlayerType() == 'B'){
					sleep(DELAY);
				}
			}
		} catch (IOException e){
			//e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Generates random coordinates for each new player
	 * If the generated coordinates are invalid, the method recursively calls itself
	 * Until valid coordinates are obtained
	 */
	private int[] generateCoordinates(){
		Random rand = new Random();
		int[] mapDimensions = game.getMapDimensions();
		
		// Decrement the maps width/length by 1 to avoid spawning in walls
		int mapX = mapDimensions[0]-1;
		int mapY = mapDimensions[1]-1;
		int[] coords = new int[]{rand.nextInt(mapX), rand.nextInt(mapY)};
		
		// Check if the coordinates are valid
		boolean valid = game.isValidSpawnPoint(coords);
		if (valid != true){
			generateCoordinates();
		}
		return coords;
	}
	
	/*
	 * Sends a message to the client
	 */
	public void sendMessage(String message){
		try {
			ToClient.writeObject(message);
		} catch (SocketException e){
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Sends out a win message to all clients
	 * Does this by cycling through all threads, and sending them each the same message
	 */
	private void announceWinMessage(){
		int winner = game.getWinningPlayerID();
		String message = "\n*******************\nPlayer ID " + winner + " has won!\nThe game is over!\nThanks for playing!\n*******************\n";
		for (int i = 0; i<allThreads.size(); i++){
			DODServerThread t = allThreads.get(i);
				t.sendMessage(message);
		}
	}
	
	/**
	 * Used for the SHOUT protocol
	 * Cycles through all threads, sending each one the message
	 */
	public void shoutMessage(int playerID, String message){
		for (int i = 0; i<allThreads.size(); i++){
			DODServerThread t = allThreads.get(i);
			t.sendMessage("User ID " + playerID + ": " + message);
		}
	}
	
	/**
	 * Updates every connected client's GUI
	 */
	public void updateGUI(){
		for (int i = 0; i<allThreads.size(); i++){
			DODServerThread t = allThreads.get(i);
			t.sendUpdatedMap();
			t.sendUpdatedInfo();
		}
		updateServerGUIMap();
	}
	
	/**
	 * Sends an updated map to this thread's player
	 */
	private synchronized void sendUpdatedMap(){
		char[][] charMap;
		try {
			charMap = game.lookGUI(this.playerID);
			ToClient.writeObject(charMap);
		} catch (SocketException e) {
		} catch (IOException e){
		}
	}
	
	/**
	 * Sends updated amounts of gold required/possessed to this thread's player
	 */
	private void sendUpdatedInfo(){
		int[] info = new int[3];
		try {
			info[0] = game.goldRequiredGUI(this.playerID);
			info[1] = game.goldPossessedGUI(this.playerID);
			info[2] = playerID;
			ToClient.writeObject(info);
		} catch (SocketException e) {
		} catch (IOException e){
		}
	}
	
	// Images to be used as icons
	private BufferedImage wall;
	private BufferedImage client;
	private BufferedImage robot;
	private BufferedImage empty;
	private BufferedImage gold;
	private BufferedImage exit;
	
	/**
	 * Updates the map on the server GUI
	 */
	private synchronized void updateServerGUIMap(){
		// Only run this method if blank mode is turned off
		if (!blankMode.getMode()){
			char[][] map = game.godLookGUI();
			try {
				// Retrieving the needed images, and turning them into icons
				wall = ImageIO.read(new File("icons/#.png"));
				ImageIcon wallIcon = new ImageIcon(wall);
				client = ImageIO.read(new File("icons/P.png"));
				ImageIcon playerIcon = new ImageIcon(client);
				robot = ImageIO.read(new File("icons/B.png"));
				ImageIcon robotIcon = new ImageIcon(robot);
				empty = ImageIO.read(new File("icons/dot.png"));
				ImageIcon emptyIcon = new ImageIcon(empty);
				gold = ImageIO.read(new File("icons/G.png"));
				ImageIcon goldIcon = new ImageIcon(gold);
				exit = ImageIO.read(new File("icons/E.png"));
				ImageIcon exitIcon = new ImageIcon(exit);
	    	
				// Adding icons to the map according to what is found in the look window
	    		for (int i = 0; i<mapComponents.length; i++){
	    			for (int j = 0; j<mapComponents[0].length; j++){
	    				if (map[i][j] == '#'){
	    					mapComponents[i][j].setIcon(wallIcon);
	    				} else if (map[i][j] == 'X'){
	    					mapComponents[i][j].setIcon(wallIcon);
	    				} else if (map[i][j] == 'P'){
	    					mapComponents[i][j].setIcon(playerIcon);
	    				} else if (map[i][j] == 'B'){
	    					mapComponents[i][j].setIcon(robotIcon);
	    				} else if (map[i][j] == 'G'){
	    					mapComponents[i][j].setIcon(goldIcon);
	    				} else if (map[i][j] == 'E'){
	    					mapComponents[i][j].setIcon(exitIcon);
	    				} else if (map[i][j] == '.'){
	    					mapComponents[i][j].setIcon(emptyIcon);
	    				}
	    			}
	    		}
			} catch (IOException e) {
				//e.printStackTrace();
			}
		}
	}
	
	/**
	 * Displays a win message when the game ends.
	 */
	private void showWinMessage(){
		JOptionPane.showMessageDialog(gameWindow, "Game ended!\nThe server will now shut down.");
		System.exit(0);
	}
}
