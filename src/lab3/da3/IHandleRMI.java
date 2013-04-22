package da3;

import java.rmi.Remote;
import java.rmi.RemoteException;

import da3.message.Ack;
import da3.message.PayloadMessage;
import da3.message.Safe;

public interface IHandleRMI extends Remote{

	public void transfer(PayloadMessage pMessage) throws RemoteException;

	public void transfer(Ack ack) throws RemoteException;
	
	public void transfer(Safe safe) throws RemoteException;
	
	
}
