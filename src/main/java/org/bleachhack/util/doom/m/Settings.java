/*-----------------------------------------------------------------------------
//
// Copyright (C) 1993-1996 Id Software, Inc.
// Copyright (C) 2017 Good Sign
//
// This program is free software; you can redistribute it and/or
// modify it under the terms of the GNU General Public License
// as published by the Free Software Foundation; either version 2
// of the License, or (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.
//
// From m_misc.c
//-----------------------------------------------------------------------------*/

package org.bleachhack.util.doom.m;

import org.bleachhack.util.doom.awt.FullscreenOptions;
import static org.bleachhack.util.doom.doom.ConfigBase.FILE_DOOM;
import static org.bleachhack.util.doom.doom.ConfigBase.FILE_MOCHADOOM;
import org.bleachhack.util.doom.doom.ConfigBase.Files;
import org.bleachhack.util.doom.doom.ConfigManager;
import static org.bleachhack.util.doom.doom.englsh.*;
import static org.bleachhack.util.doom.g.Signals.ScanCode.*;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.bleachhack.util.doom.mochadoom.Engine;
import org.bleachhack.util.doom.utils.QuoteType;
import org.bleachhack.util.doom.v.graphics.Plotter;
import org.bleachhack.util.doom.v.renderers.BppMode;
import org.bleachhack.util.doom.v.renderers.SceneRendererMode;
import org.bleachhack.util.doom.v.tables.GreyscaleFilter;

/**
 * An enumeration with the most basic default Doom settings their default values, used if nothing else is available.
 * They are applied first thing, and then updated from the .cfg file.
 * 
 * The file now also contains settings on many features introduced by this new version of Mocha Doom
 *  - Good Sign 2017/04/11
 * 
 * TODO: find a trick to separate settings groups in the same file vanilla-compatibly
 */
public enum Settings {
    /**
     * Defaults (default.cfg) defined in vanilla format, ordered in vanilla order
     */
    mouse_sensitivity(FILE_DOOM, 5),
    sfx_volume(FILE_DOOM, 8),
    music_volume(FILE_DOOM, 8),
    show_messages(FILE_DOOM, 1),
    key_right(FILE_DOOM, SC_RIGHT.ordinal()),
    key_left(FILE_DOOM, SC_LEFT.ordinal()),
    key_up(FILE_DOOM, SC_W.ordinal()),
    key_down(FILE_DOOM, SC_S.ordinal()),
    key_strafeleft(FILE_DOOM, SC_A.ordinal()),
    key_straferight(FILE_DOOM, SC_D.ordinal()),
    key_fire(FILE_DOOM, SC_LCTRL.ordinal()),
    key_use(FILE_DOOM, SC_SPACE.ordinal()),
    key_strafe(FILE_DOOM, SC_LALT.ordinal()),
    key_speed(FILE_DOOM, SC_RSHIFT.ordinal()),
    use_mouse(FILE_DOOM, 1),
    mouseb_fire(FILE_DOOM, 0),
    mouseb_strafe(FILE_DOOM, 1), // AX: Fixed
    mouseb_forward(FILE_DOOM, 2), // AX: Value inverted with the one above
    use_joystick(FILE_DOOM, 0),
    joyb_fire(FILE_DOOM, 0),
    joyb_strafe(FILE_DOOM, 1),
    joyb_use(FILE_DOOM, 3),
    joyb_speed(FILE_DOOM, 2),
    screenblocks(FILE_DOOM, 9),
    detaillevel(FILE_DOOM, 0),
    snd_channels(FILE_DOOM, 8),
    snd_musicdevice(FILE_DOOM, 3), // unused, here for compatibility
    snd_sfxdevice(FILE_DOOM, 3), // unused, here for compatibility
    snd_sbport(FILE_DOOM, 0), // unused, here for compatibility
    snd_sbirq(FILE_DOOM, 0), // unused, here for compatibility
    snd_sbdma(FILE_DOOM, 0), // unused, here for compatibility
    snd_mport(FILE_DOOM, 0), // unused, here for compatibility
    usegamma(FILE_DOOM, 0),
    chatmacro0(FILE_DOOM, HUSTR_CHATMACRO0),
    chatmacro1(FILE_DOOM, HUSTR_CHATMACRO1),
    chatmacro2(FILE_DOOM, HUSTR_CHATMACRO2),
    chatmacro3(FILE_DOOM, HUSTR_CHATMACRO3),
    chatmacro4(FILE_DOOM, HUSTR_CHATMACRO4),
    chatmacro5(FILE_DOOM, HUSTR_CHATMACRO5),
    chatmacro6(FILE_DOOM, HUSTR_CHATMACRO6),
    chatmacro7(FILE_DOOM, HUSTR_CHATMACRO7),
    chatmacro8(FILE_DOOM, HUSTR_CHATMACRO8),
    chatmacro9(FILE_DOOM, HUSTR_CHATMACRO9),

    /**
     * Mocha Doom (mochadoom.cfg), these can be defined to anything and will be sorded by name
     */
    mb_used(FILE_MOCHADOOM, 2),
    fullscreen(FILE_MOCHADOOM, false),
    fullscreen_mode(FILE_MOCHADOOM, FullscreenOptions.FullMode.Best),
    fullscreen_stretch(FILE_MOCHADOOM, FullscreenOptions.StretchMode.Fit),
    fullscreen_interpolation(FILE_MOCHADOOM, FullscreenOptions.InterpolationMode.Nearest),
    alwaysrun(FILE_MOCHADOOM, false), // Always run is OFF
    vanilla_key_behavior(FILE_MOCHADOOM, true), // Currently forces LSHIFT on RSHIFT, TODO: layouts, etc 
    automap_plotter_style(FILE_MOCHADOOM, Plotter.Style.Thin), // Thin is vanilla, Thick is scaled, Deep slightly rounded scaled
    enable_colormap_lump(FILE_MOCHADOOM, true), // Enables usage of COLORMAP lump read from wad during lights and specials generation
    color_depth(FILE_MOCHADOOM, BppMode.Indexed), // Indexed: 256, HiColor: 32 768, TrueColor: 16 777 216
    extend_plats_limit(FILE_MOCHADOOM, true), // Resize instead of "P_AddActivePlat: no more plats!"
    extend_button_slots_limit(FILE_MOCHADOOM, true), // Resize instead of "P_StartButton: no button slots left!"
    fix_blockmap(FILE_MOCHADOOM, true), // Add support for 512x512 blockmap
    fix_gamma_ramp(FILE_MOCHADOOM, false), // Vanilla do not use pure black color because Gamma LUT calculated without it, doubling 128
    fix_gamma_palette(FILE_MOCHADOOM, false), // In vanilla, switching gamma with F11 hides Berserk or Rad suit tint
    fix_sky_change(FILE_MOCHADOOM, false), // In vanilla, sky does not change when you exit the level and the next level with new sky
    fix_sky_palette(FILE_MOCHADOOM, false), // In vanilla, sky color does not change when under effect of Invulnerability powerup
    fix_medi_need(FILE_MOCHADOOM, false), // In vanilla, message "Picked up a medikit that you REALLY need!" never appears due to bug
    fix_ouch_face(FILE_MOCHADOOM, false), // In vanilla, ouch face displayed only when acuired 25+ health when damaged for 25+ health
    line_of_sight(FILE_MOCHADOOM, LOS.Vanilla), // Deaf monsters when thing pos corellates somehow with map vertex, change desync demos
    vestrobe(FILE_MOCHADOOM, false), // Strobe effect on automap cut off from vanilla
    scale_screen_tiles(FILE_MOCHADOOM, true), // If you scale screen tiles, it looks like vanilla
    scale_melt(FILE_MOCHADOOM, true), // If you scale melt and use DoomRandom generator (not truly random), it looks exacly like vanilla
    semi_translucent_fuzz(FILE_MOCHADOOM, false), // only works in AlphaTrueColor mode. Also ignored with fuzz_mix = true
    fuzz_mix(FILE_MOCHADOOM, false), // Maes unique features on Fuzz effect. Vanilla dont have that, so they are switched off by default
    parallelism_realcolor_tint(FILE_MOCHADOOM, Runtime.getRuntime().availableProcessors()), // Used for real color tinting to speed up
    parallelism_patch_columns(FILE_MOCHADOOM, 0), // When drawing screen graphics patches, this speeds up column drawing, <= 0 is serial
    greyscale_filter(FILE_MOCHADOOM, GreyscaleFilter.Luminance), // Used for FUZZ effect or with -greypal comand line argument (for test)
    scene_renderer_mode(FILE_MOCHADOOM, SceneRendererMode.Serial), // In vanilla, scene renderer is serial. Parallel can be faster
    reconstruct_savegame_pointers(FILE_MOCHADOOM, true); // In vanilla, infighting targets are not restored on savegame load
    
    public final static Map<Files, EnumSet<Settings>> SETTINGS_MAP = new HashMap<>();
    
    static {
        Arrays.stream(values()).forEach(Settings::updateConfig);
    }

    <T extends Enum<T>> Settings(Files config, final T defaultValue) {
        this.defaultValue = defaultValue;
        this.valueType = defaultValue.getClass();
        this.configBase = config;
    }
    
    Settings(Files config, final String defaultValue) {
        this.defaultValue = defaultValue;
        this.valueType = String.class;
        this.configBase = config;
    }

    Settings(Files config, final char defaultValue) {
        this.defaultValue = defaultValue;
        this.valueType = Character.class;
        this.configBase = config;
    }

    Settings(Files config, final int defaultValue) {
        this.defaultValue = defaultValue;
        this.valueType = Integer.class;
        this.configBase = config;
    }

    Settings(Files config, final long defaultValue) {
        this.defaultValue = defaultValue;
        this.valueType = Long.class;
        this.configBase = config;
    }

    Settings(Files config, final double defaultValue) {
        this.defaultValue = defaultValue;
        this.valueType = Double.class;
        this.configBase = config;
    }

    Settings(Files config, final boolean defaultValue) {
        this.defaultValue = defaultValue;
        this.valueType = Boolean.class;
        this.configBase = config;
    }

    public final Class<?> valueType;
    public final Object defaultValue;
    private Files configBase;
    
    public boolean is(Object obj) {
        return Engine.getConfig().equals(obj);
    }
    
    public ConfigManager.UpdateStatus hasChange(boolean b) {
        configBase.changed = configBase.changed || b;
        return b ? ConfigManager.UpdateStatus.UPDATED : ConfigManager.UpdateStatus.UNCHANGED;
    }

    public void rebase(Files newConfig) {
        if (configBase == newConfig) {
            return;
        }
        SETTINGS_MAP.get(configBase).remove(this);
        configBase = newConfig;
        updateConfig();
    }
        
    public Optional<QuoteType> quoteType() {
        if (valueType == String.class)
            return Optional.of(QuoteType.DOUBLE);
        else if (valueType == Character.class)
            return Optional.of(QuoteType.SINGLE);
        
        return Optional.empty();
    }
    
    public enum LOS {Vanilla, Boom}

    private void updateConfig() {
        SETTINGS_MAP.compute(configBase, (c, list) -> {
            if (list == null) {
                list = EnumSet.of(this);
            } else {
                list.add(this);
            }
            
            return list;
        });
    }
}
