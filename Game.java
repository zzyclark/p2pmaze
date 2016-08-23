import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Created by clark on 22/8/16.
 */
public class Game {
    public Game() {}

    private static void updatePlayerList(List<String> userList, String myAddr, TrackerService trackerStub) throws RemoteException {
        Iterator<String> iterator = userList.iterator();
        int oldLen = userList.size();

        while (iterator.hasNext()) {
            String otherPlayerAddr = iterator.next();
            if (otherPlayerAddr.equals(myAddr)) {
                continue;
            } else {
                try {       
                    int port = Integer.parseInt(otherPlayerAddr.split(":")[1]);
                    Registry registry = LocateRegistry.getRegistry(port);
                    GameService otherPlayerStub = (GameService) registry.lookup("rmi://" + otherPlayerAddr + "/game");
                    if (otherPlayerStub.isActive()) {
                        continue;
                    }
                } catch (ConnectException e) {
                    iterator.remove();
                } catch (NotBoundException e){
                    iterator.remove();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (oldLen != userList.size()) {
            trackerStub.updatePlayerList(userList);
        }
    }

    public static void main(String[] args) {
        final String ip = args[0];
        final String port = args[1];
        final String playerID = args[2];
        final int playerPort = ThreadLocalRandom.current().nextInt(10000, 20001);//can't use the same port for all players;
        final String playerIP = "127.0.0.1";

        try {
            // Start my own game
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    GameServer gameServer = new GameServer();
                    gameServer.start(playerID, playerIP, playerPort);
                }
            });

            t.start();

            String myAddr = playerID + '@' + playerIP + ':' + playerPort;
            Registry registry = LocateRegistry.getRegistry(ip,Integer.parseInt(port));
            TrackerService trackerStub = (TrackerService) registry.lookup("Tracker");

            List<String> playerList = trackerStub.addPlayer(playerID, playerIP, playerPort);

            updatePlayerList(playerList, myAddr, trackerStub);
            System.out.println(myAddr + " joined the game");
        } catch (Exception e) {
            System.err.println("Game client/serve exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
