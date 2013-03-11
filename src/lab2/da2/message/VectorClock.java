package da2.message;

import java.io.Serializable;
import java.util.Vector;

/*
 * Timestamp contains a vector clock that is used for synchronizing between processes.
 */
public class VectorClock implements Serializable, Cloneable {

	private static final long serialVersionUID = 775137377950435498L;
	private int processId;

	public Vector<Integer> values;

	public VectorClock() {
		values = new Vector<Integer>();
	}

	public void setProcessId(int pid) {
		this.processId = pid;
	}

	public int getProcessId() {
		return processId;
	}

	/*
	 * Increment the timestamp for process i Note that the timestamp for process
	 * i is stored at V(i-1)
	 */
	public void incrementAt(int processId) {
		int next = values.get(processId - 1) + 1;
		values.set(processId - 1, next);
	}

	/**
	 * Tests for equality with another vectorclock
	 * 
	 * @param
	 * @return true if object is the same
	 */
	public boolean equals(Object o) {
		boolean is = false;
		if (o instanceof VectorClock) {
			VectorClock vc = (VectorClock) o;
			is = (vc.processId == processId && values.equals(vc.values));
		}
		return is;

	}

	public int getProcessTimeStamp(int pid) {
		return values.get(pid - 1);
	}

	/*
	 * Set all processes timestamps to 0 Since the vector length can be
	 * infinite, just set until the maximum process number.
	 */
	public void initToZeros(int maxProcessNo) {
		for (int i = 0; i < maxProcessNo; i++) {
			values.add(i, 0);
		}
	}

	/*
	 * Check if this timestamp is greater or equal to the input timestamp
	 */
	public boolean isGreaterOrEqual(VectorClock timestamp) {
		boolean isGreaterOrEqual = true;
		int vectorLength = values.size();
		assert (values.size() == timestamp.values.size());
		for (int i = 0; i < vectorLength; i++) {
			if (values.get(i) < timestamp.values.get(i)) {
				isGreaterOrEqual = false;
			}
		}

		return isGreaterOrEqual;

	}

	/*
	 * Merge two timestamps
	 */
	public void mergeWith(VectorClock timestamp) {
		int vectorLength = values.size();
		assert (values.size() == timestamp.values.size());
		for (int i = 0; i < vectorLength; i++) {
			if (values.get(i) < timestamp.values.get(i)) {
				values.set(i, timestamp.values.get(i));
			}
		}
	}

	public String toString() {
		String str = processId + ": ";
		str += values;
		return str;
	}

	@Override
	public VectorClock clone() {
		VectorClock vc = new VectorClock();
		vc.processId = processId;
		Vector<Integer> vb = (Vector<Integer>) values.clone();
		vc.values = vb;
		return vc;
	}

	public void setProcesId(int pid) {
		this.processId = pid;
	}
}
