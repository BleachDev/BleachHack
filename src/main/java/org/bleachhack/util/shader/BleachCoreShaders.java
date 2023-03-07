package org.bleachhack.util.shader;

import java.io.IOException;

import net.minecraft.client.gl.ShaderProgram;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.Identifier;

public class BleachCoreShaders {
	
	private static final ShaderProgram COLOR_OVERLAY_SHADER;
	
	public static ShaderProgram getColorOverlayShader() {
		return COLOR_OVERLAY_SHADER;
	}
	
	static {
		try {
			COLOR_OVERLAY_SHADER = ShaderLoader.load(VertexFormats.POSITION_COLOR_TEXTURE, new Identifier("bleachhack", "color_overlay"));
		} catch (IOException e) {
			throw new RuntimeException("Failed to initilize BleachHack core shaders", e);
		}
	}

}
