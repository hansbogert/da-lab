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
	public void testIncrementAt() {
		VectorClock result = new VectorClock();
		result.initToZeros(10);
		result.incrementAt(1);
		
		assertEquals((int)result.values.get(0), 1);
	}
}