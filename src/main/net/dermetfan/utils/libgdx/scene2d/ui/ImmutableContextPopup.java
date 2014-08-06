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
import net.dermetfan.utils.libgdx.scene2d.Scene2DUtils;

/** an {@link ImmutableEventPopup} that shows the {@link #popup} under the pointer
 *  @author dermetfan */
public class ImmutableContextPopup<T extends Actor> extends ImmutableEventPopup<T> {

	/** the offset from the pointer position */
	private float offsetX, offsetY;

	public ImmutableContextPopup(T popup) {
		super(popup);
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
