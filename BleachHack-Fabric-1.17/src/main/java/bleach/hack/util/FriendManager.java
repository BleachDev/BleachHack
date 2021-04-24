/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.util;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;

import net.minecraft.util.Formatting;

public class FriendManager {

	private Set<String> friends;

	public FriendManager(List<String> names) {
		friends = new TreeSet<String>(names);
	}

	public void add(String name) {
		for (Formatting f : Formatting.values()) {
			name = name.replace(f.toString(), "");
		}

		if (!name.isEmpty()) {
			friends.add(name.toLowerCase(Locale.ENGLISH));
		}
	}

	public void remove(String name) {
		for (Formatting f : Formatting.values()) {
			name = name.replace(f.toString(), "");
		}

		if (!name.isEmpty()) {
			friends.remove(name.toLowerCase(Locale.ENGLISH));
		}
	}

	public boolean has(String name) {
		for (Formatting f : Formatting.values()) {
			name = name.replace(f.toString(), "");
		}

		if (!name.isEmpty()) {
			return friends.contains(name.toLowerCase(Locale.ENGLISH));
		}

		return false;
	}

	public Set<String> getFriends() {
		return friends;
	}
}
