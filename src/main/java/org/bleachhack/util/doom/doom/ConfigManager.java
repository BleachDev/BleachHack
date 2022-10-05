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

import org.bleachhack.util.doom.doom.ConfigBase.Files;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;
import org.bleachhack.util.doom.m.Settings;
import static org.bleachhack.util.doom.m.Settings.SETTINGS_MAP;
import org.bleachhack.util.doom.utils.ParseString;
import org.bleachhack.util.doom.utils.QuoteType;
import org.bleachhack.util.doom.utils.ResourceIO;

/**
 * Loads and saves game cfg files
 * 
 * @author Good Sign
 */
public class ConfigManager {
    private static final Pattern SPLITTER = Pattern.compile("[ \t\n\r\f]+");
    
    private final List<Files> configFiles = ConfigBase.getFiles();
    private final EnumMap<Settings, Object> configMap = new EnumMap<>(Settings.class);
    
    public enum UpdateStatus {
        UNCHANGED, UPDATED, INVALID;
    }
    
    public ConfigManager() {
        LoadDefaults();
    }
    
    public UpdateStatus update(final Settings setting, final String value) {
        if (setting.valueType == String.class) {
            return setting.hasChange(!Objects.equals(configMap.put(setting, value), value));
        } else if (setting.valueType == Character.class
            || setting.valueType == Long.class
            || setting.valueType == Integer.class
            || setting.valueType == Boolean.class)
        {
            final Object parse = ParseString.parseString(value);
            if (setting.valueType.isInstance(parse)) {
                return setting.hasChange(!Objects.equals(configMap.put(setting, parse), parse));
            }
        } else if (setting.valueType.getSuperclass() == Enum.class) {
            // Enum search by name
            @SuppressWarnings({ "unchecked", "rawtypes" })
            final Object enumerated = Enum.valueOf((Class<? extends Enum>) setting.valueType, value);
            return setting.hasChange(!Objects.equals(configMap.put(setting, enumerated), enumerated));
        }
        
        return UpdateStatus.INVALID;
    }
    
    public UpdateStatus update(final Settings setting, final Object value) {
        if (setting.valueType == String.class) {
            return setting.hasChange(!Objects.equals(configMap.put(setting, value.toString()), value.toString()));
        }
        
        return UpdateStatus.INVALID;
    }
    
    public UpdateStatus update(final Settings setting, final int value) {
        if (setting.valueType == Integer.class) {
            return setting.hasChange(!Objects.equals(configMap.put(setting, value), value));
        } else if (setting.valueType == String.class) {
            final String valStr = Integer.toString(value);
            return setting.hasChange(!Objects.equals(configMap.put(setting, valStr), valStr));
        } else if (setting.valueType.getSuperclass() == Enum.class) {
            final Object[] enumValues = setting.valueType.getEnumConstants();
            if (value >= 0 && value < enumValues.length) {
                return setting.hasChange(!Objects.equals(configMap.put(setting, enumValues[value]), enumValues[value]));
            }
        }
        
        return UpdateStatus.INVALID;
    }
        
    public UpdateStatus update(final Settings setting, final long value) {
        if (setting.valueType == Long.class) {
            return setting.hasChange(!Objects.equals(configMap.put(setting, value), value));
        } else if (setting.valueType == String.class) {
            final String valStr = Long.toString(value);
            return setting.hasChange(!Objects.equals(configMap.put(setting, valStr), valStr));
        }
        
        return UpdateStatus.INVALID;
    }
        
    public UpdateStatus update(final Settings setting, final double value) {
        if (setting.valueType == Double.class) {
            return setting.hasChange(!Objects.equals(configMap.put(setting, value), value));
        } else if (setting.valueType == String.class) {
            final String valStr = Double.toString(value);
            return setting.hasChange(!Objects.equals(configMap.put(setting, valStr), valStr));
        }
        
        return UpdateStatus.INVALID;
    }
        
    public UpdateStatus update(final Settings setting, final char value) {
        if (setting.valueType == Character.class) {
            return setting.hasChange(!Objects.equals(configMap.put(setting, value), value));
        } else if (setting.valueType == String.class) {
            final String valStr = Character.toString(value);
            return setting.hasChange(!Objects.equals(configMap.put(setting, valStr), valStr));
        }
        
        return UpdateStatus.INVALID;
    }

    public UpdateStatus update(final Settings setting, final boolean value) {
        if (setting.valueType == Boolean.class) {
            return setting.hasChange(!Objects.equals(configMap.put(setting, value), value));
        } else if (setting.valueType == String.class) {
            final String valStr = Boolean.toString(value);
            return setting.hasChange(!Objects.equals(configMap.put(setting, valStr), valStr));
        }
        
        return UpdateStatus.INVALID;
    }

    private String export(final Settings setting) {
        return setting.quoteType().map(qt -> {
            return new StringBuilder()
                .append(setting.name())
                .append("\t\t")
                .append(qt.quoteChar)
                .append(configMap.get(setting))
                .append(qt.quoteChar)
                .toString();
        }).orElseGet(() -> {
            return new StringBuilder()
                .append(setting.name())
                .append("\t\t")
                .append(configMap.get(setting))
                .toString();
        });
    }
    
    public boolean equals(final Settings setting, final Object obj) {
        return obj.equals(configMap.get(setting));
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getValue(final Settings setting, final Class<T> valueType) {
        if (setting.valueType == valueType) {
            return (T) configMap.get(setting);
        } else if (valueType == String.class) {
            return (T) configMap.get(setting).toString();
        } else if (setting.valueType == String.class) {
            if (valueType == Character.class
                || valueType == Long.class
                || valueType == Integer.class
                || valueType == Boolean.class)
            {
                final Object parse = ParseString.parseString(configMap.get(setting).toString());
                if (valueType.isInstance(parse)) {
                    return (T) parse;
                }
            }
        } else if (valueType == Integer.class && setting.valueType.getSuperclass() == Enum.class) {
            return (T) ((Integer) ((Enum<?>) configMap.get(setting)).ordinal());
        }
        
        throw new IllegalArgumentException("Unsupported cast: " + setting.valueType + " to " + valueType);
    }
    
    public void SaveDefaults() {
        SETTINGS_MAP.forEach((file, settings) -> {
            // do not write unless there is changes
            if (!file.changed) {
                return;
            }
            
            // choose existing config file or create one in current working directory
            final ResourceIO rio = file.firstValidPathIO().orElseGet(file::workDirIO);
            final Iterator<Settings> it = settings.stream().sorted(file.comparator).iterator();
            if (rio.writeLines(() -> {
                if (it.hasNext()) {
                    return export(it.next());
                }

                return null;
            }, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.CREATE)) {
                // we wrote successfully - so it will not try to write it again, unless something really change
                file.changed = false;
            }
        });
    }
    
    /**
     * Handles variables and settings from default.cfg and other config files
     * They can be load even earlier then other systems
     */
    private void LoadDefaults() {
        Arrays.stream(Settings.values())
            .forEach(setting -> {
                configMap.put(setting, setting.defaultValue);
            });
        
        System.out.print("M_LoadDefaults: Load system defaults.\n");
        this.configFiles.forEach(file -> {
            final Optional<ResourceIO> maybeRIO = file.firstValidPathIO();
            
            /**
             * Each file successfully read marked as not changed, and as changed - those who don't exist
             * 
             */
            file.changed = !(maybeRIO.isPresent() && readFoundConfig(file, maybeRIO.get()));
        });
        
        // create files who don't exist (it will skip those with changed = false - all who exists)
        SaveDefaults();
    }

    private boolean readFoundConfig(Files file, ResourceIO rio) {
        System.out.print(String.format("M_LoadDefaults: Using config %s.\n", rio.getFileame()));
        if (rio.readLines(line -> {
            final String[] split = SPLITTER.split(line, 2);
            if (split.length < 2) {
                return;
            }

            final String name = split[0];
            try {
                final Settings setting = Settings.valueOf(name);
                final String value = setting.quoteType()
                        .filter(qt -> qt == QuoteType.DOUBLE)
                        .map(qt -> qt.unQuote(split[1]))
                        .orElse(split[1]);

                if (update(setting, value) == UpdateStatus.INVALID) {
                    System.err.printf("WARNING: invalid config value for: %s in %s \n", name, rio.getFileame());
                } else {
                    setting.rebase(file);
                }
            } catch (IllegalArgumentException ex) {}
        })) {
            return true; // successfully read a file
        }
        
        // Something went bad, but this won't destroy successfully read values, though.
        System.err.printf("Can't read the settings file %s\n", rio.getFileame());
        return false;
    }
    
}