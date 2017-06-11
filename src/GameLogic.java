import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Contains the main logic part of the game, as it processes.
 *
 * @author : The unnamed tutor.
 */
public class GameLogic {
	
	private Map map;
	private boolean active;
	private static final int maxPlayers = 50;
	
	// PrintWriter used to save all chat messages to log
	PrintWriter logger;
	
	// An ArrayList that holds the information for all the players currently playing
	ArrayList<PlayerInfo> players = new ArrayList<PlayerInfo>(maxPlayers);
	
	// An array list of all the current threads
	ArrayList<DODServerThread> allThreads;
	
	// The ID of the winning player, which is instantiated when a player wins (default is 0)
	private int winningPlayerID = 0;

	public GameLogic(){
		map = new Map();
		map.readMap("maps/example_map.txt");
		// Creates a log.txt file, overwriting whichever one already exists (if one exists)
		try {
			logger = new PrintWriter("log.txt", "UTF-8");
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This method is used to instantiate the allThreads field
	 * A method was made, rather than simply using a constructor argument, since the latter resulted in an error.
	 */
	public void assignThreads(ArrayList<DODServerThread> threads){
		this.allThreads = threads;
	}
	
	// Adds a PlayerInfo object to the ArrayList of all players in the game
	public void addPlayer(PlayerInfo player){
		this.players.add(player);
	}
	
    // get current players total of collected gold
    private int getPlayersCollectedGold(int playerID){
    	PlayerInfo player = players.get(playerID);
    	return player.getCollectedGold();
    }
    
    // increment current players total of collected gold
    private void incrementPlayersCollectedGold(int playerID){
    	PlayerInfo player = players.get(playerID);
    	player.incrementGold();
    }
    
    // get current players x coordinate
    private int getPlayersXCoordinate(int playerID){
    	PlayerInfo player = players.get(playerID);
    	int[] coords = player.getPlayerCoordinates();
    	return coords[0];
    }
    
    // set current players x coordinate
    private void setPlayersXCoordinate(int playerID, int newX){
    	PlayerInfo player = players.get(playerID);
    	int[] coords = player.getPlayerCoordinates();
    	coords[0] = newX;
    	player.updateCoordinates(coords);
    }

    // get current players y coordinate
    private int getPlayersYCoordinate(int playerID){
    	PlayerInfo player = players.get(playerID);
    	int[] coords = player.getPlayerCoordinates();
    	return coords[1];
    }
 
    // set current players y coordinate
    private void setPlayersYCoordinate(int playerID, int newY){
    	PlayerInfo player = players.get(playerID);
    	int[] coords = player.getPlayerCoordinates();
    	coords[1] = newY;
    	player.updateCoordinates(coords);
    }
	
    /*
     * Processes the command that the server receives from the client
     * Synchronized to ensure that a client's command is processed completely
     * Ensures that no two players can move onto the same block
     */
    public synchronized String processClientInput(int playerID, String input){
    	String result = processCommand(playerID, input);
    	return result;
    }
    
    /**
     * Processes the command. It should return a reply in form of a String, as the protocol dictates.
     * Otherwise it should return the string "Invalid".
     *
     * @param action : Input entered by the user. playerID: The ID of the player that entered this command
     * @return : Processed output or Invalid if the @param command is wrong.
     */
    private String processCommand(int playerID, String action) {
    	String [] command = action.trim().split(" ");
		String answer = "FAIL";
		
		switch (command[0].toUpperCase()){
		case "HELLO":
			answer = hello(playerID);
			break;
		case "MOVE":
			if (command.length == 2 ){
				answer = move(playerID, command[1].toUpperCase().charAt(0));
				updateGUI(playerID);
			}
			break;
		case "PICKUP":
			answer = pickup(playerID);
			updateGUI(playerID);
			break;
		case "LOOK":
			answer = look(playerID);
			break;
		case "SHOUT":
			int shoutLength = command.length;
			String shoutMessage = "";
			for (int i = 1; i<shoutLength; i++){
				shoutMessage += command[i];
				shoutMessage+= " ";
			}
			answer = shout(playerID, shoutMessage);
			break;
		case "WHISPER":
			try {
			int whisperLength = command.length;
			// The recipient is always typed in after the "WHISPER" command
			int recipient = Integer.parseInt(command[1]);
			// Create the string using a simple for loop
			String whisperMessage = "";
			for (int i = 2; i<whisperLength; i++){
				whisperMessage += command[i];
				whisperMessage+= " ";
			}
			answer = whisper(playerID, recipient, whisperMessage);
			break;
			} catch (NumberFormatException e){
				// If the client doesn't type in a number, this exception occurs
				answer = "ERROR: Invalid Syntax\nUsage: WHISPER (ID) (MESSAGE)";
				break;
			}
		case "QUIT":
			answer = "Game quit.";
			endPlayerGame(playerID, "has left the game.");
			updateGUI(playerID);
			break;
		case "ID":
			answer = "Connected! Your Player ID is " + playerID;
			break;
		default:
			answer = "FAIL";
		}
		return answer;
    }

    /**
     * @return if the game is running.
     */
    private boolean gameRunning() {
        return active;
    }

    /**
     * @return : Returns back gold player requires to exit the Dungeon.
     */
    private String hello(int playerID) {
        return "GOLD: " + (map.getGoldToWin() - getPlayersCollectedGold(playerID));
    }
    
    /**
     * Returns how much gold this player needs to win
     * @param playerID: The current player's ID
     * @return gold required
     */
    public int goldRequiredGUI(int playerID) {
    	int goldRequired = (map.getGoldToWin() - getPlayersCollectedGold(playerID));
    	if (goldRequired <= 0){
    		goldRequired = 0;
    	}
    	return goldRequired;
    }
    
    /**
     * Returns how much gold this player has picked up so far
     * @param playerID: The current player's ID
     * @return gold possessed
     */
    public int goldPossessedGUI(int playerID) {
    	return getPlayersCollectedGold(playerID);
    }

    /**
     * Checks if movement is legal and updates player's location on the map.
     *
     * @param direction : The direction of the movement.
     * @return : Protocol if success or not.
     */
    protected String move(int playerID, char direction) {
    	// Retrieve the player's information
    	PlayerInfo myPlayer = players.get(playerID);
    	    	
    	int newX = getPlayersXCoordinate(playerID);
    	int newY = getPlayersYCoordinate(playerID);
		switch (direction){
		case 'N':
			newY -=1;
			break;
		case 'E':
			newX +=1;
			break;
		case 'S':
			newY +=1;
			break;
		case 'W':
			newX -=1;
			break;
		default:
			break;
		}
		
		// The following code cycles through all the players to see if any of them are in this new position
		// If they are, then a boolean flag is raised, and the player's ID/type is stored.
		boolean otherPlayerCheck = false;
		int otherPlayerID = 0;
		char otherPlayerType = ' ';
		char myPlayerType = myPlayer.getPlayerType();
		for (int i = 0; i<players.size(); i++){
			PlayerInfo player = players.get(i);
			int[] otherPlayerCoords = player.getPlayerCoordinates();
			if(otherPlayerCoords[0] == newX && otherPlayerCoords[1] == newY && player.isGameActive()){
				otherPlayerCheck = true;
				otherPlayerID = i;
				otherPlayerType = player.getPlayerType();
				break;
			}
		}
		
		// If you try to walk onto another player
		if(otherPlayerCheck == true){
			// If it's a player of the same type, you simply fail
			if (otherPlayerType == myPlayerType){
				return "FAIL";
			} 
			
			// If a bot walks onto a player or vice versa
			else {
				// If I am a human client, end my own game
				if (myPlayerType == 'P'){
					endPlayerGame(playerID,"was caught by a bot!");
					return "\nGAME OVER!\nYou finished the game with " + players.get(playerID).getCollectedGold() + " gold.";
				} 
				
				// If I am a bot client, end my opponents game (the player I caught)
				else {
					endPlayerGame(otherPlayerID, "was caught by a bot!");
					return "CAUGHT A PLAYER!";
				}
				
			}
		} 
		
		// Otherwise, if you try to move onto something that isn't a wall, it will be successful
		else if (map.getTile(newX, newY) != '#'){
			setPlayersXCoordinate(playerID, newX);
			setPlayersYCoordinate(playerID, newY);
			return "SUCCESS";
		} 
		
		// Otherwise, you will fail.
		else {
			return "FAIL";
		}
    }

    /**
     * Converts the map from a 2D char array to a single string.
     *
     * @return : A String representation of the game map.
     */
    private String look(int playerID) {
    	// Retrieves this players details
    	PlayerInfo player = players.get(playerID);
    	int myXCoord = getPlayersXCoordinate(playerID);
    	int myYCoord = getPlayersYCoordinate(playerID);
    	
    	// get look window for current player
    	char[][] look = map.look(getPlayersXCoordinate(playerID), getPlayersYCoordinate(playerID));
    	
    	// add current player's icon to look window ... 
    	look[2][2] = player.getPlayerType();
    	
    	// look for oponents and add them to the look window
    	for (int i = 0; i<players.size(); i++){
    		// Skip the current player's ID
    		if (i == playerID){continue;}
    		
    		PlayerInfo opponent = players.get(i);
    		int[] opponentCoords = opponent.getPlayerCoordinates();
    		int xDistance = myXCoord - opponentCoords[0];
    		int yDistance = myYCoord - opponentCoords[1];
    		if(xDistance <= 2 && xDistance >= -2 && yDistance <= 2 && yDistance >= -2 && opponent.isGameActive()){
        		look[2-xDistance][2-yDistance] = opponent.getPlayerType();
        	}
    	}
    	
    	// return look window as a String for printing
    	String lookWindow = "";
    	for(int i=0; i<look.length; i++){
    		for(int j=0; j<look[i].length; j++){
    			lookWindow += look[j][i];
    		}
    		lookWindow += "\n";
    	}
        return lookWindow;
    }
    
    /**
     * Functions exactly like the regular look(), however, returns a 2d char array rather than a string
     * @param playerID
     * @return
     */
    public char[][] lookGUI(int playerID) {
    	// Retrieves this players details
    	PlayerInfo player = players.get(playerID);
    	int myXCoord = getPlayersXCoordinate(playerID);
    	int myYCoord = getPlayersYCoordinate(playerID);
    	
    	// get look window for current player
    	char[][] look = map.look(getPlayersXCoordinate(playerID), getPlayersYCoordinate(playerID));
    	
    	// add current player's icon to look window ... 
    	look[2][2] = player.getPlayerType();
    	
    	// look for oponents and add them to the look window
    	for (int i = 0; i<players.size(); i++){
    		// Skip the current player's ID
    		if (i == playerID){continue;}
    		
    		PlayerInfo opponent = players.get(i);
    		int[] opponentCoords = opponent.getPlayerCoordinates();
    		int xDistance = myXCoord - opponentCoords[0];
    		int yDistance = myYCoord - opponentCoords[1];
    		if(xDistance <= 2 && xDistance >= -2 && yDistance <= 2 && yDistance >= -2 && opponent.isGameActive()){
        		look[2-xDistance][2-yDistance] = opponent.getPlayerType();
        	}
    	}
    	return look;
    }
    
    /**
     * @return : 2d char array containing entire map
     */
    public char[][] godLookGUI(){
    	char[][] fullMap = map.getMap();
    	
    	// Add all the players to the map
    	for (int i = 0; i<players.size(); i++){
    		PlayerInfo player = players.get(i);
    		if (!player.isGameActive()){
    			continue;
    		}
    		int[] playerCoordinates = player.getPlayerCoordinates();
    		fullMap[playerCoordinates[1]][playerCoordinates[0]] = player.getPlayerType();
    	}
    	
    	return fullMap;
    }

    /**
     * Processes the player's pickup command, updating the map and the player's gold amount.
     *
     * @return If the player successfully picked-up gold or not.
     */
    protected String pickup(int playerID) {
    	if (map.getTile(getPlayersXCoordinate(playerID), getPlayersYCoordinate(playerID)) == 'G') {
    		incrementPlayersCollectedGold(playerID);
			map.replaceTile(getPlayersXCoordinate(playerID), getPlayersYCoordinate(playerID), '.');
			return "SUCCESS, GOLD COINS: " + getPlayersCollectedGold(playerID);
		}

		return "FAIL" + "\n" + "There is nothing to pick up...";
    }

    /**
     * Ends game for player when they get caught by a bot
     * Also announces that the player has left, along with the reason (quit or lost)
     */
    public void endPlayerGame(int playerID, String reason){
    	PlayerInfo player = players.get(playerID);
    	player.gameOver();
    	this.shout(playerID, reason);
    }
    
    /**
	 * Cycles through all the players, and check's if any of them meet the winning conditions
	 * @return True if all conditions are met, false otherwise
	 */
	protected boolean checkWin() {
		for (int i = 0; i<players.size(); i++){
			PlayerInfo player = players.get(i);
			if (getPlayersCollectedGold(i) >= map.getGoldToWin() && map.getTile(getPlayersXCoordinate(i), getPlayersYCoordinate(i)) == 'E' && player.isGameActive()) {
					// Take note of the winner's playerID
					winningPlayerID = i;
					return true;
				}
		}
		return false;
	}
	
	// Returns the winning player's ID
	public int getWinningPlayerID(){
		return winningPlayerID;
	}
	
	/*
	 * Returns the maps dimensions
	 */
	public int[] getMapDimensions(){
		int[] dimensions = new int[]{map.getMapWidth(), map.getMapHeight()};
		return dimensions;
	}
	
	/*
	 * Checks if a player can spawn on this location
	 */
	public boolean isValidSpawnPoint(int[] coords){
		char tile = map.getTile(coords[0], coords[1]);
		boolean otherPlayerCheck = false;
		
		// First, check if any other players are standing here
		for (int i = 0; i<players.size(); i++){
			PlayerInfo player = players.get(i);
			int[] otherPlayerCoords = player.getPlayerCoordinates();
			if(otherPlayerCoords[0] == coords[0] && otherPlayerCoords[1] == coords[1] && player.isGameActive()){
				otherPlayerCheck = true;
				break;
			}
		}
		
		// If it's an invalid spawn point, return false.
		if (tile == '#' || tile == 'G' || otherPlayerCheck == true){
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Quits the game when called
	 */
	public void quitGame() {
		active = false;
		logger.close();
	}
	
	/**
	 * Sends a message to the server
	 */
	private String shout(int playerID, String message){
		DODServerThread t = allThreads.get(playerID);
		t.shoutMessage(playerID,message);
		saveToChatLog(playerID, message, 's');
		
		return "You: " + message;
	}
	
	/**
	 * Sends a private mesage to a specific user
	 */
	private String whisper(int originPlayer, int destinationPlayer, String message){
		
		// If the user tries to enter a player ID out of bounds
		if (destinationPlayer > allThreads.size()-1){
			return "User does not exist!";
		}
		
		// If the player has left the game
		if (!players.get(destinationPlayer).isGameActive()){
			return "ERROR: Cannot send message because Player ID " + destinationPlayer + " has left the game.";
		}
		
		String finalMessage = "(PM) User ID " + originPlayer + ": " + message;
		DODServerThread destination = allThreads.get(destinationPlayer);
		destination.sendMessage(finalMessage);
		
		// The following two lines save the message to the log
		String logMessage = "User ID " + originPlayer + " whispered to User ID " + destinationPlayer + ": " + message;
		saveToChatLog(originPlayer, logMessage, 'w');
		
		return "You to User ID " + destinationPlayer + ": " + message;
	}
	
	/**
	 * Places the messages to a buffer, which is then saved to the log at the end of the game.
	 */
	private void saveToChatLog(int playerID, String message, char transmissionType){
		String finalMessage;
		
		if (transmissionType == 's'){
			finalMessage = "User ID " + playerID + " shouted: " + message;
		} else {
			finalMessage = message;
		}
		logger.println(finalMessage);
	}
	
	/**
	 * Saves all the messages in the printWriter to the log
	 */
	public void flushChat(){
		logger.close();
	}
	
	/**
	 * Calls a method in this player's server thread that updates the GUI for all other players
	 * @param playerID
	 */
	private void updateGUI(int playerID){
		DODServerThread t = allThreads.get(playerID);
		t.updateGUI();
	}
	
	/**
	 * @return : a string array containing all active player ID's
	 */
	public String[] getActivePlayerIDs(){
		int activePlayerCount = 0;
		
		for (int i = 0; i<players.size(); i++){
			PlayerInfo player = players.get(i);
			if (player.isGameActive()){
				activePlayerCount++;
			}
		}
		
		String[] activePlayerIDs = new String[activePlayerCount];
		int index = 0;
		for (int i = 0; i<players.size(); i++){
			PlayerInfo player = players.get(i);
			if (player.isGameActive()){
				activePlayerIDs[index] = Integer.toString(player.getPlayerID());
				index++;
			}
		}
		return activePlayerIDs;
	}
	
	public int getMapHeight(){
		return map.getMapHeight();
	}
	
	public int getMapWidth(){
		return map.getMapWidth();
	}
}