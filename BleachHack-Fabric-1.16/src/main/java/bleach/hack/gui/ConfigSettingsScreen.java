package bleach.hack.gui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import bleach.hack.gui.widget.BleachCheckbox;
import bleach.hack.gui.window.AbstractWindowScreen;
import bleach.hack.gui.window.Window;
import bleach.hack.utils.file.BleachFileHelper;
import bleach.hack.utils.file.BleachFileMang;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.HoverEvent.Action;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Util;

public class ConfigSettingsScreen extends AbstractWindowScreen {
	
	private BleachCheckbox checkSchedule;
	private BleachCheckbox checkOldModFormat;
	private BleachCheckbox mergedSchedule;

	public ConfigSettingsScreen() {
		super(new LiteralText("Config Settings"));
	}

	public void init() {
		windows.clear();
		Window w = new Window(
				width / 4, height / 4 - 10,
				width / 4 + width / 2, height / 4 + height / 2,
				"Config Settings", new ItemStack(Items.REDSTONE));
		windows.add(w);
		
		checkSchedule = new BleachCheckbox(w.x1 + (w.x2 - w.x1) / 2 - 50, w.y1 + 15,
				new LiteralText("Schedule Config Save").setStyle(LiteralText.EMPTY.getStyle().setHoverEvent(
						new HoverEvent(Action.SHOW_TEXT, new LiteralText("Schedules the config save only when a module is changed instead of every 5 seconds")))),
				BleachFileHelper.SCHEDULE_SAVE);
		checkOldModFormat = new BleachCheckbox(w.x1 + (w.x2 - w.x1) / 2 - 50, w.y1 + 30,
				new LiteralText("Use Old Config Format").setStyle(LiteralText.EMPTY.getStyle().setHoverEvent(
						new HoverEvent(Action.SHOW_TEXT, new LiteralText("Uses the old B13 save format to save modules")))),
				BleachFileHelper.OLD_MODULE_FORMAT);
		mergedSchedule = new BleachCheckbox(w.x1 + (w.x2 - w.x1) / 2 - 35, w.y1 + 45,
				new LiteralText("Merge Config Files").setStyle(LiteralText.EMPTY.getStyle().setHoverEvent(
						new HoverEvent(Action.SHOW_TEXT, new LiteralText("Merges all the binds, modules and settings into 1 file")))),
				BleachFileHelper.MERGE_OLD_MODULE_FORMAT);
	}
	
	public void render(MatrixStack matrix, int mouseX, int mouseY, float delta) {
		renderBackground(matrix);
		super.render(matrix, mouseX, mouseY, delta);
	}

	public void onRenderWindow(MatrixStack matrix, int window, int mX, int mY) {
		super.onRenderWindow(matrix, window, mX, mY);

		if (window == 0) {
			List<String> info = new ArrayList<>(Arrays.asList(
					"Modules:",
					//"",
					"Clickgui:",
					"\u00a77clickgui.txt",
					"Friends:",
					"\u00a77friends.txt",
					"",
					"\u00a7e\u00a7o\u00a7n[Open Folder]"));
			
			if (!checkOldModFormat.checked) {
				info.add(1, "\u00a77modules.json");
			} else if (mergedSchedule.checked) {
				info.add(1, "\u00a77modules_merged.txt");
			} else {
				info.addAll(1, Arrays.asList(
						"\u00a77binds.txt",
						"\u00a77modules.txt",
						"\u00a77settings.txt"));
			}
			
			int toRemove = windows.get(0).y2 - 12;
			for (int i = info.size() - 1; i > -1; i--) {
				textRenderer.drawWithShadow(matrix, new LiteralText(info.get(i)), windows.get(0).x1 + 2, toRemove, -1);
				toRemove -= 10;
			}
			
			checkSchedule.x = windows.get(0).x1 + (windows.get(0).x2 - windows.get(0).x1) / 2 - 60;
			checkSchedule.y = windows.get(0).y1 + 15;
			
			checkOldModFormat.x = windows.get(0).x1 + (windows.get(0).x2 - windows.get(0).x1) / 2 - 60;
			checkOldModFormat.y = windows.get(0).y1 + 30;
			
			mergedSchedule.x = windows.get(0).x1 + (windows.get(0).x2 - windows.get(0).x1) / 2 - 45;
			mergedSchedule.y = windows.get(0).y1 + 45;
			
			checkSchedule.render(matrix, mX, mY, client.getTickDelta());
			checkOldModFormat.render(matrix, mX, mY, client.getTickDelta());
			mergedSchedule.render(matrix, mX, mY, client.getTickDelta());
		}
	}
	
	public boolean mouseClicked(double double_1, double double_2, int int_1) {
		if (checkSchedule.isMouseOver(double_1, double_2)) {
			checkSchedule.onPress();
			BleachFileHelper.SCHEDULE_SAVE = checkSchedule.checked;
			BleachFileHelper.saveMiscSetting("configSchedule", Boolean.toString(checkSchedule.checked));
		}
		
		if (checkOldModFormat.isMouseOver(double_1, double_2)) {
			checkOldModFormat.onPress();
			BleachFileHelper.OLD_MODULE_FORMAT = checkOldModFormat.checked;
			BleachFileHelper.saveMiscSetting("configOldModFormat", Boolean.toString(checkOldModFormat.checked));
		}
		
		if (mergedSchedule.isMouseOver(double_1, double_2)) {
			mergedSchedule.onPress();
			BleachFileHelper.MERGE_OLD_MODULE_FORMAT = mergedSchedule.checked;
			BleachFileHelper.saveMiscSetting("configMergeOldModFormat", Boolean.toString(mergedSchedule.checked));
		}
		
		if (double_1 >= windows.get(0).x1 && double_1 <= windows.get(0).x1 + 4 + textRenderer.getWidth("[Open Folder]")
				&& double_2 >= windows.get(0).y2 - 12 && double_2 <= windows.get(0).y2) {
			Util.getOperatingSystem().open(BleachFileMang.getDir().toFile());
		}
		
		return super.mouseClicked(double_1, double_2, int_1);
	}
}
