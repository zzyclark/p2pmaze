import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Iterator;
import java.util.List;

/**
 * Created by clark on 22/8/16.
 */
public class Game {
    public Game() {}

    private static void updatePlayerList(List<String> userList, String myAddr, TrackerService trackerStub) throws RemoteException {
        Registry registry = LocateRegistry.getRegistry();
        Iterator<String> iterator = userList.iterator();
        int oldLen = userList.size();

        while (iterator.hasNext()) {
            String otherPlayerAddr = iterator.next();
            if (otherPlayerAddr.equals(myAddr)) {
                continue;
            } else {
                try {
                    GameService otherPlayerStub = (GameService) registry.lookup("rmi://" + otherPlayerAddr + "/game");
                    if (otherPlayerStub.isActive()) {
                        continue;
                    }
                } catch (ConnectException e) {
                    iterator.remove();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (oldLen != userList.size()) {
            trackerStub.updatePlayerList(userList);
        }

        for (String playerAddr : userList) {
            System.out.println("Player address: " + playerAddr);
        }
    }

    public static void main(String[] args) {
        final String ip = args[0];
        final String port = args[1];

        //TODO: later add host of tracker
        String host = null;

        try {
            // Start my own game
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    GameServer gameServer = new GameServer();
                    gameServer.start(ip, port);
                }
            });

            t.start();


            String myAddr = ip + ":" + port;
            Registry registry = LocateRegistry.getRegistry(host);
            TrackerService trackerStub = (TrackerService) registry.lookup("Tracker");

            List<String> playerList = trackerStub.addPlayer(ip, port);

            updatePlayerList(playerList, myAddr, trackerStub);
            System.out.println("You have joined the game.");
        } catch (Exception e) {
            System.err.println("Game client/serve exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
