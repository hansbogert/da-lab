package da3;


import static org.junit.Assert.*;

import java.rmi.NoSuchObjectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Vector;

import org.hamcrest.core.Is;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import da3.Process;

public class DecisionTreeNodeTest {
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
	public void testDecisionTreeNode() {
		//received order 1 from top commander 1
		ArrayList<Integer> commandersB = new ArrayList<Integer>(Arrays.asList(1));
		ArrayList<Integer> lienntautsB = new ArrayList<Integer>(Arrays.asList(3, 4, 5));
		ByzantineMessage bMessageB = new ByzantineMessage(2, 1, commandersB, 2, lienntautsB);
		
		//received order 0 from commander 1 via 2.
		ArrayList<Integer> commandersC = new ArrayList<Integer>(Arrays.asList(1, 2));
		ArrayList<Integer> lienntautsC = new ArrayList<Integer>(Arrays.asList(4, 5));
		ByzantineMessage bMessageC = new ByzantineMessage(1, 0, commandersC, 3, lienntautsC);
	
		//received order 1 from commander 1 via 3
		ArrayList<Integer> commandersD = new ArrayList<Integer>(Arrays.asList(1, 3));
		ArrayList<Integer> lienntautsD = new ArrayList<Integer>(Arrays.asList(4, 5));
		ByzantineMessage bMessageD = new ByzantineMessage(1, 1, commandersD, 2, lienntautsD);
		
		//received order 0 from commander 1 via 4 via 3, strangely, never received order from 1 via 4
		ArrayList<Integer> commandersE = new ArrayList<Integer>(Arrays.asList(1, 4, 3));
		ArrayList<Integer> lienntautsE = new ArrayList<Integer>(Arrays.asList(5));
		ByzantineMessage bMessageE = new ByzantineMessage(0, 0, commandersE, 2, lienntautsE);
		
		DecisionTreeNode dNodeA = new DecisionTreeNode(1, new ArrayList<Integer>(Arrays.asList(3, 4, 5)), 2, 0);
		dNodeA.addDecision(bMessageB);
		dNodeA.addDecision(bMessageC);
		dNodeA.addDecision(bMessageD);
		dNodeA.addDecision(bMessageE);
		
		int finalOrder = dNodeA.getMajorityOrder();
		
		
		System.out.println(dNodeA);
		System.out.println("finalOrder: " + finalOrder);	
	
		//Rule: ? doesn't count. if(freq(1)=freq(0)) than choose 0
		//Tree = 1->[0,1,?->0] = 1->[0,1,0->0] = 0
		assertEquals("Final order should be 1", 0, finalOrder);
	}

	@Test
	public void testDecisionTreeNode_SendToTopCommander() {
		
		//top commander received order from divine power, in this case, hard-coded by me.
		ArrayList<Integer> commandersA = new ArrayList<Integer>();
		ArrayList<Integer> lienntautsA = new ArrayList<Integer>(Arrays.asList(2, 3, 4, 5));
		ByzantineMessage bMessage = new ByzantineMessage(1, 1, commandersA, 1, lienntautsA);
		
		System.out.println(bMessage);
		
		DecisionTreeNode dNodeA = new DecisionTreeNode(1, new ArrayList<Integer>(Arrays.asList(2, 3, 4, 5)), 2, 0);
		dNodeA.addDecision(bMessage);
		
		int finalOrder = dNodeA.getMajorityOrder();
		
		System.out.println(dNodeA);
		System.out.println("finalOrder: " + finalOrder);
		
		assertEquals("Final order should be 1", 1, finalOrder);
	}
	
	@Test
	public void testDecisionTreeNode_Debug01() {
		
		//top commander received order from divine power, in this case, hard-coded by me.
		ArrayList<Integer> commandersA = new ArrayList<Integer>(Arrays.asList(1));
		ArrayList<Integer> lienntautsA = new ArrayList<Integer>(Arrays.asList(3, 4));
		ByzantineMessage bMessage = new ByzantineMessage(1, 1, commandersA, 2, lienntautsA);
		
		System.out.println(bMessage);
		
		DecisionTreeNode dNodeA = new DecisionTreeNode(1, new ArrayList<Integer>(Arrays.asList(3, 4, 5)), 1, 0);
		dNodeA.addDecision(bMessage);
		
		int finalOrder = dNodeA.getMajorityOrder();
		
		System.out.println(dNodeA);
		System.out.println("finalOrder: " + finalOrder);
		
		assertEquals("Final order should be 1", 1, finalOrder);
	}

}