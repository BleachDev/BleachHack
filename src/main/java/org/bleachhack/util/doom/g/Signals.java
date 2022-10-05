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

package org.bleachhack.util.doom.g;

//

import org.bleachhack.util.doom.doom.event_t;
import org.bleachhack.util.doom.doom.evtype_t;
import static java.awt.event.InputEvent.ALT_DOWN_MASK;
import static java.awt.event.InputEvent.CTRL_DOWN_MASK;
import static java.awt.event.InputEvent.META_DOWN_MASK;
import static java.awt.event.InputEvent.SHIFT_DOWN_MASK;
import java.awt.event.KeyEvent;
import static java.awt.event.KeyEvent.*;

public class Signals {
    // 65535 + 4 bytes in memory, acceptable for speed purpose
    private final static byte[] map = new byte[Character.MAX_VALUE];
    // plus 260 bytes in this
    private final static byte[] siblings = new byte[Byte.MAX_VALUE << 1];
    
    public static ScanCode getScanCode(KeyEvent e) {
        final ScanCode ret = ScanCode.v[map[e.getKeyCode()] & 0xFF];

        if (ret.location == e.getKeyLocation()) {
            return ret;
        }

        // try sibling
        final ScanCode sib = ScanCode.v[siblings[ret.ordinal()] & 0xFF];
        if (sib.location == e.getKeyLocation()) {
            return sib;
        }
        
        return ScanCode.SC_NULL;
    }

    private Signals() {}
    
    @FunctionalInterface
    public interface SignalListener {
        void sendEvent(ScanCode sc, int eventType);
    }
    
    /**
     * Maps scan codes for whatever crap we use. They should be system dependent, but
     * it seems I've invented a "keyboard" instead of passing it to real one.
     * This one "keyboard" should be very like old DOS keyboards, and most of the key mappings
     * will be compatible with those what can produce vanilla DOOM. But only most, not all.
     * The order of these is important! Do not move.
     *  - Good Sign 2017/04/19
     */
    public enum ScanCode {
        /*   0 */ SC_NULL,
        /*   1 */ SC_ESCAPE(VK_ESCAPE),
        /*   2 */ SC_1(VK_1),
        /*   3 */ SC_2(VK_2),
        /*   4 */ SC_3(VK_3),
        /*   5 */ SC_4(VK_4),
        /*   6 */ SC_5(VK_5),
        /*   7 */ SC_6(VK_6),
        /*   8 */ SC_7(VK_7),
        /*   9 */ SC_8(VK_8),
        /*  10 */ SC_9(VK_9),
        /*  11 */ SC_0(VK_0),
        /*  12 */ SC_MINUS(VK_MINUS),
        /*  13 */ SC_EQUALS(VK_EQUALS),
        /*  14 */ SC_BACKSPACE(VK_BACK_SPACE),
        /*  15 */ SC_TAB(VK_TAB),
        /*  16 */ SC_Q(VK_Q),
        /*  17 */ SC_W(VK_W),
        /*  18 */ SC_E(VK_E),
        /*  19 */ SC_R(VK_R),
        /*  20 */ SC_T(VK_T),
        /*  21 */ SC_Y(VK_Y),
        /*  22 */ SC_U(VK_U),
        /*  23 */ SC_I(VK_I),
        /*  24 */ SC_O(VK_O),
        /*  25 */ SC_P(VK_P),
        /*  26 */ SC_LBRACE(VK_OPEN_BRACKET),
        /*  27 */ SC_RBRACE(VK_CLOSE_BRACKET),
        /*  28 */ SC_ENTER(VK_ENTER),
        /*  29 */ SC_LCTRL(VK_CONTROL, KEY_LOCATION_LEFT, CTRL_DOWN_MASK),
        /*  30 */ SC_A(VK_A),
        /*  31 */ SC_S(VK_S),
        /*  32 */ SC_D(VK_D),
        /*  33 */ SC_F(VK_F),
        /*  34 */ SC_G(VK_G),
        /*  35 */ SC_H(VK_H),
        /*  36 */ SC_J(VK_J),
        /*  37 */ SC_K(VK_K),
        /*  38 */ SC_L(VK_L),
        /*  39 */ SC_SEMICOLON(VK_SEMICOLON),
        /*  40 */ SC_QUOTE(VK_QUOTE),
        /*  41 */ SC_TILDE(VK_BACK_QUOTE),
        /*  42 */ SC_LSHIFT(VK_SHIFT, KEY_LOCATION_LEFT, SHIFT_DOWN_MASK),
        /*  43 */ SC_BACKSLASH(VK_BACK_SLASH),
        /*  44 */ SC_Z(VK_Z),
        /*  45 */ SC_X(VK_X),
        /*  46 */ SC_C(VK_C),
        /*  47 */ SC_V(VK_V),
        /*  48 */ SC_B(VK_B),
        /*  49 */ SC_N(VK_N),
        /*  50 */ SC_M(VK_M),
        /*  51 */ SC_COMMA(VK_COMMA),
        /*  52 */ SC_PERIOD(VK_PERIOD),
        /*  53 */ SC_SLASH(VK_SLASH),
        /*  54 */ SC_RSHIFT(VK_SHIFT, KEY_LOCATION_RIGHT, SHIFT_DOWN_MASK),
        /*  55 */ SC_NPMULTIPLY(VK_MULTIPLY, KEY_LOCATION_NUMPAD),
        /*  56 */ SC_LALT(VK_ALT, KEY_LOCATION_LEFT, ALT_DOWN_MASK),
        /*  57 */ SC_SPACE(VK_SPACE),
        /*  58 */ SC_CAPSLK(VK_CAPS_LOCK),
        /*  59 */ SC_F1(VK_F1),
        /*  60 */ SC_F2(VK_F2),
        /*  61 */ SC_F3(VK_F3),
        /*  62 */ SC_F4(VK_F4),
        /*  63 */ SC_F5(VK_F5),
        /*  64 */ SC_F6(VK_F6),
        /*  65 */ SC_F7(VK_F7),
        /*  66 */ SC_F8(VK_F8),
        /*  67 */ SC_F9(VK_F9),
        /*  68 */ SC_F10(VK_F10),
        /*  69 */ SC_NUMLK(VK_NUM_LOCK),
        /*  70 */ SC_SCROLLLK(VK_SCROLL_LOCK),
        /*  71 */ SC_NUMKEY7(VK_NUMPAD7, KEY_LOCATION_NUMPAD),
        /*  72 */ SC_NUMKEY8(VK_NUMPAD8, KEY_LOCATION_NUMPAD),
        /*  73 */ SC_NUMKEY9(VK_NUMPAD9, KEY_LOCATION_NUMPAD),
        /*  74 */ SC_NPMINUS(VK_MINUS, KEY_LOCATION_NUMPAD),
        /*  75 */ SC_NUMKEY4(VK_NUMPAD4, KEY_LOCATION_NUMPAD),
        /*  76 */ SC_NUMKEY5(VK_NUMPAD5, KEY_LOCATION_NUMPAD),
        /*  77 */ SC_NUMKEY6(VK_NUMPAD6, KEY_LOCATION_NUMPAD),
        /*  78 */ SC_NPPLUS(VK_PLUS, KEY_LOCATION_NUMPAD),
        /*  79 */ SC_NUMKEY1(VK_NUMPAD1, KEY_LOCATION_NUMPAD),
        /*  80 */ SC_NUMKEY2(VK_NUMPAD2, KEY_LOCATION_NUMPAD),
        /*  81 */ SC_NUMKEY3(VK_NUMPAD3, KEY_LOCATION_NUMPAD),
        /*  82 */ SC_NUMKEY0(VK_NUMPAD0, KEY_LOCATION_NUMPAD),
        /*  83 */ SC_NPDOT(VK_PERIOD, KEY_LOCATION_NUMPAD),
        /*  84
          - 86 */ SC_54, SC_55, SC_56,
        /*  87 */ SC_F11(VK_F11),
        /*  88 */ SC_F12(VK_F12),
        /** Codes higher are DOS-compatible. Codes below are mapped as in Linux **/
        /*  89 */ SC_ROMAN(VK_ROMAN_CHARACTERS),
        /*  90 */ SC_KATAKANA(VK_KATAKANA),
        /*  91 */ SC_HIRAGANA(VK_HIRAGANA),
        /*  92
          - 95 */ SC_5C, SC_5D, SC_5E, SC_5F,
        /*  96 */ SC_NPENTER(VK_ENTER, KEY_LOCATION_NUMPAD),
        /*  97 */ SC_RCTRL(VK_CONTROL, KEY_LOCATION_RIGHT, CTRL_DOWN_MASK),
        /*  98 */ SC_NPSLASH(VK_SLASH, KEY_LOCATION_NUMPAD),
        /*  99 */ SC_PRTSCRN(VK_PRINTSCREEN),
        /* 100 */ SC_RALT(VK_ALT, KEY_LOCATION_RIGHT, ALT_DOWN_MASK),
        /* 101 */ SC_64,
        /* 102 */ SC_HOME(VK_HOME),
        /* 103 */ SC_UP(VK_UP),
        /* 104 */ SC_PGUP(VK_PAGE_UP),
        /* 105 */ SC_LEFT(VK_LEFT),
        /* 106 */ SC_RIGHT(VK_RIGHT),
        /* 107 */ SC_END(VK_END),
        /* 108 */ SC_DOWN(VK_DOWN),
        /* 109 */ SC_PGDOWN(VK_PAGE_DOWN),
        /* 110 */ SC_INSERT(VK_INSERT),
        /* 111 */ SC_DELETE(VK_DELETE),
        /* 112 */ SC_MACRO(VK_DEAD_MACRON),
        /* 113
         - 116 */ SC_71, SC_72, SC_73, SC_74,
        /* 117 */ SC_NPEQUALS(VK_EQUALS, KEY_LOCATION_NUMPAD),
        /* 118 */ SC_76,
        /* 119 */ SC_PAUSE(VK_PAUSE),
        /* 120 */ SC_78,
        /* 121 */ SC_NPCOMMA(VK_COMMA, KEY_LOCATION_NUMPAD),
        /* 122
         - 124 */ SC_7A, SC_7B, SC_7C,
        /* 125 */ SC_LWIN(VK_WINDOWS, KEY_LOCATION_LEFT, META_DOWN_MASK),
        /* 126 */ SC_RWIN(VK_WINDOWS, KEY_LOCATION_RIGHT, META_DOWN_MASK),
        /* 127 */ SC_COMPOSE(VK_COMPOSE),
        /* 128 */ SC_STOP(VK_STOP),
        /* 129 */ SC_AGAIN(VK_AGAIN),
        /* 130 */ SC_PROPS(VK_PROPS),
        /* 131 */ SC_UNDO(VK_UNDO),
        /* 132 */ SC_84,
        /* 133 */ SC_COPY(VK_COPY),
        /* 134 */ SC_86,
        /* 135 */ SC_PASTE(VK_PASTE),
        /* 136 */ SC_FIND(VK_FIND),
        /* 137 */ SC_CUT(VK_CUT),
        /* 138 */ SC_HELP(VK_HELP),
        /* 139 */ SC_MENU(VK_CONTEXT_MENU),
        /** Custom ScanCodes - no keyboard or platform standard **/
        /* 140 */ SC_LMETA(VK_META, KEY_LOCATION_LEFT, META_DOWN_MASK),
        /* 141 */ SC_RMETA(VK_META, KEY_LOCATION_RIGHT, META_DOWN_MASK);
        
        public final char c;
        public final event_t doomEventUp;
        public final event_t doomEventDown;
        
        private final int location;
        private final char virtualKey;
        private final static ScanCode[] v = values();
        
        ScanCode() {
            this.doomEventUp = this.doomEventDown = event_t.EMPTY_EVENT;
            this.location = this.c = this.virtualKey = 0;
        }

        ScanCode(int virtualKey, int... properties) {
            this.location = properties.length > 0 ? properties[0] : KEY_LOCATION_STANDARD;
            this.virtualKey = (char) virtualKey;
            this.doomEventUp = new event_t.keyevent_t(evtype_t.ev_keyup, this);
            this.doomEventDown = new event_t.keyevent_t(evtype_t.ev_keydown, this);
            this.c = Character.toLowerCase(this.virtualKey);
            if (map[virtualKey] != 0) {
                siblings[ordinal()] = map[virtualKey];
            }
            map[virtualKey] = (byte) ordinal();
        }
    }
}