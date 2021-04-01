package bleach.hack.module.mods;

import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Streams;
import com.google.common.eventbus.Subscribe;

import bleach.hack.BleachHack;
import bleach.hack.event.events.EventTick;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.setting.other.SettingRotate;
import bleach.hack.util.BleachLogger;
import bleach.hack.util.world.WorldUtils;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.gui.screen.ingame.Generic3x3ContainerScreen;
import net.minecraft.client.gui.screen.ingame.HopperScreen;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AirBlockItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class Dispenser32k extends Module {

	private BlockPos pos;

	private int hopper;
	private int dispenser;
	private int redstone;
	private int shulker;
	private int block;
	private int[] rot;
	private float[] startRot;

	private boolean active;
	private boolean openedDispenser;
	private int dispenserTicks;

	private int ticksPassed;
	private int timer = 0;

	public Dispenser32k() {
		super("Dispenser32k", KEY_UNBOUND, Category.COMBAT, "ching chong auto32k no skid 2020",
				new SettingRotate(true),
				new SettingToggle("Killaura", true).withDesc("Automatically attacks"),
				new SettingSlider("CPS", 0, 20, 20, 0).withDesc("Attack Speed"),
				new SettingMode("CPS", "Clicks/Sec", "Clicks/Tick", "Tick Delay").withDesc("How to interperet CPS"),
				new SettingToggle("Timeout", false).withDesc("Stops attacks after a few ticks"),
				new SettingMode("Place", "Auto", "Looking").withDesc("Where to place the dispenser"));
	}

	public void onEnable() {
		if (mc.world == null)
			return;

		super.onEnable();

		ticksPassed = 0;
		hopper = -1;
		dispenser = -1;
		redstone = -1;
		shulker = -1;
		block = -1;
		active = false;
		openedDispenser = false;
		dispenserTicks = 0;
		timer = 0;

		for (int i = 0; i <= 8; i++) {
			Item item = mc.player.getInventory().getStack(i).getItem();
			if (item == Item.fromBlock(Blocks.HOPPER))
				hopper = i;
			else if (item == Item.fromBlock(Blocks.DISPENSER))
				dispenser = i;
			else if (item == Item.fromBlock(Blocks.REDSTONE_BLOCK))
				redstone = i;
			else if (item instanceof BlockItem && ((BlockItem) item).getBlock() instanceof ShulkerBoxBlock)
				shulker = i;
			else if (item instanceof BlockItem)
				block = i;
		}

		if (hopper == -1)
			BleachLogger.errorMessage("Missing Hopper");
		else if (dispenser == -1)
			BleachLogger.errorMessage("Missing Dispenser");
		else if (redstone == -1)
			BleachLogger.errorMessage("Missing Redstone Block");
		else if (shulker == -1)
			BleachLogger.errorMessage("Missing Shulker");
		else if (block == -1)
			BleachLogger.errorMessage("Missing Generic Block");

		if (hopper == -1 || dispenser == -1 || redstone == -1 || shulker == -1 || block == -1) {
			setEnabled(false);
			return;
		}

		if (getSetting(5).asMode().mode == 1) {
			HitResult ray = mc.player.raycast(5, mc.getTickDelta(), false);
			pos = new BlockPos(ray.getPos()).up();

			double x = pos.getX() - mc.player.getPos().x;
			double z = pos.getZ() - mc.player.getPos().z;

			rot = Math.abs(x) > Math.abs(z) ? x > 0 ? new int[] { -1, 0 } : new int[] { 1, 0 } : z > 0 ? new int[] { 0, -1 } : new int[] { 0, 1 };

			if (!(WorldUtils.canPlaceBlock(pos) /* || canPlaceBlock(pos.add(rot[0], 0, rot[1])) */)
					|| !WorldUtils.isBlockEmpty(pos)
					|| !WorldUtils.isBlockEmpty(pos.add(rot[0], 0, rot[1]))
					|| !WorldUtils.isBlockEmpty(pos.add(0, 1, 0))
					|| !WorldUtils.isBlockEmpty(pos.add(0, 2, 0))
					|| !WorldUtils.isBlockEmpty(pos.add(rot[0], 1, rot[1]))) {
				BleachLogger.errorMessage("Unable to place 32k");
				setEnabled(false);
				return;
			}

			WorldUtils.placeBlock(pos, block, getSetting(0).asRotate(), false, true);

			WorldUtils.facePosPacket(
					pos.add(-rot[0], 1, -rot[1]).getX() + 0.5, pos.getY() + 1, pos.add(-rot[0], 1, -rot[1]).getZ() + 0.5);
			WorldUtils.placeBlock(pos.add(0, 1, 0), dispenser, 0, false, true);
			return;

		} else {
			for (int x = -2; x <= 2; x++) {
				for (int y = -1; y <= 1; y++) {
					for (int z = -2; z <= 2; z++) {
						rot = Math.abs(x) > Math.abs(z) ? x > 0 ? new int[] { -1, 0 } : new int[] { 1, 0 } : z > 0 ? new int[] { 0, -1 } : new int[] { 0, 1 };

						pos = mc.player.getBlockPos().add(x, y, z);

						if (mc.player.getPos().add(0, mc.player.getEyeHeight(mc.player.getPose()), 0).distanceTo(
								mc.player.getPos().add(x - rot[0] / 2, y + 0.5, z + rot[1] / 2)) > 4.5
								|| mc.player.getPos().add(0, mc.player.getEyeHeight(mc.player.getPose()), 0).distanceTo(
										mc.player.getPos().add(x + 0.5, y + 2.5, z + 0.5)) > 4.5
								|| !(WorldUtils.canPlaceBlock(pos) /* || canPlaceBlock(pos.add(rot[0], 0, rot[1])) */)
								|| !WorldUtils.isBlockEmpty(pos)
								|| !WorldUtils.isBlockEmpty(pos.add(rot[0], 0, rot[1]))
								|| !WorldUtils.isBlockEmpty(pos.add(0, 1, 0))
								|| !WorldUtils.isBlockEmpty(pos.add(0, 2, 0))
								|| !WorldUtils.isBlockEmpty(pos.add(rot[0], 1, rot[1])))
							continue;

						startRot = new float[] { mc.player.yaw, mc.player.pitch };
						WorldUtils.facePos(pos.add(-rot[0], 1, -rot[1]).getX() + 0.5, pos.getY() + 1, pos.add(-rot[0], 1, -rot[1]).getZ() + 0.5);
						WorldUtils.facePosPacket(pos.add(-rot[0], 1, -rot[1]).getX() + 0.5, pos.getY() + 1, pos.add(-rot[0], 1, -rot[1]).getZ() + 0.5);
						return;
					}
				}
			}
		}

		BleachLogger.errorMessage("Unable to place 32k");
		setEnabled(false);
	}

	@Subscribe
	public void onTick(EventTick event) {
		if ((getSetting(4).asToggle().state && !active && ticksPassed > 25) || (active && !(mc.currentScreen instanceof HopperScreen))) {
			setEnabled(false);
			return;
		}

		if (ticksPassed == 1) {
			// boolean rotate = getSetting(0).toToggle().state;

			WorldUtils.placeBlock(pos, block, 0, false, true);
			WorldUtils.placeBlock(pos.add(0, 1, 0), dispenser, 0, false, true);
			mc.player.yaw = startRot[0];
			mc.player.pitch = startRot[1];

			ticksPassed++;
			return;
		}

		if (active && getSetting(1).asToggle().state && timer == 0)
			killAura();

		if (mc.currentScreen instanceof Generic3x3ContainerScreen) {
			openedDispenser = true;
		}

		if (mc.currentScreen instanceof HopperScreen) {
			HopperScreen gui = (HopperScreen) mc.currentScreen;

			for (int i = 32; i <= 40; i++) {
				// System.out.println(EnchantmentHelper.getEnchantmentLevel(Enchantments.SHARPNESS,
				// gui.inventorySlots.getSlot(i).getStack()));
				if (EnchantmentHelper.getLevel(Enchantments.SHARPNESS, gui.getScreenHandler().getSlot(i).getStack()) > 5) {
					mc.player.getInventory().selectedSlot = i - 32;
					break;
				}
			}

			active = true;

			if (active) {
				if (getSetting(3).asMode().mode == 0) {
					timer = timer >= Math.round(20 / getSetting(2).asSlider().getValue()) ? 0 : timer + 1;
				} else if (getSetting(3).asMode().mode == 1) {
					timer = 0;
				} else if (getSetting(3).asMode().mode == 2) {
					timer = timer >= getSetting(2).asSlider().getValue() ? 0 : timer + 1;
				}
			}

			// System.out.println(EnchantmentHelper.getEnchantmentLevel(Enchantments.SHARPNESS,
			// mc.player.getInventory().getCurrentItem()));
			if (!(gui.getScreenHandler().getSlot(0).getStack().getItem() instanceof AirBlockItem) && active) {
				int slot = mc.player.getInventory().selectedSlot;
				boolean pull = false;
				for (int i = 40; i >= 32; i--) {
					if (gui.getScreenHandler().getSlot(i).getStack().isEmpty()) {
						slot = i;
						pull = true;
						break;
					}
				}

				// mc.playerController.windowClick(gui.inventorySlots.windowId, 0, 0,
				// ClickType.QUICK_MOVE, mc.player);
				if (pull) {
					// mc.interactionManager.method_2906(gui.getContainer().syncId, 0, 0,
					// SlotActionType.PICKUP, mc.player);
					// mc.interactionManager.method_2906(gui.getContainer().syncId, slot, 0,
					// SlotActionType.PICKUP, mc.player);
					mc.interactionManager.clickSlot(gui.getScreenHandler().syncId, 0, 0, SlotActionType.QUICK_MOVE, mc.player);
					mc.player.getInventory().selectedSlot = slot - 32;
				}
			}
		}

		if (ticksPassed == 2) {
			openBlock(pos.add(0, 1, 0));
		}

		if (openedDispenser && dispenserTicks == 0) {
			mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 36 + shulker, 0, SlotActionType.QUICK_MOVE, mc.player);
		}

		if (dispenserTicks == 1) {
			mc.openScreen(null);
			WorldUtils.placeBlock(pos.add(0, 2, 0), redstone, getSetting(0).asRotate(), false, true);
		}

		if (mc.world.getBlockState(pos.add(rot[0], 1, rot[1])).getBlock() instanceof ShulkerBoxBlock
				&& mc.world.getBlockState(pos.add(rot[0], 0, rot[1])).getBlock() != Blocks.HOPPER) {
			WorldUtils.placeBlock(pos.add(rot[0], 0, rot[1]), hopper, getSetting(0).asRotate(), false, true);
			openBlock(pos.add(rot[0], 0, rot[1]));
		}

		if (openedDispenser)
			dispenserTicks++;
		ticksPassed++;
	}

	private void killAura() {
		for (int i = 0; i < (getSetting(3).asMode().mode == 1 ? getSetting(2).asSlider().getValue() : 1); i++) {
			Entity target = null;

			List<Entity> players = Streams.stream(mc.world.getEntities())
					.filter(e -> e instanceof PlayerEntity && e != mc.player && !(BleachHack.friendMang.has(e.getName().getString())))
					.sorted((a, b) -> Double.compare(a.squaredDistanceTo(mc.player), b.squaredDistanceTo(mc.player)))
					.collect(Collectors.toList());

			if (!players.isEmpty() && players.get(0).getPos().distanceTo(mc.player.getPos()) < 8) {
				target = players.get(0);
			} else {
				return;
			}

			WorldUtils.facePos(target.getPos().x, target.getPos().y + 1, target.getPos().z);

			if (target.getPos().distanceTo(mc.player.getPos()) > 6)
				return;
			mc.interactionManager.attackEntity(mc.player, target);
		}
	}

	private void openBlock(BlockPos pos) {
		for (Direction d : Direction.values()) {
			if (mc.world.getBlockState(pos.method_35851(d)).getMaterial().isReplaceable()) {
				mc.interactionManager.interactBlock(
						mc.player, mc.world, Hand.MAIN_HAND, new BlockHitResult(Vec3d.of(pos), d.getOpposite(), pos, false));
				return;
			}
		}
	}
}
