package net.dermetfan.util.libgdx;

import net.dermetfan.util.Appendor;

public class Typewriter {

	private CharSequenceInterpolator interpolator = new CharSequenceInterpolator(40);
	private Appendor appendor = new Appendor(new CharSequence[] {"|", ""}, .5f);
	private boolean cursorWhileTyping = false;
	private boolean cursorAfterTyping = true;

	public Typewriter(CharSequence cursor, boolean cursorWhileTyping, boolean cursorAfterTyping) {
		this(cursorWhileTyping, cursorAfterTyping);
		appendor.getAppendixes()[0] = cursor;
	}

	public Typewriter(boolean cursorWhileTyping, boolean cursorAfterTyping) {
		this.cursorWhileTyping = cursorWhileTyping;
		this.cursorAfterTyping = cursorAfterTyping;
	}

	public void update(float delta) {
		interpolator.update(delta);
		appendor.update(delta);
	}

	public CharSequence type(CharSequence str) {
		CharSequence seq = interpolator.interpolate(str);
		if(seq.length() == str.length()) {
			if(cursorAfterTyping)
				seq = appendor.append(seq);
		} else if(cursorWhileTyping)
			seq = appendor.append(seq);
		return seq;
	}

	public CharSequence updateAndType(CharSequence str, float delta) {
		update(delta);
		return type(str);
	}

	//	public void setCursor(CharSequence cursor) {
	//		float showCursorDuration = appendor.getDurations().length < 1 ? .5f : appendor.getDurations()[0];
	//		float hideCursorDuration = appendor.getDurations().length < 2 ? .5f : appendor.getDurations()[1];
	//		setCursor(cursor, showCursorDuration, hideCursorDuration);
	//	}
	//
	//	public void setCursor(CharSequence cursor, float showCursorDuration, float hideCursorDuration) {
	//		appendor.set(new CharSequence[] {cursor, ""}, new float[] {showCursorDuration, hideCursorDuration});
	//	}
	//
	//	public CharSequence getCursor() {
	//		return appendor.getAppendixes()[0];
	//	}
	//
	public void setCursors(CharSequence[] cursors) {
		CharSequence[] curs = new CharSequence[cursors.length * 2];
		for(int i = 0; i < curs.length; i++)
			curs[i] = i % 2 == 0 ? cursors[i / 2] : "";
		float[] durations = appendor.getDurations();
		float[] durs = new float[cursors.length * 2];
		for(int i = 0; i < durs.length; i++)
			durs[i] = durations[i < durations.length ? i : i - durations.length];
		appendor.set(curs, durs);
	}

	public CharSequence[] getCursors() {
		return appendor.getAppendixes();
	}

	//
	//	public void setCursorDurations(float showCursorDuration, float hideCursorDuration) {
	//		appendor.setDurations(new float[] {showCursorDuration, hideCursorDuration});
	//	}
	//
	//	public void setCursorDurations(float[] cursorDurations) {
	//		appendor.setDurations(cursorDurations);
	//	}
	//
	//	public float[] getCursorDurations() {
	//		return appendor.getDurations();
	//	}

	/** @return the {@link #interpolator} */
	public CharSequenceInterpolator getInterpolator() {
		return interpolator;
	}

	/** @param interpolator the {@link #interpolator} to set */
	public void setInterpolator(CharSequenceInterpolator interpolator) {
		this.interpolator = interpolator;
	}

	/** @return the {@link #appendor} */
	public Appendor getAppendor() {
		return appendor;
	}

	/** @param appendor the {@link #appendor} to set */
	public void setAppendor(Appendor appendor) {
		this.appendor = appendor;
	}

}
