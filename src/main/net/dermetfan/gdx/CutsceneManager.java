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

package net.dermetfan.gdx;

import com.badlogic.gdx.utils.Array;

/** Manages {@link Cutscene Cutscenes}
 *  @author dermetfan */
public class CutsceneManager {

	/** the currently active {@link Cutscene Cutscenes} */
	private final Array<Cutscene> cutscenes = new Array<>();

	/** adds a {@link Cutscene} to the {@link #cutscenes} so it will be {@link #update(float) updated} and calls {@link Cutscene#init() init()} on it*/
	public void start(Cutscene cutscene) {
		cutscenes.add(cutscene);
		cutscene.init();
	}

	/** updates the {@link #cutscenes} */
	public void update(float delta) {
		for(Cutscene cutscene : cutscenes)
			if(cutscene.update(delta))
				end(cutscene);
	}

	/** removes a {@link Cutscene} from the {@link #cutscenes} and calls {@link Cutscene#end() end()} on it */
	public void end(Cutscene cutscene) {
		cutscenes.removeValue(cutscene, true);
		cutscene.end();
	}

	/** @return the {@link #cutscenes} */
	public Array<Cutscene> getCutscenes() {
		return cutscenes;
	}

	/** @author dermetfan */
	public interface Cutscene {

		/** called by {@link CutsceneManager#start(Cutscene)} */
		public void init();

		/** called by {@link CutsceneManager#update(float)}
		 *  @param delta the time passes since last update
		 *  @return if the cutscene is finished */
		public boolean update(float delta);

		/** called by {@link CutsceneManager#end(Cutscene)} */
		public void end();

	}

}
