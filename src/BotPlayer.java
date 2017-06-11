import java.util.Random;

/**
 * Starts the game with a Bot Player. Contains code for bot's decision making.
 *
 * @author : The unnamed tutor.
 */
public class BotPlayer {

	private Random random;
	private static final char [] DIRECTIONS = {'N','S','E','W'};
	
	public BotPlayer(){
		random = new Random();
	}
	
    // Selects the next action the bot will perform. Simple implementation - just picks a random direction
    public String getNextAction() {
    	String action = "MOVE " + DIRECTIONS[random.nextInt(4)];
    	//System.out.println("Bots action " + action);
    	return action;
    }
}