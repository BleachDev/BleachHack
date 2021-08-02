package bleach.hack.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import org.apache.commons.lang3.tuple.MutablePair;

import net.minecraft.text.BaseText;
import net.minecraft.text.Text;

public class Texts {

	/** Unpacks a text and all its nested siblings to a list of texts. **/
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
}
