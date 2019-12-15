package bleach.hack.module.mods;

import java.util.List;
import java.util.stream.Collectors;

import bleach.hack.event.events.EventTick;
import com.google.common.eventbus.Subscribe;
import net.minecraft.server.network.packet.ClientCommandC2SPacket;
import org.lwjgl.glfw.GLFW;

import com.google.common.collect.Streams;

import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.gui.clickgui.SettingToggle;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.utils.EntityUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.packet.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;

public class Killaura extends Module {
	
	private int delay = 0;
	
	public Killaura() {
		super("Killaura", GLFW.GLFW_KEY_K, Category.COMBAT, "Automatically attacks entities",
				new SettingToggle("Players", true),
				new SettingToggle("Mobs", true),
				new SettingToggle("Animals", false),
				new SettingToggle("Armor Stands", false),
				new SettingToggle("Aimbot", true),
				new SettingToggle("Thru Walls", false),
				new SettingToggle("1.9 Delay", false),
				new SettingSlider("Range: ", 0, 6, 4.25, 2),
				new SettingSlider("CPS: ", 0, 20, 8, 0));
	}

	@Subscribe
	public void onTick(EventTick event) {
		delay++;
		int reqDelay = (int) Math.round(20/getSettings().get(8).toSlider().getValue());
		
		List<Entity> targets = Streams.stream(mc.world.getEntities())
				.filter(e -> (e instanceof PlayerEntity && getSettings().get(0).toToggle().state)
						|| (e instanceof Monster && getSettings().get(1).toToggle().state)
						|| (EntityUtils.isAnimal(e) && getSettings().get(2).toToggle().state)
						|| (e instanceof ArmorStandEntity && getSettings().get(3).toToggle().state))
				.sorted((a, b) -> Float.compare(a.distanceTo(mc.player), b.distanceTo(mc.player))).collect(Collectors.toList());

		for(Entity e: targets) {
			if(mc.player.distanceTo(e) > getSettings().get(7).toSlider().getValue()
					|| ((LivingEntity)e).getHealth() <= 0 || e.getEntityName().equals(mc.getSession().getUsername()) || e == mc.player.getVehicle()
					|| (!mc.player.canSee(e) && !getSettings().get(5).toToggle().state)) continue;
			
			if(getSettings().get(4).toToggle().state) EntityUtils.facePos(e.getX(), e.getY() + e.getHeight()/2, e.getZ());
				
			if(((delay > reqDelay || reqDelay == 0) && !getSettings().get(6).toToggle().state) || 
					(mc.player.getAttackCooldownProgress(mc.getTickDelta()) == 1.0f && getSettings().get(6).toToggle().state)) {
				boolean wasSprinting = mc.player.isSprinting();

				if(wasSprinting) mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.STOP_SPRINTING));

				mc.player.networkHandler.sendPacket(new PlayerInteractEntityC2SPacket(e));
				mc.player.attack(e);
				mc.player.swingHand(Hand.MAIN_HAND);

				if(wasSprinting) mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_SPRINTING));

				delay = 0;
			}
		}
	}
}
