package da2;

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

import da2.Process;
import da2.message.MessagePackage;
import da2.message.TextMessage;
import da2.message.Token;
import da2.message.VectorClock;

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
			Process p1 = new Process(10);
			p1.register("localhost");
			Process p2 = new Process(10);
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
		try {
			Process p1 = new Process(10);
			assertFalse(p1.isTokenPresent());

			p1.setToken(new Token(10));
			assertTrue(p1.isTokenPresent());

			p1.removeToken();
			assertFalse(p1.isTokenPresent());
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testUpdateRequestAt() {
		try {
			Process p1 = new Process(10);
			p1.updateRequestAt(1, 1);

			assertEquals(p1.getRequestNoAt(1), 1);
		} catch (RemoteException e) {
			e.printStackTrace();
		}

	}

	@Test
	public void testIncrementRequestAt() {
		try {
			Process p1 = new Process(10);
			p1.updateRequestAt(1, 1);

			assertEquals(p1.getRequestNoAt(1), 1);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testIsTokenBehind() {
		try {
			Process p1 = new Process(10);
			p1.updateRequestAt(1, 1);

			Token token = new Token(10);
			p1.setToken(token);

			assertTrue(p1.isTokenBehind(1));

			// and if not?
			p1.updateRequestAt(1, 0);
			assertFalse(p1.isTokenBehind(1));
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testBroadcastRequestAndRespondToRequest() {

		try {
			Process p1 = new Process(10);
			Process p2 = new Process(10);

			p1.register("localhost");
			p2.register("localhost");

			p1.broadcastRequest();
			assertEquals("Am i added with one", p1.getRequestNoAt(1), 1);

			assertEquals("Is the remote also updated?", p2.getRequestNoAt(1), 1);

		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testRespondToTokenIfTokenIsUpdated() {
		try {
			Process p1 = new Process(10);
			Process p2 = new Process(10);

			p1.register("localhost");
			p2.register("localhost");

			Token token = new Token(10);
			// artificial situation injection, we requested, and got token
			p1.updateRequestAt(1, 1);
			p1.respondToToken(token);

			assertEquals("Is Token updated?", token.getRequestNoAt(1), 1);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void testRespondToTokenIf() {// TODO more tests needed

	}
}