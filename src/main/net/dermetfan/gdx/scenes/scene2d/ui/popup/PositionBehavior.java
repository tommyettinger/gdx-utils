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
import net.dermetfan.gdx.scenes.scene2d.ui.popup.Popup.Behavior;

/** sets the position of the popup in {@link #show(Event, Popup)}
 *  @author dermetfan
 *  @since 0.8.0 */
public class PositionBehavior extends Behavior.Adapter {

	/** the Position to {@link Position#apply(Event, Actor) apply} */
	private Position position;

	/** @param position the {@link #position} */
	public PositionBehavior(Position position) {
		this.position = position;
	}

	/** @param popup the popup which position to set */
	@Override
	public boolean show(Event event, Popup popup) {
		position.apply(event, popup.getPopup());
		return super.show(event, popup);
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

	/** @since 0.8.0
	 *  @author dermetfan */
	public interface Position {

		/** @param event the event
		 *  @param popup the popup which position to set */
		void apply(Event event, Actor popup);

	}

}
