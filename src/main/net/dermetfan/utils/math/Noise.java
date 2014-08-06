/** Copyright 2014 Robin Stumm (serverkorken@gmail.com, http://dermetfan.net)
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License. */

package net.dermetfan.utils.math;

import static net.dermetfan.utils.ArrayUtils.wrapIndex;

import net.dermetfan.utils.Function;
import net.dermetfan.utils.Pair;

import java.util.Random;

/** provides noise algorithms
 *  @author dermetfan */
public abstract class Noise {

	/** the seed used by {@link #random} */
	private static long seed = -1;

	/** if {@link #seed} should be used (false by default) */
	private static boolean seedEnabled;

	/** the {@link Random} used to generate pseudo-random values */
	private static Random random = new Random();

	/** randomizes a given float array using the midpoint-displacement algorithm
	 *  @param values the float array to randomize
	 *  @param range the range used for random values
	 *  @param smoothness the smoothness of the transitions
	 *  @return the randomized float array */
	public static float[] midpointDisplacement(float[] values, float range, float smoothness) {
		for(int i = 0; i < values.length; i++, range /= smoothness)
			values[i] = (wrapIndex(i - 1, values) + wrapIndex(i + 1, values)) / 2 + random(-range, range);
		return values;
	}

	/** @see #midpointDisplacement(int, float, float, boolean, net.dermetfan.utils.Function, int, int) */
	public static float[][] midpointDisplacement(int n, float smoothness, float range, int scaleX, int scaleY) {
		return midpointDisplacement(n, range, smoothness, true, null, scaleX, scaleY);
	}

	/** @see #midpointDisplacement(int, float, float, boolean, net.dermetfan.utils.Function, int, int) */
	public static float[][] midpointDisplacement(int n, float smoothness, float range, Function<Float, Pair<Float, Float>> init, int scaleX, int scaleY) {
		return midpointDisplacement(n, range, smoothness, false, init, scaleX, scaleY);
	}

	/** @see #midpointDisplacement(int, float, float, boolean, float, int, int) */
	public static float[][] midpointDisplacement(int n, float smoothness, float range, final float init, int scaleX, int scaleY) {
		return midpointDisplacement(n, range, smoothness, false, init, scaleX, scaleY);
	}

	/** @see #midpointDisplacement(int, float, float, boolean, net.dermetfan.utils.Function, int, int) */
	public static float[][] midpointDisplacement(int n, float smoothness, float range, boolean initializeRandomly, final float init, int scaleX, int scaleY) {
		return midpointDisplacement(n, range, smoothness, initializeRandomly, initializeRandomly ? null : new Function<Float, Pair<Float, Float>>() {

			@Override
			public Float apply(Pair<Float, Float> object) {
				return init;
			}

		}, scaleX, scaleY);
	}

	/** generates a height map using the midpoint-displacement algorithm
	 *  @param n level of detail
	 *  @param range the range used for random values
	 *  @param smoothness the smoothness of the transitions
	 *  @param initializeRandomly if init should be ignored to use random values instead
	 *  @param init an Accessor that takes the coordinate to be initialized (in a Pair) and returns the value to use for initialization
	 *  @param scaleX scale of the x axis
	 *  @param scaleY scale of the y axis
	 *  @return a height map generated using the midpoint-displacement algorithm */
	private static float[][] midpointDisplacement(int n, float smoothness, float range, boolean initializeRandomly, Function<Float, Pair<Float, Float>> init, int scaleX, int scaleY) {
		if(n < 0)
			throw new IllegalArgumentException("n must be >= 0: " + n);
		range /= 2; // divide range by two to avoid doing it later for random(-range, range) calls

		int x, y, power = (int) Math.pow(2, n), width = scaleX * power + 1, height = scaleY * power + 1, step;
		float[][] map = new float[width][height];
		boolean sy, sx;

		Pair<Float, Float> coord = new Pair<>();

		for(x = 0; x < width; x += power)
			for(y = 0; y < height; y += power)
				map[x][y] = initializeRandomly ? random(-range, range) : init.apply(coord.set((float) x, (float) y));

		for(step = power / 2; step > 0; step /= 2, range /= smoothness) {
			sx = false;
			for(x = 0; x < width; x += step, sx = !sx) {
				sy = false;
				for(y = 0; y < height; y += step, sy = !sy)
					if(sx && sy)
						map[x][y] = (map[x - step][y - step] + map[x + step][y - step] + map[x - step][y + step] + map[x + step][y + step]) / 4 + random(-range, range);
					else if(sx)
						map[x][y] = (map[x - step][y] + map[x + step][y]) / 2 + random(-range, range);
					else if(sy)
						map[x][y] = (map[x][y - step] + map[x][y + step]) / 2 + random(-range, range);
			}
		}
		return map;
	}

	/** @see #diamondSquare(int, float, float, boolean, boolean, boolean, net.dermetfan.utils.Function, int, int) */
	public static float[][] diamondSquare(int n, float smoothness, float range, boolean wrapX, boolean wrapY, Function<Float, Pair<Float, Float>> init, int scaleX, int scaleY) {
		return diamondSquare(n, smoothness, range, wrapX, wrapY, false, init, scaleX, scaleY);
	}

	/** @see #diamondSquare(int, float, float, boolean, boolean, boolean, float, int, int) */
	public static float[][] diamondSquare(int n, float smoothness, float range, boolean wrapX, boolean wrapY, int scaleX, int scaleY) {
		return diamondSquare(n, smoothness, range, wrapX, wrapY, true, Float.NaN, scaleX, scaleY);
	}

	/** @see #diamondSquare(int, float, float, boolean, boolean, boolean, float, int, int) */
	public static float[][] diamondSquare(int n, float smoothness, float range, boolean wrapX, boolean wrapY, float init, int scaleX, int scaleY) {
		return diamondSquare(n, smoothness, range, wrapX, wrapY, false, init, scaleX, scaleY);
	}

	/** @param init the value to initialize every coordinate with
	 *  @see #diamondSquare(int, float, float, boolean, boolean, boolean, net.dermetfan.utils.Function, int, int) */
	public static float[][] diamondSquare(int n, float smoothness, float range, boolean wrapX, boolean wrapY, boolean initializeRandomly, final float init, int scaleX, int scaleY) {
		return diamondSquare(n, smoothness, range, wrapX, wrapY, initializeRandomly, initializeRandomly ? null : new Function<Float, Pair<Float, Float>>() {

			@Override
			public Float apply(Pair<Float, Float> object) {
				return init;
			}

		}, scaleX, scaleY);
	}

	/** generates a height map using the diamond-square algorithm
	 *  @param n level of detail
	 *  @param range the range used for random values
	 *  @param smoothness the smoothness of the transitions
	 *  @param wrapX if the map should wrap on the x axis
	 *  @param wrapY if the map should wrap on the y axis
	 *  @param initializeRandomly if init should be ignored to use random values instead
	 *  @param init an Accessor that takes the coordinate to be initialized (in a Pair) and returns the value to use for initialization
	 *  @param scaleX scale of the x axis
	 *  @param scaleY scale of the y axis
	 *  @return a height map generated using the diamond-square algorithm */
	private static float[][] diamondSquare(int n, float smoothness, float range, boolean wrapX, boolean wrapY, boolean initializeRandomly, Function<Float, Pair<Float, Float>> init, int scaleX, int scaleY) {
		if(n < 0)
			throw new IllegalArgumentException("n must be >= 0: " + n);
		range /= 2; // divide range by two to avoid doing it later for random(-range, range) calls

		int power = (int) Math.pow(2, n), width = scaleX * power + 1, height = scaleY * power + 1, x, y;
		float map[][] = new float[width][height], avg;

		Pair<Float, Float> coord = new Pair<>();

		// seed the grid
		for(x = 0; x < width; x += power)
			for(y = 0; y < height; y += power)
				map[x][y] = initializeRandomly ? random(-range, range) : init.apply(coord.set((float) x, (float) y));

		for(power /= 2; power > 0; power /= 2, range /= smoothness) {
			// square step
			for(x = power; x < width; x += power * 2)
				for(y = power; y < height; y += power * 2)
					map[x][y] = (map[x - power][y - power] + map[x - power][y + power] + map[x + power][y + power] + map[x + power][y - power]) / 4 + random(-range, range);

			// diamond step
			for(x = 0; x < width - (wrapX ? 1 : 0); x += power)
				for(y = power * (1 - x / power % 2); y < height - (wrapY ? 1 : 0); y += power * 2) {
					map[x][y] = (avg = (map[wrapIndex(x - power, width)][y] + map[wrapIndex(x + power, width)][y] + map[x][wrapIndex(y - power, height)] + map[x][wrapIndex(y + power, height)]) / 4) + random(-range, range);

					if(wrapX && x == 0)
						map[width - 1][y] = avg;
					if(wrapY && y == 0)
						map[x][height - 1] = avg;
				}
		}

		return map;
	}

	/** a copy of {@link com.badlogic.gdx.math.MathUtils#random(float, float)} using the {@link #random Random object} of {@link Noise}
	 *  @return a random value between start (inclusive) and end (exclusive) */
	public static float random(float start, float end) {
		return start + random.nextFloat() * (end - start);
	}

	/** @param seedEnabled if {@link #seed} should be used */
	public static void setSeedEnabled(boolean seedEnabled) {
		if(Noise.seedEnabled = seedEnabled)
			random.setSeed(seed);
		else
			random = new Random();
	}

	/** @return the {@link #seedEnabled} */
	public static boolean isSeedEnabled() {
		return seedEnabled;
	}

	/** @return the {@link #seed} */
	public static long getSeed() {
		return seed;
	}

	/** @param seed the {@link #seed} to set */
	public static void setSeed(long seed) {
		random.setSeed(Noise.seed = seed);
	}

	/** @return the {@link #random} */
	public static Random getRandom() {
		return random;
	}

}
