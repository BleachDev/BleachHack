package org.bleachhack.module.mods;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.util.math.BlockPos;
import org.bleachhack.event.events.EventTick;
import org.bleachhack.event.events.EventWorldRender;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.setting.module.*;
import org.bleachhack.util.render.Renderer;
import org.bleachhack.util.render.color.QuadColor;

import java.util.*;

public class SculkESP extends Module {

    private HashSet<BlockPos> sculks = new HashSet<>();
    private HashSet<Block> blocks = new HashSet<>(Arrays.asList(
            Blocks.TORCH,
            Blocks.SOUL_TORCH,
            Blocks.LANTERN,
            Blocks.SOUL_LANTERN,
            Blocks.REDSTONE_WIRE,
            Blocks.REDSTONE_TORCH,
            Blocks.REPEATER,
            Blocks.COMPARATOR,
            Blocks.RAIL,
            Blocks.ACTIVATOR_RAIL,
            Blocks.DETECTOR_RAIL,
            Blocks.POWERED_RAIL,
            Blocks.SCULK_VEIN,
            Blocks.VINE,
            Blocks.CAVE_VINES,
            Blocks.CAVE_VINES_PLANT,
            Blocks.TWISTING_VINES,
            Blocks.TWISTING_VINES_PLANT,
            Blocks.WEEPING_VINES,
            Blocks.WEEPING_VINES_PLANT,
            Blocks.GLOW_LICHEN,
            Blocks.LAVA,
            Blocks.WATER,
            Blocks.OAK_PRESSURE_PLATE,
            Blocks.ACACIA_PRESSURE_PLATE,
            Blocks.BIRCH_PRESSURE_PLATE,
            Blocks.MANGROVE_PRESSURE_PLATE,
            Blocks.DARK_OAK_PRESSURE_PLATE,
            Blocks.JUNGLE_PRESSURE_PLATE,
            Blocks.CRIMSON_PRESSURE_PLATE,
            Blocks.WARPED_PRESSURE_PLATE,
            Blocks.SPRUCE_PRESSURE_PLATE,
            Blocks.OAK_WALL_SIGN,
            Blocks.ACACIA_WALL_SIGN,
            Blocks.BIRCH_WALL_SIGN,
            Blocks.MANGROVE_WALL_SIGN,
            Blocks.DARK_OAK_WALL_SIGN,
            Blocks.JUNGLE_WALL_SIGN,
            Blocks.CRIMSON_WALL_SIGN,
            Blocks.WARPED_WALL_SIGN,
            Blocks.SPRUCE_WALL_SIGN,
            Blocks.OAK_SIGN,
            Blocks.ACACIA_SIGN,
            Blocks.BIRCH_SIGN,
            Blocks.MANGROVE_SIGN,
            Blocks.DARK_OAK_SIGN,
            Blocks.JUNGLE_SIGN,
            Blocks.CRIMSON_SIGN,
            Blocks.WARPED_SIGN,
            Blocks.SPRUCE_SIGN,
            Blocks.OAK_BUTTON,
            Blocks.ACACIA_BUTTON,
            Blocks.BIRCH_BUTTON,
            Blocks.MANGROVE_BUTTON,
            Blocks.DARK_OAK_BUTTON,
            Blocks.JUNGLE_BUTTON,
            Blocks.CRIMSON_BUTTON,
            Blocks.WARPED_BUTTON,
            Blocks.SPRUCE_BUTTON,
            Blocks.COCOA,
            Blocks.SMALL_DRIPLEAF,
            Blocks.BIG_DRIPLEAF,
            Blocks.BIG_DRIPLEAF_STEM,
            Blocks.SPORE_BLOSSOM,
            Blocks.MOSS_CARPET,
            Blocks.BAMBOO,
            Blocks.BAMBOO_SAPLING,
            Blocks.BEETROOTS,
            Blocks.CARROTS,
            Blocks.MELON_STEM,
            Blocks.PUMPKIN_STEM,
            Blocks.OAK_SAPLING,
            Blocks.ACACIA_SAPLING,
            Blocks.BIRCH_SAPLING,
            Blocks.DARK_OAK_SAPLING,
            Blocks.JUNGLE_SAPLING,
            Blocks.SPRUCE_SAPLING,
            Blocks.POTTED_OAK_SAPLING,
            Blocks.POTTED_ACACIA_SAPLING,
            Blocks.POTTED_BIRCH_SAPLING,
            Blocks.POTTED_DARK_OAK_SAPLING,
            Blocks.POTTED_JUNGLE_SAPLING,
            Blocks.POTTED_SPRUCE_SAPLING,
            Blocks.SWEET_BERRY_BUSH,
            Blocks.SEAGRASS,
            Blocks.SEA_PICKLE,
            Blocks.FERN,
            Blocks.LARGE_FERN,
            Blocks.POTTED_FERN,
            Blocks.SUGAR_CANE,
            Blocks.POTATOES,
            Blocks.DEAD_BUSH,
            Blocks.HANGING_ROOTS,
            Blocks.GRASS,
            Blocks.SEAGRASS,
            Blocks.TALL_SEAGRASS,
            Blocks.FLOWER_POT,
            Blocks.LILY_PAD,
            Blocks.WHEAT,
            Blocks.KELP,
            Blocks.KELP_PLANT,
            Blocks.CRIMSON_FUNGUS,
            Blocks.WARPED_FUNGUS,
            Blocks.POTTED_CRIMSON_FUNGUS,
            Blocks.POTTED_WARPED_FUNGUS,
            Blocks.NETHER_WART,
            Blocks.NETHER_SPROUTS,
            Blocks.CRIMSON_ROOTS,
            Blocks.WARPED_ROOTS,
            Blocks.POTTED_CRIMSON_ROOTS,
            Blocks.POTTED_WARPED_ROOTS,
            Blocks.RED_MUSHROOM,
            Blocks.BROWN_MUSHROOM,
            Blocks.POTTED_RED_MUSHROOM,
            Blocks.POTTED_BROWN_MUSHROOM,
            Blocks.DANDELION,
            Blocks.POPPY,
            Blocks.BLUE_ORCHID,
            Blocks.ALLIUM,
            Blocks.AZURE_BLUET,
            Blocks.RED_TULIP,
            Blocks.ORANGE_TULIP,
            Blocks.WHITE_TULIP,
            Blocks.PINK_TULIP,
            Blocks.OXEYE_DAISY,
            Blocks.CORNFLOWER,
            Blocks.LILY_OF_THE_VALLEY,
            Blocks.WITHER_ROSE,
            Blocks.SUNFLOWER,
            Blocks.LILAC,
            Blocks.ROSE_BUSH,
            Blocks.PEONY,
            Blocks.POTTED_DANDELION,
            Blocks.POTTED_POPPY,
            Blocks.POTTED_BLUE_ORCHID,
            Blocks.POTTED_ALLIUM,
            Blocks.POTTED_AZURE_BLUET,
            Blocks.POTTED_RED_TULIP,
            Blocks.POTTED_ORANGE_TULIP,
            Blocks.POTTED_WHITE_TULIP,
            Blocks.POTTED_PINK_TULIP,
            Blocks.POTTED_OXEYE_DAISY,
            Blocks.POTTED_CORNFLOWER,
            Blocks.POTTED_LILY_OF_THE_VALLEY,
            Blocks.POTTED_WITHER_ROSE,
            Blocks.CHAIN,
            Blocks.CONDUIT,
            Blocks.END_ROD,
            Blocks.FIRE,
            Blocks.SOUL_FIRE,
            Blocks.CREEPER_HEAD,
            Blocks.DRAGON_HEAD,
            Blocks.PLAYER_HEAD,
            Blocks.ZOMBIE_HEAD,
            Blocks.SKELETON_SKULL,
            Blocks.WITHER_SKELETON_SKULL,
            Blocks.CREEPER_WALL_HEAD,
            Blocks.DRAGON_WALL_HEAD,
            Blocks.PLAYER_WALL_HEAD,
            Blocks.ZOMBIE_WALL_HEAD,
            Blocks.SKELETON_WALL_SKULL,
            Blocks.WITHER_SKELETON_WALL_SKULL,
            Blocks.TUBE_CORAL_FAN,
            Blocks.BRAIN_CORAL_FAN,
            Blocks.BUBBLE_CORAL_FAN,
            Blocks.FIRE_CORAL_FAN,
            Blocks.HORN_CORAL_FAN,
            Blocks.TUBE_CORAL_WALL_FAN,
            Blocks.BRAIN_CORAL_WALL_FAN,
            Blocks.BUBBLE_CORAL_WALL_FAN,
            Blocks.FIRE_CORAL_WALL_FAN,
            Blocks.HORN_CORAL_WALL_FAN,
            Blocks.DEAD_TUBE_CORAL_FAN,
            Blocks.DEAD_BRAIN_CORAL_FAN,
            Blocks.DEAD_BUBBLE_CORAL_FAN,
            Blocks.DEAD_FIRE_CORAL_FAN,
            Blocks.DEAD_HORN_CORAL_FAN,
            Blocks.DEAD_TUBE_CORAL_WALL_FAN,
            Blocks.DEAD_BRAIN_CORAL_WALL_FAN,
            Blocks.DEAD_BUBBLE_CORAL_WALL_FAN,
            Blocks.DEAD_FIRE_CORAL_WALL_FAN,
            Blocks.DEAD_HORN_CORAL_WALL_FAN,
            Blocks.SNOW,
            Blocks.MANGROVE_PROPAGULE,
            Blocks.POTTED_MANGROVE_PROPAGULE,
            Blocks.LEVER,
            Blocks.TRIPWIRE,
            Blocks.TRIPWIRE_HOOK,
            Blocks.IRON_BARS,
            Blocks.OAK_FENCE,
            Blocks.ACACIA_FENCE,
            Blocks.BIRCH_FENCE,
            Blocks.MANGROVE_FENCE,
            Blocks.DARK_OAK_FENCE,
            Blocks.JUNGLE_FENCE,
            Blocks.CRIMSON_FENCE,
            Blocks.WARPED_FENCE,
            Blocks.SPRUCE_FENCE,
            Blocks.NETHER_BRICK_FENCE,
            Blocks.LADDER,
            Blocks.SMALL_AMETHYST_BUD,
            Blocks.MEDIUM_AMETHYST_BUD,
            Blocks.LARGE_AMETHYST_BUD,
            Blocks.AMETHYST_CLUSTER,
            Blocks.POINTED_DRIPSTONE,
            Blocks.WHITE_BANNER,
            Blocks.BLACK_BANNER,
            Blocks.BLUE_BANNER,
            Blocks.ORANGE_BANNER,
            Blocks.MAGENTA_BANNER,
            Blocks.LIGHT_BLUE_BANNER,
            Blocks.YELLOW_BANNER,
            Blocks.LIME_BANNER,
            Blocks.PINK_BANNER,
            Blocks.GRAY_BANNER,
            Blocks.LIGHT_GRAY_BANNER,
            Blocks.CYAN_BANNER,
            Blocks.PURPLE_BANNER,
            Blocks.BROWN_BANNER,
            Blocks.GREEN_BANNER,
            Blocks.RED_BANNER,
            Blocks.WHITE_CANDLE,
            Blocks.BLACK_CANDLE,
            Blocks.BLUE_CANDLE,
            Blocks.ORANGE_CANDLE,
            Blocks.MAGENTA_CANDLE,
            Blocks.LIGHT_BLUE_CANDLE,
            Blocks.YELLOW_CANDLE,
            Blocks.LIME_CANDLE,
            Blocks.PINK_CANDLE,
            Blocks.GRAY_CANDLE,
            Blocks.LIGHT_GRAY_CANDLE,
            Blocks.CYAN_CANDLE,
            Blocks.PURPLE_CANDLE,
            Blocks.BROWN_CANDLE,
            Blocks.GREEN_CANDLE,
            Blocks.RED_CANDLE,
            Blocks.CANDLE,
            Blocks.WHITE_STAINED_GLASS_PANE,
            Blocks.BLACK_STAINED_GLASS_PANE,
            Blocks.BLUE_STAINED_GLASS_PANE,
            Blocks.ORANGE_STAINED_GLASS_PANE,
            Blocks.MAGENTA_STAINED_GLASS_PANE,
            Blocks.LIGHT_BLUE_STAINED_GLASS_PANE,
            Blocks.YELLOW_STAINED_GLASS_PANE,
            Blocks.LIME_STAINED_GLASS_PANE,
            Blocks.PINK_STAINED_GLASS_PANE,
            Blocks.GRAY_STAINED_GLASS_PANE,
            Blocks.LIGHT_GRAY_STAINED_GLASS_PANE,
            Blocks.CYAN_STAINED_GLASS_PANE,
            Blocks.PURPLE_STAINED_GLASS_PANE,
            Blocks.BROWN_STAINED_GLASS_PANE,
            Blocks.GREEN_STAINED_GLASS_PANE,
            Blocks.RED_STAINED_GLASS_PANE,
            Blocks.LIGHTNING_ROD,
            Blocks.STONE_PRESSURE_PLATE,
            Blocks.POLISHED_BLACKSTONE_PRESSURE_PLATE,
            Blocks.LIGHT_WEIGHTED_PRESSURE_PLATE,
            Blocks.HEAVY_WEIGHTED_PRESSURE_PLATE,
            Blocks.STONE_BUTTON,
            Blocks.POLISHED_BLACKSTONE_BUTTON));

    public SculkESP() {
        super("SculkESP", KEY_UNBOUND, ModuleCategory.RENDER, "Highlights certain blocks.",
                new SettingSlider("Radius", 1, 20, 15, 0).withDesc("Radius in which sculk sensors are getting searched."),
                new SettingColor("Color", 255, 115, 115).withDesc("Color for neighbour blocks."),
                new SettingSlider("Width", 0.1, 4, 2.5, 1).withDesc("The width of the box lines."),
                new SettingSlider("Box Opacity", 0, 1, 0, 2).withDesc("The opacity of the box fill."),
                new SettingSlider("Line Opacity", 0, 1, 1, 2).withDesc("The opacity of the lines."));
    }

    @Override
    public void onDisable(boolean inWorld) {
        sculks.clear();

        super.onDisable(inWorld);
    }

    @BleachSubscribe
    public void onTick(EventTick event) {
        if (mc.player.age % 14 == 0) {
            sculks.clear();

            int dist = getSetting(0).asSlider().getValueInt();
            int radius = 9;

            for (BlockPos shriekerPos : BlockPos.iterateOutwards(mc.player.getBlockPos(), dist, dist, dist)) {
                if (!mc.world.isInBuildLimit(shriekerPos.down())
                        || mc.world.getBlockState(shriekerPos).getBlock() != Blocks.SCULK_SHRIEKER)
                    continue;

                for (BlockPos sensorPos : BlockPos.iterateOutwards(shriekerPos, radius, radius, radius)) {
                    if (mc.world.getBlockState(sensorPos).getBlock() == Blocks.SCULK_SENSOR) {
                        for (BlockPos dangerSpot : BlockPos.iterateOutwards(sensorPos, radius, radius, radius)) {
                            if (dangerSpot.isWithinDistance(sensorPos, radius)) {
                                Block dangerBlock = mc.world.getBlockState(dangerSpot).getBlock();
                                if (!mc.world.getBlockState(dangerSpot).isAir()
                                        && mc.world.getBlockState(dangerSpot.up(1)).isAir()
                                        && mc.world.getBlockState(dangerSpot.up(2)).isAir()
                                        && mc.world.getBlockState(dangerSpot).getMaterial() != Material.WOOL
                                        && mc.world.getBlockState(dangerSpot).getMaterial() != Material.CARPET
                                        && !blocks.contains(dangerBlock)) {
                                    sculks.add(dangerSpot.toImmutable());
                                }

                                if (!mc.world.getBlockState(dangerSpot.down()).isAir()
                                        && blocks.contains(dangerBlock)) {
                                    sculks.add(dangerSpot.down().toImmutable());
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @BleachSubscribe
    public void onRender(EventWorldRender.Post event) {
        int alpha1 = (int) (getSetting(3).asSlider().getValueFloat() * 255);
        int alpha2 = (int) (getSetting(4).asSlider().getValueFloat() * 255);

        int[] color = getSetting(1).asColor().getRGBArray();

        sculks.forEach((pos) ->
                Renderer.drawBoxFill(pos, QuadColor.single(color[0], color[1], color[2], alpha1)));
        sculks.forEach((pos) ->
                Renderer.drawBoxOutline(pos, QuadColor.single(color[0], color[1], color[2], alpha2), getSetting(2).asSlider().getValueFloat()));
    }
}