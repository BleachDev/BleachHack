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

package org.bleachhack.util.doom.v.renderers;

import org.bleachhack.util.doom.doom.CommandVariable;
import org.bleachhack.util.doom.mochadoom.Engine;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.image.ColorModel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import org.bleachhack.util.doom.m.MenuMisc;
import org.bleachhack.util.doom.m.Settings;

/**
 * Base for HiColor and TrueColor parallel renderers
 * 
 * @author Good Sign
 * @author velktron
 */
abstract class SoftwareParallelVideoRenderer<T, V> extends SoftwareGraphicsSystem<T, V> {
    // How many threads it will use, but default it uses all avalable cores
    private static final int[] EMPTY_INT_PALETTED_BLOCK = new int[0];
    private static final short[] EMPTY_SHORT_PALETTED_BLOCK = new short[0];
    protected static final int PARALLELISM = Engine.getConfig().getValue(Settings.parallelism_realcolor_tint, Integer.class);
    protected static final GraphicsConfiguration GRAPHICS_CONF = GraphicsEnvironment.getLocalGraphicsEnvironment()
            .getDefaultScreenDevice().getDefaultConfiguration();
    
    protected final boolean GRAYPAL_SET = Engine.getCVM().bool(CommandVariable.GREYPAL);

    /**
     * It will render much faster on machines with display already in HiColor mode
     * Maybe even some acceleration will be possible
     */
    static boolean checkConfigurationHicolor() {
        final ColorModel cm = GRAPHICS_CONF.getColorModel();
        final int cps = cm.getNumComponents();
        return cps == 3 && cm.getComponentSize(0) == 5 && cm.getComponentSize(1) == 5 && cm.getComponentSize(2) == 5;
    }

    /**
     * It will render much faster on machines with display already in TrueColor mode
     * Maybe even some acceleration will be possible
     */
    static boolean checkConfigurationTruecolor() {
        final ColorModel cm = GRAPHICS_CONF.getColorModel();
        final int cps = cm.getNumComponents();
        return cps == 3 && cm.getComponentSize(0) == 8 && cm.getComponentSize(1) == 8 && cm.getComponentSize(2) == 8;
    }
    
    /**
     * We do not need to clear caches anymore - pallettes are applied on post-process
     *  - Good Sign 2017/04/12
     * 
     * MEGA HACK FOR SUPER-8BIT MODES
     */
    protected final HashMap<Integer, V> colcache = new HashMap<>();

    // Threads stuff
    protected final Runnable[] paletteThreads = new Runnable[PARALLELISM];
    protected final Executor executor = Executors.newFixedThreadPool(PARALLELISM);
    protected final CyclicBarrier updateBarrier = new CyclicBarrier(PARALLELISM + 1);

    SoftwareParallelVideoRenderer(RendererFactory.WithWadLoader<T, V> rf, Class<V> bufferType) {
        super(rf, bufferType);
    }

    abstract void doWriteScreen();

    @Override
    public boolean writeScreenShot(String name, DoomScreen screen) {
        // munge planar buffer to linear
        //DOOM.videoInterface.ReadScreen(screens[screen.ordinal()]);
        V screenBuffer = screens.get(screen);
        if (screenBuffer.getClass() == short[].class) {
            MenuMisc.WritePNGfile(name, (short[]) screenBuffer, width, height);
        } else {
            MenuMisc.WritePNGfile(name, (int[]) screenBuffer, width, height);
        }
        return true;
    }

    /**
     * Used to decode textures, patches, etc... It converts to the proper palette,
     * but does not apply tinting or gamma - yet
     */
    @Override
    @SuppressWarnings(value = "unchecked")
    public V convertPalettedBlock(byte... data) {
        final boolean isShort = bufferType == short[].class;
        /**
         * We certainly do not need to cache neither single color value, nor empty data
         *  - Good Sign 2017/04/09
         */
        if (data.length > 1) {
            if (isShort) {
                return colcache.computeIfAbsent(Arrays.hashCode(data), (h) -> {
                    //System.out.printf("Generated cache for %d\n",data.hashCode());
                    short[] stuff = new short[data.length];
                    for (int i = 0; i < data.length; i++) {
                        stuff[i] = (short) getBaseColor(data[i]);
                    }
                    return (V) stuff;
                });
            } else {
                return colcache.computeIfAbsent(Arrays.hashCode(data), (h) -> {
                    //System.out.printf("Generated cache for %d\n",data.hashCode());
                    int[] stuff = new int[data.length];
                    for (int i = 0; i < data.length; i++) {
                        stuff[i] = getBaseColor(data[i]);
                    }
                    return (V) stuff;
                });
            }
        } else if (data.length == 0) {
            return (V) (isShort ? EMPTY_SHORT_PALETTED_BLOCK : EMPTY_INT_PALETTED_BLOCK);
        }
        return (V) (isShort ? new short[]{(short) getBaseColor(data[0])} : new int[]{getBaseColor(data[0])});
    }
}
