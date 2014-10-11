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

import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.IntMap.Keys;
import com.badlogic.gdx.utils.ObjectIntMap;

/** an {@link IntMap} and {@link ObjectIntMap} holding each others contents in reverse for fast retrieval of both keys and values
 *  @since 0.6.0
 *  @author dermetfan */
public class DualIntMap<V> {

	/** the map holding keys as keys */
	private final IntMap<V> keyValue;

	/** the map holding values as keys */
	private final ObjectIntMap<V> valueKey;

	/** @see IntMap#IntMap() */
	public DualIntMap() {
		keyValue = new IntMap<>();
		valueKey = new ObjectIntMap<>();
	}

	/** @see IntMap#IntMap(int) */
	public DualIntMap(int initialCapacity) {
		keyValue = new IntMap<>(initialCapacity);
		valueKey = new ObjectIntMap<>(initialCapacity);
	}

	/** @see IntMap#IntMap(int, float) */
	public DualIntMap(int initialCapacity, float loadFactor) {
		keyValue = new IntMap<>(initialCapacity, loadFactor);
		valueKey = new ObjectIntMap<>(initialCapacity, loadFactor);
	}

	/** @see IntMap#IntMap(IntMap) */
	public DualIntMap(IntMap<V> map) {
		keyValue = new IntMap<>(map);
		valueKey = new ObjectIntMap<>(map.size);
		Keys keys = map.keys();
		while(keys.hasNext) {
			int key = keys.next();
			valueKey.put(map.get(key), key);
		}
	}

	/** @param map the map to copy */
	public DualIntMap(DualIntMap<V> map) {
		keyValue = new IntMap<>(map.keyValue);
		valueKey = new ObjectIntMap<>(map.valueKey);
	}

	/** @see IntMap#put(int, Object) */
	public void put(int key, V value) {
		keyValue.put(key, value);
		valueKey.put(value, key);
	}

	/** @return the key of the given value as {@link IntMap#findKey(Object, boolean, int)} would return */
	public int getKey(V value, int defaultKey) {
		return valueKey.get(value, defaultKey);
	}

	/** @see IntMap#get(int) */
	public V getValue(int key) {
		return keyValue.get(key);
	}

	/** @see IntMap#remove(int) */
	public V removeKey(int key) {
		V value = keyValue.remove(key);
		if(value != null) {
			int removed = valueKey.remove(value, key);
			assert removed == key;
		}
		return value;
	}

	/** like what {@code intMap.remove(intMap.findKey(value, true, defaultValue))} would do */
	public int removeValue(V value, int defaultKey) {
		int key = valueKey.remove(value, defaultKey);
		keyValue.remove(key);
		return key;
	}

}
