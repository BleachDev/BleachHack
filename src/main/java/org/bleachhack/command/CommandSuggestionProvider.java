/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.command;

import com.google.common.collect.Lists;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

// TODO: Rewrite this entire system because bad
public class CommandSuggestionProvider {

	private List<Suggestion> suggestions = new ArrayList<>();

	public CommandSuggestionProvider() {
	}

	public CommandSuggestionProvider(String syntax) {
		addSuggestion(syntax);
	}

	public void addSuggestion(String syntax) {
		for (String s: syntax.split(" \\| ")) {
			String[] parts = s.split(" (?![^<]*>)"); // Matches all spaces outside of <...> groups
			Suggestion suggestion = new Suggestion(parts[0]);
			List<Suggestion> bottomChilds = Lists.newArrayList(suggestion);
			for (int i = 1; i < parts.length; i++) {
				List<Suggestion> newChilds = parts[i].matches("^\\[.*\\]$")
						? Stream.of(parts[i].substring(1, parts[i].length() - 1).split("/")).map(Suggestion::new).collect(Collectors.toList())
								: Lists.newArrayList(new Suggestion(parts[i]));
				bottomChilds.forEach(p -> newChilds.forEach(p::addChild));
				bottomChilds = newChilds;
			}

			suggestions.add(suggestion);
		}
	}

	public Set<String> getSuggestions(String... typed) {
		return suggestions.stream()
				.map(s -> s.getSuggestions(typed))
				.collect(() -> new TreeSet<>(String.CASE_INSENSITIVE_ORDER), Set::addAll, Set::addAll);
	}

	public List<Suggestion> getSuggestionList() {
		return suggestions;
	}

	public int getSuggestionCount() {
		return suggestions.stream().map(Suggestion::getSuggestionCount).reduce(0, Integer::sum);
	}

	private static class Suggestion {

		private List<Suggestion> children = new ArrayList<>();
		private String suggestion;

		public Suggestion(String suggestion) {
			this.suggestion = suggestion;
		}

		public String getSuggestion() {
			return suggestion;
		}

		public void addChild(Suggestion child) {
			children.add(child);
		}

		private Suggestion getChild(String keyword) {
			return children.stream()
					.filter(s -> s.getSuggestion().equalsIgnoreCase(keyword))
					.findFirst().orElse(null);
		}

		private List<Suggestion> getChildsStartingWith(String keyword) {
			return children.stream()
					.filter(s -> s.getSuggestion().toLowerCase(Locale.ENGLISH).startsWith(keyword.toLowerCase(Locale.ENGLISH))
							|| s.getSuggestion().matches("^<.*>$"))
					.collect(Collectors.toList());
		}

		public List<String> getSuggestions(String... typed) {
			if (typed.length <= 1) {
				if (typed.length == 1 && !suggestion.matches("^<.*>$") && !suggestion.startsWith(typed[0])) {
					return new ArrayList<>();
				}

				return List.of(suggestion);
			}

			if (!suggestion.equals(typed[0])) {
				return new ArrayList<>();
			}

			if (typed.length == 2) {
				return getChildsStartingWith(typed[1]).stream()
						.map(c -> c.getSuggestions(ArrayUtils.subarray(typed, 1, typed.length)))
						.collect(ArrayList::new, List::addAll, List::addAll);
			}

			Suggestion child = getChild(typed[1]);
			if (child != null) {
				return child.getSuggestions(ArrayUtils.subarray(typed, 1, typed.length));
			}

			return new ArrayList<>();
		}

		public String toString() {
			return suggestion + (!children.isEmpty() ? " > " + (children.size() == 1 ? children.get(0) : children) : "");
		}

		public int getSuggestionCount() {
			return 1 + children.stream().map(Suggestion::getSuggestionCount).reduce(0, Integer::sum);
		}
	}
}
