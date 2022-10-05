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

package org.bleachhack.util.doom.v;

import org.bleachhack.util.doom.f.Wiper;
import java.awt.Image;
import java.awt.Rectangle;
import org.bleachhack.util.doom.m.IRandom;
import org.bleachhack.util.doom.m.Settings;
import org.bleachhack.util.doom.mochadoom.Engine;
import org.bleachhack.util.doom.rr.patch_t;
import org.bleachhack.util.doom.v.graphics.Horizontal;
import org.bleachhack.util.doom.v.graphics.Plotter;
import org.bleachhack.util.doom.v.graphics.Relocation;
import org.bleachhack.util.doom.v.renderers.DoomScreen;
import org.bleachhack.util.doom.v.scale.VideoScale;
import org.bleachhack.util.doom.v.tables.BlurryTable;

/** 
 * Refactored a lot of it; most notable changes:
 * 
 *  - 2d rendering methods are unified, standartized, generized, typehinted and incapsulated,
 *    they are moved into separate interfaces and those interfaces to separate package
 * 
 *  - Fixed buggy 2d alrorithms, such as scaling, rewritten and parallelized column drawing logic,
 *    unified and simplified calculation of areas on column-major surface
 * 
 *  - Renderer drivers are separated from drawing API and refactored a lot: fixed all issues with
 *    improper gammas, lights and tinting, fixed delay before it applied on non-indexed render,
 *    parallelized HiColor and TrueColor renderers. Only standard indexed 8-bit renderer is still
 *    single-threaded, and he is very performant and is cool too!
 * 
 *   -- Good Sign 2017/04/12
 * 
 *    Notes about method hiding:
 *  - (A comment on the notes below) It would be only wonderful, if it also will make reflection-access
 *    (that what happens when some lame Java developer cannot access something and he just use reflection to
 *    set method public) harder on these methods. I hate lame Java developers.
 * 
 *    Never trust clients. Never show them too much. So for those of you, who don't know something like that,
 *    I introduce a good method of hiding interface methods. It is called Hiding By Complexity of Implementation.
 *    Why I call it that? Because we strike a zombie sergeant using a shotgun.
 *
 *    What do we need to hide? Complexity.
 *    Why do we need to hide complexity? Because it is hard to use complex internal methods *properly*.
 *    That is why they are internal. A here it is the main contract: if you want to use internal methods,
 *    you create all their environment properly by sub-contracts of concrete interfaces.
 *    So we hide complexity of usage by complexity of implementation the usable case. And the sergeant falls.
 * 
 *    A lot of interfaces with a lot of default methods. This is intended feature hiding mechanism.
 *    Yes, it seems that a lot of PUBLIC default methods (default method is always public)
 *    gains much access and power to one who use it... But actually, these interfaces restrict
 *    much more, then static methods, because you have to actually *implement* the interface
 *    to access any of these methods, and implementing these interfaces means implementing
 *    a whole part of DoomGraphicsSystem. And I've thought out the interfaces contracts in the way
 *    that if someone *implement* them on purpose, their methods will be safe and useful for him.
 * 
 *   -- Good Sign 2017/04/14
 * 
 *  DoomVideoSystem is now an interface, that all "video drivers" (whether do screen, disk, etc.)
 *  must implement. 
 *  
 *  23/10/2011: Made into a generic type, which affects the underlying raw screen data
 *  type. This should make -in theory- true color or super-indexed (>8 bits) video modes
 *  possible. The catch is that everything directly meddling with the renderer must also
 *  be aware of the underlying implementation. E.g. the various screen arrays will not be
 *  necessarily byte[].
 * 
 * @author Maes
 */

public interface DoomGraphicSystem<T, V> {
    
    /**
     * Flags used by patch drawing functions
     * Now working as separate and optional varargs argument
     * Added by _D_. Unsure if I should use VSI objects instead, as they
     * already carry scaling information which doesn't need to be repacked...
     */
    final int V_NOSCALESTART      = 0x00010000;   // dont scale x,y, start coords
    final int V_SCALESTART        = 0x00020000;   // scale x,y, start coords
    final int V_SCALEPATCH        = 0x00040000;   // scale patch
    final int V_NOSCALEPATCH      = 0x00080000;   // don't scale patch
    final int V_WHITEMAP          = 0x00100000;   // draw white (for v_drawstring)    
    final int V_FLIPPEDPATCH      = 0x00200000;   // flipped in y
    final int V_TRANSLUCENTPATCH  = 0x00400000;   // draw patch translucent    
    final int V_PREDIVIDE         = 0x00800000;   // pre-divide by best x/y scale.    
    final int V_SCALEOFFSET       = 0x01000000;   // Scale the patch offset
    final int V_NOSCALEOFFSET     = 0x02000000;   // dont's cale patch offset
    final int V_SAFESCALE         = 0x04000000;   // scale only by minimal scale of x/y instead of both
    
    /**
     * Public API
     * See documentation in r2d package
     * 
     * These are only methods DoomGraphicSystem wants to share from the whole insanely big package r2d
     * Because using only these methods, it is minimal risk of breaking something. Actually,
     * the only problematic cases should be passing null instead of argument or invalid coordinates.
     */
    
    /* SCREENS */
    V getScreen(DoomScreen screenType);
    int getScalingX();
    int getScalingY();
    int getScreenWidth();
    int getScreenHeight();
    void screenCopy(V srcScreen, V dstScreen, Relocation relocation);
    void screenCopy(DoomScreen srcScreen, DoomScreen dstScreen);

    /* PALETTES */
    void setUsegamma(int gammalevel);
    int getUsegamma();
    void setPalette(int palette);
    int getPalette();
    int getBaseColor(byte color);
    int getBaseColor(int color);
    
    /* POINTS */
    int point(int x, int y);
    int point(int x, int y, int width);
    
    /* LINES */
    void drawLine(Plotter<?> plotter, int x1, int x2);
    
    /* PATCHES */
    void DrawPatch(DoomScreen screen, patch_t patch, int x, int y, int... flags);
    void DrawPatchCentered(DoomScreen screen, patch_t patch, int y, int... flags);
    void DrawPatchCenteredScaled(DoomScreen screen, patch_t patch, VideoScale vs, int y, int... flags);
    void DrawPatchScaled(DoomScreen screen, patch_t patch, VideoScale vs, int x, int y, int... flags);
    void DrawPatchColScaled(DoomScreen screen, patch_t patch, VideoScale vs, int x, int col);
    
    /* RECTANGLES */
    void CopyRect(DoomScreen srcScreenType, Rectangle rectangle, DoomScreen dstScreenType);
    void CopyRect(DoomScreen srcScreenType, Rectangle rectangle, DoomScreen dstScreenType, int dstPoint);
    void FillRect(DoomScreen screenType, Rectangle rectangle, V patternSrc, Horizontal pattern);
    void FillRect(DoomScreen screenType, Rectangle rectangle, V patternSrc, int point);
    void FillRect(DoomScreen screenType, Rectangle rectangle, int color);
    void FillRect(DoomScreen screenType, Rectangle rectangle, byte color);
    
    /* BLOCKS */
    V convertPalettedBlock(byte... src);
    V ScaleBlock(V block, VideoScale vs, int width, int height);
    void TileScreen(DoomScreen dstScreen, V block, Rectangle blockArea);
    void TileScreenArea(DoomScreen dstScreen, Rectangle screenArea, V block, Rectangle blockArea);
    void DrawBlock(DoomScreen dstScreen, V block, Rectangle sourceArea, int destinationPoint);
    
    /** 
     * No matter how complex/weird/arcane palette manipulations you do internally, the AWT module
     * must always be able to "tap" into what's the current, "correct" screen after all manipulation and
     * color juju was applied. Call after a palette/gamma change.
     */
    Image getScreenImage();
    
    /**
     * Saves screenshot to a file "filling a planar buffer to linear"
     * (I cannot guarantee I understood - Good Sign 2017/04/01)
     * @param name
     * @param screen
     * @return true if succeed
     */
    boolean writeScreenShot(String name, DoomScreen screen);

    /**
     * If the renderer operates color maps, get them
     * Used for scene rendering
     */
    V[] getColorMap();

    /**
     * Plotter for point-by-point drawing of AutoMap
     */
    default Plotter<V> createPlotter(DoomScreen screen) {
        switch(Engine.getConfig().getValue(Settings.automap_plotter_style, Plotter.Style.class)) {
            case Thick:
                return new Plotter.Thick<>(getScreen(screen), getScreenWidth(), getScreenHeight());
            case Deep:
                return new Plotter.Deep<>(getScreen(screen), getScreenWidth(), getScreenHeight());
            default:
                return new Plotter.Thin<>(getScreen(screen), getScreenWidth());
        }
    }
    
    Wiper createWiper(IRandom random);
    BlurryTable getBlurryTable();

    /**
     * Indexed renderer needs to reset its image 
     */
    default void forcePalette() {}
}