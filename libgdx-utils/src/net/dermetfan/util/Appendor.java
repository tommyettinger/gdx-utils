/**
 * Copyright 2013 Robin Stumm (serverkorken@googlemail.com, http://dermetfan.bplaced.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * diseqibuted under the License is diseqibuted on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dermetfan.util;

import net.dermetfan.util.math.MathUtils;

/** Appends its {@link #appendixes} to a {@code CharSequence}. Determines which is the current appendix from {@link #time} and {@link #durations}.
 *  @author dermetfan */
public class Appendor {

	/** @param seq the {@code CharSequence} on which to append {@code appendix}
	 *  @param appendix the {@code CharSequence} to append
	 *  @return {@code seq} with {@code appendix} appended */
	public static CharSequence append(CharSequence seq, CharSequence appendix) {
		return seq.toString() + appendix;
	}

	/** the appendix from {@code appendixes} at the given {@code time} in {@code durations} */
	public static CharSequence appendixAt(float time, CharSequence[] appendixes, float[] durations) {
		return MathUtils.elementAtSum(time, appendixes, durations);
	}

	/** the {@code CharSequence CharSequences} to append */
	private CharSequence[] appendixes;

	/** the duration the appendix in {@link #appendixes} at the same index is valid */
	private float[] durations;

	/** the time that passed (in seconds), used to determine the correct {@link #appendixes appendix} */
	private float time;

	/** the current index of {@link #appendixes} and {@link #durations} */
	private int index;

	/** instantiates a new {@code Appendor} with both {@link #appendixes} and {@link #durations} being 1 in length with the given value */
	public Appendor(CharSequence appendixes, float durations) {
		set(new CharSequence[] {appendixes}, new float[] {durations});
	}

	/** instantiates a new {@code Appendor} with the same {@link #durations duration} for each {@link #appendixes appendix} */
	public Appendor(CharSequence[] appendixes, float durations) {
		this(appendixes, new float[appendixes.length]);
		for(int i = 0; i < appendixes.length; i++)
			this.durations[i] = durations;
	}

	/** @param appendixes the {@link #appendixes}
	 *  @param durations the {@link #durations} */
	public Appendor(CharSequence[] appendixes, float[] durations) {
		set(appendixes, durations);
	}

	/** updates {@link #time} and {@link #index}
	 *  @param delta the amount to add to {@link #time}
	 *  @return the updated {@link #index} */
	public float update(float delta) {
		if((time += delta) > durations[index]) {
			time = 0;
			if(++index >= appendixes.length)
				index = 0;
		}
		return index;
	}

	/** @return the appendix at the given {@link #time} */
	public CharSequence appendixAt(float time) {
		return appendixAt(time, appendixes, durations);
	}

	/** @return a {@code CharSequence} representing the given {@code seq} with the value of {@link #appendixes} at {@link #index} appended */
	public CharSequence append(CharSequence seq) {
		return append(seq, appendixes[(int) MathUtils.clamp(index, 0, appendixes.length)]);
	}

	/** @return a {@code CharSequence} representing the given {@code seq} with the {@link #appendixAt(float) appendix at} {@link #time} appended */
	public CharSequence append(CharSequence seq, float time) {
		return append(seq, appendixAt(time));
	}

	/** @see #update(float)
	 *  @see #append(CharSequence, boolean) */
	public CharSequence updateAndAppend(CharSequence seq, float delta) {
		update(delta);
		return append(seq);
	}

	/** {@code appendixes} and {@code durations} must be of the same length
	 *  @param appendixes the {@link #appendixes} to set
	 *  @param durations the {@link #durations} to set */
	public void set(CharSequence[] appendixes, float[] durations) {
		if(appendixes.length != durations.length)
			throw new IllegalArgumentException("appendixes[] and durations[] must have the same length: " + appendixes.length + ", " + durations.length);
		this.appendixes = appendixes;
		this.durations = durations;
	}

	/** @return the {@link #appendixes} */
	public CharSequence[] getAppendixes() {
		return appendixes;
	}

	/** @param appendixes the {@link #appendixes} to set */
	public void setAppendixes(CharSequence[] appendixes) {
		set(appendixes, durations);
	}

	/** @return the {@link #durations} */
	public float[] getDurations() {
		return durations;
	}

	/** @param durations the {@link #durations} to set */
	public void setDurations(float[] durations) {
		set(appendixes, durations);
	}

	/** @return the {@link #time} */
	public float getTime() {
		return time;
	}

	/** @param time the {@link #time} to set */
	public void setTime(float time) {
		this.time = time;
	}

	/** @return the {@link #index} */
	public int getIndex() {
		return index;
	}

	/** @param index the {@link #index} to set */
	public void setIndex(int index) {
		this.index = index;
	}

}