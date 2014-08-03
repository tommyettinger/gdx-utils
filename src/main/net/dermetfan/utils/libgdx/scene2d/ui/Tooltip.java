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

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.utils.Timer;
import com.badlogic.gdx.utils.Timer.Task;
import net.dermetfan.utils.libgdx.scene2d.Scene2DUtils;

import static com.badlogic.gdx.scenes.scene2d.InputEvent.Type.enter;
import static com.badlogic.gdx.scenes.scene2d.InputEvent.Type.exit;
import static com.badlogic.gdx.scenes.scene2d.InputEvent.Type.mouseMoved;
import static com.badlogic.gdx.scenes.scene2d.InputEvent.Type.touchDown;
import static com.badlogic.gdx.scenes.scene2d.InputEvent.Type.touchUp;

public class Tooltip<T extends Actor> extends ContextPopup<T> {

	/** if the tooltip should follow the pointer */
	private boolean followPointer;

	/** if the tooltip should be shown at the pointer */
	private boolean showAtPointer = true;

	/** the delay before {@link #show(Event)} */
	private float showDelay = .75f;

	/** the delay before {@link #hide(Event)} */
	private float hideDelay;

	/** the offset of the tooltip in respect to the mouse or enter position */
	private float offsetX, offsetY;

	/** if not null, {@link #show(Event)} will set the touchability of the {@link #popup} to this */
	private Touchable showTouchable = Touchable.enabled;

	/** if not null, {@link #hide(Event)} will set the touchability of the {@link #popup} to this */
	private Touchable hideTouchable = Touchable.disabled;

	/** the mask bits */
	public static final byte mask = 1;

	/** the flags that define when to hide, show or cancel the tooltip */
	private int showFlags = mask << enter.ordinal(), hideFlags = mask << touchDown.ordinal() | mask << touchUp.ordinal() | mask << exit.ordinal(), cancelFlags = mask << touchDown.ordinal() | mask << exit.ordinal();

	/** calls {@link #show(Event)} after setting the position of the popup
	 *  @see #run()
	 *  @author dermetfan */
	protected class ShowTask extends Task {
		private final InputEvent event = new InputEvent();

		/** calls {@link #show(Event)} after setting the position of the tooltip */
		@Override
		public void run() {
			if(showAtPointer) {
				Vector2 pos = Scene2DUtils.pointerPosition(event.getListenerActor().getStage());
				popup.setPosition(pos.x + offsetX, pos.y + offsetY);
			} else
				popup.setPosition(event.getStageX() + offsetX, event.getStageY() + offsetY);
			show(event);
		}
	}

	/** calls {@link #hide(Event)}
	 *  @see #run()
	 *  @author dermetfan */
	protected class HideTask extends Task {
		private final InputEvent event = new InputEvent();

		@Override
		public void run() {
			hide(event);
		}
	}

	/** a local {@link ShowTask} instance */
	private final ShowTask showTask = new ShowTask();

	/** @see #hide(Event) */
	private final HideTask hideTask = new HideTask();

	/** @see net.dermetfan.utils.libgdx.scene2d.Popup#Popup(Actor)  */
	public Tooltip(T popup) {
		super(popup);
	}

	@Override
	public boolean handle(Event e) {
		if(!(e instanceof InputEvent))
			return false;
		InputEvent event = (InputEvent) e;
		if(event.getRelatedActor() == popup)
			return false;

		Type type = event.getType();
		int flag = mask << type.ordinal();

		if(type == mouseMoved && followPointer)
			popup.setPosition(event.getStageX() + offsetX, event.getStageY() + offsetY);

		if((cancelFlags & flag) == flag)
			showTask.cancel();

		if((hideFlags & flag) == flag) {
			Scene2DUtils.copy(hideTask.event, event);
			if(hideDelay > 0) {
				if(!hideTask.isScheduled())
					Timer.schedule(hideTask, hideDelay);
			} else
				hideTask.run();
		}

		if((showFlags & flag) == flag) {
			Scene2DUtils.copy(showTask.event, event);
			if(showDelay > 0) {
				if(!showTask.isScheduled())
					Timer.schedule(showTask, showDelay);
			} else
				showTask.run();
		}
		return false;
	}

	/** Brings the {@link #popup} {@link Actor#toFront() to front} and {@link Actions#fadeIn(float) fades} it in for 0.4 seconds ({@code Dialog#fadeDuration} when it still existed).
	 *  @param event {@link Scene2DUtils#copy(InputEvent, InputEvent) copied} {@link ShowTask#event} from {@link #showTask}, so cancelling has no effect */
	@Override
	public boolean show(Event event) {
		super.show(event);
		SequenceAction sequence = Actions.sequence(Actions.fadeIn(.4f));
		if(showTouchable != null)
			sequence.addAction(Actions.touchable(showTouchable));
		popup.addAction(sequence);
		return false;
	}

	/** {@link Actions#fadeOut(float) Fades} the tooltip out for 0.4 seconds ({@code Dialog#fadeDuration when it still existed}).
	 *  @param event {@link Scene2DUtils#copy(InputEvent, InputEvent) copied} {@link HideTask#event} from {@link #hideTask}, so cancelling has no effect */
	@Override
	public boolean hide(Event event) {
		super.hide(event);
		SequenceAction sequence = Actions.sequence(Actions.fadeOut(.4f));
		if(hideTouchable != null)
			sequence.addAction(Actions.touchable(hideTouchable));
		popup.addAction(sequence);
		return false;
	}

	/** @param flag the {@link Type} on which to show the tooltip */
	public void showOn(Type flag) {
		showFlags |= mask << flag.ordinal();
	}

	/** @param flag the {@link Type} on which not to show the tooltip */
	public void showNotOn(Type flag) {
		showFlags &= ~(mask << flag.ordinal());
	}

	/** @param flag the {@link Type} on which to hide the tooltip */
	public void hideOn(Type flag) {
		hideFlags |= mask << flag.ordinal();
	}

	/** @param flag the {@link Type} on which not to hide the tooltip */
	public void hideNotOn(Type flag) {
		hideFlags &= ~(mask << flag.ordinal());
	}

	/** @param flag the {@link Type} on which to cancel showing the tooltip */
	public void cancelOn(Type flag) {
		cancelFlags |= mask << flag.ordinal();
	}

	/** @param flag the {@link Type} on which to not cancel showing the tooltip */
	public void cancelNotOn(Type flag) {
		cancelFlags &= ~(mask << flag.ordinal());
	}

	/** never show the tooltip */
	public void showNever() {
		showFlags = 0;
	}

	/** never hide the tooltip */
	public void hideNever() {
		hideFlags = 0;
	}

	/** never cancel showing the tooltip */
	public void cancelNever() {
		cancelFlags = 0;
	}

	/** show the tooltip on any event */
	public void showAlways() {
		showFlags = ~0;
	}

	/** hide the tooltip on any event */
	public void hideAlways() {
		hideFlags = ~0;
	}

	/** cancel showing the tooltip on any event */
	public void cancelAlways() {
		cancelFlags = ~0;
	}

	/** @param delay the {@link #showDelay} and {@link #hideDelay} */
	public void setDelay(float delay) {
		showDelay = hideDelay = delay;
	}

	/** @param offsetX the {@link #offsetX}
	 *  @param offsetY the {@link #offsetY} */
	public void setOffset(float offsetX, float offsetY) {
		this.offsetX = offsetX;
		this.offsetY = offsetY;
	}

	/** @param popup the {@link #popup} to set */
	public void setPopup(T popup) {
		this.popup = popup;
	}

	/** @return the {@link #followPointer} */
	public boolean isFollowPointer() {
		return followPointer;
	}

	/** @param followPointer the {@link #followPointer} to set */
	public void setFollowPointer(boolean followPointer) {
		this.followPointer = followPointer;
	}

	/** @return the {@link #showAtPointer} */
	public boolean isShowAtPointer() {
		return showAtPointer;
	}

	/** @param showAtPointer the {@link #showAtPointer} to set */
	public void setShowAtPointer(boolean showAtPointer) {
		this.showAtPointer = showAtPointer;
	}

	/** @return the {@link #showDelay} */
	public float getShowDelay() {
		return showDelay;
	}

	/** @param showDelay the {@link #showDelay} to set */
	public void setShowDelay(float showDelay) {
		this.showDelay = showDelay;
	}

	/** @return the {@link #hideDelay} */
	public float getHideDelay() {
		return hideDelay;
	}

	/** @param hideDelay the {@link #hideDelay} to set */
	public void setHideDelay(float hideDelay) {
		this.hideDelay = hideDelay;
	}

	/** @return the {@link #offsetX} */
	public float getOffsetX() {
		return offsetX;
	}

	/** @param offsetX the {@link #offsetX} to set */
	public void setOffsetX(float offsetX) {
		this.offsetX = offsetX;
	}

	/** @return the {@link #offsetY} */
	public float getOffsetY() {
		return offsetY;
	}

	/** @param offsetY the {@link #offsetY} to set */
	public void setOffsetY(float offsetY) {
		this.offsetY = offsetY;
	}

	/** @return the {@link #showTouchable} */
	public Touchable getShowTouchable() {
		return showTouchable;
	}

	/** @param showTouchable the {@link #showTouchable} to set */
	public void setShowTouchable(Touchable showTouchable) {
		this.showTouchable = showTouchable;
	}

	/** @return the {@link #hideTouchable} */
	public Touchable getHideTouchable() {
		return hideTouchable;
	}

	/** @param hideTouchable the {@link #hideTouchable} to set */
	public void setHideTouchable(Touchable hideTouchable) {
		this.hideTouchable = hideTouchable;
	}

	/** @return the {@link #showFlags} */
	public int getShowFlags() {
		return showFlags;
	}

	/** @param showFlags the {@link #showFlags} to set */
	public void setShowFlags(int showFlags) {
		this.showFlags = showFlags;
	}

	/** @return the {@link #hideFlags} */
	public int getHideFlags() {
		return hideFlags;
	}

	/** @param hideFlags the {@link #hideFlags} to set */
	public void setHideFlags(int hideFlags) {
		this.hideFlags = hideFlags;
	}

	/** @return the {@link #cancelFlags} */
	public int getCancelFlags() {
		return cancelFlags;
	}

	/** @param cancelFlags the {@link #cancelFlags} to set */
	public void setCancelFlags(int cancelFlags) {
		this.cancelFlags = cancelFlags;
	}

}
