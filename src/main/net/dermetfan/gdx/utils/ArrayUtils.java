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

	/** @see #getRepeated(Object[], int) */
	public static <T> T getRepeated(Array<T> array, int index) {
		return array.get(repeat(array.size, index));
	}

	/** @param items the items to select from
	 *  @param start the array index at which to start (may be negative)
	 *  @param everyXth select every xth of items
	 *  @param dest The array to put the values in. May be null.
	 *  @return the dest array or a new array (if dest was null) containing everyXth item of the given items array */
	public static <T> Array<T> select(Array<T> items, int offset, int length, int start, int everyXth, Array<T> dest) {
		int outputLength = selectCount(offset, length, start, everyXth);
		if(dest == null)
			dest = new Array<>(outputLength);
		dest.clear();
		dest.ensureCapacity(outputLength);
		if(start + length > items.size)
			throw new ArrayIndexOutOfBoundsException(start + length - 1);
		dest.size = outputLength;
		select(items.items, offset, length, start, everyXth, dest.items);
		return dest;
	}

	/** @see #select(Array, int, int, int, int, Array) */
	public static <T> Array<T> select(Array<T> items, int start, int everyXth, Array<T> dest) {
		return select(items, 0, items.size, start, everyXth, dest);
	}

	/** @see #select(Array, int, int, Array) */
	public static <T> Array<T> select(Array<T> items, int everyXth, Array<T> dest) {
		return select(items, 0, everyXth, dest);
	}

	/** @see #select(Array, int, int, int, int, Array) */
	public static <T> Array<T> select(Array<T> items, int offset, int length, int start, int everyXth) {
		return select(items, offset, length, start, everyXth, null);
	}

	/** @see #select(Array, int, int, int, int) */
	public static <T> Array<T> select(Array<T> items, int start, int everyXth) {
		return select(items, 0, items.size, start, everyXth);
	}

	/** @see #select(Array, int, int) */
	public static <T> Array<T> select(Array<T> items, int everyXth) {
		return select(items, 0, everyXth);
	}

	/** @param items the items to select from
	 *  @param indices the indices to select
	 *  @param dest the array to fill
	 *  @return the given dest array */
	public static <T> Array<T> select(Array<T> items, int[] indices, int indicesOffset, int indicesLength, Array<T> dest) {
		if(dest == null)
			dest = new Array<>(true, indicesLength, items.items.getClass().getComponentType());
		dest.clear();
		dest.ensureCapacity(indicesLength);
		if(indicesLength > items.size)
			throw new ArrayIndexOutOfBoundsException(indicesLength - 1);
		dest.size = indicesLength;
		select(items.items, indices, indicesOffset, indicesLength, dest.items, 0);
		return dest;
	}

	/** @see #select(Array, int[], int, int, Array) */
	public static <T> Array<T> select(Array<T> items, int[] indices, Array<T> dest) {
		return select(items, indices, 0, indices.length, dest);
	}

	/** @see #select(Object[], int[], Object[]) */
	public static <T> Array<T> select(Array<T> items, int[] indices) {
		return select(items, indices, null);
	}

	/** @see #select(Array, int[], int, int, Array) */
	public static <T> Array<T> select(Array<T> items, IntArray indices, Array<T> dest) {
		return select(items, indices.items, 0, indices.size, dest);
	}

	/** @see #select(Array, IntArray, Array) */
	public static <T> Array<T> select(Array<T> items, IntArray indices) {
		return select(items, indices, null);
	}

	/** Skips, selects and goes to the next element repeatedly. Stops when {@code elements} has no more values. When {@code skips} has no more values, {@code repeatSkips} will be used repeatedly.<br>
	 *  If the length of the selection is the length of the given {@code elements}, {@code elements} is returned.
	 *  @param elements the elements from which to select not skipped ones
	 *  @param skips the number of indices to skip after each selection
	 *  @param repeatSkips The skips to use repeatedly after {@code skips} has no more values. If this is null, no more elements will be selected.
	 *  @param output the array to fill
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

	/** @see #getRepeated(int[], int) */
	public static int getRepeated(IntArray array, int index) {
		return array.get(repeat(array.size, index));
	}

	/** @param items the items to select from
	 *  @param start the array index at which to start (may be negative)
	 *  @param everyXth select every xth of items
	 *  @param dest The array to put the values in. May be null.
	 *  @return the dest array or a new array (if dest was null) containing everyXth item of the given items array */
	public static IntArray select(IntArray items, int offset, int length, int start, int everyXth, IntArray dest) {
		int outputLength = selectCount(offset, length, start, everyXth);
		if(dest == null)
			dest = new IntArray(outputLength);
		dest.clear();
		dest.ensureCapacity(outputLength);
		if(start + length > items.size)
			throw new ArrayIndexOutOfBoundsException(start + length - 1);
		dest.size = outputLength;
		select(items.items, offset, length, start, everyXth, dest.items);
		return dest;
	}

	/** @see #select(Array, int, int, int, int, Array) */
	public static IntArray select(IntArray items, int start, int everyXth, IntArray dest) {
		return select(items, 0, items.size, start, everyXth, dest);
	}

	/** @see #select(Array, int, int, Array) */
	public static IntArray select(IntArray items, int everyXth, IntArray dest) {
		return select(items, 0, everyXth, dest);
	}

	/** @see #select(Array, int, int, int, int, Array) */
	public static IntArray select(IntArray items, int offset, int length, int start, int everyXth) {
		return select(items, offset, length, start, everyXth, null);
	}

	/** @see #select(Array, int, int, int, int) */
	public static IntArray select(IntArray items, int start, int everyXth) {
		return select(items, 0, items.size, start, everyXth);
	}

	/** @see #select(Array, int, int) */
	public static IntArray select(IntArray items, int everyXth) {
		return select(items, 0, everyXth);
	}

	/** @param items the items to select from
	 *  @param indices the indices to select
	 *  @param dest the array to fill
	 *  @return the given dest array */
	public static IntArray select(IntArray items, int[] indices, int indicesOffset, int indicesLength, IntArray dest) {
		if(dest == null)
			dest = new IntArray(true, indicesLength);
		dest.clear();
		dest.ensureCapacity(indicesLength);
		if(indicesLength > items.size)
			throw new ArrayIndexOutOfBoundsException(indicesLength - 1);
		dest.size = indicesLength;
		select(items.items, indices, indicesOffset, indicesLength, dest.items, 0);
		return dest;
	}

	/** @see #select(Array, int[], int, int, Array) */
	public static IntArray select(IntArray items, int[] indices, IntArray dest) {
		return select(items, indices, 0, indices.length, dest);
	}

	/** @see #select(Object[], int[], Object[]) */
	public static IntArray select(IntArray items, int[] indices) {
		return select(items, indices, null);
	}

	/** @see #select(Array, int[], int, int, Array) */
	public static IntArray select(IntArray items, IntArray indices, IntArray dest) {
		return select(items, indices.items, 0, indices.size, dest);
	}

	/** @see #select(Array, IntArray, Array) */
	public static IntArray select(IntArray items, IntArray indices) {
		return select(items, indices, null);
	}

	/** Skips, selects and goes to the next element repeatedly. Stops when {@code elements} has no more values. When {@code skips} has no more values, {@code repeatSkips} will be used repeatedly.<br>
	 *  If the length of the selection is the length of the given {@code elements}, {@code elements} is returned.
	 *  @param elements the elements from which to select not skipped ones
	 *  @param skips the number of indices to skip after each selection
	 *  @param repeatSkips The skips to use repeatedly after {@code skips} has no more values. If this is null, no more elements will be selected.
	 *  @param output the array to fill
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

	/** @see #getRepeated(float[], int) */
	public static float getRepeated(FloatArray array, int index) {
		return array.get(repeat(array.size, index));
	}

	/** @param items the items to select from
	 *  @param start the array index at which to start (may be negative)
	 *  @param everyXth select every xth of items
	 *  @param dest The array to put the values in. May be null.
	 *  @return the dest array or a new array (if dest was null) containing everyXth item of the given items array */
	public static FloatArray select(FloatArray items, int offset, int length, int start, int everyXth, FloatArray dest) {
		int outputLength = selectCount(offset, length, start, everyXth);
		if(dest == null)
			dest = new FloatArray(outputLength);
		dest.clear();
		dest.ensureCapacity(outputLength);
		if(start + length > items.size)
			throw new ArrayIndexOutOfBoundsException(start + length - 1);
		dest.size = outputLength;
		select(items.items, offset, length, start, everyXth, dest.items);
		return dest;
	}

	/** @see #select(Array, int, int, int, int, Array) */
	public static FloatArray select(FloatArray items, int start, int everyXth, FloatArray dest) {
		return select(items, 0, items.size, start, everyXth, dest);
	}

	/** @see #select(Array, int, int, Array) */
	public static FloatArray select(FloatArray items, int everyXth, FloatArray dest) {
		return select(items, 0, everyXth, dest);
	}

	/** @see #select(Array, int, int, int, int, Array) */
	public static FloatArray select(FloatArray items, int offset, int length, int start, int everyXth) {
		return select(items, offset, length, start, everyXth, null);
	}

	/** @see #select(Array, int, int, int, int) */
	public static FloatArray select(FloatArray items, int start, int everyXth) {
		return select(items, 0, items.size, start, everyXth);
	}

	/** @see #select(Array, int, int) */
	public static FloatArray select(FloatArray items, int everyXth) {
		return select(items, 0, everyXth);
	}

	/** @param items the items to select from
	 *  @param indices the indices to select
	 *  @param dest the array to fill
	 *  @return the given dest array */
	public static FloatArray select(FloatArray items, int[] indices, int indicesOffset, int indicesLength, FloatArray dest) {
		if(dest == null)
			dest = new FloatArray(true, indicesLength);
		dest.clear();
		dest.ensureCapacity(indicesLength);
		if(indicesLength > items.size)
			throw new ArrayIndexOutOfBoundsException(indicesLength - 1);
		dest.size = indicesLength;
		select(items.items, indices, indicesOffset, indicesLength, dest.items, 0);
		return dest;
	}

	/** @see #select(Array, int[], int, int, Array) */
	public static FloatArray select(FloatArray items, int[] indices, FloatArray dest) {
		return select(items, indices, 0, indices.length, dest);
	}

	/** @see #select(Object[], int[], Object[]) */
	public static FloatArray select(FloatArray items, int[] indices) {
		return select(items, indices, null);
	}

	/** @see #select(Array, int[], int, int, Array) */
	public static FloatArray select(FloatArray items, IntArray indices, FloatArray dest) {
		return select(items, indices.items, 0, indices.size, dest);
	}

	/** @see #select(Array, IntArray, Array) */
	public static FloatArray select(FloatArray items, IntArray indices) {
		return select(items, indices, null);
	}

	/** Skips, selects and goes to the next element repeatedly. Stops when {@code elements} has no more values. When {@code skips} has no more values, {@code repeatSkips} will be used repeatedly.<br>
	 *  If the length of the selection is the length of the given {@code elements}, {@code elements} is returned.
	 *  @param elements the elements from which to select not skipped ones
	 *  @param skips the number of indices to skip after each selection
	 *  @param repeatSkips The skips to use repeatedly after {@code skips} has no more values. If this is null, no more elements will be selected.
	 *  @param output the array to fill
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
