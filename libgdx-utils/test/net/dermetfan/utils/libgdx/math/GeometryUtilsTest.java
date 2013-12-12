package net.dermetfan.utils.libgdx.math;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import net.dermetfan.utils.ArrayUtils;

import org.junit.Test;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;

public class GeometryUtilsTest {

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
		assertEquals(new Vector2(1, 1), GeometryUtils.rotate(new Vector2(1, 1), Vector2.Zero, 360 * MathUtils.degRad));
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
		Polygon expected = new Polygon(new float[] {0, 0, 1, 1, 1, 0});
		Polygon[] polygons = GeometryUtils.toPolygonArray(new Vector2[] {Vector2.Zero, new Vector2(1, 1), new Vector2(1, 0), Vector2.Zero, new Vector2(1, 1), new Vector2(1, 0), Vector2.Zero, new Vector2(1, 1), new Vector2(1, 0)}, 3);
		float[][] actuals = new float[3][3];
		for(int i = 0; i < polygons.length; i++)
			actuals[i] = polygons[i].getVertices();
		assertArrayEquals(new float[][] {expected.getVertices(), expected.getVertices(), expected.getVertices()}, actuals);
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

}
