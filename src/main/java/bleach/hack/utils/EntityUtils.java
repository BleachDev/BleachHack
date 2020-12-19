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
import net.minecraft.entity.mob.AmbientEntity;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class EntityUtils {

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static boolean isAnimal(Entity e) {
        return e instanceof PassiveEntity || e instanceof AmbientEntity || e instanceof WaterCreatureEntity || e instanceof GolemEntity;
    }

    public static void setGlowing(Entity entity, Formatting color, String teamName) {
        Team team = (mc.world.getScoreboard().getTeamNames().contains(teamName) ?
                mc.world.getScoreboard().getTeam(teamName) :
                mc.world.getScoreboard().addTeam(teamName));

        mc.world.getScoreboard().addPlayerToTeam(
                entity instanceof PlayerEntity ? entity.getEntityName() : entity.getUuidAsString(), team);
        mc.world.getScoreboard().getTeam(teamName).setColor(color);

        entity.setGlowing(true);
    }
    public static double[] calculateLookAt(double px, double py, double pz, PlayerEntity me)
    {
        double dirx = me.getX() - px;
        double diry = me.getY() - py;
        double dirz = me.getZ() - pz;

        double len = Math.sqrt(dirx * dirx + diry * diry + dirz * dirz);

        dirx /= len;
        diry /= len;
        dirz /= len;

        double pitch = Math.asin(diry);
        double yaw = Math.atan2(dirz, dirx);

        // to degree
        pitch = pitch * 180.0d / Math.PI;
        yaw = yaw * 180.0d / Math.PI;

        yaw += 90f;

        return new double[]
                { yaw, pitch };
    }

    public static float[] getLegitRotations(Vec3d vec)
    {
        Vec3d eyesPos = getEyesPos();

        double diffX = vec.x - eyesPos.x;
        double diffY = vec.y - eyesPos.y;
        double diffZ = vec.z - eyesPos.z;

        double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);

        float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90F;
        float pitch = (float) -Math.toDegrees(Math.atan2(diffY, diffXZ));

        return new float[]
                { MinecraftClient.getInstance().player.yaw + MathHelper.wrapDegrees(yaw - MinecraftClient.getInstance().player.yaw),
                        MinecraftClient.getInstance().player.pitch + MathHelper.wrapDegrees(pitch - MinecraftClient.getInstance().player.pitch) };
    }
    private static Vec3d getEyesPos()
    {
        return new Vec3d(MinecraftClient.getInstance().player.getX(), MinecraftClient.getInstance().player.getY() + MinecraftClient.getInstance().player.getEyeHeight(mc.player.getPose()), MinecraftClient.getInstance().player.getZ());
    }
    public enum FacingDirection
    {
        North,
        South,
        East,
        West,
    }

    public static FacingDirection GetFacing()
    {
        switch (MathHelper.floor((double) (mc.player.yaw * 8.0F / 360.0F) + 0.5D) & 7)
        {
            case 0:
            case 1:
                return FacingDirection.South;
            case 2:
            case 3:
                return FacingDirection.West;
            case 4:
            case 5:
                return FacingDirection.North;
            case 6:
            case 7:
                return FacingDirection.East;
            case 8:
        }
        return FacingDirection.North;
    }
}
