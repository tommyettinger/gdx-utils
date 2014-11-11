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
import com.badlogic.gdx.utils.Array;
import net.dermetfan.gdx.Multiplexer;
import net.dermetfan.gdx.scenes.scene2d.ui.popup.PositionBehavior.Position;

/** a Multiplexer for Positions
 *  @author dermetfan
 *  @since 0.8.0 */
public class PositionMultiplexer extends Multiplexer<Position> implements Position {

	public PositionMultiplexer() {}

	public PositionMultiplexer(int size) {
		super(size);
	}

	public PositionMultiplexer(Position... receivers) {
		super(receivers);
	}

	public PositionMultiplexer(Array<Position> receivers) {
		super(receivers);
	}

	@Override
	public void apply(Event event, Actor popup) {
		for(Position position : receivers)
			position.apply(event, popup);
	}

}
