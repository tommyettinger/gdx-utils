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
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.utils.Array;
import net.dermetfan.utils.libgdx.Multiplexer;
import net.dermetfan.utils.libgdx.scene2d.Scene2DUtils;

/** A classic context menu. Add this to the actor that the user should be able to right-click.
 *  <strong>Note that this can only hide on events of other actors if it receives them, so consider adding all your context menus to a {@link Manager} high up in the hierarchy (e.g. added to the {@link com.badlogic.gdx.scenes.scene2d.Stage Stage}).</strong>
 *  @author dermetfan
 *  @since 0.4.0 */
public class ContextMenu<T extends Actor> extends PositionedPopup<T> {

	/** Hides all its {@link #receivers context menus} when escape is pressed or something is touched (except a {@link ContextMenu#popup} or its children).
	 *  Meant to be added added far up in the hierarchy (e.g. the {@link com.badlogic.gdx.scenes.scene2d.Stage Stage} itself) so it can hide on events on possibly all actors.
	 *  @author dermetfan
	 *  @since 0.4.0 */
	public static class Manager extends Multiplexer<ContextMenu> implements EventListener {

		/** @see Multiplexer#Multiplexer(Object[]) */
		public Manager(ContextMenu... receivers) {
			super(receivers);
		}

		/** @see Multiplexer#Multiplexer(Array)  */
		public Manager(Array<ContextMenu> receivers) {
			super(receivers);
		}

		/** Hides {@link #receivers} on {@link Keys#ESCAPE} and {@link InputEvent.Type#touchDown} events on actors that are not their {@link ContextMenu#popup popups} or its children. */
		@Override
		public boolean handle(final Event event) {
			if(event instanceof InputEvent) {
				InputEvent ie = (InputEvent) event;
				switch(ie.getType()) {
				case keyDown:
					if(ie.getKeyCode() == Keys.ESCAPE) // escape hides all
						for(ContextMenu menu : receivers)
							menu.hide(event);
					break;
				case touchDown:
					if(ie.getButton() == Buttons.LEFT) { // ContextMenus probably hid itself, so show again if they had parents
						for(ContextMenu menu : receivers)
							if(event.getTarget().isDescendantOf(menu.getPopup())) { // target had parents
								for(EventListener listener : event.getTarget().getListeners()) // show menus again
									if(listener instanceof ContextMenu)
										((ContextMenu) listener).show(event);
							} else // target didn't have parents or a context menu, so hide
								menu.hide(event);
					} else {
						for(ContextMenu menu : receivers) // don't hide if popup or one of its parents was touched
							if(event.getTarget().getListeners().contains(menu, true) || menu.getPopup().isAscendantOf(event.getTarget()))
								return false;
						for(ContextMenu menu : receivers)
							menu.hide(event);
					}
				}
			}
			return false;
		}

	}

	/** Classic position of context menus on the right of the actor.<br>
	 *  Positions the left border of the {@link #popup} at the right border of the {@link Event#getTarget() target} and aligns their top borders.
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

	/** Positions the top border of the {@link #popup} at the bottom border of the {@link Event#getTarget() target}.
	 *  @author dermetfan
	 *  @since 0.4.0 */
	public class BelowPosition implements Position {

		/** @return {@link Scene2DUtils#tmp} */
		@Override
		public Vector2 apply(Event event) {
			return Scene2DUtils.positionInStageCoordinates(event.getTarget()).sub(0, getPopup().getHeight());
		}

	}

	/** creates a new {@code ContextMenu} with {@link ContextMenuPosition} */
	public ContextMenu(T popup) {
		super(popup, null);
		setPosition(new ContextMenuPosition());
	}

	/** @see PositionedPopup#PositionedPopup(Actor, Position) */
	public ContextMenu(T popup, Position position) {
		super(popup, position);
	}

	/** @param manager the {@link Manager} to add this ContextMenu to (may be null) */
	public ContextMenu(T popup, Manager manager) {
		this(popup);
		if(manager != null)
			manager.add(this);
	}

	/** @param manager the {@link Manager} to add this {@code ContextMenu} to (may be null) */
	public ContextMenu(T popup, Position position, Manager manager) {
		super(popup, position);
		if(manager != null)
			manager.add(this);
	}

	/** {@link #show(Event) Shows} on right click and menu key press. Hides on left click, escape key and back key.
	 * 	Note that this will not hide on clicks on other actors except the {@link Event#getListenerActor()}'s children. */
	@Override
	public boolean handle(Event e) {
		if(!(e instanceof InputEvent))
			return false;
		InputEvent event = (InputEvent) e;
		switch(event.getType()) {
		case touchDown:
			return event.getButton() == Buttons.RIGHT ? show(event) : hide(event); // right shows, left hides
		case keyDown:
			if(event.getKeyCode() == Keys.MENU) // menu key shows
				return show(event);
			else if(event.getKeyCode() == Keys.ESCAPE || event.getKeyCode() == Keys.BACK) // escape and back hide
				return hide(event);
		}
		return false;
	}

}
