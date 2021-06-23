/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.module.mods;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.zip.DeflaterOutputStream;

import org.apache.commons.lang3.tuple.Pair;

import bleach.hack.eventbus.BleachSubscribe;
import com.mojang.blaze3d.systems.RenderSystem;

import bleach.hack.BleachHack;
import bleach.hack.event.events.EventRenderInGameHud;
import bleach.hack.event.events.EventReadPacket;
import bleach.hack.gui.clickgui.UIClickGuiScreen;
import bleach.hack.module.ModuleCategory;
import bleach.hack.module.Module;
import bleach.hack.module.ModuleManager;
import bleach.hack.setting.base.SettingButton;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.util.BleachLogger;
import bleach.hack.util.FabricReflect;
import bleach.hack.util.world.ClientChunkSerializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.network.packet.s2c.play.WorldTimeUpdateS2CPacket;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class UI extends Module {

	public static UIClickGuiScreen uiScreen;

	private long prevTime = 0;
	private double tps = 20;
	private long lastPacket = 0;

	private int chunkSize;
	private long lastChunkTime;
	private ExecutorService chunkExecutor;
	private Pair<ChunkPos, Future<Integer>> chunkFuture;

	public UI() {
		super("UI", KEY_UNBOUND, ModuleCategory.RENDER, true, "Shows stuff onscreen.",
				new SettingToggle("Modulelist", true).withDesc("Shows the module list").withChildren( // 0
						new SettingToggle("Inner Line", true).withDesc("Adds an extra line to the front of the module list"), // 0-0
						new SettingToggle("Outer Line", false).withDesc("Adds an outer line to the module list"), // 0-1
						new SettingToggle("Fill", true).withDesc("Adds a black fill behind the module list"), // 0-2
						new SettingToggle("Watermark", true).withDesc("Adds the BleachHack watermark to the module list").withChildren( // 0-3
								new SettingMode("Mode", "New", "Old").withDesc("The watermark type")), // 0-3-0
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
						new SettingToggle("ChunkSize", false).withDesc("Shows the data size of the chunk you are standing in")), // 1-6
				new SettingToggle("Players", false).withDesc("Lists all the players in your render distance"), //2
				new SettingToggle("Armor", true).withDesc("Shows your current armor").withChildren( // 3
						new SettingMode("Damage", "Number", "Bar", "Both").withDesc("How to show the armor durability")), // 3-0
				new SettingToggle("Lag-Meter", true).withDesc("Shows when the server isn't responding").withChildren(
						new SettingMode("Animation", "Fall", "Fade", "None").withDesc("How to animate the lag meter when appearing")), // 4
				new SettingButton("Edit UI..", () -> MinecraftClient.getInstance().openScreen(uiScreen)));

		uiScreen = new UIClickGuiScreen(ClickGui.clickGui, this);
	}

	@Override
	public void onEnable() {
		super.onEnable();

		chunkExecutor = Executors.newSingleThreadExecutor();
	}

	@Override
	public void onDisable() {
		chunkExecutor.shutdownNow();

		super.onDisable();
	}

	@BleachSubscribe
	public void onDrawOverlay(EventRenderInGameHud event) {
		if (mc.currentScreen instanceof UIClickGuiScreen) {
			return;
		}

		// Shit way to keep the ui synced
		uiScreen.init(mc, mc.getWindow().getScaledWidth(), mc.getWindow().getScaledHeight());

		uiScreen.uiWindows.values().forEach(window -> {
			if (!window.shouldClose(this)) {
				window.renderUI(event.getMatrix());
			}
		});
	}

	// --- Module List

	public int[] getModuleListSize() {
		List<Text> lines = getModuleListText();

		if (lines.isEmpty()) {
			return new int[] { 0, 0 };
		}

		int inner = getSetting(0).asToggle().getChild(0).asToggle().state ? 1 : 0;
		int outer = getSetting(0).asToggle().getChild(1).asToggle().state ? 4 : 3;
		return new int[] { mc.textRenderer.getWidth(lines.get(0)) + inner + outer, lines.size() * 10};
	}

	public void drawModuleList(MatrixStack matrices, int x, int y) {
		List<Text> lines = getModuleListText();

		if (lines.isEmpty()) return;

		int arrayCount = 0;
		boolean inner = getSetting(0).asToggle().getChild(0).asToggle().state;
		boolean outer = getSetting(0).asToggle().getChild(1).asToggle().state;
		boolean fill = getSetting(0).asToggle().getChild(2).asToggle().state;
		boolean rightAlign = x + mc.textRenderer.getWidth(lines.get(0)) / 2 > mc.getWindow().getScaledWidth() / 2;

		int startX = rightAlign ? x + mc.textRenderer.getWidth(lines.get(0)) + 3 + (inner ? 1 : 0) + (outer ? 1 : 0) : x;
		for (Text t : lines) {
			int color = getRainbowFromSettings(arrayCount * 40);
			int textStart = (rightAlign ? startX - mc.textRenderer.getWidth(t) - 1 : startX + 2) + (inner ? 1 : 0) * (rightAlign ? -1 : 1);
			int outerX = rightAlign ? textStart - 3 : textStart + mc.textRenderer.getWidth(t) + 1;

			if (fill) {
				DrawableHelper.fill(matrices, rightAlign ? textStart - 2 : startX, y + arrayCount * 10, rightAlign ? startX : outerX, y + 10 + arrayCount * 10, 0x70003030);
			}

			if (inner) {
				DrawableHelper.fill(matrices, rightAlign ? startX - 1 : startX, y + arrayCount * 10, rightAlign ? startX : startX + 1, y + 10 + arrayCount * 10, color);
			}

			if (outer) {
				DrawableHelper.fill(matrices, outerX, y + arrayCount * 10, outerX + 1, y + 10 + arrayCount * 10, color);

				/*if (arrayCount + 1 < lines.size()) {
					DrawableHelper.fill(matrices, x + mc.textRenderer.getWidth(t) + 4 + inner, y + 10 + arrayCount * 10,
							x + mc.textRenderer.getWidth(t) + 4 + inner, y + 11 + arrayCount * 10, color);
				}*/
			}

			mc.textRenderer.drawWithShadow(matrices, t, textStart, y + 1 + arrayCount * 10, color);
			arrayCount++;
		}

		/*if (outer) {
			DrawableHelper.fill(matrices,
					x, y + arrayCount * 10,
					x + mc.textRenderer.getWidth(lines.get(arrayCount - 1)) + 4 + inner, y + 1 + arrayCount * 10,
					getRainbowFromSettings(arrayCount * 40));
		}*/
	}

	private List<Text> getModuleListText() {
		List<Text> lines = new ArrayList<>();

		for (Module m : ModuleManager.getModules())
			if (m.isEnabled())
				lines.add(new LiteralText(m.getName()));

		lines.sort(Comparator.comparing(t -> -mc.textRenderer.getWidth(t)));

		if (getSetting(0).asToggle().getChild(3).asToggle().state) {
			int watermarkMode = getSetting(0).asToggle().getChild(3).asToggle().getChild(0).asMode().mode;

			if (watermarkMode == 0) {
				MutableText text1 = new LiteralText("Bleach").styled(s -> s.withColor(TextColor.fromRgb(0xffbf30)));
				MutableText text2 = new LiteralText("Hack ").styled(s -> s.withColor(TextColor.fromRgb(0xffafcc)));
				MutableText text3 = new LiteralText(BleachHack.VERSION).styled(s -> s.withColor(TextColor.fromRgb(0xf0f0f0)));

				lines.add(0, text1.append(text2).append(text3));
			} else {
				lines.add(0, new LiteralText("\u00a7a> BleachHack " + BleachHack.VERSION));
			}
		}

		return lines;
	}

	// --- Info

	public int[] getInfoSize() {
		List<String> infoList = getInfoText();
		return new int[] { infoList.stream().map(mc.textRenderer::getWidth).sorted(Comparator.reverseOrder()).findFirst().orElse(0) + 2, infoList.size() * 10};
	}

	public void drawInfo(MatrixStack matrices, int x, int y) {
		List<String> infoList = getInfoText();

		if (y + infoList.size() * 5 > mc.getWindow().getScaledHeight() / 2) {
			Collections.reverse(infoList);
		}
		
		int count = 0;
		int longestText = infoList.stream().map(mc.textRenderer::getWidth).sorted(Comparator.reverseOrder()).findFirst().orElse(0);
		boolean rightAlign = x + longestText / 2 > mc.getWindow().getScaledWidth() / 2;
		for (String s : infoList) {
			mc.textRenderer.drawWithShadow(matrices, s,
					rightAlign ? x + longestText - mc.textRenderer.getWidth(s) + 1 : x + 1, y + 1 + count * 10, 0xa0a0a0);
			count++;
		}
	}

	private List<String> getInfoText() {
		List<String> infoList = new ArrayList<>();

		if (getSetting(1).asToggle().getChild(5).asToggle().state) {
			infoList.add("\u00a77Time: \u00a7e" + new SimpleDateFormat("MMM dd HH:mm:ss"
					+ (getSetting(1).asToggle().getChild(5).asToggle().getChild(0).asToggle().state ? " zzz" : "")
					+ (getSetting(1).asToggle().getChild(5).asToggle().getChild(1).asToggle().state ? " yyyy" : ""))
					.format(new Date()));
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
			if (chunkFuture != null && new ChunkPos(mc.player.getBlockPos()).equals(chunkFuture.getLeft())) {
				if (chunkFuture.getRight().isDone()) {
					try {
						chunkSize = chunkFuture.getRight().get();
					} catch (InterruptedException | ExecutionException e) {
						e.printStackTrace();
					}
					
					if (System.currentTimeMillis() - lastChunkTime > 1500) {
						chunkFuture = null;
					}
				}
			} else if (mc.world.getWorldChunk(mc.player.getBlockPos()) != null) {
				lastChunkTime = System.currentTimeMillis();
				chunkFuture = Pair.of(new ChunkPos(mc.player.getBlockPos()), chunkExecutor.submit(() -> {
					NbtCompound tag = ClientChunkSerializer.serialize(mc.world, mc.world.getWorldChunk(mc.player.getBlockPos()));
					DataOutputStream output = new DataOutputStream(
							new BufferedOutputStream(new DeflaterOutputStream(new ByteArrayOutputStream(8096))));
					try {
						NbtIo.writeCompressed(tag, output);
					} catch (IOException e) {
						e.printStackTrace();
						BleachLogger.errorMessage("[ChunkSize] Error serializing chunk");
						return 0;
					}

					return output.size();
				}));
			}

			infoList.add("Chunk: \u00a7f" + (chunkSize < 1000 ? chunkSize + "B" : chunkSize / 1000d + "KB"));
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

		return infoList;
	}

	// --- Players

	public int[] getPlayerSize() {
		List<Integer> nameLengths = mc.world.getPlayers().stream()
				.filter(e -> e != mc.player)
				.map(e -> mc.textRenderer.getWidth(
						e.getDisplayName().getString()
						+ " | "
						+ e.getBlockPos().getX() + " " + e.getBlockPos().getY() + " " + e.getBlockPos().getZ()
						+ " (" + (int) Math.round(mc.player.distanceTo(e)) + "m)"))
				.collect(Collectors.toList());

		nameLengths.add(mc.textRenderer.getWidth("Players:"));
		nameLengths.sort(Comparator.reverseOrder());

		return new int[] { nameLengths.get(0) + 2, nameLengths.size() * 10 + 1 };
	}

	public void drawPlayerList(MatrixStack matrices, int x, int y) {
		mc.textRenderer.drawWithShadow(matrices, "Players:", x + 1, y + 1, 0xff0000);

		int count = 1;
		for (Entity e : mc.world.getPlayers().stream()
				.filter(e -> e != mc.player)
				.sorted(Comparator.comparing(mc.player::distanceTo))
				.collect(Collectors.toList())) {
			int dist = (int) Math.round(mc.player.distanceTo(e));

			String text =
					e.getDisplayName().getString()
					+ " \u00a77|\u00a7r " +
					e.getBlockPos().getX() + " " + e.getBlockPos().getY() + " " + e.getBlockPos().getZ()
					+ " (" + dist + "m)";

			int playerColor =
					0xff000000 |
					((255 - (int) Math.min(dist * 2.1, 255) & 0xFF) << 16) |
					(((int) Math.min(dist * 4.28, 255) & 0xFF) << 8);

			mc.textRenderer.drawWithShadow(matrices, text, x + 1, y + 1 + count * 10, playerColor);
			count++;
		}
	}

	// --- Lag Meter

	public int[] getLagMeterSize() {
		return new int[] { 144, 11 };
	}

	public void drawLagMeter(MatrixStack matrices, int x, int y) {
		long time = System.currentTimeMillis();
		if (time - lastPacket > 500) {
			String text = "Server Lagging For: " + String.format("%.2f", (time - lastPacket) / 1000d) + "s";

			int xd = x + 72 - mc.textRenderer.getWidth(text) / 2;
			switch (getSetting(4).asToggle().getChild(0).asMode().mode) {
				case 0:
					mc.textRenderer.drawWithShadow(matrices, text, xd, y + 1 + Math.min((time - lastPacket - 1200) / 20, 0), 0xd0d0d0);
					break;
				case 1:
					mc.textRenderer.drawWithShadow(matrices, text, xd, y + 1,
							(MathHelper.clamp((int) (time - lastPacket - 500) / 3, 5, 255) << 24) | 0xd0d0d0);
					break;
				case 2:
					mc.textRenderer.drawWithShadow(matrices, text, xd, y + 1, 0xd0d0d0);
			}
		}
	}

	// --- Armor

	public int[] getArmorSize() {
		return new int[] { 80, 20 };
	}

	public void drawArmor(MatrixStack matrices, int x, int y) {
		for (int count = 0; count < mc.player.inventory.armor.size(); count++) {
			ItemStack is = mc.player.inventory.armor.get(count);

			if (is.isEmpty())
				continue;

			int curX = x + count * 20;
			RenderSystem.enableDepthTest();
			mc.getItemRenderer().renderGuiItemIcon(is, curX, y + 4);

			int durcolor = is.isDamageable() ? 0xff000000 | MathHelper.hsvToRgb(((float) (is.getMaxDamage() - is.getDamage()) / is.getMaxDamage()) / 3.0F, 1.0F, 1.0F) : 0;

			matrices.push();
			matrices.translate(0, 0, mc.getItemRenderer().zOffset + 200);
			if (getSetting(3).asToggle().getChild(0).asMode().mode > 0 && is.isDamaged()) {
				int barLength = Math.round(13.0F - is.getDamage() * 13.0F / is.getMaxDamage());
				DrawableHelper.fill(matrices, curX + 2, y + 17, curX + 15, y + 19, 0xff000000);
				DrawableHelper.fill(matrices, curX + 2, y + 17, curX + 2 + barLength, y + 18, durcolor);
			}

			if (getSetting(3).asToggle().getChild(0).asMode().mode != 1) {
				matrices.push();
				matrices.scale(0.75f, 0.75f, 1f);
				RenderSystem.disableDepthTest();

				if (is.getCount() > 1) {
					String s = "x" + is.getCount();
					mc.textRenderer.drawWithShadow(matrices, s, (curX + 21 - mc.textRenderer.getWidth(s)) * 1.333f, (y + 13) * 1.333f, 0xffffff);
				}

				if (is.isDamageable()) {
					String dur = Integer.toString(is.getMaxDamage() - is.getDamage());
					mc.textRenderer.drawWithShadow(
							matrices, dur, (curX + 7 - mc.textRenderer.getWidth(dur) * 1.333f / 4) * 1.333f, (y + 1) * 1.333f, durcolor);
				}

				RenderSystem.enableDepthTest();
				matrices.pop();
			}

			matrices.pop();
		}
	}

	@BleachSubscribe
	public void readPacket(EventReadPacket event) {
		lastPacket = System.currentTimeMillis();

		if (event.getPacket() instanceof WorldTimeUpdateS2CPacket) {
			long time = System.currentTimeMillis();
			long timeOffset = Math.abs(1000 - (time - prevTime)) + 1000;
			tps = Math.round(MathHelper.clamp(20 / (timeOffset / 1000d), 0, 20) * 100d) / 100d;
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
		double rainbowState = Math.ceil((System.currentTimeMillis() + offset) / speed) % 360;
		return 0xff000000 | MathHelper.hsvToRgb((float) (rainbowState / 360.0), sat, bri);
	}

	public static int getRainbowFromSettings(int offset) {
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
