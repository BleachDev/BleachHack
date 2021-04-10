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
package bleach.hack.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import bleach.hack.BleachHack;
import bleach.hack.command.Command;
import bleach.hack.command.CommandManager;
import bleach.hack.event.events.EventReadPacket;
import bleach.hack.event.events.EventSendPacket;
import bleach.hack.module.ModuleManager;
import bleach.hack.util.BleachLogger;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketEncoderException;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;

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
			}
		}
	}

	@Inject(method = "send(Lnet/minecraft/network/Packet;Lio/netty/util/concurrent/GenericFutureListener;)V", at = @At("HEAD"), cancellable = true)
	public void send(Packet<?> packet, GenericFutureListener<? extends Future<? super Void>> packetCallback, CallbackInfo callback) {
		if (packet instanceof ChatMessageC2SPacket) {
			if (!CommandManager.allowNextMsg) {
				ChatMessageC2SPacket pack = (ChatMessageC2SPacket) packet;
				if (pack.getChatMessage().startsWith(Command.PREFIX)) {
					CommandManager.callCommand(pack.getChatMessage().substring(Command.PREFIX.length()));
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

	// Packet kick blocc
	@Inject(method = "exceptionCaught(Lio/netty/channel/ChannelHandlerContext;Ljava/lang/Throwable;)V", at = @At("HEAD"), cancellable = true)
	public void exceptionCaught(ChannelHandlerContext channelHandlerContext, Throwable throwable, CallbackInfo callback) {
		if (ModuleManager.getModule("AntiChunkBan").isEnabled()) {
			if (!(throwable instanceof PacketEncoderException)) {
				BleachLogger.warningMessage("Canceled Defect Packet: " + throwable);
				callback.cancel();
			}
		}
	}
}
