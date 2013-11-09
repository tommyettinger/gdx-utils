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

package net.dermetfan.utils.libgdx.graphics;

import static net.dermetfan.utils.libgdx.box2d.Box2DUtils.height;
import static net.dermetfan.utils.libgdx.box2d.Box2DUtils.minX;
import static net.dermetfan.utils.libgdx.box2d.Box2DUtils.minY;
import static net.dermetfan.utils.libgdx.box2d.Box2DUtils.position;
import static net.dermetfan.utils.libgdx.box2d.Box2DUtils.width;
import static net.dermetfan.utils.libgdx.math.GeometryUtils.vec2_0;

import java.util.Comparator;

import net.dermetfan.utils.Accessor;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

/** A {@link Box2DSprite} is a {@link Sprite} with additional drawing information and the abililty to draw itself on a given {@link Body} or {@link Fixture}.
 *  It is supposed to be put in the user data of {@link Fixture Fixtures} or {@link Body Bodies}. The Fixture's user data is recommend though to make use of caching which will increase performance!
 *  @author dermetfan */
public class Box2DSprite extends Sprite {

	/** the z index for sorted drawing */
	private float z;

	/** if the width and height should be adjusted to those of the {@link Body} or {@link Fixture} this {@link Box2DSprite} is attached to (true by default) */
	private boolean adjustWidth = true, adjustHeight = true;

	/** if the origin of this {@link Box2DSprite} should be used when it's drawn (false by default) */
	private boolean useOriginX, useOriginY;

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
	public final static Accessor defaultUserDataAccessor = new Accessor() {

		@SuppressWarnings("unchecked")
		@Override
		public Box2DSprite access(Object userData) {
			return userData instanceof Box2DSprite ? (Box2DSprite) userData : null;
		}

	};

	/** the {@link Accessor} used to get a {@link Box2DSprite} from the user data of a body or fixture */
	private static Accessor userDataAccessor = defaultUserDataAccessor;

	/** a {@link Comparator} used to sort {@link Box2DSprite Box2DSprites} by their {@link Box2DSprite#z z index} in {@link #draw(SpriteBatch, World)} */
	private static Comparator<Box2DSprite> zComparator = new Comparator<Box2DSprite>() {

		@Override
		public int compare(Box2DSprite s1, Box2DSprite s2) {
			return s1.z - s2.z > 0 ? 1 : s1.z - s2.z < 0 ? -1 : 0;
		}

	};

	/** a temporary map to store the bodies / fixtures which user data Box2DSprites are in in {@link #draw(SpriteBatch, World, boolean)}*/
	private static final ObjectMap<Box2DSprite, Object> tmpZMap = new ObjectMap<Box2DSprite, Object>(0);

	/** temporary variable used in {@link #draw(SpriteBatch, World, boolean)} */
	private static final Array<Body> tmpBodies = new Array<Body>(0);

	/** temporary variable used in {@link #draw(SpriteBatch, World, boolean)} */
	private static Box2DSprite tmpBox2DSprite;

	/** @see #draw(SpriteBatch, World, boolean) */
	public static void draw(SpriteBatch batch, World world) {
		draw(batch, world, false);
	}

	/** draws all the {@link Box2DSprite Box2DSprites} on the {@link Body} or {@link Fixture} that hold them in their user data in the given {@link World} */
	public static void draw(SpriteBatch batch, World world, boolean sortByZ) {
		world.getBodies(tmpBodies);

		if(!sortByZ) {
			for(Body body : tmpBodies) {
				if((tmpBox2DSprite = userDataAccessor.access(body.getUserData())) != null)
					tmpBox2DSprite.draw(batch, body);
				for(Fixture fixture : body.getFixtureList())
					if((tmpBox2DSprite = userDataAccessor.access(fixture.getUserData())) != null)
						tmpBox2DSprite.draw(batch, fixture);
			}
			return;
		}

		for(Body body : tmpBodies) {
			if((tmpBox2DSprite = userDataAccessor.access(body.getUserData())) != null)
				tmpZMap.put(tmpBox2DSprite, body);
			for(Fixture fixture : body.getFixtureList())
				if((tmpBox2DSprite = userDataAccessor.access(fixture.getUserData())) != null)
					tmpZMap.put(tmpBox2DSprite, fixture);
		}

		Array<Box2DSprite> keys = tmpZMap.keys().toArray();
		keys.sort(zComparator);

		Object value;
		for(Box2DSprite key : keys) {
			value = tmpZMap.get(key);
			if(value instanceof Body)
				key.draw(batch, (Body) value);
			else
				key.draw(batch, (Fixture) value);
		}
		tmpZMap.clear();
	}

	/** draws this {@link Box2DSprite} on the given {@link Fixture} */
	public void draw(SpriteBatch batch, Fixture fixture) {
		batch.setColor(getColor());
		float width = width(fixture), height = height(fixture);
		vec2_0.set(position(fixture));
		draw(batch, vec2_0.x, vec2_0.y, width, height, fixture.getBody().getAngle());
	}

	/** draws this {@link Box2DSprite} on the given {@link Body} */
	public void draw(SpriteBatch batch, Body body) {
		batch.setColor(getColor());
		float width = width(body), height = height(body);
		vec2_0.set(minX(body) + width / 2, minY(body) + height / 2);
		vec2_0.set(body.getWorldPoint(vec2_0));
		draw(batch, vec2_0.x, vec2_0.y, width, height, body.getAngle());
	}

	/** draws this {@code Box2DSprite} on the given area */
	public void draw(SpriteBatch batch, float x, float y, float width, float height, float rotation) {
		batch.draw(this, x - width / 2 + getX(), y - height / 2 + getY(), isUseOriginX() ? getOriginX() : width / 2, isUseOriginY() ? getOriginY() : height / 2, isAdjustWidth() ? width : getWidth(), isAdjustHeight() ? height : getHeight(), getScaleX(), getScaleY(), rotation * MathUtils.radiansToDegrees + getRotation());
	}

	/** @return the {@link #z} */
	public float getZ() {
		return z;
	}

	/** @param z the {@link #z} to set */
	public void setZ(float z) {
		this.z = z;
	}

	/** @return the {@link #adjustWidth} */
	public boolean isAdjustWidth() {
		return adjustWidth;
	}

	/** @param adjustWidth the {@link #adjustWidth} to set */
	public void setAdjustWidth(boolean adjustWidth) {
		this.adjustWidth = adjustWidth;
	}

	/** @return the {@link #isAdjustHeight()} */
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
	public static Accessor getUserDataAccessor() {
		return userDataAccessor;
	}

	/** @param userDataAccessor the {@link #userDataAccessor} to set */
	public static void setUserDataAccessor(Accessor userDataAccessor) {
		if(userDataAccessor == null)
			throw new IllegalArgumentException("userDataAccessor must not be null");
		Box2DSprite.userDataAccessor = userDataAccessor;
	}

}
