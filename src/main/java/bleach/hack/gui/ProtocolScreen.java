/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/bleachhack-1.14/).
 * Copyright (c) 2019 Bleach.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package bleach.hack.gui;

import bleach.hack.utils.FabricReflect;
import net.minecraft.SharedConstants;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;

public class ProtocolScreen extends Screen {

	private TextFieldWidget nameField;
	private TextFieldWidget protocolField; // int
	private TextFieldWidget targetField;
	private TextFieldWidget packVerField; // int
	private MultiplayerScreen serverScreen;

	public ProtocolScreen(MultiplayerScreen serverScreen) {
		super(new LiteralText("Protocol Screen"));
		this.serverScreen = serverScreen;
	}

	public void init() {
		addButton(new ButtonWidget(width / 2 - 100, height / 2 + 50, 98, 20, new LiteralText("Update"), button -> {
			try {
				int i = Integer.parseInt(protocolField.getText());
				int i1 = Integer.parseInt(packVerField.getText());

				FabricReflect.writeField(SharedConstants.getGameVersion(), nameField.getText(), "field_16733", "name");
				FabricReflect.writeField(SharedConstants.getGameVersion(), i, "field_16735", "protocolVersion");
				FabricReflect.writeField(SharedConstants.getGameVersion(), i1, "field_16734", "packVersion");
				FabricReflect.writeField(SharedConstants.getGameVersion(), targetField.getText(), "field_16740", "releaseTarget");
				System.out.println("Set Protocol");
			} catch (Exception e) {
				e.printStackTrace();
			}
		}));
		addButton(new ButtonWidget(width / 2 + 2, height / 2 + 50, 98, 20, new LiteralText("Done"), button -> {
			client.openScreen(serverScreen);
		}));

		nameField = new TextFieldWidget(textRenderer, width / 2 - 98, height / 2 - 60, 196, 18, LiteralText.EMPTY);
		nameField.setText(SharedConstants.getGameVersion().getName());
		protocolField = new TextFieldWidget(textRenderer, width / 2 - 98, height / 2 - 35, 196, 18, LiteralText.EMPTY);
		protocolField.setText(SharedConstants.getGameVersion().getProtocolVersion() + "");
		targetField = new TextFieldWidget(textRenderer, width / 2 - 98, height / 2 - 10, 196, 18, LiteralText.EMPTY);
		targetField.setText(SharedConstants.getGameVersion().getReleaseTarget());
		packVerField = new TextFieldWidget(textRenderer, width / 2 - 98, height / 2 + 15, 196, 18, LiteralText.EMPTY);
		packVerField.setText(SharedConstants.getGameVersion().getPackVersion() + "");
		// ipField.changeFocus(true);
	}

	public void render(MatrixStack matrix, int mouseX, int mouseY, float delta) {
		renderBackground(matrix);
		drawStringWithShadow(matrix, textRenderer, "\u00a77Name:", width / 2 - 103 - textRenderer.getWidth("Name:"), height / 2 - 55, -1);
		drawStringWithShadow(matrix, textRenderer, "\u00a77Protocol:", width / 2 - 103 - textRenderer.getWidth("Protocol:"), height / 2 - 30, -1);
		drawStringWithShadow(matrix, textRenderer, "\u00a77Target Ver:", width / 2 - 103 - textRenderer.getWidth("Target Ver:"), height / 2 - 5, -1);
		drawStringWithShadow(matrix, textRenderer, "\u00a77Packet Ver:", width / 2 - 103 - textRenderer.getWidth("Packet Ver:"), height / 2 + 20, -1);
		nameField.render(matrix, mouseX, mouseY, delta);
		protocolField.render(matrix, mouseX, mouseY, delta);
		targetField.render(matrix, mouseX, mouseY, delta);
		packVerField.render(matrix, mouseX, mouseY, delta);

		super.render(matrix, mouseX, mouseY, delta);
	}

	public void onClose() {
		client.openScreen(serverScreen);
	}

	public boolean mouseClicked(double double_1, double double_2, int int_1) {
		if (double_1 >= nameField.x && double_1 <= nameField.x + nameField.getWidth()
		&& double_2 >= nameField.y && double_2 <= nameField.y + 18) {
			nameField.changeFocus(true);
			protocolField.setSelected(false);
			targetField.setSelected(false);
			packVerField.setSelected(false);
		}
		if (double_1 >= protocolField.x && double_1 <= protocolField.x + protocolField.getWidth()
		&& double_2 >= protocolField.y && double_2 <= protocolField.y + 18) {
			nameField.setSelected(false);
			protocolField.changeFocus(true);
			targetField.setSelected(false);
			packVerField.setSelected(false);
		}
		if (double_1 >= targetField.x && double_1 <= targetField.x + targetField.getWidth()
		&& double_2 >= targetField.y && double_2 <= targetField.y + 18) {
			nameField.setSelected(false);
			protocolField.setSelected(false);
			targetField.changeFocus(true);
			packVerField.setSelected(false);
		}
		if (double_1 >= packVerField.x && double_1 <= packVerField.x + packVerField.getWidth()
		&& double_2 >= packVerField.y && double_2 <= packVerField.y + 18) {
			nameField.setSelected(false);
			protocolField.setSelected(false);
			targetField.setSelected(false);
			packVerField.changeFocus(true);
		}
		return super.mouseClicked(double_1, double_2, int_1);
	}

	public boolean charTyped(char p_charTyped_1_, int p_charTyped_2_) {
		if (nameField.isFocused())
			nameField.charTyped(p_charTyped_1_, p_charTyped_2_);
		if (protocolField.isFocused())
			protocolField.charTyped(p_charTyped_1_, p_charTyped_2_);
		if (targetField.isFocused())
			targetField.charTyped(p_charTyped_1_, p_charTyped_2_);
		if (packVerField.isFocused())
			packVerField.charTyped(p_charTyped_1_, p_charTyped_2_);
		return super.charTyped(p_charTyped_1_, p_charTyped_2_);
	}

	public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
		if (nameField.isFocused())
			nameField.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
		if (protocolField.isFocused())
			protocolField.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
		if (targetField.isFocused())
			targetField.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
		if (packVerField.isFocused())
			packVerField.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
		return super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
	}

	public void tick() {
		nameField.tick();
		protocolField.tick();
		targetField.tick();
		packVerField.tick();
		super.tick();
	}
}
