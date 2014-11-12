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

package net.dermetfan.gdx.scenes.scene2d.ui.popup;

import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;
import net.dermetfan.gdx.scenes.scene2d.EventMultiplexer;
import net.dermetfan.gdx.scenes.scene2d.Scene2DUtils;
import net.dermetfan.gdx.scenes.scene2d.ui.popup.Popup.Behavior;

import static com.badlogic.gdx.scenes.scene2d.InputEvent.Type.enter;
import static com.badlogic.gdx.scenes.scene2d.InputEvent.Type.exit;
import static com.badlogic.gdx.scenes.scene2d.InputEvent.Type.mouseMoved;
import static com.badlogic.gdx.scenes.scene2d.InputEvent.Type.touchDown;
import static com.badlogic.gdx.scenes.scene2d.InputEvent.Type.touchUp;

/** The Behavior of a classic tooltip. Does nothing in {@link #show(Event, Popup)} and {@link #hide(Event, Popup)}. Hides on {@link Keys#ESCAPE escape}.
 *  Add the Popup using this TooltipBehavior to an {@link EventMultiplexer} high in the hierarchy (e.g. on the Stage) to make sure events on other Actors are received so that the TooltipBehavior can hide properly.
 *  @author dermetfan
 *  @since 0.8.0 */
public class TooltipBehavior extends Behavior.Adapter {

	/** the Task calling {@link Popup#show(Event)}/{@link Popup#hide(Event)} */
	private final PopupTask showTask = new PopupTask() {
		@Override
		public void run() {
			popup.show(event);
		}
	}, hideTask = new PopupTask() {
		@Override
		public void run() {
			popup.hide(event);
		}
	};

	/** the events that define when to show, hide or cancel the tooltip in the form {@code 1 << type.ordinal()} */
	private int showEvents = 1 << enter.ordinal(), hideEvents = 1 << touchDown.ordinal() | 1 << touchUp.ordinal() | 1 << exit.ordinal(), cancelEvents = 1 << touchDown.ordinal() | 1 << exit.ordinal();

	/** the events that require the {@link Popup#popup} to be added to the {@link Event#getTarget() event target} */
	private int targetPopupShowEvents = 1 << enter.ordinal() | 1 << exit.ordinal(), targetPopupHideEvents = 1 << mouseMoved.ordinal(), targetPopupCancelEvents;

	/** the delay before {@link Popup#show(Event)}/{@link Popup#hide(Event)} */
	private float showDelay = .75f, hideDelay;

	public TooltipBehavior() {}

	/** @param delay see {@link #setDelay(float)} */
	public TooltipBehavior(float delay) {
		setDelay(delay);
	}

	/** @param showDelay the {@link #showDelay}
	 *  @param hideDelay the {@link #hideDelay} */
	public TooltipBehavior(float showDelay, float hideDelay) {
		this.showDelay = showDelay;
		this.hideDelay = hideDelay;
	}

	/** @param showEvents the {@link #showEvents} */
	public TooltipBehavior(int showEvents) {
		this.showEvents = showEvents;
	}

	/** @param showEvents the {@link #showEvents}
	 *  @param hideEvents the {@link #hideEvents} */
	public TooltipBehavior(int showEvents, int hideEvents) {
		this.showEvents = showEvents;
		this.hideEvents = hideEvents;
	}

	/** @param showEvents the {@link #showEvents}
	 *  @param hideEvents the {@link #hideEvents}
	 *  @param cancelEvents the {@link #cancelEvents} */
	public TooltipBehavior(int showEvents, int hideEvents, int cancelEvents) {
		this.showEvents = showEvents;
		this.hideEvents = hideEvents;
		this.cancelEvents = cancelEvents;
	}

	@Override
	public Reaction handle(Event e, Popup popup) {
		if(!(e instanceof InputEvent))
			return super.handle(e, popup);
		InputEvent event = (InputEvent) e;

		Type type = event.getType();
		int flag = 1 << type.ordinal();

		if(type == Type.keyDown && event.getKeyCode() == Keys.ESCAPE && ((targetPopupHideEvents & flag) != flag || event.getTarget().getListeners().contains(popup, true)))
			return Reaction.Hide;

		if(event.getRelatedActor() == popup.getPopup())
			return super.handle(e, popup);

		if((cancelEvents & flag) == flag && ((targetPopupCancelEvents & flag) != flag || event.getTarget().getListeners().contains(popup, true)))
			showTask.cancel();

		if((hideEvents & flag) == flag && ((targetPopupHideEvents & flag) != flag || event.getTarget().getListeners().contains(popup, true))) {
			if(hideDelay > 0) {
				hideTask.init(event, popup);
				if(!hideTask.isScheduled())
					Timer.schedule(hideTask, hideDelay);
			} else
				return Reaction.Hide;
		}

		if((showEvents & flag) == flag && ((targetPopupShowEvents & flag) != flag || event.getTarget().getListeners().contains(popup, true))) {
			if(showDelay > 0) {
				showTask.init(event, popup);
				if(!showTask.isScheduled())
					Timer.schedule(showTask, showDelay);
			} else
				return Reaction.Show;
		}
		return super.handle(e, popup);
	}

	/** @param event the {@link Type} on which to show the tooltip
	 *  @return the new value of {@link #showEvents} */
	public int showOn(Type event) {
		return showEvents |= 1 << event.ordinal();
	}

	/** @param event the {@link Type} on which not to show the tooltip
	 *  @return the new value of {@link #showEvents} */
	public int showNotOn(Type event) {
		return showEvents &= ~(1 << event.ordinal());
	}

	/** @param event the {@link Type} on which to hide the tooltip
	 *  @return the new value of {@link #hideEvents} */
	public int hideOn(Type event) {
		return hideEvents |= 1 << event.ordinal();
	}

	/** @param event the {@link Type} on which not to hide the tooltip
	 *  @return the new value of {@link #hideEvents} */
	public int hideNotOn(Type event) {
		return hideEvents &= ~(1 << event.ordinal());
	}

	/** @param event the {@link Type} on which to cancel showing the tooltip
	 *  @return the new value of {@link #cancelEvents} */
	public int cancelOn(Type event) {
		return cancelEvents |= 1 << event.ordinal();
	}

	/** @param event the {@link Type} on which to not cancel showing the tooltip
	 *  @return the new value of {@link #cancelEvents} */
	public int cancelNotOn(Type event) {
		return cancelEvents &= ~(1 << event.ordinal());
	}

	/** @param delay the {@link #showDelay} and {@link #hideDelay} */
	public void setDelay(float delay) {
		showDelay = hideDelay = delay;
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

	/** @return the {@link #showEvents} */
	public int getShowEvents() {
		return showEvents;
	}

	/** @param showEvents the {@link #showEvents} to set */
	public void setShowEvents(int showEvents) {
		this.showEvents = showEvents;
	}

	/** @return the {@link #hideEvents} */
	public int getHideEvents() {
		return hideEvents;
	}

	/** @param hideEvents the {@link #hideEvents} to set */
	public void setHideEvents(int hideEvents) {
		this.hideEvents = hideEvents;
	}

	/** @return the {@link #cancelEvents} */
	public int getCancelEvents() {
		return cancelEvents;
	}

	/** @param cancelEvents the {@link #cancelEvents} to set */
	public void setCancelEvents(int cancelEvents) {
		this.cancelEvents = cancelEvents;
	}

	/** @return the {@link #targetPopupShowEvents} */
	public int getTargetPopupShowEvents() {
		return targetPopupShowEvents;
	}

	/** @param targetPopupShowEvents the {@link #targetPopupShowEvents} to set */
	public void setTargetPopupShowEvents(int targetPopupShowEvents) {
		this.targetPopupShowEvents = targetPopupShowEvents;
	}

	/** @return the {@link #targetPopupHideEvents} */
	public int getTargetPopupHideEvents() {
		return targetPopupHideEvents;
	}

	/** @param targetPopupHideEvents the {@link #targetPopupHideEvents} to set */
	public void setTargetPopupHideEvents(int targetPopupHideEvents) {
		this.targetPopupHideEvents = targetPopupHideEvents;
	}

	/** @return the {@link #targetPopupCancelEvents} */
	public int getTargetPopupCancelEvents() {
		return targetPopupCancelEvents;
	}

	/** @param targetPopupCancelEvents the {@link #targetPopupCancelEvents} to set */
	public void setTargetPopupCancelEvents(int targetPopupCancelEvents) {
		this.targetPopupCancelEvents = targetPopupCancelEvents;
	}

	/** used internally to call {@link Popup#show(Event)} or {@link Popup#hide(Event)}
	 *  @author dermetfan
	 *  @since 0.8.0
	 *  @see #showTask
	 *  @see #hideTask */
	private static abstract class PopupTask extends Task {

		/** a copy of the received InputEvent */
		protected final InputEvent event = new InputEvent();

		/** the Popup that received the Event */
		protected Popup popup;

		/** @param event the InputEvent to copy to {@link #event}
		 *  @param popup the {@link #popup} */
		public void init(InputEvent event, Popup popup) {
			this.event.reset();
			Scene2DUtils.copy(event, this.event);
			this.popup = popup;
		}

	}

	/** provides {@link #followPointer}
	 *  @author dermetfan
	 *  @since 0.8.0 */
	public static class TooltipPositionBehavior extends PositionBehavior {

		/** whether {@link Type#mouseMoved mouseMoved} events should apply the position */
		private boolean followPointer;

		/** @see PositionBehavior#PositionBehavior(Position) */
		public TooltipPositionBehavior(Position position) {
			super(position);
		}

		/** @param followPointer the {@link #followPointer} */
		public TooltipPositionBehavior(Position position, boolean followPointer) {
			super(position);
			this.followPointer = followPointer;
		}

		/** @return the {@link #followPointer} */
		public boolean isFollowPointer() {
			return followPointer;
		}

		/** @param followPointer the {@link #followPointer} to set */
		public void setFollowPointer(boolean followPointer) {
			this.followPointer = followPointer;
		}

		@Override
		public Reaction handle(Event event, Popup popup) {
			if(followPointer && event instanceof InputEvent && ((InputEvent) event).getType() == Type.mouseMoved)
				getPosition().apply(event, popup.getPopup());
			return super.handle(event, popup);
		}

	}

}
