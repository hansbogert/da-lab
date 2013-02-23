package da;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import message.Message;
import message.Timestamp;

public class Process extends UnicastRemoteObject implements IHandleRMI{

	private static final long serialVersionUID = -397296118682038104L;

	ArrayList<Message> messageBuffer;

	int processId;
	
	Registry registry;
	
	Vector<Timestamp> buffer;
	Timestamp timestamp;
	
	/*
	 * Process is a single component in the distributed system.
	 */
	public Process() throws RemoteException{
		//Create a vector clock, assuming no of process is under 100
		timestamp = new Timestamp();
		for(int i = 0; i<101; i++)
		{
			timestamp.timestamp.add(0);
		}
		buffer = new Vector<Timestamp>();
	}
	
	/*
	 * Generate a unique process id and Register in the RMI Registry.
	 */
	void register(String ip) {
		try{
			//Find the RMI Registry.
            registry = LocateRegistry.getRegistry(ip, 1099);
            //Generate a unique processId.
            processId = getMaxProcessID() + 1;
            //Register into the RMI Registry.
            registry.rebind((getMaxProcessID() + 1)+"", this);
            
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/*
	 * Return the maximum process ID of the processes binded the registry.
	 */
	public int getMaxProcessID() 
	{
		int maxProcessID = 0;
		String[] remoteProcesses;
		try {
			remoteProcesses = registry.list();
			//for all remote processes binded to the registry,
	        for(int i = 0; i<remoteProcesses.length; i++)
	        {
	        	//overwrite maxProcessID if any processId is greater.
	        	int processID = Integer.parseInt(remoteProcesses[i]);
	        	maxProcessID = (processID > maxProcessID) ? processID : maxProcessID;
	        }
	        return maxProcessID;
		} catch (RemoteException e) {
			e.printStackTrace();
			return maxProcessID;
		}
	}
	
	public String[] getRemoteProcesses() {
		try {
			return registry.list();
		} catch (RemoteException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/*
	 * When other remote processes call the transfer function, this process will receive the parameter as the message.
	 * Then it prints out the payload, and (only for fun) reply when receiving a random conversation.
	 */
	public void transfer(Message m) throws RemoteException
	{
		receive(m);
	}
	
	public void receive(Message m) {
		deliver(m.getPayload());
		
		/*TODO Algorithm
		if(deliveryEnabled(m))
		{
			deliver(m.getPayload());
			
			boolean nothingleft = false;
			while(!nothingleft)
			{
				nothingleft = true;
				for(Message mBuffered : messageBuffer)
				{
					if(deliveryEnabled(mBuffered))
					{
						deliver(mBuffered.getPayload());
						nothingleft = false;
					}
				}
			}
		}
		else
		{
			messageBuffer.add(m);
		} */
	
	}
	
	/*
	 * Check if it is ok to deliver
	 */
	public boolean deliveryEnabled(Message m)
	{
		//TODO Algorithm
		return true;
	}
	
	public void send(String payload, int remoteProcessId) {
		try {
			
			Message m= new Message();
			m.setPayload(payload);
			IHandleRMI remoteProcess = (IHandleRMI) registry.lookup(remoteProcessId+"");
			remoteProcess.transfer(m);
			
			/* TODO Algorithm
			//Select the remote process.
			IHandleRMI remoteProcess = (IHandleRMI) registry.lookup(remoteProcessId+"");
			
	        Message m= new Message();
	        
	        timestamp.processId = processId;
	        timestamp.incrementAt(processId);
	        
	        m.setPayload(payload);
	        m.buffer = buffer;
	        m.timestamp = timestamp;
	        remoteProcess.transfer(m);
	        
	        Timestamp timestampR = new Timestamp();
	        timestampR.processId = remoteProcessId;
	        //TODO delete timespan
	        
	        buffer.add(timestampR);
	        */
		} catch (RemoteException | NotBoundException e) {
			e.printStackTrace();
		}

	}
	
	public void deliver(String payload) {
		System.out.println("Process " + processId + " receives [" + payload + "]");
		if(payload.contains("How are you today?"))
		{
			replytoRandomProcess(payload);
		}
		/* TODO Algorithm
		for(Timestamp timestamp : buffer)
		{
			   if (there exists (j,V’’) in S) then 

               remove (j,V’’) from S               
                                                    
               V’’:=max(V’,V’’)              

               insert(j,V’’) into S          

               else insert(j,V’) into S 
               
			
		}
		*/
		

	}

	/*
	 * Send a broadcast to all processes binded to the registry.
	 * For the fun, and (or) for testing.
	 */
	public void broadcast()
	{
		String[] remoteProcesses = getRemoteProcesses();
		//for all remote processes binded to the registry,
        for(int i = 0; i<remoteProcesses.length; i++)
        {
        	//if it does not have the same name as this process,
        	if(Integer.parseInt(remoteProcesses[i]) != (processId))
        	{
        		System.out.println("Process " + processId + " sent a broadcast to Process " + remoteProcesses[i]);
               
        		String payload = "This is a broadcast from Process " + processId;
        		int remoteProcessId = Integer.parseInt(remoteProcesses[i]);
        		send(payload, remoteProcessId);
        	}
        }
	}
	
	/*
	 * Talks to a random process that is not itself.
	 * A reply from that process is expected.
	 * For the fun, and (or) for testing.
	 */
	public void talktoRandomProcess() {
		
		String[] remoteProcesses = getRemoteProcesses();
		//for all remote processes binded to the registry,
		if(remoteProcesses.length > 0)
		{
			Random randomGenerator = new Random();
			int randomInt = randomGenerator.nextInt(remoteProcesses.length);
			//send a message to a random process binded to the registry.
        	if(Integer.parseInt(remoteProcesses[randomInt]) != (processId))
        	{
        		System.out.println("Process " + processId + " talks randomly to Process " + remoteProcesses[randomInt]);
               	
        		String payload = "Hi, Process " + processId + " speaking. How are you today?";
        		int remoteProcessId = Integer.parseInt(remoteProcesses[randomInt]);
        		send(payload, remoteProcessId);
        	}
		}
	}
	
	/*
	 * After receiving a random message from another process,
	 * this process should send a reply.
	 * For the fun, and (or) for testing.
	 */
	public void replytoRandomProcess(String incomingPayload)
	{
		//find the process id of the sender process.
		String remoteProcessName = incomingPayload.replace("Hi, Process ", "");
		remoteProcessName = remoteProcessName.replace(" speaking. How are you today?", "");
		
		System.out.println("Process " + processId + " replys to Process " + remoteProcessName);
		
		String payload = "Hi, I am fine. By Process " + processId;
		int remoteProcessId = Integer.parseInt(remoteProcessName);
		send(payload, remoteProcessId);
	}
	
	/*
	 * Call broadcast() again and again.
	 * For the fun, and (or) for testing.
	 */
	public void boardcastRepeatly(int timespan)
	{
	    final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
	    service.scheduleWithFixedDelay(new Runnable()
	      {
	        @Override
	        public void run()
	        {
	        	broadcast();
	        }
	      }, 0, timespan, TimeUnit.SECONDS);
	}
	
	/*
	 * Call talktoRandomProces() again and again.
	 * For the fun, and (or) for testing.
	 */
	public void talktoRandomProcessRepeatly(int timespan)
	{
	    final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
	    service.scheduleWithFixedDelay(new Runnable()
	      {
	        @Override
	        public void run()
	        {
	        	talktoRandomProcess();
	        }
	      }, 0, timespan, TimeUnit.SECONDS);
	}
	

	

	
}
