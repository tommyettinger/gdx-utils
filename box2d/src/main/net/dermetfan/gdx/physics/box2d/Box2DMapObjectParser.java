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

import com.badlogic.gdx.maps.Map;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapLayers;
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
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Polygon;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.JointDef;
import com.badlogic.gdx.physics.box2d.JointDef.JointType;
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
import com.badlogic.gdx.utils.Pools;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import net.dermetfan.gdx.physics.box2d.Box2DMapObjectParser.Listener.Adapter;

import static net.dermetfan.gdx.maps.MapUtils.findProperty;
import static net.dermetfan.gdx.maps.MapUtils.getProperty;
import static net.dermetfan.gdx.math.GeometryUtils.decompose;
import static net.dermetfan.gdx.math.GeometryUtils.isConvex;
import static net.dermetfan.gdx.math.GeometryUtils.triangulate;

/** Parses {@link MapObjects} from a {@link Map} and generates Box2D {@link Body Bodies}, {@link Fixture Fixtures} and {@link Joint Joints} from them.<br>
 *  Just create a new {@link Box2DMapObjectParser} and call {@link #load(World, MapLayer)} to load all compatible objects (defined by the the {@link #aliases}) into your {@link World}.<br>
 *  <br>
 *  If you only want specific Fixtures or Bodies, you can use the {@link #createBody(World, MapObject)} and {@link #createFixture(MapObject)} methods.<br>
 *  <br>
 *  How you define compatible objects in the TiledMap editor:<br>
 *  In your object layer, right-click an object and set its properties to those of the Body/Fixture/both (in case you're creating an {@link Aliases#object object}) you'd like, as defined in the used {@link Aliases} object.<br>
 *  For type, you have to choose {@link Aliases#body}, {@link Aliases#fixture} or {@link Aliases#object}.<br>
 *  To add Fixtures to a Body, add a {@link Aliases#body} property with the same value to each Fixture of a Body.<br>
 *  To create {@link Joint Joints}, add any object to the layer and just put everything needed in its properties. Note that you use the editors unit here which will be converted to Box2D meters automatically using {@link Aliases#unitScale}.
 *  <br>
 *  For more information read the <a href="http://bitbucket.org/dermetfan/libgdx-utils/wiki/Box2DMapObjectParser">wiki</a>.
 *  @author dermetfan */
public class Box2DMapObjectParser {

	/** defines the {@link #aliases} to use when parsing */
	public static class Aliases {

		/** the aliases */
		public String x = "x", y = "y", width = "width", height = "height", type = "type", bodyType = "bodyType", dynamicBody = "DynamicBody", kinematicBody = "KinematicBody", staticBody = "StaticBody", active = "active", allowSleep = "allowSleep", angle = "angle", angularDamping = "angularDamping", angularVelocity = "angularVelocity", awake = "awake", bullet = "bullet", fixedRotation = "fixedRotation", gravityScale = "gravityScale", linearDamping = "linearDamping", linearVelocityX = "linearVelocityX", linearVelocityY = "linearVelocityY", density = "density", categoryBits = "categoryBits", groupIndex = "groupIndex", maskBits = "maskBits", friciton = "friction", isSensor = "isSensor", restitution = "restitution", body = "body", fixture = "fixture", joint = "joint", jointType = "jointType", distanceJoint = "DistanceJoint", frictionJoint = "FrictionJoint", gearJoint = "GearJoint", mouseJoint = "MouseJoint", prismaticJoint = "PrismaticJoint", pulleyJoint = "PulleyJoint", revoluteJoint = "RevoluteJoint", ropeJoint = "RopeJoint", weldJoint = "WeldJoint", wheelJoint = "WheelJoint", bodyA = "bodyA", bodyB = "bodyB", collideConnected = "collideConnected", dampingRatio = "dampingRatio", frequencyHz = "frequencyHz", length = "length", localAnchorAX = "localAnchorAX", localAnchorAY = "localAnchorAY", localAnchorBX = "localAnchorBX", localAnchorBY = "localAnchorBY", maxForce = "maxForce", maxTorque = "maxTorque", joint1 = "joint1", joint2 = "joint2", ratio = "ratio", targetX = "targetX", targetY = "targetY", enableLimit = "enableLimit", enableMotor = "enableMotor", localAxisAX = "localAxisAX", localAxisAY = "localAxisAY", lowerTranslation = "lowerTranslation", maxMotorForce = "maxMotorForce", motorSpeed = "motorSpeed", referenceAngle = "referenceAngle", upperTranslation = "upperTranslation", groundAnchorAX = "groundAnchorAX", groundAnchorAY = "groundAnchorAY", groundAnchorBX = "groundAnchorBX", groundAnchorBY = "groundAnchorBY", lengthA = "lengthA", lengthB = "lengthB", lowerAngle = "lowerAngle", maxMotorTorque = "maxMotorTorque", upperAngle = "upperAngle", maxLength = "maxLength", object = "object", unitScale = "unitScale", userData = "userData", tileWidth = "tilewidth", tileHeight = "tileheight", gravityX = "gravityX", gravityY = "gravityY", autoClearForces = "autoClearForces", orientation = "orientation", orthogonal = "orthogonal", isometric = "isometric", staggered = "staggered";

	}

	/** Allows modification of {@link MapObject MapObjects} before they are used to create Box2D objects.<br>
	 *  <strong>Note that the map object given to you is the one directly from the map, so if you modify it, you modify the {@link Map} instance! If you want to avoid that, make a copy.</strong><br>
	 *  Also listens to Box2D objects that have been created.
	 *  @author dermetfan */
	public interface Listener {

		/** @param parser the {@link Box2DMapObjectParser} instance that is going to {@link Box2DMapObjectParser#load(World, Map) process} a map */
		public void init(Box2DMapObjectParser parser);

		/** @param map the {@link Map} to load from
		 *  @param queue the {@link MapLayer MapLayers} to actually parse */
		public void load(Map map, Array<MapLayer> queue);

		/** @param layer the {@link MapObject MapObjects} in the layer
		 *  @param queue the {@link MapObject MapObjects} to actually parse */
		public void load(MapLayer layer, Array<MapObject> queue);

		/** @param mapObject the map object to create an object from
		 *  @return the map object to create an object from, null to cancel the creation */
		public MapObject createObject(MapObject mapObject);

		/** @param mapObject the map object to create a body from
		 *  @return the map object to create a body from, null to cancel the creation */
		public MapObject createBody(MapObject mapObject);

		/** @param mapObject the map object to create fixtures from
		 *  @return the map object to create fixtures from, null to cancel the creation */
		public MapObject createFixtures(MapObject mapObject);

		/** @param mapObject the map object to create a fixture from
		 *  @return the map object to create a fixture from, null to cancel the creation */
		public MapObject createFixture(MapObject mapObject);

		/** @param mapObject the map object to create a joint from
		 *  @return the map object to create a joint from, null to cancel the creation */
		public MapObject createJoint(MapObject mapObject);

		/** @param body the created body
		 *  @param mapObject the map object used to create the body */
		public void created(Body body, MapObject mapObject);

		/** @param fixture the created fixture
		 *  @param mapObject the map object used to create the fixture */
		public void created(Fixture fixture, MapObject mapObject);

		/** @param joint the created joint
		 *  @param mapObject the map object used to create the joint */
		public void created(Joint joint, MapObject mapObject);

		/** Does nothing. Subclass this if you only want to override only some methods.
		 *  @author dermetfan */
		public static class Adapter implements Listener {

			/** does nothing */
			@Override
			public void init(Box2DMapObjectParser parser) {}

			/** adds all layers to the processing queue */
			@Override
			public void load(Map map, Array<MapLayer> queue) {
				MapLayers layers = map.getLayers();
				queue.ensureCapacity(layers.getCount());
				for(MapLayer layer : layers)
					queue.add(layer);
			}

			/** adds all map objects to the parsing queue */
			@Override
			public void load(MapLayer layer, Array<MapObject> queue) {
				MapObjects objects = layer.getObjects();
				queue.ensureCapacity(objects.getCount());
				for(MapObject object : objects)
					queue.add(object);
			}

			/** @return the given map object */
			@Override
			public MapObject createObject(MapObject mapObject) {
				return mapObject;
			}

			/** @return the given map object */
			@Override
			public MapObject createBody(MapObject mapObject) {
				return mapObject;
			}

			/** @return the given map object */
			@Override
			public MapObject createFixtures(MapObject mapObject) {
				return mapObject;
			}

			/** @return the given map object */
			@Override
			public MapObject createFixture(MapObject mapObject) {
				return mapObject;
			}

			/** @return the given map object */
			@Override
			public MapObject createJoint(MapObject mapObject) {
				return mapObject;
			}

			/** does nothing */
			@Override
			public void created(Body body, MapObject mapObject) {}

			/** does nothing */
			@Override
			public void created(Fixture fixture, MapObject mapObject) {}

			/** does nothing */
			@Override
			public void created(Joint joint, MapObject mapObject) {}

		}

	}

	/** @see Aliases */
	private Aliases aliases = new Aliases();

	/** the {@link Listener} used by default (an {@link Adapter} instance) */
	public static final Adapter defaultListener = new Adapter();

	/** the {@link Listener} to use ({@link #defaultListener} by default) */
	private Listener listener = defaultListener;

	/** the unit scale to convert from editor units to Box2D meters */
	private float unitScale = 1;

	/** if the {@link Aliases#unitScale unit scale} found in the map should be ignored */
	private boolean ignoreMapUnitScale;

	/** if the {@link Aliases#unitScale unit scale} found in the layers should be ignored */
	private boolean ignoreLayerUnitScale;

	/** the dimensions of a tile, used to transform positions (ignore/set to 1 if the map is not a tile map) */
	private float tileWidth = 1, tileHeight = 1;

	/** if concave polygons should be triangulated instead of being decomposed into convex polygons */
	private boolean triangulate;

	/** the properties {@link MapObject MapObjects} will inherit in {@link #createBody(World, MapObject)}, {@link #createFixture(MapObject)} and {@link #createJoint(MapObject)} */
	private MapProperties heritage;

	// state variables

	/** the parsed {@link Body Bodies} */
	private ObjectMap<String, Body> bodies = new ObjectMap<>();

	/** the parsed {@link Fixture Fixtures} */
	private ObjectMap<String, Fixture> fixtures = new ObjectMap<>();

	/** the parsed {@link Joint Joints} */
	private ObjectMap<String, Joint> joints = new ObjectMap<>();

	/** the {@link MapProperties} of the currently {@link #load(World, Map) loading} map */
	private MapProperties mapProperties;

	/** the {@link MapProperties} of the currently {@link #load(World, MapLayer) loading} layer */
	private MapProperties layerProperties;

	// temporary variables

	/** for internal, temporary usage */
	private final Vector2 vec2 = new Vector2();

	/** for internal, temporary usage */
	private final Vector3 vec3 = new Vector3();

	/** for internal, temporary usage */
	private final Matrix4 mat4 = new Matrix4();

	/** creates a new {@link Box2DMapObjectParser} with the default {@link Aliases} */
	public Box2DMapObjectParser() {}

	/** creates a new {@link Box2DMapObjectParser} using the given {@link Aliases}
	 *  @param aliases the {@link #aliases} to use */
	public Box2DMapObjectParser(Aliases aliases) {
		this.aliases = aliases;
	}

	/** creates a new {@link Box2DMapObjectParser} using the given {@link Listener}
	 *  @param listener the {@link #listener} to use */
	public Box2DMapObjectParser(Listener listener) {
		this.listener = listener;
	}

	/** creates a new {@link Box2DMapObjectParser} using the given {@link Aliases} and {@link Listener}
	 *  @param aliases the {@link #aliases}
	 *  @param listener the {@link #listener} */
	public Box2DMapObjectParser(Aliases aliases, Listener listener) {
		this.aliases = aliases;
		this.listener = listener;
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

	/** creates a new {@link Box2DMapObjectParser} using the given {@link Listener}, {@link #tileWidth} and {@link #tileHeight}
	 *  @param listener the {@link #listener}
	 *  @param tileWidth the {@link #tileWidth}
	 *  @param tileHeight the {@link #tileHeight} */
	public Box2DMapObjectParser(Listener listener, float tileWidth, float tileHeight) {
		this.listener = listener;
		this.tileWidth = tileWidth;
		this.tileHeight = tileHeight;
	}

	/** creates a new {@link Box2DMapObjectParser} using the given {@link Listener}, {@link Aliases}, {@link #tileWidth} and {@link #tileHeight}
	 *  @param aliases the {@link #aliases}
	 *  @param listener the {@link #listener}
	 *  @param tileWidth the {@link #tileWidth}
	 *  @param tileHeight the {@link #tileHeight} */
	public Box2DMapObjectParser(Aliases aliases, Listener listener, float tileWidth, float tileHeight) {
		this.aliases = aliases;
		this.listener = listener;
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

	/** creates a new {@link Box2DMapObjectParser} using the given {@link Listener} and {@link #unitScale} and sets {@link #ignoreMapUnitScale} to true
	 *  @param listener the {@link #listener} to use
	 *  @param unitScale the {@link #unitScale} to use */
	public Box2DMapObjectParser(Listener listener, float unitScale) {
		this.listener = listener;
		this.unitScale = unitScale;
	}

	/** creates a new {@link Box2DMapObjectParser} using the given {@link Aliases}, {@link Listener} and {@link #unitScale} and sets {@link #ignoreMapUnitScale} to true
	 *  @param aliases the {@link #aliases} to use
	 *  @param listener the {@link #listener} to use
	 *  @param unitScale the {@link #unitScale} to use */
	public Box2DMapObjectParser(Aliases aliases, Listener listener, float unitScale) {
		this.aliases = aliases;
		this.listener = listener;
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

	/** creates a new {@link Box2DMapObjectParser} with the given parameters and sets {@link #ignoreMapUnitScale} to true
	 *  @param listener the {@link #listener} to use
	 *  @param unitScale the {@link #unitScale unitScale} to use
	 *  @param tileWidth the {@link #tileWidth} to use
	 *  @param tileHeight the {@link #tileHeight} to use */
	public Box2DMapObjectParser(Listener listener, float unitScale, float tileWidth, float tileHeight) {
		this.listener = listener;
		this.unitScale = unitScale;
		ignoreMapUnitScale = true;
		this.tileWidth = tileWidth;
		this.tileHeight = tileHeight;
	}

	/** creates a new {@link Box2DMapObjectParser} with the given parameters and sets {@link #ignoreMapUnitScale} to true
	 *  @param aliases the {@link #aliases} to use
	 *  @param listener the {@link #listener} to use
	 *  @param unitScale the {@link #unitScale unitScale} to use
	 *  @param tileWidth the {@link #tileWidth} to use
	 *  @param tileHeight the {@link #tileHeight} to use */
	public Box2DMapObjectParser(Listener listener, Aliases aliases, float unitScale, float tileWidth, float tileHeight) {
		this.aliases = aliases;
		this.listener = listener;
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
		MapProperties oldMapProperties = mapProperties;
		mapProperties = map.getProperties();

		world.setGravity(vec2.set(getProperty(mapProperties, aliases.gravityX, world.getGravity().x), getProperty(mapProperties, aliases.gravityY, world.getGravity().y)));
		world.setAutoClearForces(getProperty(mapProperties, aliases.autoClearForces, world.getAutoClearForces()));

		if(!ignoreMapUnitScale)
			unitScale = getProperty(mapProperties, aliases.unitScale, unitScale);
		tileWidth = getProperty(mapProperties, aliases.tileWidth, tileWidth);
		tileHeight = getProperty(mapProperties, aliases.tileHeight, tileHeight);

		listener.init(this);

		@SuppressWarnings("unchecked")
		Array<MapLayer> layers = Pools.obtain(Array.class);
		layers.clear();
		listener.load(map, layers);

		for(MapLayer mapLayer : layers)
			load(world, mapLayer);

		layers.clear();
		Pools.free(layers);

		mapProperties = oldMapProperties;
		return world;
	}

	/** creates the given {@link MapLayer MapLayer's} {@link MapObjects} in the given {@link World}
	 *  @param world the {@link World} to create the {@link MapObjects} of the given {@link MapLayer} in
	 *  @param layer the {@link MapLayer} which {@link MapObjects} to create in the given {@link World}
	 *  @return the given {@link World} with the parsed {@link MapObjects} of the given {@link MapLayer} created in it */
	public World load(World world, MapLayer layer) {
		MapProperties oldLayerProperties = layerProperties;
		layerProperties = layer.getProperties();

		float oldUnitScale = unitScale;
		if(!ignoreLayerUnitScale)
			unitScale = getProperty(layer.getProperties(), aliases.unitScale, unitScale);

		String typeFallback = findProperty(aliases.type, "", heritage, mapProperties, layerProperties);

		@SuppressWarnings("unchecked")
		Array<MapObject> objects = Pools.obtain(Array.class);
		objects.clear();
		listener.load(layer, objects);

		for(MapObject object : objects) {
			String type = getProperty(object.getProperties(), aliases.type, typeFallback);
			if(type.equals(aliases.object))
				createObject(world, object);
		}

		for(MapObject object : objects) {
			String type = getProperty(object.getProperties(), aliases.type, typeFallback);
			if(type.equals(aliases.body))
				createBody(world, object);
		}

		for(MapObject object : objects) {
			String type = getProperty(object.getProperties(), aliases.type, typeFallback);
			if(type.equals(aliases.fixture))
				createFixtures(object);
		}

		for(MapObject object : objects) {
			String type = getProperty(object.getProperties(), aliases.type, typeFallback);
			if(type.equals(aliases.joint))
				createJoint(object);
		}

		objects.clear();
		Pools.free(objects);

		layerProperties = oldLayerProperties;
		unitScale = oldUnitScale;
		return world;
	}

	/** @param world the {@link World} in which to create the Body and Fixtures
	 *  @param object the {@link MapObject} to parse
	 *  @return the created Body
	 *  @see #createBody(World, MapObject)
	 *  @see #createFixtures(MapObject) */
	public Body createObject(World world, MapObject object) {
		if((object = listener.createObject(object)) == null)
			return null;
		Body body = createBody(world, object);
		createFixtures(object, body);
		return body;
	}

	/** creates a {@link Body} in the given {@link World} from the given {@link MapObject}
	 *  @param world the {@link World} to create the {@link Body} in
	 *  @param mapObject the {@link MapObject} to parse the {@link Body} from
	 *  @return the {@link Body} created in the given {@link World} from the given {@link MapObject} */
	public Body createBody(World world, MapObject mapObject) {
		if(listener.createBody(mapObject) == null)
			return null;

		MapProperties properties = mapObject.getProperties();

		BodyDef bodyDef = new BodyDef();
		assignProperties(bodyDef, heritage);
		assignProperties(bodyDef, mapProperties);
		assignProperties(bodyDef, layerProperties);
		assignProperties(bodyDef, properties);

		Body body = world.createBody(bodyDef);
		body.setUserData(findProperty(aliases.userData, body.getUserData(), heritage, mapProperties, layerProperties, properties));

		bodies.put(findAvailableName(mapObject.getName(), bodies), body);
		listener.created(body, mapObject);

		return body;
	}

	/** creates a {@link Fixture} from a {@link MapObject}
	 *  @param mapObject the {@link MapObject} to parse
	 *  @param body the {@link Body} to create the {@link Fixture Fixtures} on
	 *  @return the parsed {@link Fixture} */
	public Fixture createFixture(MapObject mapObject, Body body) {
		if((mapObject = listener.createFixture(mapObject)) == null)
			return null;

		String orientation = findProperty(aliases.orientation, aliases.orthogonal, heritage, mapProperties, layerProperties, mapObject.getProperties());
		transform(mat4, orientation);

		Shape shape = null;
		if(mapObject instanceof RectangleMapObject) {
			Rectangle rectangle = ((RectangleMapObject) mapObject).getRectangle();
			vec3.set(rectangle.x, rectangle.y, 0);
			vec3.mul(mat4);
			float x = vec3.x, y = vec3.y, width, height;
			if(!orientation.equals(aliases.staggered)) {
				vec3.set(rectangle.width, rectangle.height, 0).mul(mat4);
				width = vec3.x;
				height = vec3.y;
			} else {
				width = rectangle.width * unitScale;
				height = rectangle.height * unitScale;
			}
			((PolygonShape) (shape = new PolygonShape())).setAsBox(width / 2, height / 2, vec2.set(x - body.getPosition().x + width / 2, y - body.getPosition().y + height / 2), body.getAngle());
		} else if(mapObject instanceof PolygonMapObject || mapObject instanceof PolylineMapObject) {
			FloatArray vertices = Pools.obtain(FloatArray.class);
			vertices.clear();
			vertices.addAll(mapObject instanceof PolygonMapObject ? ((PolygonMapObject) mapObject).getPolygon().getTransformedVertices() : ((PolylineMapObject) mapObject).getPolyline().getTransformedVertices());
			for(int ix = 0, iy = 1; iy < vertices.size; ix += 2, iy += 2) {
				vec3.set(vertices.get(ix), vertices.get(iy), 0);
				vec3.mul(mat4);
				vertices.set(ix, vec3.x - body.getPosition().x);
				vertices.set(iy, vec3.y - body.getPosition().y);
			}
			if(mapObject instanceof PolygonMapObject)
				((PolygonShape) (shape = new PolygonShape())).set(vertices.items, 0, vertices.size);
			else if(vertices.size == 4)
				((EdgeShape) (shape = new EdgeShape())).set(vertices.get(0), vertices.get(1), vertices.get(2), vertices.get(3));
			else {
				vertices.shrink();
				((ChainShape) (shape = new ChainShape())).createChain(vertices.items);
			}
			Pools.free(vertices);
		} else if(mapObject instanceof CircleMapObject || mapObject instanceof EllipseMapObject) {
			if(mapObject instanceof CircleMapObject) {
				Circle circle = ((CircleMapObject) mapObject).getCircle();
				vec3.set(circle.x, circle.y, circle.radius);
			} else {
				Ellipse ellipse = ((EllipseMapObject) mapObject).getEllipse();
				if(ellipse.width != ellipse.height)
					throw new IllegalArgumentException("Cannot parse " + mapObject.getName() + " because " + ClassReflection.getSimpleName(mapObject.getClass()) + "s that are not circles are not supported");
				vec3.set(ellipse.x + ellipse.width / 2, ellipse.y + ellipse.height / 2, ellipse.width / 2);
			}
			vec3.mul(mat4);
			vec3.sub(body.getPosition().x, body.getPosition().y, 0);
			CircleShape circleShape = (CircleShape) (shape = new CircleShape());
			circleShape.setPosition(vec2.set(vec3.x, vec3.y));
			circleShape.setRadius(vec3.z);
		} else if(mapObject instanceof TextureMapObject)
			throw new IllegalArgumentException("Cannot parse " + mapObject.getName() + " because " + ClassReflection.getSimpleName(mapObject.getClass()) + "s are not supported");
		else
			assert false : mapObject + " is a not known subclass of " + MapObject.class.getName();

		MapProperties properties = mapObject.getProperties();

		FixtureDef fixtureDef = new FixtureDef();
		fixtureDef.shape = shape;
		assignProperties(fixtureDef, heritage);
		assignProperties(fixtureDef, mapProperties);
		assignProperties(fixtureDef, layerProperties);
		assignProperties(fixtureDef, properties);

		Fixture fixture = body.createFixture(fixtureDef);
		fixture.setUserData(findProperty(aliases.userData, fixture.getUserData(), heritage, mapProperties, layerProperties, properties));

		shape.dispose();

		fixtures.put(findAvailableName(mapObject.getName(), fixtures), fixture);
		listener.created(fixture, mapObject);

		return fixture;
	}

	/** creates {@link Fixture Fixtures} from a {@link MapObject}
	 *  @param mapObject the {@link MapObject} to parse
	 *  @param body the {@link Body} to create the {@link Fixture Fixtures} on
	 *  @return an array of parsed {@link Fixture Fixtures} */
	public Fixture[] createFixtures(MapObject mapObject, Body body) {
		if((mapObject = listener.createFixtures(mapObject)) == null)
			return null;

		Polygon polygon;

		if(!(mapObject instanceof PolygonMapObject) || isConvex(polygon = ((PolygonMapObject) mapObject).getPolygon()) && (!Box2DUtils.checkPreconditions || polygon.getVertices().length / 2 <= Box2DUtils.maxPolygonVertices))
			return new Fixture[] {createFixture(mapObject, body)};

		Polygon[] convexPolygons = triangulate ? triangulate(polygon) : decompose(polygon);
		Fixture[] fixtures = new Fixture[convexPolygons.length];
		for(int i = 0; i < fixtures.length; i++) {
			PolygonMapObject convexObject = new PolygonMapObject(convexPolygons[i]);
			convexObject.setColor(mapObject.getColor());
			convexObject.setName(mapObject.getName());
			convexObject.setOpacity(mapObject.getOpacity());
			convexObject.setVisible(mapObject.isVisible());
			convexObject.getProperties().putAll(mapObject.getProperties());
			fixtures[i] = createFixture(convexObject, body);
		}

		return fixtures;
	}

	/** {@link #createFixture(MapObject, Body) creates} the fixture from the given {@link MapObject} on the associated body in {@link #bodies}
	 *  @see #createFixture(MapObject, Body) */
	public Fixture createFixture(MapObject mapObject) {
		return createFixture(mapObject, findBody(mapObject, heritage, mapProperties, layerProperties));
	}

	/** {@link #createFixtures(MapObject, Body) creates} the fixtures from the given {@link MapObject} on the associated body in {@link #bodies}
	 *  @see #createFixtures(MapObject, Body) */
	public Fixture[] createFixtures(MapObject mapObject) {
		return createFixtures(mapObject, findBody(mapObject, heritage, mapProperties, layerProperties));
	}

	/** transforms the given matrix according to the given orientation
	 *  @param mat the matrix to transform
	 *  @param orientation the orientation */
	public void transform(Matrix4 mat, String orientation) {
		mat.idt();
		if(orientation.equals(aliases.isometric)) {
			mat.scale((float) (Math.sqrt(2) / 2), (float) (Math.sqrt(2) / 4), 1);
			mat.rotate(0, 0, 1, -45);
			mat.translate(-1, 1, 0);
			mat.scale(unitScale * 2, unitScale * 2, unitScale * 2);
		} else if(orientation.equals(aliases.staggered)) {
			mat.scale(unitScale, unitScale, unitScale);
			int mapHeight = findProperty(aliases.height, 0, mapProperties, layerProperties);
			mat.translate(-tileWidth / 2, -tileHeight * (mapHeight / 2) + tileHeight / 2, 0);
		} else
			mat.scale(unitScale, unitScale, unitScale);
	}

	/** @param heritage the MapProperties in which to search for an {@link Aliases#body} property
	 *  @return the body associated with the given {@link MapObject} */
	private Body findBody(MapObject mapObject, MapProperties... heritage) {
		String name = mapObject.getName();
		Body body = null;
		if(name != null)
			body = bodies.get(name);
		if(body == null)
			body = bodies.get(getProperty(mapObject.getProperties(), aliases.body, ""));
		if(body == null)
			for(MapProperties properties : heritage)
				if((body = bodies.get(getProperty(properties, aliases.body, ""))) != null)
					break;
		if(body == null)
			throw new IllegalStateException("the body of " + (name == null ? "an unnamed " : "the ") + "fixture " + (name != null ? name : "") + "does not exist");
		return body;
	}

	/** creates a {@link Joint} from a {@link MapObject}
	 *  @param mapObject the {@link Joint} to parse
	 *  @return the parsed {@link Joint} */
	public Joint createJoint(MapObject mapObject) {
		if((mapObject = listener.createJoint(mapObject)) == null)
			return null;

		MapProperties properties = mapObject.getProperties();

		JointDef jointDef;

		String jointType = getProperty(properties, aliases.jointType, "");
		if(jointType.equals(aliases.distanceJoint)) {
			DistanceJointDef distanceJointDef = new DistanceJointDef();
			assignProperties(distanceJointDef, heritage);
			assignProperties(distanceJointDef, mapProperties);
			assignProperties(distanceJointDef, layerProperties);
			assignProperties(distanceJointDef, properties);
			jointDef = distanceJointDef;
		} else if(jointType.equals(aliases.frictionJoint)) {
			FrictionJointDef frictionJointDef = new FrictionJointDef();
			assignProperties(frictionJointDef, heritage);
			assignProperties(frictionJointDef, mapProperties);
			assignProperties(frictionJointDef, layerProperties);
			assignProperties(frictionJointDef, properties);
			jointDef = frictionJointDef;
		} else if(jointType.equals(aliases.gearJoint)) {
			GearJointDef gearJointDef = new GearJointDef();
			assignProperties(gearJointDef, heritage);
			assignProperties(gearJointDef, mapProperties);
			assignProperties(gearJointDef, layerProperties);
			assignProperties(gearJointDef, properties);
			jointDef = gearJointDef;
		} else if(jointType.equals(aliases.mouseJoint)) {
			MouseJointDef mouseJointDef = new MouseJointDef();
			assignProperties(mouseJointDef, heritage);
			assignProperties(mouseJointDef, mapProperties);
			assignProperties(mouseJointDef, layerProperties);
			assignProperties(mouseJointDef, properties);
			jointDef = mouseJointDef;
		} else if(jointType.equals(aliases.prismaticJoint)) {
			PrismaticJointDef prismaticJointDef = new PrismaticJointDef();
			assignProperties(prismaticJointDef, heritage);
			assignProperties(prismaticJointDef, mapProperties);
			assignProperties(prismaticJointDef, layerProperties);
			assignProperties(prismaticJointDef, properties);
			jointDef = prismaticJointDef;
		} else if(jointType.equals(aliases.pulleyJoint)) {
			PulleyJointDef pulleyJointDef = new PulleyJointDef();
			assignProperties(pulleyJointDef, heritage);
			assignProperties(pulleyJointDef, mapProperties);
			assignProperties(pulleyJointDef, layerProperties);
			assignProperties(pulleyJointDef, properties);
			jointDef = pulleyJointDef;
		} else if(jointType.equals(aliases.revoluteJoint)) {
			RevoluteJointDef revoluteJointDef = new RevoluteJointDef();
			assignProperties(revoluteJointDef, heritage);
			assignProperties(revoluteJointDef, mapProperties);
			assignProperties(revoluteJointDef, layerProperties);
			assignProperties(revoluteJointDef, properties);
			jointDef = revoluteJointDef;
		} else if(jointType.equals(aliases.ropeJoint)) {
			RopeJointDef ropeJointDef = new RopeJointDef();
			assignProperties(ropeJointDef, heritage);
			assignProperties(ropeJointDef, mapProperties);
			assignProperties(ropeJointDef, layerProperties);
			assignProperties(ropeJointDef, properties);
			jointDef = ropeJointDef;
		} else if(jointType.equals(aliases.weldJoint)) {
			WeldJointDef weldJointDef = new WeldJointDef();
			assignProperties(weldJointDef, heritage);
			assignProperties(weldJointDef, mapProperties);
			assignProperties(weldJointDef, layerProperties);
			assignProperties(weldJointDef, properties);
			jointDef = weldJointDef;
		} else if(jointType.equals(aliases.wheelJoint)) {
			WheelJointDef wheelJointDef = new WheelJointDef();
			assignProperties(wheelJointDef, heritage);
			assignProperties(wheelJointDef, mapProperties);
			assignProperties(wheelJointDef, layerProperties);
			assignProperties(wheelJointDef, properties);
			jointDef = wheelJointDef;
		} else
			throw new IllegalArgumentException(ClassReflection.getSimpleName(JointType.class) + " " + jointType + " is unknown");

		assignProperties(jointDef, properties);

		Joint joint = jointDef.bodyA.getWorld().createJoint(jointDef);
		joint.setUserData(getProperty(properties, aliases.userData, joint.getUserData()));

		joints.put(findAvailableName(mapObject.getName(), joints), joint);
		listener.created(joint, mapObject);

		return joint;
	}

	/** assigns the given {@link MapProperties properties} to the values of the given BodyDef
	 *  @param bodyDef the {@link BodyDef} which values to set according to the given {@link MapProperties}
	 *  @param properties the {@link MapProperties} to assign to the given {@link BodyDef} */
	public void assignProperties(BodyDef bodyDef, MapProperties properties) {
		if(properties == null)
			return;
		bodyDef.type = getProperty(properties, aliases.bodyType, "").equals(aliases.staticBody) ? BodyType.StaticBody : getProperty(properties, aliases.bodyType, "").equals(aliases.dynamicBody) ? BodyType.DynamicBody : getProperty(properties, aliases.bodyType, "").equals(aliases.kinematicBody) ? BodyType.KinematicBody : bodyDef.type;
		bodyDef.active = getProperty(properties, aliases.active, bodyDef.active);
		bodyDef.allowSleep = getProperty(properties, aliases.allowSleep, bodyDef.allowSleep);
		bodyDef.angle = getProperty(properties, aliases.angle, bodyDef.angle) * MathUtils.degRad;
		bodyDef.angularDamping = getProperty(properties, aliases.angularDamping, bodyDef.angularDamping);
		bodyDef.angularVelocity = getProperty(properties, aliases.angularVelocity, bodyDef.angularVelocity);
		bodyDef.awake = getProperty(properties, aliases.awake, bodyDef.awake);
		bodyDef.bullet = getProperty(properties, aliases.bullet, bodyDef.bullet);
		bodyDef.fixedRotation = getProperty(properties, aliases.fixedRotation, bodyDef.fixedRotation);
		bodyDef.gravityScale = getProperty(properties, aliases.gravityScale, bodyDef.gravityScale);
		bodyDef.linearDamping = getProperty(properties, aliases.linearDamping, bodyDef.linearDamping);
		bodyDef.linearVelocity.set(getProperty(properties, aliases.linearVelocityX, bodyDef.linearVelocity.x), getProperty(properties, aliases.linearVelocityY, bodyDef.linearVelocity.y));
		bodyDef.position.set(getProperty(properties, aliases.x, bodyDef.position.x) * unitScale, getProperty(properties, aliases.y, bodyDef.position.y) * unitScale);
	}

	/** @see #assignProperties(BodyDef, MapProperties) */
	public void assignProperties(FixtureDef fixtureDef, MapProperties properties) {
		if(properties == null)
			return;
		fixtureDef.density = getProperty(properties, aliases.density, fixtureDef.density);
		fixtureDef.filter.categoryBits = getProperty(properties, aliases.categoryBits, fixtureDef.filter.categoryBits);
		fixtureDef.filter.groupIndex = getProperty(properties, aliases.groupIndex, fixtureDef.filter.groupIndex);
		fixtureDef.filter.maskBits = getProperty(properties, aliases.maskBits, fixtureDef.filter.maskBits);
		fixtureDef.friction = getProperty(properties, aliases.friciton, fixtureDef.friction);
		fixtureDef.isSensor = getProperty(properties, aliases.isSensor, fixtureDef.isSensor);
		fixtureDef.restitution = getProperty(properties, aliases.restitution, fixtureDef.restitution);
	}

	/** assigns the common properties of all JointDefs */
	public void assignProperties(JointDef jointDef, MapProperties properties) {
		if(properties == null)
			return;
		jointDef.bodyA = bodies.get(getProperty(properties, aliases.bodyA, ""));
		jointDef.bodyB = bodies.get(getProperty(properties, aliases.bodyB, ""));
		jointDef.collideConnected = getProperty(properties, aliases.collideConnected, jointDef.collideConnected);
	}

	/** assigns all properties unique to the given joint definition */
	public void assignProperties(DistanceJointDef distanceJointDef, MapProperties properties) {
		if(properties == null)
			return;
		distanceJointDef.dampingRatio = getProperty(properties, aliases.dampingRatio, distanceJointDef.dampingRatio);
		distanceJointDef.frequencyHz = getProperty(properties, aliases.frequencyHz, distanceJointDef.frequencyHz);
		distanceJointDef.length = getProperty(properties, aliases.length, distanceJointDef.length) * (tileWidth + tileHeight) / 2 * unitScale;
		distanceJointDef.localAnchorA.set(getProperty(properties, aliases.localAnchorAX, distanceJointDef.localAnchorA.x) * tileWidth * unitScale, getProperty(properties, aliases.localAnchorAY, distanceJointDef.localAnchorA.y) * tileHeight * unitScale);
		distanceJointDef.localAnchorB.set(getProperty(properties, aliases.localAnchorBX, distanceJointDef.localAnchorB.x) * tileWidth * unitScale, getProperty(properties, aliases.localAnchorBY, distanceJointDef.localAnchorB.y) * tileHeight * unitScale);
	}

	/** assigns all properties unique to the given joint definition */
	public void assignProperties(FrictionJointDef frictionJointDef, MapProperties properties) {
		if(properties == null)
			return;
		frictionJointDef.localAnchorA.set(getProperty(properties, aliases.localAnchorAX, frictionJointDef.localAnchorA.x) * tileWidth * unitScale, getProperty(properties, aliases.localAnchorAY, frictionJointDef.localAnchorA.y) * tileHeight * unitScale);
		frictionJointDef.localAnchorB.set(getProperty(properties, aliases.localAnchorBX, frictionJointDef.localAnchorB.x) * tileWidth * unitScale, getProperty(properties, aliases.localAnchorBY, frictionJointDef.localAnchorB.y) * tileHeight * unitScale);
		frictionJointDef.maxForce = getProperty(properties, aliases.maxForce, frictionJointDef.maxForce);
		frictionJointDef.maxTorque = getProperty(properties, aliases.maxTorque, frictionJointDef.maxTorque);
	}

	/** assigns all properties unique to the given joint definition */
	public void assignProperties(GearJointDef gearJointDef, MapProperties properties) {
		if(properties == null)
			return;
		gearJointDef.joint1 = joints.get(getProperty(properties, aliases.joint1, ""));
		gearJointDef.joint2 = joints.get(getProperty(properties, aliases.joint2, ""));
		gearJointDef.ratio = getProperty(properties, aliases.ratio, gearJointDef.ratio);
	}

	/** assigns all properties unique to the given joint definition */
	public void assignProperties(MouseJointDef mouseJointDef, MapProperties properties) {
		if(properties == null)
			return;
		mouseJointDef.dampingRatio = getProperty(properties, aliases.dampingRatio, mouseJointDef.dampingRatio);
		mouseJointDef.frequencyHz = getProperty(properties, aliases.frequencyHz, mouseJointDef.frequencyHz);
		mouseJointDef.maxForce = getProperty(properties, aliases.maxForce, mouseJointDef.maxForce);
		mouseJointDef.target.set(getProperty(properties, aliases.targetX, mouseJointDef.target.x) * tileWidth * unitScale, getProperty(properties, aliases.targetY, mouseJointDef.target.y) * tileHeight * unitScale);
	}

	/** assigns all properties unique to the given joint definition */
	public void assignProperties(PrismaticJointDef prismaticJointDef, MapProperties properties) {
		if(properties == null)
			return;
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

	/** assigns all properties unique to the given joint definition */
	public void assignProperties(PulleyJointDef pulleyJointDef, MapProperties properties) {
		if(properties == null)
			return;
		pulleyJointDef.groundAnchorA.set(getProperty(properties, aliases.groundAnchorAX, pulleyJointDef.groundAnchorA.x) * tileWidth * unitScale, getProperty(properties, aliases.groundAnchorAY, pulleyJointDef.groundAnchorA.y) * tileHeight * unitScale);
		pulleyJointDef.groundAnchorB.set(getProperty(properties, aliases.groundAnchorBX, pulleyJointDef.groundAnchorB.x) * tileWidth * unitScale, getProperty(properties, aliases.groundAnchorBY, pulleyJointDef.groundAnchorB.y) * tileHeight * unitScale);
		pulleyJointDef.lengthA = getProperty(properties, aliases.lengthA, pulleyJointDef.lengthA) * (tileWidth + tileHeight) / 2 * unitScale;
		pulleyJointDef.lengthB = getProperty(properties, aliases.lengthB, pulleyJointDef.lengthB) * (tileWidth + tileHeight) / 2 * unitScale;
		pulleyJointDef.localAnchorA.set(getProperty(properties, aliases.localAnchorAX, pulleyJointDef.localAnchorA.x) * tileWidth * unitScale, getProperty(properties, aliases.localAnchorAY, pulleyJointDef.localAnchorA.y) * tileHeight * unitScale);
		pulleyJointDef.localAnchorB.set(getProperty(properties, aliases.localAnchorBX, pulleyJointDef.localAnchorB.x) * tileWidth * unitScale, getProperty(properties, aliases.localAnchorBY, pulleyJointDef.localAnchorB.y) * tileHeight * unitScale);
		pulleyJointDef.ratio = getProperty(properties, aliases.ratio, pulleyJointDef.ratio);
	}

	/** assigns all properties unique to the given joint definition */
	public void assignProperties(RevoluteJointDef revoluteJointDef, MapProperties properties) {
		if(properties == null)
			return;
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

	/** assigns all properties unique to the given joint definition */
	public void assignProperties(RopeJointDef ropeJointDef, MapProperties properties) {
		if(properties == null)
			return;
		ropeJointDef.localAnchorA.set(getProperty(properties, aliases.localAnchorAX, ropeJointDef.localAnchorA.x) * tileWidth * unitScale, getProperty(properties, aliases.localAnchorAY, ropeJointDef.localAnchorA.y) * tileHeight * unitScale);
		ropeJointDef.localAnchorB.set(getProperty(properties, aliases.localAnchorBX, ropeJointDef.localAnchorB.x) * tileWidth * unitScale, getProperty(properties, aliases.localAnchorBY, ropeJointDef.localAnchorB.y) * tileHeight * unitScale);
		ropeJointDef.maxLength = getProperty(properties, aliases.maxLength, ropeJointDef.maxLength) * (tileWidth + tileHeight) / 2 * unitScale;
	}

	/** assigns all properties unique to the given joint definition */
	public void assignProperties(WeldJointDef weldJointDef, MapProperties properties) {
		if(properties == null)
			return;
		weldJointDef.localAnchorA.set(getProperty(properties, aliases.localAnchorAX, weldJointDef.localAnchorA.x) * tileWidth * unitScale, getProperty(properties, aliases.localAnchorAY, weldJointDef.localAnchorA.y) * tileHeight * unitScale);
		weldJointDef.localAnchorB.set(getProperty(properties, aliases.localAnchorBX, weldJointDef.localAnchorB.x) * tileWidth * unitScale, getProperty(properties, aliases.localAnchorBY, weldJointDef.localAnchorB.y) * tileHeight * unitScale);
		weldJointDef.referenceAngle = getProperty(properties, aliases.referenceAngle, weldJointDef.referenceAngle) * MathUtils.degRad;
	}

	/** assigns all properties unique to the given joint definition */
	public void assignProperties(WheelJointDef wheelJointDef, MapProperties properties) {
		if(properties == null)
			return;
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
	public static String findAvailableName(String desiredName, ObjectMap<String, ?> map) {
		if(desiredName == null)
			desiredName = String.valueOf(map.size);
		if(map.containsKey(desiredName)) {
			int duplicate = 1;
			while(map.containsKey(desiredName + duplicate))
				duplicate++;
			desiredName += duplicate;
		}
		return desiredName;
	}

	/** resets all fields to their default values */
	public void reset() {
		aliases = new Aliases();
		listener = defaultListener;
		unitScale = 1;
		tileWidth = 1;
		tileHeight = 1;
		triangulate = false;
		bodies.clear();
		fixtures.clear();
		joints.clear();
		heritage = null;
		mapProperties = null;
		layerProperties = null;
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

	/** @return the {@link #listener} */
	public Listener getListener() {
		return listener;
	}

	/** @param listener the {@link #listener} to set */
	public void setListener(Listener listener) {
		this.listener = listener != null ? listener : defaultListener;
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

	/** @return the {@link #heritage} */
	public MapProperties getHeritage() {
		return heritage;
	}

	/** @param heritage the {@link #heritage} to set */
	public void setHeritage(MapProperties heritage) {
		this.heritage = heritage;
	}

}
