package org.bleachhack.eventbus;

import org.apache.logging.log4j.Logger;
import org.bleachhack.event.Event;
import org.bleachhack.eventbus.registry.BleachSubscriberRegistry;

public class BleachEventBus {

	private final Logger logger;
	private final BleachSubscriberRegistry subscriberRegistry;

	public BleachEventBus(BleachSubscriberRegistry registry, Logger logger) {
		this.subscriberRegistry = registry;
		this.logger = logger;
	}

	public boolean subscribe(Object object) {
		return subscriberRegistry.subscribe(object);
	}

	public boolean unsubscribe(Object object) {
		return subscriberRegistry.unsubscribe(object);
	}

	public void post(Event event) {
		subscriberRegistry.post(event, logger);
	}

	public long getEventsPosted() {
		return subscriberRegistry.getEventsPosted();
	}
}
