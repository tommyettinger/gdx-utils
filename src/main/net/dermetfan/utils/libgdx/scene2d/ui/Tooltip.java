package net.dermetfan.utils.libgdx.scene2d.ui;

import static com.badlogic.gdx.scenes.scene2d.InputEvent.Type.enter;
import static com.badlogic.gdx.scenes.scene2d.InputEvent.Type.exit;
import static com.badlogic.gdx.scenes.scene2d.InputEvent.Type.mouseMoved;
import static com.badlogic.gdx.scenes.scene2d.InputEvent.Type.touchDown;
import static com.badlogic.gdx.scenes.scene2d.InputEvent.Type.touchUp;

import net.dermetfan.utils.libgdx.scene2d.Scene2DUtils;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;

/** An {@link EventListener} that shows or hides an Actor at the event or pointer position on different events with an optional offset. Making only the {@link Touchable#childrenOnly children} of {@link #tooltip} touchable or {@link Touchable#disabled disabling} touchability completely, you can also create interactive tooltips.
 *  <p>
 *  	If you want to set flags manually, note that the {@link Enum#ordinal() ordinal} value of the {@link Type event type} is used as flag and masked with {@link #mask}.
 *  	For example, this is how you would add the {@link Type#scrolled} event manually: {@code tooltip.setHideFlags(tooltip.getHideFlags() | Tooltip.mask << Type.scrolled.ordinal())}
 *  </p>
 *  @author dermetfan */
public class Tooltip implements EventListener {

	/** the actual tooltip */
	private Actor tooltip;

	/** if the tooltip should follow the pointer */
	private boolean followPointer;

	/** if the tooltip should be shown at the pointer */
	private boolean showAtPointer = true;

	/** the delay before {@link #show()} */
	private float showDelay = .75f;

	/** the delay before {@link #hide()} */
	private float hideDelay;

	/** the offset of the tooltip in respect to the mouse or enter position */
	private float offsetX, offsetY;

	/** the mask bits */
	public static final byte mask = 1;

	/** the flags that define when to hide, show or cancel the tooltip */
	private int showFlags = mask << enter.ordinal(), hideFlags = mask << touchDown.ordinal() | mask << touchUp.ordinal() | mask << exit.ordinal(), cancelFlags = mask << touchDown.ordinal() | mask << exit.ordinal();

	/** calls {@link Tooltip#show()} after setting the position of the tooltip
	 *  @see #run()
	 *  @author dermetfan */
	protected class ShowTask extends Task {

		/** @see InputEvent#getListenerActor() */
		private Actor listenerActor;

		/** @see InputEvent#getStageX() */
		private float x, y;

		/** calls {@link Tooltip#show()} after setting the position of the tooltip */
		@Override
		public void run() {
			if(showAtPointer) {
				Vector2 pos = Scene2DUtils.getPointerPosition(listenerActor.getStage());
				tooltip.setPosition(pos.x + offsetX, pos.y + offsetY);
			} else
				tooltip.setPosition(x + offsetX, y + offsetY);
			show();
		}

	}

	/** a local {@link ShowTask} instance */
	private final ShowTask showTask = new ShowTask();

	/** @see #hide() */
	private final Task hideTask = new Task() {

		@Override
		public void run() {
			hide();
		}

	};

	/** <strong>Use with caution.</strong> {@link #setTooltip(Actor) Set} a tooltip before usage (may throw NPEs otherwise). */
	public Tooltip() {}

	/** @param tooltip the {@link #tooltip} (will be set to {@link Touchable#disabled}) */
	public Tooltip(Actor tooltip) {
		this(tooltip, Touchable.disabled);
	}

	/** @param tooltip the {@link #tooltip}
	 *  @param touchable the {@link Touchable} to set on the {@link #tooltip} (if null, not touchability will not be changed) */
	public Tooltip(Actor tooltip, Touchable touchable) {
		this.tooltip = tooltip;
		if(touchable != null)
			tooltip.setTouchable(touchable);
	}

	@Override
	public boolean handle(Event e) {
		if(!(e instanceof InputEvent))
			return false;
		InputEvent event = (InputEvent) e;
		if(event.getRelatedActor() == tooltip)
			return false;

		Type type = event.getType();
		int flag = mask << type.ordinal();

		if(type == mouseMoved && followPointer)
			tooltip.setPosition(event.getStageX() + offsetX, event.getStageY() + offsetY);

		if((cancelFlags & flag) == flag)
			showTask.cancel();

		if((hideFlags & flag) == flag)
			if(hideDelay > 0) {
				if(!hideTask.isScheduled())
					Timer.schedule(hideTask, hideDelay);
			} else
				hideTask.run();

		if((showFlags & flag) == flag) {
			showTask.listenerActor = event.getListenerActor();
			showTask.x = event.getStageX();
			showTask.y = event.getStageY();
			if(showDelay > 0) {
				if(!showTask.isScheduled())
					Timer.schedule(showTask, showDelay);
			} else
				showTask.run();
		}
		return false;
	}

	/** Brings the {@link #tooltip} {@link Actor#toFront() to front} and {@link Actions#fadeIn(float) fades} it in for {@link Dialog#fadeDuration} seconds. Override this for custom behavior. */
	public void show() {
		tooltip.toFront();
		tooltip.addAction(Actions.fadeIn(Dialog.fadeDuration));
	}

	/** {@link Actions#fadeOut(float) Fades} the tooltip out for {@link Dialog#fadeDuration} seconds. Override this for custom behavior. */
	public void hide() {
		tooltip.addAction(Actions.fadeOut(Dialog.fadeDuration));
	}

	/** @param flag the {@link Type} on which to show the tooltip */
	public void showOn(Type flag) {
		showFlags |= mask << flag.ordinal();
	}

	/** @param flag the {@link Type} on which not to show the tooltip */
	public void showNotOn(Type flag) {
		showFlags &= ~(mask << flag.ordinal());
	}

	/** @param flag the {@link Type} on which to hide the tooltip */
	public void hideOn(Type flag) {
		hideFlags |= mask << flag.ordinal();
	}

	/** @param flag the {@link Type} on which not to hide the tooltip */
	public void hideNotOn(Type flag) {
		hideFlags &= ~(mask << flag.ordinal());
	}

	/** @param flag the {@link Type} on which to cancel showing the tooltip */
	public void cancelOn(Type flag) {
		cancelFlags |= mask << flag.ordinal();
	}

	/** @param flag the {@link Type} on which to not cancel showing the tooltip */
	public void cancelNotOn(Type flag) {
		cancelFlags &= ~(mask << flag.ordinal());
	}

	/** never show the tooltip */
	public void showNever() {
		showFlags = 0;
	}

	/** never hide the tooltip */
	public void hideNever() {
		hideFlags = 0;
	}

	/** never cancel showing the tooltip */
	public void cancelNever() {
		cancelFlags = 0;
	}

	/** show the tooltip on any event */
	public void showAlways() {
		showFlags = ~0;
	}

	/** hide the tooltip on any event */
	public void hideAlways() {
		hideFlags = ~0;
	}

	/** cancel showing the tooltip on any event */
	public void cancelAlways() {
		cancelFlags = ~0;
	}

	/** @param delay the {@link #showDelay} and {@link #hideDelay} */
	public void setDelay(float delay) {
		showDelay = hideDelay = delay;
	}

	/** @param offsetX the {@link #offsetX}
	 *  @param offsetY the {@link #offsetY} */
	public void setOffset(float offsetX, float offsetY) {
		this.offsetX = offsetX;
		this.offsetY = offsetY;
	}

	/** @return the {@link #tooltip} */
	public Actor getTooltip() {
		return tooltip;
	}

	/** @param tooltip the {@link #tooltip} to set */
	public void setTooltip(Actor tooltip) {
		this.tooltip = tooltip;
	}

	/** @return the {@link #followPointer} */
	public boolean isFollowPointer() {
		return followPointer;
	}

	/** @param followPointer the {@link #followPointer} to set */
	public void setFollowPointer(boolean followPointer) {
		this.followPointer = followPointer;
	}

	/** @return the {@link #showAtPointer} */
	public boolean isShowAtPointer() {
		return showAtPointer;
	}

	/** @param showAtPointer the {@link #showAtPointer} to set */
	public void setShowAtPointer(boolean showAtPointer) {
		this.showAtPointer = showAtPointer;
	}

	/** @return the {@link #showDelay} */
	public float getShowDelay() {
		return showDelay;
	}

	/** @param showDelay the {@link #showDelay} to set */
	public void setShowDelay(float showDelay) {
		this.showDelay = showDelay;
	}

	/** @return the {@link #hideDelay} */
	public float getHideDelay() {
		return hideDelay;
	}

	/** @param hideDelay the {@link #hideDelay} to set */
	public void setHideDelay(float hideDelay) {
		this.hideDelay = hideDelay;
	}

	/** @return the {@link #offsetX} */
	public float getOffsetX() {
		return offsetX;
	}

	/** @param offsetX the {@link #offsetX} to set */
	public void setOffsetX(float offsetX) {
		this.offsetX = offsetX;
	}

	/** @return the {@link #offsetY} */
	public float getOffsetY() {
		return offsetY;
	}

	/** @param offsetY the {@link #offsetY} to set */
	public void setOffsetY(float offsetY) {
		this.offsetY = offsetY;
	}

	/** @return the {@link #showFlags} */
	public int getShowFlags() {
		return showFlags;
	}

	/** @param showFlags the {@link #showFlags} to set */
	public void setShowFlags(int showFlags) {
		this.showFlags = showFlags;
	}

	/** @return the {@link #hideFlags} */
	public int getHideFlags() {
		return hideFlags;
	}

	/** @param hideFlags the {@link #hideFlags} to set */
	public void setHideFlags(int hideFlags) {
		this.hideFlags = hideFlags;
	}

	/** @return the {@link #cancelFlags} */
	public int getCancelFlags() {
		return cancelFlags;
	}

	/** @param cancelFlags the {@link #cancelFlags} to set */
	public void setCancelFlags(int cancelFlags) {
		this.cancelFlags = cancelFlags;
	}

}
