package bleach.hack.module.mods;

import bleach.hack.BleachHack;
import bleach.hack.event.events.*;
import bleach.hack.module.Category;
import bleach.hack.module.Module;
import bleach.hack.setting.base.SettingMode;
import bleach.hack.setting.base.SettingSlider;
import bleach.hack.setting.base.SettingToggle;
import bleach.hack.setting.other.SettingRotate;
import bleach.hack.utils.CrystalUtils;
import bleach.hack.utils.EntityUtils;
import bleach.hack.utils.Timer;
import bleach.hack.utils.WorldUtils;
import com.google.common.eventbus.Subscribe;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolItem;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RayTraceContext;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

import static net.minecraft.util.Hand.MAIN_HAND;

public class CrystalAuraRewrite extends Module
{

    public CrystalAuraRewrite()
    {
        super("CrystalAuraRewrite", KEY_UNBOUND, Category.COMBAT, "Improves the CA",
                new SettingMode("BreakMode", "Always", "Smart", "OnlyOwn").withDesc("Mode of break"),
                new SettingMode("PlaceMode", "Most", "Lethal").withDesc("Mode of place"),
                new SettingSlider("PlaceRadius", 0, 5, 4, 1).withDesc("Radius for place"),
                new SettingSlider("BreakRadius", 0, 5, 4, 1).withDesc("Radius for break"),
                new SettingSlider("WallsRange", 0, 5, 3.5, 1).withDesc("Max distance through walls"),
                new SettingToggle("MultiPlace", false).withDesc("Tries to multiplace"),
                new SettingSlider("Ticks", 0, 20, 2, 0).withDesc("Ticks to ignore on Client Update"),
                new SettingSlider("MinDMG", 0, 20, 4, 1).withDesc("Min damage to enemy"),
                new SettingSlider("MaxSelfDMG", 0, 20, 4, 1).withDesc("Max damage you yourself"),
                new SettingSlider("FacePlace", 0, 20, 8, 1).withDesc("Required health for faceplace"),
                new SettingToggle("AutoSwitch", true).withDesc("Switch to crystal automatically"),
                new SettingToggle("PauseIfNeeded", true).withDesc("Pause when eating, breaking block, etc"),
                new SettingToggle("NoSuicide", true).withDesc("Try not to kill yourself"),
                new SettingToggle("AntiWeakness", true).withDesc("Switches to a sword to try to break crystals"),
                new SettingRotate(true));

    }

    private CrystalAuraRewrite Mod = null;
    public static Timer _removeVisualTimer = new Timer();
    private Timer _rotationResetTimer = new Timer();
    private ConcurrentLinkedQueue<BlockPos> _placedCrystals = new ConcurrentLinkedQueue<>();
    private ConcurrentHashMap<BlockPos, Float> _placedCrystalsDamage = new ConcurrentHashMap<>();
    private double[] _rotations = null;
    private ConcurrentHashMap<EndCrystalEntity, Integer> _attackedEnderCrystals = new ConcurrentHashMap<>();
    private String _lastTarget = null;
    private int _remainingTicks;
    private BlockPos _lastPlaceLocation = BlockPos.ORIGIN;
    private boolean _surround = false;
    final MinecraftClient mc = MinecraftClient.getInstance();
    final ClientPlayerEntity player = mc.player;

    @Override
    public void onEnable()
    {
        super.onEnable();

        // clear placed crystals, we don't want to display them later on
        _placedCrystals.clear();
        _placedCrystalsDamage.clear();

        // also reset ticks on enable, we need as much speed as we can get.
        _remainingTicks = 0;

        // reset this, we will get a new one
        _lastPlaceLocation = BlockPos.ORIGIN;
    }

    @Subscribe
    public void onRemEntity(EventEntityRemoved event)
    {
        if (event.GetEntity() instanceof EndCrystalEntity)
        {
            // we don't need null things in this list.
            _attackedEnderCrystals.remove((EndCrystalEntity) event.GetEntity());
        }
    }

    private boolean ValidateCrystal(EndCrystalEntity e)
    {
        if (e == null || !e.isAlive())
            return false;

        if (_attackedEnderCrystals.containsKey(e) && _attackedEnderCrystals.get(e) > 5)
            return false;

        if (e.distanceTo(mc.player) > (!mc.player.canSee(e) ? getSetting(5).asSlider().getValue() : getSetting(4).asSlider().getValue()))
            return false;

        switch (getSetting(0).asMode().mode)
        {
            case 0:
                return e.squaredDistanceTo(e.getX(), e.getY(), e.getZ()) <= 3;
            case 1:
                float selfDamage = CrystalUtils.calculateDamage(mc.world, e.getX(), e.getY(), e.getZ(), mc.player, 0);

                if (selfDamage > getSetting(8).asSlider().getValue())
                    return false;

                if (getSetting(12).asToggle().state && selfDamage >= mc.player.getHealth()+mc.player.getAbsorptionAmount())
                    return false;

                // iterate through all players, and crystal positions to find the best position for most damage
                for (PlayerEntity player : mc.world.getPlayers())
                {
                    // Ignore if the player is us, a friend, dead, or has no health (the dead variable is sometimes delayed)
                    if (player == mc.player || BleachHack.friendMang.has(player.getName().asString()) || !mc.player.isAlive() || (mc.player.getHealth() + mc.player.getAbsorptionAmount()) <= 0.0f)
                        continue;

                    // store this as a variable for faceplace per player
                    double minDamage = getSetting(7).asSlider().getValue();

                    // check if players health + gap health is less than or equal to faceplace, then we activate faceplacing
                    if (player.getHealth() + player.getAbsorptionAmount() <= getSetting(9).asSlider().getValue())
                        minDamage = 1f;

                    float calculatedDamage = CrystalUtils.calculateDamage(mc.world,  e.getX(), e.getY(), e.getZ(), player, 0);

                    if (calculatedDamage > minDamage)
                        return true;
                }
                return false;
            case 2:
            default:
                break;
        }

        return true;
    }

    /*
     * Returns nearest crystal to an entity, if the crystal is not null or dead
     * @entity - entity to get smallest distance from
     */
    public EndCrystalEntity GetNearestCrystalTo(Entity entity, final BlockPos pos)
    {
        return mc.world.getOtherEntities(null, new Box(pos.add(0, 1, 0))).stream().filter(e -> e instanceof EndCrystalEntity && ValidateCrystal((EndCrystalEntity) e)).map(e -> (EndCrystalEntity)e).min(Comparator.comparing(e -> entity.distanceTo(e))).orElse(null);
    }

    public void AddAttackedCrystal(EndCrystalEntity crystal)
    {
        if (_attackedEnderCrystals.containsKey(crystal))
        {
            int value = _attackedEnderCrystals.get(crystal);
            _attackedEnderCrystals.put(crystal, value + 1);
        }
        else
            _attackedEnderCrystals.put(crystal, 1);
    }

    private boolean VerifyCrystalBlocks(BlockPos pos)
    {
        final MinecraftClient mc = MinecraftClient.getInstance();

        // check distance
        if (mc.player.squaredDistanceTo(Vec3d.of(pos)) > getSetting(2).asSlider().getValue() * getSetting(2).asSlider().getValue())
            return false;

        // check self damage
        float selfDamage = CrystalUtils.calculateDamage(mc.world, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, mc.player, 0);

        // make sure self damage is not greater than maxselfdamage
        if (selfDamage > getSetting(8).asSlider().getValue())
            return false;

        // no suicide, verify self damage won't kill us
        if (getSetting(12).asToggle().state && selfDamage >= mc.player.getHealth()+mc.player.getAbsorptionAmount())
            return false;

        // it's an ok position.
        return true;
    }

    @Subscribe
    public void onTick(EventTick event)
    {
        // this is our 1 second timer to remove our attackedEnderCrystals list, and remove the first placedCrystal for the visualizer.
        if (_removeVisualTimer.passed(1000))
        {
            _removeVisualTimer.reset();

            if (!_placedCrystals.isEmpty())
            {
                BlockPos removed = _placedCrystals.remove();

                if (removed != null)
                    _placedCrystalsDamage.remove(removed);
            }

            _attackedEnderCrystals.clear();
        }

        if (NeedPause())
        {
            _remainingTicks = 0;
            return;
        }

        // override
        if (getSetting(1).asMode().mode == 1 && _lastPlaceLocation != BlockPos.ORIGIN)
        {
            float damage = 0f;

            PlayerEntity trappedTarget = null;

            // verify that this location will exceed lethal damage for atleast one enemy.
            // iterate through all players, and crystal positions to find the best position for most damage
            for (PlayerEntity player : mc.world.getPlayers())
            {
                // Ignore if the player is us, a friend, dead, or has no health (the dead variable is sometimes delayed)
                if (player == mc.player || BleachHack.friendMang.has(player.getName().asString()) || !mc.player.isAlive() || (mc.player.getHealth() + mc.player.getAbsorptionAmount()) <= 0.0f)
                    continue;

                // store this as a variable for faceplace per player
                double minDamage = getSetting(7).asSlider().getValue();

                // check if players health + gap health is less than or equal to faceplace, then we activate faceplacing
                if (player.getHealth() + player.getAbsorptionAmount() <= getSetting(9).asSlider().getValue())
                    minDamage = 1f;

                float calculatedDamage = CrystalUtils.calculateDamage(mc.world, _lastPlaceLocation.getX() + 0.5, _lastPlaceLocation.getY() + 1.0, _lastPlaceLocation.getZ() + 0.5, player, 0);

                if (calculatedDamage >= minDamage && calculatedDamage > damage)
                {
                    damage = calculatedDamage;
                    trappedTarget = player;
                }
            }

            if (damage == 0f || trappedTarget == null)
            {
                // set this back to null
                _lastPlaceLocation = BlockPos.ORIGIN;
            }
        }


        if (_remainingTicks > 0)
        {
            --_remainingTicks;
        }

        boolean skipUpdateBlocks = _lastPlaceLocation != BlockPos.ORIGIN && getSetting(1).asMode().mode == 1;

        // create a list of available place locations
        ArrayList<BlockPos> placeLocations = new ArrayList<BlockPos>();
        PlayerEntity playerTarget = null;

        // if we don't need to skip update, get crystal blocks
        if (!skipUpdateBlocks && _remainingTicks <= 0)
        {
            _remainingTicks = (int) getSetting(6).asSlider().getValue();

            // this is the most expensive code, we need to get valid crystal blocks. -> todo verify stream to see if it's slower than normal looping.
            final List<BlockPos> cachedCrystalBlocks = CrystalUtils.findCrystalBlocks(mc.player, (float) getSetting(2).asSlider().getValue()).stream().filter(pos -> VerifyCrystalBlocks(pos)).collect(Collectors.toList());

            // this is where we will iterate through all players (for most damage) and cachedCrystalBlocks
            if (!cachedCrystalBlocks.isEmpty())
            {
                float damage = 0f;
                String target = null;

                // iterate through all players, and crystal positions to find the best position for most damage
                for (PlayerEntity player : mc.world.getPlayers())
                {
                    // Ignore if the player is us, a friend, dead, or has no health (the dead variable is sometimes delayed)
                    if (player == mc.player || BleachHack.friendMang.has(player.getName().asString()) || !mc.player.isAlive() || (mc.player.getHealth() + mc.player.getAbsorptionAmount()) <= 0.0f)
                        continue;

                    // store this as a variable for faceplace per player
                    double minDamage = getSetting(7).asSlider().getValue();

                    // check if players health + gap health is less than or equal to faceplace, then we activate faceplacing
                    if (player.getHealth() + player.getAbsorptionAmount() <= getSetting(9).asSlider().getValue())
                        minDamage = 1f;

                    // iterate through all valid crystal blocks for this player, and calculate the damages.
                    for (BlockPos pos : cachedCrystalBlocks)
                    {
                        float calculatedDamage = CrystalUtils.calculateDamage(mc.world, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, player, 0);

                        if (calculatedDamage >= minDamage && calculatedDamage > damage)
                        {
                            damage = calculatedDamage;
                            if (!placeLocations.contains(pos))
                                placeLocations.add(pos);
                            target = player.getName().asString();
                            playerTarget = player;
                        }
                    }
                }

                // playerTarget can nullptr during client tick
                if (playerTarget != null)
                {
                    // the player could have died during this code run, wait till next tick for doing more calculations.
                    if (!playerTarget.isAlive() || playerTarget.getHealth() <= 0.0f)
                        return;

                    // ensure we have place locations
                    if (!placeLocations.isEmpty())
                    {
                        // store this as a variable for faceplace per player
                        double minDamage = getSetting(7).asSlider().getValue();

                        // check if players health + gap health is less than or equal to faceplace, then we activate faceplacing
                        if (playerTarget.getHealth() + playerTarget.getAbsorptionAmount() <= getSetting(9).asSlider().getValue())
                            minDamage = 1f;

                        final double finalMinDamage = minDamage;
                        final PlayerEntity finalTarget = playerTarget;

                        // iterate this again, we need to remove some values that are useless, since we iterated all players
                        placeLocations.removeIf(pos -> CrystalUtils.calculateDamage(mc.world, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, finalTarget, 0) < finalMinDamage);

                        // at this point, the place locations list is in asc order, we need to reverse it to get to desc
                        Collections.reverse(placeLocations);

                        // store our last target name.
                        _lastTarget = target;
                    }
                }
            }
        }

        // at this point, we are going to destroy/place crystals.

        // Get nearest crystal to the player, we will need to null check this on the timer.
        EndCrystalEntity crystal = GetNearestCrystalTo(mc.player, mc.player.getBlockPos());

        // get a valid crystal in range, and check if it's in break radius
        boolean isValidCrystal = crystal != null ? mc.player.distanceTo(crystal) < getSetting(3).asSlider().getValue() : false;

        // no where to place or break
        if (!isValidCrystal && placeLocations.isEmpty() && !skipUpdateBlocks)
        {
            _remainingTicks = 0;
            return;
        }

        if (isValidCrystal && (skipUpdateBlocks ? true : _remainingTicks == getSetting(6).asSlider().getValue())) // we are checking null here because we don't want to waste time not destroying crystals right away
        {
            if (getSetting(13).asToggle().state && mc.player.hasStatusEffect(StatusEffect.byRawId(18)))
            {
                if (mc.player.inventory.getMainHandStack() == ItemStack.EMPTY || (!(mc.player.inventory.getMainHandStack().getItem() instanceof SwordItem) && !(mc.player.inventory.getMainHandStack().getItem() instanceof ToolItem)))
                {
                    for (int i = 0; i < 9; ++i)
                    {
                        ItemStack stack = mc.player.inventory.getStack(i);

                        if (stack.isEmpty())
                            continue;

                        if (stack.getItem() instanceof ToolItem || stack.getItem() instanceof SwordItem)
                        {
                            mc.player.inventory.selectedSlot = 1;
                            mc.player.inventory.swapSlotWithHotbar(i);
                            break;
                        }
                    }
                }
            }

            // get facing rotations to the crystal
            _rotations = EntityUtils.calculateLookAt(crystal.getX() + 0.5, crystal.getY() - 0.5, crystal.getZ() + 0.5, mc.player);
            _rotationResetTimer.reset();

            // swing arm and attack the entity
            mc.player.attack(crystal);
            mc.player.swingHand(MAIN_HAND);
            AddAttackedCrystal(crystal);

            // if we are not multiplacing return here, we have something to do for this tick.
            if (!getSetting(5).asToggle().state)
                return;
        }

        // verify the placeTimer is ready, selectedPosition is not 0,0,0 and the event isn't already cancelled
        if (!placeLocations.isEmpty() || skipUpdateBlocks)
        {
            // auto switch
            if (getSetting(10).asToggle().state)
            {
                if (mc.player.getOffHandStack().getItem() != Items.END_CRYSTAL)
                {
                    if (mc.player.getOffHandStack().getItem() != Items.END_CRYSTAL)
                    {
                        for (int i = 0; i < 9; ++i)
                        {
                            ItemStack stack = mc.player.inventory.getStack(i);

                            if (!stack.isEmpty() && stack.getItem() == Items.END_CRYSTAL)
                            {
                                mc.player.inventory.selectedSlot = i;
                                break;
                            }
                        }
                    }
                }
            }

            // no need to process the code below if we are not using off hand crystal or main hand crystal
            if (mc.player.getMainHandStack().getItem() != Items.END_CRYSTAL && mc.player.getOffHandStack().getItem() != Items.END_CRYSTAL)
                return;

            BlockPos selectedPos = null;

            // iterate through available place locations
            if (!skipUpdateBlocks)
            {
                for (BlockPos pos : placeLocations)
                {
                    // verify we can still place crystals at this location, if we can't we try next location
                    if (CrystalUtils.canPlaceCrystal(pos))
                    {
                        selectedPos = pos;
                        break;
                    }
                }
            }
            else
                selectedPos = _lastPlaceLocation;

            // nothing found... this is bad, wait for next tick to correct it
            if (selectedPos == null)
            {
                _remainingTicks = 0;
                return;
            }

            // get facing rotations to the position, store them for the motion tick to handle it
            _rotations = EntityUtils.calculateLookAt(selectedPos.getX() + 0.5, selectedPos.getY() - 0.5, selectedPos.getZ() + 0.5, mc.player);
            _rotationResetTimer.reset();

            // create a raytrace between player's position and the selected block position
//            assert mc.world != null;
//            RayTraceResult result = mc.world.rayTraceBlock(new Vec3d(mc.player.getX(), mc.player.getY() + mc.player.getEyeHeight(EntityPose.STANDING), mc.player.getZ()), new Vec3d(selectedPos.getX() + 0.5, selectedPos.getY() - 0.5, selectedPos.getZ() + 0.5));
//
//            // this will allow for bypassing placing through walls afaik
//            EnumFacing facing;
//
//            if (result == null || result.sideHit == null)
//                facing = EnumFacing.UP;
//            else
//                facing = result.sideHit;
//
//            mc.getConnection().sendPacket(new CPacketPlayerTryUseItemOnBlock(selectedPos, facing,
//                    mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, 0, 0, 0));

            // if placedcrystals already contains this position, remove it because we need to have it at the back of the list

            if (getSetting(5).asRotate().state) {
                WorldUtils.facePosAuto(crystal.getX(), crystal.getY(), crystal.getZ(), getSetting(15).asRotate());
            }

            Direction f;
            if (!getSetting(4).asToggle().getChild(1).asToggle().state) {
                f = Direction.UP;
            } else {
                BlockHitResult result = mc.world.rayTrace(new RayTraceContext(
                        new Vec3d(mc.player.getX(), mc.player.getY() + mc.player.getEyeHeight(mc.player.getPose()), mc.player.getZ()),
                        new Vec3d(crystal.getX() + 0.5D, crystal.getY() - 0.5D, crystal.getZ() + 0.5D),
                        RayTraceContext.ShapeType.OUTLINE, RayTraceContext.FluidHandling.NONE, mc.player));

                if (_placedCrystals.contains(selectedPos))
                    _placedCrystals.remove(selectedPos);

                // adds the selectedPos to the back of the placed crystals list
                _placedCrystals.add(selectedPos);

                if (playerTarget != null) {
                    float calculatedDamage = CrystalUtils.calculateDamage(mc.world, selectedPos.getX() + 0.5, selectedPos.getY() + 1.0, selectedPos.getZ() + 0.5, playerTarget, 0);

                    _placedCrystalsDamage.put(selectedPos, calculatedDamage);
                }

                if (_lastPlaceLocation != BlockPos.ORIGIN && _lastPlaceLocation == selectedPos) {
                    // reset ticks, we don't need to do more rotations for this position, so we can crystal faster.
                    if (getSetting(1).asMode().mode == 1)
                        _remainingTicks = 0;
                } else // set this to our last place location
                    _lastPlaceLocation = selectedPos;
            }
        }
    }

    @Subscribe
    public void onClientMove(EventClientMove event) {
        // we only want to run this event on pre motion, but don't reset rotations here
        if (event.isCancelled())
        {
            _rotations = null;
            return;
        }

        // if the previous event isn't cancelled, or if we don't need to pause.
        if (NeedPause())
        {
            _rotations = null;
            return;
        }

        // in order to not flag NCP, we don't want to reset our pitch after we have nothing to do, so do it every second. more legit
        if (_rotationResetTimer.passed(1000))
        {
            _rotations = null;
        }
    }

    @Subscribe
    public void onPacket(EventReadPacket event)
    {
        if (event.getPacket() instanceof PlaySoundS2CPacket)
        {
            PlaySoundS2CPacket packet = (PlaySoundS2CPacket) event.getPacket();

            if (mc.world == null)
                return;

            // we need to remove crystals on this packet, because the server sends packets too slow to remove them
            if (packet.getCategory() == SoundCategory.BLOCKS.BLOCKS && packet.getSound() == SoundEvents.ENTITY_GENERIC_EXPLODE)
            {
                ArrayList<Entity> entities = new ArrayList<Entity>();
                mc.world.getEntities().forEach(entities::add);
                // loadedEntityList is not thread safe, create a copy and iterate it
                entities.forEach(e ->
                {
                    // if it's an endercrystal, within 6 distance, set it to be dead
                    if (e instanceof EndCrystalEntity)
                        if (e.squaredDistanceTo(packet.getX(), packet.getY(), packet.getZ()) <= 6.0)
                            e.remove();

                    // remove all crystals within 6 blocks from the placed crystals list
                    _placedCrystals.removeIf(p_Pos -> p_Pos.getSquaredDistance((double) packet.getX(), (double) packet.getY(), (double) packet.getZ(), true) <= 6.0);
                });
            }
        }
    }

    public boolean NeedPause()
    {
        /// We need to pause if we have surround enabled, and don't have obsidian
        if (EntityUtils.IsEating()) {
            return true;
        }
        return false;
    }

    public String getTarget()
    {
        return _lastTarget;
    }
}
