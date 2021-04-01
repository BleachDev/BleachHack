package bleach.hack.module.mods;

import java.util.stream.Stream;

import com.google.common.eventbus.Subscribe;

import bleach.hack.event.events.EventClientMove;
import bleach.hack.event.events.EventTick;
import bleach.hack.event.events.EventWorldRender;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingColor;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.util.RenderUtils;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket.Mode;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;

// credit: https://github.com/Wurst-Imperium/Wurst7/blob/21w11a/src/main/java/net/wurstclient/hacks/ParkourHack.java
// modified by https://github.com/lasnikprogram
// modified by https://github.com/BleachDrinker420

public class AutoParkour extends Module {

	private BlockPos smartPos = null;

	public AutoParkour() {
		super("AutoParkour", KEY_UNBOUND, Category.MOVEMENT, "You jump when you reach the edge of a block (Use this if you suck at parkouring)",
				new SettingToggle("AutoSprint", true).withDesc("Automatically makes you sprint when jumping"),
				new SettingToggle("Smart", true).withDesc("Tries to figure out what block you're jumping to then auto jumps to that block").withChildren(
						new SettingToggle("Snap", false).withDesc("Snaps you to the target block to prevent you from overshooting it"),
						new SettingToggle("Highlight", false).withDesc("Highlights the target block you're jumping to").withChildren(
								new SettingColor("Color", 0.85f, 0.44f, 0.57f, false).withDesc("The highlight color"))));
	}

	@Override
	public void onDisable() {
		smartPos = null;

		super.onDisable();
	}

	@Subscribe
	public void onTick(EventTick event) {
		if (smartPos != null) {
			if (mc.player.getY() - 0.5 < smartPos.getY() && mc.player.getVelocity().y < 0) {
				smartPos = null;
			}
		}

		if (!mc.player.isSneaking() && mc.player.isOnGround()) {
			smartPos = null;

			Box box = mc.player.getBoundingBox().offset(0, -0.51, 0);
			Stream<VoxelShape> blockCollisions = mc.world.getBlockCollisions(mc.player, box);

			if (!blockCollisions.findAny().isPresent()) {
				if (getSetting(0).asToggle().state && !mc.player.isSprinting()) {
					mc.player.setSprinting(true);
					mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, Mode.START_SPRINTING));
				}

				if (getSetting(1).asToggle().state) {
					Vec3d lookVec = mc.player.getPos().add(new Vec3d(0, 0, 3.5).rotateY(-(float) Math.toRadians(mc.player.yaw)));

					BlockPos nearestPos = null;
					for (BlockPos pos: BlockPos.iterateOutwards(mc.player.getBlockPos().down(), 4, 1, 4)) {
						if (mc.world.isTopSolid(pos, mc.player)
								&& mc.player.getPos().distanceTo(Vec3d.of(pos).add(0.5, 1, 0.5)) >= 1
								&& mc.player.getPos().distanceTo(Vec3d.of(pos).add(0.5, 1, 0.5)) <= 4.5 /* ? */
								&& !mc.world.getBlockCollisions(mc.player, new Box(pos.up(), pos.add(1, 2, 1))).findAny().isPresent()
								&& (nearestPos == null || pos.getSquaredDistance(lookVec, false) < nearestPos.getSquaredDistance(lookVec, false))) {
							nearestPos = pos.toImmutable();
						}
					}

					if (nearestPos != null) {
						smartPos = nearestPos;
					}
				}

				mc.player.jump();
			}
		}
	}
	
	@Subscribe
	public void onWorldRender(EventWorldRender event) {
		if (smartPos != null && getSetting(1).asToggle().getChild(1).asToggle().state) {
			float[] rgb = getSetting(1).asToggle().getChild(1).asToggle().getChild(0).asColor().getRGBFloat();
			RenderUtils.drawFilledBox(smartPos, rgb[0], rgb[1], rgb[2], 0.5f);
		}
	}

	@Subscribe
	public void onClientMove(EventClientMove event) {
		if (smartPos != null && getSetting(1).asToggle().state) {
			if (!getSetting(1).asToggle().getChild(0).asToggle().state
					&& (mc.player.getBoundingBox().maxX < smartPos.getX()
						|| mc.player.getBoundingBox().minX > smartPos.getX() + 1
						|| mc.player.getBoundingBox().maxZ < smartPos.getZ()
						|| mc.player.getBoundingBox().minZ > smartPos.getZ() + 1)) {
				return;
			}

			// maffs
			double targetDiffX = (smartPos.getX() + 0.5) - mc.player.getX();
			double targetDiffZ = (smartPos.getZ() + 0.5) - mc.player.getZ();

			float targetYaw = (float) Math.toDegrees(Math.atan2(targetDiffZ, targetDiffX)) - 90F;

			double currentDiffX = (mc.player.getX() + event.vec3d.x) - mc.player.getX();
			double currentDiffZ = (mc.player.getZ() + event.vec3d.z) - mc.player.getZ();

			float currentYaw = (float) Math.toDegrees(Math.atan2(currentDiffZ, currentDiffX)) - 90F;

			event.vec3d = event.vec3d.rotateY(-(float) Math.toRadians(targetYaw - currentYaw));
		}
	}
}
