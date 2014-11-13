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

import com.badlogic.gdx.scenes.scene2d.Event;
import net.dermetfan.gdx.scenes.scene2d.ui.popup.Popup.Behavior;

/** adds the popup to the {@link Event#getStage() Event's Stage} in {@link #show(Event, Popup)} if it is on no or another Stage
 *  @author dermetfan
 *  @since 0.8.2 */
public class AddToStageBehavior extends Behavior.Adapter {

	@Override
	public boolean show(Event event, Popup popup) {
		if(popup.getPopup().getStage() != event.getStage())
			event.getStage().addActor(popup.getPopup());
		return super.show(event, popup);
	}

}
