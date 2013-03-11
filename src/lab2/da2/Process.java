package da2;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.hamcrest.core.IsInstanceOf;

import da2.message.Message;
import da2.message.MessagePackage;
import da2.message.Request;
import da2.message.TextMessage;
import da2.message.Token;
import da2.message.VectorClock;


public class Process extends UnicastRemoteObject implements IHandleRMI {

	private static final long serialVersionUID = -397296118682038104L;

	/*
	 * Critical section of this process
	 */
	public Object CS;
	
	int processId;

	Registry registry;
	
	/*
	 * An array RN with for every process the number of the last request they know about
	 * Be updated when they receive a request.
	 */
	int[] RN;
	
	/*
	 * If the token is present, this process can access its CS (or even all other CS's in the system).
	 * If not, then token == null. 
	 */
	Token token;

	/*
	 * When some process is working in the CS, this should be true.
	 * When it leaves the CS, this should be false;
	 */
	boolean isCSOccupied = false;
	
	//boolean isTokenPresent = false;;
	
	public boolean isTokenPresent()
	{
		return (token != null);
	}
	/*
	 * Process is a single component in the distributed system.
	 */
	public Process() throws RemoteException {
	}
	
	/*
	 * Process is a single component in the distributed system.
	 */
	public Process(int maxProcessCount) throws RemoteException {
		RN = new int[maxProcessCount];
		CS = new Object();
	}

	/*
	 * Generate a unique process id and Register in the RMI Registry.
	 */
	void register(String ip) {
		// Find the RMI Registry.
		try {
			registry = LocateRegistry.getRegistry(ip, 1099);
			// Generate a unique processId.
			processId = getMaxProcessID() + 1;
			// Register into the RMI Registry.
			registry.rebind(Integer.toString((getMaxProcessID() + 1)), this);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/*
	 * Return the maximum process ID of the processes binded the registry.
	 */
	public int getMaxProcessID() {
		int maxProcessID = 0;
		String[] remoteProcesses;
		try {
			remoteProcesses = registry.list();
			// for all remote processes binded to the registry,
			for (int i = 0; i < remoteProcesses.length; i++) {
				// overwrite maxProcessID if any processId is greater.
				int processID = Integer.parseInt(remoteProcesses[i]);
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
	public String[] getRemoteProcesses() {
		try {
			return registry.list();
		} catch (RemoteException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public int getProcessId() {

		return processId;
	}
	
	/*
	 * When other remote processes call the transfer function, this process will
	 * receive the parameter as the message.
	 */
	public void transfer(MessagePackage m) throws RemoteException {
		receive(m);		
	}

	/*
	 * Receive a message
	 */
	public void receive(MessagePackage m) {
			deliver(m);
	}
	
	/*
	 * Deliver a message.
	 */
	public void deliver(MessagePackage m) {
		respondToDelivery((TextMessage) m.getMessage());
	}
	
	/*
	 * Send a message.
	 */
	public void send(MessagePackage m, int remoteProcessId) {
		try {
			
			IHandleRMI remoteProcess = (IHandleRMI) registry.lookup(Integer
					.toString(remoteProcessId));
			remoteProcess.transfer(m);
			
		} catch (RemoteException e) {
			e.printStackTrace();

		} catch (NotBoundException e) {
			e.printStackTrace();
		}

	}

	/*
	 * Response to the delivery of a message.
	 */
	public void respondToDelivery(Message m) {
		
		if(m instanceof TextMessage)
		{
			respondToTextMessage((TextMessage) m);
		}
		else if( m instanceof Token) {
			respondToToken((Token) m);
		}
		else if(m instanceof Request) {
			respondToRequest((Request) m);
		}
	}
	
	public void respondToTextMessage(TextMessage textMessage)
	{
		System.out.println("Process " + processId + " receives [" + textMessage.getTextContent() + "]");
	}
	
	public void respondToToken(Token token)
	{
		this.token = token;
		occupyCS();
		doTrivialTasksInCS();
		unoccupyCS();
		//
		token.setRequestNoAt(getRequestNoAt(processId), processId);
		token.updateQueue(processId, RN);
		if(token.getQueue().size() !=0)
		{
			int remoteProcessId = token.getQueue().peek();
			MessagePackage m = new MessagePackage();
			m.setMessage(token);
			send(m, remoteProcessId);
			token = null;
		}
		
		
		
	}
	
	public void respondToRequest(Request request)
	{
		updateRequestAt(request.getProcessId(), request.getRequestNo());
		
		
		if(isTokenPresent() && !isCSOccupied && isTokenBehind(request.getProcessId()))
		{
				MessagePackage m = new MessagePackage();
				m.setMessage(token);
				send(m, request.getProcessId());
				token = null;
		}
	}
	
	public void occupyCS()
	{
		isCSOccupied = true;
	}
	
	public void unoccupyCS()
	{
		isCSOccupied = false;
	}
	
	public void doTrivialTasksInCS()
	{
		CS = new Object();
	}
	
	public void incrementRequestAt(int processId)
	{
		int requestId = RN[processId - 1];
		RN[processId - 1] = requestId + 1;
	}
	
	public void updateRequestAt(int processId, int requestNo)
	{
		RN[processId - 1] = requestNo;
	}
	
	public int getRequestNoAt(int processId)
	{
		return RN[processId - 1];
	}
	
	public boolean isTokenBehind(int remoteProcessId)
	{
		return getRequestNoAt(remoteProcessId)>token.getRequestNoAt(remoteProcessId);
	}
	
	public void broadcastRequest()
	{
		incrementRequestAt(processId);
		String[] remoteProcesses = getRemoteProcesses();
		// for all remote processes binded to the registry,
		for (int i = 0; i < remoteProcesses.length; i++) {
				Request request = new Request(processId, getRequestNoAt(processId));
				MessagePackage m = new MessagePackage();
				m.setMessage(request);
				send(m, i);
		}
	}

	/*
	 * Send a broadcast to all processes binded to the registry. For the fun,
	 * and (or) for testing.
	 */
	public void broadcast() {
		String[] remoteProcesses = getRemoteProcesses();
		// for all remote processes binded to the registry,
		for (int i = 0; i < remoteProcesses.length; i++) {
			// if it does not have the same name as this process,
			if (Integer.parseInt(remoteProcesses[i]) != (processId)) {
				System.out.println("Process " + processId
						+ " sent a broadcast to Process " + remoteProcesses[i]);

				String payload = "This is a broadcast from Process " + processId;
				TextMessage t = new TextMessage(payload);
				MessagePackage m = new MessagePackage();
				m.setMessage(t);
				int remoteProcessId = Integer.parseInt(remoteProcesses[i]);
				send(m, remoteProcessId);
			}
		}
	}

	/*
	 * Call broadcast() again and again. For the fun, and (or) for testing.
	 */
	public void broadcastRepeatedly(int timespan) {
		final ScheduledExecutorService service = Executors
				.newSingleThreadScheduledExecutor();
		service.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				broadcast();
			}
		}, 0, timespan, TimeUnit.SECONDS);
	}


}
