/** Copyright 2014 Robin Stumm (serverkorken@googlemail.com, http://dermetfan.net/)
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

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.Stage;

/** provides useful methods for scene2d
 *  @author dermetfan */
public class Scene2dUtils {

	/** Some methods return this, so if you get your hands on it make sure to make a copy! This is used internally so it might change unexpectedly. */
	private static Vector2 tmp = new Vector2();

	/** Adds the given Actor to the given Group at the coordinates relative to the Stage.
	 *  @param actor the Actor to add to the given Group
	 *  @param newParent the Group to add the given Actor to */
	public static void addAtStageCoordinates(Actor actor, Group newParent) {
		tmp.set(actor.getX(), actor.getY());
		actor.localToStageCoordinates(tmp);
		newParent.stageToLocalCoordinates(tmp);
		actor.setPosition(tmp.x, tmp.y);
		newParent.addActor(actor);
	}

	public static Vector2 getCursorPosition(Stage stage) {
		return getCursorPosition(stage, 0);
	}

	public static Vector2 getCursorPosition(Stage stage, int pointer) {
		tmp.set(Gdx.input.getX(pointer), Gdx.input.getY(pointer));
		stage.screenToStageCoordinates(tmp);
		return tmp;
	}

	public static class TooltipListener extends InputListener {

		private boolean inside;

		private boolean followCursor = true;
		private Vector2 position = new Vector2();
		private Actor tooltip;

		public TooltipListener(Actor tooltip) {
			this.tooltip = tooltip;
		}

		@Override
		public boolean mouseMoved(InputEvent event, float x, float y) {
			if(inside && followCursor) {
				event.getListenerActor().localToStageCoordinates(tmp.set(x, y));
				tooltip.setPosition(tmp.x + position.x, tmp.y + position.y);
			}
			return false;
		}

		@Override
		public void enter(InputEvent event, float x, float y, int pointer, Actor fromActor) {
			inside = true;
			tooltip.setVisible(true);
			tmp.set(x, y);
			event.getListenerActor().localToStageCoordinates(tmp);
			tooltip.setPosition(tmp.x + position.x, tmp.y + position.y);
		}

		@Override
		public void exit(InputEvent event, float x, float y, int pointer, Actor toActor) {
			inside = false;
			tooltip.setVisible(false);
		}

	}

}
