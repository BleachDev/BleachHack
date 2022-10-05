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

package org.bleachhack.util.doom.v.tables;

import org.bleachhack.util.doom.doom.DoomMain;
import org.bleachhack.util.doom.doom.player_t;
import static org.bleachhack.util.doom.p.MobjFlags.MF_TRANSLATION;
import static org.bleachhack.util.doom.p.MobjFlags.MF_TRANSSHIFT;
import org.bleachhack.util.doom.v.renderers.BppMode;

/**
 *   Combined colormap and light LUTs.
 *   Used for z-depth cuing per column/row,
 *   and other lighting effects (sector ambient, flash).
 *   
 * @author velktron
 *
 * @param <V> The data type of the SCREEN
 */

public class LightsAndColors<V> {
    
    private final LCData LC_DATA;
    
    /** For HiColor, these are, effectively, a bunch of 555 RGB palettes,
     *  for TrueColor they are a bunch of 32-bit ARGB palettes etc.
     *  Only for indexed they represent index remappings.  
     */

    /** "peg" this to the one from RendererData */
    public V[] colormaps;

    /** lighttable_t** */
    public V[] walllights;

    /** Use in conjunction with player.fixedcolormap */
    public V fixedcolormap;
	
	/**
	 * Color tables for different players, translate a limited part to another
	 * (color ramps used for suit colors).
	 */
	public byte[][] translationtables;

    // bumped light from gun blasts
    public int extralight;

    public V[][] scalelight;
    public V[] scalelightfixed;
    public V[][] zlight;
    public V[] spritelights;

    public LightsAndColors(final DoomMain<?, V> DM) {
        this.LC_DATA = new LCData(DM.bppMode);
    }

    public int lightBits() {
        return LC_DATA.bpp.lightBits;
    }

    public int lightBright() {
        return LC_DATA.LIGHTBRIGHT;
    }

    public int lightLevels() {
        return LC_DATA.LIGHTLEVELS;
    }

    public int lightScaleShift() {
        return LC_DATA.LIGHTSCALESHIFT;
    }

    public int lightSegShift() {
        return LC_DATA.LIGHTSEGSHIFT;
    }

    public int lightZShift() {
        return LC_DATA.LIGHTZSHIFT;
    }

    public int maxLightScale() {
        return LC_DATA.MAXLIGHTSCALE;
    }

    public int maxLightZ() {
        return LC_DATA.MAXLIGHTZ;
    }

    public int numColorMaps() {
        return LC_DATA.NUMCOLORMAPS;
    }
    
    /**
     * player_t.fixedcolormap have a range of 0..31 in vanilla.
     * We must respect it. However, we can have more lightlevels then vanilla.
     * So we must scale player_t.fixedcolormap by the difference with vanilla lightBits
     * 
     * @param player
     * @return index in rich bit liteColorMaps
     */
    public V getFixedColormap(player_t player) {
        if (LC_DATA.bpp.lightBits > 5) {
            return colormaps[player.fixedcolormap << (LC_DATA.bpp.lightBits - 5)];
        }
        
        return colormaps[player.fixedcolormap];
    }

    public final byte[] getTranslationTable(long mobjflags) {
        return translationtables[(int) ((mobjflags & MF_TRANSLATION) >> MF_TRANSSHIFT)];
    }

    private static class LCData {
        final BppMode bpp;
        
        /**
         * These two are tied by an inverse relationship. E.g. 256 levels, 0 shift
         * 128 levels, 1 shift ...etc... 16 levels, 4 shift (default). Or even less,
         * if you want.
         * 
         * By setting it to the max however you get smoother light and get rid of
         * lightsegshift globally, too. Of course, by increasing the number of light
         * levels, you also put more memory pressure, and due to their being only
         * 256 colors to begin with, visually, there won't be many differences.
         */


        final int LIGHTLEVELS;
        final int LIGHTSEGSHIFT;


        /** Number of diminishing brightness levels.
           There a 0-31, i.e. 32 LUT in the COLORMAP lump. 
           TODO: how can those be distinct from the light levels???
           */    

        final int NUMCOLORMAPS;


        // These are a bit more tricky to figure out though.

        /** Maximum index used for light levels of sprites. In practice,
         *  it's capped by the number of light levels???
         *  
         *  Normally set to 48 (32 +16???)
         */

        final int MAXLIGHTSCALE;

        /** Used to scale brightness of walls and sprites. Their "scale" is shifted by
         *  this amount, and this results in an index, which is capped by MAXLIGHTSCALE.
         *  Normally it's 12 for 32 levels, so 11 for 64, 10 for 128, ans 9 for 256.
         *  
         */
        final int LIGHTSCALESHIFT;

        /** This one seems arbitrary. Will auto-fit to 128 possible levels? */
        final int MAXLIGHTZ;


        final int LIGHTBRIGHT;

        /** Normally 20 for 32 colormaps, applied to distance.
         * Formula: 25-LBITS
         *  
         */
        final int LIGHTZSHIFT;

        LCData(final BppMode bpp) {
            this.bpp = bpp;
            LIGHTLEVELS = 1 << bpp.lightBits;
            MAXLIGHTZ = LIGHTLEVELS * 4;
            LIGHTBRIGHT = 2;
            LIGHTSEGSHIFT = 8 - bpp.lightBits;
            NUMCOLORMAPS = LIGHTLEVELS;
            MAXLIGHTSCALE = 3 * LIGHTLEVELS / 2;
            LIGHTSCALESHIFT = 17 - bpp.lightBits;
            LIGHTZSHIFT = 25 - bpp.lightBits;
        }
    }
}