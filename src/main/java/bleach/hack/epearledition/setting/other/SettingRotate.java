package bleach.hack.epearledition.setting.other;

import bleach.hack.epearledition.setting.base.SettingMode;
import bleach.hack.epearledition.setting.base.SettingToggle;

public class SettingRotate extends SettingToggle {

    public SettingRotate(boolean state) {
        super("Rotate", state);
        children.add(new SettingMode("Mode", "Server", "Client").withDesc("How to rotate"));
    }

    public int getRotateMode() {
        return getChild(0).asMode().mode;
    }
}
