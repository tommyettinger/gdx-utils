package net.dermetfan.util.libgdx.box2d;

import net.dermetfan.util.Accessor;
import net.dermetfan.util.math.MathUtils;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.utils.Array;

public class Breakable implements ContactListener {

	/** how much force the breakable can bear */
	private float robustness;

	/** creates a new Breakable with the given {@link #robustness} */
	public Breakable(float robustness) {
		this.robustness = robustness;
	}

	/** The {@link Accessor} used to access user data. */
	private static Accessor userDataAccessor = new Accessor() {

		@SuppressWarnings("unchecked")
		@Override
		public Breakable access(Object userData) {
			return userData instanceof Breakable ? (Breakable) userData : null;
		}

	};

	/** the fixtures that broke in the last timestep */
	private static final Array<Fixture> brokenFixtures = new Array<Fixture>(0);

	public static void update() {
		for(Fixture fixture : brokenFixtures) {
			brokenFixtures.removeValue(fixture, true);
			fixture.getBody().destroyFixture(fixture); // TODO if body has no fixtures left, destroy it? is that desired?
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
		// TODO break bodies as well

		Fixture a = contact.getFixtureA(), b = contact.getFixtureB();
		if(userDataAccessor.access(a.getUserData()) != null && MathUtils.sum(impulse.getNormalImpulses()) > robustness && !brokenFixtures.contains(a, true))
			brokenFixtures.add(a);

		if(userDataAccessor.access(b.getUserData()) != null && MathUtils.sum(impulse.getNormalImpulses()) > robustness && !brokenFixtures.contains(b, true))
			brokenFixtures.add(b);
	}

	/** @return the {@link #robustness} */
	public float getRobustness() {
		return robustness;
	}

	/** @param robustness the {@link #robustness} to set */
	public void setRobustness(float robustness) {
		this.robustness = robustness;
	}

	/** @return the {@link #brokenFixtures} */
	public static Array<Fixture> getBrokenFixtures() {
		return brokenFixtures;
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
