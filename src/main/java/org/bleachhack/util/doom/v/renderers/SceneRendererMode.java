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
package org.bleachhack.util.doom.v.renderers;

import org.bleachhack.util.doom.doom.CommandVariable;
import org.bleachhack.util.doom.doom.DoomMain;
import java.util.function.Function;
import org.bleachhack.util.doom.m.Settings;
import org.bleachhack.util.doom.mochadoom.Engine;
import org.bleachhack.util.doom.rr.SceneRenderer;
import org.bleachhack.util.doom.rr.UnifiedRenderer;
import org.bleachhack.util.doom.rr.parallel.ParallelRenderer;
import org.bleachhack.util.doom.rr.parallel.ParallelRenderer2;

/**
 * This class helps to choose between scene renderers
 */
public enum SceneRendererMode {
    Serial(UnifiedRenderer.Indexed::new, UnifiedRenderer.HiColor::new, UnifiedRenderer.TrueColor::new),
    Parallel(SceneRendererMode::Parallel_8, SceneRendererMode::Parallel_16, SceneRendererMode::Parallel_32),
    Parallel2(SceneRendererMode::Parallel2_8, SceneRendererMode::Parallel2_16, SceneRendererMode::Parallel2_32);
    
    private static final boolean cVarSerial = Engine.getCVM().bool(CommandVariable.SERIALRENDERER);
    private static final boolean cVarParallel = Engine.getCVM().present(CommandVariable.PARALLELRENDERER);
    private static final boolean cVarParallel2 = Engine.getCVM().present(CommandVariable.PARALLELRENDERER2);
    private static final int[] threads = cVarSerial ? null : cVarParallel
        ? parseSwitchConfig(CommandVariable.PARALLELRENDERER)
        : cVarParallel2
            ? parseSwitchConfig(CommandVariable.PARALLELRENDERER2)
            : new int[]{2, 2, 2};
            
    final SG<byte[], byte[]> indexedGen;
    final SG<byte[], short[]> hicolorGen;
    final SG<byte[], int[]> truecolorGen;

    private SceneRendererMode(SG<byte[], byte[]> indexed, SG<byte[], short[]> hi, SG<byte[], int[]> truecolor) {
        this.indexedGen = indexed;
        this.hicolorGen = hi;
        this.truecolorGen = truecolor;
    }
    
    static int[] parseSwitchConfig(CommandVariable sw) {
        // Try parsing walls, or default to 1
        final int walls = Engine.getCVM().get(sw, Integer.class, 0).orElse(1);
        // Try parsing floors. If wall succeeded, but floors not, it will default to 1.
        final int floors = Engine.getCVM().get(sw, Integer.class, 1).orElse(1);
        // In the worst case, we will use the defaults.
        final int masked = Engine.getCVM().get(sw, Integer.class, 2).orElse(2);
        return new int[]{walls, floors, masked};
    }
    
    static SceneRendererMode getMode() {
        if (cVarSerial) {
            /**
             * Serial renderer in command line argument will override everything else
             */
            return Serial;
        } else if (cVarParallel) {
            /**
             * The second-top priority switch is parallelrenderer (not 2) command line argument
             */
            return Parallel;
        } else if (cVarParallel2) {
            /**
             * If we have parallelrenderer2 on command line, it will still override config setting
             */
            return Parallel2;
        }

        /**
         * We dont have overrides on command line - get mode from default.cfg (or whatever)
         * Set default parallelism config in this case
         * TODO: make able to choose in config, but on ONE line along with scene_renderer_mode, should be tricky!
         */
        return Engine.getConfig().getValue(Settings.scene_renderer_mode, SceneRendererMode.class);
    }
    
    private static SceneRenderer<byte[], byte[]> Parallel_8(DoomMain<byte[], byte[]> DOOM) {
        return new ParallelRenderer.Indexed(DOOM, threads[0], threads[1], threads[2]);
    }
    
    private static SceneRenderer<byte[], short[]> Parallel_16(DoomMain<byte[], short[]> DOOM) {
        return new ParallelRenderer.HiColor(DOOM, threads[0], threads[1], threads[2]);
    }
    
    private static SceneRenderer<byte[], int[]> Parallel_32(DoomMain<byte[], int[]> DOOM) {
        return new ParallelRenderer.TrueColor(DOOM, threads[0], threads[1], threads[2]);
    }
    
    private static SceneRenderer<byte[], byte[]> Parallel2_8(DoomMain<byte[], byte[]> DOOM) {
        return new ParallelRenderer2.Indexed(DOOM, threads[0], threads[1], threads[2]);
    }
    
    private static SceneRenderer<byte[], short[]> Parallel2_16(DoomMain<byte[], short[]> DOOM) {
        return new ParallelRenderer2.HiColor(DOOM, threads[0], threads[1], threads[2]);
    }
    
    private static SceneRenderer<byte[], int[]> Parallel2_32(DoomMain<byte[], int[]> DOOM) {
        return new ParallelRenderer2.TrueColor(DOOM, threads[0], threads[1], threads[2]);
    }
    
    interface SG<T, V> extends Function<DoomMain<T, V>, SceneRenderer<T, V>> {}
}
