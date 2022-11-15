/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDev/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package org.bleachhack.gui;

import org.apache.commons.lang3.math.NumberUtils;

import com.mojang.bridge.game.PackType;

import net.minecraft.MinecraftVersion;
import net.minecraft.SharedConstants;
import net.minecraft.client.ClientBrandRetriever;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;


public class ProtocolScreen extends Screen {
	
	public static String BRAND = null;

	private TextFieldWidget versionField;
	private TextFieldWidget protocolField; // int
	private TextFieldWidget packVerField; // int
	private TextFieldWidget brandField;
	private ButtonWidget addButton;
	private Screen parent;

	public ProtocolScreen(Screen parent) {
		super(Text.literal("Protocol Screen"));
		this.parent = parent;
	}

	public void init() {
		super.init();

		addButton = addDrawableChild(new ButtonWidget(width / 2 - 100, height / 2 + 50, 196, 20, Text.literal("Done"), button -> {
			int i = Integer.parseInt(protocolField.getText());
			int i1 = Integer.parseInt(packVerField.getText());

			MinecraftVersion version = (MinecraftVersion) SharedConstants.getGameVersion();
			version.name = versionField.getText();
			version.releaseTarget = versionField.getText();
			version.protocolVersion =  i;
			version.dataPackVersion = i1;
			BRAND = brandField.getText();

			close();
		}));

		addDrawableChild(new ButtonWidget(width / 2 - 100, height / 2 + 73, 196, 20, Text.literal("Cancel"),
				button -> close()));

		versionField = addDrawableChild(new TextFieldWidget(textRenderer, width / 2 - 98, height / 2 - 60, 196, 18, Text.empty()));
		versionField.setText(SharedConstants.getGameVersion().getName());

		protocolField = addDrawableChild(new TextFieldWidget(textRenderer, width / 2 - 98, height / 2 - 35, 196, 18, Text.empty()));
		protocolField.setText(Integer.toString(SharedConstants.getGameVersion().getProtocolVersion()));
		protocolField.setChangedListener(text -> updateAddButton());

		packVerField = addDrawableChild(new TextFieldWidget(textRenderer, width / 2 - 98, height / 2 - 10, 196, 18, Text.empty()));
		packVerField.setText(Integer.toString(SharedConstants.getGameVersion().getPackVersion(PackType.DATA)));
		packVerField.setChangedListener(text -> updateAddButton());
		
		brandField = addDrawableChild(new TextFieldWidget(textRenderer, width / 2 - 98, height / 2 + 15, 128, 18, Text.empty()));
		brandField.setText(ClientBrandRetriever.getClientModName());
		
		addDrawableChild(new ButtonWidget(width / 2 + 33, height / 2 + 14, 20, 20, Text.literal("V"),
				button -> brandField.setText("vanilla")));
		addDrawableChild(new ButtonWidget(width / 2 + 56, height / 2 + 14, 20, 20, Text.literal("Fa"),
				button -> brandField.setText("fabric")));
		addDrawableChild(new ButtonWidget(width / 2 + 79, height / 2 + 14, 20, 20, Text.literal("Fo"),
				button -> brandField.setText("forge")));
	}

	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		renderBackground(matrices);
		drawCenteredText(matrices, textRenderer, "NOTE: This will not make the game compatible with other versions", width / 2, 5, 0xaaaaaa);
		drawCenteredText(matrices, textRenderer, "It will only change what the client says it is to servers.", width / 2, 15, 0xaaaaaa);

		drawStringWithShadow(matrices, textRenderer, "Version:", width / 2 - 103 - textRenderer.getWidth("Version:"), height / 2 - 55, 0xaaaaaa);
		drawStringWithShadow(matrices, textRenderer, "Protocol:", width / 2 - 103 - textRenderer.getWidth("Protocol:"), height / 2 - 30, 0xaaaaaa);
		drawStringWithShadow(matrices, textRenderer, "Pack Ver:", width / 2 - 103 - textRenderer.getWidth("Pack Ver:"), height / 2 - 5, 0xaaaaaa);
		drawStringWithShadow(matrices, textRenderer, "Brand:", width / 2 - 103 - textRenderer.getWidth("Brand:"), height / 2 + 20, 0xaaaaaa);

		super.render(matrices, mouseX, mouseY, delta);
	}

	public void close() {
		client.setScreen(parent);
	}

	public void tick() {
		versionField.tick();
		protocolField.tick();
		packVerField.tick();
		brandField.tick();

		super.tick();
	}

	private void updateAddButton() {
		addButton.active = NumberUtils.isDigits(protocolField.getText()) && NumberUtils.isDigits(packVerField.getText());
	}
}
