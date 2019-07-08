package bleach.hack.utils.file;

import java.util.List;

import bleach.hack.gui.clickgui.SettingBase;
import bleach.hack.gui.clickgui.SettingMode;
import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.gui.clickgui.SettingToggle;
import bleach.hack.module.Module;
import bleach.hack.module.ModuleManager;

public class BleachFileReader {

	private BleachFileMang fileMang = new BleachFileMang();
	
	public BleachFileReader() {
	}
	
	public void saveModules() {
		fileMang.createFile("modules.txt", "");
		fileMang.rewriteFile("modules.txt", "");
		
		for(Module m: ModuleManager.getModules()) {
			fileMang.appendFile("modules.txt", m.getName() + ":" + Boolean.toString(m.isToggled()));
		}
	}
	
	public void readModules() {
		List<String> lines = fileMang.readFileLines("modules.txt");
		
		for(Module m: ModuleManager.getModules()) {
			for(String s: lines) {
				String[] line = s.split(":");
				try {
					if(line[0].contains(m.getName()) && line[1].contains("true")) {
						m.setToggled(true);
						break;
					}
				}catch(Exception e) {}
			}
		}
	}
	
	public void saveSettings() {
		fileMang.createFile("settings.txt", "");
		fileMang.rewriteFile("settings.txt", "");
		
		for(Module m: ModuleManager.getModules()) {
			String line = m.getName();
			int count = 0;
			
			for(SettingBase set: m.getSettings()) {
				if(set instanceof SettingSlider) line += ":" + m.getSettings().get(count).toSlider().getValue();
				if(set instanceof SettingMode) line += ":" + m.getSettings().get(count).toMode().mode;
				if(set instanceof SettingToggle) line += ":" + m.getSettings().get(count).toToggle().state;
				count++;
			}
			
			fileMang.appendFile("settings.txt", line);
		}
	}
	
	public void readSettings() {
		List<String> lines = fileMang.readFileLines("settings.txt");
		
		for(Module m: ModuleManager.getModules()) {
			for(String s: lines) {
				String[] line = s.split(":");
				int count = 0;
				
				for(SettingBase set: m.getSettings()) {
					try {
						if(line[0].contains(m.getName())) {
							if(set instanceof SettingSlider) {
								m.getSettings().get(count).toSlider().value = Double.parseDouble(line[count+1]);}
							if(set instanceof SettingMode) {
								m.getSettings().get(count).toMode().mode = Integer.parseInt(line[count+1]);}
							if(set instanceof SettingToggle) {
								m.getSettings().get(count).toToggle().state = Boolean.parseBoolean(line[count+1]);}
						}
					}catch(Exception e) {}
					count++;
				}
			}
		}
	}
}
