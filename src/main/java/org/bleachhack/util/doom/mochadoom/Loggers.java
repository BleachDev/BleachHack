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

package org.bleachhack.util.doom.mochadoom;

import org.bleachhack.util.doom.awt.DoomWindow;
import org.bleachhack.util.doom.awt.EventBase;
import org.bleachhack.util.doom.awt.EventBase.ActionMode;
import org.bleachhack.util.doom.awt.EventBase.ActionStateHolder;
import org.bleachhack.util.doom.awt.EventBase.RelationType;
import java.awt.AWTEvent;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.bleachhack.util.doom.p.ActiveStates;
import org.bleachhack.util.doom.v.graphics.Patches;

/**
 * Facility to manage Logger Levels for different classes
 * All of that should be used instead of System.err.println for debug
 * 
 * @author Good Sign
 */
public class Loggers {
    private static final Level DEFAULT_LEVEL = Level.WARNING;
    
    private static final Map<Level, Logger> PARENT_LOGGERS_MAP = Stream.of(
            Level.FINE, Level.FINER, Level.FINEST, Level.INFO, Level.SEVERE, Level.WARNING
        ).collect(Collectors.toMap(l -> l, Loggers::newLoggerHandlingLevel));
    
    private static final Logger DEFAULT_LOGGER = PARENT_LOGGERS_MAP.get(DEFAULT_LEVEL);
    private static final HashMap<String, Logger> INDIVIDUAL_CLASS_LOGGERS = new HashMap<>();
    
    static {
        //INDIVIDUAL_CLASS_LOGGERS.put(EventObserver.class.getName(), PARENT_LOGGERS_MAP.get(Level.FINE));
        //INDIVIDUAL_CLASS_LOGGERS.put(TraitFactory.class.getName(), PARENT_LOGGERS_MAP.get(Level.FINER));
        INDIVIDUAL_CLASS_LOGGERS.put(ActiveStates.class.getName(), PARENT_LOGGERS_MAP.get(Level.FINER));
        INDIVIDUAL_CLASS_LOGGERS.put(DoomWindow.class.getName(), PARENT_LOGGERS_MAP.get(Level.FINE));
        INDIVIDUAL_CLASS_LOGGERS.put(Patches.class.getName(), PARENT_LOGGERS_MAP.get(Level.INFO));
    }
    
    public static Logger getLogger(final String className) {
        final Logger ret = Logger.getLogger(className);
        ret.setParent(INDIVIDUAL_CLASS_LOGGERS.getOrDefault(className, DEFAULT_LOGGER));
        
        return ret;
    }
    
    private static EventBase<?> lastHandler = null;
    
    public static <EventHandler extends Enum<EventHandler> & EventBase<EventHandler>> void LogEvent(
        final Logger logger,
        final ActionStateHolder<EventHandler> actionStateHolder,
        final EventHandler handler,
        final AWTEvent event
    ) {
        if (!logger.isLoggable(Level.ALL) && lastHandler == handler) {
            return;
        }
        
        lastHandler = handler;
        
        final IntFunction<EventBase<EventHandler>[]> arrayGenerator = EventBase[]::new;
        final EventBase<EventHandler>[] depends = actionStateHolder
                .cooperations(handler, RelationType.DEPEND)
                .stream()
                .filter(hdl -> actionStateHolder.hasActionsEnabled(hdl, ActionMode.DEPEND))
                .toArray(arrayGenerator);

        final Map<RelationType, Set<EventHandler>> adjusts = actionStateHolder
                .adjustments(handler);
        
        final EventBase<EventHandler>[] causes = actionStateHolder
                .cooperations(handler, RelationType.CAUSE)
                .stream()
                .filter(hdl -> actionStateHolder.hasActionsEnabled(hdl, ActionMode.DEPEND))
                .toArray(arrayGenerator);

        final EventBase<EventHandler>[] reverts = actionStateHolder
                .cooperations(handler, RelationType.REVERT)
                .stream()
                .filter(hdl -> actionStateHolder.hasActionsEnabled(hdl, ActionMode.DEPEND))
                .toArray(arrayGenerator);
        
        if (logger.isLoggable(Level.FINEST))
            logger.log(Level.FINEST, () -> String.format(
                "\n\nENCOUNTERED EVENT: %s [%s] \n%s: %s \n%s \n%s: %s \n%s: %s \nOn event: %s",
                handler, ActionMode.PERFORM,
                RelationType.DEPEND, Arrays.toString(depends),
                adjusts.entrySet().stream().collect(StringBuilder::new, (sb, e) -> sb.append(e.getKey()).append(' ').append(e.getValue()).append('\n'), StringBuilder::append),
                RelationType.CAUSE, Arrays.toString(causes),
                RelationType.REVERT, Arrays.toString(reverts),
                event
            ));
        else if (logger.isLoggable(Level.FINER)) {
            logger.log(Level.FINER, () -> String.format(
                "\n\nENCOUNTERED EVENT: %s [%s] \n%s: %s \n%s \n%s: %s \n%s: %s \n",
                handler, ActionMode.PERFORM,
                RelationType.DEPEND, Arrays.toString(depends),
                adjusts.entrySet().stream().collect(StringBuilder::new, (sb, e) -> sb.append(e.getKey()).append(' ').append(e.getValue()).append('\n'), StringBuilder::append),
                RelationType.CAUSE, Arrays.toString(causes),
                RelationType.REVERT, Arrays.toString(reverts)
            ));
        } else {
            logger.log(Level.FINE, () -> String.format(
                "\nENCOUNTERED EVENT: %s [%s]",
                handler, ActionMode.PERFORM
            ));
        }
    }
    
    private Loggers() {}
    
    private static Logger newLoggerHandlingLevel(final Level l) {
        final OutHandler h = new OutHandler();
        h.setLevel(l);
        final Logger ret = Logger.getAnonymousLogger();
        ret.setUseParentHandlers(false);
        ret.setLevel(l);
        ret.addHandler(h);
        return ret;
    }
    
    private static final class OutHandler extends ConsoleHandler {
        @Override
        @SuppressWarnings("UseOfSystemOutOrSystemErr")
        protected synchronized void setOutputStream(final OutputStream out) throws SecurityException {
            super.setOutputStream(System.out);
        }
    }
}
