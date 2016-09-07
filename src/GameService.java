import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.*;

/**
 * Created by clark on 22/8/16.
 */
public interface GameService extends Remote {
	Boolean isActive() throws RemoteException;
	void printGameState() throws RemoteException;
	String[][] makeMove(int m) throws RemoteException;
	List<String> contactServer(String userAddr) throws RemoteException;
	String[] getServerList() throws RemoteException;
	String[][] getGameState() throws RemoteException;
	void updateServerList(String[] newServerList) throws RemoteException;
    void setServer(Boolean IsPrimary, Boolean IsBackup) throws RemoteException;
	void updateBackupServer() throws Exception;
	void updateGameState(String[][] gameState) throws RemoteException;
	String[] updateServerList(Boolean isMain, String[] oldList) throws RemoteException;
	void setUserList(List<String> userList) throws RemoteException;
	String initContact(String myAddr) throws RemoteException;
	Boolean updateGui(String[][] gameState) throws RemoteException;
	void startNewGame() throws RemoteException;
}

