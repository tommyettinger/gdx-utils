/** Copyright 2014 Robin Stumm (serverkorken@googlemail.com, http://dermetfan.net/)
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

package net.dermetfan.utils.libgdx.graphics;

/**	an animation state machine
 *  @author dermetfan */
public interface Animated<T> {

	/** @param delta the duration by which to advance the animation */
	default void update(float delta) {
		setTime(getTime() + delta);
	}

	/** plays the animation */
	default void play() {
		setPlaying(true);
	}

	/** pauses the animation */
	default void pause() {
		setPlaying(false);
	}

	/** stops the animation */
	void stop();

	/** @param duration the duration by which to rewind the animation */
	default void rewind(float duration) {
		update(getTime() - duration);
	}

	/** @param time the state time to which to set the animation */
	void setTime(float time);

	/** @return the state time of the animation */
	float getTime();

	/** @param playing if the animation should be playing */
	void setPlaying(boolean playing);

	/** @return if the animation is playing */
	boolean isPlaying();

	/** @return the animated object */
	T getAnimated();

}
