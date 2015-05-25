package net.dermetfan.gdx.utils;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ArrayPoolTest {

	@Test
	public void test() {
		ArrayPool<String> pool = new ArrayPool<String>(5, 2) {
			@Override
			protected String[] newArray(int length) {
				return new String[length];
			}
		};

		String[] test = pool.obtain(5), test1 = pool.obtain(5), test2 = pool.obtain(5), test3 = pool.obtain(5), test4 = pool.obtain(5);
		assertEquals(test.length, 5);
		assertEquals(test1.length, 5);
		assertEquals(test2.length, 5);
		assertEquals(test3.length, 5);
		assertEquals(test4.length, 5);
		assertEquals(pool.getFree(5), 0);

		assertEquals(pool.getFree(5), 0);
		pool.free(test);
		assertEquals(pool.getFree(5), 1);
		pool.free(test1);
		assertEquals(pool.getFree(5), 2);
		pool.free(test2);
		pool.free(test3);
		pool.free(test4);
		assertEquals(pool.getFree(5), 2);

		assertEquals(pool.getFree(4), 0);
		String[] testt = pool.obtain(4), testt1 = pool.obtain(4);
		pool.free(testt);
		assertEquals(pool.getFree(4), 1);
		pool.free(testt1);
		assertEquals(pool.getFree(4), 2);

		pool.clear();
		assertEquals(pool.getFree(5), 0);
		assertEquals(pool.getFree(4), 0);
		assertEquals(pool.getFree(1), 0);
	}

}