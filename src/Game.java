import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.NotBoundException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.Scanner;

/**
 * Created by clark on 22/8/16.
 */
public class Game {
    private static String[] serverList = new String[2];
    public Game() {}

    private static boolean updatePlayerList(List<String> userList, String myAddr, TrackerService trackerStub) throws RemoteException {
        Iterator<String> iterator = userList.iterator();
        Boolean playerLeft = false;
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
                    if (null != otherPlayerStub.initContact(myAddr)) {
                        continue;
                    }
                } catch (ConnectException e) {
                    System.out.println("Cant connect to player "+otherPlayerAddr+" "+e.getMessage());
                    playerLeft = true;
                    iterator.remove();
                }
                catch (NotBoundException e){
                    System.out.println("Player address not bount "+otherPlayerAddr);
                    playerLeft = true;
                    iterator.remove();
                }
                catch (Exception e) {
                    playerLeft = true;
                    e.printStackTrace();
                }
            }
        }

        if (oldLen != userList.size()) {
            trackerStub.updatePlayerList(userList);
        }
        return playerLeft;
    }

    private static List<String> getInactiveUserlist(String[][] gameState) {
        List<String> allUserList = new ArrayList<String>();
        for (int i = 0; i < gameState.length; ++i) {
            String[] row = gameState[i];
            for (int j = 0; j < row.length; ++j) {
                if (null != row[j] && !row[j].equals("x")) {
                    allUserList.add(row[j]);
                }
            }
        }

        List<String> inactiveUserList = new ArrayList<String>();
        for (String userAddr : allUserList) {
            try {
                GameService otherUser = getGameService(userAddr);
            } catch (Exception e) {
                inactiveUserList.add(userAddr);
            }
        }

        if (0 != inactiveUserList.size()) {
            return inactiveUserList;
        } else {
            return null;
        }
    }

    private static GameService getServerList(List<String> userList, String userIp, Integer userPort, String userId) throws Exception {
        String[] newServerList;
        String myAddr = userId + '@' + userIp + ':' + userPort;
        GameService userStub = getGameService(myAddr);

        // Less than 2 player, no complete main backup server structure
        if (userList.size() <= 2) {
            //First player is the server
            //Second is the back up server
            String mainServer = userList.get(0);
            GameService mainServerStub = getGameService(mainServer);

            newServerList = mainServerStub.getServerList();

            if (newServerList[0] == null && newServerList[1] == null) {
                //condition 1, no server in list
                //Create new game state
                newServerList[0] = myAddr;
                userStub.setServer(true,false);
                //If new server exist, init new game state at main server
                userStub.startNewGame();
                System.out.println("set primary server");
            } else if (newServerList[1] == null) {
                //condition 2, 1 main server in list
                newServerList[1] = myAddr;
                userStub.setServer(false,true);
                //inform main server about the new back up server
                mainServerStub.updateServerList(newServerList);
                System.out.println("set backup server");
            }
        } else {
            String otherUser = userList.get(0);
            GameService otherUserStub = getGameService(otherUser);
            newServerList = otherUserStub.getServerList();
        }

        //update game console server list
        serverList = newServerList;
        //update server list to user own server
        userStub.updateServerList(serverList);

        //validate server list
        checkServer(myAddr);
        System.out.println("Server lists info: " + serverList[0] + " " + serverList[1]);
        GameService mainServerStub = getGameService(serverList[0]);
        return mainServerStub;
    }

    private static void waitUserServerStart (String addr) {
        try {
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
        System.out.println(userAddr + " make move: " + movement);
        //Check server status and update
        checkServer(userAddr);

        System.out.println("Current Servers are: " + serverList[0] + " " + serverList[1]);

        String server = serverList[0];
        GameService serverStub = getGameService(server);
        GameService userStub = getGameService(userAddr);

        Integer[] myPos = userStub.getPos();

        //Call server to remove those inactive user
//        String[][] beforeMovementState = serverStub.getGameState();
//        List<String> inactiveList = getInactiveUserlist(beforeMovementState);
//        if (null != inactiveList) {
//            serverStub.removeInactiveUser(inactiveList);
//        }

        //Make movement
        myPos = serverStub.makeMove(movement, myPos, userAddr);
        userStub.updatePos(myPos);
        String[][] newGameState = serverStub.getGameState();
        userStub.updateGameState(newGameState);
        userStub.updatePlayerScores(serverStub.getPlayerScores());
        userStub.updateGui();
        //Each time make move, update back up server
        if (null != serverList[1]) {
            userStub.updateBackupServer();
        }

        System.out.println("You have get new contact history list after your movement\n");
        for (String[] row: newGameState) {
            for(String item: row) {
                if (null == item) {
                    System.out.print("   ");
                } else {
                    System.out.print(item);
                }
            }
            System.out.println();
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

    /**
     * Each time before update game state, try to test if main/backup server is active
     * @throws Exception
     */
    private static void checkServer(String myAddr) throws Exception{
        //only check if server list has 2 value
        if(null != serverList[1]) {
            String mainServer = serverList[0];
            String backupServer =serverList[1];
            try {
                GameService mainStub = getGameService(mainServer);
            } catch (Exception e) {
                GameService backupStub = getGameService(backupServer);
                GameService myStub = getGameService(myAddr);
                serverList = backupStub.updateServerList(true, serverList);
                System.out.println("After update: " + serverList[0] + serverList[1]);
                myStub.updateServerList(serverList);
            }

            try {
                GameService backupStub = getGameService(backupServer);
            } catch (Exception e) {
                GameService mainStub = getGameService(mainServer);
                GameService myStub = getGameService(myAddr);
                serverList = mainStub.updateServerList(false, serverList);
                myStub.updateServerList(serverList);
            }
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
            final GameServer player = new GameServer();

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

            //get the player list from tracker
            List<String> playerList = trackerStub.addPlayer(playerID, playerIP, playerPort);

            //update the player list (remove dead and add myself)
            boolean playerLeft = updatePlayerList(playerList, myAddr, trackerStub);

            System.out.println(myAddr + " joined the game");

            //get this game client server
            GameService serverService = getServerList(playerList, playerIP, playerPort, playerID);
            GameService myService = getGameService(myAddr);

            //update server about the player list if changed
            if(playerLeft){
                serverService.playerListChanged(playerList);
            }

            System.out.println("after my server start");

            //update player list for user
            myService.setUserList(playerList);

            System.out.println("after set user list");

            //join game
            Integer[] myPos = serverService.newPlayerJoin(myAddr);
            myService.updatePos(myPos);

            //get updated game state
            myService.updateGameState(serverService.getGameState());
            myService.updatePlayerScores(serverService.getPlayerScores());

            System.out.println("after update gamestate");

            //join the game
            myService.printGameState();

            //initial refresh, in order to show the right info in window
            //TODO: need to fix this later
            makeMovement(0, myAddr);
            while(true){
                Scanner reader = new Scanner(System.in);
                int step = reader.nextInt();
                try {
                    makeMovement(step, myAddr);
                }
                catch (Exception ex){
                    System.out.println(ex.toString());
                }
            }

        } catch (Exception e) {
            System.err.println("Game client/serve exception: " + e.toString());
            e.printStackTrace();
        }
    }
}
