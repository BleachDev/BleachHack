package bleach.hack.utils;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.OtherClientPlayerEntity;

public class FakePlayer extends OtherClientPlayerEntity
{
    public FakePlayer()
    {
        this(MinecraftClient.getInstance().player.getGameProfile());
    }

    public FakePlayer(GameProfile profile)
    {
        this(profile, MinecraftClient.getInstance().player.getX(), MinecraftClient.getInstance().player.getY(), MinecraftClient.getInstance().player.getZ());
    }

    public FakePlayer(GameProfile profile, double x, double y, double z)
    {
        super(MinecraftClient.getInstance().world, profile);
        setPos(x, y, z);
    }

    public void spawn()
    {
        MinecraftClient.getInstance().world.addEntity(this.getEntityId(), this);
    }

    public void despawn()
    {
        MinecraftClient.getInstance().world.removeEntity(this.getEntityId());
    }
}