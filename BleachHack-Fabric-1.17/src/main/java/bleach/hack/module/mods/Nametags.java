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

import org.lwjgl.opengl.GL11;

import com.google.common.eventbus.Subscribe;
import com.google.common.io.Resources;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.systems.RenderSystem;

import bleach.hack.BleachHack;
import bleach.hack.command.commands.CmdEntityStats;
import bleach.hack.event.events.EventEntityRender;
import bleach.hack.event.events.EventTick;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.util.WorldRenderUtils;
import bleach.hack.util.world.EntityUtils;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.passive.HorseBaseEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
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
						new SettingToggle("Owner", true).withDesc("Hows the owner of the pet if its tameable"),
						new SettingToggle("HorseStats", false).withDesc("Shows the entities stats if its a horse")),
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

		Map<UUID, String> cacheCopy = new HashMap<>(uuidCache);
		uuidCache.clear();

		cacheCopy.forEach((u, s) -> {
			if (!s.startsWith("\u00a7c")) uuidCache.put(u, s);
		});

		super.onDisable();
	}

	public void onEnable() {
		super.onEnable();
		uuidExecutor = Executors.newFixedThreadPool(4);
	}

	@Subscribe
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

	@Subscribe
	public void onLivingLabelRender(EventEntityRender.Single.Label event) {
		if ((EntityUtils.isAnimal(event.getEntity()) && getSetting(3).asToggle().state)
				|| (event.getEntity() instanceof Monster && getSetting(4).asToggle().state)
				|| (event.getEntity() instanceof PlayerEntity && getSetting(2).asToggle().state)
				|| (event.getEntity() instanceof ItemEntity && getSetting(5).asToggle().state))
			event.setCancelled(true);
	}

	@Subscribe
	public void onLivingRender(EventEntityRender.Single.Post event) {
		List<String> lines = new ArrayList<>();
		double scale = 0;

		Vec3d rPos = getRenderPos(event.getEntity());

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
			if (event.getEntity() == mc.player || event.getEntity().hasPassenger(mc.player) || mc.player.hasPassenger(event.getEntity())) {
				return;
			}

			LivingEntity e = (LivingEntity) event.getEntity();

			if (e instanceof PlayerEntity && getSetting(2).asToggle().state) {
				scale = Math.max(getSetting(2).asToggle().getChild(0).asSlider().getValue() * (mc.cameraEntity.distanceTo(e) / 20), 1);

				addNameHealthLine(lines, e, BleachHack.friendMang.has(e.getName().getString()) ? Formatting.AQUA : Formatting.RED,
						getSetting(2).asToggle().getChild(1).asToggle().state,
						getSetting(2).asToggle().getChild(2).asToggle().state);
			} else if (EntityUtils.isAnimal(e) && getSetting(3).asToggle().state) {
				scale = Math.max(getSetting(3).asToggle().getChild(0).asSlider().getValue() * (mc.cameraEntity.distanceTo(e) / 20), 1);

				addNameHealthLine(lines, e, Formatting.GREEN,
						getSetting(3).asToggle().getChild(1).asToggle().state,
						getSetting(3).asToggle().getChild(2).asToggle().state);

				if (e instanceof HorseBaseEntity || e instanceof TameableEntity) {
					boolean tame = e instanceof HorseBaseEntity
							? ((HorseBaseEntity) e).isTame() : ((TameableEntity) e).isTamed();

					UUID ownerUUID = e instanceof HorseBaseEntity
							? ((HorseBaseEntity) e).getOwnerUuid() : ((TameableEntity) e).getOwnerUuid();

					if (getSetting(3).asToggle().getChild(3).asToggle().state && !e.isBaby()
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

					if (getSetting(3).asToggle().getChild(5).asToggle().state && e instanceof HorseBaseEntity) {
						HorseBaseEntity he = (HorseBaseEntity) e;

						lines.add(0, Formatting.GREEN.toString()
								+ CmdEntityStats.getSpeed(he) + " m/s"
								+ Formatting.GRAY + " | " + Formatting.GREEN
								+ CmdEntityStats.getJumpHeight(he) + " Jump");
					}
				}
			} else if (e instanceof Monster && getSetting(4).asToggle().state) {
				scale = Math.max(getSetting(4).asToggle().getChild(0).asSlider().getValue() * (mc.cameraEntity.distanceTo(e) / 20), 1);

				addNameHealthLine(lines, e, Formatting.DARK_PURPLE,
						getSetting(4).asToggle().getChild(1).asToggle().state,
						getSetting(4).asToggle().getChild(2).asToggle().state);
			}

			/* Drawing Items */
			double c = 0;
			double lscale = scale * 0.4;
			double up = ((0.3 + lines.size() * 0.25) * scale) + lscale / 2;

			if (getSetting(0).asMode().mode == 0) {
				drawItem(rPos.x, rPos.y + up, rPos.z, -2.5, 0, lscale, e.getEquippedStack(EquipmentSlot.MAINHAND));
				drawItem(rPos.x, rPos.y + up, rPos.z, 2.5, 0, lscale, e.getEquippedStack(EquipmentSlot.OFFHAND));

				for (ItemStack i : e.getArmorItems()) {
					drawItem(rPos.x, rPos.y + up, rPos.z, c + 1.5, 0, lscale, i);
					c--;
				}
			} else if (getSetting(0).asMode().mode == 1) {
				drawItem(rPos.x, rPos.y + up, rPos.z, -1.25, 0, lscale, e.getEquippedStack(EquipmentSlot.MAINHAND));
				drawItem(rPos.x, rPos.y + up, rPos.z, 1.25, 0, lscale, e.getEquippedStack(EquipmentSlot.OFFHAND));

				for (ItemStack i : e.getArmorItems()) {
					drawItem(rPos.x, rPos.y + up, rPos.z, 0, c, lscale, i);
					c++;
				}
			}
		}

		if (!lines.isEmpty()) {
			float offset = 0.25f + lines.size() * 0.25f;

			for (String s: lines) {
				WorldRenderUtils.drawText(s, rPos.x, rPos.y + (offset * scale), rPos.z, scale);

				offset -= 0.25f;
			}
		}
	}

	private void drawItem(double x, double y, double z, double offX, double offY, double scale, ItemStack item) {
		MatrixStack matrix = WorldRenderUtils.drawGuiItem(x, y, z, offX, offY, scale, item);

		matrix.scale(-0.05F, -0.05F, 0.05f);

		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.enableDepthTest();
		RenderSystem.depthFunc(GL11.GL_ALWAYS);

		if (!item.isEmpty()) {
			int w = mc.textRenderer.getWidth("x" + item.getCount()) / 2;
			mc.textRenderer.draw("x" + item.getCount(), 7 - w, 3, 0xffffff, true, matrix.peek().getModel(),
					mc.getBufferBuilders().getEntityVertexConsumers(), true, 0, 0xf000f0);
		}

		matrix.scale(0.85F, 0.85F, 1F);

		int c = 0;
		for (Entry<Enchantment, Integer> m : EnchantmentHelper.get(item).entrySet()) {
			String text = I18n.translate(m.getKey().getName(2).getString());

			if (text.isEmpty())
				continue;

			String subText = text.substring(0, Math.min(text.length(), 2)) + m.getValue();

			int w1 = mc.textRenderer.getWidth(subText) / 2;
			mc.textRenderer.draw(subText, -2 - w1, c * 10 - 19,
					m.getKey() == Enchantments.VANISHING_CURSE || m.getKey() == Enchantments.BINDING_CURSE ? 0xff5050 : 0xffb0e0,
							true, matrix.peek().getModel(), mc.getBufferBuilders().getEntityVertexConsumers(), true, 0, 0xf000f0);
			c--;
		}

		RenderSystem.depthFunc(GL11.GL_LEQUAL);
		RenderSystem.disableDepthTest();

		RenderSystem.disableBlend();
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
	
	private void addNameHealthLine(List<String> lines, LivingEntity entity, Formatting color, boolean addName, boolean addHealth) {
		if (getSetting(1).asMode().mode == 1) {
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

	// how to download future client 2020 :flushed:
	private void addUUIDFuture(UUID uuid) {
		uuidFutures.put(uuid, uuidExecutor.submit(new Callable<String>() {

			@Override
			public String call() throws Exception {
				try {
					String url = "https://api.mojang.com/user/profiles/" + uuid.toString().replace("-", "") + "/names";
					String response = Resources.toString(new URL(url), StandardCharsets.UTF_8);
					System.out.println("bruh uuid time: " + url);

					JsonElement json = new JsonParser().parse(response);

					if (!json.isJsonArray()) {
						System.out.println("[Nametags] Invalid Owner UUID: " + uuid.toString());
						return "\u00a7c[Invalid]";
					}

					JsonArray ja = json.getAsJsonArray();

					return ja.get(ja.size() - 1).getAsJsonObject().get("name").getAsString();
				} catch (IOException e) {
					System.out.println("[Nametags] Error Getting Owner UUID: " + uuid.toString());
					return "\u00a7c[Error]";
				}
			}
		}));
	}
}
