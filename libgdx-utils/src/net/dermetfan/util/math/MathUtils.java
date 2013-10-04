package net.dermetfan.util.math;

/** math utility methods */
public class MathUtils {

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

	/**
	 * scales the given float array to have the given min and max values
	 * @param values the values to scale
	 * @param min the desired minimal value in the array
	 * @param max the desired maximal value in the array
	 * @return the rescaled array
	 */
	public static float[] scale(float[] values, float min, float max) {
		float tmp = amplitude(values) / (max - min);
		for(int i = 0; i < values.length; i++)
			values[i] /= tmp;

		tmp = min - min(values);
		for(int i = 0; i < values.length; i++)
			values[i] += tmp;

		return values;
	}

}
