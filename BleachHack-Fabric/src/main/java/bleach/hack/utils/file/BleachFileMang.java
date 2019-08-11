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

	private Path dir;
	
	public BleachFileMang() {
		this.dir = Paths.get(MinecraftClient.getInstance().runDirectory.getPath(), "bleach/");
		if(!dir.toFile().exists()) dir.toFile().mkdirs();
	}
	
	/** Gets the bleach directory in your minecraft folder. **/
	public Path getDir() {
		return dir;
	}
	
	/** Reads a file and returns a list of the lines. **/
	public List<String> readFileLines(Path file) {
		try { return Files.readAllLines(Paths.get(dir.toString(), file.toFile().getPath()));
		} catch (IOException e) { System.out.println("Error Reading File: " + file); e.printStackTrace(); } 
		
		return new ArrayList<String>();
	}
	
	/** Creates a file, doesn't do anything if the file already exists. **/
	public void createFile(Path file, String content) {
		try { 
			if(fileExists(file)) return;
			dir.toFile().mkdirs();
			Files.createFile(Paths.get(dir.toString(), file.toFile().getPath()));
			FileWriter writer = new FileWriter(Paths.get(dir.toString(), file.toFile().getPath()).toFile());
			writer.write(content);
			writer.close();
		} catch (IOException e) { System.out.println("Error Creating File: " + file); e.printStackTrace(); } 
	}
	
	/** Creates a file, clears it if it already exists **/
	public void createEmptyFile(Path file) {
		try { 
			dir.toFile().mkdirs();
			if(!fileExists(file)) {
				Files.createFile(Paths.get(dir.toString(), file.toFile().getPath()));
			}
			FileWriter writer = new FileWriter(Paths.get(dir.toString(), file.toFile().getPath()).toFile());
			writer.write("");
			writer.close();
		} catch (IOException e) { System.out.println("Error Clearing/Creating File: " + file); e.printStackTrace(); } 
	}
	
	/** Adds a line to a file. **/
	public void appendFile(Path file, String content) {
		try {
			FileWriter writer = new FileWriter(Paths.get(dir.toString(), file.toFile().getPath()).toFile(), true);
			writer.write(content + "\n");
			writer.close();
		} catch (IOException e) { System.out.println("Error Appending File: " + file); e.printStackTrace(); } 
	}
	
	/** Returns true if a file exists, returns false otherwise **/
	public boolean fileExists(Path file) {
		try { return Paths.get(dir.toString(), file.toFile().getPath()).toFile().exists();
		} catch (Exception e) { return false; }
	}
	
}
