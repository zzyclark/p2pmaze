import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;

/**
 * Created by clark on 22/8/16.
 */
public class Game {
    public Game() {}

    public static void main(String[] args) {
        String ip = args[0];
        String port = args[1];

        //TODO: later add host of tracker
        String host = null;

        try {
            Registry registry = LocateRegistry.getRegistry(host);
            TrackerService stub = (TrackerService) registry.lookup("Tracker");

            List<String> playerList = stub.updatePlayerList(ip, port);
            for (String playerAddr : playerList) {
                System.out.println("Player address: " + playerAddr);
            }
            System.out.println("You have joined the game.");
        } catch (Exception e) {
            System.err.println("Game client/serve exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
