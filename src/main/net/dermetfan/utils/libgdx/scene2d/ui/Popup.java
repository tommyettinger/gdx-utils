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

package net.dermetfan.utils.libgdx.scene2d.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;

/** Shows {@link #popup} on certain events.
 *  @param <T> the type of {@link #popup}
 *  @author dermetfan */
public abstract class Popup<T extends Actor> implements EventListener {

	/** the {@code T} to pop up */
	private T popup;

	/** @param popup the {@link #popup} */
	public Popup(T popup) {
		this.popup = popup;
	}

	/** Makes the {@link #popup} {@link Actor#setVisible(boolean) visible} and brings it to {@link Actor#toFront() front}. Override this for custom behaviour.
	 *  @return if the event is handled, true by default (suggestive) */
	public boolean show(Event event) {
		popup.setVisible(true);
		popup.toFront();
		return true;
	}

	/** Makes the {@link #popup} {@link Actor#setVisible(boolean) invisible}. Override this for custom behavior.
	 *  @return if the event is handled, false by default (suggestive) */
	public boolean hide(Event event) {
		popup.setVisible(false);
		return false;
	}

	// getters and setters

	/** @return the {@link #popup} */
	public T getPopup() {
		return popup;
	}

	/** @param popup the {@link #popup} to set */
	public void setPopup(T popup) {
		this.popup = popup;
	}

}
