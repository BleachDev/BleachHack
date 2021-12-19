package org.bleachhack.util.shader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Stream;

import com.google.gson.JsonSyntaxException;

import it.unimi.dsi.fastutil.io.FastByteArrayInputStream;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.ShaderEffect;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceImpl;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourcePack;
import net.minecraft.util.Identifier;

public class ShaderEffectLoader {

	public static ShaderEffect load(Framebuffer framebuffer, Identifier id, String input) throws JsonSyntaxException, IOException {
		return load(framebuffer, id, new FastByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
	}

	public static ShaderEffect load(Framebuffer framebuffer, Identifier id, InputStream input) throws JsonSyntaxException, IOException {
		TextureManager texMang = MinecraftClient.getInstance().getTextureManager();
		ResourceManager resMang = MinecraftClient.getInstance().getResourceManager();

		return new ShaderEffect(texMang, new UnionResourceManager(resMang, id, new ResourceImpl(id.getNamespace(), id, input, null)), framebuffer, id);
	}

	private static class UnionResourceManager implements ResourceManager {

		private ResourceManager parent;

		private Identifier id;
		private Resource resource;

		public UnionResourceManager(ResourceManager parent, Identifier id, Resource resource) {
			this.parent = parent;
			this.id = id;
			this.resource = resource;
		}

		@Override
		public Resource getResource(Identifier id) throws IOException {
			return id.equals(this.id) ? resource : MinecraftClient.getInstance().getResourceManager().getResource(id);
		}

		@Override
		public Set<String> getAllNamespaces() {
			return parent.getAllNamespaces();
		}

		@Override
		public boolean containsResource(Identifier id) {
			return parent.containsResource(id);
		}

		@Override
		public List<Resource> getAllResources(Identifier id) throws IOException {
			return parent.getAllResources(id);
		}

		@Override
		public Collection<Identifier> findResources(String startingPath, Predicate<String> pathPredicate) {
			return parent.findResources(startingPath, pathPredicate);
		}

		@Override
		public Stream<ResourcePack> streamResourcePacks() {
			return parent.streamResourcePacks();
		}

	}

}
