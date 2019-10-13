package bleach.hack.module.mods;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import bleach.hack.event.events.EventTick;

import com.google.common.collect.Streams;
import com.google.common.eventbus.Subscribe;
import com.sun.javafx.property.adapter.PropertyDescriptor;
import net.minecraft.network.Packet;
import net.minecraft.server.network.packet.PlayerActionC2SPacket;
import net.minecraft.server.network.packet.PlayerInteractBlockC2SPacket;
import net.minecraft.util.math.*;
import org.lwjgl.glfw.GLFW;

import bleach.hack.gui.clickgui.SettingBase;
import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.gui.clickgui.SettingToggle;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.utils.EntityUtils;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.decoration.EnderCrystalEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolItem;
import net.minecraft.server.network.packet.PlayerInteractEntityC2SPacket;
import net.minecraft.util.DefaultedList;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.world.RayTraceContext;
import net.minecraft.world.RayTraceContext.FluidHandling;
import net.minecraft.world.RayTraceContext.ShapeType;
import net.minecraft.world.explosion.Explosion;
import org.lwjgl.opengl.GL11;

public class KamiSkid extends Module {

	private static List<SettingBase> settings = Arrays.asList(
			new SettingToggle(false, "Aimbot"), //0
			new SettingToggle(false, "Thru Walls"), //1
			new SettingToggle(false, "Switch Aura"), //2
			new SettingToggle(true, "Players"), //3
			new SettingToggle(true, "Mobs"), //4
			new SettingToggle(true, "Animals"), //5
			new SettingToggle(false, "Place"), //6
			new SettingToggle(false, "Explode"), //7
			new SettingToggle(false, "Anti Weakness"), //8
			new SettingSlider(0, 6, 4.25, 2, "Range: "), //9
			new SettingSlider(0, 20, 16, 0, "CPS: ")); //10

	private BlockPos render;
	private Entity renderEnt;
	private int oldSlot = -1;
	private int newSlot;
	private long systemTime = -1;
	private boolean switchCooldown = false;
	private boolean isAttacking = false;
	private static boolean togglePitch = false;
	public KamiSkid() {
		super("KamiSkid", GLFW.GLFW_KEY_I, Category.COMBAT, "ftuy9h4qt78wEBNHY67TR&/tw6NTA 6GYg67gew67RG67SRT", settings);
	}

	@Subscribe
	public void onTick(EventTick event) {
		EnderCrystalEntity crystal = Streams.stream(mc.world.getEntities())
				.filter(entity -> entity instanceof EnderCrystalEntity)
				.map(entity -> (EnderCrystalEntity) entity)
				.min(Comparator.comparing(c -> mc.player.distanceTo(c)))
				.orElse(null);
		for(Entity e: mc.world.getEntities()) {
			if(e instanceof EnderCrystalEntity && mc.player.distanceTo(e) <= getSettings().get(9).toSlider().getValue()) {
				if (getSettings().get(7).toToggle()	.state && e != null) {
					if (((System.nanoTime() / 1000000) - systemTime) >= 250) {
						if (getSettings().get(8).toToggle().state && mc.player.hasStatusEffect(StatusEffects.WEAKNESS)) {
							if (!isAttacking) {
								// save initial player hand
								oldSlot = mc.player.inventory.selectedSlot;
								isAttacking = true;
							}
							newSlot = -1;
							for (int i = 0; i < 9; i++) {
								ItemStack stack = mc.player.getMainHandStack();
								if (stack == ItemStack.EMPTY) {
									continue;
								}
								if ((stack.getItem() instanceof SwordItem)) {
									newSlot = i;
									break;
								}
								if ((stack.getItem() instanceof ToolItem)) {
									newSlot = i;
									break;
								}
							}
							if (newSlot != -1) {
								mc.player.inventory.selectedSlot = newSlot;
								switchCooldown = true;
							}
						}
						if (!mc.player.canSee(e) && !getSettings().get(1).toToggle().state) continue;
						if(getSettings().get(0).toToggle().state) EntityUtils.facePos(e.x, e.y + e.getHeight() / 2, e.z);
						mc.player.networkHandler.sendPacket(new PlayerInteractEntityC2SPacket(e));
						mc.player.attack(e);
						mc.player.swingHand(Hand.MAIN_HAND);
						systemTime = System.nanoTime() / 1000000;
					}
					return;
				}
				else {
					if (oldSlot != -1) {
						oldSlot = mc.player.inventory.selectedSlot;
						oldSlot = -1;
					}
					isAttacking = false;
				}
				int crystalSlot = mc.player.getMainHandStack().getItem() == Items.END_CRYSTAL ? mc.player.inventory.selectedSlot : -1;
				if (crystalSlot == -1) {
					for (int l = 0; l < 9; ++l) {
						if (mc.player.inventory.getInvStack(l).getItem() == Items.END_CRYSTAL) {
							crystalSlot = l;
							break;
						}
					}
				}
				boolean offhand = false;
				if (mc.player.getOffHandStack().getItem() == Items.END_CRYSTAL) {
					offhand = true;
				} else if (crystalSlot == -1) {
					return;
				}

				List<BlockPos> blocks = findCrystalBlocks();
				List<Entity> entities = new ArrayList<>();
				entities.addAll(Streams.stream(mc.world.getEntities())
						.filter(entity -> (
								entity instanceof PlayerEntity && getSettings().get(3).toToggle().state)
								|| (entity instanceof Monster && getSettings().get(4).toToggle().state)
								|| (EntityUtils.isAnimal(entity) && getSettings().get(5).toToggle().state))
						.sorted((a, b) -> Float.compare(a.distanceTo(mc.player), b.distanceTo(mc.player))).collect(Collectors.toList()));

				BlockPos q = null;
				double damage = .5;
				for (Entity entity : entities) {
					if (entity == mc.player || ((LivingEntity) entity).getHealth() <= 0) {
						continue;
					}
					for (BlockPos blockPos : blocks) {
						double b = entity.squaredDistanceTo(blockPos.getX(), blockPos.getY(), blockPos.getZ());
						if (b >= 169) {
							continue; // If this block if further than 13 (3.6^2, less calc) blocks, ignore it. It'll take no or very little damage
						}
						double d = calculateDamage(blockPos.getX() + .5, blockPos.getY() + 1, blockPos.getZ() + .5, entity);
						if (d > damage) {
							double self = calculateDamage(blockPos.getX() + .5, blockPos.getY() + 1, blockPos.getZ() + .5, mc.player);
							// If this deals more damage to ourselves than it does to our target, continue. This is only ignored if the crystal is sure to kill our target but not us.
							// Also continue if our crystal is going to hurt us.. alot
							if ((self > d && !(d < ((LivingEntity) entity).getHealth())) || self - .5 > mc.player.getHealth()) {
								continue;
							}
							damage = d;
							q = blockPos;
							renderEnt = entity;
						}
					}
				}
				if (damage == .5) {
					render = null;
					renderEnt = null;
					resetRotation();
					return;
				}
				render = q;

				if (getSettings().get(6).toToggle()	.state) {
					if (!offhand && mc.player.inventory.selectedSlot != crystalSlot) {
						if (getSettings().get(2).toToggle()	.state) {
							mc.player.inventory.selectedSlot = crystalSlot;
							resetRotation();
							switchCooldown = true;
						}
						return;
					}
					lookAtPacket(q.getX() + .5, q.getY() - .5, q.getZ() + .5, mc.player);
					BlockHitResult result = mc.world.rayTrace(new RayTraceContext(new Vec3d(mc.player.getPos().getX(), mc.player.getPos().getY() + mc.player.getEyeHeight(mc.player.getPose()), mc.player.getPos().getZ()),
							new Vec3d(q.getX() + .5, q.getY() - .5d, q.getZ() + .5),
							ShapeType.COLLIDER, FluidHandling.NONE, mc.player));
					Direction f;
					if (result == null || result.getSide() == null) {
						f = Direction.UP;
					} else {
						f = result.getSide();
					}
					// return after we did an autoswitch
					if (switchCooldown) {
						switchCooldown = false;
						return;
					}


					//mc.player.interactAt(mc.player, new Vec3d(q.getX(), q.getY(), q.getZ()), offhand ? Hand.OFF_HAND : Hand.MAIN_HAND);
					//new PlayerInteractBlockC2SPacket(offhand ? Hand.OFF_HAND : Hand.MAIN_HAND,)
							//new PlayerInteractBlockC2SPacket(offhand ? Hand.OFF_HAND : Hand.MAIN_HAND, result));
					mc.interactionManager.interactBlock(mc.player, mc.world, offhand ? Hand.OFF_HAND : Hand.MAIN_HAND, new BlockHitResult(new Vec3d(q.getX(), q.getY(), q.getZ()), f, q, false));
				}
				//this sends a constant packet flow for default packets
				if (isSpoofingAngles) {
					if (togglePitch) {
						mc.player.pitch += 0.0004;
						togglePitch = false;
					} else {
						mc.player.pitch -= 0.0004;
						togglePitch = true;
					}
				}
			}
		}
	}

	private static boolean isSpoofingAngles;
	//this modifies packets being sent so no extra ones are made. NCP used to flag with "too many packets"
    private static void setYawAndPitch(float yaw1, float pitch1) {
        isSpoofingAngles = true;
    }

    private void resetRotation() {
        if (isSpoofingAngles) {
            isSpoofingAngles = false;
        }
    }

    private void lookAtPacket(double px, double py, double pz, PlayerEntity me) {
        double[] v = calculateLookAt(px, py, pz, me);
        setYawAndPitch((float) v[0], (float) v[1]);
    }

    public double[] calculateLookAt(double px, double py, double pz, PlayerEntity me) {
        double dirx = me.x - px;
        double diry = me.y - py;
        double dirz = me.z - pz;

        double len = Math.sqrt(dirx*dirx + diry*diry + dirz*dirz);

        dirx /= len;
        diry /= len;
        dirz /= len;

        double pitch = Math.asin(diry);
        double yaw = Math.atan2(dirz, dirx);

        //to degree
        pitch = pitch * 180.0d / Math.PI;
        yaw = yaw * 180.0d / Math.PI;

        yaw += 90f;

        return new double[]{yaw,pitch};
    }

	private BlockPos getPlayerPos() {
        return new BlockPos(Math.floor(mc.player.x), Math.floor(mc.player.y), Math.floor(mc.player.z));
    }

	private List<BlockPos> findCrystalBlocks() {
        DefaultedList<BlockPos> positions = DefaultedList.of();
        positions.addAll(getSphere(getPlayerPos(),
        		(float) getSettings().get(9).toSlider().getValue(), (int) getSettings().get(9).toSlider().getValue(), false, true, 0)
        		.stream().filter(this::canPlaceCrystal).collect(Collectors.toList()));
        return positions;
    }

	public List<BlockPos> getSphere(BlockPos loc, float r, int h, boolean hollow, boolean sphere, int plus_y) {
	        List<BlockPos> circleblocks = new ArrayList<>();
	        int cx = loc.getX();
	        int cy = loc.getY();
	        int cz = loc.getZ();
	        for (int x = cx - (int) r; x <= cx + r; x++) {
	            for (int z = cz - (int) r; z <= cz + r; z++) {
	                for (int y = (sphere ? cy - (int) r : cy); y < (sphere ? cy + r : cy + h); y++) {
	                    double dist = (cx - x) * (cx - x) + (cz - z) * (cz - z) + (sphere ? (cy - y) * (cy - y) : 0);
	                    if (dist < r * r && !(hollow && dist < (r - 1) * (r - 1))) {
	                        BlockPos l = new BlockPos(x, y + plus_y, z);
	                        circleblocks.add(l);
	                    }
	                }
	            }
	        }
	        return circleblocks;
	    }

	private boolean canPlaceCrystal(BlockPos blockPos) {
        BlockPos boost = blockPos.add(0, 1, 0);
        BlockPos boost2 = blockPos.add(0, 2, 0);
        if ((mc.world.getBlockState(blockPos).getBlock() != Blocks.BEDROCK
                && mc.world.getBlockState(blockPos).getBlock() != Blocks.OBSIDIAN)
                || mc.world.getBlockState(boost).getBlock() != Blocks.AIR
                || mc.world.getBlockState(boost2).getBlock() != Blocks.AIR
                || !mc.world.getEntities(Entity.class, new Box(boost)).isEmpty()) {
            return false;
        }
        return true;
    }

	public float calculateDamage(double posX, double posY, double posZ, Entity entity) {
        float doubleExplosionSize = 6.0F * 2.0F;
        double distancedsize = entity.squaredDistanceTo(posX, posY, posZ) / (double) doubleExplosionSize;
        Vec3d vec3d = new Vec3d(posX, posY, posZ);
        double blockDensity = (double) entity.world.getBlockState(new BlockPos(vec3d)).getBlock().getBlastResistance();//entity.world.getBlockDensity(vec3d, entity.getBoundingBox());
        double v = (1.0D - distancedsize) * blockDensity;
        float damage = (float) ((int) ((v * v + v) / 2.0D * 7.0D * (double) doubleExplosionSize + 1.0D));
        double finald = 1;
        /*if (entity instanceof EntityLivingBase)
            finald = getBlastReduction((EntityLivingBase) entity,getDamageMultiplied(damage));*/
        if (entity instanceof LivingEntity) {
            finald = getBlastReduction((LivingEntity) entity, getDamageMultiplied(damage),
            		new Explosion(mc.world, null, posX, posY, posZ, 6F, false, Explosion.DestructionType.DESTROY));
        }
        return (float) finald;
    }

    public float getBlastReduction(LivingEntity entity, float damage, Explosion explosion) {
        if (entity instanceof PlayerEntity) {
            PlayerEntity ep = (PlayerEntity) entity;
            DamageSource ds = DamageSource.explosion(explosion);
            //damage = CombatRules.getDamageAfterAbsorb(damage, (float) ep.getArmor(), (float) ep.getAttributeInstance(EntityAttributes.ARMOR_TOUGHNESS).getValue());

            int k = EnchantmentHelper.getProtectionAmount(ep.getArmorItems(), ds);
            float f = MathHelper.clamp(k, 0.0F, 20.0F);
            damage = damage * (1.0F - f / 25.0F);

            if (entity.hasStatusEffect(StatusEffect.byRawId(11))) {
                damage = damage - (damage / 4);
            }

            damage = Math.max(damage - ep.getAbsorptionAmount(), 0.0F);
            return damage;
        }
        //damage = CombatRules.getDamageAfterAbsorb(damage, (float) entity.getArmor(), (float) entity.getAttributeInstance(EntityAttributes.ARMOR_TOUGHNESS).getValue());
        return damage;
    }

    private float getDamageMultiplied(float damage) {
        int diff = mc.world.getDifficulty().getId();
        return damage * (diff == 0 ? 0 : (diff == 2 ? 1 : (diff == 1 ? 0.5f : 1.5f)));
    }

    public float calculateDamage(EnderCrystalEntity crystal, Entity entity) {
        return calculateDamage(crystal.x, crystal.y, crystal.z, entity);
    }

	@Override
	public void onDisable() {
			render = null;
			renderEnt = null;
			resetRotation();
		super.onDisable();
	}
}
