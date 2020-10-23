package bleach.hack.module.mods;

import bleach.hack.event.events.EventTick;
import bleach.hack.event.events.EventWorldRender;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingColor;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.utils.RenderUtils;
import com.google.common.eventbus.Subscribe;
import net.minecraft.client.util.InputUtil;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult.Type;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import org.lwjgl.glfw.GLFW;

public class ClickTp extends Module {

    private BlockPos pos = null;
    private Direction dir = null;

    private boolean antiSpamClick = false;

    public ClickTp() {
        super("ClickTP", KEY_UNBOUND, Category.MOVEMENT, "Allows you to teleport by clicking",
                new SettingToggle("In Air", true).withDesc("Teleports even if you are pointing in the air"),
                new SettingToggle("Liquids", false).withDesc("Interacts with liquids"),
                new SettingToggle("Y First", false).withDesc("Sets you to the correct Y level first, then to your XZ coords, might fix going through walls"),
                new SettingToggle("Always Up", false).withDesc("Always teleports you to the top of blocks instead of sides"),
                new SettingColor("Highlight", 0.333f, 0.333f, 1f, false));
    }

    public void onDisable() {
        pos = null;
        dir = null;

        super.onDisable();
    }

    @Subscribe
    public void onWorldRender(EventWorldRender event) {
        if (pos != null && dir != null) {
            float[] col = getSetting(4).asColor().getRGBFloat();
            RenderUtils.drawFilledBox(new Box(
                            pos.getX() + (dir == Direction.EAST ? 0.95 : 0), pos.getY() + (dir == Direction.UP ? 0.95 : 0), pos.getZ() + (dir == Direction.SOUTH ? 0.95 : 0),
                            pos.getX() + (dir == Direction.WEST ? 0.05 : 1), pos.getY() + (dir == Direction.DOWN ? 0.05 : 1), pos.getZ() + (dir == Direction.NORTH ? 0.05 : 1)),
                    col[0], col[1], col[2], 1f);
        }
    }

    @Subscribe
    public void onTick(EventTick event) {
        if (InputUtil.isKeyPressed(mc.getWindow().getHandle(), GLFW.GLFW_KEY_LEFT_SHIFT)) {
            pos = null;
            dir = null;
            return;
        }

        BlockHitResult hit = (BlockHitResult) mc.player.raycast(100, mc.getTickDelta(), getSetting(1).asToggle().state);

        boolean miss = hit.getType() == Type.MISS && !getSetting(0).asToggle().state;

        pos = miss ? null : hit.getBlockPos();
        dir = miss ? null : getSetting(3).asToggle().state ? Direction.UP : hit.getSide();

        if (pos != null && dir != null) {
            if (GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_RIGHT) == 1 && mc.currentScreen == null && !antiSpamClick) {
                antiSpamClick = true;

                BlockPos tpPos = new BlockPos(pos.offset(dir, dir == Direction.DOWN ? 2 : 1)).add(0.5, 0, 0.5);

                if (getSetting(2).asToggle().state) {
                    mc.player.updatePosition(mc.player.getX(), tpPos.getY(), mc.player.getZ());
                    mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(mc.player.getX(), tpPos.getY(), mc.player.getZ(), false));
                }

                mc.player.updatePosition(tpPos.getX(), tpPos.getY(), tpPos.getZ());
                mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionOnly(tpPos.getX(), tpPos.getY(), tpPos.getZ(), false));
            } else if (GLFW.glfwGetMouseButton(mc.getWindow().getHandle(), GLFW.GLFW_MOUSE_BUTTON_RIGHT) == 0) {
                antiSpamClick = false;
            }
        }
    }
}