package bleach.hack.utils;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.entity.projectile.thrown.ThrownEntity;
import net.minecraft.item.*;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import org.apache.commons.lang3.tuple.Triple;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProjectileSimulator {

    private static final MinecraftClient mc = MinecraftClient.getInstance();

    public static Entity summonProjectile(PlayerEntity thrower, boolean allowThrowables, boolean allowXp, boolean allowPotions) {
        ItemStack hand = (isThrowable(thrower.inventory.getMainHandStack().getItem(), allowThrowables, allowXp, allowPotions)
                ? thrower.inventory.getMainHandStack() : isThrowable(thrower.inventory.offHand.get(0).getItem(), allowThrowables, allowXp, allowPotions)
                ? thrower.inventory.offHand.get(0) : null);

        if (hand == null) return null;

        Vec3d spawnVec = new Vec3d(
                thrower.getX() - Math.cos(Math.toRadians(thrower.yaw)) * 0.05,
                thrower.getEyeY() - 0.1000000015,
                thrower.getZ() - Math.sin(Math.toRadians(thrower.yaw)) * 0.05);

        if (hand.getItem() instanceof RangedWeaponItem) {
            float charged = hand.getItem() == Items.CROSSBOW && CrossbowItem.isCharged(hand) ? 1f
                    : hand.getItem() == Items.CROSSBOW ? 0f : BowItem.getPullProgress(thrower.getItemUseTime());

            if (charged > 0f) {
                Entity e = new ArrowEntity(mc.world, spawnVec.x, spawnVec.y, spawnVec.z);
                initProjectile(e, thrower, 0f, charged * 3);
                return e;
            }
        } else if (hand.getItem() instanceof SnowballItem || hand.getItem() instanceof EggItem || hand.getItem() instanceof EnderPearlItem) {
            Entity e = new SnowballEntity(mc.world, spawnVec.x, spawnVec.y, spawnVec.z);
            initProjectile(e, thrower, 0f, 1.5f);
            return e;
        } else if (hand.getItem() instanceof ExperienceBottleItem) {
            Entity e = new ExperienceBottleEntity(mc.world, spawnVec.x, spawnVec.y, spawnVec.z);
            initProjectile(e, thrower, -20f, 0.7f);
            return e;
        } else if (hand.getItem() instanceof ThrowablePotionItem) {
            Entity e = new PotionEntity(mc.world, spawnVec.x, spawnVec.y, spawnVec.z);
            initProjectile(e, thrower, -20f, 0.5f);
            return e;
        } else if (hand.getItem() instanceof TridentItem) {
            Entity e = new TridentEntity(mc.world, spawnVec.x, spawnVec.y, spawnVec.z);
            initProjectile(e, thrower, 0f, 2.5f);
            return e;
        }

        return null;
    }

    public static boolean isThrowable(Item item) {
        return isThrowable(item, true, true, true);
    }

    public static boolean isThrowable(Item item, boolean allowThrowables, boolean allowXp, boolean allowPotions) {
        return item instanceof RangedWeaponItem
                || (allowThrowables && (item instanceof EggItem || item instanceof SnowballItem || item instanceof EnderPearlItem))
                || (allowXp && item instanceof ExperienceBottleItem)
                || (allowPotions && item instanceof ThrowablePotionItem) || item instanceof TridentItem;
    }

    private static void initProjectile(Entity e, Entity thrower, float addPitch, float strength) {
        float velX = -MathHelper.sin(thrower.yaw * 0.017453292F) * MathHelper.cos(thrower.pitch * 0.017453292F);
        float velY = -MathHelper.sin((thrower.pitch + addPitch) * 0.017453292F);
        float velZ = MathHelper.cos(thrower.yaw * 0.017453292F) * MathHelper.cos(thrower.pitch * 0.017453292F);

        Vec3d velVec = new Vec3d(velX, velY, velZ).normalize().multiply(strength);
        e.setVelocity(velVec);
        float float_3 = MathHelper.sqrt(Entity.squaredHorizontalLength(velVec));
        e.yaw = (float) (MathHelper.atan2(velVec.x, velVec.z) * 57.2957763671875D);
        e.pitch = (float) (MathHelper.atan2(velVec.y, float_3) * 57.2957763671875D);
        e.prevYaw = e.yaw;
        e.prevPitch = e.pitch;

        e.setVelocity(velVec.add(thrower.getVelocity().x, thrower.isOnGround() ? 0.0D : thrower.getVelocity().y, thrower.getVelocity().z));
    }

    public static Triple<List<Vec3d>, Entity, BlockPos> simulate(Entity e) {
        List<Vec3d> vecs = new ArrayList<>();

        SimulatedProjectile spoofE = new SimulatedProjectile(e);
        for (int i = 0; i < 100; i++) {
            Vec3d vel = spoofE.velocity;
            Vec3d newVec = spoofE.getPos().add(vel);
            //EntityHitResult entityHit = ProjectileUtil.raycast(mc.player, e.getPos(), newVec, e.getBoundingBox(), null, 1f);
            List<Entity> entities = mc.world.getOtherEntities(null, spoofE.getBoundingBox().expand(0.15));
            entities.removeAll(Arrays.asList(mc.player, e, spoofE));
            if (!entities.isEmpty()) {
                return Triple.of(vecs, entities.get(0), null);
            }

            BlockHitResult blockHit = mc.world.raycast(
                    new RaycastContext(spoofE.getPos(), newVec, RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, e));
            if (blockHit.getType() != HitResult.Type.MISS) {
                return Triple.of(vecs, null, blockHit.getBlockPos());
            }

            float prevPitch = spoofE.pitch;
            spoofE.pitch = MathHelper.lerp(0.2F, spoofE.prevPitch, spoofE.pitch);
            spoofE.prevPitch = prevPitch;

            double gravity = e instanceof PotionEntity ? 0.05
                    : e instanceof ExperienceBottleEntity ? 0.07 : e instanceof ThrownEntity ? 0.03 : 0.05000000074505806;
            spoofE.velocity = new Vec3d(vel.x * 0.99, vel.y * 0.99 - gravity, vel.z * 0.99);
            spoofE.setPos(spoofE.getPos().x + spoofE.velocity.x,
                    spoofE.getPos().y + spoofE.velocity.y, spoofE.getPos().z + spoofE.velocity.z);

            vecs.add(spoofE.getPos());
        }

        return Triple.of(vecs, null, null);
    }

    /**
     * Lightweight projectile entity without having to create an entirely new entity
     **/
    private static class SimulatedProjectile {

        public double x;
        public double y;
        public double z;

        public float pitch;
        public float prevPitch = 0f;

        public Vec3d velocity;

        private final float width;
        private final float height;

        public SimulatedProjectile(Entity realProjectile) {
            x = realProjectile.getX();
            y = realProjectile.getY();
            z = realProjectile.getZ();

            pitch = realProjectile.pitch;

            velocity = realProjectile.getVelocity();

            width = realProjectile.getWidth();
            height = realProjectile.getHeight();
        }

        public Vec3d getPos() {
            return new Vec3d(x, y, z);
        }

        public void setPos(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
        }

        public Box getBoundingBox() {
            return new Box(x - width / 2, y - height / 2, z - width / 2, x + width / 2, y + height / 2, z + width / 2);
        }
    }
}
