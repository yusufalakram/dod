import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;

public class DODServerMapUpdaterThread extends Thread{
	
	// Contains the entire map
	private static JLabel[][] mapComponents;
	
	// Contains the gameLogic being used by all players
	private static GameLogic gameLogic;
	
	public DODServerMapUpdaterThread (GameLogic game, JLabel[][] components){
		mapComponents = components;
		gameLogic = game;
	}
	
	public void run(){
		updateServerViewMap();
	}
	
	// Images to be used as icons
	private BufferedImage wall;
	private BufferedImage client;
	private BufferedImage robot;
	private BufferedImage empty;
	private BufferedImage gold;
	private BufferedImage exit;
	private BufferedImage blank;
		
	/**
	 * Updates the view of the map in the GUI
	 */
	public synchronized void updateServerViewMap(){
		// Retrieve a char array of the entire map
		char[][] map = gameLogic.godLookGUI();
			
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
		    	
			// Adding icons to the map according to what is found in the "god" look window
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
	
	/**
	 * Used to blank the map
	 */
	public synchronized void blankMap(){
		try {
			blank = ImageIO.read(new File("icons/blank.png"));
			ImageIcon blankIcon = new ImageIcon(blank);
			
			// Set all JLabels to the blank icon
			for (int i = 0; i<mapComponents.length; i++){
				for (int j = 0; j<mapComponents[0].length; j++){
					mapComponents[i][j].setIcon(blankIcon);
				}
			}
		} catch (IOException e){
			//e.printStackTrace();
		}
	}

}
