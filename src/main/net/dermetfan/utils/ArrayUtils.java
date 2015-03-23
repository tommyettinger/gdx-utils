package net.dermetfan.utils;

import java.lang.reflect.Array;

/** array utility methods
 *  @author dermetfan */
public class ArrayUtils {

	/** @param array the array from which to access a value at the wrapped index
	 *  @return the value at the wrapped index
	 *  @see #repeat(int, int) */
	public static <T> T getRepeated(T[] array, int index) {
		return array[repeat(index, array.length)];
	}

	/** @see #getRepeated(Object[], int) */
	public static int getRepeated(int[] array, int index) {
		return array[repeat(index, array.length)];
	}

	/** @see #getRepeated(Object[], int) */
	public static float getRepeated(float[] array, int index) {
		return array[repeat(index, array.length)];
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
	 *  @param index the desired index
	 *  @param length the length of the array
	 *  @return {@code (index + length) % length} */
	public static int repeat(int index, int length) {
		return (index + length) % length;
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
	 *  @return the dest array or a new array (if dest was null) containing everyXth item of the given items array */
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
		return select(items, offset, length, start, everyXth, (T[]) Array.newInstance(items.getClass().getComponentType(), selectCount(offset, length, start, everyXth)), 0);
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
			dest = (T[]) Array.newInstance(items.getClass().getComponentType(), destOffset + indicesLength);
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
	 *  @throws IllegalArgumentException if the array is null */
	public static void checkRegion(float[] array, int offset, int length) {
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
	 *  @return the dest array or a new array (if dest was null) containing everyXth item of the given items array */
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
	 *  @return the dest array or a new array (if dest was null) containing everyXth item of the given items array */
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
