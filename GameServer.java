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

	public void start(String ip, String port) {
		GameService stub = null;
		Registry registry = null;
		playerAddr = ip + ":" +port;
		String bindName = "rmi://" + playerAddr + "/game";

		try {
			GameServer obj = new GameServer();
			stub = (GameService) UnicastRemoteObject.exportObject(obj, 0);
			registry = LocateRegistry.getRegistry();
			registry.bind(bindName, stub);

			System.out.println("Game " + playerAddr + " started normally.");
		} catch (Exception e) {
			try {
				registry.unbind(bindName);
				registry.bind(bindName, stub);
				System.out.println("Game " + playerAddr + " started normally.");
			} catch (Exception ee) {
				System.err.println("Game Server Exception: " + ee.toString());
				ee.printStackTrace();
			}
		}
	}
}
