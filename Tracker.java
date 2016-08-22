import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

/**
 * Created by clark on 22/8/16.
 */
public class Tracker implements TrackerService {

    public Tracker() {}

    private List<String> playerList = new ArrayList<>();

    public List<String> updatePlayerList(String ip, String port) throws RemoteException {
        String newPlayerAddr = ip + ':' + port;
        playerList.add(newPlayerAddr);
        return playerList;
    }

    public static void main(String args[]) {
        TrackerService stub = null;
        Registry registry = null;

        try {
            Tracker obj = new Tracker();
            stub = (TrackerService) UnicastRemoteObject.exportObject(obj, 0);
            registry = LocateRegistry.getRegistry();
            registry.bind("Tracker", stub);

            System.out.println("Tracker started normally.");
        } catch (Exception e) {
            try {
                registry.unbind("Tracker");
                registry.bind("Tracker", stub);
                System.out.println("Tracker started normally.");
            } catch (Exception ee) {
                System.err.println("Server Exception: " + ee.toString());
                ee.printStackTrace();
            }
        }
    }
}
