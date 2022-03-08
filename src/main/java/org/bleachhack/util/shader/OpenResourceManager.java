package org.bleachhack.util.shader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceImpl;
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
	public Resource getResource(Identifier id) throws IOException {
		if ("minecraft".equals(id.getNamespace()))
			return parent.getResource(id);

		if ("__url__".equals(id.getNamespace()))
			return new ResourceImpl(id.getNamespace(), id, parseURL(id.getPath()), null);

		// Scuffed resource loader
		@SuppressWarnings("deprecation")
		Path path = FabricLoader.getInstance().getModContainer(id.getNamespace()).get().getPath("assets/" + id.getNamespace() + "/" + id.getPath());
		return new ResourceImpl(id.getNamespace(), id, Files.newInputStream(path), null);
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

	private InputStream parseURL(String path) throws IOException {
		String decoded = DECODE_PATTERN.matcher(path).replaceAll(m -> Character.toString(Integer.parseInt(m.group(1))));
		return new URL(decoded).openStream();
	}

}
