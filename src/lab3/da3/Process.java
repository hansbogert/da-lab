package da3;

import java.rmi.RemoteException;
import java.util.ArrayList;

import da3.message.PayloadMessage;


public class Process {

	private int processId;
	private Synchronizer synchronizer;
	private ArrayList<Integer> neigbourProcessIds;
	/*
	 * Process is a single component in the distributed system.
	 */
	public Process(){
		try {
			
			Synchronizer synchronizer = new Synchronizer(this);
			setSynchronizer(synchronizer);
			
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Generate a unique process id and Register in the RMI Registry.
	 */
	public void register(String ip) {
		synchronizer.register(ip);
	}
	
	public void receive(PayloadMessage pMessage) {
		//TODO
	}
	
	public boolean allMessagesSent()
	{
		return true;
	}
	
	public void setProcessId(int processId) {
		this.processId = processId;
	}
	
	public int getProcessId() {
		return processId;
	}
	
	public Synchronizer getSynchronizer() {
		return synchronizer;
	}
	
	public void setSynchronizer(Synchronizer synchronizer) {
		this.synchronizer = synchronizer;
	}
	

	


}
