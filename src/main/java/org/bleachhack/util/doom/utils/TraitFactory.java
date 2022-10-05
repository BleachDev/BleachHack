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
package org.bleachhack.util.doom.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Logger;
import org.bleachhack.util.doom.mochadoom.Loggers;

/**
 * Purpose of this pattern-interface: store Trait-specific class-wise context objects
 * and be able to get them in constant time.
 * 
 * Simple usage:
 *    You may read the theory below to understand, why and for what reason I wrote
 *    TraitFactory. However, the simplest use is: create an interface extending Trait,
 *    put there static final KeyChain object field, and declare some static ContextKey<>
 *    fields in descending classes and/or interfaces for your objects using KeyChain.newKey.
 *    
 *    Then to initialize everything, just call TraitFactory.build() and the result
 *    will be SharedContext to return on overriden method of Trait.
 * 
 *    TraitFactory.build utilizes (at the instantiation time, not in runtime) some
 *    black reflection magic to free you from need to look for every Trait in line,
 *    and call some registering function to add Objects to Keys in InsertConveyor.
 * 
 * General contract of Trait:
 * 
 * 0. In the constructor of object implementing the subset of Traits based
 *    on this Trait, you must call TraitFactory.build(this, idCapacity);
 *    Implementing this Trait otherwise means nothing.
 * 
 *    The result of TraitFactory.build(this, idCapacity); must be stored
 *    and the overriden method getContext() must return it.
 * 
 *    You can use some static non-final int[] field of deepest Trait dependency
 *    that is incremented by static initialization of all who depend on it,
 *    to determine idCapacity, or just guess big enough on your own. Also you can
 *    use helper object, KeyChain.
 * 
 * 1. In a Trait of your subset, where you want to have some object in context, you
 *    must create static final ContextKey fild. During the static final ContextKey
 *    initialization, you can also hack into incrementing some static non-final
 *    somewhere, to be sure all who do the same produce unique fast ContextKeys.
 * 
 *    You can create several ContextKeys per Trait and store several contexts,
 *    and, if your preferedIds are unique, they will be still instant-fast.
 * 
 * 2. You may want to be sure that all of your interfaces have created their context
 *    objects and put them into the InsertConveyor. To do that, you should have a
 *    method on the class using traits, that will descend into the top level traits,
 *    then lower and lower until the last of the traits.
 * 
 *    ContextKey does not override hashCode and is a final class. So the hashCode()
 *    method will be something like memory pointer, and uniqye per ContextKey.
 *    Default context storage (FactoryContext.class) does not check it until
 *    any new stored ContextKey have preferedId already taken, and reports different
 *    context Object Class<?>. If such happen, all associated contexts are moved
 *    into HashMap and context acquisition will be since significantly slower.
 * 
 *    If your ContextKey does not overlap with another one, access to context Object
 *    would be the most instant of all possible.
 * 
 * 3. In use, call contextGet(ContextKey) or some helper methods to get
 *    the Object from context. Alternatively, you can acquire the SharedContext.
 *    The helper methods are better in case you fear nulls.
 * 
 *    As the SharedContext is Shared, you can use it and objects from it in any
 *    descendants of the trait where you put this object into the context by key.
 *    
 *    If you made sure you never put two Objects of different type with two ContextKeys
 *    with matching preferedIds and Class<?>'es, the cost of get(ContextKey) will be
 *    as negligible as one level of indirection + array access by int.
 */
public class TraitFactory {
    private final static Logger LOGGER = Loggers.getLogger(TraitFactory.class.getName());
    
    public static <T extends Trait> SharedContext build(T traitUser, KeyChain usedChain)
        throws IllegalArgumentException, IllegalAccessException
    {
        return build(traitUser, usedChain.currentCapacity);
    }
    
    public static <T extends Trait> SharedContext build(T traitUser, int idCapacity)
        throws IllegalArgumentException, IllegalAccessException
    {
        final FactoryContext c = new FactoryContext(idCapacity);
        repeatRecursive(traitUser.getClass().getInterfaces(), c);
        return c;
    }

    private static void repeatRecursive(final Class<?>[] traitUserInteraces, final FactoryContext c)
        throws IllegalAccessException, SecurityException, IllegalArgumentException
    {
        for (Class<?> cls: traitUserInteraces) {
            final Field[] declaredFields = cls.getDeclaredFields();
            for (final Field f: declaredFields) {
                final int modifiers = f.getModifiers();
                if (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers)) {
                    final Class<?> fieldClass = f.getType();
                    if (fieldClass == ContextKey.class) {
                        final ContextKey<?> key = ContextKey.class.cast(f.get(null));
                        c.put(key, key.contextConstructor);
                        LOGGER.fine(() -> String.format("%s for %s", c.get(key).getClass(), f.getDeclaringClass()));
                    }
                }
            }
            
            repeatRecursive(cls.getInterfaces(), c);
        }
    }
    
    public interface Trait {
        SharedContext getContext();
        
        default <T> T contextGet(ContextKey<T> key, T defaultValue) {
            final T got = getContext().get(key);
            return got == null ? defaultValue : got;
        }
        
        default <T> T contextRequire(ContextKey<T> key) {
            final T got = getContext().get(key);
            if (got == null) {
                throw defaultException(key).get();
            }
            
            return got;
        }
        
        default <T, E extends Throwable> T contextRequire(ContextKey<T> key, Supplier<E> exceptionSupplier) throws E {
            final T got = getContext().get(key);
            if (got == null) {
                throw exceptionSupplier.get();
            }
            
            return got;
        }
        
        default <T> boolean contextTest(ContextKey<T> key, Predicate<T> predicate) {
            final T got = getContext().get(key);
            return got == null ? false : predicate.test(got);
        }
        
        default <T> void contextWith(ContextKey<T> key, Consumer<T> consumer) {
            final T got = getContext().get(key);
            if (got != null) {
                consumer.accept(got);
            }
        }
        
        default <T, R> R contextMap(ContextKey<T> key, Function<T, R> mapper, R defaultValue) {
            final T got = getContext().get(key);
            if (got != null) {
                return mapper.apply(got);
            } else {
                return defaultValue;
            }
        }
        
        default Supplier<? extends RuntimeException> defaultException(ContextKey<?> key) {
            return () -> new SharedContextException(key, this.getClass());
        }
    }
    
    public final static class ContextKey<T> {
        final Class<? extends Trait> traitClass;
        final int preferredId;
        final Supplier<T> contextConstructor;

        public ContextKey(final Class<? extends Trait> traitClass, int preferredId, Supplier<T> contextConstructor) {
            this.traitClass = traitClass;
            this.preferredId = preferredId;
            this.contextConstructor = contextConstructor;
        }

        @Override
        public String toString() {
            return String.format("context in the Trait %s (preferred id: %d)", traitClass, preferredId);
        }
    }
    
    public final static class KeyChain {
        int currentCapacity;
        
        public <T> ContextKey<T> newKey(final Class<? extends Trait> traitClass, Supplier<T> contextConstructor) {
            return new ContextKey<>(traitClass, currentCapacity++, contextConstructor);
        }
    }

    public interface SharedContext {
        <T> T get(ContextKey<T> key);
    }
    
    final static class FactoryContext implements InsertConveyor, SharedContext {
        private HashMap<ContextKey<?>, Object> traitMap;
        private ContextKey<?>[] keys;
        private Object[] contexts;
        private boolean hasMap = false;

        private FactoryContext(final int idCapacity) {
            this.keys = new ContextKey[idCapacity];
            this.contexts = new Object[idCapacity];
        }

        @Override
        public void put(ContextKey<?> key, Supplier<?> context) {
            if (!hasMap) {
                if (key.preferredId >= 0 && key.preferredId < keys.length) {
                    // return in the case of duplicate initialization of trait
                    if (keys[key.preferredId] == key) {
                        LOGGER.finer(() -> "Already found, skipping: " + key);
                        return;
                    } else if (keys[key.preferredId] == null) {
                        keys[key.preferredId] = key;
                        contexts[key.preferredId] = context.get();
                        return;
                    }
                }
            
                hasMap = true;
                for (int i = 0; i < keys.length; ++i) {
                    traitMap.put(keys[i], contexts[i]);
                }

                keys = null;
                contexts = null;
            }
            
            traitMap.put(key, context.get());
        }

        @Override
        @SuppressWarnings("unchecked")
        public <T> T get(ContextKey<T> key) {
            if (hasMap) {
                return (T) traitMap.get(key);
            } else if (key.preferredId >= 0 && key.preferredId < keys.length) {
                return (T) contexts[key.preferredId];
            }
            
            return null;
        }
    }
    
    public interface InsertConveyor {
        void put(ContextKey<?> key, Supplier<?> context);
        
        default void putObj(ContextKey<?> key, Object context) {
            put(key, () -> context);
        }
    }
    
    private static class SharedContextException extends RuntimeException {
		private static final long serialVersionUID = 5356800492346200764L;

		SharedContextException(ContextKey<?> key, Class<? extends Trait> topLevel) {
            super(String.format("Trait context %s is not initialized when used by %s or"
                + "is dereferencing a null pointer when required to do not",
                key, topLevel));
        }        
    }
    
    private static Type[] getParameterizedTypes(Object object) {
        Type superclassType = object.getClass().getGenericSuperclass();
        if (!ParameterizedType.class.isAssignableFrom(superclassType.getClass())) {
            return null;
        }
        return ((ParameterizedType)superclassType).getActualTypeArguments();
    }
    
    private TraitFactory() {}
}
