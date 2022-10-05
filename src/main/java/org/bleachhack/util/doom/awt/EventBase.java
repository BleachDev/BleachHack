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

import org.bleachhack.util.doom.g.Signals;
import java.awt.AWTEvent;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.IntSupplier;

/**
 * The base for construction of Event handling dictionaries
 * EventHandler is a reference implementation of this base
 * 
 * Note the type safety with generics. It could be a complex task, but you can avoid
 * unchecked casts and warnings suppression. Whoa... Make my head swirl around!
 *  - Good Sign 2017/04/24
 * 
 * @author Good Sign
 */
public interface EventBase<Handler extends Enum<Handler> & EventBase<Handler>> extends IntSupplier {
    static final Comparator<IntSupplier> EVENT_SORT = Comparator.comparingInt(IntSupplier::getAsInt);
    
    static <H extends Enum<H> & EventBase<H>> H[] sortHandlers(H[] values) {
        Arrays.sort(values, EVENT_SORT);
        return values;
    }
    
    static <H extends Enum<H> & EventBase<H>> Optional<H> findById(H[] values, int eventId) {
        final int index = Arrays.binarySearch(values, (IntSupplier) () -> eventId, EVENT_SORT);
        if (index < 0) {
            return Optional.empty();
        }
        
        return Optional.of(values[index]);
    }
    
    @SafeVarargs
    static <H extends Enum<H> & EventBase<H>> Relation<H>[] Relate(H src, H... dests) {
        final IntFunction<Relation<H>[]> arrayer = Relation[]::new;
        return Arrays.stream(dests)
            .map(dest -> new Relation<>(src, dest))
            .toArray(arrayer);
    }
    
    Set<ActionMode> defaultEnabledActions();
    Map<ActionMode, EventAction<Handler>> allActions();
    Map<RelationType, Set<Handler>> cooperations();
    Map<RelationType, Set<Handler>> adjustments();

    default boolean hasActions(final ActionMode... modes) {
        final Set<ActionMode> actions = defaultEnabledActions();
        if (actions.isEmpty()) {
            return false;
        }
        
        for (final ActionMode m: modes) {
            if (!actions.contains(m)) {
                return false;
            }
        }
        
        return true;
    }
    
    enum KeyStateSatisfaction {
        SATISFIED_ATE,
        GENEOROUS_PASS,
        WANTS_MORE_ATE,
        WANTS_MORE_PASS
    }
    
    enum ActionMode {
        PERFORM, DEPEND, CAUSE, REVERT;
    }
    
    enum RelationAffection {
        ENABLES, DISABLES, COOPERATES;
    }
    
    enum RelationType {
        ENABLE(RelationAffection.ENABLES, ActionMode.PERFORM),
        ENABLE_DEPEND(RelationAffection.ENABLES, ActionMode.DEPEND),
        ENABLE_CAUSE(RelationAffection.ENABLES, ActionMode.CAUSE),
        ENABLE_REVERT(RelationAffection.ENABLES, ActionMode.REVERT),
        
        DISABLE(RelationAffection.DISABLES, ActionMode.PERFORM),
        DISABLE_DEPEND(RelationAffection.DISABLES, ActionMode.DEPEND),
        DISABLE_CAUSE(RelationAffection.DISABLES, ActionMode.CAUSE),
        DISABLE_REVERT(RelationAffection.DISABLES, ActionMode.REVERT),
        
        DEPEND(RelationAffection.COOPERATES, ActionMode.DEPEND),
        CAUSE(RelationAffection.COOPERATES, ActionMode.CAUSE),
        REVERT(RelationAffection.COOPERATES, ActionMode.REVERT);
        
        final RelationAffection affection;
        final ActionMode affectedMode;

        private RelationType(RelationAffection affection, ActionMode affectedMode) {
            this.affection = affection;
            this.affectedMode = affectedMode;
        }

        @Override
        public String toString() {
            return String.format("%s on [%s]", affection, affectedMode);
        }
    }
    
    @FunctionalInterface
    interface ActionMapper<Handler extends Enum<Handler> & EventBase<Handler>> {
        void map(ActionMode mode, EventAction<Handler> action);
    }
    
    @FunctionalInterface
    interface RelationMapper<Handler extends Enum<Handler> & EventBase<Handler>> {
        void map(RelationType type, Relation<Handler>[] relations);
    }
    
    @FunctionalInterface
    interface EventAction<Handler extends Enum<Handler> & EventBase<Handler>> {
        void act(EventObserver<Handler> obs, AWTEvent ev);
    }
    
    interface KeyStateCallback<Handler extends Enum<Handler> & EventBase<Handler>>  {
        KeyStateSatisfaction call(EventObserver<Handler> observer);
    }
    
    final class KeyStateInterest<Handler extends Enum<Handler> & EventBase<Handler>> {
        private final Set<Signals.ScanCode> interestSet;
        private final KeyStateCallback<Handler> satisfiedCallback;

        public KeyStateInterest(
            final KeyStateCallback<Handler> satisfiedCallback,
            final Signals.ScanCode interestFirstKey,
            Signals.ScanCode... interestKeyChain
        ) {
            this.interestSet = EnumSet.of(interestFirstKey, interestKeyChain);
            this.satisfiedCallback = satisfiedCallback;
        }
    }
    
    final class KeyStateHolder<Handler extends Enum<Handler> & EventBase<Handler>> {
        private final Set<Signals.ScanCode> holdingSet;
        private final LinkedHashSet<KeyStateInterest<Handler>> keyInterests;
        private final IntFunction<KeyStateInterest<Handler>[]> generator = KeyStateInterest[]::new;

        public KeyStateHolder() {
            this.holdingSet = EnumSet.noneOf(Signals.ScanCode.class);
            this.keyInterests = new LinkedHashSet<>();
        }
        
        public void removeAllKeys() {
            holdingSet.clear();
        }
        
        public boolean contains(Signals.ScanCode sc) {
            return holdingSet.contains(sc);
        }
        
        public void addInterest(KeyStateInterest<Handler> interest) {
            this.keyInterests.add(interest);
        }
        
        public void removeInterest(KeyStateInterest<Handler> interest) {
            this.keyInterests.remove(interest);
        }
        
        public boolean matchInterest(final KeyStateInterest<Handler> check) {
            return holdingSet.containsAll(check.interestSet);
        }
        
        public boolean notifyKeyChange(EventObserver<Handler> observer, Signals.ScanCode code, boolean press) {
            if (press) {
                holdingSet.add(code);
                
                final KeyStateInterest<Handler>[] matched = keyInterests.stream()
                    .filter(this::matchInterest)
                    .toArray(this.generator);
                
                boolean ret = false;
                for (int i = 0; i < matched.length; ++i) {
                    switch (matched[i].satisfiedCallback.call(observer)) {
                        case SATISFIED_ATE:
                            ret = true;
                        case GENEOROUS_PASS:
                            keyInterests.remove(matched[i]);
                            break;
                        case WANTS_MORE_ATE:
                            ret = true;
                        case WANTS_MORE_PASS:
                            break;
                    }
                }
                
                return ret;
            } else {
                holdingSet.remove(code);
                return false;
            }
        }
    }
    
    /**
     * Enable/disable and remaps of actions is actually reflected here. It is only initial template in the Handler
     */
    final class ActionStateHolder<Handler extends Enum<Handler> & EventBase<Handler>> {
        private final Map<Handler, Set<ActionMode>> enabledActions;
        private final Map<Handler, Map<ActionMode, EventAction<Handler>>> actionsMap;
        private final Map<Handler, Map<RelationType, Set<Handler>>> cooperationMap;
        private final Map<Handler, Map<RelationType, Set<Handler>>> adjustmentMap;
        private final EventObserver<Handler> observer;
        private final EnumSet<Handler> emptyEnumSet;

        public boolean hasActionsEnabled(final Handler h, final ActionMode... modes) {
            final Set<ActionMode> actions = enabledActions.get(h);
            if (actions.isEmpty()) {
                return false;
            }

            for (final ActionMode m: modes) {
                if (!actions.contains(m)) {
                    return false;
                }
            }

            return true;
        }

        public ActionStateHolder(final Class<Handler> hClass, final EventObserver<Handler> observer) {
            final Handler[] values = hClass.getEnumConstants();
            this.enabledActions = populate(hClass, values, h -> {
                final Set<ActionMode> set = h.defaultEnabledActions();
                return set.isEmpty() ? EnumSet.noneOf(ActionMode.class) : EnumSet.copyOf(set);
            });
            this.actionsMap = populate(hClass, values, h -> {
                final Map<ActionMode, EventAction<Handler>> map = h.allActions();
                return map.isEmpty() ? new EnumMap<>(ActionMode.class) : new EnumMap<>(map);
            });
            this.cooperationMap = populate(hClass, values, h -> deepCopyMap(h.cooperations()));
            this.adjustmentMap = populate(hClass, values, h -> deepCopyMap(h.adjustments()));
            this.observer = observer;
            this.emptyEnumSet = EnumSet.noneOf(hClass);
        }
        
        private Map<RelationType, Set<Handler>> deepCopyMap(final Map<RelationType, Set<Handler>> map) {
            if (map.isEmpty()) {
                return new EnumMap<>(RelationType.class);
            }
            
            // shallow copy first
            final EnumMap<RelationType, Set<Handler>> copy = new EnumMap<>(map);
            // now values
            copy.replaceAll((r, l) -> EnumSet.copyOf(l));
            return copy;
        }
        
        private <V> Map<Handler, V> populate(Class<Handler> hClass, Handler[] values, Function<? super Handler, ? extends V> mapper) {
            return Arrays.stream(values).collect(
                () -> new EnumMap<>(hClass),
                (m, h) -> m.put(h, mapper.apply(h)),
                EnumMap::putAll
            );
        }
        
        public ActionStateHolder<Handler> run(final Handler h, final ActionMode mode, final AWTEvent ev) {
            if (enabledActions.get(h).contains(mode)) {
                Optional.ofNullable(actionsMap.get(h).get(mode)).ifPresent(action -> action.act(observer, ev));
            }

            return this;
        }
        
        public Map<RelationType, Set<Handler>> cooperations(final Handler h) {
            return cooperationMap.get(h);
        }
        
        public Map<RelationType, Set<Handler>> adjustments(final Handler h) {
            return adjustmentMap.get(h);
        }
                
        public Set<Handler> cooperations(final Handler h, final RelationType type) {
            return cooperationMap.get(h).getOrDefault(type, emptyEnumSet);
        }
        
        public Set<Handler> adjustments(final Handler h, final RelationType type) {
            return adjustmentMap.get(h).getOrDefault(type, emptyEnumSet);
        }
                
        @SafeVarargs
        public final ActionStateHolder<Handler> unmapCooperation(final Handler h, RelationType type, final Handler... targets) {
            final Set<Handler> set = cooperationMap.get(h).get(type);
            if (set == null || set.isEmpty()) {
                return this;
            }
            
            if (targets.length == 0) {
                set.clear();
            } else {
                set.removeAll(Arrays.asList(targets));
            }
            
            return this;
        }
        
        @SafeVarargs
        public final ActionStateHolder<Handler> mapCooperation(final Handler h, RelationType mode, final Handler... targets) {
            cooperationMap.get(h).compute(mode, (m, set) -> {
                if (set == null) {
                    set = EnumSet.copyOf(emptyEnumSet);
                }
                set.addAll(Arrays.asList(targets));
                return set;
            });
            
            return this;
        }
        
        @SafeVarargs
        public final ActionStateHolder<Handler> restoreCooperation(final Handler h, RelationType mode, final Handler... targets) {
            final Set<Handler> orig = h.adjustments().get(mode);
            
            if (orig != null) {
                final Set<Handler> a = EnumSet.copyOf(orig);
                final Set<Handler> b = cooperationMap.get(h).get(mode);
                a.retainAll(Arrays.asList(targets));
                b.addAll(a);
            } else {
                cooperationMap.get(h).remove(mode);
            }
            
            return this;
        }
        
        @SafeVarargs
        public final ActionStateHolder<Handler> unmapAdjustment(final Handler h, RelationType type, final Handler... targets) {
            final Set<Handler> set = adjustmentMap.get(h).get(type);
            if (set == null || set.isEmpty()) {
                return this;
            }
            
            if (targets.length == 0) {
                set.clear();
            } else {
                set.removeAll(Arrays.asList(targets));
            }
            
            return this;
        }
        
        @SafeVarargs
        public final ActionStateHolder<Handler> mapAdjustment(final Handler h, RelationType mode, final Handler... targets) {
            adjustmentMap.get(h).compute(mode, (m, set) -> {
                if (set == null) {
                    set = EnumSet.copyOf(emptyEnumSet);
                }
                set.addAll(Arrays.asList(targets));
                return set;
            });
            
            return this;
        }
        
        @SafeVarargs
        public final ActionStateHolder<Handler> restoreAdjustment(final Handler h, RelationType mode, final Handler... targets) {
            final Set<Handler> orig = h.adjustments().get(mode);
            
            if (orig != null) {
                final Set<Handler> a = EnumSet.copyOf(orig);
                final Set<Handler> b = adjustmentMap.get(h).get(mode);
                a.retainAll(Arrays.asList(targets));
                b.addAll(a);
            } else {
                adjustmentMap.get(h).remove(mode);
            }
            
            return this;
        }
        
        public ActionStateHolder<Handler> enableAction(final Handler h, ActionMode mode) {
            enabledActions.get(h).add(mode);
            
            return this;
        }
        
        public ActionStateHolder<Handler> disableAction(final Handler h, ActionMode mode) {
            enabledActions.get(h).remove(mode);
            
            return this;
        }
        
        public ActionStateHolder<Handler> unmapAction(final Handler h, ActionMode mode) {
            actionsMap.get(h).remove(mode);
            
            return this;
        }
        
        public ActionStateHolder<Handler> mapAction(final Handler h, ActionMode mode, EventAction<Handler> remap) {
            actionsMap.get(h).put(mode, remap);
            
            return this;
        }
        
        public ActionStateHolder<Handler> remapAction(final Handler h, ActionMode mode, EventAction<Handler> remap) {
            actionsMap.get(h).replace(mode, remap);
            
            return this;
        }
        
        public ActionStateHolder<Handler> restoreAction(final Handler h, ActionMode mode) {
            final EventAction<Handler> a = h.allActions().get(mode);
            
            if (a != null) {
                actionsMap.get(h).put(mode, a);
            } else {
                actionsMap.get(h).remove(mode);
            }
            
            return this;
        }
    }
    
    final class Relation<Handler extends Enum<Handler> & EventBase<Handler>> {
        public final Handler sourceHandler;
        public final Handler targetHandler;

        public Relation(Handler sourceHandler, Handler targetHandler) {
            this.sourceHandler = sourceHandler;
            this.targetHandler = targetHandler;
        }
    }
}
