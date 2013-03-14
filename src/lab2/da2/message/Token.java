package da2.message;

import java.util.LinkedList;
import java.util.Queue;

public class Token extends Message {

	private static final long serialVersionUID = -471820463274967721L;

	/*
	 * An array LN with for every process the number of the last request that
	 * was granted
	 */
	int[] LN;
	Queue<Integer> q;

	public Token(int maxProcessCount) {
		LN = new int[maxProcessCount];
		q = new LinkedList<Integer>();
	}

	public int[] getLN() {
		return LN;
	}

	public void setLN(int[] ln) {
		this.LN = ln;
	}

	public int getRequestNoAt(int processId) {
		return LN[processId - 1];
	}

	public void setRequestNoAt(int grantedRequestId, int processId) {
		LN[processId - 1] = grantedRequestId;
	}

	public Queue<Integer> getQueue() {
		return q;
	}
	
	public String printQueue()
	{
		Integer[] queueContent = q.toArray(new Integer[q.size()]);
		String str = "[";
		
		if(queueContent.length>0)
		{
			str += queueContent[0];
		}
		
		for(int i = 1; i<queueContent.length;i++)
		{
			str += "," + queueContent[i];
		}
		str += "]";
		return str;
	}
	
	public String printLN()
	{
		String str = "[";
		
		if(LN.length>0)
		{
			str += LN[0];
		}
		
		for(int i = 1; i<LN.length;i++)
		{
			str += "," + LN[i];
		}
		str += "]";
		return str;
	}

	/*
	 * Update the quene, remove this process id from the quene
	 * Add processes with new request to queue, by checking the different in RN and LN
	 */
	public void updateQueue(int processId, int[] RN) {
		assert (RN.length == LN.length);

		int processesCount = RN.length;

		for (int i = 0; i < processesCount; i++) {

			if (RN[i] != LN[i]) { // TODO:review shouldnt this be inequality of rn>ln? Because in no way ln<rn, that's why doesn't matter
				// because the queue starts at 0, process 1 stored in 0.
				int pid = i + 1;
				
				//If process not already in the queue, than add it to the quene.
				if (!q.contains(pid)) {
					q.add(pid);
				}
			}
		}
		// TODO: reviewed, can be removed, this block below was IN the loop, so it was happily
		// removing every round. I imagine this was not intended. (see diff for
		// original code) //Stupid bug, you are right.
		
		assert (q.peek() == processId); //TODO review, assert not working :P
		// remove itself from the queue, if it is in the quene
		if(q.peek() != null && q.peek() == processId)
		{
			q.poll();
		}
		
	}
}
