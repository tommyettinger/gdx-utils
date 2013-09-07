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

import static com.badlogic.gdx.math.MathUtils.cos;
import static com.badlogic.gdx.math.MathUtils.sin;
import static net.dermetfan.libgdx.math.GeometryUtils.amplitude;
import static net.dermetfan.libgdx.math.GeometryUtils.filterX;
import static net.dermetfan.libgdx.math.GeometryUtils.filterY;
import static net.dermetfan.libgdx.math.GeometryUtils.max;
import static net.dermetfan.libgdx.math.GeometryUtils.min;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.ChainShape;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.EdgeShape;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.Shape.Type;

/** provides methods for geometric operations with Box2D bodies, fixtures and shapes */
public abstract class Box2DUtils {

	/** temporary {@link Vector2} used by some methods that have an optional output parameter if it's not given
	 *  @warning not safe to use as it may change often */
	public static Vector2 tmp = new Vector2();

	/** temporary {@link Vector2 Vector2[]} used by some methods that have an optional outpout parameter if it's not given
	 *  @warning not safe to use as it may change often */
	public static Vector2[] tmpArray = new Vector2[0];

	/** @return the vertices of all fixtures of the given body
	 *  @see #vertices(Shape) */
	public static Vector2[] vertices(Body body, Vector2[] output) {
		Vector2[][] fixtureVertices = new Vector2[body.getFixtureList().size()][]; // caching fixture vertices for performance
		for(int i = 0; i < fixtureVertices.length; i++)
			fixtureVertices[i] = vertices(body.getFixtureList().get(i), tmpArray);

		int vertexCount = 0;
		int fvi = -1;
		for(Fixture fixture : body.getFixtureList())
			if(fixture.getShape().getType() == Type.Circle) // for performance (doesn't call #vertices(Shape))
				vertexCount += 4;
			else
				vertexCount += fixtureVertices[++fvi].length;

		output = new Vector2[vertexCount];
		int vi = -1;
		for(Vector2[] verts : fixtureVertices)
			for(Vector2 vertice : verts)
				output[++vi] = vertice;

		return output;
	}

	/** @see #vertices(Body, Vector2[]) */
	public static Vector2[] vertices(Body body) {
		return vertices(body, tmpArray);
	}

	/** @see #vertices(Shape) */
	public static Vector2[] vertices(Fixture fixture, Vector2[] output) {
		return vertices(fixture.getShape(), output);
	}

	/** @see #vertices(Fixture, Vector2[]) */
	public static Vector2[] vertices(Fixture fixture) {
		return vertices(fixture, tmpArray);
	}

	/** @return the vertices of the given Shape */
	public static Vector2[] vertices(Shape shape, Vector2[] output) {
		switch(shape.getType()) {
		case Polygon:
			PolygonShape polygonShape = (PolygonShape) shape;

			output = new Vector2[polygonShape.getVertexCount()];

			for(int i = 0; i < output.length; i++) {
				output[i] = new Vector2();
				polygonShape.getVertex(i, output[i]);
			}
			break;
		case Edge:
			EdgeShape edgeShape = (EdgeShape) shape;

			Vector2 vertex1 = new Vector2(),
			vertex2 = new Vector2();
			edgeShape.getVertex1(vertex1);
			edgeShape.getVertex2(vertex2);

			output = new Vector2[] {vertex1, vertex2};
			break;
		case Chain:
			ChainShape chainShape = (ChainShape) shape;

			output = new Vector2[chainShape.getVertexCount()];

			for(int i = 0; i < output.length; i++) {
				output[i] = new Vector2();
				chainShape.getVertex(i, output[i]);
			}
			break;
		case Circle:
			CircleShape circleShape = (CircleShape) shape;

			output = new Vector2[] {
					new Vector2(circleShape.getPosition().x - circleShape.getRadius(), circleShape.getPosition().y + circleShape.getRadius()), // top left
					new Vector2(circleShape.getPosition().x - circleShape.getRadius(), circleShape.getPosition().y - circleShape.getRadius()), // bottom left
					new Vector2(circleShape.getPosition().x + circleShape.getRadius(), circleShape.getPosition().y - circleShape.getRadius()), // bottom right
					new Vector2(circleShape.getPosition().x + circleShape.getRadius(), circleShape.getPosition().y + circleShape.getRadius()) // top right
			};
			break;
		default:
			throw new IllegalArgumentException("Shapes of the type '" + shape.getType().name() + "' are not supported");
		}

		return output;
	}

	/** @see #vertices(Shape, Vector2[]) */
	public static Vector2[] vertices(Shape shape) {
		return vertices(shape, tmpArray);
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
		return min(filterX(vertices(fixture)));
	}

	/** @return the minimal y value of the vertices of the given Fixture */
	public static float minY(Fixture fixture) {
		return min(filterY(vertices(fixture)));
	}

	/** @return the maximal x value of the vertices of the given Fixture */
	public static float maxX(Fixture fixture) {
		return max(filterX(vertices(fixture)));
	}

	/** @return the maximal y value of the vertices of the given Fixture */
	public static float maxY(Fixture fixture) {
		return max(filterY(vertices(fixture)));
	}

	/** @return the width of the given Body */
	public static float width(Body body) {
		float min = Float.POSITIVE_INFINITY, max = Float.NEGATIVE_INFINITY, tmp;

		for(Fixture fixture : body.getFixtureList()) {
			min = (tmp = minX(fixture)) < min ? tmp : min;
			max = (tmp = maxX(fixture)) > max ? tmp : max;
		}

		return Math.abs(max - min);
	}

	/** @return the height of the given Body */
	public static float height(Body body) {
		float min = Float.POSITIVE_INFINITY, max = Float.NEGATIVE_INFINITY, tmp;

		for(Fixture fixture : body.getFixtureList()) {
			min = (tmp = minY(fixture)) < min ? tmp : min;
			max = (tmp = maxY(fixture)) > max ? tmp : max;
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
		return amplitude(filterX(vertices(shape)));
	}

	/** @return the height of the given Shape */
	public static float height(Shape shape) {
		return amplitude(filterY(vertices(shape)));
	}

	/** @see #size(Shape) */
	public static Vector2 size(Shape shape, Vector2 output) {
		if(shape.getType() == Type.Circle) // no call to #vertices(Shape) for performance
			return output.set(shape.getRadius() * 2, shape.getRadius() * 2);
		return output.set(width(shape), height(shape));
	}

	/** @return the size of the given Shape */
	public static Vector2 size(Shape shape) {
		return size(shape, tmp);
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
		Vector2[] vertices = vertices(shape, tmpArray);
		output.set(max(filterX(vertices)) - amplitude(filterX(vertices)) / 2, max(filterY(vertices)) - amplitude(filterY(vertices)) / 2);

		// transform position according to rotation
		// http://stackoverflow.com/questions/1469149/calculating-vertices-of-a-rotated-rectangle
		float xx = output.x, xy = output.y, yx = output.x, yy = output.y;

		xx = xx * cos(rotation) - xy * sin(rotation);
		yy = yx * sin(rotation) + yy * cos(rotation);

		return output.set(xx, yy);
	}

	/** @see #positionRelative(Shape, float, Vector2) */
	public static Vector2 positionRelative(Shape shape, float rotation) {
		return positionRelative(shape, rotation, tmp);
	}

	/** @see #position(Fixture) */
	public static Vector2 position(Fixture fixture, Vector2 output) {
		return output.set(position(fixture.getShape(), fixture.getBody()));
	}

	/** @return the position of the given Fixture in world coordinates */
	public static Vector2 position(Fixture fixture) {
		return position(fixture.getShape(), fixture.getBody(), tmp);
	}

	/** @see #position(Shape, Body) */
	public static Vector2 position(Shape shape, Body body, Vector2 output) {
		return output.set(body.getPosition().add(positionRelative(shape, body.getTransform().getRotation(), output)));
	}

	/** @return the position of the given Shape in world coordinates
	 *  @param shape the Shape which position to get
	 *  @param body the Body the given Shape is attached to */
	public static Vector2 position(Shape shape, Body body) {
		return body.getPosition().add(positionRelative(shape, body.getTransform().getRotation(), tmp));
	}

}
