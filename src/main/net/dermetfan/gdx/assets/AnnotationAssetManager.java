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

package net.dermetfan.gdx.assets;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Field;
import com.badlogic.gdx.utils.reflect.ReflectionException;

/** An {@link AssetManager} that {@link AssetManager#load(AssetDescriptor) loads} assets from a container class using reflection.
 *  @author dermetfan */
public class AnnotationAssetManager extends AssetManager {

	/** Indicates whether a field should be {@link AnnotationAssetManager#load(Field) loaded} and which {@link AssetDescriptor#type} to use if necessary.
	 *  @author dermetfan */
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface Asset {

		/** @return whether this field should be loaded */
		boolean load() default true;

		/** @return the {@link AssetDescriptor#type} to use */
		Class<?> value() default void.class;

		/** @return The fully qualified or simple name of a field which value is an instance of {@link AssetLoaderParameters}. An empty String means no parameters.<br>
		 *  The AssetLoaderParameters instance is assumed to be usable with {@link #value()} and no type parameters are checked. */
		String params() default "";

	}

	/** @see AssetManager#AssetManager() */
	public AnnotationAssetManager() {
		super();
	}

	/** @see AssetManager#AssetManager(FileHandleResolver) */
	public AnnotationAssetManager(FileHandleResolver resolver) {
		super(resolver);
	}

	/** {@link #load(Field) Loads} all fields in the given {@code container} class if they are annotated with {@link Asset} and {@link Asset#load()} is true.
	 *  @param container the instance of a container class from which to load fields annotated with {@link Asset} */
	public void load(Object container) {
		for(Field field : ClassReflection.getDeclaredFields(container.getClass()))
			if(field.isAnnotationPresent(Asset.class) && field.getDeclaredAnnotation(Asset.class).getAnnotation(Asset.class).load())
				load(field, container);
	}

	/** @param container the class containing the fields whose {@link AssetDescriptor AssetDescriptors} to load */
	public void load(Class<?> container) {
		for(Field field : ClassReflection.getDeclaredFields(container))
			if(field.isAnnotationPresent(Asset.class) && field.getDeclaredAnnotation(Asset.class).getAnnotation(Asset.class).load())
				load(field);
	}

	/** {@link AssetManager#load(String, Class) loads} the given field
	 *  @param field the field to load
	 *  @param container the instance of the class containing the given field (may be null if it's static) */
	@SuppressWarnings("unchecked")
	public void load(Field field, Object container) {
		String path = getAssetPath(field, container);
		Class<?> type = getAssetType(field, container);
		@SuppressWarnings("rawtypes")
		AssetLoaderParameters params = getAssetLoaderParameters(field, container);
		if(path == null || type == null)
			Gdx.app.debug(ClassReflection.getSimpleName(getClass()), '@' + ClassReflection.getSimpleName(Asset.class) + " (" + path + ", " + type + ") " + field.getName());
		load(path, type, params);
	}

	/** @param field the static field to load
	 *  @see #load(Field, Object) */
	public void load(Field field) {
		load(field, null);
	}

	/** @param field the field to get the asset path from
	 *  @param container the instance of the class containing the given field (may be null if it's static)
	 *  @return the asset path stored by the field */
	public static String getAssetPath(Field field, Object container) {
		String path = null;
		try {
			if(!field.isAccessible())
				field.setAccessible(true);
			Object content = field.get(container);
			if(content instanceof AssetDescriptor)
				path = ((AssetDescriptor<?>) content).fileName;
			else if(content instanceof FileHandle)
				path = ((FileHandle) content).path();
			else
				path = content.toString();
		} catch(IllegalArgumentException | ReflectionException e) {
			Gdx.app.error("AnnotationAssetManager", "could not access field \"" + field.getName() + "\"", e);
		}
		return path;
	}

	/** @param container the instance of the class containing the given field (may be null if it's static)
	 *  @return the {@link Asset#value()} of the given Field */
	public static Class<?> getAssetType(Field field, Object container) {
		if(ClassReflection.isAssignableFrom(AssetDescriptor.class, field.getType()))
			try {
				if(!field.isAccessible())
					field.setAccessible(true);
				return ((AssetDescriptor<?>) field.get(container)).type;
			} catch(IllegalArgumentException | ReflectionException e) {
				Gdx.app.error("AnnotationAssetManager", "could not access field \"" + field.getName() + "\"", e);
			}
		if(field.isAnnotationPresent(Asset.class))
			return field.getDeclaredAnnotation(Asset.class).getAnnotation(Asset.class).value();
		return null;
	}

	/** @param container the instance of the class containing the given field (may be null if it's static)
	 *  @return the {@link AssetDescriptor#params AssetLoaderParameters} of the AssetDescriptor in the given field */
	@SuppressWarnings("unchecked")
	public static <T> AssetLoaderParameters<T> getAssetLoaderParameters(Field field, Object container) {
		if(field.isAnnotationPresent(Asset.class)) {
			String params = field.getDeclaredAnnotation(Asset.class).getAnnotation(Asset.class).params();
			if(params.length() > 0) {
				Field paramsField = null;
				if(params.contains(".")) { // fully qualified name
					int lastPeriod = params.lastIndexOf('.');
					String className = params.substring(0, lastPeriod), fieldName = params.substring(lastPeriod + 1);
					try {
						paramsField = ClassReflection.getDeclaredField(ClassReflection.forName(className), fieldName);
					} catch(ReflectionException e) {
						Gdx.app.error("AnnotationAssetManager", "could not access class " + className, e);
					}
				} else { // simple name of field in declaring class
					try {
						paramsField = ClassReflection.getDeclaredField(field.getDeclaringClass(), params);
					} catch(ReflectionException e) {
						Gdx.app.error("AnnotationAssetManager", "could not access field \"" + field.getName() + "\" of class " + ClassReflection.getSimpleName(field.getDeclaringClass()), e);
					}
				}
				if(paramsField != null) {
					if(ClassReflection.isAssignableFrom(AssetLoaderParameters.class, paramsField.getType()))
						try {
							if(!paramsField.isAccessible())
								paramsField.setAccessible(true);
							return (AssetLoaderParameters<T>) paramsField.get(container);
						} catch(ReflectionException e) {
							Gdx.app.error("AnnotationAssetManager", "could not access value of field \"" + paramsField.getName() + "\" of class " + ClassReflection.getSimpleName(paramsField.getDeclaringClass()) + " and instance " + container, e);
						}
					else
						Gdx.app.debug("AnnotationAssetManager", "field \"" + paramsField.getName() + "\" of class " + ClassReflection.getSimpleName(paramsField.getDeclaringClass()) + " and instance " + container + " is not assignable from AssetLoaderParameters");
				}
			}
		}
		if(ClassReflection.isAssignableFrom(AssetDescriptor.class, field.getType()))
			try {
				if(!field.isAccessible())
					field.setAccessible(true);
				return ((AssetDescriptor<T>) field.get(container)).params;
			} catch(IllegalArgumentException | ReflectionException e) {
				Gdx.app.error("AnnotationAssetManager", "could not access field\"" + field.getName() + "\"", e);
			}
		return null;
	}

	/** Creates an {@link AssetDescriptor} from a field that is annotated with {@link Asset}.
	 *  @param field the field annotated with {@link Asset} to create an {@link AssetDescriptor} from
	 *  @param container the instance of the class containing the given field (may be null if it's static)
	 *  @return an {@link AssetDescriptor} created from the given, with {@link Asset} annotated field (may be null if all fields in the class annotated with {@link Asset} are static) */
	@SuppressWarnings("unchecked")
	public <T> AssetDescriptor<T> createAssetDescriptor(Field field, Object container) {
		Class<?> fieldType = field.getType(), type = getAssetType(field, container);
		if(fieldType == AssetDescriptor.class)
			try {
				if(!field.isAccessible())
					field.setAccessible(true);
				AssetDescriptor<?> alreadyExistingDescriptor = (AssetDescriptor<?>) field.get(container);
				if(alreadyExistingDescriptor.type == type)
					return (AssetDescriptor<T>) alreadyExistingDescriptor;
				else
					return new AssetDescriptor<>(alreadyExistingDescriptor.file, (Class<T>) type, alreadyExistingDescriptor.params);
			} catch(IllegalArgumentException | ReflectionException e) {
				Gdx.app.error(ClassReflection.getSimpleName(getClass()), "couldn't access field \"" + field.getName() + "\"", e);
			}
		else
			try {
				if(!field.isAccessible())
					field.setAccessible(true);
				if(fieldType == FileHandle.class)
					return new AssetDescriptor<>((FileHandle) field.get(container), (Class<T>) type);
				else
					return new AssetDescriptor<>(field.get(container).toString(), (Class<T>) type);
			} catch(IllegalArgumentException | ReflectionException e) {
				Gdx.app.error(ClassReflection.getSimpleName(getClass()), "couldn't access field \"" + field.getName() + "\"", e);
			}
		return null;
	}

	/** creates an {@link AssetDescriptor} from a static field
	 *  @param field the field annotated with {@link Asset} to create an {@link AssetDescriptor} from (must be static)
	 *  @return the {@link AssetDescriptor} created from the given static {@code field} annotated with {@link Asset}
	 *  @see #createAssetDescriptor(Field, Object) */
	public <T> AssetDescriptor<T> createAssetDescriptor(Field field) {
		return createAssetDescriptor(field, null);
	}

}
