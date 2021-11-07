package bleach.hack.module.mods;

import bleach.hack.event.events.EventTick;
import bleach.hack.eventbus.BleachSubscribe;
import bleach.hack.module.ModuleCategory;
import bleach.hack.module.setting.base.SettingMode;
import bleach.hack.module.setting.base.SettingSlider;
import bleach.hack.module.setting.base.SettingToggle;
import bleach.hack.module.setting.other.SettingRotate;
import bleach.hack.util.InventoryUtils;
import bleach.hack.util.world.DamageUtils;
import bleach.hack.util.world.EntityUtils;
import bleach.hack.util.world.WorldUtils;
import com.google.common.collect.Streams;
import net.minecraft.block.BedBlock;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.*;
import java.util.*;
import java.util.stream.Collectors;
import bleach.hack.module.Module;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.dimension.DimensionType;

public class Fucker extends Module {
    private int placeCooldown = 0;

    public Fucker() {
        super("Fucker", KEY_UNBOUND, ModuleCategory.COMBAT, "AnchorAura and BedAura combined",
                new SettingToggle("Players", true).withDesc("Targets players."),
                new SettingToggle("Mobs", false).withDesc("Targets mobs."),
                new SettingToggle("Animals", false).withDesc("Targets animals."),
                // For overworld it's always anchor
                // For nether it's always beds
                new SettingMode("Ender dimension mode", "Anchors", "Beds").withDesc("Which mode use in Ender dimension"),
                new SettingToggle("AntiSuicide", true).withDesc("Prevents you from killing yourself."),
                new SettingToggle("Anchors", true).withDesc("Anchor mode settings.").withChildren(
                        new SettingSlider("MinDmg", 1, 20, 2, 0).withDesc("Minimum damage to the target to place crystals."),
                        new SettingSlider("MinRatio", 0.5, 6, 2, 1).withDesc("Minimum damage ratio to place a crystal at (Target dmg/Player dmg)."),
                        new SettingToggle("Raycast", false).withDesc("Only places it if you can see it.")),
                new SettingToggle("Beds", true).withDesc("Bed mode settings.").withChildren(
                        new SettingSlider("MinDmg", 1, 20, 2, 0).withDesc("Minimum damage to the target to place crystals."),
                        new SettingSlider("MinRatio", 0.5, 6, 2, 1).withDesc("Minimum damage ratio to place a crystal at (Target dmg/Player dmg)."),
                        new SettingToggle("Raycast", false).withDesc("Only places a crystal if you can see it.")),
                new SettingRotate(false).withDesc("Rotates to block you place/charge."),
                new SettingSlider("Range", 0, 6, 4.5, 2).withDesc("Range to place and attack crystals."),
                new SettingSlider("CPT", 1, 10, 2, 0).withDesc("How many times to place it per tick."),
                new SettingSlider("Cooldown", 0, 10, 0, 0).withDesc("How many ticks to wait before exploding the next batch."));
    }

    @BleachSubscribe
    public void onTick(EventTick event) {
        placeCooldown = Math.max(0, placeCooldown - 1);

        // Prevent from doing anything if we are eating
        if (mc.player.isUsingItem() && mc.player.getMainHandStack().isFood()) return;

        // Players to target
        List<LivingEntity> targets = Streams.stream(mc.world.getEntities())
                .filter(e -> EntityUtils.isAttackable(e, true))
                .filter(e -> (getSetting(0).asToggle().state && EntityUtils.isPlayer(e))
                        || (getSetting(1).asToggle().state && EntityUtils.isMob(e))
                        || (getSetting(2).asToggle().state && EntityUtils.isAnimal(e)))
                .map(e -> (LivingEntity) e)
                .collect(Collectors.toList());

        if (targets.isEmpty()) return;

        // Bed Slots
        List<Integer> bedSlots = Arrays.asList();
        for (String str : Arrays.asList("white_bed", "orange_bed", "magenta_bed", "light_blue_bed", "yellow_bed",
                "lime_bed", "pink_bed", "gray_bed", "light_gray_bed", "cyan_bed", "purple_bed", "blue_bed", "brown_bed",
                "green_bed", "red_bed", "black_bed", "bed")) {
            int slot = InventoryUtils.getSlot(false, i -> mc.player.getInventory().getStack(i).getItem()
                    == Registry.ITEM.get(new Identifier(str)));
            if (slot == -1) continue;
            bedSlots.add(slot);
        }

        // First slots available
        int bedSlot = bedSlots.get(0);
        int anchorSlot = InventoryUtils.getSlot(false, i -> mc.player.getInventory().getStack(i).getItem()
                == Registry.ITEM.get(new Identifier("respawn_anchor")));
        int glowstoneSlot = InventoryUtils.getSlot(false, i -> mc.player.getInventory().getStack(i).getItem()
                == Registry.ITEM.get(new Identifier("glowstone")));

        // Useful variables
        double range = getSetting(6).asSlider().getValue();
        int ceilRange = MathHelper.ceil(range);

        // Explode nearest anchor
        if (glowstoneSlot != -1 && getSetting(5).asToggle().state && !mc.world.getDimension().equals(DimensionType.THE_NETHER_ID)
            || (mc.world.getDimension().equals(DimensionType.THE_END_ID) && getSetting(3).asMode().mode == 0)) {
            BlockPos nearestAnchor = BlockPos.streamOutwards(new BlockPos(mc.player.getEyePos()), ceilRange, ceilRange, ceilRange)
                    .filter(e -> mc.world.getBlockState(new BlockPos(e.getX(), e.getY(), e.getZ())).getBlock() instanceof RespawnAnchorBlock)
                    .map(e -> new BlockPos(e.getX(), e.getY(), e.getZ()))
                    .collect(Collectors.toList())
                    .get(0);

            if (nearestAnchor != null) {
                if (mc.world.isAir(nearestAnchor)) return;
                if (mc.player.getInventory().getStack(glowstoneSlot).getCount() == 0) return;
                Hand hand = InventoryUtils.selectSlot(glowstoneSlot);
                mc.interactionManager.interactBlock(mc.player, mc.world, hand,
                        new BlockHitResult(Vec3d.ofCenter(nearestAnchor, 1), Direction.UP, nearestAnchor, false));
            }
        }

        // Explode nearest bed
        if (glowstoneSlot != -1 && getSetting(6).asToggle().state && !mc.world.getDimension().equals(DimensionType.OVERWORLD_ID)
                || (mc.world.getDimension().equals(DimensionType.THE_END_ID) && getSetting(3).asMode().mode == 1)) {
            BlockPos nearestBed = BlockPos.streamOutwards(new BlockPos(mc.player.getEyePos()), ceilRange, ceilRange, ceilRange)
                    .filter(e -> mc.world.getBlockState(new BlockPos(e.getX(), e.getY(), e.getZ())).getBlock() instanceof BedBlock)
                    .map(e -> new BlockPos(e.getX(), e.getY(), e.getZ()))
                    .collect(Collectors.toList())
                    .get(0);

            if (nearestBed != null) {
                if (mc.world.isAir(nearestBed)) return;
                mc.interactionManager.interactBlock(mc.player, mc.world, mc.player.getActiveHand(),
                        new BlockHitResult(Vec3d.ofCenter(nearestBed, 1), Direction.UP, nearestBed, false));
            }
        }

        // Place blocks
        if (getSetting(5).asToggle().state) placeBlock(anchorSlot, getSetting(5).asToggle(), targets, 5f);
        if (getSetting(6).asToggle().state) placeBlock(bedSlot, getSetting(6).asToggle(), targets, 5f);
    }

    private void placeBlock(int slot, SettingToggle root, List<LivingEntity> targets, float power) {
        if (root.state && placeCooldown <= 0) {
            Map<BlockPos, Float> placeBlocks = new LinkedHashMap<>();

            // Get places where we can place the block
            for (Vec3d v : getPoses(root.getChild(2).asToggle().state, getSetting(8).asSlider().getValue())) {
                float playerDamg = DamageUtils.getExplosionDamage(v, power, mc.player);

                if (DamageUtils.willKill(mc.player, playerDamg) && getSetting(4).asToggle().state)
                    continue;

                for (LivingEntity e : targets) {
                    float targetDamg = DamageUtils.getExplosionDamage(v, 5f, e);
                    if (DamageUtils.willPop(mc.player, playerDamg) && !DamageUtils.willPopOrKill(e, targetDamg)
                            && getSetting(4).asToggle().state) continue;

                    if (targetDamg >= root.getChild(0).asSlider().getValue()) {
                        float ratio = playerDamg == 0 ? targetDamg : targetDamg / playerDamg;

                        if (ratio > root.getChild(1).asSlider().getValue())
                            placeBlocks.put(new BlockPos(v).down(), ratio);
                    }
                }
            }

            placeBlocks = placeBlocks.entrySet().stream()
                    .sorted((b1, b2) -> Float.compare(b2.getValue(), b1.getValue()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (x, y) -> y, LinkedHashMap::new));

            for (Map.Entry<BlockPos, Float> e : placeBlocks.entrySet()) {
                BlockPos block = e.getKey();

                Vec3d eyeVec = mc.player.getEyePos();

                Vec3d vec = Vec3d.ofCenter(block, 1);
                Direction dir = null;
                for (Direction d : Direction.values()) {
                    Vec3d vd = WorldUtils.getLegitLookPos(block, d, true, 5);
                    if (vd != null && eyeVec.distanceTo(vd) <= eyeVec.distanceTo(vec)) {
                        vec = vd;
                        dir = d;
                    }
                }

                if (dir == null) continue;
                if (getSetting(7).asRotate().state) WorldUtils.facePosAuto(vec.x, vec.y, vec.z, getSetting(6).asRotate());
                WorldUtils.placeBlock(block, slot, getSetting(9).asRotate(), true, false, true);
            }
        }
    }

    public Set<Vec3d> getPoses(boolean raycast, double rangeReal) {
        Set<Vec3d> poses = new HashSet<>();

        int range = (int) Math.floor(rangeReal);
        for (int x = -range; x <= range; x++) {
            for (int y = -range; y <= range; y++) {
                for (int z = -range; z <= range; z++) {
                    BlockPos basePos = new BlockPos(mc.player.getEyePos()).add(x, y, z);

                    if (raycast) {
                        boolean allBad = true;
                        for (Direction d : Direction.values()) {
                            if (WorldUtils.getLegitLookPos(basePos, d, true, 5) != null) {
                                allBad = false;
                                break;
                            }
                        }

                        if (allBad) continue;
                    }

                    if (mc.player.getPos().distanceTo(Vec3d.of(basePos).add(0.5, 1, 0.5)) <= rangeReal + 0.25)
                        poses.add(Vec3d.of(basePos).add(0.5, 1, 0.5));
                }
            }
        }

        return poses;
    }
}
