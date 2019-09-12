package bleach.hack.utils.file;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.MinecraftClient;

public class BleachFileMang {

	private static Path dir;
	
	public static void init() {
		dir = Paths.get(MinecraftClient.getInstance().runDirectory.getPath(), "bleach/");
		if(!dir.toFile().exists()) dir.toFile().mkdirs();
	}
	
	/** Gets the bleach directory in your minecraft folder. **/
	public static Path getDir() {
		return dir;
	}
	
	/** Reads a file and returns a list of the lines. **/
	public static List<String> readFileLines(String... file) {
		try { return Files.readAllLines(stringsToPath(file));
		} catch (IOException e) { System.out.println("Error Reading File: " + stringsToPath(file)); e.printStackTrace(); } 
		
		return new ArrayList<String>();
	}
	
	/** Creates a file, doesn't do anything if the file already exists. **/
	public static void createFile(String... file) {
		try { 
			if(fileExists(file)) return;
			dir.toFile().mkdirs();
			Files.createFile(stringsToPath(file));
		} catch (IOException e) { System.out.println("Error Creating File: " + file); e.printStackTrace(); } 
	}
	
	/** Creates a file, clears it if it already exists **/
	public static void createEmptyFile(String... file) {
		try { 
			dir.toFile().mkdirs();
			if(!fileExists(file)) Files.createFile(stringsToPath(file));
			FileWriter writer = new FileWriter(stringsToPath(file).toFile());
			writer.write("");
			writer.close();
		} catch (IOException e) { System.out.println("Error Clearing/Creating File: " + file); e.printStackTrace(); } 
	}
	
	/** Adds a line to a file. **/
	public static void appendFile(String content, String... file) {
		try {
			FileWriter writer = new FileWriter(stringsToPath(file).toFile(), true);
			writer.write(content + "\n");
			writer.close();
		} catch (IOException e) { System.out.println("Error Appending File: " + file); e.printStackTrace(); } 
	}
	
	/** Returns true if a file exists, returns false otherwise **/
	public static boolean fileExists(String... file) {
		try { return stringsToPath(file).toFile().exists();
		} catch (Exception e) { return false; }
	}
	
	public static Path stringsToPath(String... strings) {
		Path path = dir;
		for(String s: strings) path = path.resolve(s);
		return path;
	}
	
}
