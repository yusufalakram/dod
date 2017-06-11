import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class HumanClient {
	
	public static void main(String[] args){
		if (args.length != 2) {
            System.err.println("Usage: java HumanClient <host name> <port number>");
            System.exit(1);
        }
		
		String host = args[0];
        int port = Integer.parseInt(args[1]);
        
        // A HumanPlayer object is created everytime a client starts up
        HumanPlayer player = new HumanPlayer();
        
        System.out.println("Connecting to " + host + ":" + port + "...");
        
        // A try with resources block
        try (
        	Socket socket = new Socket(host,port);
        	ObjectOutputStream ToServer = new ObjectOutputStream(socket.getOutputStream());
        	ObjectInputStream FromServer = new ObjectInputStream(socket.getInputStream());
        ) {
        	// Start a thread that constantly listens to the server and prints out what it receives
        	new HumanClientThread(FromServer).start();
        	
        	// The server always expects the first line to be an identification of what type of player you are
        	ToServer.writeObject("P");
        	
        	// In my game's protocol, "ID" returns your player's ID.
        	// Thus, by sending it in the beginning, the player can know their ID at the start of the game.
        	ToServer.writeObject("ID");
        	
        	// The following loop continues to run unless the client types "quit", upon which it will break the loop, and terminate the client.
        	while (true){
        		String playerCommand = player.getNextAction();
        		String [] command = playerCommand.trim().split(" ");
        		if (command[0].toUpperCase().equals("QUIT")){
            		ToServer.writeObject(playerCommand);
            		ToServer.close();
            		break;
        		}
        		ToServer.writeObject(playerCommand);
        	}
        	System.exit(0);
        } catch (UnknownHostException e){
        	System.err.println("The host " + host + " does not exist.");
        } catch (IOException e){
        	System.err.println("Failed to connect to " + host);
        }
	}
}
