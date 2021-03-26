package bleach.hack.util;

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

	public static enum InteractType {
		INTERACT,
		ATTACK,
		INTERACT_AT
	}
}
