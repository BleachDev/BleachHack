package bleach.hack.utils;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import bleach.hack.mixin.MixinSharedConstants;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClientGame;
import net.minecraft.client.MinecraftClient;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.util.math.Box;
import net.minecraft.world.explosion.Explosion;

public class CrystalUtils
{
    public static boolean CanPlaceCrystalIfObbyWasAtPos(final BlockPos pos)
    {
        final MinecraftClient mc = MinecraftClient.getInstance();
        final Block floor = mc.world.getBlockState(pos.add(0, 1, 0)).getBlock();
        final Block ceil = mc.world.getBlockState(pos.add(0, 2, 0)).getBlock();

        if (floor == Blocks.AIR && ceil == Blocks.AIR)
        {
            if (mc.world.getOtherEntities(null, new Box(pos.add(0, 1, 0))).isEmpty())
            {
                return true;
            }
        }

        return false;
    }

    public static boolean canPlaceCrystal(final BlockPos pos)
    {
        final MinecraftClient mc = MinecraftClient.getInstance();

        final Block block = mc.world.getBlockState(pos).getBlock();

        if (block == Blocks.OBSIDIAN || block == Blocks.BEDROCK)
        {
            final Block floor = mc.world.getBlockState(pos.add(0, 1, 0)).getBlock();
            final Block ceil = mc.world.getBlockState(pos.add(0, 2, 0)).getBlock();

            if (floor == Blocks.AIR && ceil == Blocks.AIR)
            {
                if (mc.world.getOtherEntities(null, new Box(pos.add(0, 1, 0))).isEmpty())
                {
                    return true;
                }
            }
        }

        return false;
    }

    /// Returns a BlockPos object of player's position floored.
    public static BlockPos GetPlayerPosFloored(final PlayerEntity p_Player)
    {
        return new BlockPos(Math.floor(p_Player.getX()), Math.floor(p_Player.getY()), Math.floor(p_Player.getZ()));
    }

    public static Vec3d GetPlayerPos(final PlayerEntity p_Player)
    {
        return new Vec3d(p_Player.getX(), p_Player.getY(), p_Player.getZ());
    }

    public static List<BlockPos> findCrystalBlocks(final PlayerEntity p_Player, float p_Range)
    {
//        NonNullList<BlockPos> positions = NonNullList.create();
//        positions.addall(
//                getSphere(GetPlayerPosFloored(p_Player), p_Range, (int) p_Range, false, true, 0)
//                        .stream().filter(CrystalUtils::canPlaceCrystal).collect(Collectors.toList()));
        ArrayList<BlockPos> positions = new ArrayList<>();
        List<BlockPos> bps = getSphere(GetPlayerPosFloored(p_Player), p_Range, (int) p_Range, false, true, 0)
                .stream().filter(CrystalUtils::canPlaceCrystal).collect(Collectors.toList());
        for (BlockPos pos : bps) {
            if (pos != null) {
                positions.add(pos);
            }
        }
        return positions;
    }

    public static float calculateDamage(final World p_World, double posX, double posY, double posZ, Entity entity,
            int p_InterlopedAmount)
    {
        float doubleExplosionSize = 12.0F;

        double l_Distance = entity.squaredDistanceTo(posX, posY, posZ);
        
        if (l_Distance > doubleExplosionSize)
            return 0f;

        if (p_InterlopedAmount > 0)
        {
            Vec3d l_Interloped = EntityUtils.getInterpolatedAmount(entity, p_InterlopedAmount);
            l_Distance = EntityUtils.GetDistance(l_Interloped.x, l_Interloped.y, l_Interloped.z, posX, posY, posZ);
        }

        double distancedsize = l_Distance / (double) doubleExplosionSize;
        Vec3d vec3d = new Vec3d(posX, posY, posZ);
        double blockDensity = 0.5;
        double v = (1.0D - distancedsize) * blockDensity;
        float damage = (int) ((v * v + v) / 2.0D * 7.0D * doubleExplosionSize + 1.0D);
        double finald = 1.0D;
        /*
         * if (entity instanceof EntityLivingBase) finald =
         * getBlastReduction((EntityLivingBase) entity,getDamageMultiplied(damage));
         */
        if (entity instanceof LivingEntity)
        {
            finald = getBlastReduction((PlayerEntity) entity, getDamageMultiplied(p_World, damage),
                    new Explosion(p_World, null, posX, posY, posZ, 6F, false, Explosion.DestructionType.DESTROY));
        }
        return (float) finald;
    }

    public static float getBlastReduction(PlayerEntity entity, float damage, Explosion explosion)
    {
        if (entity instanceof PlayerEntity)
        {
            PlayerEntity ep = (PlayerEntity) entity;
            DamageSource ds = DamageSource.explosion(explosion);
            damage = EntityUtils.getDamageAfterAbsorb(damage, (float) ep.getArmor(),
                    (float) EntityAttributes.GENERIC_ARMOR_TOUGHNESS.getDefaultValue());

            int k = EnchantmentHelper.getProtectionAmount(ep.getArmorItems(), ds);
            float f = MathHelper.clamp(k, 0.0F, 20.0F);
            damage *= 1.0F - f / 25.0F;

            if (entity.hasStatusEffect(StatusEffect.byRawId(11)))
            {
                damage -= damage / 4;
            }
            // damage = Math.max(damage - ep.getAbsorptionAmount(), 0.0F);
            return damage;
        }

        damage = EntityUtils.getDamageAfterAbsorb(damage, (float) entity.getArmor(),
                (float) EntityAttributes.GENERIC_ARMOR_TOUGHNESS.getDefaultValue());
        return damage;
    }

    private static float getDamageMultiplied(final World p_World, float damage)
    {
        int diff = p_World.getDifficulty().getId();
        return damage * (diff == 0 ? 0 : (diff == 2 ? 1 : (diff == 1 ? 0.5f : 1.5f)));
    }

    public static float calculateDamage(final World p_World, EndCrystalEntity crystal, Entity entity)
    {
        return calculateDamage(p_World, crystal.getX(), crystal.getY(), crystal.getZ(), entity, 0);
    }

    public static List<BlockPos> getSphere(BlockPos loc, float r, int h, boolean hollow, boolean sphere, int plus_y)
    {
        List<BlockPos> circleblocks = new ArrayList<>();
        int cx = loc.getX();
        int cy = loc.getY();
        int cz = loc.getZ();
        for (int x = cx - (int) r; x <= cx + r; x++)
        {
            for (int z = cz - (int) r; z <= cz + r; z++)
            {
                for (int y = (sphere ? cy - (int) r : cy); y < (sphere ? cy + r : cy + h); y++)
                {
                    double dist = (cx - x) * (cx - x) + (cz - z) * (cz - z) + (sphere ? (cy - y) * (cy - y) : 0);
                    if (dist < r * r && !(hollow && dist < (r - 1) * (r - 1)))
                    {
                        circleblocks.add(new BlockPos(x, y + plus_y, z));
                    }
                }
            }
        }
        return circleblocks;
    }

}
