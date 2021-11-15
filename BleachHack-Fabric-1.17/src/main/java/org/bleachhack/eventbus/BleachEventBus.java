package org.bleachhack.eventbus;

import org.apache.logging.log4j.Logger;
import org.bleachhack.event.Event;

public class BleachEventBus {

	private final Logger logger;
	private BleachSubscriberRegistry subscriberRegistry = new BleachSubscriberRegistry("default");

	public BleachEventBus(Logger logger) {
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
