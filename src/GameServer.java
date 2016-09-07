import java.rmi.ConnectException;
import java.rmi.NotBoundException;
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
	private List<String> playerList = new ArrayList<String>();
	//public  Player player = null;
	public boolean IsPrimaryServer;
	public boolean IsBackupServer;
	public testGUI gui;

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
	public void updateGameState(String[][] gameState) throws RemoteException {
		//for (int i = 0; i < gameState.length; i++)
		//	this.GameState[i] = Arrays.copyOf(gameState[i], gameState[i].length);
		this.GameState = gameState;
	}

	@Override
	public String[] updateServerList(Boolean isMain, String[] oldList) throws RemoteException {
		if (isMain && oldList[0].equals(this.serverList[0])) {
			//Main server down, backup server change to main server
			this.serverList[0] = this.serverList[1];
			changeServer();
		} else if (isMain) {
			//Server list already changed, user may request new server list
			return this.serverList;
		} else {
			//Backup server down, random pick user to be backup server
			changeServer();
			return this.serverList;
		}
		return this.serverList;
	}

	@Override
	public void setUserList(List<String> userList) throws RemoteException {
		this.playerList = userList;
	}

	@Override
	public String initContact(String myAddr) throws RemoteException {
		//new user in, add user to list
		this.playerList.add(myAddr);
		return "Last Update timestamp";
	}

	@Override
	public Boolean updateGui(String[][] gameState) throws RemoteException {
		this.gui.updateState(this.GameState);
		return true;
	}

	@Override
	public void updateBackupServer() throws Exception {
		String backupServer = serverList[1];
		Integer backupServerPort = Integer.parseInt(backupServer.substring(backupServer.indexOf(":") + 1));
		Registry registry = LocateRegistry.getRegistry(backupServerPort);
		GameService backupServerStub = (GameService) registry.lookup("rmi://" + backupServer + "/game");
		backupServerStub.updateGameState(GameState);
	}

	@Override
	public Boolean isActive() throws RemoteException {
		return true;
	}

	@Override
	public void printGameState(){
		this.GameState[0][1] = "*";
		this.GameState[1][2] = "x";
		this.GameState[2][3]="ab";
		System.out.println("Current Game State:");
		//print out game state
		String servername = this.ID;
		if(IsPrimaryServer)
			servername += "(Main Server)";
		else if(IsBackupServer)
			servername += "(Backup Server)";
		this.gui = new testGUI(servername, this.players, this.GameState, this.N, this.K);
		this.gui.setSize(500,500);
	}

	@Override
	public String[][] makeMove(int m){
		if(m != 0 && m != 1 && m != 2 && m != 3 && m != 4 && m != 9){
			System.out.println("Wrong step detected!");
		}
		this.GameState[2][3]="aaa";
		this.GameState[3][4]="ab";

		return this.GameState;
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
	public String[][] getGameState() throws RemoteException{
		return GameState;
	}

	@Override
	public void updateServerList(String[] newServerList) throws RemoteException {
		serverList = newServerList;
	}

	@Override
	public void setServer(Boolean IsPrimary, Boolean IsBackup) throws RemoteException{
		System.out.println("ser server");
		IsPrimaryServer = IsPrimary;
		IsBackupServer = IsBackup;
	}

	public void start(String playerID, String playerIP, int playerPort, int N, int K) {
		GameService stub = null;
		Registry registry = null;
        String addr = playerID + '@' + playerIP + ':' + playerPort;
		this.playerAddr = addr;
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

	private void changeServer() throws RemoteException {
		//get an active user, and let it be backup server
		Iterator<String> iterator = this.playerList.iterator();
		while (iterator.hasNext()) {
			String userAddr = iterator.next();
			try {
				Integer backupServerPort = Integer.parseInt(userAddr.substring(userAddr.indexOf(":") + 1));
				Registry registry = LocateRegistry.getRegistry(backupServerPort);
				GameService userStub = (GameService) registry.lookup("rmi://" + userAddr + "/game");

				if (userStub.isActive() && !userAddr.equals(this.playerAddr)) {
					//peak first active user to be backup server
					this.serverList[1] = userAddr;
					break;
				}
			} catch (ConnectException ce) {
				iterator.remove();
			} catch (NotBoundException ne) {
				iterator.remove();
			}
		}
	}
}
