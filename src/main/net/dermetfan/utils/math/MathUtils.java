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

/** math utility methods
 *  @author dermetfan */
public class MathUtils {

	/** @param n the number which cross sum to calculate
	 *  @return the cross sum (sum of a number's digits) */
	public static int crossSum(int n) {
		int csum = 0;
		while(n > 0) {
			csum += n % 10;
			n /= 10;
		}
		return csum;
	}

	/** int wrapper for {@link #factorial(float)}
	 *  @see #factorial(float) */
	public static int factorial(int n) {
		return (int) factorial((float) n);
	}

	/** @param n the number to find the factorial for
	 *  @return the factorial of the given number */
	public static float factorial(float n) {
		if(n < 0)
			throw new IllegalArgumentException("n must be >= 0: " + n);
		return n <= 1 ? 1 : n * factorial(n - 1);
	}

	/** @return if the given value is in between min and max (exclusive)
	 *  @see #between(float, float, float, boolean) */
	public static boolean between(float value, float min, float max) {
		return between(value, min, max, false);
	}

	/** min and max will be swapped if they are given in the wrong order
	 *  @param inclusive if the given value is allowed to be equal to min or max
	 *  @return if the given value is in between min and max */
	public static boolean between(float value, float min, float max, boolean inclusive) {
		min = Math.min(min, max);
		max = Math.max(min, max);
		return inclusive ? value >= min && value <= max : value > min && value < max;
	}

	/** @return the determinant of the given 3x3 matrix */
	public static float det(float x1, float y1, float x2, float y2, float x3, float y3) {
		return x1 * y2 + x2 * y3 + x3 * y1 - y1 * x2 - y2 * x3 - y3 * x1;
	}

	/** @param value the value to normalize between 0 and the given range
	 *  @param range the other bound of the interval with 0
	 *  @see #normalize(float, float, float) */
	public static float normalize(float value, float range) {
		return normalize(value, 0, range);
	}

	/** Normalizes/repeats the given value in the given interval [min, max] as if min and max were portals the value travels through. For example:<br>
	 *  <table summary="examples">
	 *      <tr>
	 *          <th>value</th>
	 *          <th>min</th>
	 *          <th>max</th>
	 *          <th>result</th>
	 *      </tr>
	 *      <tr>
	 *          <td>150</td>
	 *          <td>0</td>
	 *          <td>100</td>
	 *          <td>50</td>
	 *      </tr>
	 *      <tr>
	 *          <td>200</td>
	 *          <td>-100</td>
	 *          <td>100</td>
	 *          <td>0</td>
	 *      </tr>
	 *      <tr>
	 *          <td>50</td>
	 *          <td>0</td>
	 *          <td>100</td>
	 *          <td>50</td>
	 *      </tr>
	 *  </table>
	 *  @param value the value to normalize in the interval [min, max]
	 *  @param min the minimum
	 *  @param max the maximum
	 *  @return the value repeated in the interval [min, max] */
	public static float normalize(float value, float min, float max) {
		if(min == max)
			return min;
		float oldMin = min, oldMax = max;
		min = Math.min(min, max);
		max = Math.max(oldMin, max);
		float under = value < min ? Math.abs(min - value) : 0, over = value > max ? value - Math.abs(max) : 0;
		if(under > 0)
			return normalize(oldMax + (oldMax > oldMin ? -under : under), min, max);
		if(over > 0)
			return normalize(oldMin + (oldMin < oldMax ? over : -over), min, max);
		return value;
	}

	/** @param value the value to mirror
	 *  @param baseline the baseline of symmetry
	 *  @return the value mirrored at baseline */
	public static float mirror(float value, float baseline) {
		return baseline * 2 - value;
	}

	/** @return {@code replacement} if {@code value} is NaN */
	public static float replaceNaN(float value, float replacement) {
		return Float.isNaN(value) ? replacement : value;
	}

	/** @param sum the sum at which to return the element
	 *  @param values the values to add together to calculate {@code sum}
	 *  @param elements the elements from which to return one when {@code sum} is reached
	 *  @return the element from {@code elements} when {@code sum} was reached by adding the given {@code values} together */
	public static <T> T elementAtSum(float sum, float[] values, T[] elements, int valuesOffset, int valuesLength, int elementsOffset, int elementsLength) {
		float total = 0;
		for(int i = valuesOffset; i < valuesOffset + valuesLength; i++)
			if((total += values[i]) >= sum)
				return elements[elementsOffset + i - valuesOffset];
		return total <= 0 ? elements[elementsOffset] : elements[elementsOffset + elementsLength - 1];
	}

	/** @see #elementAtSum(float, float[], Object[], int, int, int, int) */
	public static <T> T elementAtSum(float sum, float[] values, T[] elements) {
		return elementAtSum(sum, values, elements, 0, values.length, 0, elements.length);
	}

	/** @return the given array clamped to min and max */
	public static float[] clamp(float[] items, float min, float max, int offset, int length) {
		for(int i = offset; i < offset + length; i++)
			items[i]= com.badlogic.gdx.math.MathUtils.clamp(items[i], min, max);
		return items;
	}

	/** @see #clamp(float[], float, float, int, int) */
	public static float[] clamp(float[] items, float min, float max) {
		return clamp(items, min, max, 0, items.length);
	}

	/** @return the given values with each element being the {@link Math#abs(float) absolute} of its value */
	public static float[] abs(float[] items, int offset, int length) {
		for(int i = offset; i < offset + length; i++)
			items[i] = Math.abs(items[i]);
		return items;
	}

	/** @see #abs(float[], int, int) */
	public static float[] abs(float[] items) {
		return abs(items, 0, items.length);
	}

	/** @return the given values with the given value added to each element */
	public static float[] add(float[] items, float value, int offset, int length) {
		for(int i = offset; i < offset + length; i++)
			items[i] += value;
		return items;
	}

	/** @see #add(float[], float, int, int) */
	public static float[] add(float[] items, float value) {
		return add(items, value, 0, items.length);
	}

	/** @return the given values with the given value subtracted from each element */
	public static float[] sub(float[] items, float value, int offset, int length) {
		return add(items, -value, offset, length);
	}

	/** @see #sub(float[], float, int, int) */
	public static float[] sub(float[] items, float value) {
		return sub(items, value, 0, items.length);
	}

	/** @return the given values with each element multiplied with the given factor */
	public static float[] mul(float[] items, float factor, int offset, int length) {
		for(int i = offset; i < offset + length; i++)
			items[i] *= factor;
		return items;
	}

	/** @see #mul(float[], float, int, int) */
	public static float[] mul(float[] items, float factor) {
		return mul(items, factor, 0, items.length);
	}

	/** @return the given values with each element divided by the given divisor */
	public static float[] div(float[] items, float divisor, int offset, int length) {
		return mul(items, 1 / divisor, offset, length);
	}

	/** @see #div(float[], float, int, int) */
	public static float[] div(float[] items, float divisor) {
		return div(items, divisor, 0, items.length);
	}

	/** @param value the value from which to start
	 *  @param amount the amount by which to approach zero
	 *  @return the given value modified into the direction of zero by the given amount */
	public static float approachZero(float value, float amount) {
		amount = Math.abs(amount);
		if(amount > Math.abs(value) || value == 0)
			return 0;
		return value - (value > 0 ? amount : -amount);
	}

	/** @return the sum of all values in the given array */
	public static float sum(float[] items, int offset, int length) {
		float sum = 0;
		for(int i = offset; i < offset + length; i++)
			sum += items[i];
		return sum;
	}

	/** @see #sum(float[], int, int) */
	public static float sum(float[] items) {
		return sum(items, 0, items.length);
	}

	/** @return the peak-to-peak amplitude of the given array */
	public static float amplitude(float[] items, int offset, int length) {
		return max(items, offset, length) - min(items, offset, length);
	}

	/** @see #amplitude(float[], int, int) */
	public static float amplitude(float[] items) {
		return amplitude(items, 0, items.length);
	}

	/** @return the largest element of the given array */
	public static float max(float[] items, int offset, int length) {
		float max = Float.NEGATIVE_INFINITY;
		for(int i = offset; i < offset + length; i++) {
			float f = items[i];
			if(f > max)
				max = f;
		}
		return max;
	}

	/** @see #max(float[], int, int) */
	public static float max(float[] items) {
		return max(items, 0, items.length);
	}

	/** @return the smallest element of the given array */
	public static float min(float[] items, int offset, int length) {
		float min = Float.POSITIVE_INFINITY;
		for(int i = offset; i < offset + length; i++) {
			float f = items[i];
			if(f < min)
				min = f;
		}
		return min;
	}

	/** @see #min(float[], int, int) */
	public static float min(float[] items) {
		return min(items, 0, items.length);
	}

	/** @param value the desired value
	 *  @param items the values to inspect
	 *  @param range values out of this range will not be returned
	 *  @return the nearest to value in values, {@code NaN} if none is found */
	public static float nearest(float value, float[] items, float range, int offset, int length) {
		float diff, smallestDiff = Float.POSITIVE_INFINITY, nearest = Float.NaN;

		if(value == Float.POSITIVE_INFINITY) {
			float max = max(items, offset, length);
			if(max - range <= value)
				return max;
			return nearest;
		} else if(value == Float.NEGATIVE_INFINITY) {
			float min = min(items, offset, length);
			if(min + range >= value)
				return min;
			return nearest;
		}

		for(int i = offset; i < offset + length; i++) {
			float candidate = items[i];
			if(candidate == value)
				return value;
			if((diff = Math.abs(candidate - value)) < smallestDiff)
				if((smallestDiff = diff) <= range)
					nearest = candidate;
		}

		return nearest;
	}

	/** @see #nearest(float, float[], float, int, int) */
	public static float nearest(float value, float[] items, float range) {
		return nearest(value, items, range, 0, items.length);
	}

	/** @return the nearest to value in values
	 *  @see #nearest(float, float[], float, int, int) */
	public static float nearest(float value, float[] items, int offset, int length) {
		return nearest(value, items, Float.POSITIVE_INFINITY, offset, length);
	}

	/** @see #nearest(float, float[], int, int) */
	public static float nearest(float value, float[] items) {
		return nearest(value, items, 0, items.length);
	}

	/** scales the given float array to have the given min and max values
	 *  @param items the values to scale
	 *  @param min the desired minimal value in the array
	 *  @param max the desired maximal value in the array
	 *  @return the scaled array */
	public static float[] scale(float[] items, float min, float max, int offset, int length) {
		float tmp = amplitude(items, offset, length) / (max - min);
		for(int i = offset; i < offset + length; i++)
			items[i] /= tmp;

		tmp = min - min(items, offset, length);
		for(int i = offset; i < offset + length; i++)
			items[i] += tmp;

		return items;
	}

	/** @see #scale(float[], float, float, int, int) */
	public static float[] scale(float[] items, float min, float max) {
		return scale(items, min, max, 0, items.length);
	}

}
