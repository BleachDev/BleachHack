package bleach.hack.module.mods;

import bleach.hack.event.events.EventEntityRender;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import com.google.common.eventbus.Subscribe;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.DonkeyEntity;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.entity.passive.MuleEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;


public class DonkeyAlert extends Module {

    public DonkeyAlert(){
        super("Donkey Alert", KEY_UNBOUND, Category.WORLD, "Stole this shit from aurora");
    }

    private int antiSpam;

    @Subscribe
    public void onLivingRender(EventEntityRender.Render event) {
        if(mc.world == null){
            return;
        }
        ++this.antiSpam;
        for (final Entity e : mc.world.getEntities()) {
            if (e instanceof DonkeyEntity && this.antiSpam >= 100) {
                mc.inGameHud.getChatHud().addMessage(new LiteralText(Formatting.BLUE + "[Epearl Hack] Found Donkey! X:" + Formatting.WHITE + (int)e.getX() + Formatting.BLUE + " Z:" + Formatting.WHITE + (int)e.getZ()));
                this.antiSpam = -600;
            }
            if (e instanceof MuleEntity && this.antiSpam >= 100) {
                mc.inGameHud.getChatHud().addMessage(new LiteralText(Formatting.BLUE + "[Epearl Hack] Found Mule! X:" + Formatting.WHITE + (int)e.getX() + Formatting.BLUE + " Z:" + Formatting.WHITE + (int)e.getZ()));
                this.antiSpam = -600;
            }
            if (e instanceof LlamaEntity && this.antiSpam >= 100) {
                mc.inGameHud.getChatHud().addMessage(new LiteralText(Formatting.BLUE + "[Epearl Hack] Found Llama! X:" + Formatting.WHITE + (int)e.getX() + Formatting.BLUE + " Z:" + Formatting.WHITE + (int)e.getZ()));
                this.antiSpam = -600;
            }
        }
    }
}
