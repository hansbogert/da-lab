package da3;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import da3.message.Ack;
import da3.message.PayloadMessage;
import da3.message.Safe;

public class Synchronizer extends UnicastRemoteObject implements IHandleRMI {
	
	private static final long serialVersionUID = 9209021558794481415L;

	private ArrayList<PayloadMessage> bufferedIncomingMessages;
	private ArrayList<Safe> bufferedSafesNextRound;
	
	private ArrayList<PayloadMessage> unackedMessages;
	private ArrayList<Safe> receivedSafes;
	
	private Process process;
	private Registry registry;
	private int roundId;
	private int currentMessageId;
	
	public Synchronizer(Process process) throws RemoteException  {
		this.process = process;
		bufferedIncomingMessages = new ArrayList<PayloadMessage>();
		bufferedSafesNextRound = new ArrayList<Safe>();
		unackedMessages = new ArrayList<PayloadMessage>();
		receivedSafes = new ArrayList<Safe>();
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
				if(Integer.parseInt(str) != process.getProcessId())
				{
					remoteProcessIds.add(Integer.parseInt(str));
				}
			}
			return remoteProcessIds;
			
		} catch (RemoteException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public void send(PayloadMessage pMessage) {
		try {
			currentMessageId++;
			pMessage.setMessageId(currentMessageId);
			IHandleRMI remoteSynchronizer = (IHandleRMI) registry.lookup(Integer.toString(pMessage.getReceiveProcessId()));
			remoteSynchronizer.transfer(pMessage);
		} catch (RemoteException | NotBoundException e) {
			e.printStackTrace();
		}
	}
	
	public void receive(PayloadMessage pMessage) {
		
		System.out.println("Process " + process.getProcessId() + " receives :" + pMessage.toString());
		
		if(pMessage.getRoundId()==roundId)
		{
			process.receive(pMessage);
		}
		else
		{
			bufferedIncomingMessages.add(pMessage);
		}
		Ack ack = new Ack(roundId, process.getProcessId(), pMessage.getSentProcessId(), pMessage.getMessageId());
		int remoteProcessId = pMessage.getSentProcessId();
		send(ack);
	}
	
	public void receive(Ack ack) {
		
		System.out.println("Process " + process.getProcessId() + " receives :" + ack.toString());
		
		for(int i = 0; i<unackedMessages.size(); i++)
		{
			if(unackedMessages.get(i).getMessageId()==ack.getMessageId())
			{
				unackedMessages.remove(i);
			}
		}
		
		final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
		service.schedule(new Runnable() {
			@Override
			public void run() {
				regulateSafety();
			}
		}, 0,TimeUnit.MILLISECONDS);

	}
	
	public void send(Ack ack)
	{
		try {
			IHandleRMI remoteSynchronizer = (IHandleRMI) registry.lookup(Integer.toString(ack.getReceiveProcessId()));
			remoteSynchronizer.transfer(ack);
		} catch (RemoteException | NotBoundException e) {
			e.printStackTrace();
		}
	}
	
	public void receive(Safe safe) {
		
		System.out.println("Process " + process.getProcessId() + " receives :" + safe.toString());
		
		if(safe.getRoundId() > getRoundId())
		{
			bufferedSafesNextRound.add(safe);
		}
		else
		{
			receivedSafes.add(safe);
		}

		final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
		service.schedule(new Runnable() {
			@Override
			public void run() {
				regulateProgress();
			}
		}, 0,TimeUnit.MILLISECONDS);
		
	}
	
	public synchronized void regulateSafety()
	{
		if(isSafe())
		{
			System.out.println("Process " + process.getProcessId() + " is safe");
			broadcastSafe();
		}
	}
	
	public void broadcastSafe() {
		try {
			ArrayList<Integer> neighbourIds= getRemoteProcessIds();
			for(Integer i : neighbourIds)
			{
				if(process.getProcessId() == 3)
				{
					//System.out.println();//Debug here
				}
				IHandleRMI remoteSynchronizer = (IHandleRMI) registry.lookup(Integer.toString(i));
				Safe safe = new Safe(roundId, process.getProcessId(), i);
				System.out.println("Process " + process.getProcessId() + " tries to send :" + safe.toString());
				remoteSynchronizer.transfer(safe);
				System.out.println("Process " + process.getProcessId() + " sent :" + safe.toString());
			}

		} catch (RemoteException | NotBoundException e) {
			e.printStackTrace();
		}
	}
	
	public boolean isSafe()
	{
		boolean isSafe = true;
		
		if(process.isAllMessagesSent() != true)
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
	
	public synchronized void regulateProgress()
	{
		if(canProgress())
		{
			System.out.println("Process " + process.getProcessId() + " progress to round " + (getRoundId() + 1));
			progressToNexRound();
		}
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
		
		//TODO Just to stop the process after certian rounds
		if(roundId >= 20)
		{
			canProgress = false;
		}
		
		return canProgress;
	}
	
	public void progressToNexRound() {

		roundId++;
		process.progressToNextRound();
		receivedSafes = bufferedSafesNextRound;
		bufferedIncomingMessages.clear();
		
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
		
		final PayloadMessage incomingpMessage = pMessage;
		
		final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
		service.schedule(new Runnable() {
			@Override
			public void run() {
				receive(incomingpMessage);;
			}
		}, 0,TimeUnit.MILLISECONDS);
	}
	
	/*
	 * When other remote processes call the transfer function, this process will
	 * receive the parameter as the message.
	 */
	public synchronized void transfer(Ack ack) throws RemoteException {
		
		final Ack incomingAck = ack;
		
		final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
		service.schedule(new Runnable() {
			@Override
			public void run() {
				receive(incomingAck);;
			}
		}, 0,TimeUnit.MILLISECONDS);
		
	}
	
	/*
	 * When other remote processes call the transfer function, this process will
	 * receive the parameter as the message.
	 */
	public synchronized void transfer(Safe safe) throws RemoteException {
		
		final Safe incomingSafe = safe;
		
		final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
		service.schedule(new Runnable() {
			@Override
			public void run() {
				receive(incomingSafe);;
			}
		}, 0,TimeUnit.MILLISECONDS);
		
	}
	
	public int getRoundId() {
		return roundId;
	}
	
	public void setRoundId(int roundId) {
		this.roundId = roundId;
	}
	
	
}
