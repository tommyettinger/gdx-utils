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

public abstract class Box2DUtils {

	public static long getAddr(Body body) {
		return body.addr;
	}

	public static long getAddr(Fixture fixture) {
		return fixture.addr;
	}

	public static long getAddr(Joint joint) {
		return joint.addr;
	}

	public static int hashCode(long n) {
		int c1 = 37;
		int c2 = 17;
		int result = (int) (n ^ (n >>> 32));
		return c1 * result + c2;
	}

	public static int hashCode(Body body) {
		return 31 * body.hashCode() + hashCode(getAddr(body));
	}

	public static int hashCode(Fixture fixture) {
		return 31 * fixture.hashCode() + hashCode(fixture.getBody());
	}

	public static int hashCode(Joint joint) {
		int result = joint.hashCode();
		result = 31 * result + hashCode(joint.getBodyA());
		result = 31 * result + hashCode(joint.getBodyB());
		return result;
	}

}
