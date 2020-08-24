package bleach.hack.mixin;

import bleach.hack.module.Module;
import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.NoKeyBlock;
import net.minecraft.SharedConstants;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

@Mixin(SharedConstants.class)
public class MixinSharedConstants {

    @Overwrite
    public static boolean isValidChar(char chr) {
        Module noKeyBlock = ModuleManager.getModule(NoKeyBlock.class);

        if (!noKeyBlock.isToggled()) {
            return chr != 167 && chr >= ' ' && chr != 127;
        }

        return (noKeyBlock.getSetting(0).asToggle().state || chr != 167)
                && (noKeyBlock.getSetting(1).asToggle().state || chr >= ' ')
                && (noKeyBlock.getSetting(2).asToggle().state || chr != 127);
    }
}