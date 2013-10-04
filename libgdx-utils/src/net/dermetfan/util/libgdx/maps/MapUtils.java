/**
 * Copyright 2013 Robin Stumm (serverkorken@googlemail.com, http://dermetfan.bplaced.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dermetfan.util.libgdx.maps;

import java.util.Iterator;

import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;

/**
 * provides useful methods when dealing with maps
 * @author dermetfan
 */
public abstract class MapUtils {

	/**
	 * Makes sure the return value is of the desired type. If the value of the property is not of the desired type, it will be parsed. 
	 * @param properties the {@link MapProperties} to get the value from
	 * @param key the key of the property
	 * @param defaultValue the value to return in case the value was null or an empty String or couldn't be returned 
	 * @return the key's value as the type of defaultValue
	 */
	@SuppressWarnings("unchecked")
	public static <T> T getProperty(MapProperties properties, String key, T defaultValue) {
		Object value = properties.get(key);

		if(value == null || value == "")
			return defaultValue;

		if(defaultValue.getClass() == Boolean.class && value.getClass() != Boolean.class)
			return (T) new Boolean(Boolean.parseBoolean(value.toString()));

		if(defaultValue.getClass() == Integer.class && value.getClass() != Integer.class)
			return (T) new Integer(Integer.parseInt(value.toString()));

		if(defaultValue.getClass() == Float.class && value.getClass() != Float.class)
			return (T) new Float(Float.parseFloat(value.toString()));

		if(defaultValue.getClass() == Double.class && value.getClass() != Double.class)
			return (T) new Double(Double.parseDouble(value.toString()));

		if(defaultValue.getClass() == Short.class && value.getClass() != Short.class)
			return (T) new Short(Short.parseShort(value.toString()));

		if(defaultValue.getClass() == Byte.class && value.getClass() != Byte.class)
			return (T) new Byte(Byte.parseByte(value.toString()));

		return (T) value;
	}

	/**
	 * creates an array of TiledMapTiles from a {@link TiledMapTileSet}
	 * @param tiles the {@link TiledMapTileSet} to create an array from
	 * @return the array of TiledMapTiles
	 */
	public static TiledMapTile[] toTiledMapTileArray(TiledMapTileSet tiles) {
		TiledMapTile[] tileArray = new TiledMapTile[tiles.size()];

		int i = -1;
		Iterator<TiledMapTile> tileIterator = tiles.iterator();
		while(tileIterator.hasNext())
			tileArray[++i] = tileIterator.next();

		return tileArray;
	}

}
