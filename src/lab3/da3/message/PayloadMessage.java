package da3.message;

public class PayloadMessage extends Message {
	
	private static final long serialVersionUID = 4321388220582463184L;
	
	private int messageId;
	public int randomAdditiveNumber;
	
	public PayloadMessage(int roundId, int processId, int receiveProcessId)
	{
		super(roundId, processId, receiveProcessId);
	}	
	
	public int getMessageId() {
		return messageId;
	}

	public void setMessageId(int messageId) {
		this.messageId = messageId;
	}

	public String toString()
	{
		return "Payload [Round " + getRoundId() + ", SendProcess " + getSentProcessId() + ", ReceiveProcess " + getReceiveProcessId() + ", Message " + getMessageId() + ", RandomAddNo " + randomAdditiveNumber +"]";
	}
}
