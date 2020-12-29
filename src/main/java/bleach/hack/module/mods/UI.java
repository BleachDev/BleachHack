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

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.lwjgl.opengl.GL11;

import com.google.common.eventbus.Subscribe;

import bleach.hack.BleachHack;
import bleach.hack.event.events.EventDrawOverlay;
import bleach.hack.event.events.EventReadPacket;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.module.ModuleManager;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.utils.FabricReflect;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class UI extends Module {

	public List<String> infoList = new ArrayList<>();
	private long prevTime = 0;
	private double tps = 20;
	private long lastPacket = 0;

	public UI() {
		super("UI", KEY_UNBOUND, Category.RENDER, "Shows stuff onscreen.",
				new SettingToggle("Arraylist", true).withDesc("Shows the module list"), // 0
				new SettingToggle("Extra Line", false).withDesc("Adds a extra line to the front of the arralist"), // 1
				new SettingToggle("Watermark", true).withDesc("Adds the BleachHack watermark to the arraylist"), // 2
				new SettingToggle("FPS", true).withDesc("Shows your FPS"), // 3
				new SettingToggle("Ping", true).withDesc("Shows your ping"), // 4
				new SettingToggle("Coords", true).withDesc("Shows your coords and nether coords"), // 5
				new SettingToggle("TPS", true).withDesc("Shows the estimated server tps"), // 6
				new SettingToggle("Lag-Meter", true).withDesc("Shows when the server is lagging"), // 7
				new SettingToggle("Server", false).withDesc("Shows the current server you are on"), // 8
				new SettingToggle("Players", false).withDesc("Lists all the players in your render distance"), // 9
				new SettingToggle("Armor", true).withDesc("Shows your current armor").withChildren( // 10
						new SettingMode("Damage", "Number", "Bar", "Both").withDesc("How to show the armor durability")),
				new SettingToggle("TimeStamp", false).withDesc("Shows the current time").withChildren( // 11
						new SettingToggle("Time Zone", true).withDesc("Shows your time zone in the time"),
						new SettingToggle("Year", false).withDesc("Shows the current year in the time")),
				new SettingSlider("HueBright", 0, 1, 1, 2).withDesc("Rainbow Hue"), // 12
				new SettingSlider("HueSat", 0, 1, 0.5, 2).withDesc("Rainbow Saturation"), // 13
				new SettingSlider("HueSpeed", 0.1, 50, 10, 1).withDesc("Rainbow Speed"), // 14
				new SettingMode("Info", "BL", "TR", "BR").withDesc("Where on the screan to show the info")); // 15
	}

	@Subscribe
	public void onDrawOverlay(EventDrawOverlay event) {
		infoList.clear();

		int arrayCount = 0;
		if ((getSetting(0).asToggle().state || getSetting(2).asToggle().state) && !mc.options.debugEnabled) {
			List<String> lines = new ArrayList<>();

			if (getSetting(2).asToggle().state)
				lines.add(0, "\u00a7a> BleachHack " + BleachHack.VERSION);

			if (getSetting(0).asToggle().state) {
				for (Module m : ModuleManager.getModules())
					if (m.isToggled())
						lines.add(m.getName());

				lines.sort((a, b) -> Integer.compare(mc.textRenderer.getWidth(b), mc.textRenderer.getWidth(a)));
			}

			// new colors
			int color = getRainbowFromSettings(0);
			int extra = getSetting(1).asToggle().state ? 1 : 0;
			for (String s : lines) {
				color = getRainbowFromSettings(arrayCount);
				DrawableHelper.fill(event.matrix, 0, arrayCount * 10, mc.textRenderer.getWidth(s) + 3 + extra, 10 + (arrayCount * 10), 0x70003030);
				DrawableHelper.fill(event.matrix, 0, arrayCount * 10, extra, 10 + (arrayCount * 10), color);
				DrawableHelper.fill(event.matrix, mc.textRenderer.getWidth(s) + 3 + extra, (arrayCount * 10), mc.textRenderer.getWidth(s) + 4 + extra,
						10 + (arrayCount * 10), color);

				if (arrayCount + 1 < lines.size()) {
					DrawableHelper.fill(event.matrix, mc.textRenderer.getWidth(lines.get(arrayCount + 1)) + 4 + extra, 10 + (arrayCount * 10),
							mc.textRenderer.getWidth(s) + 4 + extra, 11 + (arrayCount * 10), color);
				}

				mc.textRenderer.drawWithShadow(event.matrix, s, 2 + extra, 1 + (arrayCount * 10), color);
				arrayCount++;
			}

			if (!lines.isEmpty()) {
				DrawableHelper.fill(event.matrix, 0, (arrayCount * 10), mc.textRenderer.getWidth(lines.get(arrayCount - 1)) + 4 + extra, 1 + (arrayCount * 10), color);
			}
		}

		if (getSetting(9).asToggle().state && !mc.options.debugEnabled) {
			mc.textRenderer.drawWithShadow(event.matrix, "Players:", 2, 4 + arrayCount * 10, 0xff0000);
			arrayCount++;

			for (Entity e : mc.world.getPlayers().stream().sorted(
					(a, b) -> Double.compare(mc.player.getPos().distanceTo(a.getPos()), mc.player.getPos().distanceTo(b.getPos())))
					.collect(Collectors.toList())) {
				if (e == mc.player)
					continue;

				int dist = (int) Math.round(mc.player.getPos().distanceTo(e.getPos()));

				String text = "" + e.getDisplayName().getString() + " \u00a77|\u00a7r " +
						e.getBlockPos().getX() + " " + e.getBlockPos().getY() + " " + e.getBlockPos().getZ()
						+ " (" + dist + "m)";

				mc.textRenderer.drawWithShadow(event.matrix, text, 2, 4 + arrayCount * 10,
						new Color(255 - Math.min(dist * 3, 255), Math.min(dist * 3, 255), 0).brighter().getRGB());
				arrayCount++;
			}
		}

		if (getSetting(11).asToggle().state) {
			infoList.add("\u00a77Time: \u00a7e" + new SimpleDateFormat("MMM dd HH:mm:ss"
					+ (getSetting(11).asToggle().getChild(0).asToggle().state ? " zzz" : "")
					+ (getSetting(11).asToggle().getChild(1).asToggle().state ? " yyyy" : "")).format(new Date()));
		}

		if (getSetting(5).asToggle().state) {
			boolean nether = mc.world.getRegistryKey().getValue().getPath().contains("nether");
			BlockPos pos = mc.player.getBlockPos();
			Vec3d vec = mc.player.getPos();
			BlockPos pos2 = nether ? new BlockPos(vec.getX() * 8, vec.getY(), vec.getZ() * 8)
					: new BlockPos(vec.getX() / 8, vec.getY(), vec.getZ() / 8);

			infoList.add("XYZ: " + (nether ? "\u00a74" : "\u00a7b") + pos.getX() + " " + pos.getY() + " " + pos.getZ()
			+ " \u00a77[" + (nether ? "\u00a7b" : "\u00a74") + pos2.getX() + " " + pos2.getY() + " " + pos2.getZ() + "\u00a77]");
		}

		if (getSetting(8).asToggle().state) {
			String server = mc.getCurrentServerEntry() == null ? "Singleplayer" : mc.getCurrentServerEntry().address;
			infoList.add("\u00a77Server: \u00a7d" + server);
		}

		if (getSetting(3).asToggle().state) {
			int fps = (int) FabricReflect.getFieldValue(MinecraftClient.getInstance(), "field_1738", "currentFps");
			infoList.add("FPS: " + getColorString(fps, 120, 60, 30, 15, 10, false) + fps);
		}

		if (getSetting(4).asToggle().state) {
			PlayerListEntry playerEntry = mc.player.networkHandler.getPlayerListEntry(mc.player.getGameProfile().getId());
			int ping = playerEntry == null ? 0 : playerEntry.getLatency();
			infoList.add("Ping: " + getColorString(ping, 75, 180, 300, 500, 1000, true) + ping);
		}

		if (getSetting(6).asToggle().state) {
			String suffix = "\u00a77";
			if (lastPacket + 7500 < System.currentTimeMillis())
				suffix += "....";
			else if (lastPacket + 5000 < System.currentTimeMillis())
				suffix += "...";
			else if (lastPacket + 2500 < System.currentTimeMillis())
				suffix += "..";
			else if (lastPacket + 1200 < System.currentTimeMillis())
				suffix += ".";

			infoList.add("TPS: " + getColorString((int) tps, 18, 15, 12, 8, 4, false) + tps + suffix);
		}

		if (getSetting(7).asToggle().state) {
			long time = System.currentTimeMillis();
			if (time - lastPacket > 500) {
				String text = "Server Lagging For: " + ((time - lastPacket) / 1000d) + "s";
				mc.textRenderer.drawWithShadow(event.matrix, text, mc.getWindow().getScaledWidth() / 2 - mc.textRenderer.getWidth(text) / 2,
						Math.min((time - lastPacket - 500) / 20 - 20, 10), 0xd0d0d0);
			}
		}

		if (getSetting(10).asToggle().state && !mc.player.isCreative() && !mc.player.isSpectator()) {
			GL11.glPushMatrix();
			// GL11.glEnable(GL11.GL_TEXTURE_2D);

			int count = 0;
			int x1 = mc.getWindow().getScaledWidth() / 2;
			int y = mc.getWindow().getScaledHeight() -
					(mc.player.isSubmergedInWater() || mc.player.getAir() < mc.player.getMaxAir() ? 64 : 55);
			for (ItemStack is : mc.player.inventory.armor) {
				count++;
				if (is.isEmpty())
					continue;
				int x = x1 - 90 + (9 - count) * 20 + 2;

				GL11.glEnable(GL11.GL_DEPTH_TEST);
				mc.getItemRenderer().zOffset = 200F;
				mc.getItemRenderer().renderGuiItemIcon(is, x, y);

				if (getSetting(10).asToggle().getChild(0).asMode().mode > 0) {
					mc.getItemRenderer().renderGuiItemOverlay(mc.textRenderer, is, x, y);
				}

				mc.getItemRenderer().zOffset = 0F;
				GL11.glDisable(GL11.GL_DEPTH_TEST);

				if (getSetting(10).asToggle().getChild(0).asMode().mode != 1) {
					GL11.glPushMatrix();
					GL11.glScaled(0.75, 0.75, 0.75);
					String s = is.getCount() > 1 ? "x" + is.getCount() : "";
					mc.textRenderer.drawWithShadow(event.matrix, s, (x + 19 - mc.textRenderer.getWidth(s)) * 1.333f, (y + 9) * 1.333f, 0xffffff);

					if (is.isDamageable()) {
						String dur = is.getMaxDamage() - is.getDamage() + "";
						int durcolor = 0x000000;
						try {
							durcolor = MathHelper.hsvToRgb(((float) (is.getMaxDamage() - is.getDamage()) / is.getMaxDamage()) / 3.0F, 1.0F, 1.0F);
						} catch (Exception e) {
						}

						mc.textRenderer.drawWithShadow(event.matrix, dur, (x + 10 - mc.textRenderer.getWidth(dur) / 2) * 1.333f, (y - 3) * 1.333f, durcolor);
					}

					GL11.glPopMatrix();
				}
			}

			GL11.glEnable(GL11.GL_DEPTH_TEST);
			GL11.glPopMatrix();
		}

		int count2 = 0;
		int infoMode = getSetting(15).asMode().mode;
		for (String s : infoList) {
			mc.textRenderer.drawWithShadow(event.matrix, s,
					infoMode == 0 ? 2 : mc.getWindow().getScaledWidth() - mc.textRenderer.getWidth(s) - 2,
							infoMode == 1 ? 2 + (count2 * 10) : mc.getWindow().getScaledHeight() - 9 - (count2 * 10), 0xa0a0a0);
			count2++;
		}
	}

	@Subscribe
	public void readPacket(EventReadPacket event) {
		lastPacket = System.currentTimeMillis();
		if (event.getPacket() instanceof WorldTimeUpdateS2CPacket) {
			long time = System.currentTimeMillis();
			if (time < 500)
				return;
			long timeOffset = Math.abs(1000 - (time - prevTime)) + 1000;
			tps = Math.round(MathHelper.clamp(20 / ((double) timeOffset / 1000), 0, 20) * 100d) / 100d;
			prevTime = time;
		}
	}

	public String getColorString(int value, int best, int good, int mid, int bad, int worst, boolean rev) {
		if (!rev ? value > best : value < best)
			return "\u00a72";
		else if (!rev ? value > good : value < good)
			return "\u00a7a";
		else if (!rev ? value > mid : value < mid)
			return "\u00a7e";
		else if (!rev ? value > bad : value < bad)
			return "\u00a76";
		else if (!rev ? value > worst : value < worst)
			return "\u00a7c";
		else
			return "\u00a74";
	}

	public static int getRainbow(float sat, float bri, double speed, int offset) {
		double rainbowState = Math.ceil((System.currentTimeMillis() + offset) / speed);
		rainbowState %= 360.0;
		return Color.HSBtoRGB((float) (rainbowState / 360.0), sat, bri);
	}

	public static int getRainbowFromSettings(int offset) {
		Module ui = ModuleManager.getModule(UI.class);

		if (ui == null)
			return getRainbow(0.5f, 0.5f, 10, 0);

		return getRainbow((float) ui.getSetting(13).asSlider().getValue(),
				(float) ui.getSetting(12).asSlider().getValue(),
				ui.getSetting(14).asSlider().getValue(),
				offset);
	}
}
