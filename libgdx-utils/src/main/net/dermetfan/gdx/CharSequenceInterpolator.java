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

import com.badlogic.gdx.math.Interpolation;
import net.dermetfan.utils.math.MathUtils;

/** Interpolates the {@code endIndex} of a {@link CharSequence#subSequence(int, int) subSequence} of a {@code CharSequence} by {@link #time} and {@link #charsPerSecond}.
 *  @author dermetfan */
public class CharSequenceInterpolator {

	/** @return the duration it takes to fully interpolate a {@code CharSequence} of the given {@code length} with the given {@link #charsPerSecond} */
	public static float duration(int length, float charsPerSecond) {
		return length * (1 / charsPerSecond);
	}

	/** @param duration the desired duration of the interpolation using the returned chars per second 
	 *  @param length the length of the {@code CharSequence} 
	 *  @return the chars per second needed to get the desired duration when interpolating */
	public static float charsPerSecondFor(float duration, int length) {
		return length / duration;
	}

	/** @param time the {@link #time}
	 *  @param charsPerSecond the {@link #charsPerSecond}
	 *  @param length the length of the {@code CharSequence}
	 *  @return the result of linear interpolation / percent of the {@link #duration(int, float) duration} at the given {@code time} */
	public static float linear(float time, float charsPerSecond, int length) {
		return time / duration(length, charsPerSecond);
	}

	/** @see #interpolate(CharSequence, float, float, int, int, Interpolation) */
	public static CharSequence interpolate(CharSequence seq, float time, float charsPerSecond, Interpolation interpolation) {
		return interpolate(seq, time, charsPerSecond, 0, seq.length(), interpolation);
	}

	/** @param seq the {@code CharSequence} to interpolate
	 *  @param time the {@link #time}
	 *  @param charsPerSecond the {@link #charsPerSecond}
	 *  @param beginIndex the index at which the returned {@code CharSequence} should start
	 *  @param endIndex the index at which the returned {@code CharSequence} should end if the given {@code Interpolation} {@link Interpolation#apply(float, float, float) applied} 1
	 *  @param interpolation the {@code Interpolation} to use
	 *  @return a {@link CharSequence#subSequence(int, int) subsequence} representing the given {@code seq} from {@code beginIndex} to {@code endIndex} with its {@link CharSequence#length() length} interpolated using the given {@code interpolation} */
	public static CharSequence interpolate(CharSequence seq, float time, float charsPerSecond, int beginIndex, int endIndex, Interpolation interpolation) {
		return seq.subSequence(beginIndex, (int) com.badlogic.gdx.math.MathUtils.clamp(MathUtils.replaceNaN(interpolation.apply(beginIndex, endIndex, linear(time, charsPerSecond, endIndex - beginIndex)), 0), beginIndex, endIndex));
	}

	/** the {@link Interpolation} to use */
	private Interpolation interpolation = Interpolation.linear;

	/** the number of chars / indices to proceed in a second */
	private float charsPerSecond;

	/** the time that passed */
	private float time;

	/** @param charsPerSecond the {@link #charsPerSecond}*/
	public CharSequenceInterpolator(float charsPerSecond) {
		this.charsPerSecond = charsPerSecond;
	}

	/** increases {@link #time} by the given delta */
	public void update(float delta) {
		time += delta;
	}

	/** @see #interpolate(CharSequence, float, float, Interpolation) */
	public CharSequence interpolate(CharSequence seq) {
		return interpolate(seq, time, charsPerSecond, interpolation);
	}

	/** {@link #update(float) updates} the {@link #time} and returns the {@link #interpolate(CharSequence) interpolated} given {@code CharSequence} */
	public CharSequence updateAndInterpolate(CharSequence seq, float delta) {
		update(delta);
		return interpolate(seq);
	}

	/** @return the {@link #interpolation} */
	public Interpolation getInterpolation() {
		return interpolation;
	}

	/** @param interpolation the {@link #interpolation} to set */
	public void setInterpolation(Interpolation interpolation) {
		this.interpolation = interpolation;
	}

	/** @return the {@link #charsPerSecond} */
	public float getCharsPerSecond() {
		return charsPerSecond;
	}

	/** @param charsPerSecond the {@link #charsPerSecond} to set */
	public void setCharsPerSecond(float charsPerSecond) {
		this.charsPerSecond = charsPerSecond;
	}

	/** @return the {@link #time} */
	public float getTime() {
		return time;
	}

	/** @param time the {@link #time} to set */
	public void setTime(float time) {
		this.time = time;
	}

}
