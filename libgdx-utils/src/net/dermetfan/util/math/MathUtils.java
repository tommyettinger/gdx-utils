package net.dermetfan.util.math;

/** math utility methods */
public class MathUtils {

	/** @return the sum of all values in the given array */
	public static float sum(float[] values) {
		float sum = 0;
		for(float v : values)
			sum += v;
		return sum;
	}

	/** @return the peak-to-peak amplitude of the given array */
	public static float amplitude(float[] f) {
		return Math.abs(max(f) - min(f));
	}

	/** @return the largest element of the given array */
	public static float max(float[] floats) {
		float max = Float.NEGATIVE_INFINITY;
		for(float f : floats)
			max = f > max ? f : max;
		return max;
	}

	/** @return the smallest element of the given array */
	public static float min(float[] floats) {
		float min = Float.POSITIVE_INFINITY;
		for(float f : floats)
			min = f < min ? f : min;
		return min;
	}

	/** @param value the desired value
	 *  @param values the values to inspect
	 *  @param range values out of this range will not be returned
	 *  @return the nearest to value in values, NaN if none is found */
	public static float nearest(float value, float[] values, float range) {
		float diff, smallestDiff = Float.POSITIVE_INFINITY, closest = Float.NaN;

		for(float candidate : values) {
			if(candidate == value)
				return value;
			if((diff = Math.abs(candidate - value)) < smallestDiff)
				if((smallestDiff = diff) <= range)
					closest = candidate;
		}

		return closest;
	}

	/** @return the nearest to value in values
	 *  @see #nearest(float, float[], float) */
	public static float nearest(float value, float[] values) {
		return nearest(value, values, Float.POSITIVE_INFINITY);
	}

	/** scales the given float array to have the given min and max values
	 *  @param values the values to scale
	 *  @param min the desired minimal value in the array
	 *  @param max the desired maximal value in the array
	 *  @param clamp if the values should be clamped to correct floating point inaccuracy
	 *  @return the scaled array */
	public static float[] scale(float[] values, float min, float max, boolean clamp) {
		int i;
		float tmp = amplitude(values) / (max - min);
		for(i = 0; i < values.length; i++)
			values[i] /= tmp;

		tmp = min - min(values);
		for(i = 0; i < values.length; i++)
			values[i] += tmp;

		if(clamp)
			for(i = 0; i < values.length; i++)
				if(values[i] > max)
					values[i] = max;

		return values;
	}

	/** @return an array of the unboxed values of the given values
	 *  @see Float#floatValue() */
	public static float[] unbox(Float[] values) {
		float[] unboxed = new float[values.length];
		for(int i = 0; i < unboxed.length; i++)
			unboxed[i] = values[i].floatValue();
		return unboxed;
	}

}
