package net.dermetfan.gdx.maps;

import com.badlogic.gdx.math.Vector2;
import org.junit.Test;

import static org.junit.Assert.*;

public class MapUtilsTest {

	@Test
	public void toIsometricGridPoint() {
		float x = 10, y = 15, cellWidth = 32, cellHeight = 35;

		Vector2 point = MapUtils.toIsometricGridPoint(x, y, cellWidth, cellHeight);
		float resultX = point.x, resultY = point.y;

		point.set(x, y);
		point.x = (point.x /= cellWidth) - ((point.y = (point.y - cellHeight / 2) / cellHeight + point.x) - point.x); // tha magic formula that defines truth

		assertEquals(point.x, resultX, 0);
		assertEquals(point.y, resultY, 0);
	}

}
