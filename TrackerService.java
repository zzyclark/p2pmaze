import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.*;

/**
 * Created by clark on 22/8/16.
 */

public interface TrackerService extends Remote{
    List<String> updatePlayerList(String ip, String port) throws RemoteException;
}

