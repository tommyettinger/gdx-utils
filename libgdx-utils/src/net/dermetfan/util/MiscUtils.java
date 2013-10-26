/**
 * Copyright 2013 Robin Stumm (serverkorken@googlemail.com, http://dermetfan.bplaced.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dermetfan.util;

import java.util.Arrays;

/** Miscellaneous utility methods 
 *  @author dermetfan */
public abstract class MiscUtils {

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

	/** <p>
	 *  	Skips and selects repeatedly. Stops when {@code elements} has no more values. When {@code skips} has no more values, {@code infiniteSkips} will be used.<br/>
	 * 		If {@code skips} is null or has a length of zero, {@link #skipselect(Object[], int)} will be used with {@code infiniteSkips}.<br/>
	 *  	If the length of the selection is the length of the given {@code elements} / all {@code skips} are zero, {@code elements} is returned.
	 *  </p>
	 *  <p>
	 *  	For example, skipselecting [1, 4, 2] from [0, 1, 2, 3, 4, 5, 6, 7, 8, 9] will result in [1, 6, 9].<br/>
	 *  	Skipselecting [0, 2] with negative {@code infiniteSkips} will result in [0, 3].<br/>
	 *  	Skipselecting [0, 2] with 4 {@code infiniteSkips} will result in [0, 3, 8].<br/>
	 *  	Skipselecting [0, 0, ...] with zero {@code infiniteSkips} will return the given {@code elements}.
	 *  	Skipselecting [0, 1] with negative 1 {@code infiniteSkips} will return every second element ([0, 2, 4, 6, 8]).
	 *  </p>
	 *  @param elements the elements from which to select not skipped ones
	 *  @param firstSkip the number of indices to skip initially
	 *  @param skips the number of indices to skip after each selection
	 *  @param infiniteSkips The skips to use after {@code skips} has no more values. If this is negative, no more elements will be selected. 
	 *  @return the {@code elements} that were not skipped */
	public static <T> T[] skipselect(T[] elements, int[] skips, int infiniteSkips) {
		if(skips.length < 1 || skips == null)
			return skipselect(elements, 0, infiniteSkips);

		int length, span = 0;
		for(length = 0; length < elements.length; length++)
			if(span + (length >= skips.length ? infiniteSkips > -1 ? infiniteSkips : Integer.MAX_VALUE - span - 1 : skips[length]) + 1 <= elements.length)
				span += (length >= skips.length ? infiniteSkips : skips[length]) + 1;
			else
				break;

		if(length == elements.length)
			return elements;

		@SuppressWarnings("unchecked")
		T[] selection = (T[]) new Object[length];

		for(int si = 0, ei = 0; si < length; si++) {
			selection[si] = elements[ei++];
			if(si + 1 >= skips.length)
				ei += infiniteSkips;
			else
				ei += skips[si + 1];
		}

		return selection;
	}

	/** Like {@link #skipselect(Object[], int[], int)} with an empty skips array.
	 * 	If {@code skips} is smaller than 1, {@code elements} will be returned.
	 *  @see #skipselect(Object[], int[]) */
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

	public static void main(String[] args) {
		Integer[] array = new Integer[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};

		System.out.println(Arrays.toString(skipselect(array, 0, 1)));
		System.out.println(Arrays.toString(skipselect(array, new int[] {0, 1}, 1)));
	}

}
