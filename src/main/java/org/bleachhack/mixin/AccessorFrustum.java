/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.client.render.Frustum;
import net.minecraft.util.math.Vector4f;

@Mixin(Frustum.class)
public interface AccessorFrustum {
	
	@Accessor
	public abstract Vector4f[] getHomogeneousCoordinates();
	
	@Accessor
	public abstract void setHomogeneousCoordinates(Vector4f[] vector4f);
	
	@Accessor
	public abstract double getX();
	
	@Accessor
	public abstract void setX(double x);
	
	@Accessor
	public abstract double getY();
	
	@Accessor
	public abstract void setY(double y);
	
	@Accessor
	public abstract double getZ();
	
	@Accessor
	public abstract void setZ(double z);
}
