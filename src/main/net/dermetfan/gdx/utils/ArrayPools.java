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

	/** holds pools by type */
	private static final ObjectMap<Class, ArrayPool> typePools = new ObjectMap<>();

	/** a static pool for float arrays */
	private static FloatArrayPool floatPool;

	/** a static pool for int arrays */
	private static IntArrayPool intPool;

	/** note the max and maxEach sizes are ignored if this is not the first time this pool has been requested
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

	/** @see #get(Class, int, int) */
	public static FloatArrayPool getFloats(int max, int maxEach) {
		if(floatPool == null)
			floatPool = new FloatArrayPool(max, maxEach);
		return floatPool;
	}

	/** @see #get(Class, int, int) */
	public static IntArrayPool getInts(int max, int maxEach) {
		if(intPool == null)
			intPool = new IntArrayPool(max, maxEach);
		return intPool;
	}

	/** calls {@link #get(Class, int, int)} with a max size of -1 (no max size) and a maxEach size of 100
	 *  @see #get(Class, int, int) */
	public static <T> ArrayPool<T> get(Class<T> type) {
		return get(type, -1, 100);
	}

	/** @see #get(Class) */
	public static FloatArrayPool getFloats() {
		return getFloats(-1, 100);
	}

	/** @see #get(Class) */
	public static IntArrayPool getInts() {
		return getInts(-1, 100);
	}

	/** @param type the type for which to set the pool in a Class to {@link ArrayPool} map
	 *  @param pool the pool to set for the given type */
	public static <T> void set(Class<T> type, ArrayPool<T> pool) {
		typePools.put(type, pool);
	}

	/** @see #set(Class, ArrayPool) */
	public static void set(FloatArrayPool pool) {
		floatPool = pool;
	}

	/** @see #set(Class, ArrayPool) */
	public static void set(IntArrayPool pool) {
		intPool = pool;
	}

	/** @see ArrayPool#obtain(int) */
	public static <T> T[] obtain(Class<T> type, int length) {
		if(type.isPrimitive())
			throw new IllegalArgumentException("Cannot return primitive array without boxing. Use obtain" + (type == float.class ? "Float" : type == int.class ? "Int" : "[Type]") + "s(int) instead.");
		return get(type).obtain(length);
	}

	/** @see FloatArrayPool#obtain(int) */
	public static float[] obtainFloats(int length) {
		return getFloats().obtain(length);
	}

	/** @see IntArrayPool#obtain(int) */
	public static int[] obtainInts(int length) {
		return getInts().obtain(length);
	}

	/** @see ArrayPool#free(Object[]) */
	public static <T> void free(T[] array) {
		if(array == null)
			throw new IllegalArgumentException("array cannot be null");
		@SuppressWarnings("unchecked")
		ArrayPool<T> pool = (ArrayPool<T>) get(array.getClass().getComponentType());
		pool.free(array);
	}

	/** @see FloatArrayPool#free(float[]) */
	public static void free(float[] array) {
		getFloats().free(array);
	}

	/** @see IntArrayPool#free(int[]) */
	public static void free(int[] array) {
		getInts().free(array);
	}

	private ArrayPools() {}

}
