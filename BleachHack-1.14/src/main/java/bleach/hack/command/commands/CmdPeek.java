package bleach.hack.command.commands;

import bleach.hack.command.Command;
import bleach.hack.utils.BleachLogger;
import bleach.hack.utils.BleachQueue;
import net.minecraft.block.ContainerBlock;
import net.minecraft.client.gui.screen.inventory.ShulkerBoxScreen;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.inventory.container.ShulkerBoxContainer;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.NonNullList;

public class CmdPeek extends Command {

	@Override
	public String getAlias() {
		return "peek";
	}

	@Override
	public String getDescription() {
		return "Shows whats inside a container";
	}

	@Override
	public String getSyntax() {
		return ".peek";
	}

	@Override
	public void onCommand(String command, String[] args) throws Exception {
		ItemStack item = mc.player.inventory.getCurrentItem();
		
		if(!(item.getItem() instanceof BlockItem)) {
			BleachLogger.errorMessage("Must be holding a containter to peek.");
			return;
		}
		
		if(!(((BlockItem) item.getItem()).getBlock() instanceof ContainerBlock)) {
			BleachLogger.errorMessage("Must be holding a containter to peek.");
			return;
		}
		
		NonNullList<ItemStack> items = NonNullList.withSize(27, new ItemStack(Items.AIR));
		CompoundNBT nbt = item.getTag();
		
		if(nbt != null && nbt.contains("BlockEntityTag")) {
			CompoundNBT itemnbt = nbt.getCompound("BlockEntityTag");
			if(itemnbt.contains("Items")) ItemStackHelper.loadAllItems(itemnbt, items);
		}
		
		Inventory inv = new Inventory(items.toArray(new ItemStack[27]));
		
		BleachQueue.queue.add(() -> {
			mc.displayGuiScreen(new ShulkerBoxScreen(
					new ShulkerBoxContainer(420, mc.player.inventory, inv),
					mc.player.inventory,
					item.getDisplayName()));
		});
	}

}
