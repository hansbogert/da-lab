package da;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import message.Message;
import message.ProcessTimestamp;
import message.Timestamp;

public class Process extends UnicastRemoteObject implements IHandleRMI {

	private static final long serialVersionUID = -397296118682038104L;

	ArrayList<Message> B;

	int processId;

	Registry registry;

	Vector<ProcessTimestamp> S;
	Timestamp V;

	/*
	 * Process is a single component in the distributed system.
	 */
	public Process() throws RemoteException {

	}

	/*
	 * Generate a unique process id and Register in the RMI Registry.
	 */
	void register(String ip) {
		try {
			// Find the RMI Registry.
			registry = LocateRegistry.getRegistry(ip, 1099);
			// Generate a unique processId.
			processId = getMaxProcessID() + 1;
			// Register into the RMI Registry.
			registry.rebind((getMaxProcessID() + 1) + "", this);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * Crate a buffer S, containing ProcessTimestamp (processId, V). Create a
	 * vector clock V, assuming no of process is under 20
	 */
	public void init() {
		S = new Vector<ProcessTimestamp>();
		V = new Timestamp();
		V.initToZeros(10);
	}

	/*
	 * Return the maximum process ID of the processes binded the registry.
	 */
	public int getMaxProcessID() {
		int maxProcessID = 0;
		String[] remoteProcesses;
		try {
			remoteProcesses = registry.list();
			// for all remote processes binded to the registry,
			for (int i = 0; i < remoteProcesses.length; i++) {
				// overwrite maxProcessID if any processId is greater.
				int processID = Integer.parseInt(remoteProcesses[i]);
				maxProcessID = (processID > maxProcessID) ? processID
						: maxProcessID;
			}
			return maxProcessID;
		} catch (RemoteException e) {
			e.printStackTrace();
			return maxProcessID;
		}
	}

	/*
	 * Get the names of all remote processes
	 */
	public String[] getRemoteProcesses() {
		try {
			return registry.list();
		} catch (RemoteException e) {
			e.printStackTrace();
			return null;
		}
	}

	/*
	 * When other remote processes call the transfer function, this process will
	 * receive the parameter as the message. Then it prints out the payload, and
	 * (only for fun) reply when receiving a random conversation.
	 */
	public void transfer(Message m) throws RemoteException {
		receive(m);
	}

	/*
	 * Receive a message
	 */
	public void receive(Message m) {

		// if D(m) then
		if (deliveryPermitted(m)) {
			// deliver(m)
			deliver(m);

			// while ( |{(m,k,Vm) in B | Dk(m)}| > 0 ) TODO what in heaven's
			// name is k?
			boolean nothingToDeliver = false;
			while (!nothingToDeliver) {
				nothingToDeliver = true;
				for (Message n : B) {
					if (deliveryPermitted(n)) {
						nothingToDeliver = false;
						// deliver such a message m
						deliver(n);
					}
				}
			}

		}
		// else add (m,j,Vm) to B
		else {
			B.add(m);
			// System.out.println("Send to Buffer");
		}
	}

	/*
	 * Check if it is permitted to deliver Condition for delivery of message m
	 * with accompanying buffer Sm in Pi 1. there does not exist (i,V) in Sm 2.
	 * or there does exist (i,V) in Sm and V <= Vi
	 */
	public boolean deliveryPermitted(Message m) {
		boolean deliveryPermitted = true;

		for (ProcessTimestamp pt : m.S) {
			if (pt.getProcessId() == processId) {
				if (V.isGreaterOrEqual(pt)) {
					deliveryPermitted = false;
					System.out.println(V.toString() + " " + pt.toString());
				}
			}
		}

		return deliveryPermitted;
	}

	/*
	 * Send a message.
	 */
	public void send(String payload, int remoteProcessId) {
		try {

			// Increment timestamp before send event
			V.incrementAt(processId);

			// send(m,S,V) to Pj
			Message m = new Message();
			m.setPayload(payload);
			m.S = S;
			m.V = V;
			IHandleRMI remoteProcess = (IHandleRMI) registry.lookup(Integer
					.toString(remoteProcessId));
			remoteProcess.transfer(m);

			// delete any old element for Pj
			for (int i = 0; i < S.size(); i++) {
				if (S.get(i).getProcessId() == remoteProcessId) {
					S.remove(i);
				}
			}

			// insert(j,V) into S
			ProcessTimestamp rpt = new ProcessTimestamp(remoteProcessId);
			// TODO V.value == rpt.value Problem?
			rpt.values = V.values;

		} catch (RemoteException e) {
			e.printStackTrace();

		} catch (NotBoundException e) {
			e.printStackTrace();
		}

	}

	/*
	 * Deliver a message.
	 */
	public void deliver(Message m) {

		// Increment timestamp before send event
		V.incrementAt(processId);

		// deliver(m) to P
		reponseToDelivery(m.payload);
		// for all ((j,V’) in Sm) do
		for (ProcessTimestamp ptRemote : m.S) {
			// if (there exists (j,V’’) in S) then
			boolean ptLocalExist = false;

			for (int i = 0; i < S.size(); i++) {
				if (S.get(i).getProcessId() == ptRemote.getProcessId()) {
					ptLocalExist = true;
					ProcessTimestamp merged = S.get(i);
					// remove (j,V’’) from S
					S.remove(i);
					// V’’:=max(V’,V’’)
					merged.mergeWith(ptRemote);
					// insert(j,V’’) into S
					S.add(merged);
				}
			}

			// else insert(j,V’) into S
			if (!ptLocalExist) {
				S.add(ptRemote);
			}
		}

	}

	/*
	 * Response to the delivery of a message. For the fun, and (or) for testing
	 */
	public void reponseToDelivery(String payload) {
		System.out.println("Process " + processId + " receives [" + payload
				+ "]");
		if (payload.contains("How are you today?")) {
			replytoRandomProcess(payload);
		}
	}

	/*
	 * Send a broadcast to all processes binded to the registry. For the fun,
	 * and (or) for testing.
	 */
	public void broadcast() {
		String[] remoteProcesses = getRemoteProcesses();
		// for all remote processes binded to the registry,
		for (int i = 0; i < remoteProcesses.length; i++) {
			// if it does not have the same name as this process,
			if (Integer.parseInt(remoteProcesses[i]) != (processId)) {
				System.out.println("Process " + processId
						+ " sent a broadcast to Process " + remoteProcesses[i]);

				String payload = "This is a broadcast from Process "
						+ processId;
				int remoteProcessId = Integer.parseInt(remoteProcesses[i]);
				send(payload, remoteProcessId);
			}
		}

	}

	/*
	 * Talks to a random process that is not itself. A reply from that process
	 * is expected. For the fun, and (or) for testing.
	 */
	public void talktoRandomProcess() {
		// printBuffer_Timestamp(); //TODO To be deleted

		String[] remoteProcesses = getRemoteProcesses();
		// for all remote processes binded to the registry,
		if (remoteProcesses.length > 0) {
			Random randomGenerator = new Random();
			int randomInt = randomGenerator.nextInt(remoteProcesses.length);
			// send a message to a random process binded to the registry.
			if (Integer.parseInt(remoteProcesses[randomInt]) != (processId)) {
				System.out.println("Process " + processId
						+ " talks randomly to Process "
						+ remoteProcesses[randomInt]);

				String payload = "Hi, Process " + processId
						+ " speaking. How are you today?";
				int remoteProcessId = Integer
						.parseInt(remoteProcesses[randomInt]);
				send(payload, remoteProcessId);

			}
		}

	}

	/*
	 * After receiving a random message from another process, this process
	 * should send a reply. For the fun, and (or) for testing.
	 */
	public void replytoRandomProcess(String incomingPayload) {
		// printBuffer_Timestamp();

		// find the process id of the sender process.
		String remoteProcessName = incomingPayload.replace("Hi, Process ", "");
		remoteProcessName = remoteProcessName.replace(
				" speaking. How are you today?", "");

		System.out.println("Process " + processId + " replys to Process "
				+ remoteProcessName);

		String payload = "Hi, I am fine. By Process " + processId;
		int remoteProcessId = Integer.parseInt(remoteProcessName);
		send(payload, remoteProcessId);

	}

	/*
	 * Call broadcast() again and again. For the fun, and (or) for testing.
	 */
	public void broadcastRepeatedly(int timespan) {
		final ScheduledExecutorService service = Executors
				.newSingleThreadScheduledExecutor();
		service.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				broadcast();
			}
		}, 0, timespan, TimeUnit.SECONDS);
	}

	/*
	 * Call talktoRandomProces() again and again. For the fun, and (or) for
	 * testing.
	 */
	public void talktoRandomProcessRepeatedly(int timespan) {
		final ScheduledExecutorService service = Executors
				.newSingleThreadScheduledExecutor();
		service.scheduleWithFixedDelay(new Runnable() {
			@Override
			public void run() {
				talktoRandomProcess();
			}
		}, 0, timespan, TimeUnit.SECONDS);
	}

	public int getProcessId() {

		return processId;
	}

	/*
	 * Print buffer, timestamp.
	 */
	public void printBuffer_Timestamp() {
		System.out.print("{");
		for (ProcessTimestamp pt : S) {
			System.out.print(pt.toString());
		}
		System.out.println(V.toString() + "}");
	}
}
