package da3;

import java.util.ArrayList;
import java.util.Random;

import da3.message.PayloadMessage;

public class FaultyProcess extends Process {

	boolean messageSendingbyCoinFlip;
	boolean orderValueByCoinFlip;
	boolean neverSend;
	
	int messagesWithheld=0;
	
	public FaultyProcess(boolean messageSendingbyCoinFlip, boolean orderValueByCoinFlip, boolean neverSend)
	{
		super();
		this.messageSendingbyCoinFlip = messageSendingbyCoinFlip;
		this.orderValueByCoinFlip = orderValueByCoinFlip;
		this.neverSend = neverSend;
	}
	
	public int getMessagesWithheld() {
		return messagesWithheld;
	}

	public void setMessagesWithheld(int messagesWithheld) {
		this.messagesWithheld = messagesWithheld;
	}

	@Override
	public void processByzantineMessage(ByzantineMessage bMessage)
	{
		processByzantineMessageInAFaultyWay(bMessage, messageSendingbyCoinFlip, orderValueByCoinFlip);
	}
	
	public void processByzantineMessageInAFaultyWay(ByzantineMessage bMessage, boolean messageSendingbyCoinFlip, boolean orderValueByCoinFlip)
	{
		
		bMessageList.add(bMessage);
		decisionTree.addDecision(bMessage);
		
		//if f is greater than 0
		if(bMessage.getF() > 0)
		{
			int f = bMessage.getF();
			
			Random random = new Random();
			
			ArrayList<Integer> commanderProcessIds = (ArrayList<Integer>) bMessage.getCommanderProcessIds().clone(); 
			commanderProcessIds.add(bMessage.getLieutenantProcessId());
			
			for(Integer i : bMessage.getLieutenantsProcessIds())
			{
				ArrayList<Integer> lieutenantsProcessIds = (ArrayList<Integer>) bMessage.getLieutenantsProcessIds().clone();
				lieutenantsProcessIds.remove(Integer.valueOf(i));
				//Hmm should I mix up the order?
				int orderValue = (orderValueByCoinFlip) ? random.nextInt(2) : bMessage.getValue();
				ByzantineMessage bMessageChild = new ByzantineMessage(f-1, orderValue, commanderProcessIds, i, lieutenantsProcessIds);
				PayloadMessage pMessage = new PayloadMessage(synchronizer.getRoundId()+1, processId, i, bMessageChild);
				if(!neverSend)
				{
					if(messageSendingbyCoinFlip && !(random.nextInt(2)==1))
					{
						//Hmm should I send the message?
						setMessagesWithheld(getMessagesWithheld()+1);
					}
					else
					{
						outMessageNextRound.add(pMessage);
					}
				}

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
}
