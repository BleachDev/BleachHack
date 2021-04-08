/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2019 Bleach.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package bleach.hack.util;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map.Entry;

import org.apache.commons.lang3.tuple.MutablePair;

public class BleachQueue {

	/* cum queue */
	private static HashMap<String, Deque<MutablePair<Runnable, Integer>>> queues = new HashMap<>();

	public static void add(Runnable runnable) {
		add("", runnable);
	}

	public static void add(String id, Runnable runnable) {
		add(id, runnable, 0);
	}

	public static void add(String id, Runnable runnable, int inTicks) {
		if (!queues.containsKey(id)) {
			Deque<MutablePair<Runnable, Integer>> newQueue = new ArrayDeque<>();
			newQueue.add(MutablePair.of(runnable, inTicks));

			queues.put(id, newQueue);
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
		for (Entry<String, Deque<MutablePair<Runnable, Integer>>> e : new HashMap<>(queues).entrySet()) {
			Deque<MutablePair<Runnable, Integer>> deque = e.getValue();

			MutablePair<Runnable, Integer> first = deque.peek();

			if (first.right > 0) {
				first.right--;
				//System.out.println("sdrbubdu " + deque.peek().getRight() + " | " + first.getRight() + " | " + (deque.peek().getRight() > 0));
			} else {
				first.left.run();
				deque.removeFirst();
			}

			if (deque.isEmpty()) {
				queues.remove(e.getKey());
			}
		}
	}
}
