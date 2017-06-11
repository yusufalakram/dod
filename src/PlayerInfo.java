/**
 * This class holds the information for each player
 * An instance is created in the DODServerThread class
 */

public class PlayerInfo {
	
	private int playerID;
	private int[] playerCoordinates;
	private int goldCollected;
	private char playerType;
	private boolean gameActive;
	
	public PlayerInfo(int playerID, int[] coords){
		this.playerID = playerID;
		this.playerCoordinates = coords;
		goldCollected = 0;
		gameActive = true;
	}
	
	/**
	 * Updates the players coordinates
	 * @param newCoords
	 */
	public void updateCoordinates(int[] newCoords){
		this.playerCoordinates = newCoords;
	}
	
	/**
	 * Returns the players coordinates
	 */
	public int[] getPlayerCoordinates(){
		return playerCoordinates;
	}
	
	/**
	 * Increments the amount of gold this player has
	 */
	public void incrementGold(){
		goldCollected++;
	}
	
	/**
	 * Returns how much gold this player has
	 */
	public int getCollectedGold(){
		return goldCollected;
	}
	
	/**
	 * Sets the player type to human/bot
	 * Takes string rather than char, since the server reads entire 
	 * lines (strings) at a time, and that's where this method is used
	 */
	public void setPlayerType(String type){
		char charType = type.charAt(0);
		this.playerType = charType;
	}
	
	/**
	 * Returns the player's type (human/bot)
	 */
	public char getPlayerType(){
		return playerType;
	}
	
	/**
	 * Returns the player's ID
	 */
	public int getPlayerID(){
		return playerID;
	}
	
	/**
	 * Ends this player's game
	 */
	public void gameOver(){
		gameActive = false;
	}
	
	/**
	 * Returns whether this player is still playing or not
	 */
	public boolean isGameActive(){
		return gameActive;
	}

}
