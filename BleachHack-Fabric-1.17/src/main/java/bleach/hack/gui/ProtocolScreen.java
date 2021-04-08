/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
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

import com.mojang.bridge.game.PackType;

import bleach.hack.util.FabricReflect;
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
		super.init();

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
		packVerField.setText(SharedConstants.getGameVersion().getPackVersion(PackType.DATA) + "");
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

	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (mouseX >= nameField.x && mouseX <= nameField.x + nameField.getWidth() && mouseY >= nameField.y && mouseY <= nameField.y + 18) {
			nameField.setTextFieldFocused(true);
			protocolField.setTextFieldFocused(false);
			targetField.setTextFieldFocused(false);
			packVerField.setTextFieldFocused(false);
		}
		if (mouseX >= protocolField.x && mouseX <= protocolField.x + protocolField.getWidth() && mouseY >= protocolField.y && mouseY <= protocolField.y + 18) {
			nameField.setTextFieldFocused(false);
			protocolField.setTextFieldFocused(true);
			targetField.setTextFieldFocused(false);
			packVerField.setTextFieldFocused(false);
		}
		if (mouseX >= targetField.x && mouseX <= targetField.x + targetField.getWidth() && mouseY >= targetField.y && mouseY <= targetField.y + 18) {
			nameField.setTextFieldFocused(false);
			protocolField.setTextFieldFocused(false);
			targetField.setTextFieldFocused(true);
			packVerField.setTextFieldFocused(false);
		}
		if (mouseX >= packVerField.x && mouseX <= packVerField.x + packVerField.getWidth() && mouseY >= packVerField.y && mouseY <= packVerField.y + 18) {
			nameField.setTextFieldFocused(false);
			protocolField.setTextFieldFocused(false);
			targetField.setTextFieldFocused(false);
			packVerField.setTextFieldFocused(true);
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	public boolean charTyped(char chr, int modifiers) {
		if (nameField.isFocused())
			nameField.charTyped(chr, modifiers);
		if (protocolField.isFocused())
			protocolField.charTyped(chr, modifiers);
		if (targetField.isFocused())
			targetField.charTyped(chr, modifiers);
		if (packVerField.isFocused())
			packVerField.charTyped(chr, modifiers);
		return super.charTyped(chr, modifiers);
	}

	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (nameField.isFocused())
			nameField.keyPressed(keyCode, scanCode, modifiers);
		if (protocolField.isFocused())
			protocolField.keyPressed(keyCode, scanCode, modifiers);
		if (targetField.isFocused())
			targetField.keyPressed(keyCode, scanCode, modifiers);
		if (packVerField.isFocused())
			packVerField.keyPressed(keyCode, scanCode, modifiers);
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	public void tick() {
		nameField.tick();
		protocolField.tick();
		targetField.tick();
		packVerField.tick();
		super.tick();
	}
}
