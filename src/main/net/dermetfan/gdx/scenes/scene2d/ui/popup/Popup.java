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

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import net.dermetfan.gdx.scenes.scene2d.ui.popup.Popup.Behavior.Reaction;

/** Shows {@link #popup} on certain events.
 *  @param <T> the type of {@link #popup}
 *  @author dermetfan */
public class Popup<T extends Actor> implements EventListener {

	/** @param popup the possibly ascendant popup
	 *  @param target the event target
	 *  @return whether the given Actor is the {@link Popup#popup popup} of the given or Popup or a child {@link Popup} of the given Popup */
	public static boolean isAscendantOf(Popup popup, Actor target) {
		if(popup.getPopup() == target)
			return true;
		for(EventListener listener : popup.getPopup().getListeners()) {
			if(listener instanceof Popup) {
				if(isAscendantOf((Popup) listener, target))
					return true;
			}
		}
		return false;
	}

	/** the {@code T} to pop up */
	private T popup;

	/** the Behavior to delegate to */
	private Behavior behavior;

	/** if the context menu is currently showing */
	private boolean showing;

	/** @param popup the {@link #popup}
	 *  @param behavior the {@link #behavior}*/
	public Popup(T popup, Behavior behavior) {
		this.popup = popup;
		this.behavior = behavior;
	}

	/** Makes the {@link #popup} {@link Actor#setVisible(boolean) visible} and brings it to {@link Actor#toFront() front}. Override this for custom behaviour.
	 *  @return if the event is handled, true by default (suggestive) */
	public boolean show(Event event) {
		if(!showing) {
			showing = true;
			return behavior.show(event, this);
		}
		return false;
	}

	/** Makes the {@link #popup} {@link Actor#setVisible(boolean) invisible}. Override this for custom behavior.
	 *  @return if the event is handled, false by default (suggestive) */
	public boolean hide(Event event) {
		if(showing) {
			showing = false;
			return behavior.hide(event, this);
		}
		return false;
	}

	/** @see Behavior#handle(Event) */
	@Override
	public boolean handle(Event event) {
		Reaction reaction = behavior.handle(event, this);
		if(reaction == null)
			reaction = Reaction.None;
		switch(reaction) {
		case ShowHandle:
			show(event);
			return true;
		case Show:
			show(event);
			return false;
		case HideHandle:
			hide(event);
			return true;
		case Hide:
			hide(event);
			return false;
		case Handle:
			return true;
		}
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

	/** @return the {@link #showing} */
	public boolean isShowing() {
		return showing;
	}

	/** what to do in the Popup methods
	 *  @since 0.8.0
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

		/** @since 0.8.0
		 *  @author dermetfan
		 *  @see {@link Behavior#handle(Event)} */
		public enum Reaction {

			/** call {@link Popup#show(Event)} and {@link Event#handle()} */
			ShowHandle,

			/** call {@link Popup#show(Event)} */
			Show,

			/** call {@link Popup#hide(Event)} and {@link Event#handle()} */
			HideHandle,

			/** call {@link Popup#hide(Event)} */
			Hide,

			/** call {@link Event#handle()} */
			Handle,

			/** do nothing */
			None

		}

		/** Does nothing. Override this if you want to override only some methods.
		 *  @since 0.8.0
		 *  @author dermetfan */
		public class Adapter implements Behavior {

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

}
