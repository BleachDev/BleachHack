/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.util.shader;

// all my homies hate importing resources from files
public class StaticShaders {
	
	/** Entity outline shader without any blurring **/
	public static final String OUTLINE_SHADER = "{\"targets\":[\"swap\",\"final\"],\"passes\":[{\"name\":\"entity_outline\",\"intarget\":\"final\",\"outtarget\":\"swap\"},{\"name\":\"blit\",\"intarget\":\"swap\",\"outtarget\":\"final\"}]}";
	
	/** Entity outline with blur (Format: %1 = Range, %2 = Blur range) **/
	public static final String MC_SHADER_UNFOMATTED = "{\"targets\":[\"swap\",\"final\"],\"passes\":[{\"name\":\"entity_outline\",\"intarget\":\"final\",\"outtarget\":\"swap\"},{\"name\":\"blur\",\"intarget\":\"swap\",\"outtarget\":\"final\",\"uniforms\":[{\"name\":\"BlurDir\",\"values\":[%2,0]},{\"name\":\"Radius\",\"values\":[%1]}]},{\"name\":\"blur\",\"intarget\":\"final\",\"outtarget\":\"swap\",\"uniforms\":[{\"name\":\"BlurDir\",\"values\":[0,%2]},{\"name\":\"Radius\",\"values\":[%1]}]},{\"name\":\"blit\",\"intarget\":\"swap\",\"outtarget\":\"final\"}]}";

}
