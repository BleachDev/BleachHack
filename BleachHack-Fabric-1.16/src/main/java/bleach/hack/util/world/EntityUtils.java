/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
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
package bleach.hack.util.world;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.AmbientEntity;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.Formatting;

public class EntityUtils {

	private static MinecraftClient mc = MinecraftClient.getInstance();

	public static boolean isAnimal(Entity e) {
		return e instanceof PassiveEntity || e instanceof AmbientEntity || e instanceof WaterCreatureEntity || e instanceof GolemEntity;
	}

	public static void setGlowing(Entity entity, Formatting color, String teamName) {
		Team team = (mc.world.getScoreboard().getTeamNames().contains(teamName) ? mc.world.getScoreboard().getTeam(teamName)
				: mc.world.getScoreboard().addTeam(teamName));

		mc.world.getScoreboard().addPlayerToTeam(
				entity instanceof PlayerEntity ? entity.getEntityName() : entity.getUuidAsString(), team);
		mc.world.getScoreboard().getTeam(teamName).setColor(color);

		entity.setGlowing(true);
	}
}
