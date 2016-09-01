import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.*;

/**
 * Created by clark on 22/8/16.
 */
public interface GameService extends Remote {
	Boolean isActive() throws RemoteException;
	void printGameState() throws RemoteException;
	void makeMove(int m) throws RemoteException;
	List<String> contactServer(String userAddr) throws RemoteException;
	String[] getServerList() throws RemoteException;
	void updateServerList(String[] newServerList) throws RemoteException;
}

