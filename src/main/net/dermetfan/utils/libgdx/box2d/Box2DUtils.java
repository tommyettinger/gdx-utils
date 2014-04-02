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
import net.dermetfan.utils.ArrayUtils;
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

	/** cached method results
	 *  @author dermetfan */
	public static class ShapeCache {

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
		public ShapeCache(Vector2[] vertices, float width, float height, float minX, float maxX, float minY, float maxY) {
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
	public static final ObjectMap<Shape, ShapeCache> cache = new ObjectMap<Shape, ShapeCache>();

	/** if shapes should automatically be cached when they are inspected for the first time */
	public static boolean autoCache = true;

	/** for internal, temporary usage */
	private static final Vector2 vec2_0 = new Vector2(), vec2_1 = new Vector2();

	/** @param shape the Shape to create a new {@link ShapeCache} for that will be added to {@link #cache} */
	public static ShapeCache cache(Shape shape) {
		if(cache.containsKey(shape))
			return cache.get(shape);
		Vector2[] vertices = vertices0(shape), cachedVertices = new Vector2[vertices.length];
		System.arraycopy(vertices, 0, cachedVertices, 0, vertices.length);
		ShapeCache results = new ShapeCache(cachedVertices, width0(shape), height0(shape), minX0(shape), maxX0(shape), minY0(shape), maxY0(shape));
		cache.put(shape, results);
		return results;
	}

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
			vertices = new Vector2[] {new Vector2(vec2_0), new Vector2(vec2_1)};
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

	/** @return a Vector2 representing the size of the given Shape */
	private static Vector2 size0(Shape shape) {
		if(shape.getType() == Type.Circle) // skip #width0(Shape) and #height0(Shape) for performance
			return vec2_0.set(shape.getRadius() * 2, shape.getRadius() * 2);
		return vec2_0.set(width0(shape), height0(shape));
	}

	// cache

	/** @return the vertices of the given Shape */
	public static Vector2[] vertices(Shape shape) {
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
	public static Vector2[] vertices(Fixture fixture) {
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
	public static Vector2[][] fixtureVertices(Body body) {
		Array<Fixture> fixtures = body.getFixtureList();
		Vector2[][] vertices = new Vector2[fixtures.size][];
		for(int i = 0; i < vertices.length; i++)
			vertices[i] = vertices(fixtures.get(i));
		return vertices;
	}

	/** @return the vertices of a body's fixtures */
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

	/** @see #positionRelative(Shape, float)
	 *  @see CircleShape#getPosition() */
	public static Vector2 positionRelative(CircleShape shape) {
		return shape.getPosition();
	}

	/** @return the position of the given shape relativley to its Body */
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

	// clone

	/** clones a {@link Body} (without deep copying the {@link Shape Shapes} of its {@link Fixture Fixtures})<br>
	 *  @return {@link #clone(Body, boolean) copy(body, false)}
	 *  @see #clone(Body, boolean) */
	public static Body clone(Body body) {
		return clone(body, false);
	}

	/** clones a {@link Body}
	 *  @param body the {@link Body} to copy
	 *  @param shapes if the {@link Shape Shapes} of the {@link Fixture Fixures} of the given {@code body} should be {@link #clone(Shape) copied} as well
	 *  @return a deep copy of the given {@code body} */
	public static Body clone(Body body, boolean shapes) {
		Body clone = body.getWorld().createBody(createDef(body));
		clone.setUserData(body.getUserData());
		for(Fixture fixture : body.getFixtureList())
			clone(fixture, clone, shapes);
		return clone;
	}

	/** clones a {@link Fixture} (without deep copying its {@link Shape})
	 *  @return {@link #clone(Fixture, Body, boolean) copy(fixture, body, false)}
	 *  @see #clone(Fixture, Body, boolean) */
	public static Fixture clone(Fixture fixture, Body body) {
		return clone(fixture, body, false);
	}

	/** clones a {@link Fixture}
	 *  @param fixture the {@link Fixture} to copy
	 *  @param body the {@link Body} to create a copy of the given {@code fixture} on
	 *  @param shape if the {@link Fixture#getShape() shape} of the given {@code fixture} should be deep {@link #clone(Shape) copied} as well
	 *  @return the copied {@link Fixture} */
	public static Fixture clone(Fixture fixture, Body body, boolean shape) {
		FixtureDef fixtureDef = createDef(fixture);
		if(shape)
			fixtureDef.shape = clone(fixture.getShape());
		Fixture clone = body.createFixture(fixtureDef);
		clone.setUserData(clone.getUserData());
		return clone;
	}

	/** creates a deep copy of a {@link Shape}<br>
	 *  <strong>Note: The {@link ChainShape#setPrevVertex(float, float) previous} and {@link ChainShape#setNextVertex(float, float) next} vertex of a {@link ChainShape} will not be copied since this is not possible due to the API.</strong>
	 *  @param shape the {@link Shape} to copy
	 *  @return a {@link Shape} exactly like the one passed in */
	@SuppressWarnings("unchecked")
	public static <T extends Shape> T clone(T shape) {
		T clone;
		switch(shape.getType()) {
		case Circle:
			CircleShape circleClone = (CircleShape) (clone = (T) new CircleShape());
			circleClone.setPosition(((CircleShape) shape).getPosition());
			break;
		case Polygon:
			PolygonShape polyClone = (PolygonShape) (clone = (T) new PolygonShape()),
			poly = (PolygonShape) shape;
			float[] vertices = new float[poly.getVertexCount()];
			for(int i = 0; i < vertices.length; i++) {
				poly.getVertex(i, vec2_0);
				vertices[i++] = vec2_0.x;
				vertices[i] = vec2_0.y;
			}
			polyClone.set(vertices);
			break;
		case Edge:
			EdgeShape edgeClone = (EdgeShape) (clone = (T) new EdgeShape()),
			edge = (EdgeShape) shape;
			edge.getVertex1(vec2_0);
			edge.getVertex2(vec2_1);
			edgeClone.set(vec2_0, vec2_1);
			break;
		case Chain:
			ChainShape chainClone = (ChainShape) (clone = (T) new ChainShape()),
			chain = (ChainShape) shape;
			vertices = new float[chain.getVertexCount()];
			for(int i = 0; i < vertices.length; i++) {
				chain.getVertex(i, vec2_0);
				vertices[i++] = vec2_0.x;
				vertices[i] = vec2_0.y;
			}
			if(chain.isLooped())
				chainClone.createLoop(GeometryUtils.toVector2Array(vertices));
			else
				chainClone.createChain(vertices);
			break;
		default:
			return shape;
		}
		clone.setRadius(shape.getRadius());
		return clone;
	}

	/* Not implemented because the Box2D API does not provide all necessary information.
	public static Joint clone(Joint joint, Body bodyA, Body bodyB) {
		JointDef jointDef;
		Joint copy;
		switch(joint.getType()) {
		case Unknown:
			jointDef = new JointDef();
			break;
		case RevoluteJoint:
			RevoluteJoint revoluteJoint = (RevoluteJoint) joint;
			RevoluteJointDef revoluteJointDef = (RevoluteJointDef) (jointDef = new RevoluteJointDef());
			revoluteJointDef.collideConnected = revoluteJoint.isCollideConnected(); // missing
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
			prismaticJointDef.collideConnected = prismaticJoint.isCollideConnected(); // missing
			prismaticJointDef.enableLimit = prismaticJoint.isLimitEnabled();
			prismaticJointDef.enableMotor = prismaticJoint.isMotorEnabled();
			prismaticJointDef.localAnchorA.set(prismaticJoint.getAnchorA());
			prismaticJointDef.localAnchorB.set(prismaticJoint.getAnchorB());
			prismaticJointDef.localAxisA.set(prismaticJoint.getLocalAxisA()); // missing
			prismaticJointDef.lowerTranslation = prismaticJoint.getLowerTranslation(); // missing
			prismaticJointDef.maxMotorForce = prismaticJoint.getMaxMotorForce(); // missing
			prismaticJointDef.motorSpeed = prismaticJoint.getMotorSpeed();
			prismaticJointDef.referenceAngle = prismaticJoint.getReferenceAngle(); // missing
			prismaticJointDef.upperTranslation = prismaticJoint.getUpperTranslation(); // missing
			break;
		case DistanceJoint:
			DistanceJoint distanceJoint = (DistanceJoint) joint;
			DistanceJointDef distanceJointDef = (DistanceJointDef) (jointDef = new DistanceJointDef());
			distanceJointDef.collideConnected = distanceJoint.isCollideConnected(); // missing
			distanceJointDef.dampingRatio = distanceJoint.getDampingRatio();
			distanceJointDef.frequencyHz = distanceJoint.getFrequency();
			distanceJointDef.length = distanceJoint.getLength();
			distanceJointDef.localAnchorA.set(distanceJoint.getAnchorA());
			distanceJointDef.localAnchorB.set(distanceJoint.getAnchorB());
			break;
		case PulleyJoint:
			PulleyJoint pulleyJoint = (PulleyJoint) joint;
			PulleyJointDef pulleyJointDef = (PulleyJointDef) (jointDef = new PulleyJointDef());
			pulleyJointDef.collideConnected = pulleyJoint.isCollideConnected(); // missing
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
			mouseJointDef.collideConnected = mouseJoint.isCollideConnected(); // missing
			mouseJointDef.dampingRatio = mouseJoint.getDampingRatio();
			mouseJointDef.frequencyHz = mouseJoint.getFrequency();
			mouseJointDef.maxForce = mouseJoint.getMaxForce();
			mouseJointDef.target.set(mouseJoint.getTarget());
			break;
		case GearJoint:
			GearJoint gearJoint = (GearJoint) joint;
			GearJointDef gearJointDef = (GearJointDef) (jointDef = new GearJointDef());
			gearJointDef.collideConnected = gearJoint.isCollideConnected(); // missing
			gearJointDef.joint1 = gearJoint.getJoint1(); // missing
			gearJointDef.joint2 = gearJoint.getJoint2(); // missing
			gearJointDef.ratio = gearJoint.getRatio();
			break;
		case WheelJoint:
			WheelJoint wheelJoint = (WheelJoint) joint;
			WheelJointDef wheelJointDef = (WheelJointDef) (jointDef = new WheelJointDef());
			wheelJointDef.collideConnected = wheelJoint.isCollideConnected(); // missing
			wheelJointDef.dampingRatio = wheelJoint.getSpringDampingRatio();
			wheelJointDef.enableMotor = wheelJoint.isMotorEnabled(); // missing
			wheelJointDef.frequencyHz = wheelJoint.getSpringFrequencyHz();
			wheelJointDef.localAnchorA.set(wheelJoint.getAnchorA());
			wheelJointDef.localAnchorB.set(wheelJoint.getAnchorB());
			wheelJointDef.localAxisA.set(wheelJoint.getLocalAxisA()); // missing
			wheelJointDef.maxMotorTorque = wheelJoint.getMaxMotorTorque();
			wheelJointDef.motorSpeed = wheelJoint.getMotorSpeed();
			break;
		case WeldJoint:
			WeldJoint weldJoint = (WeldJoint) joint;
			WeldJointDef weldJointDef = (WeldJointDef) (jointDef = new WeldJointDef());
			weldJointDef.collideConnected = weldJoint.isCollideConnected(); // missing
			weldJointDef.localAnchorA.set(weldJoint.getAnchorA());
			weldJointDef.localAnchorB.set(weldJoint.getAnchorB());
			weldJointDef.referenceAngle = weldJoint.getReferenceAngle();
			break;
		case FrictionJoint:
			FrictionJoint frictionJoint = (FrictionJoint) joint;
			FrictionJointDef frictionJointDef = (FrictionJointDef) (jointDef = new FrictionJointDef());
			frictionJointDef.collideConnected = frictionJointDef.isCollideConnected(); // missing
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

	// createDef

	/** @param body the body for which to setup a new {@link BodyDef}
	 *  @return a new {@link BodyDef} instance that can be used to clone the given body */
	public static BodyDef createDef(Body body) {
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
		return bodyDef;
	}

	/** @param fixture the fixture for which to setup a new {@link FixtureDef}
	 *  @return a new {@link FixtureDef} instance that can be used to clone the given fixture */
	public static FixtureDef createDef(Fixture fixture) {
		FixtureDef fixtureDef = new FixtureDef();
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

	/* Not implemented because the Box2D API does not provide all necessary information.
	 * public static JointDef createDef(Joint joint) {
	 * 	return null;
	 * } */

	// split

	//	@SuppressWarnings("unchecked")
	//	public static boolean split(Fixture fixture, Vector2 a, Vector2 b, Body aBody, Body bBody, Pair<Fixture, Fixture> store) {
	//		Pair<FixtureDef, FixtureDef> defs = Pools.obtain(Pair.class);
	//		if(!split(fixture, a, b, defs)) {
	//			Pools.free(defs);
	//			return false;
	//		}
	//		store.set(aBody.createFixture(defs.key()), bBody.createFixture(defs.value()));
	//		return true;
	//	}
	//
	//	@SuppressWarnings("unchecked")
	//	public static boolean split(Fixture fixture, Vector2 a, Vector2 b, Pair<FixtureDef, FixtureDef> store) { // TODO use actual intersections
	//		Vector2 bodyPos = fixture.getBody().getPosition();
	//		Vector2 tmpA = Pools.obtain(Vector2.class).set(a).sub(bodyPos), tmpB = Pools.obtain(Vector2.class).set(b).sub(bodyPos);
	//		Pair<Shape, Shape> shapes = Pools.obtain(Pair.class);
	//		boolean split = split(fixture.getShape(), tmpA, tmpB, shapes); // TODO rotate line before or something to support rotated splits
	//		Pools.free(tmpA);
	//		Pools.free(tmpB);
	//		if(!split) {
	//			Pools.free(shapes);
	//			return false;
	//		}
	//		FixtureDef aDef = createDef(fixture), bDef = createDef(fixture);
	//		aDef.shape = shapes.key();
	//		bDef.shape = shapes.value();
	//		Pools.free(shapes);
	//		store.set(aDef, bDef);
	//		return true;
	//	}
	//
	//	private static final Polygon tmpPolygon = new Polygon();
	//
	//	@SuppressWarnings("unchecked")
	//	public static <T extends Shape> boolean split(T shape, Vector2 a, Vector2 b, Pair<T, T> store) {
	//		switch(shape.getType()) {
	//		case Circle:
	//			throw new IllegalArgumentException("shapes of the type " + Type.Circle + " cannot be split since Box2D does not support curved shapes other than circles: " + shape);
	//		case Polygon:
	//			Vector2[] vertices = vertices(shape);
	//			tmpPolygon.setVertices(GeometryUtils.toFloatArray(vertices));
	//			if(!Intersector.intersectLinePolygon(a, b, tmpPolygon))
	//				return false;
	//			Array<Vector2> aVertices = Pools.obtain(Array.class),
	//			bVertices = Pools.obtain(Array.class);
	//			aVertices.clear();
	//			bVertices.clear();
	//			aVertices.add(a);
	//			aVertices.add(b);
	//			bVertices.add(a);
	//			bVertices.add(b);
	//
	//			for(int i = 0; i < vertices.length; i++) {
	//				float det = MathUtils.det(a.x, a.y, vertices[i].x, vertices[i].y, b.x, b.y);
	//				if(det < 0)
	//					aVertices.add(vertices[i]);
	//				else if(det > 0)
	//					bVertices.add(vertices[i]);
	//				else {
	//					aVertices.add(vertices[i]);
	//					bVertices.add(vertices[i]);
	//				}
	//			}
	//
	//			GeometryUtils.arrangeClockwise(aVertices);
	//			GeometryUtils.arrangeClockwise(bVertices);
	//
	//			PolygonShape aShape = new PolygonShape(),
	//			bShape = new PolygonShape();
	//			aShape.set((Vector2[]) aVertices.toArray(Vector2.class));
	//			bShape.set((Vector2[]) bVertices.toArray(Vector2.class));
	//
	//			Pools.free(aVertices);
	//			Pools.free(bVertices);
	//
	//			store.set((T) aShape, (T) bShape);
	//			return true;
	//		case Edge:
	//			break;
	//		case Chain:
	//			break;
	//		default:
	//			assert false : Shape.Type.class.getSimpleName() + " is unknown";
	//		}
	//		return false;
	//	}

	// various

	/** sets the {@link Fixture#isSensor() sensor flag} of all of the given Body's Fixtures
	 *  @param body the {@link Body} which {@link Fixture Fixtures'} sensor flag to set
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
		for(int preserved = 0; preserved < fixtures.size;) {
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
		for(int preserved = 0; preserved < fixtures.size;) {
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
		for(int preserved = 0; preserved < fixtures.size;) {
			Fixture fixture = fixtures.get(fixtures.size - 1 - preserved);
			if(fixture != exclude)
				body.destroyFixture(fixture);
			else
				preserved++;
		}
	}

}
