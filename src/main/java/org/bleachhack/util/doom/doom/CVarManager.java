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
package org.bleachhack.util.doom.doom;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.logging.Level;
import org.bleachhack.util.doom.mochadoom.Loggers;
import org.bleachhack.util.doom.utils.ResourceIO;

/**
 * New, object-oriented Console Variable Manager
 * Usage:
 * 1. Define CVars in CommandVariable Enum
 * 2. In program entry main function, create any ICommandLineManager and pass an instance to create CVarManager
 * 3. Use methods bool, present, get and with to check or get CVars
 * 
 * @author Good Sign
 */
public class CVarManager {
    
    private final EnumMap<CommandVariable, Object[]> cVarMap = new EnumMap<>(CommandVariable.class);

    public CVarManager(final List<String> commandList) {
        System.out.println(processAllArgs(commandList) + " command-line variables");
    }
    
    /**
     * Checks that CVar of switch-type is passed as Command Line Argument
     * @param cv
     * @return boolean
     */
    public boolean bool(final CommandVariable cv) {
        return cv.getType() == CommandVariable.Type.SWITCH && cVarMap.containsKey(cv);
    }
    
    /**
     * Checks that CVar of any type is passed as Command Line Argument with proper value(s)
     * @param cv
     * @return boolean
     */
    public boolean present(final CommandVariable cv) {
        return cVarMap.get(cv) != null;
    }
    
    /**
     * Checks that CVar of any type is passed as Command Line Argument
     * @param cv
     * @return boolean
     */
    public boolean specified(final CommandVariable cv) {
        return cVarMap.containsKey(cv);
    }
    
    /**
     * Gets an Optional with or without a value of CVar argument at position
     * @param cv
     * @return Optional
     */
    public <T> Optional<T> get(final CommandVariable cv, final Class<T> itemType, final int position) {        
        if (cv.arguments[position] == itemType) {
            if (!cVarMap.containsKey(cv)) {
                return Optional.empty();
            }
            
            @SuppressWarnings("unchecked")
            final T ret = (T) cVarMap.get(cv)[position];
            return Optional.ofNullable(ret);
        }
        
        throw new IllegalArgumentException("CVar argument at position " + position + " is not of class " + itemType.getName());
    }
    
    /**
     * Tries to apply a CVar argument at position to the consuming function
     * The magic is that you declare a lambda function or reference some method
     * and the type of object will be automatically picked from what you hinted
     * 
     * i.e. (String s) -> System.out.println(s) will try to get string,
     * (Object o) -> map.put(key, o) or o -> list.add(o.hashCode()) will try to get objects
     * and you dont have to specify class
     * 
     * The drawback is the ClassCastException will be thrown if the value is neither
     * what you expected, nor a subclass of it
     * 
     * @param cv
     * @param position
     * @param action
     * @return false if CVar is not passed as Command Line Argument or the consuming action is incompatible
     */
    public <T> boolean with(final CommandVariable cv, final int position, final Consumer<T> action) {
        try {
            @SuppressWarnings("unchecked")
            final Object[] mapped = cVarMap.get(cv);
            if (mapped == null) {
                return false;
            }
            
            @SuppressWarnings("unchecked")
            final T item = (T) mapped[position];
            action.accept(item);
            return true;
        } catch (ClassCastException ex) {
            return false;
        }
    }
    
    /**
     * Tries to replace the CVar argument if already present or add it along with CVar
     * @param cv
     * @param value
     * @param position
     * @return false if invalid position or value class
     */
    public <T> boolean override(final CommandVariable cv, final T value, final int position) {
        if (position < 0 || position >= cv.arguments.length) {
            return false;
        }
        
        if (!cv.arguments[position].isInstance(value)) {
            return false;
        }
        
        cVarMap.compute(cv, (key, array) -> {
            if (array == null) {
                array = new Object[cv.arguments.length];
            }
            
            array[position] = value;
            return array;
        });
        
        return true;
    }
    
    private void readResponseFile(final String filename) {
        final ResponseReader r = new ResponseReader();
        if (new ResourceIO(filename).readLines(r)) {
            System.out.println(String.format("Found response file %s, read %d command line variables", filename, r.cVarCount));
        } else {
            System.out.println(String.format("No such response file %s!", filename));
            System.exit(1);
        }
    }

    private int processAllArgs(final List<String> commandList) {
        int cVarCount = 0, position = 0;
        
        for (
            final int limit = commandList.size();
            limit > position;
            position = processCVar(commandList, position),
            ++position,
            ++cVarCount
        ) {}
        
        return cVarCount;
    }

    private int processCVar(final List<String> commandList, int position) {
        final String arg = commandList.get(position);
        
        if (!isCommandArgument(arg)) {
            return position;
        }
        
        final char cVarPrefix = arg.charAt(0);
        final String cVarName = arg.substring(1);
        
        if (cVarPrefix == '@') {
            readResponseFile(cVarName);
            return position;
        } else try {
            final CommandVariable cVar = CommandVariable.valueOf(cVarName.toUpperCase());
            if (cVar.prefix == cVarPrefix) {
                switch(cVar.getType()) {
                    case PARAMETER:
                        cVarMap.put(cVar, null);
                    case VARARG:
                        return processCVarSubArgs(commandList, position, cVar);
                    case SWITCH:
                    default:
                        cVarMap.put(cVar, null);
                        return position;
                }
            }
        } catch (IllegalArgumentException ex) {} // ignore
        return position;
    }
    
    private int processCVarSubArgs(final List<String> commandList, int position, final CommandVariable cVar) {
        final Object[] cVarMappings = new Object[cVar.arguments.length];
        for (int j = 0; j < cVar.arguments.length; ++j) {
            if (cVar.arguments[j].isArray()) {
                final Class<?> elementClass = cVar.arguments[j].getComponentType();
                final Object[] mapping = processVarArg(elementClass, commandList, position + 1);
                cVarMappings[j] = mapping;
                position += mapping.length;
                if (mapping.length == 0) {
                    break;
                }
            } else if ((cVarMappings[j] = processValue(cVar.arguments[j], commandList, position + 1)) == null) {
                break;
            } else {
                ++position;
            }
        }
        cVarMap.put(cVar, cVarMappings);
        return position;
    }

    private Object processValue(final Class<?> elementClass, final List<String> commandList, int position) {
        if (position < commandList.size()) {
            final String arg = commandList.get(position);
            if (!isCommandArgument(arg)) {
                return formatArgValue(elementClass, arg);
            }
        }
        return null;
    }

    private Object[] processVarArg(final Class<?> elementClass, final List<String> commandList, int position) {
        final List<Object> list = new ArrayList<>();
        for (Object value; (value = processValue(elementClass, commandList, position)) != null; ++position) {
            list.add(value);
        }
        // as String[] instanceof Object[], upcast
        return list.toArray((Object[]) Array.newInstance(elementClass, list.size()));
    }

    private Object formatArgValue(final Class<?> format, final String arg) {
        if (format == Integer.class) {
            try {
                return Integer.parseInt(arg);
            } catch (NumberFormatException ex) {
                Loggers.getLogger(CommandVariable.class.getName()).log(Level.WARNING, null, ex);
                return null;
            }
        } else if (format == String.class) {
            return arg;
        }
        try {
            return format.getDeclaredConstructor(String.class).newInstance(arg);
        } catch (
            NoSuchMethodException
            | SecurityException
            | InstantiationException
            | IllegalAccessException
            | IllegalArgumentException
            | InvocationTargetException ex
        ) {
            Loggers.getLogger(CommandVariable.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }

    private boolean isCommandArgument(final String arg) {
        if (arg.length() < CommandVariable.MIN_CVAR_LENGTH)
            return false;
        
        switch (arg.charAt(0)) {
            case '-':
            case '+':
            case '@':
                return true;
        }
        
        return false;
    }
    
    private class ResponseReader implements Consumer<String> {
        int cVarCount = 0;

        @Override
        public void accept(final String line) {
            cVarCount += processAllArgs(Arrays.asList(line.split(" ")));
        }
    }
}
