package bleach.hack.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.world.World;

/**
 * Created by 086 on 11/11/2017.
 */
public class Wrapper {

    private static TextRenderer textRenderer;

    public static MinecraftClient getMinecraft() {
        return MinecraftClient.getInstance();
    }
    public static ClientPlayerEntity getPlayer() {
        return getMinecraft().player;
    }
    public static World getWorld() {
        return getMinecraft().world;
    }

}