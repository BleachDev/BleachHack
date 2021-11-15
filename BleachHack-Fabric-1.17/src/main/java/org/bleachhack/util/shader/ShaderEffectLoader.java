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
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.metadata.ResourceMetadataReader;
import net.minecraft.util.Identifier;

public class ShaderEffectLoader {

	public static ShaderEffect load(Framebuffer framebuffer, String name, InputStream input) throws JsonSyntaxException, IOException {
		Identifier id = new Identifier("bleachhack", name);
		return new ShaderEffect(MinecraftClient.getInstance().getTextureManager(), new OwResourceManager(id, new InputStreamResource(input)), framebuffer, id);
	}

	public static ShaderEffect load(Framebuffer framebuffer, String name, String input) throws JsonSyntaxException, IOException {
		return load(framebuffer, name, new FastByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8)));
	}

	private static class InputStreamResource implements Resource {

		private InputStream input;

		public InputStreamResource(InputStream input) {
			this.input = input;
		}

		@Override
		public void close() throws IOException {
			input.close();
		}

		@Override
		public boolean hasMetadata() {
			return false;
		}

		@Override
		public String getResourcePackName() {
			return null;
		}

		@Override
		public <T> T getMetadata(ResourceMetadataReader<T> metaReader) {
			return null;
		}

		@Override
		public InputStream getInputStream() {
			return input;
		}

		@Override
		public Identifier getId() {
			return null;
		}
	}

	private static class OwResourceManager implements ResourceManager {

		private Identifier id;
		private Resource resource;

		public OwResourceManager(Identifier id, Resource resource) {
			this.id = id;
			this.resource = resource;
		}

		@Override
		public Resource getResource(Identifier id) throws IOException {
			return id.equals(this.id) ? resource : MinecraftClient.getInstance().getResourceManager().getResource(id);
		}

		@Override
		public Set<String> getAllNamespaces() {
			return MinecraftClient.getInstance().getResourceManager().getAllNamespaces();
		}

		@Override
		public boolean containsResource(Identifier id) {
			return MinecraftClient.getInstance().getResourceManager().containsResource(id);
		}

		@Override
		public List<Resource> getAllResources(Identifier id) throws IOException {
			return MinecraftClient.getInstance().getResourceManager().getAllResources(id);
		}

		@Override
		public Collection<Identifier> findResources(String startingPath, Predicate<String> pathPredicate) {
			return MinecraftClient.getInstance().getResourceManager().findResources(startingPath, pathPredicate);
		}

		@Override
		public Stream<ResourcePack> streamResourcePacks() {
			return MinecraftClient.getInstance().getResourceManager().streamResourcePacks();
		}

	}

}
