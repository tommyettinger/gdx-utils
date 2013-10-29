package net.dermetfan.util.libgdx;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Field;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.files.FileHandle;

/** An {@link AssetManager} that loads assets from a container class.
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
	 *  @param container the class containing the fields whose {@link AssetDescriptor AssetDescriptors} to load */
	public void load(Class<?> container) {
		for(Field field : container.getFields())
			if(field.isAnnotationPresent(Asset.class) && field.getAnnotation(Asset.class).load())
				load(field);
	}

	/** {@link AssetManager#load(AssetDescriptor) Loads} the {@link AssetDescriptor} from the given field, or if the field's type is {@code String} or {@link FileHandle}, a {@link #createAssetDescriptor(Field) created} one.
	 *  @param field the field whose {@link AssetDescriptor} to load or null if not successful */
	public void load(Field field) {
		if(!field.isAnnotationPresent(Asset.class))
			return;
		if(field.getDeclaringClass() == AssetDescriptor.class)
			try {
				AssetDescriptor<?> descriptor = (AssetDescriptor<?>) field.get(null);
				if(descriptor != null)
					load(descriptor);
			} catch(IllegalArgumentException e) {
				e.printStackTrace();
			} catch(IllegalAccessException e) {
				e.printStackTrace();
			}
		else {
			AssetDescriptor<?> descriptor = createAssetDescriptor(field);
			if(descriptor != null)
				load(descriptor);
		}
	}

	/** Creates an {@link AssetDescriptor} from a field that is annotated with {@link Asset}. The field's type must be {@code String} or {@link FileHandle} and the {@link Asset#type()} must not be primitive.
	 *  @param field the field annotated with {@link Asset} to create an {@link AssetDescriptor} from
	 *  @return an {@link AssetDescriptor} created from the given, with {@link Asset} annotated field */
	@SuppressWarnings("unchecked")
	public <T> AssetDescriptor<T> createAssetDescriptor(Field field) {
		if(!field.isAnnotationPresent(Asset.class))
			return null;
		Class<?> fieldType = field.getType();
		if(fieldType != String.class && fieldType != FileHandle.class) {
			Gdx.app.error(AnnotationAssetManager.class.getSimpleName(), "type of @" + Asset.class.getSimpleName() + " field \"" + field.getName() + "\" must be " + String.class.getSimpleName() + " to create an " + AssetDescriptor.class.getSimpleName() + " from it");
			return null;
		}
		Asset asset = field.getAnnotation(Asset.class);
		if(asset.type().isPrimitive()) {
			Gdx.app.error(AnnotationAssetManager.class.getSimpleName(), "cannot create an " + AssetDescriptor.class.getSimpleName() + " of the generic type " + asset.type().getSimpleName() + " from the @" + Asset.class.getSimpleName() + " field \"" + field.getName() + "\"");
			return null;
		}
		AssetDescriptor<T> descriptor = null;
		try {
			if(fieldType == String.class)
				descriptor = new AssetDescriptor<T>((String) field.get(null), (Class<T>) asset.type());
			else
				descriptor = new AssetDescriptor<T>((FileHandle) field.get(null), (Class<T>) asset.type());
		} catch(IllegalArgumentException e) {
			e.printStackTrace();
		} catch(IllegalAccessException e) {
			e.printStackTrace();
		}
		return descriptor;
	}

}