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
import com.badlogic.gdx.utils.Pools;
import net.dermetfan.gdx.scenes.scene2d.ui.popup.PositionBehavior.Position;

/** The position of the event if it is an {@link InputEvent}. The position is composed of {@link InputEvent#getStageX()} and {@link InputEvent#getStageY()}.
 *  @author dermetfan
 *  @since 0.8.0 */
public class EventPosition implements Position {

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
