/** Copyright 2014 Robin Stumm (serverkorken@gmail.com, http://dermetfan.net)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License. */

package net.dermetfan.gdx.math;

import java.util.Comparator;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.Ellipse;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Polyline;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Shape2D;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.ShortArray;
import net.dermetfan.gdx.utils.ArrayUtils;

import static net.dermetfan.gdx.math.MathUtils.amplitude2;
import static net.dermetfan.gdx.math.MathUtils.max;
import static net.dermetfan.gdx.math.MathUtils.min;
import static net.dermetfan.utils.math.MathUtils.det;

/** Provides some useful methods for geometric calculations. Note that many methods return the same array instance so make a copy for subsequent calls.
 *  @author dermetfan */
public class GeometryUtils extends net.dermetfan.utils.math.GeometryUtils {

	/** a {@link Vector2} for temporary usage */
	private static final Vector2 vec2_0 = new Vector2(), vec2_1 = new Vector2();

	/** a temporarily used array, returned by some methods */
	private static Array<Vector2> tmpVector2Array = new Array<>();

	/** a temporarily used array, returned by some methods */
	private static final FloatArray tmpFloatArray = new FloatArray();

	/** @see net.dermetfan.utils.math.GeometryUtils#between(float, float, float, float, float, float, boolean) */
	public static boolean between(Vector2 point, Vector2 a, Vector2 b, boolean inclusive) {
		return between(point.x, point.y, a.x, a.y, b.x, b.y, inclusive);
	}

	/** @see net.dermetfan.utils.math.GeometryUtils#between(float, float, float, float, float, float) */
	public static boolean between(Vector2 point, Vector2 a, Vector2 b) {
		return between(point.x, point.y, a.x, a.y, b.x, b.y);
	}

	/** @param vector the {@link Vector2} which components to set to their absolute value
	 *  @return the given vector with all components set to its absolute value
	 *  @see Math#abs(float) */
	public static Vector2 abs(Vector2 vector) {
		vector.x = Math.abs(vector.x);
		vector.y = Math.abs(vector.y);
		return vector;
	}

	/** @see #abs(Vector2) */
	public static Vector3 abs(Vector3 vector) {
		vector.x = Math.abs(vector.x);
		vector.y = Math.abs(vector.y);
		vector.z = Math.abs(vector.z);
		return vector;
	}

	/** @param vertices the vertices to add the given values to
	 *  @param x the x value to add
	 *  @param y the y value to add
	 *  @return the given vertices for chaining */
	public static Array<Vector2> add(Array<Vector2> vertices, float x, float y) {
		for(Vector2 vertice : vertices)
			vertice.add(x, y);
		return vertices;
	}

	/** @see #add(Array, float, float) */
	public static Array<Vector2> sub(Array<Vector2> vertices, float x, float y) {
		return add(vertices, -x, -y);
	}

	/** @see #add(Array, float, float) */
	public static FloatArray add(FloatArray vertices, float x, float y) {
		add(vertices.items, 0, vertices.size, x, y);
		return vertices;
	}

	/** @see #add(Array, float, float) */
	public static FloatArray sub(FloatArray vertices, float x, float y) {
		sub(vertices.items, 0, vertices.size, x, y);
		return vertices;
	}

	/** @see #add(FloatArray, float, float) */
	public static FloatArray addX(FloatArray vertices, float value) {
		addX(vertices.items, 0, vertices.size, value);
		return vertices;
	}

	/** @see #sub(FloatArray, float, float) */
	public static FloatArray subX(FloatArray vertices, float value) {
		subX(vertices.items, 0, vertices.size, value);
		return vertices;
	}

	/** @see #add(FloatArray, float, float) */
	public static FloatArray addY(FloatArray vertices, float value) {
		addY(vertices.items, 0, vertices.size, value);
		return vertices;
	}

	/** @see #sub(FloatArray, float, float) */
	public static FloatArray subY(FloatArray vertices, float value) {
		subY(vertices.items, 0, vertices.size, value);
		return vertices;
	}

	/** @return a Vector2 representing the size of a rectangle containing all given vertices */
	public static Vector2 size(Array<Vector2> vertices, Vector2 output) {
		return output.set(width(vertices), height(vertices));
	}

	/** @see #size(Array, Vector2) */
	public static Vector2 size(Array<Vector2> vertices) {
		return size(vertices, vec2_0);
	}

	/** @return the amplitude from the min x vertice to the max x vertice */
	public static float width(Array<Vector2> vertices) {
		return amplitude2(filterX(vertices));
	}

	/** @return the amplitude from the min y vertice to the max y vertice */
	public static float height(Array<Vector2> vertices) {
		return amplitude2(filterY(vertices));
	}

	/** @see #width(Array) */
	public static float width(FloatArray vertices) {
		return amplitude2(filterX(vertices));
	}

	/** @see #height(Array) */
	public static float height(FloatArray vertices) {
		return amplitude2(filterY(vertices));
	}

	/** @return the amplitude of the min z vertice to the max z vertice */
	public static float depth(FloatArray vertices) {
		return amplitude2(filterZ(vertices));
	}

	/** @return the x values of the given vertices */
	public static FloatArray filterX(Array<Vector2> vertices, FloatArray output) {
		if(output == null)
			output = new FloatArray(vertices.size);
		output.clear();
		output.ensureCapacity(vertices.size);
		for(int i = 0; i < vertices.size; i++)
			output.add(vertices.get(i).x);
		return output;
	}

	/** @see #filterX(Array, FloatArray) */
	public static FloatArray filterX(Array<Vector2> vertices) {
		return filterX(vertices, tmpFloatArray);
	}

	/** @param vertices the vertices in [x, y, x, y, ...] order
	 *  @see #filterX(Array) */
	public static FloatArray filterX(FloatArray vertices, FloatArray output) {
		return ArrayUtils.select(vertices, -1, 2, output);
	}

	/** @see #filterX(FloatArray, FloatArray) */
	public static FloatArray filterX(FloatArray vertices) {
		return filterX(vertices, tmpFloatArray);
	}

	/** @param vertices the vertices in [x, y, z, x, y, z, ...] order
	 *  @see #filterX(FloatArray, FloatArray) */
	public static FloatArray filterX3D(FloatArray vertices, FloatArray output) {
		return ArrayUtils.select(vertices, -2, 3, output);
	}

	/** @see #filterX3D(FloatArray, FloatArray) */
	public static FloatArray filterX3D(FloatArray vertices) {
		return filterX3D(vertices, tmpFloatArray);
	}

	/** @return the y values of the given vertices */
	public static FloatArray filterY(Array<Vector2> vertices, FloatArray output) {
		if(output == null)
			output = new FloatArray(vertices.size);
		output.clear();
		output.ensureCapacity(vertices.size);
		for(int i = 0; i < vertices.size; i++)
			output.add(vertices.get(i).y);
		return output;
	}

	/** @see #filterY(Array, FloatArray) */
	public static FloatArray filterY(Array<Vector2> vertices) {
		return filterY(vertices, tmpFloatArray);
	}

	/** @see #filterY(Array, FloatArray)
	 *  @see #filterX(FloatArray, FloatArray)*/
	public static FloatArray filterY(FloatArray vertices, FloatArray output) {
		return ArrayUtils.select(vertices, 2, output);
	}

	/** @see #filterY(FloatArray, FloatArray) */
	public static FloatArray filterY(FloatArray vertices) {
		return filterY(vertices, tmpFloatArray);
	}

	/** @see #filterY(FloatArray, FloatArray)
	 *  @see #filterX3D(FloatArray, FloatArray) */
	public static FloatArray filterY3D(FloatArray vertices, FloatArray output) {
		return ArrayUtils.select(vertices, -4, 3, output);
	}

	/** @see #filterY3D(FloatArray, FloatArray) */
	public static FloatArray filterY3D(FloatArray vertices) {
		return filterY3D(vertices, tmpFloatArray);
	}

	/** @see #filterX(Array, FloatArray)
	 *  @see #filterX3D(FloatArray, FloatArray) */
	public static FloatArray filterZ(FloatArray vertices, FloatArray output) {
		return ArrayUtils.select(vertices, 3, output);
	}

	/** @see #filterZ(FloatArray, FloatArray) */
	public static FloatArray filterZ(FloatArray vertices) {
		return filterZ(vertices, tmpFloatArray);
	}

	/** @see #filterX3D(FloatArray) */
	public static FloatArray filterW(FloatArray vertices, FloatArray output) {
		return ArrayUtils.select(vertices, 4, output);
	}

	/** @see #filterW(FloatArray, FloatArray) */
	public static FloatArray filterW(FloatArray vertices) {
		return filterW(vertices, tmpFloatArray);
	}

	/** @return the min x value of the given vertices */
	public static float minX(Array<Vector2> vertices) {
		return min(filterX(vertices));
	}

	/** @return the min y value of the given vertices */
	public static float minY(Array<Vector2> vertices) {
		return min(filterY(vertices));
	}

	/** @return the max x value of the given vertices */
	public static float maxX(Array<Vector2> vertices) {
		return max(filterX(vertices));
	}

	/** @return the max y value of the given vertices */
	public static float maxY(Array<Vector2> vertices) {
		return max(filterY(vertices));
	}

	/** @see #minX(Array) */
	public static float minX(FloatArray vertices) {
		return min(filterX(vertices));
	}

	/** @see #minY(Array) */
	public static float minY(FloatArray vertices) {
		return min(filterY(vertices));
	}

	/** @see #maxX(Array) */
	public static float maxX(FloatArray vertices) {
		return max(filterX(vertices));
	}

	/** @see #maxY(Array) */
	public static float maxY(FloatArray vertices) {
		return max(filterY(vertices));
	}

	/** rotates a {@code point} around {@code center}
	 *  @param point the point to rotate
	 *  @param origin the point around which to rotate {@code point}
	 *  @param radians the rotation
	 *  @return the given {@code point} rotated around {@code center} by {@code radians} */
	public static Vector2 rotate(Vector2 point, Vector2 origin, float radians) {
		if(point.equals(origin))
			return point;
		return point.sub(origin).rotateRad(radians).add(origin);
	}

	/** rotates the line around its center (same as {@link #rotate(Vector2, Vector2, float)} using the center between both points as origin)
	 *  @param a a point on the line
	 *  @param b another point on the line
	 *  @param radians the rotation */
	public static void rotateLine(Vector2 a, Vector2 b, float radians) {
		rotate(a, vec2_0.set(a).add(b).scl(.5f), radians);
		rotate(b, vec2_0, radians);
	}

	/** @see net.dermetfan.utils.math.GeometryUtils#rotate(float, float, float, float, float, float[], int) */
	public static FloatArray rotate(float x, float y, float width, float height, float radians, FloatArray output) {
		output.clear();
		output.ensureCapacity(8);
		rotate(x, y, width, height, radians, output.items, 0);
		return output;
	}

	/** @see #rotate(float, float, float, float, float, FloatArray) */
	public static FloatArray rotate(float x, float y, float width, float height, float radians) {
		return rotate(x, y, width, height, radians, tmpFloatArray);
	}

	/** @see #rotate(float, float, float, float, float, FloatArray) */
	public static FloatArray rotate(Rectangle rectangle, float radians, FloatArray output) {
		return rotate(rectangle.x, rectangle.y, rectangle.width, rectangle.height, radians, output);
	}

	/** @see #rotate(Rectangle, float, FloatArray) */
	public static FloatArray rotate(Rectangle rectangle, float radians) {
		return rotate(rectangle, radians, tmpFloatArray);
	}

	/** @param vector2s the Vector2s to convert to a FloatArray
	 *  @return the FloatArray converted from the given Vector2s */
	public static FloatArray toFloatArray(Array<Vector2> vector2s, FloatArray output) {
		if(output == null)
			output = new FloatArray(vector2s.size * 2);
		output.clear();
		output.ensureCapacity(vector2s.size * 2);

		for(int i = 0, vi = -1; i < vector2s.size * 2; i++)
			if(i % 2 == 0)
				output.add(vector2s.get(++vi).x);
			else
				output.add(vector2s.get(vi).y);

		return output;
	}

	/** @see #toFloatArray(Array, FloatArray) */
	public static FloatArray toFloatArray(Array<Vector2> vector2s) {
		return toFloatArray(vector2s, tmpFloatArray);
	}

	/** @param floats the FloatArray to convert to an Array&lt;Vector2&gt;
	 *  @return the Array&lt;Vector2&gt; converted from the given FloatArray */
	public static Array<Vector2> toVector2Array(FloatArray floats, Array<Vector2> output) {
		if(floats.size % 2 != 0)
			throw new IllegalArgumentException("the float array's length is not dividable by two, so it won't make up a Vector2 array: " + floats.size);

		if(output == null)
			output = new Array<>(floats.size / 2);
		output.clear();

		for(int i = 0, fi = -1; i < floats.size / 2; i++)
			output.add(new Vector2(floats.get(++fi), floats.get(++fi)));

		return output;
	}

	/** @see #toVector2Array(FloatArray, Array) */
	public static Array<Vector2> toVector2Array(FloatArray floats) {
		return toVector2Array(floats, tmpVector2Array);
	}

	/** @param vertexCount the number of vertices for each {@link Polygon}
	 *  @see #toPolygonArray(Array, IntArray) */
	public static Polygon[] toPolygonArray(Array<Vector2> vertices, int vertexCount) {
		IntArray vertexCounts = Pools.obtain(IntArray.class);
		vertexCounts.clear();
		vertexCounts.ensureCapacity(vertices.size / vertexCount);
		for(int i = 0; i < vertices.size / vertexCount; i++)
			vertexCounts.add(vertexCount);
		Polygon[] polygons = toPolygonArray(vertices, vertexCounts);
		vertexCounts.clear();
		Pools.free(vertexCounts);
		return polygons;
	}

	/** @param vertices the vertices which should be split into a {@link Polygon} array
	 *  @param vertexCounts the number of vertices of each {@link Polygon}
	 *  @return the {@link Polygon} array extracted from the vertices */
	public static Polygon[] toPolygonArray(Array<Vector2> vertices, IntArray vertexCounts) {
		Polygon[] polygons = new Polygon[vertexCounts.size];

		for(int i = 0, vertice = -1; i < polygons.length; i++) {
			tmpVector2Array.clear();
			tmpVector2Array.ensureCapacity(vertexCounts.get(i));
			for(int i2 = 0; i2 < vertexCounts.get(i); i2++)
				tmpVector2Array.add(vertices.get(++vertice));
			polygons[i] = new Polygon(toFloatArray(tmpVector2Array).toArray());
		}

		return polygons;
	}

	/** @param polygon the polygon, assumed to be simple
	 *  @return if the vertices are in clockwise order */
	public static boolean areVerticesClockwise(Polygon polygon) {
		return polygon.area() < 0;
	}

	/** @see #areVerticesClockwise(Polygon) */
	public static boolean areVerticesClockwise(FloatArray vertices) {
		return areVerticesClockwise(vertices.items, 0, vertices.size);
	}

	/** @see #areVerticesClockwise(FloatArray) */
	public static boolean areVerticesClockwise(Array<Vector2> vertices) {
		return vertices.size <= 2 || areVerticesClockwise(toFloatArray(vertices));
	}

	/** @see com.badlogic.gdx.math.GeometryUtils#polygonArea(float[], int, int) */
	public static float polygonArea(FloatArray vertices) {
		return polygonArea(vertices.items, 0, vertices.size);
	}

	/** @see #arrangeConvexPolygon(float[], int, int, boolean) */
	public static void arrangeConvexPolygon(FloatArray vertices, boolean clockwise) {
		arrangeConvexPolygon(vertices.items, 0, vertices.size, clockwise);
	}

	/** used in {@link #arrangeCounterClockwise(Array)} */
	private static final Comparator<Vector2> arrangeCounterClockwiseComparator = new Comparator<Vector2>() {
		/** compares the x coordinates */
		@Override
		public int compare(Vector2 a, Vector2 b) {
			if(a.x > b.x)
				return 1;
			else if(a.x < b.x)
				return -1;
			return 0;
		}
	};

	/** @param vertices the vertices to arrange in clockwise order */
	@Deprecated
	public static void arrangeCounterClockwise(Array<Vector2> vertices) {
		// http://www.emanueleferonato.com/2011/08/05/slicing-splitting-and-cutting-objects-with-box2d-part-4-using-real-graphics
		int n = vertices.size, i1 = 1, i2 = vertices.size - 1;

		if(tmpVector2Array == null)
			tmpVector2Array = new Array<>(vertices.size);
		tmpVector2Array.clear();
		tmpVector2Array.addAll(vertices);
		tmpVector2Array.sort(arrangeCounterClockwiseComparator);

		tmpVector2Array.set(0, vertices.first());
		Vector2 C = vertices.first();
		Vector2 D = vertices.get(n - 1);

		float det;
		for(int i = 1; i < n - 1; i++) {
			det = det(C.x, C.y, D.x, D.y, vertices.get(i).x, vertices.get(i).y);
			if(det < 0)
				tmpVector2Array.set(i1++, vertices.get(i));
			else
				tmpVector2Array.set(i2--, vertices.get(i));
		}

		tmpVector2Array.set(i1, vertices.get(n - 1));

		vertices.clear();
		vertices.addAll(tmpVector2Array, 0, n);
	}

	/** @see #invertAxes(float[], int, int, boolean, boolean) */
	public static FloatArray invertAxes(FloatArray vertices, boolean x, boolean y) {
		invertAxes(vertices.items, 0, vertices.size, x, y);
		return vertices;
	}

	/** @see #toYDown(float[]) */
	public static FloatArray toYDown(FloatArray vertices) {
		toYDown(vertices.items, 0, vertices.size);
		return vertices;
	}

	/** @see #toYUp(float[]) */
	public static FloatArray toYUp(FloatArray vertices) {
		toYUp(vertices.items, 0, vertices.size);
		return vertices;
	}

	/** @param aabb the rectangle to set as AABB of the given vertices
	 *  @param vertices the vertices */
	public static Rectangle setToAABB(Rectangle aabb, FloatArray vertices) {
		return aabb.set(minX(vertices), minY(vertices), width(vertices), height(vertices));
	}

	/** @see #setToAABB(Rectangle, FloatArray) */
	public static Rectangle setToAABB(Rectangle aabb, Array<Vector2> vertices) {
		return aabb.set(minX(vertices), minY(vertices), width(vertices), height(vertices));
	}

	/** @see #isConvex(float[], int, int) */
	public static boolean isConvex(FloatArray vertices) {
		return isConvex(vertices.items, 0, vertices.size);
	}

	/** @see #isConvex(float[]) */
	public static boolean isConvex(Polygon polygon) {
		return isConvex(polygon.getVertices());
	}

	/** @see #isConvex(FloatArray) */
	public static boolean isConvex(Array<Vector2> vertices) {
		return isConvex(toFloatArray(vertices));
	}

	/** @param concave the concave polygon to triangulate
	 *  @return an array of triangles representing the given concave polygon
	 *  @see EarClippingTriangulator#computeTriangles(float[]) */
	public static Polygon[] triangulate(Polygon concave) {
		@SuppressWarnings("unchecked")
		Array<Vector2> polygonVertices = Pools.obtain(Array.class);
		polygonVertices.clear();
		tmpFloatArray.clear();
		tmpFloatArray.addAll(concave.getTransformedVertices());
		polygonVertices.addAll(toVector2Array(tmpFloatArray));
		ShortArray indices = new EarClippingTriangulator().computeTriangles(tmpFloatArray);

		@SuppressWarnings("unchecked")
		Array<Vector2> vertices = Pools.obtain(Array.class);
		vertices.clear();
		vertices.ensureCapacity(indices.size);
		for(int i = 0; i < indices.size; i++)
			vertices.set(i, polygonVertices.get(indices.get(i)));
		Polygon[] polygons = toPolygonArray(vertices, 3);

		polygonVertices.clear();
		vertices.clear();
		Pools.free(polygonVertices);
		Pools.free(vertices);
		return polygons;
	}

	/** @param concave the concave polygon to to decompose
	 *  @return an array of convex polygons representing the given concave polygon
	 *  @see BayazitDecomposer#convexPartition(Array) */
	public static Polygon[] decompose(Polygon concave) {
		tmpFloatArray.clear();
		tmpFloatArray.addAll(concave.getTransformedVertices());
		Array<Array<Vector2>> convexPolys = BayazitDecomposer.convexPartition(new Array<>(toVector2Array(tmpFloatArray)));
		Polygon[] convexPolygons = new Polygon[convexPolys.size];
		for(int i = 0; i < convexPolygons.length; i++)
			convexPolygons[i] = new Polygon(toFloatArray(convexPolys.get(i)).toArray());
		return convexPolygons;
	}

	/** Keeps the first described rectangle in the second described rectangle. If the second rectangle is smaller than the first one, the first will be centered on the second one.
	 *  @param position the position of the first rectangle
	 *  @param width the width of the first rectangle
	 *  @param height the height of the first rectangle
	 *  @param x2 the x of the second rectangle
	 *  @param y2 the y of the second rectangle
	 *  @param width2 the width of the second rectangle
	 *  @param height2 the height of the second rectangle
	 *  @return the position of the first rectangle */
	public static Vector2 keepWithin(Vector2 position, float width, float height, float x2, float y2, float width2, float height2) {
		if(width2 < width)
			position.x = x2 + width2 / 2 - width / 2;
		else if(position.x < x2)
			position.x = x2;
		else if(position.x + width > x2 + width2)
			position.x = x2 + width2 - width;
		if(height2 < height)
			position.y = y2 + height2 / 2 - height / 2;
		else if(position.y < y2)
			position.y = y2;
		else if(position.y + height > y2 + height2)
			position.y = y2 + height2 - height;
		return position;
	}

	/** @see #keepWithin(Vector2, float, float, float, float, float, float) */
	public static Vector2 keepWithin(float x, float y, float width, float height, float rectX, float rectY, float rectWidth, float rectHeight) {
		return keepWithin(vec2_0.set(x, y), width, height, rectX, rectY, rectWidth, rectHeight);
	}

	/** @see #keepWithin(float, float, float, float, float, float, float, float) */
	public static Rectangle keepWithin(Rectangle rect, Rectangle other) {
		return rect.setPosition(keepWithin(rect.x, rect.y, rect.width, rect.height, other.x, other.y, other.width, other.height));
	}

	/** Keeps the given {@link OrthographicCamera} in the given rectangle. If the rectangle is smaller than the camera viewport times the camera zoom, the camera will be centered on the rectangle.<br>
	 *  Note that the camera will not be {@link OrthographicCamera#update() updated}.
	 *  @param camera the camera to keep in the rectangle
	 *  @see #keepWithin(float, float, float, float, float, float, float, float) */
	public static void keepWithin(OrthographicCamera camera, float x, float y, float width, float height) {
		vec2_0.set(keepWithin(camera.position.x - camera.viewportWidth / 2 * camera.zoom, camera.position.y - camera.viewportHeight / 2 * camera.zoom, camera.viewportWidth * camera.zoom, camera.viewportHeight * camera.zoom, x, y, width, height));
		camera.position.x = vec2_0.x + camera.viewportWidth / 2 * camera.zoom;
		camera.position.y = vec2_0.y + camera.viewportHeight / 2 * camera.zoom;
	}

	/** @see #intersectSegmentConvexPolygon(float, float, float, float, float[], int, int, Vector2, Vector2) */
	public static int intersectSegmentConvexPolygon(float x1, float y1, float x2, float y2, float[] polygon, Vector2 intersection1, Vector2 intersection2) {
		return intersectSegmentConvexPolygon(x1, y1, x2, y2, polygon, 0,  polygon.length, intersection1, intersection2);
	}

	/** @param x1 the x coordinate of the first point of the segment to intersect with the polygon
	 *  @param y1 the y coordinate of the first point of the segment to intersect with the polygon
	 *  @param x2 the x coordinate of the second point of the segment to intersect with the polygon
	 *  @param y2 the y coordinate of the second point of the segment to intersect with the polygon
	 *  @param polygon the convex polygon
	 *  @param intersection1 The first intersection point. May be null.
	 *  @param intersection2 The second intersection point. May be null.
	 *  @return The number of intersection points. May return 0, 1, 2 or -1 for an infinite number of intersections (if the segment lies on a side of the polygon).
	 *  @see #intersectSegments(float, float, float, float, float[], int, int, boolean, FloatArray) */
	public static int intersectSegmentConvexPolygon(float x1, float y1, float x2, float y2, float[] polygon, int offset, int length, Vector2 intersection1, Vector2 intersection2) {
		FloatArray intersections = Pools.obtain(FloatArray.class);
		intersectSegments(x1, y1, x2, y2, polygon, offset, length, true, intersections);
		assert intersections.size % 2 == 0;
		int count = intersections.size / 2;
		if(count >= 1) {
			if(intersection1 != null)
				intersection1.set(intersections.get(0), intersections.get(1));
			if(count >= 2 && intersection2 != null)
				intersection2.set(intersections.get(2), intersections.get(3));
		}
		intersections.clear();
		Pools.free(intersections);
		if(count > 3)
			throw new IllegalArgumentException("More intersections with a convex polygon found than possible: " + count + ". Is your polygon concave? " + ArrayUtils.toString(polygon, offset, length) + " segment: [" + x1 + ", " + y1 + "; " + x2 + ", " + y2 + "]");
		return count == 3 ? -1 : count;
	}

	/** @see #intersectSegmentConvexPolygon(float, float, float, float, float[], int, int, Vector2, Vector2) */
	public static int intersectSegmentConvexPolygon(Vector2 a, Vector2 b, FloatArray polygon, Vector2 intersection1, Vector2 intersection2) {
		return intersectSegmentConvexPolygon(a.x, a.y, b.x, b.y, polygon.items, 0, polygon.size, intersection1, intersection2);
	}

	/** @see #intersectSegments(float, float, float, float, FloatArray, boolean, FloatArray) */
	public static boolean intersectSegments(Vector2 a, Vector2 b, FloatArray segments, boolean polygon, Array<Vector2> intersections) {
		FloatArray floatIntersections = Pools.obtain(FloatArray.class);
		intersections.clear();
		if(!intersectSegments(a.x, a.y, b.x, b.y, segments, polygon, floatIntersections)) {
			floatIntersections.clear();
			Pools.free(floatIntersections);
			return false;
		}
		intersections.ensureCapacity(floatIntersections.size / 2);
		for(int i = 1; i < floatIntersections.size; i += 2)
			intersections.add(new Vector2(floatIntersections.get(i - 1), floatIntersections.get(i)));
		floatIntersections.clear();
		Pools.free(floatIntersections);
		return true;
	}

	/** @see #intersectSegments(float, float, float, float, float[], int, int, boolean, FloatArray) */
	public static boolean intersectSegments(float x1, float y1, float x2, float y2, FloatArray segments, boolean polygon, FloatArray intersections) {
		return intersectSegments(x1, y1, x2, y2, segments.items, 0, segments.size, polygon, intersections);
	}

	/** @param x1 the x coordinate of the first point of the segment
	 *  @param y1 the y coordinate of the first point of the segment
	 *  @param x2 the x coordinate of the second point of the segment
	 *  @param y2 the y coordinate of the second point of the segment
	 *  @param segments the segments
	 *  @param polygon if the segments represent a closed polygon
	 *  @param intersections the array to store the intersections in
	 *  @return whether the given segment intersects with any of the given segments */
	public static boolean intersectSegments(float x1, float y1, float x2, float y2, float[] segments, int offset, int length, boolean polygon, FloatArray intersections) {
		ArrayUtils.checkRegion(segments, offset, length);
		if(polygon && length < 6)
			throw new IllegalArgumentException("A polygon consists of at least 3 points. length: " + length);
		else if(length < 4)
			throw new IllegalArgumentException("segments does not contain enough vertices to represent at least one segment: " + length);
		if(length % 2 != 0)
			throw new IllegalArgumentException("malformed segments, length is odd: " + length);
		intersections.clear();
		boolean intersects = false;
		for(int i = offset, n = offset + length - (polygon ? 0 : 2); i < n; i += 2) {
			float x3 = segments[i], y3 = segments[i + 1], x4 = segments[ArrayUtils.repeat(offset, length, i + 2)], y4 = segments[ArrayUtils.repeat(offset, length, i + 3)];
			if(Intersector.intersectSegments(x1, y1, x2, y2, x3, y3, x4, y4, vec2_0)) {
				intersects = true;
				intersections.add(vec2_0.x);
				intersections.add(vec2_0.y);
			}
		}
		return intersects;
	}

	/** dispatch method
	 *  @param shape the shape to reset
	 *  @return the given shape for chaining */
	@SuppressWarnings("unchecked")
	public static <T extends Shape2D> T reset(T shape) {
		if(shape instanceof Polygon)
			return (T) reset((Polygon) shape);
		if(shape instanceof Polyline)
			return (T) reset((Polyline) shape);
		if(shape instanceof Rectangle)
			return (T) reset((Rectangle) shape);
		if(shape instanceof Circle)
			return (T) reset((Circle) shape);
		if(shape instanceof Ellipse)
			return (T) reset((Ellipse) shape);
		return shape;
	}

	/** @param polygon the Polygon to reset
	 *  @return the given Polygon for chaining */
	public static Polygon reset(Polygon polygon) {
		polygon.setPosition(0, 0);
		polygon.setRotation(0);
		polygon.setOrigin(0, 0);
		polygon.setScale(1, 1);
		float[] vertices = polygon.getVertices();
		for(int i = 0; i < vertices.length; i++)
			vertices[i] = 0;
		polygon.setVertices(vertices);
		return polygon;
	}

	/** @param polyline the polyline to reset
	 *  @return the given polyline for chaining */
	public static Polyline reset(Polyline polyline) {
		polyline.setPosition(0, 0);
		polyline.setRotation(0);
		polyline.setOrigin(0, 0);
		polyline.setScale(1, 1);
		float[] vertices = polyline.getVertices();
		for(int i = 0; i < vertices.length; i++)
			vertices[i] = 0;
		polyline.dirty();
		return polyline;
	}

	/** @param rectangle the rectangle to reset
	 *  @return the given rectangle for chaining */
	public static Rectangle reset(Rectangle rectangle) {
		return rectangle.set(0, 0, 0, 0);
	}

	/** @param circle the circle to reset
	 *  @return the given circle for chaining */
	public static Circle reset(Circle circle) {
		circle.set(0, 0, 0);
		return circle;
	}

	/** @param ellipse the ellipse to reset
	 *  @return the given ellipse for chaining */
	public static Ellipse reset(Ellipse ellipse) {
		ellipse.set(0, 0, 0, 0);
		return ellipse;
	}

}
