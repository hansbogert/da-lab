package da3;

import java.io.Serializable;
import java.util.ArrayList;

public class ByzantineMessage implements Serializable {
	
	private static final long serialVersionUID = -3993898502124970456L;

	//Number of faulty processes.
	private int f;
	
	private int value;
	
	private ArrayList<Integer> commanderProcessIds;
	
	private int lieutenantProcessId;
	
	private ArrayList<Integer> lieutenantsProcessIds;
	
	public boolean forged;
	


	public ByzantineMessage(int f, int value, ArrayList<Integer> commanderProcessIds, int lieutenantProcessId, ArrayList<Integer> lieutenantsProcessIds)
	{
		setF(f);
		setValue(value);
		setCommanderProcessIds(commanderProcessIds);
		setLieutenantProcessId(lieutenantProcessId);
		setLieutenantsProcessIds(lieutenantsProcessIds);
	}
	
	public int getF() {
		return f;
	}

	public void setF(int f) {
		this.f = f;
	}

	public String toString()
	{
		return "Byzantine Message {"
				+ " f: " + getF()
				+ " value: " + getValue()
				+ " commander: " + getCommanderProcessIds().toString() 
				+ " lieutenant: " + getLieutenantProcessId()
				+ " lieutenants: " + getLieutenantsProcessIds().toString()
				+ ((forged) ? " forged " : "") +" }";
	}
	
	public ArrayList<Integer> getCommanderProcessIds() {
		return commanderProcessIds;
	}

	public void setCommanderProcessIds(ArrayList<Integer> commanderPrcocessIds) {
		this.commanderProcessIds = commanderPrcocessIds;
	}

	public int getLieutenantProcessId() {
		return lieutenantProcessId;
	}

	public void setLieutenantProcessId(int lieutenantProcessId) {
		this.lieutenantProcessId = lieutenantProcessId;
	}

	public ArrayList<Integer> getLieutenantsProcessIds() {
		return lieutenantsProcessIds;
	}

	public void setLieutenantsProcessIds(ArrayList<Integer> lieutenantsProcessIds) {
		this.lieutenantsProcessIds = lieutenantsProcessIds;
	}
	

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}
	
	public boolean isForged() {
		return forged;
	}

	public void setForged(boolean forged) {
		this.forged = forged;
	}
}
