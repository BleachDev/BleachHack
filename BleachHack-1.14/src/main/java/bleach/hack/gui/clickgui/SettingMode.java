package bleach.hack.gui.clickgui;

public class SettingMode extends SettingBase {

	public String[] modes;
	public int mode;
	public String text;
	
	public SettingMode(String[] modes, String text) {
		this.modes = modes;
		this.text = text;
	}
	
	public int getNextMode() {
		if(mode + 1 >= modes.length) {
			return 0;
		}
		return mode+1;
	}
}
