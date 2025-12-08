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

import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.utils.Array;
import net.dermetfan.gdx.Multiplexer;

/** a {@link ContactListener} that sends {@link Contact Contacts} to an {@link Array} of ContactListeners
 *  @author dermetfan */
public class ContactMultiplexer extends Multiplexer<ContactListener> implements ContactListener {

	public ContactMultiplexer(ContactListener... receivers) {
		super(receivers);
	}

	public ContactMultiplexer(Array<ContactListener> receivers) {
		super(receivers);
	}

	@Override
	public void beginContact(Contact contact) {
		for(ContactListener listener : receivers)
			listener.beginContact(contact);
	}

	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {
		for(ContactListener listener : receivers)
			listener.preSolve(contact, oldManifold);
	}

	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {
		for(ContactListener listener : receivers)
			listener.postSolve(contact, impulse);
	}

	@Override
	public void endContact(Contact contact) {
		for(ContactListener listener : receivers)
			listener.endContact(contact);
	}

}
