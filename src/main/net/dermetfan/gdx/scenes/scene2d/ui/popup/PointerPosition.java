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
import net.dermetfan.gdx.scenes.scene2d.Scene2DUtils;
import net.dermetfan.gdx.scenes.scene2d.ui.popup.PositionBehavior.Position;

/** the position of a pointer in stage coordinates
 *  @author dermetfan
 *  @since 0.8.0 */
public class PointerPosition implements Position {

	/** the pointer which position to resolve */
	private int pointer;

	/** resolves pointer 0 */
	public PointerPosition() {}

	/** @param pointer the {@link #pointer} */
	public PointerPosition(int pointer) {
		this.pointer = pointer;
	}

	@Override
	public void apply(Event event, Actor popup) {
		Vector2 pos = Scene2DUtils.pointerPosition(event.getStage(), pointer);
		if(popup.hasParent())
			popup.getParent().stageToLocalCoordinates(pos);
		popup.setPosition(pos.x, pos.y);
	}

	// getters and setters

	/** @return the {@link #pointer} */
	public int getPointer() {
		return pointer;
	}

	/** @param pointer the {@link #pointer} to set */
	public void setPointer(int pointer) {
		this.pointer = pointer;
	}

}
