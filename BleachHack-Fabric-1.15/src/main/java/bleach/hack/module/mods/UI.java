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

import bleach.hack.BleachHack;
import bleach.hack.event.events.EventDrawOverlay;
import bleach.hack.event.events.EventReadPacket;
import bleach.hack.gui.clickgui.SettingMode;
import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.gui.clickgui.SettingToggle;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.module.ModuleManager;
import bleach.hack.utils.FabricReflect;
import com.google.common.eventbus.Subscribe;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.dimension.DimensionType;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

public class UI extends Module {
	
	public List<String> infoList = new ArrayList<>();
	private int count = 0;
	private long prevTime = 0;
	private double tps = 20;
	private long lastPacket = 0;
	
	public UI() {
		super("UI", KEY_UNBOUND, Category.RENDER, "Shows stuff onscreen.",
				new SettingToggle("Arraylist", true), // 0
				new SettingToggle("Extra Line", false), // 1
				new SettingToggle("Watermark", true), // 2
				new SettingToggle("FPS", true), // 3
				new SettingToggle("Ping", true), // 4
				new SettingToggle("Coords", true), // 5
				new SettingToggle("TPS", true), // 6
				new SettingToggle("Lag-Meter", true), // 7
				new SettingToggle("Server", false), // 8
				new SettingToggle("Players", false), // 9
				new SettingToggle("Armor", true), // 10
				new SettingToggle("TimeStamp", false), // 11
				new SettingSlider("HueBright: ", 0, 1, 1, 2), // 12
				new SettingSlider("HueSat: ", 0, 1, 0.5, 2), // 13
				new SettingSlider("HueSpeed: ", 0.1, 50, 10, 1), // 14
				new SettingMode("Info: ", "Down Left", "Top Right", "Down Right")); // 15
	}
	
	@Subscribe
	public void onDrawOverlay(EventDrawOverlay event) {
		infoList.clear();
		
		if (getSettings().get(0).toToggle().state && !mc.options.debugEnabled) {
			List<String> lines = new ArrayList<>();
			for (Module m: ModuleManager.getModules()) if (m.isToggled()) lines.add(m.getName());
			
			lines.sort((a, b) -> Integer.compare(mc.textRenderer.getStringWidth(b), mc.textRenderer.getStringWidth(a)));
			if (getSettings().get(2).toToggle().state) lines.add(0, "§a> BleachHack " + BleachHack.VERSION);
			
			//new colors
			int color = getRainbowFromSettings(0);
			count = 0;
			int extra = getSettings().get(1).toToggle().state ? 1 : 0;
			for (String s: lines) {
				color = getRainbowFromSettings(count);
				InGameHud.fill(0, count*10, mc.textRenderer.getStringWidth(s)+3+extra, 10+(count*10), 0x70003030);
				InGameHud.fill(0, count*10, extra, 10+(count*10), color);
				InGameHud.fill(mc.textRenderer.getStringWidth(s)+3+extra, (count*10), mc.textRenderer.getStringWidth(s)+4+extra, 10+(count*10), color);
				if (count + 1 < lines.size()) {
					InGameHud.fill(mc.textRenderer.getStringWidth(lines.get(count + 1))+4+extra, 10+(count*10),
							mc.textRenderer.getStringWidth(s)+4+extra, 11+(count*10), color);
				}
				mc.textRenderer.drawWithShadow(s, 2+extra, 1+(count*10), color);
				count++;
			}
			
			InGameHud.fill(0, (count*10), mc.textRenderer.getStringWidth(lines.get(count-1))+4+extra, 1+(count*10), color);
		}

		if (getSettings().get(11).toToggle().state) {
			infoList.add("§7Time: §e" + Calendar.getInstance(TimeZone.getDefault()).getTime().toString());
		}
		
		if (getSettings().get(5).toToggle().state) {
			boolean nether = mc.player.dimension == DimensionType.THE_NETHER;
			BlockPos pos = mc.player.getBlockPos();
			Vec3d vec = mc.player.getPos();
			BlockPos pos2 = nether ? new BlockPos(vec.getX()*8, vec.getY(), vec.getZ()*8)
					: new BlockPos(vec.getX()/8, vec.getY(), vec.getZ()/8);
			
			infoList.add("XYZ: " + (nether ? "§4" : "§b") + pos.getX() + " " + pos.getY() + " " + pos.getZ()
			+ " §7[" + (nether ? "§b" : "§4") + pos2.getX() + " " + pos2.getY() + " " + pos2.getZ() + "§7]");
		}
		
		if (getSettings().get(3).toToggle().state) {
			int fps = (int) FabricReflect.getFieldValue(MinecraftClient.getInstance(), "field_1738", "currentFps");
			infoList.add("FPS: " + getColorString(fps, 120, 60, 30, 15, 10, false) + fps);
		}
		
		if (getSettings().get(4).toToggle().state) {
			int ping = 0;
			try{ ping = mc.getNetworkHandler().getPlayerListEntry(mc.player.getGameProfile().getId()).getLatency(); } catch (Exception e) {}
			infoList.add("Ping: " + getColorString(ping, 75, 180, 300, 500, 1000, true) + ping);
		}
		
		if (getSettings().get(6).toToggle().state) {
			String suffix = "§7";
			if (lastPacket + 7500 < System.currentTimeMillis()) suffix += "....";
			else if (lastPacket + 5000 < System.currentTimeMillis()) suffix += "...";
			else if (lastPacket + 2500 < System.currentTimeMillis()) suffix += "..";
			else if (lastPacket + 1200 < System.currentTimeMillis()) suffix += ".";
			
			infoList.add("TPS: " + getColorString((int) tps, 18, 15, 12, 8, 4, false) + tps + suffix);
		}

		if (getSettings().get(7).toToggle().state && !(mc.world.getServer() == null || mc.world.getServer().isSinglePlayer())) {
			long time = System.currentTimeMillis();
			if (time - lastPacket > 500) {
				String text = "Server Lagging For: " + ((time - lastPacket) / 1000d) + "s";
				mc.textRenderer.drawWithShadow(text, mc.getWindow().getScaledWidth() / 2 - mc.textRenderer.getStringWidth(text) / 2,
						Math.min((time - lastPacket - 500) / 20 - 20, 10), 0xd0d0d0);
			}
		}

		if (getSettings().get(8).toToggle().state) {
			String server = "";
			try{ server = mc.getCurrentServerEntry().address; } catch (Exception e) {}
			InGameHud.fill(mc.getWindow().getScaledWidth() - mc.textRenderer.getStringWidth(server) - 4, 2, mc.getWindow().getScaledWidth() - 3, 12, 0xa0000000);
			mc.textRenderer.drawWithShadow(server, mc.getWindow().getScaledWidth() - mc.textRenderer.getStringWidth(server) - 3, 3, 0xb0b0b0);
		}
		
		if (getSettings().get(9).toToggle().state && !mc.options.debugEnabled) {
			InGameHud.fill(0, 3+(count*10), mc.textRenderer.getStringWidth("Players:")+4, 13+(count*10), 0x40000000);
			mc.textRenderer.drawWithShadow("Players:", 2, 4+count*10, 0xff0000);
			count++;
			
			for (Entity e: mc.world.getPlayers().stream().sorted(
					(a,b) -> Double.compare(mc.player.getPos().distanceTo(a.getPos()), mc.player.getPos().distanceTo(b.getPos())))
					.collect(Collectors.toList())) {
				if (e == mc.player) continue;
				
				String text = e.getDisplayName().asFormattedString() + " | " + 
						e.getBlockPos().getX() + " " + e.getBlockPos().getY() + " " + e.getBlockPos().getZ()
						+ " (" + Math.round(mc.player.getPos().distanceTo(e.getPos())) + "m)";
				
				InGameHud.fill(0, 3+(count*10), mc.textRenderer.getStringWidth(text)+4, 13+(count*10), 0x40000000);
				mc.textRenderer.drawWithShadow(text, 2, 4+count*10,
						0xf00000 + (int) Math.min(mc.player.getPos().distanceTo(e.getPos()) * 3, 255));
				count++;
			}
		}
		
		if (getSettings().get(10).toToggle().state && !mc.player.isCreative() && !mc.player.isSpectator()) {
			GL11.glPushMatrix();
			GL11.glEnable(GL11.GL_TEXTURE_2D);

	        int count = 0;
	        int x1 = mc.getWindow().getScaledWidth() / 2;
	        int y = mc.getWindow().getScaledHeight() -
	        		(mc.player.isInFluid(FluidTags.WATER) || mc.player.getAir() < mc.player.getMaxAir() ? 64 : 55);
	        for (ItemStack is : mc.player.inventory.armor) {
	            count++;
	            if (is.isEmpty()) continue;
	            int x = x1 - 90 + (9 - count) * 20 + 2;

	            GL11.glEnable(GL11.GL_DEPTH_TEST);
	            mc.getItemRenderer().zOffset = 200F;
	            mc.getItemRenderer().renderGuiItemIcon(is, x, y);
	            mc.getItemRenderer().zOffset = 0F;
	            GL11.glEnable(GL11.GL_TEXTURE_2D);
	            GL11.glDisable(GL11.GL_DEPTH_TEST);
	            
	            GL11.glPushMatrix();
	            GL11.glScaled(0.75, 0.75, 0.75);
	            String s = is.getCount() > 1 ? "x" + is.getCount() : "";
	            mc.textRenderer.drawWithShadow(s, (x + 19 - mc.textRenderer.getStringWidth(s)) * 1.333f, (y + 9) * 1.333f, 0xffffff);

	            if (is.isDamageable()) {
	            	String dur = is.getMaxDamage() - is.getDamage() + "";
		            int durcolor = 0x000000;
		            try{ durcolor = MathHelper.hsvToRgb(((float) (is.getMaxDamage() - is.getDamage()) / is.getMaxDamage()) / 3.0F, 1.0F, 1.0F); } catch (Exception e) {}
		    	    
		            mc.textRenderer.drawWithShadow(dur + "", (x + 10 - mc.textRenderer.getStringWidth(dur + "") / 2) * 1.333f, (y - 3) * 1.333f, durcolor);
	            }
	            
	            GL11.glPopMatrix();
	        }
	        GL11.glEnable(GL11.GL_DEPTH_TEST);
	        GL11.glPopMatrix();
		}


		int count2 = 0;
		int infoMode = getSettings().get(15).toMode().mode;
		for (String s: infoList) {
			mc.textRenderer.drawWithShadow(s, 
					infoMode == 0 ? 2 : mc.getWindow().getScaledWidth() - mc.textRenderer.getStringWidth(s) - 2,
					infoMode == 1 ? 2+(count2*10) : mc.getWindow().getScaledHeight()-9-(count2*10), 0xa0a0a0);
			count2++;
		}
	}
	
	@Subscribe
	public void readPacket(EventReadPacket event) {
		lastPacket = System.currentTimeMillis();
		if (event.getPacket() instanceof WorldTimeUpdateS2CPacket) {
			long time = System.currentTimeMillis();
			if (time < 500) return;
			long timeOffset = Math.abs(1000 - (time - prevTime)) + 1000;
			tps = Math.round(MathHelper.clamp(20 / ((double) timeOffset / 1000), 0, 20) * 100d) / 100d;
			prevTime = time;
		}
	}
	
	public String getColorString(int value, int best, int good, int mid, int bad, int worst, boolean rev) {
		if (!rev ? value > best : value < best) return "§2";
		else if (!rev ? value > good : value < good) return "§a";
		else if (!rev ? value > mid : value < mid) return "§e";
		else if (!rev ? value > bad : value < bad) return "§6";
		else if (!rev ? value > worst : value < worst) return "§c";
		else return "§4";
	}

	public static int getRainbow(float sat, float bri, double speed, int offset) {
		double rainbowState = Math.ceil((System.currentTimeMillis() + offset) / speed);
		rainbowState %= 360.0;
		return Color.HSBtoRGB((float) (rainbowState / 360.0), sat, bri);
	}

	public static int getRainbowFromSettings(int offset) {
		Module ui = ModuleManager.getModule(UI.class);
		
		if (ui == null) return getRainbow(0.5f, 0.5f, 10, 0);
		
		return getRainbow((float) ui.getSettings().get(13).toSlider().getValue(),
				(float) ui.getSettings().get(12).toSlider().getValue(),
				ui.getSettings().get(14).toSlider().getValue(),
				offset);
	}
}
