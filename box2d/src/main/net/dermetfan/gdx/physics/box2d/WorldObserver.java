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
import com.badlogic.gdx.physics.box2d.MassData;
import com.badlogic.gdx.physics.box2d.Transform;
import com.badlogic.gdx.physics.box2d.World;
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
				jointChange = Pools.obtain(JointChange.class);
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
		 *  @since 0.6.1
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
	 *  @since 0.6.1
	 *  @author dermetfan */
	public static class UnexpectedListener implements Listener {

		/** the Listener to notify */
		private Listener listener;

		/** the ExpectationBases mapped to their Bodies */
		private final ObjectMap<Body, ExpectationBase> bases = new ObjectMap<>();

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
			else if(change.transform != null && !change.transform.getPosition().equals(base.transform.getPosition().mulAdd(base.linearVelocity, step))) // the linear damping of the body must be applied to the linear velocity of the base already
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
			bases.put(body, ExpectationBase.Pool.instance.obtain().set(body));
			listener.created(body);
		}

		@Override
		public void destroyed(Body body) {
			ExpectationBase.Pool.instance.free(bases.remove(body));
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
		 *  @since 0.6.1
		 *  @author dermetfan */
		private static class ExpectationBase implements Poolable {

			final Transform transform = new Transform();
			final Vector2 linearVelocity = new Vector2();
			float angularVelocity;

			public ExpectationBase set(Body body) {
				Transform bodyTransform = body.getTransform();
				transform.setPosition(bodyTransform.getPosition());
				transform.vals[Transform.SIN] = bodyTransform.vals[Transform.SIN];
				transform.vals[Transform.COS] = bodyTransform.vals[Transform.COS];
				linearVelocity.set(body.getLinearVelocity());
				angularVelocity = body.getAngularVelocity();
				return this;
			}

			@Override
			public void reset() {
				transform.setPosition(linearVelocity.setZero());
				transform.setRotation(0);
				angularVelocity = 0;
			}

			/** A Pool for ExpectationBases. Singleton.
			 *  @since 0.6.1
			 *  @author dermetfan */
			private static class Pool extends com.badlogic.gdx.utils.Pool<ExpectationBase> {

				public static final Pool instance = new Pool();

				private Pool() {
					super(8, 50);
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

			if(newAutoClearForces != oldAutoClearForces) {
				oldAutoClearForces = autoClearForces = newAutoClearForces;
				changed = true;
			}
			if(!newGravity.equals(oldGravity)) {
				oldGravity.set(gravity = newGravity);
				changed = true;
			}

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
		private transient Float oldAngularDamping;
		private transient Float oldAngularVelocity;
		private transient Float oldGravityScale;
		private transient final Vector2 oldLinearVelocity = new Vector2();
		private transient final MassData oldMassData = new MassData();
		private transient Object oldUserData;

		Transform transform;
		BodyType type;
		Float angularDamping;
		Float angularVelocity;
		Float gravityScale;
		Vector2 linearVelocity;
		MassData massData;
		Object userData;

		/** if the {@link Body#userData} changed */
		private boolean userDataChanged;

		private void updateOldTransform(Transform transform) {
			oldTransform.setPosition(transform.getPosition());
			oldTransform.setRotation(transform.getRotation());
		}

		private void updateOldLinearVelocity(Vector2 linearVelocity) {
			oldLinearVelocity.set(linearVelocity);
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
			Float newAngularDamping = body.getAngularDamping();
			Float newAngularVelocity = body.getAngularVelocity();
			Float newGravityScale = body.getGravityScale();
			Vector2 newLinearVelocity = body.getLinearVelocity();
			MassData newMassData = body.getMassData();
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
			if(!newAngularDamping.equals(oldAngularDamping)) {
				oldAngularDamping = angularDamping = newAngularDamping;
				changed = true;
			} else
				angularDamping = null;
			if(!newAngularVelocity.equals(oldAngularVelocity)) {
				oldAngularVelocity = angularVelocity = newAngularVelocity;
				changed = true;
			} else
				angularVelocity = null;
			if(!newGravityScale.equals(oldGravityScale)) {
				oldGravityScale = gravityScale = newGravityScale;
				changed = true;
			} else
				gravityScale = null;
			if(!newLinearVelocity.equals(oldLinearVelocity)) {
				updateOldLinearVelocity(linearVelocity = newLinearVelocity);
				changed = true;
			} else
				linearVelocity = null;
			if(!Box2DUtils.equals(newMassData, oldMassData)) {
				updateOldMassData(massData = newMassData);
				changed = true;
			} else
				massData = null;
			if(newUserData != null ? !newUserData.equals(oldUserData) : oldUserData != null) {
				oldUserData = userData = newUserData;
				changed = userDataChanged = true;
			} else
				userDataChanged = false;

			return changed;
		}

		@Override
		public void apply(Body body) {
			if(transform != null)
				body.setTransform(transform.getPosition(), transform.getRotation());
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
			if(userDataChanged)
				body.setUserData(userData);
		}

		@Override
		public <C extends Change<Body>> boolean newValuesEqual(C other) {
			if(!(other instanceof BodyChange))
				return false;
			BodyChange o = (BodyChange) other;
			boolean diff = !Objects.equals(transform, o.transform);
			diff |= !Objects.equals(type, o.type);
			diff |= !Objects.equals(angularDamping, o.angularDamping);
			diff |= !Objects.equals(angularVelocity, o.angularVelocity);
			diff |= !Objects.equals(gravityScale, o.gravityScale);
			diff |= !Objects.equals(linearVelocity, o.linearVelocity);
			diff |= !Objects.equals(massData, o.massData);
			diff |= !Objects.equals(userData, o.userData);
			return diff;
		}

		@Override
		public void reset() {
			oldTransform.setPosition(Vector2.Zero);
			oldTransform.setRotation(0);
			oldType = null;
			oldAngularDamping = null;
			oldAngularVelocity = null;
			oldGravityScale = null;
			oldLinearVelocity.setZero();
			oldMassData.mass = 0;
			oldMassData.I = 0;
			oldMassData.center.setZero();
			oldUserData = null;

			transform = null;
			type = null;
			angularDamping = null;
			angularVelocity = null;
			gravityScale = null;
			linearVelocity = null;
			massData = null;
			userData = null;

			userDataChanged = false;
		}

	}

	/** the changes of a {@link Fixture} */
	public static class FixtureChange implements Change<Fixture> {

		private transient Body oldBody;
		private transient boolean destroyed;

		private transient Float oldDensity;
		private transient Float oldFriction;
		private transient Float oldRestitution;
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

			Float newDensity = fixture.getDensity();
			Float newFriction = fixture.getFriction();
			Float newRestitution = fixture.getRestitution();
			Filter newFilter = fixture.getFilterData();
			Object newUserData = fixture.getUserData();

			boolean changed = false;

			if(!newDensity.equals(oldDensity)) {
				oldDensity = density = newDensity;
				changed = true;
			} else
				density = null;
			if(!newFriction.equals(oldFriction)) {
				oldFriction = friction = newFriction;
				changed = true;
			} else
				friction = null;
			if(!newRestitution.equals(oldRestitution)) {
				oldRestitution = restitution = newRestitution;
				changed = true;
			} else
				restitution = null;
			if(!newFilter.equals(oldFilter)) {
				updateOldFilter(filter = newFilter);
				changed = true;
			} else
				filter = null;
			if(newUserData != null ? !newUserData.equals(oldUserData) : oldUserData != null) {
				oldUserData = userData = newUserData;
				changed = userDataChanged = true;
			} else
				userDataChanged = false;

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
			boolean diff = !Objects.equals(density, o.density);
			diff |= !Objects.equals(friction, o.friction);
			diff |= !Objects.equals(restitution, o.restitution);
			diff |= !Objects.equals(filter, o.filter);
			diff |= !Objects.equals(userData, o.userData);
			return diff;
		}

		@Override
		public void reset() {
			oldBody = null;
			destroyed = false;

			oldDensity = null;
			oldFriction = null;
			oldRestitution = null;
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
	public static class JointChange implements Change<Joint> { // TODO implement

		@Override
		public boolean update(Joint obj) {
			return false;
		}

		@Override
		public void apply(Joint obj) {}

		@Override
		public void reset() {}

		@Override
		public <C extends Change<Joint>> boolean newValuesEqual(C other) {
			return true;
		}

	}

}
