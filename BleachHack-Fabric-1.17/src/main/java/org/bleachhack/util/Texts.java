package org.bleachhack.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.tuple.MutablePair;

import net.minecraft.text.BaseText;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

public class Texts {

	/** Unpacks a text and all its nested siblings into a list of texts. **/
	public static List<Text> unpack(Text text) {
		Stack<MutablePair<Text, Integer>> nodes = new Stack<>();
		List<Text> texts = new ArrayList<>();

		nodes.push(MutablePair.of(text, 0));
		texts.add(copy(text, true, false));
		while (!nodes.isEmpty()) {
			MutablePair<Text, Integer> pair = nodes.peek();
			if (pair.getLeft().getSiblings().size() > pair.getRight()) {
				Text sibling = pair.getLeft().getSiblings().get(pair.getRight());
				nodes.push(MutablePair.of(sibling, 0));
				texts.add(copy(sibling, true, false));
				pair.setRight(pair.getRight() + 1);
			} else {
				nodes.pop();
			}
		}

		return texts;
	}

	/** Copies a text with optional style and siblings. **/
	public static Text copy(Text text, boolean style, boolean siblings) {
		BaseText newText = (BaseText) text.copy();
		if (style)
			newText.setStyle(text.getStyle());

		if (siblings)
			newText.getSiblings().addAll(text.getSiblings());

		return newText;
	}

	/** Replaces every string in this text that matches the pattern. **/
	public static Text replaceAll(Text text, String regex, String replacement) {
		return replaceAll(text, Pattern.compile(regex), replacement);
	}

	/** Replaces every string in this text that matches the pattern. **/
	public static Text replaceAll(Text text, Pattern pattern, String replacement) {
		MutableText newText = new LiteralText("");
		for (Text t: unpack(text)) {
			newText.append(pattern.matcher(t.getString()).replaceAll(replacement));
		}

		return newText;
	}

	/** Replaces every string in this text that matches the pattern with a text. **/
	public static Text replaceAll(Text text, String regex, Text replacement) {
		return replaceAll(text, Pattern.compile(regex), replacement);
	}
	
	/** Replaces every string in this text that matches the pattern with a text. **/
	public static Text replaceAll(Text text, Pattern pattern, Text replacement) {
		return replaceAll(text, pattern, (string, style) -> replacement);
	}

	/** Replaces every string in this text that matches the pattern with a text from the provider. **/
	public static Text replaceAll(Text text, Pattern pattern, BiFunction<String, Style, Text> replacement) {
		if (pattern.pattern().isEmpty())
			return text;

		MutableText newText = new LiteralText("");
		for (Text t: unpack(text)) {
			String string = t.getString();

			Map<Integer, Integer> positions = new HashMap<>();
			Matcher mat = pattern.matcher(string);
			while (mat.find()) {
				positions.put(mat.start(), mat.end());
			}

			String curString = "";
			for (int i = 0; i < string.length(); i++) {
				if (positions.containsKey(i)) {
					if (!curString.isEmpty()) {
						newText = newText.append(new LiteralText(curString).setStyle(t.getStyle()));
						curString = "";
					}

					newText.append(replacement.apply(string.substring(i, positions.get(i)), t.getStyle()));
					i = positions.get(i) - 1;
				} else {
					curString += string.charAt(i);
				}
			}

			if (!curString.isEmpty())
				newText.append(new LiteralText(curString).setStyle(t.getStyle()));
		}

		return newText;
	}

	/** Does an operation to each word in a text. **/
	public static Text forEachWord(Text text, BiFunction<String, Style, Text> operator) {
		MutableText newFullText = new LiteralText("");

		for (Text t: unpack(text)) {
			MutableText newText = new LiteralText("");
			String curString = "";

			String[] split = t.getString().split(" ", -1);
			for (int i = 0; i < split.length; i++) {
				Text word = operator.apply(split[i], t.getStyle());
				if (word != null) {
					if (!curString.isEmpty()) {
						newText.append(new LiteralText(curString).setStyle(t.getStyle()));
						curString = "";
					}

					newText.append(word);
				} else {
					curString += split[i];
				}

				if (i != split.length - 1)
					curString += " ";
			}

			if (!curString.isEmpty())
				newText.append(new LiteralText(curString).setStyle(t.getStyle()));

			newFullText.append(newText);
		}

		return newFullText;
	}
}
