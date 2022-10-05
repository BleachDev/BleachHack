package org.bleachhack.util.doom.st;

import org.bleachhack.util.doom.doom.DoomMain;

public abstract class AbstractStatusBar implements IDoomStatusBar {
    protected final DoomMain<?, ?> DOOM;

    public AbstractStatusBar(DoomMain<?, ?> DOOM) {
        this.DOOM = DOOM;
    }
}
