/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client/).
 * Copyright (c) 2021 Meteor Development.
 */

package org.bleachhack.util.meteor.misc;

public interface ICopyable<T extends ICopyable<T>> {
    T set(T value);

    T copy();
}