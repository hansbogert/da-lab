package da2.message;

import java.io.Serializable;
import java.util.Vector;

/*
 * Message contains
 * payload, which is a string
 * buffer S, which is a list of ordered pair (i, V)
 * timestamp V, which is a vector clock. 
 */
public class MessagePackage implements Serializable {

	private static final long serialVersionUID = 616261230788353828L;
	
	public Message message;
	public Vector<VectorClock> buffer;
	public VectorClock vectorClock;
	
	private long delay = 0;
	
	public long getDelay() {
		return delay;
	}

	public void setDelay(long delay) {
		this.delay = delay;
	}

	public MessagePackage() {
		buffer = new Vector<VectorClock>(); 
	}
	
	public Message getMessage() {
		return message;
	}
	
	public void setMessage(Message message){
		this.message = message;
	}
	
	@Override
	public boolean equals(Object o){
		boolean is = false;
		if(o instanceof MessagePackage){
			MessagePackage m = (MessagePackage) o;
			is = (message.equals(m.message) && buffer.equals(m.buffer) && vectorClock.equals(m.vectorClock));
		}
		return is;
	}
}
