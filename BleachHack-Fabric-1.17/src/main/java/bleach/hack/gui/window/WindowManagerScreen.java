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

	@Override
	public Text getTitle() {
		return getSelectedScreen().getTitle();
	}

	@Override
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

			RenderSystem.getModelViewStack().push();
			RenderSystem.getModelViewStack().scale(0.7f, 0.7f, 1f);

			itemRenderer.renderGuiItemIcon(windows[i].getRight(), (int) ((wid + 2) * (1 / 0.7)), (int) ((height - 12) * (1 / 0.7)));

			RenderSystem.getModelViewStack().pop();
			RenderSystem.applyModelViewMatrix();

			textRenderer.draw(matrices, windows[i].getMiddle(), wid + 16, height - 10, selected == i ? 0xffccff : 0xffffff);
			wid += size;
		}
	}

	/**
	 * Checks whether this screen should be closed when the escape key is pressed.
	 */
	@Override
	public boolean shouldCloseOnEsc() {
		return getSelectedScreen().shouldCloseOnEsc();
	}

	@Override
	public void onClose() {
		getSelectedScreen().onClose();
	}

	@Override
	public List<Text> getTooltipFromItem(ItemStack stack) {
		return getSelectedScreen().getTooltipFromItem(stack);
	}

	@Override
	public void renderOrderedTooltip(MatrixStack matrices, List<? extends OrderedText> lines, int x, int y) {
		getSelectedScreen().renderOrderedTooltip(matrices, lines, x, y);
	}

	@Override
	public boolean handleTextClick(Style style) {
		return getSelectedScreen().handleTextClick(style);
	}

	@Override
	public void sendMessage(String message) {
		getSelectedScreen().sendMessage(message, true);
	}

	@Override
	public void sendMessage(String message, boolean toHud) {
		getSelectedScreen().sendMessage(message, toHud);
	}

	/*@Override
	public void init(MinecraftClient client, int width, int height) {
		super.init(client, width, height);

		getSelectedScreen().init(client, width, height - 14);
	}*/

	@Override
	protected void init() {
		super.init();
		selectWindow(selected);
		//getSelectedScreen().init();
	}

	@Override
	public void tick() {
		getSelectedScreen().tick();
	}

	@Override
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
	@Override
	public void renderBackground(MatrixStack matrices, int vOffset) {
		getSelectedScreen().renderBackground(matrices, vOffset);
	}

	/**
	 * Renders the fullscreen {@linkplain #BACKGROUND_TEXTURE background texture} of this screen.
	 * 
	 * @param vOffset an offset applied to the V coordinate of the background texture
	 */
	@Override
	public void renderBackgroundTexture(int vOffset) {
		getSelectedScreen().renderBackgroundTexture(vOffset);
	}

	@Override
	public boolean isPauseScreen() {
		return getSelectedScreen().isPauseScreen();
	}

	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return getSelectedScreen().isMouseOver(mouseX, mouseY);
	}

	@Override
	public void filesDragged(List<Path> paths) {
		getSelectedScreen().filesDragged(paths);
	}

	@Override
	public Optional<Element> hoveredElement(double mouseX, double mouseY) {
		return getSelectedScreen().hoveredElement(mouseX, mouseY);
	}

	@Override
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

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		return getSelectedScreen().mouseReleased(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		return this.getFocused() != null && this.isDragging() && button == 0 ? this.getFocused().mouseDragged(mouseX, mouseY, button, deltaX, deltaY) : false;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double amount) {
		return getSelectedScreen().mouseScrolled(mouseX, mouseY, amount);
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		return getSelectedScreen().keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
		return getSelectedScreen().keyReleased(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean charTyped(char chr, int modifiers) {
		return getSelectedScreen().charTyped(chr, modifiers);
	}

	@Override
	public void setInitialFocus(Element element) {
		getSelectedScreen().setInitialFocus(element);
	}

	@Override
	public void focusOn(Element element) {
		getSelectedScreen().setFocused(element);
	}

	@Override
	public boolean changeFocus(boolean lookForwards) {
		return getSelectedScreen().changeFocus(lookForwards);
	}

	@Override
	public List<? extends Element> children() {
		return getSelectedScreen().children();
	}
}
