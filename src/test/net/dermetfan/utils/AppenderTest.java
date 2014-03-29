package net.dermetfan.utils;

import static org.junit.Assert.*;

import org.junit.Test;

public class AppenderTest {

	/** tests {@link Appender#update(float)}, {@link Appender#append(CharSequence)}, {@link Appender#updateAndAppend(CharSequence, float)}, {@link Appender#getAppendices()}, {@link Appender#getDurations()}, {@link Appender#getTime()}, {@link Appender#appendixAt(float)} {@link Appender#Appender(CharSequence[], float)} */
	@Test
	public void multi() {
		CharSequence[] appendices = new CharSequence[] {"0", "1"};
		Appender appender = new Appender(appendices, .5f);
		assertArrayEquals(appendices, appender.getAppendices());
		assertArrayEquals(new float[] {.5f, .5f}, appender.getDurations(), 0);
		assertEquals(appendices[0], appender.append(""));
		assertEquals(appendices[0], appender.updateAndAppend("", .5f));
		assertEquals("stuff" + appendices[1], appender.updateAndAppend("stuff", .25f));
		assertEquals(.25f, appender.getTime(), 0);
		assertEquals(appendices[1], appender.appendixAt(.75f));
	}

}
