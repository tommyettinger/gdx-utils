/** Copyright 2015 Robin Stumm (serverkorken@gmail.com, http://dermetfan.net)
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

import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.PooledLinkedList;

/** pools arrays by their size
 *  @author dermetfan
 *  @since 0.11.1 */
public abstract class ArrayPool<T> {

	private final IntMap<PooledLinkedList<T[]>> lists;
	private final Pool<PooledLinkedList<T[]>> listPool;

	/** the maximum amount of arrays of different lengths that will be pooled */
	public final int max;

	/** the maximum amount of arrays of the same length that will be pooled */
	public final int maxEach;

	public ArrayPool(int max, final int maxEach) {
		this.max = max;
		this.maxEach = maxEach;

		lists = new IntMap<>(max < 0 ? 10 : max, 1);
		listPool = new Pool<PooledLinkedList<T[]>>() {
			@Override
			protected PooledLinkedList<T[]> newObject() {
				return new PooledLinkedList<>(maxEach);
			}
		};
	}

	/** @return a new array of the given length */
	protected abstract T[] newArray(int length);

	/** @param length the desired length of the array */
	public T[] obtain(int length) {
		if(length < 0)
			throw new IllegalArgumentException("negative array length: " + length);
		PooledLinkedList<T[]> list = lists.get(length);
		if(list == null)
			return newArray(length);
		list.iterReverse();
		T[] array = list.previous();
		list.remove();
		if(list.previous() == null) {
			lists.remove(length);
			listPool.free(list);
		}
		return array;
	}

	/** @param array the array to put back into the pool */
	public void free(T[] array) {
		if(array == null)
			throw new IllegalArgumentException("array cannot be null");
		PooledLinkedList<T[]> list = lists.get(array.length);
		if(list == null && lists.size < max) {
			list = listPool.obtain();
			lists.put(array.length, list);
		}
		if(list != null && size(list) < maxEach)
			list.add(array);
	}

	public void clear() {
		for(PooledLinkedList<T[]> list : lists.values())
			list.clear();
		lists.clear();
	}

	/** @return the number of arrays of the given length in the pool */
	public int getFree(int length) {
		PooledLinkedList<T[]> list = lists.get(length);
		return list == null ? 0 : size(list);
	}

	private int size(PooledLinkedList list) {
		int size = 0;
		list.iter();
		while(list.next() != null)
			size++;
		return size;
	}

}
