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

import static net.dermetfan.utils.ArrayUtils.checkRegion;

/** math utility methods
 *  @author dermetfan */
public class MathUtils {

	/** @param value the value to clamp
	 *  @param min the min value
	 *  @param max the max value
	 *  @return the value clamped in between min and max */
	public static float clamp(float value, float min, float max) {
		if(value < min)
			return min;
		if(value > max)
			return max;
		return value;
	}

	/** @param value the value to clamp
	 *  @param min the min value
	 *  @param max the max value
	 *  @return the value clamped in between min and max */
	public static int clamp(int value, int min, int max) {
		if(value < min)
			return min;
		if(value > max)
			return max;
		return value;
	}

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

	/** Normalizes/repeats the given value in the given interval [min, max] as if min and max were portals the value travels through. For example (min and max are both inclusive):<br>
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
	 *  min may be greater than max - if so, they will be swapped.
	 *  @param value the value to normalize in the interval [min, max]
	 *  @param min the minimum
	 *  @param max the maximum
	 *  @param minExclusive whether the minimum is exclusive
	 *  @param maxExclusive whether the maximum is exclusive
	 *  @return the value repeated in the interval [min, max]
	 *  @throws IllegalArgumentException if both minExclusive and maxExclusive are true */
	public static float normalize(float value, float min, float max, boolean minExclusive, boolean maxExclusive) {
		if(minExclusive && maxExclusive)
			throw new IllegalArgumentException("min and max cannot both be exclusive");
		if(min == max)
			return min;
		if(min > max) {
			float oldMin = min;
			min = max;
			max = oldMin;
		}
		float over = value > max ? value - max : 0;
		if(over > 0)
			return normalize(min + over, min, max, minExclusive, maxExclusive);
		float under = value < min ? min - value : 0;
		if(under > 0)
			return normalize(max - under, min, max, minExclusive, maxExclusive);
		if(maxExclusive && value == max)
			return min;
		if(minExclusive && value == min)
			return max;
		return value;
	}

	/** @see #normalize(float, float, float, boolean, boolean) */
	public static int normalize(int value, int min, int max, boolean minExclusive, boolean maxExclusive) {
		if(minExclusive && maxExclusive)
			throw new IllegalArgumentException("min and max cannot both be exclusive");
		if(min == max)
			return min;
		int over = value > max ? value - max : 0;
		if(over > 0)
			return normalize(min + over, min, max, minExclusive, maxExclusive);
		int under = value < min ? min - value : 0;
		if(under > 0)
			return normalize(max - under, min, max, minExclusive, maxExclusive);
		if(maxExclusive && value == max)
			return min;
		if(minExclusive && value == min)
			return max;
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
	public static float[] clamp(float[] items, int offset, int length, float min, float max) {
		for(int i = offset; i < offset + length; i++)
			items[i]= clamp(items[i], min, max);
		return items;
	}

	/** @see #clamp(float[], int, int, float, float) */
	public static float[] clamp(float[] items, float min, float max) {
		return clamp(items, 0, items.length, min, max);
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
	public static float[] add(float[] items, int offset, int length, float value) {
		for(int i = offset; i < offset + length; i++)
			items[i] += value;
		return items;
	}

	/** @see #add(float[], int, int, float) */
	public static float[] add(float[] items, float value) {
		return add(items, 0, items.length, value);
	}

	/** @return the given values with the given value subtracted from each element */
	public static float[] sub(float[] items, int offset, int length, float value) {
		return add(items, offset, length, -value);
	}

	/** @see #sub(float[], int, int, float) */
	public static float[] sub(float[] items, float value) {
		return sub(items, 0, items.length, value);
	}

	/** @return the given values with each element multiplied with the given factor */
	public static float[] mul(float[] items, int offset, int length, float factor) {
		for(int i = offset; i < offset + length; i++)
			items[i] *= factor;
		return items;
	}

	/** @see #mul(float[], int, int, float) */
	public static float[] mul(float[] items, float factor) {
		return mul(items, 0, items.length, factor);
	}

	/** @return the given values with each element divided by the given divisor */
	public static float[] div(float[] items, int offset, int length, float divisor) {
		return mul(items, offset, length, 1 / divisor);
	}

	/** @see #div(float[], int, int, float) */
	public static float[] div(float[] items, float divisor) {
		return div(items, 0, items.length, divisor);
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
	public static float amplitude2(float[] items, int offset, int length) {
		return max(items, offset, length) - min(items, offset, length);
	}

	/** @see #amplitude2(float[], int, int) */
	public static float amplitude2(float[] items) {
		return amplitude2(items, 0, items.length);
	}

	/** @return the largest element of the given array */
	public static float max(float[] items, int offset, int length) {
		checkRegion(items, offset, length);
		if(length == 0)
			return Float.NaN;
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
		checkRegion(items, offset, length);
		if(length == 0)
			return Float.NaN;
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
	 *  @param minDiff the minimal difference to the given value
	 *  @return the nearest to value in values, {@code NaN} if none is found */
	public static float nearest(float value, float minDiff, float[] items, int offset, int length) {
		if(value == Float.POSITIVE_INFINITY)
			return max(items, offset, length);
		if(value == Float.NEGATIVE_INFINITY)
			return min(items, offset, length);

		float smallestDiff = Float.POSITIVE_INFINITY, nearest = Float.NaN;
		for(int i = offset; i < offset + length; i++) {
			float diff = Math.abs(value - items[i]);
			if(diff < minDiff)
				continue;
			if(diff < smallestDiff) {
				smallestDiff = diff;
				nearest = items[i];
			}
		}
		return nearest;
	}

	/** @see #nearest(float, float, float[], int, int) */
	public static float nearest(float value, float minDiff, float[] items) {
		return nearest(value, minDiff, items, 0, items.length);
	}

	/** @return the nearest to value in values
	 *  @see #nearest(float, float, float[], int, int) */
	public static float nearest(float value, float[] items, int offset, int length) {
		return nearest(value, 0, items, offset, length);
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
	public static float[] scale(float[] items, int offset, int length, float min, float max) {
		float tmp = amplitude2(items, offset, length) / (max - min);
		for(int i = offset; i < offset + length; i++)
			items[i] /= tmp;

		tmp = min - min(items, offset, length);
		for(int i = offset; i < offset + length; i++)
			items[i] += tmp;

		return items;
	}

	/** @see #scale(float[], int, int, float, float) */
	public static float[] scale(float[] items, float min, float max) {
		return scale(items, 0, items.length, min, max);
	}

}
