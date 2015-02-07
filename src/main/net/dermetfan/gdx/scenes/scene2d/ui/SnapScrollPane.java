/** Copyright 2015 Robin Stumm (serverkorken@gmail.com, http://dermetfan.net)
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

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.utils.SnapshotArray;

/** a ScrollPane that snaps to certain scroll values
 *  @author dermetfan
 *  @since 0.10.0 */
public class SnapScrollPane extends ScrollPane {

	/** the SlotFinder to use */
	private SlotFinder slotFinder = new AlignSlotFinder(Align.center);

	/** the minimal velocity to snap to the next slot even though it's not the closest one */
	private float snapNextThreshold = 150;

	/** @see SlotFinder#getNextSlot(ScrollPane, float, float, float) */
	private float directionTolerance = .25f;

	/** if the {@link #amountX} and {@link #amountY} are currently correctly snapped, for internal use */
	private boolean snapped;

	public SnapScrollPane(Actor widget) {
		super(widget);
	}

	public SnapScrollPane(Actor widget, Skin skin) {
		super(widget, skin);
	}

	public SnapScrollPane(Actor widget, Skin skin, String styleName) {
		super(widget, skin, styleName);
	}

	public SnapScrollPane(Actor widget, ScrollPaneStyle style) {
		super(widget, style);
	}

	/** @param slotFinder the {@link #slotFinder}
	 *  @see #SnapScrollPane(Actor) */
	public SnapScrollPane(Actor widget, SlotFinder slotFinder) {
		this(widget);
		this.slotFinder = slotFinder;
	}

	/** @param slotFinder the {@link #slotFinder}
	 *  @see #SnapScrollPane(Actor, Skin) */
	public SnapScrollPane(Actor widget, SlotFinder slotFinder, Skin skin) {
		this(widget, skin);
		this.slotFinder = slotFinder;
	}

	/** @param slotFinder the {@link #slotFinder}
	 *  @see #SnapScrollPane(Actor, Skin, String) */
	public SnapScrollPane(Actor widget, SlotFinder slotFinder, Skin skin, String styleName) {
		this(widget, skin, styleName);
		this.slotFinder = slotFinder;
	}

	/** @param slotFinder the {@link #slotFinder}
	 *  @see #SnapScrollPane(Actor, Skin) */
	public SnapScrollPane(Actor widget, SlotFinder slotFinder, ScrollPaneStyle style) {
		this(widget, style);
		this.slotFinder = slotFinder;
	}

	@Override
	public void act(float delta) {
		super.act(delta);
		if(isDragging() || isPanning()) {
			snapped = false;
			return;
		}
		if(snapped)
			return;

		float halfWidth = getWidth() / 2, halfHeight = getHeight() / 2;
		Vector2 nearest = slotFinder.getNearestSlot(this, getVisualScrollX() + halfWidth, getVisualScrollY() + halfHeight);
		float slotX = nearest.x, slotY = nearest.y;

		if(isFlinging() && (Math.abs(getVelocityX()) + Math.abs(getVelocityY())) / 2 >= snapNextThreshold) {
			Vector2 next = slotFinder.getNextSlot(this, slotX, slotY, directionTolerance);
			if(next != null) {
				slotX = next.x;
				slotY = next.y;
			}
		}

		fling(0, 0, 0);
		setScrollX(slotX - halfWidth);
		setScrollY(slotY - halfHeight);
		snapped = true;
	}

	// getters and setters

	/** @return the {@link #slotFinder} */
	public SlotFinder getSlotFinder() {
		return slotFinder;
	}

	/** @param slotFinder the {@link #slotFinder} to set */
	public void setSlotFinder(SlotFinder slotFinder) {
		this.slotFinder = slotFinder;
	}

	/** @return the {@link #snapNextThreshold} */
	public float getSnapNextThreshold() {
		return snapNextThreshold;
	}

	/** @param snapNextThreshold the {@link #snapNextThreshold} to set */
	public void setSnapNextThreshold(float snapNextThreshold) {
		this.snapNextThreshold = snapNextThreshold;
	}

	/** @return the {@link #directionTolerance} */
	public float getDirectionTolerance() {
		return directionTolerance;
	}

	/** @param directionTolerance the {@link #directionTolerance} to set */
	public void setDirectionTolerance(float directionTolerance) {
		this.directionTolerance = directionTolerance;
	}

	/** finds slots on the actor the ScrollPane scrolls
	 *  @author dermetfan
	 *  @since 0.10.0 */
	public interface SlotFinder {

		/** @param targetX the x coordinate of the point to which to find the closest slot
		 *  @param targetY the y coordinate of the point to which to find the closest slot
		 *  @return the slot nearest to the given target */
		Vector2 getNearestSlot(ScrollPane pane, float targetX, float targetY);

		/** @param currentSlotX the x coordinate of the slot in respect to which the next slot should be found
		 *  @param currentSlotY the y coordinate of the slot in respect to which the next slot should be found
		 *  @param directionTolerance the ratio of the significances of locality and collinearity of each slot in respect to the given current slot, in the range from 0 to 1
		 *  @throws IllegalArgumentException if the given directionTolerance is &lt; 0 or &gt; 1
		 *  @return the next slot seen from the given slot in the direction of the velocity of the ScrollPane */
		Vector2 getNextSlot(ScrollPane pane, float currentSlotX, float currentSlotY, float directionTolerance);

	}

	/** each slot is determined by an {@link Align} flag
	 *  @author dermetfan
	 *  @since 0.10.0 */
	public static class AlignSlotFinder implements SlotFinder {

		/** the {@link Align} flag to use */
		private int align;

		private final Vector2 vec2 = new Vector2();

		/** @param align the {@link #align} */
		public AlignSlotFinder(int align) {
			this.align = align;
		}

		@Override
		public Vector2 getNearestSlot(ScrollPane pane, float targetX, float targetY) {
			Actor actor = pane.getWidget();
			vec2.set(actor.getX(align) - actor.getX(), actor.getY(align) - actor.getY());
			if(actor instanceof Group) {
				SnapshotArray<Actor> children = ((Group) actor).getChildren();
				if(children.size > 0) {
					float nearestX = Float.POSITIVE_INFINITY, nearestY = Float.POSITIVE_INFINITY;
					for(Actor child : children) {
						float slotX = child.getX(align), slotY = child.getY(align);
						if(Math.abs(targetX - slotX) < Math.abs(targetX - nearestX))
							nearestX = slotX;
						if(Math.abs(targetY - slotY) < Math.abs(targetY - nearestY))
							nearestY = slotY;
					}
					vec2.set(nearestX, nearestY);
				}
			}
			return vec2;
		}

		@Override
		public Vector2 getNextSlot(ScrollPane pane, float currentSlotX, float currentSlotY, float directionTolerance) {
			if(directionTolerance < 0 || directionTolerance > 1)
				throw new IllegalArgumentException("directionTolerance < 0 || directionTolerance > 1: " + directionTolerance);
			Actor actor = pane.getWidget();
			vec2.set(actor.getX(align) - actor.getX(), actor.getY(align) - actor.getY());
			if(actor instanceof Group) {
				SnapshotArray<Actor> children = ((Group) actor).getChildren();
				if(children.size > 0) {
					Actor candidate = null;
					float candidateNonCollinearity = Float.POSITIVE_INFINITY, candidateNonLocality = Float.POSITIVE_INFINITY;
					float targetAngle = vec2.set(pane.getVelocityX(), pane.getVelocityY()).angleRad();
					for(Actor child : children) {
						float childSlotX = child.getX(align), childSlotY = child.getY(align);

						if(Math.signum(pane.getVelocityX()) != Math.signum(currentSlotX - childSlotX) || Math.signum(pane.getVelocityY()) != Math.signum(currentSlotY - childSlotY))
							continue;
						vec2.set(currentSlotX, currentSlotY);
						float childAngle = (float) Math.atan2(vec2.crs(childSlotX, childSlotY), vec2.dot(childSlotX, childSlotY)); // perform Vector2#angleRad(Vector2) manually because we just have one Vector2 instance
						float childNonCollinearity = Math.abs(childAngle - targetAngle);

						float childNonLocality = Math.abs(childSlotX - currentSlotX) + Math.abs(childSlotY - currentSlotY);

						if(childNonCollinearity * (1 - directionTolerance) + childNonLocality * directionTolerance < candidateNonCollinearity * (1 - directionTolerance) + candidateNonLocality * directionTolerance) {
							candidateNonCollinearity = childNonCollinearity;
							candidateNonLocality = childNonLocality;
							candidate = child;
						}
					}
					return candidate != null ? vec2.set(candidate.getX(align), candidate.getY(align)) : null;
				}
			}
			return vec2;
		}

		// getters and setters

		/** @return the {@link #align} */
		public int getAlign() {
			return align;
		}

		/** @param align the {@link #align} to set */
		public void setAlign(int align) {
			this.align = align;
		}

	}

}
