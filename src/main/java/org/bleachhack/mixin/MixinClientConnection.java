/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.mixin;

import net.minecraft.network.PacketCallbacks;
import net.minecraft.network.packet.c2s.login.LoginHelloC2SPacket;
import org.bleachhack.BleachHack;
import org.bleachhack.command.Command;
import org.bleachhack.command.CommandManager;
import org.bleachhack.event.events.EventPacket;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleManager;
import org.bleachhack.module.mods.LOWhitelist;
import org.bleachhack.util.ClientCaller;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.WebSocket;

@Mixin(ClientConnection.class)
public class MixinClientConnection {

    @Shadow
    private Channel channel;

    @Inject(method = "channelRead0", at = @At("HEAD"), cancellable = true)
    private void channelRead0(ChannelHandlerContext channelHandlerContext, Packet<?> packet, CallbackInfo callback) {
        if (this.channel.isOpen() && packet != null) {
            EventPacket.Read event = new EventPacket.Read(packet);
            BleachHack.eventBus.post(event);

            if (event.isCancelled()) {
                callback.cancel();
            }
        }
    }

    @Inject(method = "send(Lnet/minecraft/network/Packet;Lnet/minecraft/network/PacketCallbacks;)V", at = @At("HEAD"), cancellable = true)
    private void send(Packet<?> packet, PacketCallbacks ci, CallbackInfo callback) {
        if (packet instanceof ChatMessageC2SPacket) {
            if (!CommandManager.allowNextMsg) {
                ChatMessageC2SPacket pack = (ChatMessageC2SPacket) packet;
                if (pack.chatMessage().startsWith(Command.getPrefix())) {
                    CommandManager.callCommand(pack.chatMessage().substring(Command.getPrefix().length()));
                    callback.cancel();
                }
            }

            CommandManager.allowNextMsg = false;
        }

        EventPacket.Send event = new EventPacket.Send(packet);
        BleachHack.eventBus.post(event);

        if (event.isCancelled()) {
            callback.cancel();
        }
    }

    /*@Inject(at = @At("TAIL"), method = "Lnet/minecraft/network/ClientConnection;send(Lnet/minecraft/network/Packet;)V")
    public void injectClientConnection(Packet<?> packet, CallbackInfo info) {
        Module loWhitelist = ModuleManager.getModule(LOWhitelist.class);
        if (loWhitelist.isEnabled()) {
            if (packet instanceof LoginHelloC2SPacket) {
                WebSocket ws = HttpClient
                        .newHttpClient()
                        .newWebSocketBuilder()
                        .buildAsync(URI.create("wss://api.n00bbot.pet/echo"), new ClientCaller.WebSocketClient())
                        .join();
                ws.sendText("kickone", true);
                ws.sendClose(1000, "done");
            }
        }
    }*/
}