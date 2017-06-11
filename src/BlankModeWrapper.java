/**
 * Wrapper class used for blanking the map
 *
 */
public class BlankModeWrapper {
	
	boolean mode;
	
	public BlankModeWrapper(){
		// Default mode is false
		mode = false;
	}
	
	// Returns the current mode
	public boolean getMode(){
		return mode;
	}
	
	// Flips the mode
	public void flipMode(){
		mode = !mode;
	}

}
