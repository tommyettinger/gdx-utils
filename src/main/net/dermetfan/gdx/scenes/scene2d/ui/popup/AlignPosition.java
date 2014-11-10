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

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.utils.Pools;
import net.dermetfan.gdx.scenes.scene2d.Scene2DUtils;
import net.dermetfan.gdx.scenes.scene2d.ui.popup.PositionBehavior.Position;

/** positions the popup relative to {@link Event#getTarget() target}
 *  @author dermetfan
 *  @since 0.8.0 */
public class AlignPosition implements Position {

	/** the {@link Align} flag for alignment on {@link Event#getTarget()} */
	private int targetAlign;

	/** the {@link Align} flag */
	private int align;

	/** @param align the {@link #align} */
	public AlignPosition(int targetAlign, int align) {
		this.targetAlign = targetAlign;
		this.align = align;
	}

	@Override
	public void apply(Event event, Actor popup) {
		Actor target = event.getTarget();
		Vector2 pos = Pools.obtain(Vector2.class).setZero();
		pos.set(Scene2DUtils.align(target.getWidth(), target.getHeight(), targetAlign));
		target.localToStageCoordinates(pos);
		popup.stageToLocalCoordinates(pos);
		popup.localToParentCoordinates(pos);
		popup.setPosition(pos.x, pos.y, align);
		Pools.free(pos);
	}

	// getters and setters

	/** @return the {@link #align} */
	public int getAlign() {
		return align;
	}

	/** @param align the {@link #align} to set */
	public void setAlign(int align) {
		this.align = align;
	}

	/** @return the {@link #targetAlign} */
	public int getTargetAlign() {
		return targetAlign;
	}

	/** @param targetAlign the {@link #targetAlign} to set */
	public void setTargetAlign(int targetAlign) {
		this.targetAlign = targetAlign;
	}

}
