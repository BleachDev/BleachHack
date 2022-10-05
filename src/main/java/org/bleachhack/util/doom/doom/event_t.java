/**
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

package org.bleachhack.util.doom.doom;

// Event structure.

import org.bleachhack.util.doom.g.Signals.ScanCode;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntConsumer;
import java.util.function.IntPredicate;
import java.util.function.Predicate;
import org.bleachhack.util.doom.utils.C2JUtils;

@FunctionalInterface
public interface event_t {
    int MOUSE_LEFT = 1;
    int MOUSE_RIGHT = 2;
    int MOUSE_MID = 4;
    
    int JOY_1 = 1;
    int JOY_2 = 2;
    int JOY_3 = 4;
    int JOY_4 = 8;
    
    // Special FORCED and PAINFUL key and mouse cancel event.
    event_t EMPTY_EVENT = () -> evtype_t.ev_null;
    event_t CANCEL_KEYS = () -> evtype_t.ev_clear;
    event_t CANCEL_MOUSE = new event_t.mouseevent_t(evtype_t.ev_mouse, 0, 0, 0);

    default boolean hasData() { return false; }
    default boolean isKey() { return false; }
    default boolean isKey(ScanCode sc) { return false; }
    default <T> T mapByKey(Function<? super ScanCode, ? extends T> scMapper) { return scMapper.apply(null); }
    default boolean withKey(Consumer<? super ScanCode> scConsumer) { return false; }
    default boolean ifKey(Predicate<? super ScanCode> scCondition) { return false; }
    default boolean withKeyChar(IntConsumer scCharConsumer) { return false; }
    default boolean ifKeyChar(IntPredicate scCharCondition) { return false; }
    default boolean withKeyAsciiChar(IntConsumer scAsciiCharConsumer) { return false; }
    default boolean ifKeyAsciiChar(IntPredicate scCharCondition) { return false; }
    default <T> boolean withKey(Consumer<? super T> scConsumer, Function<? super ScanCode, ? extends T> extractor) { return false; }
    default <T> boolean ifKey(Predicate<? super T> scCondition, Function<? super ScanCode, ? extends T> extractor) { return false; }
    default ScanCode getSC() { return ScanCode.SC_NULL; }
    default boolean isMouse() { return false; }
    default boolean isMouse(int button) { return false; }
    default <T> T mapByMouse(Function<? super mouseevent_t, ? extends T> mouseMapper) { return mouseMapper.apply(null); }
    default boolean withMouse(Consumer<? super mouseevent_t> mouseConsumer) { return false; }
    default boolean ifMouse(Predicate<? super mouseevent_t> mouseCondition) { return false; }
    default <T> boolean withMouse(Consumer<? super T> mouseConsumer, Function<? super mouseevent_t, ? extends T> extractor) { return false; }
    default <T> boolean ifMouse(Predicate<? super T> mouseCondition, Function<? super mouseevent_t, ? extends T> extractor) { return false; }
    default boolean isJoy() { return false;  }
    default boolean isJoy(int button) { return false; }
    default <T> T mapByJoy(Function<? super joyevent_t, ? extends T> joyMapper) { return joyMapper.apply(null); }
    default boolean withJoy(Consumer<? super joyevent_t> joyConsumer) { return false; }
    default boolean ifJoy(Predicate<? super joyevent_t> joyCondition) { return false; }
    default <T> boolean withJoy(Consumer<? super T> joyConsumer, Function<? super joyevent_t, ? extends T> extractor) { return false; }
    default <T> boolean ifJoy(Predicate<? super T> joyCondition, Function<? super joyevent_t, ? extends T> extractor) { return false; }
    evtype_t type();
    
    default boolean isType(evtype_t type) {
        return type() == type;
    }
    
    default boolean isKey(ScanCode sc, evtype_t type) {
        return type() == type && isKey(sc);
    }
    
    default boolean ifKey(evtype_t type, Predicate<? super ScanCode> scCondition) {
        if (type() == type) {
            return ifKey(scCondition);
        }
        
        return false;
    }
    
    default boolean withKey(evtype_t type, Consumer<? super ScanCode> scConsumer) {
        if (type() == type) {
            return event_t.this.withKey(scConsumer);
        }
        
        return false;
    }
    
    default boolean withKey(ScanCode sc, evtype_t type, Runnable runnable) {
        if (type() == type) {
            return withKey(sc, runnable);
        }
        
        return false;
    }
    
    default boolean withKey(ScanCode sc, Runnable runnable) {
        if (isKey(sc)) {
            runnable.run();
            return true;
        }
        
        return false;
    }
    
    default boolean isMouse(int button, evtype_t type) {
        return type() == type && isMouse(button);
    }
    
    default boolean ifMouse(evtype_t type, Predicate<? super mouseevent_t> mouseCondition) {
        if (type() == type) {
            return ifMouse(mouseCondition);
        }
        
        return false;
    }
    
    default boolean withMouse(evtype_t type, Consumer<? super mouseevent_t> mouseConsumer) {
        if (type() == type) {
            return event_t.this.withMouse(mouseConsumer);
        }
        
        return false;
    }
    
    default boolean withMouse(int button, evtype_t type, Runnable runnable) {
        if (type() == type) {
            return withMouse(button, runnable);
        }
        
        return false;
    }
    
    default boolean withMouse(int button, Runnable runnable) {
        if (isMouse(button)) {
            runnable.run();
            return true;
        }
        
        return false;
    }
    
    default boolean isJoy(int button, evtype_t type) {
        return type() == type && isJoy(button);
    }
    
    default boolean ifJoy(evtype_t type, Predicate<? super joyevent_t> joyCondition) {
        if (type() == type) {
            return ifJoy(joyCondition);
        }
        
        return false;
    }
    
    default boolean withJoy(evtype_t type, Consumer<? super joyevent_t> joyConsumer) {
        if (type() == type) {
            return event_t.this.withJoy(joyConsumer);
        }
        
        return false;
    }
    
    default boolean withJoy(int button, evtype_t type, Runnable runnable) {
        if (type() == type) {
            return withJoy(button, runnable);
        }
        
        return false;
    }
    
    default boolean withJoy(int button, Runnable runnable) {
        if (isJoy(button)) {
            runnable.run();
            return true;
        }
        
        return false;
    }
    
    static int mouseBits(int button) {
        switch(button) {
            case MouseEvent.BUTTON1:
                return MOUSE_LEFT;
            case MouseEvent.BUTTON2:
                return MOUSE_RIGHT;
            case MouseEvent.BUTTON3:
                return MOUSE_MID;
        }
        
        return 0;
    }
    
    final class keyevent_t implements event_t {
        public evtype_t type;
        public ScanCode sc;

        public keyevent_t(evtype_t type, ScanCode sc) {
            this.type = type;
            this.sc = sc;
        }

        @Override
        public boolean hasData() {
            return sc != ScanCode.SC_NULL;
        }

        @Override
        public evtype_t type() {
            return type;
        }

        @Override
        public boolean isKey() {
            return true;
        }

        @Override
        public boolean isKey(ScanCode sc) {
            return this.sc == sc;
        }

        @Override
        public boolean ifKey(Predicate<? super ScanCode> scCondition) {
            return scCondition.test(sc);
        }

        @Override
        public boolean withKey(Consumer<? super ScanCode> scConsumer) {
            scConsumer.accept(sc);
            return true;
        }

        @Override
        public boolean ifKeyChar(IntPredicate scCharCondition) {
            return scCharCondition.test(sc.c);
        }

        @Override
        public boolean withKeyChar(IntConsumer scCharConsumer) {
            scCharConsumer.accept(sc.c);
            return true;
        }

        @Override
        public boolean ifKeyAsciiChar(IntPredicate scAsciiCharCondition) {
            return sc.c > 255 ? false : ifKeyChar(scAsciiCharCondition);
        }

        @Override
        public boolean withKeyAsciiChar(IntConsumer scAsciiCharConsumer) {
            return sc.c > 255 ? false : withKeyChar(scAsciiCharConsumer);
        }
        
        @Override
        public <T> boolean ifKey(Predicate<? super T> scCondition, Function<? super ScanCode, ? extends T> extractor) {
            return scCondition.test(extractor.apply(sc));
        }

        @Override
        public <T> boolean withKey(Consumer<? super T> scConsumer, Function<? super ScanCode, ? extends T> extractor) {
            scConsumer.accept(extractor.apply(sc));
            return true;
        }

        @Override
        public <T> T mapByKey(Function<? super ScanCode, ? extends T> scMapper) {
            return scMapper.apply(sc);
        }

        @Override
        public ScanCode getSC() {
            return sc;
        }
    }
    
    final class mouseevent_t implements event_t {
        public volatile evtype_t type;
        public volatile boolean robotMove;
        public volatile boolean processed = true;
        public volatile int buttons;
        public volatile int x, y;

        public mouseevent_t(evtype_t type, int buttons, int x, int y) {
            this.type = type;
            this.buttons = buttons;
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean hasData() {
            return buttons != 0;
        }
        
        public void buttonOn(MouseEvent ev) {
            buttons |= mouseBits(ev.getButton());
        }

        public void buttonOff(MouseEvent ev) {
            buttons ^= mouseBits(ev.getButton());
        }
        
        public void processedNotify() {
            this.processed = true;
        }
        
        public void resetNotify() {
            this.processed = false;
        }
        
        public void moveIn(MouseEvent ev, int centreX, int centreY, boolean drag) {
            final int mouseX = ev.getX(), mouseY = ev.getY();
            
            // Mouse haven't left centre of the window
            if (mouseX == centreX && mouseY == centreY) {
                return;
            }
            
            // A pure move has no buttons.
            if (!drag) {
                buttons = 0;
            }

            /**
             * Now also fix for -fasttic mode
             *  - Good Sign 2017/05/07
             * 
             * Fix bug with processing mouse: the DOOM underlying engine does not
             * react on the event as fast as it came, they are processed in constant time instead.
             * 
             * In Mocha Doom, mouse events are not generated in bulks and sent to underlying DOOM engine,
             * instead the one only mouse event reused and resend modified if was consumed.
             * 
             * So, if we have event system reacting faster then DOOM underlying engine,
             * mouse will be harder to move because the new move is forgotten earlier then processed.
             * 
             * As a workaround, do not replace value in moveIn, and increment it instead,
             * and only when the underlying engine gives signal it has processed event, we clear x and y
             * 
             *  - Good Sign 2017/05/06
             */
            if (processed) {
                this.x = (mouseX - centreX) << 2;
                this.y = (centreY - mouseY) << 2;
            } else {
                this.x += (mouseX - centreX) << 2;
                this.y += (centreY - mouseY) << 2;
            }
        }
        
        public void moveIn(MouseEvent ev, Robot robot, Point windowOffset, int centreX, int centreY, boolean drag) {
            moveIn(ev, centreX, centreY, drag);
            resetIn(robot, windowOffset, centreX, centreY);
        }

        public void resetIn(Robot robot, Point windowOffset, int centreX, int centreY) {
            // Mark that the next event will be from robot
            robotMove = true;

            // Move the mouse to the window center
            robot.mouseMove(windowOffset.x + centreX, windowOffset.y + centreY);
        }
        
        @Override
        public evtype_t type() {
            return type;
        }

        @Override
        public boolean isMouse() {
            return true;
        }

        @Override
        public boolean isMouse(int button) {
            return C2JUtils.flags(buttons, button);
        }

        @Override
        public boolean ifMouse(Predicate<? super mouseevent_t> mouseCondition) {
            return mouseCondition.test(this);
        }

        @Override
        public boolean withMouse(Consumer<? super mouseevent_t> mouseConsumer) {
            mouseConsumer.accept(this);
            return true;
        }

        @Override
        public <T> boolean ifMouse(Predicate<? super T> mouseCondition, Function<? super mouseevent_t, ? extends T> extractor) {
            return mouseCondition.test(extractor.apply(this));
        }

        @Override
        public <T> boolean withMouse(Consumer<? super T> mouseConsumer, Function<? super mouseevent_t, ? extends T> extractor) {
            mouseConsumer.accept(extractor.apply(this));
            return true;
        }

        @Override
        public <T> T mapByMouse(Function<? super mouseevent_t, ? extends T> mouseMapper) {
            return mouseMapper.apply(this);
        }
    }
    
    final class joyevent_t implements event_t {
        public evtype_t type;
        public int buttons;
        public int x, y;

        public joyevent_t(evtype_t type, int buttons, int x, int y) {
            this.type = type;
            this.buttons = buttons;
            this.x = x;
            this.y = y;
        }

        @Override
        public boolean hasData() {
            return buttons != 0;
        }        

        @Override
        public evtype_t type() {
            return type;
        }

        @Override
        public boolean isJoy() {
            return true;
        }

        @Override
        public boolean isJoy(int button) {
            return C2JUtils.flags(buttons, button);
        }

        @Override
        public boolean ifJoy(Predicate<? super joyevent_t> joyCondition) {
            return joyCondition.test(this);
        }

        @Override
        public boolean withJoy(Consumer<? super joyevent_t> joyConsumer) {
            joyConsumer.accept(this);
            return true;
        }

        @Override
        public <T> boolean ifJoy(Predicate<? super T> joyCondition, Function<? super joyevent_t, ? extends T> extractor) {
            return joyCondition.test(extractor.apply(this));
        }

        @Override
        public <T> boolean withJoy(Consumer<? super T> joyConsumer, Function<? super joyevent_t, ? extends T> extractor) {
            joyConsumer.accept(extractor.apply(this));
            return true;
        }

        @Override
        public <T> T mapByJoy(Function<? super joyevent_t, ? extends T> mouseMapper) {
            return mouseMapper.apply(this);
        }
    }
};
