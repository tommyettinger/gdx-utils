/**
 * Copyright 2013 Robin Stumm (serverkorken@googlemail.com, http://dermetfan.bplaced.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dermetfan.util.libgdx.box2d;

import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.JointDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
import com.badlogic.gdx.utils.Array;

/** an {@link InputAdapter} managing a {@link MouseJoint}
 *  @author dermetfan */
public class MouseJointAdapter extends InputAdapter {

	/** the {@link Camera} used to convert to world coordinates */
	private Camera camera;

	/** if the {@link MouseJointDef#maxForce maxForce} of {@link #jointDef} should be multiplied with its {@link JointDef#bodyB bodyB}'s {@link Body#getMass() mass} */
	private boolean adaptMaxForceToBodyMass;

	/** the {@link MouseJointDef} used to create {@link #joint} */
	private MouseJointDef jointDef;

	/** the managed {@link MouseJoint} */
	private MouseJoint joint;

	/** a temporary variable */
	private final Vector3 tmp = new Vector3();

	/** a temporary variable */
	private final Vector2 tmp2 = new Vector2();

	/** a temporary variable */
	private final Array<Body> tmpBodies = new Array<Body>();

	/** constructs a {@link MouseJointAdapter} using the given {@link MouseJointDef}
	 *  @param jointDef The {@link MouseJointDef} to use. <strong>Note that its {@link JointDef#bodyB bodyB} will be changed by the {@link MouseJointAdapter} so it can be null but {@link JointDef#bodyA bodyA} has to be set!</strong> 
	 *  @param adaptMaxForceToBodyMass the {@link #adaptMaxForceToBodyMass}
	 *  @param camera the {@link #camera} */
	public MouseJointAdapter(MouseJointDef jointDef, boolean adaptMaxForceToBodyMass, Camera camera) {
		this.jointDef = jointDef;
		this.adaptMaxForceToBodyMass = adaptMaxForceToBodyMass;
		this.camera = camera;
	}

	@Override
	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
		camera.unproject(tmp.set(screenX, screenY, 0));

		World world = jointDef.bodyA.getWorld();

		world.getBodies(tmpBodies);
		for(Body body : tmpBodies)
			for(Fixture fixture : body.getFixtureList())
				if(fixture.testPoint(tmp.x, tmp.y)) {
					jointDef.bodyB = body;
					jointDef.target.set(tmp.x, tmp.y);
					if(adaptMaxForceToBodyMass) {
						float maxForce = jointDef.maxForce;
						jointDef.maxForce *= body.getMass();
						joint = (MouseJoint) world.createJoint(jointDef);
						jointDef.maxForce = maxForce;
						return true;
					}
					joint = (MouseJoint) world.createJoint(jointDef);
					return true;
				}

		return false;
	}

	@Override
	public boolean touchDragged(int screenX, int screenY, int pointer) {
		if(joint == null)
			return false;

		camera.unproject(tmp.set(screenX, screenY, 0));
		joint.setTarget(tmp2.set(tmp.x, tmp.y));
		System.out.println(joint.getMaxForce());

		return true;
	}

	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		if(joint == null)
			return false;

		jointDef.bodyA.getWorld().destroyJoint(joint);
		joint = null;

		return true;
	}

	/** @return the {@link #camera} */
	public Camera getCamera() {
		return camera;
	}

	/** @param camera the {@link #camera} to set */
	public void setCamera(Camera camera) {
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

	/** @return the {@link #jointDef} */
	public MouseJointDef getJointDef() {
		return jointDef;
	}

	/** @param jointDef the {@link #jointDef} to set */
	public void setJointDef(MouseJointDef jointDef) {
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