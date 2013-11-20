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

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;

/** navigates bodies to a destination
 *  @author dermetfan */
public class Autopilot {

	/** calculates the force to continuously {@link Body#applyForce(Vector2, Vector2, boolean) apply} to reach the given destination
	 *  @param position the position at which to apply the force
	 *  @param destination the destination
	 *  @param force the force to apply
	 *  @return the force to {@link Body#applyForce(Vector2, Vector2, boolean) apply} to navigate to the given {@code destination}
	 *  {@link #calculateForce(Vector2, Vector2, float, float, Interpolation)} */
	public static Vector2 calculateForce(Vector2 position, Vector2 destination, float force) {
		return vec2_0.set(destination).sub(position).scl(force);
	}

	/** calculates the force to continuously {@link Body#applyForce(Vector2, Vector2, boolean) apply} to reach the given {@code destination} and interpolates it based on distance
	 *  @param position the position at which the body currently is
	 *  @param destination the destination to go to
	 *  @param force the force to apply
	 *  @param distanceScalar the distance at which the given force should be fully applied
	 *  @param interpolation the interpolation used to interpolate the given {@code force} based on the {@code distanceScalar}
	 *  @return the force to {@link Body#applyForce(Vector2, Vector2, boolean) apply} to navigate to the given {@code destination}
	 *  @see #calculateForce(Vector2, Vector2, float) */
	public static Vector2 calculateForce(Vector2 position, Vector2 destination, float force, float distanceScalar, Interpolation interpolation) {
		return calculateForce(position, destination, force).scl(interpolation.apply(destination.dst(position) / distanceScalar));
	}

	/** applies the force from {@link #calculateForce(Vector2, Vector2, float)}
	 *  @see #calculateForce(Vector2, Vector2, float, float, Interpolation)
	 *  @see #move(Body, Vector2, Vector2, float, float, Interpolation, boolean) */
	public static void move(Body body, Vector2 position, Vector2 destination, float force, boolean wake) {
		body.applyForce(calculateForce(position, destination, force), position, wake);
	}

	/** applies the force of {@link #calculateForce(Vector2, Vector2, float, float, Interpolation)}
	 *  @param body the body to move
	 *  @param position the position of the body (in world coordinates) at which to apply the force
	 *  @param destination the destination of the body
	 *  @param force the force used to move the body
	 *  @param distanceScalar the distance at which the force should be fully applied
	 *  @param interpolation the interpolation  used to interpolate the given {@code force} based on the {@code distanceScalar}
	 *  @param wake if the body should be woken up in case it is sleeping
	 *  @see #move(Body, Vector2, Vector2, float, boolean)
	 *  @see #calculateForce(Vector2, Vector2, float, float, Interpolation) */
	public static void move(Body body, Vector2 position, Vector2 destination, float force, float distanceScalar, Interpolation interpolation, boolean wake) {
		body.applyForce(calculateForce(position, destination, force, distanceScalar, interpolation), position, wake);
	}

	/** calculates the torque needed to repeatedly {@link Body#applyTorque(float, boolean) apply} to a body to make it rotate to a given point
	 *  @param target the point to rotate the body to
	 *  @param origin the point around which to rotate the body (in world coordinates)
	 *  @param rotation the current rotation of the body
	 *  @param angularVelocity the current {@link Body#getAngularVelocity() angular velocity} of the body
	 *  @param inertia the current {@link Body#getInertia() rotational inertia} of the body
	 *  @param force the force to use
	 *  @param delta the time that passed since the last world update
	 *  @return the torque needed to apply to a body to make it rotate to the given {@code target} */
	public static float calculateTorque(Vector2 target, Vector2 origin, float rotation, float angularVelocity, float inertia, float force, float delta) {
		// http://www.iforce2d.net/b2dtut/rotate-to-angle
		float rotate = MathUtils.atan2(vec2_0.set(target).sub(origin).y, vec2_0.x) - (rotation + angularVelocity * delta);
		while(rotate < -MathUtils.PI)
			rotate += MathUtils.PI2;
		while(rotate > MathUtils.PI)
			rotate -= MathUtils.PI2;
		return inertia * (rotate / MathUtils.PI2 * force * delta) / delta;
	}

	/** @param wake if the body should be woken up if its sleeping
	 *  @see #calculateTorque(Vector2, Vector2, float, float, float, float, float) */
	public static void rotate(Body body, Vector2 target, float force, float delta, boolean wake) {
		body.applyTorque(calculateTorque(body.getPosition(), target, body.getTransform().getRotation(), body.getAngularVelocity(), body.getInertia(), force, delta), wake);
	}

	/** the point to move and rotate to */
	public final Vector2 destination = new Vector2();

	/** the force used for movement */
	private float movementForce;

	/** the force used for rotation */
	private float rotationForce;

	/** if the force used should be adapted to the body mass */
	private boolean adaptForceToMass;

	/** the distance at which the force should be fully applied
	 *  @see #calculateForce(Vector2, Vector2, float, float, Interpolation) */
	private float distanceScalar = 1;

	/** the interpolation to apply to the force based on the {@link #distanceScalar} */
	private Interpolation interpolation = Interpolation.linear;

	/** used to access the position of a body (might be his center, origin, center of mass or custom) */
	public static interface BodyLocator {

		public Vector2 locate(Body body);

	}

	/** returns {@link Body#getPosition()} */
	public static final BodyLocator defaultBodyLocator = new BodyLocator() {

		@Override
		public Vector2 locate(Body body) {
			return body.getPosition();
		}

	};

	/** used to determine a bodies position */
	private BodyLocator bodyLocator = defaultBodyLocator;

	/** sets {@link #movementForce} and {@link #rotationForce} to the given {@code force}
	 *  @see #Autopilot(Vector2, float, float) */
	public Autopilot(Vector2 destination, float forces) {
		this(destination, forces, forces);
	}

	/** @see #Autopilot(Vector2, float, float, boolean) */
	public Autopilot(Vector2 destination, float movementForce, float rotationForce) {
		this(destination, movementForce, rotationForce, true);
	}

	/** The given {@code destination} will not be used directly. Instead {@link #destination} will be set to it. */
	public Autopilot(Vector2 destination, float movementForce, float rotationForce, boolean adaptForceToMass) {
		this.destination.set(destination);
		this.movementForce = movementForce;
		this.rotationForce = rotationForce;
		this.adaptForceToMass = adaptForceToMass;
	}

	/** {@link #move(Body, Vector2, Vector2, float, boolean) moves} the given {@code body} */
	public void move(Body body, boolean interpolate, boolean wake) {
		if(interpolate)
			move(body, bodyLocator.locate(body), destination, adaptForceToMass ? body.getMass() * movementForce : movementForce, distanceScalar, interpolation, wake);
		else
			move(body, bodyLocator.locate(body), destination, adaptForceToMass ? body.getMass() * movementForce : movementForce, wake);
	}

	/** {@link #rotate(Body, Vector2, float, float, boolean) rotates} the given {@code body} */
	public void rotate(Body body, float delta, boolean wake) {
		rotate(body, destination, adaptForceToMass ? body.getMass() * rotationForce : rotationForce, delta, wake);
	}

	/** {@link #rotate(Body, float, boolean) rotates} and {@link #move(Body, boolean, boolean) moves} the given {@code body} */
	public void navigate(Body body, float delta, boolean interpolate, boolean wake) {
		rotate(body, delta, wake);
		move(body, interpolate, wake);
	}

	/** @return the {@link #movementForce} */
	public float getMovementForce() {
		return movementForce;
	}

	/** @param movementForce the {@link #movementForce} to set */
	public void setMovementForce(float movementForce) {
		this.movementForce = movementForce;
	}

	/** @return the {@link #rotationForce} */
	public float getRotationForce() {
		return rotationForce;
	}

	/** @param rotationForce the {@link #rotationForce} to set */
	public void setRotationForce(float rotationForce) {
		this.rotationForce = rotationForce;
	}

	/** @return the {@link #adaptForceToMass} */
	public boolean isAdaptForceToMass() {
		return adaptForceToMass;
	}

	/** @param adaptForceToMass the {@link #adaptForceToMass} to set */
	public void setAdaptForceToMass(boolean adaptForceToMass) {
		this.adaptForceToMass = adaptForceToMass;
	}

	/** @return the {@link #distanceScalar} */
	public float getDistanceScalar() {
		return distanceScalar;
	}

	/** @param distanceScalar the {@link #distanceScalar} to set */
	public void setDistanceScalar(float distanceScalar) {
		this.distanceScalar = distanceScalar;
	}

	/** @return the {@link #interpolation} */
	public Interpolation getInterpolation() {
		return interpolation;
	}

	/** @param interpolation the {@link #interpolation} to set */
	public void setInterpolation(Interpolation interpolation) {
		this.interpolation = interpolation;
	}

	/** @return the {@link #bodyLocator} */
	public BodyLocator getBodyLocator() {
		return bodyLocator;
	}

	/** @param bodyLocator the {@link #bodyLocator} to set */
	public void setBodyLocator(BodyLocator bodyLocator) {
		this.bodyLocator = bodyLocator != null ? bodyLocator : defaultBodyLocator;
	}

}