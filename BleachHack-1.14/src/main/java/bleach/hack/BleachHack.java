package bleach.hack;

import bleach.hack.command.CommandManager;
import bleach.hack.gui.BleachMainMenu;
import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.ClickGui;
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
	
	public static String VERSION = "B2";
	public static int INTVERSION = 2;
	
	private BleachFileReader fileReader = new BleachFileReader();
	
    public BleachHack() {
    	FMLJavaModLoadingContext.get().getModEventBus().addListener(this::init);
        MinecraftForge.EVENT_BUS.register(this);
    }
    
    public void init(FMLCommonSetupEvent event) {
    	fileReader.readModules();
    	fileReader.readSettings();
    	ClickGui.clickGui.initWindows();
    }
    
	@SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
		if(!(event.phase == Phase.END)) return;
		
		if(Minecraft.getInstance().currentScreen instanceof MainMenuScreen) {
			Minecraft.getInstance().displayGuiScreen(new BleachMainMenu(null));
		}
		
    	try {
    		ModuleManager.onUpdate();
    		
    		if(Minecraft.getInstance().player.ticksExisted % 100 == 0) {
    			fileReader.saveModules();
    			fileReader.saveSettings();
    		}
    	}catch(Exception e){ /* World Not Loaded. */ }
    }
    
    @SubscribeEvent
    public void onRender(RenderWorldLastEvent event) {
    	try { ModuleManager.onRender();
    	}catch(Exception e){ /* World Not Loaded. */ }
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
