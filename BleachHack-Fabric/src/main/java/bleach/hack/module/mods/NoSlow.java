package bleach.hack.module.mods;

import bleach.hack.event.events.EventTick;
import bleach.hack.gui.clickgui.SettingToggle;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.utils.WorldUtils;
import com.google.common.eventbus.Subscribe;
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.Vec3d;

public class NoSlow extends Module {
	
	private Vec3d addVelocity = Vec3d.ZERO;
	
	public NoSlow() {
		super("NoSlow", -1, Category.MOVEMENT, "Disables Stuff From Slowing You Down",
				new SettingToggle(true, "Slowness"),
				new SettingToggle(true, "Soul Sand"),
				new SettingToggle(true, "Slime Blocks"),
				new SettingToggle(true, "Webs"));
	}

	@Subscribe
	public void onTick(EventTick event) {
		if(!isToggled()) return;
			
		/* Slowness */
		if(getSettings().get(0).toToggle().state && (mc.player.getStatusEffect(StatusEffects.SLOWNESS) != null || mc.player.getStatusEffect(StatusEffects.BLINDNESS) != null)) {
			if(mc.options.keyForward.isPressed() 
					&& mc.player.getVelocity().x > -0.15 && mc.player.getVelocity().x < 0.15
					&& mc.player.getVelocity().z > -0.15 && mc.player.getVelocity().z < 0.15) {
				mc.player.setVelocity(mc.player.getVelocity().add(addVelocity));
				addVelocity = addVelocity.add(new Vec3d(0, 0, 0.05).rotateY(-(float)Math.toRadians(mc.player.yaw)));
			}else addVelocity = addVelocity.multiply(0.75, 0.75, 0.75);
		}
		
		/* Soul Sand */
		if(getSettings().get(1).toToggle().state && WorldUtils.doesAABBTouchBlock(mc.player.getBoundingBox(), Blocks.SOUL_SAND)) {
			Vec3d m = new Vec3d(0, 0, 0.125).rotateY(-(float) Math.toRadians(mc.player.yaw));
			if(!mc.player.abilities.flying && mc.options.keyForward.isPressed()) {
				mc.player.setVelocity(mc.player.getVelocity().add(m));
			}
		}
		
		/* Slime Block */
		if(getSettings().get(2).toToggle().state && WorldUtils.doesAABBTouchBlock(mc.player.getBoundingBox().offset(0,-0.02,0), Blocks.SLIME_BLOCK)) {
			Vec3d m1 = new Vec3d(0, 0, 0.1).rotateY(-(float) Math.toRadians(mc.player.yaw));
			if(!mc.player.abilities.flying && mc.options.keyForward.isPressed()) {
				mc.player.setVelocity(mc.player.getVelocity().add(m1));
			}
		}
		
		/* Web */
		if(getSettings().get(3).toToggle().state && WorldUtils.doesAABBTouchBlock(mc.player.getBoundingBox(), Blocks.COBWEB)) {
			Vec3d m2 = new Vec3d(0, -1, 0.9).rotateY(-(float) Math.toRadians(mc.player.yaw));
			if(!mc.player.abilities.flying && mc.options.keyForward.isPressed()) {
				mc.player.setVelocity(mc.player.getVelocity().add(m2));
			}
		}
	}
}
