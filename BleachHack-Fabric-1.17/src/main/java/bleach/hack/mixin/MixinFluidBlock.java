package bleach.hack.mixin;

import bleach.hack.module.ModuleManager;
import bleach.hack.module.mods.Jesus;
import net.minecraft.block.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(FluidBlock.class)
public abstract class MixinFluidBlock extends Block implements FluidDrainable
{
    private MixinFluidBlock(Settings block$Settings_1)
    {
        super(block$Settings_1);
    }

    @Override
    public VoxelShape getCollisionShape(BlockState blockState_1,
                                        BlockView blockView_1, BlockPos blockPos_1,
                                        ShapeContext entityContext_1)
    {
        if (!((Jesus) ModuleManager.getModule("Jesus")).isEnabled())
            return super.getCollisionShape(blockState_1, blockView_1, blockPos_1,
                    entityContext_1);

        MinecraftClient mc = MinecraftClient.getInstance();
        if(mc.player != null && mc.player.fallDistance <= 3 && !mc.options.keySneak.isPressed() && !mc.player.isTouchingWater())
            return VoxelShapes.fullCube();

        return super.getCollisionShape(blockState_1, blockView_1, blockPos_1,
                entityContext_1);
    }
}