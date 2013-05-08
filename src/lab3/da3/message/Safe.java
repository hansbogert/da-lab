package da3.message;

public class Safe extends Message {
	
	private static final long serialVersionUID = 353680458411969055L;

	public Safe(int roundId, int processId, int receiveProcessId)
	{
		super(roundId, processId, receiveProcessId);
	}
	
	public String toString()
	{
		return "Safe [Round " + getRoundId() + ", SendProcess " + getSentProcessId() + ", ReceiveProcess " + getReceiveProcessId() + "]";
	}
	

}
