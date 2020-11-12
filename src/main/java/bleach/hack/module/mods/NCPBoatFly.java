//package bleach.hack.module.mods;
//
//import baritone.api.event.events.TickEvent;
//import baritone.api.event.events.WorldEvent;
//import bleach.hack.event.events.EventEntityRemoved;
//import bleach.hack.event.events.EventReadPacket;
//import bleach.hack.event.events.EventSendPacket;
//import bleach.hack.module.Category;
//import bleach.hack.module.Module;
//import bleach.hack.utils.BleachLogger;
//import com.google.common.eventbus.Subscribe;
//import net.minecraft.entity.Entity;
//import net.minecraft.network.packet.c2s.play.BoatPaddleStateC2SPacket;
//import net.minecraft.network.packet.c2s.play.PlayerInputC2SPacket;
//import net.minecraft.network.packet.c2s.play.TeleportConfirmC2SPacket;
//import net.minecraft.network.packet.c2s.play.VehicleMoveC2SPacket;
//import net.minecraft.network.packet.s2c.play.EntityAttachS2CPacket;
//import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
//import net.minecraft.network.packet.s2c.play.UnloadChunkS2CPacket;
//import net.minecraft.util.math.Vec3d;
//
//public class NCPBoatFly extends Module {
//
//    public NCPBoatFly() {
//        super("NCPBoatFly", KEY_UNBOUND, Category.CHAT, "2b2t stinky. ez boatfly");
//    }
//
//    private Entity last_boat = null;
//
//    //@Subscribe
//    //public void onWorldUnload(UnloadChunkS2CPacket event) {
//    //    mc.player.dismountRidingEntity();
//    //    last_boat = null;
//    //}
//
//    @Subscribe
//    public void onEntityRemoved(EventEntityRemoved event) {
//        if (event.getEntity().equals(last_boat))
//            last_boat = null;
//    }
//
//    @Subscribe
//    public void onReceivePacket(EventReadPacket event) {
//        if (mc.player == null) return;
//        if (force_boat_pos.get() && event.getPacket() instanceof VehicleMoveC2SPacket &&
//                mc.player.getRidingEntity() != null) {
//            event.setCancelled(true);
//        }
//        if (event.getPacket() instanceof PlayerPositionLookS2CPacket) {
//            if (cancel_rubber.get() && last_boat != null && !mc.gameSettings.keyBindSneak.isKeyDown()) {
//                event.setCancelled(true);
//                if (!spoof.get()) {
//                    mc.player.networkHandler.sendPacket(
//                            new TeleportConfirmC2SPacket(((PlayerPositionLookS2CPacket) event.getPacket()).getTeleportId()));
//                }
//            }
//            if (remount.get() && last_boat != null && !MC.gameSettings.keyBindSneak.isKeyDown()) {
//                Vec3d pos = new Vec3d(last_boat.posX, last_boat.posY, last_boat.posZ);
//                mc.player.networkHandler.sendPacket(new EntityAttachS2CPacket(last_boat, EnumHand.MAIN_HAND, pos));
//                mc.player.networkHandler.sendPacket(new EntityAttachS2CPacket(last_boat, EnumHand.MAIN_HAND));
//            }
//        }
//    }
//
//    @Subscribe
//    public void onSendPacket(EventSendPacket event) {
//        if (mc.player == null) return;
//        if (spoof.get() && event.getPacket() instanceof EntityAttachS2CPacket &&
//                ((CPacketUseEntity) event.getPacket()).getAction() == Action.INTERACT_AT) {
//            event.setCancelled(true);
//        }
//        if (last_boat == null && mc.player.getRidingEntity() == null) return;
//        if (spoof.get() && (
//                event.getPacket() instanceof BoatPaddleStateC2SPacket ||
//                        event.getPacket() instanceof CPacketPlayer ||
//                        event.getPacket() instanceof PlayerInputC2SPacket ||
//                        event.getPacket() instanceof CPacketEntityAction ||
//                        event.getPacket() instanceof CPacketConfirmTeleport
//        )) {
//            event.setCancelled(true);
//        }
//    }
//
//    @SubscribeEvent
//    public void onPlaceBoat(PlayerInteractEvent.RightClickItem event) {
//        if (place_spoof.get() && event.getItemStack().getItem() instanceof ItemBoat) {
//            event.setCancelled(true);
//            mc.player.networkHandler.sendPacket(new CPacketPlayerTryUseItem(EnumHand.MAIN_HAND));
//        }
//    }
//
//    @SubscribeEvent // disable gravity
//    public void onLocalPlayerUpdate(LocalPlayerUpdateEvent event) {
//        ForgeHaxHooks.isNoBoatGravityActivated =
//                getRidingEntity() instanceof EntityBoat; // disable gravity if in boat
//    }
//
//    @Override
//    public void onDisabled() {
//        // ForgeHaxHooks.isNoClampingActivated = false; // disable view clamping
//        ForgeHaxHooks.isNoBoatGravityActivated = false; // disable gravity
//        ForgeHaxHooks.isBoatSetYawActivated = false;
//        // ForgeHaxHooks.isNotRowingBoatActivated = false; // items always usable - can not be disabled
//        last_boat = null;
//    }
//
//    @Override
//    public void onLoad() {
//        ForgeHaxHooks.isNoClampingActivated = noClamp.getAsBoolean();
//    }
//
//    @SubscribeEvent
//    public void onClientTick(TickEvent.ClientTickEvent event) {
//        if (mc.player == null) return;
//        // if (getModManager().get(FreecamMod.class).get().isEnabled()) return;
//
//        // check if the player is really riding a entity
//        if (mc.player.getRidingEntity() != null) {
//            last_boat = mc.player.getRidingEntity();
//
//            ForgeHaxHooks.isNoClampingActivated = noClamp.getAsBoolean();
//            ForgeHaxHooks.isBoatSetYawActivated = setYaw.getAsBoolean();
//            ForgeHaxHooks.isNoBoatGravityActivated = noGravity.getAsBoolean();
//
//            mc.player.getRidingEntity().motionY = 0f;
//
//            if (mc.gameSettings.keyBindJump.isKeyDown()) {
//                mc.player.getRidingEntity().onGround = force_on_ground.get();
//                mc.player.getRidingEntity().motionY += speedUp.get();
//            } else if (mc.gameSettings.keyBindSprint.isKeyDown()) {
//                mc.player.getRidingEntity().motionY -= speedDown.get();
//            } else if (MC.gameSettings.keyBindSneak.isKeyDown()) {
//                mc.player.dismountRidingEntity();
//                last_boat = null;
//                return;
//            }
//            setMoveSpeedEntity(speed.getAsDouble());
//        } else {
//            if (mc.gameSettings.keyBindSneak.isKeyDown()) {
//                mc.player.dismountRidingEntity();
//                last_boat = null;
//            } else if ((force_boat_pos.get()) && last_boat != null) {
//                mc.player.startRiding(last_boat, true);
//            }
//        }
//    }
//
//    public void setMoveSpeedEntity(double speed) {
//        if (mc.player != null && mc.player.getRidingEntity() != null) {
//            MovementInput movementInput = mc.player.movementInput;
//            double forward = movementInput.moveForward;
//            double strafe = movementInput.moveStrafe;
//            float yaw = MC.player.rotationYaw;
//
//            if ((forward == 0.0D) && (strafe == 0.0D)) {
//                mc.player.getRidingEntity().motionX = (0.0D);
//                mc.player.getRidingEntity().motionZ = (0.0D);
//            } else {
//                if (forward != 0.0D) {
//                    if (strafe > 0.0D) {
//                        yaw += (forward > 0.0D ? -45 : 45);
//                    } else if (strafe < 0.0D) {
//                        yaw += (forward > 0.0D ? 45 : -45);
//                    }
//
//                    strafe = 0.0D;
//
//                    if (forward > 0.0D) {
//                        forward = 1.0D;
//                    } else if (forward < 0.0D) {
//                        forward = -1.0D;
//                    }
//                }
//                mc.player.getRidingEntity().motionX =
//                        (forward * speed * Math.cos(Math.toRadians(yaw + 90.0F))
//                                + strafe * speed * Math.sin(Math.toRadians(yaw + 90.0F)));
//                mc.player.getRidingEntity().motionZ =
//                        (forward * speed * Math.sin(Math.toRadians(yaw + 90.0F))
//                                - strafe * speed * Math.cos(Math.toRadians(yaw + 90.0F)));
//            }
//        }
//    }
//
//}
