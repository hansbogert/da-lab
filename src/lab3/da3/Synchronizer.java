package da3;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;

import da3.message.Ack;
import da3.message.PayloadMessage;
import da3.message.Safe;

public class Synchronizer extends UnicastRemoteObject implements IHandleRMI {
	
	private ArrayList<PayloadMessage> bufferedIncomingMessages;
	
	private ArrayList<PayloadMessage> unackedMessages;
	private ArrayList<Safe> receivedSafes;
	
	private Process process;
	private Registry registry;
	private int roundId;
	private int currentMessageId;
	
	public Synchronizer(Process process) throws RemoteException  {
		this.process = process;
		bufferedIncomingMessages = new ArrayList<PayloadMessage>();
		unackedMessages = new ArrayList<PayloadMessage>();
		roundId = 0;
	}
	
	/*
	 * Generate a unique process id and Register in the RMI Registry.
	 */
	void register(String ip) {
		// Find the RMI Registry.
		try {
			registry = LocateRegistry.getRegistry(ip, 1099);
			// Generate a unique processId.
			process.setProcessId(getMaxProcessID() + 1);
			// Register into the RMI Registry.
			registry.rebind(Integer.toString((getMaxProcessID() + 1)), this);
			System.out.println("Process " + process.getProcessId() + " started!!!");
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * Return the maximum process ID of the processes binded the registry.
	 */
	public int getMaxProcessID() {
		int maxProcessID = 0;
		String[] remoteProcessIds;
		try {
			remoteProcessIds = registry.list();
			// for all remote processes binded to the registry,
			for (int i = 0; i < remoteProcessIds.length; i++) {
				// overwrite maxProcessID if any processId is greater.
				int processID = Integer.parseInt(remoteProcessIds[i]);
				maxProcessID = (processID > maxProcessID) ? processID
						: maxProcessID;
			}
			return maxProcessID;
		} catch (RemoteException e) {
			e.printStackTrace();
			return maxProcessID;
		}
	}

	/*
	 * Get the names of all remote processes
	 */
	public ArrayList<Integer> getRemoteProcessIds() {
		try {
			String[] processNameStrings = registry.list();
			ArrayList<Integer> remoteProcessIds = new ArrayList<Integer>();
			for(String str : processNameStrings)
			{
				remoteProcessIds.add(Integer.parseInt(str));
			}
			return remoteProcessIds;
			
		} catch (RemoteException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void send(PayloadMessage pMessage, int remoteProcessId) {
		try {
			currentMessageId++;
			pMessage.setMessageId(currentMessageId);
			IHandleRMI remoteSynchronizer = (IHandleRMI) registry.lookup(Integer.toString(remoteProcessId));
			remoteSynchronizer.transfer(pMessage);
		} catch (RemoteException | NotBoundException e) {
			e.printStackTrace();
		}
	}
	
	public void receive(PayloadMessage pMessage) {
		if(pMessage.getRoundId()==roundId)
		{
			process.receive(pMessage);
		}
		else
		{
			bufferedIncomingMessages.add(pMessage);
		}
		Ack ack = new Ack(roundId, process.getProcessId(), pMessage.getMessageId());
		int remoteProcessId = pMessage.getProcessId();
		send(ack, remoteProcessId);
	}
	
	public void receive(Ack ack) {
		
		for(int i = 0; i<unackedMessages.size(); i++)
		{
			if(unackedMessages.get(i).getMessageId()==ack.getMessageId())
			{
				unackedMessages.remove(i);
			}
		}
		
		if(isSafe())
		{
			broadcastSafe();
			if(canProgress())
			{
				progressToNexRound();
			}
		}
	}
	
	public void send(Ack ack, int remoteProcessId)
	{
		try {
			IHandleRMI remoteSynchronizer = (IHandleRMI) registry.lookup(Integer.toString(remoteProcessId));
			remoteSynchronizer.transfer(ack);
		} catch (RemoteException | NotBoundException e) {
			e.printStackTrace();
		}
	}
	
	public void receive(Safe safe) {
		receivedSafes.add(safe);
		if(isSafe() && canProgress())
		{
			progressToNexRound();
		}
	}
	
	public void broadcastSafe() {
		try {
			ArrayList<Integer> neighbourIds= getRemoteProcessIds();
			for(Integer i : neighbourIds)
			{
				IHandleRMI remoteSynchronizer = (IHandleRMI) registry.lookup(Integer.toString(i));
				Safe safe = new Safe(roundId, process.getProcessId());
				remoteSynchronizer.transfer(safe);
			}

		} catch (RemoteException | NotBoundException e) {
			e.printStackTrace();
		}
	}
	
	public boolean isSafe()
	{
		boolean isSafe = true;
		
		if(process.allMessagesSent() != true)
		{
			isSafe = false;
		}
		
		for(PayloadMessage uMessage : unackedMessages)
		{
			if(uMessage.getRoundId() >= roundId)
			{
				isSafe = false;
			}
		}
		return isSafe;
	}
	
	public boolean canProgress() {
		boolean canProgress = true;
		
		if(!isSafe())
		{
			canProgress = false;
		}
		
		ArrayList<Integer> neighbourIds= getRemoteProcessIds();
		if(neighbourIds.size() != receivedSafes.size())
		{
			canProgress = false;
		}
		
		return canProgress;
	}
	
	public void progressToNexRound() {
		roundId++;
		receivedSafes.clear();
		
		for(PayloadMessage pMessage : bufferedIncomingMessages)
		{
			if(pMessage.getRoundId() == roundId)
			{
				process.receive(pMessage);
			}
		}
		bufferedIncomingMessages.clear();
	}
	
	public Registry getRegistry() {
		return registry;
	}
	
	public void setRegistry(Registry registry) {
		this.registry = registry;
	}
	
	/*
	 * When other remote processes call the transfer function, this process will
	 * receive the parameter as the message.
	 */
	public synchronized void transfer(PayloadMessage pMessage) throws RemoteException {
		receive(pMessage);
	}
	
	/*
	 * When other remote processes call the transfer function, this process will
	 * receive the parameter as the message.
	 */
	public synchronized void transfer(Ack ack) throws RemoteException {
		receive(ack);
	}
	
	/*
	 * When other remote processes call the transfer function, this process will
	 * receive the parameter as the message.
	 */
	public synchronized void transfer(Safe safe) throws RemoteException {
		receive(safe);
	}
	
	public int getRoundId() {
		return roundId;
	}
	
	public void setRoundId(int roundId) {
		this.roundId = roundId;
	}
	
	
}
