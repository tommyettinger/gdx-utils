/** Copyright 2014 Robin Stumm (serverkorken@googlemail.com, http://dermetfan.bplaced.net)
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

import java.io.BufferedReader;
import java.io.IOException;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.assets.AssetLoaderParameters;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.assets.loaders.AsynchronousAssetLoader;
import com.badlogic.gdx.assets.loaders.FileHandleResolver;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

/** loads {@link PolygonRegion PolygonRegions} using a {@link com.badlogic.gdx.graphics.g2d.PolygonRegionLoader}
 *  @author dermetfan */
public class PolygonRegionLoader extends AsynchronousAssetLoader<PolygonRegion, AssetLoaderParameters<PolygonRegion>> {

	public static class Info {

		/** what the line starts with that contains the file name of the texture for this {@code PolygonRegion} */
		public String texturePrefix = "i ";

		/** what buffer size of the reader should be used to read the {@link #texturePrefix} line
		 *  @see FileHandle#reader(int) */
		public int readerBuffer = 1024;

		/** the possible file name extensions of the texture file */
		public String[] textureExtensions = new String[] {"png", "PNG", "jpeg", "JPEG", "jpg", "JPG", "cim", "CIM", "etc1", "ETC1"};

	}

	private Info info;

	private com.badlogic.gdx.graphics.g2d.PolygonRegionLoader loader = new com.badlogic.gdx.graphics.g2d.PolygonRegionLoader();

	public PolygonRegionLoader(FileHandleResolver resolver) {
		this(resolver, new Info());
	}

	public PolygonRegionLoader(FileHandleResolver resolver, Info info) {
		super(resolver);
		this.info = info;
	}

	@Override
	public void loadAsync(AssetManager manager, String fileName, FileHandle file, AssetLoaderParameters<PolygonRegion> parameter) {
	}

	@Override
	public PolygonRegion loadSync(AssetManager manager, String fileName, FileHandle file, AssetLoaderParameters<PolygonRegion> parameter) {
		Texture texture = manager.get(manager.getDependencies(fileName).first());
		return loader.load(new TextureRegion(texture), file);
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Array<AssetDescriptor> getDependencies(String fileName, FileHandle file, AssetLoaderParameters<PolygonRegion> parameter) {
		String image = null;
		try {
			BufferedReader reader = file.reader(info.readerBuffer);
			for(String line = reader.readLine(); line != null; line = reader.readLine())
				if(line.startsWith(info.texturePrefix)) {
					image = line.substring(info.texturePrefix.length());
					break;
				}
			reader.close();
		} catch(IOException e) {
			Gdx.app.error(PolygonRegionLoader.class.getSimpleName(), "could not read " + fileName, e);
		}

		if(image == null && info.textureExtensions != null)
			for(String extension : info.textureExtensions) {
				FileHandle sibling = file.sibling(file.nameWithoutExtension().concat("." + extension));
				if(sibling.exists())
					image = sibling.name();
			}

		if(image != null) {
			Array<AssetDescriptor> deps = new Array<AssetDescriptor>(1);
			deps.add(new AssetDescriptor<Texture>(file.sibling(image), Texture.class));
			return deps;
		}

		return null;
	}

}
