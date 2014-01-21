/** Copyright 2013 Robin Stumm (serverkorken@googlemail.com, http://dermetfan.net/)
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

package net.dermetfan.utils.libgdx;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;

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
		Class<?> type() default void.class;

	}

	/** {@link #load(Field) Loads} all fields in the given {@code container} class if they are annotated with {@link Asset} and {@link Asset#load()} is true.
	 *  @param container the class containing the fields whose {@link AssetDescriptor AssetDescriptors} to load
	 *  @param instance the instance of the class containing the given {@code field} (may be null if all fields in the class annotated with {@link Asset} are static) */
	public <T> void load(Class<? extends T> container, T instance) {
		for(Field field : container.getFields())
			if(field.isAnnotationPresent(Asset.class) && field.getAnnotation(Asset.class).load())
				load(field, instance);
	}

	/** @param instance the instance of a container class from which to load fields annotated with {@link Asset}
	 *  @see #load(Class, Object) */
	public void load(Object instance) {
		load(instance.getClass(), instance);
	}

	/** @param container the class with the fields annotated with {@link Asset} (must all be static, use {@link #load(Class, Object)} otherwise)
	 *  @see #load(Class, Object) */
	public void load(Class<?> container) {
		load(container, null);
	}

	/** {@link AssetManager#load(String, Class) loads} the given field
	 *  @param field the field to load
	 *  @param instance the instance of the class containing the given field (may be null if it's static) */
	@SuppressWarnings("unchecked")
	public void load(Field field, Object instance) {
		String path = getAssetPath(field, instance);
		Class<?> type = getAssetType(field, instance);
		@SuppressWarnings("rawtypes")
		AssetLoaderParameters params = getAssetLoaderParameters(field, instance);
		if(path != null && type != null)
			if(params == null)
				load(path, type);
			else
				load(path, type, params);
	}

	/** @param field the static field to load
	 *  @see #load(Field, Object) */
	public void load(Field field) {
		load(field, null);
	}

	/** @param field the field to get the asset path from
	 *  @param instance an instance of the class containing the given field
	 *  @return the asset path stored by the field */
	public static String getAssetPath(Field field, Object instance) {
		String path = null;
		try {
			Object content = field.get(instance);
			if(content instanceof AssetDescriptor)
				path = ((AssetDescriptor<?>) content).fileName;
			else if(content instanceof String)
				path = (String) content;
			else if(content instanceof FileHandle)
				path = ((FileHandle) content).path();
		} catch(Exception e) { // IllegalArgumentException | IllegalAccessException
			Gdx.app.error(AnnotationAssetManager.class.getSimpleName(), "could not access field \"" + field.getName() + "\"", e);
		}
		return path;
	}

	/** @return the {@link Asset#type()} of the given Field */
	public static Class<?> getAssetType(Field field, Object instance) {
		if(AssetDescriptor.class.isAssignableFrom(field.getType()))
			try {
				return ((AssetDescriptor<?>) field.get(instance)).type;
			} catch(Exception e) { // IllegalArgumentException | IllegalAccessException
				Gdx.app.error(AnnotationAssetManager.class.getSimpleName(), "could not access field \"" + field.getName() + "\"", e);
			}
		if(field.isAnnotationPresent(Asset.class))
			return field.getAnnotation(Asset.class).type();
		return null;
	}

	/** @return the {@link AssetDescriptor#params AssetLoaderParameters} of the AssetDescriptor in the given field */
	@SuppressWarnings("unchecked")
	public static <T> AssetLoaderParameters<T> getAssetLoaderParameters(Field field, Object instance) {
		if(AssetDescriptor.class.isAssignableFrom(field.getType()))
			try {
				return ((AssetDescriptor<T>) field.get(instance)).params;
			} catch(Exception e) { // IllegalArgumentException | IllegalAccessException
				Gdx.app.error(AnnotationAssetManager.class.getSimpleName(), "could not access field\"" + field.getName() + "\"", e);
			}
		return null;
	}

	/** Creates an {@link AssetDescriptor} from a field that is annotated with {@link Asset}. The field's type must be {@code String} or {@link FileHandle} and the {@link Asset#type()} must not be primitive.
	 *  @param field the field annotated with {@link Asset} to create an {@link AssetDescriptor} from
	 *  @param instance the instance of the class containing the given {@code field}
	 *  @return an {@link AssetDescriptor} created from the given, with {@link Asset} annotated field (may be null if all fields in the class annotated with {@link Asset} are static) */
	@SuppressWarnings("unchecked")
	public <T> AssetDescriptor<T> createAssetDescriptor(Field field, Object instance) {
		if(!field.isAnnotationPresent(Asset.class))
			return null;
		Class<?> fieldType = field.getType();
		if(fieldType != String.class && fieldType != FileHandle.class && fieldType != AssetDescriptor.class) {
			Gdx.app.error(AnnotationAssetManager.class.getSimpleName(), "type of @" + Asset.class.getSimpleName() + " field \"" + field.getName() + "\" must be " + String.class.getSimpleName() + " or " + FileHandle.class.getSimpleName() + " to create an " + AssetDescriptor.class.getSimpleName() + " from it");
			return null;
		}
		Class<?> type = getAssetType(field, instance);
		if(type.isPrimitive()) {
			Gdx.app.error(AnnotationAssetManager.class.getSimpleName(), "cannot create an " + AssetDescriptor.class.getSimpleName() + " of the generic type " + type.getSimpleName() + " from the @" + Asset.class.getSimpleName() + " field \"" + field.getName() + "\"");
			return null;
		}
		if(fieldType == AssetDescriptor.class)
			try {
				AssetDescriptor<?> alreadyExistingDescriptor = (AssetDescriptor<?>) field.get(instance);
				if(alreadyExistingDescriptor.type == type)
					return (AssetDescriptor<T>) alreadyExistingDescriptor;
				else
					return new AssetDescriptor<T>(alreadyExistingDescriptor.file, (Class<T>) type);
			} catch(Exception e) { // IllegalArgumentException | IllegalAccessException
				Gdx.app.error(AnnotationAssetManager.class.getSimpleName(), "couldn't access field \"" + field.getName() + "\"", e);
			}
		else
			try {
				if(fieldType == String.class)
					return new AssetDescriptor<T>((String) field.get(instance), (Class<T>) type);
				else
					return new AssetDescriptor<T>((FileHandle) field.get(instance), (Class<T>) type);
			} catch(Exception e) { // IllegalArgumentException | IllegalAccessException
				Gdx.app.error(AnnotationAssetManager.class.getSimpleName(), "couldn't access field \"" + field.getName() + "\"", e);
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