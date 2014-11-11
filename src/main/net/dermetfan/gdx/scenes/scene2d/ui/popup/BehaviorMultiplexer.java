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
import com.badlogic.gdx.utils.Array;
import net.dermetfan.gdx.Multiplexer;
import net.dermetfan.gdx.scenes.scene2d.ui.popup.Popup.Behavior;

/** a Multiplexer for Behaviors
 *  @author dermetfan
 *  @since 0.8.0 */
public class BehaviorMultiplexer extends Multiplexer<Behavior> implements Behavior {

	public BehaviorMultiplexer() {}

	public BehaviorMultiplexer(int size) {
		super(size);
	}

	public BehaviorMultiplexer(Behavior... receivers) {
		super(receivers);
	}

	public BehaviorMultiplexer(Array<Behavior> receivers) {
		super(receivers);
	}

	/** @return whether any of the Behaviors handled the event */
	@Override
	public boolean show(Event event, Popup popup) {
		boolean handled = false;
		for(Behavior behavior : receivers)
			handled |= behavior.show(event, popup);
		return handled;
	}

	/** @return whether any of the Behaviors handled the event */
	@Override
	public boolean hide(Event event, Popup popup) {
		boolean handled = false;
		for(Behavior behavior : receivers)
			handled |= behavior.hide(event, popup);
		return handled;
	}

	/** @return the first Reaction received from the last Behavior combined with Reaction.Handle if any Behavior handles the event */
	@Override
	public Reaction handle(Event event, Popup popup) {
		Reaction reaction = null;
		int i = receivers.size - 1;
		for(; reaction == null && i >= 0; i--)
			reaction = receivers.get(i).handle(event, popup);
		if(reaction != Reaction.ShowHandle && reaction != Reaction.HideHandle && reaction != Reaction.Handle) {
			for(; i >= 0; i--) {
				Reaction react = receivers.get(i).handle(event, popup);
				if(react == Reaction.ShowHandle || react == Reaction.HideHandle || react == Reaction.Handle)
					switch(reaction != null ? reaction : Reaction.None) {
					case Show:
						return Reaction.ShowHandle;
					case Hide:
						return Reaction.HideHandle;
					case None:
					default:
						return Reaction.Handle;
					}
			}
		}
		return reaction;
	}

}
