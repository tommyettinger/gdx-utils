package net.dermetfan.gdx.math;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class GeometryUtilsTest {

	@Test
	public void size() {
		Array<Vector2> arr = new Array<>(new Vector2[] {new Vector2(-5, -5), new Vector2(5, 5)});
		assertEquals(new Vector2(10, 10), GeometryUtils.size(arr));
	}

	@Test
	public void filterX() {
		assertArrayEquals(new float[] {1, 2, 3}, GeometryUtils.filterX(new Array<>(new Vector2[] {new Vector2(1, 5), new Vector2(2, 5), new Vector2(3, 5)})).toArray(), 0);
	}

	@Test
	public void filterX_array() {
		assertArrayEquals(new float[] {0, 0, 0, 0}, GeometryUtils.filterX(new FloatArray(new float[] {0, 1, 0, 1, 0, 1, 0, 1})).toArray(), 0);
	}

	@Test
	public void filterX3D() {
		assertArrayEquals(new float[] {0, 0, 0, 0}, GeometryUtils.filterX3D(new FloatArray(new float[] {0, 1, 2, 0, 1, 2, 0, 1, 2, 0, 1, 2})).toArray(), 0);
	}

	@Test
	public void filterY() {
		assertArrayEquals(new float[] {1, 2, 3}, GeometryUtils.filterY(new Array<>(new Vector2[] {new Vector2(5, 1), new Vector2(5, 2), new Vector2(5, 3)})).toArray(), 0);
	}

	@Test
	public void filterY_array() {
		assertArrayEquals(new float[] {1f, 1f, 1f, 1f}, GeometryUtils.filterY(new FloatArray(new float[] {0, 1, 0, 1, 0, 1, 0, 1})).toArray(), 0);
	}

	@Test
	public void filterY3D() {
		assertArrayEquals(new float[] {1f, 1f, 1f, 1f}, GeometryUtils.filterY3D(new FloatArray(new float[] {0, 1, 2, 0, 1, 2, 0, 1, 2, 0, 1, 2})).toArray(), 0);
	}

	@Test
	public void filterZ() {
		assertArrayEquals(new float[] {2f, 2f, 2f, 2f}, GeometryUtils.filterZ(new FloatArray(new float[] {0, 1, 2, 0, 1, 2, 0, 1, 2, 0, 1, 2})).toArray(), 0);
	}

	@Test
	public void filterW() {
		assertArrayEquals(new float[] {3f, 3f, 3f, 3f}, GeometryUtils.filterW(new FloatArray(new float[] {0, 1, 2, 3, 0, 1, 2, 3, 0, 1, 2, 3, 0, 1, 2, 3})).toArray(), 0);
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
	}

	@Test
	public void toFloatArray() {
		assertArrayEquals(new float[] {1, 2, 3, 4, 5, 6}, GeometryUtils.toFloatArray(new Array<Vector2>(new Vector2[] {new Vector2(1, 2), new Vector2(3, 4), new Vector2(5, 6)})).toArray(), 0);
	}

	@Test
	public void toVector2Array() {
		assertArrayEquals(new Vector2[] {new Vector2(1, 2), new Vector2(3, 4), new Vector2(5, 6)}, GeometryUtils.toVector2Array(new FloatArray(new float[] {1, 2, 3, 4, 5, 6})).toArray());
	}

	@Test
	public void toPolygonArray() {
		float[] expected = new float[] {0, 0, 1, 1, 1, 0};
		Vector2 zero = new Vector2(), oneOne = new Vector2(1, 1), oneZero = new Vector2(1, 0); // apparently testing with Gradle adds one to the Vector2 constants
		Polygon[] polygons = GeometryUtils.toPolygonArray(new Array<>(new Vector2[] {zero, oneOne, oneZero, zero, oneOne, oneZero, zero, oneOne, oneZero}), 3);
		assertTrue(polygons.length == 3);
		for(Polygon polygon : polygons)
			assertArrayEquals(expected, polygon.getVertices(), 0);
	}

	@Test
	public void areVerticesClockwise() {
		assertEquals(true, GeometryUtils.areVerticesClockwise(new FloatArray(new float[] {0, 0, 1, 1, 1, 0})));
		assertEquals(false, GeometryUtils.areVerticesClockwise(new FloatArray(new float[] {0, 0, 1, 0, 1, 1})));
	}

	@Test
	public void isConvex() {
		assertEquals(false, GeometryUtils.isConvex(new FloatArray(new float[] {0, 0, 0, 1, 1, 1, .5f, .5f, 1, 0}))); // ccw
		assertEquals(true, GeometryUtils.isConvex(new FloatArray(new float[] {0, 0, 1, 0, 1, 1, 0, 1}))); // cw
	}

	@Test
	public void polygonArea() {
		assertEquals(1, GeometryUtils.polygonArea(new FloatArray(new float[] {0, 0, 1, 0, 1, 1, 0, 1})), 0);
	}

	@Test
	public void keepWithin() {
		assertEquals(new Vector2(0, 0), GeometryUtils.keepWithin(5, 5, 5, 5, 0, 0, 5, 5));
	}

	@Test
	public void intersectSegmentPolygon() {
		FloatArray polygon = new FloatArray(new float[] {0, 0, 1, 0, 1, 1, 0, 1});
		Vector2 is1 = new Vector2(), is2 = new Vector2();
		GeometryUtils.intersectSegments(new Vector2(-1, .5f), new Vector2(2, .5f), polygon, is1, is2);
		assertEquals(new Vector2(1, .5f), is1);
		assertEquals(new Vector2(0, .5f), is2);
	}

	@Test
	public void intersectSegments() {
		FloatArray intersections = new FloatArray();
		GeometryUtils.intersectSegments(-1, .5f, 2, .5f, new FloatArray(new float[] {0, 0, 1, 0, 1, 1, 0, 1, 0, 0}), false, intersections);
		assertEquals(4, intersections.size);
		GeometryUtils.intersectSegments(-1, .5f, 2, .5f, new FloatArray(new float[] {0, 0, 1, 0, 1, 1, 0, 1}), true, intersections);
		assertEquals(4, intersections.size);
		assertEquals(1, intersections.get(0), 0);
		assertEquals(.5f, intersections.get(1), 0);
		assertEquals(0, intersections.get(2), 0);
		assertEquals(.5f, intersections.get(3), 0);
	}

}
