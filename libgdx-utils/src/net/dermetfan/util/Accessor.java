package net.dermetfan.util;

/** a generic accessor
 *  @author dermetfan */
public interface Accessor {

	/** @param object the O to access
	 *  @return the accessed T from O */
	public <T, O> T access(O object);

}
