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
 *  @author dermetfan
 *  @since 0.6.0 */
public class DualObjectMap<K, V> {

	/** The map holding keys as keys. Do not modify this! */
	private final ObjectMap<K, V> keyToValue;

	/** The map holding values as keys. Do not modify this! */
	private final ObjectMap<V, K> valueToKey;

	/** @see ObjectMap#ObjectMap() */
	public DualObjectMap() {
		keyToValue = new ObjectMap<>();
		valueToKey = new ObjectMap<>();
	}

	/** @see ObjectMap#ObjectMap(int) */
	public DualObjectMap(int initialCapacity) {
		keyToValue = new ObjectMap<>(initialCapacity);
		valueToKey = new ObjectMap<>(initialCapacity);
	}

	/** @see ObjectMap#ObjectMap(int, float) */
	public DualObjectMap(int initialCapacity, float loadFactor) {
		keyToValue = new ObjectMap<>(initialCapacity, loadFactor);
		valueToKey = new ObjectMap<>(initialCapacity, loadFactor);
	}

	/** @see ObjectMap#ObjectMap(ObjectMap) */
	public DualObjectMap(ObjectMap<K, V> map) {
		keyToValue = new ObjectMap<>(map);
		valueToKey = new ObjectMap<>(map.size);
		for(K key : map.keys())
			valueToKey.put(map.get(key), key);
	}

	/** @param map the map to copy */
	public DualObjectMap(DualObjectMap<K, V> map) {
		keyToValue = new ObjectMap<>(map.keyToValue);
		valueToKey = new ObjectMap<>(map.valueToKey);
	}

	/** @see ObjectMap#put(Object, Object) */
	public void put(K key, V value) {
		keyToValue.put(key, value);
		valueToKey.put(value, key);
	}

	/** @return the key of the given value as {@link ObjectMap#findKey(Object, boolean)} would return */
	public K getKey(V value) {
		K key = valueToKey.get(value);
		assert key != null;
		return key;
	}

	/** @see ObjectMap#get(Object) */
	public V getValue(K key) {
		V value = keyToValue.get(key);
		assert value != null;
		return value;
	}

	/** @see ObjectMap#remove(Object) */
	public V removeKey(K key) {
		V value = keyToValue.remove(key);
		assert value != null;
		K removed = valueToKey.remove(value);
		assert removed != null;
		return value;
	}

	/** like what {@code objectMap.remove(objectMap.findKey(value))} would do */
	public K removeValue(V value) {
		K key = valueToKey.remove(value);
		assert key != null;
		V oldObject = keyToValue.remove(key);
		assert oldObject != null;
		return key;
	}

	// getters and setters

	/** @return The {@link #keyToValue}. Only use this if you know what you're doing!
	 *  @since 0.7.1 */
	public ObjectMap<K, V> getKeyToValue() {
		return keyToValue;
	}

	/** @return The {@link #valueToKey}. Only use this if you know what you're doing!
	 *  @since 0.7.1 */
	public ObjectMap<V, K> getValueToKey() {
		return valueToKey;
	}

}
