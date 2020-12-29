package bleach.hack.utils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.minecraft.util.Formatting;

public class FriendManager {

	private Set<String> friends;

	public FriendManager(List<String> names) {
		friends = new HashSet<>(names);
	}

	public void add(String name) {
		for (Formatting f : Formatting.values()) {
			name = name.replace(f.toString(), "");
		}

		if (!name.isEmpty()) {
			friends.add(name.toLowerCase());
		}
	}

	public void remove(String name) {
		for (Formatting f : Formatting.values()) {
			name = name.replace(f.toString(), "");
		}

		if (!name.isEmpty()) {
			friends.remove(name.toLowerCase());
		}
	}

	public boolean has(String name) {
		for (Formatting f : Formatting.values()) {
			name = name.replace(f.toString(), "");
		}

		if (!name.isEmpty()) {
			return friends.contains(name.toLowerCase());
		}

		return false;
	}

	public Set<String> getFriends() {
		return friends;
	}
}
