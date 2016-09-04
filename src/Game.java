import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Scanner;

/**
 * Created by clark on 22/8/16.
 */
public class Game {
    private static String[] serverList = new String[2];
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
                    Registry registry = LocateRegistry.getRegistry("127.0.0.1",port);
                    GameService otherPlayerStub = (GameService) registry.lookup("rmi://" + otherPlayerAddr + "/game");
                    if (otherPlayerStub.isActive()) {
                        continue;
                    }
                } catch (ConnectException e) {
                    System.out.println("Cant connect to player "+otherPlayerAddr+" "+e.getMessage());
                    iterator.remove();
                }
                catch (NotBoundException e){
                    System.out.println("Player address not bount "+otherPlayerAddr);
                    iterator.remove();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        if (oldLen != userList.size()) {
            trackerStub.updatePlayerList(userList);
        }
    }

    private static void getServerList(List<String> userList, String userIp, Integer userPort, String userId) throws Exception {
        String otherUser = userList.get(0);
        Integer otherUserPort = Integer.parseInt(otherUser.substring(otherUser.indexOf(":") + 1));
        Registry otherUserRegistry = LocateRegistry.getRegistry(otherUserPort);
        Registry userRegistry = LocateRegistry.getRegistry(userPort);

        String[] test = otherUserRegistry.list();
        String myAddr = userId + '@' + userIp + ':' + userPort;
        GameService otherUserStub = (GameService) otherUserRegistry.lookup("rmi://" + otherUser + "/game");
        GameService userStub = (GameService) userRegistry.lookup("rmi://" + myAddr + "/game");

        String [] newServerList = otherUserStub.getServerList();

        if (newServerList[0] == null && newServerList[1] == null) {
            //condition 1, no server in list
            newServerList[0] = myAddr;
        } else if (serverList[1] == null) {
            //condition 2, 1 main server in list
            newServerList[1] = myAddr;
        }

        //update game console server list
        serverList = newServerList;
        //update server list to user own server
        userStub.updateServerList(serverList);
        System.out.println("Server list updated");
    }

    private static void waitUserServerStart (String addr) {
        try {
            int port = Integer.parseInt(addr.split(":")[1]);
            Registry registry = LocateRegistry.getRegistry(port);
            GameService otherPlayerStub = (GameService) registry.lookup("rmi://" + addr + "/game");
            if (otherPlayerStub.isActive()) {
                return;
            }
        } catch (ConnectException e) {
            waitUserServerStart(addr);
        } catch (NotBoundException e){
            waitUserServerStart(addr);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return;
    }

    private static void makeMovement(int movement, String userAddr) throws Exception {
        String server = serverList[0];
        Integer serverPort = Integer.parseInt(server.substring(server.indexOf(":") + 1));
        Registry registry = LocateRegistry.getRegistry(serverPort);
        GameService serverStub = (GameService) registry.lookup("rmi://" + server + "/game");

        List<String> newState = serverStub.contactServer(userAddr);
        serverStub.makeMove(movement);

        System.out.println("You have get new contact history list after your movement\n");
        for (String history : newState) {
            System.out.println(history);
        }
    }

    public static void main(String[] args) {
        final String ip = args[0];
        final String port = args[1];
        final String playerID = args[2];
        final int playerPort = ThreadLocalRandom.current().nextInt(10000, 20001);//can't use the same port for all players;
        final String playerIP = "127.0.0.1";
        try {

            Registry registry = LocateRegistry.getRegistry(ip,Integer.parseInt(port));
            TrackerService trackerStub = (TrackerService) registry.lookup("Tracker");
            int N = trackerStub.getN();
            int K = trackerStub.getK();
            final GameServer player = new GameServer(N,K);

            System.setProperty("java.rmi.server.hostname",playerIP);

            // Start my own game
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    player.start(playerID, playerIP, playerPort, N, K);
                }
            });

            t.start();

            String myAddr = playerID + '@' + playerIP + ':' + playerPort;

            //do all the rest until my server start
            waitUserServerStart(myAddr);

            List<String> playerList = trackerStub.addPlayer(playerID, playerIP, playerPort);

            updatePlayerList(playerList, myAddr, trackerStub);

            System.out.println(myAddr + " joined the game");
            getServerList(playerList, playerIP, playerPort, playerID);
            player.printGameState();
            while(true){
                Scanner reader = new Scanner(System.in);
                int step = reader.nextInt();
                try {
                    makeMovement(step, myAddr);
                }
                catch (Exception ex){System.out.println(ex.toString());}
            }

        } catch (Exception e) {
            System.err.println("Game client/serve exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
