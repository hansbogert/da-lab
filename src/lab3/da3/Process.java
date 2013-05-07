package da3;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.hamcrest.core.IsInstanceOf;

import da3.message.PayloadMessage;


public class Process {

	private int processId;
	private Synchronizer synchronizer;
	private ArrayList<PayloadMessage> outMessageNextRound;
	private boolean allMessagesSent;
	
	private boolean decided;
	public ArrayList<ByzantineMessage> bMessageList = new ArrayList<ByzantineMessage>();

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
		processByzantineMessage(pMessage.getByzantineMessage());
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
	
	public void initRounds()
	{
		synchronizer.progressToNexRound();
	}
	
	public void initByzantineAlgorithm(int f, int value)
	{
		ArrayList<Integer> neighboursIds = synchronizer.getRemoteProcessIds();
		ArrayList<Integer> commanderProcessIds = new ArrayList<Integer>();
		ByzantineMessage bMessage = new ByzantineMessage(f+1, value, commanderProcessIds, getProcessId(), neighboursIds);
		processByzantineMessage(bMessage);
	}
		
	public void processByzantineMessage(ByzantineMessage bMessage)
	{
		//TODO test
		bMessageList.add(bMessage);
		
		//if f is greater than 0
		if(bMessage.getF() > 0)
		{
			int f = bMessage.getF();
					
			ArrayList<Integer> commanderProcessIds = (ArrayList<Integer>) bMessage.getCommanderProcessIds().clone(); 
			commanderProcessIds.add(bMessage.getLieutenantProcessId());
			
			for(Integer i : bMessage.getLieutenantsProcessIds())
			{
				ArrayList<Integer> lieutenantsProcessIds = (ArrayList<Integer>) bMessage.getLieutenantsProcessIds().clone();
				lieutenantsProcessIds.remove(Integer.valueOf(i));
				ByzantineMessage bMessageChild = new ByzantineMessage(f-1, bMessage.getValue(), commanderProcessIds, i, lieutenantsProcessIds);
				PayloadMessage pMessage = new PayloadMessage(synchronizer.getRoundId()+1, processId, i, bMessageChild);
				outMessageNextRound.add(pMessage);
			}
			
			if(processId == 1)
			{
				System.out.println();
			}
			//if the commander is not the top commander.
			if(bMessage.getCommanderProcessIds().size()==0)
			{
				setDecided(true);
				if(synchronizer.byzantineDiagnotics){System.out.println("Process " + getProcessId() + " is top commander, decided");}
			}

		}
		else
		{
			setDecided(true);
			if(synchronizer.byzantineDiagnotics){System.out.println("Process " + getProcessId() + " sent all Byzantine message, waits for decision :");}
		}

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
	
	public boolean isDecided() {
		return decided;
	}

	public void setDecided(boolean decided) {
		this.decided = decided;
	}


}
