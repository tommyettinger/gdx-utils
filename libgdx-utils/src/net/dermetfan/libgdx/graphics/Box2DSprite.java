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

package net.dermetfan.libgdx.graphics;

import static net.dermetfan.libgdx.box2d.Box2DUtils.height;
import static net.dermetfan.libgdx.box2d.Box2DUtils.minX;
import static net.dermetfan.libgdx.box2d.Box2DUtils.minY;
import static net.dermetfan.libgdx.box2d.Box2DUtils.position;
import static net.dermetfan.libgdx.box2d.Box2DUtils.width;

import java.util.Comparator;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * A {@link Box2DSprite} is a {@link Sprite} with additional drawing information and the abililty to draw itself on a given {@link Body} or {@link Fixture}.
 * It is supposed to be put in the user data of {@link Body Bodies} or {@link Fixture Fixtures}.
 * 
 * @author dermetfan
 */
public class Box2DSprite extends Sprite {

	/** the z index for sorted drawing */
	private float z;

	/** if the width and height should be adjusted to those of the {@link Body} or {@link Fixture} this {@link Box2DSprite} is attached to (true by default) */
	private boolean adjustWidth = true, adjustHeight = true;

	/** if the origin of this {@link Box2DSprite} should be used when it's drawn (false by default) */
	private boolean useOriginX, useOriginY;

	/** a user data object replacing the user data that this {@link Box2DSprite} replaces if it's set as user data */
	private Object userData;

	/** @see Sprite#Sprite(Texture) */
	public Box2DSprite(Texture texture) {
		super(texture);
	}

	/** @see Sprite#Sprite(Sprite) */
	public Box2DSprite(Sprite sprite) {
		super(sprite);
	}

	/** a temporary map to store the bodies / fixtures which user data Box2DSprites are in in {@link #draw(SpriteBatch, World)} if enableZ is true */
	private static ObjectMap<Box2DSprite, Object> tmpZBox2DSpriteMap = new ObjectMap<Box2DSprite, Object>(0);

	/** a {@link Comparator} used to sort {@link Box2DSprite Box2DSprites} by their {@link Box2DSprite#z z index} in {@link #draw(SpriteBatch, World)} */
	private static Comparator<Box2DSprite> zSorter = new Comparator<Box2DSprite>() {

		@Override
		public int compare(Box2DSprite s1, Box2DSprite s2) {
			return s1.z - s2.z > 0 ? 1 : -1;
		}

	};

	/** temporary variable used in {@link #draw(SpriteBatch, World)} */
	private static Array<Body> tmpBodies = new Array<Body>(0);

	/** @see #draw(SpriteBatch, World, boolean) */
	public static void draw(SpriteBatch batch, World world) {
		draw(batch, world, false);
	}

	/** draws all the {@link Box2DSprite Box2DSprites} on the {@link Body} or {@link Fixture} that hold them in their user data in the given {@link World} */
	public static void draw(SpriteBatch batch, World world, boolean sortByZ) {
		world.getBodies(tmpBodies);

		if(sortByZ) {
			for(Body body : tmpBodies) {
				if(body.getUserData() instanceof Box2DSprite)
					tmpZBox2DSpriteMap.put((Box2DSprite) body.getUserData(), body);
				for(Fixture fixture : body.getFixtureList())
					if(fixture.getUserData() instanceof Box2DSprite)
						tmpZBox2DSpriteMap.put((Box2DSprite) fixture.getUserData(), fixture);
			}

			Array<Box2DSprite> keys = tmpZBox2DSpriteMap.keys().toArray();
			keys.sort(zSorter);

			for(Box2DSprite key : keys) {
				Object value = tmpZBox2DSpriteMap.get(key);
				if(value instanceof Body)
					key.draw(batch, (Body) value);
				else
					key.draw(batch, (Fixture) value);
			}
			tmpZBox2DSpriteMap.clear();
		} else {
			for(Body body : tmpBodies) {
				if(body.getUserData() instanceof Box2DSprite)
					((Box2DSprite) body.getUserData()).draw(batch, body);
				for(Fixture fixture : body.getFixtureList())
					if(fixture.getUserData() instanceof Box2DSprite)
						((Box2DSprite) fixture.getUserData()).draw(batch, fixture);
			}
		}

	}
	
	/** draws this {@link Box2DSprite} on the given {@link Fixture} */
	public void draw(SpriteBatch batch, Fixture fixture) {
		batch.setColor(getColor());
		batch.draw(this, position(fixture).x - width(fixture) / 2 + getX(), position(fixture).y - height(fixture) / 2 + getY(), isUseOriginX() ? getOriginX() : width(fixture) / 2, isUseOriginY() ? getOriginY() : height(fixture) / 2, isAdjustWidth() ? width(fixture) : getWidth(), isAdjustHeight() ? height(fixture) : getHeight(), getScaleX(), getScaleY(), fixture.getBody().getAngle() * MathUtils.radiansToDegrees + getRotation());
	}

	/** draws this {@link Box2DSprite} on the given {@link Body} */
	public void draw(SpriteBatch batch, Body body) {
		batch.setColor(getColor());
		Vector2 center = new Vector2(minX(body) + width(body) / 2, minY(body) + height(body) / 2);
		batch.draw(this, body.getWorldPoint(center).x - width(body) / 2 + getX(), body.getWorldPoint(center).y - height(body) / 2 + getY(), isUseOriginX() ? getOriginX() : width(body) / 2, isUseOriginY() ? getOriginY() : height(body) / 2, isAdjustWidth() ? width(body) : getWidth(), isAdjustHeight() ? height(body) : getHeight(), getScaleX(), getScaleY(), body.getAngle() * MathUtils.radiansToDegrees + getRotation());
	}

	/** @return the {@link #z} */
	public float getZ() {
		return z;
	}

	/** @param z the {@link #z} to set */
	public void setZ(float z) {
		this.z = z;
	}

	/** @return if the width should be adjusted to those of the {@link Fixture} this {@link Box2DSprite} is attached to */
	public boolean isAdjustWidth() {
		return adjustWidth;
	}

	/** @param adjustWidth if the width should be adjusted to that of the {@link Body} or {@link Fixture} this {@link Box2DSprite} is attached to */
	public void setAdjustWidth(boolean adjustWidth) {
		this.adjustWidth = adjustWidth;
	}

	/** @return if the height should be adjusted to that of the {@link Body} or {@link Fixture} this {@link Box2DSprite} is attached to */
	public boolean isAdjustHeight() {
		return adjustHeight;
	}

	/** @param adjustHeight if the height should be adjusted to that of the {@link Body} or {@link Fixture} this {@link Box2DSprite} is attached to */
	public void setAdjustHeight(boolean adjustHeight) {
		this.adjustHeight = adjustHeight;
	}

	/** @return the if the x origin of this {@link Box2DSprite} should be used when it's being drawn */
	public boolean isUseOriginX() {
		return useOriginX;
	}

	/** @param useOriginX if the x origin of this {@link Box2DSprite} should be used when it's being drawn */
	public void setUseOriginX(boolean useOriginX) {
		this.useOriginX = useOriginX;
	}

	/** @return if the y origin of this {@link Box2DSprite} should be used when it's being drawn */
	public boolean isUseOriginY() {
		return useOriginY;
	}

	/** @param useOriginY if the y origin of this {@link Box2DSprite} should be used when it's being drawn */
	public void setUseOriginY(boolean useOriginY) {
		this.useOriginY = useOriginY;
	}

	/** @see Sprite#setSize(float, float) */
	public void setWidth(float width) {
		setSize(width, getHeight());
	}

	/** @see Sprite#setSize(float, float) */
	public void setHeight(float height) {
		setSize(getWidth(), height);
	}

	/** @return the userData */
	public Object getUserData() {
		return userData;
	}

	/** @param userData the userData to set */
	public void setUserData(Object userData) {
		this.userData = userData;
	}

}
