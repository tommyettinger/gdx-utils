package net.dermetfan.utils.libgdx.scene2d.ui;

import java.io.File;
import java.io.FileFilter;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pools;

public abstract class FileChooser extends Table {

	/** called by a {@link ListFileChooser}
	 *  @author dermetfan */
	public static interface Listener {

		/** @return if the selection should be rejected */
		public boolean choose(FileHandle file);

		/** The Array given into the method will be {@link Pools#free(Object) returned} to the pool after the call, so make a copy if you need one.
		 * 	@return if the selection should be rejected
		 *  @see #choose(FileHandle) */
		public boolean choose(Array<FileHandle> files);

		/** @return if canceling should be rejected */
		public boolean cancel();

	}

	private Listener listener;

	protected final FileFilter handlingFileFilter = new FileFilter() {

		@Override
		public boolean accept(File file) {
			return (showHidden || !file.isHidden()) && (fileFilter == null || fileFilter != null && fileFilter.accept(file));
		}

	};

	private FileFilter fileFilter;

	private boolean showHidden = false;

	public FileChooser() {}

	public FileChooser(Listener listener) {
		this.listener = listener;
	}

	protected abstract void build();

	/** @return the {@link #listener} */
	public Listener getListener() {
		return listener;
	}

	/** @param listener the {@link #listener} to set */
	public void setListener(Listener listener) {
		this.listener = listener;
	}

	/** @return the {@link #fileFilter} */
	public FileFilter getFileFilter() {
		return fileFilter;
	}

	/** @param fileFilter the {@link #fileFilter} to set */
	public void setFileFilter(FileFilter fileFilter) {
		this.fileFilter = fileFilter;
	}

	/** @return the {@link #showHidden} */
	public boolean isShowHidden() {
		return showHidden;
	}

	/** @param showHidden the {@link #showHidden} to set */
	public void setShowHidden(boolean showHidden) {
		this.showHidden = showHidden;
	}

}
