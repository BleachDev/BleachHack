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
public enum QuoteType {
    SINGLE('\''), DOUBLE('"');
    public final char quoteChar;

    QuoteType(final char quoteChar) {
        this.quoteChar = quoteChar;
    }

    public boolean isQuoted(final String s) {
        return C2JUtils.isQuoted(s, quoteChar);
    }

    public String unQuote(final String s) {
        return C2JUtils.unquote(s, quoteChar);
    }
    
    public static Optional<QuoteType> getQuoteType(final String stringSource) {
        if (stringSource.length() > 2) {
            for (final QuoteType type: QuoteType.values()) {
                if (type.isQuoted(stringSource)) {
                    return Optional.of(type);
                }
            }
        }

        return Optional.empty();
    }
}