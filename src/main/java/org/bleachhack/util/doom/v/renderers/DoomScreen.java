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
package org.bleachhack.util.doom.v.renderers;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

/**
 *
 * @author Good Sign
 */
public enum DoomScreen {
    FG, BG, WS, WE, SB;
    
    @SuppressWarnings("unchecked")
    static <V> Map<DoomScreen, V> mapScreensToBuffers(Class<V> bufferType, int bufferLen) {
        return Arrays.stream(values())
            .collect(() -> new EnumMap<>(DoomScreen.class),
                (map, screen) -> map.put(screen, (V) Array.newInstance(bufferType.getComponentType(), bufferLen)),
                EnumMap::putAll);
    }
}
