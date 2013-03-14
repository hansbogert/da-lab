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
	 * When some process is working in the CS, this should be true.
	 * When it leaves the CS, this should be false;
	 */
	boolean isCSOccupied = false;
	
	/*
	 * If the token is present, this process can access its CS (or even all other CS's in the system).
	 * If not, then token == null. 
	 */
	Token token;
	
	int csDelayTime = 0;

	public Token getToken() {
		return token;
	}
	public void setToken(Token token) {
		this.token = token;
	}

	//boolean isTokenPresent = false;;
	
	public boolean isTokenPresent()
	{
		return (token != null);
	}
	/*
	 * Process is a single component in the distributed system.
	 */
//	public Process() throws RemoteException {
//	}
	
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
		respondToDelivery(m.getMessage());
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
			//respondToToken((Token) m);
			respondToToken((Token) m, csDelayTime); //TODO, review, hack for artifical delay.
		}
		else if(m instanceof Request) {
			respondToRequest((Request) m);
		}
	}
	
	public void respondToTextMessage(TextMessage textMessage)
	{
		System.out.println("Process " + processId + " receives [" + textMessage.getTextContent() + "]");
	}
	
	public void respondToToken(Token token, int csDelayTime)
	{
		setToken(token);	//TODO review, dirty duplication code
		occupyCS();		//TODO review, dirty duplication code
		final Token incomingToken = token;
		final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
		service.schedule(new Runnable() {
			@Override
			public void run() {
				respondToToken(incomingToken);
			}
		}, csDelayTime,TimeUnit.MILLISECONDS);
		csDelayTime = 0;
	}
	
	public void respondToToken(Token token)
	{
			
		//token_present <- true
		//Place the token at local memory.
		setToken(token);
		
		//Work on Critical Section.
		occupyCS();
		doTrivialTasksInCS();
		unoccupyCS();
		
		//TN[i] <- N[i]
		//When receiving the token, this process should have a higher request no than the one in the token.
		//Set update the request number of this process on the token, with the latest request number of this process
		//It means that the request from this process is satisfied.
		token.setRequestNoAt(getRequestNoAt(processId), processId);
		
		//Update the queue on the token, remove this process from the beginning of the queue. Add new request at the end of the queue.
		token.updateQueue(processId, RN);
		//Send the token to next process.
		if(token.getQueue().size() !=0)
		{
			int remoteProcessId = token.getQueue().peek();
			MessagePackage m = new MessagePackage();
			m.setMessage(token);
			send(m, remoteProcessId);
			removeToken();
			
		}
		
		
		
	}
	
	public  void removeToken() {
		setToken(null);
	}
	
	public void respondToRequest(Request request)
	{
		//Update the request number of the process who sent the request.
		updateRequestAt(request.getProcessId(), request.getRequestNo());
		
		//If this process has the token,
		//This process is not working in the critical section using the token.
		//If the request is not already granted. <==> the request number on the token is behind.
		if(isTokenPresent() && !isCSOccupied && isTokenBehind(request.getProcessId()))
		{
				MessagePackage m = new MessagePackage();
				m.setMessage(token);
				send(m, request.getProcessId());
				removeToken();
				
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
	
	public void setCSDelayTime(int delayTime)
	{
		csDelayTime = delayTime;
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
		//Increment request number of this process.
		incrementRequestAt(processId);
		String[] remoteProcesses = getRemoteProcesses();
		// for all remote processes binded to the registry, broadcast the request
		for (int i = 0; i < remoteProcesses.length; i++) {
				Request request = new Request(processId, getRequestNoAt(processId));
				MessagePackage m = new MessagePackage();
				m.setMessage(request);
				send(m, Integer.parseInt(remoteProcesses[i]));
		}
	}
	
	public void broadcastRequest(int rqDelayTime)
	{
		final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
		service.schedule(new Runnable() {
			@Override
			public void run() {
				broadcastRequest();
			}
		}, rqDelayTime,TimeUnit.MILLISECONDS);
	}
	
	public String printRN()
	{
		String str = "[";
		
		if(RN.length>0)
		{
			str += RN[0];
		}
		
		for(int i = 1; i<RN.length;i++)
		{
			str += "," + RN[i];
		}
		str += "]";
		return str;
	}


}
