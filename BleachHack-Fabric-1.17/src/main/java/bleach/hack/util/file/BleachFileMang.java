/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
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

import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.client.MinecraftClient;

public class BleachFileMang {

	private static Path dir;

	public static void init() {
		dir = Paths.get(MinecraftClient.getInstance().runDirectory.getPath(), "bleach/");
		if (!dir.toFile().exists()) {
			dir.toFile().mkdirs();
		}
	}

	/** Gets the bleach directory in your minecraft folder. **/
	public static Path getDir() {
		return dir;
	}

	/** Reads a file and returns a list of the lines. **/
	public static List<String> readFileLines(String... file) {
		try {
			return Files.readAllLines(stringsToPath(file));
		} catch (NoSuchFileException e) {

		} catch (Exception e) {
			System.out.println("Error Reading File: " + stringsToPath(file));
			e.printStackTrace();
		}

		return new ArrayList<>();
	}

	/** Creates a file, doesn't do anything if the file already exists. **/
	public static void createFile(String... file) {
		try {
			if (!fileExists(file)) {
				stringsToPath(file).getParent().toFile().mkdirs();
				Files.createFile(stringsToPath(file));
			}
		} catch (Exception e) {
			System.out.println("Error Creating File: " + Arrays.toString(file));
			e.printStackTrace();
		}
	}

	/** Creates a file, clears it if it already exists **/
	public static void createEmptyFile(String... file) {
		try {
			createFile(file);

			FileWriter writer = new FileWriter(stringsToPath(file).toFile());
			writer.write("");
			writer.close();
		} catch (Exception e) {
			System.out.println("Error Clearing/Creating File: " + Arrays.toString(file));
			e.printStackTrace();
		}
	}

	/** Adds a line to a file. **/
	public static void appendFile(String content, String... file) {
		try {
			String fileContent = new String(Files.readAllBytes(stringsToPath(file)));
			FileWriter writer = new FileWriter(stringsToPath(file).toFile(), true);
			writer.write(
					(fileContent.endsWith("\n") || !fileContent.contains("\n") ? "" : "\n")
					+ content
					+ (content.endsWith("\n") ? "" : "\n"));
			writer.close();
		} catch (Exception e) {
			System.out.println("Error Appending File: " + Arrays.toString(file));
			e.printStackTrace();
		}
	}

	/** Returns true if a file exists, returns false otherwise **/
	public static boolean fileExists(String... file) {
		try {
			return stringsToPath(file).toFile().exists();
		} catch (Exception e) {
			return false;
		}
	}

	/** Deletes a file if it exists. **/
	public static void deleteFile(String... file) {
		try {
			Files.deleteIfExists(stringsToPath(file));
		} catch (Exception e) {
			System.out.println("Error Deleting File: " + Arrays.toString(file));
			e.printStackTrace();
		}
	}

	/**
	 * Gets a file by walking down all of the parameters (starts at
	 * .minecraft/bleach/).
	 **/
	public static Path stringsToPath(String... strings) {
		Path path = dir;
		for (String s : strings)
			path = path.resolve(s);
		return path;
	}

}
