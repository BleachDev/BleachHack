package bleach.hack.eventbus;

import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.logging.log4j.Logger;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import bleach.hack.event.Event;

public class BleachSubscriberRegistry {

	private Multimap<Class<?>, BleachSubscriber> subscribers = HashMultimap.create();
	private final String id;
	private AtomicLong eventsPosted = new AtomicLong();

	public BleachSubscriberRegistry(String id) {
		this.id = id;
	}

	public boolean subscribe(Object object) {
		boolean added = false;
		synchronized (subscribers) {
			for (Method m: object.getClass().getDeclaredMethods()) {
				//if (m.isAnnotationPresent(Subscribe.class)) {
				if (m.isAnnotationPresent(BleachSubscribe.class)) {
					subscribers.put(object.getClass(), new BleachSubscriber(object, m));
					added = true;
				}
			}
		}

		return added;
	}

	public boolean unsubscribe(Object object) {
		synchronized (subscribers) {
			return !subscribers.removeAll(object.getClass()).isEmpty();
		}
	}

	public void post(Event event, Logger logger) {
		synchronized (subscribers) {
			subscribers.values().stream()
			.filter(s -> s.getEventClass().isAssignableFrom(event.getClass()))
			.forEach(s -> {
				try {
					s.callSubscriber(event);
					eventsPosted.incrementAndGet();
				} catch (Throwable t) {
					logger.error("Exception thrown by subscriber method " + s.getSignature() + " when dispatching event: " + s.getEventClass().getName(), t);
				}
			});
		}
	}

	public String getId() {
		return id;
	}

	public long getEventsPosted() {
		return eventsPosted.get();
	}
}
