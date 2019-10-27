package bleach.hack.event.events;

import bleach.hack.event.Event;

public class EventKeyPress extends Event {
    private int key;
    private int scanCode;

    public EventKeyPress(int key, int scanCode){
        this.key = key;
        this.scanCode = scanCode;
    }

    public int getKey() {
        return key;
    }
    
    public int getScanCode() {
    	return scanCode;
    }
}
