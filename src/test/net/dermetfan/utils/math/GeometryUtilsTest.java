package net.dermetfan.utils.math;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GeometryUtilsTest {

	@Test
	public void between() {
		assertTrue(GeometryUtils.between(.5f, .5f, 0, 0, 1, 1));
		assertTrue(GeometryUtils.between(1, 1, 0, 0, 1, 1, true));
		assertFalse(GeometryUtils.between(1, 1, 0, 0, 1, 1, false));
		assertFalse(GeometryUtils.between(-.5f, .5f, 0, 0, 1, 1));
		assertFalse(GeometryUtils.between(.4f, .5f, 0, 0, 1, 1));
	}

	@Test
	public void invertAxis() {
		assertEquals(5, GeometryUtils.invertAxis(27, 32), 0);
		assertEquals(27, GeometryUtils.invertAxis(5, 32), 0);
		assertEquals(13, GeometryUtils.invertAxis(19, 32), 0);
	}

}
