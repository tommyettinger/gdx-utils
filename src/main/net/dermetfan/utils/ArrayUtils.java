package net.dermetfan.utils;

import net.dermetfan.utils.math.MathUtils;

/** array utility methods
 *  @author dermetfan */
public class ArrayUtils {

	/** @see #toString(Object[], int, int) */
	public static String toString(Object[] array) {
		return toString(array, 0, array != null ? array.length : -1);
	}

	/** @see #toString(Object[], int, int, String) */
	public static String toString(Object[] array, String separator) {
		return toString(array, 0, array != null ? array.length : -1, separator);
	}

	/** @see #toString(Object[], int, int, String) */
	public static String toString(Object[] array, int offset, int length) {
		return toString(array, offset, length, ", ");
	}

	/** a more sophisticated version of {@link java.util.Arrays#toString(Object[])} */
	public static String toString(Object[] array, int offset, int length, String separator) {
		if(array == null)
			return "null";
		checkRegion(array, offset, length);
		if(array.length == 0)
			return "[]";
		StringBuilder s = new StringBuilder(2 + (array.length - 1) * separator.length());
		s.append('[');
		for(int i = offset; i < offset + length;) {
			s.append(String.valueOf(array[i]));
			if(++i < offset + length)
				s.append(separator);
		}
		return s.append(']').toString();
	}

	/** @see #toString(Object[], int, int) */
	public static String toString(int[] array) {
		return toString(array, 0, array != null ? array.length : -1);
	}

	/** @see #toString(Object[], int, int, String) */
	public static String toString(int[] array, String separator) {
		return toString(array, 0, array != null ? array.length : -1, separator);
	}

	/** @see #toString(Object[], int, int, String) */
	public static String toString(int[] array, int offset, int length) {
		return toString(array, offset, length, ", ");
	}

	/** a more sophisticated version of {@link java.util.Arrays#toString(Object[])} */
	public static String toString(int[] array, int offset, int length, String separator) {
		if(array == null)
			return "null";
		checkRegion(array, offset, length);
		if(array.length == 0)
			return "[]";
		StringBuilder s = new StringBuilder(2 + (array.length - 1) * separator.length());
		s.append('[');
		for(int i = offset; i < offset + length;) {
			s.append(array[i]);
			if(++i < offset + length)
				s.append(separator);
		}
		return s.append(']').toString();
	}

	/** @see #toString(Object[], int, int) */
	public static String toString(float[] array) {
		return toString(array, 0, array != null ? array.length : -1);
	}

	/** @see #toString(Object[], int, int, String) */
	public static String toString(float[] array, String separator) {
		return toString(array, 0, array != null ? array.length : -1, separator);
	}

	/** @see #toString(Object[], int, int, String) */
	public static String toString(float[] array, int offset, int length) {
		return toString(array, offset, length, ", ");
	}

	/** a more sophisticated version of {@link java.util.Arrays#toString(Object[])} */
	public static String toString(float[] array, int offset, int length, String separator) {
		if(array == null)
			return "null";
		checkRegion(array, offset, length);
		if(array.length == 0)
			return "[]";
		StringBuilder s = new StringBuilder(2 + (array.length - 1) * separator.length());
		s.append('[');
		for(int i = offset; i < offset + length;) {
			s.append(array[i]);
			if(++i < offset + length)
				s.append(separator);
		}
		return s.append(']').toString();
	}

	/** @param array the array from which to access a value at the wrapped index
	 *  @return the value at the wrapped index
	 *  @see #repeat(int, int) */
	public static <T> T getRepeated(T[] array, int index) {
		return array[repeat(array.length, index)];
	}

	/** @see #getRepeated(Object[], int) */
	public static int getRepeated(int[] array, int index) {
		return array[repeat(array.length, index)];
	}

	/** @see #getRepeated(Object[], int) */
	public static float getRepeated(float[] array, int index) {
		return array[repeat(array.length, index)];
	}

	/** @see #repeat(int, int, int) */
	public static int repeat(int length, int index) {
		return repeat(0, length, index);
	}

	/** Repeats the given index within the given length (of an array). For example for a length of 10:<br>
	 * 	<table summary="index and return value">
	 * 	<tr><th>index</th><th>returns</th></tr>
	 * 	<tr><td>0</td><td>0</td></tr>
	 * 	<tr><td>5</td><td>5</td></tr>
	 * 	<tr><td>10</td><td>0</td></tr>
	 * 	<tr><td>15</td><td>5</td></tr>
	 * 	<tr><td>20</td><td>0</td></tr>
	 * 	<tr><td>55</td><td>5</td></tr>
	 * 	</table>
	 *  @param length the length of the array
	 *  @param index the index to repeat
	 *  @return the repeated index */
	public static int repeat(int offset, int length, int index) {
		return MathUtils.normalize(index, offset, offset + length, false, true);
	}

	/** @param array the array that may contain the given value
	 *  @param value the value to search for in the given array
	 *  @param identity if {@code ==} comparison should be used instead of {@link Object#equals(Object) equals(Object)}
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

	/** @param offset the region offset in the array
	 * @param length the region length in the array
	 * @param otherOffset the region offset in the other array
	 * @param identity if {@code ==} comparison should be used instead of {@link Object#equals(Object) equals(Object)}
	 * @return whether the specified region in both arrays matches
	 * @since 0.13.5 */
	public static boolean regionEquals(final Object[] array, final int offset, final int length, final Object[] other, final int otherOffset, final boolean identity) {
		if (identity) {
			for (int i = offset; i < offset + length; i++) {
				if (array[i] != other[otherOffset + i - offset]) return false;
			}
		} else {
			for (int i = offset; i < offset + length; i++) {
				if (!array[i].equals(other[otherOffset + i - offset])) return false;
			}
		}
		return true;
	}

	/** @see #regionEquals(Object[], int, int, Object[], int, boolean) */
	public static boolean regionEquals(final Object[] array, final int offset, final int length, final Object[] other, final boolean identity) {
		return regionEquals(array, offset, length, other, 0, identity);
	}

	/** @see #regionEquals(Object[], int, int, Object[], boolean) */
	public static boolean regionEquals(final Object[] array, final int offset, final Object[] other, final boolean identity) {
		return regionEquals(array, offset, other.length, other, identity);
	}

	/** @see #regionEquals(Object[], int, int, Object[], int, boolean) */
	public static boolean regionEquals(final long[] array, final int offset, final int length, final long[] other, final int otherOffset) {
		for (int i = offset; i < offset + length; i++) {
			if (array[i] != other[otherOffset + i - offset]) return false;
		}
		return true;
	}

	/** @see #regionEquals(long[], int, int, long[], int) */
	public static boolean regionEquals(final long[] array, final int offset, final int length, final long[] other) {
		return regionEquals(array, offset, length, other, 0);
	}

	/** @see #regionEquals(long[], int, int, long[]) */
	public static boolean regionEquals(final long[] array, final int offset, final long[] other) {
		return regionEquals(array, offset, other.length, other);
	}

	/** @see #regionEquals(Object[], int, int, Object[], int, boolean) */
	public static boolean regionEquals(final int[] array, final int offset, final int length, final int[] other, final int otherOffset) {
		for (int i = offset; i < offset + length; i++) {
			if (array[i] != other[otherOffset + i - offset]) return false;
		}
		return true;
	}

	/** @see #regionEquals(int[], int, int, int[], int) */
	public static boolean regionEquals(final int[] array, final int offset, final int length, final int[] other) {
		return regionEquals(array, offset, length, other, 0);
	}

	/** @see #regionEquals(int[], int, int, int[]) */
	public static boolean regionEquals(final int[] array, final int offset, final int[] other) {
		return regionEquals(array, offset, other.length, other);
	}

	/** @see #regionEquals(Object[], int, int, Object[], int, boolean) */
	public static boolean regionEquals(final double[] array, final int offset, final int length, final double[] other, final int otherOffset, final double epsilon) {
		for (int i = offset; i < offset + length; i++) {
			if (Math.abs(array[i] - other[otherOffset + i - offset]) > epsilon) return false;
		}
		return true;
	}

	/** @see #regionEquals(double[], int, int, double[], int, double) */
	public static boolean regionEquals(final double[] array, final int offset, final int length, final double[] other, final double epsilon) {
		return regionEquals(array, offset, length, other, 0, epsilon);
	}

	/** @see #regionEquals(double[], int, int, double[], double) */
	public static boolean regionEquals(final double[] array, final int offset, final double[] other, final double epsilon) {
		return regionEquals(array, offset, other.length, other, epsilon);
	}

	/** @see #regionEquals(Object[], int, int, Object[], int, boolean) */
	public static boolean regionEquals(final float[] array, final int offset, final int length, final float[] other, final int otherOffset, final float epsilon) {
		for (int i = offset; i < offset + length; i++) {
			if (Math.abs(array[i] - other[otherOffset + i - offset]) > epsilon) return false;
		}
		return true;
	}

	/** @see #regionEquals(float[], int, int, float[], int, float) */
	public static boolean regionEquals(final float[] array, final int offset, final int length, final float[] other, final float epsilon) {
		return regionEquals(array, offset, length, other, 0, epsilon);
	}

	/** @see #regionEquals(float[], int, int, float[], float) */
	public static boolean regionEquals(final float[] array, final int offset, final float[] other, final float epsilon) {
		return regionEquals(array, offset, other.length, other, epsilon);
	}

	/** @see #regionEquals(Object[], int, int, Object[], int, boolean) */
	public static boolean regionEquals(final short[] array, final int offset, final int length, final short[] other, final int otherOffset) {
		for (int i = offset; i < offset + length; i++) {
			if (array[i] != other[otherOffset + i - offset]) return false;
		}
		return true;
	}

	/** @see #regionEquals(short[], int, int, short[], int) */
	public static boolean regionEquals(final short[] array, final int offset, final int length, final short[] other) {
		return regionEquals(array, offset, length, other, 0);
	}

	/** @see #regionEquals(short[], int, int, short[]) */
	public static boolean regionEquals(final short[] array, final int offset, final short[] other) {
		return regionEquals(array, offset, other.length, other);
	}

	/** @see #regionEquals(Object[], int, int, Object[], int, boolean) */
	public static boolean regionEquals(final byte[] array, final int offset, final int length, final byte[] other, final int otherOffset) {
		for (int i = offset; i < offset + length; i++) {
			if (array[i] != other[otherOffset + i - offset]) return false;
		}
		return true;
	}

	/** @see #regionEquals(byte[], int, int, byte[], int) */
	public static boolean regionEquals(final byte[] array, final int offset, final int length, final byte[] other) {
		return regionEquals(array, offset, length, other, 0);
	}

	/** @see #regionEquals(byte[], int, int, byte[]) */
	public static boolean regionEquals(final byte[] array, final int offset, final byte[] other) {
		return regionEquals(array, offset, other.length, other);
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

	/** @see #shift(Object[], int, int, int) */
	public static void shift(Object[] array, int shift) {
		shift(array, 0, array.length, shift);
	}

	/** @param array the array to shift
	 *  @param shift The amount by which to shift. 1 means that every item will be moved 1 index to the right. */
	public static void shift(Object[] array, int offset, int length, int shift) {
		checkRegion(array, offset, length);
		if(shift == 0)
			return;
		shift(array, offset, length, shift, offset);
	}

	/** @param i the current index */
	private static void shift(Object[] array, int offset, int length, int shift, int i) {
		if(i == offset + length)
			return;
		Object newItem = array[repeat(offset, length, i - shift)];
		shift(array, offset, length, shift, i + 1);
		array[i] = newItem;
	}

	/** @see #shift(int[], int, int, int) */
	public static void shift(int[] array, int shift) {
		shift(array, 0, array.length, shift);
	}

	/** @param array the array to shift
	 *  @param shift The amount by which to shift. 1 means that every item will be moved 1 index to the right. */
	public static void shift(int[] array, int offset, int length, int shift) {
		checkRegion(array, offset, length);
		if(shift == 0)
			return;
		shift(array, offset, length, shift, offset);
	}

	/** @param i the current index */
	private static void shift(int[] array, int offset, int length, int shift, int i) {
		if(i == offset + length)
			return;
		int newItem = array[repeat(offset, length, i - shift)];
		shift(array, offset, length, shift, i + 1);
		array[i] = newItem;
	}

	/** @see #shift(float[], int, int, int) */
	public static void shift(float[] array, int shift) {
		shift(array, 0, array.length, shift);
	}

	/** @param array the array to shift
	 *  @param shift The amount by which to shift. 1 means that every item will be moved 1 index to the right. */
	public static void shift(float[] array, int offset, int length, int shift) {
		checkRegion(array, offset, length);
		if(shift == 0)
			return;
		shift(array, offset, length, shift, offset);
	}

	/** @param i the current index */
	private static void shift(float[] array, int offset, int length, int shift, int i) {
		if(i == offset + length)
			return;
		float newItem = array[repeat(offset, length, i - shift)];
		shift(array, offset, length, shift, i + 1);
		array[i] = newItem;
	}

	/** @see #shuffle(Object[], int, int) */
	public static void shuffle(Object[] array) {
		shuffle(array, 0, array.length);
	}

	/** shuffles the given array
	 *  @param array the array to shuffle */
	public static void shuffle(Object[] array, int offset, int length) {
		checkRegion(array, offset, length);
		for(int i = offset + length - 1; i > offset; i--) {
			int ii = (int) (Math.random() * (i + 1));
			Object tmp = array[i];
			array[i] = array[ii];
			array[ii] = tmp;
		}
	}

	/** @see #shuffle(int[], int, int) */
	public static void shuffle(int[] array) {
		shuffle(array, 0, array.length);
	}

	/** shuffles the given array
	 *  @param array the array to shuffle */
	public static void shuffle(int[] array, int offset, int length) {
		checkRegion(array, offset, length);
		for(int i = offset + length - 1; i > offset; i--) {
			int ii = (int) (Math.random() * (i + 1));
			int tmp = array[i];
			array[i] = array[ii];
			array[ii] = tmp;
		}
	}

	/** @see #shuffle(float[], int, int) */
	public static void shuffle(float[] array) {
		shuffle(array, 0, array.length);
	}

	/** shuffles the given array
	 *  @param array the array to shuffle */
	public static void shuffle(float[] array, int offset, int length) {
		checkRegion(array, offset, length);
		for(int i = offset + length - 1; i > offset; i--) {
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

	/** @throws ArrayIndexOutOfBoundsException if an invalid region is specified
	 *  @throws IllegalArgumentException if the array is null */
	public static void checkRegion(Object[] array, int offset, int length) {
		if(array == null)
			throw new IllegalArgumentException("array is null");
		if(offset < 0)
			throw new ArrayIndexOutOfBoundsException("negative offset: " + offset);
		if(length < 0)
			throw new ArrayIndexOutOfBoundsException("negative length: " + length);
		if(offset + length > array.length)
			throw new ArrayIndexOutOfBoundsException(offset + length);
	}

	/** throws an appropriate exception if the specified region of the source array cannot be copied to the destination array starting at the given offset
	 *  @throws ArrayIndexOutOfBoundsException
	 *  @throws IllegalArgumentException
	 *  @see #checkRegion(Object[], int, int) */
	public static void requireCapacity(Object[] source, int offset, int length, Object[] dest, int destOffset) {
		checkRegion(source, offset, length);
		checkRegion(dest, destOffset, length);
	}

	/** @param start the index at which to start (may be negative)
	 *  @param offset the index at which the array interval to use
	 *  @param length the length of the array interval to use
	 *  @param everyXth select every xth item
	 *  @return the capacity needed in the output array
	 *  @see #select(Object[], int, int, int, int, Object[], int) */
	public static int selectCount(int offset, int length, int start, int everyXth) {
		int count = 0;
		for(int i = start - 1; i < offset + length; i += everyXth)
			if(i >= offset)
				count++;
		return count;
	}

	/** @param items the items to select from
	 *  @param start the array index at which to start (may be negative)
	 *  @param everyXth select every xth of items
	 *  @param dest The array to put the values in. Must have at least the capacity returned by {@link #selectCount(int, int, int, int) selectCount}.
	 *  @throws IllegalArgumentException if the given dest array is not null and smaller than the required length
	 *  @return the dest array containing everyXth item of the given items array */
	public static <T> T[] select(T[] items, int offset, int length, int start, int everyXth, T[] dest, int destOffset) {
		int outputLength = selectCount(offset, length, start, everyXth);
		checkRegion(dest, destOffset, outputLength);
		for(int di = destOffset, i = start - 1; di < outputLength; i += everyXth)
			if(i >= offset) {
				dest[di] = items[i];
				di++;
			}
		return dest;
	}

	/** @see #select(Object[], int, int, int, int, Object[], int) */
	public static <T> T[] select(T[] items, int offset, int length, int start, int everyXth, T[] dest) {
		return select(items, offset, length, start, everyXth, dest, 0);
	}

	/** @see #select(Object[], int, int, int, int, Object[]) */
	public static <T> T[] select(T[] items, int start, int everyXth, T[] dest) {
		return select(items, 0, items.length, start, everyXth, dest);
	}

	/** @see #select(Object[], int, int, Object[]) */
	public static <T> T[] select(T[] items, int everyXth, T[] dest) {
		return select(items, 0, everyXth, dest);
	}

	/** @see #select(Object[], int, int, int, int, Object[], int) */
	@SuppressWarnings("unchecked")
	public static <T> T[] select(T[] items, int offset, int length, int start, int everyXth) {
		return select(items, offset, length, start, everyXth, (T[]) new Object[selectCount(offset, length, start, everyXth)], 0); // XXX not using Array#newInstance for GWT compatibility
	}

	/** @see #select(Object[], int, int, int, int) */
	public static <T> T[] select(T[] items, int start, int everyXth) {
		return select(items, 0, items.length, start, everyXth);
	}

	/** @see #select(Object[], int, int) */
	public static <T> T[] select(T[] items, int everyXth) {
		return select(items, 0, everyXth);
	}

	/** @param items the items to select from
	 *  @param indices the indices to select
	 *  @param dest the array to fill
	 *  @return the given dest array */
	@SuppressWarnings("unchecked")
	public static <T> T[] select(T[] items, int[] indices, int indicesOffset, int indicesLength, T[] dest, int destOffset) {
		checkRegion(indices, indicesOffset, indicesLength);
		if(dest != null)
			checkRegion(dest, destOffset, indicesLength);
		else
			dest = (T[]) new Object[destOffset + indicesLength]; // XXX not using Array#newInstance for GWT compatibility
		for(int i = indicesOffset, di = destOffset; i < indicesOffset + indicesLength; i++, di++)
			dest[di] = items[indices[i]];
		return dest;
	}

	/** @see #select(Object[], int[], int, int, Object[], int) */
	public static <T> T[] select(T[] items, int[] indices, T[] dest, int destOffset) {
		return select(items, indices, 0, indices.length, dest, destOffset);
	}

	/** @see #select(Object[], int[], Object[], int) */
	public static <T> T[] select(T[] items, int[] indices, T[] dest) {
		return select(items, indices, dest, 0);
	}

	/** @see #select(Object[], int[], Object[]) */
	public static <T> T[] select(T[] items, int[] indices) {
		return select(items, indices, null);
	}

	// primitive copies

	// float

	/** @throws ArrayIndexOutOfBoundsException if an invalid region is specified
	 *  @throws NullPointerException if the array is null */
	public static void checkRegion(float[] array, int offset, int length) {
		if(array == null)
			throw new NullPointerException("array is null");
		if(offset < 0)
			throw new ArrayIndexOutOfBoundsException("negative offset: " + offset);
		if(length < 0)
			throw new ArrayIndexOutOfBoundsException("negative length: " + length);
		if(offset + length > array.length)
			throw new ArrayIndexOutOfBoundsException(offset + length);
	}

	/** throws an appropriate exception if the specified region of the source array cannot be copied to the destination array starting at the given offset
	 *  @throws ArrayIndexOutOfBoundsException
	 *  @throws IllegalArgumentException
	 *  @see #checkRegion(float[], int, int) */
	public static void requireCapacity(float[] source, int offset, int length, float[] dest, int destOffset) {
		checkRegion(source, offset, length);
		checkRegion(dest, destOffset, length);
	}

	/** @param items the items to select from
	 *  @param start the array index at which to start (may be negative)
	 *  @param everyXth select every xth of items
	 *  @param dest The array to put the values in. Must have at least the capacity returned by {@link #selectCount(int, int, int, int) selectCount}.
	 *  @throws IllegalArgumentException if the given dest array is not null and smaller than the required length
	 *  @return the dest array containing everyXth item of the given items array */
	public static float[] select(float[] items, int offset, int length, int start, int everyXth, float[] dest, int destOffset) {
		int outputLength = selectCount(offset, length, start, everyXth);
		checkRegion(dest, destOffset, outputLength);
		for(int di = destOffset, i = start - 1; di < outputLength; i += everyXth)
			if(i >= offset) {
				dest[di] = items[i];
				di++;
			}
		return dest;
	}

	/** @see #select(Object[], int, int, int, int, Object[], int) */
	public static float[] select(float[] items, int offset, int length, int start, int everyXth, float[] dest) {
		return select(items, offset, length, start, everyXth, dest, 0);
	}

	/** @see #select(Object[], int, int, int, int, Object[]) */
	public static float[] select(float[] items, int start, int everyXth, float[] dest) {
		return select(items, 0, items.length, start, everyXth, dest);
	}

	/** @see #select(Object[], int, int, Object[]) */
	public static float[] select(float[] items, int everyXth, float[] dest) {
		return select(items, 0, everyXth, dest);
	}

	/** @see #select(Object[], int, int, int, int, Object[], int) */
	public static float[] select(float[] items, int offset, int length, int start, int everyXth) {
		return select(items, offset, length, start, everyXth, new float[selectCount(offset, length, start, everyXth)], 0);
	}

	/** @see #select(Object[], int, int, int, int) */
	public static float[] select(float[] items, int start, int everyXth) {
		return select(items, 0, items.length, start, everyXth);
	}

	/** @see #select(Object[], int, int) */
	public static float[] select(float[] items, int everyXth) {
		return select(items, 0, everyXth);
	}

	/** @param items the items to select from
	 *  @param indices the indices to select
	 *  @param dest the array to fill
	 *  @return the given dest array */
	public static float[] select(float[] items, int[] indices, int indicesOffset, int indicesLength, float[] dest, int destOffset) {
		checkRegion(indices, indicesOffset, indicesLength);
		if(dest != null)
			checkRegion(dest, destOffset, indicesLength);
		else
			dest = new float[destOffset + indicesLength];
		for(int i = indicesOffset, di = destOffset; i < indicesOffset + indicesLength; i++, di++)
			dest[di] = items[indices[i]];
		return dest;
	}

	/** @see #select(Object[], int[], int, int, Object[], int) */
	public static float[] select(float[] items, int[] indices, float[] dest, int destOffset) {
		return select(items, indices, 0, indices.length, dest, destOffset);
	}

	/** @see #select(Object[], int[], Object[], int) */
	public static float[] select(float[] items, int[] indices, float[] dest) {
		return select(items, indices, dest, 0);
	}

	/** @see #select(Object[], int[], Object[]) */
	public static float[] select(float[] items, int[] indices) {
		return select(items, indices, null);
	}

	// int

	/** @throws ArrayIndexOutOfBoundsException if an invalid region is specified
	 *  @throws IllegalArgumentException if the array is null */
	public static void checkRegion(int[] array, int offset, int length) {
		if(array == null)
			throw new IllegalArgumentException("array is null");
		if(offset < 0)
			throw new ArrayIndexOutOfBoundsException("negative offset: " + offset);
		if(length < 0)
			throw new ArrayIndexOutOfBoundsException("negative length: " + length);
		if(offset + length > array.length)
			throw new ArrayIndexOutOfBoundsException(offset + length);
	}

	/** throws an appropriate exception if the specified region of the source array cannot be copied to the destination array starting at the given offset
	 *  @throws ArrayIndexOutOfBoundsException
	 *  @throws IllegalArgumentException
	 *  @see #checkRegion(int[], int, int) */
	public static void requireCapacity(int[] source, int offset, int length, int[] dest, int destOffset) {
		checkRegion(source, offset, length);
		checkRegion(dest, destOffset, length);
	}

	/** @param items the items to select from
	 *  @param start the array index at which to start (may be negative)
	 *  @param everyXth select every xth of items
	 *  @param dest The array to put the values in. Must have at least the capacity returned by {@link #selectCount(int, int, int, int) selectCount}.
	 *  @throws IllegalArgumentException if the given dest array is not null and smaller than the required length
	 *  @return the dest array containing everyXth item of the given items array */
	public static int[] select(int[] items, int offset, int length, int start, int everyXth, int[] dest, int destOffset) {
		int outputLength = selectCount(offset, length, start, everyXth);
		checkRegion(dest, destOffset, outputLength);
		for(int di = destOffset, i = start - 1; di < outputLength; i += everyXth)
			if(i >= offset) {
				dest[di] = items[i];
				di++;
			}
		return dest;
	}

	/** @see #select(Object[], int, int, int, int, Object[], int) */
	public static int[] select(int[] items, int offset, int length, int start, int everyXth, int[] dest) {
		return select(items, offset, length, start, everyXth, dest, 0);
	}

	/** @see #select(Object[], int, int, int, int, Object[]) */
	public static int[] select(int[] items, int start, int everyXth, int[] dest) {
		return select(items, 0, items.length, start, everyXth, dest);
	}

	/** @see #select(Object[], int, int, Object[]) */
	public static int[] select(int[] items, int everyXth, int[] dest) {
		return select(items, 0, everyXth, dest);
	}

	/** @see #select(Object[], int, int, int, int, Object[], int) */
	public static int[] select(int[] items, int offset, int length, int start, int everyXth) {
		return select(items, offset, length, start, everyXth, new int[selectCount(offset, length, start, everyXth)], 0);
	}

	/** @see #select(Object[], int, int, int, int) */
	public static int[] select(int[] items, int start, int everyXth) {
		return select(items, 0, items.length, start, everyXth);
	}

	/** @see #select(Object[], int, int) */
	public static int[] select(int[] items, int everyXth) {
		return select(items, 0, everyXth);
	}

	/** @param items the items to select from
	 *  @param indices the indices to select
	 *  @param dest the array to fill
	 *  @return the given dest array */
	public static int[] select(int[] items, int[] indices, int indicesOffset, int indicesLength, int[] dest, int destOffset) {
		checkRegion(indices, indicesOffset, indicesLength);
		if(dest != null)
			checkRegion(dest, destOffset, indicesLength);
		else
			dest = new int[destOffset + indicesLength];
		for(int i = indicesOffset, di = destOffset; i < indicesOffset + indicesLength; i++, di++)
			dest[di] = items[indices[i]];
		return dest;
	}

	/** @see #select(Object[], int[], int, int, Object[], int) */
	public static int[] select(int[] items, int[] indices, int[] dest, int destOffset) {
		return select(items, indices, 0, indices.length, dest, destOffset);
	}

	/** @see #select(Object[], int[], Object[], int) */
	public static int[] select(int[] items, int[] indices, int[] dest) {
		return select(items, indices, dest, 0);
	}

	/** @see #select(Object[], int[], Object[]) */
	public static int[] select(int[] items, int[] indices) {
		return select(items, indices, null);
	}

}
