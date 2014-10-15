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

import java.util.Objects;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.JointDef.JointType;
import com.badlogic.gdx.physics.box2d.MassData;
import com.badlogic.gdx.physics.box2d.Transform;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.DistanceJoint;
import com.badlogic.gdx.physics.box2d.joints.FrictionJoint;
import com.badlogic.gdx.physics.box2d.joints.GearJoint;
import com.badlogic.gdx.physics.box2d.joints.MotorJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.PrismaticJoint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;
import com.badlogic.gdx.physics.box2d.joints.RopeJoint;
import com.badlogic.gdx.physics.box2d.joints.WeldJoint;
import com.badlogic.gdx.physics.box2d.joints.WheelJoint;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.IntMap.Entry;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.badlogic.gdx.utils.Pools;

/** notifies a {@link Listener} of changes in the world
 *  @since 0.6.0
 *  @author dermetfan */
public class WorldObserver {

	/** The Listener to notify. May be null. */
	private Listener listener;

	/** the WorldChange used to track the World */
	private final WorldChange worldChange = new WorldChange();

	/** the BodyChanges used to track Bodies, keys are hashes computed by {@link com.badlogic.gdx.physics.box2d.Box2DUtils#hashCode(Body) Box2DUtils#hashCode(Body)} because a World pools its Bodies */
	private final IntMap<BodyChange> bodyChanges = new IntMap<>();

	/** the FixtureChanges used to track Fixtures, keys are hashes computed by {@link com.badlogic.gdx.physics.box2d.Box2DUtils#hashCode(Fixture) Box2DUtils#hashCode(Fixture)} because a world pools its Fixtures */
	private final IntMap<FixtureChange> fixtureChanges = new IntMap<>();

	/** the JointChanges used to track Joints */
	private final ObjectMap<Joint, JointChange> jointChanges = new ObjectMap<>();

	/** temporary array used internally */
	private final Array<Body> tmpBodies = new Array<>();

	/** the Bodies by {@link com.badlogic.gdx.physics.box2d.Box2DUtils#hashCode(Body) hash} since this/the last time {@link #update(World, float)} was called */
	private final IntMap<Body> currentBodies = new IntMap<>(), previousBodies = new IntMap<>();

	/** the Fixtures by {@link com.badlogic.gdx.physics.box2d.Box2DUtils#hashCode(Fixture) hash} since this/the last time {@link #update(World, float)} was called */
	private final IntMap<Fixture> currentFixtures = new IntMap<>(), previousFixtures = new IntMap<>();

	/** the Joints since this/the last time {@link #update(World, float)} was called  */
	private final Array<Joint> currentJoints = new Array<>(), previousJoints = new Array<>();

	/** creates a new WorldObserver with no {@link #listener} */
	public WorldObserver() {}

	/** @param listener the {@link #listener} */
	public WorldObserver(Listener listener) {
		setListener(listener);
	}

	/** @param world Ideally always the same World because its identity is not checked. Passing in another world instance will cause all differences between the two worlds to be processed.
	 *  @param step the time the world was last {@link World#step(float, int, int) stepped} with */
	public void update(World world, float step) {
		if(listener != null)
			listener.preUpdate(world, step);

		if(worldChange.update(world) && listener != null)
			listener.changed(world, worldChange);

		// destructions
		world.getBodies(tmpBodies);
		currentBodies.clear();
		currentFixtures.clear();
		for(Body body : tmpBodies) {
			currentBodies.put(com.badlogic.gdx.physics.box2d.Box2DUtils.hashCode(body), body);
			for(Fixture fixture : body.getFixtureList())
				currentFixtures.put(com.badlogic.gdx.physics.box2d.Box2DUtils.hashCode(fixture), fixture);
		}
		for(Entry<Body> entry : previousBodies.entries()) {
			if(!currentBodies.containsKey(entry.key)) {
				Pools.free(bodyChanges.remove(entry.key));
				if(listener != null)
					listener.destroyed(entry.value);
			}
		}
		previousBodies.clear();
		previousBodies.putAll(currentBodies);

		for(Entry<Fixture> entry : previousFixtures.entries()) {
			if(!currentFixtures.containsKey(entry.key)) {
				Pools.free(fixtureChanges.get(entry.key));
				if(listener != null)
					listener.destroyed(entry.value);
			}
		}
		previousFixtures.clear();
		previousFixtures.putAll(currentFixtures);

		// changes and creations
		for(Entry<Body> entry : currentBodies.entries()) {
			BodyChange bodyChange = bodyChanges.get(entry.key);
			if(bodyChange != null) {
				if(bodyChange.update(entry.value) && listener != null)
					listener.changed(entry.value, bodyChange);
			} else {
				bodyChange = Pools.obtain(BodyChange.class);
				bodyChange.update(entry.value);
				bodyChanges.put(entry.key, bodyChange);
				if(listener != null)
					listener.created(entry.value);
			}
		}
		for(Entry<Fixture> entry : currentFixtures.entries()) {
			FixtureChange fixtureChange = fixtureChanges.get(entry.key);
			if(fixtureChange != null) {
				if(fixtureChange.update(entry.value) && listener != null)
					listener.changed(entry.value, fixtureChange);
			} else {
				fixtureChange = Pools.obtain(FixtureChange.class);
				fixtureChange.created(entry.value.getBody());
				fixtureChange.update(entry.value);
				fixtureChanges.put(entry.key, fixtureChange);
				if(listener != null)
					listener.created(entry.value);
			}
		}

		// check for new or updated joints
		world.getJoints(currentJoints);
		for(Joint joint : currentJoints) {
			JointChange jointChange = jointChanges.get(joint);
			if(jointChange != null) { // updated
				if(jointChange.update(joint) && listener != null)
					listener.changed(joint, jointChange);
			} else { // new
				jointChange = JointChange.obtainFor(joint.getType());
				jointChange.update(joint);
				jointChanges.put(joint, jointChange);
				if(listener != null)
					listener.created(joint);
			}
		}
		// check for destroyed joints
		previousJoints.removeAll(currentJoints, true);
		for(Joint joint : previousJoints) {
			JointChange change = jointChanges.remove(joint);
			assert change != null;
			Pools.free(change);
			if(listener != null)
				listener.destroyed(joint);
		}
		previousJoints.clear();
		previousJoints.addAll(currentJoints);

		if(listener != null)
			listener.postUpdate(world, step);
	}

	/** @param hash the hash of the Body (computed via {@link com.badlogic.gdx.physics.box2d.Box2DUtils#hashCode(Body) Box2DUtils#hashCode(Body)}) which associated BodyChange to return
	 *  @return the BodyChange from {@link #bodyChanges} currently used for the Body with the given hash, or null if not found */
	public BodyChange getBodyChange(int hash) {
		return bodyChanges.get(hash);
	}

	/** @param hash the hash of the Fixture (computed via {@link com.badlogic.gdx.physics.box2d.Box2DUtils#hashCode(Fixture) Box2DUtils#hashCode(Fixture)}) which associated FixtureChange to return
	 *  @return the FixtureChange from {@link #fixtureChanges} currently used for the Fixture with the given hash, or null if not found */
	public FixtureChange getFixtureChange(int hash) {
		return fixtureChanges.get(hash);
	}

	/** @param joint the joint which associated JointChange to return
	 *  @return the JointChange from {@link #jointChanges} currently used for the given Joint */
	public JointChange getJointChange(Joint joint) {
		return jointChanges.get(joint);
	}

	// getters and setters

	/** @return the {@link #worldChange} */
	public WorldChange getWorldChange() {
		return worldChange;
	}

	/** @return the {@link #listener} */
	public Listener getListener() {
		return listener;
	}

	/** @param listener the {@link #listener} to set */
	public void setListener(Listener listener) {
		if(this.listener != null)
			this.listener.removedFrom(this);
		this.listener = listener;
		if(listener != null)
			listener.setOn(this);
	}

	/** the listener notified by a {@link WorldObserver}
	 *  @since 0.6.0
	 *  @author dermetfan */
	public static interface Listener {

		/** @param observer the WorldObserver this Listener has just been {@link WorldObserver#setListener(Listener) set} on */
		void setOn(WorldObserver observer);

		/** @param observer the WorldObserver this Listener has just been {@link WorldObserver#setListener(Listener) removed} from */
		void removedFrom(WorldObserver observer);

		/** called at the very beginning of {@link WorldObserver#update(World, float)} */
		void preUpdate(World world, float step);

		/** called at the very end of {@link WorldObserver#update(World, float)} */
		void postUpdate(World world, float step);

		/** @param world the World that changed
		 *  @param change the change */
		void changed(World world, WorldChange change);

		/** @param body the Body that changed
		 *  @param change the change */
		void changed(Body body, BodyChange change);

		/** @param body the created Body */
		void created(Body body);

		/** @param body the destroyed Body */
		void destroyed(Body body);

		/** @param fixture the Fixture that changed
		 *  @param change the change */
		void changed(Fixture fixture, FixtureChange change);

		/** @param fixture the created Fixture */
		void created(Fixture fixture);

		/** @param fixture the destroyed Fixture */
		void destroyed(Fixture fixture);

		/** @param joint the Joint that changed
		 *  @param change the change */
		void changed(Joint joint, JointChange change);

		/** @param joint the created Joint */
		void created(Joint joint);

		/** @param joint the destroyed Joint */
		void destroyed(Joint joint);

		/** A class that implements Listener. Does nothing. Subclass this if you only want to override some methods.
		 *  @since 0.7.0
		 *  @author dermetfan */
		public static class Adapter implements Listener {

			@Override
			public void setOn(WorldObserver observer) {}

			@Override
			public void removedFrom(WorldObserver observer) {}

			@Override
			public void preUpdate(World world, float step) {}

			@Override
			public void postUpdate(World world, float step) {}

			@Override
			public void changed(World world, WorldChange change) {}

			@Override
			public void changed(Body body, BodyChange change) {}

			@Override
			public void created(Body body) {}

			@Override
			public void destroyed(Body body) {}

			@Override
			public void changed(Fixture fixture, FixtureChange change) {}

			@Override
			public void created(Fixture fixture) {}

			@Override
			public void destroyed(Fixture fixture) {}

			@Override
			public void changed(Joint joint, JointChange change) {}

			@Override
			public void created(Joint joint) {}

			@Override
			public void destroyed(Joint joint) {}

		}

	}

	/** A Listener that calls another Listener on unpredictable/unexpected events.
	 *  In practice only {@link #changed(Body, BodyChange)} can be predicted and therefore the other methods will be called normally.
	 *  @since 0.7.0
	 *  @author dermetfan */
	public static class UnexpectedListener implements Listener {

		/** the Listener to notify */
		private Listener listener;

		/** the ExpectationBases mapped to their Bodies */
		private final ObjectMap<Body, ExpectationBase> bases = new ObjectMap<>();

		/** the Pool used by this UnexpectedListener */
		private final ExpectationBase.Pool pool = new ExpectationBase.Pool(5, 25);

		/** the last time step */
		private float step;

		/** @param listener the {@link #listener} to set */
		public UnexpectedListener(Listener listener) {
			this.listener = listener;
		}

		@Override
		public void changed(Body body, BodyChange change) {
			boolean unexpected = change.type != null || change.angularDamping != null || change.gravityScale != null || change.massData != null || change.userDataChanged;
			ExpectationBase base = bases.get(body);
			if(!unexpected && change.linearVelocity != null && !change.linearVelocity.equals(base.linearVelocity.mulAdd(body.getWorld().getGravity(), step).scl(1 / (1 + step * body.getLinearDamping()))))
				unexpected = true;
			else if(change.transform != null && // the linear damping of the body must be applied to the linear velocity of the base already
					change.transform.vals[Transform.POS_X] != base.transform.vals[Transform.POS_X] + base.linearVelocity.x * step &&
					change.transform.vals[Transform.POS_Y] != base.transform.vals[Transform.POS_Y] + base.linearVelocity.y * step)
				unexpected = true;
			else if(change.angularVelocity != null && change.angularVelocity != base.angularVelocity * (1 / (1 + step * body.getAngularDamping())))
				unexpected = true;
			base.set(body);
			if(unexpected)
				listener.changed(body, change);
		}

		// always unexpected

		@Override
		public void setOn(WorldObserver observer) {
			listener.setOn(observer);
		}

		@Override
		public void removedFrom(WorldObserver observer) {
			listener.removedFrom(observer);
		}

		@Override
		public void preUpdate(World world, float step) {
			listener.preUpdate(world, this.step = step);
		}

		@Override
		public void postUpdate(World world, float step) {
			listener.postUpdate(world, this.step = step);
		}

		@Override
		public void changed(World world, WorldChange change) {
			listener.changed(world, change);
		}

		@Override
		public void created(Body body) {
			bases.put(body, pool.obtain().set(body));
			listener.created(body);
		}

		@Override
		public void destroyed(Body body) {
			pool.free(bases.remove(body));
			listener.destroyed(body);
		}

		@Override
		public void changed(Fixture fixture, FixtureChange change) {
			listener.changed(fixture, change);
		}

		@Override
		public void created(Fixture fixture) {
			listener.created(fixture);
		}

		@Override
		public void destroyed(Fixture fixture) {
			listener.destroyed(fixture);
		}

		@Override
		public void changed(Joint joint, JointChange change) {
			listener.changed(joint, change);
		}

		@Override
		public void created(Joint joint) {
			listener.created(joint);
		}

		@Override
		public void destroyed(Joint joint) {
			listener.destroyed(joint);
		}

		// getters and setters

		/** @return the {@link #listener} */
		public Listener getListener() {
			return listener;
		}

		/** @param listener the {@link #listener} to set */
		public void setListener(Listener listener) {
			this.listener = listener;
		}

		/** Only for internal use. Stores the last change of predictable data.
		 *  @since 0.7.0
		 *  @author dermetfan */
		private static class ExpectationBase implements Poolable {

			final Transform transform = new Transform();
			final Vector2 linearVelocity = new Vector2();
			float angularVelocity;

			public ExpectationBase set(Body body) {
				Transform bodyTransform = body.getTransform();
				transform.vals[Transform.POS_X] = bodyTransform.vals[Transform.POS_X];
				transform.vals[Transform.POS_Y] = bodyTransform.vals[Transform.POS_Y];
				transform.vals[Transform.COS] = bodyTransform.vals[Transform.COS];
				transform.vals[Transform.SIN] = bodyTransform.vals[Transform.SIN];
				linearVelocity.set(body.getLinearVelocity());
				angularVelocity = body.getAngularVelocity();
				return this;
			}

			@Override
			public void reset() {
				transform.vals[Transform.POS_X] = transform.vals[Transform.POS_Y] = transform.vals[Transform.COS] = transform.vals[Transform.SIN] = 0;
				angularVelocity = 0;
			}

			/** a Pool for ExpectationBases
			 *  @since 0.7.0
			 *  @author dermetfan */
			private static class Pool extends com.badlogic.gdx.utils.Pool<ExpectationBase> {

				public Pool() {}

				public Pool(int initialCapacity) {
					super(initialCapacity);
				}

				public Pool(int initialCapacity, int max) {
					super(initialCapacity, max);
				}

				@Override
				protected ExpectationBase newObject() {
					return new ExpectationBase();
				}

			}

		}

	}

	/** the changes of an object in a world since the last time {@link #update(Object)} was called
	 *  @since 0.6.0
	 *  @author dermetfan */
	public static interface Change<T> extends Poolable {

		/** @param obj the object to check for changes since the last time this method was called
		 *  @return if anything changed */
		boolean update(T obj);

		/** @param obj the object to apply the changes since {@link #update(Object)} to */
		void apply(T obj);

		/** if the values applied in {@link #apply(Object)} equal */
		<C extends Change<T>> boolean newValuesEqual(C other);

	}

	/** the changes of a {@link World}
	 *  @since 0.6.0
	 *  @author dermetfan */
	public static class WorldChange implements Change<World> {

		private transient Boolean oldAutoClearForces;
		private transient final Vector2 oldGravity = new Vector2();

		Boolean autoClearForces;
		Vector2 gravity;

		@Override
		public boolean update(World world) {
			Boolean newAutoClearForces = world.getAutoClearForces();
			Vector2 newGravity = world.getGravity();

			boolean changed = false;

			if(!newAutoClearForces.equals(oldAutoClearForces)) {
				oldAutoClearForces = autoClearForces = newAutoClearForces;
				changed = true;
			} else
				autoClearForces = null;
			if(!newGravity.equals(oldGravity)) {
				oldGravity.set(gravity = newGravity);
				changed = true;
			} else
				autoClearForces = null;

			return changed;
		}

		@Override
		public void apply(World world) {
			if(autoClearForces != null)
				world.setAutoClearForces(autoClearForces);
			if(gravity != null)
				world.setGravity(gravity);
		}

		@Override
		public <C extends Change<World>> boolean newValuesEqual(C other) {
			if(!(other instanceof WorldChange))
				return false;
			WorldChange o = (WorldChange) other;
			boolean diff = !Objects.equals(autoClearForces, o.autoClearForces);
			diff |= !Objects.equals(gravity, o.gravity);
			return diff;
		}

		@Override
		public void reset() {
			oldAutoClearForces = null;
			oldGravity.setZero();

			autoClearForces = null;
			gravity = null;
		}

	}

	/** the changes of a {@link Body}
	 *  @since 0.6.0
	 *  @author dermetfan */
	public static class BodyChange implements Change<Body> {

		private transient final Transform oldTransform = new Transform();
		private transient BodyType oldType;
		private transient float oldAngularDamping;
		private transient float oldAngularVelocity;
		private transient float oldGravityScale;
		private transient final Vector2 oldLinearVelocity = new Vector2();
		private transient final MassData oldMassData = new MassData();
		private transient boolean oldFixedRotation;
		private transient boolean oldBullet;
		private transient boolean oldAwake;
		private transient boolean oldActive;
		private transient boolean oldSleepingAllowed;
		private transient Object oldUserData;

		Transform transform;
		BodyType type;
		Float angularDamping;
		Float angularVelocity;
		Float gravityScale;
		Vector2 linearVelocity;
		MassData massData;
		Boolean fixedRotation;
		Boolean bullet;
		Boolean awake;
		Boolean active;
		Boolean sleepingAllowed;
		Object userData;

		/** if the {@link Body#userData} changed */
		private boolean userDataChanged;

		private void updateOldTransform(Transform transform) {
			oldTransform.vals[Transform.POS_X] = transform.vals[Transform.POS_X];
			oldTransform.vals[Transform.POS_Y] = transform.vals[Transform.POS_Y];
			oldTransform.vals[Transform.COS] = transform.vals[Transform.COS];
			oldTransform.vals[Transform.SIN] = transform.vals[Transform.SIN];
		}

		private void updateOldMassData(MassData massData) {
			oldMassData.center.set(massData.center);
			oldMassData.mass = massData.mass;
			oldMassData.I = massData.I;
		}

		@Override
		public boolean update(Body body) {
			Transform newTransform = body.getTransform();
			BodyType newType = body.getType();
			float newAngularDamping = body.getAngularDamping();
			float newAngularVelocity = body.getAngularVelocity();
			float newGravityScale = body.getGravityScale();
			Vector2 newLinearVelocity = body.getLinearVelocity();
			MassData newMassData = body.getMassData();
			boolean newFixedRotation = body.isFixedRotation();
			boolean newBullet = body.isBullet();
			boolean newAwake = body.isAwake();
			boolean newActive = body.isActive();
			boolean newSleepingAllowed = body.isSleepingAllowed();
			Object newUserData = body.getUserData();

			boolean changed = false;

			if(!Box2DUtils.equals(newTransform, oldTransform)) {
				updateOldTransform(transform = newTransform);
				changed = true;
			} else
				transform = null;
			if(!newType.equals(oldType)) {
				oldType = type = newType;
				changed = true;
			} else
				type = null;
			if(newAngularDamping != oldAngularDamping) {
				oldAngularDamping = angularDamping = newAngularDamping;
				changed = true;
			} else
				angularDamping = null;
			if(newAngularVelocity != oldAngularVelocity) {
				oldAngularVelocity = angularVelocity = newAngularVelocity;
				changed = true;
			} else
				angularVelocity = null;
			if(newGravityScale != oldGravityScale) {
				oldGravityScale = gravityScale = newGravityScale;
				changed = true;
			} else
				gravityScale = null;
			if(!newLinearVelocity.equals(oldLinearVelocity)) {
				oldLinearVelocity.set(linearVelocity = newLinearVelocity);
				changed = true;
			} else
				linearVelocity = null;
			if(!Box2DUtils.equals(newMassData, oldMassData)) {
				updateOldMassData(massData = newMassData);
				changed = true;
			} else
				massData = null;
			if(newFixedRotation != oldFixedRotation) {
				fixedRotation = oldFixedRotation = newFixedRotation;
				changed = true;
			} else
				fixedRotation = null;
			if(newBullet != oldBullet) {
				oldBullet = bullet = newBullet;
				changed = true;
			} else
				bullet = null;
			if(newAwake != oldAwake) {
				oldAwake = awake = newAwake;
				changed = true;
			} else
				awake = null;
			if(newActive != oldActive) {
				active = oldActive = newActive;
				changed = true;
			} else
				active = null;
			if(newSleepingAllowed != oldSleepingAllowed) {
				sleepingAllowed = oldSleepingAllowed = newSleepingAllowed;
				changed = true;
			} else
				sleepingAllowed = null;
			if(newUserData != null ? !newUserData.equals(oldUserData) : oldUserData != null) {
				oldUserData = userData = newUserData;
				changed = userDataChanged = true;
			} else {
				userData = null;
				userDataChanged = false;
			}

			return changed;
		}

		@Override
		public void apply(Body body) {
			if(transform != null)
				body.setTransform(transform.vals[Transform.POS_X], transform.vals[Transform.POS_Y], transform.getRotation());
			if(type != null)
				body.setType(type);
			if(angularDamping != null)
				body.setAngularDamping(angularDamping);
			if(angularVelocity != null)
				body.setAngularVelocity(angularVelocity);
			if(gravityScale != null)
				body.setGravityScale(gravityScale);
			if(linearVelocity != null)
				body.setLinearVelocity(linearVelocity);
			if(massData != null)
				body.setMassData(massData);
			if(fixedRotation != null)
				body.setFixedRotation(fixedRotation);
			if(bullet != null)
				body.setBullet(bullet);
			if(awake != null)
				body.setAwake(awake);
			if(active != null)
				body.setActive(active);
			if(sleepingAllowed != null)
				body.setSleepingAllowed(sleepingAllowed);
			if(userDataChanged)
				body.setUserData(userData);
		}

		@Override
		public <C extends Change<Body>> boolean newValuesEqual(C other) {
			if(!(other instanceof BodyChange))
				return false;
			BodyChange o = (BodyChange) other;
			return Objects.equals(transform, o.transform) &&
					Objects.equals(type, o.type) &&
					Objects.equals(angularDamping, o.angularDamping) &&
					Objects.equals(angularVelocity, o.angularVelocity) &&
					Objects.equals(gravityScale, o.gravityScale) &&
					Objects.equals(linearVelocity, o.linearVelocity) &&
					Objects.equals(massData, o.massData) &&
					Objects.equals(fixedRotation, o.fixedRotation) &&
					Objects.equals(bullet, o.bullet) &&
					Objects.equals(awake, o.awake) &&
					Objects.equals(active, o.active) &&
					Objects.equals(sleepingAllowed, o.sleepingAllowed) &&
					Objects.equals(userData, o.userData);
		}

		@Override
		public void reset() {
			oldTransform.vals[Transform.POS_X] = oldTransform.vals[Transform.POS_Y] = oldTransform.vals[Transform.COS] = oldTransform.vals[Transform.SIN] = 0;
			oldType = null;
			oldAngularDamping = 0;
			oldAngularVelocity = 0;
			oldGravityScale = 0;
			oldLinearVelocity.setZero();
			oldMassData.mass = 0;
			oldMassData.I = 0;
			oldMassData.center.setZero();
			oldFixedRotation = false;
			oldBullet = false;
			oldAwake = false;
			oldActive = false;
			oldSleepingAllowed = false;
			oldUserData = null;

			transform = null;
			type = null;
			angularDamping = null;
			angularVelocity = null;
			gravityScale = null;
			linearVelocity = null;
			massData = null;
			fixedRotation = null;
			bullet = null;
			awake = null;
			active = null;
			sleepingAllowed = null;
			userData = null;

			userDataChanged = false;
		}

	}

	/** the changes of a {@link Fixture} */
	public static class FixtureChange implements Change<Fixture> {

		private transient Body oldBody;
		private transient boolean destroyed;

		private transient float oldDensity;
		private transient float oldFriction;
		private transient float oldRestitution;
		private transient final Filter oldFilter = new Filter();
		private transient Object oldUserData;

		Float density;
		Float friction;
		Float restitution;
		Filter filter;
		Object userData;

		/** if the {@link Fixture#userData} changed */
		boolean userDataChanged;

		/** this should be called when this FixtureChange is going to be used for a fixture on another body to make {@link #destroyed} work correctly */
		void created(Body body) {
			oldBody = body;
		}

		private void updateOldFilter(Filter newFilter) {
			oldFilter.categoryBits = newFilter.categoryBits;
			oldFilter.groupIndex = newFilter.groupIndex;
			oldFilter.maskBits = newFilter.maskBits;
		}

		/** @return the {@link #destroyed} */
		public boolean isDestroyed() {
			return destroyed;
		}

		@Override
		public boolean update(Fixture fixture) {
			Body newBody = fixture.getBody();

			if(newBody != oldBody) {
				destroyed = true;
				oldBody = newBody;
				return false;
			}

			float newDensity = fixture.getDensity();
			float newFriction = fixture.getFriction();
			float newRestitution = fixture.getRestitution();
			Filter newFilter = fixture.getFilterData();
			Object newUserData = fixture.getUserData();

			boolean changed = false;

			if(newDensity != oldDensity) {
				oldDensity = density = newDensity;
				changed = true;
			} else
				density = null;
			if(newFriction != oldFriction) {
				oldFriction = friction = newFriction;
				changed = true;
			} else
				friction = null;
			if(newRestitution != oldRestitution) {
				oldRestitution = restitution = newRestitution;
				changed = true;
			} else
				restitution = null;
			if(!Box2DUtils.equals(newFilter, oldFilter)) {
				updateOldFilter(filter = newFilter);
				changed = true;
			} else
				filter = null;
			if(newUserData != null ? !newUserData.equals(oldUserData) : oldUserData != null) {
				oldUserData = userData = newUserData;
				changed = userDataChanged = true;
			} else {
				userData = null;
				userDataChanged = false;
			}

			return changed;
		}

		/** @throws IllegalStateException if the fixture has been {@link #destroyed} */
		@Override
		public void apply(Fixture fixture) {
			if(destroyed)
				throw new IllegalStateException("destroyed FixtureChanges may not be applied");
			if(density != null)
				fixture.setDensity(density);
			if(friction != null)
				fixture.setFriction(friction);
			if(restitution != null)
				fixture.setRestitution(restitution);
			if(filter != null)
				fixture.setFilterData(filter);
			if(userDataChanged)
				fixture.setUserData(userData);
		}

		@Override
		public <C extends Change<Fixture>> boolean newValuesEqual(C other) {
			if(!(other instanceof FixtureChange))
				return false;
			FixtureChange o = (FixtureChange) other;
			return Objects.equals(density, o.density) &&
					Objects.equals(friction, o.friction) &&
					Objects.equals(restitution, o.restitution) &&
					Objects.equals(filter, o.filter) &&
					Objects.equals(userData, o.userData);
		}

		@Override
		public void reset() {
			oldBody = null;
			destroyed = false;

			oldDensity = 0;
			oldFriction = 0;
			oldRestitution = 0;
			oldFilter.categoryBits = 0x0001;
			oldFilter.maskBits = -1;
			oldFilter.groupIndex = 0;
			oldUserData = null;

			density = null;
			friction = null;
			restitution = null;
			filter = null;
			userData = null;

			userDataChanged = false;
		}

	}

	/** the changes of a {@link Joint}
	 *  @since 0.6.0
	 *  @author dermetfan */
	public static class JointChange<T extends Joint> implements Change<T> {

		/** @return a concrete JointChange from {@link Pools#obtain(Class)} */
		public static JointChange obtainFor(JointType type) {
			Class<? extends JointChange> changeType;
			switch(type) {
			case RevoluteJoint:
				changeType = RevoluteJointChange.class;
				break;
			case PrismaticJoint:
				changeType = PrismaticJointChange.class;
				break;
			case DistanceJoint:
				changeType = DistanceJointChange.class;
				break;
			case PulleyJoint:
				changeType = JointChange.class; // no named PulleyJointChange needed
				break;
			case MouseJoint:
				changeType = MouseJointChange.class;
				break;
			case GearJoint:
				changeType = GearJointChange.class;
				break;
			case WheelJoint:
				changeType = WheelJointChange.class;
				break;
			case WeldJoint:
				changeType = WeldJointChange.class;
				break;
			case FrictionJoint:
				changeType = FrictionJointChange.class;
				break;
			case RopeJoint:
				changeType = RopeJointChange.class;
				break;
			case MotorJoint:
				changeType = MotorJointChange.class;
				break;
			default:
			case Unknown:
				changeType = JointChange.class;
			}
			return Pools.obtain(changeType);
		}

		private transient Object oldUserData;

		Object userData;

		boolean userDataChanged;

		@Override
		public boolean update(T joint) {
			Object newUserData = joint.getUserData();

			boolean changed = false;

			if(newUserData != null ? !newUserData.equals(oldUserData) : oldUserData != null) {
				oldUserData = userData = newUserData;
				changed = userDataChanged = true;
			} else {
				userData = null;
				userDataChanged = false;
			}

			return changed;
		}

		@Override
		public void apply(T joint) {
			if(userDataChanged)
				joint.setUserData(userData);
		}

		@Override
		public <C extends Change<T>> boolean newValuesEqual(C other) {
			return other instanceof JointChange && Objects.equals(userData, ((JointChange) other).userData);
		}

		@Override
		public void reset() {
			oldUserData = null;
			userData = null;
			userDataChanged = false;
		}

	}

	/** the changes of a {@link RevoluteJoint}
	 *  @since 0.7.0
	 *  @author dermetfan */
	public static class RevoluteJointChange extends JointChange<RevoluteJoint> {

		private transient float oldLowerLimit;
		private transient float oldUpperLimit;
		private transient float oldMaxMotorTorque;
		private transient float oldMotorSpeed;

		Float lowerLimit;
		Float upperLimit;
		Float maxMotorTorque;
		Float motorSpeed;

		@Override
		public boolean update(RevoluteJoint joint) {
			float newLowerLimit = joint.getLowerLimit();
			float newUpperLimit = joint.getUpperLimit();
			float newMaxMotorTorque = joint.getMaxMotorTorque();
			float newMotorSpeed = joint.getMotorSpeed();

			boolean changed = super.update(joint);

			if(newLowerLimit != oldLowerLimit) {
				lowerLimit = oldLowerLimit = newLowerLimit;
				changed = true;
			} else
				lowerLimit = null;
			if(newUpperLimit != oldUpperLimit) {
				upperLimit = oldUpperLimit = newUpperLimit;
				changed = true;
			} else
				upperLimit = null;
			if(newMaxMotorTorque != oldMaxMotorTorque) {
				maxMotorTorque = oldMaxMotorTorque = newMaxMotorTorque;
				changed = true;
			} else
				maxMotorTorque = null;
			if(newMotorSpeed != oldMotorSpeed) {
				motorSpeed = oldMotorSpeed = newMotorSpeed;
				changed = true;
			} else
				motorSpeed = null;

			return changed;
		}

		@Override
		public void apply(RevoluteJoint joint) {
			super.apply(joint);
			if(lowerLimit != null || upperLimit != null)
				joint.setLimits(lowerLimit != null ? lowerLimit : joint.getLowerLimit(), upperLimit != null ? upperLimit : joint.getUpperLimit());
			if(maxMotorTorque != null)
				joint.setMaxMotorTorque(maxMotorTorque);
			if(motorSpeed != null)
				joint.setMotorSpeed(motorSpeed);
		}

		@Override
		public <C extends Change<RevoluteJoint>> boolean newValuesEqual(C other) {
			if(!(other instanceof RevoluteJointChange))
				return false;
			RevoluteJointChange o = (RevoluteJointChange) other;
			return super.newValuesEqual(other) &&
					Objects.equals(lowerLimit, o.lowerLimit) &&
					Objects.equals(upperLimit, o.upperLimit) &&
					Objects.equals(maxMotorTorque, o.maxMotorTorque) &&
					Objects.equals(motorSpeed, o.motorSpeed);
		}

		@Override
		public void reset() {
			super.reset();

			oldLowerLimit = 0;
			oldUpperLimit = 0;
			oldMaxMotorTorque = 0;
			oldMotorSpeed = 0;

			lowerLimit = null;
			upperLimit = null;
			maxMotorTorque = null;
			motorSpeed = null;
		}

	}

	/** the changes of a {@link PrismaticJoint}
	 *  @since 0.7.0
	 *  @author dermetfan */
	public static class PrismaticJointChange extends JointChange<PrismaticJoint> {

		private transient float oldLowerLimit;
		private transient float oldUpperLimit;
		private transient float oldMaxMotorTorque;
		private transient float oldMotorSpeed;

		Float lowerLimit;
		Float upperLimit;
		Float maxMotorForce;
		Float motorSpeed;

		@Override
		public boolean update(PrismaticJoint joint) {
			float newLowerLimit = joint.getLowerLimit();
			float newUpperLimit = joint.getUpperLimit();
			float newMaxMotorTorque = joint.getMaxMotorForce();
			float newMotorSpeed = joint.getMotorSpeed();

			boolean changed = super.update(joint);

			if(newLowerLimit != oldLowerLimit) {
				lowerLimit = oldLowerLimit = newLowerLimit;
				changed = true;
			} else
				lowerLimit = null;
			if(newUpperLimit != oldUpperLimit) {
				upperLimit = oldUpperLimit = newUpperLimit;
				changed = true;
			} else
				upperLimit = null;
			if(newMaxMotorTorque != oldMaxMotorTorque) {
				maxMotorForce = oldMaxMotorTorque = newMaxMotorTorque;
				changed = true;
			} else
				maxMotorForce = null;
			if(newMotorSpeed != oldMotorSpeed) {
				motorSpeed = oldMotorSpeed = newMotorSpeed;
				changed = true;
			} else
				motorSpeed = null;

			return changed;
		}

		@Override
		public void apply(PrismaticJoint joint) {
			super.apply(joint);
			if(lowerLimit != null || upperLimit != null)
				joint.setLimits(lowerLimit != null ? lowerLimit : joint.getLowerLimit(), upperLimit != null ? upperLimit : joint.getUpperLimit());
			if(maxMotorForce != null)
				joint.setMaxMotorForce(maxMotorForce);
			if(motorSpeed != null)
				joint.setMotorSpeed(motorSpeed);
		}

		@Override
		public <C extends Change<PrismaticJoint>> boolean newValuesEqual(C other) {
			if(!(other instanceof PrismaticJointChange))
				return false;
			PrismaticJointChange o = (PrismaticJointChange) other;
			return super.newValuesEqual(other) &&
					Objects.equals(lowerLimit, o.lowerLimit) &&
					Objects.equals(upperLimit, o.upperLimit) &&
					Objects.equals(maxMotorForce, o.maxMotorForce) &&
					Objects.equals(motorSpeed, o.motorSpeed);
		}

		@Override
		public void reset() {
			super.reset();

			oldLowerLimit = 0;
			oldUpperLimit = 0;
			oldMaxMotorTorque = 0;
			oldMotorSpeed = 0;

			lowerLimit = null;
			upperLimit = null;
			maxMotorForce = null;
			motorSpeed = null;
		}

	}

	/** the changes of a {@link DistanceJoint}
	 *  @since 0.7.0
	 *  @author dermetfan */
	public static class DistanceJointChange extends JointChange<DistanceJoint> {

		private transient float oldDampingRatio;
		private transient float oldFrequency;
		private transient float oldLength;

		Float dampingRatio;
		Float frequency;
		Float length;

		@Override
		public boolean update(DistanceJoint joint) {
			float newDampingRatio = joint.getDampingRatio();
			float newFrequency = joint.getFrequency();
			float newLength = joint.getLength();

			boolean changed = super.update(joint);

			if(newDampingRatio != oldDampingRatio) {
				dampingRatio = oldDampingRatio = newDampingRatio;
				changed = true;
			} else
				dampingRatio = null;
			if(newFrequency != oldFrequency) {
				frequency = oldFrequency = newFrequency;
				changed = true;
			} else
				frequency = null;
			if(newLength != oldLength) {
				length = oldLength = newLength;
				changed = true;
			} else
				length = null;

			return changed;
		}

		@Override
		public void apply(DistanceJoint joint) {
			super.apply(joint);
			if(dampingRatio != null)
				joint.setDampingRatio(dampingRatio);
			if(frequency != null)
				joint.setFrequency(frequency);
			if(length != null)
				joint.setLength(length);
		}

		@Override
		public <C extends Change<DistanceJoint>> boolean newValuesEqual(C other) {
			if(!(other instanceof DistanceJointChange))
				return false;
			DistanceJointChange o = (DistanceJointChange) other;
			return super.newValuesEqual(other) &&
					Objects.equals(dampingRatio, o.dampingRatio) &&
					Objects.equals(frequency, o.frequency) &&
					Objects.equals(length, o.length);
		}

		@Override
		public void reset() {
			super.reset();

			oldDampingRatio = 0;
			oldFrequency = 0;
			oldLength = 0;

			dampingRatio = null;
			frequency = null;
			length = null;
		}

	}

	/** the changes of a {@link MouseJoint}
	 *  @since 0.7.0
	 *  @author dermetfan */
	public static class MouseJointChange extends JointChange<MouseJoint> {

		private transient float oldDampingRatio;
		private transient float oldFrequency;
		private transient float oldMaxForce;
		private transient final Vector2 oldTarget = new Vector2();

		Float dampingRatio;
		Float frequency;
		Float maxForce;
		Vector2 target;

		@Override
		public boolean update(MouseJoint joint) {
			float newDampingRatio = joint.getDampingRatio();
			float newFrequency = joint.getFrequency();
			float newMaxForce = joint.getMaxForce();
			Vector2 newTarget = joint.getTarget();

			boolean changed = super.update(joint);

			if(newDampingRatio != oldDampingRatio) {
				dampingRatio = oldDampingRatio = newDampingRatio;
				changed = true;
			} else
				dampingRatio = null;
			if(newFrequency != oldFrequency) {
				frequency = oldFrequency = newFrequency;
				changed = true;
			} else
				frequency = null;
			if(newMaxForce != oldMaxForce) {
				maxForce = oldMaxForce = newMaxForce;
				changed = true;
			} else
				maxForce = null;
			if(!newTarget.equals(oldTarget)) {
				oldTarget.set(target = newTarget);
				changed = true;
			} else
				target = null;

			return changed;
		}

		@Override
		public void apply(MouseJoint joint) {
			super.apply(joint);
			if(dampingRatio != null)
				joint.setDampingRatio(dampingRatio);
			if(frequency != null)
				joint.setFrequency(frequency);
			if(maxForce != null)
				joint.setMaxForce(maxForce);
			if(target != null)
				joint.setTarget(target);
		}

		@Override
		public <C extends Change<MouseJoint>> boolean newValuesEqual(C other) {
			if(!(other instanceof MouseJointChange))
				return false;
			MouseJointChange o = (MouseJointChange) other;
			return super.newValuesEqual(other) &&
					Objects.equals(dampingRatio, o.dampingRatio) &&
					Objects.equals(frequency, o.frequency) &&
					Objects.equals(maxForce, o.maxForce);
		}

		@Override
		public void reset() {
			super.reset();

			oldDampingRatio = 0;
			oldFrequency = 0;
			oldMaxForce = 0;
			oldTarget.setZero();

			dampingRatio = null;
			frequency = null;
			maxForce = null;
			target = null;
		}

	}

	/** the changes of a {@link GearJoint}
	 *  @since 0.7.0
	 *  @author dermetfan */
	public static class GearJointChange extends JointChange<GearJoint> {

		private transient float oldRatio;

		Float ratio;

		@Override
		public boolean update(GearJoint joint) {
			float newRatio = joint.getRatio();

			boolean changed = super.update(joint);

			if(newRatio != oldRatio) {
				ratio = oldRatio = newRatio;
				changed = true;
			} else
				ratio = null;

			return changed;
		}

		@Override
		public void apply(GearJoint joint) {
			super.apply(joint);
			if(ratio != null)
				joint.setRatio(ratio);
		}

		@Override
		public <C extends Change<GearJoint>> boolean newValuesEqual(C other) {
			return other instanceof GearJointChange && super.newValuesEqual(other) && Objects.equals(ratio, ((GearJointChange) other).ratio);
		}

		@Override
		public void reset() {
			super.reset();
			oldRatio = 0;
			ratio = null;
		}

	}

	/** the changes of a {@link WheelJoint}
	 *  @since 0.7.0
	 *  @author dermetfan */
	public static class WheelJointChange extends JointChange<WheelJoint> {

		private transient float oldSpringDampingRatio;
		private transient float oldSpringFrequencyHz;
		private transient float oldMaxMotorTorque;
		private transient float oldMotorSpeed;

		Float springDampingRatio;
		Float springFrequencyHz;
		Float maxMotorTorque;
		Float motorSpeed;

		@Override
		public boolean update(WheelJoint joint) {
			float newSprintDampingRatio = joint.getSpringDampingRatio();
			float newSpringFrequencyHz = joint.getSpringFrequencyHz();
			float newMaxMotorTorque = joint.getMaxMotorTorque();
			float newMotorSpeed = joint.getMotorSpeed();

			boolean changed = super.update(joint);

			if(newSprintDampingRatio != oldSpringDampingRatio) {
				springDampingRatio = oldSpringDampingRatio = newSprintDampingRatio;
				changed = true;
			} else
				springDampingRatio = null;
			if(newSpringFrequencyHz != oldSpringFrequencyHz) {
				springFrequencyHz = oldSpringFrequencyHz = newSpringFrequencyHz;
				changed = true;
			} else
				springFrequencyHz = null;
			if(newMaxMotorTorque != oldMaxMotorTorque) {
				maxMotorTorque = oldMaxMotorTorque = newMaxMotorTorque;
				changed = true;
			} else
				maxMotorTorque = null;
			if(newMotorSpeed != oldMotorSpeed) {
				motorSpeed = oldMotorSpeed = newMotorSpeed;
				changed = true;
			} else
				motorSpeed = null;

			return changed;
		}

		@Override
		public void apply(WheelJoint joint) {
			super.apply(joint);
			if(springDampingRatio != null)
				joint.setSpringDampingRatio(springDampingRatio);
			if(springFrequencyHz != null)
				joint.setSpringFrequencyHz(springFrequencyHz);
			if(maxMotorTorque != null)
				joint.setMaxMotorTorque(maxMotorTorque);
			if(motorSpeed != null)
				joint.setMotorSpeed(motorSpeed);
		}

		@Override
		public <C extends Change<WheelJoint>> boolean newValuesEqual(C other) {
			if(!(other instanceof WheelJointChange))
				return false;
			WheelJointChange o = (WheelJointChange) other;
			return super.newValuesEqual(other) &&
					Objects.equals(springDampingRatio, o.springDampingRatio) &&
					Objects.equals(springFrequencyHz, o.springFrequencyHz) &&
					Objects.equals(maxMotorTorque, o.maxMotorTorque) &&
					Objects.equals(motorSpeed, o.motorSpeed);
		}

		@Override
		public void reset() {
			super.reset();

			oldSpringDampingRatio = 0;
			oldSpringFrequencyHz = 0;
			oldMaxMotorTorque = 0;
			oldMotorSpeed = 0;

			springDampingRatio = null;
			springFrequencyHz = null;
			maxMotorTorque = null;
			motorSpeed = null;
		}

	}

	/** the changes of a {@link WeldJoint}
	 *  @since 0.7.0
	 *  @author dermetfan */
	public static class WeldJointChange extends JointChange<WeldJoint> {

		private transient float oldDampingRatio;
		private transient float oldFrequency;

		Float dampingRatio;
		Float frequency;

		@Override
		public boolean update(WeldJoint joint) {
			float newDampingRatio = joint.getDampingRatio();
			float newFrequency = joint.getFrequency();

			boolean changed = super.update(joint);

			if(newDampingRatio != oldDampingRatio) {
				dampingRatio = oldDampingRatio = newDampingRatio;
				changed = true;
			} else
				dampingRatio = null;
			if(newFrequency != oldFrequency) {
				frequency = oldFrequency = newFrequency;
				changed = true;
			} else
				frequency = null;

			return changed;
		}

		@Override
		public void apply(WeldJoint joint) {
			super.apply(joint);
			if(dampingRatio != null)
				joint.setDampingRatio(dampingRatio);
			if(frequency != null)
				joint.setFrequency(frequency);
		}

		@Override
		public <C extends Change<WeldJoint>> boolean newValuesEqual(C other) {
			if(!(other instanceof WeldJointChange))
				return false;
			WeldJointChange o = (WeldJointChange) other;
			return super.newValuesEqual(other) &&
					Objects.equals(dampingRatio, o.dampingRatio) &&
					Objects.equals(frequency, o.frequency);
		}

		@Override
		public void reset() {
			super.reset();

			oldDampingRatio = 0;
			oldFrequency = 0;

			dampingRatio = null;
			frequency = null;
		}

	}

	/** the changes of a {@link FrictionJoint}
	 *  @since 0.7.0
	 *  @author dermetfan */
	public static class FrictionJointChange extends JointChange<FrictionJoint> {

		private transient float oldMaxForce;
		private transient float oldMaxTorque;

		Float maxForce;
		Float maxTorque;

		@Override
		public boolean update(FrictionJoint joint) {
			float newMaxForce = joint.getMaxForce();
			float newMaxTorque = joint.getMaxTorque();

			boolean changed = super.update(joint);

			if(newMaxForce != oldMaxForce) {
				maxForce = oldMaxForce = newMaxForce;
				changed = true;
			} else
				maxForce = null;
			if(newMaxTorque != oldMaxTorque) {
				maxTorque = oldMaxTorque = newMaxTorque;
				changed = true;
			} else
				maxTorque = null;

			return changed;
		}

		@Override
		public void apply(FrictionJoint joint) {
			super.apply(joint);
			if(maxForce != null)
				joint.setMaxForce(maxForce);
			if(maxTorque != null)
				joint.setMaxTorque(maxTorque);
		}

		@Override
		public <C extends Change<FrictionJoint>> boolean newValuesEqual(C other) {
			if(!(other instanceof FrictionJointChange))
				return false;
			FrictionJointChange o = (FrictionJointChange) other;
			return super.newValuesEqual(other) &&
					Objects.equals(maxForce, o.maxForce) &&
					Objects.equals(maxTorque, o.maxTorque);
		}

		@Override
		public void reset() {
			super.reset();

			oldMaxForce = 0;
			oldMaxTorque = 0;

			maxForce = null;
			maxTorque = null;
		}

	}

	/** the changes of a {@link RopeJoint}
	 *  @since 0.7.0
	 *  @author dermetfan */
	public static class RopeJointChange extends JointChange<RopeJoint> {

		private transient float oldMaxLength;

		Float maxLength;

		@Override
		public boolean update(RopeJoint joint) {
			float newMaxLength = joint.getMaxLength();

			boolean changed = super.update(joint);

			if(newMaxLength != oldMaxLength) {
				maxLength = oldMaxLength = newMaxLength;
				changed = true;
			} else
				maxLength = null;

			return changed;
		}

		@Override
		public void apply(RopeJoint joint) {
			super.apply(joint);
			if(maxLength != null)
				joint.setMaxLength(maxLength);
		}

		@Override
		public <C extends Change<RopeJoint>> boolean newValuesEqual(C other) {
			return other instanceof RopeJointChange && super.newValuesEqual(other) && Objects.equals(maxLength, ((RopeJointChange) other).maxLength);
		}

		@Override
		public void reset() {
			super.reset();
			oldMaxLength = 0;
			maxLength = null;
		}

	}

	/** the changes of a {@link MotorJoint}
	 *  @since 0.7.0
	 *  @author dermetfan */
	public static class MotorJointChange extends JointChange<MotorJoint> {

		private transient float oldMaxForce;
		private transient float oldMaxTorque;
		private transient float oldCorrectionFactor;
		private transient float oldAngularOffset;
		private transient final Vector2 oldLinearOffset = new Vector2();

		Float maxForce;
		Float maxTorque;
		Float correctionFactor;
		Float angularOffset;
		Vector2 linearOffset;

		@Override
		public boolean update(MotorJoint joint) {
			float newMaxForce = joint.getMaxForce();
			float newMaxTorque = joint.getMaxTorque();
			float newCorrectionFactor = joint.getCorrectionFactor();
			float newAngularOffset = joint.getAngularOffset();
			Vector2 newLinearOffset = joint.getLinearOffset();

			boolean changed = super.update(joint);

			if(newMaxForce != oldMaxForce) {
				maxForce = oldMaxForce = newMaxForce;
				changed = true;
			} else
				maxForce = null;
			if(newMaxTorque != oldMaxTorque) {
				maxTorque = oldMaxTorque = newMaxTorque;
				changed = true;
			} else
				maxTorque = null;
			if(newCorrectionFactor != oldCorrectionFactor) {
				correctionFactor = oldCorrectionFactor = newCorrectionFactor;
				changed = true;
			} else
				correctionFactor = null;
			if(newAngularOffset != oldAngularOffset) {
				angularOffset = oldAngularOffset = newAngularOffset;
				changed = true;
			} else
				angularOffset = null;
			if(!newLinearOffset.equals(oldLinearOffset)) {
				oldLinearOffset.set(linearOffset = newLinearOffset);
				changed = true;
			} else
				linearOffset = null;

			return changed;
		}

		@Override
		public void apply(MotorJoint joint) {
			super.apply(joint);
			if(maxForce != null)
				joint.setMaxForce(maxForce);
			if(maxTorque != null)
				joint.setMaxForce(maxTorque);
			if(correctionFactor != null)
				joint.setCorrectionFactor(correctionFactor);
			if(angularOffset != null)
				joint.setAngularOffset(angularOffset);
			if(linearOffset != null)
				joint.setLinearOffset(linearOffset);
		}

		@Override
		public <C extends Change<MotorJoint>> boolean newValuesEqual(C other) {
			if(!(other instanceof MotorJointChange))
				return false;
			MotorJointChange o = (MotorJointChange) other;
			return super.newValuesEqual(other) &&
					Objects.equals(angularOffset, o.angularOffset) &&
					Objects.equals(correctionFactor, o.correctionFactor) &&
					Objects.equals(linearOffset, o.linearOffset) &&
					Objects.equals(maxForce, o.maxForce);
		}

		@Override
		public void reset() {
			super.reset();

			oldMaxForce = 0;
			oldMaxTorque = 0;
			oldCorrectionFactor = 0;
			oldAngularOffset = 0;
			oldLinearOffset.setZero();

			maxForce = null;
			maxTorque = null;
			correctionFactor = null;
			angularOffset = null;
			linearOffset = null;
		}

	}

}
