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
 *  @author dermetfan
 *  @since 0.8.0 */
public class FadeBehavior extends Behavior.Adapter {

	/** the fade duration (default is 0.4) */
	private float fadeInDuration = .4f, fadeOutDuration = .4f;

	/** the fade interpolation (default is {@code Interpolation.fade}) */
	private Interpolation fadeInInterpolation = Interpolation.fade, fadeOutInterpolation = Interpolation.fade;

	/** creates a FadeBehavior with the default values */
	public FadeBehavior() {}

	/** @param fadeDuration the {@link #fadeInDuration} and {@code #fadeOutDuration} */
	public FadeBehavior(float fadeDuration) {
		this(fadeDuration, fadeDuration);
	}

	/** @param fadeInterpolation the {@link #fadeInInterpolation} and {@link #fadeOutInterpolation} */
	public FadeBehavior(Interpolation fadeInterpolation) {
		this(fadeInterpolation, fadeInterpolation);
	}

	/** @param fadeDuration the {@link #fadeInDuration} and {@link #fadeOutDuration}
	 *  @param fadeInterpolation the {@link #fadeInInterpolation} and {@link #fadeOutInterpolation} */
	public FadeBehavior(float fadeDuration, Interpolation fadeInterpolation) {
		this(fadeDuration, fadeDuration, fadeInterpolation, fadeInterpolation);
	}

	/** @param fadeInDuration the {@link #fadeInDuration}
	 *  @param fadeOutDuration the {@link #fadeOutDuration} */
	public FadeBehavior(float fadeInDuration, float fadeOutDuration) {
		this.fadeInDuration = fadeInDuration;
		this.fadeOutDuration = fadeOutDuration;
	}

	/** @param fadeInInterpolation the {@link #fadeInInterpolation}
	 *  @param fadeOutInterpolation the {@link #fadeOutInterpolation} */
	public FadeBehavior(Interpolation fadeInInterpolation, Interpolation fadeOutInterpolation) {
		this.fadeInInterpolation = fadeInInterpolation;
		this.fadeOutInterpolation = fadeOutInterpolation;
	}

	/** @param fadeInDuration the {@link #fadeInDuration}
	 *  @param fadeOutDuration the {@link #fadeOutDuration}
	 *  @param fadeInInterpolation the {@link #fadeInInterpolation}
	 *  @param fadeOutInterpolation the {@link #fadeOutInterpolation} */
	public FadeBehavior(float fadeInDuration, float fadeOutDuration, Interpolation fadeInInterpolation, Interpolation fadeOutInterpolation) {
		this.fadeInDuration = fadeInDuration;
		this.fadeOutDuration = fadeOutDuration;
		this.fadeInInterpolation = fadeInInterpolation;
		this.fadeOutInterpolation = fadeOutInterpolation;
	}

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
