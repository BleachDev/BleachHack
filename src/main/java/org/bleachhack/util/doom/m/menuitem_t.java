package org.bleachhack.util.doom.m;

import org.bleachhack.util.doom.g.Signals;

public class menuitem_t {

    public menuitem_t(int status, String name, MenuRoutine routine, Signals.ScanCode alphaKey) {
        this.status = status;
        this.name = name;
        this.routine = routine;
        this.alphaKey = alphaKey;
    }

    public menuitem_t(int status, String name, MenuRoutine routine) {
        this.status = status;
        this.name = name;
        this.routine = routine;
    }

    /**
     * 0 = no cursor here, 1 = ok, 2 = arrows ok
     */
    int status;

    String name;

    // choice = menu item #.
    // if status = 2,
    //   choice=0:leftarrow,1:rightarrow
    // MAES: OK... to probably we need some sort of "MenuRoutine" class for this one.
    // void	(*routine)(int choice);
    MenuRoutine routine;

    /**
     * hotkey in menu
     */
    Signals.ScanCode alphaKey;
}
