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

import java.util.Optional;

/**
 * @author Good Sign
 */
public enum ParseString {;
    public static Object parseString(String stringSource) {
        final Optional<QuoteType> qt = QuoteType.getQuoteType(stringSource);
        final boolean quoted = qt.isPresent();
        if (quoted) {
            stringSource = qt.get().unQuote(stringSource);
        }
        
        if (quoted && stringSource.length() == 1) {
            final Character test = stringSource.charAt(0);
            if (test >= 0 && test < 255) {
                return test;
            }
        }
        
        Optional<? extends Object> ret = checkInt(stringSource);
        if (!ret.isPresent()) {
            ret = checkDouble(stringSource);
            if (!ret.isPresent()) {
                ret = checkBoolean(stringSource);
                if (!ret.isPresent()) {
                    return stringSource;
                }
            }
        }
        
        return ret.get();
    }
    
    public static Optional<Object> checkInt(final String stringSource) {
        Optional<Object> ret;
        try {
            long longRet = Long.parseLong(stringSource);
            return longRet < Integer.MAX_VALUE
                ? Optional.of((int) longRet)
                : Optional.of(longRet);
        } catch (NumberFormatException e) {}

        try {
            long longRet = Long.decode(stringSource);
            return longRet < Integer.MAX_VALUE
                ? Optional.of((int) longRet)
                : Optional.of(longRet);
        } catch (NumberFormatException e) {}

        return Optional.empty();
    }
    
    public static Optional<Double> checkDouble(final String stringSource) {
        try {
            return Optional.of(Double.parseDouble(stringSource));
        } catch (NumberFormatException e) {}

        return Optional.empty();
    }
    
    public static Optional<Boolean> checkBoolean(final String stringSource) {
        try {
            return Optional.of(Boolean.parseBoolean(stringSource));
        } catch (NumberFormatException e) {}
        
        if ("false".compareToIgnoreCase(stringSource) == 0) {
            return Optional.of(Boolean.FALSE);
        }

        return Optional.empty();
    }
}