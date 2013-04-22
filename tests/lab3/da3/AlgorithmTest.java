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
import da3.message.MessagePackage;
import da3.message.TextMessage;
import da3.message.Token;
import da3.message.VectorClock;

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

	/*
	 * 	Start p1, p2, p3
		
		<------------ Time 0 TestCase 0
		
		p1 has token
		
		<------------ Time 0 TestCase 1
		
		Time 0ms
		P2 requests token
		p2 works in CS for 0ms
		p2 done
		
		<------------ Time 0 TestCase 2
		
		p1 requests (and working CS for 200ms);
		p3 requests
		
		<------------ Time 0 TestCase 3
		
		Time 100ms
		p2 requests
		
		<------------ Time 150 TestCase 4
		
		Time 200ms
		p1 done, passes token to p2
		p2 works in CS for 200ms
		
		<------------ Time 250 TestCase 5
		
		Time 400ms
		p2 done, passes to p3
		p3 works in CS token for 350ms
		 
		<------------ Time 450 TestCase 6
		
		Time 750
		p3 done.
		
		<------------ Time 850 TestCase 7
	 */
	@Test
	public void testAlgorithm() {
		try {
			//-------------------------------------------------------------------------------//
			//Initialize 3 processes  
			Process p1 = new Process(10);
			Process p2 = new Process(10);
			Process p3 = new Process(10);
			p1.register("localhost");
			p2.register("localhost");
			p3.register("localhost");
			
			
			//No one should have the token
			//After 0 millisecond - Test Case 0
			assertFalse(p1.isTokenPresent());
			assertFalse(p1.isTokenPresent());
			assertFalse(p1.isTokenPresent());
			
			//-------------------------------------------------------------------------------//
			Token token = new Token(10);
			p1.setToken(token);
			//P1 has the token
			//After 0 millisecond - Test Case 1
			assertTrue(p1.isTokenPresent());
			assertEquals("Printed Token LN equals","[0,0,0,0,0,0,0,0,0,0]", token.printLN());
			assertEquals("Printed Token Queue equals","[]", token.printQueue());
			assertEquals("Printed RN equals","[0,0,0,0,0,0,0,0,0,0]", p1.printRN());
			assertEquals("Printed RN equals","[0,0,0,0,0,0,0,0,0,0]", p2.printRN());
			assertEquals("Printed RN equals","[0,0,0,0,0,0,0,0,0,0]", p3.printRN());

			//-------------------------------------------------------------------------------//
			//P2 broadcasts request
			p2.broadcastRequest();
			//After 0 millisecond. - Test Case 2
			Thread.sleep(1);	//Need to wait for very short time, for example 1 ms, as the scheduler will have short delay even if the delay is set to 0;
			//All receives request from p2.
			assertEquals("Printed RN equals","[0,1,0,0,0,0,0,0,0,0]", p1.printRN());
			assertEquals("Printed RN equals","[0,1,0,0,0,0,0,0,0,0]", p2.printRN());
			assertEquals("Printed RN equals","[0,1,0,0,0,0,0,0,0,0]", p3.printRN());
			//Token goes to p2.
			assertFalse(p1.isTokenPresent());
			assertTrue(p2.isTokenPresent());
			assertFalse(p3.isTokenPresent());
			assertEquals("Printed Token LN equals","[0,1,0,0,0,0,0,0,0,0]", p2.getToken().printLN());	
			
			//-------------------------------------------------------------------------------//
			
			//P1 broadcasts request, p1 receives token from itself, but this time p1 will work on the CS for 200 milliseconds
			p1.setCSDelayTime(200);
			p1.broadcastRequest();
			//In the mean time, p2 and p3 broadcasts request, but the request from p2 is 100 millisecond later.
			p2.broadcastRequest(100);
			p3.broadcastRequest();
			p1.broadcastRequest();
			
			//After 0 millisecond. Test Case 3
			//All receives requests from p1 and p3, and previously p2.
			//P1 still have the token, because it is working in CS.
			Thread.sleep(5);	//Need to wait for very short time, for example 1 ms, as the scheduler will have short delay even if the delay is set to 0;
			
			assertTrue(p1.isTokenPresent());
			assertFalse(p2.isTokenPresent());
			assertFalse(p3.isTokenPresent());
			assertEquals("Printed RN equals","[2,1,1,0,0,0,0,0,0,0]", p1.printRN());
			assertEquals("Printed RN equals","[2,1,1,0,0,0,0,0,0,0]", p2.printRN());
			assertEquals("Printed RN equals","[2,1,1,0,0,0,0,0,0,0]", p3.printRN());
			assertEquals("Printed Token LN equals","[0,1,0,0,0,0,0,0,0,0]", p1.getToken().printLN());

			
			//-------------------------------------------------------------------------------//
			
			//After 150 millisecond.  Test Case 4
			//Request from p2 also arrived, all received request from p2 for the second time.
			//P1 still have the token, because it is working in CS.
			//Token is not updated (only request from p2 is granted).
			//P1 still have the token, because it is working in CS.
			Thread.sleep(150);
			assertEquals("Printed RN equals","[2,2,1,0,0,0,0,0,0,0]", p1.printRN());
			assertEquals("Printed RN equals","[2,2,1,0,0,0,0,0,0,0]", p2.printRN());
			assertEquals("Printed RN equals","[2,2,1,0,0,0,0,0,0,0]", p3.printRN());
			assertTrue(p1.isTokenPresent());
			assertFalse(p2.isTokenPresent());
			assertFalse(p3.isTokenPresent());
			assertEquals("Printed Token LN equals","[0,1,0,0,0,0,0,0,0,0]", p1.getToken().printLN());
			assertEquals("Printed Token Quene equals","[1]", p1.getToken().printQueue());
			
			//-------------------------------------------------------------------------------//
			//After 150+100 = 250 millisecond  Test Case 5
			//P1 is done with CS, than it should update the queue in the token.
			//Set CS delay to p2 and p3.
			//P2 received the token.
			//P2 is still working in the CS.
			//P2 can't pass the token to p3 yet.
			p2.setCSDelayTime(200);
			p3.setCSDelayTime(350);
			Thread.sleep(100);
			assertTrue(p2.isTokenPresent());
			assertEquals("Printed Token Queue equals","[2,3]", p2.getToken().printQueue());
			assertEquals("Printed Token RN equals","[2,2,1,0,0,0,0,0,0,0]", p2.printRN());
			assertEquals("Printed Token LN equals","[1,1,0,0,0,0,0,0,0,0]", p2.getToken().printLN());
			
			//-------------------------------------------------------------------------------//
			
			//AFter 250+200 = 450 millisecond  Test Case 6
			//P2 is done
			//P3 has token
			//P3 is working in CS
			Thread.sleep(200);
			assertTrue(p3.isTokenPresent());
			assertEquals("Printed Token Queue equals","[3,1]", p3.getToken().printQueue());
			assertEquals("Printed Token RN equals","[2,2,1,0,0,0,0,0,0,0]", p3.printRN());
			assertEquals("Printed Token LN equals","[1,2,0,0,0,0,0,0,0,0]", p3.getToken().printLN());			
			
			//-------------------------------------------------------------------------------//
			
			//After 450+400 = 850 millisecond  Test Case 7
			//P3 received the token
			//P3 updated the LN on the token.
			//P3 updated the queue on the token. There are no request left in the queue. Queue becomes empty.
			Thread.sleep(400);
			assertEquals("Printed Token Queue equals","[1]", p1.getToken().printQueue());
			assertEquals("Printed Token RN equals","[2,2,1,0,0,0,0,0,0,0]", p1.printRN());
			assertEquals("Printed Token LN equals","[1,2,1,0,0,0,0,0,0,0]", p1.getToken().printLN());
			
			//1050millisecond
			Thread.sleep(200);
			assertEquals("Printed Token Queue equals","[]", p1.getToken().printQueue());
			assertEquals("Printed Token RN equals","[2,2,1,0,0,0,0,0,0,0]", p1.printRN());
			assertEquals("Printed Token LN equals","[2,2,1,0,0,0,0,0,0,0]", p1.getToken().printLN());

			//-------------------------------------------------------------------------------//
			Thread.sleep(1000);
		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	

}