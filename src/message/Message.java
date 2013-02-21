package message;

import java.io.Serializable;


public class Message implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 616261230788353828L;
	String payload;
	History buffer;
	Timestamp timestamp;
	
	public String getPayload() {
		return payload;
	}
	
	public void setPayload(String payload){
		this.payload = payload;
	}
}
