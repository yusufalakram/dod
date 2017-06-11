import java.io.IOException;
import java.net.*;
import java.util.ArrayList;

public class DODServer {
	
	private static GameLogic gameLogic;
	private static boolean listening;
	private static BlankModeWrapper wrapper;

	public static void main(String[] args){
		if (args.length != 1) {
            System.err.println("Usage: java DODServer <port number>");
            System.exit(1);
        }
		
		int port = Integer.parseInt(args[0]);
		listening = true;
		
		/* 
		 * An arrayList containing all the server threads, 
		 * which is passed to each separate thread as a constructor argument
		 */
		ArrayList<DODServerThread> allThreads = new ArrayList<DODServerThread>(50);
		
		gameLogic = new GameLogic();
		
		try (
			ServerSocket serverSocket = new ServerSocket(port);
			) 
		{
			System.out.println("Dungeon of Doom Server running on port " + serverSocket.getLocalPort());
			
			wrapper = new BlankModeWrapper();
			
			// Player's ID, which always starts at zero
			int playerID = 0;
			
			/*
			 * Every time a new client connects;
			 * add the thread to the arrayList, 
			 * start the thread,
			 * and increment playerID
			 */
			while (listening){
				Socket clientSocket = serverSocket.accept();
				DODServerThread t = new DODServerThread(gameLogic,clientSocket,playerID,allThreads,null,null,wrapper);
				allThreads.add(t);
				t.start();
				playerID++;
			}
		} catch (NullPointerException e){
			e.printStackTrace();
		} catch (IOException e){
			System.err.println("Failed to listen on port " + port);
		}
	}
}