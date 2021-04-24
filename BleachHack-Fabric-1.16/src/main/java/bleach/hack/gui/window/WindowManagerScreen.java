/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.gui.window;

import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.tuple.Triple;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.LiteralText;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

public class WindowManagerScreen extends Screen {

	/** [Window Screen, Name, Icon] **/
	public Triple<WindowScreen, String, ItemStack>[] windows;
	private int selected = 0;

	@SafeVarargs
	public WindowManagerScreen(Triple<WindowScreen, String, ItemStack>... windows) {
		super(LiteralText.EMPTY);
		this.windows = windows;
	}

	public void selectWindow(int s) {
		selected = s;
		getSelectedScreen().init(client, width, height - 14);
	}

	public WindowScreen getSelectedScreen() {
		return windows[selected].getLeft();
	}
	
	public String getSelectedTitle() {
		return windows[selected].getMiddle();
	}
	
	public ItemStack getSelectedIcon() {
		return windows[selected].getRight();
	}

	public Text getTitle() {
		return getSelectedScreen().getTitle();
	}

	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		getSelectedScreen().render(matrices, mouseX, mouseY, delta);

		DrawableHelper.fill(matrices, 0, height - 13, 19, height - 12, 0xff6060b0);
		DrawableHelper.fill(matrices, 0, height - 13, 1, height, 0xff6060b0);
		DrawableHelper.fill(matrices, 19, height - 12, 20, height, 0xff6060b0);
		textRenderer.draw(matrices, "\u00a7cX", 7, height - 10, -1);

		int wid = 20;
		int size = 90;
		for (int i = 0; i < windows.length; i++) {
			DrawableHelper.fill(matrices, wid, height - 13, wid + size - 1, height - 12, 0xff6060b0);
			DrawableHelper.fill(matrices, wid, height - 13, wid + 1, height, 0xff6060b0);
			DrawableHelper.fill(matrices, wid + size - 1, height - 12, wid + size, height, 0xff6060b0);

			RenderSystem.pushMatrix();
			RenderSystem.scaled(0.7, 0.7, 1);
			itemRenderer.renderGuiItemIcon(windows[i].getRight(), (int) ((wid + 2) * (1 / 0.7)), (int) ((height - 12) * (1 / 0.7)));
			RenderSystem.popMatrix();

			textRenderer.draw(matrices, windows[i].getMiddle(), wid + 16, height - 10, selected == i ? 0xffccff : 0xffffff);
			wid += size;
		}
	}

	/**
	 * Checks whether this screen should be closed when the escape key is pressed.
	 */
	public boolean shouldCloseOnEsc() {
		return getSelectedScreen().shouldCloseOnEsc();
	}

	public void onClose() {
		getSelectedScreen().onClose();
	}

	public List<Text> getTooltipFromItem(ItemStack stack) {
		return getSelectedScreen().getTooltipFromItem(stack);
	}

	public void renderOrderedTooltip(MatrixStack matrices, List<? extends OrderedText> lines, int x, int y) {
		getSelectedScreen().renderOrderedTooltip(matrices, lines, x, y);
	}

	public boolean handleTextClick(Style style) {
		return getSelectedScreen().handleTextClick(style);
	}

	public void sendMessage(String message) {
		getSelectedScreen().sendMessage(message, true);
	}

	public void sendMessage(String message, boolean toHud) {
		getSelectedScreen().sendMessage(message, toHud);
	}

	public void init(MinecraftClient client, int width, int height) {
		super.init(client, width, height);

		this.client = client;
		this.itemRenderer = client.getItemRenderer();
		this.textRenderer = client.textRenderer;
		this.width = width;
		this.height = height;
		this.buttons.clear();
		this.children.clear();
		this.setFocused((Element)null);
		getSelectedScreen().init(client, width, height - 14);
	}

	/**
	 * Called when a screen should be initialized.
	 * 
	 * <p>This method is called when this screen is {@link MinecraftClient#openScreen(Screen) opened} or resized.
	 */
	protected void init() {
		super.init();
		getSelectedScreen().init();
	}

	public void tick() {
		getSelectedScreen().tick();
	}

	public void removed() {
		getSelectedScreen().removed();
	}

	/**
	 * Renders the background of this screen.
	 * 
	 * <p>If the client is in a world, renders the translucent background gradient.
	 * Otherwise {@linkplain #renderBackgroundTexture(int) renders the background texture}.
	 * 
	 * @param vOffset an offset applied to the V coordinate of the background texture
	 */
	public void renderBackground(MatrixStack matrices, int vOffset) {
		getSelectedScreen().renderBackground(matrices, vOffset);
	}

	/**
	 * Renders the fullscreen {@linkplain #BACKGROUND_TEXTURE background texture} of this screen.
	 * 
	 * @param vOffset an offset applied to the V coordinate of the background texture
	 */
	public void renderBackgroundTexture(int vOffset) {
		getSelectedScreen().renderBackgroundTexture(vOffset);
	}

	public boolean isPauseScreen() {
		return getSelectedScreen().isPauseScreen();
	}

	public boolean isMouseOver(double mouseX, double mouseY) {
		return getSelectedScreen().isMouseOver(mouseX, mouseY);
	}

	public void filesDragged(List<Path> paths) {
		getSelectedScreen().filesDragged(paths);
	}

	public Optional<Element> hoveredElement(double mouseX, double mouseY) {
		return getSelectedScreen().hoveredElement(mouseX, mouseY);
	}

	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (mouseX > 0 && mouseX < 20 && mouseY > height - 14 && mouseY < height) {
			selectWindow(0);
			
			MinecraftClient.getInstance().getSoundManager().play(
					PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F, 0.3F));
		}

		if (mouseY > height - 14 && mouseY < height && mouseX > 20) {
			int sel = ((int) mouseX - 20) / 90;

			if (sel >= 0 && sel < windows.length) {
				selectWindow(sel);
				
				MinecraftClient.getInstance().getSoundManager().play(
						PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK, 1.0F, 0.3F));
			}
		}

		return getSelectedScreen().mouseClicked(mouseX, mouseY, button);
	}

	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		return getSelectedScreen().mouseReleased(mouseX, mouseY, button);
	}

	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		return this.getFocused() != null && this.isDragging() && button == 0 ? this.getFocused().mouseDragged(mouseX, mouseY, button, deltaX, deltaY) : false;
	}

	public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		return getSelectedScreen().mouseScrolled(mouseX, mouseY, amount);
	}

	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		return getSelectedScreen().keyPressed(keyCode, scanCode, modifiers);
	}

	public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
		return getSelectedScreen().keyReleased(keyCode, scanCode, modifiers);
	}

	public boolean charTyped(char chr, int modifiers) {
		return getSelectedScreen().charTyped(chr, modifiers);
	}

	public void setInitialFocus(Element element) {
		getSelectedScreen().setInitialFocus(element);
	}

	public void focusOn(Element element) {
		getSelectedScreen().setFocused(element);
	}

	public boolean changeFocus(boolean lookForwards) {
		return getSelectedScreen().changeFocus(lookForwards);
	}

	public List<? extends Element> children() {
		return getSelectedScreen().children();
	}
}
