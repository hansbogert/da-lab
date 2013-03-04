package da2;

import java.rmi.Remote;
import java.rmi.RemoteException;

import da2.message.Message;


public interface IHandleRMI extends Remote{
	public void transfer(Message m) throws RemoteException;
}
