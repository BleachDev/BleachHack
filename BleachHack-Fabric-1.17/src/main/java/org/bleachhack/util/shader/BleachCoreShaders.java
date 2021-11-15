package org.bleachhack.util.shader;

import java.io.IOException;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Shader;
import net.minecraft.client.render.VertexFormats;

public class BleachCoreShaders {
	
	private static Shader COLOR_OVERLAY_SHADER;
	
	public static Shader getColorOverlayShader() {
		return COLOR_OVERLAY_SHADER;
	}
	
	static {
		try {
			COLOR_OVERLAY_SHADER = new Shader(MinecraftClient.getInstance().getResourceManager(), "bleachhack:color_overlay", VertexFormats.POSITION_COLOR_TEXTURE);
		} catch (IOException e) {
			throw new RuntimeException("Failed to initilize BleachHack core shaders");
		}
	}

}
