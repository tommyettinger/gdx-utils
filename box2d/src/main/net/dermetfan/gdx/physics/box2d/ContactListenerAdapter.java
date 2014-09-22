package net.dermetfan.gdx.physics.box2d;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;

/** Override this if you only want to use a subset of methods of the {@link ContactListener}.
 *  @author dermetfan
 *  @since 0.3.5 */
public class ContactListenerAdapter implements ContactListener {

	@Override
	public void beginContact(Contact contact) {}

	@Override
	public void endContact(Contact contact) {}

	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {}

	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {}

}
