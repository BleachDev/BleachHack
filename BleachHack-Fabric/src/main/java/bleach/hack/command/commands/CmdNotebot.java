package bleach.hack.command.commands;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;

import bleach.hack.command.Command;
import bleach.hack.module.mods.Notebot;
import bleach.hack.utils.BleachLogger;
import bleach.hack.utils.file.BleachFileMang;
import net.minecraft.block.enums.Instrument;
import net.minecraft.util.SystemUtil;

public class CmdNotebot extends Command {
	
	@Override
	public String getAlias() {
		return "notebot";
	}

	@Override
	public String getDescription() {
		return "Notebot Stuff, load loads a file, list lists files, req show required noteblocks for a file, download download songs and tutorial opens the tutorial";
	}

	@Override
	public String getSyntax() {
		return ".notebot load [file] | .notebot list | .notebot req [file] | .notebot download | .notebot tutorial";
	}

	@Override
	public void onCommand(String command, String[] args) throws Exception {
		if(args[0].equalsIgnoreCase("load")) {
			Notebot.filePath = args[1];
			BleachLogger.infoMessage("Set file to: " + args[1]);
			
		}else if(args[0].equalsIgnoreCase("list")) {
			Stream<Path> paths = Files.walk(BleachFileMang.getDir().resolve("notebot"));
			BleachLogger.infoMessage("Files:");
			paths.forEach(p -> BleachLogger.infoMessage(p.getFileName().toString()));
			paths.close();
			
		}else if(args[0].equalsIgnoreCase("req")) {
			List<List<Integer>> n = new ArrayList<>();
			List<List<Integer>> t = new ArrayList<>();
			BleachFileMang.createFile("notebot", args[1]);
			List<String> lines = BleachFileMang.readFileLines("notebot", args[1])
					.stream().filter(s -> !(s.isEmpty() || s.startsWith("//") || s.startsWith(";"))).collect(Collectors.toList());
			for(String s: lines) s = s.replaceAll(" ", " ");

			for(String s: lines) {
				String[] s1 = s.split(":");
				n.add(Arrays.asList(Integer.parseInt(s1[0]), Integer.parseInt(s1[1]), Integer.parseInt(s1[2])));
			}
			
			List<List<String>> neededTunes = new ArrayList<>();
			for(String s: lines) {
				List<String> strings = Arrays.asList(s.split(":"));
				if(!neededTunes.contains(Arrays.asList(strings.get(1), strings.get(2)))) {
					neededTunes.add(Arrays.asList(strings.get(1), strings.get(2)));
				}
			}
			for(List<String> s: neededTunes) t.add(Arrays.asList(Integer.parseInt(s.get(0)), Integer.parseInt(s.get(1))));
			
			List<Integer> t1 = Arrays.asList(0,0,0,0,0,0,0,0,0,0,0,0,0,0,0);
			String out = "";
			for(List<Integer> i: t) t1.set(i.get(1), t1.get(i.get(1)) + 1);
			for(int i = 0; i < t1.size(); i++) {
				if(t1.get(i) != 0) out += "§d[" + t1.get(i) + "x " + Instrument.values()[i].asString() + "] ";
			}
			BleachLogger.infoMessage("Requirements: " + out);
			
		}else if(args[0].equalsIgnoreCase("download")) {
			BleachLogger.infoMessage("Downloading Songs.. Screen might lag");
			
			FileUtils.copyURLToFile(
					  new URL("https://github.com/BleachDrinker420/bleachhack-1.14/raw/master/online/notebot/songs.zip"), 
					  BleachFileMang.getDir().resolve("notebot").resolve("songs.zip").toFile());
			
			ZipFile zip = new ZipFile(BleachFileMang.getDir().resolve("notebot").resolve("songs.zip").toFile());
			Enumeration<? extends ZipEntry> files = zip.entries();
			int count = 0;
			while (files.hasMoreElements()) {
				count++;
				ZipEntry file = files.nextElement();
				File outFile = BleachFileMang.getDir().resolve("notebot").resolve(file.getName()).toFile();
				if (file.isDirectory()) outFile.mkdirs();
			    else {
			        outFile.getParentFile().mkdirs();
			        InputStream in = zip.getInputStream(file);
			        OutputStream out = new FileOutputStream(outFile);
			        IOUtils.copy(in, out);
			        IOUtils.closeQuietly(in);
			        out.close();
			    }
			}
			zip.close();
			Files.deleteIfExists(BleachFileMang.getDir().resolve("notebot").resolve("songs.zip"));
			
			BleachLogger.infoMessage("Downloaded " + count + " Songs");
		}else if(args[0].equalsIgnoreCase("tutorial")) {
			SystemUtil.getOperatingSystem().open(new URI("https://youtu.be/7gxKnCtW2_4"));
		}else {
			BleachLogger.errorMessage("Invalid command usage!");
			BleachLogger.infoMessage(getSyntax());
		}
	}

}
