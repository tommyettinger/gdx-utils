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

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.utils.Pools;
import net.dermetfan.gdx.scenes.scene2d.Scene2DUtils;
import net.dermetfan.gdx.scenes.scene2d.ui.popup.Popup.Behavior;

/** sets the position of the popup in {@link #show(Event, Popup)}
 *  @author dermetfan
 *  @since 0.8.0 */
public class PositionBehavior extends Behavior.Adapter {

	/** the Position to {@link Position#apply(Event, Actor) apply} */
	private Position position;

	public PositionBehavior(Position position) {
		this.position = position;
	}

	/** @param popup the popup which position to set */
	@Override
	public boolean show(Event event, Popup popup) {
		position.apply(event, popup.getPopup());
		return super.show(event, popup);
	}

	/** @since 0.8.0
	 *  @author dermetfan */
	public interface Position {

		/** @param event the event
		 *  @param popup the popup which position to set */
		void apply(Event event, Actor popup);

	}

	/** positions the popup relative to {@link Event#getTarget() target}
	 *  @author dermetfan
	 *  @since 0.8.0 */
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

	}

	/** a preset position
	 *  @author dermetfan
	 *  @since 0.8.0 */
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

		@Override
		public void apply(Event event, Actor popup) {
			popup.setPosition(x, y);
		}

	}

	/** the position of a pointer in stage coordinates
	 *  @author dermetfan
	 *  @since 0.8.0 */
	public static class PointerPosition implements Position {

		/** the pointer which position to resolve */
		private int pointer;

		/** resolves pointer 0 */
		public PointerPosition() {}

		/** @param pointer the {@link #pointer} */
		public PointerPosition(int pointer) {
			this.pointer = pointer;
		}

		/** @return the {@link #pointer} */
		public int getPointer() {
			return pointer;
		}

		/** @param pointer the {@link #pointer} to set */
		public void setPointer(int pointer) {
			this.pointer = pointer;
		}

		@Override
		public void apply(Event event, Actor popup) {
			Vector2 pos = Scene2DUtils.pointerPosition(event.getStage(), pointer);
			if(popup.hasParent())
				popup.getParent().stageToLocalCoordinates(pos);
			popup.setPosition(pos.x, pos.y);
		}

	}

	/** The position of the event if it is an {@link InputEvent}. The position is composed of {@link InputEvent#getStageX()} and {@link InputEvent#getStageY()}.
	 *  @author dermetfan
	 *  @since 0.8.0 */
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

	/** offsets the popup by a certain amount
	 *  @since 0.8.0
	 *  @author dermetfan */
	public static class OffsetPosition implements Position {

		/** the offset */
		private float x, y;

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

		@Override
		public void apply(Event event, Actor popup) {
			popup.setPosition(popup.getX() + x, popup.getY() + y);
		}

	}

}
