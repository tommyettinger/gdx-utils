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

package com.badlogic.gdx.physics.box2d;

/** placed in this package to have access to the package-private {@code addr} fields
 *  @since 0.6.0
 *  @author dermetfan */
public abstract class Box2DUtils {

	/** @return {@link Body#addr} */
	public static long getAddr(Body body) {
		return body.addr;
	}

	/** @return {@link Fixture#addr} */
	public static long getAddr(Fixture fixture) {
		return fixture.addr;
	}

	/** @return {@link Joint#addr} */
	public static long getAddr(Joint joint) {
		return joint.addr;
	}

	/** @return a hash code of the given long */
	public static int hashCode(long n) {
		int c1 = 37;
		int c2 = 17;
		int result = (int) (n ^ (n >>> 32));
		return c1 * result + c2;
	}

	/** @return a hash code of the given body, constructed from the body's hash code and the hash code of its {@link #getAddr(Body) address} */
	public static int hashCode(Body body) {
		return 31 * body.hashCode() + hashCode(getAddr(body));
	}

	/** @return a hash code of the given fixture, constructed from the fixture's hash code and the hash code of its {@link #getAddr(Fixture) address} */
	public static int hashCode(Fixture fixture) {
		return 31 * fixture.hashCode() + hashCode(fixture.getBody());
	}

	/** @return a hash code of the given joint, constructed from the joint's hash code and the hash code of its {@link #getAddr(Joint) address} */
	public static int hashCode(Joint joint) {
		int result = joint.hashCode();
		result = 31 * result + hashCode(joint.getBodyA());
		result = 31 * result + hashCode(joint.getBodyB());
		return result;
	}

}
