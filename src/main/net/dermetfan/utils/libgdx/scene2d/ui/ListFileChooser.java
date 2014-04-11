/** Copyright 2014 Robin Stumm (serverkorken@googlemail.com, http://dermetfan.net/)
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

package net.dermetfan.utils.libgdx.scene2d.ui;

import java.io.File;

import net.dermetfan.utils.libgdx.scene2d.Scene2dUtils;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
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
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.Json.Serializable;
import com.badlogic.gdx.utils.JsonValue;
import com.badlogic.gdx.utils.Pools;

/** A {@link TextField} showing the {@link #pathField} of the currently browsed folder with {@link #backButton} and {@link #parentButton} buttons.
 *  There's a {@link #contentsPane scrollable} {@link List} under those showing the contents of the currently browsed folder and {@link #chooseButton} and {@link #cancelButton} buttons.
 *  If {@link #canChooseDirectories directories can be chosen}, a {@link #openButton} button is added so that the user is able to go into folders.
 *  Files can be {@link #fileFilter filtered}.
 *  Use the {@link #listener listener} to get user input.
 *  @author dermetfan */
public class ListFileChooser extends FileChooser {

	/** the style */
	private Style style;

	/** the directories that have been visited previously, for the {@link #backButton} */
	private Array<FileHandle> fileHistory = new Array<FileHandle>();

	/** the current directory */
	private FileHandle directory = Gdx.files.absolute(Gdx.files.getExternalStoragePath());
	{
		fileHistory.add(directory);
	}

	/** if the {@link #chooseButton choose button} should work on folders or go into them */
	private boolean canChooseDirectories;

	/** how long it takes to fade in and out */
	private float fadeDuration = Dialog.fadeDuration;

	/** @see #pathFieldListener */
	private TextField pathField;

	/** shows the {@link File#listFiles() children} of current {@link #directory} */
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
				if(loc.exists())
					if(loc.isDirectory())
						setDirectory(loc);
					else if(!getListener().choose(loc))
						hide();
			}
		}

	};

	/** {@link Listener#choose(FileHandle) chooses} the {@link List#getSelection() selected} file in from the {@link #contents} */
	public final ClickListener chooseButtonListener = new ClickListener() {

		@Override
		public void clicked(InputEvent event, float x, float y) {
			Selection<String> selection = contents.getSelection();
			if(!selection.getMultiple()) {
				FileHandle selected = directory.child(contents.getSelected());
				if(!canChooseDirectories && selected.isDirectory())
					setDirectory(selected);
				else if(!getListener().choose(selected))
					hide();
			} else {
				@SuppressWarnings("unchecked")
				Array<FileHandle> files = Pools.obtain(Array.class);
				files.clear();
				for(String fileName : selection)
					files.add(directory.child(fileName));
				if(!getListener().choose(files))
					hide();
				Pools.free(files);
			}
		}

	};

	/** goes into the currently marked folder */
	public final ClickListener openButtonListener = new ClickListener() {

		@Override
		public void clicked(InputEvent event, float x, float y) {
			FileHandle child = directory.child(contents.getSelected());
			if(child.isDirectory())
				setDirectory(child);
		}

	};

	/** {@link #hide() hides} this {@link ListFileChooser} */
	public final ClickListener cancelButtonListener = new ClickListener() {

		@Override
		public void clicked(InputEvent event, float x, float y) {
			if(!getListener().cancel())
				hide();
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
			openButton.setDisabled(!directory.child(contents.getSelected()).isDirectory());
			chooseButton.setDisabled(canChooseDirectories);
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
		(pathField = new TextField(directory.path(), style.pathFieldStyle)).setTextFieldListener(pathFieldListener);
		contents = new List<String>(style.contentsStyle);
		contents.setItems(new String[] {directory.name()});
		contents.addListener(contentsListener);

		(chooseButton = Scene2dUtils.newButton(style.chooseButtonStyle, "select")).addListener(chooseButtonListener);
		(openButton = Scene2dUtils.newButton(style.openButtonStyle, "open")).addListener(openButtonListener);
		(cancelButton = Scene2dUtils.newButton(style.cancelButtonStyle, "cancel")).addListener(cancelButtonListener);
		(backButton = Scene2dUtils.newButton(style.backButtonStyle, "back")).addListener(backButtonListener);
		(parentButton = Scene2dUtils.newButton(style.parentButtonStyle, "up")).addListener(parentButtonListener);

		contentsPane = style.contentsPaneStyle == null ? new ScrollPane(contents) : new ScrollPane(contents, style.contentsPaneStyle);

		setBackground(style.background);
	}

	/** Override this if you want to adjust the {@link Table layout}. Clears this {@link ListFileChooser} and adds {@link #backButton}, {@link #pathField}, {@link #parentButton}, {@link #contentsPane}, {@link #chooseButton}, {@link #cancelButton} and {@link #openButton} if {@link #canChooseDirectories} is true. */
	@Override
	protected void build() {
		clearChildren();
		Style style = getStyle();
		add(backButton).fill().space(style.space);
		add(pathField).fill().space(style.space);
		add(parentButton).fill().space(style.space).row();
		add(contentsPane).colspan(3).expand().fill().space(style.space).row();
		if(canChooseDirectories)
			add(openButton).fill().space(style.space);
		add(chooseButton).fill().colspan(canChooseDirectories ? 1 : 2).space(style.space);
		add(cancelButton).fill().space(style.space);
	}

	/** {@link Actions#fadeIn(float) fades in} */
	public void show() {
		addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeDuration)));
	}

	/** {@link Actions#fadeOut(float) fades out} and {@link Actions#removeActor() removes} this {@link ListFileChooser} */
	public void hide() {
		addAction(Actions.sequence(Actions.fadeOut(fadeDuration), Actions.removeActor()));
	}

	/** refreshes the {@link #contents} */
	public void refresh() {
		scan(directory);
	}

	/** populates {@link #contents} with the children of {@link #directory} */
	protected void scan(FileHandle dir) {
		File[] files = dir.file().listFiles(handlingFileFilter);
		String[] names = new String[files.length];
		for(int i = 0; i < files.length; i++) {
			String name = files[i].getName();
			if(files[i].isDirectory())
				name += File.separator;
			names[i] = name;
		}
		contents.setItems(names);
	}

	/** set {@link #directory} and adds it to {@link #fileHistory}
	 *  @see #setDirectory(FileHandle, boolean) */
	public void setDirectory(FileHandle dir) {
		setDirectory(dir, true);
	}

	/** sets {@link #directory} and updates all things that need to be udpated */
	public void setDirectory(FileHandle dir, boolean addToHistory) {
		if(dir.file().canRead()) {
			FileHandle loc = dir.isDirectory() ? dir : dir.parent();
			if(addToHistory)
				fileHistory.add(Gdx.files.absolute(dir.path()));
			scan(directory = loc);
			pathField.setText(loc.path());
			pathField.setCursorPosition(pathField.getText().length());
		} else
			Gdx.app.error(ListFileChooser.class.getSimpleName(), " cannot read " + dir);
	}

	/** @return the {@link #backButton} */
	public Button getBackButton() {
		return backButton;
	}

	/** @param backButton the {@link #backButton} to set */
	@SuppressWarnings("unchecked")
	public void setBackButton(Button backButton) {
		backButton.addListener(backButtonListener);
		getCell(this.backButton).setWidget(this.backButton = backButton);
	}

	/** @return the {@link #cancelButton} */
	public Button getCancelButton() {
		return cancelButton;
	}

	/** @param cancelButton the {@link #cancelButton} to set */
	@SuppressWarnings("unchecked")
	public void setCancelButton(Button cancelButton) {
		cancelButton.addListener(cancelButtonListener);
		getCell(this.cancelButton).setWidget(this.cancelButton = cancelButton);
	}

	/** @return the {@link #chooseButton} */
	public Button getChooseButton() {
		return chooseButton;
	}

	/** @param chooseButton the {@link #chooseButton} to set */
	@SuppressWarnings("unchecked")
	public void setChooseButton(Button chooseButton) {
		chooseButton.addListener(chooseButtonListener);
		getCell(this.chooseButton).setWidget(this.chooseButton = chooseButton);
	}

	/** @return the {@link #contents} */
	public List<String> getContents() {
		return contents;
	}

	/** @param contents the {@link #contents} to set */
	@SuppressWarnings("unchecked")
	public void setContents(List<String> contents) {
		getCell(this.contents).setWidget(this.contents = contents);
	}

	/** @return the {@link #contentsPane} */
	public ScrollPane getContentsPane() {
		return contentsPane;
	}

	/** @param contentsPane the {@link #contentsPane} to set */
	@SuppressWarnings("unchecked")
	public void setContentsPane(ScrollPane contentsPane) {
		getCell(this.contentsPane).setWidget(this.contentsPane = contentsPane);
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

	/** @return the {@link #fadeDuration} */
	public float getFadeDuration() {
		return fadeDuration;
	}

	/** @param fadeDuration the {@link #fadeDuration} to set */
	public void setFadeDuration(float fadeDuration) {
		this.fadeDuration = fadeDuration;
	}

	/** @return the {@link #openButton} */
	public Button getOpenButton() {
		return openButton;
	}

	/** @param openButton the {@link #openButton} to set */
	@SuppressWarnings("unchecked")
	public void setOpenButton(Button openButton) {
		openButton.addListener(openButtonListener);
		getCell(this.openButton).setWidget(this.openButton = openButton);
	}

	/** @return the {@link #parentButton} */
	public Button getParentButton() {
		return parentButton;
	}

	/** @param parentButton the {@link #parentButton} to set */
	@SuppressWarnings("unchecked")
	public void setParentButton(Button parentButton) {
		parentButton.addListener(parentButtonListener);
		getCell(this.parentButton).setWidget(this.parentButton = parentButton);
	}

	/** @return the {@link #pathField} */
	public TextField getPathField() {
		return pathField;
	}

	/** @param pathField the {@link #pathField} to set */
	@SuppressWarnings("unchecked")
	public void setPathField(TextField pathField) {
		pathField.setTextFieldListener(pathFieldListener);
		getCell(this.pathField).setWidget(this.pathField = pathField);
	}

	/** @return the {@link #canChooseDirectories} */
	public boolean isCanChooseDirectories() {
		return canChooseDirectories;
	}

	/** @param canChooseDirectories the {@link #canChooseDirectories} to set */
	public void setCanChooseDirectories(boolean canChooseDirectories) {
		if(this.canChooseDirectories != canChooseDirectories) {
			this.canChooseDirectories = canChooseDirectories;
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
			ButtonStyle tmpBS = Scene2dUtils.readButtonStyle("buttonStyles", json, jsonData);
			setButtonStyles(tmpBS);
			tmpBS = null;

			tmpBS = Scene2dUtils.readButtonStyle("backButtonStyle", json, jsonData);
			if(tmpBS != null)
				backButtonStyle = tmpBS;
			tmpBS = null;

			tmpBS = Scene2dUtils.readButtonStyle("cancelButtonStyle", json, jsonData);
			if(tmpBS != null)
				cancelButtonStyle = tmpBS;
			tmpBS = null;

			tmpBS = Scene2dUtils.readButtonStyle("chooseButtonStyle", json, jsonData);
			if(tmpBS != null)
				chooseButtonStyle = tmpBS;
			tmpBS = null;

			tmpBS = Scene2dUtils.readButtonStyle("openButtonStyle", json, jsonData);
			if(tmpBS != null)
				openButtonStyle = tmpBS;
			tmpBS = null;

			tmpBS = Scene2dUtils.readButtonStyle("parentButtonStyle", json, jsonData);
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
