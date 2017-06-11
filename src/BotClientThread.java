/**
 * Serves the same function as the HumanClientThread
 * Which is to print out whatever the server prints to the bot client
 */

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;

public class BotClientThread extends Thread{
	
	private ObjectInputStream FromServer;

	private boolean serverStatus = true;

	public BotClientThread(ObjectInputStream reader) {
		super("BotClientListeningThread");
		this.FromServer = reader;
		System.out.println("Connected!\n");	
	}

	/*
	 * Run the while loop until the server goes offline (returns null)
	 */
	public void run(){
		Object response;
		try {
			while((response = FromServer.readObject()) != null){
        		System.out.println("" + response);
			}			
		} catch (EOFException e) {
			System.err.println("Lost connection to server.");
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	// Returns whether the server is running or not
	public boolean checkServerStatus(){
		return serverStatus;
	}

}
