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

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**	a managed animation state machine
 *  @author dermetfan */
public interface ManagedAnimation extends Animated<Animation> {

	/** {@link #pause() pauses} and {@link #setTime(float) sets} the time to 0 */
	@Override
	default void stop() {
		pause();
		setTime(0);
	}

	/** flips all frames
	 *  @see #flipFrames(boolean, boolean, boolean) */
	default void flipFrames(boolean flipX, boolean flipY) {
		flipFrames(flipX, flipY, false);
	}

	/** flips all frames
	 *  @see #flipFrames(float, float, boolean, boolean, boolean) */
	default void flipFrames(boolean flipX, boolean flipY, boolean set) {
		flipFrames(0, getAnimated().animationDuration, flipX, flipY, set);
	}

	/** flips all frames
	 *  @see #flipFrames(float, float, boolean, boolean, boolean) */
	default void flipFrames(float startTime, float endTime, boolean flipX, boolean flipY) {
		flipFrames(startTime, endTime, flipX, flipY, false);
	}

	/** flips all frames from {@code startTime} to {@code endTime}
	 *  @param startTime the animation state time of the first frame to flip
	 *  @param endTime the animation state time of the last frame to flip
	 *  @param set if the frames should be set to {@code flipX} and {@code flipY} instead of actually flipping them */
	default void flipFrames(float startTime, float endTime, boolean flipX, boolean flipY, boolean set) {
		Animation animation = getAnimated();
		for(float t = startTime; t < endTime; t += animation.frameDuration) {
			TextureRegion frame = animation.getKeyFrame(t);
			frame.flip(flipX && (set ? !frame.isFlipX() : true), flipY && (set ? !frame.isFlipY() : true));
		}
	}

	/** @return if the animation has finished playing
	 * 	@see Animation#isAnimationFinished(float) */
	default boolean isAnimationFinished() {
		return getAnimated().isAnimationFinished(getTime());
	}

}