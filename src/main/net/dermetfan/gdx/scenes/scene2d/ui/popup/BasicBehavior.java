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

/** basic popup behavior that does not react to events
 *  @since 0.8.0
 *  @author dermetfan */
public class BasicBehavior extends Behavior.Adapter {

	/** calls {@link Actor#setVisible(boolean) setVisible(true)} and {@link Actor#toFront() toFront()} on the {@link Popup#popup} */
	@Override
	public boolean show(Event event, Popup popup) {
		popup.getPopup().setVisible(true);
		popup.getPopup().toFront();
		return true;
	}

	/** calls {@link Actor#setVisible(boolean) setVisible(false)} on the {@link Popup#popup} */
	@Override
	public boolean hide(Event event, Popup popup) {
		popup.getPopup().setVisible(false);
		return false;
	}

}
