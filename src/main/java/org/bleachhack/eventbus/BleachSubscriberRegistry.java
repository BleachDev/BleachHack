package org.bleachhack.eventbus;

import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.logging.log4j.Logger;
import org.bleachhack.event.Event;

public class BleachSubscriberRegistry {

	// <Event Class, Subscribers>
	private Map<Class<?>, List<BleachSubscriber>> subscribers = new ConcurrentHashMap<>();
	private final String id;
	private AtomicLong eventsPosted = new AtomicLong();

	public BleachSubscriberRegistry(String id) {
		this.id = id;
	}

	public boolean subscribe(Object object) {
		boolean added = false;
		for (Method m: object.getClass().getDeclaredMethods()) {
			//if (m.isAnnotationPresent(Subscribe.class)) {
			if (m.isAnnotationPresent(BleachSubscribe.class) && m.getParameters().length != 0) {
				subscribers.computeIfAbsent(m.getParameters()[0].getType(), k -> new CopyOnWriteArrayList<>()).add(new BleachSubscriber(object, m));
				added = true;
			}
		}

		return added;
	}

	public boolean unsubscribe(Object object) {
		boolean removed = false;
		Iterator<Entry<Class<?>, List<BleachSubscriber>>> iterator = subscribers.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<Class<?>, List<BleachSubscriber>> entry = iterator.next();
			if (entry.getValue().removeIf(s -> object.getClass().equals(s.getTargetClass()))) {
				removed = true;
				if (entry.getValue().isEmpty()) {
					iterator.remove();
				}
			}
		}

		return removed;
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

	public String getId() {
		return id;
	}

	public long getEventsPosted() {
		return eventsPosted.get();
	}
}
