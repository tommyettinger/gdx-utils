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
import com.badlogic.gdx.utils.ObjectIntMap;

/** an {@link IntMap} and {@link ObjectIntMap} holding each others contents in reverse for fast retrieval of both keys and values
 *  @since 0.5.1
 *  @author dermetfan */
public class DualIntMap<V> {

	private final IntMap<V> keyValue;
	private final ObjectIntMap<V> valueKey;

	public DualIntMap() {
		keyValue = new IntMap<>();
		valueKey = new ObjectIntMap<>();
	}

	public DualIntMap(int initialCapacity) {
		keyValue = new IntMap<>(initialCapacity);
		valueKey = new ObjectIntMap<>(initialCapacity);
	}

	public DualIntMap(int initialCapacity, float loadFactor) {
		keyValue = new IntMap<>(initialCapacity, loadFactor);
		valueKey = new ObjectIntMap<>(initialCapacity, loadFactor);
	}

	public void put(int key, V value) {
		keyValue.put(key, value);
		valueKey.put(value, key);
	}

	public int getKey(V value, int defaultKey) {
		return valueKey.get(value, defaultKey);
	}

	public V getValue(int key) {
		return keyValue.get(key);
	}

	public V removeKey(int key) {
		V value = keyValue.remove(key);
		if(value != null) {
			int removed = valueKey.remove(value, key);
			assert removed == key;
		}
		return value;
	}

	public int removeValue(V value, int defaultKey) {
		int key = valueKey.remove(value, defaultKey);
		keyValue.remove(key);
		return key;
	}

}
