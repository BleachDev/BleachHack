/*
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

import java.util.logging.Level;
import org.bleachhack.util.doom.mochadoom.Loggers;
import org.bleachhack.util.doom.rr.patch_t;
import org.bleachhack.util.doom.utils.C2JUtils;
import static org.bleachhack.util.doom.v.DoomGraphicSystem.V_FLIPPEDPATCH;
import static org.bleachhack.util.doom.v.DoomGraphicSystem.V_NOSCALEOFFSET;
import static org.bleachhack.util.doom.v.DoomGraphicSystem.V_NOSCALEPATCH;
import static org.bleachhack.util.doom.v.DoomGraphicSystem.V_NOSCALESTART;
import static org.bleachhack.util.doom.v.DoomGraphicSystem.V_PREDIVIDE;
import static org.bleachhack.util.doom.v.DoomGraphicSystem.V_SAFESCALE;
import static org.bleachhack.util.doom.v.DoomGraphicSystem.V_SCALEOFFSET;
import static org.bleachhack.util.doom.v.DoomGraphicSystem.V_SCALEPATCH;
import static org.bleachhack.util.doom.v.DoomGraphicSystem.V_SCALESTART;
import org.bleachhack.util.doom.v.scale.VideoScale;

/**
 * Rewritten unified patch-drawing methods with parallelism (yeah, multithread!)
 * Note that now most of the functions now support FLAGS now as a separate argument, I totally needed screen id safety.
 * Reimplemented counter-flags, that work the next way:
 *  - there is a Default Behavior chose when flag is not present
 *  - if the flag is present Default Behavior is changed
 *  - if both the flag and the opposite flag are present, then the flag that restores Default Behavior takes precedence
 * 
 * I tried my best to preserve all of the features done by prior contributors. - Good Sign 2017/04/02
 * 
 * @author Good Sign
 *
 * About all DrawPatch functions:
 * It uses FLAGS (see above) (now as a separate - Good Sign 2017/04/04) parameter, to be
 * parsed afterwards. Shamelessly ripped from Doom Legacy (for menus, etc) by _D_ ;-)
 * 
 * added:05-02-98: default params : scale patch and scale start
 *
 * Iniially implemented for Mocha Doom by _D_ (shamelessly ripped from Eternity Engine ;-), adapted to scale based
 * on a scaling info object (VSI).
 *
 * Unless overriden by flags, starting x and y are automatically scaled (implied V_SCALESTART)
 */
public interface Patches<V, E extends Enum<E>> extends Columns<V, E> {

    /**
     * V_DrawPatch
     * 
     * Draws a patch to the screen without centering or scaling
     */
    default void DrawPatch(E screen, patch_t patch, int x, int y, int... flags) {
        DrawPatchScaled(screen, patch, null, x, y, flags);
    }
    
    /**
     * V_DrawPatch
     * 
     * Draws a patch to the screen without centering or scaling
     */
    default void DrawPatchCentered(E screen, patch_t patch, int y, int... flags) {
        Patches.this.DrawPatchCenteredScaled(screen, patch, null, y, flags);
    }
    
    /**
     * V_DrawScaledPatch like V_DrawPatch, but scaled with IVideoScale object scaling
     * Centers the x coordinate on a screen based on patch width and offset
     * I have completely reworked column drawing code, so it resides in another class, and supports parallelism
     *  - Good Sign 2017/04/04
     * 
     * It uses FLAGS (see above) (now as a separate - Good Sign 2017/04/04) parameter, to be
     * parsed afterwards. Shamelessly ripped from Doom Legacy (for menus, etc) by _D_ ;-)
     */ 
    default void DrawPatchCenteredScaled(E screen, patch_t patch, VideoScale vs, int y, int... flags) {
        final int flagsV = flags.length > 0 ? flags[0] : 0;
        int dupx, dupy;
        if (vs != null) {
            if (C2JUtils.flags(flagsV, V_SAFESCALE)) {
                dupx = dupy = vs.getSafeScaling();
            } else {
                dupx = vs.getScalingX();
                dupy = vs.getScalingY();
            }
        } else dupx = dupy = 1;
        final boolean predevide = C2JUtils.flags(flagsV, V_PREDIVIDE);
        // By default we scale, if V_NOSCALEOFFSET we dont scale unless V_SCALEOFFSET (restores Default Behavior)
        final boolean scaleOffset = !C2JUtils.flags(flagsV, V_NOSCALEOFFSET) || C2JUtils.flags(flagsV, V_SCALEOFFSET);
        // By default we scale, if V_NOSCALESTART we dont scale unless V_SCALESTART (restores Default Behavior)
        final boolean scaleStart = !C2JUtils.flags(flagsV, V_NOSCALESTART) || C2JUtils.flags(flagsV, V_SCALESTART);
        // By default we do dup, if V_NOSCALEPATCH we dont dup unless V_SCALEPATCH (restores Default Behavior)
        final boolean noScalePatch = C2JUtils.flags(flagsV, V_NOSCALEPATCH) && !C2JUtils.flags(flagsV, V_SCALEPATCH);
        final boolean flip = C2JUtils.flags(flagsV, V_FLIPPEDPATCH);
        final int halfWidth = noScalePatch ? patch.width / 2 : patch.width * dupx / 2;
        int x = getScreenWidth() / 2 - halfWidth - (scaleOffset ? patch.leftoffset * dupx : patch.leftoffset);
        y = applyScaling(y, patch.topoffset, dupy, predevide, scaleOffset, scaleStart);
        
        if (noScalePatch) {
            dupx = dupy = 1;
        }
        
        try {
            doRangeCheck(x, y, patch, dupx, dupy);
            DrawPatchColumns(getScreen(screen), patch, x, y, dupx, dupy, flip);
        } catch (BadRangeException ex) {
            printDebugPatchInfo(patch, x, y, predevide, scaleOffset, scaleStart, dupx, dupy);
        }
    }

    /**
     * This method should help to debug bad patches or bad placement of them
     *  - Good Sign 2017/04/22
     */
    default void printDebugPatchInfo(patch_t patch, int x, int y, final boolean predevide, final boolean scaleOffset, final boolean scaleStart, int dupx, int dupy) {
        Loggers.getLogger(Patches.class.getName()).log(Level.INFO, () -> String.format(
            "V_DrawPatch: bad patch (ignored)\n" +
            "Patch %s at %d, %d exceeds LFB\n" + 
            "\tpredevide: %s\n" +
            "\tscaleOffset: %s\n" +
            "\tscaleStart: %s\n" +
            "\tdupx: %s, dupy: %s\n" +
            "\tleftoffset: %s\n" +
            "\ttopoffset: %s\n",
            patch.name, x, y,
            predevide, scaleOffset, scaleStart, dupx, dupy, patch.leftoffset, patch.topoffset
        ));
    }
    
    /**
     * V_DrawPatch
     * 
     * V_DrawScaledPatch like V_DrawPatch, but scaled with IVideoScale object scaling
     * I have completely reworked column drawing code, so it resides in another class, and supports parallelism
     *  - Good Sign 2017/04/04
     */ 
    default void DrawPatchScaled(E screen, patch_t patch, VideoScale vs, int x, int y, int... flags) {
        final int flagsV = flags.length > 0 ? flags[0] : 0;
        int dupx, dupy;
        if (vs != null) {
            if (C2JUtils.flags(flagsV, V_SAFESCALE)) {
                dupx = dupy = vs.getSafeScaling();
            } else {
                dupx = vs.getScalingX();
                dupy = vs.getScalingY();
            }
        } else dupx = dupy = 1;
        final boolean predevide = C2JUtils.flags(flagsV, V_PREDIVIDE);
        // By default we scale, if V_NOSCALEOFFSET we dont scale unless V_SCALEOFFSET (restores Default Behavior)
        final boolean scaleOffset = !C2JUtils.flags(flagsV, V_NOSCALEOFFSET) || C2JUtils.flags(flagsV, V_SCALEOFFSET);
        // By default we scale, if V_NOSCALESTART we dont scale unless V_SCALESTART (restores Default Behavior)
        final boolean scaleStart = !C2JUtils.flags(flagsV, V_NOSCALESTART) || C2JUtils.flags(flagsV, V_SCALESTART);
        // By default we do dup, if V_NOSCALEPATCH we dont dup unless V_SCALEPATCH (restores Default Behavior)
        final boolean noScalePatch = C2JUtils.flags(flagsV, V_NOSCALEPATCH) && !C2JUtils.flags(flagsV, V_SCALEPATCH);
        final boolean flip = C2JUtils.flags(flagsV, V_FLIPPEDPATCH);
        x = applyScaling(x, patch.leftoffset, dupx, predevide, scaleOffset, scaleStart);
        y = applyScaling(y, patch.topoffset, dupy, predevide, scaleOffset, scaleStart);
        
        if (noScalePatch) {
            dupx = dupy = 1;
        }
        
        try {
            doRangeCheck(x, y, patch, dupx, dupy);
            DrawPatchColumns(getScreen(screen), patch, x, y, dupx, dupy, flip);
        } catch (BadRangeException ex) {
            // Do not abort!
            printDebugPatchInfo(patch, x, y, predevide, scaleOffset, scaleStart, dupx, dupy);
        }
    }
    
    /**
     * Replaces DrawPatchCol for bunny scrolled in Finale.
     * Also uses my reworked column code, but that one is not parallelized
     *  - Good Sign 2017/04/04
     */
    default void DrawPatchColScaled(E screen, patch_t patch, VideoScale vs, int x, int col) {
        final int dupx = vs.getScalingX(), dupy = vs.getScalingY();
        x -= patch.leftoffset;
        x *= dupx;
        
        DrawColumn(
            getScreen(screen),
            patch.columns[col],
            new Horizontal(point(x, 0), dupx),
            convertPalettedBlock(patch.columns[col].data),
            getScreenWidth(),
            dupy
        );
    }
    
    default int applyScaling(int c, int offset, int dup, boolean predevide, boolean scaleOffset, boolean scaleStart) {
        // A very common operation, eliminates the need to pre-divide.
        if (predevide)
            c /= getScalingX();
        
        // Scale start before offsetting, it seems right to do so - Good Sign 2017/04/04
        if (scaleStart)
            c *= dup;
        
        // MAES: added this fix so that non-zero patch offsets can be
        // taken into account, regardless of whether we use pre-scaled
        // coords or not. Only Doomguy's face needs this hack for now.
        c -= scaleOffset ? offset * dup : offset;
        return c;
    }
}
