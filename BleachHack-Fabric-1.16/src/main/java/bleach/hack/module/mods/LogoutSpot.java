package bleach.hack.module.mods;

import bleach.hack.event.events.EventEntityRender;
import bleach.hack.event.events.EventOpenScreen;
import bleach.hack.event.events.EventReadPacket;
import bleach.hack.event.events.EventTick;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.util.BleachQueue;
import bleach.hack.util.render.WorldRenderUtils;
import bleach.hack.util.world.PlayerCopyEntity;
import com.google.common.eventbus.Subscribe;

import net.minecraft.client.gui.screen.DisconnectedScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.util.math.Vec3d;

import org.apache.commons.lang3.tuple.Pair;
import java.util.*;

/**
 * @author <a href="https://github.com/lasnikprogram">Lasnik</a>
 */

public class LogoutSpot extends Module {

	private final HashMap<UUID, Pair<PlayerCopyEntity, Long>> players = new HashMap<>();

	public LogoutSpot() {
		super("LogoutSpot", KEY_UNBOUND, Category.WORLD, "Shows where a player logged out",
				new SettingToggle("Remove", true).withDesc("Removes logout spots").withChildren( // 1
						new SettingToggle("Distance", false).withDesc("Remove logout spots based on distance").withChildren( // 0-0
								new SettingSlider("Radius", 1, 1000, 200, 0).withDesc("Radius in which logout spots get shown")), // 0-0-0
						new SettingToggle("Time", false).withDesc("Remove logout spots based on time since logout").withChildren( // 0-1
								new SettingSlider("Duration", 1, 1800, 120, 0).withDesc("Duration after which a logged out players gets removed (in seconds)")), // 0-1-0
						new SettingToggle("Disconnect", true).withDesc("Removes all logout spots when disconnecting"), // 0-2
						new SettingToggle("Disable", true).withDesc("Removes all logout spots when disabling LogoutSpot")), // 0-3
				new SettingToggle("Text", true).withDesc("Adds text next to players").withChildren( // 1
						new SettingToggle("Name", true).withDesc("Shows the name of the logged player"), // 1-0
						new SettingToggle("Coords", false).withDesc("Shows the coords of the logged player"), // 1-1
						new SettingToggle("Health", true).withDesc("Shows the health of the logged player"), // 1-2
						new SettingToggle("Time", true).withDesc("Shows the time ago the player logged"))); // 1-3
	}

	@Override
	public void onDisable() {
		if (mc.world != null) {
			players.values().forEach(e -> e.getKey().despawn());
		}

		if (getSetting(0).asToggle().state && getSetting(0).asToggle().getChild(3).asToggle().state) {
			players.clear();
		}

		super.onDisable();
	}

	@Override
	public void onEnable() {
		super.onEnable();

		if (mc.world != null) {
			players.values().forEach(e -> e.getKey().spawn());
		}
	}

	@Subscribe
	public void onReadPacket(EventReadPacket event) {
		if (!(event.getPacket() instanceof PlayerListS2CPacket) || mc.world == null) {
			return;
		}

		PlayerListS2CPacket list = (PlayerListS2CPacket) event.getPacket();

		// Spawns fake player when player leaves
		if (list.getAction() == PlayerListS2CPacket.Action.REMOVE_PLAYER) {
			for (PlayerListS2CPacket.Entry entry : list.getEntries()) {
				PlayerEntity player = mc.world.getPlayerByUuid(entry.getProfile().getId());

				if (player != null && !mc.player.equals(player)) {
					Pair<PlayerCopyEntity, Long> fakePlayer = Pair.of(spawnDummy(player), System.currentTimeMillis());
					BleachQueue.add("logoutspot", () -> players.put(player.getUuid(), fakePlayer));
				}
			}
		}

		// Removes fake player when player joins
		if (list.getAction().equals(PlayerListS2CPacket.Action.ADD_PLAYER)) {
			for (PlayerListS2CPacket.Entry entry : list.getEntries()) {
				Pair<PlayerCopyEntity, Long> fakePlayer = players.remove(entry.getProfile().getId());

				if (fakePlayer != null && mc.world != null) {
					BleachQueue.add("logoutspot", () -> fakePlayer.getLeft().despawn());
				}
			}
		}
	}

	// Removes the fake players based on settings
	@Subscribe
	public void onTick(EventTick event) {
		if (getSetting(0).asToggle().state && getSetting(0).asToggle().getChild(0).asToggle().state) {
			players.keySet().removeIf(player -> {
				if (mc.player.distanceTo(players.get(player).getKey())
						> getSetting(0).asToggle().getChild(0).asToggle().getChild(0).asSlider().getValue()) {
					players.get(player).getKey().despawn();
					return true;
				}

				return false;
			});
		}

		if (getSetting(0).asToggle().state && getSetting(0).asToggle().getChild(1).asToggle().state) {
			players.keySet().removeIf(player -> {
				if ((System.currentTimeMillis() - players.get(player).getValue()) / 1000L
						> getSetting(0).asToggle().getChild(1).asToggle().getChild(0).asSlider().getValueLong()) {
					players.get(player).getKey().despawn();
					return true;
				}

				return false;
			});
		}
	}

	@Subscribe
	public void onPostEntityRender(EventEntityRender.PostAll event) {
		for (Pair<PlayerCopyEntity, Long> playerPair: players.values()) {
			if (getSetting(1).asToggle().state) {
				PlayerCopyEntity player = playerPair.getLeft();

				Vec3d rVec = new Vec3d(player.lastRenderX + (player.getX() - player.lastRenderX) * mc.getTickDelta(),
						player.lastRenderY + (player.getY() - player.lastRenderY) * mc.getTickDelta() + player.getHeight(),
						player.lastRenderZ + (player.getZ() - player.lastRenderZ) * mc.getTickDelta());

				Vec3d offset = new Vec3d(0, 0, 0.45 + mc.textRenderer.getWidth(player.getDisplayName().getString()) / 90d)
						.rotateY((float) -Math.toRadians(mc.player.yaw + 90));

				List<String> lines = new ArrayList<>();
				lines.add("\u00a74Logout:");

				if (getSetting(1).asToggle().getChild(0).asToggle().state)
					lines.add("\u00a7c" + player.getDisplayName().getString());

				if (getSetting(1).asToggle().getChild(1).asToggle().state)
					lines.add("\u00a7c" + (int) player.getX() + " " + (int) player.getY() + " " + (int) player.getZ());
				
				if (getSetting(1).asToggle().getChild(2).asToggle().state)
					lines.add("\u00a7c" + (int) Math.ceil(player.getHealth() + player.getAbsorptionAmount()) + "hp");

				if (getSetting(1).asToggle().getChild(3).asToggle().state)
					lines.add("\u00a7c" + getTimeElapsed(playerPair.getRight()));

				for (int i = 0; i < lines.size(); i++) {
					WorldRenderUtils.drawText(lines.get(i), rVec.x + offset.x, rVec.y + 0.1 - i * 0.25, rVec.z + offset.z, 1f);
				}
			}
		}
	}

	@Subscribe
	public void onOpenScreen(EventOpenScreen event) {
		if (getSetting(0).asToggle().state && getSetting(0).asToggle().getChild(2).asToggle().state
				&& event.getScreen() instanceof DisconnectedScreen) {
			players.clear();
		}
	}

	private PlayerCopyEntity spawnDummy(PlayerEntity player) {
		PlayerCopyEntity dummy = new PlayerCopyEntity(player);
		dummy.spawn();

		return dummy;
	}

	private String getTimeElapsed(long time) {
		long timeDiff = (System.currentTimeMillis() - time) / 1000L;

		if (timeDiff < 60L) {
			return String.format("%ds", timeDiff);
		}

		if (timeDiff < 3600L) {
			return String.format("%dm %ds", timeDiff / 60L, timeDiff % 60L);
		}

		if (timeDiff < 86400L) {
			return String.format("%dh %dm", timeDiff / 3600L, timeDiff / 60L % 60L);
		}

		return String.format("%dd %dh", timeDiff / 86400L, timeDiff / 3600L % 24L);
	}
}
