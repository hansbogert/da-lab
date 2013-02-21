package da;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import message.Message;

public class Process extends UnicastRemoteObject implements IHandleRMI{

	/**
	 * 
	 */
	private static final long serialVersionUID = -397296118682038104L;

	ArrayList<Message> buffer;

	String processId;
	
	Registry registry;
	
	/*
	 * Process is a single component in the distributed system.
	 */
	public Process() throws RemoteException{
		
	}
	
	/*
	 * Generate a unique process id and Register in the RMI Registry.
	 */
	void register(String ip) {
		try{
			//Find the RMI Registry.
            registry = LocateRegistry.getRegistry(ip, 1099);
            //Generate a unique processId.
            processId = (getMaxProcessID() + 1)+"";
            //Register into the RMI Registry.
            registry.rebind((getMaxProcessID() + 1)+"", this);
            
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	/*
	 * When other remote processes call the receive function, this process will receive the parameter as the message.
	 * Then it prints out the payload, and (only for fun) reply when receiving a random conversation.
	 */
	public void receive(Message m) throws RemoteException
	{
		System.out.println("Process " + processId + " receives [" + m.getPayload() + "]");
		
		if(m.getPayload().contains("How are you today?"))
		{
			replytoRandomProcess(m);
		}
	}
	
	/*
	 * Talks to a 
	 */
	public void talktoRandomProcess() {
		
		String[] remoteProcesses;
		try {

			remoteProcesses = registry.list();
			
			if(remoteProcesses.length > 0)
			{
				Random randomGenerator = new Random();
				int randomInt = randomGenerator.nextInt(remoteProcesses.length);
				
	        	if(!remoteProcesses[randomInt].equals(processId))
	        	{
	        		System.out.println("Process " + processId + " talks randomly to Process " + remoteProcesses[randomInt]);
	               	IHandleRMI otherprocess = (IHandleRMI) registry.lookup(remoteProcesses[randomInt]);
		            Message m= new Message();
		            m.setPayload("Hi, Process " + processId + " speaking. How are you today?");
		            otherprocess.receive(m);
		           
	        	}
			}
			
	        
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void replytoRandomProcess(Message m)
	{
		try {
			String otherProcessID = m.getPayload().replace("Hi, Process ", "");
			otherProcessID = otherProcessID.replace(" speaking. How are you today?", "");
			
			System.out.println("Process " + processId + " replys to Process " + otherProcessID);
           	
			Message reply = new Message();
			reply.setPayload("Hi, I am fine. By Process " + processId);
			
			IHandleRMI otherprocess = (IHandleRMI) registry.lookup(otherProcessID);
			otherprocess.receive(reply);
			
		} catch (NotBoundException e) {
			e.printStackTrace();
		} catch (AccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void broadcast()
	{
		String[] remoteProcesses;
		try {
			
			remoteProcesses = registry.list();
	        for(int i = 0; i<remoteProcesses.length; i++)
	        {
	        	if(!remoteProcesses[i].equals(processId))
	        	{
	        		System.out.println("Process " + processId + " sent a broadcast to Process " + remoteProcesses[i]);
	               	IHandleRMI otherprocess;
		        	otherprocess = (IHandleRMI) registry.lookup(remoteProcesses[i]);
		            Message m= new Message();
		            m.setPayload("This is a broadcast from Process " + processId);
		            otherprocess.receive(m);
		            
	        	}
	 
	        }
	        
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
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
	
	public int getMaxProcessID() 
	{
		int maxProcessID = 0;
		String[] remoteProcesses;
		try {
			remoteProcesses = registry.list();
	        for(int i = 0; i<remoteProcesses.length; i++)
	        {
	        	int processID = Integer.parseInt(remoteProcesses[i]);
	        	if (processID >maxProcessID)
	        	{
	        		maxProcessID = processID;
	        	}
	        }
	        return maxProcessID;
		} catch (RemoteException e) {
			e.printStackTrace();
			return maxProcessID;
		}
		

	}
	
}
