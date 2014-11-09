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

import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import net.dermetfan.gdx.scenes.scene2d.EventMultiplexer;

/** The behavior of a classic context menu. Shows and hides but does not position the popup. Add this to the Popup of the actor that the user should be able to right-click.
 *  <strong>Note that this can only hide on events of other actors if it receives them, so consider adding all your context menus to an {@link EventMultiplexer} high up in the hierarchy (e.g. added to the {@link com.badlogic.gdx.scenes.scene2d.Stage Stage}).</strong>
 *  @author dermetfan
 *  @since 0.8.0 */
public class ContextMenuBehavior extends BasicBehavior {

	/** The Actor this context menu is added to. Needed to hide on events on other Actors. */
	private Actor target;

	/** @param target the {@link #target} */
	public ContextMenuBehavior(Actor target) {
		this.target = target;
	}

	/** {@link Reaction#ShowHandle Shows} on right click and menu key press. Hides on left click, escape key and back key.
	 * 	Note that this will not hide on clicks on other actors except the {@link Event#getListenerActor()}'s children. */
	@Override
	public Reaction handle(Event e, Popup popup) {
		if(!(e instanceof InputEvent))
			return Reaction.None;
		InputEvent event = (InputEvent) e;
		switch(event.getType()) {
		case touchDown:
			if(event.getButton() == Buttons.RIGHT && event.getTarget() == target)
				return Reaction.ShowHandle; // right click shows
			else if(!Popup.isAscendantOf(popup, event.getTarget())) // don't hide on clicks on this or child popups
				return Reaction.Hide; // other clicks hide
		case keyDown:
			if(event.getKeyCode() == Keys.MENU && event.getTarget() == target) // menu key shows
				return Reaction.ShowHandle;
			else if(event.getKeyCode() == Keys.ESCAPE || event.getKeyCode() == Keys.BACK) // escape and back hide
				return Reaction.HideHandle;
		}
		return null;
	}

	// getters and setters

	/** @return the {@link #target} */
	public Actor getTarget() {
		return target;
	}

	/** @param target the {@link #target} to set */
	public void setTarget(Actor target) {
		this.target = target;
	}

}
