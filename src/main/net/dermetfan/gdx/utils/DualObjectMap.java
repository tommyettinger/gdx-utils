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

/** two {@link ObjectMap ObjectMaps} holding each others contents in reverse for fast retrieval of both keys and values
 *  @since 0.5.1
 *  @author dermetfan */
public class DualObjectMap<K, V> {

	private final ObjectMap<K, V> keyValue;
	private final ObjectMap<V, K> valueKey;

	public DualObjectMap() {
		keyValue = new ObjectMap<>();
		valueKey = new ObjectMap<>();
	}

	public DualObjectMap(int initialCapacity) {
		keyValue = new ObjectMap<>(initialCapacity);
		valueKey = new ObjectMap<>(initialCapacity);
	}

	public DualObjectMap(int initialCapacity, float loadFactor) {
		keyValue = new ObjectMap<>(initialCapacity, loadFactor);
		valueKey = new ObjectMap<>(initialCapacity, loadFactor);
	}

	public void put(K key, V value) {
		keyValue.put(key, value);
		valueKey.put(value, key);
	}

	public K getKey(V value) {
		K key = valueKey.get(value);
		assert key != null;
		return key;
	}

	public V getValue(K key) {
		V value = keyValue.get(key);
		assert value != null;
		return value;
	}

	public V removeKey(K key) {
		V value = keyValue.remove(key);
		assert value != null;
		K removed = valueKey.remove(value);
		assert removed != null;
		return value;
	}

	public K removeValue(V value) {
		K key = valueKey.remove(value);
		assert key != null;
		V oldObject = keyValue.remove(key);
		assert oldObject != null;
		return key;
	}

}
