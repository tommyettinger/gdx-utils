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

package net.dermetfan.gdx.physics.box2d;

import java.util.Arrays;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.JointDef;
import com.badlogic.gdx.physics.box2d.MassData;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.Shape.Type;
import com.badlogic.gdx.physics.box2d.Transform;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.DistanceJoint;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;
import com.badlogic.gdx.physics.box2d.joints.FrictionJoint;
import com.badlogic.gdx.physics.box2d.joints.FrictionJointDef;
import com.badlogic.gdx.physics.box2d.joints.GearJoint;
import com.badlogic.gdx.physics.box2d.joints.GearJointDef;
import com.badlogic.gdx.physics.box2d.joints.MotorJoint;
import com.badlogic.gdx.physics.box2d.joints.MotorJointDef;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
import com.badlogic.gdx.physics.box2d.joints.PrismaticJoint;
import com.badlogic.gdx.physics.box2d.joints.PrismaticJointDef;
import com.badlogic.gdx.physics.box2d.joints.PulleyJoint;
import com.badlogic.gdx.physics.box2d.joints.PulleyJointDef;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJoint;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.physics.box2d.joints.RopeJoint;
import com.badlogic.gdx.physics.box2d.joints.RopeJointDef;
import com.badlogic.gdx.physics.box2d.joints.WeldJoint;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;
import com.badlogic.gdx.physics.box2d.joints.WheelJoint;
import com.badlogic.gdx.physics.box2d.joints.WheelJointDef;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ObjectMap;
import com.badlogic.gdx.utils.Pools;
import net.dermetfan.gdx.math.GeometryUtils;
import net.dermetfan.gdx.math.MathUtils;
import net.dermetfan.gdx.utils.ArrayUtils;
import net.dermetfan.utils.Pair;

import static net.dermetfan.gdx.math.GeometryUtils.filterX;
import static net.dermetfan.gdx.math.GeometryUtils.filterY;
import static net.dermetfan.gdx.math.MathUtils.amplitude2;
import static net.dermetfan.gdx.math.MathUtils.max;
import static net.dermetfan.gdx.math.MathUtils.min;
import static net.dermetfan.gdx.physics.box2d.Box2DUtils.Settings.epsilon;
import static net.dermetfan.gdx.physics.box2d.Box2DUtils.Settings.linearSlop;
import static net.dermetfan.gdx.physics.box2d.Box2DUtils.Settings.maxPolygonVertices;

/** provides methods for operations with Box2D {@link Body Bodies}, {@link Fixture Fixtures} and {@link Shape Shapes}
 *  @author dermetfan */
public class Box2DUtils extends com.badlogic.gdx.physics.box2d.Box2DUtils {

	/** b2Settings.h
	 *  @author dermetfan
	 *  @since 0.11.0 */
	public static class Settings {

		/** b2_epsilon */
		public static final float epsilon = 1e-5f; // 1.1920928955078125e-7f

		/** b2_maxPolygonVertices, the max amount of vertices of a {@link PolygonShape} */
		public static final byte maxPolygonVertices = 8;

		/** b2_linearSlop, the min distance between vertices */
		public static final float linearSlop = .005f;

	}

	/** checks if Box2D's preconditions are met to avoid native crashes
	 *  @author dermetfan
	 *  @since 0.11.0 */
	public enum PreconditionCheck {

		/** doesn't check anything */
		NONE {
			@Override
			public boolean isValidChainShape(float[] vertices, int offset, int length) {
				return true;
			}

			@Override
			public boolean isValidPolygonShape(float[] vertices, int offset, int length) {
				return true;
			}
		},

		/** checks preconditions normally */
		SILENT {
			@Override
			public boolean isValidChainShape(float[] vertices, int offset, int length) {
				try {
					checkChainShape(vertices, offset, length);
				} catch(Exception e) {
					return false;
				}
				return true;
			}

			@Override
			public boolean isValidPolygonShape(float[] vertices, int offset, int length) {
				try {
					checkPolygonShape(vertices, offset, length);
				} catch(Exception e) {
					return false;
				}
				return true;
			}
		},

		/** throws an exception */
		EXCEPTION {
			@Override
			public boolean isValidChainShape(float[] vertices, int offset, int length) {
				checkChainShape(vertices, offset, length);
				return true;
			}

			@Override
			public boolean isValidPolygonShape(float[] vertices, int offset, int length) {
				checkPolygonShape(vertices, offset, length);
				return true;
			}
		};

		/** indicates that a poly shape cannot be created from these vertices
		 *  @author dermetfan
		 *  @since 0.11.0 */
		public static abstract class InvalidPolyShapeException extends IllegalArgumentException {

			/** the vertices of the shape */
			public float[] vertices;
			public int offset, length;

			InvalidPolyShapeException(String message, float[] vertices, int offset, int length) {
				super(message);
				this.vertices = vertices;
				this.offset = offset;
				this.length = length;
			}

			/** @return the Type of the Shape */
			public abstract Type getType();

		}

		/** indicates that a PolygonShape cannot be created from this polygon
		 *  @author dermetfan
		 *  @since 0.11.0 */
		public static class InvalidPolygonShapeException extends InvalidPolyShapeException {

			/** the reason this shape is invalid
			 *  @author dermetfan
			 *  @since 0.11.10 */
			public enum Problem {

				/** malformed vertices */
				MALFORMED_VERTICES,

				/** invalid vertex count: smaller than 3 or greater than {@link Settings#maxPolygonVertices} */
				VERTEX_COUNT,

				/** the vertices form a concave polygon */
				CONCAVE,

				/** too small area: smaller than {@link Settings#epsilon} */
				AREA

			}

			/** why a PolygonShape cannot be created */
			public Problem problem;

			public InvalidPolygonShapeException(String message, Problem problem, float[] vertices, int offset, int length) {
				super(message, vertices, offset, length);
				this.problem = problem;
			}

			@Override
			public Type getType() {
				return Type.Polygon;
			}

		}

		/** indicates that a ChainShape cannot be created from this polyline
		 *  @author dermetfan
		 *  @since 0.11.0 */
		public static class InvalidChainShapeException extends InvalidPolyShapeException {

			/** the reason why this ChainShape cannot be created
			 *  @author dermetfan
			 *  @since 0.11.0 */
			public enum Problem {

				/** malformed vertices */
				MALFORMED_VERTICES,

				/** less than 2 vertices */
				VERTEX_COUNT,

				/** too close vertices: the squared distance between at least 2 vertices is closer than {@link Settings#linearSlop} squared */
				CLOSE_VERTICES

			}

			public Problem problem;

			public InvalidChainShapeException(String message, Problem problem, float[] vertices, int offset, int length) {
				super(message, vertices, offset, length);
				this.problem = problem;
			}

			@Override
			public Type getType() {
				return Type.Chain;
			}

		}

		public static void checkChainShape(float[] vertices, int offset, int length) {
			ArrayUtils.checkRegion(vertices, offset, length);
			if(length % 2 != 0)
				throw new InvalidChainShapeException("chain vertices are malformed. vertices.length: " + length, InvalidChainShapeException.Problem.MALFORMED_VERTICES, vertices, offset, length);
			if(length < 4)
				throw new InvalidChainShapeException("chain has less than 2 vertices: vertices.length: " + length, InvalidChainShapeException.Problem.VERTEX_COUNT, vertices, offset, length);
			boolean verticesTooClose = false;
			for(int i = offset; i + 3 < offset + length; i += 2) {
				float x1 = vertices[i], y1 = vertices[i + 1], x2 = vertices[i + 2], y2 = vertices[i + 3];
				if(GeometryUtils.distance2(x1, y1, x2, y2) > linearSlop * linearSlop) {
					verticesTooClose = true;
					break;
				}
			}
			if(verticesTooClose)
				throw new InvalidChainShapeException("chain vertices are too close together", InvalidChainShapeException.Problem.CLOSE_VERTICES, vertices, offset, length);
		}

		public static void checkPolygonShape(float[] vertices, int offset, int length) {
			ArrayUtils.checkRegion(vertices, offset, length);
			if(length % 2 != 0)
				throw new InvalidPolygonShapeException("polygon vertices are malformed. vertices.length: " + length, InvalidPolygonShapeException.Problem.MALFORMED_VERTICES, vertices, offset, length);
			if(length < 6 || length > maxPolygonVertices * 2)
				throw new InvalidPolygonShapeException("polygon has invalid number of vertices (min: 3, max: Settings.maxPolygonVertices = " + maxPolygonVertices + "). length: " + length, InvalidPolygonShapeException.Problem.VERTEX_COUNT, vertices, offset, length);
			float[] floats = GeometryUtils.getFloats();
			System.arraycopy(vertices, offset, floats, 0, length);
			int count = weld(floats, 0, length);
			if(count < 3)
				throw new InvalidPolygonShapeException("polygon has too few vertices after welding: " + count, InvalidPolygonShapeException.Problem.VERTEX_COUNT, vertices, offset, length);
			if(!GeometryUtils.isConvex(vertices, offset, length))
				throw new InvalidPolygonShapeException("polygon is concave", InvalidPolygonShapeException.Problem.CONCAVE, vertices, offset, length);
			float area = GeometryUtils.polygonArea(vertices, offset, length);
			if(area < epsilon)
				throw new InvalidPolygonShapeException("polygon area is too small: " + area + " (min is Settings.epsilon: " + epsilon, InvalidPolygonShapeException.Problem.AREA, vertices, offset, length);
		}

		public boolean isValidChainShape(FloatArray vertices) {
			return isValidChainShape(vertices.items, 0, vertices.size);
		}

		public boolean isValidChainShape(float[] vertices) {
			return isValidChainShape(vertices, 0, vertices.length);
		}

		public abstract boolean isValidChainShape(float[] vertices, int offset, int length);

		public boolean isValidPolygonShape(FloatArray vertices) {
			return isValidPolygonShape(vertices.items, 0, vertices.size);
		}

		public boolean isValidPolygonShape(float[] vertices) {
			return isValidPolygonShape(vertices, 0, vertices.length);
		}

		public abstract boolean isValidPolygonShape(float[] vertices, int offset, int length);

	}

	/** cached method results
	 *  @author dermetfan */
	public static class ShapeCache {

		/** @see Box2DUtils#vertices0(Shape) */
		public final float[] vertices;

		/** @see Box2DUtils#width0(Shape) */
		public final float width;

		/** @see Box2DUtils#height0(Shape) */
		public final float height;

		/** @see Box2DUtils#minX0(Shape) */
		public final float minX;

		/** @see Box2DUtils#maxX0(Shape) */
		public final float maxX;

		/** @see Box2DUtils#minY0(Shape) */
		public final float minY;

		/** @see Box2DUtils#minY0(Shape) */
		public final float maxY;

		/** @param vertices the {@link #vertices}
		 *  @param width the {@link #width}
		 *  @param height the {@link #height}
		 *  @param minX the {@link #minX}
		 *  @param maxX the {@link #maxX}
		 *  @param minY the {@link #minY}
		 *  @param maxY the {@link #maxX} */
		public ShapeCache(float[] vertices, float width, float height, float minX, float maxX, float minY, float maxY) {
			this.vertices = vertices;
			this.width = width;
			this.height = height;
			this.minX = minX;
			this.maxX = maxX;
			this.minY = minY;
			this.maxY = maxY;
		}

	}

	/** Cached {@link Shape Shapes} and their {@link ShapeCache}. You should {@link ObjectMap#clear() clear} this when you don't use the shapes anymore. */
	public static final ObjectMap<Shape, ShapeCache> cache = new ObjectMap<>();

	/** if shapes should automatically be cached when they are inspected for the first time */
	public static boolean autoCache = true;

	/** the PreconditionCheck to use */
	public static PreconditionCheck check = PreconditionCheck.SILENT;

	/** for internal, temporary usage */
	private static final Vector2 vec2_0 = new Vector2(), vec2_1 = new Vector2();

	/** for internal, temporary usage */
	private static final Polygon polygon = new Polygon(new float[maxPolygonVertices]);

	/** @param shape the Shape to create a new {@link ShapeCache} for that will be added to {@link #cache} */
	public static ShapeCache cache(Shape shape) {
		if(cache.containsKey(shape))
			return cache.get(shape);
		float[] vertices = vertices0(shape), cachedVertices = new float[vertices.length];
		System.arraycopy(vertices, 0, cachedVertices, 0, vertices.length);
		ShapeCache results = new ShapeCache(cachedVertices, width0(shape), height0(shape), minX0(shape), maxX0(shape), minY0(shape), maxY0(shape));
		cache.put(shape, results);
		return results;
	}

	// shape

	/** @param shape the Shape which vertices to get (for circles, the bounding box vertices will be returned)
	 *  @return the vertices of the given Shape*/
	private static float[] vertices0(Shape shape) {
		float[] vertices;
		switch(shape.getType()) {
		case Polygon:
			PolygonShape polygonShape = (PolygonShape) shape;
			int polygonVertexCount = polygonShape.getVertexCount();
			vertices = new float[polygonVertexCount * 2];
			for(int i = 0; i < polygonVertexCount; i++) {
				polygonShape.getVertex(i, vec2_0);
				vertices[i * 2] = vec2_0.x;
				vertices[i * 2 + 1] = vec2_0.y;
			}
			break;
		case Edge:
			EdgeShape edgeShape = (EdgeShape) shape;
			edgeShape.getVertex1(vec2_0);
			edgeShape.getVertex2(vec2_1);
			vertices = new float[] {vec2_0.x, vec2_0.y, vec2_1.x, vec2_1.y};
			break;
		case Chain:
			ChainShape chainShape = (ChainShape) shape;
			int chainVertexCount = chainShape.getVertexCount();
			vertices = new float[chainVertexCount * 2];
			for(int i = 0; i < chainVertexCount; i++) {
				chainShape.getVertex(i, vec2_0);
				vertices[i * 2] = vec2_0.x;
				vertices[i * 2 + 1] = vec2_0.y;
			}
			break;
		case Circle:
			CircleShape circleShape = (CircleShape) shape;
			Vector2 position = circleShape.getPosition();
			float radius = circleShape.getRadius();
			vertices = new float[] {
					position.x - radius, position.y - radius, // bottom left
					position.x + radius, position.y - radius, // bottom right
					position.x + radius, position.y + radius, // top right
					position.x - radius, position.y + radius // top left
			};
			break;
		default:
			throw new IllegalArgumentException("shapes of the type '" + shape.getType().name() + "' are not supported");
		}
		return vertices;
	}

	/** @return the minimal x of the vertices of the given Shape */
	private static float minX0(Shape shape) {
		if(shape instanceof CircleShape)
			return ((CircleShape) shape).getPosition().x - shape.getRadius();
		return min(filterX(vertices0(shape)));
	}

	/** @return the minimal y of the vertices of the given Shape */
	private static float minY0(Shape shape) {
		if(shape instanceof CircleShape)
			return ((CircleShape) shape).getPosition().y - shape.getRadius();
		return min(filterY(vertices0(shape)));
	}

	/** @return the maximal x of the vertices of the given Shape */
	private static float maxX0(Shape shape) {
		if(shape instanceof CircleShape)
			return ((CircleShape) shape).getPosition().x + shape.getRadius();
		return max(filterX(vertices0(shape)));
	}

	/** @return the maximal y of the vertices of the given Shape */
	private static float maxY0(Shape shape) {
		if(shape instanceof CircleShape)
			return ((CircleShape) shape).getPosition().y + shape.getRadius();
		return max(filterY(vertices0(shape)));
	}

	/** @return the width of the given Shape */
	private static float width0(Shape shape) {
		if(shape.getType() == Type.Circle)
			return shape.getRadius() * 2;
		return amplitude2(filterX(vertices0(shape)));
	}

	/** @return the height of the given Shape */
	private static float height0(Shape shape) {
		if(shape.getType() == Type.Circle)
			return shape.getRadius() * 2;
		return amplitude2(filterY(vertices0(shape)));
	}

	/** @return a Vector2 representing the size of the given Shape */
	private static Vector2 size0(Shape shape) {
		return vec2_0.set(width0(shape), height0(shape));
	}

	// cache

	/** @return the vertices of the given Shape */
	public static float[] vertices(Shape shape) {
		if(cache.containsKey(shape))
			return cache.get(shape).vertices;
		if(autoCache)
			return cache(shape).vertices;
		return vertices0(shape);
	}

	/** @return the minimal x value of the vertices of the given Shape */
	public static float minX(Shape shape) {
		if(cache.containsKey(shape))
			return cache.get(shape).minX;
		if(autoCache)
			return cache(shape).minX;
		return minX0(shape);
	}

	/** @return the minimal y value of the vertices of the given Shape */
	public static float minY(Shape shape) {
		if(cache.containsKey(shape))
			return cache.get(shape).minY;
		if(autoCache)
			return cache(shape).minY;
		return minY0(shape);
	}

	/** @return the maximal x value of the vertices of the given Shape */
	public static float maxX(Shape shape) {
		if(cache.containsKey(shape))
			return cache.get(shape).maxX;
		if(autoCache)
			return cache(shape).maxX;
		return maxX0(shape);
	}

	/** @return the maximal y value of the vertices of the given Shape */
	public static float maxY(Shape shape) {
		if(cache.containsKey(shape))
			return cache.get(shape).maxY;
		if(autoCache)
			return cache(shape).maxY;
		return maxY0(shape);
	}

	/** @return the width of the given Shape */
	public static float width(Shape shape) {
		if(cache.containsKey(shape))
			return cache.get(shape).width;
		if(autoCache)
			return cache(shape).width;
		return width0(shape);
	}

	/** @return the height of the given Shape */
	public static float height(Shape shape) {
		if(cache.containsKey(shape))
			return cache.get(shape).height;
		if(autoCache)
			return cache(shape).height;
		return height0(shape);
	}

	/** @return a {@link Vector2} representing the size of the given Shape */
	public static Vector2 size(Shape shape) {
		ShapeCache results = cache.containsKey(shape) ? cache.get(shape) : autoCache ? cache(shape) : null;
		return results != null ? vec2_0.set(results.width, results.height) : size0(shape);
	}

	// fixture

	/** @see #vertices(Shape) */
	public static float[] vertices(Fixture fixture) {
		return vertices(fixture.getShape());
	}

	/** @see #minX(Shape) */
	public static float minX(Fixture fixture) {
		return minX(fixture.getShape());
	}

	/** @see #minY(Shape) */
	public static float minY(Fixture fixture) {
		return minY(fixture.getShape());
	}

	/** @see #maxX(Shape) */
	public static float maxX(Fixture fixture) {
		return maxX(fixture.getShape());
	}

	/** @see #maxY(Shape) */
	public static float maxY(Fixture fixture) {
		return maxY(fixture.getShape());
	}

	/** @see #width(Shape) */
	public static float width(Fixture fixture) {
		return width(fixture.getShape());
	}

	/** @see #height(Shape) */
	public static float height(Fixture fixture) {
		return height(fixture.getShape());
	}

	/** @see #size(Shape) */
	public static Vector2 size(Fixture fixture) {
		return size(fixture.getShape());
	}

	// body

	/** @return the vertices of all fixtures of a body */
	public static float[][] fixtureVertices(Body body) {
		Array<Fixture> fixtures = body.getFixtureList();
		float[][] vertices = new float[fixtures.size][];
		for(int i = 0; i < vertices.length; i++)
			vertices[i] = vertices(fixtures.get(i));
		return vertices;
	}

	/** @return the minimal x value of the vertices of all fixtures of the the given Body */
	public static float minX(Body body) {
		float x = Float.POSITIVE_INFINITY, tmp;
		for(Fixture fixture : body.getFixtureList())
			if((tmp = minX(fixture)) < x)
				x = tmp;
		return x;
	}

	/** @return the minimal y value of the vertices of all fixtures of the the given Body */
	public static float minY(Body body) {
		float y = Float.POSITIVE_INFINITY, tmp;
		for(Fixture fixture : body.getFixtureList())
			if((tmp = minY(fixture)) < y)
				y = tmp;
		return y;
	}

	/** @return the maximal x value of the vertices of all fixtures of the the given Body */
	public static float maxX(Body body) {
		float x = Float.NEGATIVE_INFINITY, tmp;
		for(Fixture fixture : body.getFixtureList())
			if((tmp = maxX(fixture)) > x)
				x = tmp;
		return x;
	}

	/** @return the maximal y value of the vertices of all fixtures of the the given Body */
	public static float maxY(Body body) {
		float y = Float.NEGATIVE_INFINITY, tmp;
		for(Fixture fixture : body.getFixtureList())
			if((tmp = maxY(fixture)) > y)
				y = tmp;
		return y;
	}

	/** @return the width of the given Body */
	public static float width(Body body) {
		return Math.abs(maxX(body) - minX(body));
	}

	/** @return the height of the given Body */
	public static float height(Body body) {
		return Math.abs(maxY(body) - minY(body));
	}

	public static Vector2 size(Body body) {
		return vec2_0.set(width(body), height(body));
	}

	// position

	/** @see #positionRelative(Shape, float)
	 *  @see CircleShape#getPosition() */
	public static Vector2 positionRelative(CircleShape shape) {
		return shape.getPosition();
	}

	/** @return the position of the given shape relative to its Body */
	public static Vector2 positionRelative(Shape shape, float rotation) {
		if(shape instanceof CircleShape)
			return positionRelative((CircleShape) shape); // faster
		return vec2_0.set(minX(shape) + width(shape) / 2, minY(shape) + height(shape) / 2).rotate(rotation);
	}

	/** @return the position of the given Shape in world coordinates
	 *  @param shape the Shape which position to get
	 *  @param body the Body the given Shape is attached to */
	public static Vector2 position(Shape shape, Body body) {
		return body.getPosition().add(positionRelative(shape, body.getAngle() * com.badlogic.gdx.math.MathUtils.radDeg));
	}

	/** @see #positionRelative(Shape, float) */
	public static Vector2 positionRelative(Fixture fixture) {
		return positionRelative(fixture.getShape(), fixture.getBody().getAngle() * com.badlogic.gdx.math.MathUtils.radDeg);
	}

	/** @see #position(Shape, Body) */
	public static Vector2 position(Fixture fixture) {
		return position(fixture.getShape(), fixture.getBody());
	}

	// aabb

	/** @param shape the Shape which AABB to get
	 *  @param aabb the Rectangle to set to the given Shape's AABB
	 *  @return the given Rectangle set as axis aligned bounding box of the given Shape
	 *  @since 0.9.1 */
	public static Rectangle aabb(Shape shape, float rotation, Rectangle aabb) {
		if(com.badlogic.gdx.math.MathUtils.isZero(rotation))
			return aabb.set(minX(shape), minY(shape), width(shape), height(shape));

		float[] vertices = vertices(shape);
		GeometryUtils.reset(polygon);
		float[] polygonVertices = polygon.getVertices();
		if(polygonVertices.length < vertices.length || polygonVertices.length % 2 != 0)
			polygonVertices = new float[vertices.length];
		System.arraycopy(vertices, 0, polygonVertices, 0, vertices.length);
		// if polygonVertices.length > vertices.length, set remaining polygonVertices to the last vertex from vertices to make them redundant
		for(int i = vertices.length; i < polygonVertices.length; i += 2) {
			polygonVertices[i] = vertices[vertices.length - 2];
			polygonVertices[i + 1] = vertices[vertices.length - 1];
		}
		polygon.setVertices(polygonVertices);
		if(shape.getType() == Type.Circle) {
			polygon.setOrigin(GeometryUtils.minX(vertices) + GeometryUtils.width(vertices) / 2, GeometryUtils.minY(vertices) + GeometryUtils.height(vertices) / 2);
			polygon.setRotation(-rotation * com.badlogic.gdx.math.MathUtils.radDeg);
			polygon.setVertices(polygon.getTransformedVertices());
			polygon.setOrigin(0, 0);
		}
		polygon.setRotation(rotation * com.badlogic.gdx.math.MathUtils.radDeg);
		return aabb.set(polygon.getBoundingRectangle());
	}

	/** @see #aabb(Shape, float, Rectangle) */
	public static Rectangle aabb(Shape shape, float rotation) {
		return aabb(shape, rotation, polygon.getBoundingRectangle());
	}

	/** @return the given Rectangle set as axis aligned bounding box of the given Fixture, in world coordinates
	 *  @see #aabb(Shape, float, Rectangle) */
	public static Rectangle aabb(Fixture fixture, Rectangle aabb) {
		return aabb(fixture.getShape(), fixture.getBody().getAngle(), aabb).setPosition(fixture.getBody().getPosition().add(aabb.x, aabb.y));
	}

	/** @see #aabb(Fixture, Rectangle) */
	public static Rectangle aabb(Fixture fixture) {
		return aabb(fixture, polygon.getBoundingRectangle());
	}

	/** @return the given Rectangle set as axis aligned bounding box of all fixtures of the given Body, in world coordinates
	 *  @since 0.9.1 */
	public static Rectangle aabb(Body body, Rectangle aabb) {
		float minX = Float.POSITIVE_INFINITY, minY = Float.POSITIVE_INFINITY, maxX = Float.NEGATIVE_INFINITY, maxY = Float.NEGATIVE_INFINITY;
		for(Fixture fixture : body.getFixtureList()) {
			aabb(fixture, aabb);
			if(aabb.x < minX)
				minX = aabb.x;
			if(aabb.x + aabb.width > maxX)
				maxX = aabb.x + aabb.width;
			if(aabb.y < minY)
				minY = aabb.y;
			if(aabb.y + aabb.height > maxY)
				maxY = aabb.y + aabb.height;
		}
		return aabb.set(minX, minY, maxX - minX, maxY - minY);
	}

	/** @see #aabb(Body, Rectangle) */
	public static Rectangle aabb(Body body) {
		return aabb(body, polygon.getBoundingRectangle());
	}

	// clone

	/** clones a Body (without deep copying the Shapes of its Fixtures)
	 *  @return {@link #clone(Body, boolean) copy(body, false)}
	 *  @see #clone(Body, boolean) */
	public static Body clone(Body body) {
		return clone(body, false);
	}

	/** clones a Body
	 *  @param body the Body to copy
	 *  @param shapes if the Shapes of the Fixtures of the given Body should be {@link #clone(Shape) copied} as well
	 *  @return a deep copy of the given Body */
	public static Body clone(Body body, boolean shapes) {
		Body clone = body.getWorld().createBody(createDef(body));
		clone.setUserData(body.getUserData());
		for(Fixture fixture : body.getFixtureList())
			clone(fixture, clone, shapes);
		return clone;
	}

	/** clones a Fixture (without deep copying its Shape)
	 *  @return {@link #clone(Fixture, Body, boolean) copy(fixture, body, false)}
	 *  @see #clone(Fixture, Body, boolean) */
	public static Fixture clone(Fixture fixture, Body body) {
		return clone(fixture, body, false);
	}

	/** clones a Fixture
	 *  @param fixture the Fixture to copy
	 *  @param body the Body to create a copy of the given {@code fixture} on
	 *  @param shape if the {@link Fixture#getShape() shape} of the given Fixture should be deep {@link #clone(Shape) copied} as well
	 *  @return the copied Fixture */
	public static Fixture clone(Fixture fixture, Body body, boolean shape) {
		FixtureDef fixtureDef = createDef(fixture);
		if(shape)
			fixtureDef.shape = clone(fixture.getShape());
		Fixture clone = body.createFixture(fixtureDef);
		clone.setUserData(clone.getUserData());
		return clone;
	}

	/** creates a deep copy of a Shape
	 *  @param shape the Shape to clone
	 *  @return a Shape exactly like the one passed in */
	@SuppressWarnings("unchecked")
	public static <T extends Shape> T clone(T shape) {
		T clone;
		switch(shape.getType()) {
		case Circle:
			CircleShape circleClone = (CircleShape) (clone = (T) new CircleShape());
			circleClone.setPosition(((CircleShape) shape).getPosition());
			break;
		case Polygon:
			PolygonShape polyClone = (PolygonShape) (clone = (T) new PolygonShape()), poly = (PolygonShape) shape;
			float[] vertices = new float[poly.getVertexCount()];
			for(int i = 0; i < vertices.length; i++) {
				poly.getVertex(i, vec2_0);
				vertices[i++] = vec2_0.x;
				vertices[i] = vec2_0.y;
			}
			polyClone.set(vertices);
			break;
		case Edge:
			EdgeShape edgeClone = (EdgeShape) (clone = (T) new EdgeShape()), edge = (EdgeShape) shape;
			edge.getVertex1(vec2_0);
			edge.getVertex2(vec2_1);
			edgeClone.set(vec2_0, vec2_1);
			break;
		case Chain:
			ChainShape chainClone = (ChainShape) (clone = (T) new ChainShape()), chain = (ChainShape) shape;
			vertices = new float[chain.getVertexCount()];
			for(int i = 0; i < vertices.length; i++) {
				chain.getVertex(i, vec2_0);
				vertices[i++] = vec2_0.x;
				vertices[i] = vec2_0.y;
			}
			if(chain.isLooped())
				chainClone.createLoop(vertices);
			else
				chainClone.createChain(vertices);
			break;
		default:
			return null;
		}
		clone.setRadius(shape.getRadius());
		return clone;
	}

	/** @param joint the joint to clone
	 *  @since 0.7.1 */
	@SuppressWarnings("unchecked")
	public static <T extends Joint> T clone(T joint) {
		return (T) joint.getBodyA().getWorld().createJoint(createDef(joint));
	}

	// defs

	/** @param bodyDef the BodyDef to set according to the given Body
	 *  @param body the Body to set the given BodyDef accordingly to
	 *  @return the given BodyDef for chaining
	 *  @since 0.7.1 */
	public static BodyDef set(BodyDef bodyDef, Body body) {
		bodyDef.active = body.isActive();
		bodyDef.allowSleep = body.isSleepingAllowed();
		bodyDef.angle = body.getAngle();
		bodyDef.angularDamping = body.getAngularDamping();
		bodyDef.angularVelocity = body.getAngularVelocity();
		bodyDef.awake = body.isAwake();
		bodyDef.bullet = body.isBullet();
		bodyDef.fixedRotation = body.isFixedRotation();
		bodyDef.gravityScale = body.getGravityScale();
		bodyDef.linearDamping = body.getLinearDamping();
		bodyDef.linearVelocity.set(body.getLinearVelocity());
		bodyDef.position.set(body.getPosition());
		bodyDef.type = body.getType();
		return bodyDef;
	}

	/** @param fixtureDef the FixtureDef to set according to the given Fixture
	 *  @param fixture the Fixture to set the given FixtureDef accordingly to
	 *  @return the given FixtureDef for chaining
	 *  @since 0.7.1 */
	public static FixtureDef set(FixtureDef fixtureDef, Fixture fixture) {
		fixtureDef.density = fixture.getDensity();
		Filter filter = fixture.getFilterData();
		fixtureDef.filter.categoryBits = filter.categoryBits;
		fixtureDef.filter.groupIndex = filter.groupIndex;
		fixtureDef.filter.maskBits = filter.maskBits;
		fixtureDef.friction = fixture.getFriction();
		fixtureDef.isSensor = fixture.isSensor();
		fixtureDef.restitution = fixture.getRestitution();
		fixtureDef.shape = fixture.getShape();
		return fixtureDef;
	}

	/** @param jointDef the JointDef to set according to the given Joint
	 *  @param joint the Joint to set the given JointDef accordingly to
	 *  @return the given JointDef for chaining
	 *  @since 0.7.1 */
	public static JointDef set(JointDef jointDef, Joint joint) {
		jointDef.type = joint.getType();
		jointDef.collideConnected = joint.getCollideConnected();
		jointDef.bodyA = joint.getBodyA();
		jointDef.bodyB = joint.getBodyB();
		return jointDef;
	}

	/** @see #set(JointDef, Joint) */
	public static DistanceJointDef set(DistanceJointDef jointDef, DistanceJoint joint) {
		set((JointDef) jointDef, joint);
		jointDef.dampingRatio = joint.getDampingRatio();
		jointDef.frequencyHz = joint.getFrequency();
		jointDef.length = joint.getLength();
		jointDef.localAnchorA.set(joint.getLocalAnchorA());
		jointDef.localAnchorB.set(joint.getLocalAnchorB());
		return jointDef;
	}

	/** @see #set(JointDef, Joint) */
	public static FrictionJointDef set(FrictionJointDef jointDef, FrictionJoint joint) {
		set((JointDef) jointDef, joint);
		jointDef.localAnchorA.set(joint.getLocalAnchorA());
		jointDef.localAnchorB.set(joint.getLocalAnchorB());
		jointDef.maxForce = joint.getMaxForce();
		jointDef.maxTorque = joint.getMaxTorque();
		return jointDef;
	}

	/** @see #set(JointDef, Joint) */
	public static GearJointDef set(GearJointDef jointDef, GearJoint joint) {
		set((JointDef) jointDef, joint);
		jointDef.joint1 = joint.getJoint1();
		jointDef.joint2 = joint.getJoint2();
		jointDef.ratio = joint.getRatio();
		return jointDef;
	}

	/** @see #set(JointDef, Joint) */
	public static MotorJointDef set(MotorJointDef jointDef, MotorJoint joint) {
		set((JointDef) jointDef, joint);
		jointDef.angularOffset = joint.getAngularOffset();
		jointDef.linearOffset.set(joint.getLinearOffset());
		jointDef.correctionFactor = joint.getCorrectionFactor();
		jointDef.maxForce = joint.getMaxForce();
		jointDef.maxTorque = joint.getMaxTorque();
		return jointDef;
	}

	/** @see #set(JointDef, Joint) */
	public static MouseJointDef set(MouseJointDef jointDef, MouseJoint joint) {
		set((JointDef) jointDef, joint);
		jointDef.dampingRatio = joint.getDampingRatio();
		jointDef.frequencyHz = joint.getFrequency();
		jointDef.maxForce = joint.getMaxForce();
		jointDef.target.set(joint.getTarget());
		return jointDef;
	}

	/** @see #set(JointDef, Joint) */
	public static RevoluteJointDef set(RevoluteJointDef jointDef, RevoluteJoint joint) {
		set((JointDef) jointDef, joint);
		jointDef.enableLimit = joint.isLimitEnabled();
		jointDef.enableMotor = joint.isMotorEnabled();
		jointDef.maxMotorTorque = joint.getMaxMotorTorque();
		jointDef.motorSpeed = joint.getMotorSpeed();
		jointDef.localAnchorA.set(joint.getLocalAnchorA());
		jointDef.localAnchorB.set(joint.getLocalAnchorB());
		jointDef.lowerAngle = joint.getLowerLimit();
		jointDef.upperAngle = joint.getUpperLimit();
		jointDef.referenceAngle = joint.getReferenceAngle();
		return jointDef;
	}

	/** @see #set(JointDef, Joint) */
	public static PrismaticJointDef set(PrismaticJointDef jointDef, PrismaticJoint joint)  {
		set((JointDef) jointDef, joint);
		jointDef.enableLimit = joint.isLimitEnabled();
		jointDef.enableMotor = joint.isMotorEnabled();
		jointDef.maxMotorForce = joint.getMaxMotorForce();
		jointDef.motorSpeed = joint.getMotorSpeed();
		jointDef.localAnchorA.set(joint.getLocalAnchorA());
		jointDef.localAnchorB.set(joint.getLocalAnchorB());
		jointDef.localAxisA.set(joint.getLocalAxisA());
		jointDef.lowerTranslation = joint.getLowerLimit();
		jointDef.upperTranslation = joint.getUpperLimit();
		jointDef.referenceAngle = joint.getReferenceAngle();
		return jointDef;
	}

	/** @see #set(JointDef, Joint) */
	public static PulleyJointDef set(PulleyJointDef jointDef, PulleyJoint joint) {
		set((JointDef) jointDef, joint);
		jointDef.groundAnchorA.set(joint.getGroundAnchorA());
		jointDef.groundAnchorB.set(joint.getGroundAnchorB());
		jointDef.lengthA = joint.getLength1();
		jointDef.lengthB = joint.getLength2();
		jointDef.ratio = joint.getRatio();
		jointDef.localAnchorA.set(joint.getBodyA().getLocalPoint(joint.getAnchorA()));
		jointDef.localAnchorB.set(joint.getBodyB().getLocalPoint(joint.getAnchorB()));
		return jointDef;
	}

	/** @see #set(JointDef, Joint) */
	public static WheelJointDef set(WheelJointDef jointDef, WheelJoint joint) {
		set((JointDef) jointDef, joint);
		jointDef.dampingRatio = joint.getSpringDampingRatio();
		jointDef.frequencyHz = joint.getSpringFrequencyHz();
		jointDef.enableMotor = joint.isMotorEnabled();
		jointDef.maxMotorTorque = joint.getMaxMotorTorque();
		jointDef.motorSpeed = joint.getMotorSpeed();
		jointDef.localAnchorA.set(joint.getLocalAnchorA());
		jointDef.localAnchorB.set(joint.getLocalAnchorB());
		jointDef.localAxisA.set(joint.getLocalAxisA());
		return jointDef;
	}

	/** <strong>Note:</strong> The reference angle cannot be set due to the Box2D API not providing it.
	 *  @see #set(JointDef, Joint) */
	public static WeldJointDef set(WeldJointDef jointDef, WeldJoint joint) {
		set((JointDef) jointDef, joint);
		jointDef.dampingRatio = joint.getDampingRatio();
		jointDef.frequencyHz = joint.getFrequency();
		jointDef.localAnchorA.set(joint.getLocalAnchorA());
		jointDef.localAnchorB.set(joint.getLocalAnchorB());
		// jointDef.referenceAngle = joint.getReferenceAngle();
		return jointDef;
	}

	/** @see #set(JointDef, Joint) */
	public static RopeJointDef set(RopeJointDef jointDef, RopeJoint joint) {
		set((JointDef) jointDef, joint);
		jointDef.localAnchorA.set(joint.getLocalAnchorA());
		jointDef.localAnchorB.set(joint.getLocalAnchorB());
		jointDef.maxLength = joint.getMaxLength();
		return jointDef;
	}

	/** @param body the body for which to setup a new {@link BodyDef}
	 *  @return a new {@link BodyDef} instance that can be used to clone the given body */
	public static BodyDef createDef(Body body) {
		return set(new BodyDef(), body);
	}

	/** @param fixture the fixture for which to setup a new {@link FixtureDef}
	 *  @return a new {@link FixtureDef} instance that can be used to clone the given fixture */
	public static FixtureDef createDef(Fixture fixture) {
		return set(new FixtureDef(), fixture);
	}

	/** @param joint the joint for which to create a new JointDef
	 *  @return a new JointDef instance that can be used to clone the given joint
	 *  @since 0.7.1 */
	public static JointDef createDef(Joint joint) {
		switch(joint.getType()) {
		case RevoluteJoint:
			return createDef((RevoluteJoint) joint);
		case PrismaticJoint:
			return createDef((PrismaticJoint) joint);
		case DistanceJoint:
			return createDef((DistanceJoint) joint);
		case PulleyJoint:
			return createDef((PulleyJoint) joint);
		case MouseJoint:
			return createDef((MouseJoint) joint);
		case GearJoint:
			return createDef((GearJoint) joint);
		case WheelJoint:
			return createDef((WheelJoint) joint);
		case WeldJoint:
			return createDef((WeldJoint) joint);
		case FrictionJoint:
			return createDef((FrictionJoint) joint);
		case RopeJoint:
			return createDef((RopeJoint) joint);
		case MotorJoint:
			return createDef((MotorJoint) joint);
		case Unknown:
			return null; // set(new JointDef(), joint); // GWT backend JointDef is abstract
		}
		return null;
	}

	/** @see #createDef(Joint) */
	public static DistanceJointDef createDef(DistanceJoint joint) {
		return set(new DistanceJointDef(), joint);
	}

	/** @see #createDef(Joint) */
	public static FrictionJointDef createDef(FrictionJoint joint) {
		return set(new FrictionJointDef(), joint);
	}

	/** @see #createDef(Joint) */
	public static GearJointDef createDef(GearJoint joint) {
		return set(new GearJointDef(), joint);
	}

	/** @see #createDef(Joint) */
	public static MotorJointDef createDef(MotorJoint joint) {
		return set(new MotorJointDef(), joint);
	}

	/** @see #createDef(Joint) */
	public static MouseJointDef createDef(MouseJoint joint) {
		return set(new MouseJointDef(), joint);
	}

	/** @see #createDef(Joint) */
	public static RevoluteJointDef createDef(RevoluteJoint joint) {
		return set(new RevoluteJointDef(), joint);
	}

	/** @see #createDef(Joint) */
	public static PrismaticJointDef createDef(PrismaticJoint joint) {
		return set(new PrismaticJointDef(), joint);
	}

	/** @see #createDef(Joint) */
	public static PulleyJointDef createDef(PulleyJoint joint) {
		return set(new PulleyJointDef(), joint);
	}

	/** @see #createDef(Joint) */
	public static WheelJointDef createDef(WheelJoint joint) {
		return set(new WheelJointDef(), joint);
	}

	/** @see #createDef(Joint) */
	public static WeldJointDef createDef(WeldJoint joint) {
		return set(new WeldJointDef(), joint);
	}

	/** @see #createDef(Joint) */
	public static RopeJointDef createDef(RopeJoint joint) {
		return set(new RopeJointDef(), joint);
	}

	// split

	/** @param body the Body to split
	 *  @param a the first point of the segment
	 *  @param b the second point of the segment
	 *  @param store The {@link Pair} to store the resulting bodies in. May be null.
	 *  @return If the body was successfully split, which means that all fixtures intersecting with the given segment were split. If false, only some fixtures may have been created! */
	public static boolean split(Body body, Vector2 a, Vector2 b, Pair<Body, Body> store) {
		World world = body.getWorld();
		BodyDef bodyDef = createDef(body);
		Body aBody = world.createBody(bodyDef), bBody = world.createBody(bodyDef);
		boolean split = false;
		for(Fixture fixture : body.getFixtureList())
			split |= split(fixture, a, b, aBody, bBody, null);
		if(store != null)
			store.clear();
		if(aBody.getFixtureList().size == 0)
			world.destroyBody(aBody);
		else if(store != null)
			store.setKey(aBody);
		if(bBody.getFixtureList().size == 0)
			world.destroyBody(bBody);
		else if(store != null)
			store.setValue(bBody);
		return split;
	}

	/** @param fixture the fixture to split
	 *  @param a the first point of the segment
	 *  @param b the second point of the segment
	 *  @param aBody The body the first resulting fixture will be created on. No fixture will be created if this is null.
	 *  @param bBody The body the second resulting fixture will be created on. No fixture will be created if this is null.
	 *  @param store The {@link Pair} to store the resulting fixtures in. May be null.
	 *  @return if the fixture was split
	 *  @see #split(Shape, Vector2, Vector2, Pair) */
	public static boolean split(Fixture fixture, Vector2 a, Vector2 b, Body aBody, Body bBody, Pair<Fixture, Fixture> store) {
		@SuppressWarnings("unchecked")
		Pair<FixtureDef, FixtureDef> defs = Pools.obtain(Pair.class);
		if(store != null)
			store.clear();
		if(!split(fixture, a, b, defs)) {
			defs.clear();
			Pools.free(defs);
			return false;
		}
		Fixture aFixture = aBody != null && defs.hasKey() ? aBody.createFixture(defs.getKey()) : null, bFixture = bBody != null && defs.hasValue() ? bBody.createFixture(defs.getValue()) : null;
		if(defs.hasKey())
			defs.getKey().shape.dispose();
		if(defs.hasValue())
			defs.getValue().shape.dispose();
		defs.clear();
		Pools.free(defs);
		if(store != null)
			store.set(aFixture, bFixture);
		return aFixture != null || bFixture != null;
	}

	/** @param fixture the fixture to split
	 *  @param a the first point of the segment
	 *  @param b the second point of the segment
	 *  @param store the {@link Pair} to store the resulting {@link FixtureDef FixtureDefs} in
	 *  @return if the fixture was split
	 *  @see #split(Shape, Vector2, Vector2, Pair) */
	public static boolean split(Fixture fixture, Vector2 a, Vector2 b, Pair<FixtureDef, FixtureDef> store) {
		Body body = fixture.getBody();
		Vector2 bodyPos = body.getPosition();
		Vector2 tmpA = Pools.obtain(Vector2.class).set(a).sub(bodyPos), tmpB = Pools.obtain(Vector2.class).set(b).sub(bodyPos);
		GeometryUtils.rotate(tmpA, Vector2.Zero, -body.getAngle());
		GeometryUtils.rotate(tmpB, Vector2.Zero, -body.getAngle());
		@SuppressWarnings("unchecked")
		Pair<Shape, Shape> shapes = Pools.obtain(Pair.class);
		shapes.clear();
		boolean split = split(fixture.getShape(), tmpA, tmpB, shapes);
		Pools.free(tmpA);
		Pools.free(tmpB);
		if(store != null)
			store.clear();
		if(split) {
			if(shapes.hasKey()) {
				FixtureDef def = createDef(fixture);
				def.shape = shapes.getKey();
				if(store != null)
					store.setKey(def);
			}
			if(shapes.hasValue()) {
				FixtureDef def = createDef(fixture);
				def.shape = shapes.getValue();
				if(store != null)
					store.setValue(def);
			}
		}
		shapes.clear();
		Pools.free(shapes);
		return split;
	}

	/** splits the given Shape using the segment described by the two given Vector2s
	 *  @param shape the Shape to split
	 *  @param a The first point of the segment. Will be set to the first intersection with the given shape.
	 *  @param b The second point of the segment. Will be set to the second intersection with the given shape.
	 *  @param store the {@link Pair} to store the split Shapes in
	 *  @return if the given shape was split */
	@SuppressWarnings("unchecked")
	public static <T extends Shape> boolean split(T shape, Vector2 a, Vector2 b, Pair<T, T> store) {
		store.clear();

		Type type = shape.getType();

		if(type == Type.Circle)
			throw new IllegalArgumentException("shapes of the type " + Type.Circle + " cannot be split since Box2D does not support curved shapes other than circles: " + shape);

		if(type == Type.Edge) {
			Vector2 vertex1 = Pools.obtain(Vector2.class), vertex2 = Pools.obtain(Vector2.class), intersection = Pools.obtain(Vector2.class);
			EdgeShape es = (EdgeShape) shape;
			es.getVertex1(vertex1);
			es.getVertex2(vertex2);
			if(!Intersector.intersectSegments(a, b, vertex1, vertex2, intersection)) {
				Pools.free(vertex1);
				Pools.free(vertex2);
				Pools.free(intersection);
				return false;
			}

			EdgeShape sa = new EdgeShape(), sb = new EdgeShape();
			sa.set(vertex1, intersection);
			sb.set(intersection, vertex2);
			store.set((T) sa, (T) sb);

			Pools.free(vertex1);
			Pools.free(vertex2);
			Pools.free(intersection);
			return true;
		}

		FloatArray aVertices = Pools.obtain(FloatArray.class), bVertices = Pools.obtain(FloatArray.class);
		aVertices.clear();
		bVertices.clear();
		float[] vertices = vertices(shape);

		if(type == Type.Polygon) {
			if(GeometryUtils.intersectSegmentConvexPolygon(a.x, a.y, b.x, b.y, vertices, a, b) < 2) {
				aVertices.clear();
				bVertices.clear();
				Pools.free(aVertices);
				Pools.free(bVertices);
				return false;
			}

			aVertices.add(a.x);
			aVertices.add(a.y);
			aVertices.add(b.x);
			aVertices.add(b.y);
			bVertices.add(a.x);
			bVertices.add(a.y);
			bVertices.add(b.x);
			bVertices.add(b.y);

			for(int i = 0; i < vertices.length; i += 2) {
				float x = vertices[i], y = vertices[i + 1];
				float det = MathUtils.det(a.x, a.y, x, y, b.x, b.y);
				if(det < 0) {
					aVertices.add(x);
					aVertices.add(y);
				} else if(det > 0) {
					bVertices.add(x);
					bVertices.add(y);
				} else {
					aVertices.add(x);
					aVertices.add(y);
					bVertices.add(x);
					bVertices.add(y);
				}
			}

			GeometryUtils.arrangeConvexPolygon(aVertices, false);
			GeometryUtils.arrangeConvexPolygon(bVertices, false);

			if(check.isValidPolygonShape(aVertices.items, 0, aVertices.size)) {
				PolygonShape sa = new PolygonShape();
				sa.set(aVertices.items, 0, aVertices.size);
				store.setKey((T) sa);
			}
			if(check.isValidPolygonShape(bVertices.items, 0, bVertices.size)) {
				PolygonShape sb = new PolygonShape();
				sb.set(bVertices.items, 0, bVertices.size);
				store.setValue((T) sb);
			}
		} else if(type == Type.Chain) {
			Vector2 tmp = Pools.obtain(Vector2.class);
			boolean intersected = false;
			for(int i = 1; i < vertices.length; i += 2) {
				float x = vertices[i - 1], y = vertices[i];
				if(!intersected) {
					aVertices.add(x);
					aVertices.add(y);
				} else {
					bVertices.add(x);
					bVertices.add(y);
				}
				if(!intersected && i + 2 < vertices.length && Intersector.intersectSegments(x, y, vertices[i + 1], vertices[i + 2], a.x, a.y, b.x, b.y, tmp)) {
					intersected = true;
					aVertices.add(tmp.x);
					aVertices.add(tmp.y);
					bVertices.add(tmp.x);
					bVertices.add(tmp.y);
				}
			}
			Pools.free(tmp);
			if(intersected) {
				if(check.isValidChainShape(aVertices)) {
					ChainShape cs = new ChainShape();
					cs.createChain(aVertices.toArray());
					store.setKey((T) cs);
				}
				if(check.isValidChainShape(bVertices)) {
					ChainShape cs = new ChainShape();
					cs.createChain(bVertices.toArray());
					store.setValue((T) cs);
				}
			}
		}

		aVertices.clear();
		bVertices.clear();
		Pools.free(aVertices);
		Pools.free(bVertices);

		return !store.isEmpty();
	}

	/** @see #weld(float[], int, int) */
	public static int weld(FloatArray vertices) {
		return vertices.size = weld(vertices.items, 0, vertices.size) * 2;
	}

	/** @see #weld(float[], int, int) */
	public static int weld(float[] vertices) {
		return weld(vertices, 0, vertices.length);
	}

	/** welds the given vertices together the way {@link PolygonShape#set(float[])} does
	 *  @return the new number of vertices (starting at offset) */
	public static int weld(float[] vertices, int offset, int length) {
		ArrayUtils.checkRegion(vertices, offset, length);
		if(length % 2 != 0)
			throw new IllegalArgumentException("malformed vertices, length is odd: " + length);
		if(length < 4) // less than two points, nothing to weld
			return length / 2;
		for(int i = offset; i + 1 < offset + length;) {
			float x1 = vertices[i], y1 = vertices[i + 1], x2 = vertices[ArrayUtils.repeat(offset, length, i + 2)], y2 = vertices[ArrayUtils.repeat(offset, length, i + 3)];
			if(GeometryUtils.distance2(x1, y1, x2, y2) < linearSlop / 2) {
				ArrayUtils.shift(vertices, i + 2, length - i - 2, -2);
				length -= 2;
			} else
				i += 2;
		}
		return length / 2;
	}

	// various

	/** @return whether the two given Transforms {@link Transform#vals values} equal
	 *  @since 0.7.1 */
	public static boolean equals(Transform a, Transform b) {
		return Arrays.equals(a.vals, b.vals);
	}

	/** @return whether the two MassDatas values equal
	 *  @since 0.7.1 */
	public static boolean equals(MassData a, MassData b) {
		return a.center.equals(b.center) && a.mass == b.mass && a.I == b.I;
	}

	/** @return whether the two Filters values equal
	 *  @since 0.7.1 */
	public static boolean equals(Filter a, Filter b) {
		return a.categoryBits == b.categoryBits && a.maskBits == b.maskBits && a.groupIndex == b.groupIndex;
	}

	/** sets the {@link Fixture#isSensor() sensor flags} of all of the given Body's Fixtures
	 *  @param body the {@link Body} which {@link Fixture Fixtures'} sensor flags to set
	 *  @param sensor the parameter to pass to {@link Fixture#setSensor(boolean)}
	 *  @see Fixture#setSensor(boolean) */
	public static void setSensor(Body body, boolean sensor) {
		for(Fixture fixture : body.getFixtureList())
			fixture.setSensor(sensor);
	}

	/** {@link Body#destroyFixture(Fixture) destroys} all fixtures of the given body
	 *  @param body the body which fixtures to destroy */
	public static void destroyFixtures(Body body) {
		Array<Fixture> fixtures = body.getFixtureList();
		while(fixtures.size > 0)
			body.destroyFixture(fixtures.peek());
	}

	/** {@link Body#destroyFixture(Fixture) destroys} all fixtures of the given body except the given ones
	 *  @param exclude the fixtures not to destroy
	 *  @param body the body which fixtures to destroy */
	public static void destroyFixtures(Body body, Array<Fixture> exclude) {
		Array<Fixture> fixtures = body.getFixtureList();
		for(int preserved = 0; preserved < fixtures.size; ) {
			Fixture fixture = fixtures.get(fixtures.size - 1 - preserved);
			if(!exclude.contains(fixture, true))
				body.destroyFixture(fixture);
			else
				preserved++;
		}
	}

	/** @see #destroyFixtures(Body, Array) */
	public static void destroyFixtures(Body body, Fixture... exclude) {
		Array<Fixture> fixtures = body.getFixtureList();
		for(int preserved = 0; preserved < fixtures.size; ) {
			Fixture fixture = fixtures.get(fixtures.size - 1 - preserved);
			if(!ArrayUtils.contains(exclude, fixture, true))
				body.destroyFixture(fixture);
			else
				preserved++;
		}
	}

	/** @see #destroyFixtures(Body, Array) */
	public static void destroyFixtures(Body body, Fixture exclude) {
		Array<Fixture> fixtures = body.getFixtureList();
		for(int preserved = 0; preserved < fixtures.size; ) {
			Fixture fixture = fixtures.get(fixtures.size - 1 - preserved);
			if(fixture != exclude)
				body.destroyFixture(fixture);
			else
				preserved++;
		}
	}

	/** @param bodyDef the BodyDef to reset to default values
	 *  @return the given BodyDef for chaining
	 *  @since 0.9.1 */
	public static BodyDef reset(BodyDef bodyDef) {
		bodyDef.position.setZero();
		bodyDef.type = BodyType.StaticBody;
		bodyDef.angle = 0;
		bodyDef.linearVelocity.setZero();
		bodyDef.angularVelocity = 0;
		bodyDef.linearDamping = 0;
		bodyDef.angularDamping = 0;
		bodyDef.allowSleep = true;
		bodyDef.awake = true;
		bodyDef.fixedRotation = false;
		bodyDef.bullet = false;
		bodyDef.active = true;
		bodyDef.gravityScale = 1;
		return bodyDef;
	}

	/** @param fixtureDef the FixtureDef to reset to default values
	 *  @return the given FixtureDef for chaining
	 *  @since 0.9.1 */
	public static FixtureDef reset(FixtureDef fixtureDef) {
		fixtureDef.shape = null;
		fixtureDef.friction = 0;
		fixtureDef.restitution = 0;
		fixtureDef.density = 0;
		fixtureDef.isSensor = false;
		fixtureDef.filter.categoryBits = 1;
		fixtureDef.filter.maskBits = -1;
		fixtureDef.filter.groupIndex = 0;
		return fixtureDef;
	}

}
