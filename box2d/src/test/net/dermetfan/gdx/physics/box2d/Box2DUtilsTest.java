package net.dermetfan.gdx.physics.box2d;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class Box2DUtilsTest {

	@Test
	public void weld() {
		float[] vertices = {
				0, 0,
				1, 1, 1.0001f, 1.0001f,
				2, 2,
				3, 3, 3.0001f, 3.0001f, 3.00011f, 3.00011f,
				4, 4,
				5, 5
		}, solution = {
				0, 0,
				1, 1,
				2, 2,
				3, 3,
				4, 4,
				5, 5
		};
		assertEquals(6, Box2DUtils.weld(vertices));
		for(int i = 0; i < solution.length; i++)
			if(vertices[i] != solution[i])
				fail("differed at index " + i + ", expected: " + solution[i] + ", actual: " + vertices[i]);
	}

}
