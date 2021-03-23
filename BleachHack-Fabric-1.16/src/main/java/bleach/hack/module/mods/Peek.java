package bleach.hack.module.mods;

import java.util.Arrays;
import java.util.List;

import org.lwjgl.opengl.GL11;
import com.google.common.collect.Lists;
import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.systems.RenderSystem;

import bleach.hack.event.events.EventDrawTooltip;
import bleach.hack.module.Category;
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
import net.minecraft.block.MaterialColor;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.BlockItem;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapState;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.math.Matrix4f;

public class Peek extends Module {

	private List<List<String>> pages;
	private int[] slotPos;
	private int pageCount = 0;
	private boolean shown = false;

	public Peek() {
		super("Peek", KEY_UNBOUND, Category.MISC, "Shows whats inside containers",
				new SettingToggle("Containers", true).withDesc("Shows a tooltip for containers").withChildren(
						new SettingMode("Info", "All", "Name", "None").withDesc("How to show the old tooltip")),
				new SettingToggle("Books", true).withDesc("Show tooltips for books"),
				new SettingToggle("Maps", true).withDesc("Show tooltips for maps").withChildren(
						new SettingSlider("Map Size", 0.25, 1.5, 0.5, 2).withDesc("How big to make the map")));
	}

	@Subscribe
	public void drawScreen(EventDrawTooltip event) {
		if (!(event.screen instanceof HandledScreen)) {
			return;
		}

		Slot slot = (Slot) FabricReflect.getFieldValue(event.screen, "field_2787", "focusedSlot");
		if (slot == null)
			return;

		if (!Arrays.equals(new int[] { slot.x, slot.y }, slotPos)) {
			pageCount = 0;
			pages = null;
		}

		slotPos = new int[] { slot.x, slot.y };

		event.matrix.push();
		event.matrix.translate(0, 0, 300);

		if (getSetting(0).asToggle().state)
			drawShulkerToolTip(event, slot, event.mouseX, event.mouseY);
		if (getSetting(1).asToggle().state)
			drawBookToolTip(event.matrix, slot, event.mouseX, event.mouseY);
		if (getSetting(2).asToggle().state)
			drawMapToolTip(event.matrix, slot, event.mouseX, event.mouseY);

		event.matrix.pop();
	}

	public void drawShulkerToolTip(EventDrawTooltip event, Slot slot, int mouseX, int mouseY) {
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
			event.text = Lists.transform(Arrays.asList(slot.getStack().getName()), Text::asOrderedText);
		}

		int realY = getSetting(0).asToggle().getChild(0).asMode().mode == 2 ? mouseY + 24 : mouseY;

		if (block instanceof AbstractFurnaceBlock) {
			renderTooltipBox(event.matrix, mouseX, realY - 21, 13, 47, true);
		} else if (block instanceof HopperBlock) {
			renderTooltipBox(event.matrix, mouseX, realY - 21, 13, 82, true);
		} else if (block instanceof DispenserBlock) {
			renderTooltipBox(event.matrix, mouseX, realY - 21, 13, 150, true);
		} else {
			renderTooltipBox(event.matrix, mouseX, realY - 55, 47, 150, true);
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

	public void drawBookToolTip(MatrixStack matrix, Slot slot, int mouseX, int mouseY) {
		if (slot.getStack().getItem() != Items.WRITABLE_BOOK && slot.getStack().getItem() != Items.WRITTEN_BOOK)
			return;

		if (pages == null) {
			pages = ItemContentUtils.getTextInBook(slot.getStack());
		}

		if (pages.isEmpty())
			return;

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

		String pageString = "Page: " + (pageCount + 1) + "/" + pages.size();
		int length = mc.textRenderer.getWidth(pageString);

		renderTooltipBox(matrix, mouseX + 56 - length / 2, mouseY - pages.get(pageCount).size() * 10 - 19, 5, length, true);
		renderTooltipBox(matrix, mouseX, mouseY - pages.get(pageCount).size() * 10 - 6, pages.get(pageCount).size() * 10 - 2, 120, true);
		
		mc.textRenderer.drawWithShadow(matrix, pageString, mouseX + 68 - length / 2, mouseY - pages.get(pageCount).size() * 10 - 32, -1);

		int count = 0;
		for (String s : pages.get(pageCount)) {
			mc.textRenderer.drawWithShadow(matrix, s, mouseX + 12, mouseY - 18 - pages.get(pageCount).size() * 10 + count * 10, 0x00c0c0);
			count++;
		}

	}

	public void drawMapToolTip(MatrixStack matrix, Slot slot, int mouseX, int mouseY) {
		if (slot.getStack().getItem() != Items.FILLED_MAP)
			return;

		MapState data = FilledMapItem.getMapState(slot.getStack(), mc.world);
		if (data == null || data.colors == null)
			return;

		byte[] colors = data.colors;

		double size = getSetting(2).asToggle().getChild(0).asSlider().getValue();

		RenderSystem.pushMatrix();
		RenderSystem.scaled(size, size, 1.0);
		RenderSystem.translatef(0f, 0f, 300f);
		int x = (int) (mouseX * (1 / size) + 12 * (1 / size));
		int y = (int) (mouseY * (1 / size) - 12 * (1 / size) - 140);

		renderTooltipBox(matrix, x - 12, y + 12, 128, 128, false);
		for (byte c : colors) {
			int c1 = c & 255;

			if (c1 / 4 != 0)
				DrawableHelper.fill(matrix, x, y, x + 1, y + 1, getRenderColorFix(MaterialColor.COLORS[c1 / 4].color, c1 & 3));
			if (x - (int) (mouseX * (1 / size) + 12 * (1 / size)) == 127) {
				x = (int) (mouseX * (1 / size) + 12 * (1 / size));
				y++;
			} else {
				x++;
			}
		}

		RenderSystem.popMatrix();
	}

	/* Fix your game [B]ojang */
	private int getRenderColorFix(int color, int offset) {
		int int_2 = (offset == 3 ? 135 : offset == 2 ? 255 : offset == 0 ? 180 : 220);

		int r = (color >> 16 & 255) * int_2 / 255;
		int g = (color >> 8 & 255) * int_2 / 255;
		int b = (color & 255) * int_2 / 255;
		return -16777216 | r << 16 | g << 8 | b;
	}

	private void renderTooltipBox(MatrixStack matrix, int x1, int y1, int x2, int y2, boolean wrap) {
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
		bufferBuilder.begin(7, VertexFormats.POSITION_COLOR);

		Matrix4f matrix4f = matrix.peek().getModel();
		fillGradient(matrix4f, bufferBuilder, xStart - 3, yStart - 4, xStart + y2 + 3, yStart - 3, -267386864, -267386864);
		fillGradient(matrix4f, bufferBuilder, xStart - 3, yStart + x2 + 3, xStart + y2 + 3, yStart + x2 + 4, -267386864, -267386864);
		fillGradient(matrix4f, bufferBuilder, xStart - 3, yStart - 3, xStart + y2 + 3, yStart + x2 + 3, -267386864, -267386864);
		fillGradient(matrix4f, bufferBuilder, xStart - 4, yStart - 3, xStart - 3, yStart + x2 + 3, -267386864, -267386864);
		fillGradient(matrix4f, bufferBuilder, xStart + y2 + 3, yStart - 3, xStart + y2 + 4, yStart + x2 + 3, -267386864, -267386864);
		fillGradient(matrix4f, bufferBuilder, xStart - 3, yStart - 3 + 1, xStart - 3 + 1, yStart + x2 + 3 - 1, 1347420415, 1344798847);
		fillGradient(matrix4f, bufferBuilder, xStart + y2 + 2, yStart - 3 + 1, xStart + y2 + 3, yStart + x2 + 3 - 1, 1347420415, 1344798847);
		fillGradient(matrix4f, bufferBuilder, xStart - 3, yStart - 3, xStart + y2 + 3, yStart - 3 + 1, 1347420415, 1347420415);
		fillGradient(matrix4f, bufferBuilder, xStart - 3, yStart + x2 + 2, xStart + y2 + 3, yStart + x2 + 3, 1344798847, 1344798847);
		
		RenderSystem.enableDepthTest();
        RenderSystem.disableTexture();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.shadeModel(GL11.GL_SMOOTH);
        bufferBuilder.end();
        BufferRenderer.draw(bufferBuilder);
        RenderSystem.shadeModel(GL11.GL_FLAT);
        RenderSystem.disableBlend();
        RenderSystem.enableTexture();
	}

	private void fillGradient(Matrix4f matrix, BufferBuilder bufferBuilder, int xStart, int yStart, int xEnd, int yEnd, int colorStart, int colorEnd) {
		float f = (float)(colorStart >> 24 & 255) / 255.0F;
		float g = (float)(colorStart >> 16 & 255) / 255.0F;
		float h = (float)(colorStart >> 8 & 255) / 255.0F;
		float i = (float)(colorStart & 255) / 255.0F;
		float j = (float)(colorEnd >> 24 & 255) / 255.0F;
		float k = (float)(colorEnd >> 16 & 255) / 255.0F;
		float l = (float)(colorEnd >> 8 & 255) / 255.0F;
		float m = (float)(colorEnd & 255) / 255.0F;
		bufferBuilder.vertex(matrix, (float) xEnd, (float) yStart, 0f).color(g, h, i, f).next();
		bufferBuilder.vertex(matrix, (float) xStart, (float) yStart, 0f).color(g, h, i, f).next();
		bufferBuilder.vertex(matrix, (float) xStart, (float) yEnd, 0f).color(k, l, m, j).next();
		bufferBuilder.vertex(matrix, (float) xEnd, (float) yEnd, 0f).color(k, l, m, j).next();
	}
}
