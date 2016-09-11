import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.*;

/**
 * Created by clark on 22/8/16.
 */
public interface GameService extends Remote {
	Boolean isActive() throws RemoteException;
	void printGameState() throws RemoteException;
	String[][] makeMove(int m, int oldX, int oldY, int newX, int newY, GameService Player) throws Exception;
	List<String> contactServer(String userAddr) throws RemoteException;
	String[] getServerList() throws RemoteException;
	String[][] getGameState() throws RemoteException;
	void updateServerList(String[] newServerList) throws RemoteException;
    void setServer(Boolean IsPrimary, Boolean IsBackup) throws RemoteException;
	void updateBackupServer() throws Exception;
	void updateGameState(String[][] gameState) throws RemoteException;
	Hashtable<String,Integer> getPlayerScores() throws RemoteException;
	void updatePlayerScores(Hashtable<String, Integer> scores) throws RemoteException;
	String[] updateServerList(Boolean isMain, String[] oldList) throws RemoteException;
	void setUserList(List<String> userList) throws RemoteException;
	String initContact(String myAddr) throws RemoteException;
	Boolean updateGui() throws RemoteException;
	void startNewGame() throws RemoteException;
	Integer[] newPlayerJoin(String userAddr, String PlayerID) throws RemoteException;
	void addScore() throws RemoteException;
	void playerListChanged(List<String> playerList) throws RemoteException;
}

