package net.dermetfan.utils;

import org.junit.Test;
import static org.junit.Assert.*;

public class PairTest {

	@Test
	public void swap() {
		Pair<String, String> pair = new Pair<>("1", "2");
		pair.swap();
		assertEquals(new Pair<>("2", "1"), pair);
		assertTrue(pair.isFull());
		assertFalse(pair.isEmpty());
		pair.value(null);
		assertFalse(pair.isFull());
		assertFalse(pair.isEmpty());
		assertFalse(pair.hasValue());
		assertTrue(pair.hasKey());
		pair.clear();
		assertTrue(pair.isEmpty());
		assertFalse(pair.isFull());
		assertFalse(pair.hasKey());
	}

	@Test
	public void isFull() {
		Pair<String, String> pair = new Pair<>("1", "2");
		assertTrue(pair.isFull());
		pair.value(null);
		assertFalse(pair.isFull());
		pair.clear();
		assertFalse(pair.isFull());
	}

	@Test
	public void isEmpty() {
		Pair<String, String> pair = new Pair<>("1", "2");
		assertFalse(pair.isEmpty());
		pair.value(null);
		assertFalse(pair.isEmpty());
		pair.clear();
		assertTrue(pair.isEmpty());
	}

}
