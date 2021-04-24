/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
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

import bleach.hack.BleachHack;
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
			BleachHack.logger.error("Error Reading File: " + stringsToPath(file));
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
			BleachHack.logger.error("Error Creating File: " + Arrays.toString(file));
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
			BleachHack.logger.error("Error Clearing/Creating File: " + Arrays.toString(file));
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
			BleachHack.logger.error("Error Appending File: " + Arrays.toString(file));
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
			BleachHack.logger.error("Error Deleting File: " + Arrays.toString(file));
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
