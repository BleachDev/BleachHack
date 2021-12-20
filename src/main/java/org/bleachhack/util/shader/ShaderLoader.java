package org.bleachhack.util.shader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.function.Function;

import com.google.gson.JsonSyntaxException;

import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.client.render.Shader;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public class ShaderLoader {

	private static final ResourceManager RES_MANG = MinecraftClient.getInstance().getResourceManager();
	private static final TextureManager TEX_MANG = MinecraftClient.getInstance().getTextureManager();

	// Shaders

	public static Shader load(VertexFormat format, Identifier id) throws IOException {
		return new Shader(new OpenResourceManager(RES_MANG), id.toString(), format);
	}

	public static Shader load(VertexFormat format, Identifier id, String input) throws IOException {
		return load(format, id, new FastByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
	}

	public static Shader load(VertexFormat format, Identifier id, InputStream input) throws IOException {
		return new Shader(new OpenResourceManager(RES_MANG, createPair(id, input)), id.getPath(), format);
	}

	// ShaderEffects

	public static ShaderEffect loadEffect(Framebuffer framebuffer, Identifier id) throws JsonSyntaxException, IOException {
		return new ShaderEffect(TEX_MANG, new OpenResourceManager(RES_MANG), framebuffer, id);
	}

	public static ShaderEffect loadEffect(Framebuffer framebuffer, Identifier id, String input) throws JsonSyntaxException, IOException {
		return loadEffect(framebuffer, id, new FastByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
	}

	public static ShaderEffect loadEffect(Framebuffer framebuffer, Identifier id, InputStream input) throws JsonSyntaxException, IOException {
		return new ShaderEffect(TEX_MANG, new OpenResourceManager(RES_MANG, createPair(id, input)), framebuffer, id);
	}
	
	private static Function<Identifier, InputStream> createPair(Identifier id, InputStream input) {
		// Bad practice because were turning a single use InputStream into a function
		// but shaders only use it once to load the main json so whatever
		return id2 -> id2.equals(id) ? input : null;
	}

}
