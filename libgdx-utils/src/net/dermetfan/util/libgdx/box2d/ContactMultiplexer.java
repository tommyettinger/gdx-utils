package net.dermetfan.util.libgdx.box2d;

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.utils.Array;

/** a {@link ContactListener} that sends {@link Contact Contacts} to an {@link Array} of ContactListeners
 *  @author dermetfan */
public class ContactMultiplexer implements ContactListener {

	/** the {@link ContactListener ContactListeners} to notify */
	private Array<ContactListener> listeners;

	public ContactMultiplexer(ContactListener... listeners) {
		this.listeners = new Array<ContactListener>(listeners);
	}

	public void add(ContactListener listener) {
		listeners.add(listener);
	}

	public void remove(ContactListener listener) {
		listeners.removeValue(listener, true);
	}

	public void add(int index, ContactListener listener) {
		listeners.insert(index, listener);
	}

	public void remove(int index) {
		listeners.removeIndex(index);
	}

	public int size() {
		return listeners.size;
	}

	public void clear() {
		listeners.clear();
	}

	@Override
	public void beginContact(Contact contact) {
		for(ContactListener listener : listeners)
			listener.beginContact(contact);
	}

	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {
		for(ContactListener listener : listeners)
			listener.preSolve(contact, oldManifold);
	}

	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {
		for(ContactListener listener : listeners)
			listener.postSolve(contact, impulse);
	}

	@Override
	public void endContact(Contact contact) {
		for(ContactListener listener : listeners)
			listener.endContact(contact);
	}

	/** @return the {@link #listeners} */
	public Array<ContactListener> getListeners() {
		return listeners;
	}

	/** @param listeners the {@link #listeners} to set */
	public void setListeners(Array<ContactListener> listeners) {
		this.listeners = listeners;
	}

}