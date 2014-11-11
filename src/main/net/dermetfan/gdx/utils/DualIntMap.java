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
 *  @author dermetfan
 *  @since 0.6.0 */
public class DualIntMap<V> {

	/** The map holding keys as keys. Do not modify this! */
	private final IntMap<V> keyToValue;

	/** The map holding values as keys. Do not modify this! */
	private final ObjectIntMap<V> valueToKey;

	/** @see IntMap#IntMap() */
	public DualIntMap() {
		keyToValue = new IntMap<>();
		valueToKey = new ObjectIntMap<>();
	}

	/** @see IntMap#IntMap(int) */
	public DualIntMap(int initialCapacity) {
		keyToValue = new IntMap<>(initialCapacity);
		valueToKey = new ObjectIntMap<>(initialCapacity);
	}

	/** @see IntMap#IntMap(int, float) */
	public DualIntMap(int initialCapacity, float loadFactor) {
		keyToValue = new IntMap<>(initialCapacity, loadFactor);
		valueToKey = new ObjectIntMap<>(initialCapacity, loadFactor);
	}

	/** @see IntMap#IntMap(IntMap) */
	public DualIntMap(IntMap<V> map) {
		keyToValue = new IntMap<>(map);
		valueToKey = new ObjectIntMap<>(map.size);
		Keys keys = map.keys();
		while(keys.hasNext) {
			int key = keys.next();
			valueToKey.put(map.get(key), key);
		}
	}

	/** @param map the map to copy */
	public DualIntMap(DualIntMap<V> map) {
		keyToValue = new IntMap<>(map.keyToValue);
		valueToKey = new ObjectIntMap<>(map.valueToKey);
	}

	/** @see IntMap#put(int, Object) */
	public void put(int key, V value) {
		keyToValue.put(key, value);
		valueToKey.put(value, key);
	}

	/** @return the key of the given value as {@link IntMap#findKey(Object, boolean, int)} would return */
	public int getKey(V value, int defaultKey) {
		return valueToKey.get(value, defaultKey);
	}

	/** @see IntMap#get(int) */
	public V getValue(int key) {
		return keyToValue.get(key);
	}

	/** @see IntMap#remove(int) */
	public V removeKey(int key) {
		V value = keyToValue.remove(key);
		if(value != null) {
			int removed = valueToKey.remove(value, key);
			assert removed == key;
		}
		return value;
	}

	/** like what {@code intMap.remove(intMap.findKey(value, true, defaultValue))} would do */
	public int removeValue(V value, int defaultKey) {
		int key = valueToKey.remove(value, defaultKey);
		keyToValue.remove(key);
		return key;
	}

	// getters and setters

	/** @return The {@link #keyToValue}. Only use this if you know what you're doing!
	 *  @since 0.7.1 */
	public IntMap<V> getKeyToValue() {
		return keyToValue;
	}

	/** @return The {@link #valueToKey}. Only use this if you know what you're doing!
	 *  @since 0.7.1 */
	public ObjectIntMap<V> getValueToKey() {
		return valueToKey;
	}

}
