package bleach.hack.module.mods;

import java.util.stream.Stream;

import com.google.common.eventbus.Subscribe;

import bleach.hack.event.events.EventTick;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import net.minecraft.util.math.Box;
import net.minecraft.util.shape.VoxelShape;

// credit: https://github.com/Wurst-Imperium/Wurst7/blob/21w11a/src/main/java/net/wurstclient/hacks/ParkourHack.java
// modified by https://github.com/lasnikprogram

public class AutoParkour extends Module {
	public AutoParkour() {
		super("Auto Parkour", KEY_UNBOUND, Category.MOVEMENT,
				"You jump when you reach a blocks edge (Use this if you are suck at parkouring)");
	}

	@Subscribe
	public void onTick(EventTick event) {
		if (mc.player.isSneaking() || !mc.player.isOnGround())
			return;

		Box box = mc.player.getBoundingBox().offset(0, -0.5, 0);

		Stream<VoxelShape> blockCollisions = mc.world.getBlockCollisions(mc.player, box);

		if (!blockCollisions.findAny().isPresent())
			mc.player.jump();
	}
}
