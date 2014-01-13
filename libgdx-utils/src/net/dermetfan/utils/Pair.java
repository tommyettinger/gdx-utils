package net.dermetfan.utils;

/** @param <K> the type of the key 
 *  @param <V> the type of the value
 *  @author dermetfan */
public class Pair<K, V> implements Cloneable {

	/** the key */
	protected K key;

	/** the value */
	protected V value;

	/** @param key the {@link #key}
	 *  @param value the {@link #value} */
	public Pair(K key, V value) {
		this.key = key;
		this.value = value;
	}

	/** @param pair the {@link Pair} to copy */
	public Pair(Pair<K, V> pair) {
		this.key = pair.key;
		this.value = pair.value;
	}

	/** swaps key and value
	 *  @throws IllegalStateException if {@link #key} and {@link #value} are not of the same class */
	@SuppressWarnings("unchecked")
	public void swap() {
		if(key.getClass() != value.getClass())
			throw new IllegalStateException("key and value are not of the same type");
		K oldValue = (K) value;
		value = (V) key;
		key = oldValue;
	}

	/** @return the {@link #key} */
	public K key() {
		return key;
	}

	/** @param key the {@link #key} to set */
	public void key(K key) {
		this.key = key;
	}

	/** @return the {@link #value} */
	public V value() {
		return value;
	}

	/** @param value the {@link #value} to set */
	public void value(V value) {
		this.value = value;
	}

	/** if the given object is a {@link Pair} instance, {@link Object#equals(Object) equals} comparison will be used on key and value */
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof Pair) {
			Pair<?, ?> pair = (Pair<?, ?>) obj;
			return key.equals(pair.key) && value.equals(pair.value);
		}
		return super.equals(obj);
	}

	/** @see #Pair(Pair) */
	@Override
	public Pair<K, V> clone() {
		return new Pair<K, V>(this);
	}

	/** @return [key, value] */
	@Override
	public String toString() {
		return "[" + key + ", " + value + "]";
	}

}
