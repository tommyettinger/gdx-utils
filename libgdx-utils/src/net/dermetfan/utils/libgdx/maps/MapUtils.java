/** Copyright 2013 Robin Stumm (serverkorken@googlemail.com, http://dermetfan.net/)
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

package net.dermetfan.utils.libgdx.maps;

import static net.dermetfan.utils.libgdx.math.GeometryUtils.vec2_0;

import java.util.Iterator;

import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

/** provides useful methods when dealing with maps
 *  @author dermetfan */
public abstract class MapUtils {

	/** Makes sure the return value is of the desired type. If the value of the property is not of the desired type, it will be parsed. 
	 *  @param properties the {@link MapProperties} to get the value from
	 *  @param key the key of the property
	 *  @param defaultValue the value to return in case the value was null or an empty String or couldn't be returned 
	 *  @return the key's value as the type of defaultValue */
	@SuppressWarnings("unchecked")
	public static <T> T getProperty(MapProperties properties, String key, T defaultValue) {
		Object value = properties.get(key);

		if(value == null || defaultValue == null || value instanceof String && ((String) value).isEmpty())
			return defaultValue;

		if(defaultValue.getClass() == Boolean.class && !(value instanceof Boolean))
			return (T) Boolean.valueOf(value.toString());

		if(defaultValue.getClass() == Integer.class && !(value instanceof Integer))
			return (T) Integer.valueOf(value.toString());

		if(defaultValue.getClass() == Float.class && !(value instanceof Float))
			return (T) Float.valueOf(value.toString());

		if(defaultValue.getClass() == Double.class && !(value instanceof Double))
			return (T) Double.valueOf(value.toString());

		if(defaultValue.getClass() == Short.class && !(value instanceof Short))
			return (T) Short.valueOf(value.toString());

		if(defaultValue.getClass() == Byte.class && !(value instanceof Byte))
			return (T) Byte.valueOf(value.toString());

		return (T) value;
	}

	/** creates an array of TiledMapTiles from a {@link TiledMapTileSet}
	 *  @param tiles the {@link TiledMapTileSet} to create an array from
	 *  @return the array of TiledMapTiles */
	public static TiledMapTile[] toTiledMapTileArray(TiledMapTileSet tiles) {
		TiledMapTile[] tileArray = new TiledMapTile[tiles.size()];
		Iterator<TiledMapTile> tileIterator = tiles.iterator();
		for(int i = 0; tileIterator.hasNext(); i++)
			tileArray[i] = tileIterator.next();
		return tileArray;
	}

	/** converts point to its coordinates on an isometric grid
	 *  @param point the point to convert
	 *  @param cellWidth the width of the grid cells
	 *  @param cellHeight the height of the grid cells
	 *  @return the given point converted to its coordinates on an isometric grid */
	public static Vector2 toIsometricGridPoint(Vector2 point, float cellWidth, float cellHeight) {
		point.x = (point.x /= cellWidth) - ((point.y = (point.y - cellHeight / 2) / cellHeight + point.x) - point.x);
		return point;
	}

	/** @see #toIsometricGridPoint(Vector2, float, float) */
	public static Vector2 toIsometricGridPoint(float x, float y, float cellWidth, float cellHeight) {
		return toIsometricGridPoint(vec2_0.set(x, y), cellWidth, cellHeight);
	}

	/** @see #toIsometricGridPoint(Vector2, float, float) */
	public static Vector3 toIsometricGridPoint(Vector3 point, float cellWidth, float cellHeight) {
		Vector2 vec2 = toIsometricGridPoint(point.x, point.y, cellWidth, cellHeight);
		point.x = vec2.x;
		point.y = vec2.y;
		return point;
	}

}
