package bleach.hack;

import bleach.hack.module.ModuleManager;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.gameevent.TickEvent.RenderTickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.WorldTickEvent;

@Mod("bleachhack")
public class BleachHack {
	
    public BleachHack() {
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    @SubscribeEvent
    public void onTick(WorldTickEvent event) {
    	ModuleManager.onUpdate();
    }
    
    @SubscribeEvent
    public void onRender(RenderTickEvent event) {
    	ModuleManager.onRender();
    }
    
    @SubscribeEvent
    public void onKeyPress(KeyInputEvent event) {
    	ModuleManager.onKeyPressed(event.getKey());
    }
}
