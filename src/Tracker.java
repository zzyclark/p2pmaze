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

    private List<String> playerList = new ArrayList<String>();
    
    private static int N;
    private static int K;

    @Override
    public List<String> addPlayer(String playerID, String playerIP, int playerPort) throws RemoteException {
        String newPlayerAddr = playerID + '@' + playerIP + ':' + playerPort;
        if(!playerList.contains(newPlayerAddr)){
            playerList.add(newPlayerAddr);
            System.out.println("Updated Game List as below:");
            for(String player : playerList){
                System.out.println(player);
            }
        }
        return playerList;
    }

    public Boolean updatePlayerList(List<String> updatedList) throws RemoteException {
        playerList = updatedList;
        System.out.println("Updated Game List as below:");
        for(String player : playerList){
            System.out.println(player);
        }
        return true;
    }

    public int getN(){
        return N;
    }

    public  int getK(){
        return K;
    }

    public static void main(String args[]) {
        int port;
        try{
            port = Integer.parseInt(args[0]);
            N = Integer.parseInt(args[1]);
            K = Integer.parseInt(args[2]);
        }catch(Exception ex){
            System.out.println("Please indicate the port number, N and k");
            return;

        }
        TrackerService stub = null;
        Registry registry = null;
        System.setProperty("java.rmi.server.hostname","127.0.0.1");


        try {
            Tracker obj = new Tracker();
            stub = (TrackerService) UnicastRemoteObject.exportObject(obj, 0);

            try{
                LocateRegistry.createRegistry(port);
                //Runtime.getRuntime().exec("rmiregistry "+port);
            }
            catch(Exception ex){
                System.out.println("Error encountered during start RMI: "+ex.toString());
            }
            registry = LocateRegistry.getRegistry(port);
            registry.bind("Tracker", stub);

            System.out.println("Tracker started normally.");
        } catch (Exception e) {
            try {
                registry.unbind("Tracker");
                registry.bind("Tracker", stub);
                System.out.println("Tracker restart normally.");
            } catch (Exception ee) {
                System.err.println("Server Exception: " + ee.toString());
                ee.printStackTrace();
            }
        }
    }
}
