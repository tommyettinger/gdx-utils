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

import net.dermetfan.utils.math.MathUtils;

/** Appends its {@link #appendices} to a {@code CharSequence}. Determines which is the current appendix from {@link #time} and {@link #durations}.
 *  @author dermetfan */
public class Appender {

	/** @param seq the {@code CharSequence} on which to append {@code appendix}
	 *  @param appendix the {@code CharSequence} to append
	 *  @return {@code seq} with {@code appendix} appended */
	public static CharSequence append(CharSequence seq, CharSequence appendix) {
		return seq.toString() + appendix;
	}

	/** the appendix from {@code appendices} at the given {@code time} in {@code durations} */
	public static CharSequence appendixAt(float time, CharSequence[] appendices, float[] durations) {
		return MathUtils.elementAtSum(time, durations, appendices);
	}

	/** the {@code CharSequences} to append */
	private CharSequence[] appendices;

	/** the duration the appendix in {@link #appendices} at the same index is valid */
	private float[] durations;

	/** the time that passed since the current {@link #appendices appendix} has been selected / {@link #index} has been {@link #update(float) updated} */
	private float time;

	/** the current index of {@link #appendices} and {@link #durations} */
	private int index;

	/** instantiates a new {@code Appender} with both {@link #appendices} and {@link #durations} being 1 in length with the given value */
	public Appender(CharSequence appendices, float durations) {
		set(new CharSequence[] {appendices}, new float[] {durations});
	}

	/** instantiates a new {@code Appender} with the same {@link #durations duration} for each {@link #appendices appendix} */
	public Appender(CharSequence[] appendices, float durations) {
		this(appendices, new float[appendices.length]);
		for(int i = 0; i < appendices.length; i++)
			this.durations[i] = durations;
	}

	/** @param appendices the {@link #appendices}
	 *  @param durations the {@link #durations} */
	public Appender(CharSequence[] appendices, float[] durations) {
		set(appendices, durations);
	}

	/** updates {@link #time} and {@link #index}
	 *  @param delta the amount to add to {@link #time}
	 *  @return the updated {@link #index} */
	public float update(float delta) {
		if((time += delta) > durations[index]) {
			time -= durations[index];
			if(++index >= appendices.length)
				index = 0;
			if(time > durations[index])
				time -= (int) (time / durations[index]) * durations[index];
		}
		return index;
	}

	/** @return the appendix at the given {@link #time} */
	public CharSequence appendixAt(float time) {
		return appendixAt(time, appendices, durations);
	}

	/** @return a {@code CharSequence} representing the given {@code seq} with the value of {@link #appendices} at {@link #index} appended */
	public CharSequence append(CharSequence seq) {
		return append(seq, appendices[com.badlogic.gdx.math.MathUtils.clamp(index, 0, appendices.length)]);
	}

	/** @return a {@code CharSequence} representing the given {@code seq} with the {@link #appendixAt(float) appendix at} {@link #time} appended */
	public CharSequence append(CharSequence seq, float time) {
		return append(seq, appendixAt(time));
	}

	/** @see #update(float)
	 *  @see #append(CharSequence) */
	public CharSequence updateAndAppend(CharSequence seq, float delta) {
		update(delta);
		return append(seq);
	}

	/** {@code appendices} and {@code durations} must be of the same length
	 *  @param appendices the {@link #appendices} to set
	 *  @param durations the {@link #durations} to set */
	public void set(CharSequence[] appendices, float[] durations) {
		if(appendices.length != durations.length)
			throw new IllegalArgumentException("appendices[] and durations[] must have the same length: " + appendices.length + ", " + durations.length);
		this.appendices = appendices;
		this.durations = durations;
	}

	/** sets the same {@link #durations duration} for each {@link #appendices appendix} */
	public void set(CharSequence[] appendices, float durations) {
		this.appendices = appendices;
		this.durations = new float[appendices.length];
		for(int i = 0; i < appendices.length; i++)
			this.durations[i] = durations;
	}

	/** @return the {@link #appendices} */
	public CharSequence[] getAppendices() {
		return appendices;
	}

	/** @param appendices the {@link #appendices} to set */
	public void setAppendices(CharSequence[] appendices) {
		set(appendices, durations);
	}

	/** @return the {@link #durations} */
	public float[] getDurations() {
		return durations;
	}

	/** @param durations the {@link #durations} to set */
	public void setDurations(float[] durations) {
		set(appendices, durations);
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
