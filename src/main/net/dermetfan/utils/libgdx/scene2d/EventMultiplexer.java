package net.dermetfan.utils.libgdx.scene2d;

import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.utils.Array;

/** an {@link EventListener} that notifies an array of other EventListeners
 *  @author dermetfan */
public class EventMultiplexer implements EventListener {

	/** the listeners to notify */
	private Array<EventListener> listeners;

	/** @param listeners the listeners to notify */
	public EventMultiplexer(EventListener... listeners) {
		this.listeners = new Array<>(listeners);
	}

	/** @param listeners the {@link #listeners} */
	public EventMultiplexer(Array<EventListener> listeners) {
		this.listeners = listeners;
	}

	/** @see Array#add(Object) */
	public void add(EventListener listener) {
		listeners.add(listener);
	}

	/** @see Array#removeValue(Object, boolean) */
	public void remove(EventListener value) {
		listeners.removeValue(value, true);
	}

	/** @see Array#size */
	public int size() {
		return listeners.size;
	}

	/** @see Array#clear()  */
	public void clear() {
		listeners.clear();
	}

	/** @return if one of the {@link #listeners} returned true in {@link EventListener#handle(com.badlogic.gdx.scenes.scene2d.Event) handle(Event)} */
	@Override
	public boolean handle(Event event) {
		boolean handled = false;
		for(EventListener listener : listeners)
			handled |= listener.handle(event);
		return handled;
	}

	/** @return the {@link #listeners} */
	public Array<EventListener> getListeners() {
		return listeners;
	}

	/** @param listeners the {@link #listeners} to set */
	public void setListeners(Array<EventListener> listeners) {
		this.listeners = listeners;
	}

}
