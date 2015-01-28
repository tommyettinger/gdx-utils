package net.dermetfan.utils.math;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GeometryUtilsTest {

	@Before
	public void before() {
		float[] floats = GeometryUtils.getFloats();
		for(int i = 0; i < floats.length; i++)
			floats[i] = Float.NEGATIVE_INFINITY;
	}

	@Test
	public void between() {
		assertTrue(GeometryUtils.between(.5f, .5f, 0, 0, 1, 1));
		assertTrue(GeometryUtils.between(1, 1, 0, 0, 1, 1, true));
		assertFalse(GeometryUtils.between(1, 1, 0, 0, 1, 1, false));
		assertFalse(GeometryUtils.between(-.5f, .5f, 0, 0, 1, 1));
		assertFalse(GeometryUtils.between(.4f, .5f, 0, 0, 1, 1));
	}

	@Test
	public void width() {
		assertEquals(1, GeometryUtils.width(new float[] {0, 0, 1, 0, 1, 1, 0, 1}), 0);
		assertEquals(1, GeometryUtils.width(new float[] {5, 5, 0, 0, 1, 0, 1, 1, 0, 1, 5, 5}, 2, 8), 0);
	}

	@Test
	public void height() {
		assertEquals(1, GeometryUtils.height(new float[] {0, 0, 1, 0, 1, 1, 0, 1}), 0);
		assertEquals(1, GeometryUtils.height(new float[] {5, 5, 0, 0, 1, 0, 1, 1, 0, 1, 5, 5}, 2, 8), 0);
	}

	@Test
	public void depth() {
		assertEquals(1, GeometryUtils.depth(new float[] {0, 0, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 0, 0, 1, 1, 0, 1, 1, 1, 1, 0, 1, 1}), 0);
		assertEquals(1, GeometryUtils.depth(new float[] {5, 5, 5, 0, 0, 0, 1, 0, 0, 1, 1, 0, 0, 1, 0, 0, 0, 1, 1, 0, 1, 1, 1, 1, 0, 1, 1, 5, 5, 5}, 3, 24), 0);
	}

	@Test
	public void filterX() {
		assertArrayEquals(new float[] {0, 0, 0, 0}, GeometryUtils.filterX(new float[] {0, 1, 0, 1, 0, 1, 0, 1}), 0);
		assertArrayEquals(new float[] {0, 0}, GeometryUtils.filterX(new float[] {0, 1, 0, 1, 0, 1, 0, 1}, 2, 4), 0);
	}

	@Test
	public void filterY() {
		assertArrayEquals(new float[] {1, 1, 1, 1}, GeometryUtils.filterY(new float[] {0, 1, 0, 1, 0, 1, 0, 1}), 0);
		assertArrayEquals(new float[] {1, 1}, GeometryUtils.filterY(new float[] {0, 1, 0, 1, 0, 1, 0, 1}, 2, 4), 0);
	}

	@Test
	public void filterZ() {
		assertArrayEquals(new float[] {0, 0, 0}, GeometryUtils.filterZ(new float[] {1, 1, 0, 1, 1, 0, 1, 1, 0}), 0);
		assertArrayEquals(new float[] {0}, GeometryUtils.filterZ(new float[] {1, 1, 0, 1, 1, 0, 1, 1, 0}, 3, 3), 0);
	}

	@Test
	public void filterW() {
		assertArrayEquals(new float[] {0, 0, 0}, GeometryUtils.filterW(new float[] {1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1, 0}), 0);
		assertArrayEquals(new float[] {0}, GeometryUtils.filterW(new float[] {1, 1, 1, 0, 1, 1, 1, 0, 1, 1, 1, 0}, 4, 4), 0);
	}

	@Test
	public void minX() {
		assertEquals(-1, GeometryUtils.minX(new float[] {-1, 1, 1, 1, 5, 1, 0, 1}), 0);
		assertEquals(1, GeometryUtils.minX(new float[] {-1, 1, 1, 1, 5, 1, 0, 1}, 2, 2), 0);
	}

	@Test
	public void minY() {
		assertEquals(-1, GeometryUtils.minY(new float[] {1, -1, 1, 1, 1, 5, 1, 0}), 0);
		assertEquals(1, GeometryUtils.minY(new float[] {1, -1, 1, 1, 1, 5, 1, 0}, 2, 2), 0);
	}

	@Test
	public void maxX() {
		assertEquals(1, GeometryUtils.maxX(new float[] {0, 5, -1, 5, 1, 5, -2, 5}), 0);
		assertEquals(1, GeometryUtils.maxX(new float[] {0, 5, -1, 5, 1, 5, -2, 5}, 2, 4), 0);
	}

	@Test
	public void maxY() {
		assertEquals(1, GeometryUtils.maxY(new float[] {5, 0, 5, -1, 5, 1, 5, -2}), 0);
		assertEquals(1, GeometryUtils.maxY(new float[] {5, 0, 5, -1, 5, 1, 5, -2}, 2, 4), 0);
	}

	@Test
	public void invertAxis() {
		assertEquals(5, GeometryUtils.invertAxis(27, 32), 0);
		assertEquals(27, GeometryUtils.invertAxis(5, 32), 0);
		assertEquals(13, GeometryUtils.invertAxis(19, 32), 0);
	}

	@Test
	public void invertAxes() {
		assertArrayEquals(new float[] {1, 1, 0, 1, 0, 0, 1, 0}, GeometryUtils.invertAxes(new float[] {0, 0, 1, 0, 1, 1, 0, 1}, true, true), 0);
		assertArrayEquals(new float[] {5, 5, 1, 1, 0, 1, 0, 0, 1, 0, 5, 5}, GeometryUtils.invertAxes(new float[] {5, 5, 0, 0, 1, 0, 1, 1, 0, 1, 5, 5}, 2, 8, true, true), 0);
		assertArrayEquals(new float[] {1, 0, 0, 0, 0, 1, 1, 1}, GeometryUtils.invertAxes(new float[] {0, 0, 1, 0, 1, 1, 0, 1}, true, false), 0);
		assertArrayEquals(new float[] {0, 1, 1, 1, 1, 0, 0, 0}, GeometryUtils.invertAxes(new float[] {0, 0, 1, 0, 1, 1, 0, 1}, false, true), 0);
		assertArrayEquals(new float[] {0, 0, 1, 0, 1, 1, 0, 1}, GeometryUtils.invertAxes(new float[] {0, 0, 1, 0, 1, 1, 0, 1}, false, false), 0);
	}

	@Test
	public void toYDown() {
		assertArrayEquals(new float[] {0, 0, 1, 0, 1, -1, 0, -1}, GeometryUtils.toYDown(new float[] {0, 0, 1, 0, 1, 1, 0, 1}), 0);
		assertArrayEquals(new float[] {5, 5, 0, 0, 1, 0, 1, -1, 0, -1, 5, 5}, GeometryUtils.toYDown(new float[] {5, 5, 0, 0, 1, 0, 1, 1, 0, 1, 5, 5}, 2, 8), 0);
	}

	@Test
	public void toYUp() {
		assertArrayEquals(new float[] {0, 0, 1, 0, 1, 1, 0, 1}, GeometryUtils.toYUp(new float[] {0, 0, 1, 0, 1, -1, 0, -1}), 0);
		assertArrayEquals(new float[] {5, 5, 0, 0, 1, 0, 1, 1, 0, 1, 5, 5}, GeometryUtils.toYUp(new float[] {5, 5, 0, 0, 1, 0, 1, -1, 0, -1, 5, 5}, 2, 8), 0);
	}

}
