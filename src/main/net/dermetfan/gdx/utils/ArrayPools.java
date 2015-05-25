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

import com.badlogic.gdx.utils.ObjectMap;

/** stores a map of {@link ArrayPool ArrayPools} (usually {@link ReflectionArrayPool}) for convenient static access
 *  @author dermetfan
 *  @since 0.11.1 */
public class ArrayPools {

	private static final ObjectMap<Class, ArrayPool> typePools = new ObjectMap<>();

	/** a static pool for float arrays */
	private static FloatArrayPool floatPool;

	/** a static pool for int arrays */
	private static IntArrayPool intPool;

	/** @return the {@link #floatPool} */
	public static FloatArrayPool getFloatPool(int max, int maxEach) {
		if(floatPool == null)
			floatPool = new FloatArrayPool(max, maxEach);
		return floatPool;
	}

	/** @return the {@link #intPool} */
	public static IntArrayPool getIntPool(int max, int maxEach) {
		if(intPool == null)
			intPool = new IntArrayPool(max, maxEach);
		return intPool;
	}

	/** calls {@link #getFloatPool(int, int)} with a max size of -1 (no max size) and a maxEach size of 100
	 *  @see #getFloatPool(int, int) */
	public static FloatArrayPool getFloatPool() {
		return getFloatPool(-1, 100);
	}

	/** calls {@link #getIntPool(int, int)} with a max size of -1 (no max size) and a maxEach size of 100
	 *  @see #getIntPool(int, int) */
	public static IntArrayPool getIntPool() {
		return getIntPool(-1, 100);
	}

	/** @param max note the max size is ignored if this is not the first time this pool has been requested
	 *  @return a new {@link ReflectionArrayPool} or existing pool for the specified type, stored in a Class to {@link ArrayPool} map */
	public static <T> ArrayPool<T> get(Class<T> type, int max, int maxEach) {
		@SuppressWarnings("unchecked")
		ArrayPool<T> pool = typePools.get(type);
		if(pool == null) {
			pool = new ReflectionArrayPool<>(type, max, maxEach);
			typePools.put(type, pool);
		}
		return pool;
	}

	/** calls {@link #get(Class, int, int)} with a max size of -1 (no max size) and a maxEach size of 100
	 *  @see #get(Class, int, int) */
	public static <T> ArrayPool<T> get(Class<T> type) {
		return get(type, -1, 100);
	}

	/** @param type the type for which to set the pool in a Class to {@link ArrayPool} map
	 *  @param pool the pool to set for the given type */
	public static <T> void set(Class<T> type, ArrayPool<T> pool) {
		typePools.put(type, pool);
	}

	/** @see ArrayPool#obtain(int) */
	public static <T> T[] obtain(Class<T> type, int length) {
		return get(type).obtain(length);
	}

	/** @see ArrayPool#free(Object[]) */
	public static <T> void free(T[] array) {
		if(array == null)
			throw new IllegalArgumentException("array cannot be null");
		@SuppressWarnings("unchecked")
		ArrayPool<T> pool = (ArrayPool<T>) get(array.getClass().getComponentType());
		pool.free(array);
	}

	private ArrayPools() {}

}
