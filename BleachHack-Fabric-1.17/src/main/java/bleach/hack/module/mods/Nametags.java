/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.module.mods;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.lang3.text.WordUtils;
import bleach.hack.eventbus.BleachSubscribe;
import com.google.common.io.Resources;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import bleach.hack.BleachHack;
import bleach.hack.command.commands.CmdEntityStats;
import bleach.hack.event.events.EventEntityRender;
import bleach.hack.event.events.EventTick;
import bleach.hack.event.events.EventWorldRender;
import bleach.hack.module.ModuleCategory;
import bleach.hack.module.setting.base.SettingMode;
import bleach.hack.module.setting.base.SettingSlider;
import bleach.hack.module.setting.base.SettingToggle;
import bleach.hack.module.Module;
import bleach.hack.util.BleachLogger;
import bleach.hack.util.render.Renderer;
import bleach.hack.util.render.WorldRenderUtils;
import bleach.hack.util.world.EntityUtils;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.HorseBaseEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TextColor;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.GameMode;

public class Nametags extends Module {

	private ExecutorService uuidExecutor;
	// list of future client licenses
	private Map<UUID, Future<String>> uuidFutures = new HashMap<>();

	private Queue<UUID> uuidQueue = new ArrayDeque<>();
	private Map<UUID, String> uuidCache = new HashMap<>();
	private Set<UUID> failedUUIDs = new HashSet<>();
	private long lastLookup = 0;

	public Nametags() {
		super("Nametags", KEY_UNBOUND, ModuleCategory.RENDER, "Shows bigger/cooler nametags above entities.",
				new SettingMode("Armor", "H", "V", "None").withDesc("How to show items/armor."),
				new SettingMode("Health", "Number", "NumberOf", "Bar", "Percent").withDesc("How to show health."),
				new SettingToggle("Players", true).withDesc("Shows nametags over player.").withChildren(
						new SettingSlider("Size", 0.5, 5, 2, 1).withDesc("The size of the nametags."),
						new SettingToggle("Name", true).withDesc("Shows the name of the player."),
						new SettingToggle("Health", true).withDesc("Shows the health of the player."),
						new SettingToggle("Ping", true).withDesc("Shows the ping of the player."),
						new SettingToggle("Gamemode", false).withDesc("Shows the gamemode of the player.")),
				new SettingToggle("Animals", true).withDesc("Shows nametags over animals.").withChildren(
						new SettingSlider("Size", 0.5, 5, 1, 1).withDesc("The size of the nametags."),
						new SettingToggle("Name", true).withDesc("Shows the name of the animal."),
						new SettingToggle("Health", true).withDesc("Shows the health of the animal."),
						new SettingToggle("Tamed", false).withDesc("Shows if the animal is tamed.").withChildren(
								new SettingMode("If Not", "Show", "Hide").withDesc("What to show if the animal isn't tamed.")),
						new SettingToggle("Owner", true).withDesc("Shows the owner of the pet if its tamed."),
						new SettingToggle("HorseStats", false).withDesc("Shows the entities stats if its a horse.")),
				new SettingToggle("Mobs", false).withDesc("Shows nametags over mobs.").withChildren(
						new SettingSlider("Size", 0.5, 5, 1, 1).withDesc("The size of the nametags."),
						new SettingToggle("Name", true).withDesc("Shows the name of the mob."),
						new SettingToggle("Health", true).withDesc("Shows the health of the mob.")),
				new SettingToggle("Items", true).withDesc("Shows nametags for items.").withChildren(
						new SettingSlider("Size", 0.5, 5, 1, 1).withDesc("Size of the nametags."),
						new SettingToggle("CustomName", true).withDesc("Shows the items custom name if it has it."),
						new SettingToggle("ItemCount", true).withDesc("Shows how many items are in the stack.")));
	}

	@Override
	public void onDisable(boolean inWorld) {
		uuidQueue.clear();
		failedUUIDs.clear();
		uuidExecutor.shutdownNow();
		uuidFutures.clear();

		Map<UUID, String> cacheCopy = new HashMap<>(uuidCache);
		uuidCache.clear();

		cacheCopy.forEach((u, s) -> {
			if (!s.startsWith("\u00a7c")) uuidCache.put(u, s);
		});

		super.onDisable(inWorld);
	}

	@Override
	public void onEnable(boolean inWorld) {
		super.onEnable(inWorld);

		uuidExecutor = Executors.newFixedThreadPool(4);
	}

	@BleachSubscribe
	public void onTick(EventTick event) {
		// collect revenue from all the future copies
		for (Entry<UUID, Future<String>> f: new HashMap<>(uuidFutures).entrySet()) {
			if (f.getValue().isDone()) {
				try {
					String s = f.getValue().get();
					uuidCache.put(f.getKey(), s);

					uuidFutures.remove(f.getKey());
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
			}
		}

		if (!uuidQueue.isEmpty() && System.currentTimeMillis() - lastLookup > 1000) {
			lastLookup = System.currentTimeMillis();
			addUUIDFuture(uuidQueue.poll());
		}
	}

	@BleachSubscribe
	public void onLivingLabelRender(EventEntityRender.Single.Label event) {
		if ((EntityUtils.isAnimal(event.getEntity()) && getSetting(3).asToggle().state)
				|| (event.getEntity() instanceof Monster && getSetting(4).asToggle().state)
				|| (event.getEntity() instanceof PlayerEntity && getSetting(2).asToggle().state)
				|| (event.getEntity() instanceof ItemEntity && getSetting(5).asToggle().state))
			event.setCancelled(true);
	}

	@BleachSubscribe
	public void onWorldRender(EventWorldRender.Post event) {
		for (Entity entity: mc.world.getEntities()) {
			Vec3d rPos = entity.getPos().subtract(Renderer.getInterpolationOffset(entity)).add(0, entity.getHeight(), 0);
			List<String> lines = new ArrayList<>();
			double scale = 0;

			if (entity instanceof ItemEntity && getSetting(5).asToggle().state) {
				scale = Math.max(getSetting(5).asToggle().getChild(0).asSlider().getValue() * (mc.cameraEntity.distanceTo(entity) / 20), 1);

				addItemLines(lines, (ItemEntity) entity);
			} else if (entity instanceof LivingEntity) {
				if (entity == mc.player || entity.hasPassenger(mc.player) || mc.player.hasPassenger(entity)) {
					continue;
				}

				LivingEntity livingEntity = (LivingEntity) entity;

				if (entity instanceof PlayerEntity && getSetting(2).asToggle().state) {
					scale = Math.max(getSetting(2).asToggle().getChild(0).asSlider().getValue() * (mc.cameraEntity.distanceTo(entity) / 20), 1);

					addPlayerLines(lines, (PlayerEntity) entity);
				} else if (EntityUtils.isAnimal(entity) && getSetting(3).asToggle().state) {
					scale = Math.max(getSetting(3).asToggle().getChild(0).asSlider().getValue() * (mc.cameraEntity.distanceTo(entity) / 20), 1);

					addAnimalLines(lines, livingEntity);
				} else if (entity instanceof Monster && getSetting(4).asToggle().state) {
					scale = Math.max(getSetting(4).asToggle().getChild(0).asSlider().getValue() * (mc.cameraEntity.distanceTo(entity) / 20), 1);

					addMobLines(lines, livingEntity);
				}

				/* Drawing Items */
				double c = 0;
				double lscale = scale * 0.4;
				double up = ((0.3 + lines.size() * 0.25) * scale) + lscale / 2;

				if (getSetting(0).asMode().mode == 0) {
					drawItem(rPos.x, rPos.y + up, rPos.z, -2.5, 0, lscale, livingEntity.getEquippedStack(EquipmentSlot.MAINHAND));
					drawItem(rPos.x, rPos.y + up, rPos.z, 2.5, 0, lscale, livingEntity.getEquippedStack(EquipmentSlot.OFFHAND));

					for (ItemStack i : livingEntity.getArmorItems()) {
						drawItem(rPos.x, rPos.y + up, rPos.z, c + 1.5, 0, lscale, i);
						c--;
					}
				} else if (getSetting(0).asMode().mode == 1) {
					drawItem(rPos.x, rPos.y + up, rPos.z, -1.25, 0, lscale, livingEntity.getEquippedStack(EquipmentSlot.MAINHAND));
					drawItem(rPos.x, rPos.y + up, rPos.z, 1.25, 0, lscale, livingEntity.getEquippedStack(EquipmentSlot.OFFHAND));

					for (ItemStack i : livingEntity.getArmorItems()) {
						drawItem(rPos.x, rPos.y + up, rPos.z, 0, c, lscale, i);
						c++;
					}
				}
			}

			if (!lines.isEmpty()) {
				float offset = 0.25f + lines.size() * 0.25f;

				for (String s: lines) {
					WorldRenderUtils.drawText(new LiteralText(s), rPos.x, rPos.y + (offset * scale), rPos.z, scale, true);

					offset -= 0.25f;
				}
			}
		}
	}

	public void addPlayerLines(List<String> lines, PlayerEntity player) {
		addPlayerNameHealthLine(lines, player, BleachHack.friendMang.has(player) ? Formatting.AQUA : Formatting.RED,
				getSetting(2).asToggle().getChild(1).asToggle().state,
				getSetting(2).asToggle().getChild(2).asToggle().state,
				getSetting(2).asToggle().getChild(3).asToggle().state,
				getSetting(2).asToggle().getChild(4).asToggle().state);
	}

	public void addAnimalLines(List<String> lines, LivingEntity animal) {
		addNameHealthLine(lines, animal, Formatting.GREEN,
				getSetting(3).asToggle().getChild(1).asToggle().state,
				getSetting(3).asToggle().getChild(2).asToggle().state);

		if (animal instanceof HorseBaseEntity || animal instanceof TameableEntity) {
			boolean tame = animal instanceof HorseBaseEntity
					? ((HorseBaseEntity) animal).isTame() : ((TameableEntity) animal).isTamed();

			UUID ownerUUID = animal instanceof HorseBaseEntity
					? ((HorseBaseEntity) animal).getOwnerUuid() : ((TameableEntity) animal).getOwnerUuid();

			if (getSetting(3).asToggle().getChild(3).asToggle().state && !animal.isBaby()
					&& (getSetting(3).asToggle().getChild(3).asToggle().getChild(0).asMode().mode != 1 || tame)) {
				lines.add(0, tame ? Formatting.GREEN + "Tamed: Yes" : Formatting.RED + "Tamed: No");
			}

			if (getSetting(3).asToggle().getChild(4).asToggle().state && ownerUUID != null) {
				if (uuidCache.containsKey(ownerUUID)) {
					lines.add(0, Formatting.GREEN + "Owner: " + uuidCache.get(ownerUUID));
				} else if (failedUUIDs.contains(ownerUUID)) {
					lines.add(0, Formatting.GREEN + "Owner: " + Formatting.GRAY + "Invalid UUID!");
				} else {
					// Try to see if the owner is online on the server before calling the mojang api
					Optional<GameProfile> owner = mc.player.networkHandler.getPlayerList().stream()
							.filter(en -> en.getProfile() != null && ownerUUID.equals(en.getProfile().getId()) && en.getProfile().getName() != null)
							.map(en -> en.getProfile()).findFirst();

					if (owner.isPresent()) {
						uuidCache.put(ownerUUID, owner.get().getName());
					} else if (!uuidQueue.contains(ownerUUID) && !uuidFutures.containsKey(ownerUUID)) {
						uuidQueue.add(ownerUUID);
					}

					lines.add(0, Formatting.GREEN + "Owner: " + Formatting.GRAY + "Loading...");
				}
			}

			if (getSetting(3).asToggle().getChild(5).asToggle().state && animal instanceof HorseBaseEntity) {
				HorseBaseEntity he = (HorseBaseEntity) animal;

				lines.add(0, Formatting.GREEN.toString()
						+ CmdEntityStats.getSpeed(he) + " m/s"
						+ Formatting.GRAY + " | " + Formatting.GREEN
						+ CmdEntityStats.getJumpHeight(he) + " Jump");
			}
		}
	}

	public void addMobLines(List<String> lines, LivingEntity mob) {
		addNameHealthLine(lines, mob, Formatting.DARK_PURPLE,
				getSetting(4).asToggle().getChild(1).asToggle().state,
				getSetting(4).asToggle().getChild(2).asToggle().state);
	}

	public void addItemLines(List<String> lines, ItemEntity item) {
		lines.add(Formatting.GOLD + item.getName().getString()
				+ (getSetting(5).asToggle().getChild(2).asToggle().state
						? Formatting.YELLOW + " [x" + item.getStack().getCount() + "]" : ""));

		if (!item.getName().getString().equals(item.getStack().getName().getString()) && getSetting(5).asToggle().getChild(1).asToggle().state) {
			lines.add(0, Formatting.GOLD + "\"" + item.getStack().getName().getString() + Formatting.GOLD + "\"");
		}
	}

	private void drawItem(double x, double y, double z, double offX, double offY, double scale, ItemStack item) {
		WorldRenderUtils.drawGuiItem(x, y, z, offX * scale, offY * scale, scale, item);

		if (!item.isEmpty()) {
			double w = mc.textRenderer.getWidth("x" + item.getCount()) / 52d;
			WorldRenderUtils.drawText(new LiteralText("x" + item.getCount()),
					x, y, z, (offX - w) * scale, (offY - 0.07) * scale, scale * 1.75, false);
		}

		int c = 0;
		for (Entry<Enchantment, Integer> m : EnchantmentHelper.get(item).entrySet()) {
			String text = I18n.translate(m.getKey().getName(2).getString());

			if (text.isEmpty())
				continue;
			
			text = WordUtils.capitalizeFully(text.replaceFirst("Curse of (.)", "C$1"));

			String subText = text.substring(0, Math.min(text.length(), 2)) + m.getValue();

			WorldRenderUtils.drawText(new LiteralText(subText).styled(s-> s.withColor(TextColor.fromRgb(m.getKey().isCursed() ? 0xff5050 : 0xffb0e0))),
					x, y, z, (offX + 0.01) * scale, (offY + 0.75 - c * 0.34) * scale, scale * 1.4, false);
			c--;
		}
	}

	private String getHealthText(LivingEntity e) {
		if (getSetting(1).asMode().mode == 0) {
			return getHealthColor(e).toString() + (int) (e.getHealth() + e.getAbsorptionAmount());
		} else if (getSetting(1).asMode().mode == 1) {
			return Formatting.GREEN.toString() + getHealthColor(e) + (int) (e.getHealth() + e.getAbsorptionAmount()) + Formatting.GREEN + "/" + (int) e.getMaxHealth();
		} else if (getSetting(1).asMode().mode == 2) {
			/* Health bar */
			String health = "";
			/* - Add Green Normal Health */
			for (int i = 0; i < e.getHealth(); i++)
				health += Formatting.GREEN + "|";
			/* - Add Red Empty Health (Remove Based on absorption amount) */
			for (int i = 0; i < MathHelper.clamp(e.getAbsorptionAmount(), 0, e.getMaxHealth() - e.getHealth()); i++)
				health += Formatting.YELLOW + "|";
			/* - Add Yellow Absorption Health */
			for (int i = 0; i < e.getMaxHealth() - (e.getHealth() + e.getAbsorptionAmount()); i++)
				health += Formatting.RED + "|";
			/* - Add "+??" to the end if the entity has extra hearts */
			if (e.getAbsorptionAmount() - (e.getMaxHealth() - e.getHealth()) > 0) {
				health += Formatting.YELLOW + " +" + (int) (e.getAbsorptionAmount() - (e.getMaxHealth() - e.getHealth()));
			}

			return health;
		} else {
			return getHealthColor(e).toString() + (int) ((e.getHealth() + e.getAbsorptionAmount()) / e.getMaxHealth() * 100) + "%";
		}
	}

	private Formatting getHealthColor(LivingEntity entity) {
		if (entity.getHealth() + entity.getAbsorptionAmount() > entity.getMaxHealth()) {
			return Formatting.YELLOW;
		} else if (entity.getHealth() + entity.getAbsorptionAmount() >= entity.getMaxHealth() * 0.7) {
			return Formatting.GREEN;
		} else if (entity.getHealth() + entity.getAbsorptionAmount() >= entity.getMaxHealth() * 0.4) {
			return Formatting.GOLD;
		} else if (entity.getHealth() + entity.getAbsorptionAmount() >= entity.getMaxHealth() * 0.1) {
			return Formatting.RED;
		} else {
			return Formatting.DARK_RED;
		}
	}

	private void addNameHealthLine(List<String> lines, LivingEntity entity, Formatting color, boolean addName, boolean addHealth) {
		if (getSetting(1).asMode().mode == 2) {
			if (addName) {
				lines.add(color + entity.getName().getString());
			}

			if (addHealth) {
				lines.add(0, getHealthText(entity));
			}
		} else if (addName || addHealth) {
			lines.add((addName ? color + entity.getName().getString() + (addHealth ? " " : "") : "") + (addHealth ? getHealthText(entity) : ""));
		}
	}

	private void addPlayerNameHealthLine(List<String> lines, PlayerEntity player, Formatting color, boolean addName, boolean addHealth, boolean addPing, boolean addGm) {
		PlayerListEntry playerEntry = mc.player.networkHandler.getPlayerListEntry(player.getGameProfile().getId());

		String pingText = addPing && playerEntry != null ? Formatting.GRAY.toString() + playerEntry.getLatency() + "ms" : "";
		String nameText = addName ? color + player.getName().getString() : "";
		String gmText = addGm && playerEntry != null ?
				Formatting.GOLD + "[" + playerEntry.getGameMode().toString().substring(0, playerEntry.getGameMode() == GameMode.SPECTATOR ? 2 : 1) + "]" : "";

		if (getSetting(1).asMode().mode == 2) {
			if (addName) {
				lines.add(new String(color + pingText + " " + nameText + " " + gmText).trim().replaceAll("  *", " "));
			}

			if (addHealth) {
				lines.add(0, getHealthText(player));
			}
		} else if (addName || addHealth || addPing || addGm) {
			lines.add(new String(color + pingText + " " + nameText + " " + (addHealth ? getHealthText(player) : "") + " " + gmText).trim().replaceAll("  *", " "));
		}
	}

	// how to download future client 2020 :flushed:
	private void addUUIDFuture(UUID uuid) {
		uuidFutures.put(uuid, uuidExecutor.submit(new Callable<String>() {

			@Override
			public String call() throws Exception {
				try {
					String url = "https://api.mojang.com/user/profiles/" + uuid.toString().replace("-", "") + "/names";
					String response = Resources.toString(new URL(url), StandardCharsets.UTF_8);
					BleachLogger.logger.info("bruh uuid time: " + url);

					JsonElement json = new JsonParser().parse(response);

					if (!json.isJsonArray()) {
						BleachLogger.logger.error("[Nametags] Invalid Owner UUID: " + uuid.toString());
						return "\u00a7c[Invalid]";
					}

					JsonArray ja = json.getAsJsonArray();

					return ja.get(ja.size() - 1).getAsJsonObject().get("name").getAsString();
				} catch (IOException e) {
					BleachLogger.logger.error("[Nametags] Error Getting Owner UUID: " + uuid.toString());
					return "\u00a7c[Error]";
				}
			}
		}));
	}
}
