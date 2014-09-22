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

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import net.dermetfan.utils.Function;

import static net.dermetfan.utils.math.MathUtils.normalize;

/** navigates bodies to a destination
 *  @author dermetfan */
public class Autopilot {

	/** for internal, temporary usage */
	private static final Vector2 vec2_0 = new Vector2(), vec2_1 = new Vector2();

	/** calculates the force to continuously {@link Body#applyForce(Vector2, Vector2, boolean) apply} to reach the given destination
	 *  @param destination the relative position of the destination
	 *  @param force the force to apply
	 *  @return the force to {@link Body#applyForce(Vector2, Vector2, boolean) apply} to navigate to the given {@code destination} */
	public static Vector2 calculateForce(Vector2 destination, Vector2 force) {
		return vec2_0.set(destination).nor().scl(force);
	}

	/** calculates the force to continuously {@link Body#applyForce(Vector2, Vector2, boolean) apply} to reach the given {@code destination} and interpolates it based on distance
	 *  @param destination the destination to go to
	 *  @param force the force to apply
	 *  @param distanceScalar the distance at which the given force should be fully applied
	 *  @param interpolation the interpolation used to interpolate the given {@code force} based on the {@code distanceScalar}
	 *  @return the force to {@link Body#applyForce(Vector2, Vector2, boolean) apply} to navigate to the given {@code destination}
	 *  @see #calculateForce(Vector2, Vector2) */
	public static Vector2 calculateForce(Vector2 destination, Vector2 force, float distanceScalar, Interpolation interpolation) {
		return calculateForce(destination, force).scl(interpolation.apply(destination.len() / distanceScalar));
	}

	/** calculates the torque needed to repeatedly {@link Body#applyTorque(float, boolean) apply} to a body to make it rotate to a given point
	 *  @param target the point to rotate the body to
	 *  @param rotation the current rotation of the body
	 *  @param angularVelocity the current {@link Body#getAngularVelocity() angular velocity} of the body
	 *  @param inertia the current {@link Body#getInertia() rotational inertia} of the body
	 *  @param force the force to use
	 *  @param delta the time that passed since the last world update
	 *  @return the torque needed to apply to a body to make it rotate to the given {@code target} */
	public static float calculateTorque(Vector2 target, float rotation, float angularVelocity, float inertia, float force, float delta) {
		// http://www.iforce2d.net/b2dtut/rotate-to-angle
		float rotate = MathUtils.atan2(target.y, target.x) - (rotation + angularVelocity * delta);
		rotate = normalize(rotate, -MathUtils.PI, MathUtils.PI);
		return inertia * (rotate / MathUtils.PI2 * force * delta) / delta;
	}

	/** the point to move and rotate to */
	public final Vector2 destination = new Vector2();

	/** the desired angle to {@link #destination} */
	private float angle;

	/** if the {@link #destination} is relative to the {@link #positionAccessor body} */
	private boolean moveRelative, rotateRelative;

	/** the force used for movement */
	private final Vector2 movementForce = new Vector2();

	/** the force used for rotation */
	private float rotationForce;

	/** if the force used should be adapted to the body mass */
	private boolean adaptForceToMass;

	/** the distance at which the force should be fully applied
	 *  @see #calculateForce(Vector2, Vector2, float, Interpolation) */
	private float distanceScalar = 1;

	/** the interpolation to apply to the force based on the {@link #distanceScalar} */
	private Interpolation interpolation = Interpolation.linear;

	/** returns {@link Body#getPosition()} */
	public static final Function<Vector2, Body> defaultPositionAccessor = new Function<Vector2, Body>() {

		@Override
		public Vector2 apply(Body body) {
			return body.getWorldCenter();
		}

	};

	/** used to determine a bodies position */
	private Function<Vector2, Body> positionAccessor = defaultPositionAccessor;

	/** sets {@link #movementForce} and {@link #rotationForce} to the given {@code force}
	 *  @see #Autopilot(Vector2, float, float) */
	public Autopilot(Vector2 destination, float angle, float forces) {
		this(destination, angle, vec2_0.set(forces, forces), forces);
	}

	/** @see #Autopilot(Vector2, float, Vector2, float, boolean) */
	public Autopilot(Vector2 destination, float angle, Vector2 movementForce, float rotationForce) {
		this(destination, angle, movementForce, rotationForce, true);
	}

	/** The given {@code destination} will not be used directly. Instead {@link #destination} will be set to it. */
	public Autopilot(Vector2 destination, float angle, Vector2 movementForce, float rotationForce, boolean adaptForceToMass) {
		this.destination.set(destination);
		this.angle = angle;
		this.movementForce.set(movementForce);
		this.rotationForce = rotationForce;
		this.adaptForceToMass = adaptForceToMass;
	}

	/** applies the force from {@link #calculateForce(Vector2, Vector2)}
	 *  @see #calculateForce(Vector2, Vector2)
	 *  @see #move(Body, Vector2, Vector2, Interpolation, float, boolean) */
	public void move(Body body, Vector2 destination, Vector2 force, boolean wake) {
		Vector2 position = positionAccessor.apply(body);
		Vector2 apply = calculateForce(moveRelative ? destination : vec2_0.set(destination).sub(position), adaptForceToMass ? vec2_1.set(force).scl(body.getMass()) : force);
		body.applyForce(apply, position, wake);
	}

	/** applies the force of {@link #calculateForce(Vector2, Vector2, float, Interpolation)}
	 *  @param body the body to move
	 *  @param destination the destination of the body
	 *  @param force the force used to move the body
	 *  @param interpolation the interpolation  used to interpolate the given {@code force} based on the {@code distanceScalar}
	 *  @param distanceScalar the distance at which the force should be fully applied
	 *  @param wake if the body should be woken up in case it is sleeping
	 *  @see #move(Body, Vector2, Vector2, boolean)
	 *  @see #calculateForce(Vector2, Vector2, float, Interpolation) */
	public void move(Body body, Vector2 destination, Vector2 force, Interpolation interpolation, float distanceScalar, boolean wake) {
		Vector2 position = positionAccessor.apply(body);
		body.applyForce(calculateForce(moveRelative ? destination : vec2_0.set(destination).sub(position), adaptForceToMass ? vec2_1.set(force).scl(body.getMass()) : force, distanceScalar, interpolation), position, wake);
	}

	/** {@link #move(Body, Vector2, Vector2, boolean) moves}/{@link #move(Body, Vector2, Vector2, Interpolation, float, boolean) moves} the given {@code Body} */
	public void move(Body body, boolean interpolate, boolean wake) {
		if(interpolate)
			move(body, destination, movementForce, interpolation, distanceScalar, wake);
		else
			move(body, destination, movementForce, wake);
	}

	/** @param wake if the body should be woken up if its sleeping
	 *  @see #calculateTorque(Vector2, float, float, float, float, float) */
	public void rotate(Body body, Vector2 target, float angle, float force, float delta, boolean wake) {
		body.applyTorque(calculateTorque(rotateRelative ? target : vec2_0.set(positionAccessor.apply(body)).sub(target), body.getTransform().getRotation() + angle, body.getAngularVelocity(), body.getInertia(), adaptForceToMass ? force * body.getMass() : force, delta), wake);
	}

	/** {@link #rotate(Body, Vector2, float, float, float, boolean) rotates} the given {@code body} */
	public void rotate(Body body, float delta, boolean wake) {
		rotate(body, destination, angle, rotationForce, delta, wake);
	}

	/** {@link #rotate(Body, float, boolean) rotates} the body waking it up if it sleeps */
	public void rotate(Body body, float delta) {
		rotate(body, delta, true);
	}

	/** {@link #rotate(Body, float, boolean) rotates} and {@link #move(Body, boolean, boolean) moves} the given {@code body} */
	public void navigate(Body body, float delta, boolean interpolate, boolean wake) {
		rotate(body, delta, wake);
		move(body, interpolate, wake);
	}

	/** @see #navigate(Body, float, boolean, boolean) */
	public void navigate(Body body, float delta, boolean wake) {
		navigate(body, delta, false, wake);
	}

	/** @see #navigate(Body, float, boolean) */
	public void navigate(Body body, float delta) {
		navigate(body, delta, true);
	}

	/** @param x the x coordinate to set {@link #destination} to
	 *  @param y the y coordinate to set {@link #destination} to */
	public void setDestination(float x, float y) {
		destination.set(x, y);
	}

	/** @param destination the destination to set {@link #destination} to */
	public void setDestination(Vector2 destination) {
		this.destination.set(destination);
	}

	/** @return the {@link #destination} */
	public Vector2 getDestination() {
		return destination;
	}

	/** @return the {@link #angle} */
	public float getAngle() {
		return angle;
	}

	/** @param angle the {@link #angle} to set */
	public void setAngle(float angle) {
		this.angle = angle;
	}

	/** @param relative the {@link #moveRelative} and {@link #rotateRelative} */
	public void setRelative(boolean relative) {
		moveRelative = rotateRelative = relative;
	}

	/** @param moveRelative the {@link #moveRelative}
	 *  @param rotateRelative the {@link #rotateRelative} */
	public void setRelative(boolean moveRelative, boolean rotateRelative) {
		this.moveRelative = moveRelative;
		this.rotateRelative = rotateRelative;
	}

	/** @return the {@link #moveRelative} */
	public boolean isMoveRelative() {
		return moveRelative;
	}

	/** @param moveRelative the {@link #moveRelative} to set */
	public void setMoveRelative(boolean moveRelative) {
		this.moveRelative = moveRelative;
	}

	/** @return the {@link #rotateRelative} */
	public boolean isRotateRelative() {
		return rotateRelative;
	}

	/** @param rotateRelative the {@link #rotateRelative} to set */
	public void setRotateRelative(boolean rotateRelative) {
		this.rotateRelative = rotateRelative;
	}

	/** @param movementForceX the x value of {@link #movementForce}
	 *  @param movementForceY the y value of {@link #movementForce}
	 *  @param rotationForce the {@link #rotationForce} */
	public void setForces(float movementForceX, float movementForceY, float rotationForce) {
		movementForce.set(movementForceX, movementForceY);
		this.rotationForce = rotationForce;
	}

	/** @param movementForce the {@link #movementForce}
	 *  @param rotationForce the {@link #rotationForce} */
	public void setForces(Vector2 movementForce, float rotationForce) {
		this.movementForce.set(movementForce);
		this.rotationForce = rotationForce;
	}

	/** @return the {@link #movementForce} */
	public Vector2 getMovementForce() {
		return movementForce;
	}

	/** @param x the x to set {@link #movementForce} to
	 *  @param y the y to set {@link #movementForce} to */
	public void setMovementForce(float x, float y) {
		movementForce.set(x, y);
	}

	/** @param movementForce the {@link #movementForce} to set */
	public void setMovementForce(Vector2 movementForce) {
		this.movementForce.set(movementForce);
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

	/** @return the {@link #positionAccessor} */
	public Function<Vector2, Body> getPositionAccessor() {
		return positionAccessor;
	}

	/** @param positionAccessor the {@link #positionAccessor} to set */
	public void setPositionAccessor(Function<Vector2, Body> positionAccessor) {
		this.positionAccessor = positionAccessor != null ? positionAccessor : defaultPositionAccessor;
	}

}
