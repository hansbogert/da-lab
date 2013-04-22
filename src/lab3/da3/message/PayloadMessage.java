package da3.message;

public class PayloadMessage extends Message {
	
	private int messageId;
	
	public PayloadMessage(int roundId, int processId, int messageId)
	{
		super(roundId, processId);
		setMessageId(messageId);
	}	
	
	public int getMessageId() {
		return messageId;
	}

	public void setMessageId(int messageId) {
		this.messageId = messageId;
	}


}
