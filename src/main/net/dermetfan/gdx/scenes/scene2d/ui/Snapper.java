package net.dermetfan.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.Pools;

/** @author dermetfan
 *  @since 0.10.0 */
public class Snapper extends InputListener {

	/** the slots to snap to
	 *  @see #findClosestSlot(ScrollPane, Vector2) */
	private final FloatArray slots = new FloatArray();

	/** the event used to search for slots
	 *  @see #updateSlots(ScrollPane) */
	private final SlotSearchEvent event = new SlotSearchEvent() {{
		this.setBubbles(false);
	}};

//	/** The minimal mean velocity to snap to the {@link #findNextSlot(ScrollPane, float, float, Vector2) next} Slot. Default is {@value}. */
//	private float snapNextThreshold = 150;
//
//	/** Direction to position ratio for Slot rating in {@link #findNextSlot(ScrollPane, float, float, Vector2)}.
//	 *  1 means the Slot has to be perfectly aligned, 0 means the direction is ignored (so only position matters, resulting in behavior equivalent to {@link #findClosestSlot(ScrollPane, Vector2)}).
//	 *  Default is {@value}. */
//	private float nextSlotAccuracy = .75f;

	/** the target position all slots shall snap to */
	private Value targetX = Value.percentWidth(.5f), targetY = Value.percentHeight(.5f);

	/** for internal, temporary use */
	private final Vector2 vec2 = new Vector2();

	@Override
	public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
		return true;
	}

	@Override
	public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
		if(!(event.getListenerActor() instanceof ScrollPane))
			return;
		ScrollPane pane = (ScrollPane) event.getListenerActor();
		if(event.isTouchFocusCancel() && (pane.isDragging() || pane.isPanning())) {
			event.getStage().addTouchFocus(this, event.getListenerActor(), event.getTarget(), pointer, button);
			return;
		}
		updateSlots(pane);
		snapClosest(pane);
	}

	/** clears and fills {@link #slots} with slots on the given ScrollPane
	 *  @param pane the ScrollPane on which to search for slots */
	public void updateSlots(ScrollPane pane) {
		slots.clear();
		findSlots(pane, pane.getWidget());
	}

	/** @param pane the ScrollPane on which to find slots
	 *  @param root the Actor from which to start searching downward recursively */
	private void findSlots(ScrollPane pane, Actor root) {
		event.setTarget(pane.getWidget());
		root.notify(event, false);
		if(root instanceof Group)
			for(Actor child : ((Group) root).getChildren())
				findSlots(pane, child);
	}

	/** @param x the x coordinate of the Slot in the {@link ScrollPane#getWidget() widget}'s coordinates
	 *  @param y the y coordinate of the Slot in the {@link ScrollPane#getWidget() widget}'s coordinates */
	public void reportSlot(float x, float y) {
		slots.ensureCapacity(2);
		slots.add(x);
		slots.add(y);
	}

	/** @param pane the ScrollPane on which to get the closest slot
	 *  @param slot is set to the Slot closest to the target with the visual scroll amount */
	public void findClosestSlot(ScrollPane pane, Vector2 slot) {
		float targetX = this.targetX.get(pane) + pane.getVisualScrollX(), targetY = this.targetY.get(pane) + pane.getVisualScrollY();
		float closestDistance = Float.POSITIVE_INFINITY;
		for(int i = 1; i < slots.size; i += 2) {
			float slotX = slots.get(i - 1), slotY = slots.get(i);
			float distance = Vector2.dst2(targetX, targetY, slotX, slotY);
			if(distance < closestDistance) {
				closestDistance = distance;
				slot.set(slotX, slotY);
			}
		}
	}

//	/** @param pane the ScrollPane on which to get the closest slot
//	 *  @param slot is set to the next Slot in the given direction starting from the {@link #getSnappedSlotX(ScrollPane) current} Slot */
//	public void findNextSlot(ScrollPane pane, float directionX, float directionY, Vector2 slot) {
//		float candidateX = Float.NaN, candidateY = Float.NaN, currentX = getSnappedSlotX(pane), currentY = getSnappedSlotY(pane);
//		float candidateNonScore = Float.POSITIVE_INFINITY;
//		float targetAngle = vec2.set(directionX, directionY).angleRad();
//		for(int i = 1; i < slots.size; i += 2) {
//			float slotX = slots.get(i - 1), slotY = slots.get(i);
//			if(slotX == currentX && slotY == currentY)
//				continue;
//
//			// collinearity
//			vec2.set(currentX, currentY);
//			float childAngle = (float) Math.atan2(vec2.crs(slotX, slotY), vec2.dot(slotX, slotY)); // perform Vector2#angleRad(Vector2) manually because we just have one Vector2 instance
//			float childNonCollinearity = Math.abs(childAngle - targetAngle);
//			float criteriaRatio = childNonCollinearity / (MathUtils.PI / 2);
//			if(criteriaRatio > 1)
//				continue;
//			// locality
//			float childNonLocality = Vector2.dst2(currentX, currentY, slotX, slotY);
//			// score
//			float childNonScore = childNonCollinearity * criteriaRatio + childNonLocality * (1 - criteriaRatio);
//
//			if(childNonScore < candidateNonScore) {
//				candidateNonScore = childNonScore;
//				candidateX = slotX;
//				candidateY = slotY;
//			}
//		}
//		slot.set(candidateX, candidateY);
//	}

	/** {@link #snap(ScrollPane, float, float) snaps} to the {@link #findClosestSlot(ScrollPane, Vector2) closest} slot
	 *  @param pane the ScrollPane to snap */
	private void snapClosest(ScrollPane pane) {
		vec2.set(Float.NaN, Float.NaN);
		findClosestSlot(pane, vec2);
		if(Float.isNaN(vec2.x) || Float.isNaN(vec2.y))
			snap(pane, getSnappedSlotX(pane), getSnappedSlotY(pane));
		else
			snap(pane, vec2.x, vec2.y);
	}

//	private void snapNext(float velocityX, float velocityY, ScrollPane pane) {
//		vec2.set(Float.NaN, Float.NaN);
//		if((Math.abs(velocityX) + Math.abs(velocityY)) / 2 >= snapNextThreshold)
//			findNextSlot(pane, -velocityX, velocityY, vec2);
//		if(Float.isNaN(vec2.x) || Float.isNaN(vec2.y))
//			findClosestSlot(pane, vec2);
//		snap(pane, vec2.x, vec2.y);
//	}

	/** @param pane the ScrollPane to snap to the given slot
	 *  @param slotX the x coordinate of the slot to snap to
	 *  @param slotY the y coordinate of the slot to snap to */
	public void snap(ScrollPane pane, float slotX, float slotY) {
		pane.fling(0, 0, 0);
		pane.setScrollX(slotX - targetX.get(pane));
		pane.setScrollY(slotY - targetY.get(pane));
	}

	/** @param pane the ScrollPane which currently snapped slot x to get
	 *  @return the slot x the given pane is currently snapped to (assuming it is) */
	public float getSnappedSlotX(ScrollPane pane) {
		return pane.getScrollX() + targetX.get(pane);
	}

	/** @param pane the ScrollPane which currently snapped slot y to get
	 *  @return the slot y the given pane is currently snapped to (assuming it is) */
	public float getSnappedSlotY(ScrollPane pane) {
		return pane.getScrollY() + targetY.get(pane);
	}

	// getters and setters

//	/** @return the {@link #snapNextThreshold} */
//	public float getSnapNextThreshold() {
//		return snapNextThreshold;
//	}
//
//	/** @param snapNextThreshold the {@link #snapNextThreshold} to set */
//	public void setSnapNextThreshold(float snapNextThreshold) {
//		this.snapNextThreshold = snapNextThreshold;
//	}
//
//	/** @return the {@link #nextSlotAccuracy} */
//	public float getNextSlotAccuracy() {
//		return nextSlotAccuracy;
//	}
//
//	/** @param nextSlotAccuracy the {@link #nextSlotAccuracy} to set */
//	public void setNextSlotAccuracy(float nextSlotAccuracy) {
//		this.nextSlotAccuracy = nextSlotAccuracy;
//	}

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

	/** @author dermetfan
	 *  @since 0.10.0 */
	private class SlotSearchEvent extends Event {

		/** @return the enclosing Snapper instance */
		public Snapper getSnapper() {
			return Snapper.this;
		}

		/** @return the ScrollPane (parent of the {@link #getListenerActor() listener actor}) */
		public ScrollPane getScrollPane() {
			assert getTarget().getParent() instanceof ScrollPane : "SlotSearchEvent#getTarget() must be ScrollPane#getWidget()";
			return (ScrollPane) getTarget().getParent();
		}

		/** converts the given coordinates and calls {@link Snapper#reportSlot(float, float)}
		 *  @param x the x coordinate of the Slot in the {@link #getListenerActor() listener actor}'s coordinates
		 *  @param y the y coordinate of the Slot in the {@link #getListenerActor() listener actor}'s coordinates */
		public void reportSlot(float x, float y) {
			getListenerActor().localToAscendantCoordinates(getScrollPane().getWidget(), vec2.set(x, y));
			getSnapper().reportSlot(vec2.x, vec2.y);
		}

	}

	/** convenience class, calls {@link Snapper#reportSlot(float, float)} on SnapEvents
	 *  @author dermetfan
	 *  @since 0.10.0 */
	public static abstract class Slot implements EventListener {

		/** calls {@link Snapper#reportSlot(float, float) reportSlot} if the event is a {@link SlotSearchEvent} */
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

}
