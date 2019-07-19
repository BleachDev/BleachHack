package bleach.hack.module.mods;

import java.util.Arrays;
import java.util.List;

import bleach.hack.gui.clickgui.SettingBase;
import bleach.hack.gui.clickgui.SettingToggle;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import net.minecraft.block.Blocks;
import net.minecraft.potion.Effects;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class NoSlow extends Module {

	private static List<SettingBase> settings = Arrays.asList(
			new SettingToggle(true, "Slowness"),
			new SettingToggle(true, "Soul Sand"));
	
	private Vec3d addMotion = Vec3d.ZERO;
	
	public NoSlow() {
		super("NoSlow", -1, Category.MOVEMENT, "Disables Stuff From Slowing You Down", settings);
	}
	
	public void onUpdate() {
		if(this.isToggled()) {
			/* Slowness */
			if(getSettings().get(0).toToggle().state && (mc.player.getActivePotionEffect(Effects.SLOWNESS) != null || mc.player.getActivePotionEffect(Effects.BLINDNESS) != null)) {
				if(mc.gameSettings.keyBindForward.isKeyDown() 
						&& mc.player.getMotion().x > -0.15 && mc.player.getMotion().x < 0.15
						&& mc.player.getMotion().z > -0.15 && mc.player.getMotion().z < 0.15) {
					mc.player.setMotion(mc.player.getMotion().add(addMotion));
					addMotion = addMotion.add(new Vec3d(0, 0, 0.05).rotateYaw(-(float)Math.toRadians(mc.player.rotationYaw)));
				}else addMotion = addMotion.scale(0.75);
			}
			
			/* Soul Sand */
			if(getSettings().get(1).toToggle().state && mc.world.getBlockState(new BlockPos(mc.player.getPositionVec())).getBlock() == Blocks.SOUL_SAND) {
				mc.player.setMotion(mc.player.getMotion().mul(3, 1, 3));
			}
		}
	}

}
