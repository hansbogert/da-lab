package da;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

public class Process extends UnicastRemoteObject implements IHandleRMI {
	ArrayList<Message> buffer;

	Registry registry;
	Integer pid;

	public Process(Integer pid) throws RemoteException {
		this.pid = pid;
	}

	void register(String ip) throws RemoteException {
		registry = LocateRegistry.getRegistry(ip, 1099);
		registry.rebind(pid.toString(), this);

		System.out.println("system is ready, Process" + pid);
	}

	public void receive(Message m) throws RemoteException {
		System.out.println(pid + ": " + m.payload);
	}

	public void sendTo(Message m, Integer pid) throws AccessException,
			RemoteException, NotBoundException {
		IHandleRMI otherprocess;
		otherprocess = (IHandleRMI) registry.lookup(pid.toString());
		otherprocess.receive(m);
	}
	
	public void send(Message m, Integer pid) throws AccessException, RemoteException, NotBoundException
	{
		IHandleRMI otherprocess;
			otherprocess = (IHandleRMI) registry.lookup(pid.toString());
	        otherprocess.receive(m);
	}
}
