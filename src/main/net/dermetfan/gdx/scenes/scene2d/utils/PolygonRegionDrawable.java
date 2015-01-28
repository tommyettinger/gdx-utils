package net.dermetfan.gdx.scenes.scene2d.utils;

import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.PolygonRegion;
import com.badlogic.gdx.graphics.g2d.PolygonSpriteBatch;
import com.badlogic.gdx.scenes.scene2d.utils.BaseDrawable;
import com.badlogic.gdx.scenes.scene2d.utils.TransformDrawable;
import net.dermetfan.utils.math.GeometryUtils;

/** Drawable for a {@link PolygonRegion}.
 *  @author dermetfan
 *  @since 0.10.0 */
public class PolygonRegionDrawable extends BaseDrawable implements TransformDrawable {

	/** the region to draw */
	private PolygonRegion region;

	/** the min x and y values of the vertices of {@link #region} */
	private float polygonX, polygonY;

	/** the size of the vertices of {@link #region} */
	private float polygonWidth, polygonHeight;

	/** Creates an uninitialized instance. The region must be {@link #setRegion(PolygonRegion) set} before use. */
	public PolygonRegionDrawable() {}

	/** @param region the region to use */
	public PolygonRegionDrawable(PolygonRegion region) {
		setRegion(region);
	}

	/** @param drawable the drawable to copy */
	public PolygonRegionDrawable(PolygonRegionDrawable drawable) {
		super(drawable);
		this.region = drawable.region;
		this.polygonX = drawable.polygonX;
		this.polygonY = drawable.polygonY;
		this.polygonWidth = drawable.polygonWidth;
		this.polygonHeight = drawable.polygonHeight;
	}

	@Override
	public void draw(Batch batch, float x, float y, float width, float height) {
		width += region.getRegion().getRegionWidth() - polygonWidth;
		height += region.getRegion().getRegionHeight() - polygonHeight;
		if(batch instanceof PolygonSpriteBatch)
			((PolygonSpriteBatch) batch).draw(region, x - polygonX, y - polygonY, width, height);
		else
			batch.draw(region.getRegion(), x, y, width, height);
	}

	@Override
	public void draw(Batch batch, float x, float y, float originX, float originY, float width, float height, float scaleX, float scaleY, float rotation) {
		width += region.getRegion().getRegionWidth() - polygonWidth;
		height += region.getRegion().getRegionHeight() - polygonHeight;
		if(batch instanceof PolygonSpriteBatch)
			((PolygonSpriteBatch) batch).draw(region, x - polygonX, y - polygonY, originX, originY, width, height, scaleX, scaleY, rotation);
		else
			batch.draw(region.getRegion(), x, y, originX, originY, width, height, scaleX, scaleY, rotation);
	}

	/** @param region the {@link #region} to set */
	public void setRegion(PolygonRegion region) {
		this.region = region;
		float[] vertices = region.getVertices();
		polygonWidth = GeometryUtils.width(vertices);
		polygonHeight = GeometryUtils.height(vertices);
		polygonX = GeometryUtils.minX(vertices);
		polygonY = GeometryUtils.minY(vertices);
		setMinWidth(polygonWidth);
		setMinHeight(polygonHeight);
	}

	/** @return the {@link #region} */
	public PolygonRegion getRegion() {
		return region;
	}

	/** @return the {@link #polygonX} */
	public float getPolygonX() {
		return polygonX;
	}

	/** @return the {@link #polygonY} */
	public float getPolygonY() {
		return polygonY;
	}

	/** @return the {@link #polygonWidth} */
	public float getPolygonWidth() {
		return polygonWidth;
	}

	/** @return the {@link #polygonHeight} */
	public float getPolygonHeight() {
		return polygonHeight;
	}

}
