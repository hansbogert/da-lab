package da1;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/*
 * Start a process.
 * Check if the registry already started.
 * If not, start the registry first.
 */
public class StartProcess {

	Process process;
	String registryIP;
	Registry registry;
	Boolean isRegistryStarted;

	public static void main(String[] args) {

		StartProcess s = new StartProcess();
		s.start();
	}

	/*
	 * Start a process. Check if the registry already started. If not, start the
	 * registry first.
	 */
	public void start() {

		// Set the ip addres of the registry.
		registryIP = "localhost";
		// Check if the registry is started.
		isRegistryStarted = isRegistryStarted(registryIP);

		// print Registry status
		String registryStatus = (isRegistryStarted) ? "Registry started."
				: "Starting registry";
		System.out.println(registryStatus);

		try {

			// If Registry is not started, start it.
			if (!isRegistryStarted) {
				registry = LocateRegistry.createRegistry(1099);
			}

			// Start the process.
			startProcess(registryIP);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	/*
	 * Start the process.
	 */
	public void startProcess(String registryIP) throws RemoteException {
		process = new Process();
		process.register(registryIP);
		process.broadcastRepeatedly(20);
		process.talktoRandomProcessRepeatedly(10);
	}

	/*
	 * Check if a registry is already started.
	 */
	public boolean isRegistryStarted(String registryIP) {
		boolean isStarted = false;

		try {
			registry = LocateRegistry.getRegistry(registryIP, 1099);
			if (!registry.list().equals(null)) {
				isStarted = true;
			}

		} catch (ConnectException e) {
			System.out.println("RMI probably not already started.");
		} catch (RemoteException e) {
			e.printStackTrace();
		}
		return isStarted;
	}
}
