/** Copyright 2013 Robin Stumm (serverkorken@googlemail.com, http://dermetfan.net/)
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
import java.io.FileFilter;

import com.badlogic.gdx.Files.FileType;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton.ImageTextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.List.ListStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane.ScrollPaneStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldListener;
import com.badlogic.gdx.scenes.scene2d.ui.TextField.TextFieldStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Window;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.Drawable;
import com.badlogic.gdx.utils.Array;

/** A {@link TextField} showing the {@link #pathField} of the currently browsed folder with {@link #backButton} and {@link #parentButton} buttons.
 *  There's a {@link #contentsPane scrollable} {@link List} under those showing the contents of the currently browsed folder and {@link #chooseButton} and {@link #cancelButton} buttons.
 *  If {@link #canChooseDirectories directories can be chosen}, a {@link #openButton} button is added so that the user is able to go into folders.
 *  Files can be {@link #fileFilter filtered}.
 *  Use the {@link #fileChooserListener listener} to get user input.
 *  @author dermetfan */
public class FileChooser extends Window {

	/** called when a file is {@link #chooseButton chosen} or the {@link #cancelButton cancel button} was clicked */
	private FileChooserListener fileChooserListener;

	/** the directories that have been visited previously, for the {@link #backButton} */
	private Array<FileHandle> fileHistory = new Array<FileHandle>();

	/** the current directory */
	private FileHandle directory = Gdx.files.absolute(Gdx.files.getExternalStoragePath());
	{
		fileHistory.add(directory);
	}

	/** does not {@link FileFilter#accept(File) accept} hidden files if {@link #showHidden} is false */
	public final FileFilter defaultFileFilter = new FileFilter() {

		@Override
		public boolean accept(File pathname) {
			return showHidden || !pathname.isHidden();
		}

	};

	/** used to filter the files for {@link #contents} */
	private FileFilter fileFilter = defaultFileFilter;

	/** if the {@link #chooseButton choose button} should work on folders or go into them */
	private boolean canChooseDirectories;

	/** if hidden folders and files should be shown */
	private boolean showHidden;

	/** how long it takes to fade in and out */
	private float fadeDuration = Dialog.fadeDuration;

	/** @see #pathFieldListener */
	private TextField pathField;

	/** shows the {@link File#listFiles() children} of current {@link #directory} */
	private List contents;

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
					else if(!fileChooserListener.chosen(loc))
						hide();
			}
		}

	};

	/** {@link FileChooserListener#chosen(FileHandle) chooses} the {@link List#getSelection() selected} file in from the {@link #contents} */
	public final ClickListener chooseButtonListener = new ClickListener() {

		@Override
		public void clicked(InputEvent event, float x, float y) {
			FileHandle selection = directory.child(contents.getSelection());
			if(!canChooseDirectories && selection.isDirectory())
				setDirectory(selection);
			else if(!fileChooserListener.chosen(selection))
				hide();
		}

	};

	/** goes into the currently marked folder */
	public final ClickListener openButtonListener = new ClickListener() {

		@Override
		public void clicked(InputEvent event, float x, float y) {
			setDirectory(directory.child(contents.getSelection()));
		}

	};

	/** {@link #hide() hides} this {@link FileChooser} */
	public final ClickListener cancelButtonListener = new ClickListener() {

		@Override
		public void clicked(InputEvent event, float x, float y) {
			if(!fileChooserListener.canceled())
				hide();
		}

	};

	/** goes back to the {@link #fileHistory previous} {@link #directory} */
	public final ClickListener backButtonListener = new ClickListener() {

		@Override
		public void clicked(InputEvent event, float x, float y) {
			if(fileHistory.size > 1) {
				fileHistory.removeIndex(fileHistory.size - 1);
				scan(directory = fileHistory.peek());
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

	/** @see Window#Window(String, Skin, String) */
	public FileChooser(FileChooserListener fileChooserListener, String title, Skin skin, String styleName) {
		this(fileChooserListener, title, skin.get(styleName, FileChooserStyle.class));
		setSkin(skin);
	}

	/** @see Window#Window(String, Skin) */
	public FileChooser(FileChooserListener fileChooserListener, String title, Skin skin) {
		this(fileChooserListener, title, skin.get(FileChooserStyle.class));
		setSkin(skin);
	}

	/** @see Window#Window(String, WindowStyle) */
	public FileChooser(FileChooserListener fileChooserListener, String title, FileChooserStyle style) {
		super(title, style);
		this.fileChooserListener = fileChooserListener;

		pathField = new TextField(directory.path(), style.pathFieldStyle);
		pathField.setTextFieldListener(pathFieldListener);

		contents = new List(new String[] {directory.name()}, style.contentsStyle);
		refresh();

		chooseButton = createButton("select", style.chooseButtonStyle);
		openButton = createButton("open", style.openButtonStyle);
		cancelButton = createButton("cancel", style.cancelButtonStyle);
		backButton = createButton("back", style.backButtonStyle);
		parentButton = createButton("up", style.parentButtonStyle);

		chooseButton.addListener(chooseButtonListener);
		openButton.addListener(openButtonListener);
		cancelButton.addListener(cancelButtonListener);
		backButton.addListener(backButtonListener);
		parentButton.addListener(parentButtonListener);

		contentsPane = style.scrollPaneStyle == null ? new ScrollPane(contents) : new ScrollPane(contents, style.scrollPaneStyle);

		build();

		show();
	}

	/** Override this if you want to adjust the {@link Table layout}. Clears this {@link FileChooser} and adds {@link #backButton}, {@link #pathField}, {@link #parentButton}, {@link #contentsPane}, {@link #chooseButton}, {@link #cancelButton} and {@link #openButton} if {@link #canChooseDirectories} is true. */
	protected void build() {
		clearChildren();
		FileChooserStyle style = (FileChooserStyle) getStyle();
		add(backButton).fill().space(style.space);
		add(pathField).fill().space(style.space);
		add(parentButton).fill().space(style.space).row();
		add(contentsPane).colspan(3).expand().fill().space(style.space).row();
		if(canChooseDirectories)
			add(openButton).fill().space(style.space);
		add(chooseButton).fill().colspan(canChooseDirectories ? 1 : 2).space(style.space);
		add(cancelButton).fill().space(style.space);
	}

	/** creates a {@link Button} according to the given {@link ButtonStyle} instance that may be a subclass */
	protected Button createButton(String textIfAny, ButtonStyle style) {
		Button button;
		if(style instanceof TextButtonStyle)
			button = new TextButton(textIfAny, (TextButtonStyle) style);
		else if(style instanceof ImageButtonStyle)
			button = new ImageButton((ImageButtonStyle) style);
		else if(style instanceof ImageTextButtonStyle)
			button = new ImageTextButton(textIfAny, (ImageTextButtonStyle) style);
		else
			button = new Button(style);
		return button;
	}

	/** {@link Actions#fadeIn(float) fades in} */
	public void show() {
		addAction(Actions.sequence(Actions.alpha(0), Actions.fadeIn(fadeDuration)));
	}

	/** {@link Actions#fadeOut(float) fades out} and {@link Actions#removeActor() removes} this {@link FileChooser} */
	public void hide() {
		addAction(Actions.sequence(Actions.fadeOut(fadeDuration), Actions.removeActor()));
	}

	/** refreshes the {@link #contents} */
	public void refresh() {
		scan(directory);
	}

	/** populates {@link #contents} with the children of {@link #directory} */
	protected void scan(FileHandle dir) {
		File[] files = dir.file().listFiles(fileFilter);
		String[] names = new String[files.length];
		for(int i = 0; i < names.length; i++)
			names[i] = files[i].getName();
		contents.setItems(names);
	}

	/** sets {@link #directory} and updates all things that need to be udpated */
	public void setDirectory(FileHandle dir) {
		if(dir.file().canRead()) {
			FileHandle loc = dir.isDirectory() ? dir : dir.parent();
			scan(directory = loc);
			pathField.setText(loc.path());
			pathField.setCursorPosition(pathField.getText().length());
			fileHistory.add(Gdx.files.absolute(dir.path()));
		} else
			Gdx.app.log(FileChooser.class.getSimpleName(), "cannot read " + dir);
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
	public List getContents() {
		return contents;
	}

	/** @param contents the {@link #contents} to set */
	@SuppressWarnings("unchecked")
	public void setContents(List contents) {
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

	/** @return the {@link #fileFilter} */
	public FileFilter getFileFilter() {
		return fileFilter;
	}

	/** @param fileFilter the {@link #fileFilter} to set */
	public void setFileFilter(FileFilter fileFilter) {
		this.fileFilter = fileFilter != null ? fileFilter : defaultFileFilter;
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
		getCell(parentButton).setWidget(this.parentButton = parentButton);
	}

	/** @return the {@link #showHidden} */
	public boolean isShowHidden() {
		return showHidden;
	}

	/** @param showHidden the {@link #showHidden} to set */
	public void setShowHidden(boolean showHidden) {
		this.showHidden = showHidden;
	}

	/** @return the {@link #pathField} */
	public TextField getPathField() {
		return pathField;
	}

	/** @param pathField the {@link #pathField} to set */
	public void setPathField(TextField pathField) {
		pathField.setTextFieldListener(pathFieldListener);
	}

	/** @return the {@link #fileChooserListener} */
	public FileChooserListener getFileChooserListener() {
		return fileChooserListener;
	}

	/** @param fileChooserListener the {@link #fileChooserListener} to set */
	public void setFileChooserListener(FileChooserListener fileChooserListener) {
		if(fileChooserListener == null)
			throw new IllegalArgumentException("fileChooserListener must not be null");
		this.fileChooserListener = fileChooserListener;
	}

	/** @return the {@link #canChooseDirectories} */
	public boolean isCanChooseDirectories() {
		return canChooseDirectories;
	}

	/** @param cannotChooseDirectories the {@link #canChooseDirectories} to set */
	public void setCanChooseDirectories(boolean canChooseDirectories) {
		if(this.canChooseDirectories != canChooseDirectories) {
			this.canChooseDirectories = canChooseDirectories;
			build();
		}
	}

	/** called by a {@link FileChooser}
	 *  @author dermetfan */
	public static interface FileChooserListener {

		/** @return if the selection should be rejected */
		public boolean chosen(FileHandle file);

		/** @return if canceling should be rejected */
		public boolean canceled();

	}

	/** Defines styles for the Widgets
	 *  @author dermetfan */
	public static class FileChooserStyle extends WindowStyle {

		/** the style of {@link #pathField} */
		public TextFieldStyle pathFieldStyle;

		/** the style of {@link #contents} */
		public ListStyle contentsStyle;

		/** the styles of the buttons */
		public ButtonStyle chooseButtonStyle, openButtonStyle, cancelButtonStyle, backButtonStyle, parentButtonStyle;

		/** the spacing between the Widgets */
		public float space;

		/** optional */
		public ScrollPaneStyle scrollPaneStyle;

		public FileChooserStyle() {
			super();
		}

		public FileChooserStyle(BitmapFont titleFont, Color titleFontColor, Drawable background) {
			super(titleFont, titleFontColor, background);
		}

		public FileChooserStyle(WindowStyle style) {
			super(style);
		}

		public FileChooserStyle(WindowStyle style, ButtonStyle buttonStyles) {
			super(style);
			chooseButtonStyle = openButtonStyle = cancelButtonStyle = backButtonStyle = parentButtonStyle = buttonStyles;
		}

		public FileChooserStyle(FileChooserStyle style) {
			super(style);
			pathFieldStyle = style.pathFieldStyle;
			contentsStyle = style.contentsStyle;
			chooseButtonStyle = style.chooseButtonStyle;
			openButtonStyle = style.openButtonStyle;
			cancelButtonStyle = style.cancelButtonStyle;
			backButtonStyle = style.backButtonStyle;
			parentButtonStyle = style.parentButtonStyle;
		}

		public FileChooserStyle(WindowStyle windowStyle, TextFieldStyle textFieldStyle, ListStyle listStyle, ButtonStyle buttonStyles) {
			this(textFieldStyle, listStyle, buttonStyles, buttonStyles, buttonStyles, buttonStyles, buttonStyles, windowStyle.titleFont, windowStyle.titleFontColor, windowStyle.background);
		}

		public FileChooserStyle(TextFieldStyle textFieldStyle, ListStyle listStyle, ButtonStyle buttonStyles, BitmapFont titleFont, Color titleFontColor, Drawable background) {
			this(textFieldStyle, listStyle, buttonStyles, buttonStyles, buttonStyles, buttonStyles, buttonStyles, titleFont, titleFontColor, background);
		}

		public FileChooserStyle(TextFieldStyle textFieldStyle, ListStyle listStyle, ButtonStyle chooseButtonStyle, ButtonStyle openButtonStyle, ButtonStyle cancelButtonStyle, ButtonStyle backButtonStyle, ButtonStyle parentButtonStyle, BitmapFont titleFont, Color titleFontColor, Drawable background) {
			super(titleFont, titleFontColor, background);
			pathFieldStyle = textFieldStyle;
			contentsStyle = listStyle;
			this.chooseButtonStyle = chooseButtonStyle;
			this.openButtonStyle = openButtonStyle;
			this.cancelButtonStyle = cancelButtonStyle;
			this.backButtonStyle = backButtonStyle;
			this.parentButtonStyle = parentButtonStyle;
		}

	}

}
