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
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferUShort;
import java.awt.image.VolatileImage;
import java.util.concurrent.BrokenBarrierException;
import java.util.logging.Level;
import org.bleachhack.util.doom.mochadoom.Loggers;
import org.bleachhack.util.doom.v.tables.BlurryTable;
import org.bleachhack.util.doom.v.tables.ColorTint;
import static org.bleachhack.util.doom.v.tables.ColorTint.GREY_TINTS;
import static org.bleachhack.util.doom.v.tables.ColorTint.NORMAL_TINTS;

/**
 * Redesigned to follow as closely as possible its 32-bit complement
 *
 * It ulitizes now the same parallelization as 32-bit TrueColor renderer,
 * becasue it allows palettes and gammas to be applied properly on post-process.
 * The separate LUT's are generated for this renderer
 * 
 * Most likely, this renderer will be the least performant.
 * - Good Sign 2017/04/12
 */
class BufferedRenderer16 extends SoftwareParallelVideoRenderer<byte[], short[]> {
    protected final short[] raster;
    
    // VolatileImage speeds up delivery to VRAM - it is 30-40 fps faster then directly rendering BufferedImage
    protected VolatileImage screen;
    
    // indicated whether machine display in the same mode as this renderer
    protected final boolean compatible = checkConfigurationHicolor();
    protected final BlurryTable blurryTable;
    
    /**
     * This implementation will "tie" a bufferedimage to the underlying byte raster.
     *
     * NOTE: this relies on the ability to "tap" into a BufferedImage's backing array, in order to have fast writes
     * without setpixel/getpixel. If that is not possible, then we'll need to use a special renderer.
     */
    BufferedRenderer16(RendererFactory.WithWadLoader<byte[], short[]> rf) {
        super(rf, short[].class);
        /**
         * There is only sense to create and use VolatileImage if it can use native acceleration
         * which is impossible if we rendering into alien color space or bit depth
         */
        if (compatible) {
            // if we lucky to have 16-bit accelerated screen
            screen = GRAPHICS_CONF.createCompatibleVolatileImage(width, height);
            currentscreen = GRAPHICS_CONF.createCompatibleImage(width, height);
        } else {
            currentscreen = new BufferedImage(width, height, BufferedImage.TYPE_USHORT_555_RGB);
        }
        
        // extract raster from the created image
        currentscreen.setAccelerationPriority(1.0f);
        raster = ((DataBufferUShort)((BufferedImage) currentscreen).getRaster().getDataBuffer()).getData();
        
        blurryTable = new BlurryTable(liteColorMaps);

        /**
         * Create postprocess worker threads
         * 320 is dividable by 16, so any scale of it would
         * TODO: support for custom resolutions?
         */
        final int len = raster.length, chunk = len / PARALLELISM;
        for (int i = 0; i < PARALLELISM; i++) {
            paletteThreads[i] = new ShortPaletteThread(i * chunk, (i + 1) * chunk);
        }
    }

    /**
     * This method is accessed by AWTDoom to render the screen
     * As we use VolatileImage that can lose its contents, it must have special care.
     * doWriteScreen is called in the moment, when the VolatileImage is ready and
     * we can copy to it and post-process
     * 
     * If we use incompatible display, just draw our existing BufferedImage - it would be faster
     */
    @Override
    public Image getScreenImage() {
        doWriteScreen();
        if (!compatible) {
            return currentscreen;
        } else do {
            if (screen.validate(GRAPHICS_CONF) == VolatileImage.IMAGE_INCOMPATIBLE) {
                screen.flush();
                // old vImg doesn't work with new GraphicsConfig; re-create it
                screen = GRAPHICS_CONF.createCompatibleVolatileImage(width, height);
            }

            final Graphics2D g = screen.createGraphics();
            g.drawImage(currentscreen, 0, 0, null);
            g.dispose();
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
    }

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
    private class ShortPaletteThread implements Runnable {
        private final short[] FG;
        private final int start;
        private final int stop;

        ShortPaletteThread(int start, int stop) {
            this.start = start;
            this.stop = stop;
            this.FG = screens.get(DoomScreen.FG);
        }

        /**
         * For BFG-9000 look at BufferedRenderer32.IntPaletteThread
         * But there is BFG-2704
         */
        @Override
        public void run() {
            final ColorTint t = (GRAYPAL_SET ? GREY_TINTS : NORMAL_TINTS).get(usepalette);
            final byte[] LUT_R = t.LUT_r5[usegamma];
            final byte[] LUT_G = t.LUT_g5[usegamma];
            final byte[] LUT_B = t.LUT_b5[usegamma];
            for (int i = start; i < stop;) {
                raster[i] = (short) (((LUT_R[(FG[i] >> 10) & 0x1F] & 0x1F) << 10) | ((LUT_G[(FG[i] >> 5) & 0x1F] & 0x1F) << 5) | (LUT_B[FG[i++] & 0x1F] & 0x1F));
                raster[i] = (short) (((LUT_R[(FG[i] >> 10) & 0x1F] & 0x1F) << 10) | ((LUT_G[(FG[i] >> 5) & 0x1F] & 0x1F) << 5) | (LUT_B[FG[i++] & 0x1F] & 0x1F));
                raster[i] = (short) (((LUT_R[(FG[i] >> 10) & 0x1F] & 0x1F) << 10) | ((LUT_G[(FG[i] >> 5) & 0x1F] & 0x1F) << 5) | (LUT_B[FG[i++] & 0x1F] & 0x1F));
                raster[i] = (short) (((LUT_R[(FG[i] >> 10) & 0x1F] & 0x1F) << 10) | ((LUT_G[(FG[i] >> 5) & 0x1F] & 0x1F) << 5) | (LUT_B[FG[i++] & 0x1F] & 0x1F));
                raster[i] = (short) (((LUT_R[(FG[i] >> 10) & 0x1F] & 0x1F) << 10) | ((LUT_G[(FG[i] >> 5) & 0x1F] & 0x1F) << 5) | (LUT_B[FG[i++] & 0x1F] & 0x1F));
                raster[i] = (short) (((LUT_R[(FG[i] >> 10) & 0x1F] & 0x1F) << 10) | ((LUT_G[(FG[i] >> 5) & 0x1F] & 0x1F) << 5) | (LUT_B[FG[i++] & 0x1F] & 0x1F));
                raster[i] = (short) (((LUT_R[(FG[i] >> 10) & 0x1F] & 0x1F) << 10) | ((LUT_G[(FG[i] >> 5) & 0x1F] & 0x1F) << 5) | (LUT_B[FG[i++] & 0x1F] & 0x1F));
                raster[i] = (short) (((LUT_R[(FG[i] >> 10) & 0x1F] & 0x1F) << 10) | ((LUT_G[(FG[i] >> 5) & 0x1F] & 0x1F) << 5) | (LUT_B[FG[i++] & 0x1F] & 0x1F));
                raster[i] = (short) (((LUT_R[(FG[i] >> 10) & 0x1F] & 0x1F) << 10) | ((LUT_G[(FG[i] >> 5) & 0x1F] & 0x1F) << 5) | (LUT_B[FG[i++] & 0x1F] & 0x1F));
                raster[i] = (short) (((LUT_R[(FG[i] >> 10) & 0x1F] & 0x1F) << 10) | ((LUT_G[(FG[i] >> 5) & 0x1F] & 0x1F) << 5) | (LUT_B[FG[i++] & 0x1F] & 0x1F));
                raster[i] = (short) (((LUT_R[(FG[i] >> 10) & 0x1F] & 0x1F) << 10) | ((LUT_G[(FG[i] >> 5) & 0x1F] & 0x1F) << 5) | (LUT_B[FG[i++] & 0x1F] & 0x1F));
                raster[i] = (short) (((LUT_R[(FG[i] >> 10) & 0x1F] & 0x1F) << 10) | ((LUT_G[(FG[i] >> 5) & 0x1F] & 0x1F) << 5) | (LUT_B[FG[i++] & 0x1F] & 0x1F));
                raster[i] = (short) (((LUT_R[(FG[i] >> 10) & 0x1F] & 0x1F) << 10) | ((LUT_G[(FG[i] >> 5) & 0x1F] & 0x1F) << 5) | (LUT_B[FG[i++] & 0x1F] & 0x1F));
                raster[i] = (short) (((LUT_R[(FG[i] >> 10) & 0x1F] & 0x1F) << 10) | ((LUT_G[(FG[i] >> 5) & 0x1F] & 0x1F) << 5) | (LUT_B[FG[i++] & 0x1F] & 0x1F));
                raster[i] = (short) (((LUT_R[(FG[i] >> 10) & 0x1F] & 0x1F) << 10) | ((LUT_G[(FG[i] >> 5) & 0x1F] & 0x1F) << 5) | (LUT_B[FG[i++] & 0x1F] & 0x1F));
                raster[i] = (short) (((LUT_R[(FG[i] >> 10) & 0x1F] & 0x1F) << 10) | ((LUT_G[(FG[i] >> 5) & 0x1F] & 0x1F) << 5) | (LUT_B[FG[i++] & 0x1F] & 0x1F));
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
// $Log: BufferedRenderer16.java,v $
// Revision 1.4  2012/11/06 16:07:00  velktron
// Corrected palette & color generation.
//
// Revision 1.3  2012/09/24 17:16:23  velktron
// Massive merge between HiColor and HEAD. There's no difference from now on, and development continues on HEAD.
//
// Revision 1.2.2.5  2012/09/24 16:56:06  velktron
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