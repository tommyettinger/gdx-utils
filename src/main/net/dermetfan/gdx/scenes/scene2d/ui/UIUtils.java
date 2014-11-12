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

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton.ImageButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton;
import com.badlogic.gdx.scenes.scene2d.ui.ImageTextButton.ImageTextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.JsonValue;

/** Provides useful methods for {@link com.badlogic.gdx.scenes.scene2d.ui scene2d.ui}.
 *  Extends {@link com.badlogic.gdx.scenes.scene2d.utils.UIUtils UIUtils} to avoid name clashes.
 *  @author dermetfan
 *  @since 0.7.1 */
public class UIUtils extends com.badlogic.gdx.scenes.scene2d.utils.UIUtils {

	/** @param widget the Widget to resize according to its min, pref and max size */
	public static void layoutSize(Widget widget) {
		widget.setSize(MathUtils.clamp(widget.getPrefWidth(), widget.getMinWidth(), widget.getMaxWidth() == 0 ? Float.POSITIVE_INFINITY : widget.getMaxWidth()), MathUtils.clamp(widget.getPrefHeight(), widget.getMinHeight(), widget.getMaxHeight() == 0 ? Float.POSITIVE_INFINITY : widget.getMaxHeight()));
	}

	/** @see #newButton(Button.ButtonStyle, String) */
	public static Button newButton(ButtonStyle style) {
		return newButton(style, "");
	}

	/** creates a {@link Button} according to the given {@link Button.ButtonStyle} instance that may be {@link Button.ButtonStyle}, {@link TextButton.TextButtonStyle}, {@link ImageButton.ImageButtonStyle} or {@link ImageTextButton.ImageTextButtonStyle} */
	public static Button newButton(ButtonStyle style, String textIfAny) {
		if(style instanceof ImageTextButtonStyle)
			return new ImageTextButton(textIfAny, (ImageTextButtonStyle) style);
		if(style instanceof TextButtonStyle)
			return new TextButton(textIfAny, (TextButtonStyle) style);
		if(style instanceof ImageButtonStyle)
			return new ImageButton((ImageButtonStyle) style);
		return new Button(style);
	}

	/** Tries to load a {@link TextButton.TextButtonStyle}, then {@link ImageButton.ImageButtonStyle}, then {@link ImageTextButton.ImageTextButtonStyle} and then {@link Button.ButtonStyle} using {@link Json#readValue(String, Class, JsonValue)} brutally by catching NPEs. Nasty... */
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

}
