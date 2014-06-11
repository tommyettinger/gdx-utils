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

}
