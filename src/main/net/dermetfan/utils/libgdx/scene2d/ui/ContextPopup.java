package net.dermetfan.utils.libgdx.scene2d.ui;

import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import net.dermetfan.utils.Function;

/** an {@link ImmutableContextPopup} that is not immutable
 *  @author dermetfan
 *  @since 0.4.0 */
public class ContextPopup<T extends Actor> extends ImmutableContextPopup<T> {

	public ContextPopup(T popup) {
		super(popup);
	}

	// getters and setters

	/** @return the {@link #handler} */
	public Function<Boolean, InputEvent> getHandler() {
		return handler;
	}

	/** @param handler the {@link #handler} to set */
	public void setHandler(Function<Boolean, InputEvent> handler) {
		this.handler = handler;
	}

}
