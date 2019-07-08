package bleach.hack.module.mods;

import bleach.hack.module.Category;
import bleach.hack.module.Module;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.item.Items;
import net.minecraft.network.play.client.CPlayerDiggingPacket;
import net.minecraft.network.play.client.CPlayerDiggingPacket.Action;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;

public class AutoTotem extends Module {

	public AutoTotem() {
		super("AutoTotem", -1, Category.COMBAT, "Automatically equips totems.", null);
	}
	
	public void onUpdate() {
		if(this.isToggled()) {
			if(mc.player.getHeldItemOffhand().getItem() == Items.TOTEM_OF_UNDYING) return;
			
			/*Inventory*/
			for(int i = 9; i < 44; i++) {
				if(mc.player.inventory.getStackInSlot(i).getItem() == Items.TOTEM_OF_UNDYING) {
					mc.playerController.windowClick(0, i, 0, ClickType.PICKUP, mc.player);
					mc.playerController.windowClick(0, 45, 0, ClickType.PICKUP, mc.player);
					return;
				}
			}
			
			/*Hotbar*/
			for(int i = 0; i < 8; i++) {
				if(mc.player.inventory.getStackInSlot(i).getItem() == Items.TOTEM_OF_UNDYING) {
					//int oldSlot = mc.player.inventory.currentItem;
					mc.player.inventory.currentItem = i;
					mc.player.connection.sendPacket(new CPlayerDiggingPacket(
							Action.SWAP_HELD_ITEMS, BlockPos.ZERO, Direction.DOWN));
					//mc.player.inventory.currentItem = oldSlot;
					return;
				}
			}
		}
	}

}
