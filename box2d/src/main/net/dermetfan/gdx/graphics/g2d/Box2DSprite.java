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

package net.dermetfan.gdx.graphics.g2d;

import java.util.Comparator;
import java.util.Iterator;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pools;
import net.dermetfan.utils.Function;

import static net.dermetfan.gdx.physics.box2d.Box2DUtils.height;
import static net.dermetfan.gdx.physics.box2d.Box2DUtils.minX;
import static net.dermetfan.gdx.physics.box2d.Box2DUtils.minY;
import static net.dermetfan.gdx.physics.box2d.Box2DUtils.position;
import static net.dermetfan.gdx.physics.box2d.Box2DUtils.width;

/** A {@link Box2DSprite} is a {@link Sprite} with additional drawing information and the ability to draw itself on a given {@link Body} or {@link Fixture}.
 *  It is supposed to be put in the user data of {@link Fixture Fixtures} or {@link Body Bodies}. Because geometrical information about bodies cannot be cached, it is faster to put Box2DPolygonSprites in the user data of Fixtures.
 *  @author dermetfan */
public class Box2DSprite extends Sprite {

	/** the z index for sorted drawing */
	private float zIndex;

	/** if the width and height should be adjusted to those of the {@link Body} or {@link Fixture} this {@link Box2DSprite} is attached to (true by default) */
	private boolean adjustWidth = true, adjustHeight = true;

	/** if the origin of this {@link Box2DSprite} should be used when it's drawn (false by default) */
	private boolean useOriginX, useOriginY;

	/** for internal, temporary usage */
	private static final Vector2 vec2 = new Vector2();

	/** @see Sprite#Sprite() */
	public Box2DSprite() {
		super();
	}

	/** @see Sprite#Sprite(Texture, int, int) */
	public Box2DSprite(Texture texture, int srcWidth, int srcHeight) {
		super(texture, srcWidth, srcHeight);
	}

	/** @see Sprite#Sprite(Texture, int, int, int, int) */
	public Box2DSprite(Texture texture, int srcX, int srcY, int srcWidth, int srcHeight) {
		super(texture, srcX, srcY, srcWidth, srcHeight);
	}

	/** @see Sprite#Sprite(TextureRegion, int, int, int, int) */
	public Box2DSprite(TextureRegion region, int srcX, int srcY, int srcWidth, int srcHeight) {
		super(region, srcX, srcY, srcWidth, srcHeight);
	}

	/** @see Sprite#Sprite(Texture) */
	public Box2DSprite(Texture texture) {
		super(texture);
	}

	/** @see Sprite#Sprite(TextureRegion) */
	public Box2DSprite(TextureRegion region) {
		super(region);
	}

	/** @see Sprite#Sprite(Sprite) */
	public Box2DSprite(Sprite sprite) {
		super(sprite);
	}

	/** the {@link #userDataAccessor} used by default */
	public static final Function<Box2DSprite, Object> defaultUserDataAccessor = new Function<Box2DSprite, Object>() {
		@Override
		public Box2DSprite apply(Object userData) {
			return userData instanceof Box2DSprite ? (Box2DSprite) userData : null;
		}
	};

	/** the {@link Function} used to get a {@link Box2DSprite} from the user data of a body or fixture */
	private static Function<Box2DSprite, Object> userDataAccessor = defaultUserDataAccessor;

	/** a {@link Comparator} used to sort {@link Box2DSprite Box2DSprites} by their {@link Box2DSprite#zIndex z index} in {@link #draw(Batch, World)} */
	private static Comparator<Box2DSprite> zComparator = new Comparator<Box2DSprite>() {
		@Override
		public int compare(Box2DSprite s1, Box2DSprite s2) {
			return s1.zIndex - s2.zIndex > 0 ? 1 : s1.zIndex - s2.zIndex < 0 ? -1 : 0;
		}
	};

	/** @see #draw(Batch, World, boolean) */
	public static void draw(Batch batch, World world) {
		draw(batch, world, false);
	}

	/** draws all the {@link Box2DSprite Box2DSprites} on the {@link Body} or {@link Fixture} that hold them in their user data in the given {@link World} */
	public static void draw(Batch batch, World world, boolean sortByZ) {
		@SuppressWarnings("unchecked")
		Array<Body> tmpBodies = Pools.obtain(Array.class);
		world.getBodies(tmpBodies);

		if(sortByZ) {
			@SuppressWarnings("unchecked")
			ObjectMap<Box2DSprite, Object> tmpZMap = Pools.obtain(ObjectMap.class);
			tmpZMap.clear();
			for(Body body : tmpBodies) {
				Box2DSprite tmpBox2DSprite;
				if((tmpBox2DSprite = userDataAccessor.apply(body.getUserData())) != null)
					tmpZMap.put(tmpBox2DSprite, body);
				for(Fixture fixture : body.getFixtureList())
					if((tmpBox2DSprite = userDataAccessor.apply(fixture.getUserData())) != null)
						tmpZMap.put(tmpBox2DSprite, fixture);
			}

			@SuppressWarnings("unchecked")
			Array<Box2DSprite> tmpKeys = Pools.obtain(Array.class);
			Iterator<Box2DSprite> keys = tmpZMap.keys();
			while(keys.hasNext())
				tmpKeys.add(keys.next());
			tmpKeys.sort(zComparator);
			for(Box2DSprite key : tmpKeys) {
				Object value = tmpZMap.get(key);
				if(value instanceof Body)
					key.draw(batch, (Body) value);
				else
					key.draw(batch, (Fixture) value);
			}

			tmpKeys.clear();
			tmpZMap.clear();
			Pools.free(tmpKeys);
			Pools.free(tmpZMap);
		} else
			for(Body body : tmpBodies) {
				Box2DSprite tmpBox2DSprite;
				if((tmpBox2DSprite = userDataAccessor.apply(body.getUserData())) != null)
					tmpBox2DSprite.draw(batch, body);
				for(Fixture fixture : body.getFixtureList())
					if((tmpBox2DSprite = userDataAccessor.apply(fixture.getUserData())) != null)
						tmpBox2DSprite.draw(batch, fixture);
			}

		tmpBodies.clear();
		Pools.free(tmpBodies);
	}

	/** draws this {@link Box2DSprite} on the given {@link Fixture} */
	public void draw(Batch batch, Fixture fixture) {
		vec2.set(position(fixture));
		draw(batch, vec2.x, vec2.y, width(fixture), height(fixture), fixture.getBody().getAngle());
	}

	/** draws this {@link Box2DSprite} on the given {@link Body} */
	public void draw(Batch batch, Body body) {
		float width = width(body), height = height(body);
		vec2.set(minX(body) + width / 2, minY(body) + height / 2);
		vec2.set(body.getWorldPoint(vec2));
		draw(batch, vec2.x, vec2.y, width, height, body.getAngle());
	}

	/** Used internally. Draws this {@code Box2DSprite} in classic sprite coordinate system fashion with the given Box2D coordinates (combined with its own position, size and rotation).<br>
	 *  If {@link #useOriginX useOriginX/Y} is enabled, the {@link #originX origin} will be used instead of calculating an appropriate one for the given Box2D coordinates.<br>
	 *  If {@link #adjustWidth adjustWidth/Height} is disabled, the size of the drawing area of the sprite will be {@link #width} * {@link #height} instead of the given size.<br>
	 *  The drawing position of the sprite is always the bottom left of the body or fixture.
	 *  @param box2dX the x coordinate (center) of the body or fixture
	 *  @param box2dY the y coordinate (center) of the body or fixture
	 *  @param box2dWidth the width of the body or fixture
	 *  @param box2dHeight the height of the body or fixture
	 *  @param box2dRotation the rotation of the body or fixture */
	public void draw(Batch batch, float box2dX, float box2dY, float box2dWidth, float box2dHeight, float box2dRotation) {
		batch.setColor(getColor());
		batch.draw(this, box2dX - box2dWidth / 2 + getX(), box2dY - box2dHeight / 2 + getY(), useOriginX ? getOriginX() : box2dWidth / 2, useOriginY ? getOriginY() : box2dHeight / 2, adjustWidth ? box2dWidth : getWidth(), adjustHeight ? box2dHeight : getHeight(), getScaleX(), getScaleY(), box2dRotation * MathUtils.radDeg + getRotation());
	}

	// getters and setters

	/** @return the {@link #zIndex} */
	public float getZIndex() {
		return zIndex;
	}

	/** @param zIndex the {@link #zIndex} to set */
	public void setZIndex(float zIndex) {
		this.zIndex = zIndex;
	}

	/** @return the {@link #adjustWidth} */
	public boolean isAdjustWidth() {
		return adjustWidth;
	}

	/** @param adjustWidth the {@link #adjustWidth} to set */
	public void setAdjustWidth(boolean adjustWidth) {
		this.adjustWidth = adjustWidth;
	}

	/** @return the {@link #adjustHeight} */
	public boolean isAdjustHeight() {
		return adjustHeight;
	}

	/** @param adjustHeight the {@link #adjustHeight} to set */
	public void setAdjustHeight(boolean adjustHeight) {
		this.adjustHeight = adjustHeight;
	}

	/** @param adjustSize the {@link #adjustWidth} and {@link #adjustHeight} to set */
	public void setAdjustSize(boolean adjustSize) {
		adjustWidth = adjustHeight = adjustSize;
	}

	/** @return the {@link #useOriginX} */
	public boolean isUseOriginX() {
		return useOriginX;
	}

	/** @param useOriginX the {@link #useOriginX} to set */
	public void setUseOriginX(boolean useOriginX) {
		this.useOriginX = useOriginX;
	}

	/** @return the {@link #useOriginY} */
	public boolean isUseOriginY() {
		return useOriginY;
	}

	/** @param useOriginY the {@link #useOriginY} to set */
	public void setUseOriginY(boolean useOriginY) {
		this.useOriginY = useOriginY;
	}

	/** @param useOrigin the {@link #useOriginX} and {@link #useOriginY} to set */
	public void setUseOrigin(boolean useOrigin) {
		useOriginX = useOriginY = useOrigin;
	}

	/** @see Sprite#setSize(float, float) */
	public void setWidth(float width) {
		setSize(width, getHeight());
	}

	/** @see Sprite#setSize(float, float) */
	public void setHeight(float height) {
		setSize(getWidth(), height);
	}

	/** @return the {@link #zComparator} */
	public static Comparator<Box2DSprite> getZComparator() {
		return zComparator;
	}

	/** @param zComparator the {@link #zComparator} to set */
	public static void setZComparator(Comparator<Box2DSprite> zComparator) {
		if(zComparator == null)
			throw new IllegalArgumentException("zComparator must not be null");
		Box2DSprite.zComparator = zComparator;
	}

	/** @return the {@link #userDataAccessor} */
	public static Function<Box2DSprite, ?> getUserDataAccessor() {
		return userDataAccessor;
	}

	/** @param userDataAccessor the {@link #userDataAccessor} to set */
	public static void setUserDataAccessor(Function<Box2DSprite, Object> userDataAccessor) {
		Box2DSprite.userDataAccessor = userDataAccessor != null ? userDataAccessor : defaultUserDataAccessor;
	}

}
