package message;

import java.util.Vector;
import org.junit.*;
import static org.junit.Assert.*;

/**
 * The class <code>VectorClockTest</code> contains tests for the class
 * <code>{@link VectorClock}</code>.
 * 
 * @author hans
 * @version $Revision: 1.0 $
 */
public class VectorClockTest {

	/**
	 * Run the boolean isGreaterOrEqual(VectorClock) method test.
	 * 
	 * @throws Exception
	 * 
	 */
	@Test
	public void testIsGreaterOrEqual() throws Exception {
		VectorClock greaterVc = new VectorClock();
		greaterVc.setProcessId(1);
		greaterVc.initToZeros(10);
		greaterVc.values.add(0, 1);

		VectorClock smallerOrEqualVc = new VectorClock();
		smallerOrEqualVc.setProcessId(2);
		smallerOrEqualVc.initToZeros(10);
		smallerOrEqualVc.values.add(0, 1);

		assertTrue(greaterVc.isGreaterOrEqual(smallerOrEqualVc));

		smallerOrEqualVc.initToZeros(10);
		smallerOrEqualVc.values.add(0, 2);

		assertFalse(greaterVc.isGreaterOrEqual(smallerOrEqualVc));

	}
	
	@Test
	public void testMergeWith()
	{
		VectorClock v1 = new VectorClock();
		v1.initToZeros(10);
		VectorClock v2 = new VectorClock();
		v2.initToZeros(10);
		
		v1.incrementAt(1);
		v1.incrementAt(2);
		v1.incrementAt(2);
		
		v2.incrementAt(1);
		v2.incrementAt(1);
		v2.incrementAt(2);
		
		v1.mergeWith(v2);
		
		assertEquals(v1.getProcessTimeStamp(1), 2);
		assertEquals(v1.getProcessTimeStamp(2), 2);
		assertEquals(v1.getProcessTimeStamp(3), 0);
		
		assertEquals(v2.getProcessTimeStamp(1), 2);
		assertEquals(v2.getProcessTimeStamp(2), 1);
		assertEquals(v2.getProcessTimeStamp(3), 0);
	}

	@Test
	public void testIncrementAt() {
		VectorClock result = new VectorClock();
		result.initToZeros(10);
		result.incrementAt(1);
		
		assertEquals((int)result.values.get(0), 1);
	}
}