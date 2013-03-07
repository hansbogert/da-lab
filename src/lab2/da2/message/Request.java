package da2.message;

public class Request extends Message {

	private static final long serialVersionUID = -3588037939678469700L;
	
	int processId;
	int requestId;
	
	public Request(int processId, int requestId)
	{
		this.processId = processId;
		this.requestId = requestId;
	}
	
	public int getProcessId()
	{
		return processId;
	}
	
	public int getRequestNo(){
		return requestId;
	}

}
