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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;
import com.badlogic.gdx.utils.SnapshotArray;
import net.dermetfan.gdx.math.GeometryUtils;

import static net.dermetfan.utils.math.MathUtils.approachZero;
import static net.dermetfan.utils.math.MathUtils.replaceNaN;

/** a group that aligns its children in a circle
 *  @since 0.5.0
 *  @author dermetfan */
public class CircularGroup extends WidgetGroup {

	/** @since 0.5.0
	 *  @author dermetfan
	 *  @see #modifier */
	public static interface Modifier {

		/** @param angle the linearly calculated angle
		 *  @param index the index of the child which angle to calculate
		 *  @param numChildren the number of children
		 *  @param group the CircularGroup the child in question belongs to
		 *  @return the angle of the child at the given index ({@link #angleOffset} will be added to this) */
		float angle(float angle, int index, int numChildren, CircularGroup group);

		/** @param rotation the angle of the child (may be influenced by {@link #angle(float, int, int, CircularGroup)} which by default also is its rotation
		 *  @param index the index of the child which rotation to calculate
		 *  @param numChildren the number of children
		 *  @param group the CircularGroup the child in question belongs to
		 *  @return the rotation of the child at the given index */
		float rotation(float rotation, int index, int numChildren, CircularGroup group);

		/** Use this if you only want to override some of {@link Modifier}'s methods.
		 *  All implementations return the default value.
		 *  @since 0.5.0
		 *  @author dermetfan */
		public static abstract class Adapter implements Modifier {

			/** @return the given angle */
			@Override
			public float angle(float angle, int index, int numChildren, CircularGroup group) {
				return angle;
			}

			/** @return the given rotation */
			@Override
			public float rotation(float rotation, int index, int numChildren, CircularGroup group) {
				return rotation;
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

	/** The size of each child to base the {@link #getPrefWidth() preferred size} on. Default is {@link Value#prefWidth}/{@link Value#prefHeight}. */
	private Value prefWidth = Value.prefWidth, prefHeight = Value.prefHeight;

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
	private boolean virtualChild = true;

	/** allows advanced modification of each child's angle */
	private Modifier modifier;

	/** Even if distanceFromCenter is greater, no child's unrotated bound will escape the group size. NaN values mean no fixed distance from the group center. Default is {@link Float#NaN}. */
	private float distanceFromCenter = Float.NaN;

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
	public void layout() {
		SnapshotArray<Actor> children = getChildren();
		for(int index = 0; index < children.size; index++) {
			Actor child = children.get(index);
			int numChildren = children.size - (virtualChild ? 0 : 1);
			float angle = fullAngle / numChildren * index;
			if(modifier != null)
				angle = modifier.angle(angle, index, numChildren, this);
			angle += angleOffset;
			child.setRotation(modifier != null ? modifier.rotation(angle, index, numChildren, this) : angle);
			float groupWidth = getWidth(), groupHeight = getHeight();
			float width, height;
			if(child instanceof Layout) {
				Layout childLayout = (Layout) child;
				width = Math.min(childLayout.getPrefWidth(), groupWidth / 2 - replaceNaN(MathUtils.clamp(distanceFromCenter, 0, groupWidth / 2), 0));
				width = Math.max(width, childLayout.getMinWidth());
				if(childLayout.getMaxWidth() != 0)
					width = Math.min(width, childLayout.getMaxWidth());
				height = Math.min(childLayout.getPrefHeight(), groupHeight / 2 - replaceNaN(MathUtils.clamp(distanceFromCenter, 0, groupHeight / 2), 0));
				height = Math.max(height, childLayout.getMinHeight());
				if(childLayout.getMaxHeight() != 0)
					height = Math.min(height, childLayout.getMaxHeight());
				child.setSize(width, height);
				childLayout.validate();
			} else {
				width = child.getWidth();
				height = child.getHeight();
			}
			child.setOrigin(width / 2, height / 2);
			float realDistanceFromCenter = Float.isNaN(distanceFromCenter) ? groupWidth / 2 - width : Math.min(distanceFromCenter, groupWidth / 2 - width);
			GeometryUtils.rotate(tmp.set(groupWidth / 2 - width / 2 - realDistanceFromCenter, groupHeight / 2), tmp2.set(groupWidth / 2, groupHeight / 2), angle * MathUtils.degRad);
			child.setPosition(tmp.x - child.getWidth() / 2, tmp.y - child.getHeight() / 2);
		}
	}

	@Override
	public void invalidate() {
		super.invalidate();
		sizeInvalid = true;
	}

	@Override
	public void drawDebug(ShapeRenderer shapes) {
		shapes.setColor(Color.CYAN);
		for(Actor child : getChildren())
			shapes.line(getX() + getWidth() / 2 * getScaleX(), getY() + getHeight() / 2 * getScaleY(), getX() + (child.getX() + child.getWidth() / 2) * getScaleX(), getY() + (child.getY() + child.getHeight() / 2) * getScaleY());
		super.drawDebug(shapes);
	}

	/** computes {@link #cachedMinWidth}, {@link #cachedMinHeight}, {@link #cachedPrefWidth} and {@link #cachedPrefHeight} */
	protected void computeSize() {
		cachedMinWidth = cachedMinHeight = Float.POSITIVE_INFINITY;
		cachedPrefWidth = cachedPrefHeight = 0;
		SnapshotArray<Actor> children = getChildren();
		for(Actor child : children) {
			float minWidth, minHeight;
			if(child instanceof Layout) {
				Layout layout = (Layout) child;
				minWidth = layout.getMinWidth();
				minHeight = layout.getMinHeight();
			} else {
				minWidth = child.getWidth();
				minHeight = child.getHeight();
			}
			if(minWidth < cachedMinWidth)
				cachedMinWidth = minWidth;
			if(minHeight < cachedMinHeight)
				cachedMinHeight = minHeight;

			float prefWidth = this.prefWidth.get(child), prefHeight = this.prefHeight.get(child);
			if(prefWidth > cachedPrefWidth)
				cachedPrefWidth = prefWidth;
			if(prefHeight > cachedPrefHeight)
				cachedPrefHeight = prefHeight;
		}
		float realDistanceFromCenter2 = replaceNaN(distanceFromCenter, 0) * 2;
		cachedMinWidth += cachedMinWidth + realDistanceFromCenter2;
		cachedMinHeight += cachedMinHeight + realDistanceFromCenter2;
		cachedPrefWidth += cachedPrefWidth + realDistanceFromCenter2;
		cachedPrefHeight += cachedPrefHeight + realDistanceFromCenter2;
		sizeInvalid = false;
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

	/** @param draggable if this group should be rotatable by dragging with the pointer */
	public void setDraggable(boolean draggable) {
		dragManager.setDraggingActivated(draggable);
		// add/remove dragManager for performance
		if(draggable)
			addListener(dragManager);
		else
			removeListener(dragManager);
	}

	/** @return if this group is rotatable by dragging with the pointer */
	public boolean isDraggable() {
		return dragManager.isDraggingActivated();
	}

	// getters and setters

	/** {@link #setFullAngle(float, boolean)} with automatic estimation if a {@link #virtualChild} would make sense.
	 *  @param fullAngle the {@link #fullAngle} to set
	 *  @see #setFullAngle(float, boolean) */
	public void setFullAngle(float fullAngle) {
		setFullAngle(fullAngle, fullAngle >= 360);
	}

	/** @param fullAngle the {@link #fullAngle} to set
	 *  @param virtualChild the {@link #virtualChild} to set */
	public void setFullAngle(float fullAngle, boolean virtualChild) {
		this.fullAngle = fullAngle;
		this.virtualChild = virtualChild;
		invalidate();
	}

	/** @return the {@link #fullAngle} */
	public float getFullAngle() {
		return fullAngle;
	}

	/** @param angleOffset The {@link #angleOffset} to set. Will be clamped to {@link #minAngleOffset} and {@link #maxAngleOffset}. */
	public void setAngleOffset(float angleOffset) {
		this.angleOffset = MathUtils.clamp(angleOffset, minAngleOffset, maxAngleOffset);
		invalidate();
	}

	/** @param angleOffset The {@link #angleOffset} to set. Will be clamped to the given min and max.
	 *  @param minAngleOffset the {@link #minAngleOffset} to set
	 *  @param maxAngleOffset the {@link #maxAngleOffset} to set */
	public void setAngleOffset(float angleOffset, float minAngleOffset, float maxAngleOffset) {
		this.angleOffset = angleOffset;
		setMinAngleOffset(minAngleOffset);
		setMaxAngleOffset(maxAngleOffset);
	}

	/** @return the {@link #angleOffset} */
	public float getAngleOffset() {
		return angleOffset;
	}

	/** @param amount the amount by which to translate {@link #minAngleOffset} and {@link #maxAngleOffset} */
	public void translateAngleOffsetLimits(float amount) {
		setMinAngleOffset(minAngleOffset + amount);
		setMaxAngleOffset(maxAngleOffset + amount);
	}

	/** clamps {@link #angleOffset} to the new bounds
	 *  @param minAngleOffset the {@link #minAngleOffset} to set */
	public void setMinAngleOffset(float minAngleOffset) {
		if(minAngleOffset > maxAngleOffset)
			throw new IllegalArgumentException("minAngleOffset must not be > maxAngleOffset");
		this.minAngleOffset = minAngleOffset;
		this.angleOffset = Math.max(minAngleOffset, angleOffset);
	}

	/** @return the {@link #minAngleOffset} */
	public float getMinAngleOffset() {
		return minAngleOffset;
	}

	/** clamps {@link #angleOffset} to the new bounds
	 *  @param maxAngleOffset the {@link #maxAngleOffset} to set */
	public void setMaxAngleOffset(float maxAngleOffset) {
		if(maxAngleOffset < minAngleOffset)
			throw new IllegalArgumentException("maxAngleOffset must not be < minAngleOffset");
		this.maxAngleOffset = maxAngleOffset;
		this.angleOffset = Math.min(angleOffset, maxAngleOffset);
	}

	/** @return the {@link #maxAngleOffset} */
	public float getMaxAngleOffset() {
		return maxAngleOffset;
	}

	/** @return the {@link #virtualChild} */
	public boolean isVirtualChild() {
		return virtualChild;
	}

	/** @param virtualChild the {@link #virtualChild} to set */
	public void setVirtualChild(boolean virtualChild) {
		this.virtualChild = virtualChild;
	}

	/** @param prefWidth the {@link #prefWidth} to set */
	public void setPrefWidth(Value prefWidth) {
		this.prefWidth = prefWidth;
		invalidateHierarchy();
	}

	/** @param prefHeight the {@link #prefHeight} to set */
	public void setPrefHeight(Value prefHeight) {
		this.prefHeight = prefHeight;
		invalidateHierarchy();
	}

	/** @return the {@link #modifier} */
	public Modifier getModifier() {
		return modifier;
	}

	/** @param modifier the {@link #modifier} to set */
	public void setModifier(Modifier modifier) {
		this.modifier = modifier;
	}

	/** @return the {@link #distanceFromCenter} */
	public float getDistanceFromCenter() {
		return distanceFromCenter;
	}

	/** @param distanceFromCenter the {@link #distanceFromCenter} to set */
	public void setDistanceFromCenter(float distanceFromCenter) {
		this.distanceFromCenter = distanceFromCenter;
		invalidateHierarchy();
	}

	/** @return the {@link #dragManager} */
	public DragManager getDragManager() {
		return dragManager;
	}

}
