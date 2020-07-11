package bleach.hack.module.mods;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import com.google.common.eventbus.Subscribe;

import bleach.hack.event.events.EventReadPacket;
import bleach.hack.event.events.EventTick;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.module.ModuleManager;
import bleach.hack.utils.BleachLogger;
import net.minecraft.client.gui.screen.ingame.ContainerScreen;
import net.minecraft.client.gui.screen.ingame.HorseScreen;
import net.minecraft.client.util.InputUtil;
import net.minecraft.container.SlotActionType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;

public class AutoDonkeyDupe extends Module {
	
	private AbstractDonkeyEntity entity;
	private List<Integer> slotsToMove = new ArrayList<>();
	private List<Integer> slotsToThrow = new ArrayList<>();

	public AutoDonkeyDupe() {
		super("AutoDonkeyDupe", KEY_UNBOUND, Category.EXPLOITS, "Automatically does the mountbypass dupe");
	}
	
	public void onEnable() {
		super.onEnable();
		
		int chest = -1;
		for (int i = 0; i < 9; i++) {
			if (mc.player.inventory.getInvStack(i).getItem() == Items.CHEST) {
				chest = i;
				break;
			}
		}
		
		if (chest == -1) {
			BleachLogger.errorMessage("No chests in hotbar");
			setToggled(false);
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
	public void onReadPacket(EventReadPacket event) {
		/*if (event.getPacket() instanceof PlayerInteractEntityC2SPacket) {
			System.out.println(((PlayerInteractEntityC2SPacket) event.getPacket()).getType());
		}*/
	}
	
	@Subscribe
	public void onTick(EventTick event) {
		if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_KEY_ESCAPE)) {
			setToggled(false);
			return;
		}
		
		int slots = getInvSize(mc.player.getVehicle());
		
		for (Entity e: mc.world.getEntities()) {
			if (e.getPos().distanceTo(mc.player.getPos()) < 6 && e instanceof AbstractDonkeyEntity) {
				entity = (AbstractDonkeyEntity) e;
			}
		}
		
		if (entity == null) return;
		isDupeTime(entity);
		
		if (slots == -1) {
			if (entity.hasChest() || mc.player.inventory.getMainHandStack().getItem() == Items.CHEST) {
				mc.player.networkHandler.sendPacket(new PlayerInteractEntityC2SPacket(entity, Hand.MAIN_HAND));
			} else {
				int chest = -1;
				for (int i = 0; i < 9; i++) {
					if (mc.player.inventory.getInvStack(i).getItem() == Items.CHEST) {
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
					mc.interactionManager.clickSlot(mc.player.container.syncId, slotsToThrow.get(0), 0, SlotActionType.THROW, mc.player);
					slotsToThrow.remove(0);
				} else {
					for (int i = 2; i < getDupeSize(entity) + 1; i++) {
						slotsToThrow.add(i);
					}
				}
			} else {
				if (mc.currentScreen instanceof ContainerScreen) mc.player.closeContainer();
				mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, Mode.PRESS_SHIFT_KEY));
				mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, Mode.RELEASE_SHIFT_KEY));
				//mc.player.detach();
			}
		} else if (!(mc.currentScreen instanceof HorseScreen)) {
			mc.player.openRidingInventory();
		} else if (slots > 0) {
			if (slotsToMove.isEmpty()) {
				boolean empty = true;
				for (int i = 2; i <= slots + 1; i++) {
					if (mc.player.container.slots.get(i).hasStack()) {
						empty = false;
						break;
					}
				}
				
				if (empty) {
					for (int i = slots + 2; i < mc.player.container.slots.size(); i++) {
						if (mc.player.container.slots.get(i).hasStack()) {
							slotsToMove.add(i);
							
							if (slotsToMove.size() >= slots) break;
						}
					}
				} else {
					((MountBypass) ModuleManager.getModule(MountBypass.class)).dontCancel = true;
					mc.player.networkHandler.sendPacket(
							new PlayerInteractEntityC2SPacket(
									entity, Hand.MAIN_HAND, entity.getPos().add(entity.getWidth() / 2, entity.getHeight() / 2, entity.getWidth() / 2)));
					((MountBypass) ModuleManager.getModule(MountBypass.class)).dontCancel = false;
					return;
				}
			}
			
			if (!slotsToMove.isEmpty()) {
				mc.interactionManager.clickSlot(mc.player.container.syncId, slotsToMove.get(0), 0, SlotActionType.QUICK_MOVE, mc.player);
				slotsToMove.remove(0);
			}
		}
		
		/*int i = 0;
		for (Slot s: mc.player.container.slots) {
			System.out.println(s.getStack() + " | " + i);
			i++;
		}*/
	}
	
	private int getInvSize(Entity e) {
		if (!(e instanceof AbstractDonkeyEntity)) return -1;
		
		if (!((AbstractDonkeyEntity)e).hasChest()) return 0;
		
		if (e instanceof LlamaEntity) {
			return 3 * ((LlamaEntity) e).method_6702();
		}
		
		return 15;
	}
	
	private boolean isDupeTime(AbstractDonkeyEntity e) {
		if (mc.player.getVehicle() != e || e.hasChest() || mc.player.container.slots.size() == 46) {
			return false;
		}
		
		if (mc.player.container.slots.size() > 38) {
			for (int i = 2; i < getDupeSize(e) + 1; i++) {
				if (mc.player.container.getSlot(i).hasStack()) {
					return true;
				}
			}
		}
		
		return false;
	}
	
	private int getDupeSize(AbstractDonkeyEntity e) {
		if (mc.player.getVehicle() != e || e.hasChest() || mc.player.container.slots.size() == 46) {
			return 0;
		}
		
		return mc.player.container.slots.size() - 38;
	}

}
