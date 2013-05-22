package da3;

import static org.junit.Assert.*;

import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import da3.message.PayloadMessage;

public class SynchronizerTest {
	
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
	public void testSynchronizer2Processes() {
		try {
			//-------------------------------------------------------------------------------//
			//Initialize 3 processes  
			Process p1 = new Process();
			Process p2 = new Process();
			p1.register("localhost");
			p2.register("localhost");
			
			p1.setUpDecisionTree(1, 0);
			p2.setUpDecisionTree(1, 0);
			p1.initByzantineAlgorithm(1, 1);
			p1.initRounds();
			p2.initRounds();
			
			Thread.sleep(10*1000);
			assertEquals("RoundNumber P1: ", 10, p1.getSynchronizer().getRoundId());
			

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}


	@Test
	public void testSynchronizer3Processes() {
		try {
			//-------------------------------------------------------------------------------//
			//Initialize 3 processes  
			Process p1 = new Process();
			Process p2 = new Process();
			Process p3 = new Process();
			p1.register("localhost");
			p2.register("localhost");
			p3.register("localhost");
			
			//PayloadMessage m1 = new PayloadMessage(p1.getSynchronizer().getRoundId(), p1.getProcessId(), 2);
			//p1.send(m1);
			
			p1.setUpDecisionTree(1, 1);
			p2.setUpDecisionTree(1, 1);
			p3.setUpDecisionTree(1, 1);
			p1.initByzantineAlgorithm(1, 1);
			p1.initRounds();
			p2.initRounds();
			p3.initRounds();
			
			Thread.sleep(20*1000);
			assertEquals("RoundNumber P1: ", 10, p1.getSynchronizer().getRoundId());
			

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}
