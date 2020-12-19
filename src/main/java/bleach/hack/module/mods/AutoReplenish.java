
//package bleach.hack.module.mods;
//
////Created by squidoodly 8/05/2020
////Updated by squidoodly 14/07/2020
//
//import bleach.hack.event.Event;
//import bleach.hack.module.Category;
//import bleach.hack.module.Module;
//import bleach.hack.setting.base.SettingSlider;
//import bleach.hack.setting.base.SettingToggle;
//import bleach.hack.utils.BleachLogger;
//import bleach.hack.utils.InvUtils;
//import com.google.common.eventbus.Subscribe;
//import net.minecraft.client.MinecraftClient;
//import net.minecraft.client.gui.screen.ingame.AbstractInventoryScreen;
//import net.minecraft.client.gui.screen.ingame.HandledScreen;
//import net.minecraft.client.gui.screen.ingame.InventoryScreen;
//import net.minecraft.entity.player.PlayerEntity;
//import net.minecraft.item.Item;
//import net.minecraft.item.ItemStack;
//import net.minecraft.item.Items;
//import net.minecraft.screen.slot.SlotActionType;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class AutoReplenish extends Module {
//
//    private final List<Item> items = new ArrayList<>();
//
//    private Item lastMainHand, lastOffHand;
//    private int lastSlot;
//
//    public AutoReplenish(){
//        super("AutoReplenish", KEY_UNBOUND, Category.MISC, "Replenishes automatically.",
//                new SettingSlider("Amount", 1, 63, 1, 0),
//                new SettingToggle("Offhand", false),
//                new SettingToggle("Alert", false),
//                new SettingToggle("Unstackables", true),
//                new SettingToggle("Hotbar", true),
//                new SettingToggle("Containers", true),
//                new SettingToggle("Inventory", true));
//    }
//
//    public void onEnable() {
//        if (mc.world == null) return;
//        MinecraftClient mc = MinecraftClient.getInstance();
//        PlayerEntity player = mc.player;
//        lastSlot = mc.player.inventory.selectedSlot;
//    }
//
//    public void onDisable() {
//        lastMainHand = lastOffHand = null;
//    }
//
//    @Subscribe
//    public void onTick(Event event) {
//        if (!getSettings().get(5).asToggle().state && !getSettings().get(6).asToggle().state) {
//            if (mc.currentScreen instanceof HandledScreen<?>) return;
//        } else if (getSettings().get(5).asToggle().state && !getSettings().get(6).asToggle().state) {
//            if (mc.currentScreen instanceof HandledScreen<?> && mc.currentScreen instanceof InventoryScreen) return;
//        } else if (!getSettings().get(5).asToggle().state && getSettings().get(6).asToggle().state) {
//            if (mc.currentScreen instanceof HandledScreen<?> && !(mc.currentScreen instanceof InventoryScreen)) return;
//        }
//
//        // Hotbar, stackable items
//        for (int i = 0; i < 9; i++) {
//            ItemStack stack = mc.player.inventory.getStack(i);
//            InvUtils.FindItemResult result = InvUtils.findItemWithCount(stack.getItem());
//            if(result.slot < i && i != mc.player.inventory.selectedSlot) continue;
//            if(isUnstackable(stack.getItem()) || stack.getItem() == Items.AIR) continue;
//            if (stack.getCount() < getSettings().get(0).asSlider().getValue() && (stack.getMaxCount() > getSettings().get(0).asSlider().getValue() || stack.getCount() < stack.getMaxCount())) {
//                int slot = -1;
//                if (getSettings().get(4).asToggle().state) {
//                    for (int j = 0; j < 9; j++) {
//                        if (mc.player.inventory.getStack(j).getItem() == stack.getItem() && ItemStack.areTagsEqual(stack, mc.player.inventory.getStack(j)) && mc.player.inventory.selectedSlot != j && i != j) {
//                            slot = j;
//                            break;
//                        }
//                    }
//                }
//                if (slot == -1) {
//                    for (int j = 9; j < mc.player.inventory.main.size(); j++) {
//                        if (mc.player.inventory.getStack(j).getItem() == stack.getItem() && ItemStack.areTagsEqual(stack, mc.player.inventory.getStack(j))) {
//                            slot = j;
//                            break;
//                        }
//                    }
//                }
//                if(slot != -1) {
//                    moveItems(slot, i, true);
//                }
//            }
//        }
//
//        // OffHand, stackable items
//        if (getSettings().get(1).asToggle().state) {
//            ItemStack stack = mc.player.getOffHandStack();
//            if(stack.getItem() != Items.AIR) {
//                if (stack.getCount() < getSettings().get(0).asSlider().getValue() && (stack.getMaxCount() > getSettings().get(0).asSlider().getValue() || stack.getCount() < stack.getMaxCount())) {
//                    int slot = -1;
//                    for (int i = 9; i < mc.player.inventory.main.size(); i++) {
//                        if (mc.player.inventory.getStack(i).getItem() == stack.getItem() && ItemStack.areTagsEqual(stack, mc.player.inventory.getStack(i))) {
//                            slot = i;
//                            break;
//                        }
//                    }
//                    if (getSettings().get(4).asToggle().state && slot == -1) {
//                        for (int i = 0; i < 9; i++) {
//                            if (mc.player.inventory.getStack(i).getItem() == stack.getItem() && ItemStack.areTagsEqual(stack, mc.player.inventory.getStack(i))) {
//                                slot = i;
//                                break;
//                            }
//                        }
//                    }
//                    if (slot != -1) {
//                        moveItems(slot, InvUtils.OFFHAND_SLOT, true);
//                    }
//                }
//            }
//        }
//
//        // MainHand, unstackable items
//        if (getSettings().get(3).asToggle().state) {
//            ItemStack mainHandItemStack = mc.player.getMainHandStack();
//            if (mainHandItemStack.getItem() != lastMainHand && mainHandItemStack.isEmpty() && (lastMainHand != null && lastMainHand != Items.AIR) && isUnstackable(lastMainHand) && mc.player.inventory.selectedSlot == lastSlot) {
//                int slot = findSlot(lastMainHand, lastSlot);
//                if (slot != -1) moveItems(slot, lastSlot, false);
//            }
//            lastMainHand = mc.player.getMainHandStack().getItem();
//            lastSlot = mc.player.inventory.selectedSlot;
//
//            if (getSettings().get(1).asToggle().state) {
//                // OffHand, unstackable items
//                ItemStack offHandItemStack = mc.player.getOffHandStack();
//                if (offHandItemStack.getItem() != lastOffHand && offHandItemStack.isEmpty() && (lastOffHand != null && lastOffHand != Items.AIR) && isUnstackable(lastOffHand)) {
//                    int slot = findSlot(lastOffHand, InvUtils.OFFHAND_SLOT);
//                    if (slot != -1) moveItems(slot, InvUtils.OFFHAND_SLOT, false);
//                }
//                lastOffHand = mc.player.getOffHandStack().getItem();
//            }
//        }
//    }
//
//    @Subscribe
//    public void eventOpenScreen(Event event) {
//        if (mc.currentScreen instanceof HandledScreen<?>) {
//            if (!(mc.currentScreen instanceof AbstractInventoryScreen)) items.clear();
//            lastMainHand = lastOffHand = null;
//        }
//    }
//
//    private void moveItems(int from, int to, boolean stackable) {
//        InvUtils.clickSlot(InvUtils.invIndexToSlotId(from), 0, SlotActionType.PICKUP);
//        InvUtils.clickSlot(InvUtils.invIndexToSlotId(to), 0, SlotActionType.PICKUP);
//        if (stackable) InvUtils.clickSlot(InvUtils.invIndexToSlotId(from), 0, SlotActionType.PICKUP);
//    }
//
//    private int findSlot(Item item, int excludeSlot) {
//        int slot = findItems(item, excludeSlot);
//
//        if(slot == -1 && !items.contains(item)){
//            if(getSettings().get(2).asToggle().state) {
//                BleachLogger.errorMessage( "You are out of " + item + "(s). Cannot refill.");
//            }
//
//            items.add(item);
//        }
//
//        return slot;
//    }
//
//    private int findItems(Item item, int excludeSlot) {
//        int slot = -1;
//
//        for (int i = getSetting(4).asToggle().state ? 0 : 9; i < mc.player.inventory.main.size(); i++) {
//            if (i != excludeSlot && mc.player.inventory.main.get(i).getItem() == item && (!getSetting(4).asToggle().state || i != mc.player.inventory.selectedSlot)) {
//                slot = i;
//                return slot;
//            }
//        }
//
//        return slot;
//    }
//
//    private boolean isUnstackable(Item item) {
//        return item.getMaxCount() <= 1;
//    }
//}