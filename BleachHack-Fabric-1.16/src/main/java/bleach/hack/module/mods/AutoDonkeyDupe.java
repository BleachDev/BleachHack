package bleach.hack.module.mods;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import com.google.common.eventbus.Subscribe;

import bleach.hack.event.events.EventSendPacket;
import bleach.hack.event.events.EventTick;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.module.ModuleManager;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.util.BleachLogger;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.gui.screen.ingame.HorseScreen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket.InteractionType;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;

public class AutoDonkeyDupe extends Module {

	private AbstractDonkeyEntity entity;
	private List<Integer> slotsToMove = new ArrayList<>();
	private List<Integer> slotsToThrow = new ArrayList<>();

	private boolean firstFrameSneak = false;

	public AutoDonkeyDupe() {
		super("AutoDonkeyDupe", KEY_UNBOUND, Category.EXPLOITS, "Automatically does the mountbypass dupe (PRESS ESCAPE TO CANCEL)",
				new SettingSlider("Limit", 1, 15, 15, 0).withDesc("Max chests to dupe"),
				new SettingMode("Mode", "Instant", "Single").withDesc("Whether to dupe all at once one chest per tick"),
				new SettingToggle("Shulkers Only", true).withDesc("Only dupe shulkers"));
	}

	public void onEnable() {
		super.onEnable();

		int chest = -1;
		for (int i = 0; i < 9; i++) {
			if (mc.player.inventory.getStack(i).getItem() == Items.CHEST) {
				chest = i;
				break;
			}
		}

		if (chest == -1) {
			BleachLogger.errorMessage("No chests in hotbar");
			setEnabled(false);
			return;
		}

		if (!(mc.currentScreen instanceof HorseScreen)) {
			BleachLogger.infoMessage("Open a donkey gui to start");
		}
	}

	public void onDisable() {
		super.onDisable();

		entity = null;
		slotsToMove.clear();
		slotsToThrow.clear();
	}

	@Subscribe
	public void onSendPacket(EventSendPacket event) {
		if (((MountBypass) ModuleManager.getModule("MountBypass")).dontCancel)
			return;

		if (event.getPacket() instanceof PlayerInteractEntityC2SPacket
				&& ((PlayerInteractEntityC2SPacket) event.getPacket()).getType() == InteractionType.INTERACT_AT
				&& ((PlayerInteractEntityC2SPacket) event.getPacket()).getEntity(mc.world) instanceof AbstractDonkeyEntity) {
			event.setCancelled(true);
		}
	}

	@Subscribe
	public void onTick(EventTick event) {
		if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_KEY_ESCAPE)) {
			setEnabled(false);
			return;
		}

		int slots = getSetting(0).asSlider().getValue() <= 0 ? getInvSize(mc.player.getVehicle())
				: Math.min((int) getSetting(0).asSlider().getValue(), getInvSize(mc.player.getVehicle()));

		for (Entity e : mc.world.getEntities()) {
			if (e.getPos().distanceTo(mc.player.getPos()) < 6
					&& e instanceof AbstractDonkeyEntity && ((AbstractDonkeyEntity) e).isTame()) {
				entity = (AbstractDonkeyEntity) e;
			}
		}

		if (entity == null)
			return;

		if (firstFrameSneak) {
			mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, Mode.RELEASE_SHIFT_KEY));
			firstFrameSneak = false;
			return;
		}

		boolean instant = getSetting(1).asMode().mode == 0;

		if (slots == -1) {
			if (entity.hasChest() || mc.player.inventory.getMainHandStack().getItem() == Items.CHEST) {
				// mc.player.networkHandler.sendPacket(new PlayerInteractEntityC2SPacket(entity,
				// Hand.MAIN_HAND));
				// mc.interactionManager.interactEntityAtLocation(playerEntity_1, entity_1,
				// entityHitResult_1, hand_1)
				mc.player.networkHandler.sendPacket(new PlayerInteractEntityC2SPacket(entity, Hand.MAIN_HAND, false));
			} else {
				int chest = -1;
				for (int i = 0; i < 9; i++) {
					if (mc.player.inventory.getStack(i).getItem() == Items.CHEST) {
						chest = i;
						break;
					}
				}

				if (chest != -1) {
					mc.player.inventory.selectedSlot = chest;
				}
			}

			return;
		} else if (slots == 0) {
			if (isDupeTime(entity)) {
				if (!slotsToThrow.isEmpty()) {
					if (instant) {
						for (int i : slotsToThrow) {
							mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, i, 1, SlotActionType.THROW, mc.player);
						}
						slotsToThrow.clear();
					} else {
						mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slotsToThrow.get(0), 1, SlotActionType.THROW, mc.player);
						slotsToThrow.remove(0);
					}
				} else {
					for (int i = 2; i < getDupeSize(entity) + 1; i++) {
						slotsToThrow.add(i);
					}
				}
			} else {
				mc.player.closeHandledScreen();
				mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, Mode.PRESS_SHIFT_KEY));
				firstFrameSneak = true;
			}
		} else if (!(mc.currentScreen instanceof HorseScreen)) {
			mc.player.openRidingInventory();
		} else if (slots > 0) {
			if (slotsToMove.isEmpty()) {
				boolean empty = true;
				for (int i = 2; i <= slots + 1; i++) {
					if (mc.player.currentScreenHandler.slots.get(i).hasStack()) {
						empty = false;
						break;
					}
				}

				if (empty) {
					for (int i = slots + 2; i < mc.player.currentScreenHandler.slots.size(); i++) {
						if (mc.player.currentScreenHandler.slots.get(i).hasStack()) {
							if (mc.player.currentScreenHandler.slots.get(i).getStack().getItem() == Items.CHEST)
								continue;
							if (!(mc.player.currentScreenHandler.slots.get(i).getStack().getItem() instanceof BlockItem
									&& ((BlockItem) mc.player.currentScreenHandler.slots.get(i).getStack().getItem()).getBlock() instanceof ShulkerBoxBlock)
									&& getSetting(2).asToggle().state)
								continue;
							slotsToMove.add(i);

							if (slotsToMove.size() >= slots)
								break;
						}
					}
				} else {
					((MountBypass) ModuleManager.getModule("MountBypass")).dontCancel = true;
					mc.player.networkHandler.sendPacket(
							new PlayerInteractEntityC2SPacket(entity, Hand.MAIN_HAND, entity.getBoundingBox().getCenter(), false));
					((MountBypass) ModuleManager.getModule("MountBypass")).dontCancel = false;
					return;
				}
			}

			if (!slotsToMove.isEmpty()) {
				if (instant) {
					for (int i : slotsToMove)
						mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, i, 0, SlotActionType.QUICK_MOVE, mc.player);
					slotsToMove.clear();
				} else {
					mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slotsToMove.get(0), 0, SlotActionType.QUICK_MOVE, mc.player);
					slotsToMove.remove(0);
				}
			}
		}

		/* int i = 0; for (Slot s: mc.player.container.slots) {
		 * System.out.println(s.getStack() + " | " + i); i++; } */
	}

	private int getInvSize(Entity e) {
		if (!(e instanceof AbstractDonkeyEntity))
			return -1;

		if (!((AbstractDonkeyEntity) e).hasChest())
			return 0;

		if (e instanceof LlamaEntity) {
			return 3 * ((LlamaEntity) e).getStrength();
		}

		return 15;
	}

	private boolean isDupeTime(AbstractDonkeyEntity e) {
		if (mc.player.getVehicle() != e || e.hasChest() || mc.player.currentScreenHandler.slots.size() == 46) {
			return false;
		}

		if (mc.player.currentScreenHandler.slots.size() > 38) {
			for (int i = 2; i < getDupeSize(e) + 1; i++) {
				if (mc.player.currentScreenHandler.getSlot(i).hasStack()) {
					return true;
				}
			}
		}

		return false;
	}

	private int getDupeSize(AbstractDonkeyEntity e) {
		if (mc.player.getVehicle() != e || e.hasChest() || mc.player.currentScreenHandler.slots.size() == 46) {
			return 0;
		}

		return mc.player.currentScreenHandler.slots.size() - 38;
	}

}
