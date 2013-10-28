package net.dermetfan.util.libgdx.maps;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;

/** draws {@link TextureMapObject TextureMapObjects}
 *  @author dermetfan */
public class OrthogonalTiledMapRenderer extends com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer {

	public OrthogonalTiledMapRenderer(TiledMap map) {
		super(map);
	}

	public OrthogonalTiledMapRenderer(TiledMap map, float unitScale, SpriteBatch spriteBatch) {
		super(map, unitScale, spriteBatch);
	}

	public OrthogonalTiledMapRenderer(TiledMap map, float unitScale) {
		super(map, unitScale);
	}

	public OrthogonalTiledMapRenderer(TiledMap map, SpriteBatch spriteBatch) {
		super(map, spriteBatch);
	}

	@Override
	public void renderObject(MapObject object) {
		if(object instanceof TextureMapObject) {
			TextureMapObject mapObject = (TextureMapObject) object;
			spriteBatch.draw(mapObject.getTextureRegion(), mapObject.getX(), mapObject.getY(), mapObject.getOriginX(), mapObject.getOriginY(), mapObject.getTextureRegion().getRegionWidth(), mapObject.getTextureRegion().getRegionWidth(), mapObject.getScaleX(), mapObject.getScaleY(), mapObject.getRotation());
		}
	}

}
