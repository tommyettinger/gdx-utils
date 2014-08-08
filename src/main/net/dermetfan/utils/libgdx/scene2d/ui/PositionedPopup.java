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

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import net.dermetfan.utils.Function;
import net.dermetfan.utils.libgdx.scene2d.Scene2DUtils;

/** Shows the {@link #popup} at a {@link net.dermetfan.utils.libgdx.scene2d.ui.PositionedPopup.Position}.
 *  @author dermetfan
 *  @since 0.4.0 */
public abstract class PositionedPopup<T extends Actor> extends Popup<T> {

	/** Determines a position based on an event.
	 *  @author dermetfan
	 *  @since 0.4.0 */
	public static interface Position extends Function<Vector2, Event> {}

	/** Resolves a preset position.
	 *  @author dermetfan
	 *  @since 0.4.0 */
	public static class PresetPosition implements Position {

		/** the position to {@link #apply(Event)} */
		public static final Vector2 preset = new Vector2();

		public PresetPosition() {}

		/** @param x the x component of {@link #preset}
		 *  @param y the y component of {@link #preset} */
		public PresetPosition(float x, float y) {
			preset.set(x, y);
		}

		/** @return {@link #preset} */
		@Override
		public Vector2 apply(Event event) {
			return preset;
		}

	}

	/** Resolves the position of a pointer.
	 *  @author dermetfan
	 *  @since 0.4.0 */
	public static class PointerPosition implements Position {

		/** the pointer which position to resolve */
		private int pointer;

		/** resolves pointer 0 */
		public PointerPosition() {}

		/** @param pointer the {@link #pointer} */
		public PointerPosition(int pointer) {
			this.pointer = pointer;
		}

		/** @return the return value of {@link Scene2DUtils#pointerPosition(com.badlogic.gdx.scenes.scene2d.Stage, int)} with {@link Event#getStage()} and {@link #pointer} */
		@Override
		public Vector2 apply(Event event) {
			return Scene2DUtils.pointerPosition(event.getStage(), pointer);
		}

		/** @return the {@link #pointer} */
		public int getPointer() {
			return pointer;
		}

		/** @param pointer the {@link #pointer} to set */
		public void setPointer(int pointer) {
			this.pointer = pointer;
		}

	}

	/** Resolves the event position if the given event is an {@link InputEvent}. The event position is composed of {@link InputEvent#getStageX()} and {@link InputEvent#getStageY()}.
	 *  @author dermetfan
	 *  @since 0.4.0 */
	public static class EventPosition implements Position {

		/** the return value of {@link #apply(Event)} */
		private final Vector2 pos = new Vector2();

		/** @return the event position if the event is an {@link InputEvent}, {@link Float#NaN} otherwise */
		@Override
		public Vector2 apply(Event event) {
			if(event instanceof InputEvent) {
				InputEvent inputEvent = (InputEvent) event;
				return pos.set(inputEvent.getStageX(), inputEvent.getStageY());
			}
			return pos.set(Float.NaN, Float.NaN);
		}

	}

	/** the position in stage coordinates */
	private Position position;

	/** the offset from {@link #position}*/
	private float offsetX, offsetY;

	/** @param position the {@link #position} */
	public PositionedPopup(T popup, Position position) {
		super(popup);
		this.position = position;
	}

	/** @param position the {@link #position}
	 *  @param offsetX {@link #offsetX}
	 *  @param offsetY {@link #offsetY} */
	public PositionedPopup(T popup, Position position, float offsetX, float offsetY) {
		this(popup, position);
		this.offsetX = offsetX;
		this.offsetY = offsetY;
	}

	/** {@link Popup#show(Event) Shows} the {@link #popup} under the pointer, offset by {@link #offsetX} and {@link #offsetY}. Also adds it to the stage of the event if it has no stage. */
	@Override
	public boolean show(Event event) {
		Vector2 pos = position.apply(event).add(offsetX, offsetY);
		if(getPopup().hasParent()) // convert position to popup's coordinates
			Scene2DUtils.stageToLocalCoordinates(pos, getPopup().getParent());
		getPopup().setPosition(pos.x, pos.y);
		return super.show(event);
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

	/** @param x the {@link #offsetX}
	 *  @param y the {@link #offsetY} */
	public void setOffset(float x, float y) {
		offsetX = x;
		offsetY = y;
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

}
