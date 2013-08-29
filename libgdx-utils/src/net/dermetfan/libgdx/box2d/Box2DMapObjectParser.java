/**
 * Copyright 2013 Robin Stumm (serverkorken@googlemail.com, http://dermetfan.bplaced.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.dermetfan.libgdx.box2d;

import static net.dermetfan.libgdx.math.GeometryUtils.areVerticesClockwise;
import static net.dermetfan.libgdx.math.GeometryUtils.isConvex;
import static net.dermetfan.libgdx.math.GeometryUtils.toFloatArray;
import static net.dermetfan.libgdx.math.GeometryUtils.toPolygonArray;
import static net.dermetfan.libgdx.math.GeometryUtils.toVector2Array;

import java.util.Iterator;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.maps.Map;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.CircleMapObject;
import com.badlogic.gdx.maps.objects.EllipseMapObject;
import com.badlogic.gdx.maps.objects.PolygonMapObject;
import com.badlogic.gdx.maps.objects.PolylineMapObject;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.objects.TextureMapObject;
import com.badlogic.gdx.math.Circle;
import com.badlogic.gdx.math.EarClippingTriangulator;
import com.badlogic.gdx.math.Ellipse;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Polyline;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.JointDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.physics.box2d.joints.DistanceJointDef;
import com.badlogic.gdx.physics.box2d.joints.FrictionJointDef;
import com.badlogic.gdx.physics.box2d.joints.GearJointDef;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;
import com.badlogic.gdx.physics.box2d.joints.PrismaticJointDef;
import com.badlogic.gdx.physics.box2d.joints.PulleyJointDef;
import com.badlogic.gdx.physics.box2d.joints.RevoluteJointDef;
import com.badlogic.gdx.physics.box2d.joints.RopeJointDef;
import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;
import com.badlogic.gdx.physics.box2d.joints.WheelJointDef;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.ObjectMap;

/**
 * An utility class that parses {@link MapObjects} from a {@link Map} and generates Box2D {@link Body Bodies}, {@link Fixture Fixtures} and {@link Joint Joints} from it.<br/>
 * Just create a new {@link Box2DMapObjectParser} in any way you like and call {@link #load(World, MapLayer)} to load all compatible objects (defined by the {@link Aliases}) into your {@link World}.<br/>
 * <br/>
 * If you only want specific Fixtures or Bodies, you can use the {@link #createBody(World, MapObject)} and {@link #createFixture(MapObject)} methods.<br/>
 * <br/>
 * How you define compatible objects in the TiledMap editor:<br/>
 * In your object layer, right-click an object and set its properties to those of the Body / Fixture / both (in case you're creating an {@link Aliases#object object}) you'd like, as defined in the used {@link Aliases} object.<br/>
 * For type, you have to choose {@link Aliases#body}, {@link Aliases#fixture} or {@link Aliases#object}.<br/>
 * To add Fixtures to a Body, add a {@link Aliases#body} property with the same value to each Fixture of a Body.<br/>
 * To create {@link Joint Joints}, add any object to the layer and just put everything needed in its properties. Note that you use the editors unit here which will be converted to Box2D meters automatically using {@link Aliases#unitScale}.
 * 
 * @author dermetfan
 */
public class Box2DMapObjectParser {

	/** @see Aliases */
	private Aliases aliases;

	/** the unit scale to convert from editor units to Box2D meters */
	private float unitScale = 1;

	/** if the unit scale found in the map and it's layers should be ignored */
	private boolean ignoreMapUnitScale = false;

	/** the dimensions of a tile, used to transform positions (ignore / set to 1 if the used map is not a tile map) */
	private float tileWidth = 1, tileHeight = 1;

	/** the parsed {@link Body Bodies} */
	private ObjectMap<String, Body> bodies = new ObjectMap<String, Body>();

	/** the parsed {@link Fixture Fixtures} */
	private ObjectMap<String, Fixture> fixtures = new ObjectMap<String, Fixture>();

	/** the parsed {@link Joint Joints} */
	private ObjectMap<String, Joint> joints = new ObjectMap<String, Joint>();

	/** creates a new {@link Box2DMapObjectParser} with the default {@link Aliases} */
	public Box2DMapObjectParser() {
		this(new Aliases());
	}

	/**
	 * creates a new {@link Box2DMapObjectParser} using the given {@link Aliases}
	 * @param aliases the {@link Aliases} to use
	 */
	public Box2DMapObjectParser(Aliases aliases) {
		this.aliases = aliases;
	}

	/**
	 * creates a new {@link Box2DMapObjectParser} using the given {@link #unitScale unitScale} and sets {@link #ignoreMapUnitScale} to true
	 * @param unitScale the {@link #unitScale unitScale} to use
	 */
	public Box2DMapObjectParser(float unitScale) {
		this(unitScale, 1, 1);
	}

	/**
	 * creates a new {@link Box2DMapObjectParser} using the given {@link #unitScale}, {@link #tileWidth}, {@link #tileHeight} and sets {@link #ignoreMapUnitScale} to true
	 * @param unitScale the {@link #unitScale} to use
	 * @param tileWidth the {@link #tileWidth} to use
	 * @param tileHeight the {@link #tileHeight} to use
	 */
	public Box2DMapObjectParser(float unitScale, float tileWidth, float tileHeight) {
		this(new Aliases(), unitScale, tileWidth, tileHeight);
	}

	/**
	 * creates a new {@link Box2DMapObjectParser} using the given {@link Aliases} and {@link #unitScale} and sets {@link #ignoreMapUnitScale} to true
	 * @param aliases the {@link Aliases} to use
	 * @param unitScale the {@link #unitScale} to use
	 */
	public Box2DMapObjectParser(Aliases aliases, float unitScale) {
		this(aliases, unitScale, 1, 1);
	}

	/**
	 * creates a new {@link Box2DMapObjectParser} with the given parameters and sets {@link #ignoreMapUnitScale} to true
	 * @param aliases the {@link Aliases} to use
	 * @param unitScale the {@link #unitScale unitScale} to use
	 * @param tileWidth the {@link #tileWidth} to use
	 * @param tileHeight the {@link #tileHeight} to use
	 */
	public Box2DMapObjectParser(Aliases aliases, float unitScale, float tileWidth, float tileHeight) {
		this.aliases = aliases;
		this.unitScale = unitScale;
		ignoreMapUnitScale = true;
		this.tileWidth = tileWidth;
		this.tileHeight = tileHeight;
	}

	/**
	 * creates the given {@link Map Map's} {@link MapObjects} in the given {@link World}  
	 * @param world the {@link World} to create the {@link MapObjects} of the given {@link Map} in
	 * @param map the {@link Map} which {@link MapObjects} to create in the given {@link World}
	 * @return the given {@link World} with the parsed {@link MapObjects} of the given {@link Map} created in it
	 */
	public World load(World world, Map map) {
		if(!ignoreMapUnitScale)
			unitScale = (Float) getProperty(map.getProperties(), aliases.unitScale, unitScale, Float.class);
		tileWidth = (Integer) getProperty(map.getProperties(), "tilewidth", tileWidth, Integer.class);
		tileHeight = (Integer) getProperty(map.getProperties(), "tileheight", tileHeight, Integer.class);

		for(MapLayer mapLayer : map.getLayers())
			load(world, mapLayer);

		return world;
	}

	/**
	 * creates the given {@link MapLayer MapLayer's} {@link MapObjects} in the given {@link World}  
	 * @param world the {@link World} to create the {@link MapObjects} of the given {@link MapLayer} in
	 * @param layer the {@link MapLayer} which {@link MapObjects} to create in the given {@link World}
	 * @return the given {@link World} with the parsed {@link MapObjects} of the given {@link MapLayer} created in it
	 */
	public World load(World world, MapLayer layer) {
		for(MapObject object : layer.getObjects()) {
			if(!ignoreMapUnitScale)
				unitScale = (Float) getProperty(layer.getProperties(), aliases.unitScale, unitScale, Float.class);
			if(object.getProperties().get("type", "", String.class).equals(aliases.object)) {
				createBody(world, object);
				createFixtures(object);
			}
		}

		for(MapObject object : layer.getObjects()) {
			if(!ignoreMapUnitScale)
				unitScale = (Float) getProperty(layer.getProperties(), aliases.unitScale, unitScale, Float.class);
			if(object.getProperties().get("type", String.class).equals(aliases.body))
				createBody(world, object);
		}

		for(MapObject object : layer.getObjects()) {
			if(!ignoreMapUnitScale)
				unitScale = (Float) getProperty(layer.getProperties(), aliases.unitScale, unitScale, Float.class);
			if(object.getProperties().get("type", String.class).equals(aliases.fixture))
				createFixtures(object);
		}

		for(MapObject object : layer.getObjects()) {
			if(!ignoreMapUnitScale)
				unitScale = (Float) getProperty(layer.getProperties(), aliases.unitScale, unitScale, Float.class);
			if(object.getProperties().get("type", String.class).equals(aliases.joint))
				createJoint(object);
		}

		return world;
	}

	/**
	 * creates a {@link Body} in the given {@link World} from the given {@link MapObject}
	 * @param world the {@link World} to create the {@link Body} in
	 * @param mapObject the {@link MapObject} to parse the {@link Body} from
	 * @return the {@link Body} created in the given {@link World} from the given {@link MapObject}
	 */
	public Body createBody(World world, MapObject mapObject) {
		MapProperties properties = mapObject.getProperties();

		String type = properties.get("type", String.class);
		if(!type.equals(aliases.body) && !type.equals(aliases.object))
			throw new IllegalArgumentException("type of " + mapObject + " is  \"" + type + "\" instead of \"" + aliases.body + "\" or \"" + aliases.object + "\"");

		BodyDef bodyDef = new BodyDef();
		bodyDef.type = properties.get(aliases.bodyType, String.class) != null ? properties.get(aliases.bodyType, String.class).equals(aliases.dynamicBody) ? BodyType.DynamicBody : properties.get(aliases.bodyType, String.class).equals(aliases.kinematicBody) ? BodyType.KinematicBody : properties.get(aliases.bodyType, String.class).equals(aliases.staticBody) ? BodyType.StaticBody : bodyDef.type : bodyDef.type;
		bodyDef.active = (Boolean) getProperty(properties, aliases.active, bodyDef.active, Boolean.class);
		bodyDef.allowSleep = (Boolean) getProperty(properties, aliases.allowSleep, bodyDef.allowSleep, Boolean.class);
		bodyDef.angle = (Float) getProperty(properties, aliases.angle, bodyDef.angle, Float.class);
		bodyDef.angularDamping = (Float) getProperty(properties, aliases.angularDamping, bodyDef.angularDamping, Float.class);
		bodyDef.angularVelocity = (Float) getProperty(properties, aliases.angularVelocity, bodyDef.angularVelocity, Float.class);
		bodyDef.awake = (Boolean) getProperty(properties, aliases.awake, bodyDef.awake, Boolean.class);
		bodyDef.bullet = (Boolean) getProperty(properties, aliases.bullet, bodyDef.bullet, Boolean.class);
		bodyDef.fixedRotation = (Boolean) getProperty(properties, aliases.fixedRotation, bodyDef.fixedRotation, Boolean.class);
		bodyDef.gravityScale = (Float) getProperty(properties, aliases.gravityunitScale, bodyDef.gravityScale, Float.class);
		bodyDef.linearDamping = (Float) getProperty(properties, aliases.linearDamping, bodyDef.linearDamping, Float.class);
		bodyDef.linearVelocity.set((Float) getProperty(properties, aliases.linearVelocityX, bodyDef.linearVelocity.x, Float.class), (Float) getProperty(properties, aliases.linearVelocityY, bodyDef.linearVelocity.y, Float.class));
		bodyDef.position.set((Integer) getProperty(properties, "x", bodyDef.position.x, Integer.class) * unitScale, (Integer) getProperty(properties, "y", bodyDef.position.y, Integer.class) * unitScale);

		Body body = world.createBody(bodyDef);

		String name = mapObject.getName();
		if(bodies.containsKey(name)) {
			int duplicate = 1;
			while(bodies.containsKey(name + duplicate))
				duplicate++;
			name += duplicate;
		}

		bodies.put(name, body);

		return body;
	}

	/**
	 * creates a {@link Fixture} from a {@link MapObject}
	 * @param mapObject the {@link MapObject} to parse
	 * @return the parsed {@link Fixture}
	 */
	public Fixture createFixture(MapObject mapObject) {
		MapProperties properties = mapObject.getProperties();

		String type = properties.get("type", String.class);

		Body body = bodies.get(type.equals(aliases.object) ? mapObject.getName() : properties.get(aliases.body, String.class));

		if(!type.equals(aliases.fixture) && !type.equals(aliases.object))
			throw new IllegalArgumentException("type of " + mapObject + " is  \"" + type + "\" instead of \"" + aliases.fixture + "\" or \"" + aliases.object + "\"");

		FixtureDef fixtureDef = new FixtureDef();
		Shape shape = null;

		if(mapObject instanceof RectangleMapObject) {
			shape = new PolygonShape();
			Rectangle rectangle = ((RectangleMapObject) mapObject).getRectangle();
			rectangle.x *= unitScale;
			rectangle.y *= unitScale;
			rectangle.width *= unitScale;
			rectangle.height *= unitScale;
			((PolygonShape) shape).setAsBox(rectangle.width / 2, rectangle.height / 2, new Vector2(rectangle.x - body.getPosition().x + rectangle.width / 2, rectangle.y - body.getPosition().y + rectangle.height / 2), body.getAngle());
		} else if(mapObject instanceof PolygonMapObject) {
			shape = new PolygonShape();
			Polygon polygon = ((PolygonMapObject) mapObject).getPolygon();
			polygon.setPosition(polygon.getX() * unitScale - body.getPosition().x, polygon.getY() * unitScale - body.getPosition().y);
			polygon.setScale(unitScale, unitScale);
			((PolygonShape) shape).set(polygon.getTransformedVertices());
		} else if(mapObject instanceof PolylineMapObject) {
			shape = new ChainShape();
			Polyline polyline = ((PolylineMapObject) mapObject).getPolyline();
			polyline.setPosition(polyline.getX() * unitScale - body.getPosition().x, polyline.getY() * unitScale - body.getPosition().y);
			polyline.setScale(unitScale, unitScale);
			((ChainShape) shape).createChain(polyline.getTransformedVertices());
		} else if(mapObject instanceof CircleMapObject) {
			shape = new CircleShape();
			Circle circle = ((CircleMapObject) mapObject).getCircle();
			circle.setPosition(circle.x * unitScale - body.getPosition().x, circle.y * unitScale - body.getPosition().y);
			circle.radius *= unitScale;
			((CircleShape) shape).setPosition(new Vector2(circle.x, circle.y));
			((CircleShape) shape).setRadius(circle.radius);
		} else if(mapObject instanceof EllipseMapObject) {
			Ellipse ellipse = ((EllipseMapObject) mapObject).getEllipse();

			if(ellipse.width == ellipse.height) {
				CircleMapObject circleMapObject = new CircleMapObject(ellipse.x, ellipse.y, ellipse.width / 2);
				circleMapObject.setName(mapObject.getName());
				circleMapObject.getProperties().putAll(mapObject.getProperties());
				circleMapObject.setColor(mapObject.getColor());
				circleMapObject.setVisible(mapObject.isVisible());
				circleMapObject.setOpacity(mapObject.getOpacity());
				return createFixture(circleMapObject);
			}

			IllegalArgumentException exception = new IllegalArgumentException("Cannot parse " + mapObject.getName() + " because " + mapObject.getClass().getSimpleName() + "s that are not circles are not supported");
			Gdx.app.error(getClass().getSimpleName(), exception.getMessage(), exception);
			throw exception;
		} else if(mapObject instanceof TextureMapObject) {
			IllegalArgumentException exception = new IllegalArgumentException("Cannot parse " + mapObject.getName() + " because " + mapObject.getClass().getSimpleName() + "s are not supported");
			Gdx.app.error(getClass().getSimpleName(), exception.getMessage(), exception);
			throw exception;
		} else
			assert false : mapObject + " is a not known subclass of " + MapObject.class.getName();

		fixtureDef.shape = shape;
		fixtureDef.density = (Float) getProperty(properties, aliases.density, fixtureDef.density, Float.class);
		fixtureDef.filter.categoryBits = (Short) getProperty(properties, aliases.categoryBits, fixtureDef.filter.categoryBits, Short.class);
		fixtureDef.filter.groupIndex = (Short) getProperty(properties, aliases.groupIndex, fixtureDef.filter.groupIndex, Short.class);
		fixtureDef.filter.maskBits = (Short) getProperty(properties, aliases.maskBits, fixtureDef.filter.maskBits, Short.class);
		fixtureDef.friction = (Float) getProperty(properties, aliases.friciton, fixtureDef.friction, Float.class);
		fixtureDef.isSensor = (Boolean) getProperty(properties, aliases.isSensor, fixtureDef.isSensor, Boolean.class);
		fixtureDef.restitution = (Float) getProperty(properties, aliases.restitution, fixtureDef.restitution, Float.class);

		Fixture fixture = body.createFixture(fixtureDef);

		shape.dispose();

		String name = mapObject.getName();
		if(fixtures.containsKey(name)) {
			int duplicate = 1;
			while(fixtures.containsKey(name + duplicate))
				duplicate++;
			name += duplicate;
		}

		fixtures.put(name, fixture);

		return fixture;
	}

	/**
	 * creates {@link Fixture Fixtures} from a {@link MapObject}
	 * @param mapObject the {@link MapObject} to parse
	 * @return an array of parsed {@link Fixture Fixtures}
	 */
	public Fixture[] createFixtures(MapObject mapObject) {
		if(mapObject instanceof PolygonMapObject) {
			Polygon polygon = ((PolygonMapObject) mapObject).getPolygon();

			if(!isConvex(polygon)) {
				// ensure the vertices are in counterclockwise order (not really necessary according to EarClippingTriangulator's javadoc, but sometimes better)
				if(areVerticesClockwise(polygon)) {
					Array<Vector2> vertices = new Array<Vector2>(toVector2Array(polygon.getVertices()));
					Vector2 first = vertices.removeIndex(0);
					vertices.reverse();
					vertices.insert(0, first);
					polygon.setVertices(toFloatArray(vertices.items));
				}

				// put the vertices of the polygon in an ArrayList
				// removed

				// get the split triangle vertices in a List
				FloatArray triangleVertices = new EarClippingTriangulator().computeTriangles(polygon.getTransformedVertices());

				// put the triangles' vertices in a Vector2 array
				Vector2[] triangleVerts = toVector2Array(triangleVertices.items);

				// create the triangles as polygons
				Polygon[] triangles = toPolygonArray(triangleVerts, 3);

				// create the fixtures of the triangles
				Fixture[] fixtures = new Fixture[triangles.length];
				for(int i = 0; i < triangles.length; i++) {
					PolygonMapObject triangleObject = new PolygonMapObject(triangles[i]);
					triangleObject.setColor(mapObject.getColor());
					triangleObject.setName(mapObject.getName());
					triangleObject.setOpacity(mapObject.getOpacity());
					triangleObject.setVisible(mapObject.isVisible());
					triangleObject.getProperties().putAll(mapObject.getProperties());
					fixtures[i] = createFixture(triangleObject);
				}

				return fixtures;
			}
		}

		return new Fixture[] {createFixture(mapObject)};
	}

	/**
	 * creates a {@link Joint} from a {@link MapObject}
	 * @param mapObject the {@link Joint} to parse
	 * @return the parsed {@link Joint}
	 */
	public Joint createJoint(MapObject mapObject) {
		MapProperties properties = mapObject.getProperties();

		JointDef jointDef = null;

		String type = properties.get("type", String.class);
		if(!type.equals(aliases.joint))
			throw new IllegalArgumentException("type of " + mapObject + " is  \"" + type + "\" instead of \"" + aliases.joint + "\"");

		String jointType = properties.get(aliases.jointType, String.class);

		// get all possible values
		if(jointType.equals(aliases.distanceJoint)) {
			DistanceJointDef distanceJointDef = new DistanceJointDef();
			distanceJointDef.dampingRatio = (Float) getProperty(properties, aliases.dampingRatio, distanceJointDef.dampingRatio, Float.class);
			distanceJointDef.frequencyHz = (Float) getProperty(properties, aliases.frequencyHz, distanceJointDef.frequencyHz, Float.class);
			distanceJointDef.length = (Float) getProperty(properties, aliases.length, distanceJointDef.length, Float.class) * (tileWidth + tileHeight) / 2 * unitScale;
			distanceJointDef.localAnchorA.set((Float) getProperty(properties, aliases.localAnchorAX, distanceJointDef.localAnchorA.x, Float.class) * tileWidth * unitScale, (Float) getProperty(properties, aliases.localAnchorAY, distanceJointDef.localAnchorA.y, Float.class) * tileHeight * unitScale);
			distanceJointDef.localAnchorB.set((Float) getProperty(properties, aliases.localAnchorBX, distanceJointDef.localAnchorB.x, Float.class) * tileWidth * unitScale, (Float) getProperty(properties, aliases.localAnchorBY, distanceJointDef.localAnchorB.y, Float.class) * tileHeight * unitScale);

			jointDef = distanceJointDef;
		} else if(jointType.equals(aliases.frictionJoint)) {
			FrictionJointDef frictionJointDef = new FrictionJointDef();
			frictionJointDef.localAnchorA.set((Float) getProperty(properties, aliases.localAnchorAX, frictionJointDef.localAnchorA.x, Float.class) * tileWidth * unitScale, (Float) getProperty(properties, aliases.localAnchorAY, frictionJointDef.localAnchorA.y, Float.class) * tileHeight * unitScale);
			frictionJointDef.localAnchorB.set((Float) getProperty(properties, aliases.localAnchorBX, frictionJointDef.localAnchorB.x, Float.class) * tileWidth * unitScale, (Float) getProperty(properties, aliases.localAnchorBY, frictionJointDef.localAnchorB.y, Float.class) * tileHeight * unitScale);
			frictionJointDef.maxForce = (Float) getProperty(properties, aliases.maxForce, frictionJointDef.maxForce, Float.class);
			frictionJointDef.maxTorque = (Float) getProperty(properties, aliases.maxTorque, frictionJointDef.maxTorque, Float.class);

			jointDef = frictionJointDef;
		} else if(jointType.equals(aliases.gearJoint)) {
			GearJointDef gearJointDef = new GearJointDef();
			gearJointDef.joint1 = joints.get(properties.get(aliases.joint1, String.class));
			gearJointDef.joint2 = joints.get(properties.get(aliases.joint2, String.class));
			gearJointDef.ratio = (Float) getProperty(properties, aliases.ratio, gearJointDef.ratio, Float.class);

			jointDef = gearJointDef;
		} else if(jointType.equals(aliases.mouseJoint)) {
			MouseJointDef mouseJointDef = new MouseJointDef();
			mouseJointDef.dampingRatio = (Float) getProperty(properties, aliases.dampingRatio, mouseJointDef.dampingRatio, Float.class);
			mouseJointDef.frequencyHz = (Float) getProperty(properties, aliases.frequencyHz, mouseJointDef.frequencyHz, Float.class);
			mouseJointDef.maxForce = (Float) getProperty(properties, aliases.maxForce, mouseJointDef.maxForce, Float.class);
			mouseJointDef.target.set((Float) getProperty(properties, aliases.targetX, mouseJointDef.target.x, Float.class) * tileWidth * unitScale, (Float) getProperty(properties, aliases.targetY, mouseJointDef.target.y, Float.class) * tileHeight * unitScale);

			jointDef = mouseJointDef;
		} else if(jointType.equals(aliases.prismaticJoint)) {
			PrismaticJointDef prismaticJointDef = new PrismaticJointDef();
			prismaticJointDef.enableLimit = (Boolean) getProperty(properties, aliases.enableLimit, prismaticJointDef.enableLimit, Boolean.class);
			prismaticJointDef.enableMotor = (Boolean) getProperty(properties, aliases.enableMotor, prismaticJointDef.enableMotor, Boolean.class);
			prismaticJointDef.localAnchorA.set((Float) getProperty(properties, aliases.localAnchorAX, prismaticJointDef.localAnchorA.x, Float.class) * tileWidth * unitScale, (Float) getProperty(properties, aliases.localAnchorAY, prismaticJointDef.localAnchorA.y, Float.class) * tileHeight * unitScale);
			prismaticJointDef.localAnchorB.set((Float) getProperty(properties, aliases.localAnchorBX, prismaticJointDef.localAnchorB.x, Float.class) * tileWidth * unitScale, (Float) getProperty(properties, aliases.localAnchorBY, prismaticJointDef.localAnchorB.y, Float.class) * tileHeight * unitScale);
			prismaticJointDef.localAxisA.set((Float) getProperty(properties, aliases.localAxisAX, prismaticJointDef.localAxisA.x, Float.class), (Float) getProperty(properties, aliases.localAxisAY, prismaticJointDef.localAxisA.y, Float.class));
			prismaticJointDef.lowerTranslation = (Float) getProperty(properties, aliases.lowerTranslation, prismaticJointDef.lowerTranslation, Float.class) * (tileWidth + tileHeight) / 2 * unitScale;
			prismaticJointDef.maxMotorForce = (Float) getProperty(properties, aliases.maxMotorForce, prismaticJointDef.maxMotorForce, Float.class);
			prismaticJointDef.motorSpeed = (Float) getProperty(properties, aliases.motorSpeed, prismaticJointDef.motorSpeed, Float.class);
			prismaticJointDef.referenceAngle = (Float) getProperty(properties, aliases.referenceAngle, prismaticJointDef.referenceAngle, Float.class);
			prismaticJointDef.upperTranslation = (Float) getProperty(properties, aliases.upperTranslation, prismaticJointDef.upperTranslation, Float.class) * (tileWidth + tileHeight) / 2 * unitScale;

			jointDef = prismaticJointDef;
		} else if(jointType.equals(aliases.pulleyJoint)) {
			PulleyJointDef pulleyJointDef = new PulleyJointDef();
			pulleyJointDef.groundAnchorA.set((Float) getProperty(properties, aliases.groundAnchorAX, pulleyJointDef.groundAnchorA.x, Float.class) * tileWidth * unitScale, (Float) getProperty(properties, aliases.groundAnchorAY, pulleyJointDef.groundAnchorA.y, Float.class) * tileHeight * unitScale);
			pulleyJointDef.groundAnchorB.set((Float) getProperty(properties, aliases.groundAnchorBX, pulleyJointDef.groundAnchorB.x, Float.class) * tileWidth * unitScale, (Float) getProperty(properties, aliases.groundAnchorBY, pulleyJointDef.groundAnchorB.y, Float.class) * tileHeight * unitScale);
			pulleyJointDef.lengthA = (Float) getProperty(properties, aliases.lengthA, pulleyJointDef.lengthA, Float.class) * (tileWidth + tileHeight) / 2 * unitScale;
			pulleyJointDef.lengthB = (Float) getProperty(properties, aliases.lengthB, pulleyJointDef.lengthB, Float.class) * (tileWidth + tileHeight) / 2 * unitScale;
			pulleyJointDef.localAnchorA.set((Float) getProperty(properties, aliases.localAnchorAX, pulleyJointDef.localAnchorA.x, Float.class) * tileWidth * unitScale, (Float) getProperty(properties, aliases.localAnchorAY, pulleyJointDef.localAnchorA.y, Float.class) * tileHeight * unitScale);
			pulleyJointDef.localAnchorB.set((Float) getProperty(properties, aliases.localAnchorBX, pulleyJointDef.localAnchorB.x, Float.class) * tileWidth * unitScale, (Float) getProperty(properties, aliases.localAnchorBY, pulleyJointDef.localAnchorB.y, Float.class) * tileHeight * unitScale);
			pulleyJointDef.ratio = (Float) getProperty(properties, aliases.ratio, pulleyJointDef.ratio, Float.class);

			jointDef = pulleyJointDef;
		} else if(jointType.equals(aliases.revoluteJoint)) {
			RevoluteJointDef revoluteJointDef = new RevoluteJointDef();
			revoluteJointDef.enableLimit = (Boolean) getProperty(properties, aliases.enableLimit, revoluteJointDef.enableLimit, Boolean.class);
			revoluteJointDef.enableMotor = (Boolean) getProperty(properties, aliases.enableMotor, revoluteJointDef.enableMotor, Boolean.class);
			revoluteJointDef.localAnchorA.set((Float) getProperty(properties, aliases.localAnchorAX, revoluteJointDef.localAnchorA.x, Float.class) * tileWidth * unitScale, (Float) getProperty(properties, aliases.localAnchorAY, revoluteJointDef.localAnchorA.y, Float.class) * tileHeight * unitScale);
			revoluteJointDef.localAnchorB.set((Float) getProperty(properties, aliases.localAnchorBX, revoluteJointDef.localAnchorB.x, Float.class) * tileWidth * unitScale, (Float) getProperty(properties, aliases.localAnchorBY, revoluteJointDef.localAnchorB.y, Float.class) * tileHeight * unitScale);
			revoluteJointDef.lowerAngle = (Float) getProperty(properties, aliases.lowerAngle, revoluteJointDef.lowerAngle, Float.class);
			revoluteJointDef.maxMotorTorque = (Float) getProperty(properties, aliases.maxMotorTorque, revoluteJointDef.maxMotorTorque, Float.class);
			revoluteJointDef.motorSpeed = (Float) getProperty(properties, aliases.motorSpeed, revoluteJointDef.motorSpeed, Float.class);
			revoluteJointDef.referenceAngle = (Float) getProperty(properties, aliases.referenceAngle, revoluteJointDef.referenceAngle, Float.class);
			revoluteJointDef.upperAngle = (Float) getProperty(properties, aliases.upperAngle, revoluteJointDef.upperAngle, Float.class);

			jointDef = revoluteJointDef;
		} else if(jointType.equals(aliases.ropeJoint)) {
			RopeJointDef ropeJointDef = new RopeJointDef();
			ropeJointDef.localAnchorA.set((Float) getProperty(properties, aliases.localAnchorAX, ropeJointDef.localAnchorA.x, Float.class) * tileWidth * unitScale, (Float) getProperty(properties, aliases.localAnchorAY, ropeJointDef.localAnchorA.y, Float.class) * tileHeight * unitScale);
			ropeJointDef.localAnchorB.set((Float) getProperty(properties, aliases.localAnchorBX, ropeJointDef.localAnchorB.x, Float.class) * tileWidth * unitScale, (Float) getProperty(properties, aliases.localAnchorBY, ropeJointDef.localAnchorB.y, Float.class) * tileHeight * unitScale);
			ropeJointDef.maxLength = (Float) getProperty(properties, aliases.maxLength, ropeJointDef.maxLength, Float.class) * (tileWidth + tileHeight) / 2 * unitScale;

			jointDef = ropeJointDef;
		} else if(jointType.equals(aliases.weldJoint)) {
			WeldJointDef weldJointDef = new WeldJointDef();
			weldJointDef.localAnchorA.set((Float) getProperty(properties, aliases.localAnchorAX, weldJointDef.localAnchorA.x, Float.class) * tileWidth * unitScale, (Float) getProperty(properties, aliases.localAnchorAY, weldJointDef.localAnchorA.y, Float.class) * tileHeight * unitScale);
			weldJointDef.localAnchorB.set((Float) getProperty(properties, aliases.localAnchorBX, weldJointDef.localAnchorB.x, Float.class) * tileWidth * unitScale, (Float) getProperty(properties, aliases.localAnchorBY, weldJointDef.localAnchorB.y, Float.class) * tileHeight * unitScale);
			weldJointDef.referenceAngle = (Float) getProperty(properties, aliases.referenceAngle, weldJointDef.referenceAngle, Float.class);

			jointDef = weldJointDef;
		} else if(jointType.equals(aliases.wheelJoint)) {
			WheelJointDef wheelJointDef = new WheelJointDef();
			wheelJointDef.dampingRatio = (Float) getProperty(properties, aliases.dampingRatio, wheelJointDef.dampingRatio, Float.class);
			wheelJointDef.enableMotor = (Boolean) getProperty(properties, aliases.enableMotor, wheelJointDef.enableMotor, Boolean.class);
			wheelJointDef.frequencyHz = (Float) getProperty(properties, aliases.frequencyHz, wheelJointDef.frequencyHz, Float.class);
			wheelJointDef.localAnchorA.set((Float) getProperty(properties, aliases.localAnchorAX, wheelJointDef.localAnchorA.x, Float.class) * tileWidth * unitScale, (Float) getProperty(properties, aliases.localAnchorAY, wheelJointDef.localAnchorA.y, Float.class) * tileHeight * unitScale);
			wheelJointDef.localAnchorB.set((Float) getProperty(properties, aliases.localAnchorBX, wheelJointDef.localAnchorB.x, Float.class) * tileWidth * unitScale, (Float) getProperty(properties, aliases.localAnchorBY, wheelJointDef.localAnchorB.y, Float.class) * tileHeight * unitScale);
			wheelJointDef.localAxisA.set((Float) getProperty(properties, aliases.localAxisAX, wheelJointDef.localAxisA.x, Float.class), (Float) getProperty(properties, aliases.localAxisAY, wheelJointDef.localAxisA.y, Float.class));
			wheelJointDef.maxMotorTorque = (Float) getProperty(properties, aliases.maxMotorTorque, wheelJointDef.maxMotorTorque, Float.class);
			wheelJointDef.motorSpeed = (Float) getProperty(properties, aliases.motorSpeed, wheelJointDef.motorSpeed, Float.class);

			jointDef = wheelJointDef;
		}

		jointDef.bodyA = bodies.get(properties.get(aliases.bodyA, String.class));
		jointDef.bodyB = bodies.get(properties.get(aliases.bodyB, String.class));
		jointDef.collideConnected = (Boolean) getProperty(properties, aliases.collideConnected, jointDef.collideConnected, Boolean.class);

		Joint joint = jointDef.bodyA.getWorld().createJoint(jointDef);

		String name = mapObject.getName();
		if(joints.containsKey(name)) {
			int duplicate = 1;
			while(joints.containsKey(name + duplicate))
				duplicate++;
			name += duplicate;
		}

		joints.put(name, joint);

		return joint;
	}

	/**
	 * internal method for easier access of {@link MapProperties}
	 * @param properties the {@link MapProperties} from which to get a property
	 * @param property the key of the desired property
	 * @param defaultValue the default return value in case the value of the given key cannot be returned
	 * @param clazz The {@link Class} of the desired property. You still need to cast because it was not possible to make this a generic method.
	 * @return the property value associated with the given property key
	 */
	private Object getProperty(MapProperties properties, String property, Object defaultValue, Class<?> clazz) {
		if(clazz == Float.class)
			return properties.get(property, String.class) != null ? Float.parseFloat(properties.get(property, String.class)) : defaultValue;
		else if(clazz == Integer.class)
			return properties.get(property, String.class) != null ? properties.get(property, Integer.class) : defaultValue;
		else if(clazz == Short.class)
			return properties.get(property, String.class) != null ? properties.get(property, Short.class) : defaultValue;
		else if(clazz == Boolean.class)
			return properties.get(property, String.class) != null ? Boolean.parseBoolean(properties.get(property, String.class)) : defaultValue;
		return defaultValue;
	}

	/**
	 * @param map the {@link Map} which hierarchy to print
	 * @return a human readable {@link String} containing the hierarchy of the {@link MapObjects} of the given {@link Map}
	 */
	public String getHierarchy(Map map) {
		String hierarchy = map.getClass().getSimpleName() + "\n";

		Iterator<String> keys = map.getProperties().getKeys();
		while(keys.hasNext()) {
			String key = keys.next();
			hierarchy += key + ": " + map.getProperties().get(key) + "\n";
		}

		for(MapLayer layer : map.getLayers()) {
			hierarchy += "\t" + layer.getName() + " (" + layer.getClass().getSimpleName() + "):\n";
			String layerHierarchy = getHierarchy(layer).replace("\n", "\n\t\t");
			layerHierarchy = layerHierarchy.endsWith("\n\t\t") ? layerHierarchy.substring(0, layerHierarchy.lastIndexOf("\n\t\t")) : layerHierarchy;
			hierarchy += !layerHierarchy.equals("") ? "\t\t" + layerHierarchy : layerHierarchy;
		}

		return hierarchy;
	}

	/**
	 * @param layer the {@link MapLayer} which hierarchy to print
	 * @return a human readable {@link String} containing the hierarchy of the {@link MapObjects} of the given {@link MapLayer}
	 */
	public String getHierarchy(MapLayer layer) {
		String hierarchy = "";

		for(MapObject object : layer.getObjects()) {
			hierarchy += object.getName() + " (" + object.getClass().getSimpleName() + "):\n";
			Iterator<String> keys = object.getProperties().getKeys();
			while(keys.hasNext()) {
				String key = keys.next();
				hierarchy += "\t" + key + ": " + object.getProperties().get(key) + "\n";
			}
		}

		return hierarchy;
	}

	/** @return the {@link #unitScale} */
	public float getUnitScale() {
		return unitScale;
	}

	/** @param unitScale the {@link #unitScale} to set */
	public void setUnitScale(float unitScale) {
		this.unitScale = unitScale;
	}

	/** @return the {@link #ignoreMapUnitScale} */
	public boolean isIgnoreMapUnitScale() {
		return ignoreMapUnitScale;
	}

	/** @param ignoreMapUnitScale the {@link #ignoreMapUnitScale} to set */
	public void setIgnoreMapUnitScale(boolean ignoreMapUnitScale) {
		this.ignoreMapUnitScale = ignoreMapUnitScale;
	}

	/** @return the {@link #tileWidth} */
	public float getTileWidth() {
		return tileWidth;
	}

	/** @param tileWidth the {@link #tileWidth} to set */
	public void setTileWidth(float tileWidth) {
		this.tileWidth = tileWidth;
	}

	/** @return the {@link #tileHeight} */
	public float getTileHeight() {
		return tileHeight;
	}

	/** @param tileHeight the {@link #tileHeight} to set */
	public void setTileHeight(float tileHeight) {
		this.tileHeight = tileHeight;
	}

	/** @return the {@link Aliases} */
	public Aliases getAliases() {
		return aliases;
	}

	/** @param aliases the {@link Aliases} to set */
	public void setAliases(Aliases aliases) {
		this.aliases = aliases;
	}

	/** @return the parsed {@link #bodies} */
	public ObjectMap<String, Body> getBodies() {
		return bodies;
	}

	/** @return the parsed {@link #fixtures} */
	public ObjectMap<String, Fixture> getFixtures() {
		return fixtures;
	}

	/** @return the parsed {@link #joints} */
	public ObjectMap<String, Joint> getJoints() {
		return joints;
	}

	/** defines the {@link #aliases} to use when parsing */
	public static class Aliases {

		/** the aliases */
		public String
				bodyType = "bodyType",
				dynamicBody = "DynamicBody",
				kinematicBody = "KinematicBody",
				staticBody = "StaticBody",
				active = "active",
				allowSleep = "allowSleep",
				angle = "angle",
				angularDamping = "angularDamping",
				angularVelocity = "angularVelocity",
				awake = "awake",
				bullet = "bullet",
				fixedRotation = "fixedRotation",
				gravityunitScale = "gravityunitScale",
				linearDamping = "linearDamping",
				linearVelocityX = "linearVelocityX",
				linearVelocityY = "linearVelocityY",
				density = "density",
				categoryBits = "categoryBits",
				groupIndex = "groupIndex",
				maskBits = "maskBits",
				friciton = "friction",
				isSensor = "isSensor",
				restitution = "restitution",
				body = "body",
				fixture = "fixture",
				joint = "joint",
				jointType = "jointType",
				distanceJoint = "DistanceJoint",
				frictionJoint = "FrictionJoint",
				gearJoint = "GearJoint",
				mouseJoint = "MouseJoint",
				prismaticJoint = "PrismaticJoint",
				pulleyJoint = "PulleyJoint",
				revoluteJoint = "RevoluteJoint",
				ropeJoint = "RopeJoint",
				weldJoint = "WeldJoint",
				wheelJoint = "WheelJoint",
				bodyA = "bodyA",
				bodyB = "bodyB",
				collideConnected = "collideConnected",
				dampingRatio = "dampingRatio",
				frequencyHz = "frequencyHz",
				length = "length",
				localAnchorAX = "localAnchorAX",
				localAnchorAY = "localAnchorAY",
				localAnchorBX = "localAnchorBX",
				localAnchorBY = "localAnchorBY",
				maxForce = "maxForce",
				maxTorque = "maxTorque",
				joint1 = "joint1",
				joint2 = "joint2",
				ratio = "ratio",
				targetX = "targetX",
				targetY = "targetY",
				enableLimit = "enableLimit",
				enableMotor = "enableMotor",
				localAxisAX = "localAxisAX",
				localAxisAY = "localAxisAY",
				lowerTranslation = "lowerTranslation",
				maxMotorForce = "maxMotorForce",
				motorSpeed = "motorSpeed",
				referenceAngle = "referenceAngle",
				upperTranslation = "upperTranslation",
				groundAnchorAX = "groundAnchorAX",
				groundAnchorAY = "groundAnchorAY",
				groundAnchorBX = "groundAnchorBX",
				groundAnchorBY = "groundAnchorBY",
				lengthA = "lengthA",
				lengthB = "lengthB",
				lowerAngle = "lowerAngle",
				maxMotorTorque = "maxMotorTorque",
				upperAngle = "upperAngle",
				maxLength = "maxLength",
				object = "object",
				unitScale = "unitScale";
	}

}
