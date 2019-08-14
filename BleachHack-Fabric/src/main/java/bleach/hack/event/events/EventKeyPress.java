package bleach.hack.event.events;

import bleach.hack.event.Event;

public class EventKeyPress extends Event {
    private int key;

    public EventKeyPress(int key){
        this.key = key;
    }

    public int getKey() {
        return key;
    }
}
