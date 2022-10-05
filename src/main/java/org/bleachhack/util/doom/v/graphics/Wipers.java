/**
 * Copyright (C) 1993-1996 Id Software, Inc.
 * from f_wipe.c
 * 
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

package org.bleachhack.util.doom.v.graphics;

import org.bleachhack.util.doom.f.Wiper;
import java.lang.reflect.Array;
import org.bleachhack.util.doom.m.IRandom;
import org.bleachhack.util.doom.utils.GenericCopy;
import org.bleachhack.util.doom.v.graphics.Wipers.WipeFunc.WF;

/**
 * SCREEN WIPE PACKAGE
 */
public class Wipers implements ColorTransform, Melt {
    private static final Wipers instance = new Wipers();
    
    /**
     * They are repeated thrice for a reason - they are overloads with different arguments
     * - Good Sign 2017/04/06
     * 
     * ASS-WIPING functions
     */
    public enum WipeFunc {
        doColorXFormB(instance::colorTransformB, byte[].class),
        doColorXFormS(instance::colorTransformS, short[].class),
        doColorXFormI(instance::colorTransformI, int[].class),
        
        initColorXForm(instance::initTransform),
        doColorXForm(doColorXFormB, doColorXFormS, doColorXFormI),
        exitColorXForm(w -> false),

        initScaledMelt(instance::initMeltScaled),
        doScaledMelt(instance::doMeltScaled),

        initMelt(instance::initMelt),
        doMelt(instance::doMelt),
        exitMelt(instance::exitMelt);
        
        private final Class<?> supportFor;
        private final WF<?> func;
        
        WipeFunc(WF<?> func) {
            this.supportFor = null;
            this.func = func;
        }
        
        <V> WipeFunc(WF<V> func, Class<V> supportFor) {
            this.supportFor = supportFor;
            this.func = func;
        }
        
        WipeFunc(final WipeFunc... wf) {
            this.supportFor = null;
            this.func = wipeChoice(wf);
        }
        
        private static <V> WF<V> wipeChoice(final WipeFunc[] wf) {
        	return (WiperImpl<V, ?> wiper) -> {
        		for (int i = 0; i < wf.length; ++i) {
        			if (wiper.bufferType == wf[i].supportFor) {
        				@SuppressWarnings("unchecked") // checked
						final WF<V> supported = (WF<V>) wf[i].func;
        				return supported.invoke(wiper);
        			}
        		}
        		
        		throw new UnsupportedOperationException("Do not have support for: " + wiper.bufferType);
        	};
        }
        
        interface WF<V> { public boolean invoke(WiperImpl<V, ?> wiper); }
    }
    
    public static <V, E extends Enum<E>> Wiper createWiper(IRandom rnd, Screens<V, E> screens, E ws, E we, E ms) {
        return new WiperImpl<>(rnd, screens, ws, we, ms);
    }
    
    protected final static class WiperImpl<V, E extends Enum<E>> implements Wiper {
        private final Relocation relocation = new Relocation(0, 0, 1);
        final IRandom random;
        final Screens<V, E> screens;
        final Class<?> bufferType;
        final V wipeStartScr;
        final V wipeEndScr;
        final V wipeScr;
        final int screenWidth;
        final int screenHeight;
        final int dupx;
        final int dupy;
        final int scaled_16;
        final int scaled_8;
        int[] y;
        int ticks;

        /** when false, stop the wipe */
        volatile boolean go = false;

        private WiperImpl(IRandom RND, Screens<V, E> screens, E wipeStartScreen, E wipeEndScreen, E mainScreen) {
            this.random = RND;
            this.wipeStartScr = screens.getScreen(wipeStartScreen);
            this.wipeEndScr = screens.getScreen(wipeEndScreen);
            this.wipeScr = screens.getScreen(mainScreen);
            this.bufferType = this.wipeScr.getClass();
            this.screens = screens;
            this.screenWidth = screens.getScreenWidth();
            this.screenHeight = screens.getScreenHeight();
            this.dupx = screens.getScalingX();
            this.dupy = screens.getScalingY();
            this.scaled_16 = dupy << 4;
            this.scaled_8 = dupy << 3;
        }
        
        void startToScreen(int source, int destination) {
            screens.screenCopy(wipeStartScr, wipeScr, relocation.retarget(source, destination));
        }

        void endToScreen(int source, int destination) {
            screens.screenCopy(wipeEndScr, wipeScr, relocation.retarget(source, destination));
        }

        /**
         * Sets "from" screen and stores it in "screen 2"
         */
        @Override
        public boolean StartScreen(int x, int y, int width, int height) {
            GenericCopy.memcpy(wipeScr, 0, wipeStartScr, 0, Array.getLength(wipeStartScr));
            return false;
        }

        /**
         * Sets "to" screen and stores it to "screen 3"
         */
        @Override
        public boolean EndScreen(int x, int y, int width, int height) {
            // Set end screen to "screen 3" and copy visible screen to it.
            GenericCopy.memcpy(wipeScr, 0, wipeEndScr, 0, Array.getLength(wipeEndScr));
            // Restore starting screen.
            GenericCopy.memcpy(wipeStartScr, 0, wipeScr, 0, Array.getLength(wipeScr));
            return false;
        }
        
        @SuppressWarnings("unchecked")
		private boolean invokeCheckedFunc(WipeFunc f) {
        	return ((WF<V>) f.func).invoke(this);
        }

        @Override
        public boolean ScreenWipe(WipeType type, int x, int y, int width, int height, int ticks) {
            boolean rc;

            //System.out.println("Ticks do "+ticks);
            this.ticks = ticks;
            
            // initial stuff
            if (!go) {
                go = true;
                //wipe_scr = new byte[width*height]; // DEBUG
                // HOW'S THAT FOR A FUNCTION POINTER, BIATCH?!
                invokeCheckedFunc(type.getInitFunc());
            }

            // do a piece of wipe-in
            rc = invokeCheckedFunc(type.getDoFunc());
            // V.DrawBlock(x, y, 0, width, height, wipe_scr); // DEBUG

            // final stuff
            if (rc) {
                go = false;
                invokeCheckedFunc(type.getExitFunc());
            }

            return !go;
        }
    }
    
    public interface WipeType {
        WipeFunc getInitFunc();
        WipeFunc getDoFunc();
        WipeFunc getExitFunc();
    }
    
    private Wipers() {}
}
