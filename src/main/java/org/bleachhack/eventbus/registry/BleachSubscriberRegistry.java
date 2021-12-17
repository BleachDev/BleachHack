package org.bleachhack.eventbus.registry;

import java.util.concurrent.atomic.AtomicLong;

import org.apache.logging.log4j.Logger;
import org.bleachhack.event.Event;

public abstract class BleachSubscriberRegistry {

	private final String id;
	protected final AtomicLong eventsPosted = new AtomicLong();

	public BleachSubscriberRegistry(String id) {
		this.id = id;
	}

	public abstract boolean subscribe(Object object);

	public abstract boolean unsubscribe(Object object);

	public abstract void post(Event event, Logger logger);

	public String getId() {
		return id;
	}

	public long getEventsPosted() {
		return eventsPosted.get();
	}
}
