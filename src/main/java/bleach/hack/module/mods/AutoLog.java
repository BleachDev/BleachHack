package bleach.hack.module.mods;

import bleach.hack.BleachHack;
import bleach.hack.event.events.EventEntityRender;
import bleach.hack.event.events.EventTick;
import bleach.hack.event.events.EventWorldRenderEntity;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.utils.EntityUtils;
import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.mob.Monster;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.s2c.play.DisconnectS2CPacket;
import net.minecraft.text.LiteralText;

import java.util.Iterator;

public class AutoLog extends Module {

    public AutoLog()
    {
        super("AutoLog", KEY_UNBOUND, Category.COMBAT, "Automatically Logs out when ___",
                new SettingToggle("Health", false),
                new SettingSlider("Health: ", 0.0D, 20.0D, 5.0D, 0),
                new SettingToggle("Totems", true),
                new SettingSlider("Totems: ", 0.0D, 6.0D, 0.0D, 0),
                new SettingMode("Crystal: ", "None", "Near", "Near+No Totem", "Near+Health"),
                new SettingSlider("CrystalRange: ", 0.0D, 8.0D, 4.0D, 2),
                new SettingToggle("Nearby Player", false),
                new SettingSlider("Range: ", 0.0D, 100.0D, 20.0D, 1),
                new SettingMode("Nearby Mode","Range","Render"));
    }


    @Subscribe
    public void onLivingLabelRender(EventEntityRender.Label event) {
        if (event.getEntity() instanceof PlayerEntity && getSetting(6).asToggle().state && getSetting(8).asMode().mode == 1) {
            if (!event.getEntity().getName().equals(this.mc.player.getName()) && !BleachHack.friendMang.has(event.getEntity().getDisplayName().asString()))
            {
                this.logOut(event.getEntity().getDisplayName().asString()+" entered render distance, logged off.");
            }
        }
    }

    @Subscribe
    public void onTick(EventTick event)
    {
        if (!mc.player.isCreative())
        {
            if (this.getSettings().get(0).asToggle().state && (double) this.mc.player.getHealth() < this.getSettings().get(1).asSlider().getValue())
            {
                this.logOut("Logged Out At " + this.mc.player.getHealth() + " Health");
            } else
            {
                if (this.getSettings().get(2).asToggle().state)
                {
                    int t = this.getTotems();

                    if (t <= (int) this.getSettings().get(3).asSlider().getValue())
                    {
                        this.logOut("Logged Out With " + t + " Totems Left");

                        return;
                    }
                }

                Iterator entityIter;

                if (this.getSettings().get(4).asMode().mode != 0)
                {
                    entityIter = this.mc.world.getEntities().iterator();

                    while (entityIter.hasNext())
                    {
                        Entity e = (Entity) entityIter.next();

                        if (e instanceof EndCrystalEntity)
                        {
                            double d = this.mc.player.distanceTo(e);

                            if (d <= this.getSettings().get(5).asSlider().getValue() && (this.getSettings().get(4).asMode().mode == 1
                                    || this.getSettings().get(4).asMode().mode == 2 && this.getTotems() <= (int) this.getSettings().get(3).asSlider().getValue()
                                    || this.getSettings().get(4).asMode().mode == 3 && (double) this.mc.player.getHealth() < this.getSettings().get(1).asSlider().getValue()))
                            {
                                this.logOut("Logged Out " + d + " Blocks Away From A Crystal");

                                return;
                            }
                        }
                    }
                }

                if (this.getSettings().get(6).asToggle().state)
                {
                    entityIter = this.mc.world.getPlayers().iterator();

                    while (entityIter.hasNext())
                    {
                        Entity e = (Entity) entityIter.next();

                        if (!e.getName().equals(this.mc.player.getName()) && (double) this.mc.player.distanceTo(e) <= this.getSettings().get(7).asSlider().getValue() && !BleachHack.friendMang.has(e.getDisplayName().asString()))
                        {
                            this.logOut("Logged Out " + this.mc.player.distanceTo(e) + " Blocks Away From A Player (" + e.getDisplayName().asString() + ")");
                        }
                    }
                }

            }
        }
    }

    private int getTotems()
    {
        int c = 0;

        for (int i = 0; i < 45; ++i)
        {
            if (this.mc.player.inventory.getStack(i).getItem() == Items.TOTEM_OF_UNDYING)
            {
                ++c;
            }
        }

        return c;
    }

    private void logOut(String reason)
    {
        this.mc.player.networkHandler.onDisconnect(new DisconnectS2CPacket(new LiteralText(reason)));
        this.setToggled(false);
    }

}
