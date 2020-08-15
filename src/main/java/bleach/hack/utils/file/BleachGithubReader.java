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
package bleach.hack.utils.file;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class BleachGithubReader {

    private static URI url;

    static {
        //why java, why do i have to use a static block to set a varable, theres nothing wrong with that url
        try {
            url = new URI("https://raw.githubusercontent.com/BleachDrinker420/bleachhack-1.14/master/online/");
        } catch (URISyntaxException e) {
        }
    }

    public static List<String> readFileLines(String file) {
        List<String> st = new ArrayList<>();
        try {
            URL fileUrl = url.resolve(file).toURL();
            Scanner sc = new Scanner(fileUrl.openStream());
            while (sc.hasNextLine()) st.add(sc.nextLine());
            sc.close();
        } catch (IOException e) {
        }
        return st;
    }
}