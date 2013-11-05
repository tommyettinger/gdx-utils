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

package net.dermetfan.util;

/** array utility methods 
 *  @author dermetfan */
public abstract class ArrayUtils {

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

	/** selects the given {@code indices} from the given {@code elements}
	 *  @param elements the elements to select from
	 *  @param indices the indeces to select from {@code select}
	 *  @return the selected {@code indices} from {@code elements} */
	public static <T> T[] select(T[] elements, int[] indices) {
		@SuppressWarnings("unchecked")
		T[] selection = (T[]) new Object[indices.length];
		for(int i = 0; i < indices.length; i++)
			selection[i] = elements[indices[i]];
		return selection;
	}

	/** Skips, selects and goes to the next element repeatedly. Stops when {@code elements} has no more values. When {@code skips} has no more values, {@code repeatSkips} will be used repeatedly.<br/>
	 *  If the length of the selection is the length of the given {@code elements}, {@code elements} is returned.
	 *  @param elements the elements from which to select not skipped ones
	 *  @param skips the number of indices to skip after each selection
	 *  @param repeatSkips The skips to use repeatedly after {@code skips} has no more values. If this is null, no more elements will be selected. 
	 *  @return the {@code elements} that were not skipped */
	public static <T> T[] skipselect(T[] elements, int[] skips, int[] repeatSkips) {
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

		@SuppressWarnings("unchecked")
		T[] selection = (T[]) new Object[length];

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

	/** Like {@link #skipselect(Object[], int[], int[])} with a skips array that contains only {@code firstSkip} and an infinite {@code repeatSkips} array which elements are all {@code skips}.
	 * 	If {@code skips} is smaller than 1, {@code elements} will be returned.
	 *  @see #skipselect(Object[], int[], int[]) */
	public static <T> T[] skipselect(T[] elements, int firstSkip, int skips) {
		if(skips < 1)
			return elements;

		int length, span = firstSkip;
		for(length = 0; length <= elements.length; length++)
			if(span + skips + 1 <= elements.length)
				span += skips + 1;
			else
				break;

		@SuppressWarnings("unchecked")
		T[] selection = (T[]) new Object[length];

		for(int si = 0, ei = firstSkip; si < selection.length; si++, ei += skips + 1)
			selection[si] = elements[ei];

		return selection;
	}

}
