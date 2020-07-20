/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/bleachhack-1.14/).
 * Copyright (c) 2019 Bleach.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package bleach.hack.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.WaterCreatureEntity;
import net.minecraft.entity.mob.AmbientEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.packet.PlayerMoveC2SPacket;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;

public class EntityUtils {

	private static MinecraftClient mc = MinecraftClient.getInstance();

	public static boolean isAnimal(Entity e) {
		return e instanceof AnimalEntity || e instanceof AmbientEntity || e instanceof WaterCreatureEntity ||
				e instanceof GolemEntity || e instanceof VillagerEntity;
	}

	public static void setGlowing(Entity entity, Formatting color, String teamName) {
		Team team = mc.world.getScoreboard().getTeamNames().contains(teamName) ?
				mc.world.getScoreboard().getTeam(teamName) :
					mc.world.getScoreboard().addTeam(teamName);

				mc.world.getScoreboard().addPlayerToTeam(
						entity instanceof PlayerEntity ? entity.getEntityName() : entity.getUuidAsString(), team);
				mc.world.getScoreboard().getTeam(teamName).setColor(color);

				entity.setGlowing(true);
	}

	public static void facePos(double x, double y, double z) {
		double diffX = x - mc.player.x;
		double diffY = y - (mc.player.y + mc.player.getEyeHeight(mc.player.getPose()));
		double diffZ = z - mc.player.z;

		double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

		float yaw = (float)Math.toDegrees(Math.atan2(diffZ, diffX)) - 90F;
		float pitch = (float)-Math.toDegrees(Math.atan2(diffY, diffXZ));

		mc.player.yaw += MathHelper.wrapDegrees(yaw - mc.player.yaw);
		mc.player.pitch += MathHelper.wrapDegrees(pitch - mc.player.pitch);
	}

	public static void facePosPacket(double x, double y, double z) {
		double diffX = x - mc.player.x;
		double diffY = y - (mc.player.y + mc.player.getEyeHeight(mc.player.getPose()));
		double diffZ = z - mc.player.z;

		double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

		float yaw = (float)Math.toDegrees(Math.atan2(diffZ, diffX)) - 90F;
		float pitch = (float)-Math.toDegrees(Math.atan2(diffY, diffXZ));

		mc.player.networkHandler.sendPacket(
				new PlayerMoveC2SPacket.LookOnly(
						mc.player.yaw + MathHelper.wrapDegrees(yaw - mc.player.yaw),
						mc.player.pitch + MathHelper.wrapDegrees(pitch - mc.player.pitch), mc.player.onGround));
	}
}
