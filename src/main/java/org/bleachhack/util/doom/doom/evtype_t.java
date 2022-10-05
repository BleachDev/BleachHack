package org.bleachhack.util.doom.doom;

/** The possible events according to Doom */

public enum evtype_t {
        ev_null,
        ev_keydown,
        ev_keyup,
        ev_mouse,
        ev_joystick,
        ev_mousewheel, // extension
        ev_clear // Forcibly clear all button input (e.g. when losing focus)
    };
