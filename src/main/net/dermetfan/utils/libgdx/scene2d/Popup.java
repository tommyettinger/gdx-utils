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

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;

/** Holds a {@link #popup} and implements some basic show and hide actions.
 *  @param <T> the type of the {@link #popup}
 *  @author dermetfan */
public class Popup<T extends Actor>  {

	/** the {@code T} to pop up */
	protected T popup;

	/** @param popup the {@link #popup} */
	public Popup(T popup) {
		this.popup = popup;
	}

	/** Brings the {@link #popup} {@link Actor#toFront() to front} and makes it {@link Actor#setVisible(boolean) visible}. Override this for custom behaviour.
	 *  @return if the event is handled, true by default */
	public boolean show(Event event) {
		popup.toFront();
		popup.setVisible(true);
		return true;
	}

	/** Makes the {@link #popup} {@link Actor#setVisible(boolean) invisible}. Override this for custom behavior.
	 *  @return if the event is handled, false by default */
	public boolean hide(Event event) {
		popup.setVisible(false);
		return false;
	}

}
