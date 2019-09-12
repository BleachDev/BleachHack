package bleach.hack.module.mods;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.google.common.eventbus.Subscribe;

import bleach.hack.event.events.EventTick;
import bleach.hack.gui.clickgui.SettingBase;
import bleach.hack.gui.clickgui.SettingMode;
import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.gui.clickgui.SettingToggle;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.utils.EntityUtils;
import bleach.hack.utils.WorldUtils;
import net.minecraft.block.Blocks;
import net.minecraft.server.network.packet.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RayTraceContext;

public class Nuker extends Module {

	private static List<SettingBase> settings = Arrays.asList(
			new SettingMode("Mode: ", "Survival", "Creative", "Multi"),
			new SettingSlider(1, 6, 4.2, 1, "Range: "),
			new SettingToggle(false, "Flatten"),
			new SettingToggle(false, "Rotate"),
			new SettingMode("Sort: ", "Normal", "Hardness"));
	
	public Nuker() {
		super("Nuker", -1, Category.WORLD, "Breaks blocks around you", settings);
	}
	
	@Subscribe
	public void onTick(EventTick event) {
		double range = getSettings().get(1).toSlider().getValue();
		List<BlockPos> blocks = new ArrayList<>();
		
		/* Add blocks around player */
		for(int x = (int) range; x >= (int) -range; x--) {
			for(int y = (int) range; y >= (getSettings().get(2).toToggle().state ? 0 : (int) -range); y--) {
				for(int z = (int) range; z >= (int) -range; z--) {
					BlockPos pos = new BlockPos(mc.player.getPos().add(x, y + 0.1, z));
					if(!canSeeBlock(pos) || mc.world.getBlockState(pos).getBlock() == Blocks.AIR || WorldUtils.isFluid(pos)) continue;
					blocks.add(pos);
				}
			}
		}
		
		if(getSettings().get(4).toMode().mode == 1) blocks.sort((a, b) -> Float.compare(
				mc.world.getBlockState(a).getHardness(null, a), mc.world.getBlockState(b).getHardness(null, b)));
		
		for(BlockPos pos: blocks) {
			Vec3d vec = new Vec3d(pos).add(0.5, 0.5, 0.5);
			
			if(mc.player.getPos().distanceTo(vec) > range + 0.5) continue;
			
			Direction dir = null;
			double dist = 6.9;
			for(Direction d: Direction.values()) {
				double dist2 = mc.player.getPos().distanceTo(new Vec3d(pos.offset(d)).add(0.5, 0.5, 0.5));
				if(dist2 > range || mc.world.getBlockState(pos.offset(d)).getBlock() != Blocks.AIR || dist2 > dist) continue;
				dist = dist2;
				dir = d;
			}
			
			if(dir == null) continue;
			
			if(getSettings().get(3).toToggle().state) {
				float[] prevRot = new float[] {mc.player.yaw, mc.player.pitch};
				EntityUtils.facePos(vec.x, vec.y, vec.z);
				mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.LookOnly(
						mc.player.yaw, mc.player.pitch, mc.player.onGround));
				mc.player.yaw = prevRot[0];
				mc.player.pitch = prevRot[1];
			}
			
			if(getSettings().get(0).toMode().mode == 0) mc.interactionManager.method_2902(pos, dir);
			else mc.interactionManager.attackBlock(pos, dir);
			
			mc.player.swingHand(Hand.MAIN_HAND);
			if(getSettings().get(0).toMode().mode != 2) return;
		}
	}
	
	public boolean canSeeBlock(BlockPos pos) {
		double diffX = pos.getX() + 0.5 - mc.player.getCameraPosVec(mc.getTickDelta()).x;
		double diffY = pos.getY() + 0.5 - mc.player.getCameraPosVec(mc.getTickDelta()).y;
		double diffZ = pos.getZ() + 0.5 - mc.player.getCameraPosVec(mc.getTickDelta()).z;
			
		double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
			
		float yaw = mc.player.yaw + MathHelper.wrapDegrees((float)Math.toDegrees(Math.atan2(diffZ, diffX)) - 90 - mc.player.yaw);
		float pitch = mc.player.pitch + MathHelper.wrapDegrees((float)-Math.toDegrees(Math.atan2(diffY, diffXZ)) - mc.player.pitch);
		
		Vec3d rotation = new Vec3d(
				(double)(MathHelper.sin(-yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F)),
				(double)(-MathHelper.sin(pitch * 0.017453292F)),
				(double)(MathHelper.cos(-yaw * 0.017453292F) * MathHelper.cos(pitch * 0.017453292F)));
		
		Vec3d rayVec = mc.player.getCameraPosVec(mc.getTickDelta()).add(rotation.x * 6, rotation.y * 6, rotation.z * 6);
		return mc.world.rayTrace(new RayTraceContext(mc.player.getCameraPosVec(mc.getTickDelta()),
				rayVec, RayTraceContext.ShapeType.OUTLINE, RayTraceContext.FluidHandling.NONE, mc.player))
				.getBlockPos().equals(pos);
	}

}
