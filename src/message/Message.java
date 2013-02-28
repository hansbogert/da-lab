package message;

import java.io.Serializable;
import java.util.Vector;

/*
 * Message contains
 * payload, which is a string
 * buffer S, which is a list of ordered pair (i, V)
 * timestamp V, which is a vector clock. 
 */
public class Message implements Serializable {

	private static final long serialVersionUID = 616261230788353828L;
	
	public String payload;
	public Vector<VectorClock> buffer;
	public VectorClock vectorClock;
	
	public Message() {
		buffer = new Vector<VectorClock>(); 
	}
	
	public String getPayload() {
		return payload;
	}
	
	public void setPayload(String payload){
		this.payload = payload;
	}
}
