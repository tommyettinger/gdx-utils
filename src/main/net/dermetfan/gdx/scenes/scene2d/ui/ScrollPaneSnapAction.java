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
import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.Pools;
import net.dermetfan.gdx.scenes.scene2d.ui.ScrollPaneSnapAction.SnapEvent.Type;
import net.dermetfan.utils.math.MathUtils;

/** Lets a {@link ScrollPane} snap to certain {@link #slots slots}.
 *  Does nothing when added to other Actors. Stays until it is manually removed.
 *  @author dermetfan
 *  @since 0.10.0 */
public class ScrollPaneSnapAction extends Action {

	/** the target position all slots shall snap into */
	private Value targetX, targetY;

	/** the Indicator to notify (may be null) */
	private Indicator indicator;

	/** whether the y-axis should be indicated to the {@link #indicator} rather than the x-axis */
	private boolean indicateVertical;

	/** the slots to snap to
	 *  @see #findClosestSlot(Vector2) */
	private final FloatArray slots = new FloatArray();

	/** the event used to search for slots
	 *  @see #updateSlots() */
	private final SlotSearchEvent searchEvent = new SlotSearchEvent() {{
		this.setBubbles(false);
	}};

	/** the {@link #target} cast to a ScrollPane */
	private ScrollPane pane;

	/** whether a SnapEvent of the type Type.Out has already been fired */
	private boolean snapOutFired = true;

	/** whether snapping out was cancelled, needed to not snap after failing to snap out */
	private boolean snapOutCancelled;

	/** whether the ScrollPane is currently snapped */
	private boolean snapped;

	/** the last {@link ScrollPane#getVisualScrollX() visual scroll amount} of {@link #pane}, needed to know whether the {@link #indicator} must be notified */
	private float visualScrollX = Float.NaN, visualScrollY = Float.NaN;

	/** uses {@link Value#percentWidth(float) percentWidth(.5f)} and {@link Value#percentHeight(float) percentHeight(.5f)} as target */
	public ScrollPaneSnapAction() {
		this(Value.percentWidth(.5f), Value.percentHeight(.5f));
	}

	/** @param targetX the {@link #targetX}
	 *  @param targetY the {@link #targetY} */
	public ScrollPaneSnapAction(Value targetX, Value targetY) {
		this.targetX = targetX;
		this.targetY = targetY;
	}

	@Override
	public boolean act(float delta) {
		if(pane == null)
			return false;

		boolean cancelSnapping = false;
		if(pane.isDragging() || pane.isPanning()) {
			snapped = false;
			if(!snapOutFired) {
				SnapEvent event = Pools.obtain(SnapEvent.class);
				event.init(this, Type.Out, getSnappedSlotX(), getSnappedSlotY());
				if(snapOutCancelled = pane.fire(event)) {
					snap0(getSnappedSlotX(), getSnappedSlotY());
					pane.cancel();
				}
				Pools.free(event);
				snapOutFired = true;
			}
			cancelSnapping = true;
		}
		if(!cancelSnapping && (snapped |= snapOutCancelled)) {
			snapOutCancelled = false;
			snapOutFired = false;
			cancelSnapping = true;
		}
		boolean slotsUpdated = false;
		if(!cancelSnapping) {
			updateSlots();
			slotsUpdated = true;
			snapClosest();
		}

		if(indicator != null && (pane.getVisualScrollX() != visualScrollX || pane.getVisualScrollY() != visualScrollY)) {
			visualScrollX = pane.getVisualScrollX();
			visualScrollY = pane.getVisualScrollY();
			if(!slotsUpdated)
				updateSlots();
			float currentSlot = indicateVertical ? getSnappedSlotY() : getSnappedSlotX();
			int page = 0;
			float closestSmaller = Float.NEGATIVE_INFINITY, closestGreater = Float.POSITIVE_INFINITY;
			for(int i = indicateVertical ? 1 : 0; i < slots.size; i += 2) {
				float slot = slots.get(i), diff = currentSlot - slot;
				if(diff >= 0) {
					if(diff <= currentSlot - closestSmaller)
						closestSmaller = slot;
				} else if(diff >= currentSlot - closestGreater)
					closestGreater = slot;
				if(slot <= currentSlot)
					page++;
			}
			indicator.indicate(this, page, slots.size / 2, MathUtils.replaceNaN((currentSlot - closestSmaller) / (closestGreater - closestSmaller), 1));
		}

		return false;
	}

	@Override
	public void setTarget(Actor target) {
		super.setTarget(target);
		if(target instanceof ScrollPane) {
			pane = (ScrollPane) target;
			dirtyIndicator();
		} else if(target == null) {
			pane = null;
			if(indicator != null)
				indicator.indicate(this, 0, 0, 0);
		}
	}

	@Override
	public void reset() {
		super.reset();
		targetX = Value.percentWidth(.5f);
		targetY = Value.percentHeight(.5f);
		indicator = null;
		indicateVertical = false;
		slots.clear();
		searchEvent.reset();
		pane = null;
		snapOutFired = true;
		snapOutCancelled = false;
		snapped = false;
		visualScrollX = Float.NaN;
		visualScrollY = Float.NaN;
	}

	/** clears and fills {@link #slots} with slots on the given ScrollPane */
	public void updateSlots() {
		slots.clear();
		findSlots(pane.getWidget());
	}

	/** @param root the Actor from which to start searching downward recursively */
	private void findSlots(Actor root) {
		searchEvent.setTarget(pane.getWidget());
		root.notify(searchEvent, false);
		if(root instanceof Group)
			for(Actor child : ((Group) root).getChildren())
				findSlots(child);
	}

	/** @param x the x coordinate of the Slot in the {@link ScrollPane#getWidget() widget}'s coordinates
	 *  @param y the y coordinate of the Slot in the {@link ScrollPane#getWidget() widget}'s coordinates */
	public void reportSlot(float x, float y) {
		slots.ensureCapacity(2);
		slots.add(x);
		slots.add(y);
		dirtyIndicator();
	}

	/** @param slot is set to the Slot closest to the target with the visual scroll amount
	 *  @return true if a slot was found */
	public boolean findClosestSlot(Vector2 slot) {
		float targetX = this.targetX.get(pane) + pane.getVisualScrollX(), targetY = this.targetY.get(pane) + pane.getVisualScrollY();
		float closestDistance = Float.POSITIVE_INFINITY;
		boolean found = false;
		for(int i = 1; i < slots.size; i += 2) {
			float slotX = slots.get(i - 1), slotY = slots.get(i);
			float distance = Vector2.dst2(targetX, targetY, slotX, slotY);
			if(distance <= closestDistance) {
				closestDistance = distance;
				slot.set(slotX, slotY);
				found = true;
			}
		}
		return found;
	}

	/** {@link #snap(float, float) snaps} to the {@link #findClosestSlot(Vector2) closest} slot */
	private void snapClosest() {
		Vector2 vec2 = Pools.obtain(Vector2.class);
		if(findClosestSlot(vec2))
			snap(vec2.x, vec2.y);
		else
			snap(getSnappedSlotX(), getSnappedSlotY());
		Pools.free(vec2);
	}

	/** @param slotX the x coordinate of the slot to snap to
	 *  @param slotY the y coordinate of the slot to snap to */
	public void snap(float slotX, float slotY) {
		SnapEvent event = Pools.obtain(SnapEvent.class);
		if(!snapOutFired) {
			event.init(this, Type.Out, getSnappedSlotX(), getSnappedSlotY());
			snapOutCancelled = pane.fire(event);
			snapOutFired = true;
			if(snapOutCancelled) {
				Pools.free(event);
				return;
			}
		}
		event.init(this, Type.In, slotX, slotY);
		if(!pane.fire(event)) {
			snap0(slotX, slotY);
			snapped = true;
			snapOutFired = false;
		}
		Pools.free(event);
	}

	private void snap0(float slotX, float slotY) {
		pane.fling(0, 0, 0);
		pane.setScrollX(slotX - targetX.get(pane));
		pane.setScrollY(slotY - targetY.get(pane));
	}

	/** forces the {@link #indicator} to be notified next time {@link #act(float) act} is called */
	public void dirtyIndicator() {
		visualScrollX = visualScrollY = Float.NaN;
	}

	/** @return the slot x the given pane is currently snapped to (assuming it is) */
	public float getSnappedSlotX() {
		return pane.getScrollX() + targetX.get(pane);
	}

	/** @return the slot y the given pane is currently snapped to (assuming it is) */
	public float getSnappedSlotY() {
		return pane.getScrollY() + targetY.get(pane);
	}

	// getters and setters

	/** @return the {@link #targetX} */
	public Value getTargetX() {
		return targetX;
	}

	/** @param targetX the {@link #targetX} to set */
	public void setTargetX(Value targetX) {
		this.targetX = targetX;
	}

	/** @return the {@link #targetY} */
	public Value getTargetY() {
		return targetY;
	}

	/** @param targetY the {@link #targetY} to set */
	public void setTargetY(Value targetY) {
		this.targetY = targetY;
	}

	/** @param targetX the {@link #targetX} to set
	 *  @param targetY the {@link #targetY} to set */
	public void setTarget(Value targetX, Value targetY) {
		this.targetX = targetX;
		this.targetY = targetY;
	}

	/** @return the {@link #indicator} */
	public Indicator getIndicator() {
		return indicator;
	}

	/** @param indicator the {@link #indicator} to set */
	public void setIndicator(Indicator indicator) {
		this.indicator = indicator;
	}

	/** @return the {@link #indicateVertical} */
	public boolean isIndicateVertical() {
		return indicateVertical;
	}

	/** @param indicateVertical the {@link #indicateVertical} to set */
	public void setIndicateVertical(boolean indicateVertical) {
		this.indicateVertical = indicateVertical;
	}

	/** @return the {@link #slots} */
	public FloatArray getSlots() {
		return slots;
	}

	/** @return the {@link #snapped} */
	public boolean isSnapped() {
		return snapped;
	}

	/** Fired when the ScrollPane snaps into or out of a slot.
	 *  Cancelling this event will cause the ScrollPane to not snap into/out of the slot.
	 *  @author dermetfan
	 *  @since 0.10.0 */
	public static class SnapEvent extends Event {

		/** the ScrollPaneSnapAction that fired this event */
		private ScrollPaneSnapAction action;

		/** the Type of this SnapEvent */
		private Type type;

		/** the slot position */
		private float slotX, slotY;

		private void init(ScrollPaneSnapAction action, Type type, float slotX, float slotY) {
			this.action = action;
			this.type = type;
			this.slotX = slotX;
			this.slotY = slotY;
		}

		@Override
		public void reset() {
			super.reset();
			action = null;
			type = null;
			slotX = 0;
			slotY = 0;
		}

		/** @return the ScrollPane that snapped */
		public ScrollPane getScrollPane() {
			assert getListenerActor() instanceof ScrollPane;
			return (ScrollPane) getListenerActor();
		}

		// getters and setters

		/** @return the {@link #action} */
		public ScrollPaneSnapAction getAction() {
			return action;
		}

		/** @return the {@link #type} */
		public Type getType() {
			return type;
		}

		/** @return the {@link #slotX} */
		public float getSlotX() {
			return slotX;
		}

		/** @return the {@link #slotY} */
		public float getSlotY() {
			return slotY;
		}

		/** whether the slot was snapped into or out of
		 *  @author dermetfan
		 *  @since 0.10.0 */
		public enum Type {

			/** the slot was snapped into */
			In,

			/** the slot was snapped out of */
			Out

		}

	}

	/** @author dermetfan
	 *  @since 0.10.0 */
	private class SlotSearchEvent extends Event {

		/** @return the enclosing ScrollPaneSnapAction instance */
		public ScrollPaneSnapAction getAction() {
			return ScrollPaneSnapAction.this;
		}

		/** @return the ScrollPane (parent of the {@link #getListenerActor() listener actor}) */
		public ScrollPane getScrollPane() {
			assert getTarget().getParent() instanceof ScrollPane : "SlotSearchEvent#getTarget() must be ScrollPane#getWidget()";
			return (ScrollPane) getTarget().getParent();
		}

		/** converts the given coordinates and calls {@link ScrollPaneSnapAction#reportSlot(float, float)}
		 *  @param x the x coordinate of the Slot in the {@link #getListenerActor() listener actor}'s coordinates
		 *  @param y the y coordinate of the Slot in the {@link #getListenerActor() listener actor}'s coordinates */
		public void reportSlot(float x, float y) {
			Vector2 vec2 = Pools.obtain(Vector2.class);
			getListenerActor().localToAscendantCoordinates(getScrollPane().getWidget(), vec2.set(x, y));
			getAction().reportSlot(vec2.x, vec2.y);
			Pools.free(vec2);
		}

	}

	/** convenience class, calls {@link ScrollPaneSnapAction#reportSlot(float, float)} on SnapEvents
	 *  @author dermetfan
	 *  @since 0.10.0 */
	public static abstract class Slot implements EventListener {

		/** calls {@link ScrollPaneSnapAction#reportSlot(float, float) reportSlot} if the event is a {@link SlotSearchEvent} */
		@Override
		public boolean handle(Event e) {
			if(e instanceof SlotSearchEvent) {
				SlotSearchEvent event = (SlotSearchEvent) e;
				Vector2 vec2 = Pools.obtain(Vector2.class);
				getSlot(event.getListenerActor(), vec2);
				event.reportSlot(vec2.x, vec2.y);
				Pools.free(vec2);
			}
			return false;
		}

		/** @param actor the Actor which slot to get
		 *  @param slot the Vector2 to store the slot position in */
		public abstract void getSlot(Actor actor, Vector2 slot);

	}

	/** a Slot determined by an {@link Align}
	 *  @author dermetfan
	 *  @since 0.10.0 */
	public static class AlignSlot extends Slot {

		/** the {@link Align} flag */
		private int align;

		/** @param align the {@link #align} to set */
		public AlignSlot(int align) {
			this.align = align;
		}

		@Override
		public void getSlot(Actor actor, Vector2 slot) {
			slot.set(actor.getX(align) - actor.getX(), actor.getY(align) - actor.getY());
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

	/** a Slot determined by {@link Value Values}
	 *  @author dermetfan
	 *  @since 0.10.0 */
	public static class ValueSlot extends Slot {

		/** the Value determining this slot */
		private Value valueX, valueY;

		/** @param valueX the {@link #valueX} to set
		 *  @param valueY the {@link #valueY} to set */
		public ValueSlot(Value valueX, Value valueY) {
			this.valueX = valueX;
			this.valueY = valueY;
		}

		@Override
		public void getSlot(Actor actor, Vector2 slot) {
			slot.set(valueX.get(actor), valueY.get(actor));
		}

		// getters and setters

		/** @return the {@link #valueX} */
		public Value getValueX() {
			return valueX;
		}

		/** @param valueX the {@link #valueX} to set */
		public void setValueX(Value valueX) {
			this.valueX = valueX;
		}

		/** @return the {@link #valueY} */
		public Value getValueY() {
			return valueY;
		}

		/** @param valueY the {@link #valueY} to set */
		public void setValueY(Value valueY) {
			this.valueY = valueY;
		}

	}

	/** indicates the position of the {@link #getSnappedSlotX() snapped} slot
	 *  @author dermetfan
	 *  @since 0.10.0 */
	public interface Indicator {

		/** called by {@link #act(float) if the {@link ScrollPane#getVisualScrollX() visual scroll amount} changed
		 *  @param action the instance calling this method
		 *  @param page the current slot index in a sorted sequence of all slots
		 *  @param pages the number of slots
		 *  @param progress how far the ScrollPane's visual scroll amount is to the next slot */
		void indicate(ScrollPaneSnapAction action, int page, int pages, float progress);

	}

}
