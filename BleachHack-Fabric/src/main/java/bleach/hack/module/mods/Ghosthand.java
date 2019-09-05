package bleach.hack.module.mods;

import java.util.ArrayList;
import java.util.List;

import com.google.common.eventbus.Subscribe;

import bleach.hack.event.events.EventTick;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class Ghosthand extends Module {

	public Ghosthand() {
		super("Ghosthand", -1, Category.PLAYER, "Opens Containers Through Walls", null);
	}
	
	@Subscribe
	public void onTick(EventTick eventTick) {
		if(!mc.options.keyUse.isPressed() || mc.player.isSneaking()) return;
		
		for(BlockEntity b: mc.world.blockEntities) {
			if(new BlockPos(mc.player.rayTrace(4.25, mc.getTickDelta(), false).getPos()).equals(b.getPos())) return;
		}
		
		List<BlockPos> posList = new ArrayList<>();
		
		Vec3d nextPos = new Vec3d(0,0,0.1)
				.rotateX(-(float) Math.toRadians(mc.player.pitch))
				.rotateY(-(float) Math.toRadians(mc.player.yaw));
		
		for(int i = 1; i < 50; i++) {
			BlockPos curPos = new BlockPos(mc.player.getCameraPosVec(mc.getTickDelta()).add(nextPos.multiply(i)));
			if(posList.contains(curPos)) continue;
			posList.add(curPos);
			
			for(BlockEntity b: mc.world.blockEntities) {
				if(b.getPos().equals(curPos)) {
					mc.interactionManager.interactBlock(mc.player, mc.world, Hand.MAIN_HAND,
							new BlockHitResult(mc.player.getPos(), Direction.UP, curPos, true));
					return;
				}
			}
		}
	}

}
