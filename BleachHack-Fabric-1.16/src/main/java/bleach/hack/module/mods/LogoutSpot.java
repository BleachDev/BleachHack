package bleach.hack.module.mods;

import bleach.hack.BleachHack;
import bleach.hack.event.events.EventEntityRender;
import bleach.hack.event.events.EventReadPacket;
import bleach.hack.event.events.EventTick;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.util.render.WorldRenderUtils;
import bleach.hack.util.world.PlayerCopyEntity;
import com.google.common.eventbus.Subscribe;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.PlayerListS2CPacket;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author <a href="https://github.com/lasnikprogram">Lasnik</a>
 */

public class LogoutSpot extends Module {
    private final HashMap<PlayerEntity, Map.Entry<PlayerCopyEntity, Long>> players = new HashMap<>();

    public LogoutSpot() {
        super("LogoutSpot", KEY_UNBOUND, Category.WORLD, "Shows where a player logged out",
                new SettingToggle("Distance", true).withDesc("Remove log out spots based on distance").withChildren( // 0
                        new SettingSlider("Radius", 1, 1000, 200, 0).withDesc("Radius in which log out spots get shown")), // 0-0
                new SettingToggle("Time", false).withDesc("Remove log out spots based on time since logout").withChildren( // 1
                        new SettingSlider("Duration", 1, 1800, 120, 0).withDesc("Duration after which a logged out players gets removed")), // 1-1
                new SettingToggle("Remove", true).withDesc("Whether all log out spots should be cleared after disconnection or disabling"), // 2
                new SettingToggle("Nametag", true).withDesc("Shows a nametag over the log out spot").withChildren(  // 3
                        new SettingSlider("Size", 0.5, 5, 2, 1).withDesc("Size of the nametag"), // 3-0
                        new SettingToggle("Name", true).withDesc("Shows the name of the logged out player"), // 3-1
                        new SettingToggle("Time", true).withDesc("Shows the time since the player logged out"), // 3-2
                        new SettingMode("Health", "Number", "Bar", "Percent", "None").withDesc("How to show the health"), // 3-3
                        new SettingMode("Armor", "H", "V", "None").withDesc("How to show items/armor")), // 3-4
                new SettingToggle("Glowing", true).withDesc("Whether logged out players should be glowing")); // 4
    }

    @Override
    public void onDisable() {
        clearHash();
        super.onDisable();
    }

    @Subscribe
    public void readPacket(EventReadPacket event) {
        if (!(event.getPacket() instanceof PlayerListS2CPacket) || mc.world == null) {
            return;
        }

        PlayerListS2CPacket list = (PlayerListS2CPacket) event.getPacket();

        // Spawns fake player when player leaves
        if (list.getAction().equals(PlayerListS2CPacket.Action.REMOVE_PLAYER)) {
            for (PlayerListS2CPacket.Entry entry : list.getEntries()) {
                PlayerEntity player = mc.world.getPlayerByUuid(entry.getProfile().getId());

                if (player != null && !mc.player.equals(player)) {
                    players.put(player, new AbstractMap.SimpleEntry<>(spawnDummy(player), System.currentTimeMillis()));
                }
            }
        }

        // Despawns fake player when player joins
        if (list.getAction().equals(PlayerListS2CPacket.Action.ADD_PLAYER)) {
            for (PlayerListS2CPacket.Entry entry : list.getEntries()) {
                PlayerEntity player = mc.world.getPlayerByUuid(entry.getProfile().getId());

                if (player != null && !mc.player.equals(player)) {
                    removePlayer(player);
                }
            }
        }
    }

    // Removes the players based on settings
    @Subscribe
    public void onTick(EventTick event) {
        if (getSetting(0).asToggle().state) {
            players.keySet().stream().forEach(player -> {
                if (mc.player.distanceTo(players.get(player).getKey()) > getSetting(0).asToggle().getChild(0).asSlider().getValueInt()) {
                    removePlayer(player);
                }
            });
        }

        if (getSetting(1).asToggle().state) {
            players.keySet().stream().forEach(player -> {
                if ((System.currentTimeMillis() - players.get(player).getValue().longValue()) / 1000 > getSetting(1).asToggle().getChild(0).asSlider().getValueInt()) {
                    removePlayer(player);
                }
            });
        }
    }

    private PlayerCopyEntity spawnDummy(PlayerEntity player) {
        PlayerCopyEntity dummy = new PlayerCopyEntity(player);
        dummy.spawn();
        return dummy;
    }

    // Despawns the fakeplayer to the corresponding player. Entry will get deleted
    private void removePlayer(PlayerEntity player) {
        players.keySet().removeIf(e -> {
            if (e.equals(player)) {
                players.get(e).getKey().despawn();
                return true;
            }
            return false;
        });
    }

    // Ran from MixinClientConnection when leaving world and onDisable()
    public void clearHash() {
        if (getSetting(2).asToggle().state) {
            players.values().stream().forEach(e -> {
                if (mc.world != null) {
                    e.getKey().despawn();
                }
                players.remove(e);
            });
        }
    }

    // Disables minecraft's nametags if setting is enabled
    @Subscribe
    public void onLivingLabelRender(EventEntityRender.Single.Label event) {
        if (!getSetting(3).asToggle().state) {
            return;
        }

        players.values().stream().forEach(entry -> {
            if (entry != null && entry.getKey().getUuid().equals(event.getEntity().getUuid()))
                event.setCancelled(true);
        });
    }

    // Everything after this is basically just modified code from Nametags.java. It is used for nametags omg
    @Subscribe
    public void onLivingRender(EventEntityRender.Single.Post event) {
        SettingToggle nametagSetting = getSetting(3).asToggle();
        AtomicBoolean shouldRender = new AtomicBoolean(false);
        AtomicLong spawnMillis = new AtomicLong(-1);
        players.values().forEach(entry -> {
            if (entry.getKey().getUuid().equals(event.getEntity().getUuid())) {
                shouldRender.set(true);
                spawnMillis.set(entry.getValue());
            }
        });

        if (shouldRender.get()) {
            event.getEntity().setGlowing(getSetting(4).asToggle().state);
        }

        if (!nametagSetting.state || !shouldRender.get()) {
            return;
        }

        PlayerCopyEntity e = (PlayerCopyEntity) event.getEntity();

        List<String> lines = new ArrayList<>();
        double scale;

        Vec3d rPos = getRenderPos(event.getEntity());

        scale = Math.max(nametagSetting.getChild(0).asSlider().getValue() * (mc.cameraEntity.distanceTo(e) / 20), 1);

        if (nametagSetting.getChild(2).asToggle().state) {
            lines.add((System.currentTimeMillis() - spawnMillis.longValue()) / 1000 + "s");
        }

        addNameHealthLine(lines, event.getEntity(), BleachHack.friendMang.has(event.getEntity().getName().getString()) ? Formatting.AQUA : Formatting.RED,
                nametagSetting.getChild(1).asToggle().state,
                nametagSetting.getChild(3).asMode().mode != 3);


        /* Drawing Items */
        double c = 0;
        double lscale = scale * 0.4;
        double up = ((0.3 + lines.size() * 0.25) * scale) + lscale / 2;

        if (nametagSetting.getChild(4).asMode().mode == 0) {
            drawItem(rPos.x, rPos.y + up, rPos.z, -2.5, 0, lscale, e.getEquippedStack(EquipmentSlot.MAINHAND));
            drawItem(rPos.x, rPos.y + up, rPos.z, 2.5, 0, lscale, e.getEquippedStack(EquipmentSlot.OFFHAND));

            for (ItemStack i : e.getArmorItems()) {
                drawItem(rPos.x, rPos.y + up, rPos.z, c + 1.5, 0, lscale, i);
                c--;
            }
        } else if (nametagSetting.getChild(4).asMode().mode == 1) {
            drawItem(rPos.x, rPos.y + up, rPos.z, -1.25, 0, lscale, e.getEquippedStack(EquipmentSlot.MAINHAND));
            drawItem(rPos.x, rPos.y + up, rPos.z, 1.25, 0, lscale, e.getEquippedStack(EquipmentSlot.OFFHAND));

            for (ItemStack i : e.getArmorItems()) {
                drawItem(rPos.x, rPos.y + up, rPos.z, 0, c, lscale, i);
                c++;
            }
        }

        if (!lines.isEmpty()) {
            float offset = 0.25f + lines.size() * 0.25f;

            for (String s : lines) {
                WorldRenderUtils.drawText(s, rPos.x, rPos.y + (offset * scale), rPos.z, scale);

                offset -= 0.25f;
            }
        }
    }

    private String getHealthText(Entity entity) {
        PlayerEntity e = (PlayerEntity) entity;
        SettingToggle nametagSetting = getSetting(3).asToggle();
        if (nametagSetting.getChild(3).asMode().mode == 0) {
            return Formatting.GREEN + "[" + getHealthColor(e) + (int) (e.getHealth() + e.getAbsorptionAmount()) + Formatting.GREEN + "/" + (int) e.getMaxHealth() + "]";
        } else if (nametagSetting.getChild(3).asMode().mode == 1) {
            /* Health bar */
            String health = "";
            /* - Add Green Normal Health */
            for (int i = 0; i < e.getHealth(); i++)
                health += Formatting.GREEN + "|";
            /* - Add Red Empty Health (Remove Based on absorption amount) */
            for (int i = 0; i < MathHelper.clamp(e.getAbsorptionAmount(), 0, e.getMaxHealth() - e.getHealth()); i++)
                health += Formatting.YELLOW + "|";
            /* Add Yellow Absorption Health */
            for (int i = 0; i < e.getMaxHealth() - (e.getHealth() + e.getAbsorptionAmount()); i++)
                health += Formatting.RED + "|";
            /* Add "+??" to the end if the entity has extra hearts */
            if (e.getAbsorptionAmount() - (e.getMaxHealth() - e.getHealth()) > 0) {
                health += Formatting.YELLOW + " +" + (int) (e.getAbsorptionAmount() - (e.getMaxHealth() - e.getHealth()));
            }

            return health;
        } else {
            return getHealthColor(e) + "[" + (int) ((e.getHealth() + e.getAbsorptionAmount()) / e.getMaxHealth() * 100) + "%]";
        }
    }

    private void addNameHealthLine(List<String> lines, Entity entity, Formatting color, boolean addName, boolean addHealth) {
        if (getSetting(3).asToggle().getChild(3).asMode().mode == 1) {
            if (addName) {
                lines.add(color + entity.getName().getString());
            }

            if (addHealth) {
                lines.add(0, getHealthText(entity));
            }
        } else if (addName || addHealth) {
            lines.add((addName ? color + entity.getName().getString() + (addHealth ? " " : "") : "") + (addHealth ? getHealthText(entity) : ""));
        }
    }

    private Formatting getHealthColor(LivingEntity entity) {
        if (entity.getHealth() + entity.getAbsorptionAmount() > entity.getMaxHealth()) {
            return Formatting.YELLOW;
        } else if (entity.getHealth() + entity.getAbsorptionAmount() >= entity.getMaxHealth() * 0.7) {
            return Formatting.GREEN;
        } else if (entity.getHealth() + entity.getAbsorptionAmount() >= entity.getMaxHealth() * 0.4) {
            return Formatting.GOLD;
        } else if (entity.getHealth() + entity.getAbsorptionAmount() >= entity.getMaxHealth() * 0.1) {
            return Formatting.RED;
        } else {
            return Formatting.DARK_RED;
        }
    }

    private void drawItem(double x, double y, double z, double offX, double offY, double scale, ItemStack item) {
        MatrixStack matrix = WorldRenderUtils.drawGuiItem(x, y, z, offX, offY, scale, item);

        matrix.scale(-0.05F, -0.05F, 0.05f);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.enableDepthTest();
        RenderSystem.depthFunc(GL11.GL_ALWAYS);

        if (!item.isEmpty()) {
            int w = mc.textRenderer.getWidth("x" + item.getCount()) / 2;
            mc.textRenderer.draw("x" + item.getCount(), 7 - w, 3, 0xffffff, true, matrix.peek().getModel(),
                    mc.getBufferBuilders().getEntityVertexConsumers(), true, 0, 0xf000f0);
        }

        matrix.scale(0.85F, 0.85F, 1F);

        int c = 0;
        for (Map.Entry<Enchantment, Integer> m : EnchantmentHelper.get(item).entrySet()) {
            String text = I18n.translate(m.getKey().getName(2).getString());

            if (text.isEmpty())
                continue;

            String subText = text.substring(0, Math.min(text.length(), 2)) + m.getValue();

            int w1 = mc.textRenderer.getWidth(subText) / 2;
            mc.textRenderer.draw(subText, -2 - w1, c * 10 - 19,
                    m.getKey() == Enchantments.VANISHING_CURSE || m.getKey() == Enchantments.BINDING_CURSE ? 0xff5050 : 0xffb0e0,
                    true, matrix.peek().getModel(), mc.getBufferBuilders().getEntityVertexConsumers(), true, 0, 0xf000f0);
            c--;
        }

        RenderSystem.depthFunc(GL11.GL_LEQUAL);
        RenderSystem.disableDepthTest();

        RenderSystem.disableBlend();
    }

    private Vec3d getRenderPos(Entity e) {
        return mc.currentScreen != null && mc.currentScreen.isPauseScreen() ? e.getPos().add(0, e.getHeight(), 0)
                : new Vec3d(
                e.lastRenderX + (e.getX() - e.lastRenderX) * mc.getTickDelta(),
                (e.lastRenderY + (e.getY() - e.lastRenderY) * mc.getTickDelta()) + e.getHeight(),
                e.lastRenderZ + (e.getZ() - e.lastRenderZ) * mc.getTickDelta());
    }

}