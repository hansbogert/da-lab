package da3;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.hamcrest.core.IsInstanceOf;

import com.sun.swing.internal.plaf.synth.resources.synth;

import da3.message.PayloadMessage;


public class Process {


	protected int processId;
	protected Synchronizer synchronizer;
	protected ArrayList<PayloadMessage> outMessageNextRound;
	private boolean allMessagesSent;
	
	private boolean decided;
	public ArrayList<ByzantineMessage> bMessageList = new ArrayList<ByzantineMessage>();
	public ArrayList<ByzantineMessage> bMessageListOut = new ArrayList<ByzantineMessage>();
	protected DecisionTreeNode decisionTree;
	
	private int messagesSent = 0;
	
	private boolean decisionPublished;
	
	private boolean isTopCommander;

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

	public int getMessagesSent() {
		return messagesSent;
	}

	public void setMessagesSent(int messagesSent) {
		this.messagesSent = messagesSent;
	}

	/*
	 * Generate a unique process id and Register in the RMI Registry.
	 */
	public void register(String ip) {
		synchronizer.register(ip);
	}
	
	public void receive(PayloadMessage pMessage) {
		processByzantineMessage(pMessage.getByzantineMessage());
		if(pMessage.getByzantineMessage().getCommanderProcessIds().size() < synchronizer.getRoundId())
		{
			System.out.println("Why would round "+synchronizer.getRoundId()+" receive stuffs from " +pMessage.getByzantineMessage().getCommanderProcessIds().size()+" commandars?"+ pMessage.toString());
		}
	}
	
	public void progressToNextRound()
	{
		if(!isTopCommander)
		{
			int lastRoundId = getSynchronizer().getRoundId() - 1;
			checkMissedMessages(decisionTree, lastRoundId - 1);
		}

		
		regulateByzantineAgreement();
		
		setAllMessagesSent(false);
		
		//Send outgoing Messages generated in previous round but should be sent in this round.
		for(PayloadMessage pMessage : outMessageNextRound)
		{
			send(pMessage);
		}
		outMessageNextRound.clear();
		
		setAllMessagesSent(true);
		
		
		final ExecutorService service = Executors.newSingleThreadExecutor();
		service.execute(new Runnable() {
			@Override
			public void run() {
				if(synchronizer.synchronizerDiagnotics){
					System.out.println("Process " + getProcessId() + " sent all messages at round " + synchronizer.getRoundId());}
				synchronizer.regulateSafety("all messages sent");
				synchronizer.regulateProgress("all messages sent");


			}
		});
		
	}
	
	
	synchronized public void send(PayloadMessage pMessage)
	{
		setMessagesSent(getMessagesSent()+1);

		pMessage.setRoundId(synchronizer.getRoundId()); //TODO: Dirty Code, safety measure.
		synchronizer.send(pMessage);
		if(pMessage.getRoundId()> synchronizer.getRoundId())
		{
			System.out.println("how can round id be different?" + pMessage.getRoundId() +" "+ synchronizer.getRoundId() +" "+ pMessage.toString());
		}
		if(pMessage.getByzantineMessage().getCommanderProcessIds().size() < synchronizer.getRoundId())
		{
			System.out.println("Why would round "+synchronizer.getRoundId()+" send stuffs from " +pMessage.getByzantineMessage().getCommanderProcessIds().size()+" commandars?"+ pMessage.toString());
		}
	}
	
	public void initRounds()
	{
		synchronizer.progressToNextRound();
	}
	
	public void regulateByzantineAgreement()
	{
		if(isDecided() & !decisionPublished)
		{
			if(synchronizer.byzantineDiagnotics){
				System.out.println("Process " + getProcessId() + " decided on order " + decisionTree.getMajorityOrder() + " at round " + synchronizer.getRoundId());}
			decisionPublished = true;
		}
	}
	
	public ByzantineMessage forgeByzantineMessage(DecisionTreeNode dNode, ArrayList<Integer> neighbourIds, Integer processId)
	{
		ArrayList<Integer> commanderProcessIds = dNode.getAncestorIds();
		
		commanderProcessIds.add(dNode.getCommander());

		ArrayList<Integer> lieutenantsProcessIds = neighbourIds;
		for(Integer i : commanderProcessIds)
		{
			lieutenantsProcessIds.remove(Integer.valueOf(i));
		}
		lieutenantsProcessIds.remove(Integer.valueOf(processId));
		
		int defaultValue = 0;
		
		ByzantineMessage bMessage = new ByzantineMessage(dNode.getF(), defaultValue, commanderProcessIds, processId, lieutenantsProcessIds);
		bMessage.setForged(true);
		//System.out.println("Process " + getProcessId() + " FORGED: " + bMessage.toString() + " at round " + synchronizer.getRoundId() );
		processByzantineMessage(bMessage);
		return bMessage;
	}
	
	public void checkMissedMessages(DecisionTreeNode dNode, int level)
	{
		if(level == 0)
		{
			if(!dNode.isOrderReceived() && dNode.getOrder() == null)
			{
				ArrayList<Integer> neighboursIds = synchronizer.getRemoteProcessIds();
				forgeByzantineMessage(dNode, neighboursIds, getProcessId());
			}
		}
		else if(level > 0)
		{
			for(DecisionTreeNode dNodeChild : dNode.getChildren())
			{
				checkMissedMessages(dNodeChild, level - 1);
			}
				
		}
	}
	
	public void setUpDecisionTree(Integer topCommanderId, Integer f)
	{
		if(topCommanderId.intValue()==getProcessId())
		{
			isTopCommander = true;
		}
		ArrayList<Integer> lieutenantIds = synchronizer.getRemoteProcessIds();
		lieutenantIds.remove(Integer.valueOf(topCommanderId));
		lieutenantIds.remove(Integer.valueOf(getProcessId()));
		Integer level = 0;
		decisionTree = new DecisionTreeNode(topCommanderId, lieutenantIds, f, level);
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
		int order = bMessage.getValue();
		bMessageList.add(bMessage);
		decisionTree.addDecision(bMessage);
		
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
				bMessageListOut.add(bMessageChild);
				
//				if(bMessageChild.getValue()==0)
//				{
//					System.out.println("P " + getProcessId() + "get" + bMessage.toString() +"and  produces bMessage "+ bMessageChild.toString());
//				}
			}
			
			//if the commander is not the top commander.
			if(bMessage.getCommanderProcessIds().size()==0)
			{
				setDecided(true);
			}

		}
		else
		{
			setDecided(true);
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


	public DecisionTreeNode getDecisionTree() {
		return decisionTree;
	}

	public void setDecisionTree(DecisionTreeNode decisionTree) {
		this.decisionTree = decisionTree;
	}
}
