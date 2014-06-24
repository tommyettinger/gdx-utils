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

import java.lang.Override;import com.badlogic.gdx.Input.Buttons;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import net.dermetfan.utils.libgdx.scene2d.Scene2DUtils;

/** Shows an actor at the mouse position on right click and menu key press. Hides on left click, escape key and back key.
 * 	Note that as {@link com.badlogic.gdx.scenes.scene2d.EventListener EventListener}, this will not hide on clicks on other actors except its children.
 * 	@see TargetedContextPopup
 *  @author dermetfan */
public class ContextPopup<T extends Actor> extends Popup<T> implements EventListener {

	/** the offset from the pointer position */
	private float offsetX, offsetY;

	/** @see Popup#Popup(Actor)  */
	public ContextPopup(T popup) {
		super(popup);
	}

	/** {@link #show(Event) Shows} on right click and the {@link Keys#MENU menu} key, {@link #hide(Event) hides} on left click. */
	@Override
	public boolean handle(Event e) {
		if(!(e instanceof InputEvent))
			return false;
		InputEvent event = (InputEvent) e;
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

	/** {@link Popup#show(Event) Shows} the {@link #popup} under the pointer, offset by {@link #offsetX} and {@link #offsetY}. Also adds it to the stage of the event if it has no stage. */
	@Override
	public boolean show(Event event) {
		if(popup.getStage() == null)
			event.getStage().addActor(popup);
		Vector2 pos = Scene2DUtils.pointerPosition(event.getStage());
		if(popup.hasParent() && popup.getParent() != event.getStage().getRoot())
			event.getStage().getRoot().localToDescendantCoordinates(popup.getParent(), pos);
		pos.add(offsetX, offsetY - popup.getHeight());
		popup.setPosition(pos.x, pos.y);
		return super.show(event);
	}

	// getters and setters

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

}
