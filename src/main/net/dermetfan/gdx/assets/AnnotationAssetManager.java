/** Copyright 2015 Robin Stumm (serverkorken@gmail.com, http://dermetfan.net)
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
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.Field;
import com.badlogic.gdx.utils.reflect.Method;
import com.badlogic.gdx.utils.reflect.ReflectionException;

/** an AssetManager that loads assets from annotated fields and methods using reflection
 *  @author dermetfan */
public class AnnotationAssetManager extends AssetManager {

	private static Object get(Field field, Object container) {
		if(container == null && !field.isStatic())
			throw new IllegalArgumentException("field is not static but container instance is null: " + field.getName());
		boolean accessible = field.isAccessible();
		if(!accessible)
			field.setAccessible(true);
		Object obj = null;
		try {
			obj = field.get(container);
		} catch(ReflectionException e) {
			Gdx.app.error("AnnotationAssetManager", "could not access " + field, e);
		}
		if(!accessible)
			field.setAccessible(false);
		return obj;
	}

	private static Object get(Method method, Object container, Object... parameters) {
		if(container == null && !method.isStatic())
			throw new IllegalArgumentException("method is not static but container instance is null: " + method.getName());
		boolean accessible = method.isAccessible();
		if(!accessible)
			method.setAccessible(true);
		Object obj = null;
		try {
			obj = method.invoke(container, parameters);
		} catch(ReflectionException e) {
			Gdx.app.error("AnnotationAssetManager", "could not invoke " + method, e);
		}
		if(!accessible)
			method.setAccessible(false);
		return obj;
	}

	private static String getAssetPath(Object pathObj) {
		if(pathObj instanceof FileHandle)
			return ((FileHandle) pathObj).path();
		if(pathObj instanceof AssetDescriptor)
			return ((AssetDescriptor<?>) pathObj).fileName;
		return pathObj.toString();
	}

	private static Class getAssetType(Asset asset, Object pathObj) {
		if(pathObj instanceof AssetDescriptor)
			return ((AssetDescriptor<?>) pathObj).type;
		return asset.value();
	}

	private static AssetLoaderParameters getAssetLoaderParameters(Asset asset, Object pathObj, Class containerType, Object container) {
		if(pathObj instanceof AssetDescriptor)
			return ((AssetDescriptor) pathObj).params;

		Class<?> clazz; // class of the field or method containing the AssetLoaderParameters
		boolean method; // if a method contains the AssetLoaderParameters
		String name; // the name of the field or method inside clazz
		{
			String location = asset.params();
			if(location.contains(".")) { // fully qualified path
				int end = location.lastIndexOf('#');
				if(end == -1) {
					method = false;
					end = location.lastIndexOf('.');
				} else
					method = true;
				String className = location.substring(0, end);
				name = location.substring(end + 1);
				try {
					clazz = Class.forName(className);
				} catch(ClassNotFoundException e) {
					throw new IllegalArgumentException("Failed to load AssetLoaderParameters from " + location + ": class " + className + " does not exist");
				}
			} else { // in container class
				clazz = containerType;
				method = location.contains("#");
				name = method ? location.substring(1) : location;
			}
		}

		if(method) {
			Method m;
			boolean withParams;
			try {
				m = ClassReflection.getDeclaredMethod(clazz, name, Class.class, String.class, Object.class);
				withParams = true;
			} catch(ReflectionException e) {
				try {
					m = ClassReflection.getDeclaredMethod(clazz, name);
					withParams = false;
				} catch(ReflectionException e1) {
					throw new GdxRuntimeException("failed to access method " + name, e1);
				}
			}
			if(!ClassReflection.isAssignableFrom(AssetLoaderParameters.class, m.getReturnType()))
				throw new IllegalArgumentException("AssetLoaderParameters supplier method does not return AssetLoaderParameters: " + m.getReturnType());
			if(withParams)
				return (AssetLoaderParameters) get(m, container, getAssetType(asset, pathObj), getAssetPath(pathObj), pathObj);
			return (AssetLoaderParameters) get(m, container);
		} else {
			try {
				Field f = ClassReflection.getDeclaredField(clazz, name);
				return (AssetLoaderParameters) get(f, container);
			} catch(ReflectionException e) {
				throw new GdxRuntimeException("failed to access field " + name, e);
			}
		}
	}

	public static String getAssetPath(Field field, Object container) {
		return getAssetPath(get(field, container));
	}

	public static String getAssetPath(Field field) {
		return getAssetPath(field, null);
	}

	public static String getAssetPath(Method method, Object container) {
		return getAssetPath(get(method, container));
	}

	public static String getAssetPath(Method method) {
		return getAssetPath(method, null);
	}

	public static Class getAssetType(Field field, Object container) {
		return getAssetType(field.isAnnotationPresent(Asset.class) ? field.getDeclaredAnnotation(Asset.class).getAnnotation(Asset.class) : null, get(field, container));
	}

	public static Class getAssetType(Field field) {
		return getAssetType(field, null);
	}

	public static Class getAssetType(Method method, Object container) {
		throw new UnsupportedOperationException("com.badlogic.gdx.reflect.Method does not provide access to annotations");
		// TODO com.badlogic.gdx.reflect.Method does not provide access to annotations
	}

	public static Class getAssetType(Method method) {
		return getAssetType(method, null);
	}

	public static AssetLoaderParameters getAssetLoaderParameters(Field field, Object container) {
		return getAssetLoaderParameters(field.isAnnotationPresent(Asset.class) ? field.getDeclaredAnnotation(Asset.class).getAnnotation(Asset.class) : null, get(field, container), field.getDeclaringClass(), container);
	}

	public static AssetLoaderParameters getAssetLoaderParameters(Field field) {
		return getAssetLoaderParameters(field, null);
	}

	public static AssetLoaderParameters getAssetLoaderParameters(Method method, Object container) {
		throw new UnsupportedOperationException("com.badlogic.gdx.reflect.Method does not provide access to annotations");
		// TODO com.badlogic.gdx.reflect.Method does not provide access to annotations
	}

	public static AssetLoaderParameters getAssetLoaderParameters(Method method) {
		return getAssetLoaderParameters(method, null);
	}

	public static <T> AssetDescriptor<T> createAssetDescriptor(Field field, Object container) {
		Object obj = get(field, container);
		if(obj instanceof AssetDescriptor)
			return (AssetDescriptor<T>) obj;
		if(!field.isAnnotationPresent(Asset.class))
			return null;
		Asset asset = field.getDeclaredAnnotation(Asset.class).getAnnotation(Asset.class);
		Object pathObj = get(field, container);
		return new AssetDescriptor<>(getAssetPath(pathObj), getAssetType(asset, pathObj), getAssetLoaderParameters(asset, pathObj, field.getDeclaringClass(), container));
	}

	public static <T> AssetDescriptor<T> createAssetDescriptor(Field field) {
		return createAssetDescriptor(field, null);
	}

	public static <T> AssetDescriptor<T> createAssetDescriptor(Method method, Object container) {
		Object obj = get(method, container);
		if(obj instanceof AssetDescriptor)
			return (AssetDescriptor<T>) obj;
		throw new UnsupportedOperationException("com.badlogic.gdx.reflect.Method does not provide access to annotations");
		// TODO com.badlogic.gdx.reflect.Method does not provide access to annotations
	}

	public static <T> AssetDescriptor<T> createAssetDescriptor(Method method) {
		return createAssetDescriptor(method, null);
	}

	/** @param asset The @Asset annotation annotating the field or method obj was extracted from. May be null if obj is an AssetDescriptor.
	 *  @param pathObj the Object describing the asset path, extracted from a field or method
	 *  @param containerType The class containing the field or method obj was extracted from. May be null if obj is an AssetDescriptor or no AssetLoaderParameters are specified by the @Asset annotation.
	 *  @param container The instance of containerType. May be null if the field or method containing the AssetLoaderParameters is static or no AssetLoaderParameters are specified by the @Asset annotation. */
	private void load(Asset asset, Object pathObj, Class<?> containerType, Object container) {
		if(pathObj instanceof Object[]) {
			Object[] pathObjs = (Object[]) pathObj;
			for(Object path : pathObjs)
				load(asset, path, containerType, container);
		} else
			load(getAssetPath(pathObj), getAssetType(asset, pathObj), getAssetLoaderParameters(asset, pathObj, containerType, container));
	}

	private <T> void load(Class<T> container, T instance) {
		for(Field field : ClassReflection.getDeclaredFields(container)) {
			if(!field.isAnnotationPresent(Asset.class))
				continue;
			Asset asset = field.getDeclaredAnnotation(Asset.class).getAnnotation(Asset.class);
			if(asset.load())
				load(field, instance);
		}
		for(Method method : ClassReflection.getDeclaredMethods(container)) {
			// TODO com.badlogic.gdx.reflect.Method does not provide access to annotations
		}
	}

	@SuppressWarnings("unchecked")
	public <T> void load(T container) {
		load((Class<T>) container.getClass(), container);
	}

	public void load(Class<?> container) {
		load(container, null);
	}

	public void load(Field field, Object container) {
		load(field.isAnnotationPresent(Asset.class) ? field.getDeclaredAnnotation(Asset.class).getAnnotation(Asset.class) : null, get(field, container), field.getDeclaringClass(), container);
	}

	public void load(Field field) {
		load(field, null);
	}

	public void load(Method method, Object container) {
		if(method.getParameterTypes().length != 0)
			throw new IllegalArgumentException(method + " takes parameters. Methods that take parameters are not supported.");
		if(method.getReturnType().isPrimitive())
			throw new IllegalArgumentException(method + " returns " + method.getReturnType() + ". Methods that return primitives are not supported.");
		throw new UnsupportedOperationException("com.badlogic.gdx.reflect.Method does not provide access to annotations");
		// TODO com.badlogic.gdx.reflect.Method does not provide access to annotations
	}

	public void load(Method method) {
		load(method, null);
	}

	/** provides information about assets that fields or methods represent
	 *  @author dermetfan */
	@Documented
	@Retention(RetentionPolicy.RUNTIME)
	@Target({ElementType.FIELD, ElementType.METHOD})
	public @interface Asset {

		/** @return Whether this field or method should be loaded by {@link AnnotationAssetManager#load(Class, Object)}. Default is @{@code true}. */
		boolean load() default true;

		/** @return the type of the asset this field or method represents */
		Class<?> value() default void.class;

		/** @return The fully qualified or simple name of a field or method providing AssetLoaderParameters.
		 *  If the name is simple, the declaring class of this field or method is assumed to be the declaring class of the AssetLoaderParameters field or method as well. */
		String params() default "";

	}

}
