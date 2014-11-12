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

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.InputListener;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldListener;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.scenes.scene2d.utils.Selection;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Json.Serializable;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Pools;

/** A {@link TextField} showing the {@link #pathField} of the currently browsed folder with {@link #backButton} and {@link #parentButton} buttons.
 *  There's a {@link #contentsPane scrollable} {@link List} under those showing the contents of the currently browsed folder and {@link #chooseButton} and {@link #cancelButton} buttons.
 *  If {@link FileChooser#isDirectoriesChoosable() directories can be chosen}, a {@link #openButton} button is added so that the user is able to go into folders.
 *  Files can be {@link #fileFilter filtered}.
 *  Use the {@link #listener listener} to get user input.
 *  @author dermetfan */
public class ListFileChooser extends FileChooser {

	/** the style */
	private Style style;

	/** the directories that have been visited previously, for the {@link #backButton} */
	private Array<FileHandle> fileHistory = new Array<>();

	/** the current directory */
	private FileHandle directory = Gdx.files.absolute(Gdx.files.getExternalStoragePath());
	{ fileHistory.add(directory); }

	/** @see #pathFieldListener */
	private TextField pathField;

	/** shows the {@link FileHandle#list() children} of current {@link #directory} */
	private List<String> contents;

	/** makes the {@link #contents} scrollable */
	private ScrollPane contentsPane;

	/** @see #backButtonListener */
	private Button backButton;

	/** @see #parentButtonListener */
	private Button parentButton;

	/** @see #chooseButtonListener */
	private Button chooseButton;

	/** @see #openButtonListener */
	private Button openButton;

	/** @see #cancelButtonListener */
	private Button cancelButton;

	/** if it exists, this open the file at the given {@link FileType#Absolute absolute} path if it is not a folder, {@link #setDirectory(FileHandle) goes into} it otherwise, */
	public final TextFieldListener pathFieldListener = new TextFieldListener() {
		@Override
		public void keyTyped(TextField textField, char key) {
			if(key == '\r' || key == '\n') {
				FileHandle loc = Gdx.files.absolute(textField.getText());
				if(loc.exists()) {
					if(loc.isDirectory())
						setDirectory(loc);
					else
						getListener().choose(loc);
					getStage().setKeyboardFocus(ListFileChooser.this);
				}
			}
		}
	};

	/** {@link Listener#choose(FileHandle) chooses} the {@link List#getSelection() selected} file in from the {@link #contents} */
	public final ClickListener chooseButtonListener = new ClickListener() {
		@Override
		public void clicked(InputEvent event, float x, float y) {
			Selection<String> selection = contents.getSelection();
			if(!selection.getMultiple()) {
				FileHandle selected = currentlySelected();
				if(!isDirectoriesChoosable() && selected.isDirectory())
					setDirectory(selected);
				else
					getListener().choose(selected);
			} else {
				@SuppressWarnings("unchecked")
				Array<FileHandle> files = Pools.obtain(Array.class);
				files.clear();
				for(String fileName : selection)
					files.add(directory.child(fileName));
				getListener().choose(files);
				Pools.free(files);
			}
		}
	};

	/** goes into the currently marked folder */
	public final ClickListener openButtonListener = new ClickListener() {
		@Override
		public void clicked(InputEvent event, float x, float y) {
			FileHandle child = currentlySelected();
			if(child.isDirectory())
				setDirectory(child);
		}
	};

	/** @see Listener#cancel() */
	public final ClickListener cancelButtonListener = new ClickListener() {
		@Override
		public void clicked(InputEvent event, float x, float y) {
			getListener().cancel();
		}
	};

	/** goes back to the {@link #fileHistory previous} {@link #directory} */
	public final ClickListener backButtonListener = new ClickListener() {
		@Override
		public void clicked(InputEvent event, float x, float y) {
			if(fileHistory.size > 1) {
				fileHistory.removeIndex(fileHistory.size - 1);
				setDirectory(directory = fileHistory.peek(), false);
			}
		}
	};

	/** {@link #setDirectory(FileHandle) sets} {@link #directory} to its {@link FileHandle#parent() parent} */
	public final ClickListener parentButtonListener = new ClickListener() {
		@Override
		public void clicked(InputEvent event, float x, float y) {
			setDirectory(directory.parent());
		}
	};

	/** {@link Button#setDisabled(boolean) enables/disables} {@link #chooseButton} and {@link #openButton} */
	public final ChangeListener contentsListener = new ChangeListener() {
		@Override
		public void changed(ChangeEvent event, Actor actor) {
			openButton.setDisabled(!currentlySelected().isDirectory());
			chooseButton.setDisabled(isDirectoriesChoosable());
		}
	};

	/** key controls of {@link #contents} */
	public final InputListener keyControlsListener = new InputListener() {
		@Override
		public boolean keyTyped(InputEvent event, char c) {
			if(event.isHandled())
				return true;

			if(getStage().getKeyboardFocus() != pathField && (c == '\r' || c == 'n')) {
				if(currentlySelected().isDirectory())
					openButtonListener.clicked(null, 0, 0); // fake event
				else
					chooseButtonListener.clicked(null, 0, 0); // fake event
				return true;
			}

			int keyCode = event.getKeyCode();

			if(keyCode == Keys.DEL) {
				backButtonListener.clicked(null, 0, 0); // fake event
				return true;
			} else if(keyCode == Keys.LEFT) {
				parentButtonListener.clicked(null, 0, 0); // fake event
				return true;
			}

			int direction;
			if(keyCode == Keys.UP)
				direction = -1;
			else if(keyCode == Keys.DOWN)
				direction = 1;
			else
				return false;

			int newIndex = contents.getSelectedIndex() + direction;
			newIndex = MathUtils.clamp(newIndex, 0, contents.getItems().size - 1);
			contents.setSelectedIndex(newIndex);
			return true;
		}
	};

	public ListFileChooser(Skin skin, Listener listener) {
		this(skin.get(Style.class), listener);
		setSkin(skin);
	}

	public ListFileChooser(Skin skin, String styleName, Listener listener) {
		this(skin.get(styleName, Style.class), listener);
		setSkin(skin);
	}

	public ListFileChooser(Style style, Listener listener) {
		super(listener);
		this.style = style;
		buildWidgets();
		build();
		refresh();
	}

	/** Override this if you want to adjust all the Widgets. Be careful though! */
	protected void buildWidgets() {
		addListener(keyControlsListener);

		(pathField = new TextField(directory.path(), style.pathFieldStyle)).setTextFieldListener(pathFieldListener);
		contents = new List<>(style.contentsStyle);
		contents.setItems(directory.name());
		contents.addListener(contentsListener);

		(chooseButton = UIUtils.newButton(style.chooseButtonStyle, "select")).addListener(chooseButtonListener);
		(openButton = UIUtils.newButton(style.openButtonStyle, "open")).addListener(openButtonListener);
		(cancelButton = UIUtils.newButton(style.cancelButtonStyle, "cancel")).addListener(cancelButtonListener);
		(backButton = UIUtils.newButton(style.backButtonStyle, "back")).addListener(backButtonListener);
		(parentButton = UIUtils.newButton(style.parentButtonStyle, "up")).addListener(parentButtonListener);

		contentsPane = style.contentsPaneStyle == null ? new ScrollPane(contents) : new ScrollPane(contents, style.contentsPaneStyle);

		setBackground(style.background);
	}

	/** Override this if you want to adjust the {@link Table layout}. Clears this {@link ListFileChooser}'s children and adds {@link #backButton}, {@link #pathField}, {@link #parentButton}, {@link #contentsPane}, {@link #chooseButton}, {@link #cancelButton} and {@link #openButton} if {@link #isDirectoriesChoosable()} is true. */
	@Override
	protected void build() {
		clearChildren();
		Style style = getStyle();
		add(backButton).fill().space(style.space);
		add(pathField).fill().space(style.space);
		add(parentButton).fill().space(style.space).row();
		add(contentsPane).colspan(3).expand().fill().space(style.space).row();
		if(isDirectoriesChoosable())
			add(openButton).fill().space(style.space);
		add(chooseButton).fill().colspan(isDirectoriesChoosable() ? 1 : 2).space(style.space);
		add(cancelButton).fill().space(style.space);
	}

	/** refreshes the {@link #contents} */
	public void refresh() {
		scan(directory);
	}

	/** populates {@link #contents} with the children of {@link #directory} */
	protected void scan(FileHandle dir) {
		try {
			FileHandle[] files = dir.list(handlingFileFilter);
			String[] names = new String[files.length];
			for(int i = 0; i < files.length; i++) {
				String name = files[i].name();
				if(files[i].isDirectory())
					name += File.separator;
				names[i] = name;
			}
			contents.setItems(names);
		} catch(GdxRuntimeException ignore) {
			Gdx.app.error("ListFileChooser", " cannot read " + dir);
		}
	}

	/** @return the file currently selected in {@link #contents} */
	public FileHandle currentlySelected() {
		String selected = contents.getSelected();
		return selected == null ? directory : directory.child(selected);
	}

	/** set {@link #directory} and adds it to {@link #fileHistory}
	 *  @see #setDirectory(FileHandle, boolean) */
	public void setDirectory(FileHandle dir) {
		setDirectory(dir, true);
	}

	/** sets {@link #directory} and updates all things that need to be udpated */
	public void setDirectory(FileHandle dir, boolean addToHistory) {
		FileHandle loc = dir.isDirectory() ? dir : dir.parent();
		if(addToHistory)
			fileHistory.add(Gdx.files.absolute(dir.path()));
		scan(directory = loc);
		pathField.setText(loc.path());
		pathField.setCursorPosition(pathField.getText().length());
	}

	/** @return the {@link #backButton} */
	public Button getBackButton() {
		return backButton;
	}

	/** @param backButton the {@link #backButton} to set */
	public void setBackButton(Button backButton) {
		this.backButton.removeListener(backButtonListener);
		backButton.addListener(backButtonListener);
		getCell(this.backButton).setActor(this.backButton = backButton);
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

	/** @return the {@link #contents} */
	public List<String> getContents() {
		return contents;
	}

	/** @param contents the {@link #contents} to set */
	public void setContents(List<String> contents) {
		this.contents.removeListener(contentsListener);
		contents.addListener(contentsListener);
		contentsPane.setWidget(contents);
	}

	/** @return the {@link #contentsPane} */
	public ScrollPane getContentsPane() {
		return contentsPane;
	}

	/** @param contentsPane the {@link #contentsPane} to set */
	public void setContentsPane(ScrollPane contentsPane) {
		contentsPane.setWidget(contents);
		getCell(this.contentsPane).setActor(this.contentsPane = contentsPane);
	}

	/** @return the {@link #directory} */
	public FileHandle getDirectory() {
		return directory;
	}

	/** @return the {@link #fileHistory} */
	public Array<FileHandle> getFileHistory() {
		return fileHistory;
	}

	/** @param fileHistory the {@link #fileHistory} to set */
	public void setFileHistory(Array<FileHandle> fileHistory) {
		this.fileHistory = fileHistory;
	}

	/** @return the {@link #openButton} */
	public Button getOpenButton() {
		return openButton;
	}

	/** @param openButton the {@link #openButton} to set */
	public void setOpenButton(Button openButton) {
		this.openButton.removeListener(openButtonListener);
		openButton.addListener(openButtonListener);
		getCell(this.openButton).setActor(this.openButton = openButton);
	}

	/** @return the {@link #parentButton} */
	public Button getParentButton() {
		return parentButton;
	}

	/** @param parentButton the {@link #parentButton} to set */
	public void setParentButton(Button parentButton) {
		this.parentButton.removeListener(parentButtonListener);
		parentButton.addListener(parentButtonListener);
		getCell(this.parentButton).setActor(this.parentButton = parentButton);
	}

	/** @return the {@link #pathField} */
	public TextField getPathField() {
		return pathField;
	}

	/** @param pathField the {@link #pathField} to set */
	public void setPathField(TextField pathField) {
		this.pathField.setTextFieldListener(null);
		pathField.setTextFieldListener(pathFieldListener);
		getCell(this.pathField).setActor(this.pathField = pathField);
	}

	/** {@link #build() builds} if necessary */
	@Override
	public void setDirectoriesChoosable(boolean directoriesChoosable) {
		if(isDirectoriesChoosable() != directoriesChoosable) {
			super.setDirectoriesChoosable(directoriesChoosable);
			build();
		}
	}

	/** @return the {@link #style} */
	public Style getStyle() {
		return style;
	}

	/** @param style the {@link #style} to set and use for all widgets */
	public void setStyle(Style style) {
		this.style = style;
		setBackground(style.background);
		backButton.setStyle(style.backButtonStyle);
		cancelButton.setStyle(style.cancelButtonStyle);
		chooseButton.setStyle(style.chooseButtonStyle);
		contents.setStyle(style.contentsStyle);
		contentsPane.setStyle(style.contentsPaneStyle);
		openButton.setStyle(style.openButtonStyle);
		parentButton.setStyle(style.parentButtonStyle);
		pathField.setStyle(style.pathFieldStyle);
	}

	/** defines styles for the widgets of a {@link ListFileChooser}
	 *  @author dermetfan */
	public static class Style implements Serializable {

		/** the style of {@link #pathField} */
		public TextFieldStyle pathFieldStyle;

		/** the style of {@link #contents} */
		public ListStyle contentsStyle;

		/** the styles of the buttons */
		public ButtonStyle chooseButtonStyle, openButtonStyle, cancelButtonStyle, backButtonStyle, parentButtonStyle;

		/** the spacing between the Widgets */
		public float space;

		/** optional */
		public ScrollPaneStyle contentsPaneStyle;

		/** optional */
		public Drawable background;

		public Style() {}

		public Style(Style style) {
			set(style);
		}

		public Style(TextFieldStyle textFieldStyle, ListStyle listStyle, ButtonStyle buttonStyles, Drawable background) {
			this(textFieldStyle, listStyle, buttonStyles, buttonStyles, buttonStyles, buttonStyles, buttonStyles, background);
		}

		public Style(TextFieldStyle pathFieldStyle, ListStyle contentsStyle, ButtonStyle chooseButtonStyle, ButtonStyle openButtonStyle, ButtonStyle cancelButtonStyle, ButtonStyle backButtonStyle, ButtonStyle parentButtonStyle, Drawable background) {
			this.pathFieldStyle = pathFieldStyle;
			this.contentsStyle = contentsStyle;
			this.chooseButtonStyle = chooseButtonStyle;
			this.openButtonStyle = openButtonStyle;
			this.cancelButtonStyle = cancelButtonStyle;
			this.backButtonStyle = backButtonStyle;
			this.parentButtonStyle = parentButtonStyle;
			this.background = background;
		}

		/** @param style the {@link Style} to set this instance to (giving all fields the same value) */
		public void set(Style style) {
			pathFieldStyle = style.pathFieldStyle;
			contentsStyle = style.contentsStyle;
			chooseButtonStyle = style.chooseButtonStyle;
			openButtonStyle = style.openButtonStyle;
			cancelButtonStyle = style.cancelButtonStyle;
			backButtonStyle = style.backButtonStyle;
			parentButtonStyle = style.parentButtonStyle;
			contentsPaneStyle = style.contentsPaneStyle;
			background = style.background;
			space = style.space;
		}

		/** @param style the {@link #backButtonStyle}, {@link #cancelButtonStyle}, {@link #chooseButtonStyle}, {@link #openButtonStyle} and {@link #parentButtonStyle} to set */
		public void setButtonStyles(ButtonStyle style) {
			chooseButtonStyle = openButtonStyle = cancelButtonStyle = backButtonStyle = parentButtonStyle = style;
		}

		@Override
		public void write(Json json) {
			json.writeObjectStart("");
			json.writeFields(this);
			json.writeObjectEnd();
		}

		@Override
		public void read(Json json, JsonValue jsonData) {
			ButtonStyle tmpBS = UIUtils.readButtonStyle("buttonStyles", json, jsonData);
			setButtonStyles(tmpBS);

			tmpBS = UIUtils.readButtonStyle("backButtonStyle", json, jsonData);
			if(tmpBS != null)
				backButtonStyle = tmpBS;

			tmpBS = UIUtils.readButtonStyle("cancelButtonStyle", json, jsonData);
			if(tmpBS != null)
				cancelButtonStyle = tmpBS;

			tmpBS = UIUtils.readButtonStyle("chooseButtonStyle", json, jsonData);
			if(tmpBS != null)
				chooseButtonStyle = tmpBS;

			tmpBS = UIUtils.readButtonStyle("openButtonStyle", json, jsonData);
			if(tmpBS != null)
				openButtonStyle = tmpBS;

			tmpBS = UIUtils.readButtonStyle("parentButtonStyle", json, jsonData);
			if(tmpBS != null)
				parentButtonStyle = tmpBS;

			contentsStyle = json.readValue("contentsStyle", ListStyle.class, jsonData);
			pathFieldStyle = json.readValue("pathFieldStyle", TextFieldStyle.class, jsonData);
			if(jsonData.has("contentsPaneStyle"))
				contentsPaneStyle = json.readValue("contentsPaneStyle", ScrollPaneStyle.class, jsonData);
			if(jsonData.has("space"))
				space = json.readValue("space", float.class, jsonData);
		}

	}

}
