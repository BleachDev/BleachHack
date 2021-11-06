package bleach.hack.event.events;

import bleach.hack.event.Event;

public class EventModuleToggle extends Event {

    private boolean state;

    private String moduleName;

    public EventModuleToggle(boolean state, String moduleName) {
        this.state = state;
        this.moduleName = moduleName;
    }

    public boolean getState() {
        return state;
    }

    public String getModuleName() {
        return moduleName;
    }

}
