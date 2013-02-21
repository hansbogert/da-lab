package da;


import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class StartProcess {

	public static void main(String[] args) {
		Process process1;
		Process process2;
		System.out.println("starting processes");
		try {
			process1 = new Process();
			process2 = new Process();
			// create on port 1099
			process1.register("127.0.0.1", "1");
			process2.register("127.0.0.1", "2");
			
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
