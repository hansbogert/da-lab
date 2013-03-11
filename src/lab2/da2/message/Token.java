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

	public void updateQueue(int processId, int[] RN) {
		assert (RN.length == LN.length);

		int processesCount = RN.length;

		for (int i = 0; i < processesCount; i++) {

			if (RN[i] != LN[i]) { // TODO:review shouldnt this be inequality of rn>ln?
				// because the queue starts at 0, process 1 stored in 0.
				int pid = i + 1;
				if (!q.contains(pid)) {
					q.add(pid);
				}
			}
		}
		// TODO:review, this block below was IN the loop, so it was happily
		// removing every round. I imagine this was not intended. (see diff for
		// original code)
		assert (q.peek() == processId);
		// remove itself from the queue.
		q.poll();
	}
}
