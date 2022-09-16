package org.bleachhack.module.mods;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import org.bleachhack.event.events.EventWorldRender;
import org.bleachhack.eventbus.BleachSubscribe;
import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.setting.module.SettingColor;
import org.bleachhack.setting.module.SettingMode;
import org.bleachhack.setting.module.SettingSlider;
import org.bleachhack.util.SignData;
import org.bleachhack.util.io.BleachFileMang;
import org.bleachhack.util.render.Renderer;
import org.bleachhack.util.render.color.QuadColor;
import org.bleachhack.util.shader.BleachCoreShaders;
import org.bleachhack.util.shader.ColorVertexConsumerProvider;
import org.bleachhack.util.shader.ShaderEffectWrapper;
import org.bleachhack.util.shader.ShaderLoader;

import java.io.IOException;
import java.util.*;

public class SignRestorer extends Module {
    private HashSet<Block> signs = new HashSet<>(Arrays.asList(
            Blocks.OAK_SIGN,
            Blocks.ACACIA_SIGN,
            Blocks.SPRUCE_SIGN,
            Blocks.JUNGLE_SIGN,
            Blocks.BIRCH_SIGN,
            Blocks.DARK_OAK_SIGN,
            Blocks.MANGROVE_SIGN,
            Blocks.WARPED_SIGN,
            Blocks.CRIMSON_SIGN,
            Blocks.OAK_WALL_SIGN,
            Blocks.ACACIA_WALL_SIGN,
            Blocks.SPRUCE_WALL_SIGN,
            Blocks.JUNGLE_WALL_SIGN,
            Blocks.BIRCH_WALL_SIGN,
            Blocks.DARK_OAK_WALL_SIGN,
            Blocks.MANGROVE_WALL_SIGN,
            Blocks.WARPED_WALL_SIGN,
            Blocks.CRIMSON_WALL_SIGN));

    public static List<Map<String, SignData>> signData = null;

    public SignRestorer() {
        super("SignRestorer", KEY_UNBOUND, ModuleCategory.PLAYER, "Restores signs from signdata.json.",
                new SettingMode("Render", "Box+Fill", "Box", "Fill").withDesc("The rendering method."),
                new SettingSlider("Box", 0.1, 4, 2, 1).withDesc("The width/thickness of the box lines."),
                new SettingSlider("Fill", 0, 1, 0.3, 2).withDesc("The opacity of the fill."),
                new SettingColor("Color", 128, 128, 128).withDesc("The color of the highlight."));
        ;
    }

    @Override
    public void onEnable(boolean inWorld) {
        super.onEnable(inWorld);

        try {
            reloadSignData();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void reloadSignData() throws IOException {
        String fileData = null;
        try {
            Gson gson = new Gson();
            fileData = fetchFile();
            signData = gson.fromJson(fileData, new TypeToken<ArrayList<Map<String, SignData>>>() {
            }.getType());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String fetchFile() {
        if (!BleachFileMang.fileExists("signdata.json")) {
            BleachFileMang.createEmptyFile("signdata.json");
            BleachFileMang.appendFile("signdata.json", "[\n" + "  {\"0 68 0\":{\"text\":[\"DarkHack\",\"on\",\"top\",\"since 2019\"],\"flags\":{}}}\n" + "  ]");
        }

        String output = "";
        for (String line : BleachFileMang.readFileLines("signdata.json")) {
            output += line;
        }

        return output;
    }

    private BlockPos stringToBlockPos(String string) {
        String[] parts = string.split(" ");
        return new BlockPos(
                Integer.parseInt(parts[0]),
                Integer.parseInt(parts[1]),
                Integer.parseInt(parts[2])
        );
    }

    @BleachSubscribe
    public void onWorldRender(EventWorldRender.Post event) {
        for (int i = 0; i < signData.size(); i++) {
            for (String key : signData.get(i).keySet()) {
                BlockPos pos = this.stringToBlockPos(key);
                BlockState state = mc.world.getBlockState(pos);

                if (!signs.contains(state.getBlock())) {
                    int mode = getSetting(0).asMode().getMode();
                    int[] rgb = getSetting(3).asColor().getRGBArray();

                    if (mode == 0 || mode == 1) {
                        float outlineWidth = getSetting(1).asSlider().getValueFloat();
                        Renderer.drawBoxOutline(pos, QuadColor.single(rgb[0], rgb[1], rgb[2], 255), outlineWidth);
                    }

                    if (mode == 0 || mode == 2) {
                        int fillAlpha = (int) (getSetting(2).asSlider().getValueFloat() * 255);
                        Renderer.drawBoxFill(pos, QuadColor.single(rgb[0], rgb[1], rgb[2], fillAlpha));
                    }
                }
            }
        }
    }
}
