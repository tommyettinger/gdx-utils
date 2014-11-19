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

package net.dermetfan.gdx.scenes.scene2d.ui;

import java.io.File;
import java.io.FileFilter;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
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
import net.dermetfan.utils.Function;

/** A {@link FileChooser} that uses a {@link Tree}. <strong>DO NOT FORGET TO {@link #add(FileHandle) ADD ROOTS}!</strong>
 *  @author dermetfan */
public class TreeFileChooser extends FileChooser {

	/** @see #fileNode(FileHandle, Label.LabelStyle, Function) */
	public static Node fileNode(FileHandle file, LabelStyle labelStyle) {
		return fileNode(file, labelStyle, null);
	}

	/** @see #fileNode(FileHandle, FileFilter, Label.LabelStyle, Function) */
	public static Node fileNode(FileHandle file, LabelStyle labelStyle, Function<Void, Node> nodeConsumer) {
		return fileNode(file, null, labelStyle, nodeConsumer);
	}

	/** @see #fileNode(FileHandle, FileFilter, Label.LabelStyle, Function) */
	public static Node fileNode(FileHandle file, FileFilter filter, final LabelStyle labelStyle) {
		return fileNode(file, filter, labelStyle, null);
	}

	/** passes an Accessor that creates labels representing the file name (with slash if it's a folder) using the given label style to {@link #fileNode(FileHandle, FileFilter, net.dermetfan.utils.Function, net.dermetfan.utils.Function)} (labelSupplier)
	 *  @param labelStyle the {@link LabelStyle} to use for created labels
	 *  @see #fileNode(FileHandle, FileFilter, Function, Function) */
	public static Node fileNode(FileHandle file, FileFilter filter, final LabelStyle labelStyle, Function<Void, Node> nodeConsumer) {
		return fileNode(file, filter, new Function<Label, FileHandle>() {
			@Override
			public Label apply(FileHandle file) {
				String name = file.name();
				if(name.length() == 0) {
					name = file.path();
					name = name.substring(0, name.lastIndexOf('/'));
				}
				if(file.isDirectory())
					name += File.separatorChar;
				return new Label(name, labelStyle);
			}
		}, nodeConsumer);
	}

	/** @see #fileNode(FileHandle, FileFilter, Function, Function) */
	public static Node fileNode(FileHandle file, FileFilter filter, Function<Label, FileHandle> labelSupplier) {
		return fileNode(file, filter, labelSupplier, null);
	}

	/** creates an anonymous subclass of {@link Node} that recursively adds the children of the given file to it when being {@link Node#setExpanded(boolean) expanded} for the first time
	 *  @param file the file to put in {@link Node#setObject(Object)}
	 *  @param filter Filters children from being added. May be null to accept all files.
	 *  @param labelSupplier supplies labels to use
	 *  @param nodeConsumer Does something with nodes after they were created. May be null.
	 *  @return the created Node */
	public static Node fileNode(final FileHandle file, final FileFilter filter, final Function<Label, FileHandle> labelSupplier, final Function<Void, Node> nodeConsumer) {
		Label label = labelSupplier.apply(file);

		Node node;
		if(file.isDirectory()) {
			final Node dummy = new Node(new Actor());

			node = new Node(label) {
				private boolean childrenAdded;

				@Override
				public void setExpanded(boolean expanded) {
					if(expanded == isExpanded())
						return;

					if(expanded && !childrenAdded) {
						if(filter != null)
							for(FileHandle child : file.list(filter))
								add(fileNode(file.child(child.name()), filter, labelSupplier, nodeConsumer));
						else
							for(FileHandle child : file.list())
								add(fileNode(child, filter, labelSupplier, nodeConsumer));
						childrenAdded = true;
						remove(dummy);
					}

					super.setExpanded(expanded);
				}
			};
			node.add(dummy);

			if(nodeConsumer != null)
				nodeConsumer.apply(dummy);
		} else
			node = new Node(label);
		node.setObject(file);

		if(nodeConsumer != null)
			nodeConsumer.apply(node);

		return node;
	}

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
			if(selection.getMultiple() && selection.size() > 1) {
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
	 *  @see #TreeFileChooser(Style, FileChooser.Listener) */
	public TreeFileChooser(Skin skin, Listener listener) {
		this(skin.get(Style.class), listener);
		setSkin(skin);
	}

	/** @param skin the skin holding the {@link Style} to use
	 *  @param styleName the {@link Skin#get(String, Class) name} of the {@link Style} to use
	 *  @param listener the {@link #setListener(Listener) listener}
	 *  @see #TreeFileChooser(Style, FileChooser.Listener)*/
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
	 *  @return the added {@link #fileNode(FileHandle, FileFilter, Label.LabelStyle) file node} */
	public Node add(FileHandle file) {
		Node node = fileNode(file, handlingFileFilter, style.labelStyle);
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
		(chooseButton = UIUtils.newButton(style.selectButtonStyle, "select")).addListener(chooseButtonListener);
		chooseButton.setDisabled(true);
		(cancelButton = UIUtils.newButton(style.cancelButtonStyle, "cancel")).addListener(cancelButtonListener);
	}

	@Override
	protected void build() {
		clearChildren();
		treePane.setWidget(tree);
		add(treePane).colspan(2).row();
		add(chooseButton).fill();
		add(cancelButton).fill();
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

	/** @return the {@link #tree} */
	public Tree getTree() {
		return tree;
	}

	/** @param tree the {@link #tree} to set */
	public void setTree(Tree tree) {
		this.tree.removeListener(treeListener);
		(this.tree = tree).addListener(treeListener);
		treePane.setWidget(tree);
	}

	/** @return the {@link #treePane} */
	public ScrollPane getTreePane() {
		return treePane;
	}

	/** @param treePane the {@link #treePane} to set */
	public void setTreePane(ScrollPane treePane) {
		treePane.setWidget(tree);
		getCell(this.treePane).setActor(this.treePane = treePane);
	}

	/** @return the {@link #chooseButton} */
	public Button getChooseButton() {
		return chooseButton;
	}

	/** @param chooseButton the {@link #chooseButton} to set */
	public void setChooseButton(Button chooseButton) {
		this.chooseButton.removeListener(chooseButtonListener);
		chooseButton.addListener(chooseButtonListener);
		getCell(this.chooseButton).setActor(this.chooseButton = chooseButton);
	}

	/** @return the {@link #cancelButton} */
	public Button getCancelButton() {
		return cancelButton;
	}

	/** @param cancelButton the {@link #cancelButton} to set */
	public void setCancelButton(Button cancelButton) {
		this.cancelButton.removeListener(cancelButtonListener);
		cancelButton.addListener(cancelButtonListener);
		getCell(this.cancelButton).setActor(this.cancelButton = cancelButton);
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
			selectButtonStyle = UIUtils.readButtonStyle("selectButtonStyle", json, jsonData);
			cancelButtonStyle = UIUtils.readButtonStyle("cancelButtonStyle", json, jsonData);
			if(jsonData.has("background"))
				background = json.readValue("background", Drawable.class, jsonData);
		}

	}

}
