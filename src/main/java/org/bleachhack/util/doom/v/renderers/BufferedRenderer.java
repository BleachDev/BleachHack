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

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;

class BufferedRenderer extends SoftwareIndexedVideoRenderer {
    private final WritableRaster[] rasters = new WritableRaster[SCREENS_COUNT];

    /**
     * This actually creates a raster with a fixed underlying array, but NOT the images themselves. So it's possible to
     * have "imageless" rasters (unless you specifically request to make them visible, of course).
     */
    BufferedRenderer(RendererFactory.WithWadLoader<byte[], byte[]> rf) {
        super(rf);
        for (DoomScreen s: DoomScreen.values()) {
            final int index = s.ordinal();
            // Only create non-visible data, pegged to the raster. Create visible images only on-demand.
            final DataBufferByte db = (DataBufferByte) newBuffer(s);
            // should be fully compatible with IndexColorModels from SoftwareIndexedVideoRenderer
            rasters[index] = Raster.createInterleavedRaster(db, width, height, width, 1, new int[]{0}, new Point(0, 0));
        }
        // Thou shalt not best nullt!!! Sets currentscreen
        forcePalette();
    }

    /**
     * Clear the screenbuffer so when the whole screen will be recreated palettes will too
     * These screens represent a complete range of palettes for a specific gamma and specific screen
     */
    @Override
    public final void forcePalette() {
        this.currentscreen = new BufferedImage(cmaps[usegamma][usepalette], rasters[DoomScreen.FG.ordinal()], true, null);
    }
}

//$Log: BufferedRenderer.java,v $
//Revision 1.18  2012/09/24 17:16:23  velktron
//Massive merge between HiColor and HEAD. There's no difference from now on, and development continues on HEAD.
//
//Revision 1.17.2.3  2012/09/24 16:56:06  velktron
//New hierarchy, less code repetition.
//
