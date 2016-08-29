import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;
/**
 * Created by clark on 22/8/16.
 */

public class GameServer implements GameService {
	public GameServer() {}
	public GameServer(int n, int k) { N =n;K=k; }

	@Override
	public Boolean isActive() throws RemoteException {
		return true;
	}

	public void printGameState(){
		System.out.println("Current Game State:");
		//print out game state
	}

	public void makeMove(int m){
		if(m != 1 && m != 2 && m != 3 && m != 4 && m != 9){
			System.out.println("Wrong step detected!");
		}
	}

	private String playerAddr = "";
	private String[][] gameState = null;
	//private List<String[]> gameState = new ArrayList<String[]>();
	private List<String> playerList = new ArrayList<String>();
	private int xCord;
	private int yCord;
	private int score;
	public int N;
	public int K;
	public void start(String playerID, String playerIP, int playerPort, int N, int K) {
		gameState = new String[playerPort][playerPort];
		GameService stub = null;
		Registry registry = null;
		playerAddr = playerID + '@' + playerIP + ':' + playerPort;
		String bindName = "rmi://" + playerAddr + "/game";
       	System.setProperty("java.rmi.server.hostname",playerIP);
		try {
			GameServer obj = new GameServer();

			stub = (GameService) UnicastRemoteObject.exportObject(obj, 0);

            try{
                LocateRegistry.createRegistry(playerPort);
                //Runtime.getRuntime().exec("rmiregistry "+port);
            }
            catch(Exception ex){
            	//maybe rmi already started at playerPort
				System.err.println("Game Server can't start normally: " + ex.toString());
				ex.printStackTrace();
				return;
            }
			registry = LocateRegistry.getRegistry(playerPort);
			registry.bind(bindName, stub);
			xCord = -1;
			yCord = -1;
			score = 0;
			System.out.println("Game " + playerAddr + " started normally.");
		} catch (Exception e) {
			try {
				registry.unbind(bindName);
				registry.bind(bindName, stub);
				System.out.println("Game " + playerAddr + " restart normally.");
			} catch (Exception ee) {
				System.err.println("Game Server Exception: " + ee.toString());
				ee.printStackTrace();
			}
		}
	}

}
