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
import static net.dermetfan.utils.libgdx.math.GeometryUtils.rotate;
import static net.dermetfan.utils.libgdx.math.GeometryUtils.vec2_0;
import static net.dermetfan.utils.libgdx.math.GeometryUtils.vec2_1;
import static net.dermetfan.utils.math.MathUtils.amplitude;
import static net.dermetfan.utils.math.MathUtils.max;
import static net.dermetfan.utils.math.MathUtils.min;
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
import com.badlogic.gdx.utils.ObjectMap;

/** provides methods for operations with Box2D {@link Body Bodies}, {@link Fixture Fixtures} and {@link Shape Shapes}
 *  @author dermetfan */
public abstract class Box2DUtils {

	/** cached method results */
	public static class ShapeCache {

		public final Vector2[] vertices;
		public final float width, height, minX, maxX, minY, maxY;

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

	/** Cached {@link Shape Shapes} and their vertices. You should {@link ObjectMap#clear() clear} this when you don't use the shapes anymore. */
	public static final ObjectMap<Shape, ShapeCache> cache = new ObjectMap<Shape, ShapeCache>();

	/** if new {@link Shape} passed in {@link #vertices(Shape)} should be automatically added to the {@link #cache} */
	private static boolean autoCache = true;

	/** the maximum of {@link Shape Shapes} and their vertices to automatically cache if {@link #autoCache} is true */
	private static int autoCacheMaxSize = Integer.MAX_VALUE;

	/** temporary {@link Vector2} array used by some methods
	 *  warning: not safe to use as it may change unexpectedly */
	public static Vector2[] tmpVecArr;

	/** @return the vertices of all fixtures of the given body
	 *  @see #vertices(Shape) */
	public static Vector2[][] vertices(Body body, Vector2[][] output) {
		if(output == null || output.length != body.getFixtureList().size)
			output = new Vector2[body.getFixtureList().size][]; // caching fixture vertices for performance
		for(int i = 0; i < output.length; i++)
			output[i] = vertices(body.getFixtureList().get(i), tmpVecArr);
		return output;
	}

	/** @return the vertices of all fixtures of the given body
	 *  @see #vertices(Shape) */
	public static Vector2[] vertices(Body body, Vector2[] output) {
		Vector2[][] vertices = vertices(body, (Vector2[][]) null);

		int vertexCount = 0;
		for(int i = 0; i < vertices.length; i++)
			vertexCount += vertices[i].length;

		if(output == null || output.length != 4)
			output = new Vector2[vertexCount];
		int vi = -1;
		for(Vector2[] verts : vertices)
			for(Vector2 vertice : verts)
				output[++vi] = vertice;

		return output;
	}

	/** @see #vertices(Body, Vector2[]) */
	public static Vector2[] vertices(Body body) {
		return vertices(body, tmpVecArr);
	}

	/** @see #vertices(Shape) */
	public static Vector2[] vertices(Fixture fixture, Vector2[] output) {
		return vertices(fixture.getShape(), output);
	}

	/** @see #vertices(Fixture, Vector2[]) */
	public static Vector2[] vertices(Fixture fixture) {
		return vertices(fixture, tmpVecArr);
	}

	/** @return the vertices of the given Shape */
	public static Vector2[] vertices(Shape shape, Vector2[] output) {
		if(cache.containsKey(shape))
			return output = cache.get(shape).vertices;
		output = vertices0(shape, output);
		if(autoCache && cache.size < autoCacheMaxSize) {
			Vector2[] cachedOutput = new Vector2[output.length];
			System.arraycopy(output, 0, cachedOutput, 0, output.length);
			cache.put(shape, new ShapeCache(cachedOutput, width0(shape), height0(shape), minX0(shape), maxX0(shape), minY0(shape), maxY0(shape)));
		}
		return output;
	}

	/** {@link #vertices(Shape)} without caching */
	private static Vector2[] vertices0(Shape shape, Vector2[] output) {
		switch(shape.getType()) {
		case Polygon:
			PolygonShape polygonShape = (PolygonShape) shape;

			if(output == null || output.length != polygonShape.getVertexCount())
				output = new Vector2[polygonShape.getVertexCount()];

			for(int i = 0; i < output.length; i++) {
				if(output[i] == null)
					output[i] = new Vector2();
				polygonShape.getVertex(i, output[i]);
			}
			break;
		case Edge:
			EdgeShape edgeShape = (EdgeShape) shape;

			edgeShape.getVertex1(vec2_0);
			edgeShape.getVertex2(vec2_1);

			if(output == null || output.length != 2)
				output = new Vector2[] {vec2_0, vec2_1};
			else {
				if(output[0] == null)
					output[0] = new Vector2(vec2_0);
				else
					output[0].set(vec2_0);
				if(output[1] == null)
					output[1] = new Vector2(vec2_1);
				else
					output[1].set(vec2_1);
			}
			break;
		case Chain:
			ChainShape chainShape = (ChainShape) shape;

			if(output == null || output.length != chainShape.getVertexCount())
				output = new Vector2[chainShape.getVertexCount()];

			for(int i = 0; i < output.length; i++) {
				if(output[i] == null)
					output[i] = new Vector2();
				chainShape.getVertex(i, output[i]);
			}
			break;
		case Circle:
			CircleShape circleShape = (CircleShape) shape;

			if(output == null || output.length != 4)
				output = new Vector2[4];
			vec2_0.set(circleShape.getPosition().x - circleShape.getRadius(), circleShape.getPosition().y + circleShape.getRadius()); // top left
			output[0] = output[0] != null ? output[0].set(vec2_0) : new Vector2(vec2_0);
			vec2_0.set(circleShape.getPosition().x - circleShape.getRadius(), circleShape.getPosition().y - circleShape.getRadius()); // bottom left
			output[1] = output[1] != null ? output[1].set(vec2_0) : new Vector2(vec2_0);
			vec2_0.set(circleShape.getPosition().x + circleShape.getRadius(), circleShape.getPosition().y - circleShape.getRadius()); // bottom right
			output[2] = output[2] != null ? output[2].set(vec2_0) : new Vector2(vec2_0);
			vec2_0.set(circleShape.getPosition().x + circleShape.getRadius(), circleShape.getPosition().y + circleShape.getRadius()); // top right
			output[3] = output[3] != null ? output[3].set(vec2_0) : new Vector2(vec2_0);
			break;
		default:
			throw new IllegalArgumentException("Shapes of the type '" + shape.getType().name() + "' are not supported");
		}
		return output;
	}

	/** @see #vertices0(Shape, Vector2[]) */
	private static Vector2[] vertices0(Shape shape) {
		return vertices0(shape, tmpVecArr);
	}

	/** @see #vertices(Shape, Vector2[]) */
	public static Vector2[] vertices(Shape shape) {
		return vertices(shape, tmpVecArr);
	}

	/** @return the minimal x value of the vertices of all fixtures of the the given Body */
	public static float minX(Body body) {
		float x = Float.POSITIVE_INFINITY, tmp;
		for(Fixture fixture : body.getFixtureList())
			x = (tmp = minX(fixture)) < x ? tmp : x;
		return x;
	}

	/** @return the minimal y value of the vertices of all fixtures of the the given Body */
	public static float minY(Body body) {
		float y = Float.POSITIVE_INFINITY, tmp;
		for(Fixture fixture : body.getFixtureList())
			y = (tmp = minY(fixture)) < y ? tmp : y;
		return y;
	}

	/** @return the maximal x value of the vertices of all fixtures of the the given Body */
	public static float maxX(Body body) {
		float x = Float.NEGATIVE_INFINITY, tmp;
		for(Fixture fixture : body.getFixtureList())
			x = (tmp = maxX(fixture)) > x ? tmp : x;
		return x;
	}

	/** @return the maximal y value of the vertices of all fixtures of the the given Body */
	public static float maxY(Body body) {
		float y = Float.NEGATIVE_INFINITY, tmp;
		for(Fixture fixture : body.getFixtureList())
			y = (tmp = maxY(fixture)) > y ? tmp : y;
		return y;
	}

	/** @return the minimal x value of the vertices of the given Fixture */
	public static float minX(Fixture fixture) {
		return minX(fixture.getShape());
	}

	/** @return the minimal y value of the vertices of the given Fixture */
	public static float minY(Fixture fixture) {
		return minY(fixture.getShape());
	}

	/** @return the maximal x value of the vertices of the given Fixture */
	public static float maxX(Fixture fixture) {
		return maxX(fixture.getShape());
	}

	/** @return the maximal y value of the vertices of the given Fixture */
	public static float maxY(Fixture fixture) {
		return maxY(fixture.getShape());
	}

	/** @return the minimal x value of the vertices of the given Shape */
	public static float minX(Shape shape) {
		if(cache.containsKey(shape))
			return cache.get(shape).minX;
		return minX0(shape);
	}

	/** {@link #minX(Shape)} without caching */
	private static float minX0(Shape shape) {
		return min(filterX(vertices0(shape)));
	}

	/** @return the minimal y value of the vertices of the given Shape */
	public static float minY(Shape shape) {
		if(cache.containsKey(shape))
			return cache.get(shape).minY;
		return minY0(shape);
	}

	/** {@link #minY(Shape)} without caching */
	private static float minY0(Shape shape) {
		return min(filterY(vertices0(shape)));
	}

	/** @return the maximal x value of the vertices of the given Shape */
	public static float maxX(Shape shape) {
		if(cache.containsKey(shape))
			return cache.get(shape).maxX;
		return maxX0(shape);
	}

	/** {@link #minX(Shape)} without caching */
	private static float maxX0(Shape shape) {
		return max(filterX(vertices0(shape)));
	}

	/** @return the maximal y value of the vertices of the given Shape */
	public static float maxY(Shape shape) {
		if(cache.containsKey(shape))
			return cache.get(shape).maxY;
		return maxY0(shape);
	}

	/** {@link #maxY(Shape)} without caching */
	private static float maxY0(Shape shape) {
		return max(filterY(vertices0(shape)));
	}

	/** @return the width of the given Body */
	public static float width(Body body) {
		float min = Float.POSITIVE_INFINITY, max = Float.NEGATIVE_INFINITY, tmp;

		for(Fixture fixture : body.getFixtureList()) {
			if((tmp = minX(fixture)) < min)
				min = tmp;
			if((tmp = maxX(fixture)) > max)
				max = tmp;
		}

		return Math.abs(max - min);
	}

	/** @return the height of the given Body */
	public static float height(Body body) {
		float min = Float.POSITIVE_INFINITY, max = Float.NEGATIVE_INFINITY, tmp;

		for(Fixture fixture : body.getFixtureList()) {
			if((tmp = minY(fixture)) < min)
				min = tmp;
			if((tmp = maxY(fixture)) > max)
				max = tmp;
		}

		return Math.abs(max - min);
	}

	/** @return the width of the given Fixture */
	public static float width(Fixture fixture) {
		return width(fixture.getShape());
	}

	/** @return the height of the given Fixture */
	public static float height(Fixture fixture) {
		return height(fixture.getShape());
	}

	/** @return the width of the given Shape */
	public static float width(Shape shape) {
		if(cache.containsKey(shape))
			return cache.get(shape).width;
		return width0(shape);
	}

	/** {@link #width(Shape)} without caching */
	private static float width0(Shape shape) {
		return amplitude(filterX(vertices0(shape)));
	}

	/** @return the height of the given Shape */
	public static float height(Shape shape) {
		if(cache.containsKey(shape))
			return cache.get(shape).height;
		return height0(shape);
	}

	/** {@link #height(Shape)} without caching */
	private static float height0(Shape shape) {
		return amplitude(filterY(vertices0(shape)));
	}

	/** @see #size(Shape) */
	public static Vector2 size(Shape shape, Vector2 output) {
		if(shape.getType() == Type.Circle) // no call to #vertices(Shape) for performance
			return output.set(shape.getRadius() * 2, shape.getRadius() * 2);
		else if(cache.containsKey(shape))
			return output.set(cache.get(shape).width, cache.get(shape).height);
		return output.set(width(shape), height(shape));
	}

	/** @return the size of the given Shape */
	public static Vector2 size(Shape shape) {
		return size(shape, vec2_0);
	}

	/** @see #positionRelative(CircleShape) */
	public static Vector2 positionRelative(CircleShape shape, Vector2 output) {
		return output.set(shape.getPosition());
	}

	/** @return the relative position of the given CircleShape to its Body */
	public static Vector2 positionRelative(CircleShape shape) {
		return shape.getPosition();
	}

	/** @return the relative position of the given Shape to its Body
	 *  @param rotation the rotation of the body in radians */
	public static Vector2 positionRelative(Shape shape, float rotation, Vector2 output) {
		// get the position without rotation
		if(cache.containsKey(shape)) {
			ShapeCache sc = cache.get(shape);
			output.set(sc.maxX - sc.width / 2, sc.maxY - sc.height / 2);
		} else {
			tmpVecArr = vertices(shape); // the shape's vertices will hopefully be put in #cache
			if(cache.containsKey(shape)) // the shape's vertices are now hopefully in #cache, so let's try again
				positionRelative(shape, rotation, output);
			else { // #autoCache is false or #cache reached #autoCacheMaxSize
				float[] xs = filterX(tmpVecArr), ys = filterY(tmpVecArr);
				output.set(max(xs) - amplitude(xs) / 2, max(ys) - amplitude(ys) / 2); // so calculating manually is faster than using the methods because there won't be the containsKey checks
			}
		}

		// transform position according to rotation
		return rotate(output, rotation);
	}

	/** @see #positionRelative(Shape, float, Vector2) */
	public static Vector2 positionRelative(Shape shape, float rotation) {
		return positionRelative(shape, rotation, vec2_0);
	}

	/** @see #position(Fixture) */
	public static Vector2 position(Fixture fixture, Vector2 output) {
		return output.set(position(fixture.getShape(), fixture.getBody()));
	}

	/** @return the position of the given Fixture in world coordinates */
	public static Vector2 position(Fixture fixture) {
		return position(fixture.getShape(), fixture.getBody(), vec2_0);
	}

	/** @see #position(Shape, Body) */
	public static Vector2 position(Shape shape, Body body, Vector2 output) {
		return output.set(position(shape, body));
	}

	/** @return the position of the given Shape in world coordinates
	 *  @param shape the Shape which position to get
	 *  @param body the Body the given Shape is attached to */
	public static Vector2 position(Shape shape, Body body) {
		return body.getPosition().add(positionRelative(shape, body.getTransform().getRotation()));
	}

	/** creates a depe copy of a {@link Body} (without deep copying the {@link Shape Shapes} of its {@link Fixture Fixtures})<br/>
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

	// This method is not implemented since the Box2D API does not offer all the necessary information.
	//	public static Joint copy(Joint joint) {
	//		JointDef jointDef;
	//		Joint copy;
	//		switch(joint.getType()) {
	//		case Unknown:
	//			jointDef = new JointDef();
	//			copy = joint.getBodyA().getWorld().createJoint(jointDef);
	//			break;
	//		case RevoluteJoint:
	//			RevoluteJoint revoluteJoint = (RevoluteJoint) joint;
	//			RevoluteJointDef revoluteJointDef = (RevoluteJointDef) (jointDef = new RevoluteJointDef());
	//			revoluteJointDef.collideConnected = revoluteJointDef.collideConnected; // TODO
	//			revoluteJointDef.enableLimit = revoluteJoint.isLimitEnabled();
	//			revoluteJointDef.enableMotor = revoluteJoint.isMotorEnabled();
	//			revoluteJointDef.localAnchorA.set(revoluteJoint.getAnchorA());
	//			revoluteJointDef.localAnchorB.set(revoluteJoint.getAnchorB());
	//			revoluteJointDef.lowerAngle = revoluteJoint.getLowerLimit();
	//			revoluteJointDef.maxMotorTorque = revoluteJoint.getMaxMotorTorque();
	//			revoluteJointDef.motorSpeed = revoluteJoint.getMotorSpeed();
	//			revoluteJointDef.referenceAngle = revoluteJoint.getReferenceAngle();
	//			revoluteJointDef.upperAngle = revoluteJoint.getUpperLimit();
	//			break;
	//		case PrismaticJoint:
	//			PrismaticJoint prismaticJoint = (PrismaticJoint) joint;
	//			PrismaticJointDef prismaticJointDef = (PrismaticJointDef) (jointDef = new PrismaticJointDef());
	//			prismaticJointDef.collideConnected = prismaticJointDef.collideConnected; // TODO
	//			prismaticJointDef.enableLimit = prismaticJoint.isLimitEnabled();
	//			prismaticJointDef.enableMotor = prismaticJoint.isMotorEnabled();
	//			prismaticJointDef.localAnchorA.set(prismaticJoint.getAnchorA());
	//			prismaticJointDef.localAnchorB.set(prismaticJoint.getAnchorB());
	//			prismaticJointDef.localAxisA.set(prismaticJointDef.localAxisA); // TODO
	//			prismaticJointDef.lowerTranslation = prismaticJointDef.lowerTranslation; // TODO
	//			prismaticJointDef.maxMotorForce = prismaticJointDef.maxMotorForce; // TODO
	//			prismaticJointDef.motorSpeed = prismaticJoint.getMotorSpeed();
	//			prismaticJointDef.referenceAngle = prismaticJointDef.referenceAngle; // TODO
	//			prismaticJointDef.upperTranslation = prismaticJointDef.upperTranslation; // TODO
	//			break;
	//		case DistanceJoint:
	//			DistanceJoint distanceJoint = (DistanceJoint) joint;
	//			DistanceJointDef distanceJointDef = (DistanceJointDef) (jointDef = new DistanceJointDef());
	//			distanceJointDef.collideConnected = distanceJointDef.collideConnected; // TODO
	//			distanceJointDef.dampingRatio = distanceJoint.getDampingRatio();
	//			distanceJointDef.frequencyHz = distanceJoint.getFrequency();
	//			distanceJointDef.length = distanceJoint.getLength();
	//			distanceJointDef.localAnchorA.set(distanceJoint.getAnchorA());
	//			distanceJointDef.localAnchorB.set(distanceJoint.getAnchorB());
	//			break;
	//		case PulleyJoint:
	//			PulleyJoint pulleyJoint = (PulleyJoint) joint;
	//			PulleyJointDef pulleyJointDef = (PulleyJointDef) (jointDef = new PulleyJointDef());
	//			pulleyJointDef.collideConnected = pulleyJointDef.collideConnected; // TODO
	//			pulleyJointDef.groundAnchorA.set(pulleyJoint.getGroundAnchorA());
	//			pulleyJointDef.groundAnchorB.set(pulleyJoint.getGroundAnchorB());
	//			pulleyJointDef.lengthA = pulleyJoint.getLength1();
	//			pulleyJointDef.lengthB = pulleyJoint.getLength2();
	//			pulleyJointDef.localAnchorA.set(pulleyJoint.getAnchorA());
	//			pulleyJointDef.localAnchorB.set(pulleyJoint.getAnchorB());
	//			pulleyJointDef.ratio = pulleyJoint.getRatio();
	//			break;
	//		case MouseJoint:
	//			MouseJoint mouseJoint = (MouseJoint) joint;
	//			MouseJointDef mouseJointDef = (MouseJointDef) (jointDef = new MouseJointDef());
	//			mouseJointDef.collideConnected = mouseJointDef.collideConnected; // TODO
	//			mouseJointDef.dampingRatio = mouseJoint.getDampingRatio();
	//			mouseJointDef.frequencyHz = mouseJoint.getFrequency();
	//			mouseJointDef.maxForce = mouseJoint.getMaxForce();
	//			mouseJointDef.target.set(mouseJoint.getTarget());
	//			break;
	//		case GearJoint:
	//			GearJoint gearJoint = (GearJoint) joint;
	//			GearJointDef gearJointDef = (GearJointDef) (jointDef = new GearJointDef());
	//			gearJointDef.collideConnected = gearJointDef.collideConnected; // TODO
	//			gearJointDef.joint1 = gearJointDef.joint1; // TODO
	//			gearJointDef.joint2 = gearJointDef.joint2; // TODO
	//			gearJointDef.ratio = gearJoint.getRatio();
	//			break;
	//		case WheelJoint:
	//			WheelJoint wheelJoint = (WheelJoint) joint;
	//			WheelJointDef wheelJointDef = (WheelJointDef) (jointDef = new WheelJointDef());
	//			wheelJointDef.collideConnected = wheelJointDef.collideConnected; // TODO
	//			wheelJointDef.dampingRatio = wheelJoint.getSpringDampingRatio();
	//			wheelJointDef.enableMotor = wheelJointDef.enableMotor; // TODO
	//			wheelJointDef.frequencyHz = wheelJoint.getSpringFrequencyHz();
	//			wheelJointDef.localAnchorA.set(wheelJoint.getAnchorA());
	//			wheelJointDef.localAnchorB.set(wheelJoint.getAnchorB());
	//			wheelJointDef.localAxisA.set(wheelJointDef.localAxisA); // TODO
	//			wheelJointDef.maxMotorTorque = wheelJoint.getMaxMotorTorque();
	//			wheelJointDef.motorSpeed = wheelJoint.getMotorSpeed();
	//			break;
	//		case WeldJoint:
	//			WeldJoint weldJoint = (WeldJoint) joint;
	//			WeldJointDef weldJointDef = (WeldJointDef) (jointDef = new WeldJointDef());
	//			weldJointDef.collideConnected = weldJointDef.collideConnected; // TODO
	//			weldJointDef.localAnchorA.set(weldJoint.getAnchorA());
	//			weldJointDef.localAnchorB.set(weldJoint.getAnchorB());
	//			weldJointDef.referenceAngle = weldJoint.getReferenceAngle();
	//			break;
	//		case FrictionJoint:
	//			FrictionJoint frictionJoint = (FrictionJoint) joint;
	//			FrictionJointDef frictionJointDef = (FrictionJointDef) (jointDef = new FrictionJointDef());
	//			frictionJointDef.collideConnected = frictionJointDef.collideConnected; // TODO
	//			frictionJointDef.localAnchorA.set(frictionJoint.getAnchorA());
	//			frictionJointDef.localAnchorB.set(frictionJoint.getAnchorB());
	//			frictionJointDef.maxForce = frictionJoint.getMaxForce();
	//			frictionJointDef.maxTorque = frictionJoint.getMaxTorque();
	//			break;
	//		case RopeJoint:
	//			RopeJoint ropeJoint = (RopeJoint) joint;
	//			RopeJointDef ropeJointDef = (RopeJointDef) (jointDef = new RopeJointDef());
	//			ropeJointDef.localAnchorA.set(ropeJoint.getAnchorA());
	//			ropeJointDef.localAnchorB.set(ropeJoint.getAnchorB());
	//			ropeJointDef.maxLength = ropeJoint.getMaxLength();
	//			break;
	//		default:
	//			return joint;
	//		}
	//		jointDef.type = joint.getType();
	//		jointDef.bodyA = joint.getBodyA();
	//		jointDef.bodyB = joint.getBodyB();
	//		copy = joint.getBodyA().getWorld().createJoint(jointDef);
	//		copy.setUserData(joint.getUserData());
	//		return copy;
	//	}

	/** @return the {@link #autoCache} */
	public static boolean isAutoCache() {
		return autoCache;
	}

	/** @param autoCache the {@link #autoCache} to set */
	public static void setAutoCache(boolean autoCache) {
		Box2DUtils.autoCache = autoCache;
	}

	/** @return the {@link #autoCacheMaxSize} */
	public static int getAutoCacheMaxSize() {
		return autoCacheMaxSize;
	}

	/** @param autoCacheMaxSize the {@link #autoCacheMaxSize} to set */
	public static void setAutoCacheMaxSize(int autoCacheMaxSize) {
		Box2DUtils.autoCacheMaxSize = autoCacheMaxSize;
	}

}
