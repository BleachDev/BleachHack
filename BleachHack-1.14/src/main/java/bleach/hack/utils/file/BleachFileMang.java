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

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;

public class BleachFileMang {

	private Path dir;
	
	public BleachFileMang() {
		this.dir = Paths.get(Minecraft.getInstance().gameDir.getAbsolutePath(), "bleach/");
		if(!dir.toFile().exists()) dir.toFile().mkdirs();
	}
	
	/** Gets the bleach directory in your minecraft folder. **/
	public Path getDir() {
		return dir;
	}
	
	/** Reads a file and returns a list of the lines. **/
	public List<String> readFileLines(String file) {
		try { return Files.readAllLines(Paths.get(dir.toString(), file));
		} catch (IOException e) { System.out.println("Error Reading File"); } 
		
		return new ArrayList<String>();
	}
	
	/** Creates a file, doesn't do anything if the file already exists. **/
	public void createFile(String file, String content) {
		try { 
			if(Paths.get(dir.toString(), file).toFile().exists()) return;
			dir.toFile().mkdirs();
			Files.createFile(Paths.get(dir.toString(), file));
			FileWriter writer = new FileWriter(Paths.get(dir.toString(), file).toFile());
			writer.write(content);
			writer.close();
		} catch (IOException e) { System.out.println("Error Writing File"); } 
	}
	
	/** Creates a file, clears it if it already exists **/
	public void createEmptyFile(String file) {
		try { 
			dir.toFile().mkdirs();
			if(!Paths.get(dir.toString(), file).toFile().exists()) {
				Files.createFile(Paths.get(dir.toString(), file));
			}
			FileWriter writer = new FileWriter(Paths.get(dir.toString(), file).toFile());
			writer.write("");
			writer.close();
		} catch (IOException e) { System.out.println("Error Writing File"); } 
	}
	
	/** Adds a line to a file. **/
	public void appendFile(String file, String content) {
		try {
			FileWriter writer = new FileWriter(Paths.get(dir.toString(), file).toFile(), true);
			writer.write(content + "\n");
			writer.close();
		} catch (IOException e) { System.out.println("Error Writing File"); } 
	}
	
}
