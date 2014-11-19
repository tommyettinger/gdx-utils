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

package net.dermetfan.gdx.maps;

import java.util.Iterator;

import com.badlogic.gdx.maps.Map;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.maps.tiled.TiledMapTileSets;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.StringBuilder;
import com.badlogic.gdx.utils.reflect.ClassReflection;

/** provides useful methods for dealing with maps
 *  @author dermetfan */
public class MapUtils {

	/** for internal, temporary usage */
	private static final Vector2 vec2 = new Vector2();

	/** Finds a property in an array of {@link MapProperties}. If multiple {@link MapProperties} contain the value, the later given one's value is returned.
	 *  @param key the key
	 *  @param defaultValue the default value
	 *  @param properties the {@link MapProperties} to search
	 *  @param <T> the type of the value
	 *  @return the last found value or defaultValue */
	public static <T> T findProperty(String key, T defaultValue, MapProperties... properties) {
		T value = defaultValue;
		for(MapProperties property : properties)
			value = getProperty(property, key, value);
		return value;
	}

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

		if(value == null || value instanceof String && ((String) value).length() == 0)
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

	/** @see #readableHierarchy(com.badlogic.gdx.maps.Map, int) */
	public static String readableHierarchy(Map map) {
		return readableHierarchy(map, 0);
	}

	/** @param map the map to represent
	 *  @param indent the indentation size (indent is {@code '\t'})
	 *  @return a human-readable hierarchy of the given map and its descendants */
	public static String readableHierarchy(Map map, int indent) {
		StringBuilder hierarchy = new StringBuilder();
		for(int i = 0; i < indent; i++)
			hierarchy.append('\t');
		hierarchy.append(ClassReflection.getSimpleName(map.getClass())).append('\n');
		hierarchy.append(readableHierarchy(map.getProperties(), indent + 1));
		if(map instanceof TiledMap)
			hierarchy.append(readableHierarchy(((TiledMap) map).getTileSets(), indent + 1));
		hierarchy.append(readableHierarchy(map.getLayers(), indent + 1));
		return hierarchy.toString();
	}

	/** @see #readableHierarchy(com.badlogic.gdx.maps.Map, int) */
	public static String readableHierarchy(TiledMapTileSets sets, int indent) {
		StringBuilder hierarchy = new StringBuilder();
		for(TiledMapTileSet set : sets)
			hierarchy.append(readableHierarchy(set, indent));
		return hierarchy.toString();
	}

	/** @see #readableHierarchy(com.badlogic.gdx.maps.Map, int) */
	public static String readableHierarchy(TiledMapTileSet set, int indent) {
		StringBuilder hierarchy = new StringBuilder();
		for(int i = 0; i < indent; i++)
			hierarchy.append('\t');
		hierarchy.append(ClassReflection.getSimpleName(set.getClass())).append(' ').append(set.getName()).append(" (").append(set.size()).append(" tiles)\n");
		hierarchy.append(readableHierarchy(set.getProperties(), indent + 1));
		for(TiledMapTile tile : set)
			hierarchy.append(readableHierarchy(tile, indent + 1));
		return hierarchy.toString();
	}

	/** @see #readableHierarchy(com.badlogic.gdx.maps.Map, int) */
	public static String readableHierarchy(TiledMapTile tile, int indent) {
		StringBuilder hierarchy = new StringBuilder();
		for(int i = 0; i < indent; i++)
			hierarchy.append('\t');
		hierarchy.append(ClassReflection.getSimpleName(tile.getClass())).append(" (ID: ").append(tile.getId()).append(", offset: ").append(tile.getOffsetX()).append('x').append(tile.getOffsetY()).append(", BlendMode: ").append(tile.getBlendMode()).append(")\n");
		hierarchy.append(readableHierarchy(tile.getProperties(), indent + 1));
		return hierarchy.toString();
	}

	/** @see #readableHierarchy(com.badlogic.gdx.maps.Map, int) */
	public static String readableHierarchy(MapLayers layers, int indent) {
		StringBuilder hierarchy = new StringBuilder();
		for(MapLayer layer : layers)
			hierarchy.append(readableHierarchy(layer, indent));
		return hierarchy.toString();
	}

	/** @see #readableHierarchy(com.badlogic.gdx.maps.Map, int) */
	public static String readableHierarchy(MapLayer layer, int indent) {
		StringBuilder hierarchy = new StringBuilder();
		for(int i = 0; i < indent; i++)
			hierarchy.append('\t');
		hierarchy.append(ClassReflection.getSimpleName(layer.getClass()));
		if(layer instanceof TiledMapTileLayer) {
			TiledMapTileLayer tileLayer = (TiledMapTileLayer) layer;
			hierarchy.append(" (size: ").append(tileLayer.getWidth()).append('x').append(tileLayer.getHeight()).append(", tile size: ").append(tileLayer.getTileWidth()).append('x').append(tileLayer.getTileHeight()).append(')');
		} else
			hierarchy.append(' ').append(layer.getName());
		hierarchy.append('\n');
		hierarchy.append(readableHierarchy(layer.getProperties(), indent + 1));
		hierarchy.append(readableHierarchy(layer.getObjects(), indent + 1));
		return hierarchy.toString();
	}

	/** @see #readableHierarchy(com.badlogic.gdx.maps.Map, int) */
	public static String readableHierarchy(MapObjects objects, int indent) {
		StringBuilder hierarchy = new StringBuilder();
		for(MapObject object : objects)
			hierarchy.append(readableHierarchy(object, indent));
		return hierarchy.toString();
	}

	/** @see #readableHierarchy(com.badlogic.gdx.maps.Map, int) */
	public static String readableHierarchy(MapObject object, int indent) {
		StringBuilder hierarchy = new StringBuilder();
		for(int i = 0; i < indent; i++)
			hierarchy.append('\t');
		hierarchy.append(ClassReflection.getSimpleName(object.getClass())).append(' ').append(object.getName()).append('\n');
		hierarchy.append(readableHierarchy(object.getProperties(), indent + 1));
		return hierarchy.toString();
	}

	/** @see #readableHierarchy(com.badlogic.gdx.maps.Map, int) */
	public static String readableHierarchy(MapProperties properties, int indent) {
		StringBuilder hierarchy = new StringBuilder();
		Iterator<String> keys = properties.getKeys();
		while(keys.hasNext()) {
			String key = keys.next();
			for(int i = 0; i < indent; i++)
				hierarchy.append('\t');
			hierarchy.append(key).append(": ").append(properties.get(key).toString()).append('\n');
		}
		return hierarchy.toString();
	}

	/** creates an array of TiledMapTiles from a {@link TiledMapTileSet}
	 * @param tiles the {@link TiledMapTileSet} to create an array from
	 * @return the array of TiledMapTiles */
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
		point.x /= cellWidth;
		point.y = (point.y - cellHeight / 2) / cellHeight + point.x;
		point.x -= point.y - point.x;
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
