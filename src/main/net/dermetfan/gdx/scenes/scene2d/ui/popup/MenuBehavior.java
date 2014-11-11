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
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import net.dermetfan.gdx.scenes.scene2d.EventMultiplexer;
import net.dermetfan.gdx.scenes.scene2d.ui.popup.Popup.Behavior;

/** The behavior of a menu such as a menu bar or context menu. Shows and hides but does not position the popup. Add this to the Popup of the Actor that the user should be able to click.
 *  <strong>Note that this can only hide on events of other actors if it receives them, so consider adding all your menus to an {@link EventMultiplexer} high up in the hierarchy (e.g. added to the {@link com.badlogic.gdx.scenes.scene2d.Stage Stage}).</strong>
 *  @author dermetfan
 *  @since 0.8.0 */
public class MenuBehavior extends Behavior.Adapter {

	/** Bit mask of {@link Buttons} that trigger {@link Reaction#ShowHandle}. Default is {@code 1 << Buttons.LEFT}. */
	private int showButtons = 1 << Buttons.LEFT;

	public MenuBehavior() {}

	/** @param showButtons the {@link #showButtons} */
	public MenuBehavior(int showButtons) {
		this.showButtons = showButtons;
	}

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
			else if(!Popup.isAscendantOf(popup, event.getTarget())) // don't hide on clicks on this or child popups
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
