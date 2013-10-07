/**
 * Copyright 2013 Robin Stumm (serverkorken@googlemail.com, http://dermetfan.bplaced.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dermetfan.util.libgdx.box2d;

import net.dermetfan.util.Accessor;
import net.dermetfan.util.math.MathUtils;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.utils.Array;

/** Breaks bodies or fixtures if they get hit too hard. Put in fixture's or body's user data and set {@link Notifier} as {@link ContactListener}.<br/>
 *  Don't forget to call {@link #destroy()} after every time step.<br/>
 *  You can manually destroy fixtures or bodies using the {@link #destroy(Fixture, boolean, boolean)} and {@link #destroy(Body)} methods.
 *  @author dermetfan */
public class Breakable {

	/** calls {@link Breakable#strain(Contact, ContactImpulse)} in {@link ContactListener#postSolve(Contact, ContactImpulse) postSolve} */
	public static class Notifier implements ContactListener {

		@Override
		public void beginContact(Contact contact) {
		}

		@Override
		public void preSolve(Contact contact, Manifold oldManifold) {
		}

		@Override
		public void postSolve(Contact contact, ContactImpulse impulse) {
			strain(contact, impulse);
		}

		@Override
		public void endContact(Contact contact) {
		}

	}

	/** how much force the Breakable can bear */
	private float resistance;

	/** if the fixture's body (in case the Breakable is used for a fixture) should be destroyed if the fixture is destroyed (false by default) */
	private boolean breakBody;

	/** if the fixture's body (in case the Breakable is used for a fixture) should be destroyed if the fixture is destroyed and it was the bodie's last one (true by default) */
	private boolean breakBodyWithoutFixtures = true;

	/** the fixtures that broke in {@link #strain(Contact, ContactImpulse)} */
	private static final Array<Fixture> brokenFixtures = new Array<Fixture>(0);

	/** the bodies that broke in {@link #strain(Contact, ContactImpulse)} */
	private static final Array<Body> brokenBodies = new Array<Body>(0);

	/** the {@link Accessor} used to access user data */
	private static Accessor userDataAccessor = new Accessor() {

		@SuppressWarnings("unchecked")
		@Override
		public Breakable access(Object userData) {
			return userData instanceof Breakable ? (Breakable) userData : null;
		}

	};

	/** creates a new Breakable with the given {@link #resistance} */
	public Breakable(float robustness) {
		resistance = robustness;
	}

	/** destroys all bodies in {@link #brokenBodies} and fixtures in {@link #brokenFixtures} */
	public static void destroy() {
		for(Fixture fixture : brokenFixtures) {
			brokenFixtures.removeValue(fixture, true);
			fixture.getBody().destroyFixture(fixture);
		}
		for(Body body : brokenBodies) {
			brokenBodies.removeValue(body, true);
			body.getWorld().destroyBody(body);
		}
	}

	/** {@link #destroy(Fixture, boolean, boolean) destroys}/{@link #destroy(Body) destroys} all fixtures/bodies involved in the given Contact if they could not bear the given impulse */
	public static void strain(Contact contact, ContactImpulse impulse) {
		Breakable breakable;
		float impulseSum = MathUtils.sum(impulse.getNormalImpulses());

		Fixture fixtureA = contact.getFixtureA(), fixtureB = contact.getFixtureB();
		if((breakable = userDataAccessor.access(fixtureA.getUserData())) != null && impulseSum > breakable.resistance)
			destroy(fixtureA, breakable.breakBodyWithoutFixtures, breakable.breakBody);

		if((breakable = userDataAccessor.access(fixtureB.getUserData())) != null && impulseSum > breakable.resistance)
			destroy(fixtureB, breakable.breakBodyWithoutFixtures, breakable.breakBody);

		Body bodyA = fixtureA.getBody(), bodyB = fixtureB.getBody();
		if((breakable = userDataAccessor.access(bodyA.getUserData())) != null && impulseSum > breakable.resistance)
			destroy(bodyA);

		if((breakable = userDataAccessor.access(bodyB.getUserData())) != null && impulseSum > breakable.resistance)
			destroy(bodyB);
	}

	/** destroys the given fixture (and its body depending on breakBodyWithoutFixtures and breakBody)
	 *  @param fixture the {@link Fixture} to destroy
	 *  @param breakBodyWithoutFixtures {@link #breakBodyWithoutFixtures}
	 *  @param breakBody {@link #breakBody} */
	public static void destroy(Fixture fixture, boolean breakBodyWithoutFixtures, boolean breakBody) {
		if(!brokenFixtures.contains(fixture, true))
			brokenFixtures.add(fixture);

		Body body = fixture.getBody();
		if(brokenBodies.contains(body, true))
			return;

		Array<Fixture> bodyFixtureList = body.getFixtureList();

		int brokenFixturesOfBody = 1;
		for(Fixture fixt : brokenFixtures)
			if(bodyFixtureList.contains(fixt, true))
				brokenFixturesOfBody++;

		if(breakBodyWithoutFixtures && bodyFixtureList.size <= brokenFixturesOfBody || breakBody)
			destroy(body);
	}

	/** @param body the {@link Body} to destroy */
	public static void destroy(Body body) {
		if(!brokenBodies.contains(body, true))
			brokenBodies.add(body);
	}

	/** @return the {@link #resistance} */
	public float getResistance() {
		return resistance;
	}

	/** @param resistance the {@link #resistance} to set */
	public void setResistance(float resistance) {
		this.resistance = resistance;
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

	/** @return the {@link #brokenFixtures} */
	public static Array<Fixture> getBrokenFixtures() {
		return brokenFixtures;
	}

	/** @return the {@link #brokenBodies} */
	public static Array<Body> getBrokenBodies() {
		return brokenBodies;
	}

	/** @return the {@link #userDataAccessor} */
	public static Accessor getUserDataAccessor() {
		return userDataAccessor;
	}

	/** @param userDataAccessor the {@link #userDataAccessor} to set */
	public static void setUserDataAccessor(Accessor userDataAccessor) {
		Breakable.userDataAccessor = userDataAccessor;
	}

}
