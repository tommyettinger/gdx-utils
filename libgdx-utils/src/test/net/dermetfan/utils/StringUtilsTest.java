package net.dermetfan.utils;

import org.junit.Test;

import static net.dermetfan.utils.StringUtils.count;
import static org.junit.Assert.assertEquals;

public class StringUtilsTest {

	@Test
	public void countChar() {
		assertEquals(4, count(' ', "1 2 3 4 5"));
		assertEquals(4, count(' ', " 2 3 4 "));
		assertEquals(4, count(' ', "  3  "));
		assertEquals(4, count(' ', "    "));
	}

	@Test
	public void countString() {
		assertEquals(4, count(", ", "1, 2, 3, 4, 5"));
		assertEquals(4, count(", ", ", 2, 3, 4, "));
		assertEquals(4, count(", ", ", , 3, , "));
		assertEquals(4, count(", ", ", , , , "));
		assertEquals(4, count("..", "........"));
		assertEquals(7, count("..", "........", true));
		assertEquals(6, count("...", "........", true));
	}

}
