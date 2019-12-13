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

import java.util.Comparator;

import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMapTile;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.TiledMapTileSet;
import com.badlogic.gdx.maps.tiled.tiles.AnimatedTiledMapTile;
import com.badlogic.gdx.maps.tiled.tiles.StaticTiledMapTile;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.ObjectMap.Entries;
import com.badlogic.gdx.utils.ObjectMap.Entry;

import static net.dermetfan.gdx.maps.MapUtils.getProperty;
import static net.dermetfan.gdx.maps.MapUtils.toTiledMapTileArray;

/** Animates the tiles in a {@link TiledMapTileLayer tile map layer} by replacing them with {@link AnimatedTiledMapTile animated tiles}.<br>
 * 
 *  To define an animation in the map editor, put an animation property in its properties. The value should be the name of the animation, so the TileAnimator will know which tiles are frames of the same animation.<br>
 *  You can put a property with the desired interval for each animation in the properties of the frame that defines the position of the animation on the map. Note that you cannot define a different for each frame since this is not supported by the {@link AnimatedTiledMapTile animated tiles}.<br>
 *  If an animation should have their frames in a specific order, you can set an ordered property with the index as value to the frames.<br>
 *  <br>
 *  <u>Example</u><br>
 *  There are the following frames with their properties:
 *  <ol>
 *  	<li>
 *   		one that's actually placed on the map to define the position and properties of its animation<br>
 *   		"animation": "waterfall"<br>
 *   		"interval": "0.33"<br>
 *   		"ordered": "1"
 *  	</li>
 *  	<li>
 *  		the second frame (not placed on the map)<br>
 *  		"animation": "waterfall"<br>
 *  		"ordered": "2"
 *  	</li>
 *  	<li>
 *  		the third frame (not placed on the map)<br>
 *  		"animation": "waterfall"<br>
 *  		"ordered": "3"
 *  	</li>
 *  </ol>
 *
 * Obsolete for TMX maps since libGDX has proper support: https://github.com/libgdx/libgdx/pull/2077
 * @author dermetfan */
public abstract class TileAnimator {

	/** animates the tiles that have the animationKey in the target layer
	 *  @param tiles the tiles to create {@link AnimatedTiledMapTile animated tiles} from
	 *  @param layer the target {@link TiledMapTileLayer layer}
	 *  @param animationKey the key used to tell if a tile is a frame
	 *  @param intervalKey the key used to get the animation interval (duration each frame is displayed)
	 *  @param orderedKey The key used to get the frame number of a frame tile in its animation. If no value is found, the frames won't be ordered. */
	public static void animateLayer(TiledMapTile[] tiles, TiledMapTileLayer layer, String animationKey, String orderedKey, String intervalKey, float defaultInterval) {
		ObjectMap<String, Array<StaticTiledMapTile>> animations = filterFrames(tiles, animationKey);
		sortFrames(animations, orderedKey);
		animateLayer(animations, layer, animationKey, intervalKey, defaultInterval);
	}

	/** @see #animateLayer(TiledMapTile[], TiledMapTileLayer, String, String, String, float) */
	public static void animateLayer(TiledMapTileSet tiles, TiledMapTileLayer target, String animationKey, String orderedKey, String intervalKey, float defaultInterval) {
		animateLayer(toTiledMapTileArray(tiles), target, animationKey, orderedKey, intervalKey, defaultInterval);
	}

	/** animates the {@link TiledMapTileLayer target layer} using the given animations
	 *  @param animations the animations to use
	 *  @param layer the {@link TiledMapTileLayer} which tiles to animate
	 *  @param animationKey the key used to tell if a tile is a frame
	 *  @param intervalKey the key used to get the animation interval (duration each frame is displayed)
	 *  @param defaultInterval the interval used if no value is found for the intervalKey */
	public static void animateLayer(ObjectMap<String, Array<StaticTiledMapTile>> animations, TiledMapTileLayer layer, String animationKey, String intervalKey, float defaultInterval) {
		for(int x = 0; x < layer.getWidth(); x++)
			for(int y = 0; y < layer.getHeight(); y++) {
				Cell cell;
				TiledMapTile tile;
				MapProperties tileProperties;
				if((cell = layer.getCell(x, y)) != null && (tile = cell.getTile()) != null && (tileProperties = tile.getProperties()).containsKey(animationKey)) {
					AnimatedTiledMapTile animatedTile = new AnimatedTiledMapTile(getProperty(tileProperties, intervalKey, defaultInterval), animations.get(tileProperties.get(animationKey, String.class)));
					animatedTile.getProperties().putAll(tile.getProperties());
					cell.setTile(animatedTile);
				}
			}
	}

	/** filters the tiles that are frames
	 *  @param tiles all tiles
	 *  @param animationKey the key used to tell if a tile is a frame
	 *  @return an {@link ObjectMap} which values are tiles that are frames and which keys are their animation names */
	public static ObjectMap<String, Array<StaticTiledMapTile>> filterFrames(TiledMapTile[] tiles, String animationKey) {
		ObjectMap<String, Array<StaticTiledMapTile>> animations = new ObjectMap<>();

		MapProperties tileProperties;
		String animationName;

		for(TiledMapTile tile : tiles) {
			if(!(tile instanceof StaticTiledMapTile))
				continue;

			tileProperties = tile.getProperties();

			if(tileProperties.containsKey(animationKey)) {
				animationName = tileProperties.get(animationKey, String.class);
				if(!animations.containsKey(animationName))
					animations.put(animationName, new Array<StaticTiledMapTile>(3));
				animations.get(animationName).add((StaticTiledMapTile) tile);
			}
		}

		return animations;
	}

	/** sorts the frames of the animation by their orderedKey value if it exists
	 *  @param animations the animations to sort
	 *  @param orderedKey the key of the frame property
	 *  @return the animations with sorted frames
	 *  @see #sortFrames(Array, String) */
	public static ObjectMap<String, Array<StaticTiledMapTile>> sortFrames(ObjectMap<String, Array<StaticTiledMapTile>> animations, String orderedKey) {
		Entry<String, Array<StaticTiledMapTile>> entry;
		Entries<String, Array<StaticTiledMapTile>> entries = animations.entries();
		while(entries.hasNext) {
			entry = entries.next();
			for(StaticTiledMapTile entryTile : entry.value)
				if(entryTile.getProperties().containsKey(orderedKey)) {
					sortFrames(entry.value, orderedKey);
					break;
				}
		}
		return animations;
	}

	/** sorts the frames by their orderedKey value
	 *  @param frames the frames to sort
	 *  @param orderedKey the key of the ordered/frame property */
	public static void sortFrames(Array<StaticTiledMapTile> frames, final String orderedKey) {
		frames.sort(new Comparator<StaticTiledMapTile>() {

			@Override
			public int compare(StaticTiledMapTile tile1, StaticTiledMapTile tile2) {
				int tile1Frame = getProperty(tile1.getProperties(), orderedKey, -1), tile2Frame = getProperty(tile2.getProperties(), orderedKey, -1);
				return tile1Frame < tile2Frame ? -1 : tile1Frame > tile2Frame ? 1 : 0;
			}

		});
	}

}
