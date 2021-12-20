package org.bleachhack.util.shader;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceImpl;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourcePack;
import net.minecraft.util.Identifier;

public class OpenResourceManager implements ResourceManager {
	
	private ResourceManager parent;
	private Function<Identifier, InputStream> customResources;

	public OpenResourceManager(ResourceManager parent) {
		this.parent = parent;
	}

	public OpenResourceManager(ResourceManager parent, Function<Identifier, InputStream> customResources) {
		this.parent = parent;
		this.customResources = customResources;
	}

	@Override
	public Resource getResource(Identifier id) throws IOException {
		if (id.getNamespace().equals("minecraft"))
			return parent.getResource(id);

		if (customResources != null) {
			InputStream input = customResources.apply(id);
			if (input != null) {
				return new ResourceImpl(id.getNamespace(), id, input, null);
			}
		}

		// Scuffed resource loader
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

}
