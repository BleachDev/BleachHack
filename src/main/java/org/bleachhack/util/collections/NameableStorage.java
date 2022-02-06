package org.bleachhack.util.collections;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Stream;

public class NameableStorage<T> {

	private final Set<T> values;
	private final Map<String, T> nameToValue = new HashMap<>();
	private final Map<Class<? extends T>, T> classToValue = new HashMap<>();

	private final Function<T, String> nameFunction;

	public NameableStorage(Function<T, String> nameFunction) {
		this.nameFunction = nameFunction;
		this.values = new TreeSet<>((v1, v2) -> String.CASE_INSENSITIVE_ORDER.compare(nameFunction.apply(v1), nameFunction.apply(v2)));
	}

	@SuppressWarnings("unchecked")
	public boolean add(T value) {
		if (values.contains(value))
			return false;

		values.add(value);
		nameToValue.put(nameFunction.apply(value), value);
		classToValue.put((Class<? extends T>) value.getClass(), value);
		return true;
	}

	public boolean remove(T value) {
		if (values.contains(value))
			return false;

		values.remove(value);
		nameToValue.remove(nameFunction.apply(value));
		classToValue.remove(value.getClass());
		return true;
	}
	
	public Iterable<T> values() {
		return values;
	}
	
	public Stream<T> stream() {
		return values.stream();
	}
	
	public T get(String name) {
		return nameToValue.get(name);
	}
	
	@SuppressWarnings("unchecked")
	public <U extends T> U get(Class<U> class_) {
		return (U) classToValue.get(class_);
	}
}
