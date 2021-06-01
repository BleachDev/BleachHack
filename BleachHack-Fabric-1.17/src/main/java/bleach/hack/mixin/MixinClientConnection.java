/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.mixin;

import java.util.List;
import java.util.stream.Collectors;

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
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.TextColor;

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
	
	private void handlePlayerList(PlayerListS2CPacket packet) {
		if (packet.getAction() == PlayerListS2CPacket.Action.ADD_PLAYER) {
			List<PlayerListS2CPacket.Entry> newEntries = packet.getEntries().stream()
					.map(e -> {
						if (e.getProfile().getName().equalsIgnoreCase("bleachhack")) { /* :sunglasses: */
							MutableText text1 = new LiteralText("Bleach").styled(s -> s.withColor(TextColor.fromRgb(0xffbf30)));
							MutableText text2 = new LiteralText("Hack ").styled(s -> s.withColor(TextColor.fromRgb(0xffafcc)));
							return new PlayerListS2CPacket.Entry(e.getProfile(), e.getLatency(), e.getGameMode(), text1.append(text2));
						} else if (BleachHack.friendMang.has(e.getProfile().getName())) {
							return new PlayerListS2CPacket.Entry(e.getProfile(), e.getLatency(), e.getGameMode(),
									new LiteralText("\u00a7b" + e.getProfile().getName()));
						} else {
							return e;
						}
					})
					.collect(Collectors.toList());
			
			packet.getEntries().clear();
			packet.getEntries().addAll(newEntries);
		}
	}
}