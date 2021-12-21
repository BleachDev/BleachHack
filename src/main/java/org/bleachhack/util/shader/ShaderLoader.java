package org.bleachhack.util.shader;

import java.io.IOException;
import com.google.gson.JsonSyntaxException;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.client.render.Shader;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public class ShaderLoader {

	public static Shader load(VertexFormat format, Identifier id) throws IOException {
		ResourceManager resMang = MinecraftClient.getInstance().getResourceManager();
		
		return new Shader(new OpenResourceManager(resMang), id.toString(), format);
	}

	public static ShaderEffect loadEffect(Framebuffer framebuffer, Identifier id) throws JsonSyntaxException, IOException {
		ResourceManager resMang = MinecraftClient.getInstance().getResourceManager();
		TextureManager texMang = MinecraftClient.getInstance().getTextureManager();
	
		return new ShaderEffect(texMang, new OpenResourceManager(resMang), framebuffer, id);
	}

}
