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
			process = new Process(0);
			// create on port 1099
			process.register("localhost");
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
