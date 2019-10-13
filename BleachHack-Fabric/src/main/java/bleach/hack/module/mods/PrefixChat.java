package bleach.hack.module.mods;

import java.util.Arrays;
import java.util.List;

import bleach.hack.command.CommandManager;
import bleach.hack.event.events.EventKeyPress;
import bleach.hack.module.ModuleManager;
import bleach.hack.utils.BleachLogger;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.opengl.GL11;

import bleach.hack.event.events.EventDrawContainer;
import bleach.hack.gui.clickgui.SettingBase;
import bleach.hack.gui.clickgui.SettingSlider;
import bleach.hack.gui.clickgui.SettingToggle;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.utils.FabricReflect;
import bleach.hack.utils.ItemContentUtils;
import net.minecraft.block.Block;
import net.minecraft.block.ChestBlock;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.HopperBlock;
import net.minecraft.block.MaterialColor;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.GuiLighting;
import net.minecraft.container.Slot;
import net.minecraft.item.BlockItem;
import net.minecraft.item.FilledMapItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.map.MapState;

import org.lwjgl.opengl.GL12;

import com.google.common.eventbus.Subscribe;

public class PrefixChat extends Module {

    public PrefixChat() {
        super("PrefixChat", -1, Category.MISC, "Opens chat when you hit the prefix.", null);
    }

    @Subscribe
    public static void handlePrefixKey(EventKeyPress eventKeyPress) {
        if (InputUtil.getKeycodeName(InputUtil.getKeyCode(eventKeyPress.getKey(), eventKeyPress.getScanCode()).getKeyCode()) != null && InputUtil.getKeycodeName(InputUtil.getKeyCode(eventKeyPress.getKey(), eventKeyPress.getScanCode()).getKeyCode()).equals(CommandManager.prefix)) {
            BleachLogger.infoMessage(CommandManager.prefix);
            MinecraftClient.getInstance().openScreen(new ChatScreen(CommandManager.prefix));
        }
    }

}
