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

import java.awt.Graphics2D;
import java.awt.Image;
import static java.awt.Transparency.*;
import java.awt.image.BufferedImage;
import static java.awt.image.BufferedImage.TYPE_INT_ARGB;
import static java.awt.image.BufferedImage.TYPE_INT_RGB;
import java.awt.image.DataBufferInt;
import java.awt.image.VolatileImage;
import java.util.concurrent.BrokenBarrierException;
import java.util.logging.Level;
import org.bleachhack.util.doom.mochadoom.Loggers;
import org.bleachhack.util.doom.v.tables.BlurryTable;
import org.bleachhack.util.doom.v.tables.ColorTint;
import static org.bleachhack.util.doom.v.tables.ColorTint.GREY_TINTS;
import static org.bleachhack.util.doom.v.tables.ColorTint.NORMAL_TINTS;

/**
 * Merged with ParallelTruecolorRenderer as it fixed both bugs of parallel and single-core versions
 * Now only parallel BufferedRenderer32 available in TrueColor mode because
 * single-core post-processing in software is too slow, and is the only way to apply tints and gamma properly
 * Parallelization of post-processing is so effective, that on my 4-core i7 it gives me at least equal FPS with
 * indexed renderer in no-JIT configuration of Java, and with JIT compiler it gives me much more FPS than indexed.
 * So, you will probably want this renderer if you have at least Core2Duo processor
 * - Good Sign 2017/04/12
 */
class BufferedRenderer32 extends SoftwareParallelVideoRenderer<byte[], int[]> {
    protected final int[] raster;
    
    // VolatileImage speeds up delivery to VRAM - it is 30-40 fps faster then directly rendering BufferedImage
    protected VolatileImage screen;
    
    // indicated whether machine display in the same mode as this renderer
    protected final boolean compatible = checkConfigurationTruecolor();
    protected final int transparency;
    protected final BlurryTable blurryTable;
    
    /**
     * This implementation will "tie" a BufferedImage to the underlying byte raster.
     *
     * NOTE: this relies on the ability to "tap" into a BufferedImage's backing array, in order to have fast writes
     * without setPixel/getPixel. If that is not possible, then we'll need to use a special renderer.
     */
    BufferedRenderer32(RendererFactory.WithWadLoader<byte[], int[]> rf) {
        super(rf, int[].class);
        
        /**
         * Try to create as accelerated Images as possible - these would not lose
         * more performance from attempt (in contrast to 16-bit ones)
         */
        screen = GRAPHICS_CONF.createCompatibleVolatileImage(width, height);
        transparency = rf.getBppMode().transparency;

        /**
         * It is very probably that you have 32-bit display mode, so high chance of success,
         * and if you have, for example, 24-bit mode, the TYPE_INT_RGB BufferedImage will
         * still get accelerated
         */
        currentscreen = compatible
            ? GRAPHICS_CONF.createCompatibleImage(width, height, transparency)
            : new BufferedImage(width, height, transparency == TRANSLUCENT ? TYPE_INT_ARGB : TYPE_INT_RGB);
        currentscreen.setAccelerationPriority(1.0f);
        
        // extract raster from the created image
        raster = ((DataBufferInt)((BufferedImage) currentscreen).getRaster().getDataBuffer()).getData();
        
        blurryTable = new BlurryTable(liteColorMaps);
        
        /**
         * Create postprocess worker threads
         * 320 is dividable by 16, so any scale of it would
         * TODO: support for custom resolutions?
         */
        final int len = raster.length, chunk = len / PARALLELISM;
        for (int i = 0; i < PARALLELISM; i++) {
            paletteThreads[i] = new IntPaletteThread(i * chunk, (i + 1) * chunk);
        }
    }

    /**
     * This method is accessed by AWTDoom to render the screen
     * As we use VolatileImage that can lose its contents, it must have special care.
     * doWriteScreen is called in the moment, when the VolatileImage is ready and
     * we can copy to it and post-process
     */
    @Override
    public Image getScreenImage() {
        do {
            if (screen.validate(GRAPHICS_CONF) == VolatileImage.IMAGE_INCOMPATIBLE) {
                screen.flush();
                // old vImg doesn't work with new GraphicsConfig; re-create it
                screen = GRAPHICS_CONF.createCompatibleVolatileImage(width, height);
            }
            doWriteScreen();
        } while (screen.contentsLost());
        return screen;
    }

    @Override
    void doWriteScreen() {
        for (int i = 0; i < PARALLELISM; i++) {
            executor.execute(paletteThreads[i]);
        }
        try {
            updateBarrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            Loggers.getLogger(BufferedRenderer32.class.getName()).log(Level.SEVERE, e, null);
        }

        final Graphics2D g = screen.createGraphics();
        g.drawImage(currentscreen, 0, 0, null);
        g.dispose();
    }

    /**
     * Returns pure color without tinting and gamma
     */
    @Override
    public int getBaseColor(byte color) {
        return palette[color & 0xFF];
    }

    @Override
    public BlurryTable getBlurryTable() {
        return blurryTable;
    }

    /**
     * Looks monstrous. Works swiss.
     * - Good Sign 2017/04/12
     */
    private class IntPaletteThread implements Runnable {
        private final int[] FG;
        private final int start;
        private final int stop;

        IntPaletteThread(int start, int stop) {
            this.start = start;
            this.stop = stop;
            this.FG = screens.get(DoomScreen.FG);
        }

        /**
         * BFG-9000. Definitely not the pesky pistol in the Indexed renderer
         */
        @Override
        public void run() {
            final ColorTint t = (GRAYPAL_SET ? GREY_TINTS : NORMAL_TINTS).get(usepalette);
            final byte[] LUT_R = t.LUT_r8[usegamma];
            final byte[] LUT_G = t.LUT_g8[usegamma];
            final byte[] LUT_B = t.LUT_b8[usegamma];
            for (int i = start; i < stop;) {
                raster[i] = (FG[i] & 0xFF000000) + ((LUT_R[(FG[i] >> 16) & 0xFF] & 0xFF) << 16) + ((LUT_G[(FG[i] >> 8) & 0xFF] & 0xFF) << 8) + (LUT_B[FG[i++] & 0xFF] & 0xFF);
                raster[i] = (FG[i] & 0xFF000000) + ((LUT_R[(FG[i] >> 16) & 0xFF] & 0xFF) << 16) + ((LUT_G[(FG[i] >> 8) & 0xFF] & 0xFF) << 8) + (LUT_B[FG[i++] & 0xFF] & 0xFF);
                raster[i] = (FG[i] & 0xFF000000) + ((LUT_R[(FG[i] >> 16) & 0xFF] & 0xFF) << 16) + ((LUT_G[(FG[i] >> 8) & 0xFF] & 0xFF) << 8) + (LUT_B[FG[i++] & 0xFF] & 0xFF);
                raster[i] = (FG[i] & 0xFF000000) + ((LUT_R[(FG[i] >> 16) & 0xFF] & 0xFF) << 16) + ((LUT_G[(FG[i] >> 8) & 0xFF] & 0xFF) << 8) + (LUT_B[FG[i++] & 0xFF] & 0xFF);
                raster[i] = (FG[i] & 0xFF000000) + ((LUT_R[(FG[i] >> 16) & 0xFF] & 0xFF) << 16) + ((LUT_G[(FG[i] >> 8) & 0xFF] & 0xFF) << 8) + (LUT_B[FG[i++] & 0xFF] & 0xFF);
                raster[i] = (FG[i] & 0xFF000000) + ((LUT_R[(FG[i] >> 16) & 0xFF] & 0xFF) << 16) + ((LUT_G[(FG[i] >> 8) & 0xFF] & 0xFF) << 8) + (LUT_B[FG[i++] & 0xFF] & 0xFF);
                raster[i] = (FG[i] & 0xFF000000) + ((LUT_R[(FG[i] >> 16) & 0xFF] & 0xFF) << 16) + ((LUT_G[(FG[i] >> 8) & 0xFF] & 0xFF) << 8) + (LUT_B[FG[i++] & 0xFF] & 0xFF);
                raster[i] = (FG[i] & 0xFF000000) + ((LUT_R[(FG[i] >> 16) & 0xFF] & 0xFF) << 16) + ((LUT_G[(FG[i] >> 8) & 0xFF] & 0xFF) << 8) + (LUT_B[FG[i++] & 0xFF] & 0xFF);
                raster[i] = (FG[i] & 0xFF000000) + ((LUT_R[(FG[i] >> 16) & 0xFF] & 0xFF) << 16) + ((LUT_G[(FG[i] >> 8) & 0xFF] & 0xFF) << 8) + (LUT_B[FG[i++] & 0xFF] & 0xFF);
                raster[i] = (FG[i] & 0xFF000000) + ((LUT_R[(FG[i] >> 16) & 0xFF] & 0xFF) << 16) + ((LUT_G[(FG[i] >> 8) & 0xFF] & 0xFF) << 8) + (LUT_B[FG[i++] & 0xFF] & 0xFF);
                raster[i] = (FG[i] & 0xFF000000) + ((LUT_R[(FG[i] >> 16) & 0xFF] & 0xFF) << 16) + ((LUT_G[(FG[i] >> 8) & 0xFF] & 0xFF) << 8) + (LUT_B[FG[i++] & 0xFF] & 0xFF);
                raster[i] = (FG[i] & 0xFF000000) + ((LUT_R[(FG[i] >> 16) & 0xFF] & 0xFF) << 16) + ((LUT_G[(FG[i] >> 8) & 0xFF] & 0xFF) << 8) + (LUT_B[FG[i++] & 0xFF] & 0xFF);
                raster[i] = (FG[i] & 0xFF000000) + ((LUT_R[(FG[i] >> 16) & 0xFF] & 0xFF) << 16) + ((LUT_G[(FG[i] >> 8) & 0xFF] & 0xFF) << 8) + (LUT_B[FG[i++] & 0xFF] & 0xFF);
                raster[i] = (FG[i] & 0xFF000000) + ((LUT_R[(FG[i] >> 16) & 0xFF] & 0xFF) << 16) + ((LUT_G[(FG[i] >> 8) & 0xFF] & 0xFF) << 8) + (LUT_B[FG[i++] & 0xFF] & 0xFF);
                raster[i] = (FG[i] & 0xFF000000) + ((LUT_R[(FG[i] >> 16) & 0xFF] & 0xFF) << 16) + ((LUT_G[(FG[i] >> 8) & 0xFF] & 0xFF) << 8) + (LUT_B[FG[i++] & 0xFF] & 0xFF);
                raster[i] = (FG[i] & 0xFF000000) + ((LUT_R[(FG[i] >> 16) & 0xFF] & 0xFF) << 16) + ((LUT_G[(FG[i] >> 8) & 0xFF] & 0xFF) << 8) + (LUT_B[FG[i++] & 0xFF] & 0xFF);
            }
            try {
                updateBarrier.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                Loggers.getLogger(BufferedRenderer32.class.getName()).log(Level.WARNING, e, null);
            }
        }
    }
}

//
// $Log: BufferedRenderer32.java,v $
// Revision 1.3  2012/11/06 16:07:00  velktron
// Corrected palette & color generation.
//
// Revision 1.2  2012/09/24 17:16:23  velktron
// Massive merge between HiColor and HEAD. There's no difference from now on, and development continues on HEAD.
//
// Revision 1.1.2.1  2012/09/24 16:56:06  velktron
// New hierarchy, less code repetition.
//
// Revision 1.2.2.4  2011/11/29 12:45:29  velktron
// Restored palette and gamma effects. They do work, but display hysteresis.
//
// Revision 1.2.2.3  2011/11/27 18:19:58  velktron
// Added cache clearing to keep memory down.
//
// Revision 1.2.2.2  2011/11/18 21:36:55  velktron
// More 16-bit goodness.
//
// Revision 1.2.2.1  2011/11/14 00:27:11  velktron
// A barely functional HiColor branch. Most stuff broken. DO NOT USE
//