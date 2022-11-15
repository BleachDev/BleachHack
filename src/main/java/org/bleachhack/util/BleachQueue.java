/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.util;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.tuple.MutablePair;

public class BleachQueue {

	private static final Map<String, Deque<MutablePair<Runnable, Integer>>> queues = new HashMap<>();

	public static void add(Runnable runnable) {
		add("", runnable);
	}

	public static void add(String id, Runnable runnable) {
		add(id, runnable, 0);
	}

	public static void add(String id, Runnable runnable, int inTicks) {
		if (!queues.containsKey(id)) {
			queues.put(id, new ArrayDeque<>());
		}

		queues.get(id).add(MutablePair.of(runnable, inTicks));
	}

	public static void cancelQueue(String id) {
		queues.remove(id);
	}

	public static void runAllInQueue(String id) {
		if (queues.containsKey(id)) {
			while (!queues.get(id).isEmpty()) {
				queues.get(id).poll().left.run();
			}

			queues.remove(id);
		}
	}

	public static boolean isEmpty(String id) {
		return !queues.containsKey(id);
	}

	public static void nextQueue() {
		queues.entrySet().removeIf(e -> {
			Deque<MutablePair<Runnable, Integer>> deque = e.getValue();

			MutablePair<Runnable, Integer> first = deque.peek();

			if (first.right > 0) {
				first.right--;
			} else {
				first.left.run();
				deque.removeFirst();
			}

			return deque.isEmpty();
		});
	}
}
