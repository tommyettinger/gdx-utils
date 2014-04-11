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

import java.util.Iterator;

import com.badlogic.gdx.maps.Map;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;

/** provides useful methods when dealing with maps
 *  @author dermetfan */
public abstract class MapUtils {

	/** for internal, temporary usage */
	private static final Vector2 vec2 = new Vector2();

	/** Makes sure the return value is of the desired type (null-safe). If the value of the property is not of the desired type, it will be parsed. 
	 *  @param properties the {@link MapProperties} to get the value from
	 *  @param key the key of the property
	 *  @param defaultValue the value to return in case the value was null or an empty String or couldn't be returned 
	 *  @return the key's value as the type of defaultValue */
	@SuppressWarnings("unchecked")
	public static <T> T getProperty(MapProperties properties, String key, T defaultValue) {
		if(properties == null || key == null)
			return defaultValue;

		Object value = properties.get(key);

		if(value == null || value instanceof String && ((String) value).isEmpty())
			return defaultValue;

		if(defaultValue != null) {
			if(defaultValue.getClass() == Boolean.class && !(value instanceof Boolean))
				return (T) Boolean.valueOf(value.toString());

			if(defaultValue.getClass() == Integer.class && !(value instanceof Integer))
				return (T) Integer.valueOf(Float.valueOf(value.toString()).intValue());

			if(defaultValue.getClass() == Float.class && !(value instanceof Float))
				return (T) Float.valueOf(value.toString());

			if(defaultValue.getClass() == Double.class && !(value instanceof Double))
				return (T) Double.valueOf(value.toString());

			if(defaultValue.getClass() == Long.class && !(value instanceof Long))
				return (T) Long.valueOf(value.toString());

			if(defaultValue.getClass() == Short.class && !(value instanceof Short))
				return (T) Short.valueOf(value.toString());

			if(defaultValue.getClass() == Byte.class && !(value instanceof Byte))
				return (T) Byte.valueOf(value.toString());
		}

		return (T) value;
	}

	/** @param map the {@link Map} which hierarchy to print
	 *  @return a human readable {@link String} of the hierarchy of the {@link MapObjects} of the given {@link Map} */
	public static String readableHierarchy(Map map) {
		String hierarchy = map.getClass().getSimpleName() + "\n", key, layerHierarchy;

		Iterator<String> keys = map.getProperties().getKeys();
		while(keys.hasNext())
			hierarchy += (key = keys.next()) + ": " + map.getProperties().get(key) + "\n";

		for(MapLayer layer : map.getLayers()) {
			hierarchy += "\t" + layer.getName() + " (" + layer.getClass().getSimpleName();
			if(layer instanceof TiledMapTileLayer) {
				TiledMapTileLayer tileLayer = (TiledMapTileLayer) layer;
				hierarchy += ", size: " + tileLayer.getWidth() + "x" + tileLayer.getHeight() + ", tile size: " + tileLayer.getTileWidth() + "x" + tileLayer.getTileHeight();
			}
			hierarchy += "):\n";
			layerHierarchy = readableHierarchy(layer).replace("\n", "\n\t\t");
			layerHierarchy = layerHierarchy.endsWith("\n\t\t") ? layerHierarchy.substring(0, layerHierarchy.lastIndexOf("\n\t\t")) : layerHierarchy;
			if(!layerHierarchy.isEmpty())
				hierarchy += "\t\t" + layerHierarchy + "\n";
		}

		return hierarchy;
	}

	/** @param layer the {@link MapLayer} which hierarchy to print
	 *  @return a human readable {@link String} of the hierarchy of the {@link MapObjects} of the given {@link MapLayer} */
	public static String readableHierarchy(MapLayer layer) {
		String hierarchy = "", key;
		Iterator<String> keys = layer.getProperties().getKeys();
		while(keys.hasNext())
			hierarchy += (key = keys.next()) + ": " + layer.getProperties().get(key) + "\n";
		for(MapObject object : layer.getObjects()) {
			hierarchy += object.getName() + " (" + object.getClass().getSimpleName() + "):\n";
			Iterator<String> objectKeys = object.getProperties().getKeys();
			while(objectKeys.hasNext())
				hierarchy += "\t" + (key = objectKeys.next()) + ": " + object.getProperties().get(key) + "\n";
		}
		return hierarchy;
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
		return toIsometricGridPoint(vec2.set(x, y), cellWidth, cellHeight);
	}

	/** @see #toIsometricGridPoint(Vector2, float, float) */
	public static Vector3 toIsometricGridPoint(Vector3 point, float cellWidth, float cellHeight) {
		Vector2 vec2 = toIsometricGridPoint(point.x, point.y, cellWidth, cellHeight);
		point.x = vec2.x;
		point.y = vec2.y;
		return point;
	}

	/** sets the given Vector2 to the max width and height of all {@link TiledMapTileLayer tile layers} of the given map
	 *  @param map the map to measure
	 *  @param output the Vector2 to set to the map size
	 *  @return the given Vector2 representing the map size */
	public static Vector2 size(TiledMap map, Vector2 output) {
		Array<TiledMapTileLayer> layers = map.getLayers().getByType(TiledMapTileLayer.class);
		float maxWidth = 0, maxTileWidth = 0, maxHeight = 0, maxTileHeight = 0;
		for(TiledMapTileLayer layer : layers) {
			int layerWidth = layer.getWidth(), layerHeight = layer.getHeight();
			float layerTileWidth = layer.getTileWidth(), layerTileHeight = layer.getTileHeight();
			if(layerWidth > maxWidth)
				maxWidth = layerWidth;
			if(layerTileWidth > maxTileWidth)
				maxTileWidth = layerTileWidth;
			if(layerHeight > maxHeight)
				maxHeight = layerHeight;
			if(layerTileHeight > maxTileHeight)
				maxTileHeight = layerTileHeight;
		}
		return output.set(maxWidth * maxTileWidth, maxHeight * maxTileHeight);
	}

}
