package da;

import static org.junit.Assert.*;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.junit.Before;
import org.junit.Test;

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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/*public void testSend() {
		try {
			Process p1 = new Process();
			Process p2 = new Process();

			p1.register("localhost");
			p2.register("localhost");

			p1.send("1", 2);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}*/

}
