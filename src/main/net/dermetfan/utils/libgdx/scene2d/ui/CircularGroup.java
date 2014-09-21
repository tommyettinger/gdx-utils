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

package net.dermetfan.utils.libgdx.scene2d.ui;

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
import net.dermetfan.utils.libgdx.math.GeometryUtils;

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

		/** @param angle the angle of the child (may be influenced by {@link #angle(float, int, int, CircularGroup)}
		 *  @param index the index of the child which rotation to calculate
		 *  @param numChildren the number of children
		 *  @param group the CircularGroup the child in question belongs to
		 *  @return the rotation of the child at the given index */
		float rotation(float angle, int index, int numChildren, CircularGroup group);

	}

	/** manages dragging and velocity of its enclosing CircularGroup instance
	 *  @since 0.5.0
	 *  @author dermetfan */
	public class DragManager extends DragListener {

		/** if the velocity should be applied */
		private boolean velocityActivated = true;

		/** if dragging should be possible */
		private boolean draggingActivated = true;

		/** How long it takes to decelerate {@link #velocity} to zero.
		 *  Values smaller than 0 indicate no particular duration (instead {@link #deceleration} will be used directly).
		 *  Default is {@value}. */
		private float decelerationDuration = -1;

		// internals

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
			if(decelerationDuration >= 0)
				calculateDeceleration();
		}

		public void act(float delta) {
			if(dragging || velocity == 0 || !velocityActivated)
				return;
			setAngleOffset(angleOffset + velocity * delta);
			invalidate();
			if(deceleration == 0)
				return;
			float oldVelocity = velocity;
			velocity -= (velocity > 0 ? deceleration : -deceleration) * delta;
			if(oldVelocity > 0 && velocity < 0 || oldVelocity < 0 && velocity > 0)
				velocity = 0;
		}

		private float angle(float x, float y) {
			return tmp.set(x, y).sub(getWidth() / 2, getHeight() / 2).angle();
		}

		/** @param angleOffset the {@link #angleOffset} to set so that if {@link #minAngleOffset} and {@link #maxAngleOffset} coincide on 360 degrees it doesn't get clamped */
		private void setAngleOffset(float angleOffset) {
			if(maxAngleOffset - minAngleOffset == 360)
				CircularGroup.this.angleOffset = angleOffset;
			else
				CircularGroup.this.setAngleOffset(angleOffset);
		}

		/** calculates ands sets {@link #deceleration} according to {@link #velocity} and {@link #decelerationDuration} */
		public void calculateDeceleration() {
			deceleration = velocity / decelerationDuration;
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

		/** @return the {@link #decelerationDuration} */
		public float getDecelerationDuration() {
			return decelerationDuration;
		}

		/** note this has no real effect until {@link #calculateDeceleration()} is called
		 *  @param decelerationDuration the {@link #decelerationDuration} to set */
		public void setDecelerationDuration(float decelerationDuration) {
			this.decelerationDuration = decelerationDuration;
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

	/** The preferred size. Default is {@value}, some arbitrary value. */
	float prefWidth = 500, prefHeight = 500;

	/** The max angle of all children (in degrees). Default is {@value}. */
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
	 *  Default is {@value}, as appropriate for the default of {@link #fullAngle}. */
	private boolean virtualChild = true;

	/** allows advanced modification of each child's angle */
	private Modifier modifier;

	/** Even if distanceFromCenter is greater, no child's unrotated bound will escape the group size. {@code distanceFromCenter < 0} means no fixed distance from the group center. Default is {@value}. */
	private float distanceFromCenter = -1;

	/** the DragManager used to make this group rotatable by dragging and to apply velocity */
	private final DragManager dragManager = new DragManager();

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
				angle = modifier.angle(angle, index, children.size, this);
			angle += angleOffset;
			child.setRotation(modifier != null ? modifier.rotation(angle, index, numChildren, this) : angle);
			float width, height;
			if(child instanceof Layout) {
				Layout childLayout = (Layout) child;
				width = childLayout.getPrefWidth();
				height = childLayout.getPrefHeight();
				child.setSize(width, height);
			} else {
				width = child.getWidth();
				height = child.getHeight();
			}
			child.setOrigin(width / 2, height / 2);
			float groupWidth = getWidth(), groupHeight = getHeight();
			float realDistanceFromCenter = distanceFromCenter < 0 ? groupWidth / 2 - width : Math.min(distanceFromCenter, groupWidth / 2 - width);
			GeometryUtils.rotate(tmp.set(groupWidth / 2 - width / 2 - realDistanceFromCenter, groupHeight / 2), tmp2.set(groupWidth / 2, groupHeight / 2), angle * MathUtils.degRad);
			child.setPosition(tmp.x - child.getWidth() / 2, tmp.y - child.getHeight() / 2);
		}
	}

	@Override
	public void drawDebug(ShapeRenderer shapes) {
		shapes.setColor(Color.CYAN);
		for(Actor child : getChildren())
			shapes.line(getX() + getWidth() / 2, getY() + getHeight() / 2, getX() + child.getX() + child.getWidth() / 2, getY() + child.getY() + child.getHeight() / 2);
		super.drawDebug(shapes);
	}

	@Override
	public float getPrefWidth() {
		return prefWidth;
	}

	@Override
	public float getPrefHeight() {
		return prefHeight;
	}

	/** does not take rotation into account */
	@Override
	public float getMinWidth() {
		SnapshotArray<Actor> children = getChildren();
		if(children.size == 0)
			return 0;
		float minWidth = Float.POSITIVE_INFINITY;
		Actor currentSmallest = null;
		for(Actor child : children) {
			float childMinWidth = child instanceof Layout ? ((Layout) child).getMinWidth() : child.getWidth();
			if(childMinWidth < minWidth) {
				minWidth = childMinWidth;
				currentSmallest = child;
			}
		}
		float realDistanceFromCenter = Math.max(0, distanceFromCenter);
		if(children.size == 1)
			return minWidth * 2 + realDistanceFromCenter * 2;
		float secondMinWidth = Float.POSITIVE_INFINITY;
		for(Actor child : children) {
			if(child == currentSmallest)
				continue;
			float childMinWidth = child instanceof Layout ? ((Layout) child).getMinWidth() : child.getWidth();
			if(childMinWidth < secondMinWidth)
				secondMinWidth = childMinWidth;
		}
		return minWidth * 2 + secondMinWidth * 2 + realDistanceFromCenter * 2;
	}

	/** does not take rotation into account */
	@Override
	public float getMinHeight() {
		SnapshotArray<Actor> children = getChildren();
		if(children.size == 0)
			return 0;
		float minHeight = Float.POSITIVE_INFINITY;
		Actor currentSmallest = null;
		for(Actor child : children) {
			float childMinHeight = child instanceof Layout ? ((Layout) child).getMinHeight() : child.getHeight();
			if(childMinHeight < minHeight) {
				minHeight = childMinHeight;
				currentSmallest = child;
			}
		}
		float realDistanceFromCenter = Math.max(0, distanceFromCenter);
		if(children.size == 1)
			return minHeight * 2 + realDistanceFromCenter * 2;
		float secondMinHeight = Float.POSITIVE_INFINITY;
		for(Actor child : children) {
			if(child == currentSmallest)
				continue;
			float childMinHeight = child instanceof Layout ? ((Layout) child).getMinHeight() : child.getHeight();
			if(childMinHeight < secondMinHeight)
				secondMinHeight = childMinHeight;
		}
		return minHeight * 2 + secondMinHeight * 2 + realDistanceFromCenter * 2;
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
	public void translateAngleOffsets(float amount) {
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
	public void setPrefWidth(float prefWidth) {
		this.prefWidth = prefWidth;
		invalidateHierarchy();
	}

	/** @param prefHeight the {@link #prefHeight} to set */
	public void setPrefHeight(float prefHeight) {
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
		invalidate();
	}

	/** @return the {@link #dragManager} */
	public DragManager getDragManager() {
		return dragManager;
	}

}
