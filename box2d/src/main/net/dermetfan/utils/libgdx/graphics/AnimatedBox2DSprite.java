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

package net.dermetfan.utils.libgdx.graphics;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Graphics;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;

/** A {@link Box2DSprite} using an {@link AnimatedSprite} for animation.
 *  @author dermetfan */
public class AnimatedBox2DSprite extends Box2DSprite {

	/** the {@link AnimatedSprite} used for animation */
	private AnimatedSprite animatedSprite;

	/** creates a new {@link AnimatedBox2DSprite} with the given {@link AnimatedSprite}
	 *  @param animatedSprite the {@link AnimatedSprite} to use */
	public AnimatedBox2DSprite(AnimatedSprite animatedSprite) {
		super(animatedSprite);
		this.animatedSprite = animatedSprite;
	}

	/** {@link #update() updates} before drawing if {@link #isAutoUpdate()} is true */
	@Override
	public void draw(Batch batch, float box2dX, float box2dY, float box2dWidth, float box2dHeight, float box2dRotation) {
		if(animatedSprite.isAutoUpdate())
			update();
		super.draw(batch, box2dX, box2dY, box2dWidth, box2dHeight, box2dRotation);
	}

	/** updates with {@link Graphics#getDeltaTime()}
	 *  @see #update(float) */
	public void update() {
		update(Gdx.graphics.getDeltaTime());
	}

	/** @param delta the delta time to update with */
	public void update(float delta) {
		animatedSprite.update(delta);
		setRegion(animatedSprite);
		setSize(animatedSprite.getWidth(), animatedSprite.getHeight());
	}

	/** @return the {@link AnimatedSprite} */
	public AnimatedSprite getAnimatedSprite() {
		return animatedSprite;
	}

	/** @param animatedSprite the {@link AnimatedSprite} to set */
	public void setAnimatedSprite(AnimatedSprite animatedSprite) {
		if(animatedSprite == null)
			throw new IllegalArgumentException("animatedSprite must not be null");
		this.animatedSprite = animatedSprite;
	}

	/** @see AnimatedSprite#play() */
	public void play() {
		animatedSprite.play();
	}

	/** @see AnimatedSprite#pause() */
	public void pause() {
		animatedSprite.pause();
	}

	/** @see AnimatedSprite#stop() */
	public void stop() {
		animatedSprite.stop();
	}

	/** @see AnimatedSprite#setTime(float) */
	public void setTime(float time) {
		animatedSprite.setTime(time);
	}

	/** @see AnimatedSprite#getTime() */
	public float getTime() {
		return animatedSprite.getTime();
	}

	/** @see AnimatedSprite#getAnimation() */
	public Animation getAnimation() {
		return animatedSprite.getAnimation();
	}

	/** @see AnimatedSprite#setAnimation(Animation) */
	public void setAnimation(Animation animation) {
		animatedSprite.setAnimation(animation);
	}

	/** @see AnimatedSprite#flipFrames(boolean, boolean) */
	public void flipFrames(boolean flipX, boolean flipY) {
		animatedSprite.flipFrames(flipX, flipY);
	}

	/** @see AnimatedSprite#flipFrames(boolean, boolean, boolean) */
	public void flipFrames(boolean flipX, boolean flipY, boolean invert) {
		animatedSprite.flipFrames(flipX, flipY, invert);
	}

	/** @see AnimatedSprite#flipFrames(float, float, boolean, boolean) */
	public void flipFrames(float startTime, float endTime, boolean flipX, boolean flipY) {
		animatedSprite.flipFrames(startTime, endTime, flipX, flipY);
	}

	/** @see AnimatedSprite#flipFrames(float, float, boolean, boolean, boolean) */
	public void flipFrames(float startTime, float endTime, boolean flipX, boolean flipY, boolean invert) {
		animatedSprite.flipFrames(startTime, endTime, flipX, flipY, invert);
	}

	/** @see AnimatedSprite#isPlaying() */
	public boolean isPlaying() {
		return animatedSprite.isPlaying();
	}

	/** @see AnimatedSprite#setPlaying(boolean) */
	public void setPlaying(boolean playing) {
		animatedSprite.setPlaying(playing);
	}

	/** @see AnimatedSprite#isAutoUpdate() */
	public boolean isAutoUpdate() {
		return animatedSprite.isAutoUpdate();
	}

	/** @see AnimatedSprite#setAutoUpdate(boolean) */
	public void setAutoUpdate(boolean autoUpdate) {
		animatedSprite.setAutoUpdate(autoUpdate);
	}

	/** @see AnimatedSprite#isKeepSize() */
	public boolean isKeepSize() {
		return animatedSprite.isKeepSize();
	}

	/** @see AnimatedSprite#setKeepSize(boolean) */
	public void setKeepSize(boolean keepSize) {
		animatedSprite.setKeepSize(keepSize);
	}

	/** @see AnimatedSprite#isCenterFrames() */
	public boolean isCenterFrames() {
		return animatedSprite.isCenterFrames();
	}

	/** @see AnimatedSprite#setCenterFrames(boolean) */
	public void setCenterFrames(boolean centerFrames) {
		animatedSprite.setCenterFrames(centerFrames);
	}

}
