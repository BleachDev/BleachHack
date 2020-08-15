/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/bleachhack-1.14/).
 * Copyright (c) 2019 Bleach.
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
package bleach.hack.event.events;

import bleach.hack.event.Event;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;

public class EventDrawContainer extends Event {

    private final HandledScreen<?> screen;
    public int mouseX;
    public int mouseY;
    public MatrixStack matrix;

    public EventDrawContainer(HandledScreen<?> screen, int mX, int mY, MatrixStack matrix) {
        this.screen = screen;
        this.mouseX = mX;
        this.mouseY = mY;
        this.matrix = matrix;
    }

    public Screen getScreen() {
        return screen;
    }
}
