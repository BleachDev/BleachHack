package org.bleachhack.eventbus.registry;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.logging.log4j.Logger;
import org.bleachhack.event.Event;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.eventbus.BleachSubscriber;

/**
 * Very fast subscriber registry that only posts events to an exact event class.
 */
public class ExactSubscriberRegistry extends BleachSubscriberRegistry {

	// <Event Class, Subscribers>
	private final Map<Class<?>, List<BleachSubscriber>> subscribers = new ConcurrentHashMap<>();

	public ExactSubscriberRegistry(String id) {
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
		List<BleachSubscriber> sList = subscribers.get(event.getClass());
		if (sList != null) {
			for (BleachSubscriber s: sList) {
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
