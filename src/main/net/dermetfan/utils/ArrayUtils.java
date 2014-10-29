package net.dermetfan.utils;

/** array utility methods
 *  @author dermetfan */
public class ArrayUtils {

	/** @param array the array from which to access a value at the wrapped index
	 *  @return the value at the wrapped index
	 *  @see #wrapIndex(int, int) */
	public static <T> T wrapIndex(int index, T[] array) {
		return array[wrapIndex(index, array.length)];
	}

	/** @see #wrapIndex(int, Object[]) */
	public static int wrapIndex(int index, int[] array) {
		return array[wrapIndex(index, array.length)];
	}

	/** @see #wrapIndex(int, Object[]) */
	public static float wrapIndex(int index, float[] array) {
		return array[wrapIndex(index, array.length)];
	}

	/** Wraps the given index around the given length (of an array). For example for a length of 10:<br>
	 * 	<table summary="index and return value">
	 * 	<tr><th>index</th><th>returns</th></tr>
	 * 	<tr><td>0</td><td>0</td></tr>
	 * 	<tr><td>5</td><td>5</td></tr>
	 * 	<tr><td>10</td><td>0</td></tr>
	 * 	<tr><td>15</td><td>5</td></tr>
	 * 	<tr><td>20</td><td>0</td></tr>
	 * 	<tr><td>55</td><td>5</td></tr>
	 * 	</table>
	 *  @param index the desired index
	 *  @param length the length of the array
	 *  @return the index wrapped around the array length */
	public static int wrapIndex(int index, int length) {
		return (index + length) % length;
	}

	/** @param array the array that may contain the given value
	 *  @param value the value to search for in the given array
	 *  @param identity if {@code ==} comparison should be used instead of <code>{@link Object#equals(Object) .equals()}</code>
	 *  @return if the given value is contained in the given array */
	public static <T> boolean contains(T[] array, T value, boolean identity) {
		int i = array.length - 1;
		if(identity) {
			while(i >= 0)
				if(array[i--] == value)
					return true;
		} else
			while(i >= 0)
				if(array[i--].equals(value))
					return true;
		return false;
	}

	/** @param array the array to check if it contains the other array's values
	 *  @param other the array to check if it is contained in the other array
	 *  @param <T> the type of the containing array
	 *  @param <T2> the type of the contained array
	 *  @return if the second given array's values are completely contained in the first array */
	public static <T, T2 extends T> boolean contains(T[] array, T2[] other, boolean identity) {
		for(T value : other)
			if(!contains(array, value, identity))
				return false;
		return true;
	}

	/** @param array the array to check if it contains the other array's values
	 *  @param other the array to check if any of its values is contained in the other array
	 *  @param <T> the type of the containing array
	 *  @param <T2> the type of the contained array
	 *  @return if any value from the second array is contained in the first array */
	public static <T, T2 extends T> boolean containsAny(T[] array, T2[] other, boolean identity) {
		for(T value : other)
			if(contains(array, value, identity))
				return true;
		return false;
	}

	/** @param obj the object to compare
	 *  @param array the array which items to compare
	 *  @return if the given object equals any of the items in the given array */
	public static boolean equalsAny(Object obj, Object[] array, int offset, int length) {
		for(int i = offset + length - 1; i >= offset; i--)
			if(obj.equals(array[i]))
				return true;
		return false;
	}

	/** @see #equalsAny(Object, Object[], int, int) */
	public static boolean equalsAny(Object obj, Object[] array) {
		return equalsAny(obj, array, 0, array.length);
	}

	/** shuffles the given array
	 *  @param array the array to shuffle */
	public static void shuffle(Object[] array) {
		for(int i = array.length - 1; i > 0; i--) {
			int ii = (int) (Math.random() * (i + 1));
			Object tmp = array[i];
			array[i] = array[ii];
			array[ii] = tmp;
		}
	}

	/** shuffles the given array
	 *  @param array the array to shuffle */
	public static void shuffle(int[] array) {
		for(int i = array.length - 1; i > 0; i--) {
			int ii = (int) (Math.random() * (i + 1));
			int tmp = array[i];
			array[i] = array[ii];
			array[ii] = tmp;
		}
	}

	/** shuffles the given array
	 *  @param array the array to shuffle */
	public static void shuffle(float[] array) {
		for(int i = array.length - 1; i > 0; i--) {
			int ii = (int) (Math.random() * (i + 1));
			float tmp = array[i];
			array[i] = array[ii];
			array[ii] = tmp;
		}
	}

	/** @return an array of the unboxed values from the given values
	 *  @see #box(float[]) */
	public static float[] unbox(Float[] values) {
		float[] unboxed = new float[values.length];
		for(int i = 0; i < unboxed.length; i++)
			unboxed[i] = values[i];
		return unboxed;
	}

	/** @return an array of the boxed values from the given values
	 *  @see #unbox(Float[]) */
	public static Float[] box(float[] values) {
		Float[] boxed = new Float[values.length];
		for(int i = 0; i < boxed.length; i++)
			boxed[i] = values[i];
		return boxed;
	}

	/** @return an array of the unboxed values from the given values
	 *  @see #box(int[])*/
	public static int[] unbox(Integer[] values) {
		int[] unboxed = new int[values.length];
		for(int i = 0; i < unboxed.length; i++)
			unboxed[i] = values[i];
		return unboxed;
	}

	/** @return an array of the boxed values from the given values
	 *  @see #unbox(Integer[]) */
	public static Integer[] box(int[] values) {
		Integer[] boxed = new Integer[values.length];
		for(int i = 0; i < boxed.length; i++)
			boxed[i] = values[i];
		return boxed;
	}

	/** @return an array of the unboxed values from the given values
	 *  @see #box(boolean[])*/
	public static boolean[] unbox(Boolean[] values) {
		boolean[] unboxed = new boolean[values.length];
		for(int i = 0; i < unboxed.length; i++)
			unboxed[i] = values[i];
		return unboxed;
	}

	/** @return an array of the boxed values from the given values
	 *  @see #unbox(Boolean[]) */
	public static Boolean[] box(boolean[] values) {
		Boolean[] boxed = new Boolean[values.length];
		for(int i = 0; i < boxed.length; i++)
			boxed[i] = values[i];
		return boxed;
	}

}
