/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.util.collections;

import java.io.Serial;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.tuple.MutablePair;

public class MutablePairList<L, R> extends ArrayList<MutablePair<L, R>> {

	@Serial
	private static final long serialVersionUID = 2L;

	/**
	 * @return List of all keys in this PairList.
	 */
	public List<L> getEntries() {
		List<L> list = new ArrayList<>();
		for (MutablePair<L, R> pair : this) {
			list.add(pair.getLeft());
		}

		return list;
	}

	/**
	 * @return List of all values in this PairList.
	 */
	public List<R> getValues() {
		List<R> list = new ArrayList<>();
		for (MutablePair<L, R> pair : this) {
			list.add(pair.getRight());
		}

		return list;
	}

	/**
	 * lookup for a value for a specified entry.
	 *
	 * @param key The entry key to lookup.
	 * @return The value related to the specified entry, or null if does not exist.
	 */
	public R getValue(L key) {
		for (MutablePair<L, R> pair : this) {
			if (key.equals(pair.getLeft())) {
				return pair.getRight();
			}
		}

		return null;
	}

	/**
	 * Gets the first pair with the specified key.
	 *
	 * @param key The entry key to lookup.
	 * @return The pair related to the specified entry, or null if does not exist.
	 */
	public MutablePair<L, R> getPair(L key) {
		for (MutablePair<L, R> pair : this) {
			if (key.equals(pair.getLeft())) {
				return pair;
			}
		}

		return null;
	}

	/**
	 * @param key The entry key to lookup.
	 * @return Whether any pairs contains this key.
	 */
	public boolean containsKey(L key) {
		if (key == null) {
			return false;
		}

		for (MutablePair<L, R> pair : this) {
			if (key.equals(pair.getLeft())) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Removes all entries with this key.
	 * 
	 * @param key The entry key to remove.
	 * @return Number of entries removed
	 */
	public int removeKey(L key) {
		int size = this.size();
		removeIf(e -> key.equals(e.getKey()));

		return this.size() - size;
	}
	
	/**
	 * Adds a pair to this list.
	 * 
	 * @param key The key of this pair.
	 * @param value The value of this pair.
	 * @return {@code true} (as specified by {@link Collection#add})
	 */
	public boolean add(L key, R value) {
		return add(new MutablePair<>(key, value));
	}
	
	/**
	 * Sorts this list using a comparator on the keys.
	 */
	public void sortByKey(Comparator<L> comparator) {
		sort((e1, e2) -> comparator.compare(e1.getKey(), e2.getKey()));
	}
}
