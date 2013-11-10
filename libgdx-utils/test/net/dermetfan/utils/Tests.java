package net.dermetfan.utils;

import static org.junit.Assert.assertEquals;
import net.dermetfan.utils.libgdx.math.GeometryUtilsTest;
import net.dermetfan.utils.math.MathUtilsTest;

import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;

public class Tests {

	@Test
	public void testAll() {
		Result result = JUnitCore.runClasses(ArrayUtilsTest.class, MathUtilsTest.class, GeometryUtilsTest.class, AppenderTest.class);
		System.out.println(result.getRunCount() - result.getFailureCount() + "/" + result.getRunCount() + " succeeded, " + result.getFailureCount() + " failed, " + result.getIgnoreCount() + " ignored, " + result.getRunTime() + "ms");
		assertEquals(0, result.getFailureCount());
	}

}
