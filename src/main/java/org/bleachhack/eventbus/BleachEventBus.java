package org.bleachhack.eventbus;

import org.apache.logging.log4j.Logger;
import org.bleachhack.event.Event;
import org.bleachhack.eventbus.registry.BleachSubscriberRegistry;

public class BleachEventBus {

	private final Logger logger;
	private final BleachSubscriberRegistry registry;

	public BleachEventBus(BleachSubscriberRegistry registry, Logger logger) {
		this.registry = registry;
		this.logger = logger;
	}

	public boolean subscribe(Object object) {
		return registry.subscribe(object);
	}

	public boolean unsubscribe(Object object) {
		return registry.unsubscribe(object);
	}

	public void post(Event event) {
		registry.post(event, logger);
	}

	public long getEventsPosted() {
		return registry.getEventsPosted();
	}
}
