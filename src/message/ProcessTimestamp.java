package message;

import java.util.Vector;

public class ProcessTimestamp extends VectorClock {
	
	private static final long serialVersionUID = -7441889085189603427L;
	
	private int processId;
	
	public ProcessTimestamp(int processId) {
		super();
	}

	public int getProcessId()
	{
		return processId;
	}
	
	public void setProcessId(int processId)
	{
		this.processId = processId;
	}
	
	public String toString() {
		String str = "(";
		str += processId + ", ";
		str += super.toString();
		str += ")";
		return str;
	}

}
