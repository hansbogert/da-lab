package da2;

import java.rmi.Remote;
import java.rmi.RemoteException;

import da2.message.MessagePackage;


public interface IHandleRMI extends Remote{
	public void transfer(MessagePackage m) throws RemoteException;
}
