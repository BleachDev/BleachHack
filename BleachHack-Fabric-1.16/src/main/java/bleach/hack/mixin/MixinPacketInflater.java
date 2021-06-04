package bleach.hack.mixin;

import java.util.List;
import java.util.zip.Inflater;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import bleach.hack.module.ModuleManager;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.PacketInflater;

@Mixin(PacketInflater.class)
public class MixinPacketInflater {

	@Shadow private Inflater inflater;

	@Inject(method = "decode", at = @At("HEAD"), cancellable = true)
	protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list, CallbackInfo info) throws Exception {
		if (ModuleManager.getModule("AntiChunkBan").isEnabled()) {
			info.cancel();
			if (byteBuf.readableBytes() != 0) {
				PacketByteBuf packetByteBuf_1 = new PacketByteBuf(byteBuf);
				int i = packetByteBuf_1.readVarInt();

				if (i == 0) {
					list.add(packetByteBuf_1.readBytes(packetByteBuf_1.readableBytes()));
				} else {
					if (i > 51200000) {
						throw new DecoderException("Badly compressed packet - size of " + i / 1000000 + "MB is larger than protocol maximum of 50 MB");
					}

					byte[] bs = new byte[packetByteBuf_1.readableBytes()];
					packetByteBuf_1.readBytes(bs);
					this.inflater.setInput(bs);
					byte[] cs = new byte[i];
					this.inflater.inflate(cs);
					list.add(Unpooled.wrappedBuffer(cs));
					this.inflater.reset();
				}
			}
		}
	}
}
