package da3;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import da3.message.PayloadMessage;


public class Process {

	private int processId;
	private Synchronizer synchronizer;
	private ArrayList<Integer> neigbourProcessIds;
	private ArrayList<PayloadMessage> inMessages;
	private ArrayList<PayloadMessage> inMessagesPreviousRound;
	private boolean allMessagesSent;
	
	/*
	 * Process is a single component in the distributed system.
	 */
	public Process(){
		
		try {
			
			Synchronizer synchronizer = new Synchronizer(this);
			setSynchronizer(synchronizer);
			
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		
		neigbourProcessIds = new ArrayList<Integer>();
		inMessages = new ArrayList<PayloadMessage>();
		inMessagesPreviousRound = new ArrayList<PayloadMessage>();
	}

	/*
	 * Generate a unique process id and Register in the RMI Registry.
	 */
	public void register(String ip) {
		synchronizer.register(ip);
	}
	
	public void receive(PayloadMessage pMessage) {
		inMessages.add(pMessage);
	}
	
	public void progressToNextRound()
	{
		setAllMessagesSent(false);
		
		inMessagesPreviousRound = inMessages;
		inMessages.clear();
		
		doSomeCalculation();
		
		for(PayloadMessage pMessage : inMessagesPreviousRound)
		{
			processInMessages(pMessage);
		}
		
		setAllMessagesSent(true);
		
		
		final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
		service.schedule(new Runnable() {
			@Override
			public void run() {
				System.out.println("Process " + getProcessId() + " sent all messages at round " + synchronizer.getRoundId());
				synchronizer.regulateSafety();
				synchronizer.regulateProgress();
			}
		}, 0,TimeUnit.MILLISECONDS);


	}
	
	public void processInMessages(PayloadMessage pMessage)
	{
		
	}
	
	public void send(PayloadMessage pMessage)
	{
		synchronizer.send(pMessage);
	}
	
	public void doSomeCalculation()
	{
//		try {
//			Thread.sleep(10);
//		} catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
	
	public void startRounds()
	{
		setAllMessagesSent(true);
		
		synchronizer.regulateSafety();
		synchronizer.regulateProgress();
	}
	
	public void setProcessId(int processId) {
		this.processId = processId;
	}
	
	public int getProcessId() {
		return processId;
	}
	
	public Synchronizer getSynchronizer() {
		return synchronizer;
	}
	
	public void setSynchronizer(Synchronizer synchronizer) {
		this.synchronizer = synchronizer;
	}
	
	public boolean isAllMessagesSent() {
		return allMessagesSent;
	}

	public void setAllMessagesSent(boolean allMessagesSent) {
		this.allMessagesSent = allMessagesSent;
	}
	


}
