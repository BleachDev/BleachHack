/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/bleachhack-1.14/).
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
package bleach.hack.utils;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map.Entry;

public class BleachQueue {

    private static final HashMap<String, Deque<Runnable>> specialQueues = new HashMap<>();

    private static final Deque<Runnable> queue = new ArrayDeque<>();

    public static void add(Runnable runnable) {
        queue.add(runnable);
    }

    public static void add(String id, Runnable runnable) {
        if (!specialQueues.containsKey(id)) {
            specialQueues.put(id, new ArrayDeque<>());
        }

        specialQueues.get(id).add(runnable);
    }

    public static void cancelQueue(String id) {
        specialQueues.remove(id);
    }

    public static void nextQueue() {
        if (!queue.isEmpty()) {
            if (queue.getFirst() != null) queue.poll().run();
            else queue.poll();
        }

        for (Entry<String, Deque<Runnable>> e : new HashMap<>(specialQueues).entrySet()) {
            Deque<Runnable> deque = specialQueues.get(e.getKey());

            if (deque.getFirst() != null) deque.poll().run();
            else deque.poll();

            if (deque.isEmpty()) {
                specialQueues.remove(e.getKey());
            }
        }
    }
}
