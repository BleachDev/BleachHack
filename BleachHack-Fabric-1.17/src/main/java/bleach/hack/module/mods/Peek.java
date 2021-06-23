/*
 * This file is part of the BleachHack distribution (https://github.com/BleachDrinker420/BleachHack/).
 * Copyright (c) 2021 Bleach and contributors.
 *
 * This source code is subject to the terms of the GNU General Public
 * License, version 3. If a copy of the GPL was not distributed with this
 * file, You can obtain one at: https://www.gnu.org/licenses/gpl-3.0.txt
 */
package bleach.hack.module.mods;

import java.util.Arrays;
import java.util.List;

import bleach.hack.eventbus.BleachSubscribe;
import com.mojang.blaze3d.systems.RenderSystem;

import bleach.hack.event.events.EventRenderTooltip;
import bleach.hack.module.ModuleCategory;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.util.FabricReflect;
import bleach.hack.util.ItemContentUtils;
import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.BarrelBlock;
import net.minecraft.block.Block;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.HopperBlock;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapState;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix4f;

public class Peek extends Module {

	private static final RenderLayer MAP_BACKGROUND_CHECKERBOARD = RenderLayer.getText(new Identifier("textures/map/map_background_checkerboard.png"));

	private List<List<String>> pages;
	private int[] slotPos;
	private int pageCount = 0;
	private boolean shown = false;

	public Peek() {
		super("Peek", KEY_UNBOUND, ModuleCategory.MISC, "Shows whats inside containers",
				new SettingToggle("Containers", true).withDesc("Shows a tooltip for containers").withChildren(
						new SettingMode("Info", "All", "Name", "None").withDesc("How to show the old tooltip")),
				new SettingToggle("Books", true).withDesc("Show tooltips for books"),
				new SettingToggle("Maps", true).withDesc("Show tooltips for maps").withChildren(
						new SettingSlider("Map Size", 0.25, 1.5, 0.85, 2).withDesc("How big to make the map")));
	}

	@BleachSubscribe
	public void drawScreen(EventRenderTooltip event) {
		if (!(event.getScreen() instanceof HandledScreen)) {
			return;
		}

		Slot slot = (Slot) FabricReflect.getFieldValue(event.getScreen(), "field_2787", "focusedSlot");
		if (slot == null)
			return;

		if (slotPos == null || slot.x != slotPos[0] || slot.y != slotPos[1]) {
			pageCount = 0;
			pages = null;
		}

		slotPos = new int[] { slot.x, slot.y };

		event.getMatrix().push();
		event.getMatrix().translate(0, 0, 400);

		if (getSetting(0).asToggle().state)
			drawShulkerToolTip(event, slot, event.getMouseX(), event.getMouseY());
		if (getSetting(1).asToggle().state)
			drawBookToolTip(event.getMatrix(), slot, event.getMouseX(), event.getMouseY());
		if (getSetting(2).asToggle().state)
			drawMapToolTip(event.getMatrix(), slot, event.getMouseX(), event.getMouseY());

		event.getMatrix().pop();
	}

	public void drawShulkerToolTip(EventRenderTooltip event, Slot slot, int mouseX, int mouseY) {
		if (!(slot.getStack().getItem() instanceof BlockItem)) {
			return;
		}

		Block block = ((BlockItem) slot.getStack().getItem()).getBlock();

		if (!(block instanceof ShulkerBoxBlock)
				&& !(block instanceof ChestBlock)
				&& !(block instanceof BarrelBlock)
				&& !(block instanceof DispenserBlock)
				&& !(block instanceof HopperBlock)
				&& !(block instanceof AbstractFurnaceBlock)) {
			return;
		}

		List<ItemStack> items = ItemContentUtils.getItemsInContainer(slot.getStack());

		if (items.stream().allMatch(ItemStack::isEmpty)) {
			return;
		}

		if (getSetting(0).asToggle().getChild(0).asMode().mode == 2) {
			event.setCancelled(true);
		} else if (getSetting(0).asToggle().getChild(0).asMode().mode == 1) {
			event.setText(Arrays.asList(slot.getStack().getName()));
		}

		int realY = getSetting(0).asToggle().getChild(0).asMode().mode == 2 ? mouseY + 24 : mouseY;

		if (block instanceof AbstractFurnaceBlock) {
			renderTooltipBox(event.getMatrix(), mouseX, realY - 21, 13, 47, true);
		} else if (block instanceof HopperBlock) {
			renderTooltipBox(event.getMatrix(), mouseX, realY - 21, 13, 82, true);
		} else if (block instanceof DispenserBlock) {
			renderTooltipBox(event.getMatrix(), mouseX, realY - 21, 13, 150, true);
		} else {
			renderTooltipBox(event.getMatrix(), mouseX, realY - 55, 47, 150, true);
		}

		int count = block instanceof HopperBlock || block instanceof DispenserBlock || block instanceof AbstractFurnaceBlock ? 18 : 0;

		for (ItemStack i : items) {
			if (count > 26) {
				break;
			}

			int x = mouseX + 10 + (17 * (count % 9));
			int y = realY - 69 + (17 * (count / 9));

			mc.getItemRenderer().zOffset = 400;
			mc.getItemRenderer().renderGuiItemIcon(i, x, y);
			mc.getItemRenderer().renderGuiItemOverlay(mc.textRenderer, i, x, y, i.getCount() > 1 ? i.getCount() + "" : "");
			mc.getItemRenderer().zOffset = 300;
			count++;
		}
	}

	public void drawBookToolTip(MatrixStack matrices, Slot slot, int mouseX, int mouseY) {
		if (slot.getStack().getItem() != Items.WRITABLE_BOOK && slot.getStack().getItem() != Items.WRITTEN_BOOK)
			return;

		if (pages == null) {
			pages = ItemContentUtils.getTextInBook(slot.getStack());
		}

		if (pages.isEmpty()) {
			return;
		}

		/* Cycle through pages */
		if (mc.player.age % 80 == 0 && !shown) {
			shown = true;
			if (pageCount == pages.size() - 1) {
				pageCount = 0;
			} else {
				pageCount++;
			}
		} else if (mc.player.age % 80 != 0) {
			shown = false;
		}

		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShaderTexture(0, BookScreen.BOOK_TEXTURE);
		DrawableHelper.drawTexture(
				matrices,
				mouseX, mouseY - 143, 0,
				0, 0,
				134, 134,
				179, 179);

		Text pageIndexText = new TranslatableText("book.pageIndicator", new Object[] {pageCount + 1, pages.size() });
		int pageIndexLength = mc.textRenderer.getWidth(pageIndexText);

		matrices.push();
		matrices.scale(0.7f, 0.7f, 1f);

		mc.textRenderer.draw(
				matrices,
				pageIndexText,
				(mouseX + 123 - pageIndexLength) * 1.43f,
				(mouseY - 133) * 1.43f,
				0x000000);


		int count = 0;
		for (String s : pages.get(pageCount)) {
			mc.textRenderer.draw(
					matrices,
					s,
					(mouseX + 24) * 1.43f,
					(mouseY - 123 + count * 7) * 1.43f,
					0x000000);

			count++;
		}

		matrices.pop();

	}

	public void drawMapToolTip(MatrixStack matrices, Slot slot, int mouseX, int mouseY) {
		if (slot.getStack().getItem() != Items.FILLED_MAP) {
			return;
		}

		Integer id = FilledMapItem.getMapId(slot.getStack());
		MapState mapState = FilledMapItem.getMapState(id, mc.world);

		if (mapState == null) {
			return;
		}

		float scale = getSetting(2).asToggle().getChild(0).asSlider().getValueFloat() / 1.25f;

		matrices.push();
		matrices.translate(mouseX + 14, mouseY - 18 - 135 * scale, 0);
		matrices.scale(scale, scale, 1f);

		VertexConsumer vertexConsumer = mc.getBufferBuilders().getEntityVertexConsumers().getBuffer(MAP_BACKGROUND_CHECKERBOARD);
		Matrix4f matrix4f = matrices.peek().getModel();
		vertexConsumer.vertex(matrix4f, -7f, 135f, -10f).color(255, 255, 255, 255).texture(0f, 1f).light(0xf000f0).next();
		vertexConsumer.vertex(matrix4f, 135f, 135f, -10f).color(255, 255, 255, 255).texture(1f, 1f).light(0xf000f0).next();
		vertexConsumer.vertex(matrix4f, 135f, -7f, -10f).color(255, 255, 255, 255).texture(1f, 0f).light(0xf000f0).next();
		vertexConsumer.vertex(matrix4f, -7f, -7f, -10f).color(255, 255, 255, 255).texture(0f, 0f).light(0xf000f0).next();
		mc.getBufferBuilders().getEntityVertexConsumers().draw(MAP_BACKGROUND_CHECKERBOARD);

		mc.gameRenderer.getMapRenderer().draw(matrices, mc.getBufferBuilders().getEntityVertexConsumers(), id, mapState, false, 0xf000f0);

		matrices.pop();

	}

	private void renderTooltipBox(MatrixStack matrices, int x1, int y1, int x2, int y2, boolean wrap) {
		int xStart = x1 + 12;
		int yStart = y1 - 12;
		if (wrap) {
			if (xStart + y2 > mc.currentScreen.width)
				xStart -= 28 + y2;
			if (yStart + x2 + 6 > mc.currentScreen.height)
				yStart = mc.currentScreen.height - x2 - 6;
		}

		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferBuilder = tessellator.getBuffer();
		bufferBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

		Matrix4f matrix4f = matrices.peek().getModel();
		fillGradient(matrix4f, bufferBuilder, xStart - 3, yStart - 4, xStart + y2 + 3, yStart - 3, -267386864, -267386864);
		fillGradient(matrix4f, bufferBuilder, xStart - 3, yStart + x2 + 3, xStart + y2 + 3, yStart + x2 + 4, -267386864, -267386864);
		fillGradient(matrix4f, bufferBuilder, xStart - 3, yStart - 3, xStart + y2 + 3, yStart + x2 + 3, -267386864, -267386864);
		fillGradient(matrix4f, bufferBuilder, xStart - 4, yStart - 3, xStart - 3, yStart + x2 + 3, -267386864, -267386864);
		fillGradient(matrix4f, bufferBuilder, xStart + y2 + 3, yStart - 3, xStart + y2 + 4, yStart + x2 + 3, -267386864, -267386864);
		fillGradient(matrix4f, bufferBuilder, xStart - 3, yStart - 3 + 1, xStart - 3 + 1, yStart + x2 + 3 - 1, 1347420415, 1344798847);
		fillGradient(matrix4f, bufferBuilder, xStart + y2 + 2, yStart - 3 + 1, xStart + y2 + 3, yStart + x2 + 3 - 1, 1347420415, 1344798847);
		fillGradient(matrix4f, bufferBuilder, xStart - 3, yStart - 3, xStart + y2 + 3, yStart - 3 + 1, 1347420415, 1347420415);
		fillGradient(matrix4f, bufferBuilder, xStart - 3, yStart + x2 + 2, xStart + y2 + 3, yStart + x2 + 3, 1344798847, 1344798847);

		RenderSystem.disableTexture();
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		bufferBuilder.end();
		BufferRenderer.draw(bufferBuilder);
		RenderSystem.disableBlend();
		RenderSystem.enableTexture();
	}

	private void fillGradient(Matrix4f matrices, BufferBuilder bufferBuilder, int xStart, int yStart, int xEnd, int yEnd, int colorStart, int colorEnd) {
		float f = (float)(colorStart >> 24 & 255) / 255.0F;
		float g = (float)(colorStart >> 16 & 255) / 255.0F;
		float h = (float)(colorStart >> 8 & 255) / 255.0F;
		float i = (float)(colorStart & 255) / 255.0F;
		float j = (float)(colorEnd >> 24 & 255) / 255.0F;
		float k = (float)(colorEnd >> 16 & 255) / 255.0F;
		float l = (float)(colorEnd >> 8 & 255) / 255.0F;
		float m = (float)(colorEnd & 255) / 255.0F;
		bufferBuilder.vertex(matrices, (float) xEnd, (float) yStart, 0f).color(g, h, i, f).next();
		bufferBuilder.vertex(matrices, (float) xStart, (float) yStart, 0f).color(g, h, i, f).next();
		bufferBuilder.vertex(matrices, (float) xStart, (float) yEnd, 0f).color(k, l, m, j).next();
		bufferBuilder.vertex(matrices, (float) xEnd, (float) yEnd, 0f).color(k, l, m, j).next();
	}
}
