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

import java.lang.Object;import java.lang.Override;import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputEvent.Type;
import com.badlogic.gdx.utils.Array;

/** A {@link ContextPopup} that solves the problem of not hiding on certain events on other actors (except children) by holding {@link #targets}.
 *  Supposed to be added to an ancestor high up above {@link #targets} in the hierarchy tree, e.g. the Stage itself. Will {@link #show(Event) show} only on events which {@link Event#getTarget() target} the {@link #targets} and {@link #hide(Event) hide} otherwise (except the {@link #popup} itself is the event target).
 *  @author dermetfan */
public class TargetedContextPopup<T extends Actor> extends ContextPopup<T> {

	/** the actors that are applicable for this context menu */
	private final Array<Actor> targets;

	/** @param target the {@link #targets target} to add
	 *  @see net.dermetfan.utils.libgdx.scene2d.Popup#Popup(Actor) */
	public TargetedContextPopup(T popup, Actor target) {
		super(popup);
		this.targets = new Array<>(1);
		this.targets.add(target);
	}

	/** @param targets the {@link #targets} to add
	 *  @see net.dermetfan.utils.libgdx.scene2d.Popup#Popup(Actor) */
	public TargetedContextPopup(T popup, Actor... targets) {
		super(popup);
		this.targets = new Array<>(targets);
	}

	/** @param targets the {@link #targets} to add
	 *  @see net.dermetfan.utils.libgdx.scene2d.Popup#Popup(Actor) */
	public TargetedContextPopup(T popup, Array<Actor> targets) {
		super(popup);
		this.targets = new Array<>(targets);
	}

	/** {@link #hide(Event) hides} the popup if the {@link Event#getTarget() event target} is not a {@link #targets target} or the {@link #popup}, calls its super method otherwise */
	@Override
	public boolean handle(Event e) {
		if(!(e instanceof InputEvent))
			return false;
		InputEvent event = (InputEvent) e;
		if((event.getType() == Type.touchDown || event.getType() == Type.keyDown) && !targets.contains(event.getTarget(), true))
			return event.getTarget() != popup && hide(event);
		return super.handle(event);
	}

	/** {@link Array#clear() clears} the {@link #targets} */
	public void clearTargets() {
		targets.clear();
	}

	/** @param target the target to add */
	public void addTarget(Actor target) {
		targets.add(target);
	}

	/** @param target the target to remove
	 *  @return see {@link Array#removeValue( Object , boolean)} */
	public boolean removeTarget(Actor target) {
		return targets.removeValue(target, true);
	}

	// getters and setters

	/** @param targets the targets to add to {@link #targets} after {@link Array#clear() clearing} */
	public final void setTargets(Actor... targets) {
		this.targets.clear();
		this.targets.addAll(targets);
	}

	/** @param targets the targets to add to {@link #targets} after {@link Array#clear() clearing} */
	public void setTargets(Array<Actor> targets) {
		this.targets.clear();
		this.targets.addAll(targets);
	}

	/** @return the {@link #targets} */
	public Array<Actor> getTargets() {
		return targets;
	}

}
