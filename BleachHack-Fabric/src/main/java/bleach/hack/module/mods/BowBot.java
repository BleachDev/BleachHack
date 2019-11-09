package bleach.hack.module.mods;

import java.util.List;
import java.util.stream.Collectors;

import com.google.common.collect.Streams;
import com.google.common.eventbus.Subscribe;

import bleach.hack.event.events.EventTick;
import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.gui.clickgui.SettingToggle;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.utils.EntityUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.server.network.packet.PlayerActionC2SPacket;
import net.minecraft.server.network.packet.PlayerActionC2SPacket.Action;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class BowBot extends Module {

	public BowBot() {
		super("BowBot", -1, Category.COMBAT, "Automatically aims and shoots at entities",
				new SettingToggle("Shoot", true),
				new SettingSlider("Charge: ", 0.1, 1, 0.5, 2),
				new SettingToggle("Aim", false));
	}
	
	@Subscribe
	public void onTick(EventTick event) {
		if(!(mc.player.getMainHandStack().getItem() instanceof RangedWeaponItem) || !mc.player.isUsingItem()) return;
		
		if(getSettings().get(0).toToggle().state && BowItem.getPullProgress(mc.player.getItemUseTime()) > getSettings().get(1).toSlider().getValue()) {
			mc.player.stopUsingItem();
			mc.player.networkHandler.sendPacket(new PlayerActionC2SPacket(Action.RELEASE_USE_ITEM, BlockPos.ORIGIN, Direction.UP));
		}
		
		// skidded from wurst no bully pls
		if(getSettings().get(2).toToggle().state) {
			List<Entity> targets = Streams.stream(mc.world.getEntities())
					.filter(e -> e instanceof LivingEntity && e != mc.player)
					.sorted((a, b) -> Float.compare(a.distanceTo(mc.player), b.distanceTo(mc.player))).collect(Collectors.toList());
			
			if(targets.isEmpty()) return;
			
			LivingEntity target = (LivingEntity) targets.get(0);
			
			// set velocity
			float velocity = (72000 - mc.player.getItemUseTimeLeft()) / 20F;
			velocity = (velocity * velocity + velocity * 2) / 3;
			
			if(velocity > 1) velocity = 1;
			
			// set position to aim at
			double d = mc.player.getPos().add(0, mc.player.getEyeHeight(mc.player.getPose()), 0)
				.distanceTo(target.getBoundingBox().getCenter());
			double x = target.x + (target.x - target.prevX) * d
				- mc.player.x;
			double y = target.y + (target.y - target.prevY) * d
				+ target.getHeight() * 0.5 - mc.player.y
				- mc.player.getEyeHeight(mc.player.getPose());
			double z = target.z + (target.z - target.prevZ) * d
				- mc.player.z;
			
			// set yaw
			mc.player.yaw = (float) Math.toDegrees(Math.atan2(z, x)) - 90;
			
			// calculate needed pitch
			double hDistance = Math.sqrt(x * x + z * z);
			double hDistanceSq = hDistance * hDistance;
			float g = 0.006F;
			float velocitySq = velocity * velocity;
			float velocityPow4 = velocitySq * velocitySq;
			float neededPitch = (float)-Math.toDegrees(Math.atan((velocitySq - Math
				.sqrt(velocityPow4 - g * (g * hDistanceSq + 2 * y * velocitySq)))
				/ (g * hDistance)));
			
			// set pitch
			if(Float.isNaN(neededPitch)) EntityUtils.facePos(target.x, target.y + target.getHeight() / 2, target.z);
			else mc.player.pitch = neededPitch;
		}
	}

}
