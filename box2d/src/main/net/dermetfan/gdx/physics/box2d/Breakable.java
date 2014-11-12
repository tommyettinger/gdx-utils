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

import net.dermetfan.utils.Function;
import net.dermetfan.utils.math.MathUtils;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;

/** Breaks bodies or fixtures if they get hit too hard. Put in fixture's or body's user data and set {@link Manager} as {@link ContactListener}.<br>
 *  Don't forget to call {@link Manager#destroy()} after every world time step.<br>
 *  You can manually destroy fixtures or bodies using the {@link Manager#destroy(Fixture)} and {@link Manager#destroy(Body)} methods.
 *  @author dermetfan */
public class Breakable {

	/** Manages the {@link Breakable Breakables} of the Contacts it receives. <strong>Do not forget to set as ContactListener and to call {@link #destroy()} after every world step.</strong>
	 *  @author dermetfan */
	public static class Manager implements ContactListener {

		/** the fixtures that broke in {@link #strain(Contact, ContactImpulse)} */
		public final Array<Fixture> brokenFixtures = new Array<>(1);

		/** the bodies that broke in {@link #strain(Contact, ContactImpulse)} */
		public final Array<Body> brokenBodies = new Array<>(1);

		/** the joints that broke in {@link #strain(Joint, float)} */
		public final Array<Joint> brokenJoints = new Array<>(1);

		/** the {@link #userDataAccessor} used by default */
		public static final Function<Breakable, Object> defaultUserDataAccessor = new Function<Breakable, Object>() {

			@Override
			public Breakable apply(Object userData) {
				return userData instanceof Breakable ? (Breakable) userData : null;
			}

		};

		/** the {@link net.dermetfan.utils.Function} used to access a Breakable in user data ({@link net.dermetfan.utils.Function#apply(Object) access} must return a Breakable) */
		private Function<Breakable, Object> userDataAccessor = defaultUserDataAccessor;

		/** used for {@link World#getJoints(Array)} in {@link #strain(World, float)} */
		private final Array<Joint> tmpJoints = new Array<>(0);

		/** instantiates a new {@link Manager} */
		public Manager() {}

		/** instantiates a new {@link Manager} with the given {@link #userDataAccessor} */
		public Manager(Function<Breakable, Object> userDataAccessor) {
			setUserDataAccessor(userDataAccessor);
		}

		/** actually destroys all bodies in {@link #brokenBodies} and fixtures in {@link #brokenFixtures} */
		public void destroy() {
			for(Fixture fixture : brokenFixtures) {
				brokenFixtures.removeValue(fixture, true);
				fixture.getBody().destroyFixture(fixture);
			}
			for(Body body : brokenBodies) {
				brokenBodies.removeValue(body, true);
				body.getWorld().destroyBody(body);
			}
			for(Joint joint : brokenJoints) {
				brokenJoints.removeValue(joint, true);
				joint.getBodyA().getWorld().destroyJoint(joint);
			}
		}

		/** {@link #destroy(Body) destroys}/{@link #destroy(Fixture) destroys} all fixtures/bodies involved in the given Contact if they could not bear the given impulse */
		public void strain(Contact contact, ContactImpulse impulse) {
			float normalImpulse = MathUtils.sum(impulse.getNormalImpulses()), tangentImpulse = Math.abs(MathUtils.sum(impulse.getTangentImpulses()));

			Fixture fixtureA = contact.getFixtureA(), fixtureB = contact.getFixtureB();
			Breakable breakable = userDataAccessor.apply(fixtureA.getUserData());
			if(shouldBreak(breakable, normalImpulse, tangentImpulse, contact, impulse, fixtureA))
				destroy(fixtureA);

			breakable = userDataAccessor.apply(fixtureB.getUserData());
			if(shouldBreak(breakable, normalImpulse, tangentImpulse, contact, impulse, fixtureB))
				destroy(fixtureB);

			Body bodyA = fixtureA.getBody(), bodyB = fixtureB.getBody();
			breakable = userDataAccessor.apply(bodyA.getUserData());
			if(shouldBreak(breakable, normalImpulse, tangentImpulse, contact, impulse, fixtureA))
				destroy(bodyA);

			breakable = userDataAccessor.apply(bodyB.getUserData());
			if(shouldBreak(breakable, normalImpulse, tangentImpulse, contact, impulse, fixtureB))
				destroy(bodyB);
		}

		/** {@link #strain(Joint, float) strains} all joints in the given world */
		public void strain(World world, float delta) {
			world.getJoints(tmpJoints);
			for(Joint joint : tmpJoints)
				strain(joint, delta);
		}

		/** {@link #destroy(Joint) destroy} */
		public void strain(Joint joint, float delta) {
			Breakable breakable = userDataAccessor.apply(joint.getUserData());
			if(breakable == null)
				return;
			Vector2 reactionForce = joint.getReactionForce(1 / delta);
			float reactionTorque = Math.abs(joint.getReactionTorque(1 / delta));
			if(shouldBreak(breakable, reactionForce, reactionTorque, joint))
				destroy(joint);
		}

		/** @param contact for {@link Callback#strained(Fixture, Breakable, Contact, ContactImpulse, float, float)}
		 *  @param impulse for {@link Callback#strained(Fixture, Breakable, Contact, ContactImpulse, float, float)}
		 *  @param fixture for {@link Callback#strained(Fixture, Breakable, Contact, ContactImpulse, float, float)}
		 *  @return if the fixtures and bodies involved in the given contact should break under the given circumstances */
		public static boolean shouldBreak(Breakable breakable, float normalImpulse, float tangentImpulse, Contact contact, ContactImpulse impulse, Fixture fixture) {
			return breakable != null && (normalImpulse > breakable.normalResistance || tangentImpulse > breakable.tangentResistance) && (breakable.callback == null || !breakable.callback.strained(fixture, breakable, contact, impulse, normalImpulse, tangentImpulse));
		}

		/** @param reactionForce the {@link Joint#getReactionForce(float) reaction force}
		 *  @param reactionTorque the {@link Joint#getReactionTorque(float) reaction torque}
		 *  @param joint which circumstances to test
		 *  @return if the joint strained with the given values should break */
		public static boolean shouldBreak(Breakable breakable, Vector2 reactionForce, float reactionTorque, Joint joint) {
			return breakable != null && (Math.abs(reactionForce.x) > breakable.reactionForceResistance.x || Math.abs(reactionForce.y) > breakable.reactionForceResistance.y || reactionForce.len2() > breakable.reactionForceLength2Resistance || reactionTorque > breakable.reactionTorqueResistance) && (breakable.callback == null || !breakable.callback.strained(joint, breakable, reactionForce, reactionTorque));
		}

		/** destroys the given fixture (and its body depending on {@link #breakBodyWithoutFixtures} and {@link #breakBody})
		 *  @param fixture the {@link Fixture} to destroy */
		public void destroy(Fixture fixture) {
			if(brokenFixtures.contains(fixture, true))
				return;

			Breakable breakable = userDataAccessor.apply(fixture.getUserData());
			if(breakable == null || (breakable.callback == null || !breakable.callback.destroyed(fixture, breakable)))
				brokenFixtures.add(fixture);

			if(breakable != null) {
				Body body = fixture.getBody();
				if(breakable.breakBody) {
					destroy(body);
					return;
				}
				if(breakable.breakBodyWithoutFixtures) {
					for(Fixture bodyFixture : body.getFixtureList())
						if(!brokenFixtures.contains(bodyFixture, true))
							return;
					destroy(body);
				}
			}
		}

		/** @param body the {@link Body} to destroy */
		public void destroy(Body body) {
			if(brokenBodies.contains(body, true))
				return;

			Breakable breakable = userDataAccessor.apply(body.getUserData());
			if(breakable == null || (breakable.callback == null || !breakable.callback.destroyed(body, breakable)))
				brokenBodies.add(body);
		}

		/** @param joint the {@link Joint} to destroy */
		public void destroy(Joint joint) {
			if(brokenJoints.contains(joint, true))
				return;

			Breakable breakable = userDataAccessor.apply(joint.getUserData());
			if(breakable == null || (breakable.callback == null || !breakable.callback.destroyed(joint, breakable)))
				brokenJoints.add(joint);

			if(breakable != null && breakable.breakBody) {
				destroy(joint.getBodyA());
				destroy(joint.getBodyB());
			}
		}

		/** does nothing */
		@Override
		public void beginContact(Contact contact) {
		}

		/** does nothing */
		@Override
		public void preSolve(Contact contact, Manifold oldManifold) {
		}

		/** calls {@link #strain(Contact, ContactImpulse)} */
		@Override
		public void postSolve(Contact contact, ContactImpulse impulse) {
			strain(contact, impulse);
		}

		/** does nothing */
		@Override
		public void endContact(Contact contact) {
		}

		/** @return the {@link #brokenFixtures} */
		public Array<Fixture> getBrokenFixtures() {
			return brokenFixtures;
		}

		/** @return the {@link #brokenBodies} */
		public Array<Body> getBrokenBodies() {
			return brokenBodies;
		}

		/** @return the {@link #userDataAccessor} */
		public Function<Breakable, Object> getUserDataAccessor() {
			return userDataAccessor;
		}

		/** @param userDataAccessor the {@link #userDataAccessor} to set */
		public void setUserDataAccessor(Function<Breakable, Object> userDataAccessor) {
			this.userDataAccessor = userDataAccessor != null ? userDataAccessor : defaultUserDataAccessor;
		}

	}

	/** a callback for a {@link Breakable} if its container (body or fixture) was destroyed (for example to play a sound)
	 *  @author dermetfan */
	public interface Callback {

		/** called by {@link Manager#strain(Contact, ContactImpulse)}
		 *  @param fixture the strained fixture
		 *  @param breakable the Breakable instance causing this callback to be called
		 *  @param contact the straining contact
		 *  @param impulse the straining ContactImpulse
		 *  @param normalImpulse the sum of the normal impulses of impulse
		 *  @param tangentImpulse the sum of the tangent impulses of impulse
		 *  @return true to cancel the destruction if one was going to occur */
		public boolean strained(Fixture fixture, Breakable breakable, Contact contact, ContactImpulse impulse, float normalImpulse, float tangentImpulse);

		/** called by {@link Manager#strain(Joint, float)}
		 *  @param joint the strained {@link Joint}
		 *  @param breakable the {@link Breakable} instance causing this callback to be called
		 *  @param reactionForce the {@link Joint#getReactionForce(float) reaction force}
		 *  @return true to cancel the destruction if one was going to occur */
		public boolean strained(Joint joint, Breakable breakable, Vector2 reactionForce, float reactionTorque);

		/** called by {@link Manager#destroy(Body)}
		 *  @return true to cancel the destruction */
		public boolean destroyed(Body body, Breakable breakable);

		/** called by {@link Manager#destroy(Fixture)}
		 *  @return true to cancel the destruction */
		public boolean destroyed(Fixture fixture, Breakable breakable);

		/** called by {@link Manager#destroy(Joint)}
		 *  @return true to cancel the destruction */
		public boolean destroyed(Joint joint, Breakable breakable);

		/** Returns false in all methods implemented from {@link Callback}. Instantiate this if you want to only use a subset of the methods of {@link Callback}.
		 *  @author dermetfan */
		public static class Adapter implements Callback {

			@Override
			public boolean strained(Fixture fixture, Breakable breakable, Contact contact, ContactImpulse impulse, float normalImpulse, float tangentImpulse) {
				return false;
			}

			@Override
			public boolean strained(Joint joint, Breakable breakable, Vector2 reactionForce, float reactionTorque) {
				return false;
			}

			@Override
			public boolean destroyed(Body body, Breakable breakable) {
				return false;
			}

			@Override
			public boolean destroyed(Fixture fixture, Breakable breakable) {
				return false;
			}

			@Override
			public boolean destroyed(Joint joint, Breakable breakable) {
				return false;
			}

		}

	}

	/** how much force the Breakable can bear */
	private float normalResistance;

	/** how much friction the Breakable can bear */
	private float tangentResistance;

	/** how much {@link Joint#getReactionForce(float) reaction force} the Breakable can bear */
	private final Vector2 reactionForceResistance = new Vector2();

	/** the max {@link Vector2#len2() squared length} of the {@link Joint#getReactionForce(float) reaction force} */
	private float reactionForceLength2Resistance;

	/** how much {@link Joint#getReactionTorque(float) reaction torque} the Breakable can bear */
	private float reactionTorqueResistance;

	/** if the fixture's body (in case the Breakable is used for a fixture) should be destroyed if the fixture is destroyed (false by default) */
	private boolean breakBody;

	/** if the fixture's body (in case the Breakable is used for a fixture) should be destroyed if the fixture is destroyed and it was the body's last one (true by default) */
	private boolean breakBodyWithoutFixtures = true;

	/** the {@link Callback} called when the {@link Breakable}'s container is destroyed */
	private Callback callback;

	/** @see #Breakable(float, float, boolean) */
	public Breakable(float normalResistance, float tangentResistance) {
		this(normalResistance, tangentResistance, false);
	}

	/** @see #Breakable(float, float, boolean, boolean) */
	public Breakable(float normalResistance, float tangentResistance, boolean breakBody) {
		this(normalResistance, tangentResistance, breakBody, true);
	}

	/** @see #Breakable(float, float, Vector2, float, float, boolean, boolean, Callback) */
	public Breakable(float normalResistance, float tangentResistance, boolean breakBody, boolean breakBodyWithoutFixtures) {
		this(normalResistance, tangentResistance, Vector2.Zero, 0, 0, breakBody, breakBodyWithoutFixtures, null);
	}

	/** @see #Breakable(float, float, boolean, Callback) */
	public Breakable(float normalResistance, float tangentResistance, Callback callback) {
		this(normalResistance, tangentResistance, false, callback);
	}

	/** @see #Breakable(float, float, Vector2, float, float, boolean, boolean, Callback) */
	public Breakable(float normalResistance, float tangentResistance, boolean breakBody, Callback callback) {
		this(normalResistance, tangentResistance, Vector2.Zero, 0, 0, breakBody, true, callback);
	}

	/** @see #Breakable(Vector2, float, float, boolean) */
	public Breakable(Vector2 reactionForceResistance, float reactionForceLength2Resistance, float reactionTorqueResistance) {
		this(reactionForceResistance, reactionForceLength2Resistance, reactionTorqueResistance, false);
	}

	/** @see #Breakable(Vector2, float, float, boolean, boolean) */
	public Breakable(Vector2 reactionForceResistance, float reactionForceLength2Resistance, float reactionTorqueResistance, boolean breakBody) {
		this(reactionForceResistance, reactionForceLength2Resistance, reactionTorqueResistance, breakBody, true);
	}

	/** @see #Breakable(float, float, Vector2, float, float, boolean, boolean, Callback) */
	public Breakable(Vector2 reactionForceResistance, float reactionForceLength2Resistance, float reactionTorqueResistance, boolean breakBody, boolean breakBodyWithoutFixtures) {
		this(0, 0, reactionForceResistance, reactionForceLength2Resistance, reactionTorqueResistance, breakBody, breakBodyWithoutFixtures, null);
	}

	/** @see #Breakable(Vector2, float, float, boolean, Callback) */
	public Breakable(Vector2 reactionForceResistance, float reactionForceLength2Resistance, float reactionTorqueResistance, Callback callback) {
		this(reactionForceResistance, reactionForceLength2Resistance, reactionTorqueResistance, false, callback);
	}

	/** @see #Breakable(float, float, Vector2, float, float, boolean, boolean, Callback) */
	public Breakable(Vector2 reactionForceResistance, float reactionForceLength2Resistance, float reactionTorqueResistance, boolean breakBody, Callback callback) {
		this(0, 0, reactionForceResistance, reactionForceLength2Resistance, reactionTorqueResistance, breakBody, true, callback);
	}

	/** @param normalResistance the {@link #normalResistance}
	 * 	@param tangentResistance the {@link #tangentResistance}
	 *  @param reactionForceResistance the {@link #reactionForceResistance}
	 *  @param reactionForceLength2Resistance the {@link #reactionForceLength2Resistance}
	 *  @param reactionTorqueResistance the {@link #reactionTorqueResistance}
	 *  @param breakBody the {@link #breakBody}
	 *  @param breakBodyWithoutFixtures the {@link #breakBodyWithoutFixtures}
	 *  @param callback the {@link #callback} */
	public Breakable(float normalResistance, float tangentResistance, Vector2 reactionForceResistance, float reactionForceLength2Resistance, float reactionTorqueResistance, boolean breakBody, boolean breakBodyWithoutFixtures, Callback callback) {
		this.normalResistance = normalResistance;
		this.tangentResistance = tangentResistance;
		this.reactionForceResistance.set(reactionForceResistance);
		this.reactionForceLength2Resistance = reactionForceLength2Resistance;
		this.reactionTorqueResistance = reactionTorqueResistance;
		this.breakBody = breakBody;
		this.breakBodyWithoutFixtures = breakBodyWithoutFixtures;
		this.callback = callback;
	}

	/** constructs a new Breakable exactly like the given other one */
	public Breakable(Breakable other) {
		this(other.normalResistance, other.tangentResistance, other.reactionForceResistance, other.reactionForceLength2Resistance, other.reactionTorqueResistance, other.breakBody, other.breakBodyWithoutFixtures, other.callback);
	}

	/** @return the {@link #normalResistance} */
	public float getNormalResistance() {
		return normalResistance;
	}

	/** @param normalResistance the {@link #normalResistance} to set */
	public void setNormalResistance(float normalResistance) {
		this.normalResistance = normalResistance;
	}

	/** @return the {@link #tangentResistance} */
	public float getTangentResistance() {
		return tangentResistance;
	}

	/** @param tangentResistance the {@link #tangentResistance} to set */
	public void setTangentResistance(float tangentResistance) {
		this.tangentResistance = tangentResistance;
	}

	/** @return the {@link #reactionForceResistance} */
	public Vector2 getReactionForceResistance() {
		return reactionForceResistance;
	}

	/** @param reactionForceResistance the {@link #reactionForceResistance} to set */
	public void setReactionForceResistance(Vector2 reactionForceResistance) {
		this.reactionForceResistance.set(reactionForceResistance);
	}

	/** @return the {@link #reactionForceLength2Resistance} */
	public float getReactionForceLength2Resistance() {
		return reactionForceLength2Resistance;
	}

	/** @param reactionForceLength2Resistance the {@link #reactionForceLength2Resistance} to set */
	public void setReactionForceLength2Resistance(float reactionForceLength2Resistance) {
		this.reactionForceLength2Resistance = reactionForceLength2Resistance;
	}

	/** @return the {@link #reactionTorqueResistance} */
	public float getReactionTorqueResistance() {
		return reactionTorqueResistance;
	}

	/** @param reactionTorqueResistance the {@link #reactionTorqueResistance} to set */
	public void setReactionTorqueResistance(float reactionTorqueResistance) {
		this.reactionTorqueResistance = reactionTorqueResistance;
	}

	/** @return the {@link #breakBody} */
	public boolean isBreakBody() {
		return breakBody;
	}

	/** @param breakBody the {@link #breakBody} to set */
	public void setBreakBody(boolean breakBody) {
		this.breakBody = breakBody;
	}

	/** @return the {@link #breakBodyWithoutFixtures} */
	public boolean isBreakBodyWithoutFixtures() {
		return breakBodyWithoutFixtures;
	}

	/** @param breakBodyWithoutFixtures the {@link #breakBodyWithoutFixtures} to set */
	public void setBreakBodyWithoutFixtures(boolean breakBodyWithoutFixtures) {
		this.breakBodyWithoutFixtures = breakBodyWithoutFixtures;
	}

	/** @return the {@link #callback} */
	public Callback getCallback() {
		return callback;
	}

	/** @param callback the {@link #callback} to set */
	public void setCallback(Callback callback) {
		this.callback = callback;
	}

}
