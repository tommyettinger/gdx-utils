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

package net.dermetfan.utils.libgdx.scene2d;

import java.io.File;
import java.io.FileFilter;

import net.dermetfan.utils.Accessor;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton.ImageTextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Tree.Node;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

/** provides useful methods for scene2d
 *  @author dermetfan */
public class Scene2DUtils {

	/** Some methods return this, so if you get your hands on it make sure to make a copy! This is used internally so it might change unexpectedly. */
	private static Vector2 tmp = new Vector2();

	/** @param actor the actor which position in stage coordinates to return
	 *  @return the position of the given actor in the stage coordinate system */
	public static Vector2 positionInStageCoordinates(Actor actor) {
		if(actor.hasParent())
			actor.localToStageCoordinates(tmp.set(0, 0));
		else
			tmp.set(actor.getX(), actor.getY());
		return tmp;
	}

	/** Adds the given Actor to the given Group at the coordinates relative to the Stage.
	 *  @param actor the Actor to add to the given Group
	 *  @param newParent the Group to add the given Actor to */
	public static void addAtStageCoordinates(Actor actor, Group newParent) {
		tmp.set(positionInStageCoordinates(actor));
		newParent.stageToLocalCoordinates(tmp);
		newParent.addActor(actor);
		actor.setPosition(tmp.x, tmp.y);
	}

	/** @see #getPointerPosition(Stage, int) */
	public static Vector2 getPointerPosition(Stage stage) {
		return getPointerPosition(stage, 0);
	}

	/** @param stage the Stage which coordinate system should be used
	 *  @param pointer the pointer which position to return
	 *  @return the position of the given pointer in stage coordinates */
	public static Vector2 getPointerPosition(Stage stage, int pointer) {
		tmp.set(Gdx.input.getX(pointer), Gdx.input.getY(pointer));
		stage.screenToStageCoordinates(tmp);
		return tmp;
	}

	/** @see #newButton(ButtonStyle, String) */
	public static Button newButton(ButtonStyle style) {
		return newButton(style, "");
	}

	/** creates a {@link Button} according to the given {@link ButtonStyle} instance that may be {@link ButtonStyle}, {@link TextButtonStyle}, {@link ImageButtonStyle} or {@link ImageTextButtonStyle} */
	public static Button newButton(ButtonStyle style, String textIfAny) {
		if(style instanceof TextButtonStyle)
			return new TextButton(textIfAny, (TextButtonStyle) style);
		if(style instanceof ImageButtonStyle)
			return new ImageButton((ImageButtonStyle) style);
		if(style instanceof ImageTextButtonStyle)
			return new ImageTextButton(textIfAny, (ImageTextButtonStyle) style);
		return new Button(style);
	}

	/** Tries to load a {@link TextButtonStyle}, then {@link ImageButtonStyle}, then {@link ImageTextButtonStyle} and then {@link ButtonStyle} using {@link Json#readValue(String, Class, JsonValue)} brutally by catching NPEs. Nasty... */
	public static ButtonStyle readButtonStyle(String name, Json json, JsonValue jsonValue) {
		try {
			return json.readValue(name, TextButtonStyle.class, jsonValue);
		} catch(NullPointerException e) {
			try {
				return json.readValue(name, ImageButtonStyle.class, jsonValue);
			} catch(NullPointerException e1) {
				try {
					return json.readValue(name, ImageTextButtonStyle.class, jsonValue);
				} catch(NullPointerException e2) {
					try {
						return json.readValue(name, ButtonStyle.class, jsonValue);
					} catch(NullPointerException e3) {
						return null;
					}
				}
			}
		}
	}

	/** @see #fileNode(FileHandle, LabelStyle, Accessor) */
	public static Node fileNode(FileHandle file, LabelStyle labelStyle) {
		return fileNode(file, labelStyle, null);
	}

	/** @see #fileNode(FileHandle, FileFilter, LabelStyle, Accessor) */
	public static Node fileNode(FileHandle file, LabelStyle labelStyle, Accessor<Void, Node> nodeConsumer) {
		return fileNode(file, null, labelStyle, nodeConsumer);
	}

	/** @see #fileNode(FileHandle, FileFilter, LabelStyle, Accessor) */
	public static Node fileNode(FileHandle file, FileFilter filter, final LabelStyle labelStyle) {
		return fileNode(file, filter, labelStyle, null);
	}

	/** passes an Accessor that creates labels representing the file name (with slash if it's a folder) using the given label style to {@link #fileNode(FileHandle, FileFilter, Accessor, Accessor)} (labelSupplier)  
	 *  @param labelStyle the {@link LabelStyle} to use for created labels
	 *  @see #fileNode(FileHandle, FileFilter, Accessor, Accessor) */
	public static Node fileNode(FileHandle file, FileFilter filter, final LabelStyle labelStyle, Accessor<Void, Node> nodeConsumer) {
		return fileNode(file, filter, new Accessor<Label, FileHandle>() {

			@Override
			public Label access(FileHandle file) {
				String name = file.name();
				if(file.isDirectory())
					name += File.separator;
				return new Label(name, labelStyle);
			}

		}, nodeConsumer);
	}

	/** @see #fileNode(FileHandle, FileFilter, Accessor, Accessor) */
	public static Node fileNode(FileHandle file, FileFilter filter, Accessor<Label, FileHandle> labelSupplier) {
		return fileNode(file, filter, labelSupplier, null);
	}

	/** creates an anonymous subclass of {@link Node} that recursively adds the children of the given file to it when being {@link Node#setExpanded(boolean) expanded} for the first time  
	 *  @param file the file to put in {@link Node#setObject(Object)}
	 *  @param filter Filters children from being added. May be null to accept all files.
	 *  @param labelSupplier supplies labels to use
	 *  @param nodeConsumer Does something with nodes after they were created. May be null.
	 *  @return the created Node */
	public static Node fileNode(final FileHandle file, final FileFilter filter, final Accessor<Label, FileHandle> labelSupplier, final Accessor<Void, Node> nodeConsumer) {
		Label label = labelSupplier.access(file);
		Node node = null;
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
							for(File child : file.file().listFiles(filter))
								add(fileNode(file.child(child.getName()), filter, labelSupplier, nodeConsumer));
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
				nodeConsumer.access(dummy);
		} else
			node = new Node(label);
		node.setObject(file);

		if(nodeConsumer != null)
			nodeConsumer.access(node);

		return node;
	}

}
