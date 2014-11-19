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

package net.dermetfan.utils;

/** utility methods for operations on Strings
 *  @author dermetfan
 *  @since 0.6.0 */
public class StringUtils {

	/** @param string the String to convert to a valid java identifier, as far as possible */
	public static String toJavaIdentifier(String string) {
		if(string.length() == 0)
			return string;
		StringBuilder result = new StringBuilder(string.length());
		if(isJavaIdentifierStart(string.charAt(0)))
			result.append(string.charAt(0));
		boolean nextUpperCase = false;
		for(int i = 1, n = string.length(); i < n; i++) {
			char c = string.charAt(i);
			if(isJavaIdentifierPart(c)) {
				if(nextUpperCase) {
					c = Character.toUpperCase(c);
					nextUpperCase = false;
				}
				result.append(c);
			} else
				nextUpperCase = true;
		}
		return result.toString();
	}

	/** for GWT compatibility
	 *  @see Character#isJavaIdentifierStart(char) */
	public static boolean isJavaIdentifierStart(char c) {
		return isJavaIdentifierStart((int) c);
	}

	/** for GWT compatibility
	 *  @see Character#isJavaIdentifierStart(int) */
	public static boolean isJavaIdentifierStart(int codePoint) {
		return codePoint >= 'a' && codePoint <= 'z' || codePoint >= 'A' && codePoint <= 'Z' || codePoint == '$' || codePoint == 'µ' || codePoint >= 'À' && codePoint <= 'Ö' || codePoint >= 'Ø' && codePoint <= 'ö' || codePoint >= 'ø' && codePoint <= 'ÿ';
	}

	/** for GWT compatibility
	 *  @see Character#isJavaIdentifierPart(char) */
	public static boolean isJavaIdentifierPart(char c) {
		return isJavaIdentifierPart((int) c);
	}

	/** for GWT compatibility
	 *  @see Character#isJavaIdentifierPart(int) */
	public static boolean isJavaIdentifierPart(int codePoint) {
		return isJavaIdentifierStart(codePoint) || codePoint >= '0' && codePoint <= '9';
	}

	/** @param index the index of the character to replace
	 *  @param replacement the replacement for the character at the given index
	 *  @param string the String in which to replace
	 *  @return a representation of the given String with the contents replaced */
	public static String replace(int index, String replacement, String string) {
		return string.substring(0, index).concat(replacement).concat(string.substring(index + 1));
	}

	/** @see #replace(int, String, String) */
	public static String replace(int index, char replacement, String string) {
		return (string.substring(0, index) + replacement).concat(string.substring(index + 1));
	}

	/** @param beginIndex the index of the first character to remove
	 *  @param endIndex the index at which to stop the removal
	 *  @param string the String from which to remove
	 *  @return a representation of the given String with contents removed */
	public static String remove(int beginIndex, int endIndex, String string) {
		return string.substring(0, beginIndex).concat(string.substring(endIndex));
	}

	/** @see #remove(int, int, String) */
	public static String remove(int index, String string) {
		return remove(index, index + 1, string);
	}

}
