import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

/**
 * Created by clark on 22/8/16.
 */

public class GameServer implements GameService {
	public GameServer() {}

	@Override
	public Boolean isActive() throws RemoteException {
		return true;
	}

	private String playerAddr = "";

	public void start(String playerID, String playerIP, int playerPort) {

		GameService stub = null;
		Registry registry = null;
		playerAddr = playerID + '@' + playerIP + ':' + playerPort;
		String bindName = "rmi://" + playerAddr + "/game";
       	System.setProperty("java.rmi.server.hostname",playerIP);
		try {
			GameServer obj = new GameServer();

			stub = (GameService) UnicastRemoteObject.exportObject(obj, 0);

            try{
                LocateRegistry.createRegistry(playerPort);
                //Runtime.getRuntime().exec("rmiregistry "+port);
            }
            catch(Exception ex){
            	//maybe rmi already started at playerPort
				System.err.println("Game Server can't start normally: " + ex.toString());
				ex.printStackTrace();
				return;
            }
			registry = LocateRegistry.getRegistry(playerPort);
			registry.bind(bindName, stub);

			System.out.println("Game " + playerAddr + " started normally.");
		} catch (Exception e) {
			try {
				registry.unbind(bindName);
				registry.bind(bindName, stub);
				System.out.println("Game " + playerAddr + " restart normally.");
			} catch (Exception ee) {
				System.err.println("Game Server Exception: " + ee.toString());
				ee.printStackTrace();
			}
		}
	}
}
