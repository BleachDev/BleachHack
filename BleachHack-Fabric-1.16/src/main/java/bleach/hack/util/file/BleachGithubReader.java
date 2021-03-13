/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/bleachhack-1.14/).
 * Copyright (c) 2019 Bleach.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package bleach.hack.util.file;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.google.common.io.Resources;

public class BleachGithubReader {

	private static URI url = URI.create("https://raw.githubusercontent.com/BleachDrinker420/bleachhack-1.14/master/online/");

	public static List<String> readFileLines(String file) {
		List<String> st = new ArrayList<>();
		try {
			return Arrays.asList(Resources.toString(url.resolve(file).toURL(), StandardCharsets.UTF_8).replace("\r", "").split("\n"));
		} catch (IOException e) {
		}
		return st;
	}
}