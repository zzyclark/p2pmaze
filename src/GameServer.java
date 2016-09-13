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
	private Randomizer randomizer;
	private int xCord;
	private int yCord;
	private int score;
	public int N;
	public int K;
//	public String[] players = {};
	public String[][] GameState = null;
	private String ID;
	private String playerAddr = "";
	private List<String> playerList = new ArrayList<String>();
	//public  Player player = null;
	public boolean IsPrimaryServer;
	public boolean IsBackupServer;
	public testGUI gui;
	Hashtable<String, Integer> playerScores
			= new Hashtable<String, Integer>();

	public GameServer() {}
	public GameServer(int n, int k, String id, String addr) {
		Integer treasureNum = (n + k) / 2;
	    this.N =n;
        this.K=k;
        this.ID = id;
        this.GameState = new String[n][k];
		for(String[] row : this.GameState)
			Arrays.fill(row,"O");
		this.IsPrimaryServer = false;
        this.IsBackupServer = false;
        this.playerAddr = addr;
		this.randomizer = new Randomizer(n, k, treasureNum, this.GameState);
	}

	@Override
	public void updateGameState(String[][] gameState) throws RemoteException {

		for(int i = 0; i < gameState.length; i++)
		{
			for (int j = 0; j < gameState[i].length; j++)
			{
				this.GameState[i][j] = gameState[i][j];
			}
		}
	}

	@Override
	public String[] updateServerList(Boolean isMain, String[] oldList) throws RemoteException {
		if (isMain && oldList[0].equals(this.serverList[0])) {
			//Main server down, backup server change to main server
			return changeServer(true);
		} else if (isMain) {
			//Server list already changed, user may request new server list
			return this.serverList;
		} else {
			//Backup server down, random pick user to be backup server
			return changeServer(false);
		}
	}

	@Override
	public void setUserList(List<String> userList) throws RemoteException {
		this.playerList = userList;
	}

	@Override
	public List<String> getUserList() throws RemoteException {
		return this.playerList;
	}

	@Override
	public String initContact(String myAddr) throws RemoteException {
		//new user in, add user to list
		this.playerList.add(myAddr);
		return "Last Update timestamp";
	}

	@Override
	public Boolean updateGui() throws RemoteException {
//		this.gui.updateState(this.GameState);
		String title = ID;
		if(IsPrimaryServer)
			title += "(Main Server)";
		else if(IsBackupServer)
			title += "(Backup Server)";
		if(this.gui != null)
			this.gui.update(title);
		return true;
	}

	@Override
	public void startNewGame() throws RemoteException {
		this.GameState = randomizer.loadInitTreasures();
	}

	@Override
	public Integer[] newPlayerJoin(String userAddr) throws RemoteException {
		Integer[] newPos = randomizer.setRandomLocation(true, userAddr, this.GameState);
		String userID = userAddr.split("@")[0];
		this.GameState[newPos[0]][newPos[1]] = userID;
		this.playerScores.put(userID,0);
		updatePlayerScoreList();
		return newPos;
	}

	@Override
	public void updatePos(Integer[] pos) throws RemoteException {
		this.xCord = pos[0];
		this.yCord = pos[1];
	}

	@Override
	public Integer[] getPos() throws RemoteException {
		Integer[] myPos = {this.xCord, this.yCord};
		return myPos;
	}

	@Override
	public void addScore() throws RemoteException{
		this.score ++;
	}

	@Override
	public void removeInactiveUser(List<String> inactiveUserList) throws RemoteException {
		OuterLoop:
		for (int i = 0; i < this.GameState.length; ++i) {
			String[] row = this.GameState[i];
			for (int j = 0; j < row.length; ++j) {
				if (null != row[j]) {
					for (String inactiveUser : inactiveUserList) {
						if (inactiveUser.equals(row[j])) {
							this.GameState[i][j] = "O";
							continue OuterLoop;
						}
					}
				}
			}
		}
		for (String inactiveUser : inactiveUserList) {
			this.playerScores.remove(inactiveUser);
		}
	}


	@Override
	public void playerListChanged(List<String> newplayerList) throws RemoteException{
		System.out.println("Start remove user");
		System.out.println("New user list:");
		for (String newUser: newplayerList) {
			System.out.println(newUser);
		}
		System.out.println("Old user list:");
		for (String olduser: this.playerList) {
			System.out.println(olduser);
		}
		for(String player : this.playerList){
			if(!newplayerList.contains(player)){
				System.out.println("Remove user: " + player);
				//player left, need to update the playerscore and the game state
				String leftPlayerID = player.split("@")[0];
				playerScores.remove(leftPlayerID);
				updatePlayerScoreList();
				boolean found = false;
				for(int i = 0; i < this.GameState.length && !found; i++)
				{
					for (int j = 0; j < this.GameState[i].length; j++)
					{
						if(this.GameState[i][j] != null && this.GameState[i][j].equals(leftPlayerID)){
							this.GameState[i][j] = "O";
							found = true;
							break;
						}
					}
				}
			}
		}
		try{
			updateBackupServer();
		}
		catch (Exception ex){
			System.out.println(ex.getMessage());
		}

	}

	@Override
	public void updateBackupServer() throws Exception {
		String backupServer = serverList[1];
		Integer backupServerPort = Integer.parseInt(backupServer.substring(backupServer.indexOf(":") + 1));
		Registry registry = LocateRegistry.getRegistry(backupServerPort);
		GameService backupServerStub = (GameService) registry.lookup("rmi://" + backupServer + "/game");
		backupServerStub.updateGameState(this.GameState);
		backupServerStub.updatePlayerScores(playerScores);
	}

	@Override
	public Hashtable<String,Integer> getPlayerScores() throws RemoteException{
		return playerScores;
	}

	@Override
	public void updatePlayerScores(Hashtable<String, Integer> scores) throws RemoteException{
		playerScores = (Hashtable<String, Integer>)scores.clone();
		updatePlayerScoreList();
	}

	@Override
	public Boolean isActive() throws RemoteException {
		return true;
	}

	@Override
	public void printGameState(){
		System.out.println("Current Game State:");
		//print out game state
		String servername = this.ID;
		if(IsPrimaryServer)
			servername += "(Main Server)";
		else if(IsBackupServer)
			servername += "(Backup Server)";
		this.gui = new testGUI(servername, this.playerScores, this.GameState, this.N, this.K);
		this.gui.setSize(500,500);
	}

	@Override
	public Integer[] makeMove(int m, Integer[] myPos, String userAddr){
		//In myPos, first is y axis value, second is x axis value
		if(m != 0 && m != 1 && m != 2 && m != 3 && m != 4 && m != 9){
			System.out.println("Wrong step detected!");
		}
		Integer[] oldPos = {myPos[0], myPos[1]};

		Integer y = myPos[0];
		Integer x = myPos[1];

		if (m == 4) {
			if (y - 1 >= 0 && (null == this.GameState[y-1][x] || "O".equals(this.GameState[y-1][x]) || "x".equals(this.GameState[y-1][x]))){
				myPos[0] = myPos[0] - 1;
			}
			return updateUserPos(oldPos, myPos, userAddr);
		} else if (m == 3) {
			if (x + 1 < this.K && (null == this.GameState[y][x+1] || "O".equals(this.GameState[y][x+1]) || "x".equals(this.GameState[y][x+1]))) {
				myPos[1] = myPos[1] + 1;
			}
			return updateUserPos(oldPos, myPos, userAddr);
		} else if (m == 2) {
			if (y + 1 < this.N && (null == this.GameState[y+1][x] || "O".equals(this.GameState[y+1][x]) || "x".equals(this.GameState[y+1][x]))){
				myPos[0] = myPos[0] + 1;
			}
			return updateUserPos(oldPos, myPos, userAddr);
		} else if (m == 1) {
			if (x - 1 >= 0 && (null == this.GameState[y][x-1] || "O".equals(this.GameState[y][x-1]) || "x".equals(this.GameState[y][x-1]))) {
				myPos[1] = myPos[1] - 1;
			}
			return updateUserPos(oldPos, myPos, userAddr);
		} else {
			return myPos;
		}
	}
	@Override
	public List<String> contactServer(String userAddr) throws RemoteException {
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
		serverList = newServerList.clone();
	}

	@Override
	public void setServer(Boolean IsPrimary, Boolean IsBackup) throws RemoteException{
		System.out.println("set server");
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

	private String[] changeServer(Boolean changeMain) throws RemoteException {
		String[] newList = new String[2];
		if (changeMain) {
			newList[0] = this.serverList[1];
		} else {
			newList[0] = this.serverList[0];
		}

		//get an active user, and let it be backup server
		ListIterator<String> iterator = this.playerList.listIterator(this.playerList.size());
		System.out.println("Change back up server.");
		while (iterator.hasPrevious()) {
			String userAddr = iterator.previous();
			System.out.println("Back up server candidate: " + userAddr);
			try {
				Integer backupServerPort = Integer.parseInt(userAddr.substring(userAddr.indexOf(":") + 1));
				Registry registry = LocateRegistry.getRegistry(backupServerPort);
				GameService userStub = (GameService) registry.lookup("rmi://" + userAddr + "/game");

				if (userStub.isActive() && !userAddr.equals(this.playerAddr)) {
					//peak first active user to be backup server
					newList[1] = userAddr;
					this.serverList = newList;
					userStub.setServer(false,true);
					userStub.updateGui();
					System.out.println("Back up server change to: " + this.serverList[1]);
					return this.serverList;
				}
			} catch (ConnectException ce) {
				iterator.remove();
			} catch (NotBoundException ne) {
				iterator.remove();
			}
		}
		System.out.println("Back up server change to: " + this.serverList[1]);
		this.serverList = newList;
		return this.serverList;
	}

	private Integer[] updateUserPos(Integer[] oldPos, Integer[] newPos, String userAddr){
		if (oldPos[0] == newPos[0] && oldPos[1] == newPos[1]) {
			return oldPos;
		} else {
			String player = this.GameState[oldPos[0]][oldPos[1]];
			Boolean isTreasure = "x".equals(this.GameState[newPos[0]][newPos[1]]);
			this.GameState[oldPos[0]][oldPos[1]] = "O";
			this.GameState[newPos[0]][newPos[1]] = player;
			if (isTreasure) {
				Integer[] tPos = randomizer.setRandomLocation(false, "", this.GameState);
				this.GameState[tPos[0]][tPos[1]] = "x";
				try {
					Integer score = playerScores.get(player);
					score ++;
					playerScores.put(player,score);
					addPlayerScore(userAddr);
					updatePlayerScoreList();
				}
				catch (Exception ex) {
					System.out.println(ex.getMessage());
				}
			}
			return newPos;
		}
	}

	public void updatePlayerScoreList() {
		if (this.gui != null && this.gui.players != null) {
			this.gui.players.clear();
			for (String key : playerScores.keySet()) {
				this.gui.players.addElement(key + ": " + playerScores.get(key));
			}
		}
	}

	private void addPlayerScore(String addr) throws Exception{
		try {
			Integer serverPort = Integer.parseInt(addr.substring(addr.indexOf(":") + 1));
			Registry r = LocateRegistry.getRegistry(serverPort);
			GameService userStub = (GameService) r.lookup("rmi://" + addr + "/game");
			userStub.addScore();
		}
		catch (ConnectException ce){
			throw ce;
		}
		catch (RemoteException re){
			throw re;
		}
		catch (NotBoundException nb){
			throw nb;
		}
		catch (Exception ex){
			System.out.println(ex.getStackTrace());
			throw ex;
		}
	}
}
