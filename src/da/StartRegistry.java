package da;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class StartRegistry {

	public static void main(String[] args) {
		Process process;
		System.out.println("starting registry");
		try {

			Registry registry = LocateRegistry.createRegistry(1099);
			
			process = new Process();
			process.register("127.0.0.1");
			process.boardcastRepeatly(30);
			process.talktoRandomProcessRepeatly(10);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
