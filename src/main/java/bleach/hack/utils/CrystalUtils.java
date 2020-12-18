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
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.DamageUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.AmbientEntity;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.scoreboard.Team;
import net.minecraft.util.Formatting;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.explosion.Explosion;

public class CrystalUtils {

    private static final MinecraftClient mc = MinecraftClient.getInstance();
    public static double
    getCrystalDamage(LivingEntity entity, BlockPos blockPos)
    {
        if(mc.world.getDifficulty() == Difficulty.PEACEFUL || entity.isImmuneToExplosion())
            return 0;

        Vec3d vec3d = Vec3d.of(blockPos).add(0.5, 1, 0.5f);
        double distance = Math.sqrt(entity.squaredDistanceTo(vec3d));
        if(distance >= 13)
            return 0;

        double density = Explosion.getExposure(vec3d, entity);
        double impact = (1.d - (distance / 12.d)) * density;
        double damage = ((impact * impact + impact) / 2 * 7 * (6 * 2) + 1);

        Difficulty difficulty = mc.world.getDifficulty();
        if(difficulty == Difficulty.PEACEFUL)
            damage = 0.f;
        else if(difficulty == Difficulty.EASY)
            damage = Math.min(damage / 2.f + 1.f, damage);
        else if(difficulty == Difficulty.HARD)
            damage = damage * 3.f / 2.f;

        if(entity instanceof PlayerEntity)
        {
            PlayerEntity playerEntity = (PlayerEntity) entity;
            if(playerEntity.hasStatusEffect(StatusEffects.RESISTANCE))
            {
                damage *= (1 - ((playerEntity.getStatusEffect(StatusEffects.RESISTANCE).getAmplifier() + 1) * 0.2));
            }
            if(damage < 0)
                damage = 0;
        }

        damage = DamageUtil.getDamageLeft((float) damage, (float) entity.getArmor(), (float) entity.getAttributeInstance(EntityAttributes.GENERIC_ARMOR_TOUGHNESS).getValue());

        Explosion explosion = new Explosion(mc.world, null, vec3d.x, vec3d.y, vec3d.z, 6.f, false, Explosion.DestructionType.DESTROY);
        int protectionLevel = EnchantmentHelper.getProtectionAmount(entity.getArmorItems(), DamageSource.explosion(explosion));
        if(protectionLevel > 20)
            protectionLevel = 20;

        damage *= (1 - (protectionLevel / 25.d));
        if(damage < 0)
            damage = 0;

        return damage;

    }
    public static int
    changeHotbarSlotToItem(Item item)
    {
        int oldSlot = -1;
        int itemSlot = -1;
        for(int i = 0; i < 9; ++i)
        {
            if(mc.player.inventory.getStack(i).getItem() == item)
            {
                itemSlot = i;
                oldSlot = mc.player.inventory.selectedSlot;
            }
        }
        if(itemSlot != -1)
            mc.player.inventory.selectedSlot = itemSlot;
        return oldSlot;
    }
    public static void
    placeBlock(Vec3d vec, Hand hand, Direction direction)
    {
        mc.interactionManager.interactBlock(mc.player, mc.world, hand, new BlockHitResult(vec, direction, new BlockPos(vec), false));
        mc.player.swingHand(Hand.MAIN_HAND);
    }
}
