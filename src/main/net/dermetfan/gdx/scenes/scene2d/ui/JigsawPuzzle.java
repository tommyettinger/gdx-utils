/** Copyright 2015 Robin Stumm (serverkorken@gmail.com, http://dermetfan.net)
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

package net.dermetfan.gdx.scenes.scene2d.ui;

import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop;
import com.badlogic.gdx.scenes.scene2d.utils.DragAndDrop.Payload;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;
import net.dermetfan.gdx.scenes.scene2d.Scene2DUtils;
import net.dermetfan.gdx.scenes.scene2d.utils.PolygonRegionDrawable;

/** represents a puzzle and manages {@link Piece Pieces}
 *  @author dermetfan
 *  @since 0.10.0 */
public class JigsawPuzzle {

	/** the {@link Piece pieces} of the puzzle */
	private final Array<Piece> pieces;

	public JigsawPuzzle() {
		pieces = new Array<>(Piece.class);
	}

	/** @param pieces the amount of pieces that will probably be in this puzzle */
	public JigsawPuzzle(int pieces) {
		this.pieces = new Array<>(pieces);
	}

	/** @param pieces the {@link #pieces} */
	public JigsawPuzzle(Piece... pieces) {
		this.pieces = new Array<>(pieces);
	}

	/** solves the puzzle by letting all {@link #pieces} {@link JigsawPuzzle.Piece#place(JigsawPuzzle.Piece) snap} into their spot
	 *  @param relativeTo the piece relative to which the puzzle should be solved */
	public void solve(Piece relativeTo) {
		if(!pieces.contains(relativeTo, true))
			throw new IllegalArgumentException("the reference piece is not part of the puzzle");
		for(Piece piece : pieces) {
			if(piece == relativeTo)
				continue;
			piece.place(relativeTo);
		}
	}

	/** @param piece the piece to add to {@link #pieces} */
	public void add(Piece piece) {
		if(!pieces.contains(piece, true))
			pieces.add(piece);
	}

	/** @param piece the piece to remove from {@link #pieces}
	 *  @return if the piece was found and removed */
	public boolean remove(Piece piece) {
		return pieces.removeValue(piece, true);
	}

	/** @param piece the piece to which to find the closest other piece
	 *  @return the piece most correctly placed in relation to the given piece
	 *  @deprecated no known use case */
	@Deprecated
	public Piece findClosest(Piece piece) {
		float distance = Float.POSITIVE_INFINITY;
		Piece closest = null;
		for(Piece other : pieces) {
			if(other == piece)
				continue;
			float dist = Vector2.dst(other.getX() + other.getSlotX(), other.getY() + other.getSlotY(), piece.getX() + piece.getSlotX(), piece.getY() + piece.getSlotY());
			if(dist < distance) {
				distance = dist;
				closest = other;
			}
		}
		return closest;
	}

	/** @param tolerance the distance by which each piece is allowed to be off
	 *  @return if the puzzle is solved */
	public boolean isSolved(float tolerance) {
		Piece reference = pieces.first();
		for(Piece piece : pieces)
			if(!piece.isPlacedCorrectly(reference, tolerance))
				return false;
		return true;
	}

	/** called by {@link JigsawPuzzle.Target#placed(JigsawPuzzle.Piece) placed} when all pieces are placed correctly */
	protected void solved() {}

	/** @return the {@link #pieces} */
	public Array<Piece> getPieces() {
		return pieces;
	}

	/** a piece on a {@link JigsawPuzzle}
	 *  @author dermetfan
	 *  @since 0.10.0 */
	public static class Piece extends Image {

		/** the position of the piece on the puzzle (the {@link PolygonRegionDrawable#getPolygonX() minX} and {@link PolygonRegionDrawable#getPolygonY() minY} of its vertices) */
		private float slotX, slotY;

		public Piece(Drawable drawable) {
			super(drawable);
		}

		/** @param drawable Sets the {@link #setSlot(float, float) slot} to the drawable's {@link PolygonRegionDrawable#getPolygonX() polygonX} and {@link PolygonRegionDrawable#getPolygonY() polygonY} if the given drawable is a {@link PolygonRegionDrawable}. Otherwise sets it to 0:0. */
		@Override
		public void setDrawable(Drawable drawable) {
			super.setDrawable(drawable);
			if(drawable instanceof PolygonRegionDrawable) {
				PolygonRegionDrawable pd = (PolygonRegionDrawable) drawable;
				slotX = pd.getPolygonX();
				slotY = pd.getPolygonY();
			} else
				slotX = slotY = 0;
		}

		@Override
		public Actor hit(float x, float y, boolean touchable) {
			Actor hit = super.hit(x, y, touchable);
			PolygonRegionDrawable drawable = getDrawable() instanceof PolygonRegionDrawable ? (PolygonRegionDrawable) getDrawable() : null;
			if(hit == this && drawable != null) {
				float[] vertices = drawable.getRegion().getVertices();
				if(!Intersector.isPointInPolygon(vertices, 0, vertices.length, x / getWidth() * drawable.getPolygonWidth() + slotX, y / getHeight() * drawable.getPolygonHeight() + slotY))
					return null;
			}
			return hit;
		}

		/** @param reference the piece in relation to which to this piece should snap in its spot */
		public void place(Piece reference) {
			Vector2 refPuzzlePoint = Pools.obtain(Vector2.class).set(reference.getX(), reference.getY()).sub(reference.slotX, reference.slotY);
			setPosition(refPuzzlePoint.x + slotX, refPuzzlePoint.y + slotY);
			Pools.free(refPuzzlePoint);
		}

		/** @param reference the piece in relation to which this piece's position should be checked
		 *  @param tolerance the distance by which each piece is allowed to be off
		 *  @return if this piece is placed correctly in relation to the given reference piece with the given tolerance */
		public boolean isPlacedCorrectly(Piece reference, float tolerance) {
			// get bottom left corner of each puzzle
			Vector2 puzzlePos = Pools.obtain(Vector2.class).set(-slotX, -slotY), refPuzzlePoint = Pools.obtain(Vector2.class).set(-reference.slotX, -reference.slotY);
			localToStageCoordinates(puzzlePos);
			reference.localToStageCoordinates(refPuzzlePoint);

			// see if they're the same
			boolean rel = puzzlePos.epsilonEquals(refPuzzlePoint, tolerance);

			Pools.free(puzzlePos);
			Pools.free(refPuzzlePoint);

			return rel;
		}

		// getters and setters

		/** @param slotX the {@link #slotX} to set
		 *  @param slotY the {@link #slotY} to set */
		public void setSlot(float slotX, float slotY) {
			this.slotX = slotX;
			this.slotY = slotY;
		}

		/** @return the {@link #slotX} */
		public float getSlotX() {
			return slotX;
		}

		/** @param slotX the {@link #slotX} to set */
		public void setSlotX(float slotX) {
			this.slotX = slotX;
		}

		/** @return the {@link #slotY} */
		public float getSlotY() {
			return slotY;
		}

		/** @param slotY the {@link #slotY} to set */
		public void setSlotY(float slotY) {
			this.slotY = slotY;
		}

	}

	/** @author dermetfan
	 *  @since 0.10.0 */
	public class Source extends DragAndDrop.Source {

		/** the DragAndDrop currently using this Source */
		private DragAndDrop dragAndDrop;

		/** the time it takes for the piece to move back */
		private float moveBackDuration = .5f;

		/** temporary Payload for internal use */
		private final Payload payload = new Payload();

		/** temporary Vector2 for internal use */
		private final Vector2 vec2 = new Vector2();

		/** @param dragAndDrop the {@link #dragAndDrop} */
		public Source(Group board, DragAndDrop dragAndDrop) {
			super(board);
			this.dragAndDrop = dragAndDrop;
		}

		/** @param moveBackDuration the {@link #moveBackDuration} */
		public Source(final Group board, final DragAndDrop dragAndDrop, float moveBackDuration) {
			this(board, dragAndDrop);
			this.moveBackDuration = moveBackDuration;
		}

		@Override
		public Payload dragStart(InputEvent event, float x, float y, int pointer) {
			Actor actor = getActor().hit(x, y, true); // get actor under pointer
			// don't drag the board itself or anything that's not a piece of the puzzle
			if(actor == getActor() || !(actor instanceof Piece) || !JigsawPuzzle.this.getPieces().contains((Piece) actor, true))
				return null;
			payload.setDragActor(actor);

			// put the actor in the right position under the pointer
			((Group) getActor()).localToDescendantCoordinates(actor, vec2.set(x, y));
			dragAndDrop.setDragActorPosition(-vec2.x, -vec2.y + actor.getHeight());

			vec2.set(actor.getX(), actor.getY()); // set vec2 for dragStop (to the current position to be able to move the piece back)
			return payload;
		}

		@Override
		public void dragStop(InputEvent event, float x, float y, int pointer, Payload payload, DragAndDrop.Target target) {
			final Actor actor = payload.getDragActor();
			if(actor.getParent() == null) { // move back to where the piece was dragged from
				// put it on the stage in the correct position
				getActor().getStage().addActor(actor);
				getActor().localToStageCoordinates(vec2);
				// move back
				actor.addAction(Actions.sequence(Actions.moveTo(vec2.x, vec2.y, moveBackDuration), Actions.run(new Runnable() {
					@Override
					public void run() {
						((Group) getActor()).addActor(actor);
						// set position correctly on the board
						getActor().stageToLocalCoordinates(vec2);
						actor.setPosition(vec2.x, vec2.y);
					}
				})));
			}
		}

		// getters and setters

		/** @return the {@link #dragAndDrop} */
		public DragAndDrop getDragAndDrop() {
			return dragAndDrop;
		}

		/** @param dragAndDrop the {@link #dragAndDrop} to set */
		public void setDragAndDrop(DragAndDrop dragAndDrop) {
			this.dragAndDrop = dragAndDrop;
		}

		/** @return the enclosing JigsawPuzzle instance */
		public JigsawPuzzle getPuzzle() {
			return JigsawPuzzle.this;
		}

		/** @return the {@link #moveBackDuration} */
		public float getMoveBackDuration() {
			return moveBackDuration;
		}

		/** @param moveBackDuration the {@link #moveBackDuration} to set */
		public void setMoveBackDuration(float moveBackDuration) {
			this.moveBackDuration = moveBackDuration;
		}

	}

	/** @author dermetfan
	 *  @since 0.10.0 */
	public class Target extends DragAndDrop.Target {

		/** the distance by which each piece is allowed to be off */
		private float tolerance;

		/** @param tolerance the {@link #tolerance} */
		public Target(Group group, float tolerance) {
			super(group);
			this.tolerance = tolerance;
		}

		@Override
		public boolean drag(DragAndDrop.Source source, Payload payload, float x, float y, int pointer) {
			return true;
		}

		@Override
		public void drop(DragAndDrop.Source source, Payload payload, float x, float y, int pointer) {
			Actor dragged = payload.getDragActor();
			Scene2DUtils.addAtStageCoordinates(dragged, (Group) getActor());
			if(dragged instanceof Piece) {
				Piece piece = (Piece) dragged;
				for(int i = 0; i < JigsawPuzzle.this.getPieces().size; i++) {
					Piece ref = JigsawPuzzle.this.getPieces().get(i);
					if(ref == piece)
						continue;
					if(piece.isPlacedCorrectly(ref, tolerance)) {
						piece.place(ref);
						placed(piece);
						break;
					}
				}
			}
		}

		/** called by {@link JigsawPuzzle.Target#drop(DragAndDrop.Source, DragAndDrop.Payload, float, float, int) drop} when a piece is placed
		 *  @param piece the placed piece */
		protected void placed(Piece piece) {
			if(JigsawPuzzle.this.isSolved(tolerance))
				JigsawPuzzle.this.solved();
		}

		// getters and setters

		/** @return the enclosing JigsawPuzzle instance */
		public JigsawPuzzle getPuzzle() {
			return JigsawPuzzle.this;
		}

		/** @return the {@link #tolerance} */
		public float getTolerance() {
			return tolerance;
		}

		/** @param tolerance the {@link #tolerance} to set */
		public void setTolerance(float tolerance) {
			this.tolerance = tolerance;
		}

	}

}
