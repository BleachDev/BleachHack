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
		this.dir = Paths.get(Minecraft.getInstance().gameDir.getAbsolutePath().replaceAll(".$", ""), "bleach/");
		if(!dir.toFile().exists()) dir.toFile().mkdirs();
	}
	
	public Path getDir() {
		return dir;
	}
	
	public String readFile(String file) {
		try { return new String(Files.readAllBytes(Paths.get(dir.toString(), file)));
		} catch (IOException e) { System.out.println("Error Reading File"); } 
		
		return "";
	}
	
	public List<String> readFileLines(String file) {
		try { return Files.readAllLines(Paths.get(dir.toString(), file));
		} catch (IOException e) { System.out.println("Error Reading File"); } 
		
		return new ArrayList<String>();
	}
	
	public void createFile(String file, String content) {
		try { 
			if(Paths.get(dir.toString(), file).toFile().exists()) return;
			Files.createFile(Paths.get(dir.toString(), file));
			FileWriter writer = new FileWriter(Paths.get(dir.toString(), file).toFile());
			writer.write(content);
			writer.close();
		} catch (IOException e) { System.out.println("Error Writing File"); } 
	}
	
	public void rewriteFile(String file, String content) {
		try {
			FileWriter writer = new FileWriter(Paths.get(dir.toString(), file).toFile(), false);
			writer.write(content);
			writer.close();
		} catch (IOException e) { System.out.println("Error Writing File"); } 
	}
	
	public void appendFile(String file, String content) {
		try {
			FileWriter writer = new FileWriter(Paths.get(dir.toString(), file).toFile(), true);
			writer.write(content + "\n");
			writer.close();
		} catch (IOException e) { System.out.println("Error Writing File"); } 
	}
	
}
