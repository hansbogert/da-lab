package da;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class Process extends UnicastRemoteObject implements IHandleRMI{
	ArrayList<Message> buffer;

	public Process() throws RemoteException{
		
	}
	
	void register(String ip) {
		try{
			// create a new service named myMessage
			LocateRegistry.getRegistry(ip, 1099);
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("system is ready");
	}
	
	
	
	public void receive(Message m) throws RemoteException
	{
		System.out.println("Hello world!");
	}
}
