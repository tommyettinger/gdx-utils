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

import com.badlogic.gdx.utils.ObjectMap;

/** Two {@link ObjectMap ObjectMaps} holding each others contents in reverse for fast retrieval of both keys and values.
 *  This causes null values not to be allowed.
 *  @since 0.6.0
 *  @author dermetfan */
public class DualObjectMap<K, V> {

	/** the map holding keys as keys */
	private final ObjectMap<K, V> keyValue;

	/** the map holding values as keys */
	private final ObjectMap<V, K> valueKey;

	/** @see ObjectMap#ObjectMap() */
	public DualObjectMap() {
		keyValue = new ObjectMap<>();
		valueKey = new ObjectMap<>();
	}

	/** @see ObjectMap#ObjectMap(int) */
	public DualObjectMap(int initialCapacity) {
		keyValue = new ObjectMap<>(initialCapacity);
		valueKey = new ObjectMap<>(initialCapacity);
	}

	/** @see ObjectMap#ObjectMap(int, float) */
	public DualObjectMap(int initialCapacity, float loadFactor) {
		keyValue = new ObjectMap<>(initialCapacity, loadFactor);
		valueKey = new ObjectMap<>(initialCapacity, loadFactor);
	}

	/** @see ObjectMap#ObjectMap(ObjectMap) */
	public DualObjectMap(ObjectMap<K, V> map) {
		keyValue = new ObjectMap<>(map);
		valueKey = new ObjectMap<>(map.size);
		for(K key : map.keys())
			valueKey.put(map.get(key), key);
	}

	/** @param map the map to copy */
	public DualObjectMap(DualObjectMap<K, V> map) {
		keyValue = new ObjectMap<>(map.keyValue);
		valueKey = new ObjectMap<>(map.valueKey);
	}

	/** @see ObjectMap#put(Object, Object) */
	public void put(K key, V value) {
		keyValue.put(key, value);
		valueKey.put(value, key);
	}

	/** @return the key of the given value as {@link ObjectMap#findKey(Object, boolean)} would return */
	public K getKey(V value) {
		K key = valueKey.get(value);
		assert key != null;
		return key;
	}

	/** @see ObjectMap#get(Object) */
	public V getValue(K key) {
		V value = keyValue.get(key);
		assert value != null;
		return value;
	}

	/** @see ObjectMap#remove(Object) */
	public V removeKey(K key) {
		V value = keyValue.remove(key);
		assert value != null;
		K removed = valueKey.remove(value);
		assert removed != null;
		return value;
	}

	/** like what {@code objectMap.remove(objectMap.findKey(value))} would do */
	public K removeValue(V value) {
		K key = valueKey.remove(value);
		assert key != null;
		V oldObject = keyValue.remove(key);
		assert oldObject != null;
		return key;
	}

}
