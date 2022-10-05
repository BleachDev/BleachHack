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

import java.awt.image.IndexColorModel;
import org.bleachhack.util.doom.m.MenuMisc;
import org.bleachhack.util.doom.v.graphics.Palettes;
import org.bleachhack.util.doom.v.tables.BlurryTable;
import org.bleachhack.util.doom.v.tables.GammaTables;

/**
 * @author Good Sign
 * @author velktron
 */
abstract class SoftwareIndexedVideoRenderer extends SoftwareGraphicsSystem<byte[], byte[]> {

    /**
     * Indexed renderers keep separate color models for each colormap (intended as gamma levels) and palette levels
     */
    protected final IndexColorModel[][] cmaps = new IndexColorModel[GammaTables.LUT.length][Palettes.NUM_PALETTES];
    protected final BlurryTable blurryTable;

    SoftwareIndexedVideoRenderer(RendererFactory.WithWadLoader<byte[], byte[]> rf) {
        super(rf, byte[].class);
        
        /**
         * create gamma levels
         * Now we can reuse existing array of cmaps, not allocating more memory
         * each time we change gamma or pick item
         */
        cmapIndexed(cmaps, palette);
        blurryTable = new BlurryTable(liteColorMaps);
    }

    @Override public int getBaseColor(byte color) { return color; }
    @Override public byte[] convertPalettedBlock(byte... src) { return src; }

    @Override
    public BlurryTable getBlurryTable() {
        return blurryTable;
    }

    @Override
    public boolean writeScreenShot(String name, DoomScreen screen) {
        // munge planar buffer to linear
        //DOOM.videoInterface.ReadScreen(screens[screen.ordinal()]);
        MenuMisc.WritePNGfile(name, screens.get(screen), width, height, cmaps[usegamma][usepalette]);
        return true;
    }
}
