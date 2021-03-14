package bleach.hack.util.shader;

import bleach.hack.mixinterface.IMixinWorldRenderer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.ShaderEffect;

public class OutlineShaderManager {
	
	public static void loadShader(ShaderEffect shader) {
		if (getCurrentShader() != null) {
			getCurrentShader().close();
		}

		((IMixinWorldRenderer) MinecraftClient.getInstance().worldRenderer).setOutlineShader(shader);
		((IMixinWorldRenderer) MinecraftClient.getInstance().worldRenderer).setOutlineFramebuffer(shader.getSecondaryTarget("final"));
	}
	
	public static void loadDefaultShader() {
		MinecraftClient.getInstance().worldRenderer.loadEntityOutlineShader();
	}
	
	public static ShaderEffect getCurrentShader() {
		return ((IMixinWorldRenderer) MinecraftClient.getInstance().worldRenderer).getOutlineShader();
	}
}
