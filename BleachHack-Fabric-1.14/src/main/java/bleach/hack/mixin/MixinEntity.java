package bleach.hack.mixin;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.SafeWalk;
import bleach.hack.module.mods.Scaffold;
import net.minecraft.entity.Entity;

@Mixin(Entity.class)
public class MixinEntity {

	@Redirect(
			at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;isSneaking()Z", opcode = Opcodes.INVOKEVIRTUAL, ordinal = 0),
			method = "clipSneakingMovement(Lnet/minecraft/util/math/Vec3d;Lnet/minecraft/entity/MovementType;)Lnet/minecraft/util/math/Vec3d;")
	private boolean isSneaking(Entity entity) {
		return entity.isSneaking() || ModuleManager.getModule(SafeWalk.class).isToggled()
				|| (ModuleManager.getModule(Scaffold.class).isToggled() && ModuleManager.getModule(Scaffold.class).getSetting(3).asToggle().state);
	}
}
