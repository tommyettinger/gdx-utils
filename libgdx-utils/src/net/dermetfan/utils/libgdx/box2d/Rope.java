package net.dermetfan.utils.libgdx.box2d;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.Joint;
import com.badlogic.gdx.physics.box2d.JointDef;
import com.badlogic.gdx.physics.box2d.JointEdge;
import com.badlogic.gdx.utils.SnapshotArray;

public class Rope {

	public static interface Builder {

		public Body firstSegment(Rope rope);

		public Body nextSegment(Body previous);

		public Body lastSegment(Body previous);

		public Joint connect(Body seg1, Body seg2);

	}

	public static class CopyBuilder implements Builder {

		private Body segment;
		private JointDef jointDef;

		public CopyBuilder(Body segment, JointDef jointDef) {
			this.segment = segment;
			this.jointDef = jointDef;
		}

		@Override
		public Body firstSegment(Rope rope) {
			return segment;
		}

		@Override
		public Body nextSegment(Body previous) {
			return Box2DUtils.copy(segment);
		}

		@Override
		public Body lastSegment(Body previous) {
			return nextSegment(previous);
		}

		@Override
		public Joint connect(Body seg1, Body seg2) {
			jointDef.bodyA = seg1;
			jointDef.bodyB = seg2;
			return seg1.getWorld().createJoint(jointDef);
		}

	}

	public static class LoopCopyBuilder extends CopyBuilder {

		private Body first;

		public LoopCopyBuilder(Body segment, JointDef jointDef) {
			super(segment, jointDef);
		}

		@Override
		public Body firstSegment(Rope rope) {
			return first = super.firstSegment(rope);
		}

		@Override
		public Body lastSegment(Body previous) {
			Body last = super.lastSegment(previous);
			connect(last, first);
			return last;
		}

	}

	private Builder builder;
	private final SnapshotArray<Body> segments;
	private final SnapshotArray<Joint> joints;
	private boolean locked = true;

	public Rope(int length, Builder builder) {
		segments = new SnapshotArray<Body>(length);
		joints = new SnapshotArray<Joint>(length - 1);
		this.builder = builder;
		segments.add(builder.firstSegment(this));
		for(--length; length > 1; length--) {
			segments.add(builder.nextSegment(segments.peek()));
			joints.add(builder.connect(segments.get(segments.size - 2), segments.peek()));
		}
		segments.add(builder.lastSegment(segments.peek()));
		joints.add(builder.connect(segments.get(segments.size - 2), segments.peek()));
		lock();
	}

	private void lock() {
		if(locked)
			return;
		segments.begin();
		joints.begin();
		locked = true;
	}
	private void unlock() {
		if(!locked)
			return;
		segments.end();
		joints.end();
		locked = false;
	}

	public int length() {
		return segments.size;
	}

	public Body addSegment() {
		Body last = segments.peek(), segment = builder.nextSegment(last);
		unlock();
		segments.add(segment);
		joints.add(builder.connect(segment, last));
		lock();
		return segment;
	}

	public Body insertSegment(int index) {
		Body previous = segments.get(index), segment = builder.nextSegment(previous);
		unlock();
		segments.insert(index, segment);
		joints.insert(index, builder.connect(previous, segment));
		lock();
		return segment;
	}

	public void destroySegment(int index) { // TODO finish how to destroy things with rope segments
		Body segment = unlinkSegment(index);
		segment.getWorld().destroyBody(segment);
	}

	public Body unlinkSegment(int index) {
		unlock();
		Body segment = segments.removeIndex(index);
		for(JointEdge edge : segment.getJointList())
			if(joints.contains(edge.joint, true)) {
				segment.getWorld().destroyJoint(edge.joint);
				joints.removeValue(edge.joint, true);
			}
		lock();
		return segment;
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
	public SnapshotArray<Body> getSegments() {
		return segments;
	}

	/** @return the {@link #joints} */
	public SnapshotArray<Joint> getJoints() {
		return joints;
	}

}
