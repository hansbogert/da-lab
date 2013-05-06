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
	private ArrayList<PayloadMessage> outMessageNextRound;
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
		
		outMessageNextRound = new ArrayList<PayloadMessage>();
	}

	/*
	 * Generate a unique process id and Register in the RMI Registry.
	 */
	public void register(String ip) {
		synchronizer.register(ip);
	}
	
	public void receive(PayloadMessage pMessage) {
		processByzantineMessage(pMessage);
	}
	
	public void progressToNextRound()
	{
		setAllMessagesSent(false);
		
		//Send outgoing Messages generated in previous round but should be sent in this round.
		for(PayloadMessage pMessage : outMessageNextRound)
		{
			send(pMessage);
		}
		outMessageNextRound.clear();
		
		setAllMessagesSent(true);
		
		
		final ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
		service.schedule(new Runnable() {
			@Override
			public void run() {
				if(synchronizer.synchronizerDiagnotics){System.out.println("Process " + getProcessId() + " sent all messages at round " + synchronizer.getRoundId());}
				synchronizer.regulateSafety("all messages sent");
				synchronizer.regulateProgress("all messages sent");
			}
		}, 0,TimeUnit.MILLISECONDS);
	}
	
	
	public void send(PayloadMessage pMessage)
	{
		synchronizer.send(pMessage);
	}
	
	public void startRounds()
	{
		synchronizer.progressToNexRound();
	}
	
	public void initByzantineAlgorithm()
	{
		ArrayList<Integer> neighboursIds = synchronizer.getRemoteProcessIds();
		for(Integer i : neighboursIds)
		{
			PayloadMessage pMessageOut = new PayloadMessage(synchronizer.getRoundId()+1, processId, i);
			pMessageOut.randomAdditiveNumber = i;
			outMessageNextRound.add(pMessageOut);
		}

	}
	
	public void processByzantineMessage(PayloadMessage pMessage)
	{
		int nextRoundId = synchronizer.getRoundId() + 1;
		
		PayloadMessage pMessageOut = new PayloadMessage(nextRoundId, processId, pMessage.getSentProcessId());
		pMessageOut.randomAdditiveNumber = pMessage.randomAdditiveNumber + 10;
		outMessageNextRound.add(pMessageOut);
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
