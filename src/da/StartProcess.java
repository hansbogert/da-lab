package da;

<<<<<<< HEAD
=======

>>>>>>> a8aa765cb00ac1e8334745baea452018d0e3b419
import java.rmi.NotBoundException;
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
			m.payload = "Hello, goodbye! - from 1";
			process1.sendTo(m, 2);

		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
