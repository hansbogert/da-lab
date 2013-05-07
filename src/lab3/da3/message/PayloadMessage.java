package da3.message;

import da3.ByzantineMessage;

public class PayloadMessage extends Message {
	
	private static final long serialVersionUID = 4321388220582463184L;
	
	private int messageId;
	public ByzantineMessage byzantineMessage;
	
	public PayloadMessage(int roundId, int processId, int receiveProcessId, ByzantineMessage byzantineMessage)
	{
		super(roundId, processId, receiveProcessId);
		setByzantineMessage(byzantineMessage);
	}	
	
	public ByzantineMessage getByzantineMessage() {
		return byzantineMessage;
	}

	public void setByzantineMessage(ByzantineMessage byzantineMessage) {
		this.byzantineMessage = byzantineMessage;
	}

	public int getMessageId() {
		return messageId;
	}

	public void setMessageId(int messageId) {
		this.messageId = messageId;
	}

	public String toString()
	{
		return "Payload [Round " + getRoundId() + ", SendProcess " + getSentProcessId() + ", ReceiveProcess " + getReceiveProcessId() + ", Message " + getMessageId() + ", ByzantineMessage " + getByzantineMessage() +"]";
	}
}
