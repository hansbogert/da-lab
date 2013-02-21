package da;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class Process extends UnicastRemoteObject implements IHandleRMI{
	ArrayList<Message> buffer;

	Registry registry;
	Integer pid;
	
	public Process(Integer pid) throws RemoteException{
		this.pid = pid;
	}
	
	void register(String ip) {
		try{
            registry = LocateRegistry.getRegistry(ip, 1099);
			
            registry.rebind(pid.toString(), this);
    
		} catch (Exception e) {
			e.printStackTrace();
		}
		System.out.println("system is ready, Process" + pid);
	}
	
	
	
	public void receive(Message m) throws RemoteException
	{
		System.out.println(m.payload);
	}
	
	public void send(Message m, Integer pid)
	{
		IHandleRMI otherprocess;
		try {
			otherprocess = (IHandleRMI) registry.lookup(pid.toString());
	        otherprocess.receive(m);
		} catch (AccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
	}
}
