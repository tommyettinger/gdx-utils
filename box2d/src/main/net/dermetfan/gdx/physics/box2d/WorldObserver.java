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

public class WorldObserver {

	public static interface Change<T> extends Poolable {

		boolean update(T obj);

		void apply(T obj);

	}

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
		public void reset() {
			oldAutoClearForces = null;
			oldGravity.setZero();

			autoClearForces = null;
			gravity = null;
		}

	}

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

		private boolean userDataChanged;

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

			if(!newTransform.equals(oldTransform)) {
				updateOldTransform(transform = newTransform);
				changed = true;
			}
			if(!newType.equals(oldType)) {
				oldType = type = newType;
				changed = true;
			}
			if(!newAngularDamping.equals(oldAngularDamping)) {
				oldAngularDamping = angularDamping = newAngularDamping;
				changed = true;
			}
			if(!newAngularVelocity.equals(oldAngularVelocity)) {
				oldAngularVelocity = angularVelocity = newAngularVelocity;
				changed = true;
			}
			if(!newGravityScale.equals(oldGravityScale)) {
				oldGravityScale = gravityScale = newGravityScale;
				changed = true;
			}
			if(!newLinearVelocity.equals(oldLinearVelocity)) {
				updateOldLinearVelocity(linearVelocity = newLinearVelocity);
				changed = true;
			}
			if(!newMassData.equals(oldMassData)) {
				updateOldMassData(massData = newMassData);
				changed = true;
			}
			if(newUserData != null ? !newUserData.equals(oldUserData) : oldUserData != null) {
				oldUserData = userData = newUserData;
				changed = userDataChanged = true;
			}

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

		boolean userDataChanged;

		void created(Body body) {
			oldBody = body;
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
			}
			if(!newFriction.equals(oldFriction)) {
				oldFriction = friction = newFriction;
				changed = true;
			}
			if(!newRestitution.equals(oldRestitution)) {
				oldRestitution = restitution = newRestitution;
				changed = true;
			}
			if(!newFilter.equals(oldFilter)) {
				updateOldFilter(filter = newFilter);
				changed = true;
			}
			if(newUserData != null ? !newUserData.equals(oldUserData) : oldUserData != null) {
				oldUserData = userData = newUserData;
				changed = userDataChanged = true;
			}

			return changed;
		}

		private void updateOldFilter(Filter newFilter) {
			oldFilter.categoryBits = newFilter.categoryBits;
			oldFilter.groupIndex = newFilter.groupIndex;
			oldFilter.maskBits = newFilter.maskBits;
		}

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

		public boolean isDestroyed() {
			return destroyed;
		}

	}

	public static class JointChange implements Change<Joint> {

		@Override
		public boolean update(Joint obj) {
			return false;
		}

		@Override
		public void apply(Joint obj) {

		}

		@Override
		public void reset() {

		}

	}

	public static interface Listener {

		void setOn(WorldObserver watcher);

		void removedFrom(WorldObserver watcher);

		void changed(World world, WorldChange change);

		void changed(Body body, BodyChange change);

		void created(Body body);

		void destroyed(Body body);

		void changed(Fixture fixture, FixtureChange change);

		void created(Fixture fixture);

		void destroyed(Fixture fixture);

		void changed(Joint joint, JointChange change);

		void created(Joint joint);

		void destroyed(Joint joint);

	}

	private Listener listener;

	private final WorldChange worldChange = new WorldChange();
	private final IntMap<BodyChange> bodyChanges = new IntMap<>();
	private final IntMap<FixtureChange> fixtureChanges = new IntMap<>();
	private final ObjectMap<Joint, JointChange> jointChanges = new ObjectMap<>();

	private final Array<Body> tmpBodies = new Array<>();
	private final IntMap<Body> currentBodies = new IntMap<>(), previousBodies = new IntMap<>();
	private final IntMap<Fixture> currentFixtures = new IntMap<>(), previousFixtures = new IntMap<>();
	private final Array<Joint> currentJoints = new Array<>(), previousJoints = new Array<>();

	public WorldObserver() {}

	public WorldObserver(Listener listener) {
		setListener(listener);
	}

	public void update(World world) {
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
	}

	public BodyChange getBodyChange(int hash) {
		return bodyChanges.get(hash);
	}

	public FixtureChange getFixtureChange(int hash) {
		return fixtureChanges.get(hash);
	}

	public JointChange getJointChange(Joint joint) {
		return jointChanges.get(joint);
	}

	// getters and setters

	public WorldChange getWorldChange() {
		return worldChange;
	}

	public Listener getListener() {
		return listener;
	}

	public void setListener(Listener listener) {
		if(this.listener != null)
			this.listener.removedFrom(this);
		this.listener = listener;
		if(listener != null)
			listener.setOn(this);
	}

}
