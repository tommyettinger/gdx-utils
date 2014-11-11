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
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.WidgetGroup;
import com.badlogic.gdx.scenes.scene2d.utils.DragListener;
import com.badlogic.gdx.scenes.scene2d.utils.Layout;
import com.badlogic.gdx.utils.SnapshotArray;
import net.dermetfan.gdx.scenes.scene2d.ui.CircularGroup.Modifier.Adapter;

import static net.dermetfan.utils.math.MathUtils.approachZero;

/** a group that aligns its children in a circle
 *  @author dermetfan
 *  @since 0.5.0 */
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

	/** whether children shall be shrinked by the difference between preferred and actual size if the actual size is smaller */
	private boolean shrinkChildren = true;

	/** the DragManager used to make this group rotatable by dragging and to apply velocity */
	private final DragManager dragManager = new DragManager();

	/** the current min size (used internally) */
	private float cachedMinWidth, cachedMinHeight;

	/** the current pref size (used internally) */
	private float cachedPrefWidth, cachedPrefHeight;

	/** if the current size has to be {@link #computeSize() computed} (used internally) */
	private boolean sizeInvalid = true;

	/** for internal use */
	private final Vector2 tmp = new Vector2();

	/** @see #CircularGroup(Modifier) */
	public CircularGroup() {
		this(null);
	}

	/** @param modifier the {@link #modifier} to set */
	public CircularGroup(Modifier modifier) {
		this.modifier = modifier != null ? modifier : new Adapter();
	}

	/** @see #CircularGroup(Modifier, boolean) */
	public CircularGroup(boolean draggable) {
		this(null, draggable);
	}

	/** @param draggable see {@link #setDraggable(boolean)}
	 *  @see #CircularGroup(Modifier) */
	public CircularGroup(Modifier modifier, boolean draggable) {
		this(modifier);
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
		shapes.set(ShapeType.Line);
		shapes.setColor(Color.CYAN);
		shapes.ellipse(getX(), getY(), getWidth() * getScaleX(), getHeight() * getScaleY());
		SnapshotArray<Actor> children = getChildren();
		for(int index = 0; index < children.size; index++) {
			Actor child = children.get(index);
			tmp.set(modifier.localAnchor(tmp.set(child.getWidth(), child.getHeight() / 2), child, index, children.size, this));
			shapes.line(getX() + getWidth() / 2 * getScaleX(), getY() + getHeight() / 2 * getScaleY(), getX() + (child.getX() + tmp.x) * getScaleX(), getY() + (child.getY() + tmp.y) * getScaleY());
		}
	}

	/** computes {@link #cachedMinWidth}, {@link #cachedMinHeight}, {@link #cachedPrefWidth} and {@link #cachedPrefHeight} */
	protected void computeSize() {
		cachedMinWidth = cachedMinHeight = Float.POSITIVE_INFINITY;
		cachedPrefWidth = cachedPrefHeight = 0;

		SnapshotArray<Actor> children = getChildren();
		for(int index = 0; index < children.size; index++) {
			Actor child = children.get(index);

			// find child size
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

			// anchor offset and local anchor
			tmp.set(modifier.anchorOffset(tmp.setZero(), child, index, children.size, this));
			float offsetX = tmp.x, offsetY = tmp.y;
			tmp.set(modifier.localAnchor(tmp.set(minWidth, minHeight / 2), child, index, children.size, this)).sub(offsetX, offsetY);
			if(tmp.x < minWidth || tmp.x < 0)
				minWidth -= tmp.x;
			else
				minWidth += tmp.x - minWidth;
			if(tmp.y < minHeight || tmp.y < 0)
				minHeight -= tmp.y;
			else
				minHeight += tmp.y - minHeight;
			tmp.set(modifier.localAnchor(tmp.set(prefWidth, prefHeight / 2), child, index, children.size, this)).sub(offsetX, offsetY);
			if(tmp.x < prefWidth || tmp.x < 0)
				prefWidth -= tmp.x;
			else
				prefWidth += tmp.x - prefWidth;
			if(tmp.y < prefHeight || tmp.y < 0)
				prefHeight -= tmp.y;
			else
				prefHeight += tmp.y - prefHeight;

			// update caches
			if(minWidth < cachedMinWidth)
				cachedMinWidth = minWidth;
			if(minHeight < cachedMinHeight)
				cachedMinHeight = minHeight;
			if(prefWidth > cachedPrefWidth)
				cachedPrefWidth = prefWidth;
			if(prefHeight > cachedPrefHeight)
				cachedPrefHeight = prefHeight;
		}

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
		float prefWidthUnderflow = shrinkChildren ? Math.max(0, getPrefWidth() - getWidth()) / 2 : 0, prefHeightUnderflow = shrinkChildren ? Math.max(0, getPrefHeight() - getHeight()) / 2 : 0;
		SnapshotArray<Actor> children = getChildren();
		for(int index = 0; index < children.size; index++) {
			Actor child = children.get(index);

			// get dimensions and resize
			float width, height;
			if(child instanceof Layout) {
				Layout childLayout = (Layout) child;
				width = childLayout.getPrefWidth() - prefWidthUnderflow;
				width = Math.max(width, childLayout.getMinWidth());
				if(childLayout.getMaxWidth() != 0)
					width = Math.min(width, childLayout.getMaxWidth());
				height = childLayout.getPrefHeight() - prefHeightUnderflow;
				height = Math.max(height, childLayout.getMinHeight());
				if(childLayout.getMaxHeight() != 0)
					height = Math.min(height, childLayout.getMaxHeight());
				child.setSize(width, height);
				childLayout.validate();
			} else {
				width = child.getWidth();
				height = child.getHeight();
			}

			float angle = fullAngle / (children.size - (virtualChildEnabled ? 0 : 1)) * index;
			angle += angleOffset;
			angle = modifier.angle(angle, child, index, children.size, this);

			float rotation = modifier.rotation(angle, child, index, children.size, this);

			tmp.set(modifier.anchorOffset(tmp.setZero(), child, index, children.size, this));
			tmp.rotate(angle);
			float offsetX = tmp.x, offsetY = tmp.y;

			tmp.set(modifier.localAnchor(tmp.set(width, height / 2), child, index, children.size, this));
			float localAnchorX = tmp.x, localAnchorY = tmp.y;

			child.setOrigin(localAnchorX, localAnchorY);
			child.setRotation(rotation);
			child.setPosition(getWidth() / 2 + offsetX - localAnchorX, getHeight() / 2 + offsetY - localAnchorY);
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
		if(modifier == null)
			throw new IllegalArgumentException("modifier must not be null");
		this.modifier = modifier;
		invalidateHierarchy();
	}

	/** @return the {@link #shrinkChildren} */
	public boolean isShrinkChildren() {
		return shrinkChildren;
	}

	/** @param shrinkChildren the {@link #shrinkChildren} to set */
	public void setShrinkChildren(boolean shrinkChildren) {
		this.shrinkChildren = shrinkChildren;
	}

	/** @return the {@link #dragManager} */
	public DragManager getDragManager() {
		return dragManager;
	}

	/** @author dermetfan
	 *  @since 0.5.0
	 *  @see #modifier */
	public interface Modifier {

		/** @param defaultAngle the linearly calculated angle of the child for even distribution
		 *  @return the angle of the child ({@link #angleOffset} will be added to this) */
		float angle(float defaultAngle, Actor child, int index, int numChildren, CircularGroup group);

		/** @param angle the angle of the child (from {@link #angle(float, Actor, int, int, CircularGroup)})
		 *  @return the rotation of the child */
		float rotation(float angle, Actor child, int index, int numChildren, CircularGroup group);

		/** @param anchorOffset the default anchor offset ({@code [0:0]})
		 *  @return the anchor offset of the child, relative to the group center */
		Vector2 anchorOffset(Vector2 anchorOffset, Actor child, int index, int numChildren, CircularGroup group);

		/** @param localAnchor the default local anchorOffset ({@code [childWidth:childHeight / 2]})
		 *  @return the local anchorOffset of the child, relative to the child itself */
		Vector2 localAnchor(Vector2 localAnchor, Actor child, int index, int numChildren, CircularGroup group);

		/** Use this if you only want to override some of {@link Modifier}'s methods.
		 *  All method implementations return the default value.
		 *  @author dermetfan
		 *  @since 0.5.0 */
		public static class Adapter implements Modifier {

			@Override
			public float angle(float defaultAngle, Actor child, int index, int numChildren, CircularGroup group) {
				return defaultAngle;
			}

			@Override
			public float rotation(float angle, Actor child, int index, int numChildren, CircularGroup group) {
				return angle;
			}

			@Override
			public Vector2 anchorOffset(Vector2 anchorOffset, Actor child, int index, int numChildren, CircularGroup group) {
				return anchorOffset;
			}

			@Override
			public Vector2 localAnchor(Vector2 localAnchor, Actor child, int index, int numChildren, CircularGroup group) {
				return localAnchor;
			}

		}

	}

	/** manages dragging and velocity of its enclosing CircularGroup instance
	 *  @author dermetfan
	 *  @since 0.5.0 */
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
