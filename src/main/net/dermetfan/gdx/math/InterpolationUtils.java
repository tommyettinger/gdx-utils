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

package net.dermetfan.gdx.math;

import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import net.dermetfan.utils.StringUtils;

/** useful methods regarding {@link Interpolation Interpolations}
 *  @since 0.6.0 */
public class InterpolationUtils {

	/** Retrieves one of the default {@code Interpolation} objects by name using reflection.
	 *  This is mostly for easy specification in configuration files or similar.
	 *
	 *  The name of the interpolation is {@link StringUtils#toJavaIdentifier(String) converted} to a valid java identifier.
	 *  This means that it can be given in, for example, one of the following formats, where {@code type} is the main type
	 *  (e.g. {@link Interpolation#swing swing}) and {@code sub} is the optional sub-type (e.g. {@link Interpolation#swingIn in}),
	 *  usually either {@code in} or {@code out}.
	 *  <ul>
	 *      <li>{@code typeSub}</li>
	 *      <li>{@code type-sub}</li>
	 *      <li>{@code type_sub}</li>
	 *      <li>{@code type.sub}</li>
	 *      <li>{@code type sub}</li>
	 *  </ul>
	 *
	 *  @param name The name of the {@code Interpolation} to retrieve. Converted to a valid java identifier.
	 *  @return The {@code Interpolation} if found, {@code null} if not or if {@code name} is {@code null}. */
	public static Interpolation get(String name) {
		if(name == null)
			return null;
		try {
			Object obj = ClassReflection.getField(Interpolation.class, StringUtils.toJavaIdentifier(name)).get(null);
			if(obj instanceof Interpolation)
				return (Interpolation) obj;
		} catch(ReflectionException e) {
			throw new GdxRuntimeException("failed to get Interpolation for name \"" + name + "\"", e);
		}
		return null;
	}

}
