/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
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
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.DeflaterOutputStream;

import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.systems.RenderSystem;

import bleach.hack.BleachHack;
import bleach.hack.event.events.EventDrawOverlay;
import bleach.hack.event.events.EventReadPacket;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.module.ModuleManager;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.util.BleachLogger;
import bleach.hack.util.FabricReflect;
import bleach.hack.util.world.ClientChunkSerializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class UI extends Module {

	public List<String> infoList = new ArrayList<>();
	private long prevTime = 0;
	private double tps = 20;
	private long lastPacket = 0;

	private long chunkTimer = 0;
	private int chunkSize = 0;

	public UI() {
		super("UI", KEY_UNBOUND, Category.RENDER, true, "Shows stuff onscreen.",
				new SettingToggle("Modulelist", true).withDesc("Shows the module list").withChildren( // 0
						new SettingToggle("Extra Line", true).withDesc("Adds an extra line to the front of the module list"), // 0-0
						new SettingToggle("Outer Line", false).withDesc("Adds an outer line to the module list"), // 0-1
						new SettingToggle("Fill", true).withDesc("Adds a black fill behind the module list"), // 0-2
						new SettingToggle("Watermark", true).withDesc("Adds the BleachHack watermark to the module list"), // 0-3
						new SettingSlider("HueBright", 0, 1, 1, 2).withDesc("Rainbow Hue"), // 0-4
						new SettingSlider("HueSat", 0, 1, 0.5, 2).withDesc("Rainbow Saturation"), // 0-5
						new SettingSlider("HueSpeed", 0.1, 50, 25, 1).withDesc("Rainbow Speed")), // 0-6
				new SettingToggle("Info", true).withDesc("Shows info/stats in a corner of the screen").withChildren( // 1
						new SettingToggle("FPS", true).withDesc("Shows your FPS"), // 1-0
						new SettingToggle("Ping", true).withDesc("Shows your ping"), // 1-1
						new SettingToggle("Coords", true).withDesc("Shows your coords and nether coords"), // 1-2
						new SettingToggle("TPS", true).withDesc("Shows the estimated server tps"), // 1-3
						new SettingToggle("Server", false).withDesc("Shows the current server you are on"), // 1-4
						new SettingToggle("TimeStamp", false).withDesc("Shows the current time").withChildren( // 1-5
								new SettingToggle("Time Zone", true).withDesc("Shows your time zone in the time"), // 1-5-0
								new SettingToggle("Year", false).withDesc("Shows the current year in the time")), // 1-5-1
						new SettingToggle("Chunk Size", false).withDesc("Shows the data size of the chunk you are standing in"), // 1-6
						new SettingMode("Corner", "BL", "TR", "BR").withDesc("Where on the screen to show the info")), // 1-7
				new SettingToggle("Players", false).withDesc("Lists all the players in your render distance"), //2
				new SettingToggle("Armor", true).withDesc("Shows your current armor").withChildren( // 3
						new SettingMode("Damage", "Number", "Bar", "Both").withDesc("How to show the armor durability")), // 3-0
				new SettingToggle("Lag-Meter", true).withDesc("Shows when the server isn't responding")); // 4
	}

	@Subscribe
	public void onDrawOverlay(EventDrawOverlay event) {
		infoList.clear();

		int arrayCount = 0;
		if (getSetting(0).asToggle().state && !mc.options.debugEnabled) {
			List<String> lines = new ArrayList<>();

			if (getSetting(0).asToggle().getChild(3).asToggle().state) {
				lines.add(0, "\u00a7a> BleachHack " + BleachHack.VERSION);
			}

			if (getSetting(0).asToggle().state) {
				for (Module m : ModuleManager.getModules())
					if (m.isEnabled())
						lines.add(m.getName());

				lines.sort((a, b) -> Integer.compare(mc.textRenderer.getWidth(b), mc.textRenderer.getWidth(a)));
			}

			int extra = getSetting(0).asToggle().getChild(0).asToggle().state ? 1 : 0;
			boolean outer = getSetting(0).asToggle().getChild(1).asToggle().state;
			boolean fill = getSetting(0).asToggle().getChild(2).asToggle().state;
			for (String s : lines) {
				int color = getRainbowFromSettings(arrayCount * 40);

				if (fill) {
					DrawableHelper.fill(event.matrix, 0, arrayCount * 10, mc.textRenderer.getWidth(s) + 3 + extra, 10 + (arrayCount * 10), 0x70003030);
				}

				if (extra == 1) {
					DrawableHelper.fill(event.matrix, 0, arrayCount * 10, extra, 10 + (arrayCount * 10), color);
				}

				if (outer) {
					DrawableHelper.fill(event.matrix, mc.textRenderer.getWidth(s) + 3 + extra, (arrayCount * 10), mc.textRenderer.getWidth(s) + 4 + extra,
							10 + (arrayCount * 10), color);

					if (arrayCount + 1 < lines.size()) {
						DrawableHelper.fill(event.matrix, mc.textRenderer.getWidth(lines.get(arrayCount + 1)) + 4 + extra, 10 + (arrayCount * 10),
								mc.textRenderer.getWidth(s) + 4 + extra, 11 + (arrayCount * 10), color);
					}
				}

				mc.textRenderer.drawWithShadow(event.matrix, s, 2 + extra, 1 + (arrayCount * 10), color);
				arrayCount++;
			}

			if (outer && !lines.isEmpty()) {
				DrawableHelper.fill(event.matrix,
						0, (arrayCount * 10),
						mc.textRenderer.getWidth(lines.get(arrayCount - 1)) + 4 + extra, 1 + (arrayCount * 10),
						getRainbowFromSettings(arrayCount * 40));
			}
		}

		if (getSetting(2).asToggle().state && !mc.options.debugEnabled) {
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

		if (getSetting(1).asToggle().state) {
			if (getSetting(1).asToggle().getChild(5).asToggle().state) {
				infoList.add("\u00a77Time: \u00a7e" + new SimpleDateFormat("MMM dd HH:mm:ss"
						+ (getSetting(1).asToggle().getChild(5).asToggle().getChild(0).asToggle().state ? " zzz" : "")
						+ (getSetting(1).asToggle().getChild(5).asToggle().getChild(1).asToggle().state ? " yyyy" : "")).format(new Date()));
			}

			if (getSetting(1).asToggle().getChild(2).asToggle().state) {
				boolean nether = mc.world.getRegistryKey().getValue().getPath().contains("nether");
				BlockPos pos = mc.player.getBlockPos();
				Vec3d vec = mc.player.getPos();
				BlockPos pos2 = nether ? new BlockPos(vec.getX() * 8, vec.getY(), vec.getZ() * 8)
						: new BlockPos(vec.getX() / 8, vec.getY(), vec.getZ() / 8);

				infoList.add("XYZ: " + (nether ? "\u00a74" : "\u00a7b") + pos.getX() + " " + pos.getY() + " " + pos.getZ()
				+ " \u00a77[" + (nether ? "\u00a7b" : "\u00a74") + pos2.getX() + " " + pos2.getY() + " " + pos2.getZ() + "\u00a77]");
			}

			if (getSetting(1).asToggle().getChild(4).asToggle().state) {
				String server = mc.getCurrentServerEntry() == null ? "Singleplayer" : mc.getCurrentServerEntry().address;
				infoList.add("\u00a77Server: \u00a7d" + server);
			}

			if (getSetting(1).asToggle().getChild(6).asToggle().state) {
				infoList.add("Chunk: \u00a7f" + (chunkSize < 1000 ? chunkSize + "B" : chunkSize / 1000d + "KB"));

				if (System.currentTimeMillis() - 1500 > chunkTimer) {
					chunkTimer = System.currentTimeMillis();

					if (mc.world.getWorldChunk(mc.player.getBlockPos()) != null) {
						new Thread(() -> {
							NbtCompound tag = ClientChunkSerializer.serialize(mc.world, mc.world.getWorldChunk(mc.player.getBlockPos()));
							DataOutputStream output = new DataOutputStream(
									new BufferedOutputStream(new DeflaterOutputStream(new ByteArrayOutputStream(8096))));
							try {
								NbtIo.writeCompressed(tag, output);
							} catch (IOException e) {
								BleachLogger.errorMessage("[ChunkSize] Error serializing chunk");
							}

							chunkSize = output.size();
						}).start();
					}
				}
			}

			if (getSetting(1).asToggle().getChild(0).asToggle().state) {
				int fps = (int) FabricReflect.getFieldValue(MinecraftClient.getInstance(), "field_1738", "currentFps");
				infoList.add("FPS: " + getColorString(fps, 120, 60, 30, 15, 10, false) + fps);
			}

			if (getSetting(1).asToggle().getChild(1).asToggle().state) {
				PlayerListEntry playerEntry = mc.player.networkHandler.getPlayerListEntry(mc.player.getGameProfile().getId());
				int ping = playerEntry == null ? 0 : playerEntry.getLatency();
				infoList.add("Ping: " + getColorString(ping, 75, 180, 300, 500, 1000, true) + ping);
			}

			if (getSetting(1).asToggle().getChild(3).asToggle().state) {
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
		}

		if (getSetting(4).asToggle().state) {
			long time = System.currentTimeMillis();
			if (time - lastPacket > 500) {
				String text = "Server Lagging For: " + ((time - lastPacket) / 1000d) + "s";
				mc.textRenderer.drawWithShadow(event.matrix, text, mc.getWindow().getScaledWidth() / 2 - mc.textRenderer.getWidth(text) / 2,
						Math.min((time - lastPacket - 500) / 20 - 20, 10), 0xd0d0d0);
			}
		}

		if (getSetting(3).asToggle().state && !mc.player.isCreative() && !mc.player.isSpectator()) {
			int count = 0;
			int x1 = mc.getWindow().getScaledWidth() / 2;
			int y = mc.getWindow().getScaledHeight() -
					(mc.player.isSubmergedInWater() || mc.player.getAir() < mc.player.getMaxAir() ? 64 : 55);
			for (ItemStack is : mc.player.getInventory().armor) {
				count++;
				if (is.isEmpty())
					continue;
				int x = x1 - 90 + (9 - count) * 20 + 2;

				RenderSystem.enableDepthTest();
				mc.getItemRenderer().zOffset = 200F;
				mc.getItemRenderer().renderGuiItemIcon(is, x, y);

				if (getSetting(3).asToggle().getChild(0).asMode().mode > 0) {
					mc.getItemRenderer().renderGuiItemOverlay(mc.textRenderer, is, x, y);
				}

				mc.getItemRenderer().zOffset = 0F;

				if (getSetting(3).asToggle().getChild(0).asMode().mode != 1) {
					event.matrix.push();
					event.matrix.scale(0.75f, 0.75f, 1f);
					RenderSystem.disableDepthTest();

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

					RenderSystem.enableDepthTest();
					event.matrix.pop();
				}
			}
		}

		int count2 = 0;
		int infoMode = getSetting(1).asToggle().getChild(7).asMode().mode;
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
		if (!rev ? value > best : value < best) {
			return "\u00a72";
		} else if (!rev ? value > good : value < good) {
			return "\u00a7a";
		} else if (!rev ? value > mid : value < mid) {
			return "\u00a7e";
		} else if (!rev ? value > bad : value < bad) {
			return "\u00a76";
		} else if (!rev ? value > worst : value < worst) {
			return "\u00a7c";
		} else {
			return "\u00a74";
		}
	}

	public static int getRainbow(float sat, float bri, double speed, int offset) {
		double rainbowState = Math.ceil((System.currentTimeMillis() + offset) / speed);
		rainbowState %= 360.0;
		return Color.HSBtoRGB((float) (rainbowState / 360.0), sat, bri);
	}

	public static int getRainbowFromSettings(int offset) {
		// What the fuck
		// shhh
		Module ui = ModuleManager.getModule("UI");

		if (ui == null)
			return getRainbow(0.5f, 0.5f, 10, 0);

		return getRainbow(
				(float) ui.getSetting(0).asToggle().getChild(5).asSlider().getValue(),
				(float) ui.getSetting(0).asToggle().getChild(4).asSlider().getValue(),
				ui.getSetting(0).asToggle().getChild(6).asSlider().getValue(),
				offset);
	}
}
