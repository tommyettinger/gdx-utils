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

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import net.dermetfan.gdx.scenes.scene2d.ui.popup.Popup.Behavior;

/** fades in/out in {@link #show(Event, Popup)}/{@link #hide(Event, Popup)}
 *  @since 0.8.0
 *  @author dermetfan */
public class FadeBehavior extends Behavior.Adapter {

	/** the fade duration */
	private float fadeInDuration = .4f, fadeOutDuration = .4f;

	/** the fade interpolation */
	private Interpolation fadeInInterpolation = Interpolation.fade, fadeOutInterpolation = Interpolation.fade;

	@Override
	public boolean show(Event event, Popup popup) {
		popup.getPopup().addAction(Actions.sequence(Actions.visible(true), Actions.fadeIn(fadeInDuration, fadeInInterpolation)));
		return super.show(event, popup);
	}

	@Override
	public boolean hide(Event event, Popup popup) {
		popup.getPopup().addAction(Actions.sequence(Actions.fadeOut(fadeOutDuration, fadeOutInterpolation), Actions.visible(false)));
		return super.hide(event, popup);
	}

	// getters and setters

	/** @return the {@link #fadeInDuration} */
	public float getFadeInDuration() {
		return fadeInDuration;
	}

	/** @param fadeInDuration the {@link #fadeInDuration} to set */
	public void setFadeInDuration(float fadeInDuration) {
		this.fadeInDuration = fadeInDuration;
	}

	/** @return the {@link #fadeOutDuration} */
	public float getFadeOutDuration() {
		return fadeOutDuration;
	}

	/** @param fadeOutDuration the {@link #fadeOutDuration} to set */
	public void setFadeOutDuration(float fadeOutDuration) {
		this.fadeOutDuration = fadeOutDuration;
	}

	/** @return the {@link #fadeInDuration} */
	public Interpolation getFadeInInterpolation() {
		return fadeInInterpolation;
	}

	/** @param fadeInInterpolation the {@link #fadeInInterpolation} to set */
	public void setFadeInInterpolation(Interpolation fadeInInterpolation) {
		this.fadeInInterpolation = fadeInInterpolation;
	}

	/** @return the {@link #fadeOutInterpolation} */
	public Interpolation getFadeOutInterpolation() {
		return fadeOutInterpolation;
	}

	/** @param fadeOutInterpolation the {@link #fadeOutInterpolation} to set */
	public void setFadeOutInterpolation(Interpolation fadeOutInterpolation) {
		this.fadeOutInterpolation = fadeOutInterpolation;
	}

}
