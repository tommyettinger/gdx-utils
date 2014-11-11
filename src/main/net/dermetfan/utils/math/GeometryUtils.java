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

import static net.dermetfan.utils.math.MathUtils.mirror;

/** geometric calculation utility methods
 *  @author dermetfan
 *  @since 0.5.0 */
public class GeometryUtils {

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

	/** @param vertices the vertices to add the given values to
	 *  @param x the x value to add
	 *  @param y the y value to add
	 *  @return the given vertices for chaining */
	public static float[] add(float[] vertices, float x, float y, int offset, int length) {
		for(int i = offset + 1; i < offset + length; i += 2) {
			vertices[i - 1] += x;
			vertices[i] += y;
		}
		return vertices;
	}

	/** @see #add(float[], float, float, int, int) */
	public static float[] sub(float[] items, float x, float y, int offset, int length) {
		return add(items, -x, -y, offset, length);
	}

	/** @see #add(float[], float, float, int, int) */
	public static float[] addX(float[] items, float value, int offset, int length) {
		return add(items, value, 0, offset, length);
	}

	/** @see #add(float[], float, float, int, int) */
	public static float[] addY(float[] items, float value, int offset, int length) {
		return add(items, 0, value, offset, length);
	}

	/** @see #sub(float[], float, float, int, int) */
	public static float[] subX(float[] items, float value, int offset, int length) {
		return sub(items, value, 0, offset, length);
	}

	/** @see #sub(float[], float, float, int, int) */
	public static float[] subY(float[] items, float value, int offset, int length) {
		return sub(items, 0, value, offset, length);
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
		float theta = com.badlogic.gdx.math.MathUtils.atan2(height, width);
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

}
