package da3;


import static org.junit.Assert.*;

import java.rmi.AccessException;
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


public class MultiJVMTest {

	Registry registry;
	
	@Test
	public void testByzantineAlgorithm_MultiJVM()
	{
		//Assuming 5 Process start already.
		try {
			
		ArrayList<Integer> neighbours = new ArrayList<Integer>();
		neighbours = getRemoteProcessIds();
		System.out.println(neighbours.toString());
		
		
		int initPid = 1;
		IHandleRMI initRemoteSynchronizer = (IHandleRMI) registry.lookup(Integer.toString(initPid));


		int f = 1;
		int order = 1;
		
		//Build Tree
		for(Integer p : neighbours)
		{
				IHandleRMI remoteSynchronizer = (IHandleRMI) registry.lookup(Integer.toString(p));
				remoteSynchronizer.setUpDecisionTree(initPid, f);
		}
		
		//Init Byzantine A.
		initRemoteSynchronizer.initByzantineAlgorithm(f, order);

		//Init Rounds
		initRemoteSynchronizer.initRounds();
		for(Integer p : neighbours)
		{
			if(p != initPid)
			{
				IHandleRMI remoteSynchronizer = (IHandleRMI) registry.lookup(Integer.toString(p));
				remoteSynchronizer.initRounds();
			}

		}
		
		try {
			Thread.sleep(5*1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		for(Integer p : neighbours)
		{
				IHandleRMI remoteSynchronizer = (IHandleRMI) registry.lookup(Integer.toString(p));
				int m = remoteSynchronizer.getMajority();
				System.out.println("Process " + p + " decide on " + m);
		}
		
		} catch (AccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public ArrayList<Integer> getRemoteProcessIds() {
		try {
			registry = LocateRegistry.getRegistry("localhost", 1099);
			String[] processNameStrings = registry.list();
			ArrayList<Integer> remoteProcessIds = new ArrayList<Integer>();
			for (String str : processNameStrings) {
					remoteProcessIds.add(Integer.parseInt(str));
			}
			return remoteProcessIds;

		} catch (RemoteException e) {
			e.printStackTrace();
			return null;
		}
	}
}