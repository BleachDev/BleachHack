/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.module.mods;

import org.bleachhack.event.events.EventInteract;
import org.bleachhack.event.events.EventTick;
import org.bleachhack.event.events.EventWorldRender;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.util.BleachLogger;
import org.bleachhack.util.InventoryUtils;
import org.bleachhack.util.render.Renderer;
import org.bleachhack.util.render.color.QuadColor;

import net.minecraft.block.PistonBlock;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AutoBedrockBreak extends Module {

	private BlockPos pos;
	private int step;

	public AutoBedrockBreak() {
		super("AutoBedrockBreak", KEY_UNBOUND, ModuleCategory.EXPLOITS, "Automatically does the bedrock break exploit.");
	}

	@Override
	public void onDisable(boolean inWorld) {
		reset();

		super.onDisable(inWorld);
	}

	@BleachSubscribe
	public void onTick(EventTick event) {
		if (pos != null) {
			switch (step) {
				case 0:
					if (!mc.world.isSpaceEmpty(new Box(pos.up(), pos.add(1, 8, 1)))) {
						reset();
						BleachLogger.info("Not enough empty space to break this block!");
					} else if (InventoryUtils.getSlot(true, i -> mc.player.getInventory().getStack(i).getItem() == Items.PISTON) == -1) {
						reset();
						BleachLogger.info("Missing pistons!");
					} else if (InventoryUtils.getSlot(true, i -> mc.player.getInventory().getStack(i).getItem() == Items.REDSTONE_BLOCK) == -1) {
						reset();
						BleachLogger.info("Missing a redstone block!");
					} else if (InventoryUtils.getSlot(true, i -> mc.player.getInventory().getStack(i).getItem() == Items.TNT) == -1) {
						reset();
						BleachLogger.info("Missing TNT!");
					} else if (InventoryUtils.getSlot(true, i -> mc.player.getInventory().getStack(i).getItem() == Items.LEVER) == -1) {
						reset();
						BleachLogger.info("Missing a lever!");
					} else if (dirtyPlace(pos.up(3), InventoryUtils.getSlot(true, i -> mc.player.getInventory().getStack(i).getItem() == Items.REDSTONE_BLOCK), Direction.DOWN)) {
						step++;
					}

					break;
				case 1:
					mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.getYaw(), 90, mc.player.isOnGround()));
					// mc.player.setPitch(90) "its jank either way"
					step++;

					break;
				case 2:
					if (dirtyPlace(pos.up(), InventoryUtils.getSlot(true, i -> mc.player.getInventory().getStack(i).getItem() == Items.PISTON), Direction.DOWN))
						step++;

					break;
				case 3:
					if (dirtyPlace(pos.up(7), InventoryUtils.getSlot(true, i -> mc.player.getInventory().getStack(i).getItem() == Items.TNT), Direction.DOWN))
						step++;

					break;
				case 4:
					if (dirtyPlace(pos.up(6), InventoryUtils.getSlot(true, i -> mc.player.getInventory().getStack(i).getItem() == Items.LEVER), Direction.UP))
						step++;

					break;
				case 5:
					if (dirtyPlace(pos.up(5), InventoryUtils.getSlot(true, i -> mc.player.getInventory().getStack(i).getItem() == Items.TNT), Direction.DOWN))
						step++;

					break;
				case 6:
					Vec3d leverCenter = Vec3d.ofCenter(pos.up(6));
					if (mc.player.getEyePos().distanceTo(leverCenter) <= 4.75) {
						mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, new BlockHitResult(leverCenter, Direction.DOWN, pos.up(6), false));
						step++;
					}

					break;
				default:
					if (mc.world.getBlockState(pos).isAir()
							|| mc.world.getBlockState(pos).getBlock() instanceof PistonBlock
							|| (mc.world.getBlockState(pos.up()).getBlock() instanceof PistonBlock
									&&  mc.world.getBlockState(pos.up()).get(PistonBlock.FACING) != Direction.UP)) {
						setEnabled(false);
						return;
					}

					if (step >= 82) {
						mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(mc.player.getX(), mc.player.getY(), mc.player.getZ(), mc.player.getYaw(), -90, mc.player.isOnGround()));
						// mc.player.setPitch(-90) "its jank either way"
					}

					if (step > 84) {
						Hand hand = InventoryUtils.selectSlot(true, i -> mc.player.getInventory().getStack(i).getItem() == Items.PISTON);
						if (hand != null) {
							mc.player.networkHandler.sendPacket(new PlayerInteractBlockC2SPacket(hand,
									new BlockHitResult(Vec3d.ofBottomCenter(pos.up()), Direction.DOWN, pos.up(), false), 0));
						}
					}

					step++;
					break;
			}
		}
	}

	@BleachSubscribe
	public void onWorldRender(EventWorldRender.Post event) {
		if (pos == null) {
			if (mc.crosshairTarget instanceof BlockHitResult
					&& !mc.world.isAir(((BlockHitResult) mc.crosshairTarget).getBlockPos())) {
				Renderer.drawBoxOutline(((BlockHitResult) mc.crosshairTarget).getBlockPos(), QuadColor.single(0xffc040c0), 2f);
			}
		} else {
			Renderer.drawBoxOutline(pos, QuadColor.single(0xffc080c0), 2f);
		}
	}

	@BleachSubscribe
	public void onInteract(EventInteract.InteractBlock event) {
		if (pos == null && !mc.world.isAir(event.getHitResult().getBlockPos())) {
			pos = event.getHitResult().getBlockPos();
			event.setCancelled(true);
		}
	}

	private boolean dirtyPlace(BlockPos pos, int slot, Direction dir) {
		Vec3d hitPos = Vec3d.ofCenter(pos).add(dir.getOffsetX() * 0.5, dir.getOffsetY() * 0.5, dir.getOffsetZ() * 0.5);
		if (mc.player.getEyePos().distanceTo(hitPos) >= 4.75 || !mc.world.getOtherEntities(null, new Box(pos)).isEmpty()) {
			return false;
		}

		Hand hand = InventoryUtils.selectSlot(slot);
		if (hand != null) {
			mc.interactionManager.interactBlock(mc.player, hand, new BlockHitResult(hitPos, dir, pos, false));
			return true;
		}

		return false;
	}

	private void reset() {
		pos = null;
		step = 0;
	}
}
