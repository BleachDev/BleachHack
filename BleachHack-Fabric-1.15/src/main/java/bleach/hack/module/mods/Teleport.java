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
package bleach.hack.module.mods;

import bleach.hack.event.events.EventSendPacket;
import bleach.hack.event.events.EventTick;
import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.utils.BleachLogger;

import com.google.common.eventbus.Subscribe;

import net.minecraft.network.packet.c2s.login.LoginHelloC2SPacket;
import net.minecraft.util.math.Vec3d;

/**
 * @author Cosmic (pro skidder)
 */
public class Teleport extends Module {

	private long lastTp;
    private Vec3d lastPos;
    public static Vec3d finalPos;

    public Teleport() {
        super("Teleport", -1, Category.MISC, "What are you doing here?",
            new SettingSlider("BPT: ", 0.01, 20000, 1, 2));
    }

    @Subscribe
    public void sendPacket(EventSendPacket event) {
        if (event.getPacket() instanceof LoginHelloC2SPacket) {
            setToggled(false);
        }
    }

    @Subscribe
    public void onTick(EventTick event) {
    	if (finalPos == null) {
    		BleachLogger.errorMessage("Position not set, use .tp");
    		setToggled(false);
    		return;
    	}

        Vec3d tpDirectionVec = finalPos.subtract(mc.player.getPosVector()).normalize();

        int chunkX = (int) Math.floor(mc.player.getPosVector().x / 16.0D);
        int chunkZ = (int) Math.floor(mc.player.getPosVector().z / 16.0D);
        if (mc.world.isChunkLoaded(chunkX, chunkZ)) {
            lastPos = mc.player.getPosVector();
            if (finalPos.distanceTo(mc.player.getPosVector()) < 0.3 || getSettings().get(0).toSlider().getValue() == 0) {
                BleachLogger.infoMessage("Teleport Finished!");
                setToggled(false);
            } else {
                mc.player.setVelocity(0,0,0);
            }

            if (finalPos.distanceTo(mc.player.getPosVector()) >= getSettings().get(0).toSlider().getValue()) {
                final Vec3d vec = tpDirectionVec.multiply(getSettings().get(0).toSlider().getValue());
                mc.player.setPos(mc.player.getPos().getX() + vec.getX(), mc.player.getPos().getY() + vec.getY(), mc.player.getPos().getZ() + vec.getZ());
            } else {
                final Vec3d vec = tpDirectionVec.multiply(finalPos.distanceTo(mc.player.getPosVector()));
                mc.player.setPos(mc.player.getPosVector().getX() + vec.x, mc.player.getPosVector().getY() + vec.y, mc.player.getPosVector().getZ() + vec.z);
            }
            lastTp = System.currentTimeMillis();
        } else if (lastTp + 2000L > System.currentTimeMillis()) {
            mc.player.setPos(lastPos.x, lastPos.y, lastPos.z);
        }
    }

}
