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

package net.dermetfan.gdx.scenes.scene2d;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;

import static com.badlogic.gdx.scenes.scene2d.utils.Align.bottom;
import static com.badlogic.gdx.scenes.scene2d.utils.Align.left;
import static com.badlogic.gdx.scenes.scene2d.utils.Align.right;
import static com.badlogic.gdx.scenes.scene2d.utils.Align.top;
import static com.badlogic.gdx.scenes.scene2d.utils.Align.center;

/** provides useful methods for scene2d
 *  @author dermetfan */
public class Scene2DUtils {

	/** Some methods return this, so if you get your hands on it make sure to make a copy! This is used internally so it might change unexpectedly. */
	private static final Vector2 tmp = new Vector2();

	/** @param c the event to copy from
	 *  @param e the event to copy to */
	public static void copy(Event e, Event c) {
		c.setTarget(e.getTarget());
		c.setStage(e.getStage());
		c.setCapture(e.isCapture());
		c.setBubbles(e.getBubbles());
		c.setListenerActor(e.getListenerActor());
	}

	/** @see #copy(Event, Event) */
	public static void copy(InputEvent e, InputEvent c) {
		copy((Event) e, c);
		c.setStageX(e.getStageX());
		c.setStageY(e.getStageY());
		c.setButton(e.getButton());
		c.setCharacter(e.getCharacter());
		c.setKeyCode(e.getKeyCode());
		c.setPointer(e.getPointer());
		c.setType(e.getType());
		c.setRelatedActor(e.getRelatedActor());
		c.setScrollAmount(e.getScrollAmount());
	}

	/** @param pos the coordinates
	 *  @param actor the actor in which coordinate system pos is given
	 *  @param other the actor into which coordinate system to convert the coordinates to
	 *  @return the given position, converted
	 *  @throws IllegalArgumentException if the given actors are not in the same hierarchy */
	public static Vector2 localToOtherCoordinates(Vector2 pos, Actor actor, Actor other) {
		Group lastParent = lastParent(actor);
		if(lastParent == null || lastParent != lastParent(other))
			throw new IllegalArgumentException(actor + " and " + other + " are not in the same hierarchy");
		actor.localToAscendantCoordinates(lastParent, pos);
		lastParent.localToDescendantCoordinates(other, pos);
		return pos;
	}

	/** @param pos the position
	 *  @param actor the actor to which coordinate system to convert
	 *  @deprecated use {@link Actor#stageToLocalCoordinates(Vector2)} */
	@Deprecated
	public static Vector2 stageToLocalCoordinates(Vector2 pos, Actor actor) {
		if(actor == actor.getStage().getRoot())
			return pos;
		return actor.getStage().getRoot().localToDescendantCoordinates(actor, pos);
	}

	/** @see #stageToLocalCoordinates(Vector2, Actor)
	 *  @deprecated use {@link Actor#stageToLocalCoordinates(Vector2)} */
	@Deprecated
	public static Vector2 stageToLocalCoordinates(float x, float y, Actor actor) {
		return stageToLocalCoordinates(tmp.set(x, y), actor);
	}

	/** @return the highest parent in the hierarchy tree of the given actor */
	public static Group lastParent(Actor actor) {
		if(!actor.hasParent())
			return null;
		Group parent = actor.getParent();
		while(parent.hasParent())
			parent = parent.getParent();
		assert !parent.hasParent();
		return parent;
	}

	/** @param actor the actor which position in stage coordinates to return
	 *  @return the position of the given actor in the stage coordinate system */
	public static Vector2 positionInStageCoordinates(Actor actor) {
		if(actor.hasParent())
			actor.localToStageCoordinates(tmp.set(0, 0));
		else
			tmp.set(actor.getX(), actor.getY());
		return tmp;
	}

	/** Adds the given Actor to the given Group at the coordinates relative to the Stage.
	 *  @param actor the Actor to add to the given Group
	 *  @param newParent the Group to add the given Actor to */
	public static void addAtStageCoordinates(Actor actor, Group newParent) {
		tmp.set(positionInStageCoordinates(actor));
		newParent.stageToLocalCoordinates(tmp);
		newParent.addActor(actor);
		actor.setPosition(tmp.x, tmp.y);
	}

	/** @see #pointerPosition(Stage, int) */
	public static Vector2 pointerPosition(Stage stage) {
		return pointerPosition(stage, 0);
	}

	/** @param stage the Stage which coordinate system should be used
	 *  @param pointer the pointer which position to return
	 *  @return the position of the given pointer in stage coordinates */
	public static Vector2 pointerPosition(Stage stage, int pointer) {
		tmp.set(Gdx.input.getX(pointer), Gdx.input.getY(pointer));
		stage.screenToStageCoordinates(tmp);
		return tmp;
	}

	/** @param width the width of the area
	 *  @param height the height of the area
	 *  @param align the {@link com.badlogic.gdx.scenes.scene2d.utils.Align Align} flag
	 *  @return the aligned local position
	 *  @since 0.8.0 */
	public static Vector2 align(float width, float height, int align) {
		tmp.setZero();
		if((align & center) == center)
			tmp.set(width / 2, height / 2);
		if((align & right) == right)
			tmp.x = width;
		if((align & left) == left)
			tmp.x = 0;
		if((align & top) == top)
			tmp.y = height;
		if((align & bottom) == bottom)
			tmp.y = 0;
		return tmp;
	}

}
