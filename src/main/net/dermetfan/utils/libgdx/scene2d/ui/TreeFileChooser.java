package net.dermetfan.utils.libgdx.scene2d.ui;

import java.io.File;

import net.dermetfan.utils.libgdx.scene2d.Scene2DUtils;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Tree;
import com.badlogic.gdx.scenes.scene2d.ui.Tree.Node;
import com.badlogic.gdx.scenes.scene2d.ui.Tree.TreeStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.Selection;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Json.Serializable;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Pools;

/** A {@link FileChooser} that uses a {@link Tree}. <strong>DO NOT FORGET TO {@link #add(FileHandle) ADD ROOTS}!</strong>
 *  @author dermetfan */
public class TreeFileChooser extends FileChooser {

	/** the style of this TreeFileChooser */
	private Style style;

	/** the Tree used to show files and folders */
	private Tree tree;

	/** the ScrollPane {@link #tree} is embedded in */
	private ScrollPane treePane;

	/** basic operation buttons */
	private Button chooseButton, cancelButton;

	/** Listener for {@link #tree}.
	 *  {@link Button#setDisabled(boolean) Disables/enables} {@link #chooseButton} based on the {@link Tree#getSelection() selection} of {@link #tree} and {@link #isDirectoriesChoosable()} */
	public final ClickListener treeListener = new ClickListener() {

		@Override
		public void clicked(InputEvent event, float x, float y) {
			Selection<Node> selection = tree.getSelection();
			if(selection.size() < 1) {
				chooseButton.setDisabled(true);
				return;
			}
			if(!isDirectoriesChoosable()) {
				Object lastObj = selection.getLastSelected().getObject();
				if(lastObj instanceof FileHandle) {
					FileHandle file = (FileHandle) lastObj;
					if(file.isDirectory()) {
						chooseButton.setDisabled(true);
						return;
					}
				}
			}
			chooseButton.setDisabled(false);
		}

	};

	/** Listener for {@link #chooseButton}.
	 *  Calls {@link Listener#choose(Array)} or {@link Listener#choose(FileHandle)} depending on the {@link Tree#getSelection() selection} of {@link #tree} */
	public final ClickListener chooseButtonListener = new ClickListener() {

		@Override
		public void clicked(InputEvent event, float x, float y) {
			if(chooseButton.isDisabled())
				return;
			Selection<Node> selection = tree.getSelection();
			if(selection.size() < 1)
				return;
			if(selection.getMultiple()) {
				@SuppressWarnings("unchecked")
				Array<FileHandle> files = Pools.obtain(Array.class);
				for(Node node : selection) {
					Object object = node.getObject();
					if(object instanceof FileHandle) {
						FileHandle file = (FileHandle) object;
						if(isDirectoriesChoosable() || !file.isDirectory())
							files.add(file);
					}
				}
				getListener().choose(files);
				files.clear();
				Pools.free(files);
			} else {
				Object object = selection.getLastSelected().getObject();
				if(object instanceof FileHandle) {
					FileHandle file = (FileHandle) object;
					if(isDirectoriesChoosable() || !file.isDirectory())
						getListener().choose(file);
				}
			}
		}

	};

	/** Listener for {@link #cancelButton}.
	 *  Calls {@link Listener#cancel()} of the {@link #getListener() listener} */
	public final ClickListener cancelButtonListener = new ClickListener() {

		@Override
		public void clicked(InputEvent event, float x, float y) {
			getListener().cancel();
		}

	};

	/** @param skin the skin to get a {@link Style} from
	 *  @param listener the {@link #setListener(Listener) listener}
	 *  @see #TreeFileChooser(Style, Listener) */
	public TreeFileChooser(Skin skin, Listener listener) {
		this(skin.get(Style.class), listener);
		setSkin(skin);
	}

	/** @param skin the skin holding the {@link Style} to use
	 *  @param styleName the {@link Skin#get(String, Class) name} of the {@link Style} to use
	 *  @param listener the {@link #setListener(Listener) listener}
	 *  @see #TreeFileChooser(Style, Listener)*/
	public TreeFileChooser(Skin skin, String styleName, Listener listener) {
		this(skin.get(styleName, Style.class), listener);
		setSkin(skin);
	}

	/** @param style the {@link #style}
	 *  @param listener the {@link #setListener(Listener) listener} */
	public TreeFileChooser(Style style, Listener listener) {
		super(listener);
		this.style = style;
		buildWidgets();
		build();
	}

	/** @param file the {@link File} to {@link Tree#add(Node) add a root} for
	 *  @return the added {@link Scene2DUtils#fileNode(FileHandle, java.io.FileFilter, LabelStyle) file node} */
	public Node add(FileHandle file) {
		Node node = Scene2DUtils.fileNode(file, handlingFileFilter, style.labelStyle);
		tree.add(node);
		return node;
	}

	/** builds {@link #chooseButton}, {@link #cancelButtonListener}, {@link #tree}, {@link #treePane} */
	protected void buildWidgets() {
		(tree = new Tree(style.treeStyle)).addListener(treeListener);
		if(style.scrollPaneStyle != null)
			treePane = new ScrollPane(tree, style.scrollPaneStyle);
		else
			treePane = new ScrollPane(tree);
		(chooseButton = Scene2DUtils.newButton(style.selectButtonStyle, "select")).addListener(chooseButtonListener);
		chooseButton.setDisabled(true);
		(cancelButton = Scene2DUtils.newButton(style.cancelButtonStyle, "cancel")).addListener(cancelButtonListener);
	}

	@Override
	protected void build() {
		clearChildren();
		treePane.setWidget(tree);
		add(treePane).colspan(2).row();
		add(chooseButton).fill();
		add(cancelButton).fill();
	}

	/** @return the {@link #tree} */
	public Tree getTree() {
		return tree;
	}

	/** @param tree the {@link #tree} to set */
	public void setTree(Tree tree) {
		if(tree == null)
			throw new IllegalArgumentException("tree must not be null");
		this.tree = tree;
	}

	/** @return the {@link #style} */
	public Style getStyle() {
		return style;
	}

	/** @param style the {@link #style} to set */
	public void setStyle(Style style) {
		this.style = style;
		setBackground(style.background);
		tree.setStyle(style.treeStyle);
		chooseButton.setStyle(style.selectButtonStyle);
		cancelButton.setStyle(style.cancelButtonStyle);
	}

	/** defines styles for the widgets of a {@link TreeFileChooser}
	 *  @author dermetfan */
	public static class Style implements Serializable {

		/** the style for the {@link TreeFileChooser#tree tree} */
		public TreeStyle treeStyle;

		/** the style for {@link TreeFileChooser#treePane} */
		public ScrollPaneStyle scrollPaneStyle;

		/** the style for the labels in the tree */
		public LabelStyle labelStyle;

		/** the button styles */
		public ButtonStyle selectButtonStyle, cancelButtonStyle;

		/** optional */
		public Drawable background;

		@Override
		public void write(Json json) {
			json.writeObjectStart("");
			json.writeFields(this);
			json.writeObjectEnd();
		}

		@Override
		public void read(Json json, JsonValue jsonData) {
			treeStyle = json.readValue("treeStyle", TreeStyle.class, jsonData);
			if(jsonData.has("scrollPaneStyle"))
				scrollPaneStyle = json.readValue("scrollPaneStyle", ScrollPaneStyle.class, jsonData);
			labelStyle = json.readValue("labelStyle", LabelStyle.class, jsonData);
			selectButtonStyle = Scene2DUtils.readButtonStyle("selectButtonStyle", json, jsonData);
			cancelButtonStyle = Scene2DUtils.readButtonStyle("cancelButtonStyle", json, jsonData);
			if(jsonData.has("background"))
				background = json.readValue("background", Drawable.class, jsonData);
		}

	}

}
