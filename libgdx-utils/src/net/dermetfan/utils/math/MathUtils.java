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

/** math utility methods */
public class MathUtils {

	/** @return value, min or max */
	public static float clamp(float value, float min, float max) {
		if(value < min)
			return min;
		if(value > max)
			return max;
		return value;
	}

	/** @return {@code replacement} if {@code value} is NaN */
	public static float replaceNaN(float value, float replacement) {
		return Float.isNaN(value) ? replacement : value;
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
			max = f > max ? f : max;
		return max;
	}

	/** @return the smallest element of the given array */
	public static float min(float[] floats) {
		float min = Float.POSITIVE_INFINITY;
		for(float f : floats)
			min = f < min ? f : min;
		return min;
	}

	/** @param value the desired value
	 *  @param values the values to inspect
	 *  @param range values out of this range will not be returned
	 *  @return the nearest to value in values, {@code NaN} if none is found */
	public static float nearest(float value, float[] values, float range) {
		float diff, smallestDiff = Float.POSITIVE_INFINITY, nearest = Float.NaN;

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