package net.dermetfan.utils;

import org.junit.Test;
import static org.junit.Assert.*;

public class PairTest {

	@Test
	public void swap() {
		Pair<String, String> pair = new Pair<>("1", "2");
		pair.swap();
		assertEquals(new Pair<>("2", "1"), pair);
	}

	@Test
	public void isFull() {
		Pair<String, String> pair = new Pair<>("1", "2");
		assertTrue(pair.isFull());
		pair.clearValue();
		assertFalse(pair.isFull());
		pair.clear();
		assertFalse(pair.isFull());
	}

	@Test
	public void isEmpty() {
		Pair<String, String> pair = new Pair<>("1", "2");
		assertFalse(pair.isEmpty());
		pair.clearValue();
		assertFalse(pair.isEmpty());
		pair.clear();
		assertTrue(pair.isEmpty());
	}

}
