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

/** offsets the position by aligning it using the popup's size
 *  @since 0.8.0
 *  @author dermetfan */
public class AlignedOffsetPosition implements Position {

	/** the {@link com.badlogic.gdx.scenes.scene2d.utils.Align Align} flag */
	private int align;

	/** @param align the {@link #align} */
	public AlignedOffsetPosition(int align) {
		this.align = align;
	}

	@Override
	public void apply(Event event, Actor popup) {
		Vector2 offset = Scene2DUtils.align(popup.getWidth(), popup.getHeight(), align);
		popup.setPosition(popup.getX() - offset.x, popup.getY() - offset.y);
	}

	// getters and setters

	/** @return the {@link #align} */
	public int getAlign() {
		return align;
	}

	/** @param align the {@link #align} to set */
	public void setAlign(int align) {
		this.align = align;
	}

}
