/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 * Modified for BleachHack
 */

package org.bleachhack.addons;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.entrypoint.EntrypointContainer;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.fabricmc.loader.api.metadata.Person;
import org.bleachhack.util.meteor.Init;
import org.bleachhack.util.meteor.InitStage;

import java.util.ArrayList;
import java.util.List;

public class AddonManager {
    public static BleachAddon BLEACH;
    public static final List<BleachAddon> ADDONS = new ArrayList<>();

    @Init(stage = InitStage.Pre)
    public static void init() {
        // Meteor pseudo addon
        {
            BLEACH = new BleachAddon() {
                @Override
                public void onInitialize() {}
            };

            ModMetadata metadata = FabricLoader.getInstance().getModContainer("bleachhack").get().getMetadata();

            BLEACH.name = metadata.getName();
            BLEACH.authors = new String[metadata.getAuthors().size()];

            int i = 0;
            for (Person author : metadata.getAuthors()) {
                BLEACH.authors[i++] = author.getName();
            }
        }

        // Addons
        for (EntrypointContainer<BleachAddon> entrypoint : FabricLoader.getInstance().getEntrypointContainers("bleach"/*this could ruin something*/, BleachAddon.class)) {
            ModMetadata metadata = entrypoint.getProvider().getMetadata();
            BleachAddon addon = entrypoint.getEntrypoint();

            addon.name = metadata.getName();
            addon.authors = new String[metadata.getAuthors().size()];

            int i = 0;
            for (Person author : metadata.getAuthors()) {
                addon.authors[i++] = author.getName();
            }

            ADDONS.add(addon);
        }
    }
}