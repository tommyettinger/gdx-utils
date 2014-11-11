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

/** GWT emulation for Box2DUtils
 *  @author dermetfan
 *  @since 0.8.0 */
public class Box2DUtils {

	/** @return {@code 0} */
	public static long getAddr(Body body) {
		return 0;
	}

	/** @return {@code 0} */
	public static long getAddr(Fixture fixture) {
		return 0;
	}

	/** @return {@code 0} */
	public static long getAddr(Joint joint) {
		return 0;
	}

	public static int hashCode(long n) {
		return 37 * (int) (n ^ n >>> 32) + 17;
	}

	public static int hashCode(Body body) {
		return 31 * body.hashCode() + hashCode(getAddr(body));
	}

	public static int hashCode(Fixture fixture) {
		int result = 31 * fixture.hashCode() + hashCode(getAddr(fixture));
		result = 31 * result + hashCode(fixture.getBody());
		return result;
	}

	public static int hashCode(Joint joint) {
		int result = 31 * joint.hashCode() + hashCode(getAddr(joint));
		result = 31 * result + hashCode(joint.getBodyA());
		result = 31 * result + hashCode(joint.getBodyB());
		return result;
	}

}
