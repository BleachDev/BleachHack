/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.module.mods;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang3.RandomUtils;
import com.google.common.eventbus.Subscribe;

import bleach.hack.event.events.EventOpenScreen;
import bleach.hack.event.events.EventReadPacket;
import bleach.hack.event.events.EventSendPacket;
import bleach.hack.event.events.EventTick;
import bleach.hack.event.events.EventWorldRender;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.setting.other.SettingLists;
import bleach.hack.setting.other.SettingRotate;
import bleach.hack.util.InventoryUtils;
import bleach.hack.util.render.WorldRenderUtils;
import bleach.hack.util.world.WorldUtils;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.CloseHandledScreenC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AutoSteal extends Module {

	// Keep a memory of all the items instead of the handler to track them in Project/NoGui mode
	private List<ItemStack> currentItems = null;
	private int currentSyncId;
	private BlockPos currentPos = null;

	private Map<BlockPos, Integer> opened = new HashMap<>();

	private int lastSteal = 0;
	private int currentTime = 0;

	private int lastOpen = 0;

	public AutoSteal() {
		super("AutoSteal", KEY_UNBOUND, Category.WORLD, "Automatically steals items from chests",
				new SettingMode("Gui", "Normal", "Project", "NoGui" /* Novoline cheststealer*/).withDesc("How to display the chest gui when stealing"),
				new SettingSlider("Delay", 0, 20, 2, 0).withDesc("Delay between taking items (in ticks)"),
				new SettingSlider("RandDelay", 0, 8, 2, 0).withDesc("Extra random delay between taking items (in ticks)"),
				new SettingToggle("Automatic", true).withDesc("Automatically opens chest when you are near them").withChildren(
						new SettingSlider("Range", 0.5, 6, 4.5, 2).withDesc("Range to open chests"),
						new SettingSlider("Cooldown", 1, 90, 30, 0).withDesc("How long to wait before reopening the same chest (in seconds)"),
						new SettingRotate(false).withDesc("Rotates to chests when opening them")),
				new SettingToggle("Filter", false).withDesc("Filters certain items").withChildren(
						new SettingMode("Mode", "Blacklist", "Whitelist").withDesc("How to handle the list"),
						SettingLists.newItemList("Edit Items", "Edit Filtered Items").withDesc("Edit the filtered items")));
	}

	public boolean isBlacklisted(Item item) {
		SettingToggle setting = getSetting(4).asToggle();
		return setting.state
				&& ((setting.getChild(0).asMode().mode == 0 && setting.getChild(1).asList(Item.class).contains(item))
						|| (setting.getChild(0).asMode().mode == 1 && !setting.getChild(1).asList(Item.class).contains(item)));
	}

	@Override
	public void onDisable() {
		opened.clear();
		currentItems = null;
		currentSyncId = -1;
		super.onDisable();
	}

	@Subscribe
	public void onTick(EventTick event) {
		currentTime++;

		for (Entry<BlockPos, Integer> e: new HashMap<>(opened).entrySet()) {
			if (e.getValue() <= 0) opened.remove(e.getKey());
			else opened.replace(e.getKey(), e.getValue() - 1);
		}

		if (currentItems != null && currentSyncId != -1) {
			if (currentTime - lastSteal >= getSetting(1).asSlider().getValue()) {
				for (int i = 0; i < currentItems.size(); i++) {
					if (!currentItems.get(i).isEmpty()) {
						if (isBlacklisted(currentItems.get(i).getItem())) {
							continue;
						}

						int fi = i;
						boolean openSlot = InventoryUtils.getSlot(false, j -> mc.player.inventory.getStack(j).isEmpty()
								|| (mc.player.inventory.getStack(j).isStackable()
										&& mc.player.inventory.getStack(j).getCount() < mc.player.inventory.getStack(j).getMaxCount()
										&& currentItems.get(fi).isItemEqual(mc.player.inventory.getStack(j)))) != 1;

						if (openSlot) {
							mc.interactionManager.clickSlot(currentSyncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
							currentItems.set(i, ItemStack.EMPTY);

							lastSteal = currentTime + RandomUtils.nextInt(0, getSetting(2).asSlider().getValueInt() + 1);
						}

						return;
					}
				}

				if (getSetting(0).asMode().mode >= 1 || getSetting(3).asToggle().state) {
					mc.openScreen(null);
					mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(currentSyncId));
					return;
				}
			}
		} else if (currentItems == null && currentSyncId == -1 && getSetting(3).asToggle().state) {
			for (BlockEntity be: mc.world.blockEntities) {
				if (!opened.containsKey(be.getPos())
						&& be instanceof ChestBlockEntity
						&& mc.player.getPos().add(0, mc.player.getEyeHeight(mc.player.getPose()), 0)
						.distanceTo(Vec3d.ofCenter(be.getPos())) <= getSetting(3).asToggle().getChild(0).asSlider().getValue() + 0.25) {

					Vec3d lookVec = Vec3d.ofCenter(be.getPos()).add(0, 0.5, 0);
					if (getSetting(3).asToggle().getChild(2).asRotate().state) {
						WorldUtils.facePosAuto(lookVec.x, lookVec.y, lookVec.z, getSetting(3).asToggle().getChild(2).asRotate());
					}

					mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND,
							new BlockHitResult(lookVec, Direction.UP, be.getPos(), false));
					opened.put(be.getPos(), getSetting(3).asToggle().getChild(1).asSlider().getValueInt() * 20);
					return;
				}
			}
		}
	}

	@Subscribe
	public void onWorldRender(EventWorldRender.Post event) {
		if (currentItems != null && currentPos != null) {
			if (getSetting(0).asMode().mode == 1) {
				List<ItemStack> renderItems = new ArrayList<>(currentItems);

				for (int i = 0; i < renderItems.size(); i += 9) {
					List<ItemStack> subList = renderItems.subList(i, Math.min(i + 9, renderItems.size()));
					if (subList.stream().allMatch(ItemStack::isEmpty)) {
						subList.clear();
						i -= 9;
					}
				}

				Vec3d startPos = new Vec3d(currentPos.getX() + 0.5, currentPos.getY() + 1 + (renderItems.size() / 9) * 0.4, currentPos.getZ() + 0.5);

				for (int i = 0; i < renderItems.size(); i++) {
					MatrixStack matrix = WorldRenderUtils.drawGuiItem(startPos.x, startPos.y - (i / 9) * 0.4, startPos.z, i % 9 - 4.5, 0, 0.3, renderItems.get(i));

					if (renderItems.get(i).getCount() > 1) {
						matrix.scale(-0.05F, -0.05F, 1f);
						int w = mc.textRenderer.getWidth("" + renderItems.get(i).getCount()) / 2;
						mc.textRenderer.drawWithShadow(matrix, "" + renderItems.get(i).getCount(), 7 - w, 3, 0xffffff);
					}
				}
			} else if (getSetting(0).asMode().mode == 2) {
				WorldRenderUtils.drawText("[" + currentItems.stream().filter(i -> !i.isEmpty() && !isBlacklisted(i.getItem())).count() + "]",
						currentPos.getX() + 0.5, currentPos.getY() + 1.2, currentPos.getZ() + 0.5, 0.8f);
			}
		}
	}

	@Subscribe
	public void onOpenScreen(EventOpenScreen event) {
		currentSyncId = -1;

		if (mc.player != null) {
			if (event.getScreen() instanceof HandledScreen) {
				ScreenHandler handler = ((HandledScreen<?>) event.getScreen()).getScreenHandler();

				if (handler instanceof GenericContainerScreenHandler) {
					currentSyncId = handler.syncId;
					lastOpen = currentTime;

					if (getSetting(0).asMode().mode >= 1) {
						event.setCancelled(true);
					}
				} else {
					currentItems = null;
					mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(currentSyncId));
				}
			} else {
				currentItems = null;
				mc.player.networkHandler.sendPacket(new CloseHandledScreenC2SPacket(currentSyncId));
			}
		}
	}

	@Subscribe
	public void onSendPacket(EventSendPacket event) {
		if (event.getPacket() instanceof CloseHandledScreenC2SPacket) {
			currentItems = null;
			currentSyncId = -1;
		}

		if (event.getPacket() instanceof PlayerInteractBlockC2SPacket) {
			BlockPos pos = ((PlayerInteractBlockC2SPacket) event.getPacket()).getBlockHitResult().getBlockPos();

			if (mc.world.getBlockState(pos).getBlock() instanceof ChestBlock) {
				currentPos = pos;
			}
		}
	}

	@Subscribe
	public void onReadPacket(EventReadPacket event) {
		if (event.getPacket() instanceof InventoryS2CPacket) {
			InventoryS2CPacket packet = (InventoryS2CPacket) event.getPacket();

			if ((lastOpen - currentTime >= 2 || currentItems == null) && packet.getContents().size() == 63 || packet.getContents().size() == 90) {
				currentItems = packet.getContents().subList(0, packet.getContents().size() - 36);
				//currentSyncId = -1;
			}
		} else if (currentItems != null && event.getPacket() instanceof ScreenHandlerSlotUpdateS2CPacket) {
			ScreenHandlerSlotUpdateS2CPacket packet = (ScreenHandlerSlotUpdateS2CPacket) event.getPacket();

			if (packet.getSyncId() == currentSyncId && packet.getSlot() >= 0 && packet.getSlot() < currentItems.size()) {
				currentItems.set(packet.getSlot(), packet.getItemStack());
			}
		}
	}

}
