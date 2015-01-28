package net.dermetfan.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DurationCounterTest {

	@Test
	public void cycle() {
		DurationCounter counter = new DurationCounter(1 / 60f);
		assertEquals(1, counter.cycle(1 / 60f));
		assertEquals(1, counter.cycle(1 / 60f));
		assertEquals(2, counter.cycle(2 / 60f));
		assertEquals(0, counter.cycle(.5f / 60f));
		assertEquals(1, counter.cycle(.5f / 60f));
		assertEquals(0, counter.cycle(0));
		assertEquals(-1, counter.cycle(-1 / 60f));
		assertEquals(-2, counter.cycle(-2 / 60f));
		assertEquals(0, counter.cycle(1 / 60f * .9f));
		assertEquals(1, counter.cycle(1 / 60f * .1f));
	}

}
