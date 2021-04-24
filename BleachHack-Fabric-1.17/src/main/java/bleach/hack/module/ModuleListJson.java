/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.module;

import java.util.List;

import com.google.gson.annotations.SerializedName;

public class ModuleListJson {

	@SerializedName("package")
	private String packageName;

	@SerializedName("modules")
	private List<String> modules;

	public String getPackage() {
		return this.packageName;
	}

	public List<String> getModules() {
		return this.modules;
	}
}
