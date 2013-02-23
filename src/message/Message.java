package message;

import java.io.Serializable;
import java.util.Vector;


public class Message implements Serializable {

	private static final long serialVersionUID = 616261230788353828L;
	
	public String payload;
	public Vector<Timestamp> buffer;
	public Timestamp timestamp;
	
	public Message() {
		buffer = new Vector<Timestamp>();
	}
	
	public String getPayload() {
		return payload;
	}
	
	public void setPayload(String payload){
		this.payload = payload;
	}
}
