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
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pool.Poolable;
import com.badlogic.gdx.utils.Pools;

/** notifies a {@link Listener} of changes in the world
 *  @author dermetfan
 *  @since 0.6.0 */
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
			@SuppressWarnings("unchecked")
			JointChange<Joint> jointChange = jointChanges.get(joint);
			if(jointChange != null) { // updated
				if(jointChange.update(joint) && listener != null)
					listener.changed(joint, jointChange);
			} else { // new
				@SuppressWarnings("unchecked")
				JointChange<Joint> newJointChange = JointChange.obtainFor(joint.getType());
				newJointChange.update(joint);
				jointChanges.put(joint, newJointChange);
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
	 *  @author dermetfan
	 *  @since 0.6.0 */
	public interface Listener {

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
		 *  @author dermetfan
		 *  @since 0.7.1 */
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
	 *  In practice only {@link #changed(Body, WorldObserver.BodyChange)} can be predicted and therefore the other methods will be called normally.
	 *  @author dermetfan
	 *  @since 0.7.1 */
	public static class UnexpectedListener implements Listener {

		/** the Listener to notify */
		private Listener listener;

		/** the ExpectationBases mapped to their Bodies */
		private final ObjectMap<Body, ExpectationBase> bases = new ObjectMap<>();

		/** the Pool used by this UnexpectedListener */
		private final Pool<ExpectationBase> pool = new Pool<ExpectationBase>(5, 75) {
			@Override
			protected ExpectationBase newObject() {
				return new ExpectationBase();
			}
		};

		/** the last time step */
		private float step;

		/** @param listener the {@link #listener} to set */
		public UnexpectedListener(Listener listener) {
			this.listener = listener;
		}

		@Override
		public void changed(Body body, BodyChange change) {
			boolean unexpected = change.newType != null || change.newAngularDamping != null || change.newGravityScale != null || change.newMassData != null || change.userDataChanged;
			ExpectationBase base = bases.get(body);
			if(!unexpected && change.newLinearVelocity != null && !change.newLinearVelocity.equals(base.linearVelocity.mulAdd(body.getWorld().getGravity(), step).scl(1 / (1 + step * body.getLinearDamping()))))
				unexpected = true;
			else if(change.newTransform != null && // the linear damping of the body must be applied to the linear velocity of the base already
					change.newTransform.vals[Transform.POS_X] != base.transform.vals[Transform.POS_X] + base.linearVelocity.x * step &&
					change.newTransform.vals[Transform.POS_Y] != base.transform.vals[Transform.POS_Y] + base.linearVelocity.y * step)
				unexpected = true;
			else if(change.newAngularVelocity != null && change.newAngularVelocity != base.angularVelocity * (1 / (1 + step * body.getAngularDamping())))
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
		 *  @author dermetfan
		 *  @since 0.7.1 */
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

		}

	}

	/** the changes of an object in a world since the last time {@link #update(Object)} was called
	 *  @author dermetfan
	 *  @since 0.6.0 */
	public interface Change<T> extends Poolable {

		/** @param obj the object to check for changes since the last time this method was called
		 *  @return if anything changed */
		boolean update(T obj);

		/** @param obj the object to apply the changes since {@link #update(Object)} to */
		void apply(T obj);

		/** if the values applied in {@link #apply(Object)} equal */
		<C extends Change<T>> boolean newValuesEqual(C other);

	}

	/** the changes of a {@link World}
	 *  @author dermetfan
	 *  @since 0.6.0 */
	public static class WorldChange implements Change<World> {

		private transient Boolean oldAutoClearForces;
		private transient final Vector2 oldGravity = new Vector2();

		public Boolean newAutoClearForces;
		public Vector2 newGravity;

		@Override
		public boolean update(World world) {
			Boolean autoClearForces = world.getAutoClearForces();
			Vector2 gravity = world.getGravity();

			boolean changed = false;

			if(!autoClearForces.equals(oldAutoClearForces)) {
				oldAutoClearForces = newAutoClearForces = autoClearForces;
				changed = true;
			} else
				newAutoClearForces = null;
			if(!gravity.equals(oldGravity)) {
				oldGravity.set(newGravity = gravity);
				changed = true;
			} else
				newAutoClearForces = null;

			return changed;
		}

		@Override
		public void apply(World world) {
			if(newAutoClearForces != null)
				world.setAutoClearForces(newAutoClearForces);
			if(newGravity != null)
				world.setGravity(newGravity);
		}

		@Override
		public <C extends Change<World>> boolean newValuesEqual(C other) {
			if(!(other instanceof WorldChange))
				return false;
			WorldChange o = (WorldChange) other;
			boolean diff = !Objects.equals(newAutoClearForces, o.newAutoClearForces);
			diff |= !Objects.equals(newGravity, o.newGravity);
			return diff;
		}

		@Override
		public void reset() {
			oldAutoClearForces = null;
			oldGravity.setZero();

			newAutoClearForces = null;
			newGravity = null;
		}

	}

	/** the changes of a {@link Body}
	 *  @author dermetfan
	 *  @since 0.6.0 */
	public static class BodyChange implements Change<Body> {

		private transient final Transform oldTransform = new Transform();
		private transient BodyType oldType;
		private transient float oldAngularDamping;
		private transient float oldAngularVelocity;
		private transient float oldLinearDamping;
		private transient float oldGravityScale;
		private transient final Vector2 oldLinearVelocity = new Vector2();
		private transient final MassData oldMassData = new MassData();
		private transient boolean oldFixedRotation;
		private transient boolean oldBullet;
		private transient boolean oldAwake;
		private transient boolean oldActive;
		private transient boolean oldSleepingAllowed;
		private transient Object oldUserData;

		public Transform newTransform;
		public BodyType newType;
		public Float newAngularDamping;
		public Float newAngularVelocity;
		public Float newLinearDamping;
		public Float newGravityScale;
		public Vector2 newLinearVelocity;
		public MassData newMassData;
		public Boolean newFixedRotation;
		public Boolean newBullet;
		public Boolean newAwake;
		public Boolean newActive;
		public Boolean newSleepingAllowed;
		public Object newUserData;

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
			Transform transform = body.getTransform();
			BodyType type = body.getType();
			float angularDamping = body.getAngularDamping();
			float angularVelocity = body.getAngularVelocity();
			float linearDamping = body.getLinearDamping();
			float gravityScale = body.getGravityScale();
			Vector2 linearVelocity = body.getLinearVelocity();
			MassData massData = body.getMassData();
			boolean fixedRotation = body.isFixedRotation();
			boolean bullet = body.isBullet();
			boolean awake = body.isAwake();
			boolean active = body.isActive();
			boolean sleepingAllowed = body.isSleepingAllowed();
			Object userData = body.getUserData();

			boolean changed = false;

			if(!Box2DUtils.equals(transform, oldTransform)) {
				updateOldTransform(newTransform = transform);
				changed = true;
			} else
				newTransform = null;
			if(!type.equals(oldType)) {
				oldType = newType = type;
				changed = true;
			} else
				newType = null;
			if(angularDamping != oldAngularDamping) {
				oldAngularDamping = newAngularDamping = angularDamping;
				changed = true;
			} else
				newAngularDamping = null;
			if(angularVelocity != oldAngularVelocity) {
				oldAngularVelocity = newAngularVelocity = angularVelocity;
				changed = true;
			} else
				newAngularVelocity = null;
			if(linearDamping != oldLinearDamping) {
				oldLinearDamping = newLinearDamping = linearDamping;
				changed = true;
			} else
				newLinearDamping = null;
			if(gravityScale != oldGravityScale) {
				oldGravityScale = newGravityScale = gravityScale;
				changed = true;
			} else
				newGravityScale = null;
			if(!linearVelocity.equals(oldLinearVelocity)) {
				oldLinearVelocity.set(newLinearVelocity = linearVelocity);
				changed = true;
			} else
				newLinearVelocity = null;
			if(!Box2DUtils.equals(massData, oldMassData)) {
				updateOldMassData(newMassData = massData);
				changed = true;
			} else
				newMassData = null;
			if(fixedRotation != oldFixedRotation) {
				newFixedRotation = oldFixedRotation = fixedRotation;
				changed = true;
			} else
				newFixedRotation = null;
			if(bullet != oldBullet) {
				oldBullet = newBullet = bullet;
				changed = true;
			} else
				newBullet = null;
			if(awake != oldAwake) {
				oldAwake = newAwake = awake;
				changed = true;
			} else
				newAwake = null;
			if(active != oldActive) {
				newActive = oldActive = active;
				changed = true;
			} else
				newActive = null;
			if(sleepingAllowed != oldSleepingAllowed) {
				newSleepingAllowed = oldSleepingAllowed = sleepingAllowed;
				changed = true;
			} else
				newSleepingAllowed = null;
			if(userData != null ? !userData.equals(oldUserData) : oldUserData != null) {
				oldUserData = newUserData = userData;
				changed = userDataChanged = true;
			} else {
				newUserData = null;
				userDataChanged = false;
			}

			return changed;
		}

		@Override
		public void apply(Body body) {
			if(newTransform != null)
				body.setTransform(newTransform.vals[Transform.POS_X], newTransform.vals[Transform.POS_Y], newTransform.getRotation());
			if(newType != null)
				body.setType(newType);
			if(newAngularDamping != null)
				body.setAngularDamping(newAngularDamping);
			if(newAngularVelocity != null)
				body.setAngularVelocity(newAngularVelocity);
			if(newLinearDamping != null)
				body.setLinearDamping(newLinearDamping);
			if(newGravityScale != null)
				body.setGravityScale(newGravityScale);
			if(newLinearVelocity != null)
				body.setLinearVelocity(newLinearVelocity);
			if(newMassData != null)
				body.setMassData(newMassData);
			if(newFixedRotation != null)
				body.setFixedRotation(newFixedRotation);
			if(newBullet != null)
				body.setBullet(newBullet);
			if(newAwake != null)
				body.setAwake(newAwake);
			if(newActive != null)
				body.setActive(newActive);
			if(newSleepingAllowed != null)
				body.setSleepingAllowed(newSleepingAllowed);
			if(userDataChanged)
				body.setUserData(newUserData);
		}

		@Override
		public <C extends Change<Body>> boolean newValuesEqual(C other) {
			if(!(other instanceof BodyChange))
				return false;
			BodyChange o = (BodyChange) other;
			return Objects.equals(newTransform, o.newTransform) &&
					Objects.equals(newType, o.newType) &&
					Objects.equals(newAngularDamping, o.newAngularDamping) &&
					Objects.equals(newAngularVelocity, o.newAngularVelocity) &&
					Objects.equals(newLinearDamping, o.newLinearDamping) &&
					Objects.equals(newGravityScale, o.newGravityScale) &&
					Objects.equals(newLinearVelocity, o.newLinearVelocity) &&
					Objects.equals(newMassData, o.newMassData) &&
					Objects.equals(newFixedRotation, o.newFixedRotation) &&
					Objects.equals(newBullet, o.newBullet) &&
					Objects.equals(newAwake, o.newAwake) &&
					Objects.equals(newActive, o.newActive) &&
					Objects.equals(newSleepingAllowed, o.newSleepingAllowed) &&
					Objects.equals(newUserData, o.newUserData);
		}

		@Override
		public void reset() {
			oldTransform.vals[Transform.POS_X] = oldTransform.vals[Transform.POS_Y] = oldTransform.vals[Transform.COS] = oldTransform.vals[Transform.SIN] = 0;
			oldType = null;
			oldAngularDamping = 0;
			oldAngularVelocity = 0;
			oldLinearDamping = 0;
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

			newTransform = null;
			newType = null;
			newAngularDamping = null;
			newAngularVelocity = null;
			newLinearDamping = null;
			newGravityScale = null;
			newLinearVelocity = null;
			newMassData = null;
			newFixedRotation = null;
			newBullet = null;
			newAwake = null;
			newActive = null;
			newSleepingAllowed = null;
			newUserData = null;

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
		private transient boolean oldSensor;
		private transient Object oldUserData;

		public Float newDensity;
		public Float newFriction;
		public Float newRestitution;
		public Filter newFilter;
		public Boolean newSensor;
		public Object newUserData;

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
			Body body = fixture.getBody();

			if(body != oldBody) {
				destroyed = true;
				oldBody = body;
				return false;
			}

			float density = fixture.getDensity();
			float friction = fixture.getFriction();
			float restitution = fixture.getRestitution();
			Filter filter = fixture.getFilterData();
			boolean sensor = fixture.isSensor();
			Object userData = fixture.getUserData();

			boolean changed = false;

			if(density != oldDensity) {
				oldDensity = newDensity = density;
				changed = true;
			} else
				newDensity = null;
			if(friction != oldFriction) {
				oldFriction = newFriction = friction;
				changed = true;
			} else
				newFriction = null;
			if(restitution != oldRestitution) {
				oldRestitution = newRestitution = restitution;
				changed = true;
			} else
				newRestitution = null;
			if(!Box2DUtils.equals(filter, oldFilter)) {
				updateOldFilter(newFilter = filter);
				changed = true;
			} else
				newFilter = null;
			if(sensor != oldSensor) {
				oldSensor = newSensor = sensor;
				changed = true;
			} else
				newSensor = null;
			if(userData != null ? !userData.equals(oldUserData) : oldUserData != null) {
				oldUserData = newUserData = userData;
				changed = userDataChanged = true;
			} else {
				newUserData = null;
				userDataChanged = false;
			}

			return changed;
		}

		/** @throws IllegalStateException if the fixture has been {@link #destroyed} */
		@Override
		public void apply(Fixture fixture) {
			if(destroyed)
				throw new IllegalStateException("destroyed FixtureChanges may not be applied");
			if(newDensity != null)
				fixture.setDensity(newDensity);
			if(newFriction != null)
				fixture.setFriction(newFriction);
			if(newRestitution != null)
				fixture.setRestitution(newRestitution);
			if(newFilter != null)
				fixture.setFilterData(newFilter);
			if(newSensor != null)
				fixture.setSensor(newSensor);
			if(userDataChanged)
				fixture.setUserData(newUserData);
		}

		@Override
		public <C extends Change<Fixture>> boolean newValuesEqual(C other) {
			if(!(other instanceof FixtureChange))
				return false;
			FixtureChange o = (FixtureChange) other;
			return Objects.equals(newDensity, o.newDensity) &&
					Objects.equals(newFriction, o.newFriction) &&
					Objects.equals(newRestitution, o.newRestitution) &&
					Objects.equals(newFilter, o.newFilter) &&
					Objects.equals(newSensor, o.newSensor) &&
					Objects.equals(newUserData, o.newUserData);
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
			oldSensor = false;
			oldUserData = null;

			newDensity = null;
			newFriction = null;
			newRestitution = null;
			newFilter = null;
			newSensor = null;
			newUserData = null;

			userDataChanged = false;
		}

	}

	/** the changes of a {@link Joint}
	 *  @author dermetfan
	 *  @since 0.6.0 */
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

		public Object newUserData;

		boolean userDataChanged;

		@Override
		public boolean update(T joint) {
			Object userData = joint.getUserData();

			boolean changed = false;

			if(userData != null ? !userData.equals(oldUserData) : oldUserData != null) {
				oldUserData = newUserData = userData;
				changed = userDataChanged = true;
			} else {
				newUserData = null;
				userDataChanged = false;
			}

			return changed;
		}

		@Override
		public void apply(T joint) {
			if(userDataChanged)
				joint.setUserData(newUserData);
		}

		@Override
		public <C extends Change<T>> boolean newValuesEqual(C other) {
			return other instanceof JointChange && Objects.equals(newUserData, ((JointChange) other).newUserData);
		}

		@Override
		public void reset() {
			oldUserData = null;
			newUserData = null;
			userDataChanged = false;
		}

	}

	/** the changes of a {@link RevoluteJoint}
	 *  @author dermetfan
	 *  @since 0.7.1 */
	public static class RevoluteJointChange extends JointChange<RevoluteJoint> {

		private transient float oldLowerLimit;
		private transient float oldUpperLimit;
		private transient float oldMaxMotorTorque;
		private transient float oldMotorSpeed;

		public Float newLowerLimit;
		public Float newUpperLimit;
		public Float newMaxMotorTorque;
		public Float newMotorSpeed;

		@Override
		public boolean update(RevoluteJoint joint) {
			float lowerLimit = joint.getLowerLimit();
			float upperLimit = joint.getUpperLimit();
			float maxMotorTorque = joint.getMaxMotorTorque();
			float motorSpeed = joint.getMotorSpeed();

			boolean changed = super.update(joint);

			if(lowerLimit != oldLowerLimit) {
				newLowerLimit = oldLowerLimit = lowerLimit;
				changed = true;
			} else
				newLowerLimit = null;
			if(upperLimit != oldUpperLimit) {
				newUpperLimit = oldUpperLimit = upperLimit;
				changed = true;
			} else
				newUpperLimit = null;
			if(maxMotorTorque != oldMaxMotorTorque) {
				newMaxMotorTorque = oldMaxMotorTorque = maxMotorTorque;
				changed = true;
			} else
				newMaxMotorTorque = null;
			if(motorSpeed != oldMotorSpeed) {
				newMotorSpeed = oldMotorSpeed = motorSpeed;
				changed = true;
			} else
				newMotorSpeed = null;

			return changed;
		}

		@Override
		public void apply(RevoluteJoint joint) {
			super.apply(joint);
			if(newLowerLimit != null || newUpperLimit != null)
				joint.setLimits(newLowerLimit != null ? newLowerLimit : joint.getLowerLimit(), newUpperLimit != null ? newUpperLimit : joint.getUpperLimit());
			if(newMaxMotorTorque != null)
				joint.setMaxMotorTorque(newMaxMotorTorque);
			if(newMotorSpeed != null)
				joint.setMotorSpeed(newMotorSpeed);
		}

		@Override
		public <C extends Change<RevoluteJoint>> boolean newValuesEqual(C other) {
			if(!(other instanceof RevoluteJointChange))
				return false;
			RevoluteJointChange o = (RevoluteJointChange) other;
			return super.newValuesEqual(other) &&
					Objects.equals(newLowerLimit, o.newLowerLimit) &&
					Objects.equals(newUpperLimit, o.newUpperLimit) &&
					Objects.equals(newMaxMotorTorque, o.newMaxMotorTorque) &&
					Objects.equals(newMotorSpeed, o.newMotorSpeed);
		}

		@Override
		public void reset() {
			super.reset();

			oldLowerLimit = 0;
			oldUpperLimit = 0;
			oldMaxMotorTorque = 0;
			oldMotorSpeed = 0;

			newLowerLimit = null;
			newUpperLimit = null;
			newMaxMotorTorque = null;
			newMotorSpeed = null;
		}

	}

	/** the changes of a {@link PrismaticJoint}
	 *  @author dermetfan
	 *  @since 0.7.1 */
	public static class PrismaticJointChange extends JointChange<PrismaticJoint> {

		private transient float oldLowerLimit;
		private transient float oldUpperLimit;
		private transient float oldMaxMotorTorque;
		private transient float oldMotorSpeed;

		public Float newLowerLimit;
		public Float newUpperLimit;
		public Float newMaxMotorForce;
		public Float newMotorSpeed;

		@Override
		public boolean update(PrismaticJoint joint) {
			float lowerLimit = joint.getLowerLimit();
			float upperLimit = joint.getUpperLimit();
			float maxMotorTorque = joint.getMaxMotorForce();
			float motorSpeed = joint.getMotorSpeed();

			boolean changed = super.update(joint);

			if(lowerLimit != oldLowerLimit) {
				newLowerLimit = oldLowerLimit = lowerLimit;
				changed = true;
			} else
				newLowerLimit = null;
			if(upperLimit != oldUpperLimit) {
				newUpperLimit = oldUpperLimit = upperLimit;
				changed = true;
			} else
				newUpperLimit = null;
			if(maxMotorTorque != oldMaxMotorTorque) {
				newMaxMotorForce = oldMaxMotorTorque = maxMotorTorque;
				changed = true;
			} else
				newMaxMotorForce = null;
			if(motorSpeed != oldMotorSpeed) {
				newMotorSpeed = oldMotorSpeed = motorSpeed;
				changed = true;
			} else
				newMotorSpeed = null;

			return changed;
		}

		@Override
		public void apply(PrismaticJoint joint) {
			super.apply(joint);
			if(newLowerLimit != null || newUpperLimit != null)
				joint.setLimits(newLowerLimit != null ? newLowerLimit : joint.getLowerLimit(), newUpperLimit != null ? newUpperLimit : joint.getUpperLimit());
			if(newMaxMotorForce != null)
				joint.setMaxMotorForce(newMaxMotorForce);
			if(newMotorSpeed != null)
				joint.setMotorSpeed(newMotorSpeed);
		}

		@Override
		public <C extends Change<PrismaticJoint>> boolean newValuesEqual(C other) {
			if(!(other instanceof PrismaticJointChange))
				return false;
			PrismaticJointChange o = (PrismaticJointChange) other;
			return super.newValuesEqual(other) &&
					Objects.equals(newLowerLimit, o.newLowerLimit) &&
					Objects.equals(newUpperLimit, o.newUpperLimit) &&
					Objects.equals(newMaxMotorForce, o.newMaxMotorForce) &&
					Objects.equals(newMotorSpeed, o.newMotorSpeed);
		}

		@Override
		public void reset() {
			super.reset();

			oldLowerLimit = 0;
			oldUpperLimit = 0;
			oldMaxMotorTorque = 0;
			oldMotorSpeed = 0;

			newLowerLimit = null;
			newUpperLimit = null;
			newMaxMotorForce = null;
			newMotorSpeed = null;
		}

	}

	/** the changes of a {@link DistanceJoint}
	 *  @author dermetfan
	 *  @since 0.7.1 */
	public static class DistanceJointChange extends JointChange<DistanceJoint> {

		private transient float oldDampingRatio;
		private transient float oldFrequency;
		private transient float oldLength;

		public Float newDampingRatio;
		public Float newFrequency;
		public Float newLength;

		@Override
		public boolean update(DistanceJoint joint) {
			float dampingRatio = joint.getDampingRatio();
			float frequency = joint.getFrequency();
			float length = joint.getLength();

			boolean changed = super.update(joint);

			if(dampingRatio != oldDampingRatio) {
				newDampingRatio = oldDampingRatio = dampingRatio;
				changed = true;
			} else
				newDampingRatio = null;
			if(frequency != oldFrequency) {
				newFrequency = oldFrequency = frequency;
				changed = true;
			} else
				newFrequency = null;
			if(length != oldLength) {
				newLength = oldLength = length;
				changed = true;
			} else
				newLength = null;

			return changed;
		}

		@Override
		public void apply(DistanceJoint joint) {
			super.apply(joint);
			if(newDampingRatio != null)
				joint.setDampingRatio(newDampingRatio);
			if(newFrequency != null)
				joint.setFrequency(newFrequency);
			if(newLength != null)
				joint.setLength(newLength);
		}

		@Override
		public <C extends Change<DistanceJoint>> boolean newValuesEqual(C other) {
			if(!(other instanceof DistanceJointChange))
				return false;
			DistanceJointChange o = (DistanceJointChange) other;
			return super.newValuesEqual(other) &&
					Objects.equals(newDampingRatio, o.newDampingRatio) &&
					Objects.equals(newFrequency, o.newFrequency) &&
					Objects.equals(newLength, o.newLength);
		}

		@Override
		public void reset() {
			super.reset();

			oldDampingRatio = 0;
			oldFrequency = 0;
			oldLength = 0;

			newDampingRatio = null;
			newFrequency = null;
			newLength = null;
		}

	}

	/** the changes of a {@link MouseJoint}
	 *  @author dermetfan
	 *  @since 0.7.1 */
	public static class MouseJointChange extends JointChange<MouseJoint> {

		private transient float oldDampingRatio;
		private transient float oldFrequency;
		private transient float oldMaxForce;
		private transient final Vector2 oldTarget = new Vector2();

		public Float newDampingRatio;
		public Float newFrequency;
		public Float newMaxForce;
		public Vector2 newTarget;

		@Override
		public boolean update(MouseJoint joint) {
			float dampingRatio = joint.getDampingRatio();
			float frequency = joint.getFrequency();
			float maxForce = joint.getMaxForce();
			Vector2 target = joint.getTarget();

			boolean changed = super.update(joint);

			if(dampingRatio != oldDampingRatio) {
				newDampingRatio = oldDampingRatio = dampingRatio;
				changed = true;
			} else
				newDampingRatio = null;
			if(frequency != oldFrequency) {
				newFrequency = oldFrequency = frequency;
				changed = true;
			} else
				newFrequency = null;
			if(maxForce != oldMaxForce) {
				newMaxForce = oldMaxForce = maxForce;
				changed = true;
			} else
				newMaxForce = null;
			if(!target.equals(oldTarget)) {
				oldTarget.set(newTarget = target);
				changed = true;
			} else
				newTarget = null;

			return changed;
		}

		@Override
		public void apply(MouseJoint joint) {
			super.apply(joint);
			if(newDampingRatio != null)
				joint.setDampingRatio(newDampingRatio);
			if(newFrequency != null)
				joint.setFrequency(newFrequency);
			if(newMaxForce != null)
				joint.setMaxForce(newMaxForce);
			if(newTarget != null)
				joint.setTarget(newTarget);
		}

		@Override
		public <C extends Change<MouseJoint>> boolean newValuesEqual(C other) {
			if(!(other instanceof MouseJointChange))
				return false;
			MouseJointChange o = (MouseJointChange) other;
			return super.newValuesEqual(other) &&
					Objects.equals(newDampingRatio, o.newDampingRatio) &&
					Objects.equals(newFrequency, o.newFrequency) &&
					Objects.equals(newMaxForce, o.newMaxForce);
		}

		@Override
		public void reset() {
			super.reset();

			oldDampingRatio = 0;
			oldFrequency = 0;
			oldMaxForce = 0;
			oldTarget.setZero();

			newDampingRatio = null;
			newFrequency = null;
			newMaxForce = null;
			newTarget = null;
		}

	}

	/** the changes of a {@link GearJoint}
	 *  @author dermetfan
	 *  @since 0.7.1 */
	public static class GearJointChange extends JointChange<GearJoint> {

		private transient float oldRatio;

		public Float newRatio;

		@Override
		public boolean update(GearJoint joint) {
			float ratio = joint.getRatio();

			boolean changed = super.update(joint);

			if(ratio != oldRatio) {
				newRatio = oldRatio = ratio;
				changed = true;
			} else
				newRatio = null;

			return changed;
		}

		@Override
		public void apply(GearJoint joint) {
			super.apply(joint);
			if(newRatio != null)
				joint.setRatio(newRatio);
		}

		@Override
		public <C extends Change<GearJoint>> boolean newValuesEqual(C other) {
			return other instanceof GearJointChange && super.newValuesEqual(other) && Objects.equals(newRatio, ((GearJointChange) other).newRatio);
		}

		@Override
		public void reset() {
			super.reset();
			oldRatio = 0;
			newRatio = null;
		}

	}

	/** the changes of a {@link WheelJoint}
	 *  @author dermetfan
	 *  @since 0.7.1 */
	public static class WheelJointChange extends JointChange<WheelJoint> {

		private transient float oldSpringDampingRatio;
		private transient float oldSpringFrequencyHz;
		private transient float oldMaxMotorTorque;
		private transient float oldMotorSpeed;

		public Float newSpringDampingRatio;
		public Float newSpringFrequencyHz;
		public Float newMaxMotorTorque;
		public Float newMotorSpeed;

		@Override
		public boolean update(WheelJoint joint) {
			float sprintDampingRatio = joint.getSpringDampingRatio();
			float springFrequencyHz = joint.getSpringFrequencyHz();
			float maxMotorTorque = joint.getMaxMotorTorque();
			float motorSpeed = joint.getMotorSpeed();

			boolean changed = super.update(joint);

			if(sprintDampingRatio != oldSpringDampingRatio) {
				newSpringDampingRatio = oldSpringDampingRatio = sprintDampingRatio;
				changed = true;
			} else
				newSpringDampingRatio = null;
			if(springFrequencyHz != oldSpringFrequencyHz) {
				newSpringFrequencyHz = oldSpringFrequencyHz = springFrequencyHz;
				changed = true;
			} else
				newSpringFrequencyHz = null;
			if(maxMotorTorque != oldMaxMotorTorque) {
				newMaxMotorTorque = oldMaxMotorTorque = maxMotorTorque;
				changed = true;
			} else
				newMaxMotorTorque = null;
			if(motorSpeed != oldMotorSpeed) {
				newMotorSpeed = oldMotorSpeed = motorSpeed;
				changed = true;
			} else
				newMotorSpeed = null;

			return changed;
		}

		@Override
		public void apply(WheelJoint joint) {
			super.apply(joint);
			if(newSpringDampingRatio != null)
				joint.setSpringDampingRatio(newSpringDampingRatio);
			if(newSpringFrequencyHz != null)
				joint.setSpringFrequencyHz(newSpringFrequencyHz);
			if(newMaxMotorTorque != null)
				joint.setMaxMotorTorque(newMaxMotorTorque);
			if(newMotorSpeed != null)
				joint.setMotorSpeed(newMotorSpeed);
		}

		@Override
		public <C extends Change<WheelJoint>> boolean newValuesEqual(C other) {
			if(!(other instanceof WheelJointChange))
				return false;
			WheelJointChange o = (WheelJointChange) other;
			return super.newValuesEqual(other) &&
					Objects.equals(newSpringDampingRatio, o.newSpringDampingRatio) &&
					Objects.equals(newSpringFrequencyHz, o.newSpringFrequencyHz) &&
					Objects.equals(newMaxMotorTorque, o.newMaxMotorTorque) &&
					Objects.equals(newMotorSpeed, o.newMotorSpeed);
		}

		@Override
		public void reset() {
			super.reset();

			oldSpringDampingRatio = 0;
			oldSpringFrequencyHz = 0;
			oldMaxMotorTorque = 0;
			oldMotorSpeed = 0;

			newSpringDampingRatio = null;
			newSpringFrequencyHz = null;
			newMaxMotorTorque = null;
			newMotorSpeed = null;
		}

	}

	/** the changes of a {@link WeldJoint}
	 *  @author dermetfan
	 *  @since 0.7.1 */
	public static class WeldJointChange extends JointChange<WeldJoint> {

		private transient float oldDampingRatio;
		private transient float oldFrequency;

		public Float newDampingRatio;
		public Float newFrequency;

		@Override
		public boolean update(WeldJoint joint) {
			float dampingRatio = joint.getDampingRatio();
			float frequency = joint.getFrequency();

			boolean changed = super.update(joint);

			if(dampingRatio != oldDampingRatio) {
				newDampingRatio = oldDampingRatio = dampingRatio;
				changed = true;
			} else
				newDampingRatio = null;
			if(frequency != oldFrequency) {
				newFrequency = oldFrequency = frequency;
				changed = true;
			} else
				newFrequency = null;

			return changed;
		}

		@Override
		public void apply(WeldJoint joint) {
			super.apply(joint);
			if(newDampingRatio != null)
				joint.setDampingRatio(newDampingRatio);
			if(newFrequency != null)
				joint.setFrequency(newFrequency);
		}

		@Override
		public <C extends Change<WeldJoint>> boolean newValuesEqual(C other) {
			if(!(other instanceof WeldJointChange))
				return false;
			WeldJointChange o = (WeldJointChange) other;
			return super.newValuesEqual(other) &&
					Objects.equals(newDampingRatio, o.newDampingRatio) &&
					Objects.equals(newFrequency, o.newFrequency);
		}

		@Override
		public void reset() {
			super.reset();

			oldDampingRatio = 0;
			oldFrequency = 0;

			newDampingRatio = null;
			newFrequency = null;
		}

	}

	/** the changes of a {@link FrictionJoint}
	 *  @author dermetfan
	 *  @since 0.7.1 */
	public static class FrictionJointChange extends JointChange<FrictionJoint> {

		private transient float oldMaxForce;
		private transient float oldMaxTorque;

		public Float newMaxForce;
		public Float newMaxTorque;

		@Override
		public boolean update(FrictionJoint joint) {
			float maxForce = joint.getMaxForce();
			float maxTorque = joint.getMaxTorque();

			boolean changed = super.update(joint);

			if(maxForce != oldMaxForce) {
				newMaxForce = oldMaxForce = maxForce;
				changed = true;
			} else
				newMaxForce = null;
			if(maxTorque != oldMaxTorque) {
				newMaxTorque = oldMaxTorque = maxTorque;
				changed = true;
			} else
				newMaxTorque = null;

			return changed;
		}

		@Override
		public void apply(FrictionJoint joint) {
			super.apply(joint);
			if(newMaxForce != null)
				joint.setMaxForce(newMaxForce);
			if(newMaxTorque != null)
				joint.setMaxTorque(newMaxTorque);
		}

		@Override
		public <C extends Change<FrictionJoint>> boolean newValuesEqual(C other) {
			if(!(other instanceof FrictionJointChange))
				return false;
			FrictionJointChange o = (FrictionJointChange) other;
			return super.newValuesEqual(other) &&
					Objects.equals(newMaxForce, o.newMaxForce) &&
					Objects.equals(newMaxTorque, o.newMaxTorque);
		}

		@Override
		public void reset() {
			super.reset();

			oldMaxForce = 0;
			oldMaxTorque = 0;

			newMaxForce = null;
			newMaxTorque = null;
		}

	}

	/** the changes of a {@link RopeJoint}
	 *  @author dermetfan
	 *  @since 0.7.1 */
	public static class RopeJointChange extends JointChange<RopeJoint> {

		private transient float oldMaxLength;

		public Float newMaxLength;

		@Override
		public boolean update(RopeJoint joint) {
			float maxLength = joint.getMaxLength();

			boolean changed = super.update(joint);

			if(maxLength != oldMaxLength) {
				newMaxLength = oldMaxLength = maxLength;
				changed = true;
			} else
				newMaxLength = null;

			return changed;
		}

		@Override
		public void apply(RopeJoint joint) {
			super.apply(joint);
			if(newMaxLength != null)
				joint.setMaxLength(newMaxLength);
		}

		@Override
		public <C extends Change<RopeJoint>> boolean newValuesEqual(C other) {
			return other instanceof RopeJointChange && super.newValuesEqual(other) && Objects.equals(newMaxLength, ((RopeJointChange) other).newMaxLength);
		}

		@Override
		public void reset() {
			super.reset();
			oldMaxLength = 0;
			newMaxLength = null;
		}

	}

	/** the changes of a {@link MotorJoint}
	 *  @author dermetfan
	 *  @since 0.7.1 */
	public static class MotorJointChange extends JointChange<MotorJoint> {

		private transient float oldMaxForce;
		private transient float oldMaxTorque;
		private transient float oldCorrectionFactor;
		private transient float oldAngularOffset;
		private transient final Vector2 oldLinearOffset = new Vector2();

		public Float newMaxForce;
		public Float newMaxTorque;
		public Float newCorrectionFactor;
		public Float newAngularOffset;
		public Vector2 newLinearOffset;

		@Override
		public boolean update(MotorJoint joint) {
			float maxForce = joint.getMaxForce();
			float maxTorque = joint.getMaxTorque();
			float correctionFactor = joint.getCorrectionFactor();
			float angularOffset = joint.getAngularOffset();
			Vector2 linearOffset = joint.getLinearOffset();

			boolean changed = super.update(joint);

			if(maxForce != oldMaxForce) {
				newMaxForce = oldMaxForce = maxForce;
				changed = true;
			} else
				newMaxForce = null;
			if(maxTorque != oldMaxTorque) {
				newMaxTorque = oldMaxTorque = maxTorque;
				changed = true;
			} else
				newMaxTorque = null;
			if(correctionFactor != oldCorrectionFactor) {
				newCorrectionFactor = oldCorrectionFactor = correctionFactor;
				changed = true;
			} else
				newCorrectionFactor = null;
			if(angularOffset != oldAngularOffset) {
				newAngularOffset = oldAngularOffset = angularOffset;
				changed = true;
			} else
				newAngularOffset = null;
			if(!linearOffset.equals(oldLinearOffset)) {
				oldLinearOffset.set(newLinearOffset = linearOffset);
				changed = true;
			} else
				newLinearOffset = null;

			return changed;
		}

		@Override
		public void apply(MotorJoint joint) {
			super.apply(joint);
			if(newMaxForce != null)
				joint.setMaxForce(newMaxForce);
			if(newMaxTorque != null)
				joint.setMaxForce(newMaxTorque);
			if(newCorrectionFactor != null)
				joint.setCorrectionFactor(newCorrectionFactor);
			if(newAngularOffset != null)
				joint.setAngularOffset(newAngularOffset);
			if(newLinearOffset != null)
				joint.setLinearOffset(newLinearOffset);
		}

		@Override
		public <C extends Change<MotorJoint>> boolean newValuesEqual(C other) {
			if(!(other instanceof MotorJointChange))
				return false;
			MotorJointChange o = (MotorJointChange) other;
			return super.newValuesEqual(other) &&
					Objects.equals(newAngularOffset, o.newAngularOffset) &&
					Objects.equals(newCorrectionFactor, o.newCorrectionFactor) &&
					Objects.equals(newLinearOffset, o.newLinearOffset) &&
					Objects.equals(newMaxForce, o.newMaxForce);
		}

		@Override
		public void reset() {
			super.reset();

			oldMaxForce = 0;
			oldMaxTorque = 0;
			oldCorrectionFactor = 0;
			oldAngularOffset = 0;
			oldLinearOffset.setZero();

			newMaxForce = null;
			newMaxTorque = null;
			newCorrectionFactor = null;
			newAngularOffset = null;
			newLinearOffset = null;
		}

	}

}
