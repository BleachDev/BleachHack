/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.util.render.color;

import java.util.function.Function;

public class QuadColor extends RenderColor {

	private Function<Integer, int[]> getColorFunc;

	public static QuadColor single(float red, float green, float blue, float alpha) {
		return QuadColor.single((int) (red * 255f), (int) (green * 255f), (int) (blue * 255f), (int) (alpha * 255f));
	}

	public static QuadColor single(int color) {
		return QuadColor.single((color & 0xff0000) >> 16, (color & 0xff00) >> 8, color & 0xff, color >> 24 & 0xff);
	}

	public static QuadColor single(int red, int green, int blue, int alpha) {
		return new QuadColor(curVertex -> new int[] { red, green, blue, alpha });
	}

	public static QuadColor gradient(float red1, float green1, float blue1, float alpha1, float red2, float green2, float blue2, float alpha2, CardinalDirection direction) {
		return QuadColor.gradient(
				(int) (red1 * 255f), (int) (green1 * 255f), (int) (blue1 * 255f), (int) (alpha1 * 255f),
				(int) (red2 * 255f), (int) (green2 * 255f), (int) (blue2 * 255f), (int) (alpha2 * 255f),
				direction);
	}

	public static QuadColor gradient(int color1, int color2, CardinalDirection direction) {
		return QuadColor.gradient(
				(color1 & 0xff0000) >> 16, (color1 & 0xff00) >> 8, color1 & 0xff, color1 >> 24 & 0xff,
				(color2 & 0xff0000) >> 16, (color2 & 0xff00) >> 8, color2 & 0xff, color2 >> 24 & 0xff,
				direction);
	}

	public static QuadColor gradient(int red1, int green1, int blue1, int alpha1, int red2, int green2, int blue2, int alpha2, CardinalDirection direction) {
		return new QuadColor(curVertex -> {
			if (direction.isStartVertex(curVertex)) {
				return new int[] { red1, green1, blue1, alpha1 };
			}

			return new int[] { red2, green2, blue2, alpha2 };
		});
	}

	public static QuadColor custom(float red1, float green1, float blue1, float alpha1, float red2, float green2, float blue2, float alpha2, float red3, float green3, float blue3, float alpha3, float red4, float green4, float blue4, float alpha4) {
		return QuadColor.custom(
				(int) (red1 * 255f), (int) (green1 * 255f), (int) (blue1 * 255f), (int) (alpha1 * 255f),
				(int) (red2 * 255f), (int) (green2 * 255f), (int) (blue2 * 255f), (int) (alpha2 * 255f),
				(int) (red3 * 255f), (int) (green3 * 255f), (int) (blue3 * 255f), (int) (alpha3 * 255f),
				(int) (red4 * 255f), (int) (green4 * 255f), (int) (blue4 * 255f), (int) (alpha4 * 255f));
	}

	public static QuadColor custom(int color1, int color2, int color3, int color4) {
		return QuadColor.custom(
				(color1 & 0xff0000) >> 16, (color1 & 0xff00) >> 8, color1 & 0xff, color1 >> 24 & 0xff,
				(color2 & 0xff0000) >> 16, (color2 & 0xff00) >> 8, color2 & 0xff, color2 >> 24 & 0xff,
				(color3 & 0xff0000) >> 16, (color3 & 0xff00) >> 8, color3 & 0xff, color3 >> 24 & 0xff,
				(color4 & 0xff0000) >> 16, (color4 & 0xff00) >> 8, color4 & 0xff, color4 >> 24 & 0xff);
	}

	public static QuadColor custom(int red1, int green1, int blue1, int alpha1, int red2, int green2, int blue2, int alpha2, int red3, int green3, int blue3, int alpha3, int red4, int green4, int blue4, int alpha4) {
		return new QuadColor(curVertex -> {
			return switch (curVertex) {
				case 0 -> new int[] {red1, green1, blue1, alpha1};
				case 1 -> new int[] {red2, green2, blue2, alpha2};
				case 2 -> new int[] {red3, green3, blue3, alpha3};
				default -> new int[] {red4, green4, blue4, alpha4};
			};
		});
	}

	private QuadColor(Function<Integer, int[]> getColorFunc) {
		this.getColorFunc = getColorFunc;
	}

	public int[] getColor(int curVertex) {
		int[] outColor = getColorFunc.apply(curVertex);

		for (int i = 0; i < 4; i++) {
			if (overwriteColor[i] != null) {
				outColor[i] = overwriteColor[i];
			}
		}

		return outColor;
	}

	public int[] getAllColors() {
		int[] outColor = new int[16];

		for (int i = 0; i < 4; i++) {
			System.arraycopy(getColor(i), 0, outColor, i * 4, 4);
		}

		return outColor;
	}

	public QuadColor clone() {
		QuadColor newColor = new QuadColor(getColorFunc);
		cloneOverwriteTo(newColor);

		return newColor;
	}

	public enum CardinalDirection {
		NORTH(3, 0),
		EAST(0, 1),
		SOUTH(1, 2),
		WEST(2, 3);

		public final int vertex1;
		public final int vertex2;

		CardinalDirection(int vertex1, int vertex2) {
			this.vertex1 = vertex1;
			this.vertex2 = vertex2;
		}

		public boolean isStartVertex(int vertex) {
			return vertex == vertex1 || vertex == vertex2;
		}
	}

}
