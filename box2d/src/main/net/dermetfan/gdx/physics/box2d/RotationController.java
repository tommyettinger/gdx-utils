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

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;
import net.dermetfan.utils.Function;

/** rotates a body to an angle using torque
 *  @author dermetfan
 *  @since 0.11.1 */
public abstract class RotationController {

	/** returns the argument if it is a RotationController */
	public static final Function<RotationController, Object> defaultUserDataAccessor = new Function<RotationController, Object>() {
		@Override
		public RotationController apply(Object arg) {
			return arg instanceof RotationController ? (RotationController) arg : null;
		}
	};

	/** the Function used to extract a RotationController out of a Body's user data */
	private static Function<RotationController, Object> userDataAccessor = defaultUserDataAccessor;

	/** Calls {@link #applyTorque(World, boolean) applyTorque} for every Body with a RotationController in its user data.
	 *  The RotationController is accessed using the {@link #userDataAccessor}.
	 *  @param world the world which Bodies to iterate over */
	public static void applyTorque(World world, boolean wake) {
		@SuppressWarnings("unchecked")
		Array<Body> bodies = Pools.obtain(Array.class);
		world.getBodies(bodies);
		for(Body body : bodies) {
			RotationController controller = userDataAccessor.apply(body.getUserData());
			if(controller != null)
				controller.applyTorque(body, wake);
		}
		bodies.clear();
		Pools.free(bodies);
	}

	/** @return the torque to apply */
	public abstract float calculateTorque(Body body);

	/** applies the necessary torque
	 *  @return the torque applied, calculated by {@link #calculateTorque(Body)} */
	public float applyTorque(Body body, boolean wake) {
		float torque = calculateTorque(body);
		body.applyTorque(torque, wake);
		return torque;
	}

	// getters and setters

	/** @return the {@link #userDataAccessor} */
	public static Function<RotationController, Object> getUserDataAccessor() {
		return userDataAccessor;
	}

	/** @param userDataAccessor The {@link #userDataAccessor} to set. If null, {@link #defaultUserDataAccessor} is set. */
	public static void setUserDataAccessor(Function<RotationController, Object> userDataAccessor) {
		RotationController.userDataAccessor = userDataAccessor != null ? userDataAccessor : defaultUserDataAccessor;
	}

	/** the proportional control loop component
	 *  @author dermetfan
	 *  @since 0.11.1 */
	public static class P extends RotationController {

		/** @param gain the gain
		 *  @param rotation the current rotation
		 *  @param target the setpoint
		 *  @return gain * error normalized between -pi and pi */
		public static float calculateTorque(float gain, float rotation, float target) {
			return gain * net.dermetfan.utils.math.MathUtils.normalize(target - rotation, -MathUtils.PI, MathUtils.PI);
		}

		/** @param body the Body to rotate
		 *  @param target The point to rotate towards. May be null in which case zero is returned.
		 *  @return the angle to rotate towards */
		public static float calculateTargetAngle(Body body, Vector2 target) {
			if(target != null) {
				Vector2 v = body.getPosition().sub(target).scl(-1);
				return MathUtils.atan2(v.y, v.x);
			}
			return 0;
		}

		/** the gain */
		private Number gain;

		/** the setpoint */
		private Number angle;

		/** the point to rotate towards (may be null) */
		private Vector2 target;

		/** @param gain the {@link #gain}
		 *  @param angle the {@link #angle} */
		public P(Number gain, Number angle) {
			this(gain, angle, null);
		}

		/** @param gain the {@link #gain}
		 *  @param target the {@link #target} */
		public P(Number gain, Vector2 target) {
			this(gain, 0, target);
		}

		/** @param gain the {@link #gain}
		 *  @param angle the {@link #angle}
		 *  @param target the {@link #target} */
		public P(Number gain, Number angle, Vector2 target) {
			this.gain = gain;
			this.angle = angle;
			this.target = target;
		}

		@Override
		public float calculateTorque(Body body) {
			return calculateTorque(gain.floatValue(), body.getAngle(), calculateTargetAngle(body, target) + angle.floatValue());
		}

		// getters and setters

		/** @return the {@link #gain} */
		public Number getGain() {
			return gain;
		}

		/** @param gain the {@link #gain} to set */
		public void setGain(Number gain) {
			this.gain = gain;
		}

		/** @return the {@link #angle} */
		public Number getAngle() {
			return angle;
		}

		/** @param angle the {@link #angle} to set */
		public void setAngle(Number angle) {
			this.angle = angle;
		}

		/** @return the {@link #target} */
		public Vector2 getTarget() {
			return target;
		}

		/** @param target the {@link #target} to set */
		public void setTarget(Vector2 target) {
			this.target = target;
		}

	}

	/** the derivative control loop component
	 *  @author dermetfan
	 *  @since 0.11.1 */
	public static class D extends RotationController {

		/** @param gain the gain
		 *  @param vel the angular velocity
		 *  @return {@code gain * -vel} */
		public static float calculateTorque(float gain, float vel) {
			return gain * -vel;
		}

		/** the gain */
		private Number gain;

		/** @param gain the {@link #gain} */
		public D(Number gain) {
			this.gain = gain;
		}

		@Override
		public float calculateTorque(Body body) {
			return calculateTorque(gain.floatValue(), body.getAngularVelocity());
		}

		// getters and setters

		/** @return the {@link #gain} */
		public Number getGain() {
			return gain;
		}

		/** @param gain the {@link #gain} to set */
		public void setGain(Number gain) {
			this.gain = gain;
		}

	}

	/** a proportional-derivative controller
	 *  @author dermetfan
	 *  @since 0.11.1
	 *  @see P
	 *  @see D */
	public static class PD extends RotationController {

		/** @see P#calculateTorque(float, float, float)
		 *  @see D#calculateTorque(float, float) */
		public static float calculateTorque(float gainP, float gainD, float rotation, float target, float vel) {
			return P.calculateTorque(gainP, rotation, target) + D.calculateTorque(gainD, vel);
		}

		/** the gain of the proportional component */
		private Number gainP;

		/** the gain of the derivative component */
		private Number gainD;

		/** the setpoint */
		private Number angle;

		/** the point to rotate towards (may be null) */
		private Vector2 target;

		/** @param gainP the {@link #gainP}
		 *  @param gainD the {@link #gainD}
		 *  @param angle the {@link #angle} */
		public PD(Number gainP, Number gainD, Number angle) {
			this(gainP, gainD, angle, null);
		}

		/** @param gainP the {@link #gainP}
		 *  @param gainD the {@link #gainD}
		 *  @param target the {@link #target} */
		public PD(Number gainP, Number gainD, Vector2 target) {
			this(gainP, gainD, 0, target);
		}

		/** @param gainP the {@link #gainP}
		 *  @param gainD the {@link #gainD}
		 *  @param angle the {@link #angle}
		 *  @param target the {@link #target} */
		public PD(Number gainP, Number gainD, Number angle, Vector2 target) {
			this.gainP = gainP;
			this.gainD = gainD;
			this.angle = angle;
			this.target = target;
		}

		@Override
		public float calculateTorque(Body body) {
			return calculateTorque(gainP.floatValue(), gainD.floatValue(), body.getAngle(), P.calculateTargetAngle(body, target) + angle.floatValue(), body.getAngularVelocity());
		}

		// getters and setters

		/** @return the {@link #gainP} */
		public Number getGainP() {
			return gainP;
		}

		/** @param gainP the {@link #gainP} to set */
		public void setGainP(Number gainP) {
			this.gainP = gainP;
		}

		/** @return the {@link #gainD} */
		public Number getGainD() {
			return gainD;
		}

		/** @param gainD the {@link #gainD} to set */
		public void setGainD(Number gainD) {
			this.gainD = gainD;
		}

		/** @return the {@link #target} */
		public Vector2 getTarget() {
			return target;
		}

		/** @param target the {@link #target} to set  */
		public void setTarget(Vector2 target) {
			this.target = target;
		}

	}

}
