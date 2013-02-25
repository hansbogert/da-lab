package message;

import java.io.Serializable;
import java.util.Vector;

/*
 * Tiemstamp contains a vector clock that is used for synchronizing between processes.
 */
public class Timestamp implements Serializable {
	
	private static final long serialVersionUID = 775137377950435498L;
	
	public Vector<Integer> values;
	
	public Timestamp() {
		values = new Vector<Integer>();
	}
	
	/*
	 * Increment the timestamp for process i
	 * Note that the timestamp for process i is stored at V(i-1)
	 */
	public void incrementAt(int processId) {
		int next = values.get(processId -1) + 1 ;
		values.set(processId, next);
	}
	
	/*
	 * Set all processes timestamps to 0
	 * Since the vector length can be infinite, just set until the maximum process number.
	 */
	public void initToZeros(int maxProcessNo)
	{
		for(int i = 0; i<maxProcessNo; i++)
		{
			values.add(i, 0);
		}
	}
	
	/*
	 * Check if this timestamp is greater or equal to the input timestamp
	 */
	public boolean isGreaterOrEqual(Timestamp timestamp)
	{
		boolean isGreaterOrEqual = true;
		int vectorLength = values.size();
		assert(values.size() == timestamp.values.size());
		for(int i= 0; i<vectorLength; i++)
		{
			if(values.get(i) < timestamp.values.get(i))
			{
				isGreaterOrEqual = false;
			}
		}
		
		return isGreaterOrEqual;
		
	}
	
	/*
	 * Merge two timestamps
	 */
	public void mergeWith(Timestamp timestamp)
	{
		int vectorLength = values.size();
		assert(values.size() == timestamp.values.size());
		for(int i= 0; i<vectorLength; i++)
		{
			if(values.get(i) < timestamp.values.get(i))
			{
				values.set(i, timestamp.values.get(i));
			}
		}
		
	}
	
	public String toString()
	{
		String str = "";
		str +=  values;
		return str;
	}
	
}
