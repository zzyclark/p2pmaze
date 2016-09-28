import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.*;

/**
 * Created by clark on 22/8/16.
 */
public interface GameService extends Remote {
	Boolean isActive() throws RemoteException;
	void printGameState() throws RemoteException;
	Integer[] makeMove(int m, Integer[] myPos, String userAddr) throws RemoteException;
	List<String> contactServer(String userAddr) throws RemoteException;
	String[][] getGameState() throws RemoteException;
    void setServer(Boolean IsPrimary, Boolean IsBackup) throws RemoteException;
	void updateBackupServer() throws Exception;
	void updateGameState(String[][] gameState) throws RemoteException;
	void setUserList(List<String> userList) throws RemoteException;
	List<String> getUserList() throws RemoteException;
	String initContact(String myAddr) throws RemoteException;
	Boolean updateGui() throws RemoteException;
	void startNewGame() throws RemoteException;
	Integer[] newPlayerJoin(String userAddr) throws RemoteException;
	void updatePos(Integer[] pos) throws RemoteException;
	Integer[] getPos() throws RemoteException;
	void removeInactiveUser(List<String> inactiveUserList) throws RemoteException;
	Hashtable<String,Integer> getPlayerScores() throws RemoteException;
	void updatePlayerScores(Hashtable<String, Integer> scores) throws RemoteException;
	void addScore() throws RemoteException;
	void playerListChanged(List<String> newplayerList) throws RemoteException;
}

