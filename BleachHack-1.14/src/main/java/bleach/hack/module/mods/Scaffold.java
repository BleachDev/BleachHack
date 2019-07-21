package bleach.hack.module.mods;

import java.util.Arrays;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import bleach.hack.gui.clickgui.SettingBase;
import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.utils.WorldUtils;
import net.minecraft.item.BlockItem;
import net.minecraft.network.play.client.CPlayerTryUseItemOnBlockPacket;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.Vec3d;

public class Scaffold extends Module {

	private static List<SettingBase> settings = Arrays.asList(
			new SettingSlider(0, 1.5, 0.3, 1, "Range: "));
	
	public Scaffold() {
		super("Scaffold", GLFW.GLFW_KEY_N, Category.PLAYER, "Places blocks under you", settings);
	}
	
	public void onUpdate() {
		if(this.isToggled()) {
			if(!(mc.player.inventory.getCurrentItem().getItem() instanceof BlockItem)) return;
			
			double range = getSettings().get(0).toSlider().getValue();
			for(int r = 0; r < 5; r++) {
				Vec3d r1 = new Vec3d(0,-1,0);
				if(r == 1) r1 = r1.add(range, 0, 0);
				if(r == 2) r1 = r1.add(-range, 0, 0);
				if(r == 3) r1 = r1.add(0, 0, range);
				if(r == 4) r1 = r1.add(0, 0, -range);
				
				if(WorldUtils.NONSOLID_BLOCKS.contains(
						mc.world.getBlockState(new BlockPos(mc.player.getPositionVec().add(r1))).getBlock())) {
					placeBlockAuto(new BlockPos(mc.player.getPositionVec().add(r1)));
					return;
				}
			}
		}
	}
	
	public void placeBlockAuto(BlockPos block) {
		for(Direction d: Direction.values()) {
			if(!WorldUtils.NONSOLID_BLOCKS.contains(mc.world.getBlockState(block.offset(d)).getBlock())) {
				mc.player.connection.sendPacket(new CPlayerTryUseItemOnBlockPacket(Hand.MAIN_HAND,
						new BlockRayTraceResult(new Vec3d(block), d.getOpposite(), block.offset(d), false)));
				
				mc.player.swingArm(Hand.MAIN_HAND);
				mc.world.playSound(block, SoundEvents.BLOCK_NOTE_BLOCK_HAT, SoundCategory.BLOCKS, 1f, 1f, false);
				return;
			}
		}
	}

}
