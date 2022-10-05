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

import org.bleachhack.util.doom.doom.CVarManager;
import org.bleachhack.util.doom.doom.CommandVariable;
import org.bleachhack.util.doom.doom.DoomMain;
import org.bleachhack.util.doom.mochadoom.Engine;
import java.awt.Transparency;
import java.util.function.Function;
import org.bleachhack.util.doom.m.Settings;
import org.bleachhack.util.doom.rr.SceneRenderer;
import org.bleachhack.util.doom.v.DoomGraphicSystem;

/**
 * This class helps to choose proper components for bit depth
 * selected in config or through use of command line arguments
 */
public enum BppMode {
    Indexed(5, BufferedRenderer::new, BppMode::SceneGen_8, Transparency.OPAQUE),
    HiColor(5, BufferedRenderer16::new, BppMode::SceneGen_16, Transparency.OPAQUE),
    TrueColor(8, BufferedRenderer32::new, BppMode::SceneGen_32, Transparency.OPAQUE),
    AlphaTrueColor(8, BufferedRenderer32::new, BppMode::SceneGen_32, Transparency.TRANSLUCENT);
    
    public final int transparency;
    public final int lightBits;
    final RenderGen<?, ?> renderGen;
    final ScenerGen<?, ?> scenerGen;

    private <T, V> BppMode(int lightBits, RenderGen<T, V> renderGen, ScenerGen<T, V> scenerGen, int transparency) {
        this.lightBits = lightBits;
        this.renderGen = renderGen;
        this.scenerGen = scenerGen;
        this.transparency = transparency;
    }
    
    @SuppressWarnings("unchecked")
	public <T, V> DoomGraphicSystem<T, V> graphics(RendererFactory.WithWadLoader<T, V> rf) {
        return ((RenderGen<T, V>) renderGen).apply(rf);
    }

    @SuppressWarnings("unchecked")
	public <T, V> SceneRenderer<T, V> sceneRenderer(DoomMain<T, V> DOOM) {
        return ((ScenerGen<T, V>) scenerGen).apply(DOOM);
    }

    public static BppMode chooseBppMode(CVarManager CVM) {
        if (CVM.bool(CommandVariable.TRUECOLOR)) {
            return TrueColor;
        } else if (CVM.bool(CommandVariable.HICOLOR)) {
            return HiColor;
        } else if (CVM.bool(CommandVariable.INDEXED)) {
            return Indexed;
        } else if (CVM.bool(CommandVariable.ALPHATRUECOLOR)) {
            return AlphaTrueColor;
        } else {
            return Engine.getConfig().getValue(Settings.color_depth, BppMode.class);
        }
    }
    
    @SuppressWarnings("unchecked")
	private static <T, V> SceneRenderer<T, V> SceneGen_8(DoomMain<T, V> DOOM) {
        return (SceneRenderer<T, V>) SceneRendererMode.getMode().indexedGen.apply((DoomMain<byte[], byte[]>) DOOM);
    }
    
    @SuppressWarnings("unchecked")
    private static <T, V> SceneRenderer<T, V> SceneGen_16(DoomMain<T, V> DOOM) {
        return (SceneRenderer<T, V>) SceneRendererMode.getMode().hicolorGen.apply((DoomMain<byte[], short[]>) DOOM);
    }
    
    @SuppressWarnings("unchecked")
    private static <T, V> SceneRenderer<T, V> SceneGen_32(DoomMain<T, V> DOOM) {
        return (SceneRenderer<T, V>) SceneRendererMode.getMode().truecolorGen.apply((DoomMain<byte[], int[]>) DOOM);
    }
    
    interface ScenerGen<T, V> extends Function<DoomMain<T, V>, SceneRenderer<T, V>> {}
    interface RenderGen<T, V> extends Function<RendererFactory.WithWadLoader<T, V>, SoftwareGraphicsSystem<T, V>> {}
}
