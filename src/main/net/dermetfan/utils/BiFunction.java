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

/** A basic rip-off of {@link java.util.function.BiFunction} from Java 8.
 *  @param <R> the return type
 *  @param <A1> the first argument type
 *  @param <A2> the second argument type
 *  @author dermetfan */
public interface BiFunction<R, A1, A2> {

	/** @param arg1 the first argument
	 *  @param arg2 the second argument
	 *  @return the return value */
	public R apply(A1 arg1, A2 arg2);

}
