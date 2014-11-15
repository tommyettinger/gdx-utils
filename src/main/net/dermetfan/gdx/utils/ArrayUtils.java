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

package net.dermetfan.gdx.utils;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.IntArray;

/** array utility methods
 *  @author dermetfan
 *  @since 0.5.0 */
public class ArrayUtils extends net.dermetfan.utils.ArrayUtils {

	/** @see #wrapIndex(int, Object[]) */
	public static <T> T wrapIndex(int index, Array<T> array) {
		return array.get(wrapIndex(index, array.size));
	}

	/** @param elements the elements to select from
	 *  @param start the array index of elements at which to start (may be negative)
	 *  @param everyXth select every xth of elements
	 *  @param output The array to put the values in. May be null.
	 *  @throws IllegalArgumentException if the given output array is not null and smaller than the required length
	 *  @return the output array or a new array (if output was null) containing everyXth element of the given elements array */
	@SuppressWarnings("unchecked")
	public static <T> Array<T> select(Array<T> elements, int start, int everyXth, Array<T> output) {
		int outputLength = 0;
		for(int i = start - 1; i < elements.size; i += everyXth)
			if(i >= 0)
				outputLength++;
		if(output == null)
			output = new Array<>(outputLength);
		output.clear();
		output.ensureCapacity(outputLength - output.size);
		for(int oi = 0, i = start - 1; oi < outputLength; i += everyXth)
			if(i >= 0) {
				output.add(elements.get(i));
				oi++;
			}
		return output;
	}

	/** @see #select(Array, int, int, Array) */
	public static <T> Array<T> select(Array<T> elements, int everyXth, Array<T> output) {
		return select(elements, 0, everyXth, output);
	}

	/** @see #select(Array, int, int, Array) */
	public static <T> Array<T> select(Array<T> elements, int start, int everyXth) {
		return select(elements, start, everyXth, null);
	}

	/** @see #select(Array, int, Array) */
	public static <T> Array<T> select(Array<T> elements, int everyXth) {
		return select(elements, everyXth, null);
	}

	/** selects the given {@code indices} from the given {@code elements}
	 *  @param elements the elements to select from
	 *  @param indices the indices to select from {@code select}
	 *  @param output The array to fill. May be null.
	 *  @return the selected {@code indices} from {@code elements} */
	@SuppressWarnings("unchecked")
	public static <T> Array<T> select(Array<T> elements, IntArray indices, Array<T> output) {
		if(output == null)
			output = new Array<>(indices.size);
		output.clear();
		output.ensureCapacity(indices.size - output.size);
		for(int i = 0; i < indices.size; i++)
			output.add(elements.get(indices.get(i)));
		return output;
	}

	/** @see #select(Array, IntArray, Array) */
	public static <T> Array<T> select(Array<T> elements, IntArray indices) {
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
	@SuppressWarnings("unchecked")
	public static <T> Array<T> skipselect(Array<T> elements, IntArray skips, IntArray repeatSkips, Array<T> output) {
		boolean normal = skips != null && skips.size > 0, repeat = repeatSkips != null && repeatSkips.size > 0;
		if(!normal && !repeat)
			return elements;

		int length, span = 0, rsi = 0;
		for(length = 0; length < elements.size; length++) {
			int skip = normal && length < skips.size ? skips.get(length) : repeat ? repeatSkips.get(rsi >= repeatSkips.size ? rsi = 0 : rsi++) : Integer.MAX_VALUE - span - 1;
			if(span + skip + 1 <= elements.size)
				span += skip + 1;
			else
				break;
		}

		if(length == elements.size)
			return elements;

		if(output == null)
			output = new Array<>(length);
		output.clear();
		output.ensureCapacity(length - output.size);

		rsi = 0;
		for(int si = 0, ei = 0; si < length;) {
			output.add(elements.get(ei++));
			si++;
			if(si >= skips.size)
				if(repeat)
					ei += repeatSkips.get(rsi >= repeatSkips.size ? rsi = 0 : rsi++);
				else
					break;
			else
				ei += skips.get(si);
		}

		return output;
	}

	/** @see #skipselect(Array, IntArray, IntArray, Array) */
	public static <T> Array<T> skipselect(Array<T> elements, IntArray skips, IntArray repeatSkips) {
		return skipselect(elements, skips, repeatSkips, null);
	}

	/** Like {@link #skipselect(Array, IntArray, IntArray)} with a skips array that contains only {@code firstSkip} and an infinite {@code repeatSkips} array which elements are all {@code skips}.
	 * 	If {@code skips} is smaller than 1, {@code elements} will be returned.
	 *  @throws IllegalArgumentException if the output array is not null and smaller than the required length
	 *  @see #skipselect(Array, IntArray, IntArray) */
	@SuppressWarnings("unchecked")
	public static <T> Array<T> skipselect(Array<T> elements, int firstSkip, int skips, Array<T> output) {
		int length, span = firstSkip;
		for(length = 0; length < elements.size; length++)
			if(span + skips + 1 <= elements.size)
				span += skips + 1;
			else {
				length++;
				break;
			}

		if(output == null)
			output = new Array<>(length);
		output.clear();
		output.ensureCapacity(length - output.size);

		for(int si = 0, ei = firstSkip; si < length; si++, ei += skips + 1)
			output.add(elements.get(ei));

		return output;
	}

	/** @see #skipselect(Array, int, int, Array) */
	public static <T> Array<T> skipselect(Array<T> elements, int firstSkip, int skips) {
		return skipselect(elements, firstSkip, skips, null);
	}

	/** @see #equalsAny(Object, Object[]) */
	public static <T> boolean equalsAny(T obj, Array<T> array) {
		return equalsAny(obj, array.items, 0, array.size);
	}

	// primitive copies (probably get some generation tool)

	// int

	/** @see #wrapIndex(int, int[]) */
	public static int wrapIndex(int index, IntArray array) {
		return array.get(wrapIndex(index, array.size));
	}

	/** @param elements the elements to select from
	 *  @param start the array index of elements at which to start (may be negative)
	 *  @param everyXth select every xth of elements
	 *  @param output The array to put the values in. May be null.
	 *  @throws IllegalArgumentException if the given output array is not null and smaller than the required length
	 *  @return the output array or a new array (if output was null) containing everyXth element of the given elements array */
	public static IntArray select(IntArray elements, int start, int everyXth, IntArray output) {
		int outputLength = 0;
		for(int i = start - 1; i < elements.size; i += everyXth)
			if(i >= 0)
				outputLength++;
		if(output == null)
			output = new IntArray(outputLength);
		output.clear();
		output.ensureCapacity(outputLength - output.size);
		for(int oi = 0, i = start - 1; oi < outputLength; i += everyXth)
			if(i >= 0) {
				output.add(elements.get(i));
				oi++;
			}
		return output;
	}

	/** @see #select(Array, int, int, Array) */
	public static IntArray select(IntArray elements, int everyXth, IntArray output) {
		return select(elements, 0, everyXth, output);
	}

	/** @see #select(Array, int, int, Array) */
	public static IntArray select(IntArray elements, int start, int everyXth) {
		return select(elements, start, everyXth, null);
	}

	/** @see #select(Array, int, Array) */
	public static IntArray select(IntArray elements, int everyXth) {
		return select(elements, everyXth, null);
	}

	/** selects the given {@code indices} from the given {@code elements}
	 *  @param elements the elements to select from
	 *  @param indices the indices to select from {@code select}
	 *  @param output The array to fill. May be null.
	 *  @return the selected {@code indices} from {@code elements} */
	public static IntArray select(IntArray elements, IntArray indices, IntArray output) {
		if(output == null)
			output = new IntArray(indices.size);
		output.clear();
		output.ensureCapacity(indices.size - output.size);
		for(int i = 0; i < indices.size; i++)
			output.add(elements.get(indices.get(i)));
		return output;
	}

	/** @see #select(Array, IntArray, Array) */
	public static IntArray select(IntArray elements, IntArray indices) {
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
	public static IntArray skipselect(IntArray elements, IntArray skips, IntArray repeatSkips, IntArray output) {
		boolean normal = skips != null && skips.size > 0, repeat = repeatSkips != null && repeatSkips.size > 0;
		if(!normal && !repeat)
			return elements;

		int length, span = 0, rsi = 0;
		for(length = 0; length < elements.size; length++) {
			int skip = normal && length < skips.size ? skips.get(length) : repeat ? repeatSkips.get(rsi >= repeatSkips.size ? rsi = 0 : rsi++) : Integer.MAX_VALUE - span - 1;
			if(span + skip + 1 <= elements.size)
				span += skip + 1;
			else
				break;
		}

		if(length == elements.size)
			return elements;

		if(output == null)
			output = new IntArray(length);
		output.clear();
		output.ensureCapacity(length - output.size);

		rsi = 0;
		for(int si = 0, ei = 0; si < length;) {
			output.add(elements.get(ei++));
			si++;
			if(si >= skips.size)
				if(repeat)
					ei += repeatSkips.get(rsi >= repeatSkips.size ? rsi = 0 : rsi++);
				else
					break;
			else
				ei += skips.get(si);
		}

		return output;
	}

	/** @see #skipselect(Array, IntArray, IntArray, Array) */
	public static IntArray skipselect(IntArray elements, IntArray skips, IntArray repeatSkips) {
		return skipselect(elements, skips, repeatSkips, null);
	}

	/** Like {@link #skipselect(Array, IntArray, IntArray)} with a skips array that contains only {@code firstSkip} and an infinite {@code repeatSkips} array which elements are all {@code skips}.
	 * 	If {@code skips} is smaller than 1, {@code elements} will be returned.
	 *  @throws IllegalArgumentException if the output array is not null and smaller than the required length
	 *  @see #skipselect(Array, IntArray, IntArray) */
	public static IntArray skipselect(IntArray elements, int firstSkip, int skips, IntArray output) {
		int length, span = firstSkip;
		for(length = 0; length < elements.size; length++)
			if(span + skips + 1 <= elements.size)
				span += skips + 1;
			else {
				length++;
				break;
			}

		if(output == null)
			output = new IntArray(length);
		output.clear();
		output.ensureCapacity(length - output.size);

		for(int si = 0, ei = firstSkip; si < length; si++, ei += skips + 1)
			output.add(elements.get(ei));

		return output;
	}

	/** @see #skipselect(Array, int, int, Array) */
	public static IntArray skipselect(IntArray elements, int firstSkip, int skips) {
		return skipselect(elements, firstSkip, skips, null);
	}

	// float

	/** @see #wrapIndex(int, float[]) */
	public static float wrapIndex(int index, FloatArray array) {
		return array.get(wrapIndex(index, array.size));
	}

	/** @param elements the elements to select from
	 *  @param start the array index of elements at which to start (may be negative)
	 *  @param everyXth select every xth of elements
	 *  @param output The array to put the values in. May be null.
	 *  @throws IllegalArgumentException if the given output array is not null and smaller than the required length
	 *  @return the output array or a new array (if output was null) containing everyXth element of the given elements array */
	public static FloatArray select(FloatArray elements, int start, int everyXth, FloatArray output) {
		int outputLength = 0;
		for(int i = start - 1; i < elements.size; i += everyXth)
			if(i >= 0)
				outputLength++;
		if(output == null)
			output = new FloatArray(outputLength);
		output.clear();
		output.ensureCapacity(outputLength - output.size);
		for(int oi = 0, i = start - 1; oi < outputLength; i += everyXth)
			if(i >= 0) {
				output.add(elements.get(i));
				oi++;
			}
		return output;
	}

	/** @see #select(Array, int, int, Array) */
	public static FloatArray select(FloatArray elements, int everyXth, FloatArray output) {
		return select(elements, 0, everyXth, output);
	}

	/** @see #select(Array, int, int, Array) */
	public static FloatArray select(FloatArray elements, int start, int everyXth) {
		return select(elements, start, everyXth, null);
	}

	/** @see #select(Array, int, Array) */
	public static FloatArray select(FloatArray elements, int everyXth) {
		return select(elements, everyXth, null);
	}

	/** selects the given {@code indices} from the given {@code elements}
	 *  @param elements the elements to select from
	 *  @param indices the indices to select from {@code select}
	 *  @param output The array to fill. May be null.
	 *  @return the selected {@code indices} from {@code elements} */
	public static FloatArray select(FloatArray elements, IntArray indices, FloatArray output) {
		if(output == null)
			output = new FloatArray(indices.size);
		output.clear();
		output.ensureCapacity(indices.size - output.size);
		for(int i = 0; i < indices.size; i++)
			output.add(elements.get(indices.get(i)));
		return output;
	}

	/** @see #select(Array, IntArray, Array) */
	public static FloatArray select(FloatArray elements, IntArray indices) {
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
	public static FloatArray skipselect(FloatArray elements, IntArray skips, IntArray repeatSkips, FloatArray output) {
		boolean normal = skips != null && skips.size > 0, repeat = repeatSkips != null && repeatSkips.size > 0;
		if(!normal && !repeat)
			return elements;

		int length, span = 0, rsi = 0;
		for(length = 0; length < elements.size; length++) {
			int skip = normal && length < skips.size ? skips.get(length) : repeat ? repeatSkips.get(rsi >= repeatSkips.size ? rsi = 0 : rsi++) : Integer.MAX_VALUE - span - 1;
			if(span + skip + 1 <= elements.size)
				span += skip + 1;
			else
				break;
		}

		if(length == elements.size)
			return elements;

		if(output == null)
			output = new FloatArray(length);
		output.clear();
		output.ensureCapacity(length - output.size);

		rsi = 0;
		for(int si = 0, ei = 0; si < length;) {
			output.add(elements.get(ei++));
			si++;
			if(si >= skips.size)
				if(repeat)
					ei += repeatSkips.get(rsi >= repeatSkips.size ? rsi = 0 : rsi++);
				else
					break;
			else
				ei += skips.get(si);
		}

		return output;
	}

	/** @see #skipselect(Array, IntArray, IntArray, Array) */
	public static FloatArray skipselect(FloatArray elements, IntArray skips, IntArray repeatSkips) {
		return skipselect(elements, skips, repeatSkips, null);
	}

	/** Like {@link #skipselect(Array, IntArray, IntArray)} with a skips array that contains only {@code firstSkip} and an infinite {@code repeatSkips} array which elements are all {@code skips}.
	 * 	If {@code skips} is smaller than 1, {@code elements} will be returned.
	 *  @throws IllegalArgumentException if the output array is not null and smaller than the required length
	 *  @see #skipselect(Array, IntArray, IntArray) */
	public static FloatArray skipselect(FloatArray elements, int firstSkip, int skips, FloatArray output) {
		int length, span = firstSkip;
		for(length = 0; length < elements.size; length++)
			if(span + skips + 1 <= elements.size)
				span += skips + 1;
			else {
				length++;
				break;
			}

		if(output == null)
			output = new FloatArray(length);
		output.clear();
		output.ensureCapacity(length - output.size);

		for(int si = 0, ei = firstSkip; si < length; si++, ei += skips + 1)
			output.add(elements.get(ei));

		return output;
	}

	/** @see #skipselect(Array, int, int, Array) */
	public static FloatArray skipselect(FloatArray elements, int firstSkip, int skips) {
		return skipselect(elements, firstSkip, skips, null);
	}

}
