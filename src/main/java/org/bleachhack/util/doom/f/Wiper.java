package org.bleachhack.util.doom.f;

import org.bleachhack.util.doom.v.graphics.Wipers;

public interface Wiper {
    boolean ScreenWipe(Wipers.WipeType type, int x, int y, int width, int height, int ticks);

    boolean EndScreen(int x, int y, int width, int height);

    boolean StartScreen(int x, int y, int width, int height);
    
    public enum Wipe implements Wipers.WipeType {
        // simple gradual pixel change for 8-bit only
        // MAES: this transition isn't guaranteed to always terminate
        // see Chocolate Strife develpment. Unused in Doom anyway.
        ColorXForm(
            Wipers.WipeFunc.initColorXForm,
            Wipers.WipeFunc.doColorXForm,
            Wipers.WipeFunc.exitColorXForm
        ),
        // weird screen melt
        Melt(
            Wipers.WipeFunc.initMelt,
            Wipers.WipeFunc.doMelt,
            Wipers.WipeFunc.exitMelt
        ),
        ScaledMelt(
            Wipers.WipeFunc.initScaledMelt,
            Wipers.WipeFunc.doScaledMelt,
            Wipers.WipeFunc.exitMelt
        );

        private final Wipers.WipeFunc initFunc;
        private final Wipers.WipeFunc doFunc;
        private final Wipers.WipeFunc exitFunc;

        @Override
        public Wipers.WipeFunc getDoFunc() {
            return doFunc;
        }

        @Override
        public Wipers.WipeFunc getExitFunc() {
            return exitFunc;
        }

        @Override
        public Wipers.WipeFunc getInitFunc() {
            return initFunc;
        }

        private Wipe(Wipers.WipeFunc initFunc, Wipers.WipeFunc doFunc, Wipers.WipeFunc exitFunc) {
            this.initFunc = initFunc;
            this.doFunc = doFunc;
            this.exitFunc = exitFunc;
        }
    }   
}
