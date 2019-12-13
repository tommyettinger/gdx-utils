/** Copyright 2016 Robin Stumm (serverkorken@gmail.com, http://dermetfan.net)
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

package net.dermetfan.utils;

/** Enforces the final keyword at runtime.
 *  @author dermetfan
 *  @since 0.13.4 */
public class LazyFinal<T> {

	private T object;

	/** whether the {@link #object} has been set */
	private boolean initialized;

	/** Declaration. */
	public LazyFinal() {}

	/** Initialization.
	 * @param object the object to initialize with */
	public LazyFinal(T object) {
		init(object);
	}

	/** @param object the {@link #object}
	 *  @throws IllegalStateException if already {@link #initialized initialized} */
	public void init(T object) {
		if(initialized)
			throw new IllegalStateException("already initialized");
		this.object = object;
		initialized = true;
	}

	/** @return the {@link #object}
	 *  @throws IllegalStateException if not yet {@link #initialized initialized} */
	public T get() {
		if(!initialized)
			throw new IllegalStateException("not yet initialized");
		return object;
	}

}
