/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.mixin;

import org.bleachhack.BleachHack;
import org.bleachhack.command.Command;
import org.bleachhack.command.CommandManager;
import org.bleachhack.event.events.EventReadPacket;
import org.bleachhack.event.events.EventSendPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;

@Mixin(ClientConnection.class)
public class MixinClientConnection {

	@Shadow private Channel channel;

	@Shadow private void sendImmediately(Packet<?> packet, GenericFutureListener<? extends Future<? super Void>> callback) {}

	@Inject(method = "channelRead0", at = @At("HEAD"), cancellable = true)
	public void channelRead0(ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo callback) {
		if (this.channel.isOpen() && packet != null) {
			EventReadPacket event = new EventReadPacket(packet);
			BleachHack.eventBus.post(event);

			if (event.isCancelled()) {
				callback.cancel();
			} else if (packet instanceof PlayerListS2CPacket) {
				handlePlayerList((PlayerListS2CPacket) packet);
			}
		}
	}

	@Inject(method = "send(Lnet/minecraft/network/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V", at = @At("HEAD"), cancellable = true)
	public void send(Packet<?> packet, GenericFutureListener<? extends Future<? super Void>> packetCallback, CallbackInfo callback) {
		if (packet instanceof ChatMessageC2SPacket) {
			if (!CommandManager.allowNextMsg) {
				ChatMessageC2SPacket pack = (ChatMessageC2SPacket) packet;
				if (pack.getChatMessage().startsWith(Command.getPrefix())) {
					CommandManager.callCommand(pack.getChatMessage().substring(Command.getPrefix().length()));
					callback.cancel();
				}
			}

			CommandManager.allowNextMsg = false;
		}

		EventSendPacket event = new EventSendPacket(packet);
		BleachHack.eventBus.post(event);

		if (event.isCancelled()) {
			callback.cancel();
		}
	}

	private void handlePlayerList(PlayerListS2CPacket packet) {
		if (packet.getAction() == PlayerListS2CPacket.Action.ADD_PLAYER) {
			BleachHack.playerMang.addQueueEntries(packet.getEntries());
		} else if (packet.getAction() == PlayerListS2CPacket.Action.REMOVE_PLAYER) {
			BleachHack.playerMang.removeQueueEntries(packet.getEntries());
		}
	}
}