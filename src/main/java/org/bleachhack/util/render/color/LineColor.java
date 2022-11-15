/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.util.render.color;

import com.mojang.datafixers.util.Function4;

public class LineColor extends RenderColor implements Cloneable{

	private Function4<Float, Float, Float, Integer, int[]> getColorFunc;

	public static LineColor single(float red, float green, float blue, float alpha) {
		return LineColor.single((int) (red * 255f), (int) (green * 255f), (int) (blue * 255f), (int) (alpha * 255f));
	}

	public static LineColor single(int color) {
		return LineColor.single((color & 0xff0000) >> 16, (color & 0xff00) >> 8, color & 0xff, color >> 24 & 0xff);
	}

	public static LineColor single(int red, int green, int blue, int alpha) {
		return new LineColor((x, y, z, curVertex) -> new int[] { red, green, blue, alpha });
	}

	public static LineColor gradient(float red1, float green1, float blue1, float alpha1, float red2, float green2, float blue2, float alpha2) {
		return LineColor.gradient(
				(int) (red1 * 255f), (int) (green1 * 255f), (int) (blue1 * 255f), (int) (alpha1 * 255f),
				(int) (red2 * 255f), (int) (green2 * 255f), (int) (blue2 * 255f), (int) (alpha2 * 255f));
	}

	public static LineColor gradient(int color1, int color2) {
		return LineColor.gradient(
				(color1 & 0xff0000) >> 16, (color1 & 0xff00) >> 8, color1 & 0xff, color1 >> 24 & 0xff,
				(color2 & 0xff0000) >> 16, (color2 & 0xff00) >> 8, color2 & 0xff, color2 >> 24 & 0xff);
	}

	public static LineColor gradient(int red1, int green1, int blue1, int alpha1, int red2, int green2, int blue2, int alpha2) {
		return new LineColor((x, y, z, curVertex) -> {
			if (curVertex == 0) {
				return new int[] { red1, green1, blue1, alpha1 };
			}

			return new int[] { red2, green2, blue2, alpha2 };
		});
	}

	private LineColor(Function4<Float, Float, Float, Integer, int[]> getColorFunc) {
		this.getColorFunc = getColorFunc;
	}

	public int[] getColor(float x, float y, float z, int curVertex) {
		int[] outColor = getColorFunc.apply(x, y, z, curVertex);

		for (int i = 0; i < 4; i++) {
			if (overwriteColor[i] != null) {
				outColor[i] = overwriteColor[i];
			}
		}

		return outColor;
	}

	public LineColor clone() {
		LineColor newColor = new LineColor(getColorFunc);
		cloneOverwriteTo(newColor);

		return newColor;
	}

}
