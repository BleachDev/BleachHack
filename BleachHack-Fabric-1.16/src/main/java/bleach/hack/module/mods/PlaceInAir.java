package bleach.hack.module.mods;

import com.google.common.eventbus.Subscribe;

import bleach.hack.event.events.EventSendPacket;
import bleach.hack.event.events.EventTick;
import bleach.hack.event.events.EventWorldRender;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingColor;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.util.RenderUtils;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;

public class PlaceInAir extends Module {
	private boolean pressed;
	public static boolean shouldSendPacket;
	
	public PlaceInAir () {
		super("PlaceInAir", KEY_UNBOUND, Category.WORLD, "Allows you to place blocks and entities in thin air",
				new SettingToggle("Rendered", true).withDesc("Renders an overlay where it will place the block / entity").withChildren(
					new SettingMode("Render", "Box+Fill", "Box", "Fill").withDesc("The rendering method"),
					new SettingSlider("Box", 0.1, 4, 2, 1).withDesc("The thickness of the box lines"),
					new SettingSlider("Fill", 0, 1, 0.3, 2).withDesc("The opacity of the fill"),
					new SettingColor("Color", 1f, 1f, 0f, false)));
	}
	
	@Subscribe
	public void onTick (EventTick event) {
		if (mc.options.keyUse.isPressed() && !pressed && mc.crosshairTarget instanceof BlockHitResult) {
			shouldSendPacket = false;
            PlayerInteractBlockC2SPacket packet = new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, (BlockHitResult) mc.crosshairTarget);
            mc.getNetworkHandler().sendPacket(packet);
            pressed = true;
		}
			
		else if (!mc.options.keyUse.isPressed()) {
			pressed = false;
		}
	}
	
	@Subscribe
	public void onWorldRender (EventWorldRender event) {
		if (mc.crosshairTarget instanceof BlockHitResult && getSetting(0).asToggle().state) {
			BlockPos pos = new BlockPos(mc.crosshairTarget.getPos());
			int mode = getSetting(0).asToggle().getChild(0).asMode().mode;
			if (mode == 0 || mode == 2) {
				float fillAlpha = (float) getSetting(0).asToggle().getChild(2).asSlider().getValue();
				float[] rgb = getSetting(0).asToggle().getChild(3).asColor().getRGBFloat();
				RenderUtils.drawFill(pos, rgb[0], rgb[1], rgb[2], fillAlpha);
			}
			
			if (mode == 0 || mode == 1) {
				float outlineWidth = (float) getSetting(0).asToggle().getChild(1).asSlider().getValue();
				float[] rgb = getSetting(0).asToggle().getChild(3).asColor().getRGBFloat();
				RenderUtils.drawOutline(pos, rgb[0], rgb[1], rgb[2], 1f, outlineWidth);
			}
		}
	}	
	
	@Subscribe
	public void onSendPacket(EventSendPacket event) {
		if (event.getPacket() instanceof PlayerInteractBlockC2SPacket)
			event.setCancelled(true);
	}
}
