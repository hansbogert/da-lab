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
	
	public void initRounds() throws RemoteException;
	
	public void setUpDecisionTree(Integer topCommanderId, Integer f) throws RemoteException;
	
	public int getMajority() throws RemoteException;
	
	public void initByzantineAlgorithm(Integer f, Integer order)  throws RemoteException;
}
