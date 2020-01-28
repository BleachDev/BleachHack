package bleach.hack.gui.clickgui;

public class SettingBase {

	public SettingMode toMode() {
		try {
			return (SettingMode) this;
		}catch(Exception e) {
			System.out.println("Unable To Parse Setting To Mode: " + this);
			return new SettingMode("PARSING ERROR");
		}
	}
	
	public SettingToggle toToggle() {
		try {
			return (SettingToggle) this;
		}catch(Exception e) {
			System.out.println("Unable To Parse Setting To Toggle: " + this);
			return new SettingToggle("PARSING ERROR", false);
		}
	}
	
	public SettingSlider toSlider() {
		try {
			return (SettingSlider) this;
		}catch(Exception e) {
			System.out.println("Unable To Parse Setting To Slider: " + this);
			return new SettingSlider("PARSING ERROR", 0, 1, 0, 0);
		}
	}
}
