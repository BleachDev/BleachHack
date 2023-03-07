package org.bleachhack.util.shader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourcePack;
import net.minecraft.util.Identifier;

/**
 * A open resource manager that can get resources from Fabric mods using their namespace
 * or from a URL with the __url__ namespace and an encoded path.
 * 
 * USE WITH CAUTION!
 */
public class OpenResourceManager implements ResourceManager {

	private static Pattern DECODE_PATTERN = Pattern.compile("_([0-9]+)_");

	private ResourceManager parent;

	public OpenResourceManager(ResourceManager parent) {
		this.parent = parent;
	}

	public OpenResourceManager(ResourceManager parent, Function<Identifier, InputStream> customResources) {
		this.parent = parent;
	}

	@Override
	public Optional<Resource> getResource(Identifier id) {
		if ("minecraft".equals(id.getNamespace()))
			return parent.getResource(id);

		if ("__url__".equals(id.getNamespace()))
			return Optional.of(new Resource(MinecraftClient.getInstance().getDefaultResourcePack(), () -> parseURL(id.getPath())));

		// Scuffed resource loader
		Path path = FabricLoader.getInstance().getModContainer(id.getNamespace()).get().findPath("assets/" + id.getNamespace() + "/" + id.getPath()).get();
		return Optional.of(new Resource(MinecraftClient.getInstance().getDefaultResourcePack(), () -> Files.newInputStream(path)));
	}

	@Override
	public Set<String> getAllNamespaces() {
		return parent.getAllNamespaces();
	}

	@Override
	public List<Resource> getAllResources(Identifier id) {
		return parent.getAllResources(id);
	}

	@Override
	public Map<Identifier, Resource> findResources(String startingPath, Predicate<Identifier> allowedPathPredicate) {
		return parent.findResources(startingPath, allowedPathPredicate);
	}

	@Override
	public Stream<ResourcePack> streamResourcePacks() {
		return parent.streamResourcePacks();
	}

	private InputStream parseURL(String path) throws IOException {
		String decoded = DECODE_PATTERN.matcher(path).replaceAll(m -> Character.toString(Integer.parseInt(m.group(1))));
		return new URL(decoded).openStream();
	}

	@Override
	public Map<Identifier, List<Resource>> findAllResources(String startingPath,
			Predicate<Identifier> allowedPathPredicate) {
		return null;
	}

}
