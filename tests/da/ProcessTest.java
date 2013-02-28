package da;

import static org.junit.Assert.*;

import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import message.Message;
import message.VectorClock;

import org.junit.After;
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

	@After
	public void tearDown() {
		try {
			UnicastRemoteObject.unexportObject(registry, true);
		} catch (NoSuchObjectException e) {
			// TODO Auto-generated catch block
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

	@Test
	public void testDeliver(){
		try {
			Process p1 = new Process();
			p1.register("localhost");
			
			Message m = new Message();
			m.payload = "1st!";
			m.vectorClock = p1.vectorClock;
			p1.deliver(m);
			
			assertEquals((int)p1.vectorClock.values.get(0),1);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	public void testSend() {
		try {
			Process p1 = new Process();
			Process p2 = new Process();

			p1.register("localhost");
			p2.register("localhost");

			assertEquals((int)p1.getProcessId(), 1);
			p1.send("1", 2);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Test
	public void testDeliveryPermitted() {
		try {
			Process p1;
			{// make a process which has a vector clock of {1,0, 0, 0, ..}
				p1 = new Process();
			}

			Message m;
			VectorClock vc;
			{// make a message for p1, originating from p2 which will have a
				// vector clock of {0,2,0,0,0,0, ..}
				m = new Message();
				vc = new VectorClock();
				vc.setProcessId(2);
				vc.initToZeros(10);
				m.vectorClock = vc;
			}
			assertTrue(p1.deliveryPermitted(m));
			
			//introduce where this does not hold
			VectorClock bufferVc = new VectorClock();
			bufferVc.initToZeros(10);
			bufferVc.incrementAt(1);
			bufferVc.setProcessId(1);
			
			m.buffer.add(bufferVc);
			
			assertFalse(p1.deliveryPermitted(m));
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
