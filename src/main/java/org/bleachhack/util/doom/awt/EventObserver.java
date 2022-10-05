/*
 * Copyright (C) 2017 Good Sign
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.bleachhack.util.doom.awt;

import org.bleachhack.util.doom.awt.EventBase.ActionMode;
import org.bleachhack.util.doom.awt.EventBase.ActionStateHolder;
import org.bleachhack.util.doom.awt.EventBase.EventAction;
import org.bleachhack.util.doom.awt.EventBase.KeyStateHolder;
import org.bleachhack.util.doom.awt.EventBase.RelationType;
import static org.bleachhack.util.doom.awt.EventBase.findById;
import static org.bleachhack.util.doom.awt.EventBase.sortHandlers;
import org.bleachhack.util.doom.doom.event_t;
import org.bleachhack.util.doom.doom.evtype_t;
import org.bleachhack.util.doom.g.Signals;
import java.awt.AWTEvent;
import java.awt.AWTException;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bleachhack.util.doom.mochadoom.Loggers;

/**
 * Observer for AWTEvents. The description would be short in contrary to the description
 * of EventHandler Enum. This class uses rules in Handler extends Enum<Handler> & EventBase<Handler>
 * to react on AWTEvent events given to him by some listener (or by fake, don't matter) and feeds them
 * to someone who needs them (DOOM's internal event handling system)
 * 
 * Also, you may use any Enum & EventBase dictionary, not just EventHandler.
 * It may be useful if you design a game with several modes or with several systems, or something,
 * and you need one part to react in one way, another part in another.
 * 
 * @author Good Sign
 */
public class EventObserver<Handler extends Enum<Handler> & EventBase<Handler>> {
    
    static final Optional<Robot> MOUSE_ROBOT = createRobot();
    private static final Logger LOGGER = Loggers.getLogger(EventObserver.class.getName());

    /**
     * The Robot does not necessary gets created. When not, it throws an exception.
     * We ignore that exception, and set Robot to null. So, any call to Robot
     * must first check against null. So I've just made it Optional<Robot> - for no headache.
     * - Good Sign 2017/04/24
     *
     * In my opinion, its better turn off mouse at all, then without Robot.
     * But the support to run without it, though untested, must be present.
     * - Good Sign 2017/04/22
     *
     * Create AWT Robot for forcing mouse
     *
     * @author Good Sign
     * @author vekltron
     */
    private static Optional<Robot> createRobot() {
        try {
            return Optional.of(new Robot());
        } catch (AWTException e) {
            Loggers.getLogger(EventObserver.class.getName())
                .log(Level.SEVERE, "AWT Robot could not be created, mouse input focus will be loose!", e);
        }
        return Optional.empty();
    }

    /**
     * NASTY hack to hide the cursor.
     *
     * Create a 'hidden' cursor by using a transparent image
     * ...return the invisible cursor
     * @author vekltron
     */
    private Cursor createHiddenCursor() {
        final Toolkit tk = Toolkit.getDefaultToolkit();
        final Dimension dim = tk.getBestCursorSize(2, 2);
        if (dim.width == 0 || dim.height == 0) {
            return this.initialCursor;
        }
        final BufferedImage transparent = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_ARGB);
        return tk.createCustomCursor(transparent, new Point(1, 1), "HiddenCursor");
    }
    
    /**
     * This event here is used as a static scratch copy. When sending out
     * messages, its contents are to be actually copied (struct-like).
     * This avoids the continuous object creation/destruction overhead,
     * And it also allows creating "sticky" status.
     *
     * Also, as I've made event_t.mouseevent_t fields volatile, there is
     * no more need to synchronize on it in the multithread event listening solutions.
     */
    protected final event_t.mouseevent_t mouseEvent = new event_t.mouseevent_t(evtype_t.ev_mouse, 0, 0, 0);

    /**
     * Shared state of keys
     */
    protected final KeyStateHolder<Handler> keyStateHolder;
    
    /**
     * Component (Canvas or JPanel, for exaple) to deal with
     */
    protected final Component component;
    
    /**
     * This one will be given all event_t's we produce there
     */
    private final Consumer<? super event_t> doomEventConsumer;
    
    /**
     * Will be used to find Handler by AWTEvent's id
     */
    private final Handler[] eventSortedHandlers;
    
    /**
     * Shared state of actions
     */
    private final ActionStateHolder<Handler> actionStateHolder;
    
    /**
     * Presumably a system Cursor, that is to be used on cursor restore.
     */
    private final Cursor initialCursor;

    /**
     * Ivisible cursor on the systems who support changing cursors
     */
    private final Cursor hiddenCursor;
    
    /**
     * To construct the Observer you only need to provide it with the class of Enum used
     * to contain dictinary, the Component it will be working on and acceptor of event_t's
     */
    public EventObserver(Class<Handler> handlerClass, Component component, Consumer<? super event_t> doomEventConsumer) {
        this.actionStateHolder = new ActionStateHolder<>(handlerClass, this);
        this.eventSortedHandlers = sortHandlers(handlerClass.getEnumConstants());
        this.doomEventConsumer = doomEventConsumer;
        this.component = component;
        this.initialCursor = component.getCursor();
        this.hiddenCursor = createHiddenCursor();
        this.keyStateHolder = new KeyStateHolder<>();
    }
    
    public EventObserver<Handler> addInterest(EventBase.KeyStateInterest<Handler> interest) {
        keyStateHolder.addInterest(interest);
        return this;
    }

    public EventObserver<Handler>  removeInterest(EventBase.KeyStateInterest<Handler> interest) {
        keyStateHolder.removeInterest(interest);
        return this;
    }

    /**
     * This method is designed to acquire events from some kind of listener.
     * EventHandler class do not provide listener itself - but should work with any.
     */
    public void observe(final AWTEvent ev) {
        final Optional<Handler> maybe = findById(eventSortedHandlers, ev.getID());
        final Handler handler;
        if (!maybe.isPresent() || !actionStateHolder.hasActionsEnabled(handler = maybe.get(), ActionMode.PERFORM)) {
            return;
        }
        
        if (handler == EventHandler.WINDOW_ACTIVATE) {
            int u = 8;
        }
        
        // In case of debug. If level > FINE (most of cases) it will not affect anything
        Loggers.LogEvent(LOGGER, actionStateHolder, handler, ev);
        
        actionStateHolder.run(handler, ActionMode.PERFORM, ev);
        actionStateHolder.adjustments(handler).forEach((relation, affected) -> {
            switch (relation.affection) {
                case ENABLES:
                    affected.forEach(h -> {
                        actionStateHolder.enableAction(h, relation.affectedMode);
                    });
                    return;
                case DISABLES:
                    affected.forEach(h -> {
                        actionStateHolder.disableAction(h, relation.affectedMode);
                    });
                default:
                	break;
            }
        });
        
        actionStateHolder.cooperations(handler, RelationType.CAUSE).forEach(h -> {
            actionStateHolder.run(h, ActionMode.CAUSE, ev);
        });
        
        actionStateHolder.cooperations(handler, RelationType.REVERT).forEach(h -> {
            actionStateHolder.run(h, ActionMode.REVERT, ev);
        });
    }

    /**
     * The way to supply underlying engine with event generated by this class
     * This function is the last barrier between user input and DOOM's internal event hell.
     * So there are all user key interests checked.
     */
    protected void feed(final event_t ev) {
        if (!ev.ifKey(sc -> keyStateHolder.notifyKeyChange(this, sc, ev.isType(evtype_t.ev_keydown)))) {
            doomEventConsumer.accept(ev);
        }
    }

    /**
     * Restore default system cursor over the window
     */
    protected void restoreCursor(final AWTEvent event) {
        component.setCursor(initialCursor);
    }

    /**
     * Hide cursor
     */
    protected void modifyCursor(final AWTEvent event) {
        component.getInputContext().selectInputMethod(java.util.Locale.US);
        component.setCursor(hiddenCursor);
    }

    /**
     * Move the cursor into the centre of the window. The event_t.mouseevent_t implementation
     * would set robotMove flag for us to be able to distinguish the Robot-caused moves
     * and not react on them (thus preventing look to be stuck or behave weird)
     *  - Good Sign 2017/04/24
     */
    protected void centreCursor(final AWTEvent event) {
        final int centreX = component.getWidth() >> 1;
        final int centreY = component.getHeight() >> 1;
        if (component.isShowing()) {
            MOUSE_ROBOT.ifPresent(rob -> mouseEvent.resetIn(rob, component.getLocationOnScreen(), centreX, centreY));
        }
        modifyCursor(event);
    }

    /**
     * Forcibly clear key events in the underlying engine
     */
    protected void cancelKeys(final AWTEvent ev) {
        feed(event_t.CANCEL_KEYS);
        keyStateHolder.removeAllKeys();
    }

    /**
     * Forcibly clear mouse events in the underlying engine, discard cursor modifications
     */
    protected void cancelMouse(final AWTEvent ev) {
        feed(event_t.CANCEL_MOUSE);
    }

    /**
     * Send key releases to underlying engine
     */
    protected void sendKeyUps(final AWTEvent ev) {
        feed(Signals.getScanCode((KeyEvent) ev).doomEventUp);
        discardInputEvent(ev);
    }

    /**
     * Send key presses to underlying engine
     */
    protected void sendKeyDowns(final AWTEvent ev) {
        feed(Signals.getScanCode((KeyEvent) ev).doomEventDown);
        discardInputEvent(ev);
    }
    
    /**
     * Consumes InputEvents so they will not pass further
     */
    protected void discardInputEvent(final AWTEvent ev) {
        try {
            ((InputEvent) ev).consume();
        } catch (ClassCastException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
        }
    }
    
    protected final void enableAction(final Handler h, ActionMode mode) {
        actionStateHolder.enableAction(h, mode);
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, () -> String.format("ENABLE ACTION: %s [%s]", h, mode));
        }
    }
    
    protected final void disableAction(final Handler h, ActionMode mode) {
        actionStateHolder.disableAction(h, mode);
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, () -> String.format("DISABLE ACTION: %s [%s]", h, mode));
        }
    }
    
    @SafeVarargs
    protected final void mapRelation(final Handler h, RelationType type, Handler... targets) {
        if (type.affection == EventBase.RelationAffection.COOPERATES) {
            actionStateHolder.mapCooperation(h, type, targets);
        } else {
            actionStateHolder.mapAdjustment(h, type, targets);
        }
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, () -> String.format("RELATION MAPPING: %s -> [%s] {%s}", h, type, Arrays.toString(targets)));
        }
    }
    
    @SafeVarargs
    protected final void unmapRelation(final Handler h, RelationType type, Handler... targets) {
        if (type.affection == EventBase.RelationAffection.COOPERATES) {
            actionStateHolder.unmapCooperation(h, type, targets);
        } else {
            actionStateHolder.unmapAdjustment(h, type, targets);
        }
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, () -> String.format("RELATION UNMAP: %s -> [%s] {%s}", h, type, Arrays.toString(targets)));
        }
    }
    
    @SafeVarargs
    protected final void restoreRelation(final Handler h, RelationType type, Handler... targets) {
        if (type.affection == EventBase.RelationAffection.COOPERATES) {
            actionStateHolder.restoreCooperation(h, type, targets);
        } else {
            actionStateHolder.restoreAdjustment(h, type, targets);
        }
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, () -> String.format("RELATION RESTORE: %s -> [%s] {%s}", h, type, Arrays.toString(targets)));
        }
    }
    
    protected void mapAction(final Handler h, ActionMode mode, EventAction<Handler> remap) {
        actionStateHolder.mapAction(h, mode, remap);
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, () -> String.format("ACTION MAPPING (MAP): %s [%s]", h, mode));
        }
    }
    
    protected void remapAction(final Handler h, ActionMode mode, EventAction<Handler> remap) {
        actionStateHolder.remapAction(h, mode, remap);
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, () -> String.format("ACTION MAPPING (REMAP): %s [%s]", h, mode));
        }
    }
    
    protected void unmapAction(final Handler h, ActionMode mode) {
        actionStateHolder.unmapAction(h, mode);
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, () -> String.format("UNMAP ACTION: %s [%s]", h, mode));
        }
    }
    
    protected void restoreAction(final Handler h, ActionMode mode) {
        actionStateHolder.restoreAction(h, mode);
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.log(Level.FINE, () -> String.format("RESTORE ACTION: %s [%s]", h, mode));
        }
    }
}
