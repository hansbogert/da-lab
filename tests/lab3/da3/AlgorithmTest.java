package da3;


import static org.junit.Assert.*;

import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
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
	
	@Test
	public void testByzantineAlgorithm5Processes() {
		try {
			//-------------------------------------------------------------------------------//
			//Initialize 7 processes
			Process p1 = new Process();
			Process p2 = new Process();
			Process p3 = new Process();
			Process p4 = new Process();
			//Faulty Process which flip coin to decide whether to sent message(first true)
			//, and flip coin to decide whether to manipulate the order(second true).
			FaultyProcess p5 = new FaultyProcess(true, true);
			p1.register("localhost");
			p2.register("localhost");
			p3.register("localhost");
			p4.register("localhost");
			p5.register("localhost");
			
			//process 1 initializes Byzantine agreement algorithm, with order 1, with faulty process 1
			p1.initByzantineAlgorithm(1, 1);
			p1.initRounds();
			p2.initRounds();
			p3.initRounds();
			p4.initRounds();
			p5.initRounds();
			Thread.sleep(10*1000);
			
			assertEquals(p1.getDecisionTree().getMajorityOrder(), 1);
			assertEquals(p2.getDecisionTree().getMajorityOrder(), 1);
			assertEquals(p3.getDecisionTree().getMajorityOrder(), 1);
			assertEquals(p4.getDecisionTree().getMajorityOrder(), 1);
			assertEquals(p5.getDecisionTree().getMajorityOrder(), 1);
			
			//To see all Byzantine Messages received by p4
			for(ByzantineMessage bMessage : p4.bMessageList)
			{
				System.out.println(bMessage.toString());
			}
			
			//To see the decision tree of p4
			System.out.println(p4.getDecisionTree().toString());
			
			//To see the final decision made by p4
			System.out.println("final order :" + p4.getDecisionTree().getMajorityOrder());
			
			System.out.println();
		} catch (InterruptedException e) {
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
	public void testByzantineAlgorithm7Processes() {
		try {
			//-------------------------------------------------------------------------------//
			//Initialize 7 processes
			Process p1 = new Process();
			Process p2 = new Process();
			Process p3 = new Process();
			Process p4 = new Process();
			Process p5 = new Process();
			FaultyProcess p6 = new FaultyProcess(true, true);
			FaultyProcess p7 = new FaultyProcess(true, true);
			p1.register("localhost");
			p2.register("localhost");
			p3.register("localhost");
			p4.register("localhost");
			p5.register("localhost");
			p6.register("localhost");
			p7.register("localhost");
			
			p1.initByzantineAlgorithm(2, 1);
			p1.initRounds();
			p2.initRounds();
			p3.initRounds();
			p4.initRounds();
			p5.initRounds();
			p6.initRounds();
			p7.initRounds();
			
			Thread.sleep(10*1000);
			
			System.out.println();
			
			assertEquals(p1.getDecisionTree().getMajorityOrder(), 1);
			assertEquals(p2.getDecisionTree().getMajorityOrder(), 1);
			assertEquals(p3.getDecisionTree().getMajorityOrder(), 1);
			assertEquals(p4.getDecisionTree().getMajorityOrder(), 1);
			assertEquals(p5.getDecisionTree().getMajorityOrder(), 1);

		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testByzantineAlgorithmNProcesses() {
		try {
			//-------------------------------------------------------------------------------//
			//Initialize 4 processes
			int processCount = 10;
			int fcount = 2;
			int order = 1;
			ArrayList<Process> processes = new ArrayList<Process>();
			
			for(int i = 0; i<processCount-fcount; i++)
			{
				Process p = new Process();
				processes.add(p);
			}
			
			for(int i = 0; i<fcount; i++)
			{
				FaultyProcess p = new FaultyProcess(true, true);
				processes.add(p);
			}
			
			for(Process p : processes)
			{
				p.register("localhost");
			}
			
			Process p1 = processes.get(0);
			p1.initByzantineAlgorithm(fcount, order);
			
			for(Process p : processes)
			{
				p.initRounds();
			}

			
			Thread.sleep(20*1000);
			
			
			
			boolean allNonFaultyProcessAgree = true;
			for(int i = 0; i<processCount-fcount; i++)
			{
				if(processes.get(i).getDecisionTree().getMajorityOrder()!=order)
				{
					allNonFaultyProcessAgree=false;
				}
			}
			assertEquals(allNonFaultyProcessAgree, true);

			System.out.println();
			
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

}