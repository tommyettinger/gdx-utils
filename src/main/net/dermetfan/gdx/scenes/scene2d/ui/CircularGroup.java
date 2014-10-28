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

package net.dermetfan.gdx.scenes.scene2d.ui;

import java.util.Objects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;
import com.badlogic.gdx.utils.SnapshotArray;

import static net.dermetfan.utils.math.MathUtils.approachZero;

/** a group that aligns its children in a circle
 *  @since 0.5.0
 *  @author dermetfan */
public class CircularGroup extends WidgetGroup {

	/** The max angle of all children (in degrees). Default is 360. */
	private float fullAngle = 360;

	/** The angle added to each child's angle (in degrees). Default is 0. */
	private float angleOffset;

	/** The smallest {@link #angleOffset} allowed. Default is 0. */
	private float minAngleOffset;

	/** The greatest {@link #angleOffset} allowed. Default is {@link #fullAngle}. */
	private float maxAngleOffset = fullAngle;

	/** If an additional, not existent child should be considered in the angle calculation for each child.<br>
	 *  Since {@link #fullAngle} describes the min and max angle for children of this group, two children will overlap at 360 degrees (because 360 degrees mean the min and max angle coincide).
	 *  In this case it would make sense to enable the virtual child. It will reserve the angle needed for one child and therefore overlap with another child at the min/max angle instead of two actual children overlapping.<br>
	 *  Default is true, as appropriate for the default of {@link #fullAngle}. */
	private boolean virtualChildEnabled = true;

	/** allows advanced modification of each child's angle */
	private Modifier modifier;

	/** the DragManager used to make this group rotatable by dragging and to apply velocity */
	private final DragManager dragManager = new DragManager();

	/** the current min size (used internally) */
	private float cachedMinWidth, cachedMinHeight;

	/** the current pref size (used internally) */
	private float cachedPrefWidth, cachedPrefHeight;

	/** if the current size has to be {@link #computeSize() computed} (used internally) */
	private boolean sizeInvalid = true;

	/** for internal use */
	private final Vector2 tmp = new Vector2(), tmp2 = new Vector2();

	/** @see WidgetGroup#WidgetGroup() */
	public CircularGroup() {}

	/** @param draggable see {@link #setDraggable(boolean)} */
	public CircularGroup(boolean draggable) {
		setDraggable(draggable);
	}

	@Override
	public void act(float delta) {
		dragManager.act(delta);
		super.act(delta);
	}

	@Override
	public void drawDebug(ShapeRenderer shapes) {
		super.drawDebug(shapes);
		shapes.setColor(Color.CYAN);
		shapes.ellipse(getX(), getY(), getWidth(), getHeight());
		SnapshotArray<Actor> children = getChildren();
		for(int index = 0; index < children.size; index++) {
			Actor child = children.get(index);
			tmp.set(child.getWidth() / 2, child.getHeight() / 2);
			if(modifier != null)
				tmp.set(modifier.localAnchor(tmp, child, index, children.size, this));
			shapes.line(getX() + getWidth() / 2 * getScaleX(), getY() + getHeight() / 2 * getScaleY(), getX() + (child.getX() + tmp.x) * getScaleX(), getY() + (child.getY() + tmp.y) * getScaleY());
		}
	}

	/** computes {@link #cachedMinWidth}, {@link #cachedMinHeight}, {@link #cachedPrefWidth} and {@link #cachedPrefHeight} */
	protected void computeSize() {
		cachedMinWidth = cachedMinHeight = Float.POSITIVE_INFINITY;
		cachedPrefWidth = cachedPrefHeight = 0;

		float minLocalAnchorX = 0, minLocalAnchorY = 0, prefLocalAnchorX = 0, prefLocalAnchorY = 0, minDistanceFromCenter = 0, prefDistanceFromCenter = 0;
		SnapshotArray<Actor> children = getChildren();
		for(int index = 0; index < children.size; index++) {
			Actor child = children.get(index);

			float minWidth, minHeight, prefWidth, prefHeight;
			if(child instanceof Layout) {
				Layout layout = (Layout) child;
				minWidth = layout.getMinWidth();
				minHeight = layout.getMinHeight();
				prefWidth = layout.getPrefWidth();
				prefHeight = layout.getPrefHeight();
			} else {
				minWidth = prefWidth = child.getWidth();
				minHeight = prefHeight = child.getHeight();
			}

			if(minWidth < cachedMinWidth)
				cachedMinWidth = minWidth;
			if(minHeight < cachedMinHeight)
				cachedMinHeight = minHeight;

			if(prefWidth > cachedPrefWidth)
				cachedPrefWidth = prefWidth;
			if(prefHeight > cachedPrefHeight)
				cachedPrefHeight = prefHeight;

			tmp.set(minWidth / 2, minHeight / 2);
			if(modifier != null)
				modifier.localAnchor(tmp, child, index, children.size, this);
			if(Math.abs(tmp.x) > Math.abs(minLocalAnchorX))
				minLocalAnchorX = tmp.x;
			if(Math.abs(tmp.y) > Math.abs(minLocalAnchorY))
				minLocalAnchorY = tmp.y;

			tmp.set(prefWidth / 2, prefHeight / 2);
			if(modifier != null)
				modifier.localAnchor(tmp, child, index, children.size, this);
			if(Math.abs(tmp.x) > Math.abs(prefLocalAnchorX))
				prefLocalAnchorX = tmp.x;
			if(Math.abs(tmp.y) > Math.abs(prefLocalAnchorY))
				prefLocalAnchorY = tmp.y;

			float childMinDistanceFromCenter = Math.abs(modifier != null ? modifier.distanceFromCenter(minWidth / 2, child, index, children.size, this) : minWidth / 2);
			if(childMinDistanceFromCenter > minDistanceFromCenter)
				minDistanceFromCenter = childMinDistanceFromCenter;

			float childPrefDistanceFromCenter = Math.abs(modifier != null ? modifier.distanceFromCenter(prefWidth / 2, child, index, children.size, this) : prefWidth / 2);
			if(childPrefDistanceFromCenter > prefDistanceFromCenter)
				prefDistanceFromCenter = childPrefDistanceFromCenter;
		}

		cachedMinWidth += minLocalAnchorX - cachedMinWidth;
		cachedMinHeight += minLocalAnchorY;
		cachedPrefWidth += prefLocalAnchorX - cachedPrefWidth;
		cachedPrefHeight += prefLocalAnchorY;
		cachedMinWidth = Math.abs(cachedMinWidth);
		cachedMinHeight = Math.abs(cachedMinHeight);
		cachedPrefWidth = Math.abs(cachedPrefWidth);
		cachedPrefHeight = Math.abs(cachedPrefHeight);

		cachedMinWidth += minDistanceFromCenter;
		cachedPrefWidth += prefDistanceFromCenter;

		cachedMinWidth *= 2;
		cachedMinHeight *= 2;
		cachedPrefWidth *= 2;
		cachedPrefHeight *= 2;

		// ensure circle
		cachedMinWidth = cachedMinHeight = Math.max(cachedMinWidth, cachedMinHeight);
		cachedPrefWidth = cachedPrefHeight = Math.max(cachedPrefWidth, cachedPrefHeight);

		sizeInvalid = false;
	}

	/** does not take rotation into account */
	@Override
	public float getMinWidth() {
		if(sizeInvalid)
			computeSize();
		return cachedMinWidth;
	}

	/** does not take rotation into account */
	@Override
	public float getMinHeight() {
		if(sizeInvalid)
			computeSize();
		return cachedMinHeight;
	}

	@Override
	public float getPrefWidth() {
		if(sizeInvalid)
			computeSize();
		return cachedPrefWidth;
	}

	@Override
	public float getPrefHeight() {
		if(sizeInvalid)
			computeSize();
		return cachedPrefHeight;
	}

	@Override
	public void invalidate() {
		super.invalidate();
		sizeInvalid = true;
	}

	@Override
	public void layout() {
		SnapshotArray<Actor> children = getChildren();
		for(int index = 0; index < children.size; index++) {
			Actor child = children.get(index);
			float angle = fullAngle / (children.size - (virtualChildEnabled ? 0 : 1)) * index;
			if(modifier != null)
				angle = modifier.angle(angle, child, index, children.size, this);
			angle += angleOffset;
			child.setRotation(modifier != null ? modifier.rotation(angle, child, index, children.size, this) : angle);
			float width, height;
			if(child instanceof Layout) {
				Layout childLayout = (Layout) child;
				width = childLayout.getPrefWidth();
				width = Math.max(width, childLayout.getMinWidth());
				if(childLayout.getMaxWidth() != 0)
					width = Math.min(width, childLayout.getMaxWidth());
				height = childLayout.getPrefHeight();
				height = Math.max(height, childLayout.getMinHeight());
				if(childLayout.getMaxHeight() != 0)
					height = Math.min(height, childLayout.getMaxHeight());
				child.setSize(width, height);
				childLayout.validate();
			} else {
				width = child.getWidth();
				height = child.getHeight();
			}
			float distanceFromCenter = modifier == null ? width / 2 : modifier.distanceFromCenter(width / 2, child, index, children.size, this);
			tmp.set(width / 2, height / 2);
			if(modifier != null)
				tmp.set(modifier.localAnchor(tmp, child, index, children.size, this));
			child.setOrigin(tmp.x, tmp.y);
			tmp2.set(-distanceFromCenter, 0).rotate(angle);
			child.setPosition(getWidth() / 2 + tmp2.x - tmp.x, getHeight() / 2 + tmp2.y - tmp.y);
		}
	}

	/** @return if this group is rotatable by dragging with the pointer */
	public boolean isDraggable() {
		return dragManager.isDraggingActivated();
	}

	/** @param draggable if this group should be rotatable by dragging with the pointer */
	public void setDraggable(boolean draggable) {
		dragManager.setDraggingActivated(draggable);
		// add/remove dragManager for performance
		if(draggable)
			addListener(dragManager);
		else
			removeListener(dragManager);
	}

	/** @param amount the amount by which to translate {@link #minAngleOffset} and {@link #maxAngleOffset} */
	public void translateAngleOffsetLimits(float amount) {
		setMinAngleOffset(minAngleOffset + amount);
		setMaxAngleOffset(maxAngleOffset + amount);
	}

	// getters and setters

	/** @return the {@link #fullAngle} */
	public float getFullAngle() {
		return fullAngle;
	}

	/** {@link #setFullAngle(float, boolean)} with automatic estimation if a {@link #virtualChildEnabled} would make sense.
	 *  @param fullAngle the {@link #fullAngle} to set
	 *  @see #setFullAngle(float, boolean) */
	public void setFullAngle(float fullAngle) {
		setFullAngle(fullAngle, fullAngle >= 360);
	}

	/** @param fullAngle the {@link #fullAngle} to set
	 *  @param virtualChildEnabled the {@link #virtualChildEnabled} to set */
	public void setFullAngle(float fullAngle, boolean virtualChildEnabled) {
		this.fullAngle = fullAngle;
		this.virtualChildEnabled = virtualChildEnabled;
		invalidate();
	}

	/** @return the {@link #angleOffset} */
	public float getAngleOffset() {
		return angleOffset;
	}

	/** @param angleOffset The {@link #angleOffset} to set. Will be clamped to {@link #minAngleOffset} and {@link #maxAngleOffset}. */
	public void setAngleOffset(float angleOffset) {
		this.angleOffset = MathUtils.clamp(angleOffset, minAngleOffset, maxAngleOffset);
		invalidate();
	}

	/** @return the {@link #minAngleOffset} */
	public float getMinAngleOffset() {
		return minAngleOffset;
	}

	/** clamps {@link #angleOffset} to the new bounds
	 *  @param minAngleOffset the {@link #minAngleOffset} to set */
	public void setMinAngleOffset(float minAngleOffset) {
		if(minAngleOffset > maxAngleOffset)
			throw new IllegalArgumentException("minAngleOffset must not be > maxAngleOffset");
		this.minAngleOffset = minAngleOffset;
		angleOffset = Math.max(minAngleOffset, angleOffset);
	}

	/** @return the {@link #maxAngleOffset} */
	public float getMaxAngleOffset() {
		return maxAngleOffset;
	}

	/** clamps {@link #angleOffset} to the new bounds
	 *  @param maxAngleOffset the {@link #maxAngleOffset} to set */
	public void setMaxAngleOffset(float maxAngleOffset) {
		if(maxAngleOffset < minAngleOffset)
			throw new IllegalArgumentException("maxAngleOffset must not be < minAngleOffset");
		this.maxAngleOffset = maxAngleOffset;
		angleOffset = Math.min(angleOffset, maxAngleOffset);
	}

	/** @return the {@link #virtualChildEnabled} */
	public boolean isVirtualChildEnabled() {
		return virtualChildEnabled;
	}

	/** @param virtualChildEnabled the {@link #virtualChildEnabled} to set */
	public void setVirtualChildEnabled(boolean virtualChildEnabled) {
		this.virtualChildEnabled = virtualChildEnabled;
	}

	/** @return the {@link #modifier} */
	public Modifier getModifier() {
		return modifier;
	}

	/** @param modifier the {@link #modifier} to set */
	public void setModifier(Modifier modifier) {
		this.modifier = Objects.requireNonNull(modifier, "the modifier must not be null");
		invalidateHierarchy();
	}

	/** @return the {@link #dragManager} */
	public DragManager getDragManager() {
		return dragManager;
	}

	/** @since 0.5.0
	 *  @author dermetfan
	 *  @see #modifier */
	public static interface Modifier {

		/** @param defaultValue the linearly calculated angle
		 *  @param child the child
		 *  @param index the index of the child which angle to calculate
		 *  @param numChildren the number of children
		 *  @param group the CircularGroup the child in question belongs to
		 *  @return the angle of the give child ({@link #angleOffset} will be added to this) */
		float angle(float defaultValue, Actor child, int index, int numChildren, CircularGroup group);

		/** @param defaultValue the angle of the child (may be influenced by {@link #angle(float, Actor, int, int, CircularGroup)} which by default also is its rotation
		 *  @param child the child
		 *  @param index the index of the child which rotation to calculate
		 *  @param numChildren the number of children
		 *  @param group the CircularGroup the child in question belongs to
		 *  @return the rotation of the give child */
		float rotation(float defaultValue, Actor child, int index, int numChildren, CircularGroup group);

		/** @param defaultValue the default distance from center
		 *  @param child the child
		 *  @param index the index of the child which distance from center to calculate
		 *  @param numChildren the number of children
		 *  @param group the CircularGroup the child in question belongs to
		 *  @return the distance from the group center of the given child */
		float distanceFromCenter(float defaultValue, Actor child, int index, int numChildren, CircularGroup group);

		/** @param defaultValue the default local anchor (can be modified and returned)
		 *  @param child the child
		 *  @param index the index of the child which local anchor to calculate
		 *  @param numChildren the number of children
		 *  @param group the CircularGroup the child in question belongs to
		 *  @return the local anchor of the given child */
		Vector2 localAnchor(Vector2 defaultValue, Actor child, int index, int numChildren, CircularGroup group);

		/** Use this if you only want to override some of {@link Modifier}'s methods.
		 *  All implementations return the default value.
		 *  @since 0.5.0
		 *  @author dermetfan */
		public static class Adapter implements Modifier {

			/** @return the given angle */
			@Override
			public float angle(float defaultValue, Actor child, int index, int numChildren, CircularGroup group) {
				return defaultValue;
			}

			/** @return the given rotation */
			@Override
			public float rotation(float defaultValue, Actor child, int index, int numChildren, CircularGroup group) {
				return defaultValue;
			}

			/** @return the given distance from center */
			@Override
			public float distanceFromCenter(float defaultValue, Actor child, int index, int numChildren, CircularGroup group) {
				return defaultValue;
			}

			/** @return the given local anchor */
			@Override
			public Vector2 localAnchor(Vector2 defaultValue, Actor child, int index, int numChildren, CircularGroup group) {
				return defaultValue;
			}

		}

	}

	/** manages dragging and velocity of its enclosing CircularGroup instance
	 *  @since 0.5.0
	 *  @author dermetfan */
	public class DragManager extends DragListener {

		/** if the velocity should be applied */
		private boolean velocityActivated = true;

		/** if dragging should be possible */
		private boolean draggingActivated = true;

		/** the velocity of the rotation */
		private float velocity;

		/** the deceleration applied to {@link #velocity} */
		private float deceleration = 500;

		/** if this group is currently being dragged (internal use) */
		private boolean dragging;

		/** the previous angle for delta calculation (internal use) */
		private float previousAngle;

		/** The greatest absolute delta value allowed. Needed to avoid glitches. */
		private float maxAbsDelta = 350;

		/** inner class singleton */
		private DragManager() {}

		@Override
		public void dragStart(InputEvent event, float x, float y, int pointer) {
			if(!draggingActivated)
				return;
			velocity = 0;
			dragging = true;
			previousAngle = angle(x, y);
		}

		@Override
		public void drag(InputEvent event, float x, float y, int pointer) {
			if(!draggingActivated)
				return;
			float currentAngle = angle(x, y);
			float delta = currentAngle - previousAngle;
			previousAngle = currentAngle;
			if(Math.abs(delta) > maxAbsDelta)
				return;
			velocity = delta * Gdx.graphics.getFramesPerSecond();
			float newAngleOffset = angleOffset + delta;
			float oldAngleOffset = angleOffset;
			setAngleOffset(newAngleOffset);
			if(angleOffset != oldAngleOffset)
				invalidate();
		}

		@Override
		public void dragStop(InputEvent event, float x, float y, int pointer) {
			if(!draggingActivated)
				return;
			dragging = false;
		}

		/** changes {@link #angleOffset} according to {@link #velocity} and reduces {@link #velocity} according to {@link #deceleration}
		 *  @param delta see {@link com.badlogic.gdx.Graphics#getDeltaTime()} */
		public void act(float delta) {
			if(dragging || velocity == 0 || !velocityActivated)
				return;
			setAngleOffset(angleOffset + velocity * delta);
			invalidate();
			if(deceleration == 0)
				return;
			velocity = approachZero(velocity, deceleration * delta);
		}

		/** @return the angle of the given x and y to the center of the group */
		private float angle(float x, float y) {
			return tmp.set(x, y).sub(getWidth() / 2, getHeight() / 2).angle();
		}

		/** @param angleOffset the {@link #angleOffset} to set so that if {@link #minAngleOffset} and {@link #maxAngleOffset} coincide on 360 degrees it doesn't get clamped */
		private void setAngleOffset(float angleOffset) {
			if(maxAngleOffset - minAngleOffset == 360)
				CircularGroup.this.angleOffset = net.dermetfan.utils.math.MathUtils.normalize(angleOffset, minAngleOffset, maxAngleOffset);
			else
				CircularGroup.this.setAngleOffset(angleOffset);
		}

		// getters and setters

		/** @return the {@link #velocityActivated} */
		public boolean isVelocityActivated() {
			return velocityActivated;
		}

		/** @param velocityActivated the {@link #velocityActivated} to set */
		public void setVelocityActivated(boolean velocityActivated) {
			this.velocityActivated = velocityActivated;
		}

		/** @return the {@link #draggingActivated} */
		public boolean isDraggingActivated() {
			return draggingActivated;
		}

		/** @param draggingActivated the {@link #draggingActivated} to set */
		public void setDraggingActivated(boolean draggingActivated) {
			this.draggingActivated = draggingActivated;
		}

		/** @return the {@link #velocity} */
		public float getVelocity() {
			return velocity;
		}

		/** @param velocity the {@link #velocity} to set */
		public void setVelocity(float velocity) {
			this.velocity = velocity;
		}

		/** @return the {@link #deceleration} */
		public float getDeceleration() {
			return deceleration;
		}

		/** @param deceleration the {@link #deceleration} to set */
		public void setDeceleration(float deceleration) {
			this.deceleration = deceleration;
		}

		/** @return the {@link #maxAbsDelta} */
		public float getMaxAbsDelta() {
			return maxAbsDelta;
		}

		/** @param maxAbsDelta the {@link #maxAbsDelta} to set */
		public void setMaxAbsDelta(float maxAbsDelta) {
			this.maxAbsDelta = maxAbsDelta;
		}

	}

}
