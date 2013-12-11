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

import static net.dermetfan.utils.libgdx.maps.MapUtils.getProperty;
import static net.dermetfan.utils.libgdx.math.GeometryUtils.areVerticesClockwise;
import static net.dermetfan.utils.libgdx.math.GeometryUtils.decompose;
import static net.dermetfan.utils.libgdx.math.GeometryUtils.isConvex;
import static net.dermetfan.utils.libgdx.math.GeometryUtils.toFloatArray;
import static net.dermetfan.utils.libgdx.math.GeometryUtils.toVector2Array;
import static net.dermetfan.utils.libgdx.math.GeometryUtils.triangulate;
import static net.dermetfan.utils.libgdx.math.GeometryUtils.vec2_0;

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
import com.badlogic.gdx.math.Ellipse;
import com.badlogic.gdx.math.MathUtils;
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
import com.badlogic.gdx.utils.ObjectMap;

/** An utility class that parses {@link MapObjects} from a {@link Map} and generates Box2D {@link Body Bodies}, {@link Fixture Fixtures} and {@link Joint Joints} from it.<br/>
 *  Just create a new {@link Box2DMapObjectParser} in any way you like and call {@link #load(World, MapLayer)} to load all compatible objects (defined by the {@link Aliases}) into your {@link World}.<br/>
 *  <br/>
 *  If you only want specific Fixtures or Bodies, you can use the {@link #createBody(World, MapObject)} and {@link #createFixture(MapObject)} methods.<br/>
 *  <br/>
 *  How you define compatible objects in the TiledMap editor:<br/>
 *  In your object layer, right-click an object and set its properties to those of the Body/Fixture/both (in case you're creating an {@link Aliases#object object}) you'd like, as defined in the used {@link Aliases} object.<br/>
 *  For type, you have to choose {@link Aliases#body}, {@link Aliases#fixture} or {@link Aliases#object}.<br/>
 *  To add Fixtures to a Body, add a {@link Aliases#body} property with the same value to each Fixture of a Body.<br/>
 *  To create {@link Joint Joints}, add any object to the layer and just put everything needed in its properties. Note that you use the editors unit here which will be converted to Box2D meters automatically using {@link Aliases#unitScale}.
 *  <br/>
 *  For more information visit the <a href="https://bitbucket.org/dermetfan/libgdx-utils/wiki/Box2DMapObjectParser">wiki</a>.
 *  @author dermetfan */
public class Box2DMapObjectParser {

	/** defines the {@link #aliases} to use when parsing */
	public static class Aliases {

		/** the aliases */
		public String x = "x", y = "y", type = "type", bodyType = "bodyType", dynamicBody = "DynamicBody", kinematicBody = "KinematicBody", staticBody = "StaticBody", active = "active", allowSleep = "allowSleep", angle = "angle", angularDamping = "angularDamping", angularVelocity = "angularVelocity", awake = "awake", bullet = "bullet", fixedRotation = "fixedRotation", gravityunitScale = "gravityunitScale", linearDamping = "linearDamping", linearVelocityX = "linearVelocityX", linearVelocityY = "linearVelocityY", density = "density", categoryBits = "categoryBits", groupIndex = "groupIndex", maskBits = "maskBits", friciton = "friction", isSensor = "isSensor", restitution = "restitution", body = "body", fixture = "fixture", joint = "joint", jointType = "jointType", distanceJoint = "DistanceJoint", frictionJoint = "FrictionJoint", gearJoint = "GearJoint", mouseJoint = "MouseJoint", prismaticJoint = "PrismaticJoint", pulleyJoint = "PulleyJoint", revoluteJoint = "RevoluteJoint", ropeJoint = "RopeJoint", weldJoint = "WeldJoint", wheelJoint = "WheelJoint", bodyA = "bodyA", bodyB = "bodyB", collideConnected = "collideConnected", dampingRatio = "dampingRatio", frequencyHz = "frequencyHz", length = "length", localAnchorAX = "localAnchorAX", localAnchorAY = "localAnchorAY", localAnchorBX = "localAnchorBX", localAnchorBY = "localAnchorBY", maxForce = "maxForce", maxTorque = "maxTorque", joint1 = "joint1", joint2 = "joint2", ratio = "ratio", targetX = "targetX", targetY = "targetY", enableLimit = "enableLimit", enableMotor = "enableMotor", localAxisAX = "localAxisAX", localAxisAY = "localAxisAY", lowerTranslation = "lowerTranslation", maxMotorForce = "maxMotorForce", motorSpeed = "motorSpeed", referenceAngle = "referenceAngle", upperTranslation = "upperTranslation", groundAnchorAX = "groundAnchorAX", groundAnchorAY = "groundAnchorAY", groundAnchorBX = "groundAnchorBX", groundAnchorBY = "groundAnchorBY", lengthA = "lengthA", lengthB = "lengthB", lowerAngle = "lowerAngle", maxMotorTorque = "maxMotorTorque", upperAngle = "upperAngle", maxLength = "maxLength", object = "object", unitScale = "unitScale", userData = "userData", tileWidth = "tilewidth", tileHeight = "tileheight";

	}

	/** @see Aliases */
	private Aliases aliases = new Aliases();

	/** the unit scale to convert from editor units to Box2D meters */
	private float unitScale = 1;

	/** if the {@link Aliases#unitScale unit scale} found in the map should be ignored */
	private boolean ignoreMapUnitScale;

	/** if the {@link Aliases#unitScale unit scale} found in the layers should be ignored */
	private boolean ignoreLayerUnitScale;

	/** the dimensions of a tile, used to transform positions (ignore/set to 1 if the used map is not a tile map) */
	private float tileWidth = 1, tileHeight = 1;

	/** if concave polygons should be triangulated instead of being decomposed into convex polygons */
	private boolean triangulate;

	/** the parsed {@link Body Bodies} */
	private ObjectMap<String, Body> bodies = new ObjectMap<String, Body>();

	/** the parsed {@link Fixture Fixtures} */
	private ObjectMap<String, Fixture> fixtures = new ObjectMap<String, Fixture>();

	/** the parsed {@link Joint Joints} */
	private ObjectMap<String, Joint> joints = new ObjectMap<String, Joint>();

	/** the current {@link MapLayer} used in {@link #load(World, MapLayer)} */
	private MapLayer tmpLayer;

	/** creates a new {@link Box2DMapObjectParser} with the default {@link Aliases} */
	public Box2DMapObjectParser() {
	}

	/** creates a new {@link Box2DMapObjectParser} using the given {@link Aliases}
	 *  @param aliases the {@link #aliases} to use */
	public Box2DMapObjectParser(Aliases aliases) {
		this.aliases = aliases;
	}

	/** creates a new {@link Box2DMapObjectParser} using the given {@link Aliases}, {@link #tileWidth} and {@link #tileHeight}
	 *  @param aliases the {@link #aliases}
	 *  @param tileWidth the {@link #tileWidth}
	 *  @param tileHeight the {@link #tileHeight} */
	public Box2DMapObjectParser(Aliases aliases, float tileWidth, float tileHeight) {
		this.aliases = aliases;
		this.tileWidth = tileWidth;
		this.tileHeight = tileHeight;
	}

	/** creates a new {@link Box2DMapObjectParser} using the given {@link #unitScale unitScale} and sets {@link #ignoreMapUnitScale} to true
	 *  @param unitScale the {@link #unitScale unitScale} to use */
	public Box2DMapObjectParser(float unitScale) {
		this.unitScale = unitScale;
	}

	/** creates a new {@link Box2DMapObjectParser} using the given {@link #unitScale}, {@link #tileWidth}, {@link #tileHeight} and sets {@link #ignoreMapUnitScale} to true
	 *  @param unitScale the {@link #unitScale} to use
	 *  @param tileWidth the {@link #tileWidth} to use
	 *  @param tileHeight the {@link #tileHeight} to use */
	public Box2DMapObjectParser(float unitScale, float tileWidth, float tileHeight) {
		this.unitScale = unitScale;
		this.tileWidth = tileWidth;
		this.tileHeight = tileHeight;
	}

	/** creates a new {@link Box2DMapObjectParser} using the given {@link Aliases} and {@link #unitScale} and sets {@link #ignoreMapUnitScale} to true
	 *  @param aliases the {@link #aliases} to use
	 *  @param unitScale the {@link #unitScale} to use */
	public Box2DMapObjectParser(Aliases aliases, float unitScale) {
		this.aliases = aliases;
		this.unitScale = unitScale;
	}

	/** creates a new {@link Box2DMapObjectParser} with the given parameters and sets {@link #ignoreMapUnitScale} to true
	 *  @param aliases the {@link #aliases} to use
	 *  @param unitScale the {@link #unitScale unitScale} to use
	 *  @param tileWidth the {@link #tileWidth} to use
	 *  @param tileHeight the {@link #tileHeight} to use */
	public Box2DMapObjectParser(Aliases aliases, float unitScale, float tileWidth, float tileHeight) {
		this.aliases = aliases;
		this.unitScale = unitScale;
		ignoreMapUnitScale = true;
		this.tileWidth = tileWidth;
		this.tileHeight = tileHeight;
	}

	/** creates the given {@link Map Map's} {@link MapObjects} in the given {@link World}  
	 *  @param world the {@link World} to create the {@link MapObjects} of the given {@link Map} in
	 *  @param map the {@link Map} which {@link MapObjects} to create in the given {@link World}
	 *  @return the given {@link World} with the parsed {@link MapObjects} of the given {@link Map} created in it */
	public World load(World world, Map map) {
		if(!ignoreMapUnitScale)
			unitScale = getProperty(map.getProperties(), aliases.unitScale, unitScale);
		tileWidth = getProperty(map.getProperties(), aliases.tileWidth, tileWidth);
		tileHeight = getProperty(map.getProperties(), aliases.tileHeight, tileHeight);

		for(MapLayer mapLayer : map.getLayers())
			load(world, mapLayer);

		return world;
	}

	/** creates the given {@link MapLayer MapLayer's} {@link MapObjects} in the given {@link World}  
	 *  @param world the {@link World} to create the {@link MapObjects} of the given {@link MapLayer} in
	 *  @param layer the {@link MapLayer} which {@link MapObjects} to create in the given {@link World}
	 *  @return the given {@link World} with the parsed {@link MapObjects} of the given {@link MapLayer} created in it */
	public World load(World world, MapLayer layer) {
		tmpLayer = layer;

		for(MapObject object : layer.getObjects()) {
			if(!ignoreLayerUnitScale)
				unitScale = getProperty(layer.getProperties(), aliases.unitScale, unitScale);
			if(getProperty(object.getProperties(), aliases.type, "").equals(aliases.object))
				createObject(world, object);
		}

		for(MapObject object : layer.getObjects()) {
			if(!ignoreLayerUnitScale)
				unitScale = getProperty(layer.getProperties(), aliases.unitScale, unitScale);
			if(getProperty(object.getProperties(), aliases.type, "").equals(aliases.body))
				createBody(world, object);
		}

		for(MapObject object : layer.getObjects()) {
			if(!ignoreLayerUnitScale)
				unitScale = getProperty(layer.getProperties(), aliases.unitScale, unitScale);
			if(getProperty(object.getProperties(), aliases.type, "").equals(aliases.fixture))
				createFixtures(object);
		}

		for(MapObject object : layer.getObjects()) {
			if(!ignoreLayerUnitScale)
				unitScale = getProperty(layer.getProperties(), aliases.unitScale, unitScale);
			if(getProperty(object.getProperties(), aliases.type, "").equals(aliases.joint))
				createJoint(object);
		}

		return world;
	}

	/** @param world the {@link World} in which to create the Body and Fixtures
	 *  @param object the {@link MapObject} to parse
	 *  @return the created Body
	 *  @see #createBody(World, MapObject)
	 *  @see #createFixtures(MapObject) */
	public Body createObject(World world, MapObject object) {
		Body body = createBody(world, object);
		createFixtures(object);
		return body;
	}

	/** creates a {@link Body} in the given {@link World} from the given {@link MapObject}
	 *  @param world the {@link World} to create the {@link Body} in
	 *  @param mapObject the {@link MapObject} to parse the {@link Body} from
	 *  @return the {@link Body} created in the given {@link World} from the given {@link MapObject} */
	public Body createBody(World world, MapObject mapObject) {
		MapProperties properties = mapObject.getProperties(), layerProperties = tmpLayer.getProperties();

		String type = getProperty(properties, aliases.type, "");
		if(!type.equals(aliases.body) && !type.equals(aliases.object))
			throw new IllegalArgumentException(aliases.type + " of " + mapObject + " is  \"" + type + "\" instead of \"" + aliases.body + "\" or \"" + aliases.object + "\"");

		BodyDef bodyDef = new BodyDef();
		assignProperties(bodyDef, layerProperties);
		assignProperties(bodyDef, properties);

		Body body = world.createBody(bodyDef);
		body.setUserData(getProperty(layerProperties, aliases.userData, body.getUserData()));
		body.setUserData(getProperty(properties, aliases.userData, body.getUserData()));

		bodies.put(findAvailableName(mapObject.getName(), bodies), body);

		return body;
	}

	/** assigns the given {@link MapProperties properties} to the values of the given BodyDef
	 *  @param bodyDef the {@link BodyDef} which values to set according to the given {@link MapProperties}
	 *  @param properties the {@link MapProperties} to assign to the given {@link BodyDef} */
	private void assignProperties(BodyDef bodyDef, MapProperties properties) {
		bodyDef.type = getProperty(properties, aliases.bodyType, "").equals(aliases.staticBody) ? BodyType.StaticBody : getProperty(properties, aliases.bodyType, "").equals(aliases.dynamicBody) ? BodyType.DynamicBody : getProperty(properties, aliases.bodyType, "").equals(aliases.kinematicBody) ? BodyType.KinematicBody : bodyDef.type;
		bodyDef.active = getProperty(properties, aliases.active, bodyDef.active);
		bodyDef.allowSleep = getProperty(properties, aliases.allowSleep, bodyDef.allowSleep);
		bodyDef.angle = getProperty(properties, aliases.angle, bodyDef.angle) * MathUtils.degRad;
		bodyDef.angularDamping = getProperty(properties, aliases.angularDamping, bodyDef.angularDamping);
		bodyDef.angularVelocity = getProperty(properties, aliases.angularVelocity, bodyDef.angularVelocity);
		bodyDef.awake = getProperty(properties, aliases.awake, bodyDef.awake);
		bodyDef.bullet = getProperty(properties, aliases.bullet, bodyDef.bullet);
		bodyDef.fixedRotation = getProperty(properties, aliases.fixedRotation, bodyDef.fixedRotation);
		bodyDef.gravityScale = getProperty(properties, aliases.gravityunitScale, bodyDef.gravityScale);
		bodyDef.linearDamping = getProperty(properties, aliases.linearDamping, bodyDef.linearDamping);
		bodyDef.linearVelocity.set(getProperty(properties, aliases.linearVelocityX, bodyDef.linearVelocity.x), getProperty(properties, aliases.linearVelocityY, bodyDef.linearVelocity.y));
		bodyDef.position.set(getProperty(properties, aliases.x, bodyDef.position.x) * unitScale, getProperty(properties, aliases.y, bodyDef.position.y) * unitScale);
	}

	/** creates a {@link Fixture} from a {@link MapObject}
	 *  @param mapObject the {@link MapObject} to parse
	 *  @return the parsed {@link Fixture} */
	public Fixture createFixture(MapObject mapObject) {
		MapProperties properties = mapObject.getProperties(), layerProperties = tmpLayer.getProperties();

		String type = getProperty(properties, aliases.type, "");

		Body body = bodies.get(type.equals(aliases.object) ? mapObject.getName() : getProperty(properties, aliases.body, ""));

		if(!type.equals(aliases.fixture) && !type.equals(aliases.object))
			throw new IllegalArgumentException(aliases.type + " of " + mapObject + " is  \"" + type + "\" instead of \"" + aliases.fixture + "\" or \"" + aliases.object + "\"");

		FixtureDef fixtureDef = new FixtureDef();
		Shape shape = null;

		if(mapObject instanceof RectangleMapObject) {
			shape = new PolygonShape();
			Rectangle rectangle = ((RectangleMapObject) mapObject).getRectangle();
			float x = rectangle.x * unitScale, y = rectangle.y * unitScale, width = rectangle.width * unitScale, height = rectangle.height * unitScale;
			((PolygonShape) shape).setAsBox(width / 2, height / 2, vec2_0.set(x - body.getPosition().x + width / 2, y - body.getPosition().y + height / 2), body.getAngle());
		} else if(mapObject instanceof PolygonMapObject) {
			shape = new PolygonShape();
			Polygon polygon = new Polygon(((PolygonMapObject) mapObject).getPolygon().getTransformedVertices());
			polygon.setPosition(polygon.getX() * unitScale - body.getPosition().x, polygon.getY() * unitScale - body.getPosition().y);
			polygon.setScale(unitScale, unitScale);
			((PolygonShape) shape).set(polygon.getTransformedVertices());
		} else if(mapObject instanceof PolylineMapObject) {
			shape = new ChainShape();
			Polyline polyline = new Polyline(((PolylineMapObject) mapObject).getPolyline().getTransformedVertices());
			polyline.setPosition(polyline.getX() * unitScale - body.getPosition().x, polyline.getY() * unitScale - body.getPosition().y);
			polyline.setScale(unitScale, unitScale);
			((ChainShape) shape).createChain(polyline.getTransformedVertices());
		} else if(mapObject instanceof CircleMapObject) {
			shape = new CircleShape();
			Circle mapObjectCircle = ((CircleMapObject) mapObject).getCircle();
			Circle circle = new Circle(mapObjectCircle.x, mapObjectCircle.y, mapObjectCircle.radius);
			circle.setPosition(circle.x * unitScale - body.getPosition().x, circle.y * unitScale - body.getPosition().y);
			circle.radius *= unitScale;
			((CircleShape) shape).setPosition(vec2_0.set(circle.x, circle.y));
			((CircleShape) shape).setRadius(circle.radius);
		} else if(mapObject instanceof EllipseMapObject) {
			Ellipse ellipse = ((EllipseMapObject) mapObject).getEllipse();

			if(ellipse.width == ellipse.height) {
				CircleMapObject circleMapObject = new CircleMapObject(ellipse.x + ellipse.width / 2, ellipse.y + ellipse.height / 2, ellipse.width / 2);
				circleMapObject.setName(mapObject.getName());
				circleMapObject.getProperties().putAll(mapObject.getProperties());
				circleMapObject.setColor(mapObject.getColor());
				circleMapObject.setVisible(mapObject.isVisible());
				circleMapObject.setOpacity(mapObject.getOpacity());
				return createFixture(circleMapObject);
			}

			throw new IllegalArgumentException("Cannot parse " + mapObject.getName() + " because " + mapObject.getClass().getSimpleName() + "s that are not circles are not supported");
		} else if(mapObject instanceof TextureMapObject)
			throw new IllegalArgumentException("Cannot parse " + mapObject.getName() + " because " + mapObject.getClass().getSimpleName() + "s are not supported");
		else
			assert false : mapObject + " is a not known subclass of " + MapObject.class.getName();

		fixtureDef.shape = shape;
		assignProperties(fixtureDef, layerProperties);
		assignProperties(fixtureDef, properties);

		Fixture fixture = body.createFixture(fixtureDef);
		fixture.setUserData(getProperty(layerProperties, aliases.userData, fixture.getUserData()));
		fixture.setUserData(getProperty(properties, aliases.userData, fixture.getUserData()));

		shape.dispose();

		fixtures.put(findAvailableName(mapObject.getName(), fixtures), fixture);

		return fixture;
	}

	/** @see #assignProperties(BodyDef, MapProperties) */
	private void assignProperties(FixtureDef fixtureDef, MapProperties properties) {
		fixtureDef.density = getProperty(properties, aliases.density, fixtureDef.density);
		fixtureDef.filter.categoryBits = getProperty(properties, aliases.categoryBits, fixtureDef.filter.categoryBits);
		fixtureDef.filter.groupIndex = getProperty(properties, aliases.groupIndex, fixtureDef.filter.groupIndex);
		fixtureDef.filter.maskBits = getProperty(properties, aliases.maskBits, fixtureDef.filter.maskBits);
		fixtureDef.friction = getProperty(properties, aliases.friciton, fixtureDef.friction);
		fixtureDef.isSensor = getProperty(properties, aliases.isSensor, fixtureDef.isSensor);
		fixtureDef.restitution = getProperty(properties, aliases.restitution, fixtureDef.restitution);
	}

	/** creates {@link Fixture Fixtures} from a {@link MapObject}
	 *  @param mapObject the {@link MapObject} to parse
	 *  @return an array of parsed {@link Fixture Fixtures} */
	public Fixture[] createFixtures(MapObject mapObject) {
		Polygon polygon;

		if(!(mapObject instanceof PolygonMapObject) || isConvex(polygon = ((PolygonMapObject) mapObject).getPolygon()))
			return new Fixture[] {createFixture(mapObject)};

		Polygon[] convexPolygons;
		if(triangulate) {
			if(areVerticesClockwise(polygon)) { // ensure the vertices are in counterclockwise order (not really necessary according to EarClippingTriangulator's javadoc, but sometimes better)
				Array<Vector2> vertices = new Array<Vector2>(toVector2Array(polygon.getVertices()));
				Vector2 first = vertices.removeIndex(0);
				vertices.reverse();
				vertices.insert(0, first);
				polygon.setVertices(toFloatArray(vertices.items));
			}
			convexPolygons = triangulate(polygon);
		} else
			convexPolygons = decompose(polygon);

		// create the fixtures using the convex polygons
		Fixture[] fixtures = new Fixture[convexPolygons.length];
		for(int i = 0; i < fixtures.length; i++) {
			PolygonMapObject convexObject = new PolygonMapObject(convexPolygons[i]);
			convexObject.setColor(mapObject.getColor());
			convexObject.setName(mapObject.getName());
			convexObject.setOpacity(mapObject.getOpacity());
			convexObject.setVisible(mapObject.isVisible());
			convexObject.getProperties().putAll(mapObject.getProperties());
			fixtures[i] = createFixture(convexObject);
		}

		return fixtures;
	}

	/** creates a {@link Joint} from a {@link MapObject}
	 *  @param mapObject the {@link Joint} to parse
	 *  @return the parsed {@link Joint} */
	public Joint createJoint(MapObject mapObject) {
		MapProperties properties = mapObject.getProperties(), layerProperties = tmpLayer.getProperties();

		JointDef jointDef = null;

		String type = getProperty(properties, aliases.type, "");
		if(!type.equals(aliases.joint))
			throw new IllegalArgumentException(aliases.type + " of " + mapObject + " is  \"" + type + "\" instead of \"" + aliases.joint + "\"");

		String jointType = getProperty(properties, aliases.jointType, "");

		if(jointType.equals(aliases.distanceJoint)) {
			DistanceJointDef distanceJointDef = new DistanceJointDef();
			assignProperties(distanceJointDef, layerProperties);
			assignProperties(distanceJointDef, properties);
			jointDef = distanceJointDef;
		} else if(jointType.equals(aliases.frictionJoint)) {
			FrictionJointDef frictionJointDef = new FrictionJointDef();
			assignProperties(frictionJointDef, layerProperties);
			assignProperties(frictionJointDef, properties);
			jointDef = frictionJointDef;
		} else if(jointType.equals(aliases.gearJoint)) {
			GearJointDef gearJointDef = new GearJointDef();
			assignProperties(gearJointDef, layerProperties);
			assignProperties(gearJointDef, properties);
			jointDef = gearJointDef;
		} else if(jointType.equals(aliases.mouseJoint)) {
			MouseJointDef mouseJointDef = new MouseJointDef();
			assignProperties(mouseJointDef, layerProperties);
			assignProperties(mouseJointDef, properties);
			jointDef = mouseJointDef;
		} else if(jointType.equals(aliases.prismaticJoint)) {
			PrismaticJointDef prismaticJointDef = new PrismaticJointDef();
			assignProperties(prismaticJointDef, layerProperties);
			assignProperties(prismaticJointDef, properties);
			jointDef = prismaticJointDef;
		} else if(jointType.equals(aliases.pulleyJoint)) {
			PulleyJointDef pulleyJointDef = new PulleyJointDef();
			assignProperties(pulleyJointDef, layerProperties);
			assignProperties(pulleyJointDef, properties);
			jointDef = pulleyJointDef;
		} else if(jointType.equals(aliases.revoluteJoint)) {
			RevoluteJointDef revoluteJointDef = new RevoluteJointDef();
			assignProperties(revoluteJointDef, layerProperties);
			assignProperties(revoluteJointDef, properties);
			jointDef = revoluteJointDef;
		} else if(jointType.equals(aliases.ropeJoint)) {
			RopeJointDef ropeJointDef = new RopeJointDef();
			assignProperties(ropeJointDef, layerProperties);
			assignProperties(ropeJointDef, properties);
			jointDef = ropeJointDef;
		} else if(jointType.equals(aliases.weldJoint)) {
			WeldJointDef weldJointDef = new WeldJointDef();
			assignProperties(weldJointDef, layerProperties);
			assignProperties(weldJointDef, properties);
			jointDef = weldJointDef;
		} else if(jointType.equals(aliases.wheelJoint)) {
			WheelJointDef wheelJointDef = new WheelJointDef();
			assignProperties(wheelJointDef, layerProperties);
			assignProperties(wheelJointDef, properties);
			jointDef = wheelJointDef;
		}

		jointDef.bodyA = bodies.get(getProperty(properties, aliases.bodyA, ""));
		jointDef.bodyB = bodies.get(getProperty(properties, aliases.bodyB, ""));
		jointDef.collideConnected = getProperty(properties, aliases.collideConnected, jointDef.collideConnected);

		Joint joint = jointDef.bodyA.getWorld().createJoint(jointDef);
		joint.setUserData(getProperty(properties, aliases.userData, joint.getUserData()));

		joints.put(findAvailableName(mapObject.getName(), joints), joint);

		return joint;
	}

	private void assignProperties(DistanceJointDef distanceJointDef, MapProperties properties) {
		distanceJointDef.dampingRatio = getProperty(properties, aliases.dampingRatio, distanceJointDef.dampingRatio);
		distanceJointDef.frequencyHz = getProperty(properties, aliases.frequencyHz, distanceJointDef.frequencyHz);
		distanceJointDef.length = getProperty(properties, aliases.length, distanceJointDef.length) * (tileWidth + tileHeight) / 2 * unitScale;
		distanceJointDef.localAnchorA.set(getProperty(properties, aliases.localAnchorAX, distanceJointDef.localAnchorA.x) * tileWidth * unitScale, getProperty(properties, aliases.localAnchorAY, distanceJointDef.localAnchorA.y) * tileHeight * unitScale);
		distanceJointDef.localAnchorB.set(getProperty(properties, aliases.localAnchorBX, distanceJointDef.localAnchorB.x) * tileWidth * unitScale, getProperty(properties, aliases.localAnchorBY, distanceJointDef.localAnchorB.y) * tileHeight * unitScale);
	}

	private void assignProperties(FrictionJointDef frictionJointDef, MapProperties properties) {
		frictionJointDef.localAnchorA.set(getProperty(properties, aliases.localAnchorAX, frictionJointDef.localAnchorA.x) * tileWidth * unitScale, getProperty(properties, aliases.localAnchorAY, frictionJointDef.localAnchorA.y) * tileHeight * unitScale);
		frictionJointDef.localAnchorB.set(getProperty(properties, aliases.localAnchorBX, frictionJointDef.localAnchorB.x) * tileWidth * unitScale, getProperty(properties, aliases.localAnchorBY, frictionJointDef.localAnchorB.y) * tileHeight * unitScale);
		frictionJointDef.maxForce = getProperty(properties, aliases.maxForce, frictionJointDef.maxForce);
		frictionJointDef.maxTorque = getProperty(properties, aliases.maxTorque, frictionJointDef.maxTorque);
	}

	private void assignProperties(GearJointDef gearJointDef, MapProperties properties) {
		gearJointDef.joint1 = joints.get(getProperty(properties, aliases.joint1, ""));
		gearJointDef.joint2 = joints.get(getProperty(properties, aliases.joint2, ""));
		gearJointDef.ratio = getProperty(properties, aliases.ratio, gearJointDef.ratio);
	}

	private void assignProperties(MouseJointDef mouseJointDef, MapProperties properties) {
		mouseJointDef.dampingRatio = getProperty(properties, aliases.dampingRatio, mouseJointDef.dampingRatio);
		mouseJointDef.frequencyHz = getProperty(properties, aliases.frequencyHz, mouseJointDef.frequencyHz);
		mouseJointDef.maxForce = getProperty(properties, aliases.maxForce, mouseJointDef.maxForce);
		mouseJointDef.target.set(getProperty(properties, aliases.targetX, mouseJointDef.target.x) * tileWidth * unitScale, getProperty(properties, aliases.targetY, mouseJointDef.target.y) * tileHeight * unitScale);
	}

	private void assignProperties(PrismaticJointDef prismaticJointDef, MapProperties properties) {
		prismaticJointDef.enableLimit = getProperty(properties, aliases.enableLimit, prismaticJointDef.enableLimit);
		prismaticJointDef.enableMotor = getProperty(properties, aliases.enableMotor, prismaticJointDef.enableMotor);
		prismaticJointDef.localAnchorA.set(getProperty(properties, aliases.localAnchorAX, prismaticJointDef.localAnchorA.x) * tileWidth * unitScale, getProperty(properties, aliases.localAnchorAY, prismaticJointDef.localAnchorA.y) * tileHeight * unitScale);
		prismaticJointDef.localAnchorB.set(getProperty(properties, aliases.localAnchorBX, prismaticJointDef.localAnchorB.x) * tileWidth * unitScale, getProperty(properties, aliases.localAnchorBY, prismaticJointDef.localAnchorB.y) * tileHeight * unitScale);
		prismaticJointDef.localAxisA.set(getProperty(properties, aliases.localAxisAX, prismaticJointDef.localAxisA.x), getProperty(properties, aliases.localAxisAY, prismaticJointDef.localAxisA.y));
		prismaticJointDef.lowerTranslation = getProperty(properties, aliases.lowerTranslation, prismaticJointDef.lowerTranslation) * (tileWidth + tileHeight) / 2 * unitScale;
		prismaticJointDef.maxMotorForce = getProperty(properties, aliases.maxMotorForce, prismaticJointDef.maxMotorForce);
		prismaticJointDef.motorSpeed = getProperty(properties, aliases.motorSpeed, prismaticJointDef.motorSpeed);
		prismaticJointDef.referenceAngle = getProperty(properties, aliases.referenceAngle, prismaticJointDef.referenceAngle) * MathUtils.degRad;
		prismaticJointDef.upperTranslation = getProperty(properties, aliases.upperTranslation, prismaticJointDef.upperTranslation) * (tileWidth + tileHeight) / 2 * unitScale;
	}

	private void assignProperties(PulleyJointDef pulleyJointDef, MapProperties properties) {
		pulleyJointDef.groundAnchorA.set(getProperty(properties, aliases.groundAnchorAX, pulleyJointDef.groundAnchorA.x) * tileWidth * unitScale, getProperty(properties, aliases.groundAnchorAY, pulleyJointDef.groundAnchorA.y) * tileHeight * unitScale);
		pulleyJointDef.groundAnchorB.set(getProperty(properties, aliases.groundAnchorBX, pulleyJointDef.groundAnchorB.x) * tileWidth * unitScale, getProperty(properties, aliases.groundAnchorBY, pulleyJointDef.groundAnchorB.y) * tileHeight * unitScale);
		pulleyJointDef.lengthA = getProperty(properties, aliases.lengthA, pulleyJointDef.lengthA) * (tileWidth + tileHeight) / 2 * unitScale;
		pulleyJointDef.lengthB = getProperty(properties, aliases.lengthB, pulleyJointDef.lengthB) * (tileWidth + tileHeight) / 2 * unitScale;
		pulleyJointDef.localAnchorA.set(getProperty(properties, aliases.localAnchorAX, pulleyJointDef.localAnchorA.x) * tileWidth * unitScale, getProperty(properties, aliases.localAnchorAY, pulleyJointDef.localAnchorA.y) * tileHeight * unitScale);
		pulleyJointDef.localAnchorB.set(getProperty(properties, aliases.localAnchorBX, pulleyJointDef.localAnchorB.x) * tileWidth * unitScale, getProperty(properties, aliases.localAnchorBY, pulleyJointDef.localAnchorB.y) * tileHeight * unitScale);
		pulleyJointDef.ratio = getProperty(properties, aliases.ratio, pulleyJointDef.ratio);
	}

	private void assignProperties(RevoluteJointDef revoluteJointDef, MapProperties properties) {
		revoluteJointDef.enableLimit = getProperty(properties, aliases.enableLimit, revoluteJointDef.enableLimit);
		revoluteJointDef.enableMotor = getProperty(properties, aliases.enableMotor, revoluteJointDef.enableMotor);
		revoluteJointDef.localAnchorA.set(getProperty(properties, aliases.localAnchorAX, revoluteJointDef.localAnchorA.x) * tileWidth * unitScale, getProperty(properties, aliases.localAnchorAY, revoluteJointDef.localAnchorA.y) * tileHeight * unitScale);
		revoluteJointDef.localAnchorB.set(getProperty(properties, aliases.localAnchorBX, revoluteJointDef.localAnchorB.x) * tileWidth * unitScale, getProperty(properties, aliases.localAnchorBY, revoluteJointDef.localAnchorB.y) * tileHeight * unitScale);
		revoluteJointDef.lowerAngle = getProperty(properties, aliases.lowerAngle, revoluteJointDef.lowerAngle) * MathUtils.degRad;
		revoluteJointDef.maxMotorTorque = getProperty(properties, aliases.maxMotorTorque, revoluteJointDef.maxMotorTorque);
		revoluteJointDef.motorSpeed = getProperty(properties, aliases.motorSpeed, revoluteJointDef.motorSpeed);
		revoluteJointDef.referenceAngle = getProperty(properties, aliases.referenceAngle, revoluteJointDef.referenceAngle) * MathUtils.degRad;
		revoluteJointDef.upperAngle = getProperty(properties, aliases.upperAngle, revoluteJointDef.upperAngle) * MathUtils.degRad;
	}

	private void assignProperties(RopeJointDef ropeJointDef, MapProperties properties) {
		ropeJointDef.localAnchorA.set(getProperty(properties, aliases.localAnchorAX, ropeJointDef.localAnchorA.x) * tileWidth * unitScale, getProperty(properties, aliases.localAnchorAY, ropeJointDef.localAnchorA.y) * tileHeight * unitScale);
		ropeJointDef.localAnchorB.set(getProperty(properties, aliases.localAnchorBX, ropeJointDef.localAnchorB.x) * tileWidth * unitScale, getProperty(properties, aliases.localAnchorBY, ropeJointDef.localAnchorB.y) * tileHeight * unitScale);
		ropeJointDef.maxLength = getProperty(properties, aliases.maxLength, ropeJointDef.maxLength) * (tileWidth + tileHeight) / 2 * unitScale;
	}

	private void assignProperties(WeldJointDef weldJointDef, MapProperties properties) {
		weldJointDef.localAnchorA.set(getProperty(properties, aliases.localAnchorAX, weldJointDef.localAnchorA.x) * tileWidth * unitScale, getProperty(properties, aliases.localAnchorAY, weldJointDef.localAnchorA.y) * tileHeight * unitScale);
		weldJointDef.localAnchorB.set(getProperty(properties, aliases.localAnchorBX, weldJointDef.localAnchorB.x) * tileWidth * unitScale, getProperty(properties, aliases.localAnchorBY, weldJointDef.localAnchorB.y) * tileHeight * unitScale);
		weldJointDef.referenceAngle = getProperty(properties, aliases.referenceAngle, weldJointDef.referenceAngle) * MathUtils.degRad;
	}

	private void assignProperties(WheelJointDef wheelJointDef, MapProperties properties) {
		wheelJointDef.dampingRatio = getProperty(properties, aliases.dampingRatio, wheelJointDef.dampingRatio);
		wheelJointDef.enableMotor = getProperty(properties, aliases.enableMotor, wheelJointDef.enableMotor);
		wheelJointDef.frequencyHz = getProperty(properties, aliases.frequencyHz, wheelJointDef.frequencyHz);
		wheelJointDef.localAnchorA.set(getProperty(properties, aliases.localAnchorAX, wheelJointDef.localAnchorA.x) * tileWidth * unitScale, getProperty(properties, aliases.localAnchorAY, wheelJointDef.localAnchorA.y) * tileHeight * unitScale);
		wheelJointDef.localAnchorB.set(getProperty(properties, aliases.localAnchorBX, wheelJointDef.localAnchorB.x) * tileWidth * unitScale, getProperty(properties, aliases.localAnchorBY, wheelJointDef.localAnchorB.y) * tileHeight * unitScale);
		wheelJointDef.localAxisA.set(getProperty(properties, aliases.localAxisAX, wheelJointDef.localAxisA.x), getProperty(properties, aliases.localAxisAY, wheelJointDef.localAxisA.y));
		wheelJointDef.maxMotorTorque = getProperty(properties, aliases.maxMotorTorque, wheelJointDef.maxMotorTorque);
		wheelJointDef.motorSpeed = getProperty(properties, aliases.motorSpeed, wheelJointDef.motorSpeed);
	}

	/** @return the desiredName if it was available, otherwise desiredName with a number appended */
	public String findAvailableName(String desiredName, ObjectMap<String, ?> map) {
		if(map.containsKey(desiredName)) {
			int duplicate = 1;
			while(map.containsKey(desiredName + duplicate))
				duplicate++;
			desiredName += duplicate;
		}
		return desiredName;
	}

	public void reset() {
		aliases = new Aliases();
		unitScale = 1;
		tileWidth = 1;
		tileHeight = 1;
		triangulate = false;
		bodies.clear();
		fixtures.clear();
		joints.clear();
		tmpLayer = null;
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

	/** @return the {@link #ignoreLayerUnitScale} */
	public boolean isIgnoreLayerUnitScale() {
		return ignoreLayerUnitScale;
	}

	/** @param ignoreLayerUnitScale the {@link #ignoreLayerUnitScale} to set */
	public void setIgnoreLayerUnitScale(boolean ignoreLayerUnitScale) {
		this.ignoreLayerUnitScale = ignoreLayerUnitScale;
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

	/** @return the {@link #triangulate} */
	public boolean isTriangulate() {
		return triangulate;
	}

	/** @param triangulate the {@link #triangulate} to set */
	public void setTriangulate(boolean triangulate) {
		this.triangulate = triangulate;
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

}
