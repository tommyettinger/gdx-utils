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

package net.dermetfan.utils;

import net.dermetfan.utils.math.Noise;

/** array utility methods
 *  @author dermetfan */
public abstract class ArrayUtils {

	/** @param array the array from which to access a value at the wrapped index
	 *  @return the value at the wrapped index
	 *  @see #wrapIndex(int, int) */
	public static <T> T wrapIndex(int index, T[] array) {
		return array[wrapIndex(index, array.length)];
	}

	/** @see #wrapIndex(int, Object[]) */
	public static int wrapIndex(int index, int[] array) {
		return array[wrapIndex(index, array.length)];
	}

	/** @see #wrapIndex(int, Object[]) */
	public static float wrapIndex(int index, float[] array) {
		return array[wrapIndex(index, array.length)];
	}

	/** Wraps the given index around the given length (of an array). For example for a length of 10:<br>
	 * 	<table summary="index and return value">
	 * 	<tr><th>index</th><th>returns</th></tr>
	 * 	<tr><td>0</td><td>0</td></tr>
	 * 	<tr><td>5</td><td>5</td></tr>
	 * 	<tr><td>10</td><td>0</td></tr>
	 * 	<tr><td>15</td><td>5</td></tr>
	 * 	<tr><td>20</td><td>0</td></tr>
	 * 	<tr><td>55</td><td>5</td></tr>
	 * 	</table>
	 *  @param index the desired index
	 *  @param length the length of the array
	 *  @return the index wrapped around the array length */
	public static int wrapIndex(int index, int length) {
		return (index + length) % length;
	}

	/** @param array the array that may contain the given value
	 *  @param value the value to search for in the given array
	 *  @param identity if {@code ==} comparison should be used instead of <code>{@link Object#equals(Object) .equals()}</code>
	 *  @return if the given value is contained in the given array */
	public static <T> boolean contains(T[] array, T value, boolean identity) {
		int i = array.length - 1;
		if(identity) {
			while(i >= 0)
				if(array[i--] == value)
					return true;
		} else
			while(i >= 0)
				if(array[i--].equals(value))
					return true;
		return false;
	}

	/** shuffles the given array using {@link Noise#random(float, float)}, so the {@link Noise#setSeed(long) seed} influences the result
	 *  @param array the array to shuffle */
	public static void shuffle(Object[] array) {
		for(int i = 0; i < array.length; i++) {
			int ii = (int) Noise.random(0, i + 1);
			Object temp = array[i];
			array[i] = array[ii];
			array[ii] = temp;
		}
	}

	/** @return an array of the unboxed values from the given values
	 *  @see #box(float[]) */
	public static float[] unbox(Float[] values) {
		float[] unboxed = new float[values.length];
		for(int i = 0; i < unboxed.length; i++)
			unboxed[i] = values[i].floatValue();
		return unboxed;
	}

	/** @return an array of the boxed values from the given values
	 *  @see #unbox(Float[]) */
	public static Float[] box(float[] values) {
		Float[] boxed = new Float[values.length];
		for(int i = 0; i < boxed.length; i++)
			boxed[i] = values[i];
		return boxed;
	}

	/** @return an array of the unboxed values from the given values
	 *  @see #box(int[])*/
	public static int[] unbox(Integer[] values) {
		int[] unboxed = new int[values.length];
		for(int i = 0; i < unboxed.length; i++)
			unboxed[i] = values[i].intValue();
		return unboxed;
	}

	/** @return an array of the boxed values from the given values
	 *  @see #unbox(Integer[]) */
	public static Integer[] box(int[] values) {
		Integer[] boxed = new Integer[values.length];
		for(int i = 0; i < boxed.length; i++)
			boxed[i] = values[i];
		return boxed;
	}

	/** @return an array of the unboxed values from the given values
	 *  @see #box(boolean[])*/
	public static boolean[] unbox(Boolean[] values) {
		boolean[] unboxed = new boolean[values.length];
		for(int i = 0; i < unboxed.length; i++)
			unboxed[i] = values[i].booleanValue();
		return unboxed;
	}

	/** @return an array of the boxed values from the given values
	 *  @see #unbox(Boolean[]) */
	public static Boolean[] box(boolean[] values) {
		Boolean[] boxed = new Boolean[values.length];
		for(int i = 0; i < boxed.length; i++)
			boxed[i] = values[i];
		return boxed;
	}

	/** @param elements the elements to select from
	 *  @param start the array index of elements at which to start (may be negative)
	 *  @param everyXth select every xth of elements
	 *  @param output The array to put the values in. May be null.
	 *  @throws IllegalArgumentException if the given output array is not null and smaller than the required length
	 *  @return the output array or a new array (if output was null) containing everyXth element of the given elements array */
	@SuppressWarnings("unchecked")
	public static <T> T[] select(T[] elements, int start, int everyXth, T[] output) {
		int outputLength = 0;
		for(int i = start - 1; i < elements.length; i += everyXth)
			if(i >= 0)
				outputLength++;
		if(output != null && output.length < outputLength)
			throw new IllegalArgumentException("The given output array is too small: " + output.length + "/" + outputLength);
		if(output == null || output.length != outputLength)
			output = (T[]) new Object[outputLength];
		for(int oi = 0, i = start - 1; oi < output.length; i += everyXth)
			if(i >= 0)
				output[oi++] = elements[i];
		return output;
	}

	/** {@link #select(float[], int, int, float[])} starting at {@code everyXth - 1}
	 *  @see #select(float[], int, int, float[]) */
	public static <T> T[] select(T[] elements, int everyXth, T[] output) {
		return select(elements, 0, everyXth, output);
	}

	/** @see #select(Object[], int, int, Object[]) */
	public static <T> T[] select(T[] elements, int start, int everyXth) {
		return select(elements, start, everyXth, null);
	}

	/** @see #select(Object[], int, Object[]) */
	public static <T> T[] select(T[] elements, int everyXth) {
		return select(elements, everyXth, null);
	}

	/** selects the given {@code indices} from the given {@code elements}
	 *  @param elements the elements to select from
	 *  @param indices the indices to select from {@code select}
	 *  @param output The array to fill. May be null.
	 *  @return the selected {@code indices} from {@code elements} */
	public static <T> T[] select(T[] elements, int[] indices, T[] output) {
		@SuppressWarnings("unchecked")
		T[] selection = output == null ? (T[]) new Object[indices.length] : output;
		for(int i = 0; i < indices.length; i++)
			selection[i] = elements[indices[i]];
		return selection;
	}

	/** @see #select(Object[], int[], Object[]) */
	public static <T> T[] select(T[] elements, int[] indices) {
		return select(elements, indices, null);
	}

	/** Skips, selects and goes to the next element repeatedly. Stops when {@code elements} has no more values. When {@code skips} has no more values, {@code repeatSkips} will be used repeatedly.<br>
	 *  If the length of the selection is the length of the given {@code elements}, {@code elements} is returned.
	 *  @param elements the elements from which to select not skipped ones
	 *  @param skips the number of indices to skip after each selection
	 *  @param repeatSkips The skips to use repeatedly after {@code skips} has no more values. If this is null, no more elements will be selected.
	 *  @param output the array to fill
	 *  @throws IllegalArgumentException if the output array is not null and smaller than the required length
	 *  @return the {@code elements} that were not skipped */
	public static <T> T[] skipselect(T[] elements, int[] skips, int[] repeatSkips, T[] output) {
		boolean normal = skips != null && skips.length > 0, repeat = repeatSkips != null && repeatSkips.length > 0;
		if(!normal && !repeat)
			return elements;

		int length, span = 0, rsi = 0;
		for(length = 0; length < elements.length; length++) {
			int skip = normal && length < skips.length ? skips[length] : repeat ? repeatSkips[rsi >= repeatSkips.length ? rsi = 0 : rsi++] : Integer.MAX_VALUE - span - 1;
			if(span + skip + 1 <= elements.length)
				span += skip + 1;
			else
				break;
		}

		if(length == elements.length)
			return elements;

		if(output != null && output.length < length)
			throw new IllegalArgumentException("The given output array is too small: " + output.length + "/" + length);

		@SuppressWarnings("unchecked")
		T[] selection = output == null ? (T[]) new Object[length] : output;

		rsi = 0;
		for(int si = 0, ei = 0; si < length;) {
			selection[si++] = elements[ei++];
			if(si >= skips.length)
				if(repeat)
					ei += repeatSkips[rsi >= repeatSkips.length ? rsi = 0 : rsi++];
				else
					break;
			else
				ei += skips[si];
		}

		return selection;
	}

	/** @see #skipselect(Object[], int[], int[], Object[]) */
	public static <T> T[] skipselect(T[] elements, int[] skips, int[] repeatSkips) {
		return skipselect(elements, skips, repeatSkips, null);
	}

	/** Like {@link #skipselect(Object[], int[], int[])} with a skips array that contains only {@code firstSkip} and an infinite {@code repeatSkips} array which elements are all {@code skips}.
	 * 	If {@code skips} is smaller than 1, {@code elements} will be returned.
	 *  @throws IllegalArgumentException if the output array is not null and smaller than the required length
	 *  @see #skipselect(Object[], int[], int[]) */
	public static <T> T[] skipselect(T[] elements, int firstSkip, int skips, T[] output) {
		int length, span = firstSkip;
		for(length = 0; length < elements.length; length++)
			if(span + skips + 1 <= elements.length)
				span += skips + 1;
			else {
				length++;
				break;
			}

		if(output != null && output.length < length)
			throw new IllegalArgumentException("The given output array is too small: " + output.length + "/" + length);

		@SuppressWarnings("unchecked")
		T[] selection = (T[]) new Object[length];

		for(int si = 0, ei = firstSkip; si < selection.length; si++, ei += skips + 1)
			selection[si] = elements[ei];

		return selection;
	}

	/** @see #skipselect(Object[], int, int, Object[]) */
	public static <T> T[] skipselect(T[] elements, int firstSkip, int skips) {
		return skipselect(elements, firstSkip, skips, null);
	}

	// primitive copies (probably get some generation tool)

	public static float[] select(float[] elements, int start, int everyXth, float[] output) {
		int outputLength = 0;
		for(int i = start - 1; i < elements.length; i += everyXth)
			if(i >= 0)
				outputLength++;
		if(output != null && output.length < outputLength)
			throw new IllegalArgumentException("The given output array is too small: " + output.length + "/" + outputLength);
		if(output == null || output.length != outputLength)
			output = new float[outputLength];
		for(int oi = 0, i = start - 1; oi < output.length; i += everyXth)
			if(i >= 0)
				output[oi++] = elements[i];
		return output;
	}

	/** {@link #select(float[], int, int, float[])} starting at {@code everyXth - 1}
	 *  @see #select(float[], int, int, float[]) */
	public static float[] select(float[] elements, int everyXth, float[] output) {
		return select(elements, 0, everyXth, output);
	}

	/** @see #select(Object[], int, int, Object[]) */
	public static float[] select(float[] elements, int start, int everyXth) {
		return select(elements, start, everyXth, null);
	}

	/** @see #select(Object[], int, Object[]) */
	public static float[] select(float[] elements, int everyXth) {
		return select(elements, everyXth, null);
	}

	/** selects the given {@code indices} from the given {@code elements}
	 *  @param elements the elements to select from
	 *  @param indices the indices to select from {@code select}
	 *  @param output The array to fill. May be null.
	 *  @return the selected {@code indices} from {@code elements} */
	public static float[] select(float[] elements, int[] indices, float[] output) {
		float[] selection = output == null ? new float[indices.length] : output;
		for(int i = 0; i < indices.length; i++)
			selection[i] = elements[indices[i]];
		return selection;
	}

	/** @see #select(Object[], int[], Object[]) */
	public static float[] select(float[] elements, int[] indices) {
		return select(elements, indices, null);
	}

	/** Skips, selects and goes to the next element repeatedly. Stops when {@code elements} has no more values. When {@code skips} has no more values, {@code repeatSkips} will be used repeatedly.<br>
	 *  If the length of the selection is the length of the given {@code elements}, {@code elements} is returned.
	 *  @param elements the elements from which to select not skipped ones
	 *  @param skips the number of indices to skip after each selection
	 *  @param repeatSkips The skips to use repeatedly after {@code skips} has no more values. If this is null, no more elements will be selected.
	 *  @param output the array to fill
	 *  @throws IllegalArgumentException if the output array is not null and smaller than the required length
	 *  @return the {@code elements} that were not skipped */
	public static float[] skipselect(float[] elements, int[] skips, int[] repeatSkips, float[] output) {
		boolean normal = skips != null && skips.length > 0, repeat = repeatSkips != null && repeatSkips.length > 0;
		if(!normal && !repeat)
			return elements;

		int length, span = 0, rsi = 0;
		for(length = 0; length < elements.length; length++) {
			int skip = normal && length < skips.length ? skips[length] : repeat ? repeatSkips[rsi >= repeatSkips.length ? rsi = 0 : rsi++] : Integer.MAX_VALUE - span - 1;
			if(span + skip + 1 <= elements.length)
				span += skip + 1;
			else
				break;
		}

		if(length == elements.length)
			return elements;

		if(output != null && output.length < length)
			throw new IllegalArgumentException("The given output array is too small: " + output.length + "/" + length);

		float[] selection = output == null ? new float[length] : output;

		rsi = 0;
		for(int si = 0, ei = 0; si < length;) {
			selection[si++] = elements[ei++];
			if(si >= skips.length)
				if(repeat)
					ei += repeatSkips[rsi >= repeatSkips.length ? rsi = 0 : rsi++];
				else
					break;
			else
				ei += skips[si];
		}

		return selection;
	}

	/** @see #skipselect(Object[], int[], int[], Object[]) */
	public static float[] skipselect(float[] elements, int[] skips, int[] repeatSkips) {
		return skipselect(elements, skips, repeatSkips, null);
	}

	/** Like {@link #skipselect(Object[], int[], int[])} with a skips array that contains only {@code firstSkip} and an infinite {@code repeatSkips} array which elements are all {@code skips}.
	 * 	If {@code skips} is smaller than 1, {@code elements} will be returned.
	 *  @throws IllegalArgumentException if the output array is not null and smaller than the required length
	 *  @see #skipselect(Object[], int[], int[]) */
	public static float[] skipselect(float[] elements, int firstSkip, int skips, float[] output) {
		int length, span = firstSkip;
		for(length = 0; length < elements.length; length++)
			if(span + skips + 1 <= elements.length)
				span += skips + 1;
			else {
				length++;
				break;
			}

		if(output != null && output.length < length)
			throw new IllegalArgumentException("The given output array is too small: " + output.length + "/" + length);

		float[] selection = new float[length];

		for(int si = 0, ei = firstSkip; si < selection.length; si++, ei += skips + 1)
			selection[si] = elements[ei];

		return selection;
	}

	/** @see #skipselect(Object[], int, int, Object[]) */
	public static float[] skipselect(float[] elements, int firstSkip, int skips) {
		return skipselect(elements, firstSkip, skips, null);
	}

}
