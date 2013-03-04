package da2;

import static org.junit.Assert.*;

import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Vector;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import da2.Process;
import da2.message.Message;
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
	public void testDeliver() {
		try {
			Process p1 = new Process();
			p1.register("localhost");

			Message m = new Message();
			m.payload = "1st!";
			m.vectorClock = p1.vectorClock;
			p1.deliver(m);

			assertEquals((int) p1.vectorClock.values.get(0), 1);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testDeliverBuffer() {
		try {
			Process p1 = new Process();
			p1.register("localhost");
			Process p2 = new Process();
			p2.register("localhost");
			Process p3 = new Process();
			p3.register("localhost");

			p1.send("1st", 3);
			Vector<Integer> result = p1.sentBuffer.get(0).values;
			Vector<Integer> expected = new Vector<Integer>(10);
			expected.add(1);
			for (int i = 0; i < 9; i++) {
				expected.add(0);

			}
			assertEquals(expected, result);

			p1.send("1st", 2);
			p2.send("2nd", 3);
			p3.send("3rd", 1);
			p2.send("4th", 3);
			System.out.println("break");
			// Vector<VectorClock> p3Buffer = p3.sentBuffer

		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testSend() {
		try {
			Process p1 = new Process();
			Process p2 = new Process();

			p1.register("localhost");
			p2.register("localhost");

			assertEquals((int) p1.getProcessId(), 1);
			p1.send("1ss", 2);
			assertEquals(
					(int) p2.getVectorClock().getProcessTimeStamp(
							p2.getProcessId()), 1);

		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Test
	public void testSendWithBuffer() {
		try {
			Process p1 = new Process();
			Process p2 = new Process();
			Process p3 = new Process();

			p1.register("localhost");
			p2.register("localhost");
			p3.register("localhost");

			p1.send("1st", 2);
			assertEquals(p1.sentBuffer.size(), 1);

			p1.send("2nd", 2);
			assertEquals(p1.sentBuffer.size(), 1);
			int result = p1.sentBuffer.get(0).getProcessTimeStamp(1);
			int pid = p1.sentBuffer.get(0).getProcessId();
			// VectorClock expected = new VectorClock();
			// expected.setProcesId(2);
			// expected.incrementAt(1);
			// expected.incrementAt(1);
			assertEquals(result, 2);
			assertEquals(pid, 2);

			p1.send("3rd", 3);
			assertEquals(p1.sentBuffer.size(), 2);
			result = p1.sentBuffer.get(1).getProcessTimeStamp(1);
			int result1 = p1.sentBuffer.get(1).getProcessTimeStamp(2);
			int result2 = p1.sentBuffer.get(1).getProcessTimeStamp(3);

			pid = p1.sentBuffer.get(1).getProcessId();
			assertEquals(result, 3);
			assertEquals(result1, 0);
			assertEquals(result2, 0);

			assertEquals(pid, 3);
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
				p1.register("localhost");
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

			// introduce where this does not hold
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

	@Test
	public void testTransferWithDelay() {
		try {
			System.out.println("are we running?");
			Process p1 = new Process();
			p1.register("localhost");

			Message m = new Message();
			m.setPayload("whatever");

			p1.transfer(m, 4);
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("just for break");
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * We will be testing a whole assignment testcase here, this will be a
	 * trivial case in which we will verify if m1 and m2 (m1 -> m2) will be
	 * received by p2 as (m1 -> m2) even though m1 is late to arrive by
	 * injecting an artificial delay.
	 * 
	 */
	@Test
	public void testSystemTestcaseTrivial2Dependent() {
		try {
			Process p1 = new Process();
			Process p2 = new Process();

			p1.register("localhost");
			p2.register("localhost");
			/**
			 * Message m1 sending
			 */
			Message m1 = new Message();
			m1.setPayload("System testcase: 1st");
			m1.setDelay(200);
			p1.send(m1, 2);
			System.out.println("do we do this?");

			/**
			 * Message m2 sending
			 */
			p1.send("System testcase: 2nd", 2);

			/**
			 * Junit closes the whole VM including still running child
			 * processes, so we have to be sure the main-thread keeps running,
			 * and then make assertions
			 */
			Thread.sleep(300);

			// Assert that the console says the messages in order...

		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * Here we will be testing a system setup with 3 processes, of which p1 and
	 * p2 are sending concurrently (no dependency), to p3, they should be
	 * delivered in order.
	 */
	@Test
	public void testSystemTestcaseTrivial2Concurrent() {
		try {
			Process p1 = new Process();
			Process p2 = new Process();
			Process p3 = new Process();

			p1.register("localhost");
			p2.register("localhost");
			p3.register("localhost");
			/**
			 * Message m1 sending
			 */
			Message m1 = new Message();
			m1.setPayload("System testcase2: 1st, but I'm very slow");
			m1.setDelay(200);
			p1.send(m1, 3);

			/**
			 * Message m2 sending
			 */
			p2.send("System testcase2: 2nd, but I should be faster than m1", 3);

			/**
			 * Junit closes the whole VM including still running child
			 * processes, so we have to be sure the main-thread keeps running,
			 * and then make assertions
			 */
			Thread.sleep(300);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * This is the testcase as in the slides;  doing the following
	 * - p1 sends m1 to p2, slow
	 * - p1 sends m2 to p3 faster than m1
	 * - p3 sends m3 to p2 faster than m1 
	 */
	@Test
	public void testSystemTestcaseAsSlideExample() {
		try {
			Process p1 = new Process();
			Process p2 = new Process();
			Process p3 = new Process();

			p1.register("localhost");
			p2.register("localhost");
			p3.register("localhost");
			/**
			 * Message m1 sending
			 */
			Message m1 = new Message();
			m1.setPayload("System testcase3: 1st, but I'm very slow");
			m1.setDelay(200);
			p1.send(m1, 2);

			/**
			 * Message m2 sending
			 */
			p1.send("System testcase3: 2nd, but I should be faster than m1", 3);
			
			/**
			 * Message m3 sending
			 */
			p3.send("System testcase3: 3nd, but be delivered after m1", 2);

			/**
			 * Junit closes the whole VM including still running child
			 * processes, so we have to be sure the main-thread keeps running,
			 * and then make assertions
			 */
			Thread.sleep(300);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

