package net.dermetfan.utils.libgdx.math;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.FloatArray;
import net.dermetfan.utils.ArrayUtils;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class GeometryUtilsTest {

	@Test
	public void invertAxis() {
		assertEquals(5, GeometryUtils.invertAxis(27, 32), 0);
		assertEquals(27, GeometryUtils.invertAxis(5, 32), 0);
		assertEquals(13, GeometryUtils.invertAxis(19, 32), 0);
	}

	@Test
	public void between() {
		Vector2 a = new Vector2(0, 0), b = new Vector2(1, 1);
		assertTrue(GeometryUtils.between(new Vector2(.5f, .5f), a, b));
		assertTrue(GeometryUtils.between(new Vector2(1, 1), a, b, true));
		assertFalse(GeometryUtils.between(new Vector2(1, 1), a, b, false));
		assertFalse(GeometryUtils.between(new Vector2(-.5f, .5f), a, b));
		assertFalse(GeometryUtils.between(new Vector2(.4f, .5f), a, b));
	}

	@Test
	public void size() {
		Vector2[] arr = {new Vector2(-5, -5), new Vector2(5, 5)};
		assertEquals(new Vector2(10, 10), GeometryUtils.size(arr));
	}

	@Test
	public void filterX() {
		assertArrayEquals(new float[] {1, 2, 3}, GeometryUtils.filterX(new Vector2[] {new Vector2(1, 5), new Vector2(2, 5), new Vector2(3, 5)}), 0);
	}

	@Test
	public void filterX_array() {
		assertArrayEquals(ArrayUtils.box(new float[] {0, 0, 0, 0}), ArrayUtils.box(GeometryUtils.filterX(new float[] {0, 1, 0, 1, 0, 1, 0, 1})));
	}

	@Test
	public void filterX3D() {
		assertArrayEquals(ArrayUtils.box(new float[] {0, 0, 0, 0}), ArrayUtils.box(GeometryUtils.filterX3D(new float[] {0, 1, 2, 0, 1, 2, 0, 1, 2, 0, 1, 2})));
	}

	@Test
	public void filterY() {
		assertArrayEquals(new float[] {1, 2, 3}, GeometryUtils.filterY(new Vector2[] {new Vector2(5, 1), new Vector2(5, 2), new Vector2(5, 3)}), 0);
	}

	@Test
	public void filterY_array() {
		assertArrayEquals(new Float[] {1f, 1f, 1f, 1f}, ArrayUtils.box(GeometryUtils.filterY(new float[] {0, 1, 0, 1, 0, 1, 0, 1})));
	}

	@Test
	public void filterY3D() {
		assertArrayEquals(new Float[] {1f, 1f, 1f, 1f}, ArrayUtils.box(GeometryUtils.filterY3D(new float[] {0, 1, 2, 0, 1, 2, 0, 1, 2, 0, 1, 2})));
	}

	@Test
	public void filterZ() {
		assertArrayEquals(new Float[] {2f, 2f, 2f, 2f}, ArrayUtils.box(GeometryUtils.filterZ(new float[] {0, 1, 2, 0, 1, 2, 0, 1, 2, 0, 1, 2})));
	}

	@Test
	public void filterW() {
		assertArrayEquals(new Float[] {3f, 3f, 3f, 3f}, ArrayUtils.box(GeometryUtils.filterW(new float[] {0, 1, 2, 3, 0, 1, 2, 3, 0, 1, 2, 3, 0, 1, 2, 3})));
	}

	@Test
	public void rotate() {
		assertTrue(new Vector2(-1, -1).epsilonEquals(GeometryUtils.rotate(new Vector2(1, 1), new Vector2(0, 0), 180 * MathUtils.degRad), .00001f));
	}

	@Test
	public void rotateLine() {
		Vector2 a = new Vector2(0, -1), b = new Vector2(0, 1);
		GeometryUtils.rotateLine(a, b, -90 * MathUtils.degRad);
		assertTrue(new Vector2(-1, 0).epsilonEquals(a, MathUtils.FLOAT_ROUNDING_ERROR));
		assertTrue(new Vector2(1, 0).epsilonEquals(b, MathUtils.FLOAT_ROUNDING_ERROR));
		GeometryUtils.rotateLine(a.set(-2, -8), b.set(-.1f, 23), -90 * MathUtils.degRad);
		assertTrue(new Vector2(0.5500001f, -4.45f).epsilonEquals(a, MathUtils.FLOAT_ROUNDING_ERROR));
		assertTrue(new Vector2(31.55f, -6.3500013f).epsilonEquals(b, MathUtils.FLOAT_ROUNDING_ERROR));
	}

	@Test
	public void toFloatArray() {
		assertArrayEquals(new float[] {1, 2, 3, 4, 5, 6}, GeometryUtils.toFloatArray(new Vector2[] {new Vector2(1, 2), new Vector2(3, 4), new Vector2(5, 6)}), 0);
	}

	@Test
	public void toVector2Array() {
		assertArrayEquals(new Vector2[] {new Vector2(1, 2), new Vector2(3, 4), new Vector2(5, 6)}, GeometryUtils.toVector2Array(new float[] {1, 2, 3, 4, 5, 6}));
	}

	@Test
	public void toPolygonArray() {
		float[] expected = new float[] {0, 0, 1, 1, 1, 0};
		Vector2 zero = new Vector2(), oneOne = new Vector2(1, 1), oneZero = new Vector2(1, 0); // apparently testing with Gradle adds one to the Vector2 constants
		Polygon[] polygons = GeometryUtils.toPolygonArray(new Vector2[] {zero, oneOne, oneZero, zero, oneOne, oneZero, zero, oneOne, oneZero}, 3);
		assertTrue(polygons.length == 3);
		for(Polygon polygon : polygons)
			assertArrayEquals(expected, polygon.getVertices(), 0);
	}

	@Test
	public void areVerticesClockwise() {
		assertEquals(true, GeometryUtils.areVerticesClockwise(new float[] {0, 0, 1, 1, 1, 0}));
		assertEquals(false, GeometryUtils.areVerticesClockwise(new float[] {0, 0, 1, 0, 1, 1}));
	}

	@Test
	public void isConvex() {
		assertEquals(false, GeometryUtils.isConvex(new float[] {0, 0, 0, 1, 1, 1, .5f, .5f, 1, 0})); // ccw
		assertEquals(true, GeometryUtils.isConvex(new float[] {0, 0, 1, 0, 1, 1, 0, 1})); // cw
	}

	@Test
	public void area() {
		assertEquals(1, GeometryUtils.area(new float[] {0, 0, 1, 0, 1, 1, 0, 1}), 0);
	}

	@Test
	public void keepWithin() {
		assertEquals(new Vector2(0, 0), GeometryUtils.keepWithin(5, 5, 5, 5, 0, 0, 5, 5));
	}

	@Test
	public void intersectSegmentPolygon() {
		float[] polygon = {0, 0, 1, 0, 1, 1, 0, 1};
		Vector2 is1 = new Vector2(), is2 = new Vector2();
		GeometryUtils.intersectSegments(new Vector2(-1, .5f), new Vector2(2, .5f), polygon, is1, is2);
		assertEquals(new Vector2(1, .5f), is1);
		assertEquals(new Vector2(0, .5f), is2);
	}

	@Test
	public void intersectSegments() {
		FloatArray intersections = new FloatArray();
		GeometryUtils.intersectSegments(-1, .5f, 2, .5f, new float[] {0, 0, 1, 0, 1, 1, 0, 1, 0, 0}, false, intersections);
		assertEquals(4, intersections.size);
		GeometryUtils.intersectSegments(-1, .5f, 2, .5f, new float[] {0, 0, 1, 0, 1, 1, 0, 1}, true, intersections);
		assertEquals(4, intersections.size);
		assertEquals(1, intersections.get(0), 0);
		assertEquals(.5f, intersections.get(1), 0);
		assertEquals(0, intersections.get(2), 0);
		assertEquals(.5f, intersections.get(3), 0);
	}

}
