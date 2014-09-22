package net.dermetfan.gdx;

import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.IntArray;
import net.dermetfan.gdx.utils.ArrayUtils;
import org.junit.Test;

import static org.junit.Assert.*;

public class ArrayUtilsTest {

	@Test
	public void select() {
		assertArrayEquals(new float[] {2f, 5f, 8f}, ArrayUtils.select(new FloatArray(new float[] {0f, 1f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f}), 3, new FloatArray(3)).toArray(), 0);
		assertArrayEquals(new float[] {0f, 0f, 0f}, ArrayUtils.select(new FloatArray(new float[] {0f, 1f, 2f, 0f, 1f, 3f, 0f, 1f, 2f}), -2, 3, null).toArray(), 0);
		assertArrayEquals(new String[] {"zero", "three", "two"}, ArrayUtils.select(new Array<>(new String[] {"zero", "one", "two", "three"}), new IntArray(new int[] {0, 3, 2})).toArray());
	}

	@Test
	public void skipselect() {
		assertEquals(new Array<>(new String[] {"0", "2", "4"}), ArrayUtils.skipselect(new Array<>(new String[] {"0", "1", "2", "3", "4", "5"}), new IntArray(new int[] {0, 1, 1}), null));
		assertEquals(new Array<>(new String[] {"0", "2", "5", "7", "9"}), ArrayUtils.skipselect(new Array<>(new String[] {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"}), new IntArray(new int[] {0, 1, 2}), new IntArray(new int[] {1})));
		assertEquals(new Array<>(new String[] {"0", "2", "5"}), ArrayUtils.skipselect(new Array<>(new String[] {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"}), new IntArray(new int[] {0, 1, 2}), new IntArray(new int[] {10})));

		assertEquals(new Array<>(new String[] {"3", "6", "9"}), ArrayUtils.skipselect(new Array<>(new String[] {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"}), 3, 2));
		assertEquals(new Array<>(new String[] {"2", "8"}), ArrayUtils.skipselect(new Array<>(new String[] {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"}), 2, 5));
	}

}
