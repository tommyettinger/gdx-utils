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

package net.dermetfan.gdx.physics.box2d;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.JointDef;
import com.badlogic.gdx.physics.box2d.QueryCallback;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
import com.badlogic.gdx.utils.Array;

/** an {@link InputAdapter} managing a {@link MouseJoint}
 *  @author dermetfan */
public class MouseJointAdapter extends InputAdapter {

	/** Manages {@link MouseJointAdapter MouseJointAdapters} by creating new ones if necessary. Override {@link #newMouseJointAdapter(byte)} to define how now ones will be created (by default a copy of the enclosing instance).
	 *  @author dermetfan */
	public class Manager extends InputAdapter {

		/** the {@link MouseJointAdapter MouseJointAdapters} */
		private Array<MouseJointAdapter> adapters = new Array<>(false, 2);

		/** the max size of {@link #adapters} */
		private byte max = -1;

		/** a temporary variable */
		private boolean tmp;

		/** calls {@link MouseJointAdapter#touchDown(int, int, int, int) touchDown} on all {@link #adapters} and creates a new one if necessary */
		@Override
		public boolean touchDown(int screenX, int screenY, int pointer, int button) {
			if(adapters.size <= pointer && (pointer + 1 < max || max < 0))
				adapters.add(newMouseJointAdapter((byte) pointer));
			for(MouseJointAdapter adapter : adapters)
				tmp |= adapter.touchDown(screenX, screenY, pointer, button);
			return tmp;
		}

		/** calls {@link MouseJointAdapter#touchDragged(int, int, int) touchDragged} on all {@link #adapters} */
		@Override
		public boolean touchDragged(int screenX, int screenY, int pointer) {
			for(MouseJointAdapter adapter : adapters)
				tmp |= adapter.touchDragged(screenX, screenY, pointer);
			return tmp;
		}

		/** calls {@link MouseJointAdapter#mouseMoved(int, int) mouseMoved} on all {@link #adapters} */
		@Override
		public boolean mouseMoved(int screenX, int screenY) {
			for(MouseJointAdapter adapter : adapters)
				tmp |= adapter.mouseMoved(screenX, screenY);
			return tmp;
		}

		/** calls {@link MouseJointAdapter#touchUp(int, int, int, int) touchUp} on all {@link #adapters} */
		@Override
		public boolean touchUp(int screenX, int screenY, int pointer, int button) {
			for(MouseJointAdapter adapter : adapters)
				tmp |= adapter.touchUp(screenX, screenY, pointer, button);
			return tmp;
		}

		/** override this to define how new {@link MouseJointAdapter MouseJointAdapters} should be created
		 *  @param pointer the pointer to set to the returned {@link MouseJointAdapter}
		 *  @return a new {@link MouseJointAdapter} just like this {@link Manager Manager's} enclosing instance */
		public MouseJointAdapter newMouseJointAdapter(byte pointer) {
			MouseJointAdapter adapter = new MouseJointAdapter(MouseJointAdapter.this);
			adapter.pointer = pointer;
			return adapter;
		}

		/** @return the {@link #max} */
		public byte getMax() {
			return max;
		}

		/** @param max the {@link #max} to set */
		public void setMax(byte max) {
			if(max < 0)
				throw new IllegalArgumentException("max must be greater or equal to zero");
			this.max = max;
		}

		/** @return the {@link #adapters} */
		public Array<MouseJointAdapter> getAdapters() {
			return adapters;
		}

		/** @param adapters the {@link #adapters} to set. If the size of the given {@link Array} is greater than {@link #max}, it will be increased */
		public void setAdapters(Array<MouseJointAdapter> adapters) {
			if(adapters == null)
				throw new IllegalArgumentException("adapters must not be null");
			if(adapters.size > max && max > 0)
				max = (byte) adapters.size;
			this.adapters = adapters;
		}

	}

	/** called on {@link MouseJointAdapter#touchDown(int, int, int, int) touchDown}, {@link MouseJointAdapter#touchDragged(int, int, int) touchDragged} and {@link MouseJointAdapter#touchUp(int, int, int, int) touchUp}
	 *  @author dermetfan */
	public interface Listener {

		/** @return true to cancel the creation of the {@link MouseJointAdapter#joint joint} */
		public boolean touched(Fixture fixture, Vector2 position);

		/** @return true to cancel updating the target of {@link MouseJointAdapter#joint} */
		public boolean dragged(MouseJoint joint, Vector2 oldPosition, Vector2 position);

		/** @return true to cancel destroying the {@link MouseJointAdapter#joint joint} */
		public boolean released(MouseJoint joint, Vector2 position);

		/** Does nothing and returns false. Use this if you want to override only some methods of the Listener.
		 *  @author dermetfan
		 *  @since 0.9.1 */
		public static class Adapter implements Listener {

			@Override
			public boolean touched(Fixture fixture, Vector2 position) {
				return false;
			}

			@Override
			public boolean dragged(MouseJoint joint, Vector2 oldPosition, Vector2 position) {
				return false;
			}

			@Override
			public boolean released(MouseJoint joint, Vector2 position) {
				return false;
			}

		}

	}

	/** The pointer to react to. If smaller than zero, all pointers will be accepted. */
	private byte pointer;

	/** the {@link Listener} called by {@link #queryCallback} */
	private Listener listener;

	/** the {@link Camera} used to convert to world coordinates */
	private Camera camera;

	/** if the {@link MouseJointDef#maxForce maxForce} of {@link #jointDef} should be multiplied with its {@link JointDef#bodyB bodyB}'s {@link Body#getMass() mass} */
	private boolean adaptMaxForceToBodyMass;

	/** if {@link #touchDragged(int, int, int)} should be called with {@link #pointer} on {@link #mouseMoved(int, int)} */
	private boolean mouseMoved;

	/** the {@link MouseJointDef} used to create {@link #joint} */
	private MouseJointDef jointDef;

	/** the managed {@link MouseJoint} */
	private MouseJoint joint;

	/** for internal, temporary usage */
	private final Vector3 vec3 = new Vector3();

	/** for internal, temporary usage */
	private static final Vector2 vec2_0 = new Vector2(), vec2_1 = new Vector2();

	/** called by {@link #touchDown(int, int, int, int)}, instantiates {@link #joint} if {@link Listener#touched(Fixture, Vector2) touched} of {@link #listener} returns <code>true</code> */
	private final QueryCallback queryCallback = new QueryCallback() {

		@Override
		public boolean reportFixture(Fixture fixture) {
			Body body = fixture.getBody();
			if(body != jointDef.bodyA && fixture.testPoint(vec2_0) && (listener == null || !listener.touched(fixture, vec2_0))) {
				jointDef.bodyB = body;
				jointDef.target.set(vec2_0);
				if(adaptMaxForceToBodyMass) {
					float maxForce = jointDef.maxForce;
					jointDef.maxForce *= fixture.getBody().getMass();
					joint = (MouseJoint) jointDef.bodyA.getWorld().createJoint(jointDef);
					jointDef.maxForce = maxForce;
					return false;
				}
				joint = (MouseJoint) jointDef.bodyA.getWorld().createJoint(jointDef);
			}
			return true;
		}

	};

	/** constructs a {@link MouseJointAdapter} with pointer set to 0 (useful for desktop)
	 * 	@see #MouseJointAdapter(MouseJointDef, boolean, Camera, byte, Listener) */
	public MouseJointAdapter(MouseJointDef jointDef, boolean adaptMaxForceToBodyMass, Camera camera) {
		this(jointDef, adaptMaxForceToBodyMass, camera, (byte) 0);
	}

	/** @see #MouseJointAdapter(MouseJointDef, boolean, Camera, byte, Listener) */
	public MouseJointAdapter(MouseJointDef jointDef, boolean adaptMaxForceToBodyMass, Camera camera, byte pointer) {
		this(jointDef, adaptMaxForceToBodyMass, camera, pointer, null);
	}

	/** @see #MouseJointAdapter(MouseJointDef, boolean, Camera, byte, Listener) */
	public MouseJointAdapter(MouseJointDef jointDef, boolean adaptMaxForceToBodyMass, Camera camera, Listener listener) {
		this(jointDef, adaptMaxForceToBodyMass, camera, (byte) 0, listener);
	}

	/** constructs a {@link MouseJointAdapter} using the given {@link MouseJointDef}
	 *  @param jointDef The {@link MouseJointDef} to use. <strong>Note that its {@link JointDef#bodyB bodyB} will be changed by the {@link MouseJointAdapter} so it can be null but {@link JointDef#bodyA bodyA} has to be set!</strong>
	 *  @param adaptMaxForceToBodyMass the {@link #adaptMaxForceToBodyMass}
	 *  @param camera the {@link #camera}
	 *  @param pointer the {@link #pointer}
	 *  @param listener the {@link #listener} */
	public MouseJointAdapter(MouseJointDef jointDef, boolean adaptMaxForceToBodyMass, Camera camera, byte pointer, Listener listener) {
		this.jointDef = jointDef;
		this.adaptMaxForceToBodyMass = adaptMaxForceToBodyMass;
		this.camera = camera;
		this.pointer = pointer;
		setListener(listener);
	}

	/** constructs a new {@link MouseJointAdapter} that equals the given other one */
	public MouseJointAdapter(MouseJointAdapter other) {
		this(other.jointDef, other.adaptMaxForceToBodyMass, other.camera, other.pointer);
		setListener(other.listener);
		mouseMoved = other.mouseMoved;
	}

	/** uses {@link #queryCallback} to create {@link #joint} if {@link #joint} is <code>null</code>*/
	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		if(joint != null || !reactsToPointer(pointer))
			return false;

		camera.unproject(vec3.set(screenX, screenY, 0));
		vec2_0.set(vec3.x, vec3.y);
		jointDef.bodyA.getWorld().QueryAABB(queryCallback, vec2_0.x, vec2_0.y, vec2_0.x, vec2_0.y);
		return true;
	}

	/** updates the target of {@link #joint} if {@link Listener#dragged(MouseJoint, Vector2, Vector2) dragged} of {@link #listener} returns <code>true</code> */
	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		if(joint == null || !reactsToPointer(pointer))
			return false;

		camera.unproject(vec3.set(screenX, screenY, 0));
		if(listener == null || !listener.dragged(joint, vec2_1.set(vec2_0), vec2_0.set(vec3.x, vec3.y)))
			joint.setTarget(vec2_0.set(vec3.x, vec3.y));

		return true;
	}

	/** calls {@link #touchDragged(int, int, int)} with {@link #pointer} if {@link #mouseMoved} is <code>true</code> */
	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		return mouseMoved && touchDragged(screenX, screenY, pointer);
	}

	/** destroys {@link #joint} if {@link Listener#released(MouseJoint, Vector2) released} of {@link #listener} returns <code>true</code> */
	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		if(joint == null || !reactsToPointer(pointer))
			return false;

		camera.unproject(vec3.set(screenX, screenY, 0));
		if(listener == null || !listener.released(joint, vec2_0.set(vec3.x, vec3.y))) {
			jointDef.bodyA.getWorld().destroyJoint(joint);
			joint = null;
			return true;
		}

		return false;
	}

	/** @return if this MouseJointAdapter reacts to the specified pointer */
	public boolean reactsToPointer(int pointer) {
		return this.pointer == pointer || this.pointer < 0;
	}

	/** @return the {@link #pointer} */
	public byte getPointer() {
		return pointer;
	}

	/** @param pointer the {@link #pointer} to set */
	public void setPointer(byte pointer) {
		this.pointer = pointer;
	}

	/** @return the {@link #listener} */
	public Listener getListener() {
		return listener;
	}

	/** @param listener the {@link #listener} to set */
	public void setListener(Listener listener) {
		this.listener = listener;
	}

	/** @return the {@link #camera} */
	public Camera getCamera() {
		return camera;
	}

	/** @param camera the {@link #camera} to set */
	public void setCamera(Camera camera) {
		if(camera == null)
			throw new IllegalArgumentException("camera must not be null");
		this.camera = camera;
	}

	/** @return the {@link #adaptMaxForceToBodyMass} */
	public boolean isAdaptMaxForceToBodyMass() {
		return adaptMaxForceToBodyMass;
	}

	/** @param adaptMaxForceToBodyMass the {@link #adaptMaxForceToBodyMass} to set */
	public void setAdaptMaxForceToBodyMass(boolean adaptMaxForceToBodyMass) {
		this.adaptMaxForceToBodyMass = adaptMaxForceToBodyMass;
	}

	/** @return the {@link #mouseMoved} */
	public boolean isMouseMoved() {
		return mouseMoved;
	}

	/** @param mouseMoved the {@link #mouseMoved} to set */
	public void setMouseMoved(boolean mouseMoved) {
		this.mouseMoved = mouseMoved;
	}

	/** @return the {@link #jointDef} */
	public MouseJointDef getJointDef() {
		return jointDef;
	}

	/** @param jointDef the {@link #jointDef} to set */
	public void setJointDef(MouseJointDef jointDef) {
		if(jointDef == null)
			throw new IllegalArgumentException("jointDef must not be null");
		this.jointDef = jointDef;
	}

	/** @return the {@link #joint} */
	public MouseJoint getJoint() {
		return joint;
	}

	/** @param joint the {@link #joint} to set */
	public void setJoint(MouseJoint joint) {
		this.joint = joint;
	}

}
