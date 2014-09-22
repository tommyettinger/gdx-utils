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
import net.dermetfan.utils.Appender;

/** A Typewriter writing a {@code CharSequence}.<br>
 *  Uses a {@link CharSequenceInterpolator} and {@link Appender}, so {@link Interpolation Interpolations} and custom {@link Appender#setAppendices(CharSequence[]) cursors} can be used.
 *  @author dermetfan */
public class Typewriter {

	/** if the {@link #appender cursor} should be shown when the {@code Typewriter} is typing (false by default) */
	private boolean cursorWhileTyping = false;

	/** if the {@link #appender cursor} should be shown when the {@code Typewriter} is done typing (true by default) */
	private boolean cursorAfterTyping = true;

	/** the {@link CharSequenceInterpolator} used to type */
	private CharSequenceInterpolator interpolator = new CharSequenceInterpolator(40);

	/** the {@link Appender} used for the cursor */
	private Appender appender = new Appender(new CharSequence[] {"|", ""}, .5f);

	/** instantiates a {@code Typewriter} using the default values */
	public Typewriter() {
	}

	/** instantiates a new {@code Typewriter} with the given cursor */
	public Typewriter(CharSequence cursor) {
		appender.getAppendices()[0] = cursor;
	}

	/** instantiates a new {@code Typewriter} with the given {@link #cursorWhileTyping} and {@link #cursorAfterTyping} */
	public Typewriter(boolean cursorWhileTyping, boolean cursorAfterTyping) {
		this.cursorWhileTyping = cursorWhileTyping;
		this.cursorAfterTyping = cursorAfterTyping;
	}

	/** instantiates a new {@code Typewriter} with the given cursor, {@link #cursorWhileTyping} and {@link #cursorAfterTyping}
	 *  @see #Typewriter(boolean, boolean) */
	public Typewriter(CharSequence cursor, boolean cursorWhileTyping, boolean cursorAfterTyping) {
		this(cursorWhileTyping, cursorAfterTyping);
		appender.getAppendices()[0] = cursor;
	}

	/**  updates the time the {@code Typewriter} had to type */
	public void update(float delta) {
		interpolator.update(delta);
		appender.update(delta);
	}

	/** @return the given {@code CharSequence} as far is it could be typed, with or without cursor */
	public CharSequence type(CharSequence seq) {
		CharSequence str = interpolator.interpolate(seq);
		if(str.length() == seq.length()) {
			if(cursorAfterTyping)
				str = appender.append(str);
		} else if(cursorWhileTyping)
			str = appender.append(str);
		return str;
	}

	/** @see #update(float)
	 *  @see #type(CharSequence) */
	public CharSequence updateAndType(CharSequence seq, float delta) {
		update(delta);
		return type(seq);
	}

	/** @see CharSequenceInterpolator#getCharsPerSecond() */
	public float getCharsPerSecond() {
		return interpolator.getCharsPerSecond();
	}

	/** @see CharSequenceInterpolator#setCharsPerSecond(float) */
	public void setCharsPerSecond(float charsPerSecond) {
		interpolator.setCharsPerSecond(charsPerSecond);
	}

	/** @see CharSequenceInterpolator#getTime() */
	public float getTime() {
		return interpolator.getTime();
	}

	/** @see CharSequenceInterpolator#setTime(float) */
	public void setTime(float time) {
		interpolator.setTime(time);
	}

	/** @return the {@link #cursorWhileTyping} */
	public boolean isCursorWhileTyping() {
		return cursorWhileTyping;
	}

	/** @param cursorWhileTyping the {@link #cursorWhileTyping} to set */
	public void setCursorWhileTyping(boolean cursorWhileTyping) {
		this.cursorWhileTyping = cursorWhileTyping;
	}

	/** @return the {@link #cursorAfterTyping} */
	public boolean isCursorAfterTyping() {
		return cursorAfterTyping;
	}

	/** @param cursorAfterTyping the {@link #cursorAfterTyping} to set */
	public void setCursorAfterTyping(boolean cursorAfterTyping) {
		this.cursorAfterTyping = cursorAfterTyping;
	}

	/** @return the {@link #interpolator} */
	public CharSequenceInterpolator getInterpolator() {
		return interpolator;
	}

	/** @param interpolator the {@link #interpolator} to set */
	public void setInterpolator(CharSequenceInterpolator interpolator) {
		this.interpolator = interpolator;
	}

	/** @return the {@link #appender} */
	public Appender getAppender() {
		return appender;
	}

	/** @param appender the {@link #appender} to set */
	public void setAppender(Appender appender) {
		this.appender = appender;
	}

}
