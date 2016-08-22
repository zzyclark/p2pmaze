import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.*;

/**
 * Created by clark on 22/8/16.
 */
public interface GameService extends Remote {
	Boolean isActive() throws RemoteException;
}

