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

package net.dermetfan.gdx.maps.tiled;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Iterator;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.GZIPOutputStream;

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
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Base64Coder;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.StringBuilder;
import com.badlogic.gdx.utils.XmlWriter;
import net.dermetfan.gdx.math.GeometryUtils;

import static com.badlogic.gdx.math.MathUtils.round;
import static net.dermetfan.gdx.maps.MapUtils.getProperty;
import static net.dermetfan.gdx.maps.tiled.TmxMapWriter.Format.Base64;
import static net.dermetfan.gdx.maps.tiled.TmxMapWriter.Format.Base64Gzip;
import static net.dermetfan.gdx.maps.tiled.TmxMapWriter.Format.Base64Zlib;
import static net.dermetfan.gdx.maps.tiled.TmxMapWriter.Format.CSV;
import static net.dermetfan.gdx.maps.tiled.TmxMapWriter.Format.XML;

/** A {@link XmlWriter} with additional {@link #tmx(Map, Format) tmx(..)} methods.
 * 	<strong>Note:</strong> Not available in GWT.
 *  @author dermetfan */
public class TmxMapWriter extends XmlWriter {

	/** the encoding of {@link TiledMapTileLayer layer} data
	 *  @author dermetfan */
	public static enum Format {
		XML, CSV, Base64, Base64Zlib, Base64Gzip
	}

	/** The height of a layer <strong>IN PIXELS</strong>, to invert the y-axis. {@link #setLayerHeight(int) Set} this explicitly if you want to write something that does not know the layer size, like a {@link #tmx(MapLayer) single} or {@link #tmx(MapLayers, Format) multiple} layers or {@link #tmx(MapObject) object}{@link #tmx(MapObjects) s}. */
	private int layerHeight;

	/** creates a new {@link TmxMapWriter} using the given {@link Writer} */
	public TmxMapWriter(Writer writer) {
		super(writer);
	}

	/** @param map the {@link Map} to write in TMX format
	 *  @param format the {@link Format} to use
	 *  @return this {@link TmxMapWriter} */
	public TmxMapWriter tmx(Map map, Format format) throws IOException {
		append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
		append("<!DOCTYPE map SYSTEM \"http://mapeditor.org/dtd/1.0/map.dtd\">\n");

		MapProperties props = map.getProperties();
		int height = getProperty(props, "height", 0);
		int tileHeight = getProperty(props, "tileheight", 0);
		int oldLayerHeight = layerHeight;
		layerHeight = height * tileHeight;

		element("map");
		attribute("version", "1.0");
		attribute("orientation", getProperty(props, "orientation", "orthogonal"));
		attribute("width", getProperty(props, "width", 0));
		attribute("height", height);
		attribute("tilewidth", getProperty(props, "tilewidth", 0));
		attribute("tileheight", tileHeight);

		@SuppressWarnings("unchecked")
		Array<String> excludedKeys = Pools.obtain(Array.class);
		excludedKeys.clear();
		excludedKeys.add("version");
		excludedKeys.add("orientation");
		excludedKeys.add("width");
		excludedKeys.add("height");
		excludedKeys.add("tilewidth");
		excludedKeys.add("tileheight");
		tmx(props, excludedKeys);
		excludedKeys.clear();
		Pools.free(excludedKeys);

		if(map instanceof TiledMap)
			tmx(((TiledMap) map).getTileSets());

		tmx(map.getLayers(), format);

		pop();

		layerHeight = oldLayerHeight;
		return this;
	}

	/** @param properties the {@link MapProperties} to write in TMX format
	 *  @return this {@link TmxMapWriter} */
	public TmxMapWriter tmx(MapProperties properties) throws IOException {
		return tmx(properties, null);
	}

	/** writes nothing if the given {@link MapProperties} are empty or every key is excluded
	 *  @param properties the {@link MapProperties} to write in TMX format
	 *  @param exclude the keys not to write
	 *  @return this {@link TmxMapWriter} */
	public TmxMapWriter tmx(MapProperties properties, Array<String> exclude) throws IOException {
		Iterator<String> keys = properties.getKeys();
		if(!keys.hasNext())
			return this;

		boolean elementEmitted = false;
		while(keys.hasNext()) {
			String key = keys.next();
			if(exclude != null && exclude.contains(key, false))
				continue;
			if(!elementEmitted) {
				element("properties");
				elementEmitted = true;
			}
			element("property").attribute("name", key).attribute("value", properties.get(key)).pop();
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
			attribute("spacing", round(spacing));
		if(!Float.isNaN(margin))
			attribute("margin", round(margin));

		Iterator<TiledMapTile> iter = set.iterator();
		if(iter.hasNext()) {
			TiledMapTile tile = iter.next();
			element("tileoffset");
			attribute("x", round(tile.getOffsetX()));
			attribute("y", round(-tile.getOffsetY()));
			pop();
		}

		element("image");
		attribute("source", getProperty(props, "imagesource", ""));
		attribute("imagewidth", getProperty(props, "imagewidth", 0));
		attribute("imageheight", getProperty(props, "imageheight", 0));
		pop();

		iter = set.iterator();
		if(iter.hasNext()) {
			@SuppressWarnings("unchecked")
			Array<String> asAttributes = Pools.obtain(Array.class);
			asAttributes.clear();
			boolean elementEmitted = false;
			for(TiledMapTile tile = iter.next(); iter.hasNext(); tile = iter.next()) {
				MapProperties tileProps = tile.getProperties();
				for(String attribute : asAttributes)
					if(tileProps.containsKey(attribute)) {
						if(!elementEmitted) {
							element("tile");
							elementEmitted = true;
						}
						attribute(attribute, tileProps.get(attribute));
					}
				tmx(tileProps, asAttributes);
			}
			asAttributes.clear();
			Pools.free(asAttributes);
			if(elementEmitted)
				pop();
		}

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
		attribute("visible", layer.isVisible() ? 1 : 0);
		attribute("opacity", layer.getOpacity());

		tmx(layer.getProperties());

		element("data");
		if(format == XML) {
			attribute("encoding", "xml");
			for(int y = layer.getHeight() - 1; y > -1; y--)
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
		} else if(format == CSV) {
			attribute("encoding", "csv");
			StringBuilder csv = new StringBuilder();
			for(int y = layer.getHeight() - 1; y > -1; y--) {
				for(int x = 0; x < layer.getWidth(); x++) {
					Cell cell = layer.getCell(x, y);
					TiledMapTile tile = cell != null ? cell.getTile() : null;
					csv.append(tile != null ? tile.getId() : 0);
					if(x + 1 < layer.getWidth() || y - 1 > -1)
						csv.append(',');
				}
				csv.append('\n');
			}
			append('\n').append(csv);
		} else if(format == Base64 || format == Base64Zlib || format == Base64Gzip) {
			attribute("encoding", "base64");
			if(format == Base64Zlib)
				attribute("compression", "zlib");
			else if(format == Base64Gzip)
				attribute("compression", "gzip");
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			OutputStream out = format == Base64Zlib ? new DeflaterOutputStream(baos) : format == Base64Gzip ? new GZIPOutputStream(baos) : baos;
			final short LAST_BYTE = 0xFF;
			for(int y = layer.getHeight() - 1; y > -1; y--)
				for(int x = 0; x < layer.getWidth(); x++) {
					Cell cell = layer.getCell(x, y);
					TiledMapTile tile = cell != null ? cell.getTile() : null;
					int gid = tile != null ? tile.getId() : 0;
					out.write(gid & LAST_BYTE);
					out.write(gid >> 8 & LAST_BYTE);
					out.write(gid >> 16 & LAST_BYTE);
					out.write(gid >> 24 & LAST_BYTE);
				}
			if(out instanceof DeflaterOutputStream)
				((DeflaterOutputStream) out).finish();
			out.close();
			baos.close();
			append('\n').append(String.valueOf(Base64Coder.encode(baos.toByteArray()))).append('\n');
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
		if(props.containsKey("gid"))
			attribute("gid", getProperty(props, "gid", 0));

		int objectX = getProperty(props, "x", 0);
		int objectY = getProperty(props, "y", 0);

		if(object instanceof RectangleMapObject) {
			Rectangle rect = ((RectangleMapObject) object).getRectangle();
			int height = round(rect.height);
			attribute("x", objectX).attribute("y", toYDown(objectY + height));
			attribute("width", round(rect.width)).attribute("height", height);
		} else if(object instanceof EllipseMapObject) {
			Ellipse ellipse = ((EllipseMapObject) object).getEllipse();
			int height = round(ellipse.height);
			attribute("x", objectX).attribute("y", toYDown(objectY + height));
			attribute("width", round(ellipse.width)).attribute("height", height);
			element("ellipse").pop();
		} else if(object instanceof CircleMapObject) {
			Circle circle = ((CircleMapObject) object).getCircle();
			attribute("x", objectX).attribute("y", objectY);
			attribute("width", round(circle.radius * 2)).attribute("height", round(circle.radius * 2));
			element("ellipse").pop();
		} else if(object instanceof PolygonMapObject) {
			attribute("x", objectX).attribute("y", toYDown(objectY));
			Polygon polygon = ((PolygonMapObject) object).getPolygon();
			element("polygon");
			FloatArray tmp = Pools.obtain(FloatArray.class);
			tmp.clear();
			tmp.addAll(polygon.getVertices());
			attribute("points", points(GeometryUtils.toYDown(tmp)));
			tmp.clear();
			Pools.free(tmp);
			pop();
		} else if(object instanceof PolylineMapObject) {
			attribute("x", objectX).attribute("y", toYDown(objectY));
			Polyline polyline = ((PolylineMapObject) object).getPolyline();
			element("polyline");
			FloatArray tmp = Pools.obtain(FloatArray.class);
			tmp.clear();
			tmp.addAll(polyline.getVertices());
			attribute("points", points(GeometryUtils.toYDown(tmp)));
			tmp.clear();
			Pools.free(tmp);
			pop();
		}

		if(props.containsKey("rotation"))
			attribute("rotation", getProperty(props, "rotation", 0f));
		if(props.containsKey("visible"))
			attribute("visible", object.isVisible() ? 1 : 0);
		if(object.getOpacity() != 1)
			attribute("opacity", object.getOpacity());

		@SuppressWarnings("unchecked")
		Array<String> excludedKeys = Pools.obtain(Array.class);
		excludedKeys.clear();
		excludedKeys.add("type");
		excludedKeys.add("gid");
		excludedKeys.add("x");
		excludedKeys.add("y");
		excludedKeys.add("width");
		excludedKeys.add("height");
		excludedKeys.add("rotation");
		excludedKeys.add("visible");
		excludedKeys.add("opacity");
		tmx(props, excludedKeys);
		excludedKeys.clear();
		Pools.free(excludedKeys);

		pop();
		return this;
	}

	/** @param vertices the vertices to arrange in TMX format
	 *  @return a String of the given vertices ready for use in TMX maps */
	private String points(FloatArray vertices) {
		StringBuilder points = new StringBuilder();
		for(int i = 0; i < vertices.size; i++)
			points.append(round(vertices.get(i))).append(i % 2 != 0 ? i + 1 < vertices.size ? " " : "" : ",");
		return points.toString();
	}

	/** @see #toYDown(float) */
	public int toYDown(int y) {
		return round(toYDown((float) y));
	}

	/** @param y the y coordinate
	 *  @return the y coordinate converted from a y-up to a y-down coordinate system */
	public float toYDown(float y) {
		return net.dermetfan.utils.math.GeometryUtils.invertAxis(y, layerHeight);
	}

	// getters and setters

	/** @return the {@link #layerHeight} */
	public int getLayerHeight() {
		return layerHeight;
	}

	/** @param layerHeight the {@link #layerHeight} to set */
	public void setLayerHeight(int layerHeight) {
		this.layerHeight = layerHeight;
	}

}
