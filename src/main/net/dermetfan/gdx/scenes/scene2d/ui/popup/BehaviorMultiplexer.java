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

	/** Calls {@link Behavior#handle(Event, Popup)} on all Behaviors in order until one returns a non-null Reaction.
	 *  If that Reaction does not indicate that the event is handled the remaining Behaviors are called until one returns a Reaction that does handle the event.
	 *  The first Reaction is combined with the second Reaction (see table below) and the result is returned.
	 *  If no Behavior handles the event the first Reaction (or null if none) is returned.
	 *  <table summary="Reaction combinations">
	 *      <tr>
	 *          <th>first</th>
	 *          <th>second</th>
	 *          <th>result</th>
	 *      </tr>
	 *      <tr>
	 *          <td>Show</td>
	 *          <td>ShowHandle</td>
	 *          <td>ShowHandle</td>
	 *      </tr>
	 *      <tr>
	 *          <td>Show</td>
	 *          <td>HideHandle</td>
	 *          <td>ShowHandle</td>
	 *      </tr>
	 *      <tr>
	 *          <td>Show</td>
	 *          <td>Handle</td>
	 *          <td>ShowHandle</td>
	 *      </tr>
	 *      <tr>
	 *          <td>Hide</td>
	 *          <td>ShowHandle</td>
	 *          <td>HideHandle</td>
	 *      </tr>
	 *      <tr>
	 *          <td>Hide</td>
	 *          <td>HideHandle</td>
	 *          <td>HideHandle</td>
	 *      </tr>
	 *      <tr>
	 *          <td>Hide</td>
	 *          <td>Handle</td>
	 *          <td>HideHandle</td>
	 *      </tr>
	 *      <tr>
	 *          <td>None</td>
	 *          <td>ShowHandle</td>
	 *          <td>Handle</td>
	 *      </tr>
	 *      <tr>
	 *          <td>None</td>
	 *          <td>HideHandle</td>
	 *          <td>Handle</td>
	 *      </tr>
	 *      <tr>
	 *          <td>None</td>
	 *          <td>Handle</td>
	 *          <td>Handle</td>
	 *      </tr>
	 *  </table>
	 *  @return the first Reaction received combined with the next Reaction that handles the event, or null */
	@Override
	public Reaction handle(Event event, Popup popup) {
		Reaction reaction = null;
		int i = 0;
		for(; reaction == null && i < receivers.size - 1; i++)
			reaction = receivers.get(i).handle(event, popup);
		if(reaction != Reaction.ShowHandle && reaction != Reaction.HideHandle && reaction != Reaction.Handle) {
			for(; i < receivers.size - 1; i++) {
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
