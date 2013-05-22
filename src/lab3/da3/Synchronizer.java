package da3;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import da3.message.Ack;
import da3.message.PayloadMessage;
import da3.message.Safe;

public class Synchronizer extends UnicastRemoteObject implements IHandleRMI {

	private static final long serialVersionUID = 9209021558794481415L;

	private Process process;
	private Registry registry;
	private int roundId;
	private int roundSafety;
	private int roundNeighboursSafety;

	private int currentMessageId;

	private ArrayList<PayloadMessage> bufferedIncomingMessages;
	private ArrayList<Safe> bufferedSafesNextRound;

	private ArrayList<PayloadMessage> unackedMessages;
	private ArrayList<Safe> receivedSafes;

	public boolean synchronizerDiagnotics = false;
	public boolean payloadDiagnotics = false;
	public boolean byzantineDiagnotics = true;

	public Synchronizer(Process process) throws RemoteException {
		this.process = process;
		bufferedIncomingMessages = new ArrayList<PayloadMessage>();
		bufferedSafesNextRound = new ArrayList<Safe>();
		unackedMessages = new ArrayList<PayloadMessage>();
		receivedSafes = new ArrayList<Safe>();
		roundId = 0;
	}

	/*
	 * Generate a unique process id and Register in the RMI Registry.
	 */
	void register(String ip) {
		// Find the RMI Registry.
		try {
			registry = LocateRegistry.getRegistry(ip, 1099);
			// Generate a unique processId.
			process.setProcessId(getMaxProcessID() + 1);
			// Register into the RMI Registry.
			registry.rebind(Integer.toString((getMaxProcessID() + 1)), this);
			if (synchronizerDiagnotics) {
				System.out.println("Process " + process.getProcessId()
						+ " started!!!");
			}
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Return the maximum process ID of the processes binded the registry.
	 */
	public int getMaxProcessID() {
		int maxProcessID = 0;
		String[] remoteProcessIds;
		try {
			remoteProcessIds = registry.list();
			// for all remote processes binded to the registry,
			for (int i = 0; i < remoteProcessIds.length; i++) {
				// overwrite maxProcessID if any processId is greater.
				int processID = Integer.parseInt(remoteProcessIds[i]);
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
	public ArrayList<Integer> getRemoteProcessIds() {
		try {
			String[] processNameStrings = registry.list();
			ArrayList<Integer> remoteProcessIds = new ArrayList<Integer>();
			for (String str : processNameStrings) {
				if (Integer.parseInt(str) != process.getProcessId()) {
					remoteProcessIds.add(Integer.parseInt(str));
				}
			}
			return remoteProcessIds;

		} catch (RemoteException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void send(PayloadMessage pMessage) {
		try {
			currentMessageId++;
			pMessage.setMessageId(currentMessageId);
			IHandleRMI remoteSynchronizer = (IHandleRMI) registry
					.lookup(Integer.toString(pMessage.getReceiveProcessId()));
			if (synchronizerDiagnotics | payloadDiagnotics) {
				System.out.println("Process " + process.getProcessId()
						+ " tries to send :" + pMessage.toString());
			}
			remoteSynchronizer.transfer(pMessage);
			unackedMessages.add(pMessage);
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
	}

	public synchronized void receive(PayloadMessage pMessage) {
		if (synchronizerDiagnotics | payloadDiagnotics) {
			System.out.println("Process " + process.getProcessId()
					+ " receives :" + pMessage.toString() + " at round "
					+ getRoundId());
		}

		if (pMessage.getRoundId() == roundId) {
			if (payloadDiagnotics) {
				System.out.println("Process " + process.getProcessId()
						+ " delivers :" + pMessage.toString() + " directly"
						+ " at round " + getRoundId());
			}
			process.receive(pMessage);
		} else {
			if (payloadDiagnotics) {
				System.out.println("Process " + process.getProcessId()
						+ " buffers :" + pMessage.toString() + " at round "
						+ getRoundId());
			}
			bufferedIncomingMessages.add(pMessage);
		}
		Ack ack = new Ack(roundId, process.getProcessId(),
				pMessage.getSentProcessId(), pMessage.getMessageId());
		send(ack);
	}

	public synchronized void receive(Ack ack) {
		if (payloadDiagnotics) {
			System.out.println("Process " + process.getProcessId()
					+ " receives :" + ack.toString());
		}

		for (int i = 0; i < unackedMessages.size(); i++) {
			if (unackedMessages.get(i).getMessageId() == ack.getMessageId()) {
				unackedMessages.remove(i);
			}
		}

		regulateSafety("Received a Ack");

	}

	public void send(Ack ack) {
		try {
			IHandleRMI remoteSynchronizer = (IHandleRMI) registry
					.lookup(Integer.toString(ack.getReceiveProcessId()));
			remoteSynchronizer.transfer(ack);
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
	}

	public synchronized void receive(Safe safe) {
		if (synchronizerDiagnotics) {
			System.out.println("Process " + process.getProcessId()
					+ " receives :" + safe.toString());
		}

		if (safe.getRoundId() > getRoundId()) {
			bufferedSafesNextRound.add(safe);
		} else {
			receivedSafes.add(safe);
		}

		regulateProgress("safe retrieved");

	}

	public synchronized void regulateSafety(String reason) {
		if (synchronizerDiagnotics) {
			System.out.println("Process " + process.getProcessId()
					+ " regulate safety at Round " + roundId + " because of "
					+ reason);
		}

		if (isSafe()) {
			if (payloadDiagnotics) {
				for (PayloadMessage un : unackedMessages)
					System.out.println("Process " + process.getProcessId()
							+ " unackedMessageNo " + un.getMessageId());
			}
			if (getRoundSafety() < getRoundId()) {
				if (synchronizerDiagnotics) {
					System.out.println("Process " + process.getProcessId()
							+ " is safe at Round " + roundId);
				}
				setRoundSafety(getRoundId());
				broadcastSafe();
			}
		} else {
			if (synchronizerDiagnotics) {
				System.out.println("Process " + process.getProcessId()
						+ " is not yet safe at Round " + roundId);
			}
		}
	}

	public void broadcastSafe() {
		try {
			ArrayList<Integer> neighbourIds= getRemoteProcessIds();
			for(Integer i : neighbourIds)
			{
				IHandleRMI remoteSynchronizer = (IHandleRMI) registry.lookup(Integer.toString(i));
				Safe safe = new Safe(roundId, process.getProcessId(), i);
				if(synchronizerDiagnotics){System.out.println("Process " + process.getProcessId() + " tries to send :" + safe.toString());}
				remoteSynchronizer.transfer(safe);
			}

		} catch (RemoteException e) {
			e.printStackTrace();
		}
		catch (NotBoundException e) {
			e.printStackTrace();
		}
	}

	public boolean isSafe() {
		boolean isSafe = true;

		if (process.isAllMessagesSent() != true) {
			isSafe = false;
		}

		for (PayloadMessage uMessage : unackedMessages) {
			if (uMessage.getRoundId() <= getRoundId()) {
				isSafe = false;
			}
		}
		return isSafe;
	}

	public boolean areAllNeighboursSafe() {
		boolean areAllNeighboursSafe = true;

		ArrayList<Integer> neighbourIds = getRemoteProcessIds();

		int safeCount = 0;
		for (Safe safe : receivedSafes) {
			if (safe.getRoundId() == getRoundId()) {
				safeCount++;
			}
		}

		if (safeCount < neighbourIds.size()) {
			areAllNeighboursSafe = false;
		}

		return areAllNeighboursSafe;
	}

	public synchronized void regulateProgress(String reason) {
		if (synchronizerDiagnotics) {
			System.out.println("Process " + process.getProcessId()
					+ " regulate progress at round " + getRoundId()
					+ " because of " + reason);
		}
		if (canProgress()) {
			if (synchronizerDiagnotics | payloadDiagnotics) {
				System.out.println("Process " + process.getProcessId()
						+ " progress to round " + (getRoundId() + 1));
			}
			progressToNextRound();
		}
	}

	public boolean canProgress() {
		boolean canProgress = true;

		// if not safe, can't progress.
		if (getRoundSafety() < getRoundId()) {
			canProgress = false;
			if (synchronizerDiagnotics) {
				System.out.println("Process " + process.getProcessId()
						+ " stays at round " + getRoundId()
						+ " because of self not safe. roundSafety ="
						+ getRoundSafety());
			}

		}

		// if not received safe from all neighbours, can't progress
		if (getRoundNeighboursSafety() < getRoundId()) {
			if (!areAllNeighboursSafe()) {
				canProgress = false;
				if (synchronizerDiagnotics) {
					System.out
							.println("Process "
									+ process.getProcessId()
									+ " stays at round "
									+ getRoundId()
									+ " because of neighbours not safe. roundNeighoursSafety ="
									+ getRoundNeighboursSafety()
									+ " receivedSafes=" + receivedSafes.size());
				}
			} else {
				setRoundNeighboursSafety(getRoundId());
			}
		}

		// TODO Just to stop the process after certain rounds
		if (roundId >= 10) {
			canProgress = false;
			if (synchronizerDiagnotics) {
				System.out.println("Process " + process.getProcessId()
						+ " stays at round " + getRoundId()
						+ " because of round limitation");
			}

		}

		return canProgress;
	}

	public synchronized void progressToNextRound() {

		setRoundId(getRoundId() + 1);
		process.progressToNextRound();
		// unbox buffered safes which are meant for this round but retrieved at
		// previous round
		// if(synchronizerDiagnotics){System.out.println("Process " +
		// process.getProcessId() + " before receivedSafes.Size=" +
		// receivedSafes.size());}
		receivedSafes = bufferedSafesNextRound;
		// if(synchronizerDiagnotics){System.out.println("Process " +
		// process.getProcessId() + " after receivedSafes.Size=" +
		// receivedSafes.size());}

		bufferedSafesNextRound = new ArrayList<Safe>();

		// unbox buffered messages which are meant for this round but retrieved
		// at previous round
		for (PayloadMessage pMessage : bufferedIncomingMessages) {
			if (pMessage.getRoundId() == roundId) {
				if (payloadDiagnotics) {
					System.out.println("Process " + process.getProcessId()
							+ " delivers :" + pMessage.toString()
							+ "from buffer" + " at round " + getRoundId());
				}
				process.receive(pMessage);
			}
		}
		bufferedIncomingMessages.clear();
	}

	public Registry getRegistry() {
		return registry;
	}

	public void setRegistry(Registry registry) {
		this.registry = registry;
	}

	/*
	 * When other remote processes call the transfer function, this process will
	 * receive the parameter as the message.
	 */
	public void transfer(PayloadMessage pMessage) throws RemoteException {

		final PayloadMessage finalpMessage = pMessage;
		int delay = (getRoundId() == 0) ? 0 : 0; // nasty problem that if you
													// are still in round 0,
													// than receiving payload
													// message is a problem.
		final ScheduledExecutorService service = Executors
				.newSingleThreadScheduledExecutor();
		service.schedule(new Runnable() {
			@Override
			public void run() {
				receive(finalpMessage);
			}
		}, delay, TimeUnit.MILLISECONDS);
	}

	/*
	 * When other remote processes call the transfer function, this process will
	 * receive the parameter as the message.
	 */
	public void transfer(Ack ack) throws RemoteException {
		final Ack finalAck = ack;
		final ExecutorService service = Executors
				.newSingleThreadExecutor();
		service.execute(new Runnable() {
			@Override
			public void run() {
				receive(finalAck);
			}
		});
	}

	/*
	 * When other remote processes call the transfer function, this process will
	 * receive the parameter as the message.
	 */
	public void transfer(Safe safe) throws RemoteException {
		final Safe finalSafe = safe;
		final ExecutorService service = Executors
				.newSingleThreadExecutor();
		service.execute(new Runnable() {
			@Override
			public void run() {
				receive(finalSafe);
			}
		});
	}

	public int getRoundId() {
		return roundId;
	}

	public void setRoundId(int roundId) {
		this.roundId = roundId;
	}

	public int getRoundSafety() {
		return roundSafety;
	}

	public void setRoundSafety(int roundSafety) {
		this.roundSafety = roundSafety;
	}

	public int getRoundNeighboursSafety() {
		return roundNeighboursSafety;
	}

	public void setRoundNeighboursSafety(int roundNeighboursSafety) {
		this.roundNeighboursSafety = roundNeighboursSafety;
	}
}
