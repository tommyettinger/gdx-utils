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

package net.dermetfan.utils;

/** utility methods operating on Objects
 *  @since 0.5.1
 *  @author dermetfan */
public abstract class ObjectUtils {

	/** @return if a equals b, assuming {@code null == null} is {@code true} */
	public static boolean nullEquals(Object a, Object b) {
		return a == null && b == null || a != null && a.equals(b);
	}

}
