package da3.message;

import java.io.Serializable;

public class Message implements Serializable {

	private static final long serialVersionUID = -2611070696225875607L;
	
	private int sentProcessId;
	private int receiveProcessId;
	private int roundId;

	public Message(int roundId, int processId, int receiveProcessId) {
		setSentProcessId(processId);
		setRoundId(roundId);
		setReceiveProcessId(receiveProcessId);
	}
	
	public int getReceiveProcessId() {
		return receiveProcessId;
	}

	public void setReceiveProcessId(int receiveProcessId) {
		this.receiveProcessId = receiveProcessId;
	}

	public int getRoundId() {
		return roundId;
	}

	public void setRoundId(int roundId) {
		this.roundId = roundId;
	}


	public int getSentProcessId() {
		return sentProcessId;
	}

	public void setSentProcessId(int processId) {
		this.sentProcessId = processId;
	}

}