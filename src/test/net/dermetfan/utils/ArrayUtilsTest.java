package net.dermetfan.utils;

import org.junit.Test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ArrayUtilsTest {

	@Test
	public void contains() {
		Integer[] digits = {1, 2, 3, 4, 5, 6, 7, 8, 9};
		assertTrue(ArrayUtils.contains(digits, 5, false));
		assertFalse(ArrayUtils.contains(digits, 0, false));
		assertTrue(ArrayUtils.contains(digits, new Integer[] {4, 5, 6}, false));
		assertFalse(ArrayUtils.contains(digits, new Integer[] {0, 1, 2}, false));
	}

	@Test
	public void containsAny() {
		Integer[] digits = {1, 2, 3};
		assertTrue(ArrayUtils.containsAny(digits, new Integer[] {2}, false));
		assertTrue(ArrayUtils.containsAny(digits, new Integer[] {1, 2, 3}, false));
		assertFalse(ArrayUtils.containsAny(digits, new Integer[] {0, 5, 9}, false));
	}

	@Test
	public void unbox() {
		Float[] arr = {1f, 2f, 3f};
		int i = -1;
		for(float f : ArrayUtils.unbox(arr))
			if(f != arr[++i])
				fail("expected " + f + ", got " + arr[i]);
	}

	@Test
	public void box() {
		float[] arr = {1, 2, 3};
		int i = -1;
		for(Float f : ArrayUtils.box(arr))
			if(f != arr[++i])
				fail("expected " + f + ", got " + arr[i]);
	}

	@Test
	public void select() {
		assertArrayEquals(new Float[] {2f, 5f, 8f}, ArrayUtils.select(new Float[] {0f, 1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f}, 3, new Float[3]));
		assertArrayEquals(new Float[] {0f, 0f, 0f}, ArrayUtils.select(new Float[] {0f, 1f, 2f, 0f, 1f, 3f, 0f, 1f, 2f}, -2, 3, null));
		assertArrayEquals(new String[] {"zero", "three", "two"}, ArrayUtils.select(new String[] {"zero", "one", "two", "three"}, new int[] {0, 3, 2}));
	}

	@Test
	public void skipselect() {
		assertArrayEquals(new String[] {"0", "2", "4"}, ArrayUtils.skipselect(new String[] {"0", "1", "2", "3", "4", "5"}, new int[] {0, 1, 1}, null));
		assertArrayEquals(new String[] {"0", "2", "5", "7", "9"}, ArrayUtils.skipselect(new String[] {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"}, new int[] {0, 1, 2}, new int[] {1}));
		assertArrayEquals(new String[] {"0", "2", "5"}, ArrayUtils.skipselect(new String[] {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"}, new int[] {0, 1, 2}, new int[] {10}));

		assertArrayEquals(new String[] {"3", "6", "9"}, ArrayUtils.skipselect(new String[] {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"}, 3, 2));
		assertArrayEquals(new String[] {"2", "8"}, ArrayUtils.skipselect(new String[] {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"}, 2, 5));
	}

}
