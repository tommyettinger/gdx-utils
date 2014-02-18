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

package net.dermetfan.utils.libgdx.box2d;

import static net.dermetfan.utils.libgdx.math.GeometryUtils.filterX;
import static net.dermetfan.utils.libgdx.math.GeometryUtils.filterY;
import static net.dermetfan.utils.math.MathUtils.amplitude;
import static net.dermetfan.utils.math.MathUtils.max;
import static net.dermetfan.utils.math.MathUtils.min;
import net.dermetfan.utils.libgdx.box2d.Box2DUtils.ShapeCache.ShapeResults;
import net.dermetfan.utils.libgdx.math.GeometryUtils;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.Filter;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.Shape.Type;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ObjectMap;

/** provides methods for operations with Box2D {@link Body Bodies}, {@link Fixture Fixtures} and {@link Shape Shapes}
 *  @author dermetfan */
public abstract class Box2DUtils {

	/** used by {@link Box2DUtils} to cache {@link Shape Shapes}
	 *  @author dermetfan */
	public static class ShapeCache {

		/** cached method results
		 *  @author dermetfan */
		public static class ShapeResults {

			/** @see Box2DUtils#vertices0(Shape) */
			public final Vector2[] vertices;

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
			public ShapeResults(Vector2[] vertices, float width, float height, float minX, float maxX, float minY, float maxY) {
				this.vertices = vertices;
				this.width = width;
				this.height = height;
				this.minX = minX;
				this.maxX = maxX;
				this.minY = minY;
				this.maxY = maxY;
			}

		}

		/** Cached {@link Shape Shapes} and their {@link ShapeResults}. You should {@link ObjectMap#clear() clear} this when you don't use the shapes anymore. */
		private final ObjectMap<Shape, ShapeResults> cache = new ObjectMap<Shape, ShapeResults>();

		/** this Shapes should automatically be cached by {@link Box2DUtils} using this {@link ShapeCache} */
		private boolean auto = true;

		/** @return if the given Shape is already cached */
		public boolean contains(Shape shape) {
			return cache.containsKey(shape);
		}

		/** @param shape the Shape to create a new {@link ShapeResults} for that will be added to {@link #cache} */
		public ShapeResults cache(Shape shape) {
			if(contains(shape))
				return getResults(shape);
			Vector2[] vertices = vertices0(shape), cachedVertices = new Vector2[vertices.length];
			System.arraycopy(vertices, 0, cachedVertices, 0, vertices.length);
			ShapeResults results = new ShapeResults(cachedVertices, width0(shape), height0(shape), minX0(shape), maxX0(shape), minY0(shape), maxY0(shape));
			cache.put(shape, results);
			return results;
		}

		/** @param shape the Shape to {@link ObjectMap#remove(Object) remove} from {@link #cache}
		 *  @return the {@link ShapeResults} of the given Shape */
		public ShapeResults uncache(Shape shape) {
			return cache.remove(shape);
		}

		/** @return the {@link ShapeResults} of the given Shape */
		public ShapeResults getResults(Shape shape) {
			return cache.get(shape);
		}

		/** {@link ObjectMap#clear() clears} the {@link #cache} */
		public void clear() {
			cache.clear();
		}

	}

	/** used to cache {@link ShapeResults} */
	private static ShapeCache cache = new ShapeCache();

	/** for internal, temporary usage */
	private static final Vector2 vec2_0 = new Vector2(), vec2_1 = new Vector2();

	// shape

	/** @param shape the Shape which vertices to get
	 *  @return the vertices of the bounding box of the given Shape */
	private static Vector2[] vertices0(Shape shape) {
		Vector2[] vertices = null;
		switch(shape.getType()) {
		case Polygon:
			PolygonShape polygonShape = (PolygonShape) shape;
			vertices = new Vector2[polygonShape.getVertexCount()];
			for(int i = 0; i < vertices.length; i++) {
				vertices[i] = new Vector2();
				polygonShape.getVertex(i, vertices[i]);
			}
			break;
		case Edge:
			EdgeShape edgeShape = (EdgeShape) shape;
			edgeShape.getVertex1(vec2_0);
			edgeShape.getVertex2(vec2_1);
			vertices = new Vector2[] {vec2_0, vec2_1};
			break;
		case Chain:
			ChainShape chainShape = (ChainShape) shape;
			vertices = new Vector2[chainShape.getVertexCount()];
			for(int i = 0; i < vertices.length; i++) {
				vertices[i] = new Vector2();
				chainShape.getVertex(i, vertices[i]);
			}
			break;
		case Circle:
			CircleShape circleShape = (CircleShape) shape;
			vertices = new Vector2[4];
			vec2_0.set(circleShape.getPosition().x - circleShape.getRadius(), circleShape.getPosition().y + circleShape.getRadius()); // top left
			vertices[0] = vertices[0] != null ? vertices[0].set(vec2_0) : new Vector2(vec2_0);
			vec2_0.set(circleShape.getPosition().x - circleShape.getRadius(), circleShape.getPosition().y - circleShape.getRadius()); // bottom left
			vertices[1] = vertices[1] != null ? vertices[1].set(vec2_0) : new Vector2(vec2_0);
			vec2_0.set(circleShape.getPosition().x + circleShape.getRadius(), circleShape.getPosition().y - circleShape.getRadius()); // bottom right
			vertices[2] = vertices[2] != null ? vertices[2].set(vec2_0) : new Vector2(vec2_0);
			vec2_0.set(circleShape.getPosition().x + circleShape.getRadius(), circleShape.getPosition().y + circleShape.getRadius()); // top right
			vertices[3] = vertices[3] != null ? vertices[3].set(vec2_0) : new Vector2(vec2_0);
			break;
		default:
			throw new IllegalArgumentException("shapes of the type '" + shape.getType().name() + "' are not supported");
		}
		return vertices;
	}

	/** @return the minimal x of the vertices of the given Shape */
	private static float minX0(Shape shape) {
		return min(filterX(vertices0(shape)));
	}

	/** @return the minimal y of the vertices of the given Shape */
	private static float minY0(Shape shape) {
		return min(filterY(vertices0(shape)));
	}

	/** @return the maximal x of the vertices of the given Shape */
	private static float maxX0(Shape shape) {
		return max(filterX(vertices0(shape)));
	}

	/** @return the maximal y of the vertices of the given Shape */
	private static float maxY0(Shape shape) {
		return max(filterY(vertices0(shape)));
	}

	/** @return the width of the given Shape */
	private static float width0(Shape shape) {
		return amplitude(filterX(vertices0(shape)));
	}

	/** @return the height of the given Shape */
	private static float height0(Shape shape) {
		return amplitude(filterY(vertices0(shape)));
	}

	/** @return the size of the given Shape */
	private static Vector2 size0(Shape shape) {
		if(shape.getType() == Type.Circle) // skip #width0(Shape) and #height0(Shape) for performance
			return vec2_0.set(shape.getRadius() * 2, shape.getRadius() * 2);
		return vec2_0.set(width0(shape), height0(shape));
	}

	// cache

	public static Vector2[] vertices(Shape shape) {
		if(cache != null) {
			if(cache.contains(shape))
				return cache.getResults(shape).vertices;
			if(cache.auto)
				return cache.cache(shape).vertices;
		}
		return vertices0(shape);
	}

	public static float minX(Shape shape) {
		if(cache != null) {
			if(cache.contains(shape))
				return cache.getResults(shape).minX;
			if(cache.auto)
				return cache.cache(shape).minX;
		}
		return minX0(shape);
	}

	public static float minY(Shape shape) {
		if(cache != null) {
			if(cache.contains(shape))
				return cache.getResults(shape).minY;
			if(cache.auto)
				return cache.cache(shape).minY;
		}
		return minY0(shape);
	}

	public static float maxX(Shape shape) {
		if(cache != null) {
			if(cache.contains(shape))
				return cache.getResults(shape).maxX;
			if(cache.auto)
				return cache.cache(shape).maxX;
		}
		return maxX0(shape);
	}

	public static float maxY(Shape shape) {
		if(cache != null) {
			if(cache.contains(shape))
				return cache.getResults(shape).maxY;
			if(cache.auto)
				return cache.cache(shape).maxY;
		}
		return maxY0(shape);
	}

	public static float width(Shape shape) {
		if(cache != null) {
			if(cache.contains(shape))
				return cache.getResults(shape).width;
			if(cache.auto)
				return cache.cache(shape).width;
		}
		return width0(shape);
	}

	public static float height(Shape shape) {
		if(cache != null) {
			if(cache.contains(shape))
				return cache.getResults(shape).height;
			if(cache.auto)
				return cache.cache(shape).height;
		}
		return height0(shape);
	}

	public static Vector2 size(Shape shape) {
		if(cache != null) {
			if(cache.contains(shape)) {
				ShapeResults results = cache.getResults(shape);
				return vec2_0.set(results.width, results.height);
			}
			if(cache.auto) {
				ShapeResults results = cache.cache(shape);
				return vec2_0.set(results.width, results.height);
			}
		}
		return size0(shape);
	}

	// fixture

	public static Vector2[] vertices(Fixture fixture) {
		return vertices(fixture.getShape());
	}

	public static float minX(Fixture fixture) {
		return minX(fixture.getShape());
	}

	public static float minY(Fixture fixture) {
		return minY(fixture.getShape());
	}

	public static float maxX(Fixture fixture) {
		return maxX(fixture.getShape());
	}

	public static float maxY(Fixture fixture) {
		return maxY(fixture.getShape());
	}

	public static float width(Fixture fixture) {
		return width(fixture.getShape());
	}

	public static float height(Fixture fixture) {
		return height(fixture.getShape());
	}

	public static Vector2 size(Fixture fixture) {
		return size(fixture.getShape());
	}

	// body

	public static Vector2[][] fixtureVertices(Body body) {
		Array<Fixture> fixtures = body.getFixtureList();
		Vector2[][] vertices = new Vector2[fixtures.size][];
		for(int i = 0; i < vertices.length; i++)
			vertices[i] = vertices(fixtures.get(i));
		return vertices;
	}

	public static Vector2[] vertices(Body body) {
		Vector2[][] fixtureVertices = fixtureVertices(body);

		int vertexCount = 0;
		for(int i = 0; i < fixtureVertices.length; i++)
			vertexCount += fixtureVertices[i].length;

		Vector2[] vertices = new Vector2[vertexCount];
		int vi = -1;
		for(Vector2[] verts : fixtureVertices)
			for(Vector2 vertice : verts)
				vertices[++vi] = vertice;

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

	public static Vector2 positionRelative(CircleShape shape) {
		return shape.getPosition();
	}

	public static Vector2 positionRelative(Shape shape, float rotation) {
		return vec2_0.set(minX(shape) + width(shape) / 2, minY(shape) + height(shape) / 2).rotate(rotation);
	}

	/** @return the position of the given Shape in world coordinates
	 *  @param shape the Shape which position to get
	 *  @param body the Body the given Shape is attached to */
	public static Vector2 position(Shape shape, Body body) {
		return body.getPosition().add(positionRelative(shape, body.getAngle() * com.badlogic.gdx.math.MathUtils.radDeg));
	}

	public static Vector2 positionRelative(Fixture fixture) {
		return positionRelative(fixture.getShape(), fixture.getBody().getAngle() * com.badlogic.gdx.math.MathUtils.radDeg);
	}

	public static Vector2 position(Fixture fixture) {
		return position(fixture.getShape(), fixture.getBody());
	}

	// copy

	/** creates a deep copy of a {@link Body} (without deep copying the {@link Shape Shapes} of its {@link Fixture Fixtures})<br/>
	 *  @return {@link #copy(Body, boolean) copy(body, false)}
	 *  @see #copy(Body, boolean) */
	public static Body copy(Body body) {
		return copy(body, false);
	}

	/** creates a deep copy of a {@link Body}
	 *  @param body the {@link Body} to copy
	 *  @param shapes if the {@link Shape Shapes} of the {@link Fixture Fixures} of the given {@code body} should be {@link #copy(Shape) copied} as well
	 *  @return a deep copy of the given {@code body} */
	public static Body copy(Body body, boolean shapes) {
		BodyDef bodyDef = new BodyDef();
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
		Body copy = body.getWorld().createBody(bodyDef);
		copy.setUserData(body.getUserData());
		for(Fixture fixture : body.getFixtureList())
			copy(fixture, copy, shapes);
		return copy;
	}

	/** creates a deep copy of a {@link Fixture} (without deep copying its {@link Shape})
	 *  @return {@link #copy(Fixture, Body, boolean) copy(fixture, body, false)}
	 *  @see #copy(Fixture, Body, boolean) */
	public static Fixture copy(Fixture fixture, Body body) {
		return copy(fixture, body, false);
	}

	/** creates a deep copy of a {@link Fixture}
	 *  @param fixture the {@link Fixture} to copy
	 *  @param body the {@link Body} to create a copy of the given {@code fixture} on
	 *  @param shape if the {@link Fixture#getShape() shape} of the given {@code fixture} should be deep {@link #copy(Shape) copied} as well
	 *  @return the copied {@link Fixture} */
	public static Fixture copy(Fixture fixture, Body body, boolean shape) {
		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.density = fixture.getDensity();
		Filter filter = fixture.getFilterData();
		fixtureDef.filter.categoryBits = filter.categoryBits;
		fixtureDef.filter.groupIndex = filter.groupIndex;
		fixtureDef.filter.maskBits = filter.maskBits;
		fixtureDef.friction = fixture.getFriction();
		fixtureDef.isSensor = fixture.isSensor();
		fixtureDef.restitution = fixture.getRestitution();
		fixtureDef.shape = shape ? copy(fixture.getShape()) : fixture.getShape();
		Fixture copy = body.createFixture(fixtureDef);
		copy.setUserData(copy.getUserData());
		return copy;
	}

	/** creates a deep copy of a {@link Shape}<br/>
	 *  <strong>Note: The {@link ChainShape#setPrevVertex(float, float) previous} and {@link ChainShape#setNextVertex(float, float) next} vertex of a {@link ChainShape} will not be copied since this is not possible due to the API.</strong>
	 *  @param shape the {@link Shape} to copy
	 *  @return a {@link Shape} exactly like the one passed in */
	public static Shape copy(Shape shape) {
		Shape copy;
		switch(shape.getType()) {
		case Circle:
			CircleShape circleCopy = (CircleShape) (copy = new CircleShape());
			circleCopy.setPosition(((CircleShape) shape).getPosition());
			break;
		case Polygon:
			PolygonShape polyCopy = (PolygonShape) (copy = new PolygonShape()),
			poly = (PolygonShape) shape;
			float[] vertices = new float[poly.getVertexCount()];
			for(int i = 0; i < vertices.length; i++) {
				poly.getVertex(i, vec2_0);
				vertices[i++] = vec2_0.x;
				vertices[i] = vec2_0.y;
			}
			polyCopy.set(vertices);
			break;
		case Edge:
			EdgeShape edgeCopy = (EdgeShape) (copy = new EdgeShape()),
			edge = (EdgeShape) shape;
			edge.getVertex1(vec2_0);
			edge.getVertex2(vec2_1);
			edgeCopy.set(vec2_0, vec2_1);
			break;
		case Chain:
			ChainShape chainCopy = (ChainShape) (copy = new ChainShape()),
			chain = (ChainShape) shape;
			vertices = new float[chain.getVertexCount()];
			for(int i = 0; i < vertices.length; i++) {
				chain.getVertex(i, vec2_0);
				vertices[i++] = vec2_0.x;
				vertices[i] = vec2_0.y;
			}
			if(chain.isLooped())
				chainCopy.createLoop(GeometryUtils.toVector2Array(vertices));
			else
				chainCopy.createChain(vertices);
			break;
		default:
			return shape;
		}
		copy.setRadius(shape.getRadius());
		return copy;
	}

	/* This method is not implemented because the Box2D API does not offer all the necessary information.
	public static Joint copy(Joint joint, Body bodyA, Body bodyB) {
		JointDef jointDef;
		Joint copy;
		switch(joint.getType()) {
		case Unknown:
			jointDef = new JointDef();
			break;
		case RevoluteJoint:
			RevoluteJoint revoluteJoint = (RevoluteJoint) joint;
			RevoluteJointDef revoluteJointDef = (RevoluteJointDef) (jointDef = new RevoluteJointDef());
			revoluteJointDef.collideConnected = revoluteJointDef.collideConnected; // TODO
			revoluteJointDef.enableLimit = revoluteJoint.isLimitEnabled();
			revoluteJointDef.enableMotor = revoluteJoint.isMotorEnabled();
			revoluteJointDef.localAnchorA.set(revoluteJoint.getAnchorA());
			revoluteJointDef.localAnchorB.set(revoluteJoint.getAnchorB());
			revoluteJointDef.lowerAngle = revoluteJoint.getLowerLimit();
			revoluteJointDef.maxMotorTorque = revoluteJoint.getMaxMotorTorque();
			revoluteJointDef.motorSpeed = revoluteJoint.getMotorSpeed();
			revoluteJointDef.referenceAngle = revoluteJoint.getReferenceAngle();
			revoluteJointDef.upperAngle = revoluteJoint.getUpperLimit();
			break;
		case PrismaticJoint:
			PrismaticJoint prismaticJoint = (PrismaticJoint) joint;
			PrismaticJointDef prismaticJointDef = (PrismaticJointDef) (jointDef = new PrismaticJointDef());
			prismaticJointDef.collideConnected = prismaticJointDef.collideConnected; // TODO
			prismaticJointDef.enableLimit = prismaticJoint.isLimitEnabled();
			prismaticJointDef.enableMotor = prismaticJoint.isMotorEnabled();
			prismaticJointDef.localAnchorA.set(prismaticJoint.getAnchorA());
			prismaticJointDef.localAnchorB.set(prismaticJoint.getAnchorB());
			prismaticJointDef.localAxisA.set(prismaticJointDef.localAxisA); // TODO
			prismaticJointDef.lowerTranslation = prismaticJointDef.lowerTranslation; // TODO
			prismaticJointDef.maxMotorForce = prismaticJointDef.maxMotorForce; // TODO
			prismaticJointDef.motorSpeed = prismaticJoint.getMotorSpeed();
			prismaticJointDef.referenceAngle = prismaticJointDef.referenceAngle; // TODO
			prismaticJointDef.upperTranslation = prismaticJointDef.upperTranslation; // TODO
			break;
		case DistanceJoint:
			DistanceJoint distanceJoint = (DistanceJoint) joint;
			DistanceJointDef distanceJointDef = (DistanceJointDef) (jointDef = new DistanceJointDef());
			distanceJointDef.collideConnected = distanceJointDef.collideConnected; // TODO
			distanceJointDef.dampingRatio = distanceJoint.getDampingRatio();
			distanceJointDef.frequencyHz = distanceJoint.getFrequency();
			distanceJointDef.length = distanceJoint.getLength();
			distanceJointDef.localAnchorA.set(distanceJoint.getAnchorA());
			distanceJointDef.localAnchorB.set(distanceJoint.getAnchorB());
			break;
		case PulleyJoint:
			PulleyJoint pulleyJoint = (PulleyJoint) joint;
			PulleyJointDef pulleyJointDef = (PulleyJointDef) (jointDef = new PulleyJointDef());
			pulleyJointDef.collideConnected = pulleyJointDef.collideConnected; // TODO
			pulleyJointDef.groundAnchorA.set(pulleyJoint.getGroundAnchorA());
			pulleyJointDef.groundAnchorB.set(pulleyJoint.getGroundAnchorB());
			pulleyJointDef.lengthA = pulleyJoint.getLength1();
			pulleyJointDef.lengthB = pulleyJoint.getLength2();
			pulleyJointDef.localAnchorA.set(pulleyJoint.getAnchorA());
			pulleyJointDef.localAnchorB.set(pulleyJoint.getAnchorB());
			pulleyJointDef.ratio = pulleyJoint.getRatio();
			break;
		case MouseJoint:
			MouseJoint mouseJoint = (MouseJoint) joint;
			MouseJointDef mouseJointDef = (MouseJointDef) (jointDef = new MouseJointDef());
			mouseJointDef.collideConnected = mouseJointDef.collideConnected; // TODO
			mouseJointDef.dampingRatio = mouseJoint.getDampingRatio();
			mouseJointDef.frequencyHz = mouseJoint.getFrequency();
			mouseJointDef.maxForce = mouseJoint.getMaxForce();
			mouseJointDef.target.set(mouseJoint.getTarget());
			break;
		case GearJoint:
			GearJoint gearJoint = (GearJoint) joint;
			GearJointDef gearJointDef = (GearJointDef) (jointDef = new GearJointDef());
			gearJointDef.collideConnected = gearJointDef.collideConnected; // TODO
			gearJointDef.joint1 = gearJointDef.joint1; // TODO
			gearJointDef.joint2 = gearJointDef.joint2; // TODO
			gearJointDef.ratio = gearJoint.getRatio();
			break;
		case WheelJoint:
			WheelJoint wheelJoint = (WheelJoint) joint;
			WheelJointDef wheelJointDef = (WheelJointDef) (jointDef = new WheelJointDef());
			wheelJointDef.collideConnected = wheelJointDef.collideConnected; // TODO
			wheelJointDef.dampingRatio = wheelJoint.getSpringDampingRatio();
			wheelJointDef.enableMotor = wheelJointDef.enableMotor; // TODO
			wheelJointDef.frequencyHz = wheelJoint.getSpringFrequencyHz();
			wheelJointDef.localAnchorA.set(wheelJoint.getAnchorA());
			wheelJointDef.localAnchorB.set(wheelJoint.getAnchorB());
			wheelJointDef.localAxisA.set(wheelJointDef.localAxisA); // TODO
			wheelJointDef.maxMotorTorque = wheelJoint.getMaxMotorTorque();
			wheelJointDef.motorSpeed = wheelJoint.getMotorSpeed();
			break;
		case WeldJoint:
			WeldJoint weldJoint = (WeldJoint) joint;
			WeldJointDef weldJointDef = (WeldJointDef) (jointDef = new WeldJointDef());
			weldJointDef.collideConnected = weldJointDef.collideConnected; // TODO
			weldJointDef.localAnchorA.set(weldJoint.getAnchorA());
			weldJointDef.localAnchorB.set(weldJoint.getAnchorB());
			weldJointDef.referenceAngle = weldJoint.getReferenceAngle();
			break;
		case FrictionJoint:
			FrictionJoint frictionJoint = (FrictionJoint) joint;
			FrictionJointDef frictionJointDef = (FrictionJointDef) (jointDef = new FrictionJointDef());
			frictionJointDef.collideConnected = frictionJointDef.collideConnected; // TODO
			frictionJointDef.localAnchorA.set(frictionJoint.getAnchorA());
			frictionJointDef.localAnchorB.set(frictionJoint.getAnchorB());
			frictionJointDef.maxForce = frictionJoint.getMaxForce();
			frictionJointDef.maxTorque = frictionJoint.getMaxTorque();
			break;
		case RopeJoint:
			RopeJoint ropeJoint = (RopeJoint) joint;
			RopeJointDef ropeJointDef = (RopeJointDef) (jointDef = new RopeJointDef());
			ropeJointDef.localAnchorA.set(ropeJoint.getAnchorA());
			ropeJointDef.localAnchorB.set(ropeJoint.getAnchorB());
			ropeJointDef.maxLength = ropeJoint.getMaxLength();
			break;
		default:
			return joint;
		}
		jointDef.type = joint.getType();
		jointDef.bodyA = bodyA;
		jointDef.bodyB = bodyB;
		copy = bodyA.getWorld().createJoint(jointDef);
		copy.setUserData(joint.getUserData());
		return copy;
	} */

	/** @return the {@link #cache} */
	public static ShapeCache getCache() {
		return cache;
	}

	/** @param cache the {@link #cache} to set */
	public static void setCache(ShapeCache cache) {
		Box2DUtils.cache = cache;
	}

}
