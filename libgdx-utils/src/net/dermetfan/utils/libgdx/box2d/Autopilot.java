/** Copyright 2013 Robin Stumm (serverkorken@googlemail.com, http://dermetfan.net/)
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

package net.dermetfan.utils.libgdx.box2d;

import static net.dermetfan.utils.libgdx.math.GeometryUtils.vec2_0;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

/** navigates bodies
 *  @author dermetfan */
public class Autopilot {

	/** Calculates the torque needed to repeatedly {@link Body#applyTorque(float, boolean) apply} to a body to make it rotate to a given point.
	 *  @param target the point to rotate the body to
	 *  @param origin the point around which to rotate the body (in world coordinates)
	 *  @param rotation the current rotation of the body
	 *  @param angularVelocity the current {@link Body#getAngularVelocity() angular velocity} of the body
	 *  @param inertia the current {@link Body#getInertia() rotational inertia} of the body
	 *  @param force the force to use
	 *  @param delta the time that passed since the last world update
	 *  @return the torque needed to apply to a body to make it rotate to the given {@code target} */
	public static float torqueForAngle(Vector2 target, Vector2 origin, float rotation, float angularVelocity, float inertia, float force, float delta) {
		// http://www.iforce2d.net/b2dtut/rotate-to-angle
		float rotate = MathUtils.atan2(vec2_0.set(target).sub(origin).y, vec2_0.x) - (rotation + angularVelocity * delta);
		while(rotate < -MathUtils.PI)
			rotate += MathUtils.PI2;
		while(rotate > MathUtils.PI)
			rotate -= MathUtils.PI2;
		return inertia * (rotate / MathUtils.PI2 * force * delta) / delta;
	}

	/** @param wake if the body should be woken up if its sleeping
	 *  @see #torqueForAngle(Vector2, Vector2, float, float, float, float, float) */
	public static void rotateTo(Body body, Vector2 target, float force, float delta, boolean wake) {
		body.applyTorque(torqueForAngle(body.getPosition(), target, body.getTransform().getRotation(), body.getAngularVelocity(), body.getInertia(), force, delta), wake);
	}

	/** the destination to fly to */
	public final Vector2 destination = new Vector2();

	/** the force to use */
	private float force;

	/** if the {@link #force} should be adapted to the {@link Body#getMass() mass} of the {@link #body} (true by default) */
	private boolean adaptForceToMass = true;

	/** @param destination the {@link #destination}
	 *  @param force the {@link #force} */
	public Autopilot(Vector2 destination, float force) {
		this.destination.set(destination);
		this.force = force;
	}

	/** @see #apply(Body, float, boolean) */
	public void apply(Body body, float delta) {
		apply(body, delta, true);
	}

	/** @param wake if the body should be woken up in case it sleeps
	 *  navigates the given body to {@link #destination} */
	public void apply(Body body, float delta, boolean wake) {
		rotateTo(body, destination, adaptForceToMass ? body.getMass() * force : force, delta, wake);
	}

}
