package da;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IHandleRMI extends Remote {
	public void receive(Message m) throws RemoteException;
}
