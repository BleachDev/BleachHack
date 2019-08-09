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
		this.dir = Paths.get(MinecraftClient.getInstance().runDirectory.getAbsolutePath(), "bleach/");
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
	
	/** Returns true if a file exists, returns false otherwise **/
	public boolean fileExists(String file) {
		try { return Paths.get(dir.toString(), file).toFile().exists();
		} catch (Exception e) { return false; }
	}
	
}
