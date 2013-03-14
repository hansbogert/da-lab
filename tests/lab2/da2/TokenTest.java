package da2;

import static org.junit.Assert.assertEquals;

import java.util.LinkedList;
import java.util.Queue;

import org.junit.Test;

import da2.message.Token;

/**
 * The class <code>TokenTest</code> contains tests for the class
 * <code>{@link Token}</code>.
 * 
 * @generatedBy CodePro at 3/11/13 11:01 PM
 * @author Hans
 * @version $Revision: 1.0 $
 */
public class TokenTest {
	
	@Test
	public void updateQueue() {
		Token token = new Token(10);
		int[] rn = {1, 2, 3, 4, 5, 6,7, 8, 9, 10};
		int[] ln = {1, 2, 3, 4, 5, 6, 7,  7, 7, 7};
		
		token.setLN(ln);
		token.getQueue().add(1);
		token.updateQueue(1, rn);
		
		Queue<Integer> expected = new LinkedList<Integer>();
		expected.add(8);
		expected.add(9);
		expected.add(10);
		
		assertEquals("is the diff between RN and LN added?",expected, token.getQueue()); 
	}
	
	@Test
	public void testPrintQueue() {
		Token token = new Token(10);
		
		int[] rn = {1, 2, 3, 4, 5, 6,7, 8, 9, 10};
		int[] ln = {1, 2, 3, 4, 5, 6, 7,  7, 7, 7};
		
		token.setLN(ln);
		token.getQueue().add(1);
		token.updateQueue(1, rn);
		
		
		assertEquals("Printed Queue equals","[8,9,10]", token.printQueue()); 
	}
	
	@Test
	public void testPrintLN() {
		Token token = new Token(10);
		
		int[] rn = {1, 2, 3, 4, 5, 6,7, 8, 9, 10};
		int[] ln = {1, 2, 3, 4, 5, 6, 7,  7, 7, 7};
		
		assertEquals("Printed LN equals","[0,0,0,0,0,0,0,0,0,0]", token.printLN()); 
		token.setLN(ln);
		
		assertEquals("Printed LN equals","[1,2,3,4,5,6,7,7,7,7]", token.printLN()); 
	}
	
	
}