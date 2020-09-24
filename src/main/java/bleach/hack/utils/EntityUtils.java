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
import net.minecraft.item.FoodComponent;
import net.minecraft.item.Items;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
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
    public enum FacingDirection
    {
        North,
        South,
        East,
        West,
        SouthEast,
        SouthWest,
        NorthWest,
        NorthEast,
    }

    public static FacingDirection GetFacing()
    {
        switch (MathHelper.floor((double) (mc.player.yaw * 8.0F / 360.0F) + 0.5D) & 7)
        {
            case 0:
            case 1:
                return FacingDirection.South;
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

    public static Vec3d getInterpolatedAmount(Entity entity, double ticks)
    {
        return getInterpolatedAmount(entity, ticks);
    }

    public static double GetDistance(double p_X, double p_Y, double p_Z, double x, double y, double z)
    {
        double d0 = p_X - x;
        double d1 = p_Y - y;
        double d2 = p_Z - z;
        return (double)MathHelper.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
    }

    public static float getDamageAfterAbsorb(float damage, float totalArmor, float toughnessAttribute) {
        float f = 2.0F + toughnessAttribute / 4.0F;
        float f1 = MathHelper.clamp(totalArmor - damage / f, totalArmor * 0.2F, 20.0F);
        return damage * (1.0F - f1 / 25.0F);
    }

    public static boolean IsEating()
    {
        return mc.player != null &&  mc.player.getActiveItem().getItem() == Items.GOLDEN_APPLE;
    }

    public static float GetRotationYawForCalc()
    {
        float rotationYaw = mc.player.yaw;
        if (mc.player.forwardSpeed < 0.0f)
        {
            rotationYaw += 180.0f;
        }
        float n = 1.0f;
        if (mc.player.forwardSpeed < 0.0f)
        {
            n = -0.5f;
        }
        else if (mc.player.forwardSpeed > 0.0f)
        {
            n = 0.5f;
        }
        if (mc.player.sidewaysSpeed > 0.0f)
        {
            rotationYaw -= 90.0f * n;
        }
        if (mc.player.sidewaysSpeed < 0.0f)
        {
            rotationYaw += 90.0f * n;
        }
        return rotationYaw * 0.017453292f;
    }

}
