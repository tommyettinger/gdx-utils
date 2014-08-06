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

import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.utils.Array;
import net.dermetfan.utils.Function;

/** Shows the {@link #popup} on events. Behavior is defined by a {@link #handler}.
 *  @author dermetfan */
public class ImmutableEventPopup<T extends Actor> extends Popup<T> implements EventListener {

	/** A handler that solves the {@link #localHandler}'s problem of not hiding on certain events on other actors (except children) by holding {@link #targets}.
	 *  Using this, the {@code ContextPopup} is supposed to be added to an ancestor high up above {@link #targets} in the hierarchy tree, e.g. the Stage itself.
	 *  Will {@link #show(Event) show} only on events which {@link Event#getTarget() target} the {@link #targets} and {@link #hide(Event) hide} otherwise (except the {@link #popup} itself is the event target).
	 *  @see #localHandler
	 *  @author dermetfan */
	public class TargetedHandler implements Function<Boolean, InputEvent> {

		/** the actors that are applicable for this context menu */
		private final Array<Actor> targets;

		/** @param target the {@link #targets target} */
		public TargetedHandler(Actor target) {
			targets = new Array<>(1);
			targets.add(target);
		}

		/** @param targets the {@link #targets} */
		public TargetedHandler(Actor... targets) {
			this.targets = new Array<>(targets);
		}

		/** @param targets the {@link #targets} (will be copied) */
		public TargetedHandler(Array<Actor> targets) {
			this.targets = new Array<>(targets);
		}

		/** {@link #hide(Event) hides} the popup if the {@link Event#getTarget() event target} is not a {@link #targets target} or the {@link #popup}, calls its super method otherwise */
		@Override
		public Boolean apply(InputEvent event) {
			if((event.getType() == Type.touchDown || event.getType() == Type.keyDown) && !targets.contains(event.getTarget(), true))
				return event.getTarget() != popup && hide(event);
			return localHandler.apply(event);
		}

		/** {@link Array#clear() clears} the {@link #targets} */
		public void clearTargets() {
			targets.clear();
		}

		/** @param target the target to add */
		public void addTarget(Actor target) {
			targets.add(target);
		}

		/** @param target the target to remove
		 *  @return see {@link Array#removeValue(Object, boolean)} */
		public boolean removeTarget(Actor target) {
			return targets.removeValue(target, true);
		}

		// getters and setters

		/** @param targets the targets to add to {@link #targets} after {@link Array#clear() clearing} */
		public final void setTargets(Actor... targets) {
			this.targets.clear();
			this.targets.addAll(targets);
		}

		/** @return the {@link #targets} */
		public Array<Actor> getTargets() {
			return targets;
		}

		/** @param targets the targets to add to {@link #targets} after {@link Array#clear() clearing} */
		public void setTargets(Array<Actor> targets) {
			this.targets.clear();
			this.targets.addAll(targets);
		}

	}

	/** Shows the popup on right click and menu key press. Hides on left click, escape key and back key.
	 * 	Note that this will not hide on clicks on other actors except the {@link Event#getListenerActor()}'s children.
	 *  @see TargetedHandler */
	public final Function<Boolean, InputEvent> localHandler = new Function<Boolean, InputEvent>() {
		@Override
		public Boolean apply(InputEvent event) {
			switch(event.getType()) {
			case touchDown:
				if(event.getButton() == Buttons.RIGHT)
					return show(event);
				else if(event.getButton() == Buttons.LEFT)
					return hide(event);
				break;
			case keyDown:
				if(event.getKeyCode() == Keys.MENU)
					return show(event);
				else if(event.getKeyCode() == Keys.ESCAPE || event.getKeyCode() == Keys.BACK)
					return hide(event);
			}
			return false;
		}
	};

	/** {@link #handle(Event) Handles} the {@link Event} if it is an {@link InputEvent}.
	 *  Should return the return value for {@link EventListener#handle(Event)}.<br>
	 *  <strong>Important:</strong> There is no getter or setter for this so that every subclass may decide if its behavior can be changed (by implementing them) or not.
	 *  @see EventPopup
	 *  @see #handle(Event) */
	protected Function<Boolean, InputEvent> handler = localHandler;

	/** @see Popup#Popup(Actor)  */
	public ImmutableEventPopup(T popup) {
		super(popup);
	}

	/** Makes use of {@link #handler} if the given event is an {@link InputEvent}. */
	@Override
	public boolean handle(Event e) {
		if(!(e instanceof InputEvent))
			return false;
		return handler.apply((InputEvent) e);
	}

}
