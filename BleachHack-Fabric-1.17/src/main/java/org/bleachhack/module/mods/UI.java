/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.module.mods;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.zip.DeflaterOutputStream;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bleachhack.BleachHack;
import org.bleachhack.event.events.EventReadPacket;
import org.bleachhack.event.events.EventRenderInGameHud;
import org.bleachhack.event.events.EventTick;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.gui.clickgui.UIClickGuiScreen;
import org.bleachhack.gui.clickgui.window.UIContainer;
import org.bleachhack.gui.clickgui.window.UIWindow;
import org.bleachhack.gui.clickgui.window.UIWindow.Position;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.module.ModuleManager;
import org.bleachhack.module.setting.base.SettingButton;
import org.bleachhack.module.setting.base.SettingMode;
import org.bleachhack.module.setting.base.SettingSlider;
import org.bleachhack.module.setting.base.SettingToggle;
import org.bleachhack.util.world.ClientChunkSerializer;

import com.mojang.blaze3d.systems.RenderSystem;

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
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;

public class UI extends Module {

	public static UIContainer uiContainer = new UIContainer();

	private List<Text> moduleListText = new ArrayList<>();
	private Text fpsText = LiteralText.EMPTY;
	private Text pingText = LiteralText.EMPTY;
	private Text coordsText = LiteralText.EMPTY;
	private Text tpsText = LiteralText.EMPTY;
	private Text durabilityText = LiteralText.EMPTY;
	private Text serverText = LiteralText.EMPTY;
	private Text timestampText = LiteralText.EMPTY;
	private Text chunksizeText = LiteralText.EMPTY;

	private long prevTime = 0;
	private double tps = 20;
	private long lastPacket = 0;

	private int chunkSize;
	private long lastChunkTime;
	private ExecutorService chunkExecutor;
	private Pair<ChunkPos, Future<Integer>> chunkFuture;

	public UI() {
		super("UI", KEY_UNBOUND, ModuleCategory.RENDER, true, "Shows stuff onscreen.",
				new SettingToggle("Modulelist", true).withDesc("Shows the module list.").withChildren(                                 // 0
						new SettingToggle("InnerLine", true).withDesc("Adds an extra line to the front of the module list."),
						new SettingToggle("OuterLine", false).withDesc("Adds an outer line to the module list."),
						new SettingToggle("Fill", true).withDesc("Adds a black fill behind the module list."),
						new SettingToggle("Watermark", true).withDesc("Adds the BleachHack watermark to the module list.").withChildren(
								new SettingMode("Mode", "New", "Old").withDesc("The watermark type.")),
						new SettingSlider("HueBright", 0, 1, 1, 2).withDesc("The hue of the rainbow."),
						new SettingSlider("HueSat", 0, 1, 0.5, 2).withDesc("The saturation of the rainbow."),
						new SettingSlider("HueSpeed", 0.1, 50, 25, 1).withDesc("The speed of the rainbow.")),
				new SettingToggle("FPS", true).withDesc("Shows your FPS."),                                                            // 1
				new SettingToggle("Ping", true).withDesc("Shows your ping."),                                                          // 2
				new SettingToggle("Coords", true).withDesc("Shows your coords and nether coords."),                                    // 3
				new SettingToggle("TPS", true).withDesc("Shows the estimated server tps."),                                            // 4
				new SettingToggle("Durability", false).withDesc("Shows durability left on the item you're holding."),                  // 5
				new SettingToggle("Server", false).withDesc("Shows the current server you are on."),                                   // 6
				new SettingToggle("Timestamp", false).withDesc("Shows the current time.").withChildren(                                // 7
						new SettingToggle("TimeZone", true).withDesc("Shows your time zone in the time."),
						new SettingToggle("Year", false).withDesc("Shows the current year in the time.")),
				new SettingToggle("ChunkSize", false).withDesc("Shows the data size of the chunk you are standing in."),               // 8
				new SettingToggle("Players", false).withDesc("Lists all the players in your render distance."),                        // 9
				new SettingToggle("Armor", true).withDesc("Shows your current armor.").withChildren(                                   // 10
						new SettingToggle("Vertical", false).withDesc("Displays your armor vertically."),
						new SettingMode("Damage", "Number", "Bar", "BarV").withDesc("How to show the armor durability.")),
				new SettingToggle("Lag-Meter", true).withDesc("Shows when the server isn't responding.").withChildren(                 // 11
						new SettingMode("Animation", "Fall", "Fade", "None").withDesc("How to animate the lag meter when appearing.")),
				new SettingToggle("Inventory", false).withDesc("Renders your inventory on screen.").withChildren(                      // 12
						new SettingSlider("Background", 0, 255, 140, 0).withDesc("How opaque the background should be.")),
				new SettingButton("Edit UI..", () -> MinecraftClient.getInstance().setScreen(new UIClickGuiScreen(ClickGui.clickGui, uiContainer))).withDesc("Edit the position of the UI."));

		// Modulelist
		uiContainer.windows.put("modulelist",
				new UIWindow(new Position("l", 1, "t", 2), uiContainer,
						() -> getSetting(0).asToggle().state,
						this::getModuleListSize,
						this::drawModuleList)
				);

		// Info
		uiContainer.windows.put("coords",
				new UIWindow(new Position("l", 1, "b", 0), uiContainer,
						() -> getSetting(3).asToggle().state,
						() -> new int[] { mc.textRenderer.getWidth(coordsText), 8 },
						(ms, x, y) -> mc.textRenderer.drawWithShadow(ms, coordsText, x, y, 0xa0a0a0))
				);

		uiContainer.windows.put("fps",
				new UIWindow(new Position("l", 1, "coords", 0), uiContainer,
						() -> getSetting(1).asToggle().state,
						() -> new int[] { mc.textRenderer.getWidth(fpsText), 8 },
						(ms, x, y) -> mc.textRenderer.drawWithShadow(ms, fpsText, x, y, 0xa0a0a0))
				);

		uiContainer.windows.put("ping",
				new UIWindow(new Position("l", 1, "fps", 0), uiContainer,
						() -> getSetting(2).asToggle().state,
						() -> new int[] { mc.textRenderer.getWidth(pingText), 8 },
						(ms, x, y) -> mc.textRenderer.drawWithShadow(ms, pingText, x, y, 0xa0a0a0))
				);

		uiContainer.windows.put("tps",
				new UIWindow(new Position("l", 1, "ping", 0), uiContainer,
						() -> getSetting(4).asToggle().state,
						() -> new int[] { mc.textRenderer.getWidth(tpsText), 8 },
						(ms, x, y) -> mc.textRenderer.drawWithShadow(ms, tpsText, x, y, 0xa0a0a0))
				);

		uiContainer.windows.put("durability",
				new UIWindow(new Position(0.2, 0.9), uiContainer,
						() -> getSetting(5).asToggle().state,
						() -> new int[] { mc.textRenderer.getWidth(durabilityText), 8 },
						(ms, x, y) -> mc.textRenderer.drawWithShadow(ms, durabilityText, x, y, 0xa0a0a0))
				);

		uiContainer.windows.put("server",
				new UIWindow(new Position(0.2, 0.85, "durability", 0), uiContainer,
						() -> getSetting(6).asToggle().state,
						() -> new int[] { mc.textRenderer.getWidth(serverText), 8 },
						(ms, x, y) -> mc.textRenderer.drawWithShadow(ms, serverText, x, y, 0xa0a0a0))
				);

		uiContainer.windows.put("timestamp",
				new UIWindow(new Position(0.2, 0.8, "server", 0), uiContainer,
						() -> getSetting(7).asToggle().state,
						() -> new int[] { mc.textRenderer.getWidth(timestampText), 8 },
						(ms, x, y) -> mc.textRenderer.drawWithShadow(ms, timestampText, x, y, 0xa0a0a0))
				);

		uiContainer.windows.put("chunksize",
				new UIWindow(new Position(0.2, 0.75, "timestamp", 0), uiContainer,
						() -> getSetting(8).asToggle().state,
						() -> new int[] { mc.textRenderer.getWidth(chunksizeText), 8 },
						(ms, x, y) -> mc.textRenderer.drawWithShadow(ms, chunksizeText, x, y, 0xa0a0a0))
				);

		// Players
		uiContainer.windows.put("players",
				new UIWindow(new Position("l", 1, "modulelist", 2), uiContainer,
						() -> getSetting(9).asToggle().state,
						this::getPlayerSize,
						this::drawPlayerList)
				);

		// Armor
		uiContainer.windows.put("armor",
				new UIWindow(new Position(0.5, 0.85), uiContainer,
						() -> getSetting(10).asToggle().state,
						this::getArmorSize,
						this::drawArmor)
				);

		// Lag-Meter
		uiContainer.windows.put("lagmeter",
				new UIWindow(new Position(0, 0.05, "c", 1), uiContainer,
						() -> getSetting(11).asToggle().state,
						this::getLagMeterSize,
						this::drawLagMeter)
				);

		// Inventory
		uiContainer.windows.put("inventory",
				new UIWindow(new Position(0.7, 0.90), uiContainer,
						() -> getSetting(12).asToggle().state,
						this::getInventorySize,
						this::drawInventory)
				);
	}

	@Override
	public void onEnable(boolean inWorld) {
		super.onEnable(inWorld);

		chunkExecutor = Executors.newSingleThreadExecutor();
	}

	@Override
	public void onDisable(boolean inWorld) {
		chunkExecutor.shutdownNow();

		super.onDisable(inWorld);
	}

	@BleachSubscribe
	public void onTick(EventTick event) {
		// ModuleList
		moduleListText.clear();

		for (Module m : ModuleManager.getModules())
			if (m.isEnabled())
				moduleListText.add(new LiteralText(m.getName()));

		moduleListText.sort(Comparator.comparingInt(t -> -mc.textRenderer.getWidth(t)));

		if (getSetting(0).asToggle().getChild(3).asToggle().state) {
			int watermarkMode = getSetting(0).asToggle().getChild(3).asToggle().getChild(0).asMode().mode;

			if (watermarkMode == 0) {
				moduleListText.add(0, BleachHack.watermark.getText().append(new LiteralText(" " + BleachHack.VERSION).styled(s -> s.withColor(TextColor.fromRgb(0xf0f0f0)))));
			} else {
				moduleListText.add(0, new LiteralText("\u00a7a> BleachHack " + BleachHack.VERSION));
			}
		}

		// FPS
		int fps = MinecraftClient.currentFps;
		fpsText = new LiteralText("FPS: ")
				.append(colorText(Integer.toString(fps), Math.min(fps, 120) / 360f));

		// Ping
		PlayerListEntry playerEntry = mc.player.networkHandler.getPlayerListEntry(mc.player.getGameProfile().getId());
		int ping = playerEntry == null ? 0 : playerEntry.getLatency();
		pingText = new LiteralText("Ping: ")
				.append(colorText(Integer.toString(ping), (800 - MathHelper.clamp(ping, 0, 800)) / 2400f));

		// Coords
		boolean nether = mc.world.getRegistryKey().getValue().getPath().contains("nether");
		BlockPos pos = mc.player.getBlockPos();
		BlockPos pos2 = nether ? new BlockPos(mc.player.getPos().multiply(8, 1, 8))
				: new BlockPos(mc.player.getPos().multiply(0.125, 1, 0.125));

		coordsText = new LiteralText("XYZ: ")
				.append(new LiteralText(pos.getX() + " " + pos.getY() + " " + pos.getZ()).styled(s -> s.withColor(nether ? 0xb02020 : 0x40f0f0)))
				.append(" [")
				.append(new LiteralText(pos2.getX() + " " + pos2.getY() + " " + pos2.getZ()).styled(s -> s.withColor(nether ? 0x40f0f0 : 0xb02020)))
				.append("]");

		// TPS
		int time = (int) (System.currentTimeMillis() - lastPacket);
		String suffix = time >= 7500 ? "...." : time >= 5000 ? "..." : time >= 2500 ? ".." : time >= 1200 ? ".." : "";

		tpsText = new LiteralText("TPS: ")
				.append(colorText(Double.toString(tps), (float) MathHelper.clamp(tps - 2, 0, 16) / 48))
				.append(suffix);

		// Durability
		ItemStack mainhand = mc.player.getMainHandStack();
		if (mainhand.isDamageable()) {
			int durability = mainhand.getOrCreateNbt().contains("dmg")
					? NumberUtils.toInt(mainhand.getOrCreateNbt().get("dmg").asString()) : mainhand.getMaxDamage() - mainhand.getDamage();

			durabilityText = new LiteralText("Durability: ")
					.append(colorText(Integer.toString(durability), (float) durability / mainhand.getMaxDamage() / 3f % 1f));
		} else {
			durabilityText = new LiteralText("Durability: --");
		}

		// Server
		String server = mc.getCurrentServerEntry() == null ? "Singleplayer" : mc.getCurrentServerEntry().address;
		serverText = new LiteralText("Server: ")
				.append(new LiteralText(server).styled(s -> s.withColor(Formatting.LIGHT_PURPLE)));

		// Timestamp
		String timeString = ZonedDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd HH:mm:ss"
				+ (getSetting(7).asToggle().getChild(0).asToggle().state ? " zzz" : "")
				+ (getSetting(7).asToggle().getChild(1).asToggle().state ? " yyyy" : "")));

		timestampText = new LiteralText("Time: ")
				.append(new LiteralText(timeString).styled(s -> s.withColor(Formatting.YELLOW)));

		// ChunkSize
		if (chunkFuture != null && new ChunkPos(mc.player.getBlockPos()).equals(chunkFuture.getLeft())) {
			if (chunkFuture.getRight().isDone()) {
				try {
					chunkSize = chunkFuture.getRight().get();
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}

				chunkFuture = null;
			}
		} else if (System.currentTimeMillis() - lastChunkTime > 1500 && mc.world.getWorldChunk(mc.player.getBlockPos()) != null) {
			lastChunkTime = System.currentTimeMillis();
			chunkFuture = Pair.of(new ChunkPos(mc.player.getBlockPos()), chunkExecutor.submit(() -> {
				try {
					NbtCompound tag = ClientChunkSerializer.serialize(mc.world, mc.world.getWorldChunk(mc.player.getBlockPos()));
					DataOutputStream output = new DataOutputStream(
							new BufferedOutputStream(new DeflaterOutputStream(new ByteArrayOutputStream(8096))));
					NbtIo.writeCompressed(tag, output);
					return output.size();
				} catch (Exception e) {
					return 0;
				}
			}));
		}

		chunksizeText = new LiteralText("Chunk: ")
				.append(new LiteralText(chunkSize < 1000 ? chunkSize + "B" : chunkSize / 1000d + "KB").styled(s -> s.withColor(Formatting.WHITE)));
	}

	@BleachSubscribe
	public void onDrawOverlay(EventRenderInGameHud event) {
		if (mc.currentScreen instanceof UIClickGuiScreen) {
			return;
		}

		uiContainer.updatePositions(mc.getWindow().getScaledWidth(), mc.getWindow().getScaledHeight());
		uiContainer.render(event.getMatrix());
	}

	// --- Module List

	public int[] getModuleListSize() {
		if (moduleListText.isEmpty()) {
			return new int[] { 0, 0 };
		}

		int inner = getSetting(0).asToggle().getChild(0).asToggle().state ? 1 : 0;
		int outer = getSetting(0).asToggle().getChild(1).asToggle().state ? 4 : 3;
		return new int[] { mc.textRenderer.getWidth(moduleListText.get(0)) + inner + outer, moduleListText.size() * 10 };
	}

	public void drawModuleList(MatrixStack matrices, int x, int y) {
		if (moduleListText.isEmpty()) return;

		int arrayCount = 0;
		boolean inner = getSetting(0).asToggle().getChild(0).asToggle().state;
		boolean outer = getSetting(0).asToggle().getChild(1).asToggle().state;
		boolean fill = getSetting(0).asToggle().getChild(2).asToggle().state;
		boolean rightAlign = x + mc.textRenderer.getWidth(moduleListText.get(0)) / 2 > mc.getWindow().getScaledWidth() / 2;

		int startX = rightAlign ? x + mc.textRenderer.getWidth(moduleListText.get(0)) + 3 + (inner ? 1 : 0) + (outer ? 1 : 0) : x;
		for (Text t : moduleListText) {
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
			}

			mc.textRenderer.drawWithShadow(matrices, t, textStart, y + 1 + arrayCount * 10, color);
			arrayCount++;
		}
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
			String text = "Server Lagging For: " + String.format(Locale.ENGLISH, "%.2f", (time - lastPacket) / 1000d) + "s";

			int xd = x + 72 - mc.textRenderer.getWidth(text) / 2;
			switch (getSetting(11).asToggle().getChild(0).asMode().mode) {
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
		boolean vertical = getSetting(10).asToggle().getChild(0).asToggle().state;
		return new int[] { vertical ? 16 : 72, vertical ? 62 : 16 };
	}

	public void drawArmor(MatrixStack matrices, int x, int y) {
		boolean vertical = getSetting(10).asToggle().getChild(0).asToggle().state;

		for (int count = 0; count < mc.player.getInventory().armor.size(); count++) {
			ItemStack is = mc.player.getInventory().armor.get(count);

			if (is.isEmpty())
				continue;

			int curX = vertical ? x : x + count * 19;
			int curY = vertical ? y + 47 - count * 16 : y;
			RenderSystem.enableDepthTest();
			mc.getItemRenderer().renderGuiItemIcon(is, curX, curY);

			int durcolor = is.isDamageable() ? 0xff000000 | MathHelper.hsvToRgb((float) (is.getMaxDamage() - is.getDamage()) / is.getMaxDamage() / 3.0F, 1.0F, 1.0F) : 0;

			matrices.push();
			matrices.translate(0, 0, mc.getItemRenderer().zOffset + 200);
			int mode = getSetting(10).asToggle().getChild(1).asMode().mode;
			if (mode == 0) {
				matrices.push();
				matrices.scale(0.75f, 0.75f, 1f);
				RenderSystem.disableDepthTest();

				if (is.getCount() > 1) {
					String s = "x" + is.getCount();
					mc.textRenderer.drawWithShadow(matrices, s, (curX + 21 - mc.textRenderer.getWidth(s)) * 1.333f, (curY + 9) * 1.333f, 0xffffff);
				}

				if (is.isDamageable()) {
					String dur = Integer.toString(is.getMaxDamage() - is.getDamage());
					mc.textRenderer.drawWithShadow(
							matrices, dur, (curX + 7 - mc.textRenderer.getWidth(dur) * 1.333f / 4) * 1.333f, (curY - (vertical ? 2 : 3)) * 1.333f, durcolor);
				}

				RenderSystem.enableDepthTest();
				matrices.pop();
			} else if (mode == 1) {
				int barLength = Math.round(13.0F - is.getDamage() * 13.0F / is.getMaxDamage());
				DrawableHelper.fill(matrices, curX + 2, curY + 13, curX + 15, curY + 15, 0xff000000);
				DrawableHelper.fill(matrices, curX + 2, curY + 13, curX + 2 + barLength, curY + 14, durcolor);
			} else {
				int barLength = Math.round(12.0F - is.getDamage() * 12.0F / is.getMaxDamage());
				DrawableHelper.fill(matrices, curX, curY + 2, curX + 2, curY + 14, 0xff000000);
				DrawableHelper.fill(matrices, curX, curY + 2, curX + 1, curY + 2 + barLength, durcolor);
			}

			matrices.pop();
		}
	}

	// --- Inventory

	public int[] getInventorySize() {
		return new int[] { 155, 53 };
	}

	public void drawInventory(MatrixStack matrices, int x, int y) {
		if (getSetting(12).asToggle().state) {
			DrawableHelper.fill(matrices, x + 155, y, x, y + 53,
					(getSetting(12).asToggle().getChild(0).asSlider().getValueInt() << 24) | 0x212120);

			matrices.push();
			for (int i = 0; i < 27; i++) {
				ItemStack itemStack = mc.player.getInventory().getStack(i + 9);
				int offsetX = x + 1 + (i % 9) * 17;
				int offsetY = y + 1 + (i / 9) * 17;
				mc.getItemRenderer().renderGuiItemIcon(itemStack, offsetX, offsetY);
				mc.getItemRenderer().renderGuiItemOverlay(mc.textRenderer, itemStack, offsetX, offsetY);
			}

			mc.getItemRenderer().zOffset = 0.0F;
			RenderSystem.enableDepthTest();
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

	private static Text colorText(String text, float hue) {
		return new LiteralText(text).styled(s -> s.withColor(MathHelper.hsvToRgb(hue, 1f, 1f)));
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
