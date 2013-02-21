package da;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class StartProcess {
	
	Registry myRegistry;

	public static void main(String[] args) {
		Process process;
		System.out.println("starting processes");
		try {
			LocateRegistry.getRegistry("127.0.0.1", 1099);

			process = new Process();
			process.register("127.0.0.1");
			process.boardcastRepeatly(30);
			process.talktoRandomProcessRepeatly(10);

			
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
}
