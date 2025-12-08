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

package net.dermetfan.gdx;

import com.badlogic.gdx.utils.Array;

/** Base class for multiplexers that forward e.g. events to an Array of receivers
 *  @param <T> the type of the receivers
 *  @author dermetfan */
public abstract class Multiplexer<T> {

	/** the receivers */
	protected final Array<T> receivers;

	/** @see Array#Array() */
	public Multiplexer() {
		receivers = new Array<>();
	}

	/** @see Array#Array(int) */
	public Multiplexer(int size) {
		receivers = new Array<>(size);
	}

	/** @param receivers the {@link #receivers} to set */
	@SafeVarargs
	public Multiplexer(T... receivers) {
		this.receivers = new Array<>(receivers);
	}

	/** @param receivers the {@link #receivers} to set */
	public Multiplexer(Array<T> receivers) {
		this.receivers = new Array<>(receivers);
	}

	/** @param receiver the receiver to add */
	public void add(T receiver) {
		receivers.add(receiver);
	}

	/** @param receiver the receiver to remove */
	public boolean remove(T receiver) {
		return receivers.removeValue(receiver, true);
	}

	/** @see Array#clear() */
	public void clear() {
		receivers.clear();
	}

	/** @see Array#size */
	public int size() {
		return receivers.size;
	}

	/** @param receivers the {@link #receivers} to set */
	public void setReceivers(Array<T> receivers) {
		this.receivers.clear();
		this.receivers.addAll(receivers);
	}

	/** @param receivers the {@link #receivers} to set */
	@SuppressWarnings("unchecked")
	public void setReceivers(T... receivers) {
		this.receivers.clear();
		this.receivers.addAll(receivers);
	}

	/** @return the {@link #receivers} */
	public Array<T> getReceivers() {
		return receivers;
	}

}
