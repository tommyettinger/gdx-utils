package net.dermetfan.util.libgdx.box2d;

import net.dermetfan.util.Accessor;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.utils.Array;

public class Breakable implements ContactListener {

	/** the force needed to break */
	private float force;

	public Breakable(float force) {
		this.force = force;
	}

	/** the {@link Accessor} used to access user data*/
	private static Accessor userDataAccessor = new Accessor() {

		@SuppressWarnings("unchecked")
		@Override
		public Breakable access(Object userData) {
			return userData instanceof Breakable ? (Breakable) userData : null;
		}

	};

	private static Array<Fixture> brokenFixtures = new Array<Fixture>(0);

	public static void update() {
		for(Fixture fixture : brokenFixtures) {
			brokenFixtures.removeValue(fixture, true);
			fixture.getBody().destroyFixture(fixture); // TODO if body has no fixtures left, destroy it? is that wanted?
		}
	}

	@Override
	public void beginContact(Contact contact) {
	}

	@Override
	public void endContact(Contact contact) {
	}

	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {
	}

	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {
		// TODO break bodies
		// TODO use actual impulse instead of tangent speed
		if(userDataAccessor.access(contact.getFixtureA().getUserData()) != null) {
			System.out.println("breakable in fixutre a user data");
			if(contact.getTangentSpeed() >= force)
				brokenFixtures.add(contact.getFixtureA());
		}
		if(userDataAccessor.access(contact.getFixtureB().getUserData()) != null) {
			System.out.println("breakable in fixutre b user data");
			if(contact.getTangentSpeed() >= force)
				brokenFixtures.add(contact.getFixtureB());
		}
	}

	/** @return the {@link #force} */
	public float getForce() {
		return force;
	}

	/** @param force the {@link #force} to set */
	public void setForce(float force) {
		this.force = force;
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
