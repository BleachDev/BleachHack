package bleach.hack.module.mods;

import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Streams;
import com.google.common.eventbus.Subscribe;

import bleach.hack.BleachHack;
import bleach.hack.event.events.EventTick;
import bleach.hack.gui.clickgui.SettingMode;
import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.gui.clickgui.SettingToggle;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.utils.BleachLogger;
import bleach.hack.utils.WorldUtils;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.gui.screen.ingame.Generic3x3ContainerScreen;
import net.minecraft.client.gui.screen.ingame.HopperScreen;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.AirBlockItem;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
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
				new SettingToggle("Legit Place", true),
				new SettingToggle("Killaura", true),
				new SettingSlider("CPS: ", 0, 20, 20, 0),
				new SettingMode("CPS: ", "Clicks/Sec", "Clicks/Tick", "Tick Delay"),
				new SettingToggle("Timeout", false),
				new SettingMode("Place: ", "Auto", "Looking"));
	}
	
	public void onEnable() {
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
			Item item = mc.player.inventory.getStack(i).getItem();
			if (item == Item.fromBlock(Blocks.HOPPER)) hopper = i;
			else if (item == Item.fromBlock(Blocks.DISPENSER)) dispenser = i;
			else if (item == Item.fromBlock(Blocks.REDSTONE_BLOCK)) redstone = i;
			else if (item instanceof BlockItem && ((BlockItem) item).getBlock() instanceof ShulkerBoxBlock) shulker = i;
			else if (item instanceof BlockItem) block = i;
		}
		
		if (hopper == -1) BleachLogger.errorMessage("Missing Hopper");
		else if (dispenser == -1) BleachLogger.errorMessage("Missing Dispenser");
		else if (redstone == -1) BleachLogger.errorMessage("Missing Redstone Block");
		else if (shulker == -1) BleachLogger.errorMessage("Missing Shulker");
		else if (block == -1) BleachLogger.errorMessage("Missing Generic Block");
		
		if (hopper == -1 || dispenser == -1 || redstone == -1 || shulker == -1 || block == -1) {
			setToggled(false);
			return;
		}
		
		if (getSettings().get(5).asMode().mode == 1) {
			HitResult ray = mc.player.rayTrace(5, mc.getTickDelta(), false);
			pos = new BlockPos(ray.getPos()).up();
			
			double x = pos.getX() - mc.player.getPos().x;
			double z = pos.getZ() - mc.player.getPos().z;
			
			rot = Math.abs(x) > Math.abs(z) ? x > 0 ? new int[] {-1, 0} : new int[] {1, 0} : z > 0 ? new int[] {0, -1} : new int[] {0, 1};
			
			if (!(canPlaceBlock(pos) /*|| canPlaceBlock(pos.add(rot[0], 0, rot[1]))*/)
					|| !isBlockEmpty(pos)
					|| !isBlockEmpty(pos.add(rot[0], 0, rot[1]))
					|| !isBlockEmpty(pos.add(0, 1, 0))
					|| !isBlockEmpty(pos.add(0, 2, 0))
					|| !isBlockEmpty(pos.add(rot[0], 1, rot[1]))) {
				BleachLogger.errorMessage("Unable to place 32k");
				setToggled(false);
				return;
			}

			boolean rotate = getSettings().get(0).asToggle().state;

			placeBlock(pos, block, rotate, false);

			rotatePacket(
					pos.add(-rot[0], 1, -rot[1]).getX() + 0.5, pos.getY() + 1, pos.add(-rot[0], 1, -rot[1]).getZ() + 0.5);
			placeBlock(pos.add(0, 1, 0), dispenser, false, false);
			return;

		} else {
			for (int x = -2; x <= 2; x++) {
				for (int y = -1; y <= 1; y++) {
					for (int z = -2; z <= 2; z++) {
						rot = Math.abs(x) > Math.abs(z) ? x > 0 ? new int[] {-1, 0} : new int[] {1, 0} : z > 0 ? new int[] {0, -1} : new int[] {0, 1};
						
						pos = mc.player.getBlockPos().add(x, y, z);
						
						if (mc.player.getPos().add(0, mc.player.getEyeHeight(mc.player.getPose()), 0).distanceTo(
										mc.player.getPos().add(x - rot[0] / 2, y + 0.5, z + rot[1] / 2)) > 4.5
							|| mc.player.getPos().add(0, mc.player.getEyeHeight(mc.player.getPose()), 0).distanceTo(
									mc.player.getPos().add(x + 0.5, y + 2.5, z + 0.5)) > 4.5
							|| !(canPlaceBlock(pos) /*|| canPlaceBlock(pos.add(rot[0], 0, rot[1]))*/)
							|| !isBlockEmpty(pos)
							|| !isBlockEmpty(pos.add(rot[0], 0, rot[1]))
							|| !isBlockEmpty(pos.add(0, 1, 0))
							|| !isBlockEmpty(pos.add(0, 2, 0))
							|| !isBlockEmpty(pos.add(rot[0], 1, rot[1]))) continue;
						
						startRot = new float[] {mc.player.yaw, mc.player.pitch};
						rotateClient(pos.add(-rot[0], 1, -rot[1]).getX() + 0.5, pos.getY() + 1, pos.add(-rot[0], 1, -rot[1]).getZ() + 0.5);
						rotatePacket(pos.add(-rot[0], 1, -rot[1]).getX() + 0.5, pos.getY() + 1, pos.add(-rot[0], 1, -rot[1]).getZ() + 0.5);
						return;
					}
				}
			}
		}
		
		BleachLogger.errorMessage("Unable to place 32k");
		setToggled(false);
	}
	
	@Subscribe
	public void onTick(EventTick event) {
		if ((getSettings().get(4).asToggle().state && !active && ticksPassed > 25) || (active && !(mc.currentScreen instanceof HopperScreen))) {
			setToggled(false);
			return;
		}
		
		if (ticksPassed == 1) {
			//boolean rotate = getSettings().get(0).toToggle().state;
			
			placeBlock(pos, block, false, false);
			placeBlock(pos.add(0, 1, 0), dispenser, false, false);
			mc.player.yaw = startRot[0];
			mc.player.pitch = startRot[1];
			
			ticksPassed++;
			return;
		}
		
		if (active && getSettings().get(1).asToggle().state && timer == 0) killAura();
		
		if (mc.currentScreen instanceof Generic3x3ContainerScreen) {
			openedDispenser = true;
		}
		
		if (mc.currentScreen instanceof HopperScreen) {
			HopperScreen gui = (HopperScreen) mc.currentScreen;

			for (int i = 32; i <= 40; i++) {
				//System.out.println(EnchantmentHelper.getEnchantmentLevel(Enchantments.SHARPNESS, gui.inventorySlots.getSlot(i).getStack()));
				if (EnchantmentHelper.getLevel(Enchantments.SHARPNESS, gui.getScreenHandler().getSlot(i).getStack()) > 5) {
					mc.player.inventory.selectedSlot = i - 32;
					break;
				}
			}
			
			active = true;
			
			if (active) {
				if (getSettings().get(3).asMode().mode == 0) {
					timer = timer >= Math.round(20/getSettings().get(2).asSlider().getValue()) ? 0 : timer + 1;
				} else if (getSettings().get(3).asMode().mode == 1) {
					timer = 0;
				} else if (getSettings().get(3).asMode().mode == 2) {
					timer = timer >= getSettings().get(2).asSlider().getValue() ? 0 : timer + 1;
				}
			}
			
			//System.out.println(EnchantmentHelper.getEnchantmentLevel(Enchantments.SHARPNESS, mc.player.inventory.getCurrentItem()));
			if (!(gui.getScreenHandler().getSlot(0).getStack().getItem() instanceof AirBlockItem) && active) {
				int slot = mc.player.inventory.selectedSlot;
				boolean pull = false;
				for (int i = 40; i >= 32; i--) {
					if (gui.getScreenHandler().getSlot(i).getStack().isEmpty()) {
						slot = i;
						pull = true;
						break;
					}
				}
				
				//mc.playerController.windowClick(gui.inventorySlots.windowId, 0, 0, ClickType.QUICK_MOVE, mc.player);
				if (pull) {
					//mc.interactionManager.method_2906(gui.getContainer().syncId, 0, 0, SlotActionType.PICKUP, mc.player);
					//mc.interactionManager.method_2906(gui.getContainer().syncId, slot, 0, SlotActionType.PICKUP, mc.player);
					mc.interactionManager.clickSlot(gui.getScreenHandler().syncId, 0, 0, SlotActionType.QUICK_MOVE, mc.player);
					mc.player.inventory.selectedSlot = slot - 32;
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
			placeBlock(pos.add(0, 2, 0), redstone, getSettings().get(0).asToggle().state, false);
		}
		
		if (mc.world.getBlockState(pos.add(rot[0], 1, rot[1])).getBlock() instanceof ShulkerBoxBlock
				&& mc.world.getBlockState(pos.add(rot[0], 0, rot[1])).getBlock() != Blocks.HOPPER) {
			placeBlock(pos.add(rot[0], 0, rot[1]), hopper, getSettings().get(0).asToggle().state, false);
			openBlock(pos.add(rot[0], 0, rot[1]));
		}
		
		if (openedDispenser) dispenserTicks++;
		ticksPassed++;
	}
	
	private void killAura() {
		for (int i = 0; i < (getSettings().get(3).asMode().mode == 1 ? getSettings().get(2).asSlider().getValue() : 1); i++) {
			Entity target = null;
			
			List<Entity> players = Streams.stream(mc.world.getEntities())
					.filter((e) -> e instanceof PlayerEntity && e != mc.player && !(BleachHack.friendMang.has(e.getName().asString())))
					.sorted((a,b) -> Double.compare(a.squaredDistanceTo(mc.player), b.squaredDistanceTo(mc.player)))
					.collect(Collectors.toList());
			
			if (!players.isEmpty() && players.get(0).getPos().distanceTo(mc.player.getPos()) < 8) {
				target = players.get(0);
			} else {
				return;
			}
			
			rotateClient(target.getPos().x, target.getPos().y + 1, target.getPos().z);
			
			if (target.getPos().distanceTo(mc.player.getPos()) > 6) return;
			mc.interactionManager.attackEntity(mc.player, target);
			mc.player.swingHand(Hand.MAIN_HAND);
		}
	}
	
	private void openBlock(BlockPos pos) {
		for (Direction d: Direction.values()) {
			Block neighborBlock = mc.world.getBlockState(pos.offset(d)).getBlock();
			if (!WorldUtils.NONSOLID_BLOCKS.contains(neighborBlock)) continue;
			
			mc.interactionManager.interactBlock(
					mc.player, mc.world, Hand.MAIN_HAND, new BlockHitResult(Vec3d.of(pos), d.getOpposite(), pos, false));
			return;
		}
	}
	
	private boolean placeBlock(BlockPos pos, int slot, boolean rotate, boolean rotateBack) {
		if (!isBlockEmpty(pos)) return false;
		
		if (slot != mc.player.inventory.selectedSlot) mc.player.inventory.selectedSlot = slot;
		
		for (Direction d: Direction.values()) {
			Block neighborBlock = mc.world.getBlockState(pos.offset(d)).getBlock();
			
			Vec3d vec = new Vec3d(pos.getX() + 0.5 + d.getOffsetX() * 0.5,
					pos.getY() + 0.5 + d.getOffsetY() * 0.5,
					pos.getZ() + 0.5 + d.getOffsetZ() * 0.5);
			
			if (WorldUtils.NONSOLID_BLOCKS.contains(neighborBlock)
					|| mc.player.getPos().add(0, mc.player.getEyeHeight(mc.player.getPose()), 0).distanceTo(vec) > 4.25) continue;
			
			float[] rot = new float[] { mc.player.yaw, mc.player.pitch };
			
			if (rotate) rotatePacket(vec.x, vec.y, vec.z);
			if (WorldUtils.RIGHTCLICKABLE_BLOCKS.contains(neighborBlock)) mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, Mode.PRESS_SHIFT_KEY));
			mc.interactionManager.interactBlock(
					mc.player, mc.world, Hand.MAIN_HAND, new BlockHitResult(Vec3d.of(pos), d.getOpposite(), pos, false));
			if (WorldUtils.RIGHTCLICKABLE_BLOCKS.contains(neighborBlock)) mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, Mode.RELEASE_SHIFT_KEY));
			if (rotateBack) mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookOnly(rot[0], rot[1], mc.player.isOnGround()));
			return true;
		}
		return false;
	}
	
	private boolean isBlockEmpty(BlockPos pos) {
		if (!WorldUtils.NONSOLID_BLOCKS.contains(mc.world.getBlockState(pos).getBlock())) return false;
		
		Box box = new Box(pos);
		for (Entity e: mc.world.getEntities()) {
			if (e instanceof LivingEntity && box.intersects(e.getBoundingBox())) return false;
		}
		
		return true;
	}
	
	private boolean canPlaceBlock(BlockPos pos) {
		if (!isBlockEmpty(pos)) return false;
		for (Direction d: Direction.values()) {
			if (WorldUtils.NONSOLID_BLOCKS.contains(mc.world.getBlockState(pos.offset(d)).getBlock())
					|| mc.player.getPos().add(0, mc.player.getEyeHeight(mc.player.getPose()), 0).distanceTo(
							new Vec3d(pos.getX() + 0.5 + d.getOffsetX() * 0.5,
					pos.getY() + 0.5 + d.getOffsetY() * 0.5,
					pos.getZ() + 0.5 + d.getOffsetZ() * 0.5)) > 4.25) continue;
			return true;
		}
		return false;
	}
	
	private void rotateClient(double x, double y, double z) {
		double diffX = x - mc.player.getPos().x;
		double diffY = y - (mc.player.getPos().y + mc.player.getEyeHeight(mc.player.getPose()));
		double diffZ = z - mc.player.getPos().z;
			
		double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
			
		float yaw = (float)Math.toDegrees(Math.atan2(diffZ, diffX)) - 90F;
		float pitch = (float)-Math.toDegrees(Math.atan2(diffY, diffXZ));
			
		mc.player.yaw += MathHelper.wrapDegrees(yaw - mc.player.yaw);
		mc.player.pitch += MathHelper.wrapDegrees(pitch - mc.player.pitch);
	}
	
	private void rotatePacket(double x, double y, double z) {
		double diffX = x - mc.player.getPos().x;
		double diffY = y - (mc.player.getPos().y + mc.player.getEyeHeight(mc.player.getPose()));
		double diffZ = z - mc.player.getPos().z;
			
		double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
			
		float yaw = (float)Math.toDegrees(Math.atan2(diffZ, diffX)) - 90F;
		float pitch = (float)-Math.toDegrees(Math.atan2(diffY, diffXZ));
		
		mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookOnly(yaw, pitch, mc.player.isOnGround()));
	}
}
