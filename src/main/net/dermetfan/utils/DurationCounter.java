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

package net.dermetfan.utils;

/** Calculates how often a certain duration passed.
 *  Adds to {@link #time} until {@link #duration} is reached, then restarts from the rest.
 *  @author dermetfan
 *  @since 0.9.2 */
public class DurationCounter {

	/** the current time */
	private float time;

	/** the duration to count */
	private float duration;

	/** @param duration the {@link #duration} */
	public DurationCounter(float duration) {
		this.duration = duration;
	}

	/** {@link #update(float) updates}, {@link #count() counts} and {@link #restart() restarts}
	 *  @return the result of {@link #count()} */
	public int cycle(float delta) {
		update(delta);
		int count = count();
		restart();
		return count;
	}

	/** @param delta the time that passed since the last time this method was called (will be added to {@link #time})
	 *  @return the new value of {@link #time} */
	public float update(float delta) {
		return time += delta;
	}

	/** @return how many times {@link #duration} passed during {@link #time} */
	public int count() {
		return (int) (time / duration);
	}

	/** @return the new value of {@link #time} */
	public float restart() {
		return time = time % duration;
	}

	// getters and setters

	/** @return the {@link #time} */
	public float getTime() {
		return time;
	}

	/** @param time the {@link #time} to set */
	public void setTime(float time) {
		this.time = time;
	}

	/** @return the {@link #duration} */
	public float getDuration() {
		return duration;
	}

	/** @param duration the {@link #duration} to set */
	public void setDuration(float duration) {
		this.duration = duration;
	}

}

