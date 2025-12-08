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

package net.dermetfan.utils.math;

import net.dermetfan.utils.ArrayUtils;

import static net.dermetfan.utils.ArrayUtils.checkRegion;
import static net.dermetfan.utils.math.MathUtils.amplitude2;
import static net.dermetfan.utils.math.MathUtils.max;
import static net.dermetfan.utils.math.MathUtils.min;
import static net.dermetfan.utils.math.MathUtils.mirror;

/** geometric calculation utility methods
 *  @author dermetfan
 *  @since 0.5.0 */
public class GeometryUtils {

	/** temporary float array for internal usage */
	private static float[] floats = new float[Byte.MAX_VALUE];

	/** @param floats the {@link #floats} to set */
	public static void setFloats(float[] floats) {
		GeometryUtils.floats = floats;
	}

	/** @return the {@link #floats} */
	public static float[] getFloats() {
		return floats;
	}

	/** @param x the x of the point to test
	 *  @param y the y of the point to test
	 *  @param aX the x of the first point of the segment
	 *  @param aY the y of the first point of the segment
	 *  @param bX the x of the second point of the segment
	 *  @param bY the y of the second point of the segment
	 *  @param inclusive if the given point is allowed to be equal to min or maxs
	 *  @return if the given point lies on a line with and between the given points */
	public static boolean between(float x, float y, float aX, float aY, float bX, float bY, boolean inclusive) {
		return MathUtils.det(x, y, aX, aY, bX, bY) == 0 && MathUtils.between(x, aX, bX, inclusive) && MathUtils.between(y, aY, bY, inclusive);
	}

	/** @return if the given point is between a and b (inclusive)
	 *  @see #between(float, float, float, float, float, float, boolean) */
	public static boolean between(float x, float y, float aX, float aY, float bX, float bY) {
		return between(x, y, aX, aY, bX, bY, true);
	}

	/** @param items the items to add the given values to
	 *  @param x the x value to add
	 *  @param y the y value to add
	 *  @return the given items for chaining */
	public static float[] add(float[] items, int offset, int length, float x, float y) {
		for(int i = offset + 1; i < offset + length; i += 2) {
			items[i - 1] += x;
			items[i] += y;
		}
		return items;
	}

	/** @see #add(float[], int, int, float, float) */
	public static float[] add(float[] vertices, float x, float y) {
		return add(vertices, 0, vertices.length, x, y);
	}

	/** @see #add(float[], int, int, float, float) */
	public static float[] sub(float[] items, int offset, int length, float x, float y) {
		return add(items, offset, length, -x, -y);
	}

	/** @see #sub(float[], int, int, float, float) */
	public static float[] sub(float[] items, float x, float y) {
		return sub(items, 0, items.length, x, y);
	}

	/** @see #add(float[], int, int, float, float) */
	public static float[] addX(float[] items, int offset, int length, float value) {
		return add(items, offset, length, value, 0);
	}

	/** @see #addX(float[], int, int, float) */
	public static float[] addX(float[] items, float value) {
		return addX(items, 0, items.length, value);
	}

	/** @see #add(float[], int, int, float, float) */
	public static float[] addY(float[] items, int offset, int length, float value) {
		return add(items, offset, length, 0, value);
	}

	/** @see #addY(float[], int, int, float) */
	public static float[] addY(float[] items, float value) {
		return addY(items, 0, items.length, value);
	}

	/** @see #sub(float[], int, int, float, float) */
	public static float[] subX(float[] items, int offset, int length, float value) {
		return sub(items, offset, length, value, 0);
	}

	/** @see #subX(float[], int, int, float) */
	public static float[] subX(float[] items, float value) {
		return subX(items, 0, items.length, value);
	}

	/** @see #sub(float[], int, int, float, float) */
	public static float[] subY(float[] items, int offset, int length, float value) {
		return sub(items, offset, length, 0, value);
	}

	/** @see #subY(float[], int, int, float) */
	public static float[] subY(float[] items, float value) {
		return subY(items, 0, items.length, value);
	}

	/** @param items the items to multiply
	 *  @param factorX the factor for x coordinates
	 *  @param factorY the factor for y coordinates
	 *  @return the given items for chaining */
	public static float[] mul(float[] items, int offset, int length, float factorX, float factorY) {
		for(int i = offset; i < offset + length; i += 2) {
			items[i] *= factorX;
			items[i + 1] *= factorY;
		}
		return items;
	}

	/** @see #mul(float[], int, int, float, float) */
	public static float[] mul(float[] items, float factorX, float factorY) {
		return mul(items, 0, items.length, factorX, factorY);
	}

	/** @see #mul(float[], int, int, float, float) */
	public static float[] mulX(float[] items, int offset, int length, float factor) {
		return mul(items, offset, length, factor, 1);
	}

	/** @see #mulX(float[], int, int, float) */
	public static float[] mulX(float[] items, float factor) {
		return mulX(items, 0, items.length, factor);
	}

	/** @see #mul(float[], int, int, float, float) */
	public static float[] mulY(float[] items, int offset, int length, float factor) {
		return mul(items, offset, length, 1, factor);
	}

	/** @see #mulY(float[], int, int, float) */
	public static float[] mulY(float[] items, float factor) {
		return mulY(items, 0, items.length, factor);
	}

	/** @param items the items to divide
	 *  @param divisorX the divisor for x coordinates
	 *  @param divisorY the divisor for y coordinates
	 *  @return the given items for chaining */
	public static float[] div(float[] items, int offset, int length, float divisorX, float divisorY) {
		for(int i = offset; i < offset + length; i += 2) {
			items[i] /= divisorX;
			items[i + 1] /= divisorY;
		}
		return items;
	}

	/** @see #div(float[], int, int, float, float) */
	public static float[] div(float[] items, float divisorX, float divisorY) {
		return div(items, 0, items.length, divisorX, divisorY);
	}

	/** @see #div(float[], int, int, float, float) */
	public static float[] divX(float[] items, int offset, int length, float divisor) {
		return div(items, offset, length, divisor, 1);
	}

	/** @see #divX(float[], int, int, float) */
	public static float[] divX(float[] items, float divisor) {
		return divX(items, 0, items.length, divisor);
	}

	/** @see #div(float[], int, int, float, float) */
	public static float[] divY(float[] items, int offset, int length, float divisor) {
		return div(items, offset, length, 1, divisor);
	}

	/** @see #divY(float[], int, int, float) */
	public static float[] divY(float[] items, float divisor) {
		return divY(items, 0, items.length, divisor);
	}

	/** @param vertices the vertices which width to get
	 *  @return the width of the given vertices */
	public static float width(float[] vertices, int offset, int length) {
		return amplitude2(filterX(vertices, offset, length, floats), 0, length / 2);
	}

	/** @see #width(float[], int, int) */
	public static float width(float[] vertices) {
		return width(vertices, 0, vertices.length);
	}

	/** @param vertices the vertices which height to get
	 *  @return the height of the given vertices */
	public static float height(float[] vertices, int offset, int length) {
		return amplitude2(filterY(vertices, offset, length, floats), 0, length / 2);
	}

	/** @see #height(float[], int, int) */
	public static float height(float[] vertices) {
		return height(vertices, 0, vertices.length);
	}

	/** @param vertices the vertices which depth to get
	 *  @return the depth of the given vertices */
	public static float depth(float[] vertices, int offset, int length) {
		return amplitude2(filterZ(vertices, offset, length, floats), 0, length / 3);
	}

	/** @see #depth(float[], int, int) */
	public static float depth(float[] vertices) {
		return depth(vertices, 0, vertices.length);
	}

	/** @param vertices the vertices
	 *  @param dest the array to fill
	 *  @return the x values of the given vertices */
	public static float[] filterX(float[] vertices, int offset, int length, float[] dest, int destOffset) {
		checkRegion(vertices, offset, length);
		return ArrayUtils.select(vertices, offset, length, -1, 2, dest, destOffset);
	}

	/** @see #filterX(float[], int, int, float[], int) */
	public static float[] filterX(float[] vertices, int offset, int length, float[] dest) {
		return filterX(vertices, offset, length, dest, 0);
	}

	/** @see #filterX(float[], int, int, float[]) */
	public static float[] filterX(float[] vertices, float[] dest) {
		return filterX(vertices, 0, vertices.length, dest);
	}

	/** @see #filterX(float[], int, int, float[]) */
	public static float[] filterX(float[] vertices, int offset, int length) {
		return filterX(vertices, offset, length, new float[length / 2]);
	}

	/** @see #filterX(float[], float[]) */
	public static float[] filterX(float[] vertices) {
		return filterX(vertices, new float[vertices.length / 2]);
	}

	/** @param vertices the vertices
	 *  @param dest the array to fill
	 *  @return the y values of the given vertices */
	public static float[] filterY(float[] vertices, int offset, int length, float[] dest, int destOffset) {
		checkRegion(vertices, offset, length);
		return ArrayUtils.select(vertices, offset, length, 0, 2, dest, destOffset);
	}

	/** @see #filterY(float[], int, int, float[], int) */
	public static float[] filterY(float[] vertices, int offset, int length, float[] dest) {
		return filterY(vertices, offset, length, dest, 0);
	}

	/** @see #filterY(float[], int, int, float[]) */
	public static float[] filterY(float[] vertices, float[] dest) {
		return filterY(vertices, 0, vertices.length, dest);
	}

	/** @see #filterY(float[], int, int, float[]) */
	public static float[] filterY(float[] vertices, int offset, int length) {
		return filterY(vertices, offset, length, new float[length / 2]);
	}

	/** @see #filterY(float[], float[]) */
	public static float[] filterY(float[] vertices) {
		return filterY(vertices, new float[vertices.length / 2]);
	}

	/** @param vertices the vertices
	 *  @param dest the array to fill
	 *  @return the z values of the given vertices */
	public static float[] filterZ(float[] vertices, int offset, int length, float[] dest, int destOffset) {
		checkRegion(vertices, offset, length);
		return ArrayUtils.select(vertices, offset, length, 0, 3, dest, destOffset);
	}

	/** @see #filterZ(float[], int, int, float[], int) */
	public static float[] filterZ(float[] vertices, int offset, int length, float[] dest) {
		return filterZ(vertices, offset, length, dest, 0);
	}

	/** @see #filterZ(float[], int, int, float[]) */
	public static float[] filterZ(float[] vertices, float[] dest) {
		return filterZ(vertices, 0, vertices.length, dest);
	}

	/** @see #filterZ(float[], int, int, float[]) */
	public static float[] filterZ(float[] vertices, int offset, int length) {
		return filterZ(vertices, offset, length, new float[length / 3]);
	}

	/** @see #filterZ(float[], float[]) */
	public static float[] filterZ(float[] vertices) {
		return filterZ(vertices, new float[vertices.length / 3]);
	}

	/** @param vertices the vertices
	 *  @param dest the array to fill
	 *  @return the w values of the given vertices */
	public static float[] filterW(float[] vertices, int offset, int length, float[] dest, int destOffset) {
		checkRegion(vertices, offset, length);
		return ArrayUtils.select(vertices, offset, length, 0, 4, dest, destOffset);
	}

	/** @see #filterW(float[], int, int, float[], int) */
	public static float[] filterW(float[] vertices, int offset, int length, float[] dest) {
		return filterW(vertices, offset, length, dest, 0);
	}

	/** @see #filterW(float[], int, int, float[]) */
	public static float[] filterW(float[] vertices, float[] dest) {
		return filterW(vertices, 0, vertices.length, dest);
	}

	/** @see #filterW(float[], int, int, float[]) */
	public static float[] filterW(float[] vertices, int offset, int length) {
		return filterW(vertices, offset, length, new float[length / 4]);
	}

	/** @see #filterW(float[], float[]) */
	public static float[] filterW(float[] vertices) {
		return filterW(vertices, new float[vertices.length / 4]);
	}

	/** @return the min x value in the given vertices */
	public static float minX(float[] vertices, int offset, int length) {
		return min(filterX(vertices, offset, length, floats), 0, length / 2);
	}

	/** @see #minX(float[], int, int) */
	public static float minX(float[] vertices) {
		return minX(vertices, 0, vertices.length);
	}

	/** @return the min y value in the given vertices */
	public static float minY(float[] vertices, int offset, int length) {
		return min(filterY(vertices, offset, length, floats), 0, length / 2);
	}

	/** @see #minY(float[], int, int) */
	public static float minY(float[] vertices) {
		return minY(vertices, 0, vertices.length);
	}

	/** @return the max x value in the given vertices */
	public static float maxX(float[] vertices, int offset, int length) {
		return max(filterX(vertices, offset, length, floats), 0, length / 2);
	}

	/** @see #maxX(float[], int, int) */
	public static float maxX(float[] vertices) {
		return maxX(vertices, 0, vertices.length);
	}

	/** @return the max y value in the given vertices */
	public static float maxY(float[] vertices, int offset, int length) {
		return max(filterY(vertices, offset, length, floats), 0, length / 2);
	}

	/** @see #maxY(float[], int, int) */
	public static float maxY(float[] vertices) {
		return maxY(vertices, 0, vertices.length);
	}

	/** @return the distance between the two given points
	 *  @since 0.11.0 */
	public static float distance(float x1, float y1, float x2, float y2) {
		return (float) Math.sqrt(distance2(x1, y1, x2, y2));
	}

	/** @return the squared distance between the two given points
	 *  @since 0.11.0 */
	public static float distance2(float x1, float y1, float x2, float y2) {
		float x_dist = x2 - x1, y_dist = y2 - y1;
		return x_dist * x_dist + y_dist * y_dist;
	}

	/** @param vertices the vertices to scale
	 *  @param minX the desired minimal x coordinate
	 *  @param minY the desired minimal y coordinate
	 *  @param maxX the desired maximal x coordinate
	 *  @param maxY the desired maximal y coordinate
	 *  @return the given and scaled vertices for chaining */
	public static float[] scale(float[] vertices, int offset, int length, float minX, float minY, float maxX, float maxY) {
		divX(vertices, offset, length, width(vertices, offset, length) / (maxX - minX));
		addX(vertices, offset, length, minX - minX(vertices, offset, length));
		divY(vertices, offset, length, height(vertices, offset, length) / (maxY - minY));
		addY(vertices, offset, length, minY - minY(vertices, offset, length));
		return vertices;
	}

	/** @see #scale(float[], int, int, float, float, float, float) */
	public static float[] scale(float[] vertices, float minX, float minY, float maxX, float maxY) {
		return scale(vertices, 0, vertices.length, minX, minY, maxX, maxY);
	}

	/** @param vertices the vertices to reverse
	 *  @return the reversed given vertices for chaining */
	public static float[] reverse(float[] vertices, int offset, int length) {
		checkRegion(vertices, offset, length);
		for(int i = offset, ii = offset + length - 2; i < offset + length / 2; i += 2, ii -= 2) {
			float ix = vertices[i], iy = vertices[i + 1];
			float iix = vertices[ii], iiy = vertices[ii + 1];
			vertices[i] = iix;
			vertices[i + 1] = iiy;
			vertices[ii] = ix;
			vertices[ii + 1] = iy;
		}
		return vertices;
	}

	/** @see #reverse(float[], int, int) */
	public static float[] reverse(float[] vertices) {
		return reverse(vertices, 0, vertices.length);
	}

	/** @param vertices the 3D vertices to reverse
	 *  @return the reversed given vertices for chaining */
	public static float[] reverse3D(float[] vertices, int offset, int length) {
		checkRegion(vertices, offset, length);
		for(int i = offset, ii = offset + length - 3; i < offset + length / 2; i += 3, ii -= 3) {
			float ix = vertices[i], iy = vertices[i + 1], iz = vertices[i + 2];
			float iix = vertices[ii], iiy = vertices[ii + 1], iiz = vertices[ii + 2];
			vertices[i] = iix;
			vertices[i + 1] = iiy;
			vertices[i + 2] = iiz;
			vertices[ii] = ix;
			vertices[ii + 1] = iy;
			vertices[ii + 2] = iz;
		}
		return vertices;
	}

	/** @see #reverse3D(float[], int, int) */
	public static float[] reverse3D(float[] vertices) {
		return reverse3D(vertices, 0, vertices.length);
	}

	/** @param x the x coordinate of the point
	 *  @param y the y coordinate of the point
	 *  @param maxDistance2 the max squared distance between the given point and a close point
	 *  @param vertices the vertices to search for close points
	 *  @return the number of points close to the given point
	 *  @since 0.11.0 */
	public static int closePoints(float x, float y, float maxDistance2, float[] vertices, int offset, int length, float[] output, int outputOffset) {
		ArrayUtils.checkRegion(vertices, offset, length);
		int outputIndex = outputOffset;
		for(int i = offset; i < offset + length; i += 2) {
			float pX = vertices[i], pY = vertices[i + 1];
			if(distance2(x, y, pX, pY) <= maxDistance2) {
				if(output != null) {
					output[outputIndex] = pX;
					output[outputIndex + 1] = pY;
				}
				outputIndex += 2;
			}
		}
		return (outputIndex - outputOffset) / 2;
	}

	/** @param x the x coordinate of the point
	 *  @param y the y coordinate of the point
	 *  @param deltaX the max difference between the point's and a close point's x coordinate
	 *  @param deltaY the max difference between the point's and a close point's y coordinate
	 *  @param vertices the vertices to search for close points
	 *  @param output The array to store the close points in. May be null.
	 *  @return the number of points close to the given point
	 *  @since 0.11.0 */
	public static int closePoints(float x, float y, float deltaX, float deltaY, float[] vertices, int offset, int length, float[] output, int outputOffset) {
		ArrayUtils.checkRegion(vertices, offset, length);
		int outputIndex = outputOffset;
		for(int i = offset; i < offset + length; i += 2) {
			float pX = vertices[i], pY = vertices[i + 1];
			if(Math.abs(pX - x) <= deltaX && Math.abs(pY - y) <= deltaY) {
				if(output != null) {
					output[outputIndex] = pX;
					output[outputIndex + 1] = pY;
				}
				outputIndex += 2;
			}
		}
		return (outputIndex - outputOffset) / 2;
	}

	/** @see #sortPoints(float[], int, int, boolean)
	 *  @since 0.11.0 */
	public static void sortPoints(float[] vertices, boolean byY) {
		sortPoints(vertices, 0, vertices.length, byY);
	}

	/** Sorts the given points in ascending order by their x (or, if byY is true, y) coordinate.
	 *  @param vertices the points to sort
	 *  @param byY whether the points shall by sorted by their y rather than their x coordinate
	 *  @since 0.11.0 */
	public static void sortPoints(float[] vertices, int offset, int length, boolean byY) {
		ArrayUtils.checkRegion(vertices, offset, length);
		int y = byY ? 1 : 0;

		// find point with smallest x coordinate
		int smallest = offset;
		for(int i = offset; i < offset + length; i += 2)
			if(vertices[i + y] < vertices[smallest + y])
				smallest = i;

		// find point with greatest x coordinate
		int greatest = offset;
		for(int i = offset; i < offset + length; i += 2)
			if(vertices[i + y] > vertices[greatest + y])
				greatest = i;

		// find each next point
		floats[0] = vertices[smallest];
		floats[1] = vertices[smallest + 1];
		for(int i = smallest, fi = 2; fi < length; fi += 2) {
			int next = greatest;
			float coord = vertices[i + y];
			for(int ii = offset; ii < offset + length; ii += 2) {
				if(ii == i)
					continue;
				float coord2 = vertices[ii + y];
				if(coord2 < coord)
					continue;
				if(coord2 - coord <= vertices[next + y] - coord) {
					int coord2InFloats = closePoints(coord2, coord2, byY ? Float.POSITIVE_INFINITY : 0, byY ? 0 : Float.POSITIVE_INFINITY, floats, 0, fi, null, 0);
					if(coord2InFloats > 0) {
						int coord2InVertices = closePoints(coord2, coord2, byY ? Float.POSITIVE_INFINITY : 0, byY ? 0 : Float.POSITIVE_INFINITY, vertices, offset, length, null, 0);
						if(coord2InFloats >= coord2InVertices)
							continue;
					}
					next = ii;
				}
			}
			floats[fi] = vertices[next];
			floats[fi + 1] = vertices[next + 1];
			i = next;
		}

		System.arraycopy(floats, 0, vertices, offset, length);
	}

	/** @see #arrangeConvexPolygon(float[], int, int, boolean) */
	public static void arrangeConvexPolygon(float[] vertices, boolean clockwise) {
		arrangeConvexPolygon(vertices, 0, vertices.length, clockwise);
	}

	/** @param vertices the vertices of the convex polygon
	 *  @param clockwise if true, the vertices will be arranged in clockwise, otherwise counter-clockwise order */
	public static void arrangeConvexPolygon(float[] vertices, int offset, int length, boolean clockwise) {
		ArrayUtils.checkRegion(vertices, offset, length);
		if(length % 2 != 0)
			throw new IllegalArgumentException("malformed vertices, length is odd: " + length);
		if(length <= 4)
			return;
		sortPoints(vertices, offset, length, false);
		System.arraycopy(vertices, 0, floats, 0, offset + length);
		for(int i = offset + 2, ltI = i, gtI = offset + length - 2; i < offset + length; i += 2) {
			float x = floats[i], y = floats[i + 1];
			float det = MathUtils.det(floats[offset], floats[offset + 1], floats[offset + length - 2], floats[offset + length - 1], x, y);
			if(clockwise ? det > 0 : det < 0) {
				vertices[ltI++] = x;
				vertices[ltI++] = y;
			} else {
				vertices[gtI] = x;
				vertices[gtI + 1] = y;
				gtI -= 2;
			}
		}
	}

	/** @return the area contained by the given simple/non self intersecting polygon */
	public static float polygonArea(float[] vertices, int offset, int length) {
		ArrayUtils.checkRegion(vertices, offset, length);
		if(length % 2 != 0)
			throw new IllegalArgumentException("malformed vertices, length is odd: " + length);
		float area = 0;
		for(int i = offset; i < offset + length; i += 2) {
			float x = vertices[i], y = vertices[i + 1], x2 = vertices[ArrayUtils.repeat(offset, length, i + 2)], y2 = vertices[ArrayUtils.repeat(offset, length, i + 3)];
			area += x * y2;
			area -= y * x2;
		}
		return area / 2;
	}

	/** @return whether the given vertices are in clockwise order */
	public static boolean areVerticesClockwise(float[] vertices, int offset, int length) {
		ArrayUtils.checkRegion(vertices, offset, length);
		return length <= 4 || polygonArea(vertices, offset, length) < 0;
	}

	/** @see #areVerticesClockwise(float[], int, int) */
	public static boolean areVerticesClockwise(float[] vertices) {
		return areVerticesClockwise(vertices, 0, vertices.length);
	}

	/** @see #isConvex(float[], int, int) */
	public static boolean isConvex(float[] vertices) {
		return isConvex(vertices, 0, vertices.length);
	}

	/** @param vertices the polygon
	 *  @return whether the given vertices form a convex polygon */
	public static boolean isConvex(float[] vertices, int offset, int length) {
		ArrayUtils.checkRegion(vertices, offset, length);
		for(int i = offset, direction = 0; i < offset + length; i += 2) {
			float det = MathUtils.det(vertices[i], vertices[i + 1], vertices[ArrayUtils.repeat(offset, length, i + 2)], vertices[ArrayUtils.repeat(offset, length, i + 3)], vertices[ArrayUtils.repeat(offset, length, i + 4)], vertices[ArrayUtils.repeat(offset, length, i + 5)]);
			int dir = det > 0 ? 1 : det < 0 ? -1 : 0;
			if(i != offset && dir != direction)
				return false;
			direction = dir;
		}
		return true;
	}

	/** @param x the x of the rectangle
	 *  @param y the y of the rectangle
	 *  @param width the width of the rectangle
	 *  @param height the height of the rectangle
	 *  @param radians the desired rotation of the rectangle (in radians)
	 *  @param output The array to store the results in. A new one will be created if it is null or its length is less than 8.
	 *  @return the given output array with the rotated vertices as in [x1, y1, x2, y2, x3, y3, x4, y4] starting from the given offset */
	public static float[] rotate(float x, float y, float width, float height, float radians, float[] output, int offset) {
		if(output == null || offset + 8 > output.length - 1)
			output = new float[8];
		// http://www.monkeycoder.co.nz/Community/posts.php?topic=3935
		float rad = (float) (Math.sqrt(height * height + width * width) / 2.);
		float theta = (float) Math.atan2(height, width);
		float x0 = (float) (rad * Math.cos(theta + radians));
		float y0 = (float) (rad * Math.sin(theta + radians));
		float x1 = (float) (rad * Math.cos(-theta + radians));
		float y1 = (float) (rad * Math.sin(-theta + radians));
		float offsetX = x + width / 2, offsetY = y + height / 2;
		output[offset] = offsetX + x0;
		output[offset + 1] = offsetY + y0;
		output[offset + 2] = offsetX + x1;
		output[offset + 3] = offsetY + y1;
		output[offset + 4] = offsetX - x0;
		output[offset + 5] = offsetY - y0;
		output[offset + 6] = offsetX - x1;
		output[offset + 7] = offsetY - y1;
		return output;
	}

	/** @param coord the position of the object
	 *  @param axisSize the size of the axis
	 *  @return the position of the object on the axis, inverted from going to positive to negative */
	public static float invertAxis(float coord, float axisSize) {
		return mirror(coord, axisSize / 2);
	}

	/** Converts the given vertices to their position on inverted axes.
	 *  @param vertices the vertices to convert
	 *  @param x if the x-axis should be inverted
	 *  @param y if the y-axis should be inverted
	 *  @return the given vertices converted to the inverted axis in their <strong>local</strong> coordinate system */
	public static float[] invertAxes(float[] vertices, int offset, int length, boolean x, boolean y) {
		if(!x && !y)
			return vertices;
		float height = height(vertices, offset, length), width = width(vertices, offset, length);
		for(int i = (x ? 0 : 1) + offset; i < offset + length; i += x ^ y ? 2 : 1)
			vertices[i] = i % 2 == 0 ? invertAxis(vertices[i], width) : invertAxis(vertices[i], height);
		return vertices;
	}

	/** @see #invertAxes(float[], int, int, boolean, boolean) */
	public static float[] invertAxes(float[] vertices, boolean x, boolean y) {
		return invertAxes(vertices, 0, vertices.length, x, y);
	}

	/** inverts the given vertices to a y-down coordinate system and translates them according to their parent coordinate system by their {@link #height(float[]) height}
	 *  @see #invertAxes(float[], boolean, boolean) */
	public static float[] toYDown(float[] vertices, int offset, int length) {
		checkRegion(vertices, offset, length);
		invertAxes(vertices, offset, length, false, true);
		return subY(vertices, offset, length, height(vertices, offset, length));
	}

	/** @see #toYDown(float[], int, int) */
	public static float[] toYDown(float[] vertices) {
		return toYDown(vertices, 0, vertices.length);
	}

	/** inverts the given vertices to a y-up coordinate system and translates them according to their parent coordinate system by their {@link #height(float[]) height}
	 *  @see #invertAxes(float[], boolean, boolean) */
	public static float[] toYUp(float[] vertices, int offset, int length) {
		checkRegion(vertices, offset, length);
		invertAxes(vertices, offset, length, false, true);
		return subY(vertices, offset, length, height(vertices, offset, length));
	}

	/** @see #toYUp(float[], int, int) */
	public static float[] toYUp(float[] vertices) {
		return toYUp(vertices, 0, vertices.length);
	}

}
