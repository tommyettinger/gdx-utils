package net.dermetfan.utils.libgdx.box2d;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.JointDef;
import com.badlogic.gdx.physics.box2d.Shape;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.SnapshotArray;

public class Rope {

	public static interface Builder {

		public Body createSegment(int index, Body previous, int length);

		public Joint createJoint(Body seg1, int seg1index, Body seg2, int seg2index);

	}

	public static abstract class RopeBuilder implements Builder {

		public final Rope rope;

		public RopeBuilder(int length) {
			rope = new Rope(length, this, false);
		}

	}

	public static class DefBuilder implements Builder {

		protected World world;
		protected BodyDef bodyDef;
		protected FixtureDef fixtureDef;
		protected JointDef jointDef;

		public DefBuilder(World world, BodyDef bodyDef, FixtureDef fixtureDef, JointDef jointDef) {
			this.world = world;
			this.bodyDef = bodyDef;
			this.fixtureDef = fixtureDef;
			this.jointDef = jointDef;
		}

		@Override
		public Body createSegment(int index, Body previous, int length) {
			return world.createBody(bodyDef).createFixture(fixtureDef).getBody();
		}

		@Override
		public Joint createJoint(Body seg1, int seg1index, Body seg2, int seg2index) {
			jointDef.bodyA = seg1;
			jointDef.bodyB = seg2;
			return world.createJoint(jointDef);
		}

	}

	public static class DefShapeBuilder implements Builder, Disposable {

		protected World world;
		protected BodyDef bodyDef;
		protected Shape shape;
		protected float density;
		protected JointDef jointDef;

		public DefShapeBuilder(World world, BodyDef bodyDef, Shape shape, float density, JointDef jointDef) {
			this.world = world;
			this.bodyDef = bodyDef;
			this.shape = shape;
			this.density = density;
			this.jointDef = jointDef;
		}

		@Override
		public Body createSegment(int index, Body previous, int length) {
			return world.createBody(bodyDef).createFixture(shape, density).getBody();
		}

		@Override
		public Joint createJoint(Body seg1, int seg1index, Body seg2, int seg2index) {
			System.out.println(seg1index + (seg1 == null ? "(null)" : "") + " -> " + seg2index + (seg2 == null ? "(null)" : ""));
			jointDef.bodyA = seg1;
			jointDef.bodyB = seg2;
			return world.createJoint(jointDef);
		}

		@Override
		public void dispose() {
			shape.dispose();
		}

	}

	public static abstract class CopyBuilder implements Builder {

		protected Body template;

		public CopyBuilder(Body template) {
			this.template = template;
		}

		@Override
		public Body createSegment(int index, Body previous, int length) {
			return Box2DUtils.copy(template);
		}

	}

	public static abstract class JointDefCopyBuilder extends CopyBuilder {

		protected JointDef jointDef;

		public JointDefCopyBuilder(Body template, JointDef jointDef) {
			super(template);
			this.jointDef = jointDef;
		}

		@Override
		public Joint createJoint(Body seg1, int seg1index, Body seg2, int seg2index) {
			jointDef.bodyA = seg1;
			jointDef.bodyB = seg2;
			return template.getWorld().createJoint(jointDef);
		}

	}

	private Builder builder;
	private final SnapshotArray<Body> segments = new SnapshotArray<Body>();
	private final SnapshotArray<Joint> joints = new SnapshotArray<Joint>();

	public Rope(int length, Builder builder) {
		this(length, builder, true);
	}

	public Rope(int length, Builder builder, boolean build) {
		segments.ensureCapacity(length - segments.size);
		joints.ensureCapacity(length - segments.size);
		this.builder = builder;
		if(build)
			build(length);
	}

	public Rope(Builder builder, Body... segments) {
		this.builder = builder;
		for(Body segment : segments)
			addSegment(segment);
	}

	public Rope build(int length) {
		return build(length, builder);
	}

	public Rope build(int length, Builder builder) {
		while(length > 0) {
			extend();
			length--;
		}
		return this;
	}

	protected Body createSegment(int index) { // works
		return builder.createSegment(index, segments.size > 0 ? segments.peek() : null, segments.size + 1);
	}

	protected Joint createJoint(int segmentIndex1, int segmentIndex2) { // works
		Body seg1 = segments.get(segmentIndex1), seg2 = segments.get(segmentIndex2);
		return builder.createJoint(seg1, segmentIndex1, seg2, segmentIndex2);
	}

	public Body extend() { // works
		Body segment = createSegment(segments.size);
		addSegment(segment);
		return segment;
	}

	public void addSegment(Body segment) { // works
		segments.add(segment);
		if(segments.size > 1)
			joints.add(createJoint(segments.size - 2 < 0 ? 0 : segments.size - 2, segments.size - 1));
	}

	public Body insertSegment(int index) { // works
		Body segment = createSegment(index);
		insertSegment(index, segment);
		return segment;
	}

	public void insertSegment(int index, Body segment) { // works
		if(index - 1 >= 0)
			segment.getWorld().destroyJoint(joints.removeIndex(index - 1));
		segments.insert(index, segment);
		if(index - 1 >= 0)
			joints.insert(index - 1, createJoint(index - 1, index));
		if(index + 1 < segments.size)
			joints.insert(index, createJoint(index, index + 1));
	}

	public Body removeSegment(int index, boolean split) { // works
		Body segment = segments.removeIndex(index);
		if(--index >= 0)
			segment.getWorld().destroyJoint(joints.removeIndex(index));
		else
			index++;
		if(index < joints.size)
			segment.getWorld().destroyJoint(joints.removeIndex(index));
		if(!split) {
			Body prevSegment = segments.get(index), nextSegment = segments.get(MathUtils.clamp(index + 1, 0, segments.size - 1));
			if(prevSegment != nextSegment)
				joints.insert(index, builder.createJoint(prevSegment, index, nextSegment, index + 1));
		}
		return segment;
	}

	public void destroySegment(int index, boolean split) { // works
		Body segment = removeSegment(index, split);
		segment.getWorld().destroyBody(segment);
	}

	public int length() {
		return segments.size;
	}

	public Body getSegment(int index) {
		return segments.get(index);
	}

	public Joint getJoint(int index) {
		return joints.get(index);
	}

	/** @return the {@link #builder} */
	public Builder getBuilder() {
		return builder;
	}

	/** @param builder the {@link #builder} to set */
	public void setBuilder(Builder builder) {
		this.builder = builder;
	}

	/** @return the {@link #segments} */
	public Body[] getSegments() {
		return segments.begin();
	}

	/** @return the {@link #joints} */
	public Joint[] getJoints() {
		return joints.begin();
	}

}
