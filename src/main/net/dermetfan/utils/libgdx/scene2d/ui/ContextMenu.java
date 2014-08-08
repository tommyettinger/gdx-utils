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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import net.dermetfan.utils.libgdx.scene2d.Scene2DUtils;

/** A classic context menu. Add this to the actor that the user should be able to right-click.
 *  <strong>Note that this can only hide on events of other actors if it receives them, so consider adding all your context menus to one {@link net.dermetfan.utils.libgdx.scene2d.EventMultiplexer EventMultiplexer} high up in the hierarchy (e.g. added to the {@link com.badlogic.gdx.scenes.scene2d.Stage Stage}).</strong>
 *  @author dermetfan
 *  @since 0.4.0 */
public class ContextMenu<T extends Actor> extends PositionedPopup<T> {

	/** classic position of context menus on the right of the actor
	 *  @author dermetfan
	 *  @since 0.4.0 */
	public class ContextMenuPosition implements Position {

		/** @return {@link Scene2DUtils#tmp} */
		@Override
		public Vector2 apply(Event event) {
			Actor parentMenu = event.getTarget();
			Vector2 pos = Scene2DUtils.positionInStageCoordinates(parentMenu);
			pos.x += parentMenu.getWidth();
			pos.y -= getPopup().getHeight() - parentMenu.getHeight();
			return pos;
		}

	}

	/** creates a new {@code ContextMenu} with {@link ContextMenuPosition} */
	public ContextMenu(T popup) {
		super(popup, null);
		setPosition(new ContextMenuPosition());
	}

	/** {@link #show(Event) Shows} on right click and menu key press. Hides on left click, escape key and back key.
	 * 	Note that this will not hide on clicks on other actors except the {@link Event#getListenerActor()}'s children. */
	@Override
	public boolean handle(Event e) {
		if(!(e instanceof InputEvent))
			return false;
		InputEvent event = (InputEvent) e;

		boolean onListenerActor = event.getListenerActor().isAscendantOf(event.getTarget());
		switch(event.getType()) {
		case touchDown:
			if(!onListenerActor && !getPopup().isAscendantOf(event.getTarget())) {
				hide(e);
				break;
			}
			if(event.getButton() == Buttons.RIGHT)
				return show(event);
			else if(event.getButton() == Buttons.LEFT)
				return hide(event);
			break;
		case keyDown:
			if(onListenerActor && event.getKeyCode() == Keys.MENU)
				return show(event);
			else if(event.getKeyCode() == Keys.ESCAPE || event.getKeyCode() == Keys.BACK)
				return hide(event);
		}
		return false;
	}

}
