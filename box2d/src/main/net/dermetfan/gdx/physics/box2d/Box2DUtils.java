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
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
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
import static net.dermetfan.gdx.math.MathUtils.amplitude;
import static net.dermetfan.gdx.math.MathUtils.max;
import static net.dermetfan.gdx.math.MathUtils.min;

/** provides methods for operations with Box2D {@link Body Bodies}, {@link Fixture Fixtures} and {@link Shape Shapes}
 *  @author dermetfan */
public class Box2DUtils extends com.badlogic.gdx.physics.box2d.Box2DUtils {

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
	public static final ObjectMap<Shape, ShapeCache> cache = new ObjectMap<>();

	/** if shapes should automatically be cached when they are inspected for the first time */
	public static boolean autoCache = true;

	/** the area that is too small for a {@link PolygonShape} to contain it (limitation by Box2D) */
	public static final float minExclusivePolygonArea = 1.19209289550781250000e-7F;

	/** the max amount of vertices of a {@link PolygonShape}, see b2Settings.h (limitation by Box2D) */
	public static final byte maxPolygonVertices = 8;

	/** the max velocity of a body, see b2Settings.h (limitation by Box2D) */
	public static final float maxTranslation = 2;

	/** if Box2D preconditions should be checked to avoid crashes */
	public static boolean checkPreconditions = true;

	/** for internal, temporary usage */
	private static final Vector2 vec2_0 = new Vector2(), vec2_1 = new Vector2();

	/** for internal, temporary usage */
	private static final Array<Vector2> tmpVector2Array = new Array<>(8);

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

	/** @param shape the Shape which vertices to get (for circles, the bounding box vertices will be returned)
	 *  @return the vertices of the given Shape*/
	private static Vector2[] vertices0(Shape shape) {
		Vector2[] vertices;
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
			Vector2 position = circleShape.getPosition();
			float radius = circleShape.getRadius();
			vertices = new Vector2[4];
			vertices[0] = new Vector2(position.x - radius, position.y + radius); // top left
			vertices[1] = new Vector2(position.x - radius, position.y - radius); // bottom left
			vertices[2] = new Vector2(position.x + radius, position.y - radius); // bottom right
			vertices[3] = new Vector2(position.x + radius, position.y + radius); // top right
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
		tmpVector2Array.clear();
		tmpVector2Array.addAll(vertices0(shape));
		return min(filterX(tmpVector2Array));
	}

	/** @return the minimal y of the vertices of the given Shape */
	private static float minY0(Shape shape) {
		if(shape instanceof CircleShape)
			return ((CircleShape) shape).getPosition().y - shape.getRadius();
		tmpVector2Array.clear();
		tmpVector2Array.addAll(vertices0(shape));
		return min(filterY(tmpVector2Array));
	}

	/** @return the maximal x of the vertices of the given Shape */
	private static float maxX0(Shape shape) {
		if(shape instanceof CircleShape)
			return ((CircleShape) shape).getPosition().x + shape.getRadius();
		tmpVector2Array.clear();
		tmpVector2Array.addAll(vertices0(shape));
		return max(filterX(tmpVector2Array));
	}

	/** @return the maximal y of the vertices of the given Shape */
	private static float maxY0(Shape shape) {
		if(shape instanceof CircleShape)
			return ((CircleShape) shape).getPosition().y + shape.getRadius();
		tmpVector2Array.clear();
		tmpVector2Array.addAll(vertices0(shape));
		return max(filterY(tmpVector2Array));
	}

	/** @return the width of the given Shape */
	private static float width0(Shape shape) {
		if(shape.getType() == Type.Circle)
			return shape.getRadius() * 2;
		tmpVector2Array.clear();
		tmpVector2Array.addAll(vertices0(shape));
		return amplitude(filterX(tmpVector2Array));
	}

	/** @return the height of the given Shape */
	private static float height0(Shape shape) {
		if(shape.getType() == Type.Circle)
			return shape.getRadius() * 2;
		tmpVector2Array.clear();
		tmpVector2Array.addAll(vertices0(shape));
		return amplitude(filterY(tmpVector2Array));
	}

	/** @return a Vector2 representing the size of the given Shape */
	private static Vector2 size0(Shape shape) {
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
		for(Vector2[] fixtureVertice : fixtureVertices)
			vertexCount += fixtureVertice.length;

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

	// clone

	/** clones a Body (without deep copying the Shapes of its Fixtures)<br>
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
		for(Fixture fixture : body.getFixtureList())
			if(!split(fixture, a, b, aBody, bBody, null))
				return false;
		if(store != null)
			store.set(aBody, bBody);
		return true;
	}

	/** @param fixture the fixture to split
	 *  @param a the first point of the segment
	 *  @param b the second point of the segment
	 *  @param aBody the body the first resulting fixture will be created on
	 *  @param bBody the body the second resulting fixture will be created on
	 *  @param store The {@link Pair} to store the resulting fixtures in. May be null.
	 *  @return if the fixture was split
	 *  @see #split(Shape, Vector2, Vector2, Pair) */
	public static boolean split(Fixture fixture, Vector2 a, Vector2 b, Body aBody, Body bBody, Pair<Fixture, Fixture> store) {
		@SuppressWarnings("unchecked")
		Pair<FixtureDef, FixtureDef> defs = Pools.obtain(Pair.class);
		if(!split(fixture, a, b, defs)) {
			Pools.free(defs);
			return false;
		}
		Fixture aFixture = aBody.createFixture(defs.getKey()), bFixture = bBody.createFixture(defs.getValue());
		if(store != null)
			store.set(aFixture, bFixture);
		return true;
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
		boolean split = split(fixture.getShape(), tmpA, tmpB, shapes);
		Pools.free(tmpA);
		Pools.free(tmpB);
		if(!split) {
			Pools.free(shapes);
			return false;
		}
		FixtureDef aDef = createDef(fixture), bDef = createDef(fixture);
		aDef.shape = shapes.getKey();
		bDef.shape = shapes.getValue();
		Pools.free(shapes);
		store.set(aDef, bDef);
		return true;
	}

	/** splits the given Shape using the segment described by the two given Vector2s
	 *  @param shape the Shape to split
	 *  @param a the first point of the segment
	 *  @param b the second point of the segment
	 *  @param store the {@link Pair} to store the split Shapes in
	 *  @return if the given shape was split */
	@SuppressWarnings("unchecked")
	public static <T extends Shape> boolean split(T shape, Vector2 a, Vector2 b, Pair<T, T> store) {
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

		store.clear();

		Vector2 vertices[] = vertices(shape), aa = Pools.obtain(Vector2.class).set(a), bb = Pools.obtain(Vector2.class).set(b);
		Array<Vector2> aVertices = Pools.obtain(Array.class), bVertices = Pools.obtain(Array.class);
		aVertices.clear();
		bVertices.clear();

		if(type == Type.Polygon) {
			aVertices.add(aa);
			aVertices.add(bb);
			GeometryUtils.arrangeClockwise(aVertices);

			tmpVector2Array.clear();
			tmpVector2Array.addAll(vertices);
			if(GeometryUtils.intersectSegments(a, b, GeometryUtils.toFloatArray(tmpVector2Array), aVertices.first(), aVertices.peek()) < 2) {
				Pools.free(aa);
				Pools.free(bb);
				Pools.free(aVertices);
				Pools.free(bVertices);
				return false;
			}

			bVertices.add(aa);
			bVertices.add(bb);

			for(Vector2 vertice : vertices) {
				float det = MathUtils.det(aa.x, aa.y, vertice.x, vertice.y, bb.x, bb.y);
				if(det < 0)
					aVertices.add(vertice);
				else if(det > 0)
					bVertices.add(vertice);
				else {
					aVertices.add(vertice);
					bVertices.add(vertice);
				}
			}

			GeometryUtils.arrangeClockwise(aVertices);
			GeometryUtils.arrangeClockwise(bVertices);

			if(checkPreconditions) {
				if(aVertices.size >= 3 && aVertices.size <= maxPolygonVertices && bVertices.size >= 3 && bVertices.size <= maxPolygonVertices) {
					FloatArray aVerticesFloatArray = GeometryUtils.toFloatArray(aVertices, new FloatArray(aVertices.size * 2)), bVerticesFloatArray = GeometryUtils.toFloatArray(bVertices, new FloatArray(bVertices.size * 2));
					if(GeometryUtils.polygonArea(aVerticesFloatArray) > minExclusivePolygonArea && GeometryUtils.polygonArea(bVerticesFloatArray) > minExclusivePolygonArea) {
						PolygonShape sa = new PolygonShape(), sb = new PolygonShape();
						sa.set(aVerticesFloatArray.toArray());
						sb.set(bVerticesFloatArray.toArray());
						store.set((T) sa, (T) sb);
					}
				}
			} else {
				PolygonShape sa = new PolygonShape(), sb = new PolygonShape();
				sa.set((Vector2[]) aVertices.toArray(Vector2.class));
				sb.set((Vector2[]) bVertices.toArray(Vector2.class));
				store.set((T) sa, (T) sb);
			}
		} else if(type == Type.Chain) {
			Vector2 tmp = Pools.obtain(Vector2.class);
			boolean intersected = false;
			for(int i = 0; i < vertices.length; i++) {
				if(!intersected)
					aVertices.add(vertices[i]);
				else
					bVertices.add(vertices[i]);
				if(!intersected && i + 1 < vertices.length && Intersector.intersectSegments(vertices[i], vertices[i + 1], aa, bb, tmp)) {
					intersected = true;
					aVertices.add(tmp);
					bVertices.add(tmp);
				}
			}
			if(intersected)
				if(!checkPreconditions || aVertices.size >= 3 && bVertices.size >= 3) {
					ChainShape sa = new ChainShape(), sb = new ChainShape();
					sa.createChain((Vector2[]) aVertices.toArray(Vector2.class));
					sb.createChain((Vector2[]) bVertices.toArray(Vector2.class));
					store.set((T) sa, (T) sb);
				}
			Pools.free(tmp);
		}

		Pools.free(aa);
		Pools.free(bb);
		Pools.free(aVertices);
		Pools.free(bVertices);

		return store.isFull();
	}

	// various

	/** @return if the two given Transform's {@link Transform#vals values} equal
	 *  @since 0.7.1 */
	public static boolean equals(Transform a, Transform b) {
		return Arrays.equals(a.vals, b.vals);
	}

	/** @return if the two MassData's values equal
	 *  @since 0.7.1 */
	public static boolean equals(MassData a, MassData b) {
		return a.center.equals(b.center) && a.mass == b.mass && a.I == b.I;
	}

	/** @return if the two Filter's values equal
	 *  @since 0.7.1 */
	public static boolean equals(Filter a, Filter b) {
		return a.categoryBits == b.categoryBits && a.maskBits == b.maskBits && a.groupIndex == b.groupIndex;
	}

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

}
