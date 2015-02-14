package net.dermetfan.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Value;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.utils.Array;

/** @author dermetfan
 *  @since 0.10.0 */
public class SnapScrollPane extends ScrollPane {

	/** The Slots to snap to. Invalid Slots are lazily {@link #removeInvalidSlots() removed}. */
	private final Array<Slot> slots = new Array<>();

	/** the current Slot */
	private int slot;

	/** The minimal mean velocity to snap to the {@link #findNextSlot(float, float) next} Slot. Default is {@value}. */
	private float snapNextThreshold = 150;

	/** Direction tolerance for {@link #findNextSlot(float, float)}.
	 *  0 means the Slot has to be perfectly aligned, 1 means the direction is ignored (so only position matters, resulting in behavior equivalent to {@link #findClosestSlot()})
	 *  Default is {@value}. */
	private float directionTolerance = .25f;

	/** The Slot {@link #slot} is supposed to snap into. Does not need to have an {@link Slot#actor actor} explicitly set, it will be set when needed. */
	private Slot<ScrollPane> target;

	/** for internal, temporary use */
	private final Vector2 vec2 = new Vector2();

	public SnapScrollPane(Actor widget) {
		this(widget, Align.center);
	}

	public SnapScrollPane(Actor widget, Skin skin) {
		this(widget, Align.center, skin);
	}

	public SnapScrollPane(Actor widget, Skin skin, String styleName) {
		this(widget, Align.center, skin, styleName);
	}

	public SnapScrollPane(Actor widget, ScrollPaneStyle style) {
		this(widget, Align.center, style);
	}

	public SnapScrollPane(Actor widget, int align) {
		this(widget, new AlignSlot<ScrollPane>(null, align));
	}

	public SnapScrollPane(Actor widget, int align, Skin skin) {
		this(widget, new AlignSlot<ScrollPane>(null, align), skin);
	}

	public SnapScrollPane(Actor widget, int align, Skin skin, String styleName) {
		this(widget, new AlignSlot<ScrollPane>(null, align), skin, styleName);
	}

	public SnapScrollPane(Actor widget, int align, ScrollPaneStyle style) {
		this(widget, new AlignSlot<ScrollPane>(null, align), style);
	}

	public SnapScrollPane(Actor widget, Slot<ScrollPane> target) {
		super(widget);
		this.target = target;
	}

	public SnapScrollPane(Actor widget, Slot<ScrollPane> target, Skin skin) {
		super(widget, skin);
		this.target = target;
	}

	public SnapScrollPane(Actor widget, Slot<ScrollPane> target, Skin skin, String styleName) {
		super(widget, skin, styleName);
		this.target = target;
	}

	public SnapScrollPane(Actor widget, Slot<ScrollPane> target, ScrollPaneStyle style) {
		super(widget, style);
		this.target = target;
	}

	{
		addCaptureListener(new InputListener() {
			@Override
			public boolean touchDown(InputEvent event, float x, float y, int pointer, int button) {
				return event.getListenerActor() instanceof ScrollPane;
			}

			@Override
			public void touchUp(InputEvent event, float x, float y, int pointer, int button) {
				if(!(event.getListenerActor() instanceof ScrollPane))
					return;
				if(event.isTouchFocusCancel() && !isFlinging()) {
					event.getStage().addTouchFocus(this, SnapScrollPane.this, event.getTarget(), pointer, button);
					return;
				}

				removeInvalidSlots();
				if(isFlinging() && Math.abs(getVelocityX() + getVelocityY()) / 2 >= snapNextThreshold) {
					Slot slot = findNextSlot(getVelocityX(), getVelocityY());
					if(slot != null)
						setSlot(slot);
				} else
					setSlot(findClosestSlot());
			}
		});
	}

	/** @param slot the Slot to snap to */
	public void snap(Slot slot) {
		float slotX = slot.getX(), slotY = slot.getY();
		if(slot.getActor() != getWidget()) {
			assert getWidget() instanceof Group : "invalid Slot, ScrollPane#widget is not a group so Slot#actor must be ScrollPane#widget but it is not";
			assert slot.getActor().isDescendantOf(getWidget()) : "invalid Slot, Slot#actor is not a child of or ScrollPane#widget";
			vec2.set(slotX, slotY);
			slot.getActor().localToAscendantCoordinates(getWidget(), vec2);
			slotX = vec2.x;
			slotY = vec2.y;
		}
		fling(0, 0, 0);
		target.setActor(this);
		setScrollX(slotX - target.getX());
		setScrollY(slotY - target.getY());
	}

	/** @return the Slot closest to {@link #target} with the visual scroll amount */
	public Slot findClosestSlot() {
		Slot closest = null;
		target.setActor(this);
		float targetX = target.getX() + getVisualScrollX(), targetY = target.getY() + getVisualScrollY();
		float closestDistance = Float.POSITIVE_INFINITY;
		for(Slot slot : slots) {
			vec2.set(slot.getX(), slot.getY());
			slot.getActor().localToAscendantCoordinates(getWidget(), vec2);
			float distance = Vector2.dst2(targetX, targetY, vec2.x, vec2.y);
			if(distance < closestDistance) {
				closestDistance = distance;
				closest = slot;
			}
		}
		return closest;
	}

	/** @return the next Slot in the given direction starting from the {@link #findClosestSlot() closest} Slot */
	public Slot findNextSlot(float directionX, float directionY) {
		Slot next = null, current = findClosestSlot();
		current.getActor().localToAscendantCoordinates(getWidget(), vec2.set(current.getX(), current.getY()));
		float currentX = vec2.x, currentY = vec2.y;
		float candidateNonCollinearity = Float.POSITIVE_INFINITY, candidateNonLocality = Float.POSITIVE_INFINITY;
		float targetAngle = vec2.set(directionX, directionY).angleRad();
		for(Slot slot : slots) {
			slot.getActor().localToAscendantCoordinates(getWidget(), vec2.set(slot.getX(), slot.getY()));
			float slotX = vec2.x, slotY = vec2.y;

			if(directionTolerance < 1 && (Math.signum(directionX) != Math.signum(currentX - slotX) || Math.signum(directionY) != Math.signum(currentY - slotY)))
				continue;
			vec2.set(currentX, currentY);
			float childAngle = (float) Math.atan2(vec2.crs(slotX, slotY), vec2.dot(slotX, slotY)); // perform Vector2#angleRad(Vector2) manually because we just have one Vector2 instance
			float childNonCollinearity = Math.abs(childAngle - targetAngle);

			float childNonLocality = Math.abs(slotX - currentX) + Math.abs(slotY - currentY);

			if(childNonCollinearity * (1 - directionTolerance) + childNonLocality * directionTolerance < candidateNonCollinearity * (1 - directionTolerance) + candidateNonLocality * directionTolerance) {
				candidateNonCollinearity = childNonCollinearity;
				candidateNonLocality = childNonLocality;
				next = slot;
			}
		}
		return next;
	}

	/** Removes every Slot which {@link Slot#actor actor} is not a {@link Actor#isDescendantOf(Actor) descendant} of the widget */
	public void removeInvalidSlots() {
		for(int i = 0; i < slots.size; ) {
			if(!slots.get(i).getActor().isDescendantOf(this))
				slots.removeIndex(i);
			else
				i++;
		}
	}

	/** adds the given Slot if it is not already added
	 *  @param slot the Slot to add
	 *  @throws IllegalArgumentException if the Slot is {@link #removeInvalidSlots() invalid} */
	public void addSlot(Slot slot) {
		if(!slot.getActor().isDescendantOf(this))
			throw new IllegalArgumentException("the given Slot's actor is not a descendant of the ScrollPane#widget");
		if(!slots.contains(slot, true))
			slots.add(slot);
	}

	/** removes the given Slot if it is added and {@link #setSlot(Slot) sets} the closest Slot
	 *  @param slot the Slot to remove
	 *  @return if the Slot was found and removed */
	public boolean removeSlot(Slot slot) {
		Slot oldSlot = getSlot();
		boolean removed = slots.removeValue(slot, true);
		if(slot == oldSlot) {
			if(slots.size == 1)
				setSlot(slots.first());
			else if(slots.size > 1)
				setSlot(findClosestSlot());
		}
		return removed;
	}

	/** @param slot the {@link #slot} to set and {@link #snap(Slot) snap} to */
	public void setSlot(Slot slot) {
		addSlot(slot);
		this.slot = slots.indexOf(slot, true);
		snap(slot);
	}

	/** @return the Slot corresponding to {@link #slot} */
	public Slot getSlot() {
		return slots.get(slot);
	}

	/** @return the {@link #slots} with invalid slots {@link #removeInvalidSlots() removed} */
	public Array<Slot> getSlots() {
		removeInvalidSlots();
		return slots;
	}

	// getters and setters

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

	/** @return the {@link #target} */
	public Slot<ScrollPane> getTarget() {
		return target;
	}

	/** @param target the {@link #target} to set */
	public void setTarget(Slot<ScrollPane> target) {
		this.target = target;
	}

	/** a slot on an Actor the Snapper can snap to
	 *  @author dermetfan
	 *  @since 0.10.0 */
	public static abstract class Slot<T extends Actor> {

		/** the Actor of this Slot */
		protected T actor;

		/** @param actor the the {@link #actor} to set */
		public Slot(T actor) {
			this.actor = actor;
		}

		/** @return the x coordinate of the slot position in the {@link #actor}'s coordinate system */
		public abstract float getX();

		/** @return the y coordinate of the slot position in the {@link #actor}'s coordinate system */
		public abstract float getY();

		// getters and setters

		/** @param actor the {@link #actor} to set */
		public void setActor(T actor) {
			this.actor = actor;
		}

		/** @return the {@link #actor} */
		public T getActor() {
			return actor;
		}

	}

	/** a Slot determined by an {@link Align} on the reference Actor
	 *  @author dermetfan
	 *  @since 0.10.0 */
	public static class AlignSlot<T extends Actor> extends Slot<T> {

		/** the {@link Align} flag */
		private int align;

		/** @param align the {@link #align} to set */
		public AlignSlot(T actor, int align) {
			super(actor);
			this.align = align;
		}

		@Override
		public float getX() {
			return actor.getX(align) - actor.getX();
		}

		@Override
		public float getY() {
			return actor.getY(align) - actor.getY();
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
	public static class ValueSlot<T extends Actor> extends Slot<T> {

		/** the Value determining this slot */
		private Value valueX, valueY;

		/** @param valueX the {@link #valueX} to set
		 *  @param valueY the {@link #valueY} to set */
		public ValueSlot(T actor, Value valueX, Value valueY) {
			super(actor);
			this.valueX = valueX;
			this.valueY = valueY;
		}

		@Override
		public float getX() {
			return valueX.get(actor);
		}

		@Override
		public float getY() {
			return valueX.get(actor);
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
