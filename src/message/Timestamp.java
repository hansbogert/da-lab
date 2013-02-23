package message;

import java.util.ArrayList;
import java.util.Vector;

public class Timestamp {
	
	public int processId;
	public Vector<Integer> timestamp;
	
	public Timestamp() {
		timestamp = new Vector<Integer>();
	}
	
	public void incrementAt(int processId) {
		int next = timestamp.get(processId) + 1;
		timestamp.set(processId, next);
	}
	
}
