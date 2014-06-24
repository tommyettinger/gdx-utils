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

package net.dermetfan.utils.libgdx.scene2d;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.utils.Array;
import net.dermetfan.utils.libgdx.Multiplexer;

/** an {@link EventListener} that notifies an array of other EventListeners
 *  @author dermetfan */
public class EventMultiplexer extends Multiplexer<EventListener> implements EventListener {

	public EventMultiplexer(EventListener... receivers) {
		super(receivers);
	}

	public EventMultiplexer(Array<EventListener> receivers) {
		super(receivers);
	}

	/** @return if one of the {@link #receivers} returned true in {@link EventListener#handle(com.badlogic.gdx.scenes.scene2d.Event) handle(Event)} */
	@Override
	public boolean handle(Event event) {
		boolean handled = false;
		for(EventListener listener : receivers)
			handled |= listener.handle(event);
		return handled;
	}

}
