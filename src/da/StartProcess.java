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
			process1 = new Process(1);
			process2 = new Process(2);
			// create on port 1099
			process1.register("localhost");
			process2.register("localhost");
			Message m = new Message();
			m.payload = "Hello, goodbye!";
			process1.send(m, 2);
			
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
