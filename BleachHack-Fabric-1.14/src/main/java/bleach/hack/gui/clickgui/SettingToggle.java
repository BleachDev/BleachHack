package bleach.hack.gui.clickgui;

public class SettingToggle extends SettingBase {

	public boolean state;
	public String text;
	
	public SettingToggle(String text, boolean state) {
		this.state = state;
		this.text = text;
	}
}
