//package bleach.hack.utils;
//
//import javafx.geometry.BoundingBox;
//import net.minecraft.client.MinecraftClient;
//import net.minecraft.structure.rule.AxisAlignedLinearPosRuleTest;
//
//public class PlayerUtils {
//    public static void PacketFacePitchAndYaw(float p_Pitch, float p_Yaw)
//    {
//        MinecraftClient mc = MinecraftClient.getInstance();
//        boolean l_IsSprinting = mc.player.isSprinting();
//
//        boolean l_IsSneaking = mc.player.isSneaking();
//
//        if (PlayerUtils.isCurrentViewEntity())
//        {
//            float l_Pitch = p_Pitch;
//            float l_Yaw = p_Yaw;
//
//             axisalignedbb = mc.player.getBoundingBox();
//            double l_PosXDifference = mc.player.posX - mc.player.lastReportedPosX;
//            double l_PosYDifference = axisalignedbb.minY - mc.player.lastReportedPosY;
//            double l_PosZDifference = mc.player.posZ - mc.player.lastReportedPosZ;
//            double l_YawDifference = (double)(l_Yaw - mc.player.lastReportedYaw);
//            double l_RotationDifference = (double)(l_Pitch - mc.player.lastReportedPitch);
//            ++mc.player.positionUpdateTicks;
//            boolean l_MovedXYZ = l_PosXDifference * l_PosXDifference + l_PosYDifference * l_PosYDifference + l_PosZDifference * l_PosZDifference > 9.0E-4D || mc.player.positionUpdateTicks >= 20;
//            boolean l_MovedRotation = l_YawDifference != 0.0D || l_RotationDifference != 0.0D;
//
//            if (mc.player.isRiding())
//            {
//                mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.motionX, -999.0D, mc.player.motionZ, l_Yaw, l_Pitch, mc.player.onGround));
//                l_MovedXYZ = false;
//            }
//            else if (l_MovedXYZ && l_MovedRotation)
//            {
//                mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(mc.player.posX, axisalignedbb.minY, mc.player.posZ, l_Yaw, l_Pitch, mc.player.onGround));
//            }
//            else if (l_MovedXYZ)
//            {
//                mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, axisalignedbb.minY, mc.player.posZ, mc.player.onGround));
//            }
//            else if (l_MovedRotation)
//            {
//                mc.player.connection.sendPacket(new CPacketPlayer.Rotation(l_Yaw, l_Pitch, mc.player.onGround));
//            }
//            else if (mc.player.prevOnGround != mc.player.onGround)
//            {
//                mc.player.connection.sendPacket(new CPacketPlayer(mc.player.onGround));
//            }
//
//            if (l_MovedXYZ)
//            {
//                mc.player.lastReportedPosX = mc.player.posX;
//                mc.player.lastReportedPosY = axisalignedbb.minY;
//                mc.player.lastReportedPosZ = mc.player.posZ;
//                mc.player.positionUpdateTicks = 0;
//            }
//
//            if (l_MovedRotation)
//            {
//                mc.player.lastReportedYaw = l_Yaw;
//                mc.player.lastReportedPitch = l_Pitch;
//            }
//
//            mc.player.prevOnGround = mc.player.onGround;
//            mc.player.autoJumpEnabled = mc.player.mc.gameSettings.autoJump;
//        }
//    }
//    public static boolean isCurrentViewEntity()
//    {
//        MinecraftClient mc = MinecraftClient.getInstance();
//        return mc.cameraEntity == mc.player;
//    }
//}
