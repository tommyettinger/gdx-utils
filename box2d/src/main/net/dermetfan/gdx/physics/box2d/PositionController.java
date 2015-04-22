/** Copyright 2015 Robin Stumm (serverkorken@gmail.com, http://dermetfan.net)
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

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;
import net.dermetfan.utils.Function;

/** moves a body to a position using forces
 *  @author dermetfan
 *  @since 0.11.1 */
public abstract class PositionController {

	/** shared instance for internal use */
	protected static final Vector2 vec2 = new Vector2();

	/** returns the argument if it is a PositionController */
	public static final Function<PositionController, Object> defaultUserDataAccessor = new Function<PositionController, Object>() {
		@Override
		public PositionController apply(Object arg) {
			return arg instanceof PositionController ? (PositionController) arg : null;
		}
	};

	/** the Function used to extract a PositionController out of a Body's user data */
	private static Function<PositionController, Object> userDataAccessor = defaultUserDataAccessor;

	/** Calls {@link #applyForceToCenter(Body, boolean) applyForceToCenter} for every Body with a PositionController in its user data.
	 *  The PositionController is accessed using the {@link #userDataAccessor}.
	 *  @param world the world which Bodies to iterate over */
	public static void applyForceToCenter(World world, boolean wake) {
		@SuppressWarnings("unchecked")
		Array<Body> bodies = Pools.obtain(Array.class);
		world.getBodies(bodies);
		for(Body body : bodies) {
			PositionController controller = userDataAccessor.apply(body.getUserData());
			if(controller != null)
				controller.applyForceToCenter(body, wake);
		}
		bodies.clear();
		Pools.free(bodies);
	}

	/** @param point the world point at which the force should be applied
	 *  @return the force to apply at the given point */
	public abstract Vector2 calculateForce(Body body, Vector2 point);

	/** @return the force to apply at the center of mass of the Body */
	public abstract Vector2 calculateForceToCenter(Body body);

	/** applies the necessary force at the given point
	 *  @param point the world point at which to apply the force
	 *  @return the force applied, calculated by {@link #calculateForce(Body, Vector2)} */
	public Vector2 applyForce(Body body, Vector2 point, boolean wake) {
		Vector2 force = calculateForce(body, point);
		body.applyForce(force, point, wake);
		return force;
	}

	/** applies the necessary force at the center of mass of the Body
	 *  @return the force applied, calculated by {@link #calculateForceToCenter(Body)} */
	public Vector2 applyForceToCenter(Body body, boolean wake) {
		Vector2 force = calculateForceToCenter(body);
		body.applyForceToCenter(force, wake);
		return force;
	}

	// getters and setters

	/** @return the {@link #userDataAccessor} */
	public static Function<PositionController, Object> getUserDataAccessor() {
		return userDataAccessor;
	}

	/** @param userDataAccessor The {@link #userDataAccessor} to set. If null, {@link #defaultUserDataAccessor} is set. */
	public static void setUserDataAccessor(Function<PositionController, Object> userDataAccessor) {
		PositionController.userDataAccessor = userDataAccessor != null ? userDataAccessor : defaultUserDataAccessor;
	}

	/** the proportional control loop component
	 *  @author dermetfan
	 *  @since 0.11.1 */
	public static class P extends PositionController {

		/** @param gain the gain
		 *  @param pos the current position
		 *  @param dest the setpoint
		 *  @return gain * error */
		public static float calculateForce(float gain, float pos, float dest) {
			return gain * (dest - pos);
		}

		/** @see #calculateForce(float, float, float)
		 *  @return {@link #vec2} */
		public static Vector2 calculateForce(float gainX, float gainY, float x, float y, float destX, float destY) {
			return vec2.set(calculateForce(gainX, x, destX), calculateForce(gainY, y, destY));
		}

		/** @see #calculateForce(float, float, float, float, float, float) */
		public static Vector2 calculateForce(Vector2 gain, Vector2 pos, Vector2 dest) {
			return calculateForce(gain.x, gain.y, pos.x, pos.y, dest.x, dest.y);
		}

		/** the gain */
		private Vector2 gain;

		/** the setpoint */
		private Vector2 destination;

		/** @param gain the gain on both axes
		 *  @param destination the {@link #destination} */
		public P(float gain, Vector2 destination) {
			this(new Vector2(gain, gain), destination);
		}

		/** @param gain the {@link #gain}
		 *  @param destination the {@link #destination} */
		public P(Vector2 gain, Vector2 destination) {
			this.gain = gain;
			this.destination = destination;
		}

		@Override
		public Vector2 calculateForce(Body body, Vector2 point) {
			return calculateForce(gain, point, destination);
		}

		@Override
		public Vector2 calculateForceToCenter(Body body) {
			return calculateForce(gain, body.getWorldCenter(), destination);
		}

		// getters and setters

		/** @return the {@link #gain} */
		public Vector2 getGain() {
			return gain;
		}

		/** @param gain the {@link #gain} to set */
		public void setGain(Vector2 gain) {
			this.gain = gain;
		}

		/** @return the {@link #destination} */
		public Vector2 getDestination() {
			return destination;
		}

		/** @param destination the {@link #destination} to set */
		public void setDestination(Vector2 destination) {
			this.destination = destination;
		}

	}

	/** the derivative control loop component
	 *  @author dermetfan
	 *  @since 0.11.1 */
	public static class D extends PositionController {

		/** @param gain the gain
		 *  @param vel the velocity
		 *  @return {@code gain * -vel} */
		public static float calculateForce(float gain, float vel) {
			return gain * -vel;
		}

		/** @see #calculateForce(float, float)
		 *  @return {@link #vec2} */
		public static Vector2 calculateForce(float gainX, float gainY, float velX, float velY) {
			return vec2.set(calculateForce(gainX, velX), calculateForce(gainY, velY));
		}

		/** @see #calculateForce(float, float, float, float) */
		public static Vector2 calculateForce(Vector2 gain, Vector2 vel) {
			return calculateForce(gain.x, gain.y, vel.x, vel.y);
		}

		/** the gain */
		private Vector2 gain;

		/** @param gain the gain on both axes */
		public D(float gain) {
			this(new Vector2(gain, gain));
		}

		/** @param gain the {@link #gain} */
		public D(Vector2 gain) {
			this.gain = gain;
		}

		@Override
		public Vector2 calculateForce(Body body, Vector2 point) {
			return calculateForce(gain, body.getLinearVelocityFromWorldPoint(point));
		}

		@Override
		public Vector2 calculateForceToCenter(Body body) {
			return calculateForce(gain, body.getLinearVelocity());
		}

		// getters and setters

		/** @return the {@link #gain} */
		public Vector2 getGain() {
			return gain;
		}

		/** @param gain the {@link #gain} to set*/
		public void setGain(Vector2 gain) {
			this.gain = gain;
		}

	}

	/** a proportional-derivative controller
	 *  @author dermetfan
	 *  @since 0.11.1
	 *  @see P
	 *  @see D */
	public static class PD extends PositionController {

		/** @see P#calculateForce(float, float, float)
		 *  @see D#calculateForce(float, float) */
		public static float calculateForce(float gainP, float gainD, float pos, float dest, float vel) {
			return P.calculateForce(gainP, pos, dest) + D.calculateForce(gainD, vel);
		}

		/** @see #calculateForce(float, float, float, float, float) */
		public static Vector2 calculateForce(float gainPX, float gainPY, float gainDX, float gainDY, float posX, float posY, float destX, float destY, float velX, float velY) {
			return vec2.set(calculateForce(gainPX, gainDX, posX, destX, velX), calculateForce(gainPY, gainDY, posY, destY, velY));
		}

		/** @see #calculateForce(float, float, float, float, float, float, float, float, float, float) */
		public static Vector2 calculateForce(Vector2 gainP, Vector2 gainD, Vector2 pos, Vector2 dest, Vector2 vel) {
			return calculateForce(gainP.x, gainP.y, gainD.x, gainD.y, pos.x, pos.y, dest.x, dest.y, vel.x, vel.y);
		}

		/** the gain of the proportional component */
		private Vector2 gainP;

		/** the gain of the derivative component */
		private Vector2 gainD;

		/** the setpoint */
		private Vector2 destination;

		/** @param gainP the proportional gain on both axes
		 *  @param gainD the derivative gain on both axes
		 *  @param destination the {@link #destination} */
		public PD(float gainP, float gainD, Vector2 destination) {
			this(new Vector2(gainP, gainP), new Vector2(gainD, gainD), destination);
		}

		/** @param gainP the {@link #gainP}
		 *  @param gainD the {@link #gainD}
		 *  @param destination the {@link #destination} */
		public PD(Vector2 gainP, Vector2 gainD, Vector2 destination) {
			this.gainP = gainP;
			this.gainD = gainD;
			this.destination = destination;
		}

		@Override
		public Vector2 calculateForce(Body body, Vector2 point) {
			return calculateForce(gainP, gainD, point, destination, body.getLinearVelocityFromWorldPoint(point));
		}

		@Override
		public Vector2 calculateForceToCenter(Body body) {
			return calculateForce(gainP, gainD, body.getWorldCenter(), destination, body.getLinearVelocity());
		}

		// getters and setters

		/** @return the {@link #gainP} */
		public Vector2 getGainP() {
			return gainP;
		}

		/** @param gainP the {@link #gainP} to set */
		public void setGainP(Vector2 gainP) {
			this.gainP = gainP;
		}

		/** @return the {@link #gainD} */
		public Vector2 getGainD() {
			return gainD;
		}

		/** @param gainD the {@link #gainD} to set */
		public void setGainD(Vector2 gainD) {
			this.gainD = gainD;
		}

		/** @return the {@link #destination} */
		public Vector2 getDestination() {
			return destination;
		}

		/** @param destination the {@link #destination} to set */
		public void setDestination(Vector2 destination) {
			this.destination = destination;
		}

	}

}
