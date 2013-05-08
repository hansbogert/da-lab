package da3;

import static org.junit.Assert.*;

import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Vector;

import org.hamcrest.core.Is;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import da3.Process;

public class ProcessTest {
	Registry registry;

	@Before
	public void setUp() {
		try {

			// If Registry is not started, start it.
			registry = LocateRegistry.createRegistry(1099);

			// Start the process.

		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@After
	public void tearDown() {
		try {
			UnicastRemoteObject.unexportObject(registry, true);
		} catch (NoSuchObjectException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testRegister() {

		try {
			Process p1 = new Process();
			p1.register("localhost");
			Process p2 = new Process();
			p2.register("localhost");

			assertEquals("new process should be 1", 1, p1.getProcessId());
			assertEquals("new process should be 2", 2, p2.getProcessId());

			assertNotNull(registry.lookup(Integer.toString(1)));
			assertNotNull(registry.lookup(Integer.toString(2)));

		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testIsTokenPresent() {

	}

}