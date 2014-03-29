package net.dermetfan.utils;

import org.junit.Test;
import static org.junit.Assert.*;

public class PairTest {

	@Test
	public void swap() {
		Pair<String, String> pair = new Pair<String, String>("1", "2");
		pair.swap();
		assertEquals(new Pair<String, String>("2", "1"), pair);
	}

}
