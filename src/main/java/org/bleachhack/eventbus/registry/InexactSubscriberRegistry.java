package org.bleachhack.eventbus.registry;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.logging.log4j.Logger;
import org.bleachhack.event.Event;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.eventbus.BleachSubscriber;

/**
 * Slower subscriber registry that allows events to the posted to children of a event class.
 */
public class InexactSubscriberRegistry extends BleachSubscriberRegistry {

	// <Event Class, Subscribers>
	private final Map<Class<?>, List<BleachSubscriber>> subscribers = new ConcurrentHashMap<>();

	public InexactSubscriberRegistry(String id) {
		super(id);
	}

	public boolean subscribe(Object object) {
		boolean added = false;
		for (Method m: object.getClass().getDeclaredMethods()) {
			if (m.isAnnotationPresent(BleachSubscribe.class) && m.getParameters().length != 0) {
				subscribers.computeIfAbsent(m.getParameters()[0].getType(), k -> new CopyOnWriteArrayList<>()).add(new BleachSubscriber(object, m));
				added = true;
			}
		}

		return added;
	}

	public boolean unsubscribe(Object object) {
		boolean[] removed = new boolean[1];
		subscribers.values().removeIf(v -> {
			removed[0] |= v.removeIf(s -> object.getClass().equals(s.getTargetClass()));
			return v.isEmpty();
		});
		
		return removed[0];
	}

	public void post(Event event, Logger logger) {
		for (Entry<Class<?>, List<BleachSubscriber>> entry: subscribers.entrySet()) {
			if (entry.getKey().isAssignableFrom(event.getClass())) {
				for (BleachSubscriber s: entry.getValue()) {
					try {
						eventsPosted.incrementAndGet();
						s.callSubscriber(event);
					} catch (Throwable t) {
						logger.error("Exception thrown by subscriber method " + s.getSignature() + " when dispatching event: " + s.getEventClass().getName(), t);
					}
				}
			}
		}
	}
}
