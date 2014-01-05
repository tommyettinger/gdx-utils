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

import static net.dermetfan.utils.libgdx.maps.MapUtils.getProperty;

import java.io.IOException;
import java.io.Writer;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.badlogic.gdx.maps.Map;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.CircleMapObject;
import com.badlogic.gdx.maps.objects.EllipseMapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.maps.tiled.TiledMapTileSets;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.Ellipse;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Polyline;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.XmlWriter;

/** an {@link XmlWriter} with additional {@link #tmx(Map, Format) tmx(..)} methods
 *  @author dermetfan */
public class TmxMapWriter extends XmlWriter {

	/** the encoding and compression of {@link TiledMapTileLayer layer} data
	 *  @author dermetfan */
	public static enum Format {
		XML, CSV, Base64, Base64Zlib, Base64Gzip
	}

	/** keys that {@link #tmx(MapProperties)} will exclude */
	private final HashSet<String> excludedKeys = new HashSet<String>();

	/** creates a new {@link TmxMapWriter} using the given {@link Writer} */
	public TmxMapWriter(Writer writer) {
		super(writer);
	}

	/** @param map the {@link Map} to write in TMX format
	 *  @param format the {@link Format} to use
	 *  @return this {@link TmxMapWriter} */
	public TmxMapWriter tmx(Map map, Format format) throws IOException {
		append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");

		MapProperties props = map.getProperties();
		element("map");
		attribute("version", "1.0");
		attribute("orientation", getProperty(props, "orientation", "orthogonal"));
		attribute("width", getProperty(props, "width", 0));
		attribute("height", getProperty(props, "height", 0));
		attribute("tilewidth", getProperty(props, "tilewidth", 0));
		attribute("tileheight", getProperty(props, "tilewidth", 0));

		excludedKeys.clear();
		excludedKeys.add("version");
		excludedKeys.add("orientation");
		excludedKeys.add("width");
		excludedKeys.add("height");
		excludedKeys.add("tilewidth");
		excludedKeys.add("tileheight");
		tmx(props, excludedKeys);

		if(map instanceof TiledMap)
			tmx(((TiledMap) map).getTileSets());

		tmx(map.getLayers(), format);

		pop();
		return this;
	}

	/** @param properties the {@link MapProperties} to write in TMX format
	 *  @return this {@link TmxMapWriter} */
	public TmxMapWriter tmx(MapProperties properties) throws IOException {
		return tmx(properties, null);
	}

	/** @param properties the {@link MapProperties} to write in TMX format
	 *  @param exclude the keys that should not be written
	 *  @return this {@link TmxMapWriter} */
	public TmxMapWriter tmx(MapProperties properties, Set<String> exclude) throws IOException {
		Iterator<String> keys = properties.getKeys();
		if(!keys.hasNext())
			return this;

		boolean elementEmitted = false;
		while(keys.hasNext()) {
			String key = keys.next();
			if(exclude != null && exclude.contains(key))
				continue;
			if(!elementEmitted) {
				element("properties");
				elementEmitted = true;
			}
			element(key, properties.get(key));
		}

		if(elementEmitted)
			pop();
		return this;
	}

	/** @param sets the {@link TiledMapTileSets} to write in TMX format
	 *  @return this {@link TmxMapWriter} */
	public TmxMapWriter tmx(TiledMapTileSets sets) throws IOException {
		for(TiledMapTileSet set : sets)
			tmx(set);
		return this;
	}

	/** @param set the {@link TiledMapTileSet} to write in TMX format
	 *  @return this {@link TmxMapWriter} */
	public TmxMapWriter tmx(TiledMapTileSet set) throws IOException {
		MapProperties props = set.getProperties();
		element("tileset");
		attribute("firstgid", getProperty(props, "firstgid", 1));
		attribute("name", set.getName());
		attribute("tilewidth", getProperty(props, "tilewidth", 0));
		attribute("tileheight", getProperty(props, "tileheight", 0));
		float spacing = getProperty(props, "spacing", Float.NaN), margin = getProperty(props, "margin", Float.NaN);
		if(!Float.isNaN(spacing))
			attribute("spacing", (int) spacing);
		if(!Float.isNaN(margin))
			attribute("margin", (int) margin);

		element("image");
		attribute("source", getProperty(props, "imagesource", ""));
		attribute("imagewidth", getProperty(props, "imagewidth", 0));
		attribute("imageheight", getProperty(props, "imageheight", 0));
		pop();

		pop();
		return this;
	}

	/** @param layers the {@link MapLayers}
	 *  @param format the {@link Format} to use
	 *  @return this {@link TmxMapWriter} */
	public TmxMapWriter tmx(MapLayers layers, Format format) throws IOException {
		for(MapLayer layer : layers)
			if(layer instanceof TiledMapTileLayer)
				tmx((TiledMapTileLayer) layer, format);
			else
				tmx(layer);
		return this;
	}

	/** @param layer the {@link MapLayer} to write in TMX format
	 *  @return this {@link TmxMapWriter} */
	public TmxMapWriter tmx(MapLayer layer) throws IOException {
		element("objectgroup");
		attribute("name", layer.getName());
		tmx(layer.getProperties());
		tmx(layer.getObjects());
		pop();
		return this;
	}

	/** @param layer the {@link TiledMapTileLayer} to write in TMX format
	 *  @param format the {@link Format} to use
	 *  @return this {@link TmxMapWriter} */
	public TmxMapWriter tmx(TiledMapTileLayer layer, Format format) throws IOException {
		element("layer");
		attribute("name", layer.getName());
		attribute("width", layer.getWidth());
		attribute("height", layer.getHeight());

		tmx(layer.getProperties());

		element("data");
		switch(format) {
		case XML:
			attribute("encoding", "xml");
			for(int y = 0; y < layer.getHeight(); y++)
				for(int x = 0; x < layer.getWidth(); x++) {
					Cell cell = layer.getCell(x, y);
					if(cell != null) {
						TiledMapTile tile = cell.getTile();
						if(tile == null)
							continue;
						element("tile");
						attribute("gid", tile.getId());
						pop();
					}
				}
			break;
		case CSV:
			attribute("encoding", "csv");
			StringBuilder csv = new StringBuilder();
			for(int y = 0; y < layer.getHeight(); y++) {
				for(int x = 0; x < layer.getWidth(); x++) {
					Cell cell = layer.getCell(x, y);
					if(cell != null) {
						TiledMapTile tile = cell.getTile();
						if(tile != null)
							csv.append(tile.getId());
					}
					if(x + 1 < layer.getWidth() || y + 1 < layer.getHeight())
						csv.append(',');
				}
				csv.append('\n');
			}
			append('\n' + csv.toString());
			break;
		case Base64:
			attribute("encoding", "base64");
			// TODO implement
			break;
		case Base64Gzip:
			attribute("encoding", "base64");
			attribute("compression", "gzip");
			// TODO implement
			break;
		case Base64Zlib:
			attribute("encoding", "base64");
			attribute("compression", "zlib");
			// TODO implement
		}
		pop();

		pop();
		return this;
	}

	/** @param objects the {@link MapObject} to write in TMX format
	 *  @return this {@link TmxMapWriter} */
	public TmxMapWriter tmx(MapObjects objects) throws IOException {
		for(MapObject object : objects)
			tmx(object);
		return this;
	}

	/** @param object the {@link MapObject} to write in TMX format
	 *  @return this {@link TmxMapWriter} */
	public TmxMapWriter tmx(MapObject object) throws IOException {
		MapProperties props = object.getProperties();
		element("object");
		attribute("name", object.getName());
		if(props.containsKey("type"))
			attribute("type", getProperty(props, "type", ""));
		attribute("x", getProperty(props, "x", 0));
		attribute("y", getProperty(props, "y", 0));
		if(props.containsKey("width"))
			attribute("width", getProperty(props, "width", 0));
		if(props.containsKey("height"))
			attribute("height", getProperty(props, "height", 0));

		excludedKeys.clear();
		excludedKeys.add("type");
		excludedKeys.add("x");
		excludedKeys.add("y");
		excludedKeys.add("width");
		excludedKeys.add("height");
		tmx(props, excludedKeys);

		if(object instanceof RectangleMapObject) {
			Rectangle rect = ((RectangleMapObject) object).getRectangle();
			element("rectangle");
			attribute("x", rect.x);
			attribute("y", rect.y);
			attribute("width", rect.width);
			attribute("height", rect.height);
			pop();
		} else if(object instanceof CircleMapObject) {
			Circle circle = ((CircleMapObject) object).getCircle();
			element("circle");
			attribute("x", circle.x);
			attribute("y", circle.y);
			attribute("radius", circle.radius);
			pop();
		} else if(object instanceof PolygonMapObject) {
			Polygon polygon = ((PolygonMapObject) object).getPolygon();
			element("polygon");
			attribute("points", points(polygon.getVertices()));
			pop();
		} else if(object instanceof PolylineMapObject) {
			Polyline polyline = ((PolylineMapObject) object).getPolyline();
			element("polyline");
			attribute("points", points(polyline.getVertices()));
			pop();
		} else if(object instanceof EllipseMapObject) {
			Ellipse ellipse = ((EllipseMapObject) object).getEllipse();
			element("ellipse");
			attribute("x", ellipse.x);
			attribute("y", ellipse.y);
			attribute("width", ellipse.width);
			attribute("height", ellipse.height);
			pop();
		}

		pop();
		return this;
	}

	/** @param vertices the vertices to arrange in TMX format
	 *  @return a String of the given vertices ready for use TMX maps */
	private static String points(float[] vertices) {
		String points = "";
		for(int i = 0; i < vertices.length; i++)
			points += (int) vertices[i] + ((i + 1) % 2 == 0 ? i + 1 < vertices.length ? " " : "" : ",");
		return points;
	}

}
