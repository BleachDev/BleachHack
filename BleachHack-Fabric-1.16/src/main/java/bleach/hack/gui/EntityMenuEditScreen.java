package bleach.hack.gui;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.tuple.MutablePair;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import bleach.hack.gui.window.Window;
import bleach.hack.gui.window.WindowScreen;
import bleach.hack.util.PairList;
import bleach.hack.util.file.BleachFileHelper;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.MathHelper;

/**
 * @author <a href="https://github.com/lasnikprogram">Lasnik</a>
 */
public class EntityMenuEditScreen extends WindowScreen {

	private PairList<String, String> interactions;
	private String selectedEntry;
	private String hoverEntry;
	private String deleteEntry;

	private int scroll;
	private int scrollOffset;

	private boolean addEntry;

	private String insertString;

	private TextFieldWidget editNameField;
	private TextFieldWidget editValueField;

	public EntityMenuEditScreen(PairList<String, String> interactions) {
		super(new LiteralText("Interaction Edit Screen"));

		this.interactions = interactions;
	}

	@Override
	public void init() {
		super.init();

		clearWindows();
		addWindow(new Window(
				width / 4,
				height / 6,
				width - width / 4,
				height - height / 6,
				"Edit Interactions", new ItemStack(Items.OAK_SIGN)));

		if (editNameField == null) {
			editNameField = new TextFieldWidget(textRenderer, 0, 0, 1000, 16, LiteralText.EMPTY);
			editNameField.setMaxLength(32767);
		}

		if (editValueField == null) {
			editValueField = new TextFieldWidget(textRenderer, 0, 0, 1000, 16, LiteralText.EMPTY);
			editValueField.setMaxLength(32767);
		}
	}

	@Override
	public void render(MatrixStack matrix, int mouseX, int mouseY, float delta) {
		renderBackground(matrix);
		super.render(matrix, mouseX, mouseY, delta);
	}

	@Override
	public void onRenderWindow(MatrixStack matrix, int window, int mouseX, int mouseY) {
		super.onRenderWindow(matrix, window, mouseX, mouseY);

		if (window == 0) {
			int x = getWindow(0).x1;
			int y = getWindow(0).y1 + 12;
			int w = getWindow(0).x2 - getWindow(0).x1;
			int h = getWindow(0).y2 - getWindow(0).y1 - 13;

			hoverEntry = null;
			deleteEntry = null;
			scrollOffset = 0;
			addEntry = false;
			insertString = null;

			int seperator = (int) (x + w / 3.25);
			fill(matrix, seperator, y, seperator + 1, y + h, 0xff606090);

			textRenderer.drawWithShadow(matrix, "Interactions:", x + 6, y + 5, 0xffffff);

			boolean mouseOverAdd = mouseX >= seperator - 16 && mouseX <= seperator - 3 && mouseY >= y + 3 && mouseY <= y + 15;
			Window.fill(matrix, seperator - 16, y + 3, seperator - 3, y + 15,
					mouseOverAdd ? 0x4fb070f0 : 0x60606090);
			textRenderer.drawWithShadow(matrix, "\u00a7a+", seperator - 12, y + 5, 0xffffff);

			if (mouseOverAdd) {
				addEntry = true;
			}

			int maxEntries = (h - 33) / 17;
			int entries = 0;

			scroll = MathHelper.clamp(scroll, 0, interactions.size() - maxEntries);

			if (scroll > 0) {
				boolean mouseOver = mouseX >= x + 2 && mouseX <= seperator - 1 && mouseY >= y + 17 && mouseY <= y + 33;

				Window.fill(matrix, x + 3, y + 17, seperator - 2, y + 33,
						mouseOver ? 0x4fb070f0 : 0x50606090);
				drawCenteredString(matrix, textRenderer,
						"\u00a7a\u00a7l^", x + (seperator - x) / 2, y + 21, 0xffffff);

				entries++;
				if (mouseOver) {
					scrollOffset = -1;
				}
			}

			if (interactions.size() - maxEntries > 0 && scroll < interactions.size() - maxEntries) {
				boolean mouseOver = mouseX >= x + 2 && mouseX <= seperator - 1 && mouseY >= y + 17 + (maxEntries * 17) && mouseY <= y + 33 + (maxEntries * 17);

				Window.fill(matrix, x + 3, y + 17 + (maxEntries * 17), seperator - 2, y + 33 + (maxEntries * 17),
						mouseOver ? 0x4fb070f0 : 0x50606090);
				drawCenteredString(matrix, textRenderer,
						"\u00a7a\u00a7lv", x + (seperator - x) / 2, y + 21 + (maxEntries * 17), 0xffffff);

				maxEntries--;
				if (mouseOver) {
					scrollOffset = 1;
				}
			}

			int localScroll = scroll;
			for (String entry: interactions.getEntries()) {
				if (entries < localScroll) {
					localScroll--;
					continue;
				}

				int curY = y + 17 + entries * 17;
				boolean mouseOver = mouseX >= x + 2 && mouseX <= seperator - 1 && mouseY >= curY && mouseY <= curY + 16;

				Window.fill(matrix, x + 3, curY, seperator - 2, curY + 16,
						entry.equals(selectedEntry) ? 0x4f90f090 : mouseOver ? 0x4fb070f0 : 0x50606090);
				drawCenteredString(matrix, textRenderer,
						textRenderer.trimToWidth(entry, seperator - x - 6), x + (seperator - x) / 2, curY + 4, 0xffffff);

				if (mouseOver) {
					hoverEntry = entry;
				}

				entries++;
				if (entries > maxEntries) {
					break;
				}
			}

			if (selectedEntry != null) {
				textRenderer.drawWithShadow(matrix, "Name:", seperator + 8, y + 5, 0xffffff);

				editNameField.x = seperator + 8;
				editNameField.y = y + 18;
				editNameField.setWidth(w - (seperator - x) - 16);
				editNameField.render(matrix, mouseX, mouseY, client.getTickDelta());

				textRenderer.drawWithShadow(matrix, "Value:", seperator + 8, y + 45, 0xffffff);

				editValueField.x = seperator + 8;
				editValueField.y = y + 57;
				editValueField.setWidth(w - (seperator - x) - 16);
				editValueField.render(matrix, mouseX, mouseY, client.getTickDelta());

				if (!selectedEntry.equals(editNameField.getText())) {
					MutablePair<String, String> pair = interactions.getPair(selectedEntry);
					selectedEntry = editNameField.getText();
					pair.setLeft(selectedEntry);
				}

				if (!interactions.getValue(selectedEntry).equals(editValueField.getText())) {
					interactions.getPair(selectedEntry).setRight(editValueField.getText());
				}

				textRenderer.drawWithShadow(matrix, "Insert:", seperator + 8, y + 85, 0xffffff);

				int line = 0;
				int curX = 0;
				for (String insert: new String[] { "%name%", "%uuid%", "%health%", "%x%", "%y%", "%z%"}) {
					int textLen = textRenderer.getWidth(insert);
					
					if (seperator + 9 + curX + textLen > x + w) {
						line++;
						curX = 0;
					}

					boolean mouseOverInsert = mouseX >= seperator + 7 + curX && mouseX <= seperator + 10 + curX + textLen && mouseY >= y + 97 + line * 14 && mouseY <= y + 108 + line * 14;
					fill(matrix, seperator + 7 + curX, y + 97 + line * 14, seperator + 10 + curX + textLen, y + 108 + line * 14, mouseOverInsert ? 0x9f6060b0 : 0x9f8070b0);
					textRenderer.drawWithShadow(matrix, insert, seperator + 9 + curX, y + 99 + line * 14, 0xffffff);
					
					if (mouseOverInsert) {
						insertString = insert;
					}
					
					curX += textLen + 7;
				}

				boolean mouseOverDelete = mouseX >= x + w - 70 && mouseX <= x + w - 5 && mouseY >= y + h - 22 && mouseY <= y + h - 4;
				Window.fill(matrix, x + w - 70, y + h - 22, x + w - 5, y + h - 4, 0x60e05050, 0x60c07070, mouseOverDelete ? 0x20e05050 : 0x10e07070);
				drawCenteredString(matrix, textRenderer, "Delete", x + w - 37, y + h - 17, 0xf0f0f0);

				if (mouseOverDelete) {
					deleteEntry = selectedEntry;
				}
			}
		}
	}

	@Override
	public void onClose() {
		JsonObject json = new JsonObject();
		for (MutablePair<String, String> entry: interactions) {
			json.add(entry.getLeft(), new JsonPrimitive(entry.getRight()));
		}

		BleachFileHelper.saveMiscSetting("entityMenu", json);

		super.onClose();
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		editNameField.mouseClicked(mouseX, mouseY, button);
		editValueField.mouseClicked(mouseX, mouseY, button);

		if (hoverEntry != null && interactions.containsKey(hoverEntry)) {
			selectedEntry = hoverEntry;
			hoverEntry = null;

			editNameField.setText(selectedEntry);
			editValueField.setText(interactions.getValue(selectedEntry));
		}

		if (deleteEntry != null && interactions.containsKey(deleteEntry)) {
			interactions.remove(interactions.getPair(deleteEntry));
			deleteEntry = null;
			selectedEntry = null;
		}

		if (scrollOffset != 0) {
			scroll += scrollOffset;
			scrollOffset = 0;
		}

		if (addEntry) {
			interactions.add(new MutablePair<>(RandomStringUtils.randomAlphabetic(6), "bruh"));
			addEntry = false;
		}

		if (insertString != null) {
			editValueField.write(insertString);
			insertString = null;
		}

		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public void tick() {
		editNameField.tick();
		editValueField.tick();
	}

	@Override
	public boolean charTyped(char chr, int modifiers) {
		if (editNameField.isFocused()) editNameField.charTyped(chr, modifiers);
		if (editValueField.isFocused()) editValueField.charTyped(chr, modifiers);

		return super.charTyped(chr, modifiers);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (editNameField.isFocused()) editNameField.keyPressed(keyCode, scanCode, modifiers);
		if (editValueField.isFocused()) editValueField.keyPressed(keyCode, scanCode, modifiers);

		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean isPauseScreen() {
		return false;
	}
}
