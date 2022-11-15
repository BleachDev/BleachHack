/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.event.events;

import org.bleachhack.event.Event;

import net.minecraft.client.sound.Sound;
import net.minecraft.client.sound.SoundInstance;

public class EventSoundPlay extends Event {

	public static class Normal extends EventSoundPlay {

		private SoundInstance instance;

		public Normal(SoundInstance si) {
			instance = si;
		}

		public SoundInstance getInstance() {
			return instance;
		}
	}

	public static class Preloaded extends EventSoundPlay {

		private Sound sound;

		public Preloaded(Sound s) {
			sound = s;
		}

		public Sound getSound() {
			return sound;
		}
	}

}
