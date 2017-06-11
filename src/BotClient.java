import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

public class BotClient {
		
	public static void main(String[] args){
		if (args.length != 2) {
            System.err.println("Usage: java BotClient <host name> <port number>");
            System.exit(1);
        }
		
		String host = args[0];
        int port = Integer.parseInt(args[1]);
        BotPlayer bot = new BotPlayer();

        System.out.println("Bot connecting to " + host + ":" + port + "...");
        
        try (
        	Socket socket = new Socket(host,port);
        	ObjectOutputStream ToServer = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream FromServer = new ObjectInputStream(socket.getInputStream());
        ) {
        	// A listening thread that prints out whatever it receives from the server
        	BotClientThread listeningThread = new BotClientThread(FromServer);
        	listeningThread.start();
        	
        	// The server always expects the first line to be an identification of what type of player you are
        	ToServer.writeObject("B");
        	
        	while (true){
        		ToServer.writeObject(bot.getNextAction());
        		//ToServer.println("look");
        		// If the server disconnects, break out of this loop
        		if (listeningThread.checkServerStatus() == false){
        			break;
        		}
        	}
        } catch (UnknownHostException e){
        	System.err.println("The host " + host + " does not exist.");
        	// recovery
        } catch (SocketException e){
        	System.err.println("Lost connection to server");
        } catch (IOException e){
        	System.err.println("Failed to connect to " + host);
        	//recovery
        }
	}
}
