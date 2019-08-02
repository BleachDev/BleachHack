package bleach.hack;

import bleach.hack.command.CommandManager;
import bleach.hack.gui.ScreenInjector;
import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.ClickGui;
import bleach.hack.utils.BleachQueue;
import bleach.hack.utils.file.BleachFileReader;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("bleachhack")
public class BleachHack {
	
	public static String VERSION = "B5.1";
	public static int INTVERSION = 8;
	
	private BleachFileReader fileReader = new BleachFileReader();
	
    public BleachHack() {
    	FMLJavaModLoadingContext.get().getModEventBus().addListener(this::init);
        MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(new ScreenInjector());
    }
    public void init(FMLClientSetupEvent event) {
    	fileReader.readModules();
    	fileReader.readSettings();
    	ClickGui.clickGui.initWindows();
    	fileReader.readClickGui();
    }
    
	@SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
		if(!(event.phase == Phase.END)) return;
		
		try {
			ModuleManager.onUpdate();
    		ModuleManager.updateKeys();
    		
    		if(Minecraft.getInstance().player.ticksExisted % 100 == 0) {
    			fileReader.saveModules();
    			fileReader.saveSettings();
    			fileReader.saveClickGui();
    		}
    		
    		BleachQueue.nextQueue();
    	}catch(Exception e){}
    }
    
    @SubscribeEvent
    public void onRender(RenderWorldLastEvent event) {
    	try { ModuleManager.onRender();
    	}catch(Exception e){}
    }
    
    @SubscribeEvent
    public void onChatMsg(ClientChatEvent event) {
    	if(event.getMessage().startsWith(".")) {
    		CommandManager cmd = new CommandManager();
    		cmd.callCommand(event.getMessage().substring(1));
    		event.setCanceled(true);
    	}
    }
}
