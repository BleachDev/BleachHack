package bleach.hack.module.mods;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.tuple.Triple;

import com.google.common.eventbus.Subscribe;

import bleach.hack.event.events.EventTick;
import bleach.hack.event.events.EventWorldRender;
import bleach.hack.gui.clickgui.SettingColor;
import bleach.hack.gui.clickgui.SettingMode;
import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.gui.clickgui.SettingToggle;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.utils.RenderUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.entity.projectile.thrown.EggEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.entity.projectile.thrown.ThrownEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.EggItem;
import net.minecraft.item.EnderPearlItem;
import net.minecraft.item.ExperienceBottleItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.RangedWeaponItem;
import net.minecraft.item.SnowballItem;
import net.minecraft.item.ThrowablePotionItem;
import net.minecraft.item.TridentItem;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RayTraceContext;

public class Trajectories extends Module {

	private List<Triple<List<Vec3d>, Entity, BlockPos>> poses = new ArrayList<>();

	public Trajectories() {
		super("Trajectories", KEY_UNBOUND, Category.RENDER, "Shows the trajectories of projectiles",
				new SettingMode("Draw: ", "Line", "Dots").withDesc("How to draw the line where the projectile is going"),
				new SettingToggle("Throwables", true).withDesc("Shows snowballs/eggs/epearls"),
				new SettingToggle("XP Bottles", true).withDesc("Shows XP bottles"),
				new SettingToggle("Flying", true).withDesc("Shows trajectories for flying projectiles").withChildren(
						new SettingToggle("Throwables", true).withDesc("Shows snowballs/eggs/epearls"),
						new SettingToggle("XP Bottles", true).withDesc("Shows XP bottles")),
				new SettingToggle("Other Players", false).withDesc("Show other players trajectories"),
				new SettingColor("Color", 1f, 0.3f, 1f, false),
				new SettingSlider("Thick: ", 0.1, 5, 2, 2));
	}

	@Subscribe
	public void onTick(EventTick event) {
		poses.clear();

		Entity entity = summonProjectile(mc.player);

		if (entity != null) {
			poses.add(simulate(entity));
		}

		if (getSetting(2).asToggle().state) {
			for (Entity e: mc.world.getEntities()) {
				if (e instanceof ProjectileEntity) {
					if (!getSetting(3).asToggle().getChild(0).asToggle().state
							&& (e instanceof SnowballEntity || e instanceof EggEntity || e instanceof EnderPearlEntity)) {
						continue;
					}

					if (!getSetting(3).asToggle().getChild(1).asToggle().state && e instanceof ExperienceBottleEntity) {
						continue;
					}

					Triple<List<Vec3d>, Entity, BlockPos> p = simulate(e);

					if (p.getLeft().size() >= 2) poses.add(p);
				}
			}
		}
		
		if (getSetting(4).asToggle().state) {
			for (PlayerEntity e: mc.world.getPlayers()) {
				if (e == mc.player) continue;
				Entity proj = summonProjectile(e);

				if (proj != null) {
					poses.add(simulate(proj));
				}
			}
			
		}
	}

	@Subscribe
	public void onWorldRender(EventWorldRender event) {
		float[] col = getSetting(5).asColor().getRGBFloat();

		for (Triple<List<Vec3d>, Entity, BlockPos> t: poses) {
			if (t.getLeft().size() >= 2) {
				if (getSetting(0).asMode().mode == 0) {
					for (int i = 1; i < t.getLeft().size(); i++) {
						RenderUtils.drawLine(t.getLeft().get(i - 1).x, t.getLeft().get(i - 1).y, t.getLeft().get(i - 1).z,
								t.getLeft().get(i).x, t.getLeft().get(i).y, t.getLeft().get(i).z, col[0], col[1], col[2],
								(float) getSetting(6).asSlider().getValue());
					} 
				} else {
					for (Vec3d v: t.getLeft()) {
						RenderUtils.drawFilledBox(new Box(v.x - 0.1, v.y - 0.1, v.z - 0.1, v.x + 0.1, v.y + 0.1, v.z + 0.1),
								col[0], col[1], col[2], 0.75f);
					}
				}

				if (t.getMiddle() != null) {
					RenderUtils.drawFilledBox(t.getMiddle().getBoundingBox(), col[0], col[1], col[2], 0.75f);
				}

				if (t.getRight() != null) {
					RenderUtils.drawFilledBox(t.getRight(), col[0], col[1], col[2], 0.75f);
				}
			}
		}
	}

	private Entity summonProjectile(PlayerEntity thrower) {
		ItemStack hand = isThrowable(thrower.inventory.getMainHandStack().getItem())
				? thrower.inventory.getMainHandStack() : isThrowable(thrower.inventory.offHand.get(0).getItem())
						? thrower.inventory.offHand.get(0) : null;

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

	private boolean isThrowable(Item item) {
		return item instanceof RangedWeaponItem
				|| (getSetting(1).asToggle().state && (item instanceof EggItem || item instanceof SnowballItem || item instanceof EnderPearlItem))
				|| (getSetting(2).asToggle().state && item instanceof ExperienceBottleItem)
				|| item instanceof ThrowablePotionItem || item instanceof TridentItem;
	}

	private void initProjectile(Entity e, Entity thrower, float addPitch, float strength) {
		float velX = -MathHelper.sin(thrower.yaw * 0.017453292F) * MathHelper.cos(thrower.pitch * 0.017453292F);
		float velY = -MathHelper.sin((thrower.pitch + addPitch) * 0.017453292F);
		float velZ = MathHelper.cos(thrower.yaw * 0.017453292F) * MathHelper.cos(thrower.pitch * 0.017453292F);

		Vec3d velVec = new Vec3d((double) velX, (double) velY, (double) velZ).normalize().multiply((double) strength);
		e.setVelocity(velVec);
		float float_3 = MathHelper.sqrt(Entity.squaredHorizontalLength(velVec));
		e.yaw = (float) (MathHelper.atan2(velVec.x, velVec.z) * 57.2957763671875D);
		e.pitch = (float) (MathHelper.atan2(velVec.y, (double) float_3) * 57.2957763671875D);
		e.prevYaw = e.yaw;
		e.prevPitch = e.pitch;

		e.setVelocity(velVec.add(thrower.getVelocity().x, thrower.isOnGround() ? 0.0D : thrower.getVelocity().y, thrower.getVelocity().z));
	}

	private Triple<List<Vec3d>, Entity, BlockPos> simulate(Entity e) {
		List<Vec3d> vecs = new ArrayList<>();

		Entity spoofE = new SnowballEntity(mc.world, mc.player);
		spoofE.copyPositionAndRotation(e);
		spoofE.setVelocity(e.getVelocity());
		for (int i = 0; i < 100; i++) {
			Vec3d vel = spoofE.getVelocity();
			Vec3d newVec = spoofE.getPos().add(vel);
			//EntityHitResult entityHit = ProjectileUtil.rayTrace(mc.player, e.getPos(), newVec, e.getBoundingBox(), null, 1f);
			List<Entity> entities = mc.world.getOtherEntities(null, spoofE.getBoundingBox().expand(0.15));
			entities.removeAll(Arrays.asList(mc.player, e, spoofE));
			if (!entities.isEmpty()) {
				return Triple.of(vecs, entities.get(0), null);
			}

			BlockHitResult blockHit = mc.world.rayTrace(
					new RayTraceContext(spoofE.getPos(), newVec, RayTraceContext.ShapeType.COLLIDER, RayTraceContext.FluidHandling.NONE, e));
			if (blockHit.getType() != HitResult.Type.MISS) {
				return Triple.of(vecs, null, blockHit.getBlockPos());
			}

			spoofE.pitch = MathHelper.lerp(0.2F, spoofE.prevPitch, spoofE.pitch);
			spoofE.yaw = MathHelper.lerp(0.2F, spoofE.prevYaw, spoofE.yaw);

			double gravity = e instanceof PotionEntity ? 0.05
					: e instanceof ExperienceBottleEntity ? 0.07 : e instanceof ThrownEntity ? 0.03 : 0.05000000074505806;
			spoofE.setVelocity(vel.x * 0.99, vel.y * 0.99 - gravity, vel.z * 0.99);
			spoofE.updatePosition(spoofE.getPos().x + spoofE.getVelocity().x,
					spoofE.getPos().y + spoofE.getVelocity().y, spoofE.getPos().z + spoofE.getVelocity().z);

			vecs.add(spoofE.getPos());
		}

		return Triple.of(vecs, null, null);
	}

}
