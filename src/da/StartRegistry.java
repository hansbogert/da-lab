package da;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class StartRegistry {

	public static void main(String[] args) {
		Process process;
		System.out.println("starting registry");
		try {
			// create on port 1099
			Registry registry = LocateRegistry.createRegistry(1099);
			process = new Process();
			// create on port 1099
			process.register("127.0.0.1", "0");
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
