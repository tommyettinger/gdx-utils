/** Copyright 2013 Robin Stumm (serverkorken@googlemail.com, http://dermetfan.net/)
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

	/** normalizes a value in a given range using the range as step
	 *  @see #normalize(float, float, float) */
	public static float normalize(float value, float range) {
		// if((value = Math.abs(Math.min(value, max) - Math.max(value, max))) > max) value = normalize(value, max);
		return normalize(value, range, range);
	}

	/** normalizes a value in a given range
	 *  @param value the value to normalize
	 *  @param range the range in which to normalize the given value (from -range to range)
	 *  @param step the step to use to normalize the value
	 *  @return the normalized value */
	public static float normalize(float value, float range, float step) {
		if((range = Math.abs(range)) == 0)
			return range;
		if((step = Math.abs(step)) == 0)
			return value;
		while(value > range)
			value -= step;
		while(value < -range)
			value += step;
		return value;
	}

	/** @return value, min or max */
	public static float clamp(float value, float min, float max) {
		return value < min ? min : value > max ? max : value;
	}

	/** @return {@code replacement} if {@code value} is NaN */
	public static float replaceNaN(float value, float replacement) {
		return Float.isNaN(value) ? replacement : value;
	}

	/** @return the given values with each element being the {@link Math#abs(float) absolute} of its value */
	public static float[] abs(float[] values) {
		for(int i = 0; i < values.length; i++)
			values[i] = Math.abs(values[i]);
		return values;
	}

	/** @return the given values with each element multiplied with the given factor */
	public static float[] mul(float[] values, float factor) {
		for(int i = 0; i < values.length; i++)
			values[i] *= factor;
		return values;
	}

	/** @return the given values with each element divided by the given divisor */
	public static float[] div(float[] values, float divisor) {
		return mul(values, 1 / divisor);
	}

	/** @return the given values with the given value added to each element */
	public static float[] add(float[] values, float value) {
		for(int i = 0; i < values.length; i++)
			values[i] += value;
		return values;
	}

	/** @return the given values with the given value subtracted from each element */
	public static float[] sub(float[] values, float value) {
		return add(values, -value);
	}

	/** @return the sum of all values in the given array */
	public static float sum(float[] values) {
		float sum = 0;
		for(float v : values)
			sum += v;
		return sum;
	}

	/** @return the peak-to-peak amplitude of the given array */
	public static float amplitude(float[] f) {
		return Math.abs(max(f) - min(f));
	}

	/** @return the largest element of the given array */
	public static float max(float[] floats) {
		float max = Float.NEGATIVE_INFINITY;
		for(float f : floats)
			if(f > max)
				max = f;
		return max;
	}

	/** @return the smallest element of the given array */
	public static float min(float[] floats) {
		float min = Float.POSITIVE_INFINITY;
		for(float f : floats)
			if(f < min)
				min = f;
		return min;
	}

	/** @param value the desired value
	 *  @param values the values to inspect
	 *  @param range values out of this range will not be returned
	 *  @return the nearest to value in values, {@code NaN} if none is found */
	public static float nearest(float value, float[] values, float range) {
		float diff, smallestDiff = Float.POSITIVE_INFINITY, nearest = Float.NaN;

		if(value == Float.POSITIVE_INFINITY) {
			float max = max(values);
			if(max - range <= value)
				return max;
			return nearest;
		} else if(value == Float.NEGATIVE_INFINITY) {
			float min = min(values);
			if(min + range >= value)
				return min;
			return nearest;
		}

		for(float candidate : values) {
			if(candidate == value)
				return value;
			if((diff = Math.abs(candidate - value)) < smallestDiff)
				if((smallestDiff = diff) <= range)
					nearest = candidate;
		}

		return nearest;
	}

	/** @return the nearest to value in values
	 *  @see #nearest(float, float[], float) */
	public static float nearest(float value, float[] values) {
		return nearest(value, values, Float.POSITIVE_INFINITY);
	}

	/** scales the given float array to have the given min and max values
	 *  @param values the values to scale
	 *  @param min the desired minimal value in the array
	 *  @param max the desired maximal value in the array
	 *  @param clamp if the values should be clamped to correct floating point inaccuracy
	 *  @return the scaled array */
	public static float[] scale(float[] values, float min, float max, boolean clamp) {
		int i;
		float tmp = amplitude(values) / (max - min);
		for(i = 0; i < values.length; i++)
			values[i] /= tmp;

		tmp = min - min(values);
		for(i = 0; i < values.length; i++)
			values[i] += tmp;

		if(clamp)
			for(i = 0; i < values.length; i++)
				if(values[i] > max)
					values[i] = max;

		return values;
	}

	/** @param sum the sum at which to return the element
	 *  @param values the values to add together to calculate {@code sum}
	 *  @param elements the elements from which to return one when {@code sum} is reached
	 *  @return the element from {@code elements} when {@code sum} was reached by adding the given {@code values} together */
	public static <T> T elementAtSum(float sum, float[] values, T[] elements) {
		float total = 0;
		for(int i = 0; i < values.length; i++)
			if((total += values[i]) >= sum)
				return elements[i];
		return total <= 0 ? elements[0] : elements[elements.length - 1];
	}

}
