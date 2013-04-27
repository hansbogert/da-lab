package da3.message;

public class Ack extends Message {
	
	private static final long serialVersionUID = 4656072715961263562L;
	
	private int messageId;
	
	public Ack(int roundId,  int processId, int receiveProcessId, int messageId)
	{
		super(roundId, processId, receiveProcessId);
		setMessageId(messageId);
	}
	
	public int getMessageId() {
		return messageId;
	}
	
	public void setMessageId(int messageId) {
		this.messageId = messageId;
	}
	
	public String toString()
	{
		return "Ack [Round " + getRoundId() + ", SendProcess " + getSentProcessId() + ", ReceiveProcess " + getReceiveProcessId() + ", Message " + getMessageId() + "]";
	}
	
}
