package org.bleachhack.module.mods;

import org.bleachhack.module.Module;
import org.bleachhack.module.ModuleCategory;
import org.bleachhack.setting.module.SettingMode;
import org.bleachhack.util.DoomUtils;
import org.bleachhack.util.doom.mochadoom.Engine;
import org.bleachhack.util.io.BleachFileMang;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Doom extends Module {
    public static boolean running = false;
    public static ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    public static ScheduledFuture<?> future;

    // TODO: Add WAD selection
    public Doom() {
        super("Doom", KEY_UNBOUND, ModuleCategory.MISC, "Let's you play DOOM from Minecraft",
                new SettingMode("Wad", "DOOM", "DOOM2", "PLUTONIA", "TNT").withDesc("Switches between different versions"));
    }

    @Override
    public void onEnable(boolean inWorld) {
        running = true;

        if (!BleachFileMang.fileExists("doom/wads/DOOM.WAD")
                && !BleachFileMang.fileExists("doom/wads/DOOM2.WAD")
                && !BleachFileMang.fileExists("doom/wads/PLUTONIA.WAD")
                && !BleachFileMang.fileExists("doom/wads/TNT.WAD"))
            DoomUtils.downloadWads(true);

        int i = getSetting(0).asMode().getMode();
        Path dir = BleachFileMang.getDir();
        String path = dir + "/doom/wads/";

        String[] args = new String[]{"-iwad", path + "DOOM.WAD"};

        if (i == 1)
            args = new String[]{"-iwad", path + "DOOM2.WAD"};
        if (i == 2)
            args = new String[]{"-iwad", path + "PLUTONIA.WAD"};
        if (i == 3)
            args = new String[]{"-iwad", path + "TNT.WAD"};


        String[] finalArgs = args;
        future = executor.schedule(() -> {
            while (running) {
                try {
                    Engine.main(finalArgs);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 0, TimeUnit.SECONDS);
    }

    @Override
    public void onDisable(boolean inWorld) {
        running = false;
    }
}
