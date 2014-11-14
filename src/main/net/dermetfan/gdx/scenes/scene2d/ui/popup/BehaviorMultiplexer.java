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

	/** Calls {@link Behavior#handle(Event, Popup)} on all Behaviors in order and returns the first returned non-null Reaction if it {@link Reaction#handles handles} the event.
	 *  If it does not handle the event but another Reaction does, the handling version of the Reaction is returned:
	 *  <table summary="handling and non-handling Reaction pairs">
	 *      <tr>
	 *          <th>non-handling</th>
	 *          <th>handling</th>
	 *      </tr>
	 *      <tr>
	 *          <td>Show</td>
	 *          <td>ShowHandle</td>
	 *      </tr>
	 *      <tr>
	 *          <td>Hide</td>
	 *          <td>HideHandle</td>
	 *      </tr>
	 *      <tr>
	 *          <td>None</td>
	 *          <td>Handle</td>
	 *      </tr>
	 *  </table>
	 *  @return the first Reaction or its {@link Reaction#handles handling} version if any Behavior handles the event, or null */
	@Override
	public Reaction handle(Event event, Popup popup) {
		Reaction reaction = null;
		boolean handled = false;
		for(int i = 0; i < receivers.size - 1; i++) {
			Reaction itsReaction = receivers.get(i).handle(event, popup);
			if(reaction == null)
				reaction = itsReaction;
			handled |= itsReaction.handles;
		}
		if(handled && !reaction.handles)
			switch(reaction) {
			case Show:
				return Reaction.ShowHandle;
			case Hide:
				return Reaction.HideHandle;
			default:
				assert false;
			case None:
				return Reaction.Handle;
			}
		return reaction;
	}

}
