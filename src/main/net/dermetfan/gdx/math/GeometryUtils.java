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
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.ShortArray;
import net.dermetfan.gdx.utils.ArrayUtils;

import static net.dermetfan.gdx.utils.ArrayUtils.wrapIndex;
import static net.dermetfan.gdx.math.MathUtils.amplitude;
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
		return net.dermetfan.utils.math.GeometryUtils.between(point.x, point.y, a.x, a.y, b.x, b.y, inclusive);
	}

	/** @see net.dermetfan.utils.math.GeometryUtils#between(float, float, float, float, float, float) */
	public static boolean between(Vector2 point, Vector2 a, Vector2 b) {
		return net.dermetfan.utils.math.GeometryUtils.between(point.x, point.y, a.x, a.y, b.x, b.y);
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
		net.dermetfan.utils.math.GeometryUtils.add(vertices.items, x, y, 0, vertices.size);
		return vertices;
	}

	/** @see #add(Array, float, float) */
	public static FloatArray sub(FloatArray vertices, float x, float y) {
		net.dermetfan.utils.math.GeometryUtils.sub(vertices.items, x, y, 0, vertices.size);
		return vertices;
	}

	/** @see #add(FloatArray, float, float) */
	public static FloatArray addX(FloatArray vertices, float value) {
		net.dermetfan.utils.math.GeometryUtils.addX(vertices.items, value, 0, vertices.size);
		return vertices;
	}

	/** @see #sub(FloatArray, float, float) */
	public static FloatArray subX(FloatArray vertices, float value) {
		net.dermetfan.utils.math.GeometryUtils.subX(vertices.items, value, 0, vertices.size);
		return vertices;
	}

	/** @see #add(FloatArray, float, float) */
	public static FloatArray addY(FloatArray vertices, float value) {
		net.dermetfan.utils.math.GeometryUtils.addY(vertices.items, value, 0, vertices.size);
		return vertices;
	}

	/** @see #sub(FloatArray, float, float) */
	public static FloatArray subY(FloatArray vertices, float value) {
		net.dermetfan.utils.math.GeometryUtils.subY(vertices.items, value, 0, vertices.size);
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
		return amplitude(filterX(vertices));
	}

	/** @return the amplitude from the min y vertice to the max y vertice */
	public static float height(Array<Vector2> vertices) {
		return amplitude(filterY(vertices));
	}

	/** @see #width(Array) */
	public static float width(FloatArray vertices) {
		return amplitude(filterX(vertices));
	}

	/** @see #height(Array) */
	public static float height(FloatArray vertices) {
		return amplitude(filterY(vertices));
	}

	/** @return the amplitude of the min z vertice to the max z vertice */
	public static float depth(FloatArray vertices) {
		return amplitude(filterZ(vertices));
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
		net.dermetfan.utils.math.GeometryUtils.rotate(x, y, width, height, radians, output.items, 0);
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
		return vertices.size <= 4 || polygonArea(vertices) < 0;
	}

	/** @see #areVerticesClockwise(FloatArray) */
	public static boolean areVerticesClockwise(Array<Vector2> vertices) {
		return vertices.size <= 2 || areVerticesClockwise(toFloatArray(vertices));
	}

	/** @see com.badlogic.gdx.math.GeometryUtils#polygonArea(float[], int, int) */
	public static float polygonArea(FloatArray vertices) {
		return com.badlogic.gdx.math.GeometryUtils.polygonArea(vertices.items, 0, vertices.size);
	}

	/** used in {@link #arrangeClockwise(Array)} */
	private static final Comparator<Vector2> arrangeClockwiseComparator = new Comparator<Vector2>() {
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
	public static void arrangeClockwise(Array<Vector2> vertices) {
		// http://www.emanueleferonato.com/2011/08/05/slicing-splitting-and-cutting-objects-with-box2d-part-4-using-real-graphics
		int n = vertices.size, i1 = 1, i2 = vertices.size - 1;

		if(tmpVector2Array == null)
			tmpVector2Array = new Array<>(vertices.size);
		tmpVector2Array.clear();
		tmpVector2Array.addAll(vertices);
		tmpVector2Array.sort(arrangeClockwiseComparator);

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

	/** Converts the given vertices to their position on inverted axes.
	 *  @param vertices the vertices to convert
	 *  @param x if the x-axis should be inverted
	 *  @param y if the y-axis should be inverted
	 *  @return the given vertices converted to the inversed axis in their <strong>local</strong> coordinate system */
	public static FloatArray invertAxes(FloatArray vertices, boolean x, boolean y) {
		if(!x && !y)
			return vertices;
		float height = height(vertices), width = width(vertices);
		for(int i = x ? 0 : 1; i < vertices.size; i += x ^ y ? 2 : 1)
			vertices.set(i, i % 2 == 0 ? net.dermetfan.utils.math.GeometryUtils.invertAxis(vertices.get(i), width) : net.dermetfan.utils.math.GeometryUtils.invertAxis(vertices.get(i), height));
		return vertices;
	}

	/** inverts the given vertices to a y-down coordinate system and translates them according to their parent coordinate system by their {@link #height(FloatArray) height}
	 *  @see #invertAxes(FloatArray, boolean, boolean) */
	public static FloatArray toYDown(FloatArray vertices) {
		invertAxes(vertices, false, true);
		return subY(vertices, height(vertices));
	}

	/** inverts the given vertices to a y-up coordinate system and translates them according to their parent coordinate system by their {@link #height(FloatArray) height}
	 *  @see #invertAxes(FloatArray, boolean, boolean) */
	public static FloatArray toYUp(FloatArray vertices) {
		invertAxes(vertices, false, true);
		return addY(vertices, height(vertices));
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

	/** @see #isConvex(Array) */
	public static boolean isConvex(FloatArray vertices) {
		return isConvex(toVector2Array(vertices));
	}

	/** @see #isConvex(Array) */
	public static boolean isConvex(Polygon polygon) {
		tmpFloatArray.clear();
		tmpFloatArray.addAll(polygon.getVertices());
		return isConvex(tmpFloatArray);
	}

	/** @param vertices the vertices of the polygon to examine for convexity
	 *  @return if the polygon is convex */
	public static boolean isConvex(Array<Vector2> vertices) {
		// http://www.sunshine2k.de/coding/java/Polygon/Convex/polygon.htm
		Vector2 p, v = vec2_1, u;
		float res = 0;
		for(int i = 0; i < vertices.size; i++) {
			p = vertices.get(i);
			vec2_0.set(vertices.get((i + 1) % vertices.size));
			v.x = vec2_0.x - p.x;
			v.y = vec2_0.y - p.y;
			u = vertices.get((i + 2) % vertices.size);

			if(i == 0) // in first loop direction is unknown, so save it in res
				res = u.x * v.y - u.y * v.x + v.x * p.y - v.y * p.x;
			else {
				float newres = u.x * v.y - u.y * v.x + v.x * p.y - v.y * p.x;
				if(newres > 0 && res < 0 || newres < 0 && res > 0)
					return false;
			}
		}
		return true;
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

	/** @param a the first point of the segment
	 *  @param b the second point of the segment
	 *  @param polygon the polygon, assumed to be convex
	 *  @param intersection1 the first intersection point
	 *  @param intersection2 the second intersection point
	 *  @return the number of intersection points
	 *  @see #intersectSegments(Vector2, Vector2, FloatArray, boolean, Array)*/
	public static int intersectSegments(Vector2 a, Vector2 b, FloatArray polygon, Vector2 intersection1, Vector2 intersection2) {
		FloatArray intersections = Pools.obtain(FloatArray.class);
		intersectSegments(a.x, a.y, b.x, b.y, polygon, true, intersections);
		int size = intersections.size;
		if(size >= 2) {
			intersection1.set(intersections.get(0), intersections.get(1));
			if(size == 4)
				intersection2.set(intersections.get(2), intersections.get(3));
			else if(size > 4)
				assert false : "more intersection points with a convex polygon found than possible: " + size;
		}
		Pools.free(intersections);
		return size / 2;
	}

	/** @see #intersectSegments(float, float, float, float, FloatArray, boolean, FloatArray) */
	public static void intersectSegments(Vector2 a, Vector2 b, FloatArray segments, boolean polygon, Array<Vector2> intersections) {
		FloatArray fa = Pools.obtain(FloatArray.class);
		intersectSegments(a.x, a.y, b.x, b.y, segments, polygon, fa);
		if(fa.size < 1) {
			intersections.clear();
			Pools.free(fa);
			return;
		}
		intersections.ensureCapacity(fa.size / 2 - intersections.size);
		for(int i = 1; i < fa.size; i += 2)
			if(intersections.size > i / 2)
				intersections.get(i / 2).set(fa.get(i - 1), fa.get(i));
			else
				intersections.add(new Vector2(fa.get(i - 1), fa.get(i)));
		Pools.free(fa);
	}

	/** @param segments the segments
	 *  @param polygon if the segments represent a closed polygon
	 *  @param intersections the array to store the intersections in */
	public static void intersectSegments(float x1, float y1, float x2, float y2, FloatArray segments, boolean polygon, FloatArray intersections) {
		if(polygon && segments.size < 6)
			throw new IllegalArgumentException("a polygon consists of at least 3 points: " + segments.size);
		else if(segments.size < 4)
			throw new IllegalArgumentException("segments does not contain enough vertices to represent at least one segment: " + segments.size);
		if(segments.size % 2 != 0)
			throw new IllegalArgumentException("malformed segments; the number of vertices is not dividable by 2: " + segments.size);
		intersections.clear();
		vec2_0.setZero();
		for(int i = 0, n = segments.size - (polygon ? 0 : 2); i < n; i += 2) {
			float x3 = segments.get(i), y3 = segments.get(i + 1), x4 = wrapIndex(i + 2, segments), y4 = wrapIndex(i + 3, segments);
			if(Intersector.intersectSegments(x1, y1, x2, y2, x3, y3, x4, y4, vec2_0)) {
				intersections.add(vec2_0.x);
				intersections.add(vec2_0.y);
			}
		}
		Pools.free(vec2_0);
	}

}
