/**
 * Copyright 2013 Robin Stumm (serverkorken@googlemail.com, http://dermetfan.bplaced.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dermetfan.util.libgdx.math;

import static com.badlogic.gdx.math.MathUtils.cos;
import static com.badlogic.gdx.math.MathUtils.sin;
import static net.dermetfan.util.math.MathUtils.amplitude;

import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Vector2;

/** provides some useful methods for geometric calculations
 *  @author dermetfan */
public abstract class GeometryUtils {

	/** a {@link Vector2} for temporary usage */
	public static final Vector2 vec2_0 = new Vector2(), vec2_1 = new Vector2();

	/** a temporary array */
	private static Vector2[] tmpVecArr;

	/** a temporary array */
	private static float[] tmpFloatArr;

	/** @return a Vector2 representing the size of a rectangle containing all given vertices */
	public static Vector2 size(Vector2[] vertices, Vector2 output) {
		return output.set(amplitude(filterX(vertices)), amplitude(filterY(vertices)));
	}

	/** @see #size(Vector2[], Vector2) */
	public static Vector2 size(Vector2[] vertices) {
		return size(vertices, vec2_0);
	}

	/** @return the x values of the given vertices */
	public static float[] filterX(Vector2[] vertices, float[] output) {
		if(output == null || output.length != vertices.length)
			output = new float[vertices.length];
		for(int i = 0; i < output.length; i++)
			output[i] = vertices[i].x;
		return output;
	}

	/** @see #filterX(Vector2[], float[]) */
	public static float[] filterX(Vector2[] vertices) {
		return filterX(vertices, tmpFloatArr);
	}

	/** @return the y values of the given vertices */
	public static float[] filterY(Vector2[] vertices, float[] output) {
		if(output == null || output.length != vertices.length)
			output = new float[vertices.length];
		for(int i = 0; i < output.length; i++)
			output[i] = vertices[i].y;
		return output;
	}

	/** @see #filterY(Vector2[], float[]) */
	public static float[] filterY(Vector2[] vertices) {
		return filterY(vertices, tmpFloatArr);
	}

	/** rotates {@code point} by {@code radians} around [0:0] (local rotation)
	 *  @param point the point to rotate
	 *  @param radians the rotation
	 *  @return the given {@code point} rotated by {@code radians} */
	public static Vector2 rotate(Vector2 point, float radians) {
		// http://stackoverflow.com/questions/1469149/calculating-vertices-of-a-rotated-rectangle
		float xx = point.x, xy = point.y, yx = point.x, yy = point.y;
		xx = xx * cos(radians) - xy * sin(radians);
		yy = yx * sin(radians) + yy * cos(radians);
		return point.set(xx, yy);
	}

	/** rotates a {@code point} around {@code center}
	 *  @param point the point to rotate
	 *  @param center the point around which to rotate {@code point}
	 *  @param radians the rotation
	 *  @return the given {@code point} rotated around {@code center} by {@code radians}
	 *  @see #rotate(Vector2, float) */
	public static Vector2 rotate(Vector2 point, Vector2 center, float radians) {
		return rotate(point, radians).add(center);
	}

	/** @param vector2s the Vector2[] to convert to a float[]
	 *  @return the float[] converted from the given Vector2[] */
	public static float[] toFloatArray(Vector2[] vector2s, float[] output) {
		if(output == null || output.length != vector2s.length * 2)
			output = new float[vector2s.length * 2];

		for(int i = 0, vi = -1; i < output.length; i++)
			if(i % 2 == 0)
				output[i] = vector2s[++vi].x;
			else
				output[i] = vector2s[vi].y;

		return output;
	}

	/** @see #toFloatArray(Vector2[], float[]) */
	public static float[] toFloatArray(Vector2[] vector2s) {
		return toFloatArray(vector2s, tmpFloatArr);
	}

	/** @param floats the float[] to convert to a Vector2[]
	 *  @return the Vector2[] converted from the given float[] */
	public static Vector2[] toVector2Array(float[] floats, Vector2[] output) {
		if(floats.length % 2 != 0)
			throw new IllegalArgumentException("the float array's length is not dividable by two, so it won't make up a Vector2 array: " + floats.length);

		if(output == null || output.length != floats.length / 2) {
			output = new Vector2[floats.length / 2];
			for(int i = 0; i < output.length; i++)
				output[i] = new Vector2();
		}

		for(int i = 0, fi = -1; i < output.length; i++)
			output[i].set(floats[++fi], floats[++fi]);

		return output;
	}

	/** @see #toVector2Array(float[], Vector2[]) */
	public static Vector2[] toVector2Array(float[] floats) {
		return toVector2Array(floats, tmpVecArr);
	}

	/** @param vertexCount the number of vertices for each {@link Polygon}
	 *  @see #toPolygonArray(Vector2[], int[]) */
	public static Polygon[] toPolygonArray(Vector2[] vertices, int vertexCount) {
		int[] vertexCounts = new int[vertices.length / vertexCount];
		for(int i = 0; i < vertexCounts.length; i++)
			vertexCounts[i] = vertexCount;
		return toPolygonArray(vertices, vertexCounts);
	}

	/** @param vertices the vertices which should be split into a {@link Polygon} array
	 *  @param vertexCounts the number of vertices of each {@link Polygon}
	 *  @return the {@link Polygon} array extracted from the vertices */
	public static Polygon[] toPolygonArray(Vector2[] vertices, int[] vertexCounts) {
		Polygon[] polygons = new Polygon[vertexCounts.length];

		int vertice = -1;
		for(int i = 0; i < polygons.length; i++) {
			tmpVecArr = new Vector2[vertexCounts[i]];
			for(int i2 = 0; i2 < tmpVecArr.length; i2++)
				tmpVecArr[i2] = vertices[++vertice];
			polygons[i] = new Polygon(toFloatArray(tmpVecArr));
		}

		return polygons;
	}

	/** @param polygon the polygon, assumed to be simple
	 *  @return if the vertices are in clockwise order */
	public static boolean areVerticesClockwise(Polygon polygon) {
		return polygon.area() < 0;
	}

	/** @see #areVerticesClockwise(Polygon) */
	public static boolean areVerticesClockwise(float[] vertices) {
		if(vertices.length <= 4)
			return true;
		return area(vertices) < 0;
	}

	/** @see #isConvex(Vector2[]) */
	public static boolean isConvex(float[] vertices) {
		return isConvex(toVector2Array(vertices));
	}

	/** @see #isConvex(Vector2[]) */
	public static boolean isConvex(Polygon polygon) {
		return isConvex(polygon.getVertices());
	}

	/** @return the area of the polygon */
	public static float area(float[] vertices) {
		// from com.badlogic.gdx.math.Polygon#area()
		float area = 0;

		int x1, y1, x2, y2;
		for(int i = 0; i < vertices.length; i += 2) {
			x1 = i;
			y1 = i + 1;
			x2 = (i + 2) % vertices.length;
			y2 = (i + 3) % vertices.length;

			area += vertices[x1] * vertices[y2];
			area -= vertices[x2] * vertices[y1];
		}

		return area /= 2;
	}

	/** @param vertices the vertices of the polygon to examine for convexity
	 *  @return if the polygon is convex */
	public static boolean isConvex(Vector2[] vertices) {
		// http://www.sunshine2k.de/coding/java/Polygon/Convex/polygon.htm
		Vector2 p, v = vec2_1, u;
		float res = 0;
		for(int i = 0; i < vertices.length; i++) {
			p = vertices[i];
			vec2_0.set(vertices[(i + 1) % vertices.length]);
			v.x = vec2_0.x - p.x;
			v.y = vec2_0.y - p.y;
			u = vertices[(i + 2) % vertices.length];

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

}
