package net.dermetfan.utils.math;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

public class MathUtilsTest {

	@Test
	public void crossSum() {
		assertEquals(0, MathUtils.crossSum(0));
		assertEquals(1, MathUtils.crossSum(1));
		assertEquals(3, MathUtils.crossSum(3));
		assertEquals(1, MathUtils.crossSum(10));
		assertEquals(10, MathUtils.crossSum(55));
		assertEquals(19, MathUtils.crossSum(793));
	}

	@Test
	public void factorial() {
		assertEquals(1, MathUtils.factorial(0));
		assertEquals(1, MathUtils.factorial(1));
		assertEquals(120, MathUtils.factorial(5));
		assertEquals(1, MathUtils.factorial(0f), 0);
		assertEquals(1, MathUtils.factorial(.5f), 0);
		assertEquals(1, MathUtils.factorial(1f), 0);
		assertEquals(120, MathUtils.factorial(5f), 0);
		assertEquals(1.25f, MathUtils.factorial(1.25f), 0);
		assertEquals(9.140625f, MathUtils.factorial(3.25f), 0);
	}

	@Test
	public void between() {
		assertTrue(MathUtils.between(1, 0, 2, false));
		assertFalse(MathUtils.between(0, 1, 2, false));
		assertTrue(MathUtils.between(1, 0, 2, true));
		assertTrue(MathUtils.between(1, 1, 2, true));
		assertFalse(MathUtils.between(0, 1, 2, true));
		assertTrue(MathUtils.between(0, 0, 2, true));
	}

	@Test
	public void approachZero() {
		assertEquals(.5f, MathUtils.approachZero(1, .5f), 0);
		assertEquals(.5f, MathUtils.approachZero(1, -.5f), 0);
		assertEquals(0, MathUtils.approachZero(0, 1), 0);
		assertEquals(0, MathUtils.approachZero(0, -1), 0);
		assertEquals(0, MathUtils.approachZero(1, 2), 0);
		assertEquals(0, MathUtils.approachZero(1, 2), 0);
		assertEquals(0, MathUtils.approachZero(-1, 2), 0);
		assertEquals(0, MathUtils.approachZero(-1, -2), 0);
	}

	@Test
	public void mirror() {
		assertEquals(0, MathUtils.mirror(100, 50), 0);
		assertEquals(25, MathUtils.mirror(75, 50), 0);
		assertEquals(50, MathUtils.mirror(50, 50), 0);
		assertEquals(75, MathUtils.mirror(25, 50), 0);
		assertEquals(100, MathUtils.mirror(0, 50), 0);

		assertEquals(0, MathUtils.mirror(-100, -50), 0);
		assertEquals(-25, MathUtils.mirror(-75, -50), 0);
		assertEquals(-50, MathUtils.mirror(-50, -50), 0);
		assertEquals(-75, MathUtils.mirror(-25, -50), 0);
		assertEquals(-100, MathUtils.mirror(0, -50), 0);

		assertEquals(25, MathUtils.mirror(175, 100), 0);
		assertEquals(-75, MathUtils.mirror(275, 100), 0);

		assertEquals(-25, MathUtils.mirror(25, 0), 0);
		assertEquals(25, MathUtils.mirror(-25, 0), 0);

		assertEquals(200, MathUtils.mirror(0, 100), 0);
		assertEquals(0, MathUtils.mirror(200, 100), 0);
	}

	@Test
	public void normalize() {
		assertEquals(50, MathUtils.normalize(150, 100), 0);
		assertEquals(25, MathUtils.normalize(125, 25), 0);
		assertEquals(25, MathUtils.normalize(125, 50), 0);
		assertEquals(25, MathUtils.normalize(125, 100), 0);
		assertEquals(25, MathUtils.normalize(25, 100), 0);

		assertEquals(-25, MathUtils.normalize(25, -100), 0);
		assertEquals(75, MathUtils.normalize(-25, 100), 0);
		assertEquals(-25, MathUtils.normalize(-25, -100), 0);
		assertEquals(-25, MathUtils.normalize(125, -100), 0);
		assertEquals(75, MathUtils.normalize(-125, 100), 0);
		assertEquals(-75, MathUtils.normalize(-125, -100), 0);
		assertEquals(-25, MathUtils.normalize(225, -100), 0);
		assertEquals(75, MathUtils.normalize(-225, 100), 0);
		assertEquals(-75, MathUtils.normalize(-225, -100), 0);

		assertEquals(0, MathUtils.normalize(0, -100), 0);
		assertEquals(0, MathUtils.normalize(0, 100), 0);

		assertEquals(0, MathUtils.normalize(0, 0), 0);
		assertEquals(0, MathUtils.normalize(57, 0), 0);
		assertEquals(0, MathUtils.normalize(-57, 0), 0);

		assertEquals(0, MathUtils.normalize(200, -100, 100), 0);
		assertEquals(5, MathUtils.normalize(205, -100, 100), 0);

		assertEquals(25, MathUtils.normalize(25, -100, 100), 0);
		assertEquals(125, MathUtils.normalize(25, 100, 200), 0);
		assertEquals(0, MathUtils.normalize(0, -100, 100), 0);
		assertEquals(125, MathUtils.normalize(125, 100, 200), 0);
		assertEquals(100, MathUtils.normalize(100, 100, 100), 0);
		assertEquals(100, MathUtils.normalize(25, 100, 100), 0);
		assertEquals(-100, MathUtils.normalize(25, -100, -100), 0);

		assertEquals(90, MathUtils.normalize(-360 + 90, 360), 0);
		assertEquals(-90, MathUtils.normalize(360 + 90, -360), 0);
		assertEquals(-com.badlogic.gdx.math.MathUtils.PI, MathUtils.normalize(-com.badlogic.gdx.math.MathUtils.PI2 * 2.5f, -com.badlogic.gdx.math.MathUtils.PI2), com.badlogic.gdx.math.MathUtils.PI / 20f);

		assertEquals(150, MathUtils.normalize(150, 100, 250), 0);
		assertEquals(250, MathUtils.normalize(250, 100, 250), 0);
		assertEquals(150, MathUtils.normalize(300, 100, 250), 0);
		assertEquals(-50, MathUtils.normalize(-50, -100, 250), 0);
	}

	@Test
	public void nearest() {
		float[] values = {-32.1f, -53424.23f, 83232, -1, 20, 1.1f, 0};
		assertEquals(1.1f, MathUtils.nearest(3, values, 0, values.length), 0);
		assertEquals(83232, MathUtils.nearest(Float.POSITIVE_INFINITY, values, 0, values.length), 0);
		assertEquals(-53424.23f, MathUtils.nearest(Float.NEGATIVE_INFINITY, values, 100, 0, values.length), 0);
		assertEquals(180, MathUtils.nearest(100, new float[] {-300, 200, 180}, 100, 0, 3), 0);
		assertTrue(Float.isNaN(MathUtils.nearest(0, new float[] {-300, 200, 180}, 100, 0, 3)));
	}

	@Test
	public void amplitude() {
		float[] values = {-5, 1, 3, 2, 4, 1, -4, 3.2f, 5};
		assertEquals(10, MathUtils.amplitude(values, 0, values.length), 0);
	}

	@Test
	public void scale() {
		float[] values = {-50, -25, 0, 25, 50};
		assertEquals(1, MathUtils.amplitude(MathUtils.scale(values, 0, 1, 0, values.length), 0, values.length), 0);

		// float rounding error
		float min = 0, max = Float.MAX_VALUE / 3;
		assertNotEquals(Math.abs(max - min), MathUtils.amplitude(MathUtils.scale(values, min, max, 0, values.length), 0, values.length), com.badlogic.gdx.math.MathUtils.FLOAT_ROUNDING_ERROR);
		assertEquals(Math.abs(max - min), MathUtils.amplitude(MathUtils.clamp(values, min, max, 0, values.length), 0, values.length), 0); // clamped amplitude must be correct
	}

	@Test
	public void elementAtSum() {
		assertEquals("2", MathUtils.elementAtSum(5, new float[] {1, 2, 3, 4, 5}, new String[] {"0", "1", "2", "3", "4", "5"}));
	}

	@Test
	public void abs() {
		assertArrayEquals(new float[] {1, 1, 0}, MathUtils.abs(new float[] {1, -1, 0}, 0, 3), 0);
	}

	@Test
	public void add() {
		assertArrayEquals(new float[] {0, 1, 2}, MathUtils.add(new float[] {-1, 0, 1}, 1, 0, 3), 0);
	}

	@Test
	public void sub() {
		assertArrayEquals(new float[] {0, 1, 2}, MathUtils.sub(new float[] {1, 2, 3}, 1, 0, 3), 0);
	}

	@Test
	public void mul() {
		assertArrayEquals(new float[] {0, 1, 2}, MathUtils.mul(new float[] {0, .5f, 1}, 2, 0, 3), 0);
	}

	@Test
	public void div() {
		assertArrayEquals(new float[] {0, 1, 2}, MathUtils.div(new float[] {0, 2, 4}, 2, 0, 3), 0);
	}

}
