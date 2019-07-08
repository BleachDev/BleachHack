package bleach.hack;

import bleach.hack.command.CommandManager;
import bleach.hack.gui.BleachMainMenu;
import bleach.hack.module.ModuleManager;
import bleach.hack.utils.file.BleachFileReader;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.MainMenuScreen;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod("bleachhack")
public class BleachHack {
	
	public static String VERSION = "B1-DEMO-2";
	public static long tickCount = 0;
	
	private BleachFileReader fileReader = new BleachFileReader();
	
    public BleachHack() {
    	FMLJavaModLoadingContext.get().getModEventBus().addListener(this::init);
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    public void init(FMLCommonSetupEvent event) {
    	fileReader.readModules();
    	fileReader.readSettings();
    }
    
	@SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
		if(!(event.phase == Phase.END)) return;
		tickCount++;
		
		if(tickCount % 100 == 0) {
			fileReader.saveModules();
			fileReader.saveSettings();
		}
		
		if(Minecraft.getInstance().currentScreen instanceof MainMenuScreen) {
			Minecraft.getInstance().displayGuiScreen(new BleachMainMenu(null));
		}
		
    	try {ModuleManager.onUpdate();
    	}catch(Exception e){ /* World Not Loaded */ }
    }
    
    @SubscribeEvent
    public void onRender(RenderWorldLastEvent event) {
    	try { ModuleManager.onRender();
    	}catch(Exception e){ /* World Not Loaded */ }
    }
    
    @SubscribeEvent
    public void onKeyPress(KeyInputEvent event) {
    	ModuleManager.onKeyPressed(event.getKey());
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
