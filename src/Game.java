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
        //Assume main server never die
        String mainServer = userList.get(0);
        String myAddr = userId + '@' + userIp + ':' + userPort;
        GameService mainServerStub = getGameService(mainServer);
        GameService userStub = getGameService(myAddr);

        String [] newServerList = mainServerStub.getServerList();

        if (newServerList[0] == null && newServerList[1] == null) {
            //condition 1, no server in list
            newServerList[0] = myAddr;
            userStub.setServer(true,false);
            System.out.println("set primary server");
        } else if (newServerList[1] == null) {
            //condition 2, 1 main server in list
            newServerList[1] = myAddr;
            userStub.setServer(false,true);
            //inform main server about the new back up server
            mainServerStub.updateServerList(newServerList);
            System.out.println("set backup server");
        }

        //update game console server list
        serverList = newServerList;
        //update server list to user own server
        userStub.updateServerList(serverList);
        System.out.println("Server list updated");
    }

    private static void waitUserServerStart (String addr) {
        try {
//            int port = Integer.parseInt(addr.split(":")[1]);
//            Registry registry = LocateRegistry.getRegistry(port);
//            GameService otherPlayerStub = (GameService) registry.lookup("rmi://" + addr + "/game");
            GameService otherPlayerStub = getGameService(addr);
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
        GameService serverStub = getGameService(server);

        List<String> newState = serverStub.contactServer(userAddr);
        serverStub.makeMove(movement);

        System.out.println("You have get new contact history list after your movement\n");
        for (String history : newState) {
            System.out.println(history);
        }
    }

    private static GameService getGameService(String addr) throws Exception{
        try {
            Integer serverPort = Integer.parseInt(addr.substring(addr.indexOf(":") + 1));
            Registry r = LocateRegistry.getRegistry(serverPort);
            return (GameService) r.lookup("rmi://" + addr + "/game");
        }
        catch (ConnectException ce){
            throw ce;
        }
        catch (RemoteException re){
            throw re;
        }
        catch (NotBoundException nb){
            throw nb;
        }
        catch (Exception ex){
            System.out.println(ex.getStackTrace());
            throw ex;
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
            final int N = trackerStub.getN();
            final int K = trackerStub.getK();
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
//            GameService myService = getGameService(myAddr);

            Registry r = LocateRegistry.getRegistry(playerPort);
            GameService myService = (GameService) r.lookup("rmi://" + myAddr + "/game");
            myService.printGameState();
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
