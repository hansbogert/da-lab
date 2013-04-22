package da3.message;

public class Message {

	private int processId;
	private int roundId;

	public Message(int roundId, int processId) {
		setProcessId(processId);
		setRoundId(roundId);
	}
	
	public int getRoundId() {
		return roundId;
	}

	public void setRoundId(int roundId) {
		this.roundId = roundId;
	}


	public int getProcessId() {
		return processId;
	}

	public void setProcessId(int processId) {
		this.processId = processId;
	}

}