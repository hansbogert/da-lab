package da3;


import static org.junit.Assert.*;

import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;

import org.hamcrest.core.Is;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import da3.Process;


public class AlgorithmTest {
	public static final int TESTS = 10;

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
	public void testByzantineAlgorithm5ProcessesNeverSend()
	{
		testByzantineAlgorithm5Processes(false,false,true, 1);
		
	}
	
	@Test
	public void testByzantineAlgorithm5ProcessesFlipSending()
	{
		for (int i = 0; i < TESTS; i++) {
			testByzantineAlgorithm5Processes(true,false,false, 1);
			tearDown();
			setUp();
		}
	}
	
	@Test
	public void testByzantineAlgorithm5ProcessesFlipOrder()
	{
		for (int i = 0; i < TESTS; i++) {
			testByzantineAlgorithm5Processes(false,true,false, 1);
			tearDown();
			setUp();
		}
	}
	
	@Test
	public void testByzantineAlgorithm5ProcessesFlipOrderBigF()
	{
		for (int i = 0; i < TESTS; i++) {
			testByzantineAlgorithm5Processes(false,true,false, 2);
			tearDown();
			setUp();
		}
	}
	
	public void testByzantineAlgorithm5Processes(boolean sendingByCoinFlip, boolean orderByCoinFlip, boolean neverSend, int f) {
		try {
			//-------------------------------------------------------------------------------//
			//Initialize 5 processes
			Process p1 = new Process();
			Process p2 = new Process();
			Process p3 = new Process();
			Process p4 = new Process();
			//Faulty Process which flip coin to decide whether to sent message(first true)
			//, and flip coin to decide whether to manipulate the order(second true).
			FaultyProcess p5 = new FaultyProcess(sendingByCoinFlip, orderByCoinFlip, neverSend);
			p1.register("localhost");
			p2.register("localhost");
			p3.register("localhost");
			p4.register("localhost");
			p5.register("localhost");
			
			p1.setUpDecisionTree(p1.getProcessId(), f);
			p2.setUpDecisionTree(p1.getProcessId(), f);
			p3.setUpDecisionTree(p1.getProcessId(), f);
			p4.setUpDecisionTree(p1.getProcessId(), f);
			p5.setUpDecisionTree(p1.getProcessId(), f);
			
			//process 1 initializes Byzantine agreement algorithm, with order 1, with faulty process 1
			p1.initByzantineAlgorithm(f, 1);
			p1.initRounds();
			p2.initRounds();
			p3.initRounds();
			p4.initRounds();
			p5.initRounds();
			Thread.sleep(2*1000);
			
			assertEquals(p1.getDecisionTree().getMajorityOrder(), Integer.valueOf(1));
			assertEquals(p2.getDecisionTree().getMajorityOrder(), Integer.valueOf(1));
			assertEquals(p3.getDecisionTree().getMajorityOrder(), Integer.valueOf(1));
			assertEquals(p4.getDecisionTree().getMajorityOrder(), Integer.valueOf(1));
			assertEquals(p5.getDecisionTree().getMajorityOrder(), Integer.valueOf(1));
			
			//To see all Byzantine Messages received by p4
			for(ByzantineMessage bMessage : p4.bMessageList)
			{
				System.out.println(bMessage.toString());
			}
			
			//To see the decision tree of p4
			System.out.println(p4.getDecisionTree().toString());
				
			//To see the final decision made by p4
			System.out.println("final order :" + p4.getDecisionTree().getMajorityOrder());
			
			//total messages sent:
			int totalMessagesSent = p1.getMessagesSent() 
					+ p2.getMessagesSent() 
					+ p3.getMessagesSent() 
					+ p4.getMessagesSent()
					+ p5.getMessagesSent();
			int totalMessagesWithheld = p5.getMessagesWithheld();
			System.out.println("Total messages sent: " + totalMessagesSent);
			System.out.println("Total messages withheld: " + totalMessagesWithheld);

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
			FaultyProcess p6 = new FaultyProcess(true, true, false);
			FaultyProcess p7 = new FaultyProcess(true, true, false);
//			Process p6 = new Process();
//			Process p7 = new Process();
			p1.register("localhost");
			p2.register("localhost");
			p3.register("localhost");
			p4.register("localhost");
			p5.register("localhost");
			p6.register("localhost");
			p7.register("localhost");
			
			p1.setUpDecisionTree(p1.getProcessId(), 2);
			p2.setUpDecisionTree(p1.getProcessId(), 2);
			p3.setUpDecisionTree(p1.getProcessId(), 2);
			p4.setUpDecisionTree(p1.getProcessId(), 2);
			p5.setUpDecisionTree(p1.getProcessId(), 2);
			p6.setUpDecisionTree(p1.getProcessId(), 2);
			p7.setUpDecisionTree(p1.getProcessId(), 2);
			
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
			
			assertEquals(p1.getDecisionTree().getMajorityOrder(), Integer.valueOf(1));
			assertEquals(p2.getDecisionTree().getMajorityOrder(), Integer.valueOf(1));
			assertEquals(p3.getDecisionTree().getMajorityOrder(), Integer.valueOf(1));
			assertEquals(p4.getDecisionTree().getMajorityOrder(), Integer.valueOf(1));
			assertEquals(p5.getDecisionTree().getMajorityOrder(), Integer.valueOf(1));
			
			int totalMessagesSent = p1.getMessagesSent() 
					+ p2.getMessagesSent() 
					+ p3.getMessagesSent() 
					+ p4.getMessagesSent()
					+ p5.getMessagesSent()
					+ p6.getMessagesSent()
					+ p7.getMessagesSent();

			int totalMessagesWithheld = p6.getMessagesWithheld()
					+ p7.getMessagesWithheld();
			System.out.println("Total messages sent: " + totalMessagesSent);
			System.out.println("Total messages withheld: " + totalMessagesWithheld);

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
			int fcount = 3;
			int order = 1;
			ArrayList<Process> processes = new ArrayList<Process>();
			
			for(int i = 0; i<processCount-fcount; i++)
			{
				Process p = new Process();
				processes.add(p);
			}
			
			for(int i = 0; i<fcount; i++)
			{
				FaultyProcess p = new FaultyProcess(true, true, false);
				processes.add(p);
			}
			
			for(Process p : processes)
			{
				p.register("localhost");
			}
			
			Process p1 = processes.get(0);
			
			for(Process p : processes)
			{
				p.setUpDecisionTree(p1.getProcessId(), fcount);
			}
			
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

	@Test
	public void testByzantineAlgorithm_AnyProcesses()
	{
		//-------------------------------------------------------------------------------//
		//Initialize 5 processes
		Random r = new Random();
		ArrayList<Process> processList = new ArrayList<Process>();
		ArrayList<Process> processNFList = new ArrayList<Process>();
		ArrayList<Process> processFList = new ArrayList<Process>();
		Process initProcess;
		Integer order = r.nextInt(2);
		Integer non_f = 5;
		Integer f = 2;
		Integer pCount = f + non_f;
		
		for(int i = 0; i<non_f; i++)
		{
			Process p = new Process();
			processList.add(p);
			processNFList.add(p);
		}
		
		for(int i = 0; i< f; i++)
		{
			FaultyProcess fp = new FaultyProcess(true, true, false);
			processList.add(fp);
			processFList.add(fp);
		}
		
		for(Process p : processList)
		{
			p.register("localhost");
		}
		
		initProcess = processList.get(r.nextInt(pCount));//r.nextInt(pCount)
		
		for(Process p : processList)
		{
			p.setUpDecisionTree(initProcess.getProcessId(), f);
		}
		
		
		System.out.println("initProcess: " + initProcess.getProcessId());
		
		for(Process p : processNFList)
		{
			System.out.println("Non-Faulty Process: " + p.getProcessId());
		}
		for(Process p : processFList)
		{
			System.out.println("Faulty Process: " + p.getProcessId());
		}
		
		initProcess.initByzantineAlgorithm(f, order);
		//initProcess must start runs first or last
		initProcess.initRounds();
		for(Process p : processList)
		{
			if(p.getProcessId() != initProcess.getProcessId())
				p.initRounds();
		}
		
		try {
			Thread.sleep(10*1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for(Process p : processList)
		{
			System.out.println("Process " + p.getProcessId() + " Messages In " + p.bMessageList.size());
			for(ByzantineMessage bMessage : p.bMessageList)
			{
				System.out.println(bMessage.toString());
			}
			System.out.println("Process " + p.getProcessId() + " Messages Out " + p.bMessageListOut.size());
			for(ByzantineMessage bMessage : p.bMessageListOut)
			{
				System.out.println(bMessage.toString());
			}
			System.out.println();
		}

		//System.out.println(processNFList.get(1).getDecisionTree().toString());
		
		//System.out.println(p1.getDecisionTree().toString());		
		for(Process p : processNFList)
		{
			
			assertEquals(p.getDecisionTree().getMajorityOrder(), processNFList.get(0).getDecisionTree().getMajorityOrder());
		}
		
		
		System.out.println();
	}
}