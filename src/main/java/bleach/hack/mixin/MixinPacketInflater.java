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
package bleach.hack.mixin;

import java.util.List;
import java.util.zip.Inflater;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.AntiChunkBan;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.PacketInflater;

@Mixin(PacketInflater.class)
public class MixinPacketInflater {

	@Shadow
	private Inflater inflater;

	@Inject(at = @At("HEAD"), method = "decode(Lio/netty/channel/ChannelHandlerContext;Lio/netty/buffer/ByteBuf;Ljava/util/List;)V", cancellable = true)
	protected void decode(ChannelHandlerContext channelHandlerContext_1, ByteBuf byteBuf_1, List<Object> list_1, CallbackInfo info) throws Exception {

		if (!ModuleManager.getModule(AntiChunkBan.class).isToggled())
			return;

		info.cancel();

		if (byteBuf_1.readableBytes() != 0) {
			PacketByteBuf packetByteBuf_1 = new PacketByteBuf(byteBuf_1);
			int int_1 = packetByteBuf_1.readVarInt();
			if (int_1 == 0) {
				list_1.add(packetByteBuf_1.readBytes(packetByteBuf_1.readableBytes()));
			} else {
				if (int_1 > 51200000) {
					throw new DecoderException("Badly compressed packet - size of " + (int_1 / 1000000) + "MB is larger than protocol maximum of 50 MB");
				}

				byte[] bytes_1 = new byte[packetByteBuf_1.readableBytes()];
				packetByteBuf_1.readBytes(bytes_1);
				this.inflater.setInput(bytes_1);
				byte[] bytes_2 = new byte[int_1];
				this.inflater.inflate(bytes_2);
				list_1.add(Unpooled.wrappedBuffer(bytes_2));
				this.inflater.reset();
			}

		}
	}

	/* @Override public void decode(ChannelHandlerContext ctx, ByteBuf in,
	 * List<Object> out) throws Exception { // TODO Auto-generated method stub
	 *
	 * } */
}
