import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.zip.Deflater;

/**
 * Created by clark on 22/8/16.
 */

public class GameServer implements GameService {
	//private String playerAddr = "";
	//private List<int[]> gameState = new ArrayList<int[]>();
	//private List<String> playerList = new ArrayList<String>();
	private String[] serverList = new String[2];
	private List<String> userContactHistory = new ArrayList<String>();
	private int xCord;
	private int yCord;
	private int score;
	public int N;
	public int K;
	public String[] players = {};
	public String[][] GameState = null;
	private String ID;
	private String playerAddr = "";
	//private String[][] gameState = null;
	private List<String> playerList = new ArrayList<String>();
	//public  Player player = null;
	public boolean IsPrimaryServer;
	public boolean IsBackupServer;

	public GameServer() {}
	public GameServer(int n, int k, String id, String addr) {
	    this.N =n;
        this.K=k;
        this.ID = id;
        this.GameState = new String[N][K];
        this.IsPrimaryServer = false;
        this.IsBackupServer = false;
        this.playerAddr = addr;

	}

	@Override
	public Boolean isActive() throws RemoteException {
		return true;
	}

	@Override
	public void printGameState(){
		//gameState.add(new int[]{1,2,3});
		//gameState.add(new int[]{1,2,3});
		GameState[0][1] = "*";
		GameState[1][2] = "x";
		GameState[2][3]="ab";
		System.out.println("Current Game State:");
		//print out game state
		String servername = this.ID;
		if(IsPrimaryServer)
			servername += "(Main Server)";
		else if(IsBackupServer)
			servername += "(Backup Server)";
		testGUI gui = new testGUI(servername, players, GameState, N, K);
		gui.setSize(500,500);
	}

	@Override
	public void makeMove(int m){
		if(m != 0 && m != 1 && m != 2 && m != 3 && m != 4 && m != 9){
			System.out.println("Wrong step detected!");
		}
	}
	@Override
	public List<String> contactServer(String userAddr) throws RemoteException {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date currentTime = new Date();
		String history = userAddr + " has contacted server at: " + currentTime;
		userContactHistory.add(history);
		return userContactHistory;
	}

	@Override
	public String[] getServerList() throws RemoteException {
		return serverList;
	}

	@Override
	public void updateServerList(String[] newServerList) throws RemoteException {
		serverList = newServerList;
	}

	@Override
	public void setServer(Boolean IsPrimary, Boolean IsBackup) throws RemoteException{
		System.out.println("ser server");
		IsPrimaryServer = true;
		IsBackupServer = true;
	}

	public void start(String playerID, String playerIP, int playerPort, int N, int K) {
		GameService stub = null;
		Registry registry = null;
        String addr = playerID + '@' + playerIP + ':' + playerPort;
		String bindName = "rmi://" + addr + "/game";
		System.setProperty("java.rmi.server.hostname",playerIP);
		try {
			GameServer obj = new GameServer(N,K,playerID, addr);
			stub = (GameService) UnicastRemoteObject.exportObject(obj, 0);

            try{
                LocateRegistry.createRegistry(playerPort);
                Runtime.getRuntime().exec("rmiregistry "+playerPort);
            }
            catch(Exception ex){
            	//maybe rmi already started at playerPort
				System.err.println("Game Server can't start normally: " + ex.toString());
				ex.printStackTrace();
				return;
            }
			registry = LocateRegistry.getRegistry(playerPort);
			registry.bind(bindName, stub);
			System.out.println("Game " + addr + " started normally.");
		} catch (Exception e) {
			try {
				registry.unbind(bindName);
				registry.bind(bindName, stub);
				System.out.println("Game " + addr + " restart normally.");
			} catch (Exception ee) {
				System.err.println("Game Server Exception: " + ee.toString());
				ee.printStackTrace();
			}
		}
	}

}
