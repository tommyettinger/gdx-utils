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
import net.dermetfan.gdx.scenes.scene2d.ui.popup.PositionBehavior.Position;

/** offsets the popup by a certain amount
 *  @since 0.8.0
 *  @author dermetfan */
public class OffsetPosition implements Position {

	/** the offset */
	private float x, y;

	/** @param x the {@link #x}
	 *  @param y the {@link #y} */
	public OffsetPosition(float x, float y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public void apply(Event event, Actor popup) {
		popup.setPosition(popup.getX() + x, popup.getY() + y);
	}

	// getters and setters

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

}
