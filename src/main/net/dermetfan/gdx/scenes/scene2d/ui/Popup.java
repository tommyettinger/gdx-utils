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

import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;
import net.dermetfan.gdx.Multiplexer;
import net.dermetfan.gdx.scenes.scene2d.Scene2DUtils;

import static com.badlogic.gdx.scenes.scene2d.InputEvent.Type.enter;
import static com.badlogic.gdx.scenes.scene2d.InputEvent.Type.exit;
import static com.badlogic.gdx.scenes.scene2d.InputEvent.Type.mouseMoved;
import static com.badlogic.gdx.scenes.scene2d.InputEvent.Type.touchDown;
import static com.badlogic.gdx.scenes.scene2d.InputEvent.Type.touchUp;

/** Flexible popup system for things like tooltips, context menus and menu bars.
 *  <p>
 *      Popup is an EventListener with a popup Actor that can be {@link #show() shown} and {@link #hide() hidden} and is controlled via its {@link Behavior Behavior}.
 *      Behavior is an interface for actual show and hide implementations which are triggered by the {@link Reaction Reaction} returned by its {@link Behavior#handle(Event, Popup) handle} method.
 *  </p>
 *  <p>
 *      Usually a Behavior overrides either {@link Behavior#show(Event, Popup) show} and {@link Behavior#hide(Event, Popup) hide} or {@link Behavior#handle(Event, Popup) handle} so that its purpose is clearly distinct from other Behaviors.
 *      Those distinct Behaviors are then combined using a {@link BehaviorMultiplexer BehaviorMultiplexer}. You can of course override all methods in your own implementation though.
 *  </p>
 *  <p>
 *      Have a look at the {@link VisibilityBehavior VisibilityBehavior} and {@link FadeBehavior FadeBehavior} for examples of Behaviors that only override show and hide.<br>
 *      The {@link PositionBehavior PositionBehavior} just positions the popup in show. The actual position is applied by its {@link PositionBehavior.Position Position} which can be combined using {@link PositionBehavior.PositionMultiplexer PositionMultiplexers}.
 *  </p>
 *  Examples:
 *  <ul>
 *      <li>
 *          Menu bar menu:<br>
 *          {@code new Popup<>(menu, new MenuBehavior(), new PositionBehavior(new AlignPosition(Align.bottomLeft, Align.topLeft)), new VisibilityBehavior())}
 *      </li>
 *      <li>
 *          Context menu:<br>
 *          {@code new Popup<>(contextMenu, new MenuBehavior(Buttons.RIGHT), new PositionBehavior(new AlignPosition(Align.topRight, Align.topLeft)), new VisibilityBehavior())}<br>
 *          <em>The only differences between this and the previous example are the button passed to the MenuBehavior and the first parameter of the AlignPosition constructor.</em>
 *      </li>
 *      <li>
 *          Tooltip:<br>
 *          {@code new Popup<>(tooltip, new TooltipBehavior(), new PositionBehavior(new PointerPosition(), new AlignedOffsetPosition(Align.topLeft)), new VisibilityBehavior())}
 *      </li>
 *  </ul>
 *  <p>
 *      Some Behaviors (like the {@link Popup.MenuBehavior} and {@link Popup.TooltipBehavior}) can only function properly if they receive events of Actors other than their {@link Event#getListenerActor() listener Actor} (for example to hide when other Actors are clicked).
 *      Those Behaviors usually state so in their documentation. Add the Popups using such Behaviors to an {@link net.dermetfan.gdx.scenes.scene2d.EventMultiplexer EventMultiplexer} high in the hierarchy or directly on the stage.
 *      A Behavior can distinguish between an Event on its listener Actor by checking if it has its Popup added as listener.<br>
 *      Be warned that in return Behaviors not designed for this may not function properly if you forward events from other Actors to them.
 *  </p>
 *  @param <T> the type of {@link #popup}
 *  @author dermetfan
 *  @since 0.9.0 */
public class Popup<T extends Actor> implements EventListener {

	/** the {@code T} to pop up */
	private T popup;

	/** the Behavior to delegate to */
	private Behavior behavior;

	/** @param popup the {@link #popup}
	 *  @param behavior the {@link #behavior} */
	public Popup(T popup, Behavior behavior) {
		this.popup = popup;
		this.behavior = behavior;
	}

	/** @param behaviors the Behaviors to create a {@link BehaviorMultiplexer} for
	 *  @see #Popup(Actor, Behavior) */
	public Popup(T popup, Behavior... behaviors) {
		this(popup, new BehaviorMultiplexer(behaviors));
	}

	/** @see #show(Event) */
	public boolean show() {
		Event dummy = Pools.obtain(InputEvent.class);
		boolean result = show(dummy);
		Pools.free(dummy);
		return result;
	}

	/** Makes the {@link #popup} {@link Actor#setVisible(boolean) visible} and brings it to {@link Actor#toFront() front}. Override this for custom behaviour.
	 *  @return if the event is handled */
	public boolean show(Event event) {
		return behavior.show(event, this);
	}

	/** @see #hide(Event) */
	public boolean hide() {
		Event dummy = Pools.obtain(InputEvent.class);
		boolean result = hide(dummy);
		Pools.free(dummy);
		return result;
	}

	/** Makes the {@link #popup} {@link Actor#setVisible(boolean) invisible}. Override this for custom behavior.
	 *  @return if the event is handled */
	public boolean hide(Event event) {
		return behavior.hide(event, this);
	}

	/** @see Behavior#handle(Event, Popup) */
	@Override
	public boolean handle(Event event) {
		Reaction reaction = behavior.handle(event, this);
		if(reaction == null)
			reaction = Reaction.None;
		switch(reaction) {
		case ShowHandle:
		case Show:
			show(event);
			break;
		case HideHandle:
		case Hide:
			hide(event);
		}
		return reaction.handles;
	}

	/** @param child the possible popup child
	 *  @return whether the given Actor is the {@link Popup#popup popup} of this or a child {@link Popup} */
	public boolean isAscendantOf(Actor child) {
		if(popup == child)
			return true;
		for(EventListener listener : popup.getListeners())
			if(listener instanceof Popup && ((Popup) listener).isAscendantOf(child))
				return true;
		return false;
	}

	// getters and setters

	/** @return the {@link #popup} */
	public T getPopup() {
		return popup;
	}

	/** @param popup the {@link #popup} to set */
	public void setPopup(T popup) {
		this.popup = popup;
	}

	/** @return the {@link #behavior} */
	public Behavior getBehavior() {
		return behavior;
	}

	/** @param behavior the {@link #behavior} to set */
	public void setBehavior(Behavior behavior) {
		this.behavior = behavior;
	}

	/** @author dermetfan
	 *  @see Behavior#handle(Event, Popup) */
	public enum Reaction {

		/** call {@link Popup#show(Event)} */
		Show(false),

		/** call {@link Popup#hide(Event)} */
		Hide(false),

		/** do nothing */
		None(false),

		/** @see #Show */
		ShowHandle(true),

		/** @see #Hide */
		HideHandle(true),

		/** @see #None */
		Handle(true);

		/** whether this Reaction {@link Event#handle() handles} the Event */
		public final boolean handles;

		/** @param handles the {@link #handles} */
		Reaction(boolean handles) {
			this.handles = handles;
		}

	}

	/** what to do in the Popup methods
	 *  @author dermetfan */
	public interface Behavior {

		/** @param event the Event to handle
		 *  @param popup the Popup this Behavior is attached to */
		boolean show(Event event, Popup popup);

		/** @param event the Event to handle
		 *  @param popup the Popup this Behavior is attached to */
		boolean hide(Event event, Popup popup);

		/** @param event the Event to handle
		 *  @param popup the Popup this Behavior is attached to
		 *  @return what to do */
		Reaction handle(Event event, Popup popup);

		/** Does nothing. Override this if you want to override only some methods.
		 *  @author dermetfan */
		public static class Adapter implements Behavior {

			/** @return {@code true} */
			@Override
			public boolean show(Event event, Popup popup) {
				return false;
			}

			/** @return {@code false} */
			@Override
			public boolean hide(Event event, Popup popup) {
				return false;
			}

			/** @return {@code null} */
			@Override
			public Reaction handle(Event event, Popup popup) {
				return null;
			}

		}

	}

	/** a Multiplexer for Behaviors
	 *  @author dermetfan */
	public static class BehaviorMultiplexer extends Multiplexer<Behavior> implements Behavior {

		public BehaviorMultiplexer() {}

		public BehaviorMultiplexer(int size) {
			super(size);
		}

		public BehaviorMultiplexer(Behavior... receivers) {
			super(receivers);
		}

		public BehaviorMultiplexer(Array<Behavior> receivers) {
			super(receivers);
		}

		/** @return whether any of the Behaviors handled the event */
		@Override
		public boolean show(Event event, Popup popup) {
			boolean handled = false;
			for(Behavior behavior : receivers)
				handled |= behavior.show(event, popup);
			return handled;
		}

		/** @return whether any of the Behaviors handled the event */
		@Override
		public boolean hide(Event event, Popup popup) {
			boolean handled = false;
			for(Behavior behavior : receivers)
				handled |= behavior.hide(event, popup);
			return handled;
		}

		/** Calls {@link Behavior#handle(Event, Popup)} on all Behaviors in order and returns the first returned non-null Reaction if it {@link Reaction#handles handles} the event.
		 *  If it does not handle the event but another Reaction does, the handling version of the Reaction is returned:
		 *  <table summary="handling and non-handling Reaction pairs">
		 *      <tr>
		 *          <th>non-handling</th>
		 *          <th>handling</th>
		 *      </tr>
		 *      <tr>
		 *          <td>Show</td>
		 *          <td>ShowHandle</td>
		 *      </tr>
		 *      <tr>
		 *          <td>Hide</td>
		 *          <td>HideHandle</td>
		 *      </tr>
		 *      <tr>
		 *          <td>None</td>
		 *          <td>Handle</td>
		 *      </tr>
		 *  </table>
		 *  @return the first Reaction or its {@link Reaction#handles handling} version if any Behavior handles the event, or null */
		@Override
		public Reaction handle(Event event, Popup popup) {
			Reaction reaction = null;
			boolean handled = false;
			for(int i = 0; i < receivers.size - 1; i++) {
				Reaction itsReaction = receivers.get(i).handle(event, popup);
				if(reaction == null)
					reaction = itsReaction;
				if(!handled && itsReaction != null && itsReaction.handles)
					handled = true;
			}
			if(handled && !reaction.handles)
				switch(reaction) {
				case Show:
					return Reaction.ShowHandle;
				case Hide:
					return Reaction.HideHandle;
				default:
					assert false;
				case None:
					return Reaction.Handle;
				}
			return reaction;
		}

	}

	/** adds the popup to the {@link Event#getStage() Event's Stage} in {@link #show(Event, Popup)} if it is on no or another Stage
	 *  @author dermetfan */
	public static class AddToStageBehavior extends Behavior.Adapter {

		@Override
		public boolean show(Event event, Popup popup) {
			if(popup.getPopup().getStage() != event.getStage())
				event.getStage().addActor(popup.getPopup());
			return super.show(event, popup);
		}

	}

	/** shows and hides the popup by settings its visibility
	 *  @author dermetfan */
	public static class VisibilityBehavior extends Behavior.Adapter {

		/** calls {@link Actor#setVisible(boolean) setVisible(true)} and {@link Actor#toFront() toFront()} on the {@link Popup#popup} */
		@Override
		public boolean show(Event event, Popup popup) {
			popup.getPopup().setVisible(true);
			popup.getPopup().toFront();
			return true;
		}

		/** calls {@link Actor#setVisible(boolean) setVisible(false)} on the {@link Popup#popup} */
		@Override
		public boolean hide(Event event, Popup popup) {
			popup.getPopup().setVisible(false);
			return false;
		}

	}

	/** fades in/out in {@link #show(Event, Popup)}/{@link #hide(Event, Popup)} using {@link com.badlogic.gdx.scenes.scene2d.actions.AlphaAction AlphaActions}
	 *  @author dermetfan */
	public static class FadeBehavior extends Behavior.Adapter {

		/** the fade duration (default is 0.4) */
		private float fadeInDuration = .4f, fadeOutDuration = .4f;

		/** the fade interpolation (default is {@code Interpolation.fade}) */
		private Interpolation fadeInInterpolation = Interpolation.fade, fadeOutInterpolation = Interpolation.fade;

		/** creates a FadeBehavior with the default values */
		public FadeBehavior() {}

		/** @param fadeDuration the {@link #fadeInDuration} and {@code #fadeOutDuration} */
		public FadeBehavior(float fadeDuration) {
			this(fadeDuration, fadeDuration);
		}

		/** @param fadeInterpolation the  */
		public FadeBehavior(Interpolation fadeInterpolation) {
			this(fadeInterpolation, fadeInterpolation);
		}

		/** @param fadeDuration the {@link #fadeInDuration} and {@link #fadeOutDuration}
		 *  @param fadeInterpolation the {@link #fadeInInterpolation} and {@link #fadeOutInterpolation} */
		public FadeBehavior(float fadeDuration, Interpolation fadeInterpolation) {
			this(fadeDuration, fadeDuration, fadeInterpolation, fadeInterpolation);
		}

		/** @param fadeInDuration the {@link #fadeInDuration}
		 *  @param fadeOutDuration the {@link #fadeOutDuration} */
		public FadeBehavior(float fadeInDuration, float fadeOutDuration) {
			this.fadeInDuration = fadeInDuration;
			this.fadeOutDuration = fadeOutDuration;
		}

		/** @param fadeInInterpolation the {@link #fadeInInterpolation}
		 *  @param fadeOutInterpolation the {@link #fadeOutInterpolation} */
		public FadeBehavior(Interpolation fadeInInterpolation, Interpolation fadeOutInterpolation) {
			this.fadeInInterpolation = fadeInInterpolation;
			this.fadeOutInterpolation = fadeOutInterpolation;
		}

		/** @param fadeInDuration the {@link #fadeInDuration}
		 *  @param fadeOutDuration the {@link #fadeOutDuration}
		 *  @param fadeInInterpolation the {@link #fadeInInterpolation}
		 *  @param fadeOutInterpolation the {@link #fadeOutInterpolation} */
		public FadeBehavior(float fadeInDuration, float fadeOutDuration, Interpolation fadeInInterpolation, Interpolation fadeOutInterpolation) {
			this.fadeInDuration = fadeInDuration;
			this.fadeOutDuration = fadeOutDuration;
			this.fadeInInterpolation = fadeInInterpolation;
			this.fadeOutInterpolation = fadeOutInterpolation;
		}

		@Override
		public boolean show(Event event, Popup popup) {
			popup.getPopup().toFront();
			popup.getPopup().addAction(Actions.sequence(Actions.visible(true), Actions.fadeIn(fadeInDuration, fadeInInterpolation)));
			return super.show(event, popup);
		}

		@Override
		public boolean hide(Event event, Popup popup) {
			popup.getPopup().addAction(Actions.sequence(Actions.fadeOut(fadeOutDuration, fadeOutInterpolation), Actions.visible(false)));
			return super.hide(event, popup);
		}

		// getters and setters

		/** @return the {@link #fadeInDuration} */
		public float getFadeInDuration() {
			return fadeInDuration;
		}

		/** @param fadeInDuration the {@link #fadeInDuration} to set */
		public void setFadeInDuration(float fadeInDuration) {
			this.fadeInDuration = fadeInDuration;
		}

		/** @return the {@link #fadeOutDuration} */
		public float getFadeOutDuration() {
			return fadeOutDuration;
		}

		/** @param fadeOutDuration the {@link #fadeOutDuration} to set */
		public void setFadeOutDuration(float fadeOutDuration) {
			this.fadeOutDuration = fadeOutDuration;
		}

		/** @return the {@link #fadeInDuration} */
		public Interpolation getFadeInInterpolation() {
			return fadeInInterpolation;
		}

		/** @param fadeInInterpolation the {@link #fadeInInterpolation} to set */
		public void setFadeInInterpolation(Interpolation fadeInInterpolation) {
			this.fadeInInterpolation = fadeInInterpolation;
		}

		/** @return the {@link #fadeOutInterpolation} */
		public Interpolation getFadeOutInterpolation() {
			return fadeOutInterpolation;
		}

		/** @param fadeOutInterpolation the {@link #fadeOutInterpolation} to set */
		public void setFadeOutInterpolation(Interpolation fadeOutInterpolation) {
			this.fadeOutInterpolation = fadeOutInterpolation;
		}

	}

	/** The behavior of a menu such as a menu bar or context menu. Shows and hides but does not position the popup. Add this to the Popup of the Actor that the user should be able to click.
	 *  <strong>Note that this can only hide on events of other actors if it receives them, so consider adding all your menus to an {@link net.dermetfan.gdx.scenes.scene2d.EventMultiplexer EventMultiplexer} high up in the hierarchy (e.g. added to the {@link com.badlogic.gdx.scenes.scene2d.Stage Stage}).</strong>
	 *  @author dermetfan */
	public static class MenuBehavior extends Behavior.Adapter {

		/** Bit mask of {@link Buttons} that trigger {@link Reaction#ShowHandle}. Default is {@code 1 << Buttons.LEFT}. */
		private int showButtons = 1 << Buttons.LEFT;

		public MenuBehavior() {}

		/** @param showButtons the buttons to call {@link #showOn(int)} with */
		public MenuBehavior(int... showButtons) {
			for(int button : showButtons)
				showOn(button);
		}

		/** {@link Reaction#ShowHandle Shows} on {@link #showButtons} click and menu key press. Hides on all other clicks, escape key and back key.
		 * 	Note that this will not hide on clicks on other actors except the {@link Event#getListenerActor()}'s children. */
		@Override
		public Reaction handle(Event e, Popup popup) {
			if(!(e instanceof InputEvent))
				return Reaction.None;
			InputEvent event = (InputEvent) e;
			switch(event.getType()) {
			case touchDown:
				if((1 << event.getButton() & showButtons) == showButtons && event.getTarget().getListeners().contains(popup, true))
					return Reaction.ShowHandle;
				else if(!popup.isAscendantOf(event.getTarget())) // don't hide on clicks on this or child popups
					return Reaction.Hide;
			case keyDown:
				if(event.getKeyCode() == Keys.MENU && event.getTarget().getListeners().contains(popup, true)) // menu key shows
					return Reaction.ShowHandle;
				else if(event.getKeyCode() == Keys.ESCAPE || event.getKeyCode() == Keys.BACK) // escape and back hide
					return Reaction.HideHandle;
			}
			return null;
		}

		/** @param button the {@link Buttons button} on which {@link InputEvent.Type#touchDown click} to {@link #show(Event, Popup) show}
		 *  @return the new value of {@link #showButtons} */
		public int showOn(int button) {
			return showButtons |= 1 << button;
		}

		/** @param button the {@link Buttons button} on which {@link InputEvent.Type#touchDown click} not to {@link #show(Event, Popup) show}
		 *  @return the new value of {@link #showButtons} */
		public int showNotOn(int button) {
			return showButtons &= ~(1 << button);
		}

		// getters and setters

		/** @return the {@link #showButtons} */
		public int getShowButtons() {
			return showButtons;
		}

		/** @param showButtons the {@link #showButtons} to set */
		public void setShowButtons(int showButtons) {
			this.showButtons = showButtons;
		}

	}

	/** The Behavior of a classic tooltip. Does nothing in {@link #show(Event, Popup)} and {@link #hide(Event, Popup)}. Hides on {@link Keys#ESCAPE escape}.
	 *  Add the Popup using this TooltipBehavior to an {@link net.dermetfan.gdx.scenes.scene2d.EventMultiplexer EventMultiplexer} high in the hierarchy (e.g. on the Stage) to make sure events on other Actors are received so that the TooltipBehavior can hide properly.
	 *  @author dermetfan */
	public static class TooltipBehavior extends Behavior.Adapter {

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

		// getters and setters

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
		 *  @author dermetfan */
		public static class TooltipPositionBehavior extends PositionBehavior {

			/** whether {@link Type#mouseMoved mouseMoved} events should apply the position */
			private boolean followPointer;

			public TooltipPositionBehavior(Position position) {
				super(position);
			}

			/** @param followPointer the {@link #followPointer} */
			public TooltipPositionBehavior(Position position, boolean followPointer) {
				super(position);
				this.followPointer = followPointer;
			}

			@Override
			public Reaction handle(Event event, Popup popup) {
				if(followPointer && event instanceof InputEvent && ((InputEvent) event).getType() == Type.mouseMoved)
					getPosition().apply(event, popup.getPopup());
				return super.handle(event, popup);
			}

			// getters and setters

			/** @return the {@link #followPointer} */
			public boolean isFollowPointer() {
				return followPointer;
			}

			/** @param followPointer the {@link #followPointer} to set */
			public void setFollowPointer(boolean followPointer) {
				this.followPointer = followPointer;
			}

		}

	}

	/** sets the position of the popup in {@link #show(Event, Popup)}
	 *  @author dermetfan */
	public static class PositionBehavior extends Behavior.Adapter {

		/** the Position to {@link Position#apply(Event, Actor) apply} */
		private Position position;

		/** @param position the {@link #position} */
		public PositionBehavior(Position position) {
			this.position = position;
		}

		/** @param positions the Positions to create a {@link PositionMultiplexer} for */
		public PositionBehavior(Position... positions) {
			this(new PositionMultiplexer(positions));
		}

		/** @param popup the popup which position to set */
		@Override
		public boolean show(Event event, Popup popup) {
			position.apply(event, popup.getPopup());
			return super.show(event, popup);
		}

		// getters and setters

		/** @return the {@link #position} */
		public Position getPosition() {
			return position;
		}

		/** @param position the {@link #position} to set */
		public void setPosition(Position position) {
			this.position = position;
		}

		/** determines and applies a position
		 *  @author dermetfan */
		public interface Position {

			/** @param event the event
			 *  @param popup the popup which position to set */
			void apply(Event event, Actor popup);

		}

		/** a Multiplexer for Positions
		 *  @author dermetfan */
		public static class PositionMultiplexer extends Multiplexer<Position> implements Position {

			public PositionMultiplexer() {}

			public PositionMultiplexer(int size) {
				super(size);
			}

			public PositionMultiplexer(Position... receivers) {
				super(receivers);
			}

			public PositionMultiplexer(Array<Position> receivers) {
				super(receivers);
			}

			@Override
			public void apply(Event event, Actor popup) {
				for(Position position : receivers)
					position.apply(event, popup);
			}

		}

		/** the position of a pointer
		 *  @author dermetfan */
		public static class PointerPosition implements Position {

			/** the pointer which position to resolve */
			private int pointer;

			/** resolves pointer 0 */
			public PointerPosition() {}

			/** @param pointer the {@link #pointer} */
			public PointerPosition(int pointer) {
				this.pointer = pointer;
			}

			@Override
			public void apply(Event event, Actor popup) {
				Vector2 pos = Scene2DUtils.pointerPosition(event.getStage(), pointer);
				if(popup.hasParent())
					popup.getParent().stageToLocalCoordinates(pos);
				popup.setPosition(pos.x, pos.y);
			}

			// getters and setters

			/** @return the {@link #pointer} */
			public int getPointer() {
				return pointer;
			}

			/** @param pointer the {@link #pointer} to set */
			public void setPointer(int pointer) {
				this.pointer = pointer;
			}

		}

		/** a preset position
		 *  @author dermetfan */
		public static class PresetPosition implements Position {

			/** the position to {@link #apply(Event, Actor)} */
			private float x, y;

			public PresetPosition() {}

			/** @param x the {@link #x}
			 *  @param y the {@link #y} */
			public PresetPosition(float x, float y) {
				this.x = x;
				this.y = y;
			}

			@Override
			public void apply(Event event, Actor popup) {
				popup.setPosition(x, y);
			}

			// getters and setters

			/** @return the {@link #x} */
			public float getX() {
				return x;
			}

			/** @param x the {@link #x} to set */
			public void setX(float x) {
				this.x = x;
			}

			/** @return the {@link #y} */
			public float getY() {
				return y;
			}

			/** @param y the {@link #y} to set */
			public void setY(float y) {
				this.y = y;
			}

		}

		/** offsets the popup by a certain amount
		 *  @author dermetfan */
		public static class OffsetPosition implements Position {

			/** the offset */
			private float x, y;

			/** @param x the {@link #x}
			 *  @param y the {@link #y} */
			public OffsetPosition(float x, float y) {
				this.x = x;
				this.y = y;
			}

			@Override
			public void apply(Event event, Actor popup) {
				popup.setPosition(popup.getX() + x, popup.getY() + y);
			}

			// getters and setters

			/** @return the {@link #x} */
			public float getX() {
				return x;
			}

			/** @param x the {@link #x} to set */
			public void setX(float x) {
				this.x = x;
			}

			/** @return the {@link #y} */
			public float getY() {
				return y;
			}

			/** @param y the {@link #y} to set */
			public void setY(float y) {
				this.y = y;
			}

		}

		/** The position of the event if it is an {@link InputEvent}. The position is composed of {@link InputEvent#getStageX()} and {@link InputEvent#getStageY()}.
		 *  @author dermetfan */
		public static class EventPosition implements Position {

			@Override
			public void apply(Event event, Actor popup) {
				if(event instanceof InputEvent) {
					InputEvent inputEvent = (InputEvent) event;
					Vector2 pos = Pools.obtain(Vector2.class);
					pos.set(inputEvent.getStageX(), inputEvent.getStageY());
					if(popup.hasParent())
						popup.getParent().stageToLocalCoordinates(pos);
					popup.setPosition(pos.x, pos.y);
					Pools.free(pos);
				} else
					popup.setPosition(Float.NaN, Float.NaN);
			}

		}

		/** position aligned relative to {@link Event#getTarget() target}
		 *  @author dermetfan */
		public static class AlignPosition implements Position {

			/** the {@link Align} flag for alignment on {@link Event#getTarget()} */
			private int targetAlign;

			/** the {@link Align} flag */
			private int align;

			/** @param align the {@link #align} */
			public AlignPosition(int targetAlign, int align) {
				this.targetAlign = targetAlign;
				this.align = align;
			}

			@Override
			public void apply(Event event, Actor popup) {
				Actor target = event.getTarget();
				Vector2 pos = Pools.obtain(Vector2.class).setZero();
				pos.set(Scene2DUtils.align(target.getWidth(), target.getHeight(), targetAlign));
				target.localToStageCoordinates(pos);
				popup.stageToLocalCoordinates(pos);
				popup.localToParentCoordinates(pos);
				popup.setPosition(pos.x, pos.y, align);
				Pools.free(pos);
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

			/** @return the {@link #targetAlign} */
			public int getTargetAlign() {
				return targetAlign;
			}

			/** @param targetAlign the {@link #targetAlign} to set */
			public void setTargetAlign(int targetAlign) {
				this.targetAlign = targetAlign;
			}

		}

		/** offsets the position by aligning it using the popup's size
		 *  @author dermetfan */
		public static class AlignedOffsetPosition implements Position {

			/** the {@link com.badlogic.gdx.scenes.scene2d.utils.Align Align} flag */
			private int align;

			/** @param align the {@link #align} */
			public AlignedOffsetPosition(int align) {
				this.align = align;
			}

			@Override
			public void apply(Event event, Actor popup) {
				Vector2 offset = Scene2DUtils.align(popup.getWidth(), popup.getHeight(), align);
				popup.setPosition(popup.getX() - offset.x, popup.getY() - offset.y);
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

}
