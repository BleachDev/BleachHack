/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.util;

import io.netty.buffer.Unpooled;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;

/* Mojang how */
/* HOW */
public class PlayerInteractEntityC2SUtils {

	public static Entity getEntity(PlayerInteractEntityC2SPacket packet) {
		PacketByteBuf packetBuf = new PacketByteBuf(Unpooled.buffer());
		packet.write(packetBuf);

		return MinecraftClient.getInstance().world.getEntityById(packetBuf.readVarInt());
	}
	
	public static InteractType getInteractType(PlayerInteractEntityC2SPacket packet) {
		PacketByteBuf packetBuf = new PacketByteBuf(Unpooled.buffer());
		packet.write(packetBuf);

		packetBuf.readVarInt();
		return packetBuf.readEnumConstant(InteractType.class);
	}

	public enum InteractType {
		INTERACT,
		ATTACK,
		INTERACT_AT
	}
}
