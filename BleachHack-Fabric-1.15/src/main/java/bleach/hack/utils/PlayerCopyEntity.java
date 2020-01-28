package bleach.hack.utils;

import com.mojang.authlib.GameProfile;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.OtherClientPlayerEntity;

public class PlayerCopyEntity extends OtherClientPlayerEntity {

	public PlayerCopyEntity() {
		this(MinecraftClient.getInstance().player.getGameProfile());
	}
	
	public PlayerCopyEntity(GameProfile profile) {
		this(profile, MinecraftClient.getInstance().player.getX(), 
				MinecraftClient.getInstance().player.getY(), MinecraftClient.getInstance().player.getZ());
	}
	
	public PlayerCopyEntity(GameProfile profile, double x, double y, double z) {
		super(MinecraftClient.getInstance().world, profile);
		setPosition(x, y, z);
	}
	
	public void spawn() {
		MinecraftClient.getInstance().world.addEntity(this.getEntityId(), this);
	}
	
	public void despawn() {
		MinecraftClient.getInstance().world.removeEntity(this.getEntityId());
	}

}
