/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.util.operation;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

public abstract class Operation {

	protected static final MinecraftClient mc = MinecraftClient.getInstance();

	public BlockPos pos;
	public abstract boolean canExecute();
	public abstract boolean execute();
	public abstract boolean verify();
	
	public abstract void render();
}
