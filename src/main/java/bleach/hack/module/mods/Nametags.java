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

import java.net.URI;
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

import org.apache.commons.io.IOUtils;

import com.google.common.base.Charsets;
import com.google.common.eventbus.Subscribe;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;

import bleach.hack.event.events.EventEntityRender;
import bleach.hack.event.events.EventTick;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.utils.BleachLogger;
import bleach.hack.utils.EntityUtils;
import bleach.hack.utils.WorldRenderUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.HorseBaseEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class Nametags extends Module {
	
	private ExecutorService uuidExecutor;
	// list of future client licenses
	private Map<UUID, Future<String>> uuidFutures = new HashMap<>();

	private Queue<UUID> uuidQueue = new ArrayDeque<>();
	private Map<UUID, String> uuidCache = new HashMap<>();
	private Set<UUID> failedUUIDs = new HashSet<>();
	private long lastLookup = 0;

	public Nametags() {
		super("Nametags", KEY_UNBOUND, Category.RENDER, "Shows bigger/cooler nametags above entities.",
				new SettingMode("Armor", "H", "V", "None").withDesc("How to show items/armor"),
				new SettingMode("Health", "Number", "Bar", "Percent").withDesc("How to show health"),
				new SettingToggle("Players", true).withDesc("Show player nametags").withChildren(
						new SettingSlider("Size", 0.5, 5, 2, 1).withDesc("Size of the nametags"),
						new SettingToggle("Name", true).withDesc("Shows the name of the entity"),
						new SettingToggle("Health", true).withDesc("Shows the health of the entity")),
				new SettingToggle("Animals", true).withDesc("Show animal nametags").withChildren(
						new SettingSlider("Size", 0.5, 5, 1, 1).withDesc("Size of the nametags"),
						new SettingToggle("Name", true).withDesc("Shows the name of the entity"),
						new SettingToggle("Health", true).withDesc("Shows the health of the entity"),
						new SettingToggle("Tamed", false).withDesc("Shows if the animal is tamed").withChildren(
								new SettingMode("If Not", "Show", "Hide").withDesc("What to show if the animal isn't tame")),
						new SettingToggle("Owner", true).withDesc("Hows the owner of the pet if its tameable")),
				new SettingToggle("Mobs", false).withDesc("Show mob nametags").withChildren(
						new SettingSlider("Size", 0.5, 5, 1, 1).withDesc("Size of the nametags"),
						new SettingToggle("Name", true).withDesc("Shows the name of the entity"),
						new SettingToggle("Health", true).withDesc("Shows the health of the entity")),
				new SettingToggle("Items", true).withDesc("Shows nametags for items").withChildren(
						new SettingSlider("Size", 0.5, 5, 1, 1).withDesc("Size of the nametags"),
						new SettingToggle("Custom Name", true).withDesc("Shows the items custom name if it has it"),
						new SettingToggle("Item Count", true).withDesc("Shows how many items are in the stack")));
	}

	public void onDisable() {
		uuidQueue.clear();
		failedUUIDs.clear();
		uuidExecutor.shutdownNow();
		uuidFutures.clear();
		
		super.onDisable();
	}
	
	public void onEnable() {
		super.onEnable();
		uuidExecutor = Executors.newFixedThreadPool(4);
	}

	@Subscribe
	public void onTick(EventTick event) {
		// collecting that revenue from all them future copies
		for (Entry<UUID, Future<String>> f: new HashMap<>(uuidFutures).entrySet()) {
			if (f.getValue().isDone()) {
				try {
					String s = f.getValue().get();
					
					if (s != null) {
						uuidCache.put(f.getKey(), s);
					} else {
						BleachLogger.errorMessage("Error Getting Owner UUID: " + f.getKey().toString());
						failedUUIDs.add(f.getKey());
					}
					
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

	@Subscribe
	public void onLivingLabelRender(EventEntityRender.Label event) {
		if ((EntityUtils.isAnimal(event.getEntity()) && getSetting(3).asToggle().state)
				|| (event.getEntity() instanceof Monster && getSetting(4).asToggle().state)
				|| (event.getEntity() instanceof PlayerEntity && getSetting(2).asToggle().state)
				|| (event.getEntity() instanceof ItemEntity && getSetting(5).asToggle().state))
			event.setCancelled(true);
	}

	@Subscribe
	public void onLivingRender(EventEntityRender.Render event) {
		List<String> lines = new ArrayList<>();
		double scale = 0;

		if (event.getEntity() instanceof ItemEntity && getSetting(5).asToggle().state) {
			ItemEntity e = (ItemEntity) event.getEntity();

			scale = Math.max(getSetting(5).asToggle().getChild(0).asSlider().getValue() * (mc.cameraEntity.distanceTo(e) / 20), 1);

			lines.add(Formatting.GOLD + e.getName().getString()
					+ (getSetting(5).asToggle().getChild(2).asToggle().state
							? Formatting.YELLOW + " [x" + e.getStack().getCount() + "]" : ""));

			if (!e.getName().getString().equals(e.getStack().getName().getString()) && getSetting(5).asToggle().getChild(1).asToggle().state) {
				lines.add(0, Formatting.GOLD + "\"" + e.getStack().getName().getString() + Formatting.GOLD + "\"");
			}
		} else if (event.getEntity() instanceof LivingEntity) {
			if (event.getEntity() == mc.player || event.getEntity().hasPassenger(mc.player) || mc.player.hasPassenger(event.getEntity()))
				return;

			LivingEntity e = (LivingEntity) event.getEntity();

			// Color before name
			Formatting color = (e.isInvisible() ? Formatting.YELLOW
					: e instanceof Monster ? Formatting.DARK_PURPLE
							: EntityUtils.isAnimal(e) ? Formatting.GREEN
									: e.isSneaking() ? Formatting.GOLD
											: e instanceof PlayerEntity ? Formatting.RED : Formatting.WHITE);

			String health = getHealthText(e);

			if (e instanceof PlayerEntity && getSetting(2).asToggle().state) {
				scale = Math.max(getSetting(2).asToggle().getChild(0).asSlider().getValue() * (mc.cameraEntity.distanceTo(e) / 20), 1);

				if (getSetting(1).asMode().mode == 1) {
					if (getSetting(2).asToggle().getChild(1).asToggle().state)
						lines.add(color + e.getName().getString());

					if (getSetting(2).asToggle().getChild(2).asToggle().state)
						lines.add(0, health);
				} else if (getSetting(2).asToggle().getChild(1).asToggle().state || getSetting(2).asToggle().getChild(2).asToggle().state) {
					lines.add(
							(getSetting(2).asToggle().getChild(1).asToggle().state ? color + e.getName().getString()
									+ (getSetting(2).asToggle().getChild(2).asToggle().state ? " " : "") : "")
							+ (getSetting(2).asToggle().getChild(2).asToggle().state ? health : ""));
				}
			} else if (EntityUtils.isAnimal(e) && getSetting(3).asToggle().state) {
				scale = Math.max(getSetting(3).asToggle().getChild(0).asSlider().getValue() * (mc.cameraEntity.distanceTo(e) / 20), 1);

				if (getSetting(1).asMode().mode == 1) {
					if (getSetting(3).asToggle().getChild(1).asToggle().state)
						lines.add(color + e.getName().getString());

					if (getSetting(3).asToggle().getChild(2).asToggle().state)
						lines.add(0, health);
				} else if (getSetting(3).asToggle().getChild(1).asToggle().state || getSetting(3).asToggle().getChild(2).asToggle().state) {
					lines.add(
							(getSetting(3).asToggle().getChild(1).asToggle().state ? color + e.getName().getString()
									+ (getSetting(3).asToggle().getChild(2).asToggle().state ? " " : "") : "")
							+ (getSetting(3).asToggle().getChild(2).asToggle().state ? health : ""));
				}

				if (e instanceof TameableEntity) {
					TameableEntity te = (TameableEntity) e;

					if (getSetting(3).asToggle().getChild(3).asToggle().state && !te.isBaby()
							&& (getSetting(3).asToggle().getChild(3).asToggle().getChild(0).asMode().mode != 1 || te.isTamed()))
						lines.add(0, te.isTamed() ? Formatting.GREEN + "Tamed: Yes" : Formatting.RED + "Tamed: No");

					if (getSetting(3).asToggle().getChild(4).asToggle().state && te.getOwnerUuid() != null) {
						if (uuidCache.containsKey(te.getOwnerUuid())) {
							lines.add(0, Formatting.GREEN + "Owner: " + uuidCache.get(te.getOwnerUuid()));
						} else if (failedUUIDs.contains(te.getOwnerUuid())) {
							lines.add(0, Formatting.GREEN + "Owner: " + Formatting.GRAY + "Invalid UUID!");
						} else {
							// Try to see if the owner is online on the server before calling the mojang api
							Optional<GameProfile> owner = mc.player.networkHandler.getPlayerList().stream()
									.filter(en -> en.getProfile() != null && te.getOwnerUuid().equals(en.getProfile().getId()) && en.getProfile().getName() != null)
									.map(en -> en.getProfile()).findFirst();
							
							if (owner.isPresent()) {
								uuidCache.put(te.getOwnerUuid(), owner.get().getName());
							} else if (!uuidQueue.contains(te.getOwnerUuid()) && !uuidFutures.containsKey(te.getOwnerUuid())) {
								uuidQueue.add(te.getOwnerUuid());
							}
							
							lines.add(0, Formatting.GREEN + "Owner: " + Formatting.GRAY + "Loading...");
						}
					}
				} else if (e instanceof HorseBaseEntity) {
					HorseBaseEntity he = (HorseBaseEntity) e;

					if (getSetting(3).asToggle().getChild(3).asToggle().state && !he.isBaby()
							&& (getSetting(3).asToggle().getChild(3).asToggle().getChild(0).asMode().mode != 1 || he.isTame()))
						lines.add(0, he.isTame() ? Formatting.GREEN + "Tamed: Yes" : Formatting.RED + "Tamed: No");

					if (getSetting(3).asToggle().getChild(4).asToggle().state && he.getOwnerUuid() != null) {
						if (uuidCache.containsKey(he.getOwnerUuid())) {
							lines.add(0, Formatting.GREEN + "Owner: " + uuidCache.get(he.getOwnerUuid()));
						} else if (failedUUIDs.contains(he.getOwnerUuid())) {
							lines.add(0, Formatting.GREEN + "Owner: " + Formatting.GRAY + "Invalid UUID!");
						} else {
							// Try to see if the owner is online on the server before calling the mojang api
							Optional<GameProfile> owner = mc.player.networkHandler.getPlayerList().stream()
									.filter(en -> en.getProfile() != null && he.getOwnerUuid().equals(en.getProfile().getId()) && en.getProfile().getName() != null)
									.map(en -> en.getProfile()).findFirst();
							
							if (owner.isPresent()) {
								uuidCache.put(he.getOwnerUuid(), owner.get().getName());
							} else if (!uuidQueue.contains(he.getOwnerUuid()) && !uuidFutures.containsKey(he.getOwnerUuid())) {
								uuidQueue.add(he.getOwnerUuid());
							}
							
							lines.add(0, Formatting.GREEN + "Owner: " + Formatting.GRAY + "Loading...");
						}
					}
				}
			} else if (e instanceof Monster && getSetting(4).asToggle().state) {
				scale = Math.max(getSetting(4).asToggle().getChild(0).asSlider().getValue() * (mc.cameraEntity.distanceTo(e) / 20), 1);

				if (getSetting(1).asMode().mode == 1) {
					if (getSetting(4).asToggle().getChild(1).asToggle().state)
						lines.add(color + e.getName().getString());

					if (getSetting(4).asToggle().getChild(2).asToggle().state)
						lines.add(0, health);
				} else if (getSetting(4).asToggle().getChild(1).asToggle().state || getSetting(3).asToggle().getChild(2).asToggle().state) {
					lines.add(
							(getSetting(4).asToggle().getChild(1).asToggle().state ? color + e.getName().getString()
									+ (getSetting(4).asToggle().getChild(2).asToggle().state ? " " : "") : "")
							+ (getSetting(4).asToggle().getChild(2).asToggle().state ? health : ""));
				}
			}

			/* Drawing Items */
			// drawing items died
		}

		if (!lines.isEmpty()) {
			Vec3d pos = getRenderPos(event.getEntity());
			float offset = 0.25f + lines.size() * 0.25f;

			for (String s: lines) {
				WorldRenderUtils.drawText(s, pos.x, pos.y + (offset * scale), pos.z, scale);
				offset -= 0.25f;
			}
		}
	}

	private Vec3d getRenderPos(Entity e) {
		return mc.currentScreen != null && mc.currentScreen.isPauseScreen() ? e.getPos().add(0, e.getHeight(), 0)
				: new Vec3d(
						e.lastRenderX + (e.getX() - e.lastRenderX) * mc.getTickDelta(),
						(e.lastRenderY + (e.getY() - e.lastRenderY) * mc.getTickDelta()) + e.getHeight(),
						e.lastRenderZ + (e.getZ() - e.lastRenderZ) * mc.getTickDelta());
	}

	private String getHealthText(LivingEntity e) {
		if (getSetting(1).asMode().mode == 0) {
			return Formatting.GREEN + "[" + getHealthColor(e) + (int) (e.getHealth() + e.getAbsorptionAmount()) + Formatting.GREEN + "/" + (int) e.getMaxHealth() + "]";
		} else if (getSetting(1).asMode().mode == 1) {
			/* Health bar */
			String health = "";
			/* - Add Green Normal Health */
			for (int i = 0; i < e.getHealth(); i++)
				health += Formatting.GREEN + "|";
			/* - Add Red Empty Health (Remove Based on absorption amount) */
			for (int i = 0; i < MathHelper.clamp(e.getAbsorptionAmount(), 0, e.getMaxHealth() - e.getHealth()); i++)
				health += Formatting.YELLOW + "|";
			/* Add Yellow Absorption Health */
			for (int i = 0; i < e.getMaxHealth() - (e.getHealth() + e.getAbsorptionAmount()); i++)
				health += Formatting.RED + "|";
			/* Add "+??" to the end if the entity has extra hearts */
			if (e.getAbsorptionAmount() - (e.getMaxHealth() - e.getHealth()) > 0) {
				health += Formatting.YELLOW + " +" + (int) (e.getAbsorptionAmount() - (e.getMaxHealth() - e.getHealth()));
			}

			return health;
		} else {
			return getHealthColor(e) + "[" + (int) ((e.getHealth() + e.getAbsorptionAmount()) / e.getMaxHealth() * 100) + "%]";
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
	
	// how to download future client 2020 :flushed:
	private void addUUIDFuture(UUID uuid) {
		uuidFutures.put(uuid, uuidExecutor.submit(new Callable<String>() {

			@Override
			public String call() throws Exception {
				try {
					String response = IOUtils.toString(
							new URI("https://api.mojang.com/user/profiles/" + uuid.toString().replace("-", "") + "/names"), Charsets.UTF_8);
					System.out.println("bruh uuid time https://api.mojang.com/user/profiles/" + uuid.toString().replace("-", "") + "/names");
					
					JsonArray ja = new JsonParser().parse(response).getAsJsonArray();
					
					return ja.get(ja.size() - 1).getAsJsonObject().get("name").getAsString();
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}
		}));
	}
}
