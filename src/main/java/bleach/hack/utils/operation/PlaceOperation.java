package bleach.hack.utils.operation;

import bleach.hack.utils.WorldUtils;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class PlaceOperation extends Operation {

	protected Item item;

	public PlaceOperation(BlockPos pos, Item item) {
		this.pos = pos;
		this.item = item;
	}

	@Override
	public boolean canExecute() {
		for (int i = 0; i < 9; i++) {
			if (mc.player.inventory.getStack(i).getItem() == item) {
				return mc.player.getPos().add(0, mc.player.getEyeHeight(mc.player.getPose()), 0).distanceTo(Vec3d.of(pos).add(0.5, 0.5, 0.5)) < 4.5;
			}
		}

		return false;
	}

	@Override
	public boolean execute() {
		for (int i = 0; i < 9; i++) {
			if (mc.player.inventory.getStack(i).getItem() == item) {
				return WorldUtils.placeBlock(pos, i, false, false);
			}
		}

		return false;
	}

	@Override
	public boolean verify() {
		return true;
	}

	public Item getItem() {
		return item;
	}
}
