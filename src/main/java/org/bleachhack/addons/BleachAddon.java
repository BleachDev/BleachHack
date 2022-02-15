/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 * Modified for BleachHack
 */

package org.bleachhack.addons;

import org.bleachhack.util.meteor.render.color.Color;

public abstract class BleachAddon {
    /** This field is automatically assigned from fabric.mod.json file. */
    public String name;

    /** This field is automatically assigned from fabric.mod.json file. */
    public String[] authors;

    /** This field is automatically assigned from the meteor-client:color property in fabric.mod.json file. */

    public abstract void onInitialize();

    public void onRegisterCategories() {}
}