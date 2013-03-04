package da;

import java.rmi.Remote;
import java.rmi.RemoteException;

import message.Message;

public interface IHandleRMI extends Remote{
	public void transfer(Message m) throws RemoteException;
}
