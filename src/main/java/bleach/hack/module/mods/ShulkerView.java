package bleach.hack.module.mods;

import bleach.hack.event.events.EventDrawOverlay;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.utils.ColorUtils;
import bleach.hack.utils.ItemContentUtils;
import bleach.hack.utils.RenderUtils;
import com.google.common.eventbus.Subscribe;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;

import java.util.List;

public class ShulkerView extends Module {

    public ShulkerView() {
        super("ShulkerView", KEY_UNBOUND, Category.RENDER, "When looking at a shulker it will show you the contents",
                new SettingSlider("x", 1, 3840, 243, 0).withDesc("x coordinates"),
                new SettingSlider("y", 1, 3840, 459, 0).withDesc("y coordinates"),
                new SettingMode("Style", "GUI Color", "Black", "Clear").withDesc("Color of the background")
        );
    }

    @Subscribe
    public void onDrawOverlay(EventDrawOverlay event) {

        Entity e = MinecraftClient.getInstance().targetedEntity;
        if (e instanceof ItemFrameEntity && ((ItemFrameEntity) e).getHeldItemStack().getItem().getName().toString().contains("shulker")) {
            GL11.glPushMatrix();
            if (getSetting(2).asMode().mode == 0) {
                RenderUtils.drawRect(
                        (int) getSetting(0).asSlider().getValue(),
                        (int) getSetting(1).asSlider().getValue(),
                        (int) getSetting(0).asSlider().getValue() + 146,
                        (int) getSetting(1).asSlider().getValue() + 50,
                        ColorUtils.guiColour(),
                        0.5f);
            } else if (getSetting(2).asMode().mode == 1) {
                RenderUtils.drawRect(
                        (int) getSetting(0).asSlider().getValue(),
                        (int) getSetting(1).asSlider().getValue(),
                        (int) getSetting(0).asSlider().getValue() + 146,
                        (int) getSetting(1).asSlider().getValue() + 50,
                        0x000000,
                        0.5f);
            }

            List<ItemStack> items = ItemContentUtils.getItemsInContainer(((ItemFrameEntity) e).getHeldItemStack());

            for (int i = 0; i < 27; i++) {
                ItemStack itemStack = items.get(i);
                int offsetX = (int) getSetting(0).asSlider().getValue() + (i % 9) * 16;
                int offsetY = (int) getSetting(1).asSlider().getValue() + (i / 9) * 16;
                mc.getItemRenderer().renderGuiItemIcon(itemStack, offsetX, offsetY);
                mc.getItemRenderer().renderGuiItemOverlay(mc.textRenderer, itemStack, offsetX, offsetY);
            }
            mc.getItemRenderer().zOffset = 0.0F;
            GL11.glPopMatrix();
        }
        //if (e instanceof ItemEntity && e.getName().toString().contains("shulker")) {
        //        GL11.glPushMatrix();
        //        if (getSetting(2).asMode().mode == 0) {
        //            RenderUtils.drawRect(
        //                    (int) getSetting(0).asSlider().getValue(),
        //                    (int) getSetting(1).asSlider().getValue(),
        //                    (int) getSetting(0).asSlider().getValue() + 146,
        //                    (int) getSetting(1).asSlider().getValue() + 50,
        //                    ColorUtils.guiColour(),
        //                    0.5f);
        //        } else if (getSetting(2).asMode().mode == 1) {
        //            RenderUtils.drawRect(
        //                    (int) getSetting(0).asSlider().getValue(),
        //                    (int) getSetting(1).asSlider().getValue(),
        //                    (int) getSetting(0).asSlider().getValue() + 146,
        //                    (int) getSetting(1).asSlider().getValue() + 50,
        //                    0x000000,
        //                    0.5f);
        //        }
        //        List<ItemStack> items = ItemContentUtils.getItemsInContainer(((ItemEntity) e).getStack());
        //        for (int i = 0; i < 27; i++) {
        //            ItemStack itemStack = items.get(i);
        //            int offsetX = (int) getSetting(0).asSlider().getValue() + (i % 9) * 16;
        //            int offsetY = (int) getSetting(1).asSlider().getValue() + (i / 9) * 16;
        //            mc.getItemRenderer().renderGuiItemIcon(itemStack, offsetX, offsetY);
        //            mc.getItemRenderer().renderGuiItemOverlay(mc.textRenderer, itemStack, offsetX, offsetY);
        //        }
        //        mc.getItemRenderer().zOffset = 0.0F;
        //        GL11.glPopMatrix();
        //    }
    }
}
