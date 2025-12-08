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

import com.badlogic.gdx.utils.reflect.ArrayReflection;

/** an ArrayPool that creates new arrays using reflection
 *  @author dermetfan
 *  @since 0.11.1 */
public class ReflectionArrayPool<T> extends ArrayPool<T> {

	/** the array type */
	public final Class<T> type;

	public ReflectionArrayPool(Class<T> type, int max, int maxEach) {
		super(max, maxEach);
		this.type = type;
	}

	@Override
	@SuppressWarnings("unchecked")
	protected T[] newArray(int length) {
		return (T[]) ArrayReflection.newInstance(type, length);
	}

}
