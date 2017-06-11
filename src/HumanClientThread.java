/**
 * This class is meant to constantly listen to the server, printing whatever the server sends
 */

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;

public class HumanClientThread extends Thread{
	
	private ObjectInputStream FromServer;
	
	public HumanClientThread(ObjectInputStream reader) {
		super("HumanClientListeningThread");
		this.FromServer = reader;
	}

	/**
	 * The while loop in this run method continues to run until it loses connection to the server (when response == null)
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
}
