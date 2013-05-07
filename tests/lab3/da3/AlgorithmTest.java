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

public class AlgorithmTest {
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
	public void testByzantineAlgorithm6Processes() {
		try {
			//-------------------------------------------------------------------------------//
			//Initialize 6 processes
			
			Process p1 = new Process();
			Process p2 = new Process();
			Process p3 = new Process();
			Process p4 = new Process();
			Process p5 = new Process();
			Process p6 = new Process();
			p1.register("localhost");
			p2.register("localhost");
			p3.register("localhost");
			p4.register("localhost");
			p5.register("localhost");
			p6.register("localhost");
			
			p3.initByzantineAlgorithm(2, 1);
			p1.initRounds();
			p2.initRounds();
			p3.initRounds();
			p4.initRounds();
			p5.initRounds();
			p6.initRounds();
			
			Thread.sleep(10*1000);
			
			for(ByzantineMessage bMessage : p2.bMessageList)
			{
				System.out.println(bMessage.toString());
			}

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	

}