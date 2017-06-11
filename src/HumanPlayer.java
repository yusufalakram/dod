import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Runs the game with a human player and contains code needed to read inputs.
 *
 * @author : The unnamed tutor.
 */
public class HumanPlayer {

	private BufferedReader input;
	
	public HumanPlayer(){
		input = new BufferedReader(new InputStreamReader(System.in));
	}

    // Gets the next action from the human player.
    protected String getNextAction() {
    	String action = "";
    	try{
    		action = input.readLine();
    	}
        catch(IOException e){
        	System.err.println(e.toString());
        }
    	return action;
    }
}