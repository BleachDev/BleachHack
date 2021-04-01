package bleach.hack.util.operation;

import bleach.hack.util.RenderUtils;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class RemoveOperation extends Operation {

	public RemoveOperation(BlockPos pos) {
		this.pos = pos;
	}

	@Override
	public boolean canExecute() {
		if (mc.player.getPos().add(0, mc.player.getEyeHeight(mc.player.getPose()), 0).distanceTo(Vec3d.of(pos).add(0.5, 0.5, 0.5)) < 4.5) {
			for (Direction d: Direction.values()) {
				if (!mc.world.getBlockState(pos.method_35851(d)).isSideSolidFullSquare(mc.world, pos.method_35851(d), d.getOpposite())) {
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public boolean execute() {
		for (Direction d: Direction.values()) {
			if (!mc.world.getBlockState(pos.method_35851(d)).isSideSolidFullSquare(mc.world, pos.method_35851(d), d.getOpposite())) {
				mc.interactionManager.updateBlockBreakingProgress(pos, d);
				mc.player.swingHand(Hand.MAIN_HAND);

				return mc.world.getBlockState(pos).isAir();
			}
		}

		return false;
	}

	@Override
	public boolean verify() {
		return mc.world.getBlockState(pos).isAir();
	}

	@Override
	public void render() {
		RenderUtils.drawFilledBox(pos, 1f, 0f, 0f, 0.3f);
	}

}
