package net.dermetfan.utils;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ArrayUtilsTest {

	@Rule
	public ExpectedException thrown = ExpectedException.none();

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
	public void checkRegion() {
		Object[] array = new Object[10];
		ArrayUtils.checkRegion(array, 0, 10);
		ArrayUtils.checkRegion(array, 1, 9);
		thrown.expect(ArrayIndexOutOfBoundsException.class);
		ArrayUtils.checkRegion(array, 1, 10);
	}

	@Test
	public void checkRegion2() {
		thrown.expect(ArrayIndexOutOfBoundsException.class);
		ArrayUtils.checkRegion(new Object[10], -1, 10);
	}

	@Test
	public void checkRegion3() {
		thrown.expect(ArrayIndexOutOfBoundsException.class);
		ArrayUtils.checkRegion(new Object[10], 0, -1);
	}

	@Test
	public void checkRegion4() {
		thrown.expect(IllegalArgumentException.class);
		ArrayUtils.checkRegion((Object[]) null, 0, 10);
	}

	@Test
	public void requireCapacity() {
		Object[] source = new Object[10], dest = new Object[10];
		ArrayUtils.requireCapacity(source, 0, 10, dest, 0);
		ArrayUtils.requireCapacity(source, 0, 5, dest, 0);
		ArrayUtils.requireCapacity(source, 0, 5, dest, 5);
		ArrayUtils.requireCapacity(source, 0, 3, dest, 5);
		ArrayUtils.requireCapacity(source, 0, 5, dest, 3);
		thrown.expect(ArrayIndexOutOfBoundsException.class);
		ArrayUtils.requireCapacity(source, 0, 10, dest, 1);
	}

	@Test
	public void requireCapacity2() {
		thrown.expect(ArrayIndexOutOfBoundsException.class);
		ArrayUtils.requireCapacity(new Object[10], 0, 9, new Object[10], 2);
	}

	@Test
	public void select() {
		assertArrayEquals(new Integer[] {1, 3, 5}, ArrayUtils.select(new Integer[] {0, 1, 2, 3, 4, 5}, new int[] {1, 3, 5}));
		assertArrayEquals(new Integer[] {2, 5, 8}, ArrayUtils.select(new Integer[] {0, 1, 2, 3, 4, 5, 6, 7, 8, 9}, 3));
		assertArrayEquals(new Integer[] {0, 0, 0}, ArrayUtils.select(new Integer[] {0, 1, 2, 0, 1, 3, 0, 1, 2}, -2, 3));
		assertArrayEquals(new String[] {"zero", "three", "two"}, ArrayUtils.select(new String[] {"zero", "one", "two", "three"}, new int[] {0, 3, 2}));
	}

}
