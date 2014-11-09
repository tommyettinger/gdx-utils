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

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.JointDef;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.SnapshotArray;

/** Holds {@link #segments} and {@link #connections} to simulate a chain. Also provides modification methods that use a {@link Builder}.
 *  @author dermetfan */
public class Chain {

	/** used by a {@link Chain} to modify it
	 *  @author dermetfan */
	public interface Builder {

		/** creates a segment that is going to be added to {@link Chain#segments}
		 *  @param index the index of the segment to create
		 *  @param length the desired length of the {@link Chain} that is being build
		 *  @param chain the {@link Chain} this segment will be added to
		 *  @return the created segment */
		public Body createSegment(int index, int length, Chain chain);

		/** connects two segments with each other using a {@link Connection}
		 *  @param seg1 the first segment
		 *  @param seg1index the index of the first segment
		 *  @param seg2 the second segment
		 *  @param seg2index the index of the second segment
		 *  @return the created {@link Connection} */
		public Connection createConnection(Body seg1, int seg1index, Body seg2, int seg2index);

	}

	/** a {@link Builder} that builds using a {@link BodyDef}, {@link FixtureDef} and {@link Joint}
	 *  @author dermetfan */
	public static class DefBuilder implements Builder {

		/** the {@link World} in which to create segments and joints */
		protected World world;

		/** the {@link BodyDef} to use in {@link #createSegment(int, int, Chain)} */
		protected BodyDef bodyDef;

		/** the {@link FixtureDef} to use in {@link #createSegment(int, int, Chain)} */
		protected FixtureDef fixtureDef;

		/** the {@link JointDef} to use in {@link #createConnection(Body, int, Body, int)} */
		protected JointDef jointDef;

		/** @param world the {@link #world}
		 *  @param bodyDef the {@link #bodyDef}
		 *  @param fixtureDef the {@link #fixtureDef}
		 *  @param jointDef the {@link #jointDef} */
		public DefBuilder(World world, BodyDef bodyDef, FixtureDef fixtureDef, JointDef jointDef) {
			this.world = world;
			this.bodyDef = bodyDef;
			this.fixtureDef = fixtureDef;
			this.jointDef = jointDef;
		}

		/** @return a new {@link Body segment} created with{@link #bodyDef} and {@link #fixtureDef} */
		@Override
		public Body createSegment(int index, int length, Chain chain) {
			return world.createBody(bodyDef).createFixture(fixtureDef).getBody();
		}

		/** @return a new {@link JointDef} created with {@link #jointDef} */
		@Override
		public Connection createConnection(Body seg1, int seg1index, Body seg2, int seg2index) {
			jointDef.bodyA = seg1;
			jointDef.bodyB = seg2;
			return new Connection(world.createJoint(jointDef));
		}

		/** @return the {@link #world} */
		public World getWorld() {
			return world;
		}

		/** @param world the {@link #world} to set */
		public void setWorld(World world) {
			this.world = world;
		}

		/** @return the {@link #bodyDef} */
		public BodyDef getBodyDef() {
			return bodyDef;
		}

		/** @param bodyDef the {@link #bodyDef} to set */
		public void setBodyDef(BodyDef bodyDef) {
			this.bodyDef = bodyDef;
		}

		/** @return the {@link #fixtureDef} */
		public FixtureDef getFixtureDef() {
			return fixtureDef;
		}

		/** @param fixtureDef the {@link #fixtureDef} to set */
		public void setFixtureDef(FixtureDef fixtureDef) {
			this.fixtureDef = fixtureDef;
		}

		/** @return the {@link #jointDef} */
		public JointDef getJointDef() {
			return jointDef;
		}

		/** @param jointDef the {@link #jointDef} to set */
		public void setJointDef(JointDef jointDef) {
			this.jointDef = jointDef;
		}

	}

	/** A {@link Builder} that builds using a {@link BodyDef}, {@link JointDef} and {@link Shape}. Should be {@link DefShapeBuilder#dispose() dispose} if no longer used.
	 *  @author dermetfan */
	public static class DefShapeBuilder implements Builder, Disposable {

		/** the {@link World} to create things in */
		protected World world;

		/** the {@link BodyDef} to use in {@link #createSegment(int, int, Chain)} */
		protected BodyDef bodyDef;

		/** the {@link Shape} to use in {@link #createSegment(int, int, Chain)} */
		protected Shape shape;

		/** the density to use in {@link Body#createFixture(Shape, float)} */
		protected float density;

		/** the {@link JointDef} to use in {@link #createSegment(int, int, Chain)} */
		protected JointDef jointDef;

		/** @param world the {@link #world}
		 *  @param bodyDef the {@link #bodyDef}
		 *  @param shape the {@link Shape}
		 *  @param density the {@link #density}
		 *  @param jointDef the {@link #jointDef} */
		public DefShapeBuilder(World world, BodyDef bodyDef, Shape shape, float density, JointDef jointDef) {
			this.world = world;
			this.bodyDef = bodyDef;
			this.shape = shape;
			this.density = density;
			this.jointDef = jointDef;
		}

		/** creates a {@link Body segment} using {@link #bodyDef}, {@link #shape} and {@link #density}
		 *  @see Body#createFixture(Shape, float) */
		@Override
		public Body createSegment(int index, int length, Chain chain) {
			return world.createBody(bodyDef).createFixture(shape, density).getBody();
		}

		/** @return a new {@link Joint} created with {@link #jointDef} */
		@Override
		public Connection createConnection(Body seg1, int seg1index, Body seg2, int seg2index) {
			jointDef.bodyA = seg1;
			jointDef.bodyB = seg2;
			return new Connection(world.createJoint(jointDef));
		}

		/** {@link Shape#dispose() disposes} the {@link #shape} */
		@Override
		public void dispose() {
			shape.dispose();
		}

		/** @return the {@link #world} */
		public World getWorld() {
			return world;
		}

		/** @param world the {@link #world} to set */
		public void setWorld(World world) {
			this.world = world;
		}

		/** @return the {@link #bodyDef} */
		public BodyDef getBodyDef() {
			return bodyDef;
		}

		/** @param bodyDef the {@link #bodyDef} to set */
		public void setBodyDef(BodyDef bodyDef) {
			this.bodyDef = bodyDef;
		}

		/** @return the {@link #density} */
		public float getDensity() {
			return density;
		}

		/** @param density the {@link #density} to set */
		public void setDensity(float density) {
			this.density = density;
		}

		/** @return the {@link #jointDef} */
		public JointDef getJointDef() {
			return jointDef;
		}

		/** @param jointDef the {@link #jointDef} to set */
		public void setJointDef(JointDef jointDef) {
			this.jointDef = jointDef;
		}

		/** @return the {@link #shape} */
		public Shape getShape() {
			return shape;
		}

	}

	/** a {@link Builder} that {@link Box2DUtils#clone(Body) clones} a {@link Body} as template in {@link #createSegment(int, int, Chain)}
	 *  @author dermetfan */
	public static abstract class CopyBuilder implements Builder {

		/** the {@link Body} to {@link Box2DUtils#clone(Body) copy} in {@link #createSegment(int, int, Chain)} */
		protected Body template;

		/** @param template the {@link #template} */
		public CopyBuilder(Body template) {
			this.template = template;
		}

		/** @return a {@link Box2DUtils#clone(Body) copy} of {@link #template} */
		@Override
		public Body createSegment(int index, int length, Chain chain) {
			return Box2DUtils.clone(template);
		}

		/** @return the {@link #template} */
		public Body getTemplate() {
			return template;
		}

		/** @param template the {@link #template} to set */
		public void setTemplate(Body template) {
			this.template = template;
		}

	}

	/** a {@link CopyBuilder} that uses a {@link JointDef} in {@link #createConnection(Body, int, Body, int)}
	 *  @author dermetfan */
	public static class JointDefCopyBuilder extends CopyBuilder {

		/** the {@link JointDef} to use in {@link #createConnection(Body, int, Body, int)} */
		protected JointDef jointDef;

		/** @param template the {@link CopyBuilder#template}
		 *  @param jointDef the {@link #jointDef} */
		public JointDefCopyBuilder(Body template, JointDef jointDef) {
			super(template);
			this.jointDef = jointDef;
		}

		/** @return a new {@link Joint} created with {@link #jointDef} */
		@Override
		public Connection createConnection(Body seg1, int seg1index, Body seg2, int seg2index) {
			jointDef.bodyA = seg1;
			jointDef.bodyB = seg2;
			return new Connection(template.getWorld().createJoint(jointDef));
		}

		/** @return the {@link #jointDef} */
		public JointDef getJointDef() {
			return jointDef;
		}

		/** @param jointDef the {@link #jointDef} to set */
		public void setJointDef(JointDef jointDef) {
			this.jointDef = jointDef;
		}

	}

	/** holds one or more {@link Joint joints}
	 *  @author dermetfan */
	public static class Connection {

		/** the {@link Joint joints} of this {@link Connection} */
		public final Array<Joint> joints = new Array<>(2);

		/** creates a new Connection and {@link #add(Joint) adds} the given joint */
		public Connection(Joint joint) {
			joints.add(joint);
		}

		/** creates a new Connection and {@link #add(Joint) adds} the given joints */
		public Connection(Joint joint1, Joint joint2) {
			joints.add(joint1);
			joints.add(joint2);
		}

		/** creates a new Connection and {@link #add(Joint) adds} the given joints */
		public Connection(Joint joint1, Joint joint2, Joint joint3) {
			joints.add(joint1);
			joints.add(joint2);
			joints.add(joint3);
		}

		/** creates a new Connection and {@link #add(Joint) adds} the given joints */
		public Connection(Joint... joints) {
			this.joints.addAll(joints);
		}

		/** {@link Array#add(Object) adds} the given joint to {@link #joints} */
		public void add(Joint joint) {
			joints.add(joint);
		}

		/** {@link Array#removeValue(Object, boolean) removes} the given joint from {@link #joints} */
		public boolean remove(Joint joint) {
			return joints.removeValue(joint, true);
		}

		/** {@link Array#removeIndex(int) removes} the joint at the given index from {@link #joints}
		 *  @return the removed {@link Joint} */
		public Joint remove(int index) {
			return joints.removeIndex(index);
		}

		/** {@link World#destroyJoint(Joint) destroys} all {@link #joints} */
		public void destroy() {
			for(Joint joint : joints)
				joint.getBodyA().getWorld().destroyJoint(joint);
		}

	}

	/** the {@link Builder} used for modifications of this Chain */
	private Builder builder;

	/** the {@link Body segments} of this Chain */
	private final Array<Body> segments = new Array<>();

	/** the {@link Joint Joints} of this Chain */
	private final Array<Connection> connections = new Array<>();

	/** creates a shallow copy of the given {@link Chain} instance
	 *  @param other the {@link Chain} to copy */
	public Chain(Chain other) {
		builder = other.builder;
		segments.addAll(other.segments);
		connections.addAll(other.connections);
	}

	/** @param builder the {@link #builder} */
	public Chain(Builder builder) {
		this.builder = builder;
	}

	/** creates a new Chain and {@link #extend(int) builds} it to the given {@code length}
	 *  @param length the desired length of this Chain
	 *  @param builder the {@link #builder}
	 *  @see #Chain(int, Builder, boolean) */
	public Chain(int length, Builder builder) {
		this(length, builder, true);
	}

	/** creates a new Chain and {@link #extend(int) builds} it to the given {@code length} if {@code build} is true
	 *  @param length The desired length of this Chain. Will be ignored if {@code build} is false.
	 *  @param builder the {@link #builder}
	 *  @param build if this Chain should be {@link #extend(int) build} to the given {@code length} */
	public Chain(int length, Builder builder, boolean build) {
		segments.ensureCapacity(length - segments.size);
		connections.ensureCapacity(length - segments.size);
		this.builder = builder;
		if(build)
			extend(length);
	}

	/** creates a new Chain with the given {@code segments}
	 *  @param builder the {@link #builder}
	 *  @param segments the {@link #segments} */
	public Chain(Builder builder, Body... segments) {
		this.builder = builder;
		for(Body segment : segments)
			add(segment);
	}

	/** {@link #extend(int, Builder) extends} this chain by the given {@code length} using the {@link #builder}
	 *  @see #extend(int, Builder) */
	public Chain extend(int length) {
		return extend(length, builder);
	}

	/** {@link #extend(Builder) extends} this chain by the given {@code length} using the given {@link Builder}
	 *  @see #extend(Builder) */
	public Chain extend(int length, Builder builder) {
		for(; length > 0; length--)
			extend(builder);
		return this;
	}

	/** {@link #extend(Builder) extends} this Chain using the {@link #builder}
	 *  @see #extend(Builder) */
	public Body extend() {
		return extend(builder);
	}

	/** {@link #createSegment(int, Builder) creates} and {@link #add(Body) adds} a new segment to this Chain
	 *  @return the {@link #createSegment(int, Builder) created} and {@link #add(Body) added} segment */
	public Body extend(Builder builder) {
		Body segment = createSegment(segments.size, builder);
		add(segment);
		return segment;
	}

	/** {@link #destroy(int) destorys} the last segment */
	public void shorten() {
		destroy(segments.size - 1);
	}

	/** {@link #destroy(int, int) destroys} the given amount of segments from the end of the Chain */
	public void shorten(int length) {
		destroy(segments.size - length, segments.size - 1);
	}

	/** {@link #createSegment(int, Builder) creates} a segment for the given index using the {@link #builder}
	 *  @see #createSegment(int, Builder) */
	public Body createSegment(int index) {
		return createSegment(index, builder);
	}

	/** Creates a {@link Body segment} using the given {@link Builder} passing the correct parameters to {@link Builder#createSegment(int, int, Chain)} specified by the given {@code index}. Does NOT add it to this Chain.
	 *  @see Builder#createSegment(int, int, Chain) */
	public Body createSegment(int index, Builder builder) {
		return builder.createSegment(index, segments.size + 1, this);
	}

	/** {@link #createConnection(int, int, Builder) creates} a new {@link Connection}
	 *  @see #createConnection(int, int, Builder) */
	public Connection createConnection(int segmentIndex1, int segmentIndex2) {
		return createConnection(segmentIndex1, segmentIndex2, builder);
	}

	/** Creates a {@link Connection} using the {@link Builder} passing the correct parameters to {@link Builder#createConnection(Body, int, Body, int)} specified by the given indices. Does NOT add it to this Chain.
	 *  @see Builder#createConnection(Body, int, Body, int) */
	public Connection createConnection(int segmentIndex1, int segmentIndex2, Builder builder) {
		Body seg1 = segments.get(segmentIndex1), seg2 = segments.get(segmentIndex2);
		return builder.createConnection(seg1, segmentIndex1, seg2, segmentIndex2);

	}

	/** {@link SnapshotArray#add(Object) adds} the given {@code segment} to the end of this Chain
	 *  @param segment the {@link Body segment} to add */
	public void add(Body segment) {
		segments.add(segment);
		if(segments.size > 1)
			connections.add(createConnection(segments.size - 2 < 0 ? 0 : segments.size - 2, segments.size - 1));
	}

	/** {@link #add(Body) adds} the given segments to the end of this Chain
	 *  @see #add(Body) */
	public void addAll(Body... segments) {
		for(Body segment : segments)
			add(segment);
	}

	/** {@link #createSegment(int) creates} a new {@link Body segment} and {@link #insert(int, Body) inserts} it into this Chain
	 *  @see #insert(int, Body) */
	public Body insert(int index) {
		Body segment = createSegment(index);
		insert(index, segment);
		return segment;
	}

	/** inserts a {@link Body segment} into this Chain
	 *  @param index the {@link #segments index} at which to insert the given {@code segment}
	 *  @param segment the {@link Body segment} to insert */
	public void insert(int index, Body segment) {
		if(index - 1 >= 0)
			connections.removeIndex(index - 1).destroy();
		segments.insert(index, segment);
		if(index - 1 >= 0)
			connections.insert(index - 1, createConnection(index - 1, index));
		if(index + 1 < segments.size)
			connections.insert(index, createConnection(index, index + 1));
	}

	/** @param index the index of the segment to replace
	 *  @param segment the {@link Body segment} to insert
	 *  @return the {@link Body segment} that was at the given {@code index} previously */
	public Body replace(int index, Body segment) {
		Body old = remove(index);
		insert(index, segment);
		return old;
	}

	/** @param segment The {@link Body} to remove. Must be a {@link #segments segment} of this {@link Chain}.
	 *  @return the given {@code body}
	 *  @see #remove(int) */
	public Body remove(Body segment) {
		if(!segments.contains(segment, true))
			throw new IllegalArgumentException("the given body is not a segment of this Chain");
		return remove(segments.indexOf(segment, true));
	}

	/** removes a {@link #segments segment} from this Chain
	 *  @param index the index of the {@link #segments segment} to remove
	 *  @return the removed {@link #segments segment}
	 *  @see Array#removeIndex(int) */
	public Body remove(int index) {
		Body previous = index - 1 >= 0 ? segments.get(index - 1) : null, next = index + 1 < segments.size ? segments.get(index + 1) : null, segment = segments.removeIndex(index);
		if(index - 1 >= 0)
			connections.removeIndex(--index).destroy();
		if(index < connections.size)
			connections.removeIndex(index).destroy();
		if(previous != null && next != null)
			connections.insert(index, builder.createConnection(previous, index, next, index + 1));
		return segment;
	}

	/** {@link #remove(int) removes} all segmetns from beginIndex to endIndex
	 *  @param beginIndex the first index to remove
	 *  @param endIndex the last index to remove
	 *  @return an Array holding the removed segments
	 *  @see #tmpSegments */
	public Array<Body> remove(int beginIndex, int endIndex) {
		tmpSegments.clear();
		for(; endIndex >= beginIndex; endIndex--)
			tmpSegments.add(remove(endIndex));
		return tmpSegments;
	}

	/** @param segment the {@link Body segment} to destroy
	 *  @see #destroy(int) */
	public void destroy(Body segment) {
		if(!segments.contains(segment, true))
			throw new IllegalArgumentException("the given body must be a segment of this Chain");
		destroy(segments.indexOf(segment, true));
	}

	/** @param index the index of the {@link #segments segment} to {@link World#destroyBody(Body) destroy}
	 *  @see #remove(int) */
	public void destroy(int index) {
		Body segment = remove(index);
		segment.getWorld().destroyBody(segment);
	}

	/** {@link #destroy(int) destroys} all segments from beginIndex to endIndex
	 *  @param beginIndex the first index to destroy
	 *  @param endIndex the last index to destroy */
	public void destroy(int beginIndex, int endIndex) {
		Array<Body> removed = remove(beginIndex, endIndex);
		for(Body body : removed)
			body.getWorld().destroyBody(body);
		removed.clear();
	}

	/** @param connection the {@link Connection} in {@link #connections} to split
	 *  @return the new {@link Chain}
	 *  @see #split(int) */
	public Chain split(Connection connection) {
		if(!connections.contains(connection, true))
			throw new IllegalArgumentException("the joint must be part of this Chain");
		return split(connections.indexOf(connection, true));
	}

	/** splits this Chain at the given index and returns a new Chain consisting of the {@link #segments} up to the given {@code index}
	 *  @param connectionIndex the index of the {@link #connections connection} to destroy
	 *  @return a Chain consisting of the segments before the given index */
	public Chain split(int connectionIndex) {
		Body[] segs = new Body[connectionIndex + 1];
		for(int i = 0; i <= connectionIndex; i++) {
			connections.removeIndex(0).destroy();
			segs[i] = segments.removeIndex(0);
		}
		return new Chain(builder, segs);
	}

	/** @return the amount of {@link #segments} in this Chain
	 *  @see Array#size */
	public int length() {
		return segments.size;
	}

	/** @param index the index of the desired segment
	 *  @return the {@link #segments segment} at the given index */
	public Body getSegment(int index) {
		return segments.get(index);
	}

	/** @param index the index of the desired {@link #connections Joint}
	 *  @return the {@link #connections Joint} at the given index */
	public Connection getConnection(int index) {
		return connections.get(index);
	}

	/** <strong>Warning: This is used by multiple methods and may be modified unexpectedly. It's highly recommended to {@link Array#addAll(Array) add} its items to your own collection.</strong> */
	private final Array<Body> tmpSegments = new Array<>();

	/** <strong>Warning: The returned Array is used by other methods and may be modified unexpectedly. It's highly recommended to {@link Array#addAll(Array) add} its items to your own collection.</strong>
	 *  @return the {@link #segments}
	 *  @see #tmpSegments */
	public Array<Body> getSegments() {
		tmpSegments.clear();
		tmpSegments.addAll(segments);
		return tmpSegments;
	}

	/** <strong>Warning: This is used by multiple methods and may be modified unexpectedly. It's highly recommended to {@link Array#addAll(Array) add} its items to your own collection.</strong> */
	private final Array<Connection> tmpConnections = new Array<>();

	/** <strong>Warning: The returned Array is used by other methods and may be modified unexpectedly. It's highly recommended to {@link Array#addAll(Array) add} its items to your own collection.</strong> 
	 *  @return an Array representing the current state of {@link #connections}
	 *  @see #tmpConnections */
	public Array<Connection> getConnections() {
		tmpConnections.clear();
		tmpConnections.addAll(connections);
		return tmpConnections;
	}

	/** @return the {@link #builder} */
	public Builder getBuilder() {
		return builder;
	}

	/** @param builder the {@link #builder} to set */
	public void setBuilder(Builder builder) {
		if(builder == null)
			throw new IllegalArgumentException("builder must not be null");
		this.builder = builder;
	}

}
