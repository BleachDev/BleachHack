package bleach.hack.setting.other;

import bleach.hack.setting.base.SettingBase;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.setting.base.SettingToggle;

public class SettingRotate extends SettingToggle {

	public SettingRotate(boolean state) {
		super("Rotate", state);
		children.add(new SettingMode("Mode", "Server", "Client").withDesc("How to rotate"));
	}

	public SettingBase getChild(int c) {
		return children.get(c + 1);
	}
	
	public int getRotateMode() {
		return children.get(0).asMode().mode;
	}
}
