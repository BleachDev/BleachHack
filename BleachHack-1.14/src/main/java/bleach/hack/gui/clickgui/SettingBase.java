package bleach.hack.gui.clickgui;

public class SettingBase {

	public SettingMode toSettingMode() {
		try {
			return (SettingMode) this;
		}catch(Exception e){
			System.out.println("Unable To Parse Setting");
			return new SettingMode(new String[] {""}, "PARSING ERROR");
		}
	}
	
	public SettingToggle toSettingToggle() {
		try {
			return (SettingToggle) this;
		}catch(Exception e){
			System.out.println("Unable To Parse Setting");
			return new SettingToggle(false, "PARSING ERROR");
		}
	}
}
